package com.cwarner62.cirrig_lf;

//Class to represent a Valve
public class Valve {
    public static int MODE_SPRINKLER = 0;
    public static int MODE_MICRO = 1;

    private int _id, irrMode, numPlants = 3;
    private boolean todo, done;
    private float lfTarget, lchPlusTare, appPlusTare;
    private String valveName, plantName, date, time;

    public Valve() {
    }

    //create empty Valve
    public Valve(String valveName) {
        this.valveName = valveName;
    }

    //creates a "default" Valve from basic, hard-coded characteristics
    public Valve(String valveName, String plantName, int irrMode) {
        this.valveName = valveName;
        this.plantName = plantName;
        this.irrMode = irrMode;
    }


    //Create a Valve from all values in database
    public Valve(String valveName, String plantName, int irrMode, boolean todo, boolean done, String date, String time, float lfTarget, float lchPlusTare, float appPlusTare, int numPlants) {
        this.valveName = valveName;
        this.plantName = plantName;
        this.irrMode = irrMode;
        this.todo = todo;
        this.done = done;
        this.date = date;
        this.time = time;
        this.lfTarget = lfTarget;
        this.lchPlusTare = lchPlusTare;
        this.appPlusTare = appPlusTare;
        this.numPlants = numPlants;
    }

    //Create a Valve from all values in database
    public Valve(int _id, String valveName, String plantName, int irrMode, boolean todo, boolean done, String date, String time, float lfTarget, float lchPlusTare, float appPlusTare, int numPlants) {
        this._id = _id;
        this.valveName = valveName;
        this.plantName = plantName;
        this.irrMode = irrMode;
        this.todo = todo;
        this.done = done;
        this.date = date;
        this.time = time;
        this.lfTarget = lfTarget;
        this.lchPlusTare = lchPlusTare;
        this.appPlusTare = appPlusTare;
        this.numPlants = numPlants;
    }

    /* getters and setters */
    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public String getName() {
        return this.valveName;
    }

    public String getPlantName() {
        return this.plantName;
    }

    public int getIrrMode() {
        return this.irrMode;
    }

    public boolean getTodo() {
        return this.todo;
    }

    public boolean getDone() {
        return this.done;
    }

    public String getDate() {
        return this.date;
    }

    public String getTime() {
        return this.time;
    }

    public float getLFTarget() {
        return this.lfTarget;
    }

    public float getLchPlusTare() {
        return this.lchPlusTare;
    }

    public float getAppPlusTare() {
        return this.appPlusTare;
    }

    public int getNumPlants() {
        return this.numPlants;
    }

    public void setName(String valveName) {
        this.valveName = valveName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public void setIrrMode(int irrMode) {
        this.irrMode = irrMode;
    }

    public void setTodo(boolean todo) {
        this.todo = todo;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLFTarget(float lfTarget) {
        this.lfTarget = lfTarget;
    }

    public void setLchPlusTare(float lchPlusTare) {
        this.lchPlusTare = lchPlusTare;
    }

    public void setAppPlusTare(float appPlusTare) {
        this.appPlusTare = appPlusTare;
    }

    public void setNumPlants(int numPlants) {
        this.numPlants = numPlants;
    }

    //for debugging purpose
    public String printAllValue() {

        String listAllValue = "valveName is " + this.valveName
                + ", plantName is " + getPlantName() + ", irrMode is " + getIrrMode()
                + ", todo is " + getTodo() + ", done is " + getDone()
                + ", date is " + getDate() + ", time is " + getTime()
                + ", LF Target % is " + getLFTarget() + ", LchPlusTare is " + getLchPlusTare()
                + ", AppPlusTare is " + getAppPlusTare() + ", NumPlants is " + getNumPlants()
                + ", ID is " + getID();

        System.out.println(listAllValue);
        return listAllValue;
    }

}
