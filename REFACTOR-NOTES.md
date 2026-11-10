# XcraftAdvent — in-progress refactor notes

Status: 2026-11-10. The build configuration is fixed (repositories, version
catalog, JDK truststores) — `gradlew :dependencies` resolves cleanly. What
still blocks the build are **58 pre-existing compile errors** caused by the
half-finished repository/service refactor. Details below.

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
