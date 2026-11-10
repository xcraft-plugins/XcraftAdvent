# XcraftAdvent — refactor notes

## Current state (2026-11-10)

- The service-lookup + repository refactor is **complete** on branch `new`
  (10 commits on top of the old `main`, which was deleted after this work;
  nothing is pushed yet).
- Build is green: `gradlew build` + `spigotPlugin` — deprecation-free
  compile (`-Xlint:deprecation`), tests passing, jar contains `plugin.yml`
  and `PluginConstants.class`.
- `plugin.yml` (generated): permissions in sync with `Permissions.java`
  (`calendar.own`, `calendar.show` GRANT_PLAYER; `calendar.doors.open`
  GRANT_OP), command permission `calendar.show`, `api-version: 26.3`.
- Spigot dependency: `spigot-api 26.3-R0.1-SNAPSHOT`, JDK 25 toolchain
  (26.3 requires it). API review documented in
  `SPIGOT-API-CHANGES-1.21-TO-26.3.md`.
- Design in place: `IServiceProvider` (Guava `ClassToInstanceMap` on the
  `XcraftAdvent` plugin class) with `IDateChecker`, `Logger`, `JavaPlugin`,
  `CalendarManager` and `ICalendarRepository` registered;
  `CalendarLoader` is the active `ICalendarRepository` implementation
  (backed by `CalendarManager`, main-thread scheduling);
  `PlayerCalendar implements ICalendar`; command handling in
  `listener/CalendarCommandExecutor`.

## Resolution log (what was done and why)

| Commit | What was done |
|---|---|
| `6e698fe` | Pointed the stale `ICalendar` imports at `entity.ICalendar` in the four repository/listener files. |
| `c64dbea` | **Decision: rewrite call sites instead of re-adding `XcraftAdvent` helpers** — keeps the service-lookup design. `plugin.log(...)` → `plugin.getLogger().log(...)`, `plugin.message(...)` → `Messages.send(...)`, `plugin.runAsync(...)` → `getScheduler().runTaskAsynchronously(plugin, ...)` (`CalendarManager.silentSave`), `plugin.getDateChecker()` → `getService(IDateChecker.class)`. |
| `6538b98` | `ICalendar` extended with the door-interaction API (`isValidDoor`, `getClickedDay`, `isOpen`, `canOpen`, `open`) so `InventoryListener` stays abstract (no cast to `PlayerCalendar`); `PlayerCalendar implements ICalendar` (incl. the missing `Bukkit` import). `CalendarLoader` became the real repository implementation; `CalendarCache` remains a compiling stub. **Decision: load/unload run on the main thread via the scheduler, not the `ExecutorService`** — creating/mutating Bukkit inventories is not thread-safe and `CalendarManager` holds everything in memory (no blocking IO), so the executor was removed. `CalendarManager.disconnectCalendar` now returns the unloaded calendar. `XcraftAdvent.onEnable` registers all services; added `registerService(...)` helper and `services.clear()` in `onDisable` (pattern taken, rewritten, from `feature/config-utils`). |
| `f652f30` | `CalendarCommandExecutor` takes `(CalendarManager, JavaPlugin)` via constructor; `runSynced(...)` restored as scheduler delegation, `message(...)` delegates to `Messages.send`; `OWN_AND_SHOW_CALENDAR` replaced by the `OWN_CALENDAR && SHOW_CALENDAR` check; the gutted `reload` sub-command restored; dead code removed. |
| `ea5f1f5` | `InventoryListener` finished: `instanceof Player` guard, `pc` resolved via `calendarRepository.getCalendar(...)`, scheduler via the `JavaPlugin` service, close handler via the `CalendarManager` service. **The mid-refactor had lost two negations** (`isChestInventory`/`matches` guards) — restored to the pre-refactor semantics from `c178092`. `ConnectionListener`: null-guard in `checkForUnopenedDoors` (load future completes with `null` outside advent time), unused `Logger` field and dead `throws IOException` removed. |
| `be663dc` | Fixed the one deprecation in plugin code for 26.3: `TextComponent.fromLegacyText` → `TextComponent.fromLegacy` (see "Deprecated APIs" below). |
| `081d0ce` | plugin.yml permissions synced with `Permissions.java` (was out of sync, incl. the `calender.ownAndShow` typo); `calendar.admin.showAny` dropped. Test file rewritten for JUnit 5 (was JUnit 4 + Mockito, neither in the catalog) and `testImplementation(libs.spigot)` added — spigot is `compileOnly` for the jar, so tests could not even see the API. |
| `058850e` | Shared constants class `PluginConstants` (command label/aliases/description/usage + permission names/descriptions) lives in `buildSrc/src/main/java/...` so the root `build.gradle.kts` (build classpath) can reference it, and the root build adds that source dir to the main source set so the identical class is compiled into the plugin jar. `Permissions` and `XcraftAdvent.CALENDAR_COMMAND_LABEL` delegate to it. buildSrc is pinned to a JDK 21 toolchain because buildSrc classes run on the Gradle daemon JVM (Java 21 here), while the plugin itself targets JDK 25. |
| `c9b6ed1` | `apiVersion("26.3")` / `buildTools { versions("26.3") }` in `build.gradle.kts` + `SPIGOT-API-CHANGES-1.21-TO-26.3.md`. |

