package de.groovybyte.spigot.xcraftadvent.repository;

import de.groovybyte.spigot.xcraftadvent.ICalendar;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public interface ICalendarRepository {

	public CompletableFuture<ICalendar> loadCalendar(Player player) throws IOException;
	
	public ICalendar getCalendar(Player player);

	public CompletableFuture<ICalendar> unloadCalendar(Player player) throws IOException;
}
