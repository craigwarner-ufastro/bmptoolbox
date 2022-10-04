package com.cwarner62.watertips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class LFHistoryDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 7;

    private SQLiteDatabase db;
    
    public LFHistoryDatabaseHandler(Context context) {
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

    // Adding new history
    public void addLFHistory(LFHistory lfHist) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_ID, lfHist.getAID());
        values.put(KEY_HIST_DATE, lfHist.getDate());
        values.put(KEY_TIP_HIST_ID, lfHist.getTID());
        values.put(KEY_LF_PCT, lfHist.getLfPct());
        values.put(KEY_LFT_PCT, lfHist.getLfTPct());
        values.put(KEY_RT, lfHist.getRt());
        values.put(KEY_RTT, lfHist.getRTT());

        // Inserting Row
        db.insert(TABLE_LF_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    // Getting single valve
    public LFHistory getLFHistory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LF_HISTORY, new String[]{KEY_LF_HIST_ID,
                        KEY_ARDUINO_ID, KEY_TIP_HIST_ID, KEY_HIST_DATE, KEY_LF_PCT,
                        KEY_LFT_PCT, KEY_RT, KEY_RTT},
                        KEY_LF_HIST_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

            LFHistory lfHist = new LFHistory(Integer.parseInt(cursor.getString(0)),
		        Integer.parseInt(cursor.getString(1)),
		        Integer.parseInt(cursor.getString(2)), cursor.getString(3),
                Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)));

        cursor.close();
        // return valve
        return lfHist;
    }

    // Getting All Valves
    public List<LFHistory> getAllLFHistory() {
        List<LFHistory> lfHistList = new ArrayList<LFHistory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LF_HISTORY + " ORDER BY " + KEY_HIST_DATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        System.out.println(cursor.toString());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LFHistory lfHist = new LFHistory();

                lfHist.setID(Integer.parseInt(cursor.getString(0)));
                lfHist.setAID(Integer.parseInt(cursor.getString(1)));
                lfHist.setDate(cursor.getString(2));
                lfHist.setTID(Integer.parseInt(cursor.getString(3)));
                lfHist.setLfPct(Float.parseFloat(cursor.getString(4)));
                lfHist.setLfTPct(Float.parseFloat(cursor.getString(5)));
                lfHist.setRt(Float.parseFloat(cursor.getString(6)));
                lfHist.setRtT(Float.parseFloat(cursor.getString(7)));

                // Adding valve to list
                lfHistList.add(lfHist);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return lfHistList;
    }

    public List<LFHistory> getLFHistoryByUnit(int id) {
        List<LFHistory> lfHistList = new ArrayList<LFHistory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LF_HISTORY + " WHERE " + KEY_ARDUINO_ID +" = " +id + " ORDER BY " + KEY_HIST_DATE + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LFHistory lfHist = new LFHistory();
                lfHist.setID(Integer.parseInt(cursor.getString(0)));
                lfHist.setAID(Integer.parseInt(cursor.getString(1)));
                lfHist.setDate(cursor.getString(2));
                lfHist.setTID(Integer.parseInt(cursor.getString(3)));
                lfHist.setLfPct(Float.parseFloat(cursor.getString(4)));
                lfHist.setLfTPct(Float.parseFloat(cursor.getString(5)));
                lfHist.setRt(Float.parseFloat(cursor.getString(6)));
                lfHist.setRtT(Float.parseFloat(cursor.getString(7)));

                // Adding valve to list
                lfHistList.add(lfHist);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return valve list
        return lfHistList;
    }

    // Updating single valve
    public int updateLFHistory(LFHistory lfHist) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_ID, lfHist.getAID());
        values.put(KEY_TIP_HIST_ID, lfHist.getTID());
        values.put(KEY_HIST_DATE, lfHist.getDate());
        values.put(KEY_LF_PCT, lfHist.getLfPct());
        values.put(KEY_LFT_PCT, lfHist.getLfTPct());
        values.put(KEY_RT, lfHist.getRt());
        values.put(KEY_RTT, lfHist.getRTT());

        // updating row
        return db.update(TABLE_LF_HISTORY, values, KEY_LF_HIST_ID + " = ?",
                new String[]{String.valueOf(lfHist.getID())});
    }

    // Deleting single valve
    public void deleteLFHistory(LFHistory lfHist) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LF_HISTORY, KEY_LF_HIST_ID + " = ?",
                new String[]{String.valueOf(lfHist.getID())});
        db.close();
    }


    // Deleting whole arduino
    public void deleteLFHistoryByUnit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LF_HISTORY, KEY_ARDUINO_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Getting valves Count
    public int getLFHistoryCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LF_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Does a LF history with this aid and tid exist?
    public boolean hasLFHistory(int aid, int tid) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_LF_HISTORY + " WHERE " + KEY_TIP_HIST_ID + " = "+ tid + " AND " + KEY_ARDUINO_ID + " = " + aid;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) return true;
        return false;
    }

    // Getting single valve
    public float getMostRecenRtt(int id, String date) {
        if (!db.isOpen()) checkDatabase();
        String selectQuery = "SELECT  " + KEY_RTT + " FROM " + TABLE_LF_HISTORY + " WHERE " + KEY_ARDUINO_ID +" = " +id + " AND " +
                    KEY_HIST_DATE + " < DATE('" + date + "') ORDER BY " + KEY_HIST_DATE + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        float rtt = -1;

        if (cursor.moveToFirst()) {
            rtt = Float.parseFloat(cursor.getString(0));
        }
        return rtt;
    }

}
