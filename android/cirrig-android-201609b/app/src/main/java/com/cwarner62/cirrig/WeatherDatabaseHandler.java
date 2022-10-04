package com.cwarner62.cirrig;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class WeatherDatabaseHandler extends MyDatabaseHandler {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 15;

	// Database Name
	private static final String DATABASE_NAME = "cirrig";

	// Weathers table name
	private static final String TABLE_WEATHER = "weather";

	// Weathers Table Columns names
	private static final String KEY_ID = "id";
    private static final String KEY_WSID = "wsid";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_TEMP_2M = "temp2m";
    private static final String KEY_SOLAR_RAD = "solarRad";
    private static final String KEY_RAIN = "rain";
    private static final String KEY_WIND_SPEED = "windSpeed";
    private static final String KEY_RH = "rh";

	private SQLiteDatabase db;

	public WeatherDatabaseHandler(Context context) {
	    super(context);
	    //get persistent read/write connection in constructor
	    db = this.getWritableDatabase();
	}

	/** onCreate and onUpgrade are now overridden in base class MyDatabaseHandler
	 *	in order to create both weather and zone tables
	 */

	public void checkDatabase() {
	    //if for some reason the database has closed, reopen it
	    if (!db.isOpen()) db = this.getWritableDatabase();
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new weather
	public void addWeather(WeatherDatapoint weather) {
	    if (!db.isOpen()) checkDatabase();
	    long lastDbTime = getLastDate(weather.getWsid());
	    if (lastDbTime >= weather.getTimestamp()) {
	        return;
        }
		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, weather.getTimestamp());
        values.put(KEY_WSID, weather.getWsid());
        values.put(KEY_TEMP_2M, weather.getTemp());
        values.put(KEY_SOLAR_RAD, weather.getSolarRad());
        values.put(KEY_RAIN, weather.getRain());
        values.put(KEY_WIND_SPEED, weather.getWindSpeed());
        values.put(KEY_RH, weather.getRelativeHumidity());

		// Inserting Row
		db.insert(TABLE_WEATHER, null, values);
	}

	//Adding new weather in bulk
	public void addWeather(Vector <WeatherDatapoint> weather) {
	    if (!db.isOpen()) checkDatabase();
	    System.out.println("WEATHER SIZE "+weather.size());
	    new Throwable().printStackTrace();
	    if (weather.isEmpty()) return;
        String sql = "INSERT INTO " + TABLE_WEATHER + "(" + KEY_TIMESTAMP
                + ", " + KEY_WSID + ", " + KEY_TEMP_2M + ", " + KEY_SOLAR_RAD
                + ", " + KEY_RAIN + ", " + KEY_WIND_SPEED + ", " + KEY_RH
                + ") VALUES (?,?,?,?,?,?,?);";
	    SQLiteStatement statement = db.compileStatement(sql);
	    long lastDbTime = getLastDate(weather.get(0).getWsid());
	    try {
		db.beginTransaction();
		for (int i = 0; i < weather.size(); i++) {
		    WeatherDatapoint currWeather = weather.get(i);
            if (lastDbTime >= currWeather.getTimestamp()) {
                continue;
            }
            statement.clearBindings();
            statement.bindLong(1, currWeather.getTimestamp());
            statement.bindLong(2, currWeather.getWsid());
            statement.bindDouble(3, currWeather.getTemp());
            statement.bindDouble(4, currWeather.getSolarRad());
            statement.bindDouble(5, currWeather.getRain());
            statement.bindDouble(6, currWeather.getWindSpeed());
            statement.bindDouble(7, currWeather.getRelativeHumidity());
            statement.execute();
		}
		db.setTransactionSuccessful();	
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		db.endTransaction();
	    }
	} 

	// Getting single weather
	WeatherDatapoint getWeather(int id) {
        if (!db.isOpen()) checkDatabase();
        Cursor cursor = db.query(TABLE_WEATHER, new String[] { KEY_ID,
                        KEY_WSID, KEY_TIMESTAMP, KEY_TEMP_2M, KEY_SOLAR_RAD, KEY_RAIN,
                        KEY_WIND_SPEED, KEY_RH }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		WeatherDatapoint weather = new WeatherDatapoint(Integer.parseInt(cursor.getString(0)),
				Long.parseLong(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
				Float.parseFloat(cursor.getString(3)), 
				Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
				Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)));
		cursor.close();
		return weather;
	}
	
	// Getting All Weathers
	public List<WeatherDatapoint> getAllWeathers() {
                if (!db.isOpen()) checkDatabase();
		List<WeatherDatapoint> weatherList = new ArrayList<WeatherDatapoint>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_WEATHER;

		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				WeatherDatapoint weather = new WeatherDatapoint();
				weather.setID(Integer.parseInt(cursor.getString(0)));
				weather.setTimestamp(Long.parseLong(cursor.getString(1)));
				weather.setWsid(Integer.parseInt(cursor.getString(2)));
				weather.setTemp(Float.parseFloat(cursor.getString(3)));
				weather.setSolarRad(Float.parseFloat(cursor.getString(4)));
				weather.setRain(Float.parseFloat(cursor.getString(5)));
				weather.setWindSpeed(Float.parseFloat(cursor.getString(6)));
                weather.setRelativeHumidity(Float.parseFloat(cursor.getString(7)));

				// Adding weather to list
				weatherList.add(weather);
			} while (cursor.moveToNext());
		}

		cursor.close();
		// return weather list
		return weatherList;
	}

        // Getting All Weathers for specific wsid
        public List<WeatherDatapoint> getAllWeathers(int wsid) {
                if (!db.isOpen()) checkDatabase();
                List<WeatherDatapoint> weatherList = new ArrayList<WeatherDatapoint>();
                // Select All Query
                String selectQuery = "SELECT  * FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid;

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                        do {
                                WeatherDatapoint weather = new WeatherDatapoint();
                                weather.setID(Integer.parseInt(cursor.getString(0)));
                                weather.setTimestamp(Long.parseLong(cursor.getString(1)));
                                weather.setWsid(Integer.parseInt(cursor.getString(2)));
                                weather.setTemp(Float.parseFloat(cursor.getString(3)));
                                weather.setSolarRad(Float.parseFloat(cursor.getString(4)));
                                weather.setRain(Float.parseFloat(cursor.getString(5)));
                                weather.setWindSpeed(Float.parseFloat(cursor.getString(6)));
                                weather.setRelativeHumidity(Float.parseFloat(cursor.getString(7)));

                                // Adding weather to list
                                weatherList.add(weather);
                        } while (cursor.moveToNext());
                }

                cursor.close();
                return weatherList;
        }

	// Updating single weather
	public int updateWeather(WeatherDatapoint weather) {
                if (!db.isOpen()) checkDatabase();

		ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, weather.getTimestamp());
        values.put(KEY_WSID, weather.getWsid());
        values.put(KEY_TEMP_2M, weather.getTemp());
        values.put(KEY_SOLAR_RAD, weather.getSolarRad());
        values.put(KEY_RAIN, weather.getRain());
        values.put(KEY_WIND_SPEED, weather.getWindSpeed());
        values.put(KEY_RH, weather.getRelativeHumidity());

		// updating row
		int retVal = db.update(TABLE_WEATHER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(weather.getID())});
		//db.close();
		return retVal;
	}

	// Deleting single weather
	public int deleteWeather(WeatherDatapoint weather) {
        if (!db.isOpen()) checkDatabase();
		int rows = db.delete(TABLE_WEATHER, KEY_ID + " = ?",
				new String[] { String.valueOf(weather.getID()) });
		return rows;
	}

    // Deleting old weather - more than 10 days old
    public int deleteOldWeather() {
        if (!db.isOpen()) checkDatabase();
        long tenDaysAgo = System.currentTimeMillis()/1000 - 864000L;
        int rows = db.delete(TABLE_WEATHER, KEY_TIMESTAMP + " < ?",
                                new String[] { String.valueOf(tenDaysAgo) });
        return rows;
    }


	// Getting weathers Count
	public int getWeathersCount() {
        if (!db.isOpen()) checkDatabase();
		String countQuery = "SELECT  * FROM " + TABLE_WEATHER;
		Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
		cursor.close();
		return count;
	}

    // Getting weathers Count for specific wsid
    public int getWeathersCount(int wsid) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid;
        Cursor cursor = db.rawQuery(countQuery, null);
    	int count = cursor.getCount();
        cursor.close();
        return count;
    }

	public long getLastDate(int wsid) {
        if (!db.isOpen()) checkDatabase();
	    int count = getWeathersCount(wsid);
	    if (count == 0) return 0;

	    String countQuery = "SELECT " + KEY_TIMESTAMP + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " ORDER BY " + KEY_TIMESTAMP + " DESC LIMIT 1";
	    Cursor cursor = db.rawQuery(countQuery, null);
	    cursor.moveToFirst();
	    long lastTimestamp = Long.parseLong(cursor.getString(0));
	    cursor.close();
        return lastTimestamp;
	}

        // get weather for last 24 hours
     public double[] getWeatherSummaryLast24(int wsid) {
          if (!db.isOpen()) checkDatabase();
          long lastTime = getLastDate(wsid);
          double[] weather = new double[8];
          String countQuery = "SELECT AVG(" + KEY_SOLAR_RAD + "), MAX("
                    + KEY_TEMP_2M + "), MIN(" + KEY_TEMP_2M + "), SUM(" + KEY_RAIN
                    + "), AVG(" + KEY_WIND_SPEED + "), MAX(" + KEY_RH + "), MIN("
                    + KEY_RH + "), COUNT(*)" + " FROM " + TABLE_WEATHER + " WHERE "
                    + KEY_WSID + " = " + wsid + " AND " + KEY_TIMESTAMP + " > "
                    + (lastTime - 86400);
          System.out.println("getWeatherSummaryLast24> "+countQuery);
          Cursor cursor = db.rawQuery(countQuery, null);
          cursor.moveToFirst();
          for (int j = 0; j < 8; j++) {
              try {
                  weather[j] = Double.parseDouble(cursor.getString(j));
                  System.out.println("getWeatherSummaryLast24> WEATHER "+j+"; "+weather[j]);
              } catch(Exception e) { weather[j] = -1; }
          }
          cursor.close();
          return weather;
     }

    /** This method is deprecated as we want past days to be based on
     * calendar day and not the past 24-48 or 48-72 hours
    //get weather for specific past day, based on 24 hours, not calendar day
    //Past 1 days is past 24 -48 hours
    //expect pastDay is between 2-8
    public double[] getWeatherSummaryForSpecificDay(int wsid, int pastDay) {
        if(!db.isOpen()) checkDatabase();
        long lastTime = getLastDate(wsid);
        double[] weather = new double[8];

        String countQuery = "SELECT AVG(" + KEY_SOLAR_RAD + "), MAX("
                + KEY_TEMP_2M + "), MIN(" + KEY_TEMP_2M + "), SUM(" + KEY_RAIN
                + "), AVG(" + KEY_WIND_SPEED + "), MAX(" + KEY_RH + "), MIN("
                + KEY_RH + "), COUNT(*)" + " FROM " + TABLE_WEATHER + " WHERE "
                + KEY_WSID + " = " + wsid + " AND " + KEY_TIMESTAMP + " > "
                + (lastTime - 86400 * pastDay) + " AND " + KEY_TIMESTAMP + " < "
                + (lastTime - 86400 * (pastDay-1));

        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        for (int j = 0; j < 8; j++) {
            weather[j] = Double.parseDouble(cursor.getString(j));
            System.out.println("getWeatherSummaryForSpecificDay> DAY = "+pastDay+"; j="+j+" => "+weather[j]);
        }
        cursor.close();

//        List<WeatherDatapoint> fulldatapoints = getAllWeathers();

        return weather;
    }
     */

	//get individual rain datapoints for last 24 hours
	public double[] getAllRainLast24(int wsid) {
	    if (!db.isOpen()) checkDatabase();
	    long lastTimestamp = getLastDate(wsid);
	    String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_RAIN + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND " + KEY_TIMESTAMP + " > " + (lastTimestamp-86400) + " ORDER BY " + KEY_TIMESTAMP;
        Cursor cursor = db.rawQuery(query, null);
	    double[] rain = new double[cursor.getCount()];
        cursor.moveToFirst();
	    for (int j = 0; j < cursor.getCount(); j++) {
	        rain[j] = Double.parseDouble(cursor.getString(1));
	        cursor.moveToNext();
	    }
        cursor.close();
	    return rain;
	}

    /** This method is deprecated as we want past days to be based on
     * calendar day and not the past 24-48 or 48-72 hours

    public double[] getAllRainForSpecificDay(int wsid, int pastDay) {
        if (!db.isOpen()) checkDatabase();
        long lastTimestamp = getLastDate(wsid);
        String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_RAIN + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND "
                + KEY_TIMESTAMP + " > " + (lastTimestamp-86400 * pastDay) +" AND "+ KEY_TIMESTAMP + " < " + (lastTimestamp- 86400* (pastDay-1))
                + " ORDER BY " + KEY_TIMESTAMP;
        Cursor cursor = db.rawQuery(query, null);
        double[] rain = new double[cursor.getCount()];
        cursor.moveToFirst();
        for (int j = 0; j < cursor.getCount(); j++) {
            rain[j] = Double.parseDouble(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return rain;
    }
     */

    //get individual solarRad datapoints for last 24 hours
    public double[] getAllSolarRadLast24(int wsid) {
        if (!db.isOpen()) checkDatabase();
        long lastTimestamp = getLastDate(wsid);
        String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_SOLAR_RAD + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND " + KEY_TIMESTAMP + " > " + (lastTimestamp-86400) + " ORDER BY " + KEY_TIMESTAMP;
        Cursor cursor = db.rawQuery(query, null);
        double[] solarRad = new double[cursor.getCount()];
        cursor.moveToFirst();
        for (int j = 0; j < cursor.getCount(); j++) {
            solarRad[j] = Double.parseDouble(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return solarRad;
    }


    /** This method is deprecated as we want past days to be based on
     * calendar day and not the past 24-48 or 48-72 hours

    public double[] getAllSolarRadForSpecificDay(int wsid, int pastDay) {
        if (!db.isOpen()) checkDatabase();
        long lastTimestamp = getLastDate(wsid);

        String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_SOLAR_RAD + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND "
                + KEY_TIMESTAMP + " > " + (lastTimestamp-86400 *pastDay) +" AND "+ KEY_TIMESTAMP + " < " + (lastTimestamp- 86400* (pastDay-1))
                + " ORDER BY " + KEY_TIMESTAMP;
        Cursor cursor = db.rawQuery(query, null);
        double[] solarRad = new double[cursor.getCount()];
        cursor.moveToFirst();
        for (int j = 0; j < cursor.getCount(); j++) {
            solarRad[j] = Double.parseDouble(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return solarRad;
    }
     */

        // get weather for last 24 hours 
    public String[] getWeatherLast24(int wsid) {
	    if (!db.isOpen()) checkDatabase();
	    long lastTime = getLastDate(wsid); 
	    String[] retVal = new String[5];
        float[] weather = new float[5];
        String countQuery = "SELECT AVG(" + KEY_SOLAR_RAD + "), MAX("
                    + KEY_TEMP_2M + "), MIN(" + KEY_TEMP_2M + "), SUM(" + KEY_RAIN
                    + "), AVG(" + KEY_WIND_SPEED + ")" + " FROM " + TABLE_WEATHER
                    + " WHERE " + KEY_WSID + " = " + wsid + " AND " + KEY_TIMESTAMP
                    + " > " + (lastTime - 86400);
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        for (int j = 0; j < 5; j++) weather[j] = Float.parseFloat(cursor.getString(j));
        retVal[0] = String.valueOf((int)(weather[0]+0.5)); //solar radiation - round to int
        retVal[1] = String.valueOf((int)(weather[1]*1.8+32.5)); //max temp - convert to F and round to int
        retVal[2] = String.valueOf((int)(weather[2]*1.8+32.5)); //min temp - convert to F and round to int
        retVal[3] = String.valueOf(weather[3] + 0.005); // rain - round to hundredths, already in inches CW 4/16/15
        retVal[3] = retVal[3].substring(0, retVal[3].indexOf(".")+3);
	    retVal[4] = String.valueOf(weather[4]);
        cursor.close();

        return retVal;
    }

    // get weather for a previous day
    public double[] getWeatherForDay(int wsid, int daysAgo) {
        if (!db.isOpen()) checkDatabase();
        long currTimestamp = System.currentTimeMillis()/1000;
        long refTimestamp = currTimestamp - daysAgo*86400;
        double[] weather = new double[8];
        String countQuery = "SELECT AVG(" + KEY_SOLAR_RAD + "), MAX("
                    + KEY_TEMP_2M + "), MIN(" + KEY_TEMP_2M + "), SUM(" + KEY_RAIN
                    + "), AVG(" + KEY_WIND_SPEED + "), MAX(" + KEY_RH + "), MIN("
                    + KEY_RH + "), COUNT(*)" + " FROM " + TABLE_WEATHER + " WHERE "
                    + KEY_WSID + " = " + wsid + " AND date(" + KEY_TIMESTAMP
                    + ", 'unixepoch', 'localtime') =  date(" + refTimestamp
                    + ", 'unixepoch', 'localtime')";

        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        for (int j = 0; j < weather.length; j++) {
            try {
                weather[j] = Double.parseDouble(cursor.getString(j));
                System.out.println("getWeatherForDay> DAY = " + daysAgo + "; j=" + j + " => " + weather[j]);
            } catch (Exception e) {
                weather[j] = -1;
            }
        }
        cursor.close();

        return weather;
    }

    public double[] getRainForDay(int wsid, int daysAgo) {
        if (!db.isOpen()) checkDatabase();
        long currTimestamp = System.currentTimeMillis()/1000;
        long refTimestamp = currTimestamp - daysAgo*86400;
        String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_RAIN + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND date("
                + KEY_TIMESTAMP + ", 'unixepoch', 'localtime') = date(" + refTimestamp + ", 'unixepoch', 'localtime') ORDER BY " + KEY_TIMESTAMP;
        Cursor cursor = db.rawQuery(query, null);
        double[] rain = new double[cursor.getCount()];
        cursor.moveToFirst();
        for (int j = 0; j < cursor.getCount(); j++) {
            rain[j] = Double.parseDouble(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return rain;
    }

    public double[] getSolarRadForDay(int wsid, int daysAgo) {
        if (!db.isOpen()) checkDatabase();
        long currTimestamp = System.currentTimeMillis()/1000;
        long refTimestamp = currTimestamp - daysAgo*86400;
        String query = "SELECT " + KEY_TIMESTAMP + ", " + KEY_SOLAR_RAD + " FROM " + TABLE_WEATHER + " WHERE " + KEY_WSID + " = "+wsid + " AND date("
                + KEY_TIMESTAMP + ", 'unixepoch', 'localtime') = date(" + refTimestamp + ", 'unixepoch', 'localtime') ORDER BY " + KEY_TIMESTAMP;

        Cursor cursor = db.rawQuery(query, null);
        double[] solarRad = new double[cursor.getCount()];
        cursor.moveToFirst();
        for (int j = 0; j < cursor.getCount(); j++) {
            solarRad[j] = Double.parseDouble(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return solarRad;
    }

    /** Not in use
    //get weather for current calendar day
    public double[] getWeatherCurrentCalendarDay(int wsid) {
        if (!db.isOpen()) checkDatabase();
        long currTimestamp = System.currentTimeMillis()/1000;
        double[] weather = new double[7];

        String countQuery = "SELECT AVG(" + KEY_SOLAR_RAD + "), MAX("
                + KEY_TEMP_2M + "), MIN(" + KEY_TEMP_2M + "), SUM(" + KEY_RAIN
                + "), AVG(" + KEY_WIND_SPEED + "), MAX(" + KEY_RH + "), MIN("
                + KEY_RH + ")" + " FROM " + TABLE_WEATHER + " WHERE "
                + KEY_WSID + " = " + wsid + " AND date(" + KEY_TIMESTAMP
                + ", 'unixepoch', 'localtime') =  date(" + currTimestamp
                + ", 'unixepoch', 'localtime')";

        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        for (int j = 0; j < weather.length; j++) weather[j] = Double.parseDouble(cursor.getString(j));
        cursor.close();

        return weather;
    }
     */

	public void close() {
	    db.close();
	}
}
