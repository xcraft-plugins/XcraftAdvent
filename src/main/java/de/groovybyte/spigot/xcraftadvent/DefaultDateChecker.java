package de.groovybyte.spigot.xcraftadvent;

import com.google.common.collect.Range;
import java.util.Calendar;
import java.util.function.Supplier;

public class DefaultDateChecker implements IDateChecker {

    private final Supplier<Calendar> calendarSupplier;
    private final Range<Integer> calendarCreationDates = Range.closed(1, 24);
    private final Range<Integer> calendarOpeningDates = Range.closed(1, 31);

    public DefaultDateChecker() {
        this(Calendar::getInstance);
    }

    public DefaultDateChecker(Supplier<Calendar> calendarSupplier) {
        this.calendarSupplier = calendarSupplier;
    }

    private Calendar getCalendar() {
        return calendarSupplier.get();
    }

    private boolean isDecember() {
        return getCalendar().get(Calendar.MONTH) == Calendar.DECEMBER;
    }

    @Override
    public int getDayOfMonth() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean isCalendarTime() {
        return isDecember();
    }
    
    @Override
    public boolean canCreateNewCalendar() {
        return isCalendarTime() && calendarCreationDates.contains(getDayOfMonth());
    }

    @Override
    public boolean canOpenExistingCalendar() {
        return isCalendarTime() && calendarOpeningDates.contains(getDayOfMonth());
    }
}
