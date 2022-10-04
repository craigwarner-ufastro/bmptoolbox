package BMPToolbox;

/**
 * Title:        UFBMPThreadedAgent
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for java agents to override
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

//=====================================================================================================

public class UFBMPThreadedAgent implements Runnable {

    public static final
	String rcsID = "$Name:  $ $Id: UFBMPThreadedAgent.java,v 1.8 2010/07/26 21:46:53 warner Exp warner $";

    protected int _serverPort;
    protected String _mainClass = getClass().getName();
    protected boolean _verbose = false;
    protected boolean _simMode = false;
    protected boolean _shutdown = false;
    protected boolean _isConnected = false;
    protected int _timeout = 30000;
    protected int count = 0;
    protected String _health = "GOOD";
    protected long _heartbeat = 0;
    protected boolean _hasLock = false;
    protected int _clientCount = 0;

    protected Vector< UFBMPClientThread > _clientThreads = new Vector(10);
    protected ListenThread _cListener = null;
    protected AncillaryThread _ancillary = null;
    public static Object mutex = new Object();
    protected LinkedHashMap <String, Socket> deviceSockets = new LinkedHashMap(3);  //sockets to "devices"
    protected LinkedHashMap <String, BufferedReader> deviceReaders = new LinkedHashMap(3);
    protected LinkedHashMap <String, PrintWriter> deviceWriters = new LinkedHashMap(3);
    protected LinkedHashMap <String, UFRecord> database;

//----------------------------------------------------------------------------------------

    public UFBMPThreadedAgent( int serverPort, String[] args )
    {
	System.out.println( rcsID );

	_serverPort = serverPort;
	System.out.println(_mainClass + "> server port = " + _serverPort);

	options(args);
	if( _simMode )
	    System.out.println(_mainClass + "> running in simulation mode.");
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().indexOf("-v") != -1) _verbose = true;
        else if (args[j].toLowerCase().indexOf("-sim") != -1) _simMode = true;
        else if (args[j].toLowerCase().indexOf("-timeout") != -1) {
          if (args.length > j+1) try {
            _timeout = Integer.parseInt(args[j+1]);
          } catch (NumberFormatException e) {}
        } 
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void _startupListenThread()
    {
	//start a ServerSocket listening thread to accept UFLib-UFProtocol messaging clients.
	// this always must be done after full setup above so that objects exist for client threads.

	System.out.println(_mainClass + "> starting Listening thread for client connections...");
	_cListener = new ListenThread();
	_cListener.start();
    }
//-----------------------------------------------------------------------------------------------------

    protected void _startupAncillaryThread()
    {
	/* Start the ancillary thread.  This method should be overrided by subclasses */

        System.out.println(_mainClass + "> starting Ancillary thread...");
	_ancillary = new AncillaryThread();
        _ancillary.start();
    }

    protected void checkAncillaryThread() {
        //if (_heartbeat < System.currentTimeMillis()/1000 - 5) nancil++; else nancil = 0;
        if (!_ancillary.isAlive()) {
          System.out.println(_mainClass + "::checkAncillaryThread> AncillaryThread "+_ancillary.getId()+" died!");
          _ancillary.shutdownLoops();
	  _startupAncillaryThread();
          System.out.println(_mainClass + "::checkAncillaryThread> Starting new AncillaryThread: "+_ancillary.getId());
        }
    }
