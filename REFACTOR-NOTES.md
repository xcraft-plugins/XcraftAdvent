# XcraftAdvent — in-progress refactor notes

Status: 2026-11-10. **Refactor completed on branch `new`** — the build is
green (`gradlew build` + `spigotPlugin`, deprecation-free compile, tests
passing). The original problem analysis is preserved below; the resolution
log documents what was done and why.

## Resolution log (2026-11-10, branch `new`, after `5dcb861`)

| Commit | Note item | What was done |
|---|---|---|
| `6e698fe` | 1 | Pointed the stale `ICalendar` imports at `entity.ICalendar` in `repository/ICalendarRepository.java`, `repository/CalendarCache.java`, `repository/CalendarLoader.java`, `listener/ConnectionListener.java`. |
| `c64dbea` | 2 | **Decision: rewrite call sites instead of re-adding `XcraftAdvent` helpers** — keeps the service-lookup design. `plugin.log(...)` → `plugin.getLogger().log(...)` (`CalendarManager`, `CalendarEditor`, `entity/Door`), `plugin.message(...)` → `Messages.send(...)` (`entity/Door`, `PlayerCalendar.open`), `plugin.runAsync(...)` → `getScheduler().runTaskAsynchronously(plugin, ...)` (`CalendarManager.silentSave`), `plugin.getDateChecker()` → `getService(IDateChecker.class)` (`CalendarManager.getDateChecker`). |
| `6538b98` | 5, 6 | **`ICalendar` extended** with the door-interaction API (`isValidDoor`, `getClickedDay`, `isOpen`, `canOpen`, `open`) so `InventoryListener` can stay abstract — no cast to `PlayerCalendar` needed. `PlayerCalendar implements ICalendar` (also picked up the missing `Bukkit` import, item 6). `CalendarLoader` is now the real repository implementation backed by `CalendarManager`; `CalendarCache` remains a compiling stub. **Decision: load/unload run on the main thread via the scheduler, not the `ExecutorService`** — creating/mutating Bukkit inventories is not thread-safe, and `CalendarManager` holds everything in memory (no blocking IO), so the executor was removed. `CalendarManager.disconnectCalendar` now returns the unloaded calendar. `XcraftAdvent.onEnable` registers all services: `JavaPlugin`, `Logger`, `CalendarManager`, `ICalendarRepository` (`new CalendarLoader(this, calendarManager)`); added `registerService(...)` helper and `services.clear()` in `onDisable` (pattern taken, rewritten, from `feature/config-utils`). |
| `f652f30` | 3 | `CalendarCommandExecutor` now takes `(CalendarManager, JavaPlugin)` via constructor (built in `XcraftAdvent.onEnable`); `runSynced(...)` restored as scheduler delegation, `message(...)` delegates to `Messages.send`; `Permissions.OWN_AND_SHOW_CALENDAR` replaced by the `OWN_CALENDAR && SHOW_CALENDAR` check; the gutted `reload` sub-command restored (`calendarManager.load()` + log on error); dead `openCalendarView()` and the commented-out scheduler helpers removed. |
| `ea5f1f5` | 4 | `InventoryListener` finished: `instanceof Player` guard (the old code lost its negations mid-refactor — `isChestInventory`/`matches` guards restored to the pre-refactor semantics from `c178092`), `pc` resolved via `calendarRepository.getCalendar(...)`, scheduler via the `JavaPlugin` service, close handler typed via the `CalendarManager` service (was `CalendarManager man = calendarRepository`). `ConnectionListener`: null-guard in `checkForUnopenedDoors` (the load future completes with `null` outside advent time), dropped the unused `Logger` field and dead `throws IOException` (the repository API no longer declares checked exceptions — see below). |
| `be663dc` | — | **Deprecation fix (spigot 26.3):** `Messages` used `TextComponent.fromLegacyText(...)` which 26.3 deprecates in favour of `TextComponent.fromLegacy(...)` (single consolidated component). Compile is now warning-free with `-Xlint:deprecation`. |
| `081d0ce` | non-compile | **plugin.yml permissions synced** in `build.gradle.kts` (was out of sync with `Permissions.java`, incl. the `calender.ownAndShow` typo): `calendar.own` + `calendar.show` (both GRANT_PLAYER) + `calendar.doors.open` (GRANT_OP); `calendar.admin.showAny` dropped (commented out in `Permissions.java`). Command permission is `calendar.show` as the minimal gate — `CalendarCommandExecutor` enforces `own && show` at runtime. Test file rewritten for JUnit 5 (was JUnit 4 + Mockito, neither in the catalog) and `testImplementation(libs.spigot)` added — spigot is `compileOnly` for the jar, so tests could not even see the API. |
| `058850e` | — | **Shared constants class** `PluginConstants` (command label/aliases/description/usage + permission names/descriptions) lives in `buildSrc/src/main/java/...` so the root `build.gradle.kts` (build classpath) can reference it, and the root build adds that source dir to the main source set so the identical class is compiled into the plugin jar. `Permissions` and `XcraftAdvent.CALENDAR_COMMAND_LABEL` now delegate to it. buildSrc is pinned to a JDK 21 toolchain because buildSrc classes run on the Gradle daemon JVM (Java 21 here), while the plugin itself still targets JDK 25. |

