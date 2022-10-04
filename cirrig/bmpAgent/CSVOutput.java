package BMPToolbox;

import java.io.*;
import java.util.*;
import java.net.*;
import javaUFLib.*;

public class CSVOutput implements Irrigator {

    //Output to CSV file
    private int _uid = -1;
    protected String _filename = "";
    private boolean _connected = false, _busy = false;
    protected boolean err = false;
    protected String errMsg = "";
    protected boolean _verbose = false; 

    private String _className = getClass().getName() + "> ";

    /** constructors */
    public CSVOutput( String filename, int uid ) {
      _filename = filename;
      _uid = uid;
    }

    public void verbose( boolean verbose ) { _verbose = verbose; }

    /** Irrigator methods */
    //Return irrigator type
    public String getType() {
      return "CSVOutput"; 
    }

    //Return host IP
    public String getHost() { return _filename; }

    //Return UID associated with irrgator
    public int getUid() { return _uid; }

    //Power on the device.  Can be a no-op
    public synchronized boolean powerOn() {
      return true;
    }

    //Power off the device.  Can be a no-op
    public synchronized boolean powerOff() {
      return true;
    }

    //Start a timer
    public synchronized boolean startTimer() {
      //no op
      return true;
    }

    //Pause the timer - no op
    public synchronized boolean pauseTimer() {
      return true;
    }

    //Resume a paused timer - no op
    public synchronized boolean resumeTimer() {
      return true;
    }

    //Set a timer value
    public synchronized boolean setTimer(float sec) {
      //no op
      return true;
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
      return true;
    }

    //get timer set value
    public float getTimer() {
      return 0.0f;
    }

    //get current timer value
    public float getTimerValue() {
      return 0.0f;
    }

    //get the timer status
    public int getTimerStatus() {
      return 0;
    }

    /** --------------------------------   */

    public boolean isConnected() { return _connected; }

    /** Connect to the PLC */
    public boolean connect() { 
      System.out.println(_className+"::connect> SIM Connecting at "+ctime());
      _connected = true;
      return _connected;
    }

    /** close the connection to the PLC */
    public void closeConnection(){
	_connected = false;
	System.out.println(_className+"::closeConnection> SIM closing connection at "+ctime());
    }

    public boolean busy() { return _busy; }

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public boolean writeOutput(Vector<String> irrigStrings) {
      try {
        _busy = true;
	PrintWriter pw = new PrintWriter(new FileOutputStream(_filename));
	for (int j = 0; j < irrigStrings.size(); j++) {
	  pw.println(irrigStrings.get(j));
	}
        pw.close();
	_busy = false;
	System.out.println(_className+"::writeOutput | "+ctime()+"> Successfully wrote to filename "+_filename);
	return true;
      } catch(IOException e) {
	e.printStackTrace();
	err = true;
	errMsg = e.toString();
	System.out.println(_className+"::writeOutput | "+ctime()+"> ERROR writing to "+_filename+": "+e.toString());
	_busy = false;
	return false;
      } 
    }

    public static void main(String[] args) {
      CSVOutput plc = new CSVOutput("127.0.0.1", 1);
      plc.connect();
      plc.powerOn();
      plc.powerOff();
      plc.closeConnection();
    }
}
