package de.groovybyte.spigot.xcraftadvent.repository;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

/**
 * Loads, provides and unloads per-player calendars.
 *
 * <p>All results are delivered on the Bukkit main thread, because creating
 * and mutating inventories is not thread-safe.
 */
public interface ICalendarRepository {

	/**
	 * Connects the player's calendar (if the date checker allows it).
	 *
	 * @return a future completing with the player's calendar, or with
	 *         {@code null} when the player gets no calendar (e.g. outside
	 *         of advent time)
	 */
	public CompletableFuture<ICalendar> loadCalendar(Player player);

	/**
	 * @return the player's active calendar, or {@code null} if the player
	 *         has none
	 */
	public ICalendar getCalendar(Player player);

	/**
	 * Disconnects the player's calendar and persists its open status.
	 *
	 * @return a future completing with the calendar that was unloaded, or
	 *         with {@code null} if the player had none
	 */
	public CompletableFuture<ICalendar> unloadCalendar(Player player);
}
