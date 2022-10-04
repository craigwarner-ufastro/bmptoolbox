package javaMMTLib;
/**
 * Title:        UFMMTThreadedAgent
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for java agents to override
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFProtocol.*;
import javaUFLib.*;

//=====================================================================================================

public class UFMMTThreadedAgent implements Runnable {

    public static final
	String rcsID = "$Name:  $ $Id: UFMMTThreadedAgent.java,v 1.35 2020/01/06 17:40:56 pi Exp $";

    protected int _serverPort;
    protected String _mainClass = getClass().getName();
    protected boolean _verbose = false;
    protected boolean _simMode = false;
    protected boolean _isExec = false;
    protected boolean _shutdown = false;
    protected boolean _isConnected = false, _ancillaryRunning = false;
    protected int _timeout = 10000, _execTimeout = 5000, _sendTimeout = 10000;
    protected int count = 0;
    protected String _health = "GOOD";
    protected long _heartbeat = 0;
    protected boolean _hasLock = false;
    protected boolean _execConnected = false;
    protected int _clientCount = 0;
    protected String _logname = "";

    protected Vector< UFMMTClientThread > _clientThreads = new Vector(10);
    protected ListenThread _cListener = null;
    protected AncillaryThread _ancillary = null;
    public static Object mutex = new Object();
    protected LinkedHashMap <String, Socket> deviceSockets = new LinkedHashMap(3);  //sockets to "devices"
    protected LinkedHashMap <String, BufferedReader> deviceReaders = new LinkedHashMap(3);
    protected LinkedHashMap <String, PrintWriter> deviceWriters = new LinkedHashMap(3);
    protected Socket execSocket = null, execCommandSocket = null; // sockets to executive agent
    protected int execPort = 56001;
    protected String execHost = "localhost";
    public static String installDir = UFExecCommand.getEnvVar("UFMMTINSTALL");

    protected LinkedHashMap <String, UFRecord> database;
    long _nlocktries = 0;

//----------------------------------------------------------------------------------------

    public UFMMTThreadedAgent( int serverPort, String[] args )
    {
	System.out.println( rcsID );

	_serverPort = serverPort;
	_isExec = false;
	System.out.println(_mainClass + "> server port = " + _serverPort);

	options(args);
	if( _simMode )
	    System.out.println(_mainClass + "> running in simulation mode.");
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().equals("-v")) _verbose = true;
        else if (args[j].toLowerCase().equals("-sim")) _simMode = true;
        else if (args[j].toLowerCase().indexOf("-timeout") != -1) {
          if (args.length > j+1) try {
            _timeout = Integer.parseInt(args[j+1]);
          } catch (NumberFormatException e) {}
        } else if (args[j].toLowerCase().indexOf("-log") != -1) {
	  _logname = args[j+1];
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
	/* Kill if heartbeat is 15 sec behind. */
	if (_ancillaryRunning && _heartbeat < System.currentTimeMillis()/1000 - 5) {
          System.out.println(_mainClass + "::checkAncillaryThread> Heartbeat is lagging 15 sec behind.  Killing ancillary thread at "+ctime());
	  _ancillaryRunning = false;
	  _ancillary.forceShutdown();
	}
        if (!_ancillary.isAlive()) {
          System.out.println(_mainClass + "::checkAncillaryThread> AncillaryThread "+_ancillary.getId()+" died!");
          _ancillary.shutdownLoops();
	  _startupAncillaryThread();
          System.out.println(_mainClass + "::checkAncillaryThread> Starting new AncillaryThread: "+_ancillary.getId()+" at "+ctime());
        }
    }
