package com.cwarner62.watertips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ArduinoDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version

    private static final int DATABASE_VERSION = 7;

    private SQLiteDatabase db;

    public ArduinoDatabaseHandler(Context context) {
        super(context);
        //get persistent read/write connection in constructor
        db = this.getWritableDatabase();
    }

    /** onCreate and onUpgrade are now overridden in base class MyDatabaseHandler
     *	in order to create both arduino and zone tables
     */

    public void checkDatabase() {
        //if for some reason the database has closed, reopen it
        if (!db.isOpen()) db = this.getWritableDatabase();
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new arduino 
    public void addArduinoUnit(ArduinoUnit arduino) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_MAC_ADDRESS, arduino.getMac());
        values.put(KEY_UNIT_NAME, arduino.getName());
        values.put(KEY_UNIT_DESC, arduino.getDesc());
        values.put(KEY_UNIT_TYPE, arduino.getType());
        values.put(KEY_IRRIG_RATE, arduino.getIrrigRate());
        values.put(KEY_TARGET_LF, arduino.getTargetLF());
        values.put(KEY_UNIT_RUNTIME, arduino.getRuntime());
        values.put(KEY_CONTAINER_DIAM, arduino.getContainerDiam());

        // Inserting Row
        db.insert(TABLE_ARDUINO, null, values);
    }

    //Adding new arduinos in bulk
    public void addArduinoUnits(Vector<ArduinoUnit> arduinos) {
        if (!db.isOpen()) checkDatabase();
        //new Throwable().printStackTrace();
        if (arduinos.isEmpty()) return;
        String sql = "INSERT INTO " + TABLE_ARDUINO + "(" + KEY_ARDUINO_MAC_ADDRESS
                + ", " + KEY_UNIT_NAME + ", " + KEY_UNIT_DESC + ", " + KEY_UNIT_TYPE
                + ", " + KEY_IRRIG_RATE + ", " + KEY_TARGET_LF + ", " + KEY_UNIT_RUNTIME
                + ", " + KEY_CONTAINER_DIAM
                + ") VALUES (?,?,?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        try {
            db.beginTransaction();
            for (int i = 0; i < arduinos.size(); i++) {
                ArduinoUnit currArduinoUnit = arduinos.get(i);
                statement.clearBindings();
                statement.bindString(1, currArduinoUnit.getMac());
                statement.bindString(2, currArduinoUnit.getName());
                statement.bindString(3, currArduinoUnit.getDesc());
                statement.bindLong(4, currArduinoUnit.getType());
                statement.bindDouble(5, currArduinoUnit.getIrrigRate());
                statement.bindDouble(6, currArduinoUnit.getTargetLF());
                statement.bindDouble(7, currArduinoUnit.getRuntime());
                statement.bindDouble(8, currArduinoUnit.getContainerDiam());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // Getting single arduino 
    public ArduinoUnit getArduinoUnit(int id) {
        if (!db.isOpen()) checkDatabase();
        Cursor cursor = db.query(TABLE_ARDUINO, new String[] { KEY_ARDUINO_ID,
                        KEY_ARDUINO_MAC_ADDRESS, KEY_UNIT_NAME, KEY_UNIT_DESC, KEY_UNIT_TYPE, KEY_IRRIG_RATE,
                        KEY_TARGET_LF, KEY_UNIT_RUNTIME, KEY_CONTAINER_DIAM, KEY_DISABLE_TIP1,
                        KEY_DISABLE_TIP2, KEY_DISABLE_TIP3, KEY_DISABLE_TIP4 }, KEY_ARDUINO_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        ArduinoUnit arduino;

        try {
            arduino = new ArduinoUnit(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3),
                    Integer.parseInt(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                    Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)),
                    Float.parseFloat(cursor.getString(8)), Integer.parseInt(cursor.getString(9)),
                    Integer.parseInt(cursor.getString(10)), Integer.parseInt(cursor.getString(11)),
                    Integer.parseInt(cursor.getString(12)));
        } catch (Exception e) {
            arduino = null;
        }
        cursor.close();
        return arduino;
    }

    // Getting single arduino
    public ArduinoUnit getArduinoUnitByMAC(String mac) {
        if (!db.isOpen()) checkDatabase();
        Cursor cursor = db.query(TABLE_ARDUINO, new String[] { KEY_ARDUINO_ID,
                        KEY_ARDUINO_MAC_ADDRESS, KEY_UNIT_NAME, KEY_UNIT_DESC, KEY_UNIT_TYPE, KEY_IRRIG_RATE,
                        KEY_TARGET_LF, KEY_UNIT_RUNTIME, KEY_CONTAINER_DIAM, KEY_DISABLE_TIP1,
                        KEY_DISABLE_TIP2, KEY_DISABLE_TIP3, KEY_DISABLE_TIP4 }, KEY_ARDUINO_MAC_ADDRESS + "=?",
                new String[] { mac }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ArduinoUnit arduino = new ArduinoUnit(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),
                cursor.getString(3),
                Integer.parseInt(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)),
                Float.parseFloat(cursor.getString(8)), Integer.parseInt(cursor.getString(9)),
                Integer.parseInt(cursor.getString(10)), Integer.parseInt(cursor.getString(11)),
                Integer.parseInt(cursor.getString(12)));
        cursor.close();
        return arduino;
    }

    // Getting All ArduinoUnits
    public List<ArduinoUnit> getAllArduinoUnits() {
        if (!db.isOpen()) checkDatabase();
        List<ArduinoUnit> arduinoList = new ArrayList<ArduinoUnit>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ARDUINO + " ORDER BY " + KEY_ARDUINO_ID;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ArduinoUnit arduino = new ArduinoUnit();
                arduino.setID(Integer.parseInt(cursor.getString(0)));
                arduino.setMac(cursor.getString(1));
                arduino.setName(cursor.getString(2));
                arduino.setDesc(cursor.getString(3));
                arduino.setType(Integer.parseInt(cursor.getString(4)));
                arduino.setIrrigRate(Float.parseFloat(cursor.getString(5)));
                arduino.setTargetLF(Float.parseFloat(cursor.getString(6)));
                arduino.setRuntime(Float.parseFloat(cursor.getString(7)));
                arduino.setContainerDiam(Float.parseFloat(cursor.getString(8)));

                // Adding arduino to list
                arduinoList.add(arduino);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return arduino list
        return arduinoList;
    }

    // Getting All ArduinoUnits
    public List<ArduinoUnit> getAllArduinoUnitsByName() {
        if (!db.isOpen()) checkDatabase();
        List<ArduinoUnit> arduinoList = new ArrayList<ArduinoUnit>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ARDUINO + " ORDER BY lower(" + KEY_UNIT_NAME+")";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ArduinoUnit arduino = new ArduinoUnit();
                arduino.setID(Integer.parseInt(cursor.getString(0)));
                arduino.setMac(cursor.getString(1));
                arduino.setName(cursor.getString(2));
                arduino.setDesc(cursor.getString(3));
                arduino.setType(Integer.parseInt(cursor.getString(4)));
                arduino.setIrrigRate(Float.parseFloat(cursor.getString(5)));
                arduino.setTargetLF(Float.parseFloat(cursor.getString(6)));
                arduino.setRuntime(Float.parseFloat(cursor.getString(7)));
                arduino.setContainerDiam(Float.parseFloat(cursor.getString(8)));

                // Adding arduino to list
                arduinoList.add(arduino);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return arduino list
        return arduinoList;
    }

    // Updating single arduino
    public int updateArduinoUnit(ArduinoUnit arduino) {
        if (!db.isOpen()) checkDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARDUINO_MAC_ADDRESS, arduino.getMac());
        values.put(KEY_UNIT_NAME, arduino.getName());
        values.put(KEY_UNIT_DESC, arduino.getDesc());
        values.put(KEY_UNIT_TYPE, arduino.getType());
        values.put(KEY_IRRIG_RATE, arduino.getIrrigRate());
        values.put(KEY_TARGET_LF, arduino.getTargetLF());
        values.put(KEY_UNIT_RUNTIME, arduino.getRuntime());
        values.put(KEY_CONTAINER_DIAM, arduino.getContainerDiam());
        values.put(KEY_DISABLE_TIP1, arduino.getTipperDisableStatus(0));
        values.put(KEY_DISABLE_TIP2, arduino.getTipperDisableStatus(1));
        values.put(KEY_DISABLE_TIP3, arduino.getTipperDisableStatus(2));
        values.put(KEY_DISABLE_TIP4, arduino.getTipperDisableStatus(3));

        // updating row
        int retVal = db.update(TABLE_ARDUINO, values, KEY_ARDUINO_ID + " = ?",
                new String[]{String.valueOf(arduino.getID())});
        //db.close();
        return retVal;
    }

    // Deleting single arduino
    public int deleteArduinoUnit(ArduinoUnit arduino) {
        if (!db.isOpen()) checkDatabase();
        int rows = db.delete(TABLE_ARDUINO, KEY_ARDUINO_ID + " = ?",
                new String[] { String.valueOf(arduino.getID()) });
        return rows;
    }

    // Getting arduino Count
    public int getArduinoUnitsCount() {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_ARDUINO;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Getting arduino Count for specific id
    public int getArduinoUnitsCount(int id) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_ARDUINO + " WHERE " + KEY_ARDUINO_ID + " = "+id;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Is arduino with this MAC address in db?
    public boolean hasArduinoWithMAC(String macAddress) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_ARDUINO + " WHERE " + KEY_ARDUINO_MAC_ADDRESS + " = \""+macAddress+"\"";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) return true;
        return false;
    }
    public void close() {
        db.close();
    }
}
