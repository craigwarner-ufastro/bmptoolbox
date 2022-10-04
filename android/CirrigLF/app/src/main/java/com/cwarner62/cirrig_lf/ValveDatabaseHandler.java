package com.cwarner62.cirrig_lf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ValveDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cirrig_lf";

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
    private static final String KEY_APP_PLUS_TARE = "appPlusTare";
    private static final String KEY_NUM_PLANTS = "numPlants";


    private SQLiteDatabase db;


    public ValveDatabaseHandler(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context);
        //get persistent read/write connection in constructor
        db = this.getWritableDatabase();
    }

    /** onCreate and onUpgrade are now overridden in base class MyDatabaseHandler
     *      in order to create both weather and valve tables
     */

    public void checkDatabase() {
        //if for some reason the database has closed, reopen it
        if (!db.isOpen()) db = this.getWritableDatabase();
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new valve
    void addValve(Valve valve) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VALVE_NAME, valve.getName());
        values.put(KEY_PLANT_NAME, valve.getPlantName());
        values.put(KEY_IRR_MODE, valve.getIrrMode());
        values.put(KEY_TODO, valve.getTodo());
        values.put(KEY_DONE, valve.getDone());
        values.put(KEY_DATE, valve.getDate());
        values.put(KEY_TIME, valve.getTime());
        values.put(KEY_LF_TARGET, valve.getLFTarget());
        values.put(KEY_LCH_PLUS_TARE, valve.getLchPlusTare());
        values.put(KEY_APP_PLUS_TARE, valve.getAppPlusTare());
        values.put(KEY_NUM_PLANTS, valve.getNumPlants());

        // Inserting Row
        db.insert(TABLE_VALVE, null, values);
        db.close(); // Closing database connection
    }

    // Getting single valve
    Valve getValve(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VALVE, new String[]{KEY_ID,
                        KEY_VALVE_NAME, KEY_PLANT_NAME, KEY_IRR_MODE,
                        KEY_TODO, KEY_DONE, KEY_DATE,
                        KEY_TIME, KEY_LF_TARGET, KEY_LCH_PLUS_TARE,
                        KEY_APP_PLUS_TARE, KEY_NUM_PLANTS}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Valve valve = new Valve(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),
                Integer.parseInt(cursor.getString(3)), Boolean.parseBoolean(cursor.getString(4)),
                Boolean.parseBoolean(cursor.getString(5)), cursor.getString(6),
                cursor.getString(7), Float.parseFloat(cursor.getString(8)),
                Float.parseFloat(cursor.getString(9)), Float.parseFloat(cursor.getString(10)),
                Integer.parseInt(cursor.getString(11)));

        cursor.close();
        // return valve
        return valve;
    }

    // Getting All Valves
    public List<Valve> getAllValves() {
        List<Valve> valveList = new ArrayList<Valve>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_VALVE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Valve valve = new Valve();

                valve.setID(Integer.parseInt(cursor.getString(0)));
                valve.setName(cursor.getString(1));
                valve.setPlantName(cursor.getString(2));
                valve.setIrrMode(Integer.parseInt(cursor.getString(3)));
                valve.setTodo(Boolean.parseBoolean(cursor.getString(4)));
                valve.setDone(Boolean.parseBoolean(cursor.getString(5)));
                valve.setDate(cursor.getString(6));
                valve.setTime(cursor.getString(7));
                valve.setLFTarget(Float.parseFloat(cursor.getString(8)));
                valve.setLchPlusTare(Float.parseFloat(cursor.getString(9)));
                valve.setAppPlusTare(Float.parseFloat(cursor.getString(10)));
                valve.setNumPlants(Integer.parseInt(cursor.getString(11)));

                // Adding valve to list
                valveList.add(valve);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return valveList;
    }

    // Updating single valve
    public int updateValve(Valve valve) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VALVE_NAME, valve.getName());
        values.put(KEY_PLANT_NAME, valve.getPlantName());
        values.put(KEY_IRR_MODE, valve.getIrrMode());
        values.put(KEY_TODO, valve.getTodo());
        values.put(KEY_DONE, valve.getDone());
        values.put(KEY_DATE, valve.getDate());
        values.put(KEY_TIME, valve.getTime());
        values.put(KEY_LF_TARGET, valve.getLFTarget());
        values.put(KEY_LCH_PLUS_TARE, valve.getLchPlusTare());
        values.put(KEY_APP_PLUS_TARE, valve.getAppPlusTare());
        values.put(KEY_NUM_PLANTS, valve.getNumPlants());

        // updating row
        return db.update(TABLE_VALVE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(valve.getID())});
    }

    // Deleting single valve
    public void deleteValve(Valve valve) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VALVE, KEY_ID + " = ?",
                new String[]{String.valueOf(valve.getID())});
        db.close();
    }


    // Getting valves Count
    public int getValvesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_VALVE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

}
