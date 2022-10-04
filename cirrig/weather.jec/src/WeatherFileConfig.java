package wthjec;
/**
 * Title:        WeatherFileConfig
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for parsing weather file 
 */

public class WeatherFileConfig {
    public static int CUSTOM = 0;
    public static int DAVIS = 1;

    protected int lskip, col_date, col_time;
    protected int col_minTemp, col_maxTemp, col_solarRad, col_rain;
    protected boolean lines_auto, metric, usb;
    protected String delimiter;
    public String dateString="MM/dd/yy hh:mma";

    public WeatherFileConfig(int config) {
      if (config == CUSTOM) {
	lskip = 0;
	lines_auto = true;
	col_date = 0;
	col_time = -1;
	col_minTemp = 0;
	col_maxTemp = 0;
	col_solarRad = 0;
	col_rain = 0;
	metric = false;
	delimiter = "\\s+";
	dateString = "MM/dd/yy hh:mma";
	usb = true; 
      } else if (config == DAVIS) {
	lskip = 0;
	lines_auto = true;
	col_date = 1;
	col_time = 2;
	col_minTemp = 5;
	col_maxTemp = 4;
	col_solarRad = 20;
	col_rain = 18;
        metric = false;
        delimiter = "\\s+";
	dateString = "MM/dd/yy hh:mma";
	usb = true;
      }
    }

    public void setCols(int date, int time, int minTemp, int maxTemp, int solarRad, int rain) {
        col_date = date;
        col_time = time;
        col_minTemp = minTemp;
        col_maxTemp = maxTemp;
        col_solarRad = solarRad;
        col_rain = rain;
    }

    public void setDelim(String delim) {
	delimiter = delim;
    }

    public void setMetric(boolean isMetric) {
	metric = isMetric;
    }

    public void setLinesToSkip(int lines) {
	lskip = lines;
    }

    public void setAutoSkip(boolean auto) {
	lines_auto = auto;
    }

    public void setUsb(boolean autoUsb) {
	usb = autoUsb;
    }
}
