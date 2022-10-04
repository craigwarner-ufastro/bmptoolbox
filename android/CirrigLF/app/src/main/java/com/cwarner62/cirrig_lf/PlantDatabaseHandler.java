package com.cwarner62.cirrig_lf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PlantDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cirrig_lf";

    // Plants table name
    private static final String TABLE_PLANT = "plant";

    // Plants Table Columns names
    private static final String KEY_PLANT_ID = "plant_id";
    private static final String KEY_MODE = "mode";
    private static final String KEY_VID = "vid";
    private static final String KEY_PLANT_NUMBER = "plantNumber";
    private static final String KEY_DRY = "dry";
    private static final String KEY_WET = "wet";
    private static final String KEY_LEACH_PLUS_TARE = "leachPlusTare";
    private static final String KEY_APP_PLUS_TARE = "appPlusTare";

    private SQLiteDatabase db;

    public PlantDatabaseHandler(Context context) {
        super(context);
        //get persistent read/write connection in constructor
        db = this.getWritableDatabase();
    }

    /** onCreate and onUpgrade are now overridden in base class MyDatabaseHandler
     *	in order to create both plant and zone tables
     */

    public void checkDatabase() {
        //if for some reason the database has closed, reopen it
        if (!db.isOpen()) db = this.getWritableDatabase();
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new plant 
    public void addPlant(Plant plant) {
        if (!db.isOpen()) checkDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MODE, plant.getMode());
        values.put(KEY_VID, plant.getZid());
        values.put(KEY_PLANT_NUMBER, plant.getPlantNumber());
        values.put(KEY_DRY, plant.getDry());
        values.put(KEY_WET, plant.getWet());
        values.put(KEY_LEACH_PLUS_TARE, plant.getLeachPlusTare());
        values.put(KEY_APP_PLUS_TARE, plant.getAppPlusTare());

        // Inserting Row
        db.insert(TABLE_PLANT, null, values);
    }

    //Adding new plants in bulk
    public void addPlants(Vector<Plant> plants) {
        if (!db.isOpen()) checkDatabase();
        System.out.println("PLANT SIZE "+plants.size());
        new Throwable().printStackTrace();
        if (plants.isEmpty()) return;
        String sql = "INSERT INTO " + TABLE_PLANT + "(" + KEY_MODE
                + ", " + KEY_VID + ", " + KEY_PLANT_NUMBER + ", " + KEY_DRY
                + ", " + KEY_WET + ", " + KEY_LEACH_PLUS_TARE + ", " + KEY_APP_PLUS_TARE
                + ") VALUES (?,?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        try {
            db.beginTransaction();
            for (int i = 0; i < plants.size(); i++) {
                Plant currPlant = plants.get(i);
                statement.clearBindings();
                statement.bindLong(1, currPlant.getMode());
                statement.bindLong(2, currPlant.getZid());
                statement.bindLong(3, currPlant.getPlantNumber());
                statement.bindDouble(4, currPlant.getDry());
                statement.bindDouble(5, currPlant.getWet());
                statement.bindDouble(6, currPlant.getLeachPlusTare());
                statement.bindDouble(7, currPlant.getAppPlusTare());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // Getting single plant 
    Plant getPlant(int id) {
        if (!db.isOpen()) checkDatabase();
        Cursor cursor = db.query(TABLE_PLANT, new String[] { KEY_PLANT_ID,
                        KEY_MODE, KEY_VID, KEY_PLANT_NUMBER, KEY_DRY, KEY_WET,
                        KEY_LEACH_PLUS_TARE, KEY_APP_PLUS_TARE }, KEY_PLANT_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Plant plant = new Plant(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
                Integer.parseInt(cursor.getString(3)),
                Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)));
        cursor.close();
        return plant;
    }

    // Getting All Plants
    public List<Plant> getAllPlants() {
        if (!db.isOpen()) checkDatabase();
        List<Plant> plantList = new ArrayList<Plant>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLANT;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Plant plant = new Plant();
                plant.setID(Integer.parseInt(cursor.getString(0)));
                plant.setMode(Integer.parseInt(cursor.getString(1)));
                plant.setZid(Integer.parseInt(cursor.getString(2)));
                plant.setPlantNumber(Integer.parseInt(cursor.getString(3)));
                plant.setDry(Float.parseFloat(cursor.getString(4)));
                plant.setWet(Float.parseFloat(cursor.getString(5)));
                plant.setLeachPlusTare(Float.parseFloat(cursor.getString(6)));
                plant.setAppPlusTare(Float.parseFloat(cursor.getString(7)));

                // Adding plant to list
                plantList.add(plant);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return plant list
        return plantList;
    }

    // Getting All Plants for specific vid
    public List<Plant> getAllPlants(int vid) {
        if (!db.isOpen()) checkDatabase();
        List<Plant> plantList = new ArrayList<Plant>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLANT + " WHERE " + KEY_VID + " = "+vid;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Plant plant = new Plant();
                plant.setID(Integer.parseInt(cursor.getString(0)));
                plant.setMode(Integer.parseInt(cursor.getString(1)));
                plant.setZid(Integer.parseInt(cursor.getString(2)));
                plant.setPlantNumber(Integer.parseInt(cursor.getString(3)));
                plant.setDry(Float.parseFloat(cursor.getString(4)));
                plant.setWet(Float.parseFloat(cursor.getString(5)));
                plant.setLeachPlusTare(Float.parseFloat(cursor.getString(6)));
                plant.setAppPlusTare(Float.parseFloat(cursor.getString(7)));
                // Adding plant to list
                plantList.add(plant);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return plantList;
    }

    // Updating single plant
    public int updatePlant(Plant plant) {
        if (!db.isOpen()) checkDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MODE, plant.getMode());
        values.put(KEY_VID, plant.getZid());
        values.put(KEY_PLANT_NUMBER, plant.getPlantNumber());
        values.put(KEY_DRY, plant.getDry());
        values.put(KEY_WET, plant.getWet());
        values.put(KEY_LEACH_PLUS_TARE, plant.getLeachPlusTare());
        values.put(KEY_APP_PLUS_TARE, plant.getAppPlusTare());

        // updating row
        int retVal = db.update(TABLE_PLANT, values, KEY_PLANT_ID + " = ?",
                new String[]{String.valueOf(plant.getID())});
        //db.close();
        return retVal;
    }

    // Deleting single plant
    public int deletePlant(Plant plant) {
        if (!db.isOpen()) checkDatabase();
        int rows = db.delete(TABLE_PLANT, KEY_PLANT_ID + " = ?",
                new String[] { String.valueOf(plant.getID()) });
        return rows;
    }

    // Getting plant Count
    public int getPlantsCount() {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_PLANT;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Getting plant Count for specific vid
    public int getPlantsCount(int vid) {
        if (!db.isOpen()) checkDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_PLANT + " WHERE " + KEY_VID + " = "+vid;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void close() {
        db.close();
    }
}
