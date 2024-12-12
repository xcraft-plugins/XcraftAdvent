package de.groovybyte.spigot.xcraftadvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
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

import java.io.File;

public class PluginMain extends JavaPlugin implements Listener {

    public static final boolean DEBUG = false;

    public static final boolean ENABLE_COMMAND_CALENDAR_ANNOUNCEMENT = false;
    public static final boolean ENABLE_COMMAND_CALENDAR_VIEW = false;

    public static XcraftAdvent loadedPlugin;

    public void onEnable() {
        File pluginFolder = this.getDataFolder();
        if(!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        loadedPlugin = new XcraftAdvent(
            DEBUG ? new DebugDateChecker() : new DefaultDateChecker(),
            getLogger(),
            (Runnable task) -> Bukkit.getScheduler().runTask(this, task),
            (Runnable task) -> Bukkit.getScheduler().runTaskLater(this, task, 20),
            pluginFolder
        );

//        if (!DateChecker.isDecember()) {
//            this.log(Level.WARNING, "this month is not december, the plugin will be disabled");
//            this.getPluginLoader().disablePlugin(this);
//            return;
//        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("adventskalender").setExecutor(loadedPlugin.commands);
    }

    public void onDisable() {
        loadedPlugin.close();
        loadedPlugin = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        loadedPlugin.onPlayerJoin(ev.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
        loadedPlugin.onPlayerQuit(ev.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if(evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = evt.getClickedBlock();
            if(block != null && loadedPlugin.onPlayerClickedDoorBlock(evt.getPlayer(), block)) {
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent evt) {
        HumanEntity entity = evt.getPlayer();
        if(entity instanceof Player player) {
            Location location = evt.getInventory().getLocation();
            if(location != null && loadedPlugin.onPlayerOpenedDoorContainer(player, location)) {
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        HumanEntity entity = evt.getPlayer();
        if(entity instanceof Player player) {
            Inventory inventory = evt.getInventory();
            if(inventory.getType().equals(InventoryType.CHEST)) {
                if(calendarManager.EDITOR.isEditing(player)) {
                    calendarManager.EDITOR.stopEditing();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        HumanEntity entity = evt.getWhoClicked();
        if(entity instanceof Player player) {
            Inventory inventory = evt.getClickedInventory();
            if(inventory != null && inventory.getType().equals(InventoryType.CHEST)) {
                if(player.isOp() && calendarManager.EDITOR.isEditing(player)) {
                    calendarManager.EDITOR.edit(evt);
                } else {
                    PlayerCalendar calendar = calendarManager.get(player);
                    if(calendar != null && calendar.isCalendar(evt.getInventory())) {
                        evt.setCancelled(true);
                        ItemStack item = evt.getCurrentItem();
                        if(calendar.isValidDoor(item)) {
                            int day = calendar.getClickedDay(item);
                            tryOpenDoor(player, calendar, day);
                        }
                    }
                }
            }
        }
    }
}
