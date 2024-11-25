package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.ICalendar;
import de.groovybyte.spigot.xcraftadvent.IServiceProvider;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import de.groovybyte.spigot.xcraftadvent.XcraftAdvent;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

	private final Logger logger;
	private final ICalendarRepository calendarRepository;

	public ConnectionListener(IServiceProvider plugin) {
		this.logger = plugin.getService(Logger.class);
		this.calendarRepository = plugin.getService(ICalendarRepository.class);
	}

	public void checkForUnopenedDoors(ICalendar calendar) {
		Player player = calendar.getOwner();
		if (player.hasPermission(Permissions.SHOW_CALENDAR)) {
			if (calendar.hasClosedDoors()) {
				Messages.notifyAboutUnopenedDoors(calendar.getOwner());

				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					player.performCommand(XcraftAdvent.CALENDAR_COMMAND_LABEL);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev) throws IOException {
		Player player = ev.getPlayer();

		if (player.hasPermission(Permissions.OWN_CALENDAR)) {
			calendarRepository
					.loadCalendar(player)
					.thenAccept(this::checkForUnopenedDoors);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent ev) throws IOException {
		Player player = ev.getPlayer();
		
		calendarRepository.unloadCalendar(player);
	}
}
