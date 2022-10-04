package javaMMTLib;
/**
 * Title:        UFMMTClientThread
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for threads handling client requests 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.Time;

import javaUFProtocol.*;
import javaUFLib.*;

//===========================================================================================
// Class UFMMTClientThread creates a thread for each client, for handling requests from client.

public class UFMMTClientThread implements Runnable {

    protected Thread _thread;
    protected Socket _clientSocket;
    protected int _clientNumber = 0;
    protected int _sendTimeOut = 10000;
    protected String _hostname;
    protected String _agentName = "";
    protected String _clientName;
    protected String _threadName;
    protected boolean _statusClient = false, _keepRunning = true, _closeSocket=false, _statusUpdated=false, _statusUpdating=false;
    protected boolean _imageClient = false, _execClient = false, _fullClient = false;
    protected boolean _commandClient = false, _waitingForResponse = false, _isLocked = false;
    protected String _connTime;
    protected String _className = getClass().getName();
    protected boolean _verbose = false;
    protected int imageMode = UFMMTImg.MODE_REGULAR;
    protected LinkedHashMap <String, String> lastSent;
    protected ArrayDeque <UFStrings> pendingReqs;
    protected ArrayDeque <UFRecord> databaseUpdates;
    protected ArrayDeque <UFRecord> statusUpdates;
    protected UFStrings currReq = null;
    protected String responseToWaitFor = null, lastResponse = null;
    protected long responseTimeout;
    public static String installDir = UFExecCommand.getEnvVar("UFMMTINSTALL");

    public UFMMTClientThread( Socket clientSoc, int clientNumber, boolean sim) {
	_clientNumber = clientNumber;
	_clientSocket = clientSoc;
	_hostname = clientSoc.getInetAddress().toString(); //don't use getHostName anymore due to slowness 8/23/16
	_connTime = ctime();
	_clientName = _hostname + ":" + _clientNumber;
        _threadName = _className+"("+_clientName+")";
	System.out.println(_className + "> time = " + _connTime);
	System.out.println(_className + "> new connection from: " + _clientName);
	System.out.println(_clientSocket.toString());
        lastSent = new LinkedHashMap(100);
	pendingReqs = new ArrayDeque(20);
	databaseUpdates = new ArrayDeque(100);
        statusUpdates = new ArrayDeque(20);
    }

//----------------------------------------------------------------------------------------
    public String getThreadName() {
	return _threadName;
    }
//----------------------------------------------------------------------------------------

    public boolean hasRequest() {
	if (pendingReqs.isEmpty()) return false;
	UFStrings theReq = (UFStrings)pendingReqs.peek();
	if (theReq == currReq) {
	  /* hasRequest has already been called for this request */
	  return false;
	}
	/* Ensure action can't be called twice for this request */
	currReq = theReq;
	return true;
    }

    public UFStrings getRequest() {
	if (pendingReqs.isEmpty()) return null;
	UFStrings theReq = (UFStrings)pendingReqs.remove();
	currReq = null;
	return theReq; 
    }

    public String peekRequestName() {
        if (pendingReqs.isEmpty()) return null;
        UFStrings theReq = (UFStrings)pendingReqs.peek();
	return theReq.name();
    }

//----------------------------------------------------------------------------------------

    public String getDateString() {
      /* Return String of format 2010Jan01 */
      String[] date = new Date(System.currentTimeMillis()-43200).toString().split(" ");
      return date[5]+date[1]+date[2];
    }

//----------------------------------------------------------------------------------------

    public void waitForResponse(String response, int timeout) {
      /* set response to wait for -- timeout in sec */
      _waitingForResponse = true;
      responseToWaitFor = response;
      responseTimeout = System.currentTimeMillis()+timeout*1000;
      System.out.println("WAIT_FOR_RESPONSE: Timeout = "+timeout+"; responseTimeout = "+responseTimeout);
    }

    public boolean isWaitingForResponse() {
      if (_waitingForResponse && System.currentTimeMillis() > responseTimeout) {
	/* Timed out */
	_waitingForResponse = false;
	lastResponse = "ERROR: Timed Out waiting for "+responseToWaitFor;
	System.out.println("ResponseTimeout = "+responseTimeout+"; Time = "+System.currentTimeMillis());
      }
      return _waitingForResponse;
    }

    public String getLastResponse() {
      return lastResponse;
    }

