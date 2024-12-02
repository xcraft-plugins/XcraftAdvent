package de.groovybyte.spigot.xcraftadvent.datechecker;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DefaultDateChecker implements IDateChecker {

    private final Clock clock;
    private final Range<Integer> calendarCreationDates = Range.closed(1, 24);
    private final Range<Integer> calendarOpeningDates = Range.closed(1, 31);

    public DefaultDateChecker() {
        this(Clock.systemDefaultZone());
    }

    public DefaultDateChecker(Clock clock) {
        this.clock = clock;
    }

    private MonthDay currentMonthDay() {
        return MonthDay.now(clock);
    }

    private boolean isDecember() {
        return currentMonthDay().getMonth().equals(Month.DECEMBER);
    }

    @Override
    public int getDayOfMonth() {
        return currentMonthDay().getDayOfMonth();
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

    public String toString() {
        return "DefaultDateChecker{dateTime=%s, dayOfMonth=%d, isCalendarTime=%b, canCreateNewCalendar=%b, canOpenExistingCalendar=%b}"
            .formatted(
                DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now(this.clock)),
                getDayOfMonth(), isCalendarTime(),
                canCreateNewCalendar(), canOpenExistingCalendar()
            );
    }
}
