package io.github.salami555.ardania.xcraftadvent;

import java.util.Calendar;

public final class DateChecker {

    public final static boolean DEBUG = false;
    public final static int DEBUG_DAY = 24;

    public final static boolean isDecember() {
        return Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER || DEBUG;
    }
    
    public final static int getDayOfMonth() {
        if (DEBUG) {
            return Math.max(1, DEBUG_DAY);
        }
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
    
    public final static boolean isNewCalendarTime() {
        return isDecember() && getDayOfMonth() <= 24;
    }
    
    public final static boolean isLateOpenTime() {
        return isDecember() && getDayOfMonth() <= 31;
    }
}
