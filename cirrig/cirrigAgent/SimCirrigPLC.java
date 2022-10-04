package CirrigPlc; 

import java.util.*;
import java.net.*;
import javaUFLib.*;

public class SimCirrigPLC implements CirrigIrrigator {

    //Simulate PLC
    private int _uid = -1, nOutlets, nCounters, maxSimultaneous;
    protected String _plcHost = "";
    private boolean _connected = false, _busy = false, _verbose = false, _multiple = false;
    protected int _yReg = 16704, _timerStatReg = 16960;
    protected boolean err = false;
    protected String errMsg = "";
    protected CirrigPLCOutlet[] outlets;
    protected ArrayList<ZoneGroup> _groups;
    protected boolean[] power;
    protected int[] timer, timerStat;
    protected CirrigPLCConfig _config;
    protected long[] timerStartValue;
    protected int[] counterValues;

    private String _className = getClass().getName() + "> ";

    /** constructors */
    public SimCirrigPLC( String host, CirrigPLCConfig config ) {
      _plcHost = host;
      _config = config;
      outlets = config.getOutlets();
      nOutlets = config.getNOutlets();
      nCounters = config.getNCounters();
      maxSimultaneous = nOutlets;
      _groups = new ArrayList();

      power = new boolean[nOutlets];
      timer = new int[nOutlets];
      timerStat = new int[nOutlets];
      timerStartValue = new long[nOutlets];
      for (int j = 0; j < nOutlets; j++) {
	power[j] = false;
	timer[j] = 0;
        timerStat[j] = 0;
	timerStartValue[j] = 0;
      }

      counterValues = new int[nCounters];
      for (int j = 0; j < nCounters; j++) {
	counterValues[j] = 0;
      }
    }

    public SimCirrigPLC( String host, int uid, CirrigPLCConfig config ) {
      _plcHost = host;
      _uid = uid;
      _config = config;
      outlets = config.getOutlets();
      nOutlets = config.getNOutlets();
      nCounters = config.getNCounters();
      maxSimultaneous = nOutlets;
      _groups = new ArrayList();

      power = new boolean[nOutlets];
      timer = new int[nOutlets];
      timerStat = new int[nOutlets];
      timerStartValue = new long[nOutlets];
      for (int j = 0; j < nOutlets; j++) {
        power[j] = false;
        timer[j] = 0;
        timerStat[j] = 0;
        timerStartValue[j] = 0;
      }
      counterValues = new int[nCounters];
      for (int j = 0; j < nCounters; j++) {
        counterValues[j] = 0;
      }
    }

    public void verbose( boolean verbose ) { _verbose = verbose; }

    /** Irrigator methods */
    //Return irrigator type
    public String getType() {
      return "SimCirrigPLC"; 
    }

    public CirrigPLCConfig getConfig() { return _config; }

    //Return host IP
    public String getHost() { return _plcHost; }

    //Return UID associated with irrgator
    public int getUid() { return _uid; }

    //Return number of outlets
    public int getNOutlets() { return nOutlets; }

    //Return number of counters 
    public int getNCounters() { return nCounters; }

    //Return CirrigPLCOutlet object
    public CirrigPLCOutlet getOutlet(int i) {
      return outlets[i];
    }

    //Return outlet name
    public String getCounterName(int i) {
      //name = Cxx where xx is in octal.  Up to 64 counters 
      String name = "C";
      if (i < 8) {
        name += "0"+i;
      } else if (i < 16) {
        name += (i+2);
      } else if (i < 20) {
        name += (i+4);
      } else if (i < 64) {
        name += (100+(i-20)/8*10+((i-20)%8));
      } else name += "--";
      return name;
    }

    //Return outlet name
    public String getOutletName(int i) {
      //name = Yxx where xx is in octal.  Up to 16 outlets.
      String name = "Y";
      if (i < 8) {
        name += "0"+i;
      } else if (i < 16) {
        name += (i+2);
      } else if (i < 64) {
        name += (100+(i-16)/8*10+(i%8));
      } else name += "--";
      return name;
    }

    //return comma separated list of all outlet names
    public String getAllOutletNames() {
      String names = getOutletName(0);
      for (int i = 1; i < getNOutlets(); i++) {
        names+=","+getOutletName(i);
      }
      return names;
    }

    //return outlet index
    public int getOutletNumber(String outletName) {
      for (int j = 0; j < nOutlets; j++) {
        if (getOutletName(j).equals(outletName)) return j;
      }
      return -1;
    }

    public ArrayList<ZoneGroup> getZoneGroups() {
      return _groups;
    }

