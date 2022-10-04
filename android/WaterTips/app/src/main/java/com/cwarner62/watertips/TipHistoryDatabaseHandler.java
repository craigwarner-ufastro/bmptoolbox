package com.cwarner62.watertips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TipHistoryDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 7;

    private SQLiteDatabase db;

    public TipHistoryDatabaseHandler(Context context) {
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
    public void addTipHistory(TipHistory tipHist) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_ID, tipHist.getAID());
        values.put(KEY_HIST_DATE, tipHist.getDate());
        values.put(KEY_TIP_COUNTS1, tipHist.getCounts(0));
        values.put(KEY_TIP_COUNTS2, tipHist.getCounts(1));
        values.put(KEY_TIP_COUNTS3, tipHist.getCounts(2));
        values.put(KEY_TIP_COUNTS4, tipHist.getCounts(3));

        // Inserting Row
        db.insert(TABLE_TIP_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    // Getting single valve
    public TipHistory getTipHistory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TIP_HISTORY, new String[]{KEY_TIP_HIST_ID,
                        KEY_ARDUINO_ID,  KEY_HIST_DATE, KEY_TIP_COUNTS1, KEY_TIP_COUNTS2,
                        KEY_TIP_COUNTS3, KEY_TIP_COUNTS4},
                        KEY_TIP_HIST_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        TipHistory tipHist = new TipHistory(Integer.parseInt(cursor.getString(0)),
		        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                Integer.parseInt(cursor.getString(3)), Integer.parseInt(cursor.getString(4)),
                Integer.parseInt(cursor.getString(5)), Integer.parseInt(cursor.getString(6)));

        cursor.close();
        // return valve
        return tipHist;
    }

    // Getting All TipHistories
    public List<TipHistory> getAllTips() {
        List<TipHistory> tipHistList = new ArrayList<TipHistory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TIP_HISTORY + " ORDER BY " + KEY_HIST_DATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TipHistory tipHist = new TipHistory();

                tipHist.setID(Integer.parseInt(cursor.getString(0)));
                tipHist.setAID(Integer.parseInt(cursor.getString(1)));
                tipHist.setDate(cursor.getString(2));
                for (int j = 0; j < 4; j++) tipHist.setCounts(j, Integer.parseInt(cursor.getString(j+3)));

                // Adding valve to list
                tipHistList.add(tipHist);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return tipHistList;
    }

    // Getting All TipHistories
    public TipHistory getLatestTipsByUnit(int id) {
        TipHistory tipHist = null;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TIP_HISTORY + " WHERE " + KEY_ARDUINO_ID +" = " +id +" ORDER BY " + KEY_HIST_DATE + " DESC LIMIT 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                tipHist = new TipHistory();

                tipHist.setID(Integer.parseInt(cursor.getString(0)));
                tipHist.setAID(Integer.parseInt(cursor.getString(1)));
                tipHist.setDate(cursor.getString(2));
                for (int j = 0; j < 4; j++) tipHist.setCounts(j, Integer.parseInt(cursor.getString(j+3)));

            } while (cursor.moveToNext());
        }
        cursor.close();

        return tipHist;
    }

    // Getting All TipHistories
    public List<TipHistory> getTipsByUnit(int id) {
        List<TipHistory> tipHistList = new ArrayList<TipHistory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TIP_HISTORY + " WHERE " + KEY_ARDUINO_ID +" = " +id + " ORDER BY " + KEY_HIST_DATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TipHistory tipHist = new TipHistory();

                tipHist.setID(Integer.parseInt(cursor.getString(0)));
                tipHist.setAID(Integer.parseInt(cursor.getString(1)));
                tipHist.setDate(cursor.getString(2));
                for (int j = 0; j < 4; j++) tipHist.setCounts(j, Integer.parseInt(cursor.getString(j+3)));

                // Adding valve to list
                tipHistList.add(tipHist);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return tipHistList;
    }

    // Getting All TipHistories
    public List<String> getDatesByUnit(int id) {
        List<String> dateList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  " + KEY_HIST_DATE + " FROM " + TABLE_TIP_HISTORY + " WHERE " + KEY_ARDUINO_ID +" = " +id + " ORDER BY " + KEY_HIST_DATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                dateList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return dateList;
    }

    // Updating single valve
    public int updateTipHistory(TipHistory tipHist) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_ID, tipHist.getAID());
        values.put(KEY_HIST_DATE, tipHist.getDate());
        values.put(KEY_TIP_COUNTS1, tipHist.getCounts(0));
        values.put(KEY_TIP_COUNTS2, tipHist.getCounts(1));
        values.put(KEY_TIP_COUNTS3, tipHist.getCounts(2));
        values.put(KEY_TIP_COUNTS4, tipHist.getCounts(3));


        // updating row
        return db.update(TABLE_TIP_HISTORY, values, KEY_TIP_HIST_ID + " = ?",
                new String[]{String.valueOf(tipHist.getID())});
    }

    // Deleting single valve
    public void deleteTipHistory(TipHistory tipHist) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIP_HISTORY, KEY_TIP_HIST_ID + " = ?",
                new String[]{String.valueOf(tipHist.getID())});
        db.close();
    }

    // Deleting whole arduino
    public void deleteTipHistoryByUnit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIP_HISTORY, KEY_ARDUINO_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Getting valves Count
    public int getTipHistoryCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TIP_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Does a tip history with this aid and date exist?
    public boolean hasTipHistory(int aid, String date) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_TIP_HISTORY + " WHERE " + KEY_HIST_DATE + " = \""+date+"\" AND " + KEY_ARDUINO_ID + " = " + aid;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) return true;
        return false;
    }
}