While in the touched code, short/abbreviated identifiers were renamed to
readable names (e.g. `p`→`player`, `ev`→`event`, `iv`/`ei`→`*Inventory`,
`pc`→`calendar`, `man`→`calendarManager`, `t1`→`startTime`, `br`/`fw`→
`reader`/`writer`).

## Branches kept (state)

All other branches (`main`, `master`, `feature/service-provider`,
`feature/layered-architecture`) were deleted on 2026-11-10 — their content
is fully contained in `new` or obsolete (Maven era). Remote copies still
exist; `origin/HEAD` still points at `origin/master` (see upcoming steps).

### `feature/config-utils` (`590ce29`, 2024-12-02)

Hand-written continuation of the **same** layered design (not decompiled).
The branch itself does **not compile**: `XcraftAdvent.onEnable` calls
`new CalendarManager(this)` while that branch's `CalendarManager`
constructor still takes `(IServiceProvider, File, File)`; its
`InventoryListener` still has the undefined-`p`/`pc` errors.

- **Pulled into `new`** (rewritten, commit `6538b98`): the
  `registerService(Class, S)` helper, registering `JavaPlugin.class` as a
  service (needed for scheduler access in listeners), and
  `services.clear()` in `onDisable`.
- **Not pulled** (left there): `config/Config.java` (half-finished: creates
  the data folder, holds `calendar.cfg`/`players.data` paths in private
  fields without getters — no consumer), `util/BlockLocation.java` (clean
  record `(World, x, y, z)` with `fromLocation`/`toLocation` — currently
  unused), the `datechecker/` package split of the `IDateChecker`
  implementations, and a German `README.md` describing the intended
  physical-chest gameplay (see `feature/v2-rewrite` for that feature).

### `feature/v2-rewrite` (`ea01972`, 2024-12-12)

A full rewrite that **flattened the package structure** (all classes in
`xcraftadvent`, `PluginMain`/`Commands` extraction) — the opposite
direction of the layered design this project settled on, and older than
the refactor notes. It was the wrong branch to rebase onto.

- **Partially decompiled** — do not merge as-is: `CalendarManager` has
  decompiler artifacts (`var7`/`var9`/`var10`, `indexOf(58)` instead of
  `':'`, try-with-resources unrolled into nested try/catch with
  `addSuppressed`). Other files (`XcraftAdvent`, `Commands`) look
  hand-written.
- **Old build config**: no version catalog, no fixed repositories — the
  build fixes on `new` supersede it.
- **The one unmerged feature**: *physical door locations* — a
  `BlockLocation` per world block, `rewards.doorN.locations` config lists
  loaded into a `doorLocations` map in `CalendarManager`
  (`getDoorByLocation`), and handlers `onPlayerClickedDoorBlock` /
  `onPlayerOpenedDoorContainer` that open a door when a player interacts
  with the chest at that location (plus a `Consumer<Runnable>` scheduler
  injection, `scheduleTaskNext`/`scheduleTaskSoon`). If this feature is
  wanted, re-implement it **cleanly on a new feature branch off `new`**
  (e.g. `feature/door-locations`), then delete this branch.

## Deprecated APIs found (26.3) and replacements

### Affects plugin code (fixed)

| Deprecated | Replacement | Where |
|---|---|---|
| `net.md_5.bungee.api.chat.TextComponent.fromLegacyText(String)` — returns a non-standard `BaseComponent[]` | `TextComponent.fromLegacy(String)` — consolidates into a single `BaseComponent` | `Messages.java`, fixed in `be663dc` |

### Present in the 26.3 API, not used by this plugin (verified in the `spigot-api-26.3` sources)

