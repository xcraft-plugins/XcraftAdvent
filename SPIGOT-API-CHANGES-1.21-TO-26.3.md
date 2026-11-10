# Spigot API changes: 1.21 → 26.3

Overview of what changed between the last `1.21.x` releases and Spigot
`26.3-R0.1-SNAPSHOT` (the version this project compiles against), with a
focus on what affects **this plugin**.

Research date: 2026-11-10. Sources: SpigotMC news/announcements forum
(login-walled; read where excerpts were available), PaperMC news posts
(`papermc.io/news/26-1`, `/26-2`, `/26-3` — Paper is Bukkit-based, so its
Bukkit-level API notes apply to Spigot too, Paper-specific items are marked
as such), the PaperMC `plugin.yml` docs, and — most authoritatively — a
direct scan of the `spigot-api-26.3-R0.1-SNAPSHOT` sources jar in the local
Gradle cache.

## 1. The versioning change (the big one)

Mojang abandoned the `1.x` numbering. Versions are now
`<year>.<drop>[-.patch]`, e.g. `26.1.2` = third patch of the first 2026
drop. The `1.21.x` line ended with **1.21.11 (Dec 2025)**; the 26.x line
followed:

| Version | Released | Content |
|---|---|---|
| 26.1 / 26.1.1 / 26.1.2 | Mar 24, 2026 (26.1.2 Apr 9 supersedes both) | first 26.x drop |
| 26.2 | Jun 16, 2026 | "Sulfur" content (Sulfur Caves, Sulfur Cube mob, …) |
| 26.3 | Sep 15, 2026 | "Wilderness Bound", many new block/item variants |

Consequences:

- **Java:** Minecraft 26.3 requires **JDK 25** (matches the project's
  `languageVersion = JavaLanguageVersion.of(25)` toolchain).
- **Spigot Maven artifacts** still use the `-R0.1-SNAPSHOT` suffix
  (`org.spigotmc:spigot-api:26.3-R0.1-SNAPSHOT` — what this project uses).
  Paper separately moved to a `.build.N` suffix (e.g.
  `26.1.2.build.23-alpha`); that does not affect Spigot consumers.
- **`api-version` in plugin.yml:** valid values are `1.13` up to the
  running server's version (Bukkit mechanism, per the PaperMC plugin.yml
  docs: "The valid versions are 1.13 - 26.3"). Unknown/future values
  prevent the plugin from enabling; older values load but opt out of
  backwards-compat handling. → **Action taken:** `build.gradle.kts`
  updated from `apiVersion("1.21")` to `apiVersion("26.3")`, and
  `buildTools { versions("26.3") }`.

## 2. Per-release API changes

### 1.21.11 → 26.1 (Mar 2026)

- **Unobfuscated server jars:** Mojang no longer ships obfuscated server
  jars; the internal remapper was dropped. Only affects NMS/reflection
  users — *not this plugin*.
- **World storage** reworked closer to vanilla (server side; Paper notes
  world-data migration for existing worlds).
- Compression-format setting merged into vanilla
  `server.properties` (server side).
- `ClockTimeSkipEvent` added as parent of `TimeSkipEvent` (Paper).

### 26.1 → 26.2 (Jun 2026)

- **Beds are regular blocks again** — they no longer hold a
  `PersistentDataContainer`; data must be salvaged via
  `AsyncServerDataFixerRemoveBlockEntityEvent`. Not used by this plugin.
- Cube mobs share a new parent interface `AbstractCubeMob`;
  `MagmaCube` no longer extends `Slime`; `SlimeSplitEvent#getEntity`
  now returns `AbstractCubeMob`.
- Deprecations: `Vex#getSummoner` → `getOwner`;
  `World#setSpawnFlags`/`getAllowAnimals` (vanilla dropped the animal
  spawn toggle); Pointed Dripstone block data → new `Speleothem`.
- `EntityIgniteEvent` added as parent of `CreeperIgniteEvent`.
- Paper only: Adventure 5 shipped, removing previously deprecated
  Adventure API (`BookMeta` no longer extends Adventure `Book`,
  deprecated `ClickEvent`/`HoverEvent` overloads gone). **Spigot does not
  bundle Adventure** — its chat API is still BungeeCord chat components
  (`net.md_5.bungee.api.chat`), which is what this plugin uses.

### 26.2 → 26.3 (Sep 2026)

- **Data component API graduated:** most of it lost its
  `@Experimental` annotations. The classic `ItemMeta` API (used by this
  plugin) remains fully available.
