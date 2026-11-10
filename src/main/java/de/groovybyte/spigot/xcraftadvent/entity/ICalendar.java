package de.groovybyte.spigot.xcraftadvent.entity;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A player's advent calendar: which doors exist, which of them are already
 * open, and how the player interacts with them.
 */
public interface ICalendar {

	/**
	 * @return whether there is a door the player could open but hasn't yet
	 */
	public boolean hasClosedDoors();

	public Player getOwner();

	/**
	 * @param inventory the inventory that is currently open at the top slot
	 * @return whether that inventory is this calendar's view
	 */
	public boolean matches(Inventory inventory);

	/**
	 * @return whether the item is one of this calendar's door items
	 */
	public boolean isValidDoor(ItemStack item);

	/**
	 * @param item a valid door item of this calendar
	 * @return the calendar day (1-24) the item belongs to
	 */
	public int getClickedDay(ItemStack item);

	public boolean isOpen(int day);

	/**
	 * @return whether the door may be opened right now (day already reached)
	 */
	public boolean canOpen(int day);

	/**
	 * Gives out the door's rewards and marks the door as open.
	 *
	 * @param item the door item inside the calendar view
	 * @param day  the calendar day (1-24)
	 */
	public void open(ItemStack item, int day);
}