| Deprecated | Replacement |
|---|---|
| `Entity#isOnGround` (since 1.16.1 — client-controlled, unreliable/spoofable) | none official; use position/physics-derived state |
| `ItemStack#setDurability`/`getDurability` (since 1.13) | `((Damageable) item.getItemMeta()).setDamage(int)` / `getDamage()` |
| `Player#hidePlayer(Player)`/`showPlayer(Player)` (since 1.12.2) | `Player#hidePlayer(Plugin, Player)` / `showPlayer(Plugin, Player)` |
| `Player#playEffect` (spigot effects) | none in Bukkit — effects are data-driven now |
| `Player#sendTitle`/`sendSubtitle` | none in Bukkit (Paper: Adventure title API) |
| `Player#getBedSpawnLocation`/`setBedSpawnLocation` (since 1.20.4, misleading name — also covers respawn anchors) | `Player#getRespawnLocation` / `setRespawnLocation` |
| `Server#getMap(int)` (legacy `Map`/`MapView` handling) | the `MapView` API |
| `Server#banIP(String)` | `Server#banIP(String, String reason)` |
| `World#isGameRule(String)`/`getGameRules()` (string-based) | `World#getGameRule(GameRule)` / the `GameRule` registry |
| `World` legacy chunk/biome/spawn methods (`regenerateChunk`, `getBiome(int,int)`, `getTicksPer*Spawns`, spawn-limit setters, …) | the chunk API / vanilla-removed spawn limiters |
| `GameMode#getValue()` | `GameMode#ordinal()` |
| `InventoryClickEvent#setCursor` | `InventoryView#setCursor` |
| `BukkitScheduler#schedule*Task` overloads and the `runTask*(Plugin, BukkitRunnable)` variants | the `Runnable` overloads (which is what this plugin uses) |
| `net.md_5.bungee.api.ChatColor#valueOf/values/name/ordinal` (bungee `ChatColor` is a class, not an enum) | use the static color constants directly |

### Announced in release notes (26.1–26.3), not used by this plugin

- 26.3: `PlayerAnimationEvent` → `PlayerArmSwingEvent`;
  `PotionEffectTypeCategory` → `PotionEffectType.Category`;
  `PotionBrewer` (brewing is data-driven now, see `BrewingRecipe`).
- 26.2: `Vex#getSummoner` → `Vex#getOwner`;
  `World#setSpawnFlags`/`getAllowAnimals` (vanilla dropped the animal
  spawn toggle); Pointed Dripstone block data → new `Speleothem`.
- 26.1: unobfuscated server jars (remapper dropped) — only relevant for
  NMS/reflection.

Full context in `SPIGOT-API-CHANGES-1.21-TO-26.3.md`.

## Upcoming steps / TODO

- **TODO (think about it):** async loading of the player calendar.
  `CalendarLoader.loadCalendar` currently schedules
  `CalendarManager.connectCalendar` on the main thread (next tick), so the
  returned future completes there and `thenAccept` callbacks
  (`ConnectionListener.checkForUnopenedDoors` → `performCommand`) are
  main-thread safe. To actually go async, only the state preparation
  (data lookup, door status) could move to a worker thread — but the
  `PlayerCalendar` constructor creates its `Inventory` and
  `generateCalendar()` fills it, so **inventory creation/mutation must be
  synchronised back onto the Bukkit main thread** (e.g. via
  `BukkitScheduler#runTask`) before the future is completed. Only worth
  doing if join-time work ever becomes measurably heavy; today it is a
  small in-memory map lookup plus one 54-slot inventory fill.
- Smoke test on a real 26.3 Spigot server: join → calendar opens → door
  opens → rewards drop; confirm the server accepts `api-version: '26.3'`
  and that `buildTools { versions("26.3") }` resolves.
- Decide on `feature/door-locations`: re-implement the physical-chest
  feature from `feature/v2-rewrite` cleanly (or drop it and delete both
  kept branches).
- Remote cleanup: delete the stale remote branches
  (`origin/main`, `origin/master`, `origin/feature/service-provider`,
  `origin/feature/layered-architecture`,
  `origin/dependabot/maven/com.google.guava-guava-32.0.0-jre`) and
  re-point `origin/HEAD` (currently `origin/master`) — then push `new`
  (possibly renamed to `main`).
- Optional future work (stub state kept as-is): `view/ICalendarView` +
  `InventoryCalendarView` (view abstraction), `repository/CalendarCache`
  (caching repository), `PlayerCalendar.set(int, boolean)` (bit-op stub).

## Build config — verified working, no further action

- Repositories: `mavenCentral()` + `hub.spigotmc.org` snapshots +
  `mavenLocal()` (Sonatype OSSRH is dead since 2025-06-30; the `net.md-5`
  bungeecord artifacts are on Maven Central).
- Versions: guava version-less (inherited from spigot-api, currently
  33.6.0-jre); commons-lang3 pinned to 3.20.0 (spigot declares it
  `provided`, so it is non-transitive and must be kept in sync manually).
