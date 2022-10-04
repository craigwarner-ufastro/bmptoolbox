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

public class MyDatabaseHandler extends SQLiteOpenHelper {
    /**
     * This is a base classs for the sole purpose of handling creates and updates
     */

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 14;

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

    // Zones table name
    private static final String TABLE_ZONE = "zones";

    // Zones Table Columns names
    private static final String KEY_ZONE_NUMBER = "zoneNumber";
    private static final String KEY_IRRIG_CAPTURE = "irrigCapture";
    private static final String KEY_SPACING = "spacing";
    private static final String KEY_CONT_DIAM = "contDiam";
    private static final String KEY_IRRIG_IN_HR = "irrigInHr";
    private static final String KEY_IRRIG_UNIFORMITY = "irrigUniformity";
    private static final String KEY_PLANT_HEIGHT = "plantHeight";
    private static final String KEY_PLANT_WIDTH = "plantWidth";
    private static final String KEY_PCT_COVER = "pctCover";
    private static final String KEY_CONT_SPACING = "contSpacing";
    private static final String KEY_LEACHING_FRACTION = "leachingFraction";
    private static final String KEY_CANOPY_DENSITY = "canopyDensity";
    private static final String KEY_PLANT_WIDTH_FACTOR = "plantWidthFactor";
    private static final String KEY_SOLAR = "solar";
    private static final String KEY_ETO = "eto";
    private static final String KEY_TMAX = "tmax";
    private static final String KEY_TMIN = "tmin";

    public MyDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create both tables in base class
        // create both tables in base class
        String CREATE_WEATHER_TABLE = "CREATE TABLE " + TABLE_WEATHER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WSID + " INTEGER,"
                + KEY_TIMESTAMP + " INTEGER," + KEY_TEMP_2M + " REAL,"
                + KEY_SOLAR_RAD + " REAL," + KEY_RAIN + " REAL,"
                + KEY_WIND_SPEED + " REAL," + KEY_RH + " REAL" + ")";
        db.execSQL(CREATE_WEATHER_TABLE);

        String CREATE_ZONE_TABLE = "CREATE TABLE " + TABLE_ZONE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ZONE_NUMBER + " INTEGER,"
                + KEY_IRRIG_CAPTURE + " INTEGER," + KEY_SPACING + " INTEGER,"
                + KEY_CONT_DIAM + " REAL," + KEY_IRRIG_IN_HR + " REAL,"
                + KEY_IRRIG_UNIFORMITY + " REAL," + KEY_PLANT_HEIGHT + " REAL,"
                + KEY_PLANT_WIDTH + " REAL," + KEY_PCT_COVER + " REAL,"
                + KEY_CONT_SPACING + " REAL," + KEY_LEACHING_FRACTION + " REAL,"
                + KEY_CANOPY_DENSITY + " INTEGER," + KEY_PLANT_WIDTH_FACTOR + " REAL,"
                + KEY_ETO + " REAL," + KEY_SOLAR + " REAL,"
                + KEY_TMAX + " REAL," + KEY_TMIN + " REAL," + KEY_RAIN + " REAL"
                + ")";
        db.execSQL(CREATE_ZONE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade both tables in base class
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ZONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER);

        // Create tables again
        onCreate(db);
    }
}
