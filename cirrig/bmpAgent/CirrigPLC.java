package BMPToolbox;

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

public class CirrigPLC extends UFPLCifce { //implements Irrigator {

    //Irrigator class for BMP PLC ladder logic program
    private int _uid = -1;
    private boolean _connected = false, _busy = false;
    protected int _pwrReg = 1088, _timerReg = 1089, _timerReg2=1090, _timerFlagReg=1091;
    protected int _yReg = 16704, _timerStatReg = 16960, _timerValReg = 0, _timerValReg2 = 1;
    protected int _outlet = 1;
    protected String errMsg = "";
    protected CirrigPLCOutlet[] outlets;

    private String _className = getClass().getName() + "> ";

    /** constructors */
    public CirrigPLC( String host, CirrigPLCConfig config) {
      _plcHost = host;
      outlets = config.getOutlets();

      _writeReq = new WriteSingleRegisterRequest(); //To be used for sending write requests to PLC.
      _readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
    }

    public CirrigPLC( String host, int uid ) {
      _plcHost = host;
      _uid = uid;
      _writeReq = new WriteSingleRegisterRequest(); //To be used for sending write requests to PLC.
      _readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
    }

    /** Irrigator methods */
    //Return irrigator type
    public String getType() {
      return "CirrigPLC"; 
    }

    //Return host IP
    public String getHost() { return _plcHost; }

    //Return UID associated with irrgator
    public int getUid() { return _uid; }

    //Power on the device.  Can be a no-op
    public synchronized boolean powerOn(int i) {
      int _pwrReg = outlets[i].powerRegister;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 0, true);
        success &= setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 2, false);
      }
      System.out.println(_className+"::powerOn> Power on result for " +_plcHost + "= "+success);
      return success; 
    }

    //Power off the device.  Can be a no-op
    public synchronized boolean powerOff(int i) {
      int _pwrReg = outlets[i].powerRegister;
System.out.println(_pwrReg);
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 1, false);
        success &= setBit(_pwrReg, 0, false);
        success &= setBit(_pwrReg, 2, false);
      }
      System.out.println(_className+"::powerOff> Power off result for " +_plcHost + "= "+success);
      return success;
    }

    //Start a timer
    public synchronized boolean startTimer() {
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 2, true);
        success &= setBit(_pwrReg, 2, false);
        success &= setBit(_pwrReg, 1, true);
      }
      System.out.println(_className+"::startTimer> start timer result for " +_plcHost + "= "+success);
      _busy = false;
      return success;
    }

    //Pause the timer
    public synchronized boolean pauseTimer() {
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 1, false);
      }
      _busy = false;
      System.out.println(_className+"::pauseTimer> pause timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Resume a paused timer
    public synchronized boolean resumeTimer() {
      _busy = true;
      boolean success = false;
      //synchronize on this before all reads/writes
      synchronized(this) {
        success = setBit(_pwrReg, 1, true);
      }
      _busy = false;
      System.out.println(_className+"::resumeTimer> resume timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Set a timer value
    public synchronized boolean setTimer(float sec) {
      _busy = true;
      int tenths = (int)(sec*10+0.5f);
      int hex = decToHex(tenths);
      if (hex == -1) {
	System.out.println(_className+"::setTimer> "+_plcHost+" PLC decToHex ERROR at "+ctime());
	_busy = false;
	return false; 
      }
      System.out.println(_className+"::setTimer> Setting irrigation to "+sec+" sec (hex "+hex+") for " +_plcHost);
      boolean success = setTimerHex(hex);
      //set timer flag to true
      //synchronize on this before all reads/writes
      synchronized(this) {
        success &= setBit(_timerFlagReg, 0, true);
      }
      _busy = false;
      return success;
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
    public synchronized boolean getPowerStatus() {
      _busy = true;
      int bit = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_yReg);
      }
      _busy = false;
      if (_verbose) System.out.println(_className+"::getPowerStatus> "+_plcHost+" register "+_yReg+" (Y) = "+bit);
      if (bit == -1 || _outlet >= 32) { 
	System.out.println(_className+"::getPowerStatus> "+_plcHost+" ERROR reading register "+_yReg+" at "+ctime());
	return false;
      }
      return translateWord(bit)[_outlet-1];
    }

    //get timer set value
    public synchronized float getTimer() {
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
    public synchronized float getTimerValue() {
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
    public synchronized int getTimerStatus() {
      _busy = true;
      int bit = -1;
      //synchronize on this before all reads/writes
      synchronized(this) {
        bit = readRegister(_timerStatReg);
      }
      _busy = false;
      if (bit == -1) {
        System.out.println(_className+"::getTimerStatus> "+_plcHost+" PLC read ERROR at "+ctime());
	return -1;
      }
      if (_verbose) System.out.println(_className+"::getTimerStatus> "+_plcHost+" register "+_timerStatReg+" = "+bit);
      return translateWord(bit, 16)[((_outlet-1)%8)*2]; 
    }

    /** Helper methods for PLC */

    public int decToHex(int dec) {
      int hex = 0;
      int ndec = 1;
      int nhex = 1;
      int count = 0;
      while (ndec*10 < dec) {
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
      while (nhex*16 < hex) {
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

    protected synchronized boolean setTimerHex(int hex) {
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
      for (int j = 0; j < args.length; j++) {
	if (args[j].equals("-reg")) pwrReg = Integer.parseInt(args[j+1]); 
	if (args[j].equals("-outlet")) outlet = Integer.parseInt(args[j+1]);
      }
      CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride);
      CirrigPLC plc = new CirrigPLC("192.168.1.107", config);
      plc.connect();
      //plc.verbose(true);
      for (int j = 0; j < args.length; j++) {
	if (args[j].equals("-on")) plc.powerOn(Integer.parseInt(args[j+1])+1);
	if (args[j].equals("-off")) plc.powerOff(Integer.parseInt(args[j+1])+1);
	if (args[j].equals("-timer")) {
	  plc.powerOff(Integer.parseInt(args[j+1])+1);
	  plc.setTimer(Float.parseFloat(args[j+1]));
	  plc.startTimer();
	}
      }
      while (true) {
	try {Thread.sleep(1000);} catch(InterruptedException ex) {}
	System.out.println("POWER "+plc.getPowerStatus());
	System.out.println("TIMER SETTING "+plc.getTimer());
	System.out.println("TIMER VAL "+plc.getTimerValue());
	int[] status = plc.translateWord(plc.readRegister(16960), 16);
        String s = "";
        for (int j = 0; j < status.length; j++) {
          s += j+": "+status[j]+";  ";
        }
        System.out.println(s);
	status = plc.translateWord(plc.readRegister(16961), 16);
        s = "";
        for (int j = 0; j < status.length; j++) {
          s += j+": "+status[j]+";  ";
        }
        System.out.println(s);
	System.out.println("TIMER STAT "+plc.getTimerStatus());
      }
      //plc.closeConnection();
    }
}
