package com.cwarner62.cirrig_lf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHandler extends SQLiteOpenHelper {
    /**
     * This is a base classs for the sole purpose of handling creates and updates
     */

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cirrig_lf";

    // Plants table name
    private static final String TABLE_PLANT = "plant";

    // Plants Table Columns names
    private static final String KEY_PLANT_ID = "id";
    private static final String KEY_MODE = "mode";
    private static final String KEY_VID = "vid";
    private static final String KEY_PLANT_NUMBER = "plantNumber";
    private static final String KEY_DRY = "dry";
    private static final String KEY_WET = "wet";
    private static final String KEY_LEACH_PLUS_TARE = "leachPlusTare";
    private static final String KEY_APP_PLUS_TARE = "appPlusTare";

    // Valves table name
    private static final String TABLE_VALVE = "valves";

    // Valves Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_VALVE_NAME = "valveName";
    private static final String KEY_PLANT_NAME = "plantName";
    private static final String KEY_IRR_MODE = "irrMode";
    private static final String KEY_TODO = "todo";
    private static final String KEY_DONE = "done";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_LF_TARGET= "lfTarget";
    private static final String KEY_LCH_PLUS_TARE = "lchPlusTare";
    private static final String KEY_NUM_PLANTS = "numPlants";

    // Valves table name
    private static final String TABLE_VALVE_HISTORY = "valveHistory";

    // Valves Table Columns names
    private static final String KEY_HIST_ID = "hist_id";
    private static final String KEY_LF_PCT = "lfPct";


    public MyDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create all tables in base class
        String CREATE_PLANT_TABLE = "CREATE TABLE " + TABLE_PLANT + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MODE + " INTEGER,"
                + KEY_VID + " INTEGER," + KEY_PLANT_NUMBER + " INTEGER,"
                + KEY_DRY + " REAL," + KEY_WET + " REAL,"
                + KEY_LEACH_PLUS_TARE + " REAL," + KEY_APP_PLUS_TARE + " REAL" + ")";
        db.execSQL(CREATE_PLANT_TABLE);

        String CREATE_VALVE_TABLE = "CREATE TABLE " + TABLE_VALVE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_VALVE_NAME + " TEXT,"
                + KEY_PLANT_NAME + " TEXT," + KEY_IRR_MODE + " INTEGER,"
                + KEY_TODO + " INTEGER," + KEY_DONE + " INTEGER,"
                + KEY_DATE + " TEXT," + KEY_TIME + " TEXT,"
                + KEY_LF_TARGET + " REAL," + KEY_LCH_PLUS_TARE + " REAL,"
                + KEY_APP_PLUS_TARE + " REAL," + KEY_NUM_PLANTS + " INTEGER"
                + ")";
        db.execSQL(CREATE_VALVE_TABLE);

        String CREATE_VALVE_HISTORY_TABLE = "CREATE TABLE " + TABLE_VALVE_HISTORY + "("
                + KEY_HIST_ID + " INTEGER PRIMARY KEY," + KEY_VID + " INTEGER,"
                + KEY_PLANT_NAME + " TEXT," + KEY_DATE + " TEXT,"
                + KEY_LF_PCT + " REAL" + ")";
        db.execSQL(CREATE_VALVE_HISTORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade both tables in base class
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALVE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALVE_HISTORY);

        // Create tables again
        onCreate(db);
    }
}