While in the touched code, short/abbreviated identifiers were renamed to
readable names (e.g. `p`→`player`, `ev`→`event`, `iv`/`ei`→`*Inventory`,
`pc`→`calendar`, `man`→`calendarManager`, `t1`→`startTime`, `br`/`fw`→
`reader`/`writer`).

### Remaining / follow-up

- **Spigot 1.21 → 26.3 API review — done.** Result documented in
  `SPIGOT-API-CHANGES-1.21-TO-26.3.md`: nothing in the plugin's API usage
  is deprecated/removed in 26.3 except `TextComponent.fromLegacyText`
  (fixed in `be663dc`); `build.gradle.kts` updated to
  `apiVersion("26.3")` / `buildTools { versions("26.3") }`. A smoke test
  on a real 26.3 server is still recommended before release.
- **Branch cleanup:** feature branches `v2-rewrite`, `config-utils`,
  `layered-architecture`, `service-provider` (see overview in chat);
  nothing further needed from them.
- `view/ICalendarView` + `InventoryCalendarView` are still empty stubs
  (future view abstraction, left as-is).
- `PlayerCalendar.set(int, boolean)` is an unfinished bit-manipulation stub
  (left as-is).
- `feature/v2-rewrite` contains an unmerged **door-location feature**
  (physical chest locations per door, `rewards.doorN.locations`, open via
  block click) in decompiled form — candidate for a clean re-implementation
  on a feature branch if it is still wanted.

## The refactor in progress

The code is moving to a service-lookup + repository design:

- `IServiceProvider` — Guava `ClassToInstanceMap` service registry on the plugin
  class (`XcraftAdvent`); `IDateChecker` and `Logger` are registered as services.
- `ICalendarRepository` — new abstraction with two implementations:
  `CalendarCache` (stub) and `CalendarLoader` (async via `ExecutorService`).
- Classes were moved into sub-packages: `entity/`, `repository/`, `listener/`, `view/`.

Callers were not updated to the new design, and some helper methods were removed
or commented out along the way.

## Blocking compile errors (58)

### 1. Stale `ICalendar` imports — 14 errors

`ICalendar` now lives in `de.groovybyte.spigot.xcraftadvent.entity.ICalendar`,
but four files still import `de.groovybyte.spigot.xcraftadvent.ICalendar`:

| File | Import | Other affected lines |
|---|---|---|
| `repository/ICalendarRepository.java` | :3 | :10, :12, :14 |
| `repository/CalendarCache.java` | :3 | :10, :15 |
| `repository/CalendarLoader.java` | :4 | :23, :27, :32, :37 |
| `listener/ConnectionListener.java` | :3 | :29 |

Fix: point the import at `...xcraftadvent.entity.ICalendar` in all four files.

### 2. `XcraftAdvent` helper methods gone — 14 errors

Callers still use convenience methods that no longer exist on the plugin class
(only `getService(...)` / `getLogger()` remain):

| Missing member | Call sites |
|---|---|
| `log(Level, String)` | `CalendarEditor.java:91,107,150`, `CalendarManager.java:67,83`, `entity/Door.java:69,76`, `PlayerCalendar.java:101` |
| `message(Player, String)` | `entity/Door.java:85,90`, `PlayerCalendar.java:98` |
| `runAsync(Runnable)` | `CalendarManager.java:87` |
| `runSynced(Runnable)` | `listener/InventoryListener.java:63` (`plugin.runSynced`) |
| `getDateChecker()` | `CalendarManager.java:104` (plugin now exposes `IDateChecker` via `getService`) |

