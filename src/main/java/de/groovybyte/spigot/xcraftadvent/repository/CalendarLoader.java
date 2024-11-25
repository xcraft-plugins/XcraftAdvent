package de.groovybyte.spigot.xcraftadvent.repository;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.groovybyte.spigot.xcraftadvent.ICalendar;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.entity.Player;

public class CalendarLoader implements ICalendarRepository {

	protected ExecutorService executor;

	public CalendarLoader() {
		executor = Executors.newCachedThreadPool(
				new ThreadFactoryBuilder()
				.setNameFormat("CalendarLoaderThread-%d")
				.build()
		);
	}
	
	protected ICalendar loadCalendarSyncronized(Player player) {
		return null;
	}

	protected ICalendar unloadCalendarSyncronized(Player player) {
		return null;
	}
	
	@Override
	public CompletableFuture<ICalendar> loadCalendar(Player player) throws IOException {
		return CompletableFuture.supplyAsync(() -> loadCalendarSyncronized(player), executor);
	}

	@Override
	public CompletableFuture<ICalendar> unloadCalendar(Player player) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