//----------------------------------------------------------------------------------------

    public void setLocked(boolean locked) {
      _isLocked = locked;
    }

//----------------------------------------------------------------------------------------

    public boolean hasNewRecords() {
        if (databaseUpdates.isEmpty()) return false;
        return true;
    }

    public UFRecord getNextRecord() {
        if (databaseUpdates.isEmpty()) return null;
        return (UFRecord)databaseUpdates.remove();
    }

//----------------------------------------------------------------------------------------

    public void run() {

	System.out.println(_className + ".run> waiting for requests from: "+_clientName);
	//set socket timeout to infinite so thread just waits for requests:
        InputStreamReader devIn = null;
	try {
          devIn = new InputStreamReader(_clientSocket.getInputStream());
	  _clientSocket.setSoTimeout(0);
	}
	catch( IOException ioe ) { System.out.println( ioe.toString() ); }
	int nreq = 0;
	int nulls = 0;
	int zeros = 0;

	while( _keepRunning ) {

	    while (_statusClient || _imageClient) {
		/* These types of clients do not block for requests */
		try {
		  Thread.sleep(50); 
		  if (devIn.ready() && !_isLocked) break;	
		  if (!_keepRunning) break;
		}
		catch (InterruptedException e) {}
		catch (IOException e) {
		  System.out.println(e.toString());
		}
		if (_statusClient && _statusUpdated) {
		  int nbytes = _sendStatusUpdates();
		  if (nbytes <= 0) {
		    System.out.println(_threadName+ctime()+"> Error sending status update!");
		    //end thread if disconnected
                    if (++zeros > 1) _terminate();
		  }
		}
	    }
	    //recv and process requests from client:
	    UFProtocol ufpr = UFProtocol.createFrom( _clientSocket );
	    ++nreq;
	       
	    if( ufpr == null ) {
		System.out.println( _threadName + ctime() + "> Recvd null object.");
		if( ++nulls > 1 ) _keepRunning = false;
	    }
	    else if( _handleRequest( ufpr ) <= 0 ) {
		if( ++zeros > 1 )
		    _keepRunning = false;
	    }
	}

	_statusClient = false;
        _fullClient = false;
        _imageClient = false;
        _execClient = false;
	_commandClient = false;
	closeSocket();
	System.out.println(_threadName + ".run> dropped socket to ["+_clientName+"]");
    }
//----------------------------------------------------------------------------------------

    public void _terminate() {
	_keepRunning = false;
	_statusClient = false;
        _fullClient = false;
        closeSocket();
	System.out.println(_threadName + ".terminate> kill socket to ["+_clientName+"]");
    }

    public void closeSocket() {
	System.out.println(_threadName+"::closeSocket> Closing socket "+_clientSocket.toString());
        _closeSocket = true;
	try {
	  _clientSocket.close();
	} catch(Exception e) {
	  System.out.println(_threadName+"::closeSocket> "+e.toString());
	}
    }

    public boolean statusClient() { return _statusClient; }
    public boolean fullClient() { return _fullClient; }
    public boolean imageClient() { return _imageClient; }
    public boolean execClient() { return _execClient; }
    public boolean commandClient() { return _commandClient; }
    public boolean isTerminated() { return _closeSocket; }

    public void setAgentName( String name ) { 
	_agentName = name;
        _threadName = _className+"("+_clientName+":"+_agentName+")";
    }
    public String getAgentName() { return _agentName; }
    public String hostname() { return _hostname; }
    public void verbose( boolean talk ) { _verbose = talk; }
    public Socket _socket() { return _clientSocket; }

    protected void setupStatusCheck() { }  //override for optional immediate status check.
    protected UFStrings setupImageCheck(String request) { return null; }  //override
    protected UFStrings setupExecCheck(String request) { return null; }  //override

    public String ctime() {
	String date = new Date( System.currentTimeMillis() ).toString();
	return( date.substring(4,19) + " LT");
    }

