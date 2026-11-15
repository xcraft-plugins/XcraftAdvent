package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.IServiceProvider;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import de.groovybyte.spigot.xcraftadvent.XcraftAdvent;
import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

	private final ICalendarRepository calendarRepository;

	public ConnectionListener(IServiceProvider services) {
		this.calendarRepository = services.getService(ICalendarRepository.class);
	}

	/**
	 * Runs on the main thread (the load future completes there). Reminds the
	 * player about unopened doors and opens the calendar for them.
	 */
	public void checkForUnopenedDoors(ICalendar calendar) {
		if (calendar == null) {
			return;
		}
		Player player = calendar.getOwner();
		// Backward compatibility: the command permission in plugin.yml is
		// "calendar.ownAndShow" (2.0.0-alpha2 JAR), but at runtime we check
		// calendar.show individually so migrated servers also work.
		if (player.hasPermission(Permissions.SHOW_CALENDAR)) {
			if (calendar.hasClosedDoors()) {
				Messages.notifyAboutUnopenedDoors(player);

				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					player.performCommand(XcraftAdvent.CALENDAR_COMMAND_LABEL);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		// Backward compatibility: the command permission in plugin.yml is
		// "calendar.ownAndShow" (2.0.0-alpha2 JAR), but at runtime we check
		// calendar.own individually so migrated servers also work.
		if (player.hasPermission(Permissions.OWN_CALENDAR)) {
			calendarRepository
					.loadCalendar(player)
					.thenAccept(this::checkForUnopenedDoors);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		calendarRepository.unloadCalendar(event.getPlayer());
	}
}
