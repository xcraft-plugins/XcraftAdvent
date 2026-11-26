package de.groovybyte.spigot.xcraftadvent.view;

import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryCalendarView implements ICalendarView, InventoryHolder {

	private static final int INVENTORY_SIZE = 54;
	private static final ItemStack SAPLING;
	private static final ItemStack[] FILL;
	private static final ItemStack DOOR_OPEN;
	private static final ItemStack DOOR_CLOSED;
	private static final ItemStack DOOR_24;

	static {
		SAPLING = new ItemStack(Material.SPRUCE_SAPLING, 1);
		ItemMeta meta = SAPLING.getItemMeta();
		meta.setDisplayName(" ");
		SAPLING.setItemMeta(meta);

		FILL = new ItemStack[INVENTORY_SIZE];
		for (int index = 0; index < INVENTORY_SIZE; index++) {
			FILL[index] = SAPLING;
		}

		DOOR_OPEN = new ItemStack(Material.CHEST);
		DOOR_CLOSED = new ItemStack(Material.SPRUCE_DOOR);
		DOOR_24 = new ItemStack(Material.DARK_OAK_DOOR);
		meta = DOOR_OPEN.getItemMeta();
		meta.setLore(Collections.singletonList(Messages.ITEM_OPENED));
		DOOR_OPEN.setItemMeta(meta);
		meta = DOOR_CLOSED.getItemMeta();
		meta.setLore(Collections.singletonList(Messages.ITEM_CLOSED));
		DOOR_CLOSED.setItemMeta(meta);
	}

	private final ICalendar calendar;
	private final Inventory inventory;
	private final Map<ItemStack, Integer> doors = new HashMap<>();

	public InventoryCalendarView(ICalendar calendar) {
		this.calendar = calendar;
		this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, Messages.INV_TITLE);
		this.inventory.setMaxStackSize(1);
		update();
	}

	@Override
	public ICalendar getCalendar() {
		return this.calendar;
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public void show() {
		update();
		this.calendar.getOwner().openInventory(this.inventory);
	}

	@Override
	public void update() {
		this.doors.clear();
		this.inventory.setStorageContents(FILL.clone());
		int currentDay = this.calendar.getCurrentDay();
		for (int day = 1; day <= 24; day++) {
			boolean isOpen = this.calendar.isOpen(day);
			ItemStack door = new ItemStack(isOpen
					? DOOR_OPEN
					: (day == 24) ? DOOR_24 : DOOR_CLOSED);
			ItemMeta meta = door.getItemMeta();
			if (isOpen) {
				meta.setLore(Collections.singletonList(Messages.ITEM_OPENED));
			} else if (day > currentDay) {
				int daysUntilOpen = day - currentDay;
				String lore = String.format(day == 24 ? Messages.ITEM_24 : Messages.ITEM_FUTURE,
						daysUntilOpen,
						daysUntilOpen == 1 ? "" : "en");
				meta.setLore(Collections.singletonList(lore));
			} else if (day == 24) {
				meta.setLore(Collections.singletonList(Messages.ITEM_24_READY));
			}
			meta.setDisplayName(String.format(Messages.ITEM_NAME, day));
			door.setItemMeta(meta);

			this.doors.put(door, day);
			this.inventory.setItem(getDoorPosition(day), door);
		}
	}

	public boolean isValidDoor(ItemStack item) {
		return !isAir(item)
				&& !SAPLING.isSimilar(item)
				&& item.hasItemMeta()
				&& this.doors.containsKey(item);
	}

	public int getClickedDay(ItemStack item) {
		return this.doors.get(item);
	}

	@Override
	public void openDoor(int day) {
		if (this.calendar.isOpen(day)) {
			Messages.send(this.calendar.getOwner(), Messages.MSG_OPEN, day);
			return;
		}
		if (!this.calendar.canOpen(day)) {
			Messages.send(this.calendar.getOwner(), Messages.MSG_CLOSED, day);
			return;
		}
		if (!this.calendar.open(day)) {
			Messages.send(this.calendar.getOwner(), Messages.MSG_MISSING_CONTENT);
			return;
		}
		update();
		Player player = this.calendar.getOwner();
		player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1f);
	}

	private static int getDoorPosition(int day) {
		int zeroBasedDay = day - 1;
		return 10 + zeroBasedDay + 2 * (zeroBasedDay / 7) + (zeroBasedDay > 20 ? 2 : 0);
	}

	private static boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
	}
}