//-----------------------------------------------------------------------------------------------------

    /** Setup database
      * Subclasses should override this class but call super.setDefaults
      * Uses helper method addBMPRecord to add records to the database
      */
    protected void setDefaults() {
      database = new LinkedHashMap(100);
      addBMPRecord(_mainClass+":heartbeat", UFRecord.TYPE_LONG, "0");
      addBMPRecord(_mainClass+":health", "GOOD");
      addBMPRecord(_mainClass+":status", "IDLE");
    }

    /** Helper method to create and add a record to the database */
    protected boolean addBMPRecord(String name, String value) {
      UFRecord rec = new UFRecord(name, value);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addBMPRecord(String name, int type, String value) {
      UFRecord rec = new UFRecord(name, type, value);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addBMPRecord(String name, String value, String fitsKey) {
      UFRecord rec = new UFRecord(name, value, fitsKey);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addBMPRecord(String name, int type, String value, String fitsKey) {
      UFRecord rec = new UFRecord(name, type, value, fitsKey);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to add a record to the database */
    protected boolean addBMPRecord(String name, UFRecord rec) {
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    /** 
      * Subclasses should override this class with options specific to
      * connecting to the specific device.
      */
    protected void init() {
    }

//-----------------------------------------------------------------------------------------------------


    /** main loop */
    public void exec() {
      System.out.println(_mainClass + "::exec> starting service");
      /* set up database */
      setDefaults();
      /* connect to device and exec agent */
      init();
      /* start listen thread for clients */
      _startupListenThread();
      /* start ancillary thread */
      _startupAncillaryThread();
      /* create and start main thread */
      Thread t = new Thread(this);
      t.start();
      int oldcount = -1;
      int ntries = 0;
      while (true) {
        if (_shutdown) {
          System.out.println(_mainClass + "::exec> termination signal recv'd, or shutdown command...");
          shutdown();
        }
	/* Check that main thread is alive and restart if its died. */
        if (count > oldcount) {
          ntries = 0;
          oldcount = count;
        } else ntries++;
        if (! t.isAlive()) {
          System.out.println(_mainClass + "::exec> Thread "+t.getId()+" died!");
          t = new Thread(this);
          t.start();
          System.out.println(_mainClass + "::exec> Starting new thread: "+t.getId());
          ntries = 0;
        }
	/* Check that ancillary thread is alive */
	checkAncillaryThread();
        hibernate();
        if (_verbose && count%20 == 0) System.out.println("Thread "+t.getId()+": "+t.getState());
      }
    }

    /* This method is where commands are processed and sent to the device sockets */
    protected boolean action(UFBMPClientThread ct) {
      /* First increment count */
      synchronized(mutex) { count++; }
      UFStrings req = ct.getRequest();
      UFStrings reply = null;
      int nreq = 0;
      if (req == null) {
	System.err.println(_mainClass+"::action> Received null request!");
	return false;
      }
      String clientName = req.name().substring(0, req.name().indexOf(":"));
      boolean success = false;

      nreq = req.numVals();
      System.out.println(_mainClass+"::action> Received new request.");
      System.out.println("\tClient Thread: "+ct.getThreadName());
      System.out.println("\tRequest Name: "+req.name());
      System.out.println("\tRequest Size: "+nreq);
      for (int j = 0; j < nreq; j++) {
        System.out.println("\tRequest "+(j+1)+": "+req.stringAt(j));
      }

      /* Do work here */
      success = true;

      if (success) {
        reply = new UFStrings(_mainClass+": actionResponse", "Successfully executed "+nreq+" requests.");
        _health = "GOOD";
        updateDatabase(_mainClass+":health", _health, "GOOD", "");
        updateDatabase(_mainClass+":status", "IDLE");
      } else {
        _health = "BAD";
        String msg = "";
        if (reply != null) msg = reply.stringAt(0);
        updateDatabase(_mainClass+":health", _health, "BAD", msg);
        updateDatabase(_mainClass+":status", "ERROR");
      }
      if (ct._send(reply) <= 0) {
	ct._terminate();
	return false;
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    public void run() {
      while (true) {
	if (_shutdown) {
          System.out.println(_mainClass + "::run> termination signal recv'd, or shutdown command...");
          shutdown();
        }
	for (int j = 0; j < _clientThreads.size(); j++) {
	  final UFBMPClientThread ct = _clientThreads.elementAt(j);
	  if (ct.hasRequest()) {
	    Thread actionThread = new Thread() {
	      public void run() {
		System.out.println(_mainClass+"::run "+ctime()+"> Received requests from "+ct.getThreadName());
		boolean _success = action(ct);
		if (_success) {
		  System.out.println(_mainClass + "::run> Successfully executed requests from "+ct.getThreadName());
		} else {
		  updateDatabase(_mainClass+":status", "ERROR");
		  System.out.println(_mainClass + "::run> FAILED to execute requests from "+ct.getThreadName());
		}
	      }
	    };
	    actionThread.start();
	    hibernate(50);
	  }
	}
        synchronized(mutex) {
          count++;
          hibernate(200);
        }
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void hibernate() {
      //1000 millisec
      hibernate(1000);
    }

    protected void hibernate(int tout) {
      try {
        Thread.sleep(tout);
      } catch (InterruptedException e) {
        System.out.println(_mainClass + "::hibernate> error: "+e.toString());
      }
    }

    protected void shutdown() {
      System.out.println(_mainClass + "::shutdown> shutting down...");
      _ancillary.shutdownLoops();
      System.exit(0);
    }
//-----------------------------------------------------------------------------------------------------

    /* Helper methods to parse command name and params */

    public String getCmdName(UFStrings cmd, int idx) {
      String name = cmd.stringAt(idx);
      name = name.substring(0, name.indexOf("::"));
      return name;
    }

    public String getCmdName(UFStrings cmd) {
      return getCmdName(cmd, 0);
    }

    public String getCmdParam(UFStrings cmd, int idx) {
      String param = cmd.stringAt(idx);
      param = param.substring(param.indexOf("::")+2);
      return param;
    }

    public String getCmdParam(UFStrings cmd) {
      return getCmdParam(cmd, 0);
    }

    public String getReplyToken(String reply, int token) {
      return getReplyToken(reply, token, " ");
    }

    public String getReplyToken(String reply, int token, String delim) {
      String[] vals = reply.split(delim);
      if (vals.length > token) return vals[token];
      System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
      return "null";
    }

    public String roundVal(float val, int dec) {
      String outVal = ""+val;
      int n = outVal.indexOf(".");
      if (n != -1 && n+3 < outVal.length()) {
	outVal = outVal.substring(0, n+3);
      }
      return outVal;
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateDatabase(String key, String value) { 
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value);
    }

    protected void updateDatabase(String key, int value) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value);
    }

    protected void updateDatabase(String key, long value) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value);
    }

    protected void updateDatabase(String key, float value) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value);
    }

    protected void updateDatabase(String key, String value, String health, String mess) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value, health, mess);
    }

    protected void updateDatabase(String key, int value, String health, String mess) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value, health, mess);
    }

    protected void updateDatabase(String key, long value, String health, String mess) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value, health, mess);
    }

    protected void updateDatabase(String key, float value, String health, String mess) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return;
      rec.updateValue(value, health, mess);
    }

    protected boolean updateDatabase(UFRecord rec) {
      String key = rec.name();
      if (database.containsKey(key)) {
	UFRecord oldRec = (UFRecord)database.get(key);
	if (rec.isNewer(oldRec)) {
	  database.put(key, rec);
	  return true;
	}
      }
      return false;
    }
