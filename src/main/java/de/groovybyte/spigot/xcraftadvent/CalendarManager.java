package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.datechecker.IDateChecker;
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
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CalendarManager {

    private final Logger logger;

    public final CalendarEditor EDITOR;

    protected final File configFile, saveFile;
    protected final Map<UUID, Integer> data = Collections.synchronizedMap(new HashMap<>(128));
    protected final Map<UUID, PlayerCalendar> activePlayers = new HashMap<>(32);

    protected FileConfiguration config;
    protected final Door[] doors;

    public CalendarManager(
        IServiceProvider inject,
        File configFile,
        File saveFile
    ) {
        this.logger = inject.getService(Logger.class);
        this.configFile = configFile;
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
        long t1 = System.currentTimeMillis();
        data.clear();
        config = YamlConfiguration.loadConfiguration(configFile);
        for (int i = 0; i < 24; i++) {
            doors[i].update(config);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = br.readLine()) != null) {
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
        logger.log(Level.INFO, "Loaded (" + data.size() + " players) in " + (System.currentTimeMillis() - t1) + "ms");
        activePlayers.replaceAll((UUID uuid, PlayerCalendar cal) -> getNewCalendar(cal.getPlayer()));
        //in the case that there is a new player in the data, he will not be added to the active players - use rejoin
    }

    public void save() throws IOException {
        long t1 = System.currentTimeMillis();
        activePlayers.forEach((UUID pid, PlayerCalendar cal) -> data.put(pid, cal.getOpenStatus()));
        try (FileWriter fw = new FileWriter(saveFile, false)) {
            for (Map.Entry<UUID, Integer> entry : data.entrySet()) {
                fw.write(String.format("%s:%x\n",
                        entry.getKey().toString(),
                        entry.getValue()
                ));
            }
        }
        logger.log(Level.INFO, "Saved (" + data.size() + " players) in " + (System.currentTimeMillis() - t1) + "ms");
    }

    protected void silentSave() {
        plugin.runAsync(() -> {
            try {
                save();
            } catch (IOException e) {
            }
        });
    }

    public boolean isActive(Player p) {
        return activePlayers.containsKey(p.getUniqueId());
    }

    public PlayerCalendar get(Player p) {
        return activePlayers.get(p.getUniqueId());
    }
    
    public IDateChecker getDateChecker() {
        return plugin.getDateChecker();
    }

    public Optional<PlayerCalendar> connectCalendar(Player p) {
        if (getDateChecker().isCalendarTime()) {
            UUID pid = p.getUniqueId();
            if (activePlayers.containsKey(pid)) {
                return Optional.of(activePlayers.get(pid));
            } else if (data.containsKey(pid) || getDateChecker().canCreateNewCalendar()) {
                PlayerCalendar calendar = getNewCalendar(p);
                activePlayers.put(pid, calendar);
                return Optional.of(calendar);
            }
        }
        return Optional.empty();
    }

    public PlayerCalendar getNewCalendar(Player p) {
        return new PlayerCalendar(this, p, data.getOrDefault(p.getUniqueId(), 0));
    }

    public void disconnectCalendar(Player p) {
        UUID pid = p.getUniqueId();
        if (activePlayers.containsKey(pid)) {
            data.put(pid, activePlayers.remove(pid).getOpenStatus());
            silentSave();
        }
    }
}
