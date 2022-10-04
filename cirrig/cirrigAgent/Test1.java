/**
 * Title:        IrrigRealTime
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a real-time run 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import org.tiling.scheduling.*;

public class Test1 {
    private final Scheduler scheduler = new Scheduler();

    String _mainClass = getClass().getName();
    String greeting = "GREETINGS";
    int count, n, ncancel;
    boolean cancelled = false;
    TestTask[] tt;

    public Test1(int n, int firstHour, int firstMinute, int ncancel) { 
      this.n = n;
      this.ncancel = ncancel;
      count = 0;
      tt = new TestTask[n];
      int hour = firstHour;
      int minute = firstMinute;
      cancelled = false;
      for (int i = 0; i < n; i++) {
	tt[i] = new TestTask();
	scheduler.schedule(tt[i], new DailyIterator(hour, minute, 0));
        /* If it is currently schedule hour and minute, run manually */
        Calendar c = new GregorianCalendar();
        System.out.println(c.get(Calendar.HOUR_OF_DAY)+" "+hour+" "+c.get(Calendar.MINUTE)+" "+minute);
        if (c.get(Calendar.HOUR_OF_DAY) == hour && c.get(Calendar.MINUTE) == minute) tt[i].run();
	minute++;
	if (minute >= 60) {
	  minute = 0;
	  hour++;
        }
      }
    }

    public void monitor() {
      while(count < n) {
	System.out.println(count);
        try {
          Thread.sleep(10000);
        } catch(InterruptedException ie) {}
        if (count == ncancel && !cancelled) {
          for (int i = count; i < n; i++) tt[i].cancel();
          Calendar c = new GregorianCalendar();
          System.out.println("Cancelling at "+c.get(Calendar.HOUR_OF_DAY)+" "+c.get(Calendar.MINUTE));
          cancelled = true;
	  System.out.println("Rescheduling...");
	  for (int i = count; i < n; i++) {
	    tt[i] = new TestTask();
	    scheduler.schedule(tt[i], new DailyIterator(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)+1, 0));
	  }
        }
      }
    }

    private class TestTask extends SchedulerTask {
      public void run() {
	doThings();
      }

      private void doThings() {
        Calendar c = new GregorianCalendar();
        int todayDoy = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
	int hour = c.get(Calendar.HOUR_OF_DAY);
	int min = c.get(Calendar.MINUTE);
	System.out.println(greeting+" Day = "+todayDoy+"; "+hour+":"+min);
	count++;
      }
    }

    public static void main(String[] args) {
      int n = Integer.parseInt(args[0]);
      int firstHour = Integer.parseInt(args[1]);
      int firstMinute = Integer.parseInt(args[2]);
      int ncancel = Integer.parseInt(args[3]);
      Test1 t = new Test1(n, firstHour, firstMinute, ncancel);
      t.monitor();
    }
}
