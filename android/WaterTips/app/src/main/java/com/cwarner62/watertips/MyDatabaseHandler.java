package com.cwarner62.watertips;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHandler extends SQLiteOpenHelper {
    /**
     * This is a base classs for the sole purpose of handling creates and updates
     *
     Arduino unit database - contains MAC address, Unit name, Description, and parameters (type, irr rate, etc).
     Global settings - though I'm not actually sure about most of these -- We won't have a number of arduinos as a setting - you can always add or delete as many as wanted; we will always have the capacity for 4 tippers per arduino - that's hard-coded in the arduino code - we can always have less but I'm not sure we need a setting so much as we can say if 3 of the 4 are nonzero, use those 3 for instance.  Time of day is on the arduino side at least for now -- I suppose we could transfer it to the arduino and have it save it to the SD card and then read from the SD card when it starts up what time its supposed to store and reset its counts but that seems like a lot of trouble so for now I assume we'll stick with midnight-midnight.  And we can always have a button to turn off bluetooth on the phone but I don't really see the point.  If you pull down your notification bar, you'll see the bluetooth icon that you can click to turn off at a phone system level.  So this may all go away actually.  Though on second thought we probably want to have a global default for type, irr rate, etc. so that one can set this and just add a bunch of arduinos without editing that every time so we can have those as global settings.
     Tip counter history -- contains a unit MAC address (for keying against the arduino database), 4 tipper values, and a date

     And that brings up a question on LF history -- should LF history be dynamic or stored in a database ... let's say for instance you change the irrigation rate from  7 to 10 GPH, I would guess you would want all previous LF calcs to be unaffected instead of updating to the new GPH rate?  So in that case the 4th database is LF history -- unit MAC address and date (it would then read the tips from the tip counter history database), type, irr rate, LF calcs.  Presumably this database entry would be created when it is Selected from the tip history page.
     */

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    protected static final String DATABASE_NAME = "waterTips";

    // Arduino table name
    protected static final String TABLE_ARDUINO = "arduino";

    // Arduino Table Columns names
    protected static final String KEY_ARDUINO_ID = "aid";
    protected static final String KEY_ARDUINO_MAC_ADDRESS = "mac";
    protected static final String KEY_UNIT_NAME = "unitName";
    protected static final String KEY_UNIT_DESC = "unitDesc";
    protected static final String KEY_UNIT_TYPE = "unitType";
    protected static final String KEY_IRRIG_RATE = "irrigRate";
    protected static final String KEY_TARGET_LF = "targetLF";
    protected static final String KEY_UNIT_RUNTIME = "unitRuntime";
    protected static final String KEY_CONTAINER_DIAM = "containerDiam";

    protected static final String KEY_DISABLE_TIP1 = "disableTip1";
    protected static final String KEY_DISABLE_TIP2 = "disableTip2";
    protected static final String KEY_DISABLE_TIP3 = "disableTip3";
    protected static final String KEY_DISABLE_TIP4 = "disableTip4";


    //global settings for unit type, irrig rate, target LF, runtime should be settings not database
    // Tips history table name
    protected static final String TABLE_TIP_HISTORY = "tips";

    // Tips history Table Columns names
    protected static final String KEY_TIP_HIST_ID = "tid";
    //re-use KEY_ARDUINO_ID and KEY_ARDUINO_MAC_ADDRESS
    protected static final String KEY_HIST_DATE = "histDate";
    protected static final String KEY_TIP_COUNTS1 = "counts1";
    protected static final String KEY_TIP_COUNTS2 = "counts2";
    protected static final String KEY_TIP_COUNTS3 = "counts3";
    protected static final String KEY_TIP_COUNTS4 = "counts4";

    // LF History table name
    protected static final String TABLE_LF_HISTORY = "lfHistory";

    // LF History Table Columns names
    protected static final String KEY_LF_HIST_ID = "lfid";
    //re-use KEY_ARDUINO_ID and KEY_ARDUINO_MAC_ADDRESS and KEY_TIP_HIST_ID
    protected static final String KEY_LF_PCT = "lfPct";
    protected static final String KEY_LFT_PCT = "lftPct";
    protected static final String KEY_RT = "rt";
    protected static final String KEY_RTT = "rtt";

    private static final String DATABASE_ALTER_ARDUINO_DISABLE1 = "ALTER TABLE "
            + TABLE_ARDUINO + " ADD COLUMN " + KEY_DISABLE_TIP1 + " INTEGER NOT NULL DEFAULT 0;";

    private static final String DATABASE_ALTER_ARDUINO_DISABLE2 = "ALTER TABLE "
            + TABLE_ARDUINO + " ADD COLUMN " + KEY_DISABLE_TIP2 + " INTEGER NOT NULL DEFAULT 0;";

    private static final String DATABASE_ALTER_ARDUINO_DISABLE3 = "ALTER TABLE "
            + TABLE_ARDUINO + " ADD COLUMN " + KEY_DISABLE_TIP3 + " INTEGER NOT NULL DEFAULT 0;";

    private static final String DATABASE_ALTER_ARDUINO_DISABLE4 = "ALTER TABLE "
            + TABLE_ARDUINO + " ADD COLUMN " + KEY_DISABLE_TIP4 + " INTEGER NOT NULL DEFAULT 0;";

    private static final String DATABASE_ALTER_ARDUINO_FILL = "UPDATE " + TABLE_ARDUINO
            + " SET " + KEY_DISABLE_TIP1 + " = 0, " + KEY_DISABLE_TIP2 + " = 0, "
            + KEY_DISABLE_TIP3 + " = 0, " + KEY_DISABLE_TIP4 + " = 0 WHERE " + KEY_ARDUINO_ID + " > 0";


    public MyDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("CREATE");

        //create all tables in base class
        String CREATE_ARDUINO_TABLE = "CREATE TABLE " + TABLE_ARDUINO + "("
                + KEY_ARDUINO_ID + " INTEGER PRIMARY KEY," + KEY_ARDUINO_MAC_ADDRESS + " TEXT,"
                + KEY_UNIT_NAME + " TEXT," + KEY_UNIT_DESC + " TEXT,"
                + KEY_UNIT_TYPE + " INTEGER," + KEY_IRRIG_RATE + " REAL,"
                + KEY_TARGET_LF + " REAL," + KEY_UNIT_RUNTIME + " REAL,"
                + KEY_CONTAINER_DIAM + " REAL," + KEY_DISABLE_TIP1 + " INTEGER NOT NULL DEFAULT 0,"
                + KEY_DISABLE_TIP2 + " INTEGER NOT NULL DEFAULT 0,"+ KEY_DISABLE_TIP3 + " INTEGER NOT NULL DEFAULT 0,"
                + KEY_DISABLE_TIP4 + " INTEGER NOT NULL DEFAULT 0)";
        db.execSQL(CREATE_ARDUINO_TABLE);

        String CREATE_TIP_HISTORY_TABLE = "CREATE TABLE " + TABLE_TIP_HISTORY + "("
                + KEY_TIP_HIST_ID + " INTEGER PRIMARY KEY," + KEY_ARDUINO_ID + " INTEGER,"
                + KEY_HIST_DATE + " TEXT,"
                + KEY_TIP_COUNTS1 + " INTEGER," + KEY_TIP_COUNTS2 + " INTEGER,"
                + KEY_TIP_COUNTS3 + " INTEGER," + KEY_TIP_COUNTS4 + " INTEGER"
                + ")";
        db.execSQL(CREATE_TIP_HISTORY_TABLE);

        String CREATE_LF_HISTORY_TABLE = "CREATE TABLE " + TABLE_LF_HISTORY + "("
                + KEY_LF_HIST_ID + " INTEGER PRIMARY KEY," + KEY_ARDUINO_ID + " INTEGER,"
                + KEY_HIST_DATE + " TEXT," + KEY_TIP_HIST_ID + " INTEGER,"
                + KEY_LF_PCT + " REAL," + KEY_LFT_PCT + " REAL,"
                + KEY_RT + " REAL," + KEY_RTT + " REAL" + ")";
        db.execSQL(CREATE_LF_HISTORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade both tables in base class

        // Drop older tables if existed
/*
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LF_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIP_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARDUINO);

        onCreate(db);
*/

       if (oldVersion <=4 && newVersion > 5) {

            db.execSQL(DATABASE_ALTER_ARDUINO_DISABLE1);
            db.execSQL(DATABASE_ALTER_ARDUINO_DISABLE2);
            db.execSQL(DATABASE_ALTER_ARDUINO_DISABLE3);
            db.execSQL(DATABASE_ALTER_ARDUINO_DISABLE4);
       }

        if (oldVersion == 5 && newVersion == 6) {
            db.execSQL(DATABASE_ALTER_ARDUINO_FILL);
        }

        //onCreate(db);

    }
}
