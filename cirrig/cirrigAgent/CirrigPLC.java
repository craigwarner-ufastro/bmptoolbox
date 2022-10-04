package CirrigPlc; 

import java.util.*;
import java.net.*;
import javaUFLib.*;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;

public class CirrigPLC extends UFPLCifce implements CirrigIrrigator {

    //Irrigator class for BMP PLC ladder logic program
    private int _uid = -1, nOutlets, maxSimultaneous, nCounters;
    private boolean _connected = false, _busy = false, _verbose = false, _multiple = false;
    protected int _yReg = 16704, _timerStatReg = 16960;
    protected String errMsg = "";
    protected CirrigPLCOutlet[] outlets;
    protected ArrayList<ZoneGroup> _groups;
    protected CirrigPLCConfig _config;

    private String _className = getClass().getName() + "> ";

    /** constructors */
    public CirrigPLC( String host, CirrigPLCConfig config) {
      _plcHost = host;
      _config = config;
      outlets = config.getOutlets();
      nOutlets = config.getNOutlets();
      nCounters = config.getNCounters();
      maxSimultaneous = nOutlets;
      _groups = new ArrayList();

      _writeReq = new WriteSingleRegisterRequest(); //To be used for sending write requests to PLC.
      _readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
    }

    public CirrigPLC( String host, int uid, CirrigPLCConfig config ) {
      _plcHost = host;
      _uid = uid;
      _config = config;
      outlets = config.getOutlets();
      nOutlets = config.getNOutlets();
      nCounters = config.getNCounters();
      maxSimultaneous = nOutlets;
      _groups = new ArrayList();

      _writeReq = new WriteSingleRegisterRequest(); //To be used for sending write requests to PLC.
      _readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
    }

    public void verbose( boolean verbose ) { _verbose = verbose; }

