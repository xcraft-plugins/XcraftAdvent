package de.groovybyte.spigot.xcraftadvent;

public interface IDateChecker {

    public int getDayOfMonth();

    public boolean isCalendarTime();
    
    public boolean canCreateNewCalendar();

    public boolean canOpenExistingCalendar();
}
