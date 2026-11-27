# Changelog:

## todo:
 - player status checking/listing
 - implement `ENABLE_COMMAND_CALENDAR_ANNOUNCEMENT` flag: when enabled, `/advent` should display a calendar announcement message (e.g., current date, doors remaining) instead of opening the inventory GUI — currently hardcoded to `false` in the JAR, not implemented in the refactored code
 - implement `ENABLE_COMMAND_CALENDAR_VIEW` flag: when enabled, `/advent` should use an alternative calendar view implementation (e.g., a separate view layer abstracted from the inventory GUI) — currently hardcoded to `false` in the JAR, not implemented in the refactored code; `ICalendarView` / `InventoryCalendarView` stubs exist in the refactored code as placeholders for this feature
 - implement `CalendarCache`: a caching layer for `ICalendarRepository` that caches loaded player calendars to avoid redundant disk I/O — currently a stub throwing `UnsupportedOperationException`; the JAR had no equivalent, this is a planned refactoring feature

## 2.1.0
- new command `edit <day> additem` for adding hold in hand item to the day
- new command `edit <day> clearitems` for clearing all items of a day
- new command `last` to display the oldest unopened day and number of unopened doors (or if all are opened an alternative message)
- on login messages the oldest unopened day and number of unopened doors if there are any unopened days

## 2.0.0 (refactored)
- refactor: split monolithic `XcraftAdvent` class into separate listeners (`CalendarCommandExecutor`, `ConnectionListener`, `InventoryListener`)
- refactor: introduced `IServiceProvider` with Guava `ClassToInstanceMap` for dependency injection
- refactor: introduced `ICalendar` interface for calendar operations
- refactor: introduced `ICalendarRepository` interface with `CalendarLoader` implementation for async calendar loading
- refactor: introduced `PluginConstants` in `buildSrc` as single source of truth for command labels, aliases, and permission names
- refactor: updated Spigot API from `api-version: 1.17` to `api-version: 26.3` (JDK 25)
- restore: physical door locations in the world (`BlockLocation` feature) — players can now interact with registered storage containers via right-click or opening them
- restore: editor GUI click-to-navigate between days (was commented out)
- restore: `addlocation`/`clearlocations` subcommands for `/advent edit`
- fix: opened door display item is now updated in the calendar inventory slot after opening (matches JAR behavior)
- add: `ICalendarView` / `InventoryCalendarView` stubs for future view abstraction (not in JAR)
- add: `CalendarCache` stub for future caching repository (not in JAR)

## 1.5.0
- move project to Gradle from Maven

## 1.4.0
- fixed maven project

## 1.3.1
- added operation delay debug messages (save/load)

## 1.3.0
- added command "/advent edit <day> test"

## 1.2.0
- maven projekt now
- bugfix: after midnight the players can open doors multiple times

## 1.1.0
- enhancement: calendar will only show on join if there is a door to open
- enhancement: calendar will only show if in survival gamemode
- enhancement: console can now use /advent reload
- enhancement: new players can't open the calendar after the 24th, players with calendar can't open doors anymore after the 31th
- change: saving a single players status won't update every others status anymore
- bugfix: NPE after /advent reload

## 1.0.1
- change: changed name from XCraftAdvent to XcraftAdvent
- bugfix: clicking on opened door won't show error before rejoin (calendar reload)
- bugfix: date won't be updated at 0:00 (calendar instance has fixed time)

## 1.0.0
- initial plugin