    /** Irrigator methods */
    //Return irrigator type
    public String getType() {
      return "CirrigPLC"; 
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
      //name = Yxx where xx is in octal.  Up to 64 outlets.
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
	  //retVal += String.valueOf(j);
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
      int _pwrReg = outlets[i].powerRegister;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 0, true);
        success &= setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 2, false);
      }
      System.out.println(_className+"::powerOn> Power on result for " +_plcHost + " outlet "+i+" = "+success);
      return success; 
    }

    //Power off the device.  Can be a no-op
    public synchronized boolean powerOff(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::powerOff> Error: Invalid outlet number: "+i);
        return false;
      }
      int _pwrReg = outlets[i].powerRegister;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 1, false);
        success &= setBit(_pwrReg, 0, false);
        success &= setBit(_pwrReg, 2, false);
      }
      System.out.println(_className+"::powerOff> Power off result for " +_plcHost + " outlet "+i+" = "+success);
      return success;
    }

    //Power off all outlets
    public boolean powerOffAll() {
      boolean success = true;
      for (int i = 0; i < outlets.length; i++) success &= powerOff(i); 
      System.out.println(_className+"::powerOff> Power off All result for " +_plcHost + " = "+success);
      return success;
    }

    //Start a timer
    public synchronized boolean startTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::startTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      int _pwrReg = outlets[i].powerRegister;
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 2, false);
        success &= setBit(_pwrReg, 1, true);
      }
      System.out.println(_className+"::startTimer> start timer result for " +_plcHost +  " outlet "+i+" = "+success);
      _busy = false;
      return success;
    }

    //Pause the timer
    public synchronized boolean pauseTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::pauseTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      int _pwrReg = outlets[i].powerRegister;
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 1, false);
      }
      _busy = false;
      System.out.println(_className+"::pauseTimer> pause timer result for " +_plcHost +  " outlet "+i+" = "+success);
      return success;
    }

    //Resume a paused timer
    public synchronized boolean resumeTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::resumeTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      int _pwrReg = outlets[i].powerRegister;
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 1, true);
      }
      _busy = false;
      System.out.println(_className+"::resumeTimer> resume timer result for " +_plcHost +  " outlet "+i+" = "+success);
      return success;
    }

    //Set a timer value
    public synchronized boolean setTimer(int i, float sec) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::setTimer> Error: Invalid outlet number: "+i);
        return false;
      }
      int _timerFlagReg = outlets[i].timerFlagRegister; 
      _busy = true;
      int tenths = (int)(sec*10+0.5f);
      int hex = decToHex(tenths);
      if (hex == -1) {
	System.out.println(_className+"::setTimer> "+_plcHost+" PLC decToHex ERROR at "+ctime());
	_busy = false;
	return false; 
      }
      System.out.println(_className+"::setTimer> Setting irrigation to "+sec+" sec (hex "+hex+") for " +_plcHost+" outlet "+i);
      boolean success = setTimerHex(i, hex);
      //set timer flag to true
      //synchronize on this before all reads/writes
      synchronized(this) {
        success &= setBit(_timerFlagReg, 0, true);
      }
      _busy = false;
      return success;
    }

    public synchronized int getCounterValue(int cnt) {
      if (cnt < 0 || cnt >= nCounters) {
        System.out.println(_className+"::getCounterValue> Error: Invalid counter number: "+cnt);
        return -1;
      }
      int temp = cnt;
      int _counterReg = 512;
      if (cnt >= 20) {
        _counterReg = 576;
        temp = cnt-20;
      }
      _counterReg += temp;
      _busy = true;
      int bit = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_counterReg);
      }
      int val = hexToDec(bit);
      _busy = false;
      if (_verbose) System.out.println(_className+"::getCounterValue> "+_plcHost+" register "+_counterReg+" = "+bit+" => value = "+val);
      return val;
    }

    //returns true if an error
    public synchronized boolean getErrorStatus() {
      return getErrStatus();
    }

    //returns an error message
    public synchronized String getErrorMessage() {
      return errMsg;
    }

    //true = on, false = off
    public synchronized boolean[] getPowerStatus() {
      _busy = true;
      long bit = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_yReg);
	if (nOutlets > 16) { 
	  long bit2 = readRegister(16708);
	  bit += bit2*65536;
	  if (nOutlets > 32) {
	    bit2 = readRegister(16709);
	    bit += bit2*65536*65536;
	  }
	  if (nOutlets > 48) {
            bit2 = readRegister(16710);
            bit += bit2*65536*65536*65536;
	  }
	}
      }
      _busy = false;
      if (_verbose) System.out.println(_className+"::getPowerStatus> "+_plcHost+" register "+_yReg+" (Y) = "+bit);
      if (bit == -1) { 
	System.out.println(_className+"::getPowerStatus> "+_plcHost+" ERROR reading register "+_yReg+" at "+ctime());
	return null;
      }
      boolean[] status = translateWord(bit);
      //for (int j = 0; j < nOutlets; j++) System.out.println("POWER STATUS "+(j+1)+" = "+status[j]);
      return status; 
    }

    //get timer set value
    public synchronized float getTimer(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimer> Error: Invalid outlet number: "+i);
        return -1;
      }
      int _timerReg = outlets[i].timerRegister; 
      int _timerReg2 = _timerReg+1;
      _busy = true;
      int bit = -1, bit2 = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_timerReg);
        bit2 = readRegister(_timerReg2);
      }
      _busy = false;
      if (bit == -1 || bit2 == -1) {
	System.out.println(_className+"::getTimer> "+_plcHost+" PLC read ERROR at "+ctime());
	return -1;
      }
      float sec = 0.1f*hexToDec(bit+65536*bit2);
      if (sec == -1) {
	System.out.println(_className+"::getTimer> "+_plcHost+" PLC hexToDec ERROR at "+ctime());
	return -1;
      }
      if (_verbose) System.out.println(_className+"::getTimer> "+_plcHost+" register "+_timerReg+" = "+bit+"; "+ _timerReg2 + " = "+bit2+" ("+sec+" sec)");
      return sec;
    }

    //get current timer value
    public synchronized float getTimerValue(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimerValue> Error: Invalid outlet number: "+i);
        return -1;
      }
      int _timerValReg = outlets[i].timerValRegister;
      int _timerValReg2 = _timerValReg+1;
      _busy = true;
      int bit = -1, bit2 = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_timerValReg);
        bit2 = readRegister(_timerValReg2);
      }
      _busy = false;
      if (bit == -1 || bit2 == -1) {
        System.out.println(_className+"::getTimerValue> "+_plcHost+" PLC read ERROR at "+ctime());
	return -1;
      }
      float val1 = hexToDec(bit2)*10000;
      float val2 = hexToDec(bit);
      float timerVal = (val1+val2)/10.0f;
      if (val1 < 0 || val2 < 0) {
	System.out.println(_className+"::getTimerValue> "+_plcHost+" PLC hexToDec ERROR at "+ctime());
	return -1;
      }
      if (_verbose) System.out.println(_className+"::getTimerValue> "+_plcHost+" register "+_timerValReg+" = "+bit+"; "+ _timerValReg2 + " = "+bit2+" ("+timerVal+" sec)");
      return timerVal;
    }

    //get the timer status
    public synchronized int getTimerStatus(int i) {
      if (i < 0 || i >= nOutlets) {
        System.out.println(_className+"::getTimerStatus> Error: Invalid outlet number: "+i);
        return -1;
      }
      _busy = true;
      int bit = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
	//simply into one equation
	bit = readRegister(_timerStatReg+i/8);
/*
	if (i < 8) {
	  bit = readRegister(_timerStatReg);
        } else if (i < 16) {
	  bit = readRegister(_timerStatReg+1);
	} else if (i < 24) {
	  bit = readRegister(_timerStatReg+2);
	} else if (i < 32) {
          bit = readRegister(_timerStatReg+3);
	} else if (i < 40) {
          bit = readRegister(_timerStatReg+3);
        }
*/
      }
      _busy = false;
      if (bit == -1) {
        System.out.println(_className+"::getTimerStatus> "+_plcHost+" PLC read ERROR at "+ctime());
	return -1;
      }
      if (_verbose) System.out.println(_className+"::getTimerStatus> "+_plcHost+" register "+_timerStatReg+" = "+bit);
      return translateWord(bit, 16)[(i%8)*2]; 
    }

    //reset a counter
    public synchronized boolean resetCounter(int cnt) {
      if (cnt < 0 || cnt >= nCounters) {
        System.out.println(_className+"::resetCounter> Error: Invalid counter number: "+cnt);
        return false;
      }
      int _resetReg = 1093;
      int temp = cnt;
      if (temp >= 20) {
	_resetReg = 1285;
	temp -= 20;
      }
      _resetReg += temp*8;

      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
System.out.println("RESET REG "+_resetReg+"; cnt "+cnt);
      synchronized(this) {
        success = setBit(_resetReg, 0, true);
	success = setBit(_resetReg, 0, false);
      }
      _busy = false;
      System.out.println(_className+"::resetCounter> reset counter result for " +_plcHost +  " counter "+cnt+" = "+success);
      return success;
    }


    /** Helper methods for PLC */

    public int decToHex(int dec) {
      int hex = 0;
      int ndec = 1;
      int nhex = 1;
      int count = 0;
      while (ndec*10 <= dec) {
        ndec*=10;
        nhex*=16;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      while (dec > 0) {
        int x = dec/ndec;
        hex += nhex*x;
        dec -= ndec*x;
        ndec/=10;
        nhex/=16;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      return hex;
    }

    public int hexToDec(int hex) {
      int dec = 0;
      int ndec = 1;
      int nhex = 1;
      int count = 0;
      while (nhex*16 <= hex) {
        //if (_verbose) System.out.println("nhex = "+nhex+"; hex = "+hex);
        ndec*=10;
        nhex*=16;
	count++;
	if (count > 10) {
	  //Some weird error state, break out of loop
	  return -1;
	}
      }
      count = 0;
      while (hex > 0) {
        int x = hex/nhex;
        dec += ndec*x;
        hex -= nhex*x;
        nhex/=16;
        ndec/=10;
        count++;
        if (count > 10) {
          //Some weird error state, break out of loop
          return -1;
        }
      }
      return dec;
    }

    public int[] translateWord(int bit, int length) {
      int[] bitList = new int[length];
      for (int j = length-1; j >= 0; j--) {
        if (bit >= Math.pow(2,j)) {
          bitList[j] = 1;
          bit -= (int)Math.pow(2,j);
        } else bitList[j] = 0;
      }
      return bitList;
    }

    public boolean[] translateWord(int bit) {
      int n = 0;
      int x = 1;
      while (bit > x) {
        x*=2;
        n++;
      }
      if (n > 32) n = 32;
      boolean[] bitList = new boolean[32];
      for (int j = 0; j < 32; j++) bitList[j] = false;
      for (int j = n; j >=0; j--) {
        if (bit >= Math.pow(2,j)) {
          bitList[j] = true;
          bit -= (int)Math.pow(2,j);
        } else bitList[j] = false;
      }
      return bitList;
    }

    public boolean[] translateWord(long bit) {
      int n = 0;
      long x = 1;
      while (bit > x) {
        x*=2;
        n++;
      }
      if (n > 64) n = 64;
      boolean[] bitList = new boolean[64];
      for (int j = 0; j < 64; j++) bitList[j] = false;
      for (int j = n; j >=0; j--) {
        if (bit >= Math.pow(2.0,(double)j)) {
          bitList[j] = true;
          bit -= (long)Math.pow(2.0,(double)j);
        } else bitList[j] = false;
      }
      return bitList;
    }

    protected synchronized boolean setTimerHex(int outlet, int hex) {
      int _timerReg = outlets[outlet].timerRegister;
      int _timerReg2 = _timerReg+1;
      boolean success = true;
      boolean[] bitList = translateWord(hex);
      //synchronize on this before all reads/writes
      synchronized(this) {
	if (_verbose) System.out.println("\tRegister "+_timerReg+":");
        for (int i = 0; i < 16; i++) {
	  if (_verbose) System.out.println("\t\tbit "+i+" = "+bitList[i]);
	  success &= setBit(_timerReg, i, bitList[i]);
        }
        if (_verbose) System.out.println("\tRegister "+_timerReg2+":");
        for (int i = 16; i < bitList.length; i++) {
          if (_verbose) System.out.println("\t\tbit "+(i-16)+" = "+bitList[i]);
          success &= setBit(_timerReg2, (i-16), bitList[i]);
        }
      }
      return success;
    }

    /** --------------------------------   */

    public boolean isConnected() { return _connected; }

    /** Connect to the PLC */
    public boolean connect() { return connect(_plcHost, _plcTimeout); }

    public synchronized boolean connect(String host, int timeout) {
        System.out.println(_className+"::connectToPLC> Connecting to PLC "+host+" at "+ctime());
	_plcHost = host;
	_plcTimeout = timeout;
        //Clear any previous error status
	clearErr();
	super.connectToPLC(host, timeout);
        if (getErrStatus()) {
	  _connected = false;
	  errMsg = "Error connecting to PLC "+host+" at "+ctime();
	  System.out.println(_className+"::connectToPLC> ERROR connecting to PLC "+host+" at "+ctime());
	  return false;
	}
	_connected = true;
	System.out.println(_className+"::connectToPLC> Successfully connected to PLC "+host+" at "+ctime());
        return true;
    }

    /** close the connection to the PLC */
    public void closeConnection(){
	_connected = false;
        //Clear any previous error status
	clearErr();
	System.out.println(_className + "closing connection to PLC: "+_plcHost+" at "+ctime());
	super.closeConnection();
    }

    public boolean busy() { return _busy; }

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public static void main(String[] args) {
      //CirrigPLCConfig config = new CirrigPLCConfig(1088, 1);
      //CirrigPLCConfig config = new CirrigPLCConfig(1128, 6);
      int pwrReg = 1088, outlet = 1, nOutlets = 16, stride = 8;
      String host = "192.168.1.100";
      for (int j = 0; j < args.length; j++) {
	if (args[j].equals("-reg")) pwrReg = Integer.parseInt(args[j+1]); 
	if (args[j].equals("-outlet")) outlet = Integer.parseInt(args[j+1])-1;
	if (args[j].equals("-host")) host = args[j+1];
      }
      CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride);
      CirrigPLC plc = new CirrigPLC(host, config);
      plc.connect();
      //plc.verbose(true);
      for (int j = 0; j < args.length; j++) {
	if (args[j].equals("-on")) plc.powerOn(outlet);
	if (args[j].equals("-off")) plc.powerOff(outlet);
	if (args[j].equals("-timer")) {
	  plc.powerOff(outlet);
	  plc.setTimer(outlet, Float.parseFloat(args[j+1]));
	  plc.startTimer(outlet);
	}
        if (args[j].equals("-pause")) plc.pauseTimer(outlet);
        if (args[j].equals("-resume")) plc.resumeTimer(outlet);
        if (args[j].equals("-read")) {
	  int bit = Integer.parseInt(args[j+1]);
	  for (int i = bit; i < bit+100; i++) {
	    System.out.println("Bit "+i+": "+plc.readRegister(i));
	  }
	} 
	if (args[j].equals("-counter")) {
          int cnt = Integer.parseInt(args[j+1]);
	  System.out.println("COUNTER "+cnt+": "+plc.getCounterValue(cnt));
	}
        if (args[j].equals("-reset")) {
          int cnt = Integer.parseInt(args[j+1]);
          System.out.println("RESET "+cnt+": "+plc.resetCounter(cnt));
        }
      }
      while (true) {
	try {Thread.sleep(1000);} catch(InterruptedException ex) {}
	plc.getPowerStatus();
	System.out.println("TIMER SETTING "+outlet+": "+plc.getTimer(outlet));
	System.out.println("TIMER VAL "+outlet+": "+plc.getTimerValue(outlet));
	System.out.println("TIMER STAT "+outlet+": "+plc.getTimerStatus(outlet));
      }
      //plc.closeConnection();
    }
}
