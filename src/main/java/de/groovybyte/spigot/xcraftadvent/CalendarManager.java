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
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CalendarManager {
   public final CalendarEditor EDITOR;
   protected final XcraftAdvent plugin;
   protected final File cfgFile;
   protected final File saveFile;
   protected final Map<UUID, Integer> data = Collections.synchronizedMap(new HashMap<>(128));
   protected final Map<UUID, PlayerCalendar> activePlayers = new HashMap<>(32);
   protected final Map<BlockLocation, Integer> doorLocations = new HashMap<>(24);
   protected FileConfiguration config;
   protected final Door[] doors;

   public CalendarManager(XcraftAdvent plugin, File cfgFile, File saveFile) {
      this.plugin = plugin;
      this.cfgFile = cfgFile;
      this.saveFile = saveFile;
      this.EDITOR = new CalendarEditor(plugin, this);
      this.doors = new Door[24];

      for(int day = 0; day < 24; ++day) {
         this.doors[day] = new Door(this.plugin, day + 1);
      }

   }

   public Door getDoor(int day) {
      return this.doors[day - 1];
   }

   public Door getDoorByLocation(BlockLocation location) {
      return !this.doorLocations.containsKey(location) ? null : this.getDoor((Integer)this.doorLocations.get(location));
   }

   public void load() throws IOException {
      this.data.clear();
      long t1 = System.currentTimeMillis();
      this.config = YamlConfiguration.loadConfiguration(this.cfgFile);
      this.doorLocations.clear();
      int nonEmptyDoors = 0;

      for(int day = 1; day <= 24; ++day) {
         Door door = this.doors[day - 1];
         door.update(this.config);
         List<Location> locations = (List<Location>) this.config.getList("rewards.door" + day + ".locations");
         if (locations != null) {
            Iterator var7 = locations.iterator();

            while(var7.hasNext()) {
               Location location = (Location)var7.next();
               this.doorLocations.put(BlockLocation.fromLocation(location), day);
            }
         }

         if (!door.isEmpty() || locations != null) {
            ++nonEmptyDoors;
         }
      }

      this.plugin.log(Level.INFO, "Loaded (" + nonEmptyDoors + " doors with " + this.doorLocations.size() + " locations) in " + (System.currentTimeMillis() - t1) + "ms");
      t1 = System.currentTimeMillis();
      BufferedReader br = new BufferedReader(new FileReader(this.saveFile));

      String line;
      try {
         while((line = br.readLine()) != null) {
            int split = line.indexOf(58);
            if (!line.isEmpty() && split != -1) {
               this.data.put(UUID.fromString(line.substring(0, split++)), Integer.parseUnsignedInt(line.substring(split), 16));
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
      XcraftAdvent var10000 = this.plugin;
      Level var10001 = Level.INFO;
      int var10002 = this.data.size();
      var10000.log(var10001, "Loaded (" + var10002 + " players) in " + (System.currentTimeMillis() - t1) + "ms");
      this.activePlayers.replaceAll((uuid, cal) -> {
         return this.getNewCalendar(cal.getPlayer());
      });
   }

   public void save() throws IOException {
      long t1 = System.currentTimeMillis();
      this.activePlayers.forEach((pid, cal) -> {
         this.data.put(pid, cal.getOpenStatus());
      });
      FileWriter fw = new FileWriter(this.saveFile, false);

      try {
         Iterator var4 = this.data.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<UUID, Integer> entry = (Entry)var4.next();
            fw.write(String.format("%s:%x\n", ((UUID)entry.getKey()).toString(), entry.getValue()));
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
      XcraftAdvent var10000 = this.plugin;
      Level var10001 = Level.INFO;
      int var10002 = this.data.size();
      var10000.log(var10001, "Saved (" + var10002 + " players) in " + (System.currentTimeMillis() - t1) + "ms");
   }

   protected void silentSave() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
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
      return (PlayerCalendar)this.activePlayers.get(p.getUniqueId());
   }

   public IDateChecker getDateChecker() {
      return this.plugin.getDateChecker();
   }

   public Optional<PlayerCalendar> playerJoin(Player p) {
      if (this.getDateChecker().isCalendarTime()) {
         UUID pid = p.getUniqueId();
         Logger var10000 = this.plugin.getLogger();
         String var10001 = p.getName();
         var10000.info("Player " + var10001 + " (" + pid + ") joined - inData=" + this.data.containsKey(pid) + ", canCreateNewCalendar=" + this.getDateChecker().canCreateNewCalendar());
         if (this.activePlayers.containsKey(pid)) {
            return Optional.of((PlayerCalendar)this.activePlayers.get(pid));
         }

         if (this.data.containsKey(pid) || this.getDateChecker().canCreateNewCalendar()) {
            PlayerCalendar calendar = this.getNewCalendar(p);
            this.activePlayers.put(pid, calendar);
            return Optional.of(calendar);
         }
      }

      return Optional.empty();
   }

   public PlayerCalendar getNewCalendar(Player p) {
      return new PlayerCalendar(this, p, (Integer)this.data.getOrDefault(p.getUniqueId(), 0));
   }

   public void playerQuit(Player p) {
      UUID pid = p.getUniqueId();
      if (this.activePlayers.containsKey(pid)) {
         this.data.put(pid, ((PlayerCalendar)this.activePlayers.remove(pid)).getOpenStatus());
         this.silentSave();
      }

   }
}
