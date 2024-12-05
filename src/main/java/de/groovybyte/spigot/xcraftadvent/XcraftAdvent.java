package de.groovybyte.spigot.xcraftadvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class XcraftAdvent extends JavaPlugin implements Listener, CommandExecutor {
   public static final boolean DEBUG = false;
   public static final boolean ENABLE_COMMAND_CALENDAR_ANNOUNCEMENT = false;
   public static final boolean ENABLE_COMMAND_CALENDAR_VIEW = false;
   protected final String PERM_ownAndShow = "calendar.ownAndShow";
   protected final String PERM_openDoors = "calendar.openDoors";
   protected CalendarManager man;
   protected final IDateChecker dateChecker = new DefaultDateChecker();

   public void log(Level level, String log) {
      this.getLogger().log(level, log);
   }

   public void message(Player p, String s) {
      p.sendMessage(Messages.CHAT_PREFIX + s);
   }

   public IDateChecker getDateChecker() {
      return this.dateChecker;
   }

   public void onEnable() {
      File pluginFolder = this.getDataFolder();
      if (!pluginFolder.exists()) {
         pluginFolder.mkdirs();
      }

      this.man = new CalendarManager(this, new File(pluginFolder, "calendar.cfg"), new File(pluginFolder, "players.data"));
      this.getLogger().info(this.dateChecker.toString());

      try {
         this.man.load();
      } catch (IOException var3) {
         this.log(Level.WARNING, "IO error with save-file while loading: " + var3.getLocalizedMessage());
      }

      Bukkit.getPluginManager().registerEvents(this, this);
      this.getCommand("adventskalender").setExecutor(this);
   }

   public void onDisable() {
      try {
         this.man.save();
      } catch (IOException var2) {
         this.log(Level.WARNING, "IO error with save-file while saving: " + var2.getLocalizedMessage());
      }

   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent ev) {
      Player p = ev.getPlayer();
      if (p.hasPermission("calendar.ownAndShow")) {
         this.man.playerJoin(p).ifPresent((cal) -> {
         });
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent ev) {
      this.man.playerQuit(ev.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerInteract(PlayerInteractEvent evt) {
      if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
         Block block = evt.getClickedBlock();
         if (block != null) {
            Door door = this.man.getDoorByLocation(BlockLocation.fromLocation(block.getLocation()));
            if (door != null) {
               evt.setCancelled(true);
               this.onCalendarDoorOpen(evt.getPlayer(), door);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent evt) {
      HumanEntity var3 = evt.getPlayer();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         Location location = evt.getInventory().getLocation();
         if (location != null) {
            Door door = this.man.getDoorByLocation(BlockLocation.fromLocation(location));
            if (door != null) {
               evt.setCancelled(true);
               this.onCalendarDoorOpen(player, door);
            }
         }
      }
   }

   private void onCalendarDoorOpen(Player player, Door door) {
      if (player.hasPermission("calendar.ownAndShow")) {
         if (!this.man.isActive(player)) {
            this.message(player, "Du kannst den Adventskalender nicht mehr öffnen.");
         } else if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            this.tryOpenDoor(player, this.man.get(player), door.day);
         } else {
            this.message(player, "Du solltest die Türchen nur im Survival-Modus öffnen.");
         }
      } else {
         this.message(player, "Du darfst das leider nicht tun! (Versuche es nochmal in der Hauptwelt)");
      }

   }

   private boolean debugCmd(Player opPlayer, Queue<String> params) {
      Player checkPlayer = opPlayer;
      int day = this.dateChecker.getDayOfMonth();

      while(true) {
         String msg;
         while(!params.isEmpty()) {
            msg = (String)params.remove();
            String var6 = msg.toLowerCase();
            byte var7 = -1;
            switch(var6.hashCode()) {
            case -985752863:
               if (var6.equals("player")) {
                  var7 = 0;
               }
               break;
            case 99228:
               if (var6.equals("day")) {
                  var7 = 1;
               }
            }

            switch(var7) {
            case 0:
               String playerIdentifier = (String)params.remove();

               Player p;
               try {
                  UUID uuid = UUID.fromString(playerIdentifier);
                  p = Bukkit.getPlayer(uuid);
               } catch (IllegalArgumentException var11) {
                  p = Bukkit.getPlayer(playerIdentifier);
               }

               if (p == null) {
                  this.message(opPlayer, "Player " + playerIdentifier + " not found");
                  return true;
               }

               checkPlayer = p;
               break;
            case 1:
               try {
                  day = Integer.parseInt((String)params.remove());
                  if (day < 1 || day > 24) {
                     throw new NumberFormatException("not a day from the calendar");
                  }
               } catch (NumberFormatException var12) {
                  this.message(opPlayer, "Error: " + var12.getMessage());
                  return true;
               }
            }
         }

         msg = this.dateChecker.toString();
         this.getLogger().info(msg);
         this.message(opPlayer, msg);
         boolean isActive = this.man.isActive(checkPlayer);
         StringBuilder b = new StringBuilder();
         b.append("player=").append(checkPlayer.getName()).append('/').append(checkPlayer.getUniqueId());
         b.append(", hasPermission(calendar.ownAndShow)=").append(checkPlayer.hasPermission("calendar.ownAndShow"));
         b.append(", hasPermission(calendar.openDoors)=").append(checkPlayer.hasPermission("calendar.openDoors"));
         b.append(", isActive=").append(isActive);
         if (isActive) {
            PlayerCalendar playerCalendar = this.man.get(checkPlayer);
            b.append(", canOpen=").append(playerCalendar.canOpen(day));
            b.append(", isOpen=").append(playerCalendar.isOpen(day));
            b.append(", calendarGenerationDay=").append(playerCalendar.genDay);
         }

         msg = b.toString();
         this.message(opPlayer, msg);
         this.getLogger().info(msg);
         b = new StringBuilder();
         Door door = this.man.getDoor(day);
         b.append("day=").append(day);
         b.append(", hasItems=").append(door.hasItems());
         b.append(", hasCommands=").append(door.hasCommands());
         msg = b.toString();
         this.message(opPlayer, msg);
         this.getLogger().info(msg);
         return true;
      }
   }

   protected boolean opCommand(Player p, String[] args) {
      if (args[0].equalsIgnoreCase("dbg")) {
         Queue<String> params = new LinkedList(List.of(args));
         params.poll();
         return this.debugCmd(p, params);
      } else if (!args[0].equalsIgnoreCase("edit")) {
         return false;
      } else {
         if (args.length == 1) {
            Bukkit.getScheduler().runTask(this, () -> {
               this.man.EDITOR.startEditing(p);
            });
         } else {
            try {
               int day = Integer.parseInt(args[1]);
               if (day < 1 || day > 24) {
                  throw new NumberFormatException("not a day from the calendar");
               }

               String var4 = args[2].toLowerCase();
               byte var5 = -1;
               switch(var4.hashCode()) {
               case -1422509831:
                  if (var4.equals("addcmd")) {
                     var5 = 0;
                  }
                  break;
               case -1270452666:
                  if (var4.equals("clearcmds")) {
                     var5 = 1;
                  }
                  break;
               case -316512335:
                  if (var4.equals("clearlocations")) {
                     var5 = 3;
                  }
                  break;
               case 3556498:
                  if (var4.equals("test")) {
                     var5 = 4;
                  }
                  break;
               case 862119766:
                  if (var4.equals("addlocation")) {
                     var5 = 2;
                  }
               }

               switch(var5) {
               case 0:
                  this.man.EDITOR.addCommand(day, (String)Arrays.asList(args).subList(3, args.length).stream().collect(Collectors.joining(" ")));
                  break;
               case 1:
                  this.man.EDITOR.clearCommands(day);
                  break;
               case 2:
                  Block target = p.getTargetBlockExact(15);
                  if (target != null && target.getState() instanceof Container) {
                     Location location = target.getLocation();
                     Door possibleCurrentDoor = this.man.getDoorByLocation(BlockLocation.fromLocation(location));
                     if (possibleCurrentDoor != null) {
                        this.message(p, "Error: this storage container is already linked to calendar door " + possibleCurrentDoor.day);
                        return true;
                     }

                     this.man.EDITOR.addDoorInventoryHolder(day, location);
                     int var10002 = location.getBlockX();
                     this.message(p, "Added storage container at x=" + var10002 + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ() + " to calendar door " + day);
                     break;
                  }

                  this.message(p, "Error: please target a valid storage container");
                  return true;
               case 3:
                  this.man.EDITOR.clearDoorInventoryHolder(day);
                  break;
               case 4:
                  this.man.getDoor(day).reward(p);
                  break;
               default:
                  throw new NumberFormatException("wrong usage");
               }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException var9) {
               this.message(p, "Error: " + var9.getMessage());
               this.message(p, "use \"edit [day] addcmd/clearcmds/addlocation/clearlocations/test ([command])\"");
               return false;
            }
         }

         return true;
      }
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
         try {
            this.man.load();
         } catch (IOException var7) {
            this.log(Level.WARNING, "IO error with save-file while loading: " + var7.getLocalizedMessage());
         }

         return true;
      } else if (sender instanceof Player) {
         Player p = (Player)sender;
         return args.length > 0 && p.isOp() && this.opCommand(p, args) ? true : true;
      } else {
         sender.sendMessage("not supported");
         return false;
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent ev) {
      HumanEntity var3 = ev.getPlayer();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         Inventory var4 = ev.getInventory();
         if (var4.getType().equals(InventoryType.CHEST)) {
            if (this.man.EDITOR.isEditing(player)) {
               this.man.EDITOR.stopEditing();
            }

         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent ev) {
      Player p = (Player)ev.getWhoClicked();
      Inventory ei = ev.getClickedInventory();
      if (ei != null && ei.getType().equals(InventoryType.CHEST)) {
         if (p.isOp() && this.man.EDITOR.isEditing(p)) {
            this.man.EDITOR.edit(ev);
         } else {
            PlayerCalendar pc = this.man.get(p);
            if (pc != null && pc.isCalendar(ev.getInventory())) {
               ev.setCancelled(true);
               ItemStack ci = ev.getCurrentItem();
               if (pc.isValidDoor(ci)) {
                  int day = pc.getClickedDay(ci);
                  this.tryOpenDoor(p, pc, day);
               }
            }
         }
      }
   }

   void tryOpenDoor(Player player, PlayerCalendar calendar, int day) {
      if (!player.hasPermission("calendar.openDoors")) {
         this.message(player, "Du darfst das leider nicht tun! (Versuche es nochmal in der Hauptwelt)");
      } else {
         Bukkit.getScheduler().runTask(this, () -> {
            boolean isOpen = calendar.isOpen(day);
            boolean canOpen = calendar.canOpen(day);
            this.log(Level.INFO, player.getName() + " tried opening door " + day + " [canOpen=" + canOpen + ", isOpen=" + isOpen + ", calendarGenerationDay=" + calendar.genDay + "]");
            if (isOpen) {
               this.message(player, String.format("Du hast das Türchen Nummer %d bereits geöffnet!", day));
            } else if (!canOpen) {
               this.message(player, String.format("Du darfst das Türchen Nummer %d noch nicht öffnen!", day));
            } else {
               calendar.open(day);
            }

         });
      }
   }

   // $FF: synthetic method
   private void lambda$onCommand$3(Player p) {
      p.openInventory(this.man.get(p).getCalendar());
   }

   // $FF: synthetic method
   private void lambda$onPlayerJoin$0(Player p) {
      this.message(p, "Du kannst den Adventskalender mit /advent öffnen.");
      if (p.getGameMode().equals(GameMode.SURVIVAL)) {
         Bukkit.dispatchCommand(p, "advent");
      }

   }
}
