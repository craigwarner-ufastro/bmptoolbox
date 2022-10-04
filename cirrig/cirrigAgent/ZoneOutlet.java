package CirrigPlc;
/**
 * Title:        ZoneOutlet
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class representing a Zone/Outlet pair 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;
import CCROP.Zone;
import CCROP.ETZone;
import CCROP.LFZone;

public class ZoneOutlet {
    String _mainClass = getClass().getName();
    protected CirrigPLCOutlet _outlet;
    protected Zone _zone;
    protected int zid, uid, outletNum, priority, totalDailyIrrig_sec;
    protected float irrigCm, irrigMin, irrigCmHr, defIrrig;
    protected boolean running, updated, finishedLastCycle, systemTest;
    protected String zoneName, defMethod, errMsg;

    public ZoneOutlet(CirrigPLCOutlet outlet, int num, int uid, int pri) { 
      this._outlet = outlet;
      this.outletNum = num;
      this.priority = pri; //default will be zone group's max + 1
      this.uid = uid;
      //default values
      _zone = null;
      zid = -1;
      zoneName = "None";
      irrigCm = 0;
      running = false;
      updated = false;
      errMsg = "";

      finishedLastCycle = false;
      systemTest = false;
      totalDailyIrrig_sec = 0;
    }

    public String getErrMsg() { return errMsg; }
    public int getId() { return zid;}
    public CirrigPLCOutlet getOutlet() { return _outlet; }
    public int getOutletNumber() { return outletNum; }
    public int getPriority() { return priority; }
    public int getUid() { return uid; }
    public Zone getZone() { return _zone; }
    public String getZoneName() { return zoneName; }

    public float getDefIrrig() { return defIrrig; }
    public String getDefMethod() { return defMethod; }
    public float getIrrig() { return irrigCm; }
    public float getIrrigMin() { return irrigMin; }
    public float getIrrigRate() { return irrigCmHr; }

    public int getTotalDailyIrrig() { return totalDailyIrrig_sec; }
    public boolean hasFinishedLastCycle() { return finishedLastCycle; }

    public boolean running() { return running; } 
    public boolean isUpdated() { return updated; }
    public boolean isSystemTest() { return systemTest; }

    public void setId(int zid) { this.zid = zid; }
    public void setPriority(int pri) { this.priority = pri; }
    public void setZone(Zone zone) { 
      this._zone = zone;
      setZoneName(zone.getName());
      setId(zone.getId());
    }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public void setDefIrrig(float defIrrig) { this.defIrrig = defIrrig; }
    public void setDefMethod(String defMethod) { this.defMethod = defMethod; }
    public void setIrrig(float irrig) { this.irrigCm = irrig; }
    public void setIrrigMin(float irrigMin) { this.irrigMin = irrigMin; }
    public void setIrrigRate(float irrigRate) { this.irrigCmHr = irrigRate; }

    public void setRunning(boolean running) { this.running = running; }
    public void setUpdated(boolean updated) { this.updated = updated; if (!updated) finishedLastCycle=false; }

    public void setSystemTest(boolean test) { this.systemTest = test; }

    public boolean isEnabled() {
      if (zid != -1) return true;
      return false;
    }

    public boolean isFixed() {
      if (zid == -2) return true;
      return false;
    }

    public boolean isFixedWithRain() {
      if (zid == -3) return true;
      return false;
    }

    public void updateCycleForDailyTotals(int cycleNumber, int ncycles) {
      finishedLastCycle = false;
      if (cycleNumber == 0) totalDailyIrrig_sec = 0; 
      if (cycleNumber == ncycles-1) {
	//last cycle
	finishedLastCycle = true;
      } 
    }

    public void updateDailyTotals(int runTime) {
      totalDailyIrrig_sec += runTime;
    }

    public boolean updateZone(String ccropHost, int ccropPort) {
      if (!isEnabled() || isFixed()) return false;
      int _timeout = 6000;
      Socket ccropSocket = null;
      try {
	//connect to CCropAgent to download zone info
        System.out.println(_mainClass+"::updateZone | "+ctime()+"> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
        ccropSocket = new Socket(ccropHost, ccropPort);
        ccropSocket.setSoTimeout(_timeout);
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
        greet.sendTo(ccropSocket);
        UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
        if (ufpr == null) {
          System.out.println(_mainClass+"::updateZone | "+ctime()+"> ERROR: received null object!  Closing socket!");
	  errMsg = "Could not connect to CCROP agent!";
          ccropSocket.close();
	  return false;
        } else {
          String request = ufpr.name().toLowerCase();
          if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
            System.out.println(_mainClass+"::updateZone | "+ctime()+"> connection established: "+request);
          } else {
            System.out.println(_mainClass+"::updateZone | "+ctime()+"> ERROR: received "+request+".  Closing socket!");
            errMsg = "Received invalid response "+request+" from CCROP Agent";
            ccropSocket.close();
	    return false;
          }
          UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONE_INFO::"+uid+" "+zid);
          System.out.println(_mainClass+"::updateZone | "+ctime()+"> Sending GET_ZONE_INFO request.");
          ccropReq.sendTo(ccropSocket);

          ObjectInputStream inputStream = new ObjectInputStream(ccropSocket.getInputStream());
          Zone z = (Zone)inputStream.readObject();
          ccropSocket.close(); //close socket here regardless
          if (z == null) {
            System.out.println(_mainClass+"::updateZone | "+ctime()+"> ERROR: Received null Zone for zid "+zid);
            errMsg = "Received null Zone for zid "+zid;
	    return false;
          }
          System.out.println(_mainClass+"::updateZone | "+ctime()+"> Successfully read Zone "+zid+", type="+z.getZoneType()+"; name="+z.getName()+"; num="+z.getZoneNumber());
          if (z.getZoneType().startsWith("ET")) {
            setZone((ETZone)z);
          } else if (z.getZoneType().startsWith("LF")) {
            setZone((LFZone)z);
          } else {
            System.out.println(_mainClass+"::updateZone | "+ctime()+"> ERROR: Undefined zone type "+z.getZoneType());
            errMsg = "Undefined zone type "+z.getZoneType();
	    return false;
          }
          setZoneName("Zone "+z.getZoneNumber()+": "+z.getName()+" - "+z.getPlant());
        }
      } catch (Exception e) {
        System.err.println(_mainClass+"::updateZone | "+ctime()+"> ERROR: "+e.toString());
	e.printStackTrace();
        errMsg = "Error talking to CCROP Agent: "+e.toString();
        try {
          ccropSocket.close();
        } catch (Exception e2) {}
	return false;
      }
      return true;
    }

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }
}
