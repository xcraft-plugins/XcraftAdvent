package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.*;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryListener implements Listener {

    private final Logger logger;
    private final ICalendarRepository calendarRepository;

    public InventoryListener(IServiceProvider inject) {
        this.logger = inject.getService(Logger.class);
        this.calendarRepository = inject.getService(ICalendarRepository.class);
    }

    public boolean isChestInventory(Inventory iv) {
        return iv != null && iv.getType().equals(InventoryType.CHEST);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        HumanEntity player = ev.getWhoClicked();
        Inventory iv = ev.getClickedInventory();
        if(isChestInventory(iv)) {
            return;
        }
        //		if (player.isOp() && man.EDITOR.isEditing(p)) {
        //			man.EDITOR.edit(ev);
        //			return;
        //		}

        ICalendar calendar = calendarRepository.getCalendar(player);
        if(calendar.matches(ev.getInventory())) {
            return;
        }

        ev.setCancelled(true);
        if(!p.hasPermission(Permissions.OPEN_CALENDAR_DOORS)) {
            Messages.send(p, Messages.MSG_MISSING_PERM);
            return;
        }

        ItemStack ci = ev.getCurrentItem();
        if(pc.isValidDoor(ci)) {
            int day = pc.getClickedDay(ci);

            plugin.runSynced(() -> {
                boolean isOpen = pc.isOpen(day),
                    canOpen = pc.canOpen(day);
                logger.log(
                    Level.INFO, "{0} tried opening door {1} [canOpen={2}, isOpen={3}, calendarGenerationDay={4}]",
                    new Object[]{p.getName(), day, canOpen, isOpen, pc.genDay}
                );
                if(isOpen) {
                    Messages.send(p, Messages.MSG_OPEN, day);
                } else if(!canOpen) {
                    Messages.send(p, Messages.MSG_CLOSED, day);
                } else {
                    pc.open(ci, day);
                }
            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        CalendarManager man = calendarRepository;
        Player p = (Player) ev.getPlayer();
        Inventory ei = ev.getInventory();
        if(ei == null || !ei.getType().equals(InventoryType.CHEST)) {
            return;
        }
        if(man.EDITOR.isEditing(p)) {
            man.EDITOR.stopEditing();
        }
    }
}
