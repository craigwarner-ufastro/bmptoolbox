package javaUFLib;

/**
 * Title:        UFClientThread.java
 * Version:      (see rcsID)
 * Authors:      Frank Varosi and David Rashkin
 * Company:      University of Florida
 * Description:  Thread for handling client requests.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.Time;
import javaUFProtocol.*;
import javaUFLib.*;

//===========================================================================================
// Class UFClientThread creates a thread for each client, for handling requests from client.
//  Also has option to access a PLC interface.

public class UFClientThread extends Thread {

    public static final String rcsID = "$Name:  $ $Id: UFClientThread.java,v 1.22 2011/03/14 07:26:33 varosi Exp $";

    protected Socket _clientSocket;
    protected int _clientNumber = 0;
    protected int _sendTimeOut = 13000;
    protected String _hostname;
    protected String _agentName = "";
    protected String _clientName;
    protected String _threadName;
    protected boolean _statusClient = false, _keepRunning = true, _closeSocket = false;
    protected String _connTime;
    protected String _className = getClass().getName();
    protected UFPLCifce _PLCifce = null; // optional PLC interface for control sys.
    protected UFPidAgent _agent = null; // optional PID agent hook
    protected HashMap< String, UFPidLoop > _pidMap = null; // optional PID loop hook.
    protected boolean _verbose = false;

    public UFClientThread( Socket clientSoc, int clientNumber ) {
	_clientNumber = clientNumber;
	_clientSocket = clientSoc;
	_hostname = clientSoc.getInetAddress().getHostName();
	_connTime = ctime();
	_clientName = _hostname + ":" + _clientNumber;
	System.out.println( _className + "> time = " + _connTime );
	System.out.println( _className + "> new connection from: " + _clientName );
    }

    //optional ctor to access a PLC interface:

    public UFClientThread( Socket clientSoc, int clientNumber, UFPLCifce plcifce ) {
        this( clientSoc, clientNumber );
	_PLCifce = plcifce;
    }

    // optional ctor to access PID Loops:

    public UFClientThread( Socket clientSoc, int clientNumber, UFPidAgent agent ) {
	this( clientSoc, clientNumber );
	_agent = agent;
	_pidMap = agent.getPidMap();
    }
//-----------------------------------------------------------------------------------------

    public void run() {

	System.out.println( _className + ".run> waiting for requests from: " + _clientName );
	_threadName = _className + "(" + _clientName + ")> ";
	// set socket timeout to infinite so thread just waits for requests:
	try {
	    _clientSocket.setSoTimeout( 0 );
	} catch ( IOException ioe ) {
	    System.err.println( ioe.toString() );
	}
	int nreq = 0;
	int nulls = 0;
	int zeros = 0;

	while ( _keepRunning ) {
	    // recv and process requests from client:
	    UFProtocol ufpr = UFProtocol.createFrom( _clientSocket );
	    ++nreq;

	    if( ufpr == null ) {
		System.out.println( _threadName + ctime() + "> Recvd null object." );
		if( ++nulls > 1 ) _keepRunning = false;
	    }
	    else if( _handleRequest( ufpr ) <= 0 ) {
		if( ++zeros > 1 ) _keepRunning = false;
	    }

	    yield();
	}

	_statusClient = false;
	_closeSocket = true;
	System.out.println( _className + ".run> dropped socket to [" + _clientName + "]" );
    }
//-----------------------------------------------------------------------------------------

    public void _terminate() {
	_keepRunning = false;
	_statusClient = false;
	_closeSocket = true;
	System.out.println( _className + ".terminate> kill socket to [" + _clientName + "]" );
    }

    public boolean statusClient() { return _statusClient;  }

    public boolean validSocket() { if( _clientSocket == null ) return false;  else return true;  }

    public void setAgentName( String name ) { _agentName = name; }

    public String hostname() { return _hostname; }

    public void verbose( boolean talk ) { _verbose = talk; }

    protected Socket _socket() { return _clientSocket; }

    protected void setupStatusCheck() {} // override for optional immediate status check.

    public String ctime() {
	String date = new Date( System.currentTimeMillis() ).toString();
	return ( date.substring( 4, 19 ) + " LT" );
    }
//----------------------------------------------------------------------------------------
    // UFClientThread method _send() used by _handleRequest() and by _sendStatusClients().

    public synchronized int _send( UFProtocol ufpr ) {
	try {
	    // set socket timeout to finite value to try and detect send error:
	    _clientSocket.setSoTimeout( _sendTimeOut );
	    int nbytes = ufpr.sendTo( _clientSocket );
	    // set socket timeout back to infinite so thread just waits for requests:
	    _clientSocket.setSoTimeout( 0 );
	    if( nbytes <= 0 ) System.out.println( _className + ".send> zero bytes sent." );
	    return nbytes;
	} catch ( IOException iox ) {
	    System.err.println( _className + ".send> " + iox.toString() );
	    return ( -1 );
	} catch ( Exception ex ) {
	    System.err.println( _className + ".send> " + ex.toString() );
	    return ( -1 );
	}
    }
//----------------------------------------------------------------------------------------
    // UFClientThread methods _handleRequest( UFProtocol or String ) parses the request,
    // performs action if needed, and sends reply back to client.

    private int _handleRequest( UFProtocol ufpr ) {

	String request = ufpr.name().toLowerCase();
	UFStrings reply = null;

	if( ufpr instanceof UFStrings ) {

	    if( request.indexOf( "multi" ) >= 0 ) reply = _handleMultiRequest( (UFStrings)ufpr );
	    else if( request.indexOf( "pid" ) >= 0 ) reply = _handlePidRequest( (UFStrings)ufpr );
	    else reply = _handleMoreRequests( ufpr );

	    System.out.println( _threadName + reply.timeStamp().substring( 0, 19 ) + " : " + reply.name() );
	}
	else {
	    String time = ctime() + "> request: " + ufpr.name();
	    System.out.println( _threadName + time );

	    if( request.indexOf( "status" ) > 0 ) {

		_statusClient = true;
		reply = new UFStrings( "OK:accepted", "Will deliver status stream to client." );
		setupStatusCheck();
	    }
	    else if( request.indexOf("handshake") >= 0 || request.indexOf("client") > 0 ) {

		reply = new UFStrings( "OK:accepted", "Agent " + _agentName + " accepted client." );
	    }
	    else reply = _handleMoreRequests( ufpr );

	    String response = reply.valData( 0 );
	    int nchar = Math.min( 60, response.length() );
	    System.out.println( _threadName + reply.timeStamp().substring( 0, 19 ) + " : " + reply.name() + " : "
		    + response.substring( 0, nchar ) );
	}

	return _send( reply );
    }
//----------------------------------------------------------------------------------------
    // Handle multiple requests:

    private UFStrings _handleMultiRequest( UFStrings multiReq ) {
	String req = multiReq.name();
	int bpos = req.indexOf( ">" );
	if( bpos < 0 ) bpos = req.indexOf( " " );
	if( bpos < 0 ) bpos = Math.min( 13, req.length() - 2 );
	String time = ctime() + "> request: " + req.substring( 0, ++bpos );
	System.out.println( _threadName + time );
	if( bpos < req.length() - 1 ) System.out.println( req.substring( bpos ) );
	String status = "";
	Vector multiReply = new Vector( multiReq.numVals() );

	for ( int k = 0; k < multiReq.numVals(); k++ ) {
	    System.out.print( _threadName + "_handleMultiRequest> " + multiReq.valData( k ) );
	    UFStrings reply = _handleRequest( multiReq.valData( k ).toLowerCase() );
	    status += ( reply.name() + ":" );
	    multiReply.add( reply.valData( 0 ) );
	    System.out.println( "\t = " + reply.valData( 0 ) );
	}

	return new UFStrings( status, multiReply );
    }
//----------------------------------------------------------------------------------------

    protected UFStrings _handlePidRequest( UFStrings request ) {

	String req = request.valData(0);
	
	UFStrings reply = null;
	String[] loopNames = new String[1];
	
	if(_pidMap != null) {
	    String pidName = request.name().split(":", 3)[2].trim();
	    UFPidLoop pidLoop = _pidMap.get(pidName);
	    
	    if( req.indexOf("loop names") >= 0 ) {
		loopNames = _pidMap.keySet().toArray(loopNames);
		String replyStr = "";
		for(String s : loopNames) replyStr += s + ",";
		reply = new UFStrings("loop names", replyStr);
	    }
	    else if( pidLoop != null ) {
		if(req.indexOf("set") >= 0 ) {
		    try {
			int eqpos = req.indexOf("=");

			if( req.indexOf("set point") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set set point = " + val);
			    pidLoop.setSetPoint( val );
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set set point = " + val);
			}
			else if( req.indexOf("p gain") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set p gain = " + val);
			    pidLoop.setPGain(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set p gain = " + val);
			}
			else if( req.indexOf("i gain") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set i gain = " + val);
			    pidLoop.setIGain(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set i gain = " + val);
			}
			else if( req.indexOf("d gain") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set d gain = " + val);
			    pidLoop.setDGain(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set d gain = " + val);
			}
			else if( req.indexOf("output bias") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set output bias = " + val);
			    pidLoop.setOutputBias(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set output bias = " + val);
			}
			else if( req.indexOf("max power") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set Max. Power = " + val);
			    pidLoop.setMaxPower(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set Max. Power = " + val);
			}
			else if( req.indexOf("filter weight") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set filter weight = " + val);
			    pidLoop.setFilterWeight(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set filter weight = " + val);
			}
			else if( req.indexOf("manual point") >= 0 ) {
			    float val = Float.parseFloat( req.substring( ++eqpos ).trim() );
			    System.out.println(pidName + ": set manual point = " + val);
			    pidLoop.setManualSetPoint(val);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set manual point = " + val);
			}
			else if( req.indexOf("t probes") >= 0 ) {
			    String val = req.substring( ++eqpos ).trim();
			    System.out.println(pidName + ": set t probes = " + val);
			    String[] vals = val.split(",");
			    int[] tProbeIndices = new int[vals.length];
			    for(int i = 0; i < vals.length; i++) 
				tProbeIndices[i] = Integer.parseInt(vals[i]);
			    pidLoop.setTProbeIndices(tProbeIndices);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": set t probes = " + val);
			}
		    }catch(NumberFormatException e) {
			return new UFStrings("ERROR", "Could not parse float value after = " + req);
		    }
		}
		else if(req.indexOf("request") >= 0 ) {
		    if( req.indexOf("restart") >= 0 ) {
			System.out.println(pidName + ": request restart");
			pidLoop.reset();
			return new UFStrings("OK", "COMPLETED " + pidName + ": restart");
		    }
		    else if( req.indexOf("values") >= 0) {
			System.out.println(pidName + ": request values");
			_agent.queryPidValues(pidName);
			return new UFStrings("OK", "COMPLETED " + pidName + ": request values");
		    }
		    else if( req.indexOf("manual control") >= 0) {
			System.out.print(pidName + ": request manual control ");
			if(req.indexOf("off") >= 0) {
			    System.out.println("off");
			    pidLoop.setManual(false);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request manual control off");
			}
			else {
			    System.out.println("on");
			    pidLoop.setManual(true);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request manual control on");
			}
		    }
		    else if( req.indexOf("verbose") >= 0 ) {
			System.out.print(pidName + ": request verbose ");
			if(req.indexOf("off") >= 0 ) {
			    System.out.println("off");
			    pidLoop.setVerbose(false);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request verbose off");
			}
			else {
			    System.out.println("on");
			    pidLoop.setVerbose(true);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request verbose on");
			}
		    }
		    else if( req.indexOf("turn") >= 0 ) {
			System.out.print(pidName + ": request turn ");
			if(req.indexOf("off") >= 0) {
			    System.out.println("off");
			    pidLoop.setOn(false);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request turn off");
			}
			else {
			    System.out.println("on");
			    pidLoop.setOn(true);
			    return new UFStrings("OK", "COMPLETED " + pidName + ": request turn on");
			}
		    }
		}
	    }
	    else {
		return new UFStrings("ERROR", "PID Loop " + pidName + " not found");
	    }
	}
	else
	    reply = new UFStrings("ERROR", "PID requests not enabled" );
	
	return reply;			
    }
//----------------------------------------------------------------------------------------
    // Handle request in a UFProtocol, default is to just look at name field,
    // and invoke one of following methods ( can be overridden to provide more options ) :

    protected UFStrings _handleMoreRequests( UFProtocol clientReq ) {

	if( clientReq instanceof UFStrings )
	    return _handleRequest( (UFStrings)clientReq );
	else
	    return _handleRequest( clientReq.name().toLowerCase() );
    }
//----------------------------------------------------------------------------------------
    // Handle request in a UFStrings, usually ["status","FITS"]:

    protected UFStrings _handleRequest( UFStrings clientReq ) {

	String cname = clientReq.name();
	String time = ctime() + "> request from: " + cname;
	System.out.println( _threadName + time );
	String request = clientReq.valData( 0 );

	for ( int k = 1; k < clientReq.numVals(); k++ )
	    request += ( " > " + clientReq.valData( k ) );

	System.out.println( _threadName + "_handleRequest> " + request );

	return _handleRequest( request.toLowerCase() );
    }
//----------------------------------------------------------------------------------------
    // Handle specific requests of a client (method usually overridden) :

    protected UFStrings _handleRequest( String request ) {

	UFStrings reply = null;

	try {
	    if( request.indexOf( "get" ) >= 0 ) {}
	    else if( request.indexOf( "set" ) >= 0 ) {}
	    else if( request.indexOf( "request" ) >= 0 ) {}
	    else if( request.indexOf( "send" ) >= 0 ) {}
	    else if( request.indexOf( "update" ) >= 0 ) {}
	    else reply = new UFStrings( "ERROR", "Unknown request: " + request );

	    if( reply == null ) reply = new UFStrings( "ERROR", "No reply/action for request: " + request );

	} catch ( Exception ex ) {
	    System.err.println( ex.toString() );
	    ex.printStackTrace( System.out );
	    reply = new UFStrings( "ERROR", ex.toString() );
	}

	return reply;
    }
} // end of class UFClientThread
