package CirrigPlc; 
/**
 * Title:        Irrigator
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  interface for irrigators such as BMPPLCs to implement
 */


import java.util.*;

public interface CirrigIrrigator { 
    public void verbose(boolean verbose);

    //Return irrigator type
    public String getType();

    //Return config object
    public CirrigPLCConfig getConfig();

    //Return host IP
    public String getHost();

    //Return UID associated with irrgator
    public int getUid();

    //Return number of outlets
    public int getNOutlets();

    //Return number of counters 
    public int getNCounters();

    //Return CirrigOutlet object
    public CirrigPLCOutlet getOutlet(int i);

    public String getCounterName(int i);
 
    //Return outlet name
    public String getOutletName(int i);

    //return comma separated list of all outlet names
    public String getAllOutletNames(); 

    //return outlet index
    public int getOutletNumber(String outletName);

    public ArrayList<ZoneGroup> getZoneGroups();

    public ZoneGroup getZoneGroup(String groupName);

    public ZoneGroup getZoneGroup(int groupNumber);

    public ZoneGroup getZoneGroupByCounter(int counterNumber);

    public ZoneGroup getZoneGroupByOutlet(int outletNumber);

    public String getZoneNameByOutletNumber(int outletNumber);

    public boolean hasZoneGroup(String groupName);

    public boolean hasZoneGroup(int groupNumber);

    public void addZoneGroup(ZoneGroup g);

    public boolean removeZoneGroup(String groupName);

    public boolean removeZoneGroup(int groupNumber);

    public boolean propagateRecord(int outlet, String recName, String value); 

    public boolean allowMultipleGroups();

    public String getFreeCounters();

    public String getFreeOutlets();

    public int getMaxSimultaneous();

    public boolean isCounterAvailable(int counterNum);

    public boolean isOutletAvailable(int outletNum);

    public void setMaxSimultaneous(int max);

    public void setMultipleGroups(boolean allowMultiple);

    //Power on the device.  Can be a no-op
    public boolean powerOn(int i);

    //Power off the device.  Can be a no-op
    public boolean powerOff(int i);

    //Power off all outlets.  Can be a no-op
    public boolean powerOffAll();

    //Start a timer
    public boolean startTimer(int i);

    //Pause the timer
    public boolean pauseTimer(int i);

    //Resume a paused timer
    public boolean resumeTimer(int i);

    //Set a timer value
    public boolean setTimer(int i, float seconds);

    //get a counter value
    public int getCounterValue(int cnt);

    //returns true if an error
    public boolean getErrorStatus();

    //returns an error message
    public String getErrorMessage();

    //true = on, false = off
    public boolean[] getPowerStatus();

    //get timer set value
    public float getTimer(int i);

    //get current timer value
    public float getTimerValue(int i);

    //get the timer status
    public int getTimerStatus(int i);

    //reset a counter
    public boolean resetCounter(int cnt);

    //connect to the device
    public boolean connect();

    //check the connection
    public boolean isConnected();

    //close the connection to device
    public void closeConnection();

    //check if irrigator is busy
    public boolean busy();
}