Decision needed: re-add these helpers on `XcraftAdvent`, or rewrite the call
sites to `getService(Logger.class).log(...)`, `getServer().getScheduler()`,
`getService(IDateChecker.class)`.

### 3. `CalendarCommandExecutor` is orphaned — 14 errors

`CalendarCommandExecutor.java`:

- Constructed with **no arguments** (`XcraftAdvent.java:68`), but the body uses
  `this.man` (a `CalendarManager`) at :32, :41, :44, :47, :88, :91.
- `runSynced(...)` at :32 and :91 — the `runAsync`/`runSynced`/`executeAsync`
  helpers are commented out at lines 17-27.
- `this.message(...)` at :53, :54, :89, :93, :96 — no such method.
- :87 references `Permissions.OWN_AND_SHOW_CALENDAR`, which no longer exists —
  `Permissions` now has `OWN_CALENDAR` and `SHOW_CALENDAR` separately.

Fix: inject `CalendarManager` (and plugin/service provider) via the
constructor, restore or delegate the scheduler/messaging helpers, and split the
permission check into `OWN_CALENDAR` + `SHOW_CALENDAR`.

### 4. `InventoryListener` mid-refactor — 14 errors

`listener/InventoryListener.java`:

- :38/:48 — `ev.getWhoClicked()` is a `HumanEntity`, but
  `ICalendarRepository.getCalendar` takes a `Player` → type mismatch
  (needs an `instanceof Player` guard/cast).
- :54-:73 — undefined variables: `p` (the Player), `pc` (the
  `PlayerCalendar`, presumably from the repository/manager), and `plugin`
  (needed for `plugin.runSynced` at :63). The constructor currently only
  resolves `Logger` and `ICalendarRepository` services.
- :81 — `CalendarManager man = calendarRepository;` — `calendarRepository` is
  an `ICalendarRepository`, not a `CalendarManager` (incompatible types).

### 5. `getCalendar(Player)` unimplemented — 2 errors (+1 latent)

`ICalendarRepository` declares `ICalendar getCalendar(Player)`, but neither
implementation provides it:

- `CalendarCache.java:7` — "not abstract and does not override"
- `CalendarLoader.java:11` — same

Latent (surfaces after the imports in item 1 are fixed):
`CalendarCache.loadCalendar` returns `ICalendar`, but the interface declares
`CompletableFuture<ICalendar>` — return type incompatibility.

### 6. Missing import — 1 error

- `PlayerCalendar.java:177` — `Bukkit.createInventory(...)` without
  `import org.bukkit.Bukkit;`.

## Non-compile inconsistencies (plugin.yml / permissions)

The `spigot { desc { ... } }` block in `build.gradle.kts` still declares the
**old** permission set, out of sync with `Permissions.java`:

| build.gradle.kts (old) | Permissions.java (new) |
|---|---|
| `calendar.ownAndShow` (command perm is `calender.ownAndShow` — typo) | `calendar.own` + `calendar.show` |
| `calendar.openDoors` | `calendar.doors.open` |
| `calendar.admin.showAny` | commented out ("fix: give item only in gm 0") |

Update the plugin.yml description to match once the refactor is decided.

## Suggested fix order

1. Fix the four stale `ICalendar` imports (item 1).
2. Decide the helper strategy for `XcraftAdvent` (item 2).
3. Implement `getCalendar` in both repositories; fix `CalendarCache`
   return types to `CompletableFuture<ICalendar>` (item 5).
4. Wire `CalendarCommandExecutor` dependencies + permission split (item 3).
5. Finish the `InventoryListener` refactor: Player guard, `pc`, `plugin`
   scheduler, close-handler typing (item 4).
6. Add the `Bukkit` import in `PlayerCalendar` (item 6).
7. Re-run `.\gradlew.bat :compileJava` (currently 58 errors), then sync the
   plugin.yml permissions (above).

## Build config — verified working, no further action

- Repositories: `mavenCentral()` + `hub.spigotmc.org` snapshots + `mavenLocal()`
  (Sonatype OSSRH is dead since 2025-06-30; the `net.md-5` bungeecord artifacts
  are on Maven Central).
- Versions: guava version-less (inherited from spigot-api, currently 33.6.0-jre);
  commons-lang3 pinned to 3.20.0 (spigot declares it `provided`, so it is
  non-transitive and must be kept in sync manually).
