package javaUFLib;

//Title:        UFMonitorPanel
//Version:      (see rcsID)
//Author:       Frank Varosi and David Rashkin
//Copyright:    Copyright (c) 2003-7
//Company:      University of Florida, Dept. of Astronomy.
//Description:  Extension of UFLibPanel to also provide StatusMonitor

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javaUFProtocol.*;

//===============================================================================
/**
 * Extension of ULibPanel to also provide StatusMonitor thread, so that _socket and _monitorSocket
 * can both be used, for commanding agent/server and for StatusMonitor thread respectively.
 * Extended and used by classes such as UFobsMonitor in JCI and JDD, and JPanelSystem in JCI.
 * @author Frank Varosi
 */

public class UFMonitorPanel extends UFLibPanel
{
    static final String rcsID = "$Name:  $ $Id: UFMonitorPanel.java,v 1.18 2016/01/15 06:50:47 varosi Exp $";

    protected static final int MONITOR_TIMEOUT   = 0; //default is to wait forever.
    private int _monTimeout = MONITOR_TIMEOUT;

    private StatusMonitor _statusMonitor = null;
    protected Socket _monitorSocket = null;
    protected int _monSocStatus = -2;
    protected UFTextField _monSocState = new UFTextField("Mon.Soc.State",false);
    private int _nullCnt = 0;
    private int _maxBad = 2;
    private int _nullSleep = 3300;
    private UFLibPanel _cmdPanel = null; //to get hostname and port if provided.
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel default constructor.
     * Calls private method, _initMonitor(), which does all component initialization.
     */
    public UFMonitorPanel() {
	super();
	_initMonitor();
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel constructor: used for connecting to an "agent",
     *  in which case the handshake is just the client name.
     */
    public UFMonitorPanel( String host, int port ) {
	super( host, port );
	_initMonitor();
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel constructor: used for connecting to an "agent",
     *  in which case the handshake is just the client name.
     * Param: UFLibPanel cmdPanel specifies panel to check for new hostname and port.
     */
    public UFMonitorPanel( UFLibPanel cmdPanel ) {
	super( cmdPanel._Host, cmdPanel._Port );
	_cmdPanel = cmdPanel;
	_initMonitor();
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel constructor: used for connecting to a "server".
     *  in which case the handshake is specified for a purpose.
     * Param: UFLibPanel cmdPanel specifies panel to check for new hostname and port.
     */
    public UFMonitorPanel( UFLibPanel cmdPanel, String serverHandshake ) {
	super( cmdPanel._Host, cmdPanel._Port, serverHandshake );
	_cmdPanel = cmdPanel;
	_initMonitor();
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel constructor: used for connecting to a "server".
     *  in which case the handshake is specified for a purpose.
     */
    public UFMonitorPanel( String host, int port, String serverHandshake ) {
	super( host, port, serverHandshake );
	_initMonitor();
    }
//-------------------------------------------------------------------------------
    /**
     * Private method: _initMonitor()
     * Component initialization
     * Calls private method createMonitorPanel() to do all component initialization,
     *  and creates StatusMonitor object to wait for and recv status from agent/server.
     */
    private void _initMonitor()
    {
	try {
	    _statusMonitor = new StatusMonitor();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("UFMonitorPanel> Failed creating agent/server status monitor.");
	}

	this.connectButton.addActionListener( new ActionListener()
	    { public void actionPerformed(ActionEvent e) { createMonitorStream(); } });
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel#createMonitorStream
     *  Stop the obs status monitor, call connectMonitor() to reconnect
     *  to Detector Control (DC) Agent or Data Acquisition Server (DAS)
     *  and restart the obs status monitor.
     */
    public boolean createMonitorStream()
    {
	if( _statusMonitor == null ) _statusMonitor = new StatusMonitor();
	_statusMonitor.stopRunning();

	if( connectMonitor() ) {
	    _statusMonitor.resumeRunning();
	    return true;
	} else {
	    _statusMonitor.resumeRunning();  //this will try reconnect again periodically.
	    return false;
	}
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel#connectMonitor
     *  Connect via socket to the automatic status stream feature of DC Agent,
     *  or to the notification stream thread of DAS,
     *  for recving status messages via method recvStatus() below.
     */
    protected boolean connectMonitor()
    {
	closeMonitor(true);
	try { Thread.sleep(700);} catch( Exception _e ) {}

	try {
	    if( _cmdPanel != null ) {
		String h = _cmdPanel.getHostField();
		if( h != null ) { if( h.length() > 0 ) _Host = h; }
		int p = _cmdPanel.getPortField();
		if( p > 0 ) _Port = p;
	    }
	    else {
		_Host = getHostField();
		_Port = getPortField();
	    }
	    String message = ".connectMonitor> port=" + _Port + " @ host = " + _Host;
	    System.out.println( className + message );
	    statusCAR.showAction( message );
	    _monSocStatus = 1;
	    InetSocketAddress agentIPsoca = new InetSocketAddress( _Host, _Port );
	    _monitorSocket = new Socket();
	    _monitorSocket.connect( agentIPsoca, CONNECT_TIMEOUT );
	    _monitorSocket.setSoTimeout( HANDSHAKE_TIMEOUT );

	    if( configMonitor() ) {
		//normally infinite timeout (0) so socket blocks (waits forever) on recv,
		// but value can be configured to finite time, so as to detect faults.
		_monitorSocket.setSoTimeout( _monTimeout );
		indicateConnectMonitor(true);
		return true;
	    }
	    else return false;
	}
	catch (Exception x) {
	    indicateConnectMonitor(false);
	    String message = ".connectMonitor> " + x.toString();
	    System.err.println( className + message );
	    statusCAR.showResponse("ERR: " + message);
	    Toolkit.getDefaultToolkit().beep();
	    closeMonitor();
	    return false;
	}
    }
//-------------------------------------------------------------------------------
    /**
     * UFMonitorPanel#closeMonitor
     * NOTE: boolean showCAR default is false because if close is called after an error,
     * the error message in statusCAR.showResponse(error) should not be overwritten.
     */
    public boolean closeMonitor() { return closeMonitor(false); }

    public boolean closeMonitor(boolean showCAR)
    {
	if( _monitorSocket == null ) return true;

	try {
	    String message = ".connectMonitor> closing status socket...";
	    System.out.println( className + message );
	    if( showCAR ) statusCAR.showAction( message );
	    _monitorSocket.close();
	    _monitorSocket = null;
	    _monSocStatus = -1;
	    if( showCAR ) statusCAR.showResponse(".connectMonitor> socket closed.");
	    return true;
	}
	catch (IOException ioe) {
	    String message = ".connectMonitor> " + ioe.toString();
	    System.err.println( className + message );
	    statusCAR.showResponse( message );
	    _monitorSocket = null;
	    _monSocStatus = -1;
	    return false;
	}
    }
//-------------------------------------------------------------------------------

    protected boolean configMonitor() throws Exception
    {
	statusCAR.showAction(".configMonitor> Sending handshake for status stream...");
	//must send agent/server a timestamp with string "status" in it,
	// and then agent/server will automatically send status stream,
	// and status monitor thread will recv the stream:
	UFTimeStamp uft = new UFTimeStamp(clientName + ":STATUS");

	if( uft.sendTo(_monitorSocket) <= 0 ) {
	    monSocketError("configMonitor","Handshake Send");
	    return false;
	}

	//Note that error reporting method monSocketError() also closes the socket.
	//get response from agent
	UFProtocol ufp = null;

	if( (ufp = UFProtocol.createFrom(_monitorSocket)) == null ) {
	    monSocketError("configMonitor","Handshake Read");
	    return false;
	}

	String message = ".configMonitor> " + ufp.name();
	if( ufp instanceof UFStrings ) message += ("\t" + ((UFStrings)ufp).valData(0));
	statusCAR.showResponse( message );

	if( message.indexOf("ERR") > 0 ) {
	    System.err.println( className + message );
	    return false;
	}
	else {
	    System.out.println( className + message );
	    return true;
	}
    }
//-------------------------------------------------------------------------------

    protected void monSocketError( String method, String errmsg )
    {
	String message = method + "> ERR: " + errmsg;
	System.err.println( className + "." + message );
	statusCAR.showResponse( message );
	_monSocState.setNewState( message );
	_monSocStatus = -1;
	Toolkit.getDefaultToolkit().beep();
    }
//-------------------------------------------------------------------------------

    public void indicateConnectMonitor( boolean connStatus )
    {
	String target = "Connected to ";
	int status = 0;

	if( !connStatus ) {
	    target = "Failed connecting to ";
	    status = -1;
	}

	_monSocStatus = status;
	target += (serverName + " on port=" + _Port + " @ host = " + _Host);
	_monSocState.setNewState( target );
	target = ".connectMonitor> " + target;
	statusCAR.showResponse( target );

	if( status < 0 )
	    System.err.println( className + target );
	else
	    System.out.println( className + target );
    }
//-------------------------------------------------------------------------------

    public void setMonTimeout(int timeout) {
	try {
	    if( _monitorSocket != null ) _monitorSocket.setSoTimeout( timeout );
	    _monTimeout = timeout;
	    System.out.println( className + ".setMonTimeout> = " + _monTimeout );
	}
	catch (SocketException se) {
	    System.err.println( className + ".setMonTimeout> " + se.toString() );
	}
    }
//-------------------------------------------------------------------------------

    public void setNullSleep( int msSleep ) { _nullSleep = msSleep; }
    public void setMaxBad( int maxBad ) { _maxBad = maxBad; }

//-------------------------------------------------------------------------------
    /**
     * Method used by inner class StatusMonitor (see next below).
     */
    protected void recvStatus()
    {
	UFProtocol ufp = UFProtocol.createFrom( _monitorSocket );

	if( ufp == null )
	    {
		String errmsg = ".recvStatus> recvd null object.";
		System.err.println( className + errmsg );
		statusCAR.showResponse( errmsg );
	    }
	else if( ufp.name().length() == 0 || ufp.length() <= 0 || ufp.elements() < 0 )
	    {
		String errmsg = ".recvStatus> empty or corrupted object ? "
		    + ufp.description() + ": Length=" + ufp.length() + "\n name = " + ufp.name() + ".";
		System.err.println( className + errmsg );
		statusCAR.showResponse( errmsg );
	    }
	else {
	    procStatusInfo( ufp );
	    return;
	}

	if( ++_nullCnt >= _maxBad ) {
	    _nullCnt = 0;
	    setSocketStatus( 1 );
	    try { Thread.sleep( _nullSleep ); } catch( Exception x ) {}
	    reConnect();
	}
	else {
	    if( _nullCnt > 1 ) setSocketStatus( -1 );
	    try { Thread.sleep( _nullSleep ); } catch( Exception x ) {}
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Method used by recvStatus() to process the status info object.
     */
    protected void procStatusInfo( UFProtocol ufp )
    {
	if( ufp == null ) return;

	if( ufp instanceof UFFrameConfig )
	    {
		System.out.println(className + "::recvStatus> \n\t" + ufp.toString());
		return;
	    }
	else if( ufp instanceof UFObsConfig )
	    {
		System.out.println(className + "::recvStatus> \n\t" + ufp.toString());
		return;
	    }
	else if( ufp instanceof UFStrings )
	    {
		System.out.println(className + "::recvStatus> \n\t" + ufp.toString());
		return;
	    }
	else if( ufp instanceof UFTimeStamp )
	    {
		System.out.println(className + "::recvStatus> \n\t" + ufp.toString());
		return;
	    }
	else {
	    String errmsg = className + "::recvStatus> unexpected object:	"
		+ ufp.description() + ": Length=" + ufp.length() + "\n name = " + ufp.name() + ".";
	    System.err.println( errmsg );
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Inner class to monitor agent/server status messages in seperate thread
     */
    private class StatusMonitor extends Thread
    {
	private boolean _keepRunning = false;
	private boolean _didStart = false;
	private int _sleepAmount = 1000;
	private int _nsleeps = 0, _maxsleeps = 600, _ntrys = 0;
	// Public constructors:

	public StatusMonitor() { }

	public StatusMonitor( int sleepAmount ) {
	    _sleepAmount = sleepAmount;
	}

	// method to be run in seperate thread to recv status messages:

	public synchronized void run()
	{
	    while( true ) {
		while( _keepRunning ) {
		    if( _monitorSocket == null ) {
			if( ++_nsleeps > _maxsleeps ) {
			    _nsleeps = 0;
			    System.out.println(className+"::StatusMonitor.run> " + ++_ntrys);
			    if( connectMonitor() ) {
				_ntrys = 0;
				connect();  //attempt to reconnect the cmd socket.
			    }
			}
			else try { this.sleep( _sleepAmount ); } catch( Exception x ) {}
		    }
		    else {
			try {
			    recvStatus();
			    yield();
			}
			catch (Exception e) {
			    e.printStackTrace();
			    System.err.println(className+"::StatusMonitor.run> "+e.toString());
			    try { this.sleep( _sleepAmount ); } catch( Exception x ) {}
			}
		    }
		}
		try { this.sleep( _sleepAmount ); } catch( Exception x ) {}
	    }
	}

	public void stopRunning() { _keepRunning = false; }
	public boolean isRunning() { return _keepRunning;}
 
	public void startRunning() {
	    _keepRunning = true;
	    this.start();
	    _didStart = true;
	}

	public void resumeRunning() {
	    _keepRunning = true;
	    if( !_didStart ) {
		this.start();
		_didStart = true;
	    }		
	}
   } // end of private class StatusMonitor.
}
