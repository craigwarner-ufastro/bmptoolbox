package wthjec;
/**
 * Title:        WeatherRealTime
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a real-time run 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;
import org.tiling.scheduling.*;

public class WeatherRealTime {
    String _mainClass = getClass().getName();
    private final Scheduler scheduler = new Scheduler();

    protected int wsid = -1;
    protected String wthFile; 
    protected int minute;
    protected long timestamp;
    protected boolean updated = false;
    protected float minTemp, maxTemp, solarRad, rain; 
    protected String hour; 
    protected WeatherFileConfig config;
    protected Date lastDate, pendingDate;
    protected Vector <String> wthVec;

    public WeatherRealTime(int wsid, String wthFile, int minute, WeatherFileConfig config, Date lastDate) {
      this.wsid = wsid;
      this.wthFile = wthFile;
      this.minute = minute;
      this.config = config;
      this.lastDate = lastDate;
      timestamp = System.currentTimeMillis();
    }

    public void start() {
        scheduler.schedule(new SchedulerTask() {
            public void run() {
                readWeather();
            }
            private void readWeather() {
		System.out.println((new java.util.Date()).toString());
                System.out.println("Reading weather file: "+wthFile);

		String currLine = "";
		wthVec = new Vector<String>();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(wthFile));
		    boolean useCurrent = false;
		    if (config.lines_auto) {
		      //Auto-detect lines to skip
		      boolean foundNum = false;
		      while (!foundNum && br.ready()) {
		        currLine = br.readLine().trim();
		        if (Character.isDigit(currLine.trim().charAt(0))) {
		          foundNum = true;
		          useCurrent = true;
		        }
		      }
		    } else {
		      for (int j = 0; j < config.lskip && br.ready(); j++) {
		        currLine = br.readLine();
		      }
	            }
		    while (br.ready() || useCurrent) {
		        if (useCurrent) {
			    useCurrent = false;
		        } else {
			    currLine = br.readLine().trim();
		        }
		        if (isNewWeather(currLine)) {
			    String weather = parseWeather(currLine);
			    if (weather != null) wthVec.add(weather);
		        }
		    }
		    br.close();
		    updated = true;
		} catch (IOException e) {
		    wthVec.clear();
		    updated = false;
		}
            }
        }, new HourlyIterator(minute, 0));
    }

    public void cancel() {
	scheduler.cancel();
    }

    public String getWthFile() {
      return wthFile;
    }

    public void updateWthFile(String newName) {
      wthFile = newName;
    }

    public int getMinute() {
      return minute; 
    }

    public void updateMinute(int newMinute) {
      minute = newMinute; 
    }

    public long getTimeStamp() {
      return timestamp;
    }

    public Date getLastDate() {
      return lastDate;
    }

    public void updateLastDate() {
      lastDate = pendingDate; 
      System.out.println(_mainClass+"::updateLastDate> last date = "+lastDate.toString());
      pendingDate = null;
    }

    public void updateTimeStamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public boolean readyToUpdate() {
      return updated;
    }

    public Vector<String> getWeather() {
      updated = false;
      return wthVec; 
    }

    protected boolean isNewWeather(String currLine) {
	String[] tokens = currLine.split(config.delimiter);
	String dateString = tokens[config.col_date-1];
	if (config.col_time > 0) {
	    dateString += " "+tokens[config.col_time-1];
	}
	if (dateString.toLowerCase().endsWith("a") || dateString.toLowerCase().endsWith("p")) dateString += "m";
	try {
	    Date currDate = getDate(dateString);
	    if (currDate == null) return false;
	    if (lastDate == null || currDate.after(lastDate)) {
		//update pending date
		if (pendingDate == null || currDate.after(pendingDate)) {
		  pendingDate = currDate;
		  System.out.println(_mainClass+"::isNewWeather> pending date = "+pendingDate.toString());
		}
		return true;
	    } else return false;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    public Date getDate(String dateString) {
	Date currDate = null;
	dateString = dateString.trim();
	if (dateString.indexOf("/") == 1) dateString = "0"+dateString;
	SimpleDateFormat df = new SimpleDateFormat(config.dateString);
	try {
	  currDate = df.parse(dateString);
	} catch(ParseException pe) {
	  df = new SimpleDateFormat();
	  try {
	    currDate = df.parse(dateString);
	  } catch(ParseException pe2) {
	    df = new SimpleDateFormat("MM/dd/yy hh:mma");
	    try {
	      currDate = df.parse(dateString);
	    } catch(ParseException pe3) {
	      System.out.println(_mainClass+"::getDate> Cannot parse date "+dateString);
	      return null;
	    }
	  }
	}
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(currDate);
	if (gc.get(Calendar.YEAR) < 100) {
	  System.out.println(_mainClass+"::getDate> Read year as "+gc.get(Calendar.YEAR)+". Correcting by 2000.");
	  gc.add(Calendar.YEAR, 2000);
	  currDate = gc.getTime();
	}
	return currDate;
    }

    protected String parseWeather(String currLine) {
	String retVal = "";
        String[] tokens = currLine.split(config.delimiter);
        String dateString = tokens[config.col_date-1];
        if (config.col_time > 0) {
            dateString += " "+tokens[config.col_time-1];
        }
        if (dateString.toLowerCase().endsWith("a") || dateString.toLowerCase().endsWith("p")) dateString += "m";
        Date currDate = null;

	try {
	    currDate = getDate(dateString);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	if (currDate == null) return null;

        SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	retVal = sqldf.format(currDate);
	retVal += " " + tokens[config.col_solarRad-1];
	
	float maxTemp = 0, minTemp = 0, rain = 0;
	try {
	    maxTemp = Float.parseFloat(tokens[config.col_maxTemp-1]);
	    minTemp = Float.parseFloat(tokens[config.col_minTemp-1]);
            rain = Float.parseFloat(tokens[config.col_rain-1]);
	} catch (NumberFormatException nfe) {
	    return null;
	}

	if (config.metric) {
	  //Convert to deg F and inches
	  maxTemp = maxTemp*1.8f+32;
	  minTemp = minTemp*1.8f+32;
	  rain /= 2.54f;
	}

	retVal += " "+maxTemp + " " + minTemp + " " +rain;
	return retVal;
    }
}