    public ZoneGroup getZoneGroup(String groupName) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.getName().equals(groupName)) return g;
      }
      return null;
    }

    public ZoneGroup getZoneGroup(int groupNumber) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.getNumber() == groupNumber) return g;
      }
      return null;
    }

    public ZoneGroup getZoneGroupByCounter(int counterNumber) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.hasCounter(counterNumber)) return g;
      }
      return null;
    }

    public ZoneGroup getZoneGroupByOutlet(int outletNumber) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.hasOutlet(outletNumber)) return g;
      }
      return null;
    }

    public String getZoneNameByOutletNumber(int outletNumber) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.hasOutlet(outletNumber)) {
          return g.getZoneOutletByNumber(outletNumber).getZoneName();
        }
      }
      return "None";
    }

    public boolean hasZoneGroup(String groupName) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.getName().equals(groupName)) return true;
      }
      return false;
    }

    public boolean hasZoneGroup(int groupNumber) {
      ZoneGroup g;
      for (int j = 0; j < _groups.size(); j++) {
        g = _groups.get(j);
        if (g.getNumber() == groupNumber) return true;
      }
      return false;
    }

    public void addZoneGroup(ZoneGroup g) {
      _groups.add(g);
    }

    public boolean removeZoneGroup(String groupName) {
      for (int j = 0; j < _groups.size(); j++) {
        if (_groups.get(j).getName().equals(groupName)) {
          _groups.remove(j);
          return true;
        }
      }
      return false;
    }

    public boolean removeZoneGroup(int groupNumber) {
      for (int j = 0; j < _groups.size(); j++) {
        if (_groups.get(j).getNumber() == groupNumber) {
          _groups.remove(j);
          return true;
        }
      }
      return false;
    }

    public boolean propagateRecord(int outlet, String recName, String value) {
      ZoneGroup g = getZoneGroupByOutlet(outlet);
      if (g == null) return false;
      return g.propagateRecord(outlet, recName, value);
    }

    public boolean allowMultipleGroups() { return _multiple; }

    public String getFreeCounters() {
      int nfree = 0;
      String retVal = "";
      for (int j = 0; j < nCounters; j++) {
        if (isCounterAvailable(j)) {
          if (nfree != 0) retVal += ",";
          nfree++;
          retVal += getCounterName(j);
          //retVal += String.valueOf(j);
        }
      }
      if (nfree == 0) return "None";
      return retVal;
    }

    public String getFreeOutlets() {
      int nfree = 0;
      String retVal = "";
      for (int j = 0; j < nOutlets; j++) {
        if (isOutletAvailable(j)) {
          if (nfree != 0) retVal += ",";
          nfree++;
          retVal += getOutletName(j);
        }
      }
      if (nfree == 0) return "None";
      return retVal;
    }

    public int getMaxSimultaneous() { return maxSimultaneous; }

    public synchronized boolean isCounterAvailable(int counterNum) {
      synchronized(this) {
        Iterator<ZoneGroup> i = getZoneGroups().iterator();
        while (i.hasNext()) {
          ZoneGroup zg = (ZoneGroup)i.next();
          if (zg.hasCounter(counterNum)) return false; //this counter belongs to this zone group
        }
      }
      return true;
    }

    public synchronized boolean isOutletAvailable(int outletNum) {
      synchronized(this) {
        Iterator<ZoneGroup> i = getZoneGroups().iterator();
        while (i.hasNext()) {
          ZoneGroup zg = (ZoneGroup)i.next();
          if (zg.hasOutlet(outletNum)) return false; //this outlet belongs to this zone group
        }
      }
      return true;
    }

    public void setMaxSimultaneous(int max) { maxSimultaneous = max; }

    public void setMultipleGroups(boolean multipleGroups) { _multiple = multipleGroups; }

    //Power on the device.  Can be a no-op
    public synchronized boolean powerOn(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::powerOn> Error: Invalid outlet number: "+i);
        return false;
      }
      power[i] = true;
      boolean success = true;
      System.out.println(_className+"::powerOn> SIM Power on result for " +_plcHost + "= "+success);
      return success;
    }

    //Power off the device.  Can be a no-op
    public synchronized boolean powerOff(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::powerOff> Error: Invalid outlet number: "+i);
        return false;
      }
      power[i] = false;
      boolean success = true;
      System.out.println(_className+"::powerOff> SIM Power off result for " +_plcHost + "= "+success);
      return success;
    }

    //Power off all outlets.  Can be a no-op
    public boolean powerOffAll() {
      boolean success = true;
      for (int i = 0; i < outlets.length; i++) success &= powerOff(i);
      System.out.println(_className+"::powerOff> Power off All result for " +_plcHost + "= "+success);
      return success;
    }

    //Start a timer
    public synchronized boolean startTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::startTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      timerStat[i] = 1;
      timerStartValue[i] = System.currentTimeMillis();
      power[i] = true;
      boolean success = true;
      System.out.println(_className+"::startTimer> SIM start timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Pause the timer
    public synchronized boolean pauseTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::pauseTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      timerStat[i] = 2;
      boolean success = true;
      System.out.println(_className+"::pauseTimer> SIM pause timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Resume a paused timer
    public synchronized boolean resumeTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::resumeTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      timerStat[i] = 1;
      boolean success = true;
      System.out.println(_className+"::resumeTimer> SIM resume timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Set a timer value
    public synchronized boolean setTimer(int i, float sec) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::setTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      timerStat[i] = 0;
      int tenths = (int)(sec*10+0.5f);
      int hex = decToHex(tenths);
      timer[i] = hex;
      System.out.println(_className+"::setTimer> SIM Setting irrigation to "+sec+" sec (hex "+hex+") for " +_plcHost);
      boolean success = true; 
      return success;
    }

    public synchronized int getCounterValue(int cnt) {
      if (cnt < 0 || cnt >= nCounters) {
        System.out.println(_className+"::getCounterValue> Error: Invalid counter number: "+cnt);
        return -1;
      }
      return counterValues[cnt];
    }

    //returns true if an error
    public boolean getErrorStatus() {
      return err;
    }

    //returns an error message
    public String getErrorMessage() {
      return errMsg;
    }

    //true = on, false = off
    public boolean[] getPowerStatus() {
      if (_verbose) for (int j = 0; j < nOutlets; j++) System.out.println("SIM POWER STATUS "+(j+1)+" = "+power[j]);
      return power;
    }

    //get timer set value
    public float getTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimer> Error: Invalid outlet number: "+i);
        return -1;
      }
      float sec = 0.1f*hexToDec(timer[i]);
      if (_verbose) System.out.println(_className+"::getTimer> SIM "+_plcHost+" outlet "+i+": timer = "+timer[i]+" ("+sec+" sec)");
      return sec;
    }

    //get current timer value
    public float getTimerValue(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimerValue> Error: Invalid outlet number: "+i);
        return -1;
      }
      long ms = System.currentTimeMillis() - timerStartValue[i];
      if (ms/1000 > getTimer(i)) power[i] = false;
      float sec = ms/1000.0f; 

      if (_verbose) System.out.println(_className+"::getTimerValue> SIM "+_plcHost+" outlet "+i+": timer Value = "+sec);
      return sec; 
    }

    //get the timer status
    public int getTimerStatus(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimerStatus> Error: Invalid outlet number: "+i);
        return -1;
      }
      if (_verbose) System.out.println(_className+"::getTimerStatus> "+_plcHost+" outlet "+i+" timer status = "+timerStat[i]);
      return timerStat[i];
    }

    //reset a counter
    public synchronized boolean resetCounter(int cnt) {
      if (cnt < 0 || cnt >= nCounters) {
        System.out.println(_className+"::resetCounter> Error: Invalid counter number: "+cnt);
        return false;
      }
      counterValues[cnt] = 0;
      return true;
    }

    /** Helper methods for PLC */

    public int decToHex(int dec) {
      int hex = 0;
      int ndec = 1;
      int nhex = 1;
      while (ndec*10 < dec) {
        ndec*=10;
        nhex*=16;
      }
      while (dec > 0) {
        int x = dec/ndec;
        hex += nhex*x;
        dec -= ndec*x;
        ndec/=10;
        nhex/=16;
      }
      return hex;
    }

    public int hexToDec(int hex) {
      int dec = 0;
      int ndec = 1;
      int nhex = 1;
      while (nhex*16 < hex) {
        if (_verbose) System.out.println("nhex = "+nhex+"; hex = "+hex);
        ndec*=10;
        nhex*=16;
      }
      while (hex > 0) {
        int x = hex/nhex;
        dec += ndec*x;
        hex -= nhex*x;
        nhex/=16;
        ndec/=10;
      }
      return dec;
    }

    /** --------------------------------   */

    public boolean isConnected() { return _connected; }

    /** Connect to the PLC */
    public boolean connect() { 
      System.out.println(_className+"::connectToPLC> SIM Connecting to PLC "+_plcHost+" at "+ctime());
      _connected = true;
      return _connected;
    }

    /** close the connection to the PLC */
    public void closeConnection(){
	_connected = false;
	System.out.println(_className + "SIM closing connection to PLC: "+_plcHost+" at "+ctime());
    }

    public boolean busy() { return _busy; }

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public static void main(String[] args) {
      int pwrReg = 1088, outlet = 1, nOutlets = 16, stride = 8;
      for (int j = 0; j < args.length; j++) {
        if (args[j].equals("-reg")) pwrReg = Integer.parseInt(args[j+1]);
        if (args[j].equals("-outlet")) outlet = Integer.parseInt(args[j+1])-1;
      }
      CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride);
      SimCirrigPLC plc = new SimCirrigPLC("127.0.0.1", 1, config);
      plc.connect();
      plc.powerOn(1);
      plc.powerOff(1);
      plc.closeConnection();
    }
}
