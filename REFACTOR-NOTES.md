# XcraftAdvent — refactor notes

## Current state (2026-11-19)

- Layered architecture on branch `main`: `IServiceProvider` (Guava
  `ClassToInstanceMap`), `ICalendarRepository`/`CalendarLoader`,
  `PlayerCalendar implements ICalendar`, dedicated listeners
  (`InventoryListener`, `ConnectionListener`, `DoorLocationListener`),
  command handling in `CalendarCommandExecutor`.
- Build is green: `gradlew build` + `spigotPlugin` — deprecation-free
  compile, tests passing, jar contains `plugin.yml` and `PluginConstants.class`.
- `plugin.yml` (generated): `calendar.own` / `calendar.show` GRANT_PLAYER,
  `calendar.doors.open` GRANT_OP, `calendar.ownAndShow` NONE (backward compat),
  command permission `calendar.ownAndShow`, `api-version: 26.3`.
- Spigot dependency: `spigot-api 26.3-R0.1-SNAPSHOT`, JDK 25 toolchain.
- `PluginConstants` duplicated: runtime copy in `src/main/java`, build-time
  copy in `buildSrc` (kept in sync manually).
- Permission model: `calendar.ownAndShow` acts as an alternative to
  `calendar.own` AND `calendar.show` via `Permissions.hasCalendarAccess()`.
- Physical door-locations feature: `BlockLocation`, `doorLocations` map,
  `DoorLocationListener`, `edit [day] addlocation/clearlocations` OP commands.
- Editor GUI click-to-navigate between days: restored and working.
- `PlayerCalendar.open()`: inventory slot updated after door opening (matches alpha2 JAR behavior).

## Resolution log (summary)

The refactor migrated from a monolithic `XcraftAdvent` class to a layered
architecture with `IServiceProvider`, dedicated listeners, repository pattern,
and view abstractions. Key decisions: service-lookup over direct field access,
main-thread scheduling for Bukkit inventory safety, `TextComponent.fromLegacy`
over deprecated `fromLegacyText`, split permissions (`calendar.own` +
`calendar.show`) with `calendar.ownAndShow` as backward-compat alternative.
Identifier cleanup (`p`→`player`, `man`→`calendarManager`, etc.) applied
throughout. Full commit history available via `git log`.

## Legacy branches

`feature/config-utils` and `feature/v2-rewrite` are kept locally as reference
but should be deleted after server testing. `feature/v2-rewrite` provided the
reference for the physical door-locations re-implementation. Other branches
(`main`, `master`, `feature/service-provider`, `feature/layered-architecture`)
were deleted — content fully incorporated.

## Deprecated APIs (26.3)

Only `TextComponent.fromLegacyText` affected the plugin (fixed in `Messages.java`).
Full API review in `SPIGOT-API-CHANGES-1.21-TO-26.3.md`.

## Upcoming steps / TODO

- **Required:** Smoke test on a real 26.3 Spigot server: join → calendar opens
  → door opens → rewards drop; confirm `api-version: '26.3'` works.
- **Required:** Remote cleanup — delete stale remote branches and re-point
  `origin/HEAD`, then push `main`.
- **Optional future work:** implement `ENABLE_COMMAND_CALENDAR_ANNOUNCEMENT` flag
  (TODO in `XcraftAdvent.java`), implement `ENABLE_COMMAND_CALENDAR_VIEW` flag
  with `ICalendarView`/`InventoryCalendarView` stubs, wire up `CalendarCache`
  as a caching repository layer.

## Build config — verified working

- Repositories: `mavenCentral()` + `hub.spigotmc.org` snapshots + `mavenLocal()`.
- Versions: guava inherited from spigot-api (33.6.0-jre); commons-lang3
  pinned to 3.20.0 (manual sync required).
