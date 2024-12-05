package de.groovybyte.spigot.xcraftadvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

public class DebugDateChecker implements IDateChecker {
   private int dayOfMonth = 30;

   public void setDayOfMonth(int day) {
      Preconditions.checkArgument(Range.closed(1, 24).contains(day));
      this.dayOfMonth = day;
   }

   public int getDayOfMonth() {
      return this.dayOfMonth;
   }

   public boolean isCalendarTime() {
      return true;
   }

   public boolean canCreateNewCalendar() {
      return true;
   }

   public boolean canOpenExistingCalendar() {
      return true;
   }

   public String toString() {
      return "DebugDateChecker{dayOfMonth=" + this.dayOfMonth + "}";
   }
}
