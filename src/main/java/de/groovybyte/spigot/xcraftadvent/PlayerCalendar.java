package de.groovybyte.spigot.xcraftadvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PlayerCalendar {
   protected final CalendarManager man;
   protected final Player player;
   protected final Map<ItemStack, Integer> doors = new HashMap();
   protected final Inventory calendar;
   protected int genDay;
   protected int open;
   protected static final ItemStack SAPLING;
   protected static final ItemStack[] FILL;
   protected static final ItemStack DOOR_OPEN;
   protected static final ItemStack DOOR_CLOSED;
   protected static final ItemStack DOOR_24;

   public PlayerCalendar(CalendarManager man, Player player, int openStatus) {
      this.man = man;
      this.player = player;
      this.open = openStatus;
      this.calendar = Bukkit.createInventory(player, 54, "Adventskalender");
      this.calendar.setMaxStackSize(1);
      this.generateCalendar();
   }

   public boolean equals(Object obj) {
      if (obj != null && obj instanceof PlayerCalendar) {
         PlayerCalendar cal = (PlayerCalendar)obj;
         return this.player.getUniqueId().equals(cal.getPlayer().getUniqueId());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.player.getUniqueId().hashCode();
   }

   protected boolean isUp2Date() {
      return this.genDay == this.man.getDateChecker().getDayOfMonth();
   }

   protected void generateCalendar() {
      this.doors.clear();
      this.calendar.setStorageContents(FILL);
      this.genDay = this.man.getDateChecker().getDayOfMonth();

      for(int d = 1; d <= 24; ++d) {
         ItemStack door = new ItemStack(this.isOpen(d) ? DOOR_OPEN : (d == 24 ? DOOR_24 : DOOR_CLOSED));
         ItemMeta meta = door.getItemMeta();
         if (d > this.genDay) {
            String lore = String.format(d == 24 ? "Weihnachten in %s Tag%s" : "Überraschung in %s Tag%s", d - this.genDay, d - this.genDay == 1 ? "" : "en");
            meta.setLore(Collections.singletonList(lore));
         } else if (d == 24) {
            meta.setLore(Collections.singletonList("Frohe Weihnachten!"));
         }

         meta.setDisplayName(String.format("Tag %s", d));
         door.setItemMeta(meta);
         this.doors.put(door, d);
         this.calendar.setItem(this.man.getDoor(d).getPosition(), door);
      }

   }

   public boolean isValidDoor(ItemStack i) {
      return isCalendarDoor(i) && i.hasItemMeta() && this.doors.containsKey(i);
   }

   public int getClickedDay(ItemStack ci) {
      return (Integer)this.doors.get(ci);
   }

   public boolean isCalendar(Inventory i) {
      return this.calendar.equals(i);
   }

   public void open(int day) {
      if (!this.man.getDoor(day).reward(this.player)) {
         this.man.plugin.message(this.player, "Die faulen Admins haben das Türchen noch nicht gefüllt ;)");
      } else {
         this.setOpen(day);
         XcraftAdvent var10000 = this.man.plugin;
         Level var10001 = Level.INFO;
         String var10002 = this.player.getName();
         var10000.log(var10001, var10002 + " opened door " + day);
         this.doors.entrySet().stream().filter((itemStackIntegerEntry) -> {
            return ((Integer)itemStackIntegerEntry.getValue()).equals(day);
         }).toList().forEach((itemStackIntegerEntry) -> {
            this.updateItem((ItemStack)itemStackIntegerEntry.getKey(), day);
         });
         this.player.playSound(this.player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 1.0F);
      }

   }

   public void updateItem(ItemStack i, int day) {
      this.doors.remove(i);
      i.setType(Material.CHEST);
      ItemMeta meta = i.getItemMeta();
      meta.setLore(Collections.singletonList("Bereits geöffnet"));
      i.setItemMeta(meta);
      this.doors.put(i, day);
      this.calendar.setItem(this.man.getDoor(day).getPosition(), i);
   }

   public boolean hasClosedDoors() {
      int day = 0;
      boolean var2 = false;

      do {
         ++day;
         if (!this.canOpen(day)) {
            return false;
         }
      } while(this.isOpen(day));

      return true;
   }

   public void setOpen(int day) {
      --day;
      this.open |= 1 << day;
   }

   public void set(int day, boolean open) {
   }

   public boolean isOpen(int day) {
      --day;
      return (this.open >> day & 1) == 1;
   }

   public boolean isFullyOpened() {
      return 16777215 == this.open;
   }

   public boolean canOpen(int day) {
      return day <= this.genDay && day <= 24;
   }

   public Inventory getCalendar() {
      if (!this.isUp2Date()) {
         this.generateCalendar();
      }

      return this.calendar;
   }

   public Player getPlayer() {
      return this.player;
   }

   public int getOpenStatus() {
      return this.open;
   }

   public String toString() {
      return String.format("Calendar[%s]=%b", this.player.getName(), this.open);
   }

   protected static boolean isAir(ItemStack i) {
      return i.getType().equals(Material.AIR) || i.getAmount() == 0;
   }

   public static boolean isCalendarDoor(ItemStack i) {
      return !isAir(i) && !SAPLING.isSimilar(i);
   }

   static {
      SAPLING = new ItemStack(Material.SPRUCE_SAPLING, 1);
      ItemMeta meta = SAPLING.getItemMeta();
      meta.setDisplayName(" ");
      SAPLING.setItemMeta(meta);
      int dChestSize = 54;
      Inventory calendar = Bukkit.createInventory((InventoryHolder)null, dChestSize);

      for(int i = 0; i < dChestSize; ++i) {
         calendar.setItem(i, SAPLING);
      }

      FILL = calendar.getStorageContents();
      DOOR_OPEN = new ItemStack(Material.CHEST);
      DOOR_CLOSED = new ItemStack(Material.SPRUCE_DOOR);
      DOOR_24 = new ItemStack(Material.DARK_OAK_DOOR);
      meta.setLore(Collections.singletonList("Bereits geöffnet"));
      DOOR_OPEN.setItemMeta(meta);
      meta.setLore(Collections.singletonList("Klicke zum Öffnen"));
      DOOR_CLOSED.setItemMeta(meta);
   }
}