//-----------------------------------------------------------------------------------------------------

    protected String getBMPValue(String key) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return null;
      return rec.getValue();
    }

    protected int getBMPIntValue(String key) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return Integer.MIN_VALUE; 
      return rec.getInt();
    }

    protected long getBMPLongValue(String key) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return (long)Integer.MIN_VALUE; 
      return rec.getLong();
    }

    protected float getBMPFloatValue(String key) {
      UFRecord rec = (UFRecord)database.get(key);
      if (rec == null) return (float)Integer.MIN_VALUE; 
      return rec.getFloat();
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateStatusClients() {
      for (int j = 0; j < _clientThreads.size(); j++) {
	UFBMPClientThread ct = _clientThreads.elementAt(j); 
	if (ct.statusClient()) {
	  if (ct._send(database) <= 0) ct._terminate();
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateDatabase() {
      boolean updated = false;
      for (int j = 0; j < _clientThreads.size(); j++) {
	UFBMPClientThread ct = _clientThreads.elementAt(j);
	while (ct.hasNewRecords()) {
	  UFRecord rec = ct.getNextRecord();
	  updated = updateDatabase(rec);
	  if (updated) System.out.println(_mainClass + "::updateDatabase> Updated record "+rec.name()+" = "+rec.getValue());
	}
      }
    }
//-----------------------------------------------------------------------------------------------------


    protected void _sendStatusClients( UFStrings ufpStatusMsg )
    {
	for( int i = 0; i < _clientThreads.size(); i++ )
	    {
		UFBMPClientThread ct = _clientThreads.elementAt(i);

		if( ct.statusClient() ) {
		    if( ct._send( ufpStatusMsg ) <= 0 ) ct._terminate();
		}
	    }
    }
//-----------------------------------------------------------------------------------------------------

    protected synchronized boolean sendCommand(String device, String command) {
      return sendCommand(device, command, false);
    }

    protected synchronized boolean sendCommand(String device, String command, boolean noTerm) {
      if (!deviceSockets.containsKey(device)) return false;
      Socket deviceSocket = (Socket)deviceSockets.get(device);
      PrintWriter devOut;
      synchronized(deviceSocket) {
        try {
          deviceSocket.setSoTimeout(_timeout);
	  if (!deviceWriters.containsKey(device)) {
	    devOut = new PrintWriter(deviceSocket.getOutputStream(), true);
	    deviceWriters.put(device, devOut);
	  } else {
	    devOut = (PrintWriter)deviceWriters.get(device);
	  }
	  if (noTerm) {
	    devOut.print(command);
	    devOut.flush();
	  } else devOut.println(command);
          deviceSocket.setSoTimeout(0);
        } catch(IOException ioe) {
	  System.err.println(_mainClass+"::sendCommand> "+ioe.toString());
	  try { deviceSocket.setSoTimeout(0); } catch(Exception e) {}
	  return false;
        }
        if (_verbose) System.out.println(_mainClass+"::sendCommand> Sent command: "+command);
      }
      return true;
    }

    protected synchronized String recvResponse(String device) {
      return recvResponse(device, false, false);
    }

    protected synchronized String recvResponse(String device, boolean readExtra) {
      return recvResponse(device, readExtra, false);
    }

    protected synchronized String recvResponse(String device, boolean readExtra, boolean isEdas) {
      if (!deviceSockets.containsKey(device)) return null;
      Socket deviceSocket = (Socket)deviceSockets.get(device);
      BufferedReader devIn;
      String reply = null;
      synchronized(deviceSocket) {
        try {
	  deviceSocket.setSoTimeout(_timeout);
	  if (!deviceReaders.containsKey(device)) {
	    devIn = new BufferedReader(new InputStreamReader(deviceSocket.getInputStream()));
	    deviceReaders.put(device, devIn);
	  } else {
	    devIn = (BufferedReader)deviceReaders.get(device);
	  }
	  reply = devIn.readLine().trim();
          if (_verbose) System.out.println(_mainClass+"::recvResponse> Received: "+reply);
	  if (isEdas) {
	    while(devIn.ready()) {
	      String nextLine = devIn.readLine().trim();
	      if (_verbose) System.out.println(_mainClass+"::recvResponse> Received: "+nextLine);
	      reply += " "+nextLine;
	    }
	  }
	  //Camira software ends strings with \n\r instead of \r\n
	  if (readExtra) {
	    String extra = "";
	    while (devIn.ready()) {
	      int x = devIn.read();
	      extra += " byte: "+x;
	    }
	    if (_verbose) System.out.println(_mainClass+"::recvResponse> Received extra bytes: "+extra);
	  }
        } catch (IOException ioe) {
	  System.err.println(_mainClass+"::recvResponse> "+ioe.toString());
	  ioe.printStackTrace();
        }
        try {
	  deviceSocket.setSoTimeout(0);
        } catch(IOException e) { }
      }
      return reply;
    }

//-----------------------------------------------------------------------------------------------------

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

//-----------------------------------------------------------------------------------------------------


    protected void IPokeBadgersWithSpoon() {
	/* This method is called from the Ancillary Thread to update the
	 * heartbeat every second and update the database entry for the
	 * heartbeat */
	_heartbeat = System.currentTimeMillis()/1000;
	updateDatabase(_mainClass+":heartbeat", _heartbeat);
    }

//-----------------------------------------------------------------------------------------------------
    /* This helper method should be overridden to return the proper subclass
     * UFBMPClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFBMPClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      return new UFBMPClientThread(clsoc, clientCount, simMode);
    }

//=====================================================================================================

    // Class ListenThread creates thread to Listen for socket connections from clients:

    protected class ListenThread extends Thread {

	private String _className = getClass().getName();

	public ListenThread() {}

	public void run() {
	    try {
		System.out.println(_className + ".run> server port = "+_serverPort);
		ServerSocket ss = new ServerSocket(_serverPort);
		while (true) {	  
		    Socket clsoc = ss.accept();
		    System.out.println(_className + ".run> accepting new connection...");
		    /* Use helper method getNewClientThread to get correct subclass */
		    UFBMPClientThread ct = getNewClientThread(clsoc, ++_clientCount, _simMode);
		    _clientThreads.add( ct );
		    System.out.println(_className + ".run> connection accepted.");
		    ct.verbose(_verbose);
		    new Thread(ct).start();
		    yield();
		}
	    } catch (Exception ex) {
		System.err.println(_className + ".run> "+ex.toString());
		return;
	    }
	}
    }

