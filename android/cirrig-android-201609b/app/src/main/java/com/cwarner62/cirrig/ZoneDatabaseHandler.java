package com.cwarner62.cirrig;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ZoneDatabaseHandler extends MyDatabaseHandler {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 13;

    // Database Name
    private static final String DATABASE_NAME = "cirrig";

    // Zones table name
    private static final String TABLE_ZONE = "zones";

    // Zones Table Columns names
    private static final String KEY_ID = "id";
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

    //Added new Columns names
    private static final String KEY_ETO ="eto";
    private static final String KEY_SOLAR = "solar";
    private static final String KEY_TMAX = "Tmax";
    private static final String KEY_TMIN = "Tmin";
    private static final String KEY_RAIN = "rain";

    public ZoneDatabaseHandler(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context);
    }

    /** onCreate and onUpgrade are now overridden in base class MyDatabaseHandler
     *      in order to create both weather and zone tables
     */

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new zone
    void addZone(Zone zone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ZONE_NUMBER, zone.getZoneNumber());
        values.put(KEY_IRRIG_CAPTURE, zone.getIrrigCapture());
        values.put(KEY_SPACING, zone.getSpacing());
        values.put(KEY_CONT_DIAM, zone.getContainerDiam());
        values.put(KEY_IRRIG_IN_HR, zone.getIrrigRate());
        values.put(KEY_IRRIG_UNIFORMITY, zone.getIrrigUniformity());
        values.put(KEY_PLANT_HEIGHT, zone.getPlantHeight());
        values.put(KEY_PLANT_WIDTH, zone.getPlantWidth());
        values.put(KEY_PCT_COVER, zone.getPctCover());
        values.put(KEY_CONT_SPACING, zone.getContSpacing());
        values.put(KEY_LEACHING_FRACTION, zone.getLeachingFraction());
        values.put(KEY_CANOPY_DENSITY, zone.getCanopyDensity());
        values.put(KEY_PLANT_WIDTH_FACTOR, zone.getPlantWidthFactor());
        values.put(KEY_ETO, zone.getEto());
        values.put(KEY_SOLAR, zone.getSolar());
        values.put(KEY_TMAX, zone.getTmax());
        values.put(KEY_TMIN, zone.getTmin());
        values.put(KEY_RAIN, zone.getRain());

        // Inserting Row
        db.insert(TABLE_ZONE, null, values);
        db.close(); // Closing database connection
    }

    // Getting single zone
    Zone getZone(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ZONE, new String[]{KEY_ID,
                        KEY_ZONE_NUMBER, KEY_IRRIG_CAPTURE, KEY_SPACING,
                        KEY_CONT_DIAM, KEY_IRRIG_IN_HR, KEY_IRRIG_UNIFORMITY,
                        KEY_PLANT_HEIGHT, KEY_PLANT_WIDTH, KEY_PCT_COVER,
                        KEY_CONT_SPACING, KEY_LEACHING_FRACTION,
                        KEY_CANOPY_DENSITY, KEY_PLANT_WIDTH_FACTOR,
                        KEY_ETO, KEY_SOLAR, KEY_TMAX, KEY_TMIN, KEY_RAIN}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Zone zone = new Zone(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
                Integer.parseInt(cursor.getString(3)), Float.parseFloat(cursor.getString(4)),
                Float.parseFloat(cursor.getString(5)), Float.parseFloat(cursor.getString(6)),
                Float.parseFloat(cursor.getString(7)), Float.parseFloat(cursor.getString(8)),
                Float.parseFloat(cursor.getString(9)), Float.parseFloat(cursor.getString(10)),
                Float.parseFloat(cursor.getString(11)), Integer.parseInt(cursor.getString(12)),
                Float.parseFloat(cursor.getString(13)), Float.parseFloat(cursor.getString(14)),
                Float.parseFloat(cursor.getString(15)), Float.parseFloat(cursor.getString(16)),
                Float.parseFloat(cursor.getString(17)), Float.parseFloat(cursor.getString(18)));

        cursor.close();
        // return zone
        return zone;
    }

    // Getting All Zones
    public List<Zone> getAllZones() {
        List<Zone> zoneList = new ArrayList<Zone>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ZONE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Zone zone = new Zone();

                zone.setID(Integer.parseInt(cursor.getString(0)));
                zone.setZoneNumber(Integer.parseInt(cursor.getString(1)));
                zone.setIrrigCapture(Integer.parseInt(cursor.getString(2)));
                zone.setSpacing(Integer.parseInt(cursor.getString(3)));
                zone.setContainerDiam(Float.parseFloat(cursor.getString(4)));
                zone.setIrrigRate(Float.parseFloat(cursor.getString(5)));
                zone.setIrrigUniformity(Float.parseFloat(cursor.getString(6)));
                zone.setPlantHeight(Float.parseFloat(cursor.getString(7)));
                zone.setPlantWidth(Float.parseFloat(cursor.getString(8)));
                zone.setPctCover(Float.parseFloat(cursor.getString(9)));
                zone.setContSpacing(Float.parseFloat(cursor.getString(10)));
                zone.setLeachingFraction(Float.parseFloat(cursor.getString(11)));
                zone.setCanopyDensity(Integer.parseInt(cursor.getString(12)));
                zone.setPlantWidthFactor(Float.parseFloat(cursor.getString(13)));
                zone.setEto(Float.parseFloat(cursor.getString(14)));
                zone.setSolar(Float.parseFloat(cursor.getString(15)));
                zone.setTmax(Float.parseFloat(cursor.getString(16)));
                zone.setTmin(Float.parseFloat(cursor.getString(17)));
                zone.setRain(Float.parseFloat(cursor.getString(18)));

                // Adding zone to list
                zoneList.add(zone);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return zone list
        return zoneList;
    }

    // Getting zones for specified container size
    public List<Zone> getZones(float containerDiam) {
        List<Zone> zoneList = new ArrayList<Zone>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ZONE + " WHERE " + KEY_CONT_DIAM + " = " + containerDiam + " ORDER BY " + KEY_ZONE_NUMBER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Zone zone = new Zone();

                zone.setID(Integer.parseInt(cursor.getString(0)));
                zone.setZoneNumber(Integer.parseInt(cursor.getString(1)));
                zone.setIrrigCapture(Integer.parseInt(cursor.getString(2)));
                zone.setSpacing(Integer.parseInt(cursor.getString(3)));
                zone.setContainerDiam(Float.parseFloat(cursor.getString(4)));
                zone.setIrrigRate(Float.parseFloat(cursor.getString(5)));
                zone.setIrrigUniformity(Float.parseFloat(cursor.getString(6)));
                zone.setPlantHeight(Float.parseFloat(cursor.getString(7)));
                zone.setPlantWidth(Float.parseFloat(cursor.getString(8)));
                zone.setPctCover(Float.parseFloat(cursor.getString(9)));
                zone.setContSpacing(Float.parseFloat(cursor.getString(10)));
                zone.setLeachingFraction(Float.parseFloat(cursor.getString(11)));
                zone.setCanopyDensity(Integer.parseInt(cursor.getString(12)));
                zone.setPlantWidthFactor(Float.parseFloat(cursor.getString(13)));
                zone.setEto(Float.parseFloat(cursor.getString(14)));
                zone.setSolar(Float.parseFloat(cursor.getString(15)));
                zone.setTmax(Float.parseFloat(cursor.getString(16)));
                zone.setTmin(Float.parseFloat(cursor.getString(17)));
                zone.setRain(Float.parseFloat(cursor.getString(18)));

                // Adding zone to list
                zoneList.add(zone);

            } while (cursor.moveToNext());
        }
        cursor.close();

        // return zone list
        return zoneList;
    }


    // Updating single zone
    public int updateZone(Zone zone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ZONE_NUMBER, zone.getZoneNumber());
        values.put(KEY_IRRIG_CAPTURE, zone.getIrrigCapture());
        values.put(KEY_SPACING, zone.getSpacing());
        values.put(KEY_CONT_DIAM, zone.getContainerDiam());
        values.put(KEY_IRRIG_IN_HR, zone.getIrrigRate());
        values.put(KEY_IRRIG_UNIFORMITY, zone.getIrrigUniformity());
        values.put(KEY_PLANT_HEIGHT, zone.getPlantHeight());
        values.put(KEY_PLANT_WIDTH, zone.getPlantWidth());
        values.put(KEY_PCT_COVER, zone.getPctCover());
        values.put(KEY_CONT_SPACING, zone.getContSpacing());
        values.put(KEY_LEACHING_FRACTION, zone.getLeachingFraction());
        values.put(KEY_CANOPY_DENSITY, zone.getCanopyDensity());
        values.put(KEY_PLANT_WIDTH_FACTOR, zone.getPlantWidthFactor());
        values.put(KEY_ETO, zone.getEto());
        values.put(KEY_SOLAR, zone.getSolar());
        values.put(KEY_TMAX, zone.getTmax());
        values.put(KEY_TMIN, zone.getTmin());
        values.put(KEY_RAIN, zone.getRain());

        // updating row
        return db.update(TABLE_ZONE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(zone.getID())});
    }

    // Deleting single zone
    public void deleteZone(Zone zone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ZONE, KEY_ID + " = ?",
                new String[]{String.valueOf(zone.getID())});
        db.close();
    }


    // Getting zones Count
    public int getZonesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ZONE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

}
