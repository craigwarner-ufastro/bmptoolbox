package BMPToolbox;

import java.util.*;
import java.net.*;
import javaUFLib.*;

public class SimPLC implements Irrigator {

    //Simulate PLC
    private int _uid = -1;
    protected String _plcHost = "";
    private boolean _connected = false, _busy = false;
    protected int _pwrReg = 1088, _timerReg = 1089, _timerReg2=1090;
    protected int _yReg = 16704, _timerStatReg = 16960, _timerValReg = 0, _timerValReg2 = 1;
    protected boolean err = false;
    protected String errMsg = "";
    protected boolean power = false, _verbose = false; 
    protected int timer = 0, timerValue = 0, timerStatus = 0;

    private String _className = getClass().getName() + "> ";

    /** constructors */
    public SimPLC( String host, int uid ) {
      _plcHost = host;
      _uid = uid;
    }

    public void verbose( boolean verbose ) { _verbose = verbose; }

    /** Irrigator methods */
    //Return irrigator type
    public String getType() {
      return "SimPLC"; 
    }

    //Return host IP
    public String getHost() { return _plcHost; }

    //Return UID associated with irrgator
    public int getUid() { return _uid; }

    //Power on the device.  Can be a no-op
    public synchronized boolean powerOn() {
      power = true;
      timerStatus = 0;
      boolean success = true;
      System.out.println(_className+"::powerOn> SIM Power on result for " +_plcHost + "= "+success);
      return success;
    }

    //Power off the device.  Can be a no-op
    public synchronized boolean powerOff() {
      power = false;
      timerStatus = 0;
      boolean success = true;
      System.out.println(_className+"::powerOff> SIM Power off result for " +_plcHost + "= "+success);
      return success;
    }

    //Start a timer
    public synchronized boolean startTimer() {
      timerStatus = 1;
      timerValue++;
      boolean success = true;
      System.out.println(_className+"::startTimer> SIM start timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Pause the timer
    public synchronized boolean pauseTimer() {
      timerStatus = 0;
      boolean success = true;
      System.out.println(_className+"::pauseTimer> SIM pause timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Resume a paused timer
    public synchronized boolean resumeTimer() {
      timerStatus = 1;
      timerValue++;
      boolean success = true;
      System.out.println(_className+"::resumeTimer> SIM resume timer result for " +_plcHost + "= "+success);
      return success;
    }

    //Set a timer value
    public synchronized boolean setTimer(float sec) {
      int tenths = (int)(sec*10+0.5f);
      int hex = decToHex(tenths);
      System.out.println(_className+"::setTimer> SIM Setting irrigation to "+sec+" sec (hex "+hex+") for " +_plcHost);
      timer = hex;
      boolean success = true; 
      return success;
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
    public boolean getPowerStatus() {
      if (_verbose) System.out.println(_className+"::getPowerStatus> SIM "+_plcHost+" register "+_yReg+" (Y) = "+power);
      return power;
    }

    //get timer set value
    public float getTimer() {
      float sec = 0.1f*hexToDec(timer);
      if (_verbose) System.out.println(_className+"::getTimer> SIM "+_plcHost+" register "+_timerReg+" = "+timer+" ("+sec+" sec)");
      return sec;
    }

    //get current timer value
    public float getTimerValue() {
      float timerVal = (hexToDec(timerValue))/10.0f;
      if (_verbose) System.out.println(_className+"::getTimerValue> SIM "+_plcHost+" register "+_timerValReg+" = "+timerValue+" ("+timerVal+" sec)");
      return timerVal;
    }

    //get the timer status
    public int getTimerStatus() {
      if (_verbose) System.out.println(_className+"::getTimerStatus> "+_plcHost+" register "+_timerStatReg+" = "+timerStatus);
      return timerStatus;
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
      SimPLC plc = new SimPLC("127.0.0.1", 1);
      plc.connect();
      plc.powerOn();
      plc.powerOff();
      plc.closeConnection();
    }
}
