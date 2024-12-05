package de.groovybyte.spigot.xcraftadvent;

import com.google.common.collect.Range;
import java.time.Clock;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DefaultDateChecker implements IDateChecker {
   private final Clock clock;
   private final Range<Integer> calendarCreationDates;
   private final Range<Integer> calendarOpeningDates;

   public DefaultDateChecker() {
      this(Clock.systemDefaultZone());
   }

   public DefaultDateChecker(Clock clock) {
      this.calendarCreationDates = Range.closed(1, 24);
      this.calendarOpeningDates = Range.closed(1, 31);
      this.clock = clock;
   }

   private MonthDay currentMonthDay() {
      return MonthDay.now(this.clock);
   }

   private boolean isDecember() {
      return this.currentMonthDay().getMonth().equals(Month.DECEMBER);
   }

   public int getDayOfMonth() {
      return this.currentMonthDay().getDayOfMonth();
   }

   public boolean isCalendarTime() {
      return this.isDecember();
   }

   public boolean canCreateNewCalendar() {
      return this.isCalendarTime() && this.calendarCreationDates.contains(this.getDayOfMonth());
   }

   public boolean canOpenExistingCalendar() {
      return this.isCalendarTime() && this.calendarOpeningDates.contains(this.getDayOfMonth());
   }

   public String toString() {
      ZonedDateTime dateTime = ZonedDateTime.now(this.clock);
      String var10000 = DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
      return "DefaultDateChecker{dateTime=" + var10000 + ", dayOfMonth=" + this.getDayOfMonth() + ", isCalendarTime=" + this.isCalendarTime() + ", canCreateNewCalendar=" + this.canCreateNewCalendar() + ", canOpenExistingCalendar=" + this.canOpenExistingCalendar() + "}";
   }
}
