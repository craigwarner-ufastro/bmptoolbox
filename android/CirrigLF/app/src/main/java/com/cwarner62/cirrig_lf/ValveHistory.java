package com.cwarner62.cirrig_lf;

import java.text.SimpleDateFormat;
import java.util.Date;

//Class to represent a Valve
public class ValveHistory {

    private int _id, vid;
    private float lfPct;
    private String plantName, date;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public ValveHistory() {
    }

    //create empty Valve History
    public ValveHistory(int vid) {
        this.vid = vid;
    }

    //creates a Valve History 
    public ValveHistory(int vid, String plantName, float lfPct) {
        this.vid = vid;
        this.plantName = plantName;
        this.lfPct = lfPct;
        Date d = new Date();
        this.date = df.format(date);
    }

    public ValveHistory(Valve v, float lfPct) {
        this.vid = v.getID();
        this.plantName = v.getPlantName();
        this.lfPct = lfPct;
        Date d = new Date();
        this.date = df.format(date);
    }

    public ValveHistory(int vid, String plantName, String date, float lfPct) {
        this.vid = vid;
        this.plantName = plantName;
        this.lfPct = lfPct;
        this.date = date;
    }

    public ValveHistory(Valve v, String date, float lfPct) {
        this.vid = v.getID();
        this.plantName = v.getPlantName();
        this.lfPct = lfPct;
        this.date = date;
    }

    //Create a Valve History from all values in database
    public ValveHistory(int _id, int vid, String plantName, String date, float lfPct) {
        this._id = _id;
        this.vid = vid;
        this.plantName = plantName;
        this.lfPct = lfPct;
        this.date = date;
    }

    public ValveHistory(int _id, int vid, String plantName, float lfPct) {
        this._id = _id;
        this.vid = vid;
        this.plantName = plantName;
        this.lfPct = lfPct;
        Date d = new Date();
        this.date = df.format(date);
    }


    /* getters and setters */
    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public int getVID() {
        return this.vid;
    }

    public String getPlantName() {
        return this.plantName;
    }

    public String getDate() {
        return this.date;
    }

    public float getLFPct() {
        return this.lfPct;
    }

    public void setVID(int vid) {
        this.vid = vid;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLFPct(float lfPct) {
        this.lfPct = lfPct;
    }

    //for debugging purpose
    public String printAllValue(){

        String listAllValue = "valveHistroy for valve id "+ getVID()
                + ", plantName is " + getPlantName() + ", date is "+ getDate() + ", LF Pct is "+ getLFPct()
                + ", ID is " + getID();

        System.out.println(listAllValue);
        return listAllValue;
    }

}
