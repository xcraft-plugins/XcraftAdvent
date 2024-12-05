package de.groovybyte.spigot.xcraftadvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CalendarEditor {
   public static final String META_KEY = "CalendarEditing";
   protected final XcraftAdvent plugin;
   protected final CalendarManager man;
   protected Player player = null;
   protected final Inventory inv;
   protected int editingDay;

   public CalendarEditor(XcraftAdvent plugin, CalendarManager man) {
      this.plugin = plugin;
      this.man = man;
      this.inv = Bukkit.createInventory((InventoryHolder)null, InventoryType.CHEST, "Türchenfüllen");
   }

   protected Door getDoor(int day) {
      return this.man.doors[day - 1];
   }

   protected boolean inSelectionInv() {
      return this.editingDay == 0;
   }

   protected void setSelectionInv() {
      ItemStack[] content = new ItemStack[24];

      for(int i = 1; i <= 24; ++i) {
         Door door = this.getDoor(i);
         ItemStack item = new ItemStack(door.isEmpty() ? Material.COBWEB : Material.CHEST, 1);
         ItemMeta meta = item.getItemMeta();
         List<String> l = new ArrayList();
         String var10001;
         if (this.man.config.contains("rewards.door" + i + ".locations")) {
            List<Location> locations = this.man.config.getList("rewards.door" + i + ".locations");
            if (locations != null) {
               Iterator var8 = locations.iterator();

               while(var8.hasNext()) {
                  Location location = (Location)var8.next();
                  if (location.getWorld() != null) {
                     var10001 = location.getWorld().getName();
                     l.add("located at world=" + var10001 + " x=" + location.getBlockX() + " y=" + location.getBlockY() + " z=" + location.getBlockZ());
                  }
               }
            }
         }

         if (door.isEmpty()) {
            l.add("no rewards configured");
         } else {
            if (door.hasItems()) {
               ItemStack[] items = door.getItems();
               Collector joiner;
               if (items.length > 5) {
                  joiner = Collectors.joining(", ", "", "...");
               } else {
                  joiner = Collectors.joining(", ");
               }

               var10001 = (String)Arrays.stream(door.getItems()).limit(5L).map((is) -> {
                  return is.getType().toString();
               }).collect(joiner);
               l.add("contains: " + var10001);
            }

            if (door.hasCommands()) {
               l.add("executes " + door.getCommands().length + " commands");
            }
         }

         meta.setDisplayName(String.format("Türchen %s", Integer.toString(i)));
         meta.setLore(l);
         item.setItemMeta(meta);
         content[i - 1] = item;
      }

      this.inv.setContents(content);
      this.editingDay = 0;
   }

   protected void setDayInv(int day) {
      Door d = this.getDoor(day);
      this.inv.setContents(d.getItems());
      this.editingDay = day;
   }

   public void addCommand(int day, String cmd) {
      cmd = cmd.trim();
      if (!cmd.isEmpty()) {
         List<String> tmp = this.man.config.getStringList("rewards.door" + day + ".commands");
         tmp.add(cmd);
         this.man.config.set("rewards.door" + day + ".commands", tmp);
         this.man.doors[day - 1].update(this.man.config);

         try {
            this.man.config.save(this.man.cfgFile);
         } catch (IOException var5) {
            this.plugin.log(Level.WARNING, "config io error");
         }

      }
   }

   public void clearCommands(int day) {
      this.man.config.set("rewards.door" + day + ".commands", (Object)null);
      this.man.doors[day - 1].update(this.man.config);

      try {
         this.man.config.save(this.man.cfgFile);
      } catch (IOException var3) {
         this.plugin.log(Level.WARNING, "config io error");
      }

   }

   public void addDoorInventoryHolder(int day, Location inventoryLocation) {
      List<Location> tmp = this.man.config.getList("rewards.door" + day + ".locations");
      if (tmp == null) {
         tmp = new ArrayList(1);
      }

      ((List)tmp).add(inventoryLocation);
      this.man.config.set("rewards.door" + day + ".locations", tmp);
      this.man.doorLocations.put(BlockLocation.fromLocation(inventoryLocation), day);

      try {
         this.man.config.save(this.man.cfgFile);
      } catch (IOException var5) {
         this.plugin.log(Level.WARNING, "config io error");
      }

   }

   public void clearDoorInventoryHolder(int day) {
      this.man.config.set("rewards.door" + day + ".locations", (Object)null);
      this.man.doorLocations.entrySet().stream().filter((locationIntegerEntry) -> {
         return ((Integer)locationIntegerEntry.getValue()).equals(day);
      }).toList().forEach((locationIntegerEntry) -> {
         this.man.doorLocations.remove(locationIntegerEntry.getKey());
      });

      try {
         this.man.config.save(this.man.cfgFile);
      } catch (IOException var3) {
         this.plugin.log(Level.WARNING, "config io error");
      }

   }

   public void startEditing(Player player) {
      if (this.player != null) {
         player.sendMessage("editing in progress");
      } else {
         this.player = player;
         this.setSelectionInv();
         this.player.openInventory(this.inv);
      }
   }

   public void edit(InventoryClickEvent ev) {
      if (this.inSelectionInv()) {
         ev.setCancelled(true);
         this.setDayInv(ev.getRawSlot() + 1);
      }

   }

   public boolean isEditing(Player player) {
      return this.player == null ? false : this.player.equals(player);
   }

   public void stopEditing() {
      if (!this.inSelectionInv()) {
         List<ItemStack> items = (List)Arrays.asList(this.inv.getContents()).stream().filter((is) -> {
            return is != null;
         }).filter((is) -> {
            return !PlayerCalendar.isAir(is);
         }).collect(Collectors.toList());

         for(int i = 0; i < items.size(); ++i) {
            this.man.config.set("rewards.door" + this.editingDay + ".item" + (i + 1), items.get(i));
         }

         this.man.doors[this.editingDay - 1].update(this.man.config);

         try {
            this.man.config.save(this.man.cfgFile);
         } catch (IOException var3) {
            this.plugin.log(Level.WARNING, "config io error");
         }
      }

      this.player = null;
   }
}
