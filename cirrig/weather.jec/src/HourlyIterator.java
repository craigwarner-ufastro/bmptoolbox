package wthjec;

/**
 * Title:        HourlyIterator.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  hourly iterator 
 */

import org.tiling.scheduling.ScheduleIterator;
import java.util.Calendar;
import java.util.Date;

/**
 * An HourlyIterator returns a sequence of subsequent hours at the same minute/second. 
 */

public class HourlyIterator implements ScheduleIterator {

    private final int minute;
    private final int second;
    private final Calendar calendar = Calendar.getInstance();

    public HourlyIterator(int minute, int second) {
        this(minute, second, new Date());
    }

    public HourlyIterator(int minute, int second, Date date) {
        this.minute = minute;
        this.second = second;
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        if (!calendar.getTime().before(date)) {
            calendar.add(Calendar.HOUR_OF_DAY, -1);
        }
    }

    public Date next() {
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return calendar.getTime();
    }
}

