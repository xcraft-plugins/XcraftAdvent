package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PlayerCalendar implements ICalendar {

	protected final CalendarManager calendarManager;
	protected final Player player;
	protected final Map<ItemStack, Integer> doors = new HashMap<>();
	protected final Inventory calendar;
	public int genDay;
	protected int open;

	public PlayerCalendar(CalendarManager calendarManager, Player player, int openStatus) {
		this.calendarManager = calendarManager;
		this.player = player;
		this.open = openStatus;
		this.calendar = calendarManager.plugin.getServer().createInventory(player, 54, Messages.INV_TITLE);
		this.calendar.setMaxStackSize(1);
		generateCalendar();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PlayerCalendar)) {
			return false;
		}
		PlayerCalendar other = (PlayerCalendar) obj;
		return this.player.getUniqueId().equals(other.getPlayer().getUniqueId());
	}

	@Override
	public int hashCode() {
		return this.player.getUniqueId().hashCode();
	}

	@Override
	public Player getOwner() {
		return this.player;
	}

	protected boolean isUp2Date() {
		return this.genDay == calendarManager.getDateChecker().getDayOfMonth();
	}

	protected void generateCalendar() {
		this.doors.clear();
		this.calendar.setStorageContents(FILL);
		this.genDay = calendarManager.getDateChecker().getDayOfMonth();
		ItemStack door;
		ItemMeta meta;
		for (int day = 1; day <= 24; day++) {
			door = new ItemStack(isOpen(day)
					? DOOR_OPEN
					: (day == 24)
							? DOOR_24
							: DOOR_CLOSED);
			meta = door.getItemMeta();
			if (day > this.genDay) {
				String lore = String.format((day == 24)
						? Messages.ITEM_24
						: Messages.ITEM_FUTURE,
						day - this.genDay,
						(day - this.genDay == 1) ? "" : "en");
				meta.setLore(Collections.singletonList(lore));
			} else if (day == 24) {
				meta.setLore(Collections.singletonList(Messages.ITEM_24_READY));
			}

			meta.setDisplayName(String.format(Messages.ITEM_NAME, day));
			door.setItemMeta(meta);

			this.doors.put(door, day);
			this.calendar.setItem(this.calendarManager.getDoor(day).getPosition(), door);
		}
	}

	@Override
	public boolean isValidDoor(ItemStack item) {
		return isCalendarDoor(item)
				&& item.hasItemMeta()
				&& this.doors.containsKey(item);
	}

	@Override
	public int getClickedDay(ItemStack item) {
		return this.doors.get(item);
	}

	@Override
	public boolean matches(Inventory inventory) {
		return this.calendar.equals(inventory);
	}

	@Override
	public void open(ItemStack item, int day) {
		if (!this.calendarManager.getDoor(day).reward(this.player)) {
			Messages.send(this.player, Messages.MSG_MISSING_CONTENT);
		} else {
			this.setOpen(day);
			this.calendarManager.plugin.getLogger().log(Level.INFO, this.player.getName() + " opened door " + day);
			this.doors.remove(item);
			item.setType(Material.CHEST);
			ItemMeta meta = item.getItemMeta();
			meta.setLore(Collections.singletonList(Messages.ITEM_OPENED));
			item.setItemMeta(meta);
			this.doors.put(item, day);
			this.player.playSound(this.player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1f);
		}
	}

	@Override
	public boolean hasClosedDoors() {
		int day = 0;
		while (canOpen(++day)) {
			if (!isOpen(day)) {
				return true;
			}
		}
		return false;
	}

	public void setOpen(int day) {
		this.open |= 0b1 << --day;
	}
	
	public void set(int day, boolean open) {
		// 0b000111
		// 0b010010
		// 0b010101
	}

	@Override
	public boolean isOpen(int day) {
		return ((this.open >> --day) & 0b1) == 0b1;
	}

	public boolean isFullyOpened() {
		return 0b1111_1111_1111_1111_1111_1111 == this.open;
	}

	@Override
	public boolean canOpen(int day) {
		return day <= this.genDay && day <= 24;
	}

	public Inventory getCalendar() {
		if (!isUp2Date()) {
			generateCalendar();
		}
		return this.calendar;
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getOpenStatus() {
		return this.open;
	}

	@Override
	public String toString() {
		return String.format("Calendar[%s]=%b", this.player.getName(), this.open);
	}

	protected final static ItemStack SAPLING;
	protected final static ItemStack[] FILL;
	protected final static ItemStack DOOR_OPEN;
	protected final static ItemStack DOOR_CLOSED;
	protected final static ItemStack DOOR_24;

	static {
		SAPLING = new ItemStack(Material.SPRUCE_SAPLING, 1);
		ItemMeta meta = SAPLING.getItemMeta();
		meta.setDisplayName(" ");
		SAPLING.setItemMeta(meta);

		int dChestSize = 54;
		Inventory calendar = Bukkit.createInventory(null, dChestSize);
		for (int index = 0; index < dChestSize; index++) {
			calendar.setItem(index, SAPLING);
		}
		FILL = calendar.getStorageContents();

		DOOR_OPEN = new ItemStack(Material.CHEST);
		DOOR_CLOSED = new ItemStack(Material.SPRUCE_DOOR);
		DOOR_24 = new ItemStack(Material.DARK_OAK_DOOR);
		meta.setLore(Collections.singletonList(Messages.ITEM_OPENED));
		DOOR_OPEN.setItemMeta(meta);
		meta.setLore(Collections.singletonList(Messages.ITEM_CLOSED));
		DOOR_CLOSED.setItemMeta(meta);
	}

	protected static boolean isAir(ItemStack itemStack) {
		return itemStack.getType().equals(Material.AIR) || itemStack.getAmount() == 0;
	}

	public static boolean isCalendarDoor(ItemStack itemStack) {
		return !isAir(itemStack) && !PlayerCalendar.SAPLING.isSimilar(itemStack);
	}
}