//----------------------------------------------------------------------------------------
    // UFMMTClientThread method updateStatus used by updateStatusClients().
    public int updateStatus(LinkedHashMap <String, UFRecord> database) {
      int nrecs = 0;
      String name = _className.substring(0, _className.indexOf("."));
      if (_statusUpdating) return -1; //sending an update now -- may be hung
      if (!statusUpdates.isEmpty()) return -1;
      for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        if (!key.substring(0, key.indexOf(".")).equals(name)) {
          //This record refers to another agent -- presumably this is the
          //exec agent which has statuses from all agents.  Only send its own
          //records so records don't get sent twice with a lag.
          continue;
        }
        UFRecord rec = (UFRecord)database.get(key);
        if (lastSent.containsKey(key)) {
          /* Record has not been updated since it was last sent */
          if (rec.timeStamp().equals(lastSent.get(key))) continue;
        }
	//add to status updates
	statusUpdates.add(rec);
      }
      nrecs = statusUpdates.size();
      if (nrecs > 0) _statusUpdated = true;
      return nrecs;
    }

//----------------------------------------------------------------------------------------
    // _sendStatusUpdate() replaces _send(LinkedHashMap) to update status clients 
    public synchronized int _sendStatusUpdates() {
      int totbytes = 0;
      int nbytes = 0;
      String name = _className.substring(0, _className.indexOf("."));
      if (!_statusUpdated) return -1; //should not happen
      _statusUpdating = true;
      synchronized(statusUpdates) {
	//while loop to remove updates until arraydeque is empty
	while (!statusUpdates.isEmpty()) {
	  //statusUpdates should only contain recs that are for this agent that have been updated since last send
          UFRecord rec = (UFRecord)statusUpdates.remove();
          if (_verbose) System.out.println(_threadName+".sendStatusUpdates "+ctime()+"> "+rec.name()+" = "+rec.stringAt(0));
          nbytes = _send(rec);
          if (nbytes <= 0) {
            totbytes = -1;
	    statusUpdates.clear(); //since lastSent not updated, statusUpdates will be repopulated next update
            break;
          } else totbytes += nbytes;
          lastSent.put(rec.name(), rec.timeStamp());
        }
	_statusUpdated = false;
      }
      _statusUpdating = false;
      return totbytes;
    }

//----------------------------------------------------------------------------------------
    // UFMMTClientThread method _send() used by updateStatusClients().
    public synchronized int _send(LinkedHashMap <String, UFRecord> database) { 
      int totbytes = 0;
      int nbytes = 0;
      String name = _className.substring(0, _className.indexOf("."));
      synchronized(database) {
        for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
	  if (!key.substring(0, key.indexOf(".")).equals(name)) {
	    //This record refers to another agent -- presumably this is the
	    //exec agent which has statuses from all agents.  Only send its own
	    //records so records don't get sent twice with a lag.
	    continue;
	  }
          UFRecord rec = (UFRecord)database.get(key);
	  if (lastSent.containsKey(key)) {
	    /* Record has not been updated since it was last sent */
	    if (rec.timeStamp().equals(lastSent.get(key))) continue;
	  }
          if (_verbose) System.out.println(_threadName+".send "+ctime()+"> "+key+" = "+rec.stringAt(0));
	  nbytes = _send(rec);
	  if (nbytes <= 0) {
	    totbytes = -1;
	    break;
	  } else totbytes += nbytes;
	  lastSent.put(key, rec.timeStamp());
        }
      }
      return totbytes;
    }

