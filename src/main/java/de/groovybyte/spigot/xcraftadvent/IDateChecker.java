package de.groovybyte.spigot.xcraftadvent;

public interface IDateChecker {
   int getDayOfMonth();

   boolean isCalendarTime();

   boolean canCreateNewCalendar();

   boolean canOpenExistingCalendar();
}