//-----------------------------------------------------------------------------------------------------

    /** Setup database
      * Subclasses should override this class but call super.setDefaults
      * Uses helper method addMMTRecord to add records to the database
      */
    protected void setDefaults() {
      database = new LinkedHashMap(100);
      synchronized(database) {
        addMMTRecord(_mainClass+":heartbeat", UFRecord.TYPE_LONG, "0");
        addMMTRecord(_mainClass+":health", "GOOD");
        addMMTRecord(_mainClass+":status", "IDLE");
        addMMTRecord(_mainClass+":logname", _logname);
      }
    }

    /** Helper method to create and add a record to the database */
    protected boolean addMMTRecord(String name, String value) {
      UFRecord rec = new UFRecord(name, value);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addMMTRecord(String name, int type, String value) {
      UFRecord rec = new UFRecord(name, type, value);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addMMTRecord(String name, String value, String fitsKey) {
      UFRecord rec = new UFRecord(name, value, fitsKey);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addMMTRecord(String name, int type, String value, String fitsKey) {
      UFRecord rec = new UFRecord(name, type, value, fitsKey);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to create and add a record to the database */
    protected boolean addMMTRecord(String name, int type, String value, String fitsKey, String fitsComment) {
      UFRecord rec = new UFRecord(name, type, value, fitsKey);
      rec.updateMess(fitsComment);
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

    /** Helper method to add a record to the database */
    protected boolean addMMTRecord(String name, UFRecord rec) {
      UFRecord reply = database.put(name, rec);
      if (reply == null) return false;
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    /** Read XML databaseStartupValues.xml file and set default record values */
    protected boolean readXMLDefaults() {
      String xmlFile = installDir+"/etc/databaseStartupValues.xml";
      Document doc = null;
      try {
        File file = new File(xmlFile);
	if (!file.exists()) return false;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(file);
	doc.getDocumentElement().normalize();
      } catch(Exception e) {
        System.out.println(_mainClass+"::readXMLDefaults> "+e.toString());
	return false;
      }
      Element root = doc.getDocumentElement();
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("agent");
      for (int j = 0; j < nlist.getLength(); j++) {
	Node fstNode = nlist.item(j);
	if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	Element fstElmnt = (Element) fstNode;
	  if (fstElmnt.hasAttribute("name")) {
            if (_mainClass.toLowerCase().indexOf(fstElmnt.getAttribute("name").trim().toLowerCase()) == -1) continue;
	    NodeList reclist = fstElmnt.getElementsByTagName("record");
	    for (int l = 0; l < reclist.getLength(); l++) {
	      try {
		Node recNode = reclist.item(l);
		if (recNode.getNodeType() == Node.ELEMENT_NODE) {
		  Element recElmnt = (Element)recNode;
		  NodeList recnameList = recElmnt.getElementsByTagName("name");
		  elem = (Element)recnameList.item(0);
		  String recName = elem.getFirstChild().getNodeValue().trim();
		  NodeList recvalList = recElmnt.getElementsByTagName("value");
		  elem = (Element)recvalList.item(0);
		  String recVal = elem.getFirstChild().getNodeValue().trim();
		  updateDatabase(_mainClass+":"+recName, recVal);
		  if (_verbose) System.out.println(_mainClass+"::readXMLDefaults> "+_mainClass+":"+recName+" = "+recVal);
		}
	      } catch (Exception e) { 
	        System.out.println(e.toString());
	      }
	    }
            return true;
          }
        }
      }
      return false;
    }

//-----------------------------------------------------------------------------------------------------

    /** Connect to device and exec agent
      * Subclasses should override this class with options specific to
      * connecting to the specific device.
      */
    protected void init() {
      /* set up database */
      setDefaults();
      /* read XML defaults */
      readXMLDefaults();
      /* Do not proceed until connected to exec agent */
      while (!_execConnected) {
	_execConnected = connectToExec("execclient", "execclient commands");
	hibernate();
      }
    }

    /** Helper methods to connect to exec agent */
    protected boolean connectToExec(String greeting1) {
      return connectToExec(greeting1, null);
    }

    protected boolean connectToExec(String greeting1, String greeting2) {
      try {
        System.out.println(_mainClass + "::connectToExec> Trying to connect to executive agent on "+execHost+", port = "+execPort);
        execSocket = new Socket(execHost, execPort);
        execSocket.setSoTimeout(0); //infinite timeout
        //send greeting.  exec agent replies with "statusclient" and requests to be a status client
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": "+greeting1);
        greet.sendTo(execSocket);
        UFMMTClientThread ct = getNewClientThread(execSocket, ++_clientCount, _simMode);
        synchronized(_clientThreads) {
          _clientThreads.add(ct);
	}
        System.out.println(_mainClass + "::connectToExec> connection accepted.");
        ct.verbose(_verbose);
        new Thread(ct).start();

	if (greeting2 != null) {
          System.out.println(_mainClass + "::connectToExec> Trying to connect to executive agent to receive commands on "+execHost+", port = "+execPort);
          execCommandSocket = new Socket(execHost, execPort);
          execCommandSocket.setSoTimeout(0); //infinite timeout
          //send greeting.  exec agent replies with "fullclient" and requests to be a full client
          greet = new UFTimeStamp(_mainClass+": "+greeting2);
          greet.sendTo(execCommandSocket);
          UFMMTClientThread ct2 = getNewClientThread(execCommandSocket, ++_clientCount, _simMode);
	  synchronized(_clientThreads) {
            _clientThreads.add(ct2);
	  }
          System.out.println(_mainClass + "::connectToExec> connection accepted.");
          ct2.verbose(_verbose);
          new Thread(ct2).start();
	}
      } catch (Exception ex) {
        System.err.println(_mainClass + "::connectToExec> "+ex.toString());
        return false;
      }
      return true;
    }

    protected boolean disconnectFromExec() {
      _execConnected = false;
      try {
        System.out.println(_mainClass + "::disconnectFromExec> Disconnecting from executive agent on "+execHost+", port = "+execPort);
	synchronized(_clientThreads) {
	  for (int j = 0; j < _clientThreads.size(); j++) {
	    UFMMTClientThread ct = _clientThreads.elementAt(j);
	    //Use terminate to disconnect socket.  Ancillary thread will clean up _clientThreads vector.
	    if (ct._clientSocket == execSocket || ct._clientSocket == execCommandSocket) ct._terminate();
	  }
	}
	hibernate(2000);//wait 2 seconds
	return true;
      } catch (Exception ex) {
        System.err.println(_mainClass + "::disconnectFromExec> "+ex.toString());
        return false;
      }
    }

    /** Helper method to connect to device */
    protected boolean connectToDevice(String name, String hostname, int port, String greeting) {
      Socket devSocket;
      try {
        System.out.println(_mainClass + "::connectToDevice> Trying to connect to device on "+hostname+", port = "+port);
        devSocket = new Socket(hostname, port);
        devSocket.setSoTimeout(0); //infinite timeout
	synchronized(deviceSockets) {
	  deviceSockets.put(name, devSocket);
	}
	if (greeting != null) {
	  sendCommand(name, greeting);
	  recvResponse(name);
	}
        System.out.println(_mainClass + "::connectToDevice> connection accepted.");
      } catch (Exception ex) {
        System.err.println(_mainClass + "::connectToDevice> "+ex.toString());
        return false;
      }
      return true;
    }

    protected boolean connectToDevice(String name, String hostname, int port) {
      return connectToDevice(name, hostname, port, null);
    }

//-----------------------------------------------------------------------------------------------------


    /** main loop */
    public void exec() {
      System.out.println(_mainClass + "::exec> starting service");
      /* connect to device and exec agent */
      init();
      /* start listen thread for clients */
      _startupListenThread();
      /* start ancillary thread */
      _startupAncillaryThread();
      /* create and start main thread */
      Thread t = new Thread(this);
      t.setName(_mainClass);
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
	  if (count > 1000000) count = 0;  //reset count to avoid overflow!!
          oldcount = count;
        } else ntries++;
        if (! t.isAlive()) {
          System.out.println(_mainClass + "::exec> Thread "+t.getId()+" died!");
          t = new Thread(this);
	  t.setName(_mainClass);
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
    protected boolean action(UFMMTClientThread ct) {
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

      /* Must obtain lock first */
      _hasLock = getLock();
      if (!_hasLock) {
	System.err.println(_mainClass+"::action> ERROR: Received request but unable to obtain lock!");
	System.err.println("\tClient Thread: "+ct.getThreadName());
	System.err.println("\tRequest Name: "+req.name());
	reply = new UFStrings("ERROR", "Unable to obtain lock");
	_health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "WARNING", "Unable to obtain lock for request: "+req.name());
	/* If this thread somehow owns lock (dropped packet problem?) release it here */
	if (hasLock()) releaseLock();
        if (ct._send(reply) <= 0) ct._terminate();
	return false;
      }

      /* Do work here */
      success = true;

      /* Release lock */
      if (releaseLock()) {
	_hasLock = false;
      } else {
        System.err.println(_mainClass+"::action> ERROR: Unable to release lock!  This should not happen!");
        System.err.println("\tClient Thread: "+ct.getThreadName());
        System.err.println("\tRequest Name: "+req.name());
        reply = new UFStrings("ERROR", "Unable to release lock");
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "WARNING", "Unable to release lock for request: "+req.name());
	if (ct._send(reply) <= 0) ct._terminate();
        return false;
      }

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
	  final UFMMTClientThread ct = _clientThreads.elementAt(j);
	  if (ct.hasRequest()) {
	    Thread actionThread = new Thread() {
	      public void run() {
		setName("Action Thread for "+ct.getThreadName());
		System.out.println(_mainClass+"::run "+ctime()+"> Received requests from "+ct.getThreadName());
		boolean _success = action(ct);
		if (_success) {
		  System.out.println(_mainClass + "::run> Successfully executed requests from "+ct.getThreadName());
		} else {
		  updateDatabase(_mainClass+":status", "ERROR");
		  System.out.println(_mainClass + "::run> FAILED to execute requests from "+ct.getThreadName());
		  /* Make sure this thread releases the lock! */
		  if (_hasLock) releaseLock();
		  _hasLock = false;
		}
	      }
	    };
	    actionThread.start();
	    hibernate(50);
	  }
	}
        synchronized(mutex) {
          count++;
        }
	hibernate(200);
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
      if (name.indexOf("::") == -1) return "NULL";
      name = name.substring(0, name.indexOf("::"));
      return name;
    }

    public String getCmdName(UFStrings cmd) {
      return getCmdName(cmd, 0);
    }

    public String getCmdParam(UFStrings cmd, int idx) {
      String param = cmd.stringAt(idx);
      param = param.substring(param.indexOf("::")+2).trim();
      return param;
    }

    public String getCmdParam(UFStrings cmd) {
      return getCmdParam(cmd, 0);
    }

    public String getReplyToken(UFStrings reply, int token) {
      return getReplyToken(reply, token, " ");
    }

    public String getReplyToken(UFStrings reply, int token, String delim) {
      String[] vals = reply.stringAt(0).split(delim);
      if (vals.length > token) return vals[token];
      System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
      return "null";
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

    public String getReplyTokens(String reply, int firstToken) {
      return getReplyTokens(reply, firstToken, " ");
    }

    public String getReplyTokens(String reply, int firstToken, String delim) {
      String[] vals = reply.split(delim);
      if (vals.length <= firstToken) {
	System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+firstToken+" tokens.");
	return "null";
      }
      String retVal = vals[firstToken]; 
      for (int j = firstToken+1; j < vals.length; j++) {
	retVal += " "+vals[j]; 
      }
      return retVal;
    }

    public String roundVal(float val, int dec) {
      String outVal = ""+val;
      int n = outVal.indexOf(".");
      if (n != -1 && n+3 < outVal.length()) {
	outVal = outVal.substring(0, n+3);
      }
      return outVal;
    }

    public int parseForInt(String response, int startIdx) {
      String intStr = "";
      int i = startIdx;
      int retVal = Integer.MIN_VALUE; 
      while (!Character.isDigit(response.charAt(i)) && response.charAt(i) != '-') i++;
      while (i < response.length() && (Character.isDigit(response.charAt(i)) || response.charAt(i) == '-')) intStr += response.charAt(i++); 
      try {
	retVal = Integer.parseInt(intStr);
      } catch (NumberFormatException nfe) {
	System.out.println(_mainClass +"::parseForInt> Error parsing "+intStr);
      }
      return retVal; 
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized boolean getLock() {
      _nlocktries++;
      System.out.println( _mainClass + "::getLock "+_nlocktries+" ENTERED at "+ctime());
      boolean retVal = true;
      if (execSocket == null) return false;
      synchronized(execSocket) {
	if (!execSocket.isConnected()) return false;
	int nbytes = 0;
	UFStrings req = new UFStrings(_mainClass+": actionRequest", "LOCK::GET "+_nlocktries);
	try {
	  //Lock exec client
	  lockExecClient(true);
          execSocket.setSoTimeout(_execTimeout);
	  nbytes = req.sendTo(execSocket);
        } catch(Exception ex) {
	  retVal = false;
          ex.printStackTrace();
          System.err.println(_mainClass + "::getLock> "+ex.toString());
	}
        if( nbytes <= 0 || retVal == false) {
	  try {
	    lockExecClient(false);
	    System.out.println( _mainClass + "::getLock> zero bytes sent.");
	    //try again
	    retVal = disconnectFromExec();
	    if (!retVal) return false;
	    int ntries = 0;
	    while (!_execConnected && ntries < 3) {
	      hibernate(2000);
	      ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
	    }
	    hibernate(2000);
	    if (!_execConnected) {
	      System.out.println(_mainClass+"::getLock> WARNING: Can't connect to executive agent to obtain lock! "+ctime());
	      _health = "WARNING";
	      updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to obtain lock!");
	      return true;
	    }
	    lockExecClient(true);
	    execSocket.setSoTimeout(_execTimeout);
	    nbytes = req.sendTo(execSocket);
	    if (nbytes <= 0) {
              System.out.println( _mainClass + "::getLock> zero bytes sent again at "+ctime());
	      lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Zero bytes sent to Exec Agent");
              return true;
	    }
	    retVal = true;
	  } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::getLock> "+ctime()+" ERROR: "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error sending GET LOCK request!");
	    return true;
          }
	}

	UFStrings reply = null;
	try {
          execSocket.setSoTimeout(_execTimeout);
	  //Wait until a reply is ready to be read
	  waitForReply(execSocket, "LOCK::GET");
	  reply = (UFStrings)UFProtocol.createFrom(execSocket);
	  if (reply != null) System.out.println(reply.toString());
        } catch(Exception ex) {
          ex.printStackTrace();
          System.err.println(_mainClass + "::getLock> "+ex.toString());
          retVal = false;
        }
	if (reply == null || retVal == false) {
	  try {
	    lockExecClient(false);
            System.out.println(_mainClass +"::getLock> Error: Received null object!");
            //try again
            retVal = disconnectFromExec();
            if (!retVal) return false;
            int ntries = 0;
            while (!_execConnected && ntries < 3) {
              hibernate(2000);
              ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
            }
            hibernate(2000);
            if (!_execConnected) {
              System.out.println(_mainClass+"::getLock> WARNING: Can't connect to executive agent to obtain lock! "+ctime());
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to obtain lock!");
              return true;
            }
	    lockExecClient(true);
            execSocket.setSoTimeout(_execTimeout);
	    //resend request
            nbytes = req.sendTo(execSocket);
            reply = (UFStrings)UFProtocol.createFrom(execSocket);
            if (reply == null) {
              System.out.println(_mainClass +"::getLock> Error: Received null object again! "+ctime());
              lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Received null object again!");
              return true;
            }
	    System.out.println("TRY 2: "+reply.toString());
	  } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::getLock> "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error receiving GET LOCK reply!");
            return true;
          }
        }
	//Unlock exec client
	lockExecClient(false);
	if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("IS_OBTAINED")) {
	  //Lock successfully obtained
          System.out.println(_mainClass+"::getLock> Lock obtained!");
	  retVal = true;
	} else if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("NOT_OBTAINED")) {
	  System.out.println(_mainClass+"::getLock> Lock NOT obtained!");
	  retVal = false;
	}
      }
      return retVal;
    }

    protected synchronized boolean hasLock() {
      System.out.println( _mainClass + "::hasLock ENTERED at "+ctime());
      boolean retVal = true;
      if (execSocket == null) return false;
      synchronized(execSocket) {
        if (!execSocket.isConnected()) return false;
        int nbytes = 0;
        UFStrings req = new UFStrings(_mainClass+": actionRequest", "LOCK::HAS"); 
        try {
          //Lock exec client
          lockExecClient(true);
          execSocket.setSoTimeout(_execTimeout);
          nbytes = req.sendTo(execSocket);
        } catch(Exception ex) {
          retVal = false;
          ex.printStackTrace();
          System.err.println(_mainClass + "::hasLock> "+ex.toString());
        }
        if( nbytes <= 0 || retVal == false) {
          try {
	    lockExecClient(false);
            System.out.println( _mainClass + "::hasLock> zero bytes sent.");
            //try again
            retVal = disconnectFromExec();
            if (!retVal) return false;
            int ntries = 0;
            while (!_execConnected && ntries < 3) {
              hibernate(2000);
              ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
            }
            hibernate(2000);
            if (!_execConnected) {
              System.out.println(_mainClass+"::hasLock> WARNING: Can't connect to executive agent to check lock! "+ctime());
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to check lock!");
              return true;
            }
            lockExecClient(true);
            execSocket.setSoTimeout(_execTimeout);
            nbytes = req.sendTo(execSocket);
            if (nbytes <= 0) {
              System.out.println( _mainClass + "::hasLock> zero bytes sent again at "+ctime());
              lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Zero bytes sent to Exec Agent");
              return true;
            }
            retVal = true;
          } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::hasLock> "+ctime()+" ERROR: "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error sending HAS LOCK request!");
            return true;
          }
        }

        UFStrings reply = null;
        try {
          execSocket.setSoTimeout(_execTimeout);
          //Wait until a reply is ready to be read
          waitForReply(execSocket, "LOCK::HAS");
          reply = (UFStrings)UFProtocol.createFrom(execSocket);
        } catch(Exception ex) {
          ex.printStackTrace();
          System.err.println(_mainClass + "::hasLock> "+ex.toString());
          retVal = false;
        }
        if (reply == null || retVal == false) {
          try {
	    lockExecClient(false);
            System.out.println(_mainClass +"::hasLock> Error: Received null object!");
            //try again
            retVal = disconnectFromExec();
            if (!retVal) return false;
            int ntries = 0;
            while (!_execConnected && ntries < 3) {
              hibernate(2000);
              ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
            }
            hibernate(2000);
            if (!_execConnected) {
              System.out.println(_mainClass+"::hasLock> WARNING: Can't connect to executive agent to check lock! "+ctime());
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to check lock!");
              return true;
            }
	    lockExecClient(true);
            execSocket.setSoTimeout(_execTimeout);
            //resend request
            nbytes = req.sendTo(execSocket);
            reply = (UFStrings)UFProtocol.createFrom(execSocket);
            if (reply == null) {
              System.out.println(_mainClass +"::hasLock> Error: Received null object again! "+ctime());
              lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Received null object again!");
              return true;
            }
          } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::hasLock> "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error receiving HAS LOCK reply!");
            return true;
          }
        }
        //Unlock exec client
        lockExecClient(false);
        if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("IS_OWNED")) {
          //Lock successfully obtained
          retVal = true;
        } else if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("NOT_OWNED")) {
          System.out.println(_mainClass+"::hasLock> Lock NOT owned!");
          retVal = false;
        }
      }
      return retVal;
    }

    protected synchronized boolean releaseLock() {
      System.out.println( _mainClass + "::releaseLock "+_nlocktries+" ENTERED at "+ctime());
      boolean retVal = true;
      if (execSocket == null) return false;
      synchronized(execSocket) {
        if (!execSocket.isConnected()) return false;
        int nbytes = 0;
        UFStrings req = new UFStrings(_mainClass+": actionRequest", "LOCK::RELEASE "+_nlocktries); 
        try {
          //Lock exec client
          lockExecClient(true);
          execSocket.setSoTimeout(_execTimeout);
          nbytes = req.sendTo(execSocket);
        } catch(Exception ex) {
          retVal = false;
          ex.printStackTrace();
          System.err.println(_mainClass + "::releaseLock> "+ex.toString());
        }
        if( nbytes <= 0 || retVal == false) {
          try {
	    lockExecClient(false);
            System.out.println( _mainClass + "::releaseLock> zero bytes sent.");
            //try again
            retVal = disconnectFromExec();
            if (!retVal) return false;
            int ntries = 0;
            while (!_execConnected && ntries < 3) {
              hibernate(2000);
              ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
            }
            hibernate(2000);
            if (!_execConnected) {
              System.out.println(_mainClass+"::releaseLock> WARNING: Can't connect to executive agent to release lock! "+ctime());
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to release lock!");
              return true;
            }
            lockExecClient(true);
            execSocket.setSoTimeout(_execTimeout);
            nbytes = req.sendTo(execSocket);
            if (nbytes <= 0) {
              System.out.println( _mainClass + "::releaseLock> zero bytes sent again at "+ctime());
              lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Zero bytes sent to Exec Agent");
              return true;
            }
            retVal = true;
          } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::releaseLock> "+ctime()+" ERROR: "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error sending RELEASE LOCK request!");
            return true;
          }
        }

        UFStrings reply = null;
        try {
          execSocket.setSoTimeout(_execTimeout);
          //Wait until a reply is ready to be read
          waitForReply(execSocket, "LOCK::RELEASE");
          reply = (UFStrings)UFProtocol.createFrom(execSocket);
          if (reply != null) System.out.println(reply.toString());
        } catch(Exception ex) {
          ex.printStackTrace();
          System.err.println(_mainClass + "::releaseLock> "+ex.toString());
          retVal = false;
        }
        if (reply == null || retVal == false) {
          try {
	    lockExecClient(false);
            System.out.println(_mainClass +"::releaseLock> Error: Received null object!");
            //try again
            retVal = disconnectFromExec();
            if (!retVal) return false;
            int ntries = 0;
            while (!_execConnected && ntries < 3) {
              hibernate(2000);
              ntries++;
              _execConnected = connectToExec("execclient", "execclient commands");
            }
            hibernate(2000);
            if (!_execConnected) {
              System.out.println(_mainClass+"::releaseLock> WARNING: Can't connect to executive agent to release lock! "+ctime());
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Can't connect to executive agent to release lock!");
              return true;
            }
	    lockExecClient(true);
            execSocket.setSoTimeout(_execTimeout);
            //resend request
            nbytes = req.sendTo(execSocket);
            reply = (UFStrings)UFProtocol.createFrom(execSocket);
            if (reply == null) {
              System.out.println(_mainClass +"::releaseLock> Error: Received null object again! "+ctime());
              lockExecClient(false);
              _health = "WARNING";
              updateDatabase(_mainClass+":health", _health, "WARNING", "Received null object again!");
              return true;
            }
            System.out.println("TRY 2: "+reply.toString());
	    System.out.println(_mainClass +"::releaseLock> Received "+reply.toString());
	    retVal = true;
          } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(_mainClass + "::releaseLock> "+ex.toString());
            lockExecClient(false);
            _health = "WARNING";
            updateDatabase(_mainClass+":health", _health, "WARNING", "Error receiving RELEASE LOCK reply!");
            return true;
          }
        }
        //Unlock exec client
        lockExecClient(false);
        if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("IS_RELEASED")) {
          //Lock successfully released 
          System.out.println(_mainClass+"::releaseLock> Lock released!");
          retVal = true;
        } else if (getCmdName(reply).equals("LOCK") && getCmdParam(reply).equals("NOT_RELEASED")) {
          System.out.println(_mainClass+"::releaseLock> Lock NOT released!");
          retVal = false;
        }
      }
      return retVal;
    }


    protected void lockExecClient(boolean lock) {
      /* Helper method to notify exec client that it should not try to read
       * on socket because a reply from executive agent about lock status is
       * expected -- or to unlock the exec client to listen to socket again. */
      synchronized(_clientThreads) {
        for (int j = 0; j < _clientThreads.size(); j++) {
	  UFMMTClientThread ct = _clientThreads.elementAt(j);
          if (ct._clientSocket == execSocket) ct.setLocked(lock);
	}
      }
    }