//----------------------------------------------------------------------------------------
    // UFMMTClientThread method _send() used by updateStatusClients().
    public synchronized int _send(UFMMTImg image) {
      synchronized(_clientSocket) {
        try {
            //set socket timeout to finite value to try and detect send error:
            _clientSocket.setSoTimeout(_sendTimeOut);
            int nbytes = image.sendTo( _clientSocket, imageMode );
            //set socket timeout back to infinite so thread just waits for requests:
            _clientSocket.setSoTimeout(0);
            if (nbytes <= 0) {
		System.out.println( _threadName + "::_send> zero bytes sent to "+_agentName);
	    } else {
		System.out.println(_threadName + "::_send "+ctime()+"> sent "+nbytes+" of image data to client "+_agentName); 
	    }
            return nbytes;
        }
        catch( IOException iox ) {
            System.out.println( _threadName + ".send> " + iox.toString() );
            return(-1);
        }
        catch( Exception ex ) {
            System.out.println( _threadName + ".send> " + ex.toString() );
            return(-1);
        }
      }
    }

//----------------------------------------------------------------------------------------
    // UFMMTClientThread method _send() used by _handleRequest() and by _sendStatusClients().

    public synchronized int _send( UFProtocol ufpr ) {
      if (_verbose) System.out.println(_threadName+".send (to = "+_sendTimeOut+") "+ctime()+"> "+ufpr.name()+" "+_clientSocket.isClosed()); 
      synchronized(_clientSocket) {
	try {
	    //set socket timeout to finite value to try and detect send error:
	    _clientSocket.setSoTimeout(_sendTimeOut);
	    int nbytes = ufpr.sendTo( _clientSocket );
	    if (_verbose) System.out.println("Sent "+nbytes+" bytes "+ctime());
	    //set socket timeout back to infinite so thread just waits for requests:
	    _clientSocket.setSoTimeout(0);
	    if( nbytes <= 0 ) System.out.println( _threadName + ".send> zero bytes sent.");
	    return nbytes;
	}
	catch( IOException iox ) {
	    System.out.println( _threadName + ".send> " + iox.toString() );
	    return(-1);
	}
	catch( Exception ex ) {
	    System.out.println( _threadName + ".send> " + ex.toString() );
	    return(-1);
	}
      }
    }
//----------------------------------------------------------------------------------------

    /** These methods should be overridden by subclasses to invoke the
      * proper behavior upon receiving each type of request */

    protected int handleRecord(UFRecord rec) {
      databaseUpdates.add(rec);
      if (_verbose) System.out.println(_threadName+"::handleRecord> Received record: "+rec.name()+" = "+rec.getValue());
      return 1;
    } 

    protected int handleImage(UFBytes byteHeader) {
      UFMMTImg image;
      int nframes = -1;
      image = new UFMMTImg(byteHeader);
      nframes = image.recvImages(_clientSocket, imageMode);
      return nframes;
    }

    protected int handleAction(UFStrings req) {
      int nreq = req.numVals();
      System.out.println(_threadName+"::handleAction> Received action request bundle: "+req.name());
      System.out.println(_threadName+"::handleAction> contains "+nreq+" requests.");
      pendingReqs.add(req);
      return nreq;
    }

    protected int handleRequest(UFProtocol req) {
      String request = req.name().toLowerCase();
      if (req instanceof UFStrings && request.indexOf("invalid request") > 0) {
	/* Response that this client sent an invalid request */
	String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> previous request invalid: "+val);
	return val.length();
      } else if (req instanceof UFStrings && request.endsWith("error")) {
        /* Received error response */
        String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> received ERROR message: "+val);
        return val.length();
      } else if (req instanceof UFStrings && request.endsWith("success")) {
        /* Received success response */
        String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> received SUCCESS message: "+val);
        return val.length();
      }
      /* Otherwise send invalid request and echo back request name */
      UFStrings reply = new UFStrings(_threadName+": invalid request", request);
      System.out.println(_threadName+"::handleRequest> received invalid request: "+request);
      return _send(reply);
    }

    protected int handleResponse(UFStrings res) {
      int nreq = res.numVals();
      System.out.println(_threadName+"::handleResponse> Received action response: "+res.name());
      System.out.println(_threadName+"::handleResponse> contains "+nreq+" values.");
      for (int j = 0; j < nreq; j++) {
	System.out.println("\tResponse "+(j+1)+" of "+nreq+": "+res.stringAt(j));
	if (_waitingForResponse && responseToWaitFor != null) {
	  /* If waiting for a response from a command, update boolean here
	     e.g., executive agent sends observe to mmtpol agent, waits in
	     action method until observe has finished and returned indicated
	     success message or error */
	  if (res.stringAt(j).toLowerCase().indexOf(responseToWaitFor.toLowerCase()) != -1 || res.stringAt(j).toLowerCase().indexOf("error") != -1) {
	    _waitingForResponse = false;
	    lastResponse = res.stringAt(j);
	  }
	} 
      }
      return nreq;
    }

