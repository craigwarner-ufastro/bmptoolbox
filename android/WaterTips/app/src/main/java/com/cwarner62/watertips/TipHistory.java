package com.cwarner62.watertips;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//Class to represent a Tip History
public class TipHistory {

    private int _id, aid;
    private int[] counts = new int[4];
    private String date;
    private boolean _isValid = false;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");


    public TipHistory() {
    }

    //create empty Tip  History
    public TipHistory(int aid) {
        this.aid = aid;
        for (int j = 0; j < 4; j++) counts[j] = 0;
        Date d = new Date();
        this.date = df.format(date);
        _isValid = true;
    }


    public TipHistory(ArduinoUnit arduino) {
        this(arduino.getID());
    }

    public TipHistory(int aid, String date, int c1, int c2, int c3, int c4) {
        this.aid = aid;
        this.date = date;
        counts[0] = c1;
        counts[1] = c2;
        counts[2] = c3;
        counts[3] = c4;
        _isValid = true;
    }

    public TipHistory(ArduinoUnit arduino, String date, int c1, int c2, int c3, int c4) {
        this.aid = arduino.getID();
        this.date = date;
        counts[0] = c1;
        counts[1] = c2;
        counts[2] = c3;
        counts[3] = c4;
        _isValid = true;
    }

    //Create a Tip History from all values in database
    public TipHistory(int _id, int aid, String date, int c1, int c2, int c3, int c4) {
        this._id = _id;
        this.aid = aid;
        this.date = date;
        counts[0] = c1;
        counts[1] = c2;
        counts[2] = c3;
        counts[3] = c4;
        _isValid = true;
    }

    //Create a Tip History from a string buffer received from arduino
    /* Format:
    DOM
    day
    month
    TIP_0::HIST::counts
    TIP_1::HIST::counts
    TIP_2::HIST::counts
    TIP_3::HIST::counts
     *** 8/2 new format
     H::day::month::t0::t1::t2::t3::E
     */
    public TipHistory(String bufString) {
        System.out.println("TIP HISTORY> "+bufString);
        String[] responses = bufString.trim().split("::");
        SimpleDateFormat yearDF = new SimpleDateFormat("yyyy");
        Date date = new Date();
        String yyyy = yearDF.format(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH)+1; //1 - 12
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        String mm, dd;
        int count = 0;
        if (responses.length != 8) return;
        if (!responses[0].equals("H")) return;
        if (!responses[7].equals("E")) return;
        dd = responses[1].trim();
        mm = responses[2].trim();
        if (dd.length() < 2) dd = "0"+dd;
        if (mm.length() < 2) mm = "0"+mm;
        if (mm.equals("12") && month == 1) {
            //It is January and this history is from December - decrement year
            yyyy = String.valueOf(year-1);
        }
        this.date = yyyy+"-"+mm+"-"+dd;
        count++;

        for (int j = 3; j < 7; j++) {
            responses[j] = responses[j].trim();
            try {
                int c = Integer.parseInt(responses[j]);
                if (c >= 0) {
                    counts[j-3] = c;
                    count++;
                }
            } catch (Exception e) {
                continue;
            }
        }
        if (count == 5) _isValid = true;
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

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCounts(int i) {
        if (i < 0 || i > 3) return -1;
        return counts[i];
    }

    public void setCounts(int i, int c) {
        if (i < 0 || i > 3) return;
        counts[i] = c;
    }

    public float getAvgCounts() {
        float avg = 0.f;
        int ntippers = 0;
        for (int j = 0; j < 4; j++) {
            if (counts[j] > 0) {
                avg += counts[j];
                ntippers++;
            }
        }
        if (ntippers == 0) return 0;
        //float denominator = (float)ntippers;
        float denominator = 4;
        //possibility of 3 tippers?  Look at nonzero avg and zero counts[3]
        avg /= denominator;
        return avg;
    }

    public boolean valid() { return _isValid; }

    //for debugging purpose
    public String toString(){
        String listAllValue = "tipHistory for arduino id "+ getAID()
                + ", date is "+ getDate() + ":\n";
        for (int j = 0; j < 4; j++) listAllValue += "\tTipper "+(j+1)+": "+counts[j]+"\n";
        System.out.println(listAllValue);
        return listAllValue;
    }

}
