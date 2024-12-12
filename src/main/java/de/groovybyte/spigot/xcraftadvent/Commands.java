package de.groovybyte.spigot.xcraftadvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor {

    private final XcraftAdvent plugin;

    public Commands(XcraftAdvent plugin) {
        this.plugin = plugin;
    }

    // TODO
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.calendarManager.load();
            } catch(IOException var7) {
                plugin.log(Level.WARNING, "IO error with save-file while loading: " + var7.getLocalizedMessage());
            }

            return true;
        } else if(sender instanceof Player) {
            Player p = (Player) sender;
            return args.length > 0 && p.isOp() && this.opCommand(p, args) ? true : true;
        } else {
            sender.sendMessage("not supported");
            return false;
        }
    }

    // TODO
    private boolean debugCmd(Player opPlayer, Queue<String> params) {
        Player checkPlayer = opPlayer;
        int day = plugin.dateChecker.getDayOfMonth();

        while(true) {
            String msg;
            while(!params.isEmpty()) {
                msg = (String) params.remove();
                String var6 = msg.toLowerCase();
                byte var7 = -1;
                switch(var6.hashCode()) {
                    case -985752863:
                        if(var6.equals("player")) {
                            var7 = 0;
                        }
                        break;
                    case 99228:
                        if(var6.equals("day")) {
                            var7 = 1;
                        }
                }

                switch(var7) {
                    case 0:
                        String playerIdentifier = (String) params.remove();

                        Player p;
                        try {
                            UUID uuid = UUID.fromString(playerIdentifier);
                            p = Bukkit.getPlayer(uuid);
                        } catch(IllegalArgumentException var11) {
                            p = Bukkit.getPlayer(playerIdentifier);
                        }

                        if(p == null) {
                            plugin.message(opPlayer, "Player " + playerIdentifier + " not found");
                            return true;
                        }

                        checkPlayer = p;
                        break;
                    case 1:
                        try {
                            day = Integer.parseInt((String) params.remove());
                            if(day < 1 || day > 24) {
                                throw new NumberFormatException("not a day from the calendar");
                            }
                        } catch(NumberFormatException var12) {
                            plugin.message(opPlayer, "Error: " + var12.getMessage());
                            return true;
                        }
                }
            }

            msg = plugin.dateChecker.toString();
            plugin.getLogger().info(msg);
            plugin.message(opPlayer, msg);
            boolean isActive = plugin.calendarManager.isActive(checkPlayer);
            StringBuilder b = new StringBuilder();
            b.append("player=").append(checkPlayer.getName()).append('/').append(checkPlayer.getUniqueId());
            b.append(", hasPermission(calendar.ownAndShow)=").append(checkPlayer.hasPermission("calendar.ownAndShow"));
            b.append(", hasPermission(calendar.openDoors)=").append(checkPlayer.hasPermission("calendar.openDoors"));
            b.append(", isActive=").append(isActive);
            if(isActive) {
                PlayerCalendar playerCalendar = plugin.calendarManager.get(checkPlayer);
                b.append(", canOpen=").append(playerCalendar.canOpen(day));
                b.append(", isOpen=").append(playerCalendar.isOpen(day));
                b.append(", calendarGenerationDay=").append(playerCalendar.genDay);
            }

            msg = b.toString();
            plugin.message(opPlayer, msg);
            plugin.getLogger().info(msg);
            b = new StringBuilder();
            Door door = plugin.calendarManager.getDoor(day);
            b.append("day=").append(day);
            b.append(", hasItems=").append(door.hasItems());
            b.append(", hasCommands=").append(door.hasCommands());
            msg = b.toString();
            plugin.message(opPlayer, msg);
            plugin.getLogger().info(msg);
            return true;
        }
    }

    // TODO
    protected boolean opCommand(Player p, String[] args) {
        if(args[0].equalsIgnoreCase("dbg")) {
            Queue<String> params = new LinkedList(List.of(args));
            params.poll();
            return this.debugCmd(p, params);
        } else if(!args[0].equalsIgnoreCase("edit")) {
            return false;
        } else {
            if(args.length == 1) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.calendarManager.EDITOR.startEditing(p);
                });
            } else {
                try {
                    int day = Integer.parseInt(args[1]);
                    if(day < 1 || day > 24) {
                        throw new NumberFormatException("not a day from the calendar");
                    }

                    String var4 = args[2].toLowerCase();
                    byte var5 = -1;
                    switch(var4.hashCode()) {
                        case -1422509831:
                            if(var4.equals("addcmd")) {
                                var5 = 0;
                            }
                            break;
                        case -1270452666:
                            if(var4.equals("clearcmds")) {
                                var5 = 1;
                            }
                            break;
                        case -316512335:
                            if(var4.equals("clearlocations")) {
                                var5 = 3;
                            }
                            break;
                        case 3556498:
                            if(var4.equals("test")) {
                                var5 = 4;
                            }
                            break;
                        case 862119766:
                            if(var4.equals("addlocation")) {
                                var5 = 2;
                            }
                    }

                    switch(var5) {
                        case 0:
                            plugin.calendarManager.EDITOR.addCommand(day, (String) Arrays.asList(args).subList(3, args.length).stream().collect(Collectors.joining(" ")));
                            break;
                        case 1:
                            plugin.calendarManager.EDITOR.clearCommands(day);
                            break;
                        case 2:
                            Block target = p.getTargetBlockExact(15);
                            if(target != null && target.getState() instanceof Container) {
                                Location location = target.getLocation();
                                Door possibleCurrentDoor = plugin.calendarManager.getDoorByLocation(BlockLocation.fromLocation(location));
                                if(possibleCurrentDoor != null) {
                                    plugin.message(p, "Error: this storage container is already linked to calendar door " + possibleCurrentDoor.day);
                                    return true;
                                }

                                plugin.calendarManager.EDITOR.addDoorInventoryHolder(day, location);
                                int var10002 = location.getBlockX();
                                plugin.message(p, "Added storage container at x=" + var10002 + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ() + " to calendar door " + day);
                                break;
                            }

                            plugin.message(p, "Error: please target a valid storage container");
                            return true;
                        case 3:
                            plugin.calendarManager.EDITOR.clearDoorInventoryHolder(day);
                            break;
                        case 4:
                            plugin.calendarManager.getDoor(day).reward(p);
                            break;
                        default:
                            throw new NumberFormatException("wrong usage");
                    }
                } catch(NumberFormatException|ArrayIndexOutOfBoundsException var9) {
                    plugin.message(p, "Error: " + var9.getMessage());
                    plugin.message(p, "use \"edit [day] addcmd/clearcmds/addlocation/clearlocations/test ([command])\"");
                    return false;
                }
            }

            return true;
        }
    }
}
