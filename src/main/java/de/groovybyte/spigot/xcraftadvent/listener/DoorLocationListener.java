package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.CalendarManager;
import de.groovybyte.spigot.xcraftadvent.IServiceProvider;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import de.groovybyte.spigot.xcraftadvent.entity.BlockLocation;
import de.groovybyte.spigot.xcraftadvent.entity.Door;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

/**
 * Handles physical door-locations: when a player right-clicks a block
 * registered in {@code rewards.doorN.locations} or opens a chest at
 * such a location, the corresponding calendar door opens.
 */
public class DoorLocationListener implements Listener {

    private final CalendarManager calendarManager;

    public DoorLocationListener(IServiceProvider services) {
        this.calendarManager = services.getService(CalendarManager.class);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        BlockLocation blockLocation = BlockLocation.fromLocation(clickedBlock.getLocation());
        Door door = calendarManager.getDoorByLocation(blockLocation);
        if (door == null) {
            return;
        }

        event.setCancelled(true);
        openCalendarDoor(event.getPlayer(), door);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity playerEntity = event.getPlayer();
        if (!(playerEntity instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (inventory.getLocation() == null) {
            return;
        }

        BlockLocation blockLocation = BlockLocation.fromLocation(inventory.getLocation());
        Door door = calendarManager.getDoorByLocation(blockLocation);
        if (door == null) {
            return;
        }

        event.setCancelled(true);
        openCalendarDoor(player, door);
    }

    /**
     * Opens the calendar door for the given player, checking permissions,
     * active status, and game mode.
     *
     * <p>For physical door-locations the calendar inventory is not open,
     * so the calendar state is updated directly without an inventory view.
     */
    private void openCalendarDoor(Player player, Door door) {
        // Backward compatibility: accepts either the combined "calendar.ownAndShow"
        // permission (2.0.0-alpha2 JAR) OR both "calendar.own" AND "calendar.show"
        // (split permissions for migrated servers).
        if (!Permissions.hasCalendarAccess(player)) {
            Messages.send(player, Messages.MSG_MISSING_PERM);
            return;
        }

        if (!calendarManager.isActive(player)) {
            Messages.send(player, Messages.MSG_WRONG_TIME);
            return;
        }

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            Messages.send(player, Messages.MSG_WRONG_GAMEMODE);
            return;
        }

        ICalendar calendar = calendarManager.get(player);
        if (calendar == null) {
            return;
        }

        int day = door.getDay();
        if (calendar.isOpen(day)) {
            Messages.send(player, Messages.MSG_OPEN, day);
        } else if (!calendar.canOpen(day)) {
            Messages.send(player, Messages.MSG_CLOSED, day);
        } else {
           // Physical door location: no calendar inventory is open, so update
           // the calendar state directly.
           if (!calendar.open(day)) {
               Messages.send(player, Messages.MSG_MISSING_CONTENT);
           }
        }
    }
}