//----------------------------------------------------------------------------------------
    /* UFMMTClientThread method _handleRequest parses the request and either
     * performs actions necessary or calls helper methods which will be
     * overridden by subclasses.  Sends reply back to client if necessary.
     */
    protected int _handleRequest( UFProtocol ufpr )
    {
	String request = ufpr.name().toLowerCase();
	UFStrings reply = null;

	if (UFRecord.isUFRecord(ufpr)) {
	  /* This is a UFRecord object */
	  return handleRecord(new UFRecord(ufpr));
	}

        System.out.println(_threadName+"::_handleRequest>"+ctime());
	System.out.println(_threadName+"::_handleRequest> request: "+ufpr.name());

	if (ufpr instanceof UFBytes && request.indexOf("ufmmtimgheader:") == 0) {
	  /* This is an image header.  Data should follow. */
	  return handleImage((UFBytes)ufpr);
	}
	
	if (request.indexOf("statusclient") > 0) {
          _statusClient = true;
          reply = new UFStrings("OK:accepted","Will deliver status stream to client.");
	  setAgentName(request.substring(0, request.indexOf(":")));
	  setupStatusCheck();
	} else if (request.indexOf("imageclient") > 0) {
          reply = setupImageCheck(request);
          setAgentName(request.substring(0, request.indexOf(":")));
        } else if (request.indexOf("execclient") > 0) {
	  reply = setupExecCheck(request);
          setAgentName(request.substring(0, request.indexOf(":")));
        } else if (request.indexOf("fullclient") > 0) {
          _fullClient = true;
	  reply = new UFStrings("OK:accepted","Connected to full client.  Ready to receive commands.");
          setAgentName(request.substring(0, request.indexOf(":")));
	} else if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
	  System.out.println(_threadName+"::_handleRequest> connection established.");
	} else if (ufpr instanceof UFStrings && request.indexOf("actionrequest") > 0) {
	  return handleAction((UFStrings)ufpr);
        } else if (ufpr instanceof UFStrings && request.indexOf("actionresponse") > 0) {
          return handleResponse((UFStrings)ufpr);
	} else return handleRequest(ufpr);


	if (reply == null) return 1;
	String response = reply.valData(0);
	int nchar = Math.min( 60, response.length() );
	System.out.println(_threadName + reply.timeStamp().substring(0,19)+ " : " + reply.name()+" : "+ response.substring(0,nchar));
	return _send( reply );
    }

//----------------------------------------------------------------------------------------

    protected void setImageMode(String request) {
        if (request.toLowerCase().indexOf("pre") != -1) {
          imageMode = UFMMTImg.MODE_PRE_POST;
        } else if (request.toLowerCase().indexOf("first") != -1) {
          imageMode = UFMMTImg.MODE_FIRST_ONLY;
        } else imageMode = UFMMTImg.MODE_REGULAR;
    }

} //end of class UFMMTClientThread
