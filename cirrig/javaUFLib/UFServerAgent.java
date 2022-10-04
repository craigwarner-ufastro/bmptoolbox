package javaUFLib;

/**
 * Title:        UFServerAgent.java
 * Version:      (see rcsID)
 * Authors:      Julian van Eyken, Frank Varosi and David Rashkin
 * Company:      University of Florida
 * Description:  PID loop temperature control agent for MARVELS ET instrument.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;

import javaUFProtocol.*;
import javaUFLib.*;

//=====================================================================================================

public class UFServerAgent {

    public static final
	String rcsID = "$Name:  $ $Id: UFServerAgent.java,v 1.1 2008/09/11 07:02:55 varosi Exp $";

    protected int _serverPort = 42001;
    protected String _mainClass = getClass().getName();
    protected boolean _verbose = false;
    protected boolean _simMode = false;

    protected ListenThread  _cListener = null;
    protected Vector< UFClientThread > _clientThreads = new Vector(7,3);

    protected UFPLCifce     _PLCifce;   // optional bject for PLC communications.

//----------------------------------------------------------------------------------------

    public UFServerAgent( int serverPort, boolean verbose, boolean simMode )
    {
	System.out.println( rcsID );

	_serverPort = serverPort;
	_verbose = verbose;
	_simMode = simMode;

	System.out.println(_mainClass + "> server port = " + _serverPort);
	System.out.println(_mainClass + "> creating object for optional PLC interface...");
	_PLCifce = new UFPLCifce();
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

    protected void _sendStatusClients( UFStrings ufpStatusMsg )
    {
	for( int i = 0; i < _clientThreads.size(); i++ )
	    {
		UFClientThread ct = (UFClientThread)_clientThreads.elementAt(i);

		if( ct.statusClient() ) {
		    if( ct._send( ufpStatusMsg ) <= 0 ) ct._terminate();
		}
	    }
    }
//=====================================================================================================

    // Class ListenThread creates thread to Listen for socket connections from clients:

    private class ListenThread extends Thread {

	private String _className = getClass().getName();
	private int _clientCount = 0;

	public ListenThread() {}

	public void run() {
	    try {
		System.out.println(_className + ".run> server port = "+_serverPort);
		ServerSocket ss = new ServerSocket(_serverPort);
		while (true) {	  
		    Socket clsoc = ss.accept();
		    System.out.println(_className + ".run> accepting new connection...");
		    UFClientThread ct = new UFClientThread( clsoc, ++_clientCount, _PLCifce );
		    _clientThreads.add( ct );
		    System.out.println(_className + ".run> connection accepted.");
		    ct.start();
		    yield();
		}
	    } catch (Exception ex) {
		System.err.println(_className + ".run> "+ex.toString());
		return;
	    }
	}
    }
} //end of class UFServerAgent



