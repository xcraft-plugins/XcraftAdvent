package de.groovybyte.spigot.xcraftadvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CalendarManager {

   public final CalendarEditor EDITOR;

   private final XcraftAdvent plugin;

   private final File cfgFile;
   private final File saveFile;

   private final Map<UUID, Integer> data = Collections.synchronizedMap(new HashMap<>(128));
   private final Map<UUID, PlayerCalendar> activePlayers = new HashMap<>(32);
   private final Map<BlockLocation, Integer> doorLocations = new HashMap<>(24);

   private FileConfiguration config;
   private final Door[] doors;

   public CalendarManager(XcraftAdvent plugin, File cfgFile, File saveFile) {
      this.plugin = plugin;
      this.cfgFile = cfgFile;
      this.saveFile = saveFile;
      this.EDITOR = new CalendarEditor(plugin, this);

      this.doors = new Door[24];
      for(int day = 0; day < 24; ++day) {
         this.doors[day] = new Door(plugin, day + 1);
      }

   }

   public Door getDoor(int day) {
      return this.doors[day - 1];
   }

   public Door getDoorByLocation(BlockLocation location) {
      return !this.doorLocations.containsKey(location) ? null : this.getDoor((Integer)this.doorLocations.get(location));
   }

   public void load() throws IOException {
      data.clear();
      long t1 = System.currentTimeMillis();
      config = YamlConfiguration.loadConfiguration(cfgFile);
      doorLocations.clear();
      int nonEmptyDoors = 0;

      for(int day = 1; day <= 24; ++day) {
         Door door = doors[day - 1];
         door.update(config);
         List<Location> locations = (List<Location>) config.getList("rewards.door" + day + ".locations");
         if (locations != null) {
            Iterator var7 = locations.iterator();

            while(var7.hasNext()) {
               Location location = (Location)var7.next();
               doorLocations.put(BlockLocation.fromLocation(location), day);
            }
         }

         if (!door.isEmpty() || locations != null) {
            ++nonEmptyDoors;
         }
      }

      plugin.getLogger().log(Level.INFO, "Loaded (" + nonEmptyDoors + " doors with " + doorLocations.size() + " locations) in " + (System.currentTimeMillis() - t1) + "ms");
      t1 = System.currentTimeMillis();
      BufferedReader br = new BufferedReader(new FileReader(this.saveFile));

      String line;
      try {
         while((line = br.readLine()) != null) {
            int split = line.indexOf(58);
            if (!line.isEmpty() && split != -1) {
               data.put(UUID.fromString(line.substring(0, split++)), Integer.parseUnsignedInt(line.substring(split), 16));
            }
         }
      } catch (Throwable var10) {
         try {
            br.close();
         } catch (Throwable var9) {
            var10.addSuppressed(var9);
         }
         throw var10;
      }
      br.close();

      plugin.getLogger().log(Level.INFO, "Loaded (" + data.size() + " players) in " + (System.currentTimeMillis() - t1) + "ms");
      activePlayers.replaceAll((uuid, cal) -> getNewCalendar(cal.getPlayer()));
   }

   public void save() throws IOException {
      long t1 = System.currentTimeMillis();
      this.activePlayers.forEach((pid, cal) -> {
         this.data.put(pid, cal.getOpenStatus());
      });
      FileWriter fw = new FileWriter(saveFile, false);

      try {
         Iterator var4 = data.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<UUID, Integer> entry = (Entry)var4.next();
            fw.write(String.format("%s:%x\n", (entry.getKey()).toString(), entry.getValue()));
         }
      } catch (Throwable var7) {
         try {
            fw.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }
         throw var7;
      }
      fw.close();

      plugin.getLogger().log(Level.INFO, "Saved (" + data.size() + " players) in " + (System.currentTimeMillis() - t1) + "ms");
   }

   protected void silentSave() {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
         try {
            this.save();
         } catch (IOException var2) {
         }

      });
   }

   public boolean isActive(Player p) {
      return this.activePlayers.containsKey(p.getUniqueId());
   }

   public PlayerCalendar get(Player p) {
      return this.activePlayers.get(p.getUniqueId());
   }

   public Optional<PlayerCalendar> playerJoin(Player p) {
      if (plugin.dateChecker.isCalendarTime()) {
         UUID pid = p.getUniqueId();
         plugin.getLogger().info("Player " + p.getName() + " (" + pid + ") joined - inData=" +
             this.data.containsKey(pid) + ", " +
             "canCreateNewCalendar=" + this.plugin.dateChecker.canCreateNewCalendar());
         if (this.activePlayers.containsKey(pid)) {
            return Optional.of(this.activePlayers.get(pid));
         }

         if (this.data.containsKey(pid) || plugin.dateChecker.canCreateNewCalendar()) {
            PlayerCalendar calendar = this.getNewCalendar(p);
            this.activePlayers.put(pid, calendar);
            return Optional.of(calendar);
         }
      }

      return Optional.empty();
   }

   public PlayerCalendar getNewCalendar(Player p) {
      return new PlayerCalendar(this, p, this.data.getOrDefault(p.getUniqueId(), 0));
   }

   public void playerQuit(Player p) {
      UUID pid = p.getUniqueId();
      if (this.activePlayers.containsKey(pid)) {
         this.data.put(pid, (this.activePlayers.remove(pid)).getOpenStatus());
         this.silentSave();
      }

   }
}
