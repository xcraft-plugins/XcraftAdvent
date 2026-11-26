package de.groovybyte.spigot.xcraftadvent.entity;

import org.bukkit.entity.Player;

/**
 * A player's advent calendar: which doors exist, which of them are already
 * open and which doors may be opened.
 */
public interface ICalendar {

	/**
	 * @return whether there is a door the player could open but hasn't yet
	 */
	public boolean hasClosedDoors();

	public Player getOwner();

	public boolean isOpen(int day);

	/**
	 * @return the current calendar day used to determine which doors are available
	 */
	public int getCurrentDay();

	/**
	 * @return whether the door may be opened right now (day already reached)
	 */
	public boolean canOpen(int day);

	/**
	 * Gives out the door's rewards and marks the door as open.
	 *
	 * @param day  the calendar day (1-24)
	 * @return whether the door was opened successfully
	 */
	public boolean open(int day);
}