//-----------------------------------------------------------------------------------------------------
    protected synchronized void updateDatabase(String key, String value) { 
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value);
      }
    }

    protected synchronized void updateDatabase(String key, int value) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value);
      }
    }

    protected synchronized void updateDatabase(String key, long value) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value);
      }
    }

    protected synchronized void updateDatabase(String key, float value) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value);
      }
    }

    protected synchronized void updateDatabase(String key, String value, String health, String mess) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value, health, mess);
      }
    }

    protected synchronized void updateDatabase(String key, int value, String health, String mess) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value, health, mess);
      }
    }

    protected synchronized void updateDatabase(String key, long value, String health, String mess) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value, health, mess);
      }
    }

    protected synchronized void updateDatabase(String key, float value, String health, String mess) {
      synchronized(database) {
        UFRecord rec = (UFRecord)database.get(key);
        if (rec == null) return;
        rec.updateValue(value, health, mess);
      }
    }

    protected synchronized boolean updateDatabase(UFRecord rec) {
      synchronized(database) {
        String key = rec.name();
        if (database.containsKey(key)) {
	  UFRecord oldRec = (UFRecord)database.get(key);
	  if (rec.isNewer(oldRec)) {
	    database.put(key, rec);
	    return true;
	  }
        }
      }
      return false;
    }