//=====================================================================================================

    // Class AncillaryThread creates thread to poll device for status and send info to status clients

    protected class AncillaryThread extends Thread {

        protected boolean _isRunning = true;
	protected boolean _shutdown = false;
        private String _className = getClass().getName();

        public AncillaryThread() {}

	public void run() { 
          while(true) {
            while (_isRunning) {
	      if (_shutdown) return;
	      /* update heartbeat */
	      IPokeBadgersWithSpoon();
              if (_simMode) {
		/* update simulated values here */
		System.out.println(_className + ".run> simluating device...");
              } else if (_isConnected) {
		/* communicate with device here
		 * synchronize on deviceSocket
		 */
                System.out.println(_className + ".run> polling device...");
	      } else {
		/* not sim mode but not connected */
		_health = "BAD";
		updateDatabase(_mainClass+":health", _health);
                System.out.println(_className + ".run> health = BAD...");
	      }
	      /* Send new database values to status clients */
	      updateStatusClients();
	      /* Check for new records */
	      updateDatabase();
	      /* Sleep 1 second */
	      hibernate();
            }
	    hibernate();
          }
        }
        public void stopLoops()    {_isRunning = false;}
        public void restartLoops() {_isRunning = true;}
	public void shutdownLoops() {_shutdown = true;}
    }
} //end of class UFBMPThreadedAgent
