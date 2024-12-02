package de.groovybyte.spigot.xcraftadvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

public class DebugDateChecker implements IDateChecker {

    private int dayOfMonth = 1;

    public void setDayOfMonth(int day) {
        Preconditions.checkArgument(Range.closed(1, 24).contains(day));
        dayOfMonth = day;
    }

    @Override
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public boolean isCalendarTime() {
        return true;
    }
    
    @Override
    public boolean canCreateNewCalendar() {
        return true;
    }

    @Override
    public boolean canOpenExistingCalendar() {
        return true;
    }
}
