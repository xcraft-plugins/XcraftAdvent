package de.groovybyte.spigot.xcraftadvent.entity;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ICalendar {

	public boolean hasClosedDoors();

	public Player getOwner();

	public boolean matches(Inventory inventory);
}
