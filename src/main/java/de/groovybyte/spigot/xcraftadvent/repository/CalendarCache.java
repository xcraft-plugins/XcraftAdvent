package de.groovybyte.spigot.xcraftadvent.repository;

import de.groovybyte.spigot.xcraftadvent.ICalendar;
import java.io.IOException;
import org.bukkit.entity.Player;

public class CalendarCache implements ICalendarRepository {

	@Override
	public ICalendar loadCalendar(Player player) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ICalendar unloadCalendar(Player player) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