- New/consolidated packages: `org.bukkit.inventory.meta.tags` (custom
  item data), `org.bukkit.inventory.view` + `…view.builder`
  (`InventoryView` subtypes and builders, plus the new `MenuType` class
  in `org.bukkit.inventory`), `org.bukkit.inventory.recipe`,
  `org.bukkit.damage`, `org.bukkit.enchantments`,
  `org.bukkit.entity.memory`, `org.bukkit.entity.boat`.
- New events/API: `Player#postEffects`, `EntityBreakEvent` /
  `EntityBreakByEntityEvent` (cushions), `EntityConstructEvent`,
  `EntityCollideWithEntityEvent`, `SulfurCube#swallow` +
  `SulfurCubeSwallowItemEvent`, `CreakingHeart` now contains/spawns its
  `Creaking` protector, `PlayerCodeOfConductSendEvent`,
  `Entity#getRemovalReason` + `RemovalReason`.
- Deprecations: `PlayerAnimationEvent` → `PlayerArmSwingEvent`;
  `PotionEffectTypeCategory` → `PotionEffectType.Category`;
  `PotionBrewer` (brewing is data-driven now, see `BrewingRecipe`).

*Release notes marked "Paper" above come from PaperMC's announcements;
Spigot's own first posts are login-walled, so Spigot-only deltas beyond
the API jar itself could not be read.*

## 3. Impact scan for this plugin (26.3)

Method-by-method check of everything the plugin calls, against the
`spigot-api-26.3` and `bungeecord-chat-1.21-R0.4` sources (authoritative),
plus a `-Xlint:deprecation` compile (which must be clean):

| Used API | Status in 26.3 |
|---|---|
| `Bukkit.createInventory`, `Bukkit.getItemFactory` (indirect) | ok |
| `Inventory` (`setMaxStackSize`, `setStorageContents`, `setItem(s)`, `getContents`, `getStorageContents`, `equals`) | ok |
| `ItemStack` (ctor, `getType`, `hasItemMeta`, `getItemMeta`, `setItemMeta`, `isSimilar`, `setType`, `getAmount`) | ok (`setDurability`/`getDurability` are deprecated since 1.13 but unused here) |
| `ItemMeta` (`setLore`, `setDisplayName`) | ok |
| `InventoryType.CHEST` — note: the enum lives in **`org.bukkit.event.inventory`** (the plugin's import is already correct) | ok |
| `Player` (`getUniqueId`, `getName`, `getGameMode`, `performCommand`, `getInventory`, `getWorld`, `getLocation`, `getEyeLocation`, `playSound`, `openInventory`, `hasPermission`, `sendMessage`) | ok (deprecated Player methods — `playEffect`, `hidePlayer`, `sendTitle`, bed spawn, … — are all unused) |
| `CommandSender` (`sendMessage`, `spigot()`) | ok — Spigot keeps BungeeCord chat first-class, no deprecation |
| `TextComponent.fromLegacy` (after fix) / `BaseComponent` | ok — **`fromLegacyText` is deprecated in 26.3** and was replaced in `Messages.java` (commit `be663dc`) |
| `BukkitScheduler.runTask` / `runTaskAsynchronously` (Runnable overloads) | ok (only the `BukkitRunnable`/`schedule*` overloads are deprecated, unused) |
| `InventoryClickEvent` / `InventoryCloseEvent` getters | ok (only `setCursor` is deprecated, unused) |
| `Configuration`/`FileConfiguration`/`YamlConfiguration` (`contains`, `getItemStack`, `getStringList`, `set`, `save`, `loadConfiguration`) | ok |
| `Server` (`dispatchCommand`, `getConsoleSender`, `getScheduler`, `getPluginManager`) | ok |
| `World.dropItemNaturally`, `Location`, `GameMode`, `ChatColor`, `Sound.ENTITY_PLAYER_LEVELUP`, `Material` values used | ok |
| `JavaPlugin` (`getLogger`, `getServer`, `getDataFolder`, `getCommand`) | ok |

**Result: no further code changes were required for 26.3** beyond the
already-applied `fromLegacyText` → `fromLegacy` fix; the build compiles
without any deprecation warnings.

## 4. Things to verify on a real server

- That a 26.3 Spigot server accepts `api-version: '26.3'` (it should per
  the documented range; Spigot's own wiki page still documents the old
  list and lags behind).
- That `buildTools { versions("26.3") }` resolves via BuildTools on the
  next `server/` setup.
- Behavioural: the plugin's logic (inventory clicking, door rewards,
  YAML config) uses only long-stable API, but a smoke test on a 26.3
  server (join → calendar opens → door opens → rewards) is still worth
  doing before release.
