package com.cwarner62.watertips;

//Class for an individual plant within a valve
public class ArduinoUnit {
    public static int MODE_SPRINKLER = 0;
    public static int MODE_MICRO = 1;

    private int _id, unitType;
    private String macAddress, unitName, unitDesc;
    private float irrigRate, targetLF, runtime, containerDiam;
    private int[] disableTips = new int[4];

    public ArduinoUnit() {
    }


    //create from database with all fields defined
    public ArduinoUnit(int id, String mac, String name, String desc, int type, float rate, float lf, float rt, float containerDiam, int dt1, int dt2, int dt3, int dt4) {
        this._id = id;
        this.macAddress = mac;
        this.unitName = name;
        this.unitDesc = desc;
        this.unitType = type;
        this.irrigRate = rate;
        this.targetLF = lf;
        this.runtime = rt;
        this.containerDiam = containerDiam;
        this.disableTips[0] = dt1;
        this.disableTips[1] = dt2;
        this.disableTips[2] = dt3;
        this.disableTips[3] = dt4;
    }

    public ArduinoUnit(int id, String mac, String name, String desc, int type, float rate, float lf, float rt, float containerDiam) {
        this(id, mac, name, desc, type, rate, lf, rt, containerDiam, 0, 0, 0, 0);

    }

    //create from database with all fields defined
    public ArduinoUnit(int id, String mac, String name, String desc, int type, float rate, float lf, float rt) {
        this(id, mac, name, desc, type, rate, lf, rt, 0);
    }

    //create from specified components
    public ArduinoUnit(String mac, String name, String desc) {
        this(0, mac, name, desc, 0, 0, 0, 0, 0);
    }

    //create from a line in csv file
    public ArduinoUnit(String csvLine) {
        String[] tokens = csvLine.split(",");
        macAddress = tokens[0];
        unitName = tokens[1];
        unitDesc = tokens[2];
        unitType = Integer.parseInt(tokens[3]);
        irrigRate = Float.parseFloat(tokens[4]);
        targetLF = Float.parseFloat(tokens[5]);
        runtime = Float.parseFloat(tokens[6]);
        containerDiam = Float.parseFloat(tokens[7]);
    }

    /* getters and setters */

    public int getID() { return this._id; }

    public void setID(int id) { this._id = id; }

    public String getMac() { return this.macAddress; }

    public void setMac(String mac) { this.macAddress = mac; }

    public String getName() { return this.unitName; }

    public void setName(String name) { this.unitName = name; }

    public String getDesc() { return this.unitDesc; }

    public void setDesc(String desc) { this.unitDesc = desc; }

    public int getType() { return this.unitType; }

    public void setType(int type) { this.unitType = type; }

    public float getIrrigRate() { return this.irrigRate; }

    public void setIrrigRate(float rate) { this.irrigRate = rate; }

    public float getTargetLF() { return this.targetLF; }

    public void setTargetLF(float lf) { this.targetLF = lf; }

    public float getRuntime() { return this.runtime; }

    public void setRuntime(float rt) { this.runtime = rt; }

    public float getContainerDiam() { return this.containerDiam; }

    public void setContainerDiam(float containerDiam) { this.containerDiam = containerDiam; }

    public boolean getTipperDisableStatus(int i) {
        return disableTips[i] == 1;
    }

    public void setTipperDisableStatus(int i, boolean st) {
        if (st) disableTips[i] = 1; else disableTips[i] = 0;
    }

    public String toString() {
        String s = "Arduino Unit [id=" + _id;
        s += "; MAC=" + macAddress + "; name=" + unitName;
        if (unitType == MODE_SPRINKLER) {
            s += "; type=SPRINKLER";
            s += "; containerDiam="+containerDiam;
        } else if (unitType == MODE_MICRO) {
            s += "; type=MICRO";
        }
        s+= "; irrigRate="+irrigRate+"; targetLF="+targetLF+"; runtime="+runtime;
        s += "]";
        return s;
    }
}
