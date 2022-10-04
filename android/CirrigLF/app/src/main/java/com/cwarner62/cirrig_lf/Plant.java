package com.cwarner62.cirrig_lf;

//Class for an individual plant within a valve
public class Plant {
    public static int MODE_SPRINKLER = 0;
    public static int MODE_MICRO = 1;

    private int _id, vid, plantNumber, mode;
    private float dry, wet, leachPlusTare;
    private float appPlusTare;

    public Plant() {
    }

    //create from specified components
    public Plant(int vid, int plantNumber, float dry,
                            float wet, float leachPlusTare) {
        this.vid = vid;
        this.plantNumber = plantNumber;
        this.mode = MODE_SPRINKLER;
        this.dry = dry;
        this.wet = wet;
        this.leachPlusTare = leachPlusTare;
        this.appPlusTare = 0;
    }

    public Plant(int vid, int plantNumber, float appPlusTare, float leachPlusTare) {
        this.vid = vid;
        this.plantNumber = plantNumber;
        this.mode = MODE_MICRO;
        this.dry = 0;
        this.wet = 0;
        this.leachPlusTare = leachPlusTare;
        this.appPlusTare = appPlusTare;
    }

    //create from database with all fields defined
    public Plant(int id, int mode, int vid, int plantNumber, float dry, float wet, float leachPlusTare, float appPlusTare) {
        this._id = id;
        this.mode = mode;
        this.vid = vid;
        this.plantNumber = plantNumber;
        this.dry = dry;
        this.wet = wet;
        this.leachPlusTare = leachPlusTare;
        this.appPlusTare = appPlusTare;
    } 

    //create from a line in csv file
    public Plant(String csvLine) {
        String[] tokens = csvLine.split(",");
        vid = Integer.parseInt(tokens[0]);
        mode = Integer.parseInt(tokens[1]);
        plantNumber = Integer.parseInt(tokens[2]);
        if (mode == MODE_SPRINKLER) {
            dry = Float.parseFloat(tokens[3]);
            wet = Float.parseFloat(tokens[4]);
            leachPlusTare = Float.parseFloat(tokens[5]);
            appPlusTare = 0;
        } else if (mode == MODE_MICRO) {
            appPlusTare = Float.parseFloat(tokens[4]);
            leachPlusTare = Float.parseFloat(tokens[5]);
            dry = 0;
            wet = 0;
        }
    }

    /* getters and setters */

    public int getID() { return this._id; }

    public void setID(int id) { this._id = id; }

    public int getZid() { return this.vid; }

    public void setZid(int vid) { this.vid = vid; }

    public int getMode() { return this.mode; }

    public void setMode(int mode) { this.mode = mode; }

    public int getPlantNumber() { return this.plantNumber; }

    public void setPlantNumber(int plantNumber) { this.plantNumber = plantNumber; }

    public float getDry() { return this.dry; }

    public void setDry(float dry) { this.dry = dry; }

    public float getWet() { return this.wet; }

    public void setWet(float wet) { this.wet = wet; }

    public float getLeachPlusTare() { return this.leachPlusTare; }

    public void setLeachPlusTare(float leachPlusTare) { this.leachPlusTare = leachPlusTare; }

    public float getAppPlusTare() { return this.appPlusTare; }

    public void setAppPlusTare(float appPlusTare) { this.appPlusTare = appPlusTare; }

    public String toString() {
        String s = "Plant [id=" + _id;
        s += "; vid=" + vid + "; plantNumber=" + plantNumber;
        if (mode == MODE_SPRINKLER) {
            s += "; mode=SPRINKLER; dry="+dry+"; wet="+wet+"; lchPlusTare="+leachPlusTare;
        } else if (mode == MODE_MICRO) {
            s += "; mode=MICRO; appPlusTare="+appPlusTare+"; lchPlusTare="+leachPlusTare;
        }
        s += "]";
        return s;
    }
}
