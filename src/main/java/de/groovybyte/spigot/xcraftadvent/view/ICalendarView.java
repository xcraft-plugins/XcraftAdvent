package de.groovybyte.spigot.xcraftadvent.view;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;

public interface ICalendarView {
	public ICalendar getCalendar();

	public void show();

	public void update();

	public void openDoor(int day);
}
