package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.CalendarManager;
import de.groovybyte.spigot.xcraftadvent.IServiceProvider;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryListener implements Listener {

	private final Logger logger;
	private final ICalendarRepository calendarRepository;
	private final CalendarManager calendarManager;
	private final JavaPlugin plugin;

	public InventoryListener(IServiceProvider services) {
		this.logger = services.getService(Logger.class);
		this.calendarRepository = services.getService(ICalendarRepository.class);
		this.calendarManager = services.getService(CalendarManager.class);
		this.plugin = services.getService(JavaPlugin.class);
	}

	public boolean isChestInventory(Inventory inventory) {
		return inventory != null && inventory.getType().equals(InventoryType.CHEST);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		HumanEntity clickedBy = event.getWhoClicked();
		if (!(clickedBy instanceof Player player)) {
			return;
		}
		Inventory clickedInventory = event.getClickedInventory();
		if (!isChestInventory(clickedInventory)) {
			return;
		}
//		if (player.isOp() && calendarManager.EDITOR.isEditing(player)) {
//			calendarManager.EDITOR.edit(event);
//			return;
//		}

		ICalendar calendar = calendarRepository.getCalendar(player);
		if (calendar == null || !calendar.matches(event.getInventory())) {
			return;
		}

		event.setCancelled(true);
		if (!player.hasPermission(Permissions.OPEN_CALENDAR_DOORS)) {
			Messages.send(player, Messages.MSG_MISSING_PERM);
			return;
		}

		ItemStack clickedItem = event.getCurrentItem();
		if (calendar.isValidDoor(clickedItem)) {
			int day = calendar.getClickedDay(clickedItem);

			// deferred to the next tick: inventories must not be mutated
			// while the click event is still being processed
			this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
				boolean isOpen = calendar.isOpen(day);
				boolean canOpen = calendar.canOpen(day);
				this.logger.log(Level.INFO, "{0} tried opening door {1} [canOpen={2}, isOpen={3}]",
						new Object[]{player.getName(), day, canOpen, isOpen});
				if (isOpen) {
					Messages.send(player, Messages.MSG_OPEN, day);
				} else if (!canOpen) {
					Messages.send(player, Messages.MSG_CLOSED, day);
				} else {
					calendar.open(clickedItem, day);
				}
			});
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		HumanEntity closedBy = event.getPlayer();
		if (!(closedBy instanceof Player player)) {
			return;
		}
		Inventory closedInventory = event.getInventory();
		if (!isChestInventory(closedInventory)) {
			return;
		}
		if (this.calendarManager.EDITOR.isEditing(player)) {
			this.calendarManager.EDITOR.stopEditing();
		}
	}
}
