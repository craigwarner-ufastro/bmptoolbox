package com.cwarner62.cirrig_lf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ValveHistoryDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cirrig_lf";

    // Valves table name
    private static final String TABLE_VALVE_HISTORY = "valveHistory";

    // Valves Table Columns names
    private static final String KEY_HIST_ID = "hist_id";
    private static final String KEY_VID = "vid";
    private static final String KEY_PLANT_NAME = "plantName";
    private static final String KEY_DATE = "date";
    private static final String KEY_LF_PCT = "lfPct";

    private SQLiteDatabase db;


    public ValveHistoryDatabaseHandler(Context context) {
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
    void addValveHistory(ValveHistory valveHist) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VID, valveHist.getVID());
        values.put(KEY_PLANT_NAME, valveHist.getPlantName());
        values.put(KEY_DATE, valveHist.getDate());
        values.put(KEY_LF_PCT, valveHist.getLFPct());

        // Inserting Row
        db.insert(TABLE_VALVE_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    // Getting single valve
    ValveHistory getValveHistory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VALVE_HISTORY, new String[]{KEY_HIST_ID,
                        KEY_VID, KEY_PLANT_NAME, KEY_DATE, KEY_LF_PCT},
                        KEY_HIST_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ValveHistory valveHist = new ValveHistory(Integer.parseInt(cursor.getString(0)),
		Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                cursor.getString(3), Float.parseFloat(cursor.getString(4)));

        cursor.close();
        // return valve
        return valveHist;
    }

    // Getting All Valves
    public List<ValveHistory> getAllValves() {
        List<ValveHistory> valveHistList = new ArrayList<ValveHistory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_VALVE_HISTORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ValveHistory valveHist = new ValveHistory();

                valveHist.setID(Integer.parseInt(cursor.getString(0)));
                valveHist.setVID(Integer.parseInt(cursor.getString(1)));
                valveHist.setPlantName(cursor.getString(2));
                valveHist.setDate(cursor.getString(3));
                valveHist.setLFPct(Float.parseFloat(cursor.getString(4)));

                // Adding valve to list
                valveHistList.add(valveHist);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return valveHistList;
    }

    // Updating single valve
    public int updateValveHistory(ValveHistory valveHist) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VID, valveHist.getVID());
        values.put(KEY_PLANT_NAME, valveHist.getPlantName());
        values.put(KEY_DATE, valveHist.getDate());
        values.put(KEY_LF_PCT, valveHist.getLFPct());

        // updating row
        return db.update(TABLE_VALVE_HISTORY, values, KEY_HIST_ID + " = ?",
                new String[]{String.valueOf(valveHist.getID())});
    }

    // Deleting single valve
    public void deleteValveHistory(ValveHistory valveHist) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VALVE_HISTORY, KEY_HIST_ID + " = ?",
                new String[]{String.valueOf(valveHist.getID())});
        db.close();
    }


    // Getting valves Count
    public int getValveHistoryCount() {
        String countQuery = "SELECT  * FROM " + TABLE_VALVE_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

}
