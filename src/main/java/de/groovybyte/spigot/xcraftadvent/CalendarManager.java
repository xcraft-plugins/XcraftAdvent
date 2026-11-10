package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.Door;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CalendarManager {

    public final CalendarEditor EDITOR;

    protected final XcraftAdvent plugin;
    protected final File cfgFile, saveFile;
    protected final Map<UUID, Integer> data = Collections.synchronizedMap(new HashMap<>(128));
    protected final Map<UUID, PlayerCalendar> activePlayers = new HashMap<>(32);

    protected FileConfiguration config;
    protected final Door[] doors;

    public CalendarManager(XcraftAdvent plugin, File cfgFile, File saveFile) {
        this.plugin = plugin;
        this.cfgFile = cfgFile;
        this.saveFile = saveFile;
        EDITOR = new CalendarEditor(plugin, this);

        doors = new Door[24];
        for (int day = 0; day < 24; day++) {
            doors[day] = new Door(plugin, day + 1);
        }
    }

    public Door getDoor(int day) {
        return doors[day - 1];
    }

    public void load() throws IOException {
        long startTime = System.currentTimeMillis();
        data.clear();
        config = YamlConfiguration.loadConfiguration(cfgFile);
        for (int index = 0; index < doors.length; index++) {
            doors[index].update(config);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int split = line.indexOf(':');
                if (line.isEmpty() || split == -1) {
                    continue;
                }
                data.put(
                        UUID.fromString(line.substring(0, split++)),
                        Integer.parseUnsignedInt(line.substring(split), 16)
                );
            }
        }
        plugin.getLogger().log(Level.INFO, "Loaded (" + data.size() + " players) in " + (System.currentTimeMillis() - startTime) + "ms");
        activePlayers.replaceAll((playerId, calendar) -> getNewCalendar(calendar.getPlayer()));
        //in the case that there is a new player in the data, he will not be added to the active players - use rejoin
    }

    public void save() throws IOException {
        long startTime = System.currentTimeMillis();
        activePlayers.forEach((playerId, calendar) -> data.put(playerId, calendar.getOpenStatus()));
        try (FileWriter writer = new FileWriter(saveFile, false)) {
            for (Map.Entry<UUID, Integer> entry : data.entrySet()) {
                writer.write(String.format("%s:%x\n",
                        entry.getKey().toString(),
                        entry.getValue()
                ));
            }
        }
        plugin.getLogger().log(Level.INFO, "Saved (" + data.size() + " players) in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Saves the player data in the background. Failures are ignored
     * on purpose: the next explicit save (e.g. on shutdown) retries
     * with the same in-memory data.
     */
    protected void silentSave() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                save();
            } catch (IOException exception) {
                // ignore: saving is best-effort here
            }
        });
    }

    public boolean isActive(Player player) {
        return activePlayers.containsKey(player.getUniqueId());
    }

    public PlayerCalendar get(Player player) {
        return activePlayers.get(player.getUniqueId());
    }

    public IDateChecker getDateChecker() {
        return plugin.getService(IDateChecker.class);
    }

    public Optional<PlayerCalendar> connectCalendar(Player player) {
        if (getDateChecker().isCalendarTime()) {
            UUID playerId = player.getUniqueId();
            if (activePlayers.containsKey(playerId)) {
                return Optional.of(activePlayers.get(playerId));
            } else if (data.containsKey(playerId) || getDateChecker().canCreateNewCalendar()) {
                PlayerCalendar calendar = getNewCalendar(player);
                activePlayers.put(playerId, calendar);
                return Optional.of(calendar);
            }
        }
        return Optional.empty();
    }

    public PlayerCalendar getNewCalendar(Player player) {
        return new PlayerCalendar(this, player, data.getOrDefault(player.getUniqueId(), 0));
    }

    /**
     * Removes the player's calendar from the active set and persists its
     * open status.
     *
     * @return the calendar that was removed, or {@code null} if the player
     *         had none
     */
    public PlayerCalendar disconnectCalendar(Player player) {
        UUID playerId = player.getUniqueId();
        if (activePlayers.containsKey(playerId)) {
            PlayerCalendar calendar = activePlayers.remove(playerId);
            data.put(playerId, calendar.getOpenStatus());
            silentSave();
            return calendar;
        }
        return null;
    }
}
