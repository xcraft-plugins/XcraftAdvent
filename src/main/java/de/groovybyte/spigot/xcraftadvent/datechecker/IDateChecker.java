package de.groovybyte.spigot.xcraftadvent.datechecker;

public interface IDateChecker {

    int getDayOfMonth();

    boolean isCalendarTime();

    boolean canCreateNewCalendar();

    boolean canOpenExistingCalendar();
}
