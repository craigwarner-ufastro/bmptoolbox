package com.cwarner62.watertips;

import java.text.SimpleDateFormat;
import java.util.Date;

//Class to represent LF History
public class LFHistory {

    private int _id, aid, tid;
    private float lfPct, lfTPct, rt, rtT;
    private String date;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public LFHistory() {
    }

    //create empty LF History
    public LFHistory(int aid) {
        this.aid = aid;
        Date d = new Date();
        this.date = df.format(d);
    }


    public LFHistory(ArduinoUnit arduino) {
        this(arduino.getID());
    }

    //Create LF History from all values in database
    public LFHistory(int _id, int aid, int tid, String date, float lfPct, float lfTPct, float rt, float rtT) {
        this._id = _id;
        this.aid = aid;
        this.tid = tid;
        this.date = date;
        this.lfPct = lfPct;
        this.lfTPct = lfTPct;
        this.rt = rt;
        this.rtT = rtT;
    }

    /* getters and setters */
    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public int getAID() {
        return this.aid;
    }

    public void setAID(int aid) { this.aid = aid; }

    public int getTID() {
        return this.tid;
    }

    public void setTID(int tid) { this.tid = tid; }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getLfPct() { return this.lfPct; }

    public void setLfPct(float lfPct) { this.lfPct = lfPct; }

    public float getLfTPct() { return this.lfTPct; }

    public void setLfTPct(float lfTPct) { this.lfTPct = lfTPct; }

    public float getRt() { return this.rt; }

    public void setRt(float rt) { this.rt = rt; }

    public float getRTT() { return this.rtT; }

    public void setRtT(float rtT) { this.rtT = rtT; }

    //for debugging purpose
    public String printAllValues(){
        String listAllValue = "LF History for arduino id "+ getAID()
                + ", tip id " + getTID() + ", date is "+ getDate()
                + ": lfPct = " + getLfPct()+"; lfTPct = " + getLfTPct()
                + "; rt = " + getRt() + "; rtT = " + getRTT();
        System.out.println(listAllValue);
        return listAllValue;
    }

}