//-----------------------------------------------------------------------------------------------------

    protected synchronized String getMMTValue(String key) {
      UFRecord rec = null;
      synchronized(database) { rec = (UFRecord)database.get(key); }
      if (rec == null) return null;
      return rec.getValue();
    }

    protected synchronized int getMMTIntValue(String key) {
      UFRecord rec = null;
      synchronized(database) { rec = (UFRecord)database.get(key); }
      if (rec == null) return Integer.MIN_VALUE; 
      return rec.getInt();
    }

    protected synchronized long getMMTLongValue(String key) {
      UFRecord rec = null;
      synchronized(database) { rec = (UFRecord)database.get(key); }
      if (rec == null) return (long)Integer.MIN_VALUE; 
      return rec.getLong();
    }

    protected synchronized float getMMTFloatValue(String key) {
      UFRecord rec = null;
      synchronized(database) { rec = (UFRecord)database.get(key); }
      if (rec == null) return (float)Integer.MIN_VALUE; 
      return rec.getFloat();
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateStatusClients() {
      //Loop through in descending order for remove to not cause issues.
      synchronized(_clientThreads) {
        for (int j = _clientThreads.size()-1; j >= 0; j--) {
          UFMMTClientThread ct = _clientThreads.elementAt(j);
	  //check for terminated threads and remove these from vector
          if (ct.isTerminated()) {
            System.out.println(_mainClass+"::updateStatusClients> Removing terminated client "+ct.getThreadName());
	    try {
	      _clientThreads.remove(j);
	    } catch (Exception e) {
	      System.out.println(_mainClass+"::updateStatusClients> client "+ct.getThreadName()+" is already removed.");
	      return;
	    }
          } else if (ct.statusClient()) {
	    /* now call updateStatus method to update records in client thread and notify that it is ready to send
	     * Actual socket send takes place in client thread not here so that if one socket gets hung, it doesn't
	     * hang entire ancillary thread!!! */
            if (_verbose) System.out.println(_mainClass+"::updateStatusClients> Updating client "+ct.getThreadName());
	    ct.updateStatus(database);
          }
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateDatabase() {
      boolean updated = false;
      synchronized(_clientThreads) {
        for (int j = 0; j < _clientThreads.size(); j++) {
	  UFMMTClientThread ct = _clientThreads.elementAt(j);
	  while (ct.hasNewRecords()) {
	    UFRecord rec = ct.getNextRecord();
	    updated = updateDatabase(rec);
	    if (updated) System.out.println(_mainClass + "::updateDatabase> Updated record "+rec.name()+" = "+rec.getValue());
	  }
	}
      }
    }
//-----------------------------------------------------------------------------------------------------


    protected void _sendStatusClients( UFStrings ufpStatusMsg )
    {
      synchronized(_clientThreads) {
	for( int i = 0; i < _clientThreads.size(); i++ )
	    {
		UFMMTClientThread ct = _clientThreads.elementAt(i);

		if( ct.statusClient() ) {
		    if( ct._send( ufpStatusMsg ) <= 0 ) ct._terminate();
		}
	    }
      }
    }
//-----------------------------------------------------------------------------------------------------

    protected void _sendStatusClients( UFInts ufpStatusMsg )
    {
      synchronized(_clientThreads) {
	for( int i = 0; i < _clientThreads.size(); i++ )
	    {
		UFMMTClientThread ct = _clientThreads.elementAt(i);

		if( ct.statusClient() ) {
		    if( ct._send( ufpStatusMsg ) <= 0 ) ct._terminate();
		}
	    }
      }
    }

//-----------------------------------------------------------------------------------------------------
    /* Disconnect method */
    protected synchronized void disconnect(String device) {
      synchronized(deviceSockets) {
        if (!deviceSockets.containsKey(device)) return;
        Socket deviceSocket = (Socket)deviceSockets.get(device);
        System.out.println(_mainClass+"::disconnect> Disconnecting from "+device);
        try {
	  deviceSocket.close();
	} catch (Exception e) { }
	deviceSockets.remove(device);
	/* Remove reader and writer as well so new ones will be created
	 * and bound to new socket on reconnect */
	if (deviceReaders.containsKey(device)) deviceReaders.remove(device);
	if (deviceWriters.containsKey(device)) deviceWriters.remove(device);
      }
    }

//-----------------------------------------------------------------------------------------------------


    protected synchronized boolean sendCommand(String device, String command) {
      return sendCommand(device, command, false);
    }

    protected synchronized boolean sendCommand(String device, String command, boolean noTerm) {
      Socket deviceSocket;
      synchronized(deviceSockets) {
        if (!deviceSockets.containsKey(device)) return false;
        deviceSocket = (Socket)deviceSockets.get(device);
      }
      if (deviceSocket == null) {
	System.out.println(_mainClass+"::sendCommand> ERROR: Null device socket!");
	return false;
      }
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

    protected boolean sendCommandImmediately(String device, String command, boolean noTerm) {
      System.out.println(_mainClass+"::sendCommandImmediately> sending "+command+" to "+device);
      Socket deviceSocket;
      synchronized(deviceSockets) {
        if (!deviceSockets.containsKey(device)) return false;
        deviceSocket = (Socket)deviceSockets.get(device);
      }
      System.out.println(_mainClass+"::sendCommandImmediately> found "+device);
      if (deviceSocket == null) {
        System.out.println(_mainClass+"::sendCommand> ERROR: Null device socket!");
        return false;
      }
      PrintWriter devOut;
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
      return true;
    }

    protected synchronized String recvResponse(String device) {
      return recvResponse(device, false, false);
    }

    protected synchronized String recvResponse(String device, boolean readExtra) {
      return recvResponse(device, readExtra, false);
    }

    protected synchronized String recvResponse(String device, boolean readExtra, boolean isEdas) {
      Socket deviceSocket;
      synchronized(deviceSockets) {
        if (!deviceSockets.containsKey(device)) return null;
        deviceSocket = (Socket)deviceSockets.get(device);
      }
      if (deviceSocket == null) {
        System.out.println(_mainClass+"::sendCommand> ERROR: Null device socket!");
        return null;
      }
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
	    String extra = "";
	    while(devIn.ready()) {
	      int x = devIn.read();
	      extra += (char)x;
	    }
	    if (_verbose) System.out.println(_mainClass+"::recvResponse> Received extra bytes: "+extra);
	    //New version of EDAS contains multi-line responses.  Return all bytes read.
	    reply += extra;
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
	  reply = "ERROR: "+ioe.toString();
	  if (ioe.toString().indexOf("SocketException") != -1) {
	    try {
	      deviceSocket.close();
	    } catch (Exception e) { }
	  }
	  return null;
	}
        try {
	  deviceSocket.setSoTimeout(0);
        } catch(IOException e) { }
      }
      return reply;
    }

    protected String recvResponseNoSync(String device, boolean readExtra) {
      Socket deviceSocket;
      synchronized(deviceSockets) {
        if (!deviceSockets.containsKey(device)) return null;
        deviceSocket = (Socket)deviceSockets.get(device);
      }
      if (deviceSocket == null) {
        System.out.println(_mainClass+"::sendCommand> ERROR: Null device socket!");
        return null;
      }
      BufferedReader devIn;
      String reply = null;
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
        reply = "ERROR: "+ioe.toString();
        if (ioe.toString().indexOf("SocketException") != -1) {
          try {
            deviceSocket.close();
          } catch (Exception e) { }
        }
        return null;
      }
      try {
        deviceSocket.setSoTimeout(0);
      } catch(IOException e) { }
      return reply;
    }

//-----------------------------------------------------------------------------------------------------

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public String getDateString() {
      /* Return String of format 2010Jan01 */
      String[] date = new Date(System.currentTimeMillis()-43200).toString().split(" ");
      return date[5]+date[1]+date[2];
    }

//-----------------------------------------------------------------------------------------------------

    public void waitForReply(Socket commSoc, String name) {
      long startTime = System.currentTimeMillis();
      try {
        while(commSoc.getInputStream().available() <= 0) {
          try {
            Thread.sleep(25);
          } catch(InterruptedException e) { }
	  if (System.currentTimeMillis() - startTime >= _execTimeout) break;
        }
      } catch(IOException e) {
        System.out.println(e.toString());
        e.printStackTrace();
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void heartbeat() {
	/* This method is called from the Ancillary Thread to update the
	 * heartbeat every second and update the database entry for the
	 * heartbeat */
	_heartbeat = System.currentTimeMillis()/1000;
	updateDatabase(_mainClass+":heartbeat", _heartbeat);
    }

//-----------------------------------------------------------------------------------------------------
    /* This helper method should be overridden to return the proper subclass
     * UFMMTClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFMMTClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      return new UFMMTClientThread(clsoc, clientCount, simMode);
    }

//=====================================================================================================

    // Class ListenThread creates thread to Listen for socket connections from clients:

    protected class ListenThread extends Thread {

	private String _className = getClass().getName();

	public ListenThread() {
	  setName("Listen Thread");
	}

	public void run() {
	    try {
		System.out.println(_className + ".run> server port = "+_serverPort);
		ServerSocket ss = new ServerSocket(_serverPort);
		while (true) {	  
		    Socket clsoc = ss.accept();
		    System.out.println(_className + ".run> accepting new connection at "+ctime()+"...");
		    int nclients = _clientThreads.size();
		    System.out.println("\tTotal clients: "+nclients);
/*
 * No longer have a max number of clients!
		    if (nclients > 20) {
		      //maximum number of clients = 20!
		      UFStrings ufs = new UFStrings("ERROR", "Error: Already at max client count of 20.");
		      try {
			//send error message and close socket
			clsoc.setSoTimeout(_sendTimeout);
			int nbytes = ufs.sendTo(clsoc);
			clsoc.close();
		      } catch (Exception ex) {
			System.out.println(_className+".run> Error: "+ex.toString()); 
		      } 
		      System.out.println(_className+".run> Error: Already at max client count of 20.");
		      continue;
		    }
*/
		    /* Use helper method getNewClientThread to get correct subclass */
		    UFMMTClientThread ct = getNewClientThread(clsoc, ++_clientCount, _simMode);
		    _clientThreads.add( ct );
		    System.out.println(_className + ".run> connection accepted.");
		    ct.verbose(_verbose);
		    Thread clientThread = new Thread(ct);
		    clientThread.setName(ct.getThreadName());
		    clientThread.start();
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

        public AncillaryThread() {
	  setName("Ancillary Thread");
	}

	public void run() { 
          while(true) {
	    if (_shutdown) return;
            while (_isRunning) {
	      if (_shutdown) return;
	      /* update heartbeat */
	      heartbeat();
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
	      /* Send new database values to status client threads.
		Actual sending will occur in each UFMMTClientThread */
	      updateStatusClients();
	      /* Check for new records */
	      updateDatabase();
	      /* Sleep 1 second */
	      hibernate();
	      /* Update _ancillaryRunning at end of loop */
	      _ancillaryRunning = true;
            }
	    hibernate();
	    System.out.println("shutdown: "+_shutdown);
          }
        }
        public void stopLoops()    {_isRunning = false;}
        public void restartLoops() {_isRunning = true;}
	public void shutdownLoops() {_shutdown = true;}
	public void forceShutdown() {
	  _isRunning = false;
	  _shutdown = true;
	  /* Kill all status clients.  Loop through in descending order for remove to not cause issues. */
	  synchronized(_clientThreads) {
	    for (int j = _clientThreads.size()-1; j >= 0; j--) {
	      UFMMTClientThread ct = _clientThreads.elementAt(j);
	      if (ct.statusClient()) {
	        System.out.println(_className+"::forceShutdown> Terminating client "+ct.getThreadName()+" at "+ctime());
	        ct._terminate();
	        _clientThreads.remove(j);
	      }
            }
	    System.out.println(_className+"::forceShutdown> clients all terminated.");
	  }
	}
    }
} //end of class UFMMTThreadedAgent
