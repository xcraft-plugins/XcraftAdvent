package de.groovybyte.spigot.xcraftadvent.repository;

import de.groovybyte.spigot.xcraftadvent.CalendarManager;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Calendar repository backed by the {@link CalendarManager}.
 *
 * <p>The load/unload work is scheduled on the Bukkit main thread instead of
 * a worker pool: creating the calendar inventories is not thread-safe, and
 * the manager already keeps its state in memory (no blocking IO involved).
 */
public class CalendarLoader implements ICalendarRepository {

	protected final JavaPlugin plugin;
	protected final CalendarManager calendarManager;

	public CalendarLoader(JavaPlugin plugin, CalendarManager calendarManager) {
		this.plugin = plugin;
		this.calendarManager = calendarManager;
	}

	@Override
	public ICalendar getCalendar(Player player) {
		return this.calendarManager.get(player);
	}

	@Override
	public CompletableFuture<ICalendar> loadCalendar(Player player) {
		CompletableFuture<ICalendar> future = new CompletableFuture<>();
		this.plugin.getServer().getScheduler().runTask(this.plugin, () ->
				future.complete(this.calendarManager.connectCalendar(player).orElse(null)));
		return future;
	}

	@Override
	public CompletableFuture<ICalendar> unloadCalendar(Player player) {
		CompletableFuture<ICalendar> future = new CompletableFuture<>();
		this.plugin.getServer().getScheduler().runTask(this.plugin, () ->
				future.complete(this.calendarManager.disconnectCalendar(player)));
		return future;
	}
}
