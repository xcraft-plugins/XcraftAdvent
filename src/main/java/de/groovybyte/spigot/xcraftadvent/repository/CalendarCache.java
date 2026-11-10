package de.groovybyte.spigot.xcraftadvent.repository;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

/**
 * Stub for a caching calendar repository. Not wired up yet; the plugin
 * currently uses {@link CalendarLoader}.
 */
public class CalendarCache implements ICalendarRepository {

	@Override
	public CompletableFuture<ICalendar> loadCalendar(Player player) {
		return CompletableFuture.failedFuture(new UnsupportedOperationException("Not supported yet."));
	}

	@Override
	public ICalendar getCalendar(Player player) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CompletableFuture<ICalendar> unloadCalendar(Player player) {
		return CompletableFuture.failedFuture(new UnsupportedOperationException("Not supported yet."));
	}
}
