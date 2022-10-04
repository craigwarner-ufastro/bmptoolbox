package javaUFLib;

//Title:        UFLibPanel
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-7
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  Extension of JPanel, used by other UF obs. tools (e.g. JCI and JDD)

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javaUFProtocol.*;

//===============================================================================
/**
 * Extension of JPanel, so that socket send & recv and generic methods/buttons can be used
 *  by UFLib components, or by other UF tools such as the JCI and JDD for CanariCam.
 * @author Frank Varosi
 */

public class UFLibPanel extends JPanel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFLibPanel.java,v 1.65 2014/03/20 07:08:22 varosi Exp $";

    protected static final int RECV_TIMEOUT      =  3000;  //milliseconds.
    protected static final int DEFAULT_TIMEOUT   = 12000;
    protected static final int CONNECT_TIMEOUT   = 13000;
    protected static final int HANDSHAKE_TIMEOUT = 14000;
    private int _socTimeout = DEFAULT_TIMEOUT;
    private int _recvTimeout = RECV_TIMEOUT;

    private UFLibPanel _tandemPanel = null; //option to use another UFLibPanel for transactions.

    protected Socket _socket = null;
    protected String _Host = "";
    protected int _Port = 0;
    protected boolean _verbose = false;
    protected int _socketStatus = -2;
    protected int _hardwareStatus = -2;
    protected String _serverHandshake = null, _connType="connect> ";
    public String serverName = "Agent";

    protected DataOutputStream _outpStream = null;
    protected DataInputStream _inpStream = null;
    private byte[] _recvBuffer = null;
    private int _bufferSize = 1024*1024;

    protected String className = this.getClass().getName();
    protected String clientName = className;
    protected String _LocalHost;

    public JPanel connectPanel = new JPanel();
    public UFButton connectButton = new UFButton("Connect", Color.yellow);
    protected UFHostPortPanel hostPortPanel = new UFHostPortPanel( _Host , _Port );

    // for Command Action & Response info:
    public UFStatusCAR statusCAR = new UFStatusCAR(className);

    // kept for upward compatibility with old code:
    public UFLabel statusAction   = new UFLabel("Action :");
    public UFLabel statusResponse = new UFLabel("Response :", new Color(0,99,0) ); //dark green.

//-------------------------------------------------------------------------------
    /**
     * UFLibPanel default constructor.
     * Calls private method, createPanel(), which does all component initialization.
     */
    public UFLibPanel() {
	try {
	    _createPanel();
	}
	catch (Exception e) { e.printStackTrace(); }
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel constructor: used for connecting to an "agent",
     *  in which case the handshake is just the client name.
     */
    public UFLibPanel( String host, int port ) {
	try {
	    setHost( host );
	    setPort( port );
	    _createPanel();
	}
	catch (Exception e) { e.printStackTrace(); }
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel constructor: used for connecting to a "server".
     *  in which case the handshake is specified for a purpose.
     */
    public UFLibPanel( String host, int port, String serverHandshake ) {
	try {
	    setHost( host );
	    setPort( port );
	    _serverHandshake = serverHandshake;
	    _createPanel();
	}
	catch (Exception e) { e.printStackTrace(); }
    }
//-------------------------------------------------------------------------------
    /**
     *Component creation
     */
    private void _createPanel() throws Exception
    {
	if( _serverHandshake == null ) {
	    connectButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e) {
			if( connectToAgent() ) getNewParams();
		    }
		});
	}
	else {
	    connectButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e) { connectToServer(); }
		});
	}

	connectPanel.setLayout(new GridLayout(2,1));
	connectPanel.add( connectButton );  
	connectPanel.add( hostPortPanel );
	connectButton.setToolTipText("Press Button to Re-Connect");
	this.setBackground( UFColor.default_Color );
    }
//------------------------------------------------------------------------

    public void setTandemPanel( UFLibPanel tandemPanel ) {
	_tandemPanel = tandemPanel;
	if( tandemPanel != null ) _socket = tandemPanel._socket;
    }

    public void verbose( boolean verbose ) { _verbose = verbose; }
    public boolean verbosity() { return _verbose; }

    public int bufferSize() { return _bufferSize; }

    public void bufferSize(int bufSize) {
	if( _recvBuffer.length != bufSize ) _recvBuffer = new byte[bufSize];
	_bufferSize = bufSize;
    }
//----------------------------------------------------------------------------------------

    public String dateTime() {
	String date = new Date( System.currentTimeMillis() ).toString();
	return( date.substring(4,19) + " LT.");
    }
//------------------------------------------------------------------------
// generic methods to be overriden.

    public void getNewParams() {}
    public void getNewParams( String callerName ) {}
    public void setNewStatus( UFStrings newStatus ) {}
    public void updateFrames( UFFrameConfig frameConfig ) {}
    public void updateImage( UFImageConfig imageConfig ) {}
    public void updateObsInfo( UFStrings obsInfo ) {}
    public void updateObsConfig( UFObsConfig obsConfig ) {}

//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#Connect button action performed
     */
    public boolean connectToAgent()  { return connect("connectToAgent> ", clientName); }
    public boolean connectToServer() { return connect("connectToServer> ", _serverHandshake); }

    public boolean connect() { return connect(_connType, _serverHandshake); }
    public boolean close()   { return close(_connType); }

    public Socket socket() { return _socket; }

    public boolean reConnect()
    {
	connectButton.doClick(900);

	if( _socket == null )
	    return false;
	else
	    return true;
    }

//-------------------------------------------------------------------------------
    /**
     *  Connect via socket to the Agent or Server.
     */
    protected boolean connect(String cType, String handshake)
    {
	close( cType, true );

	try {
	    _Host = getHostField();
	    _Port = getPortField();
	    String message = cType + "port=" + _Port + " @ host = " + _Host;
	    statusCAR.showAction( message );
	    System.out.println( className + "." + message );
	    setSocketStatus(1);
	    InetSocketAddress agentIPsoca = new InetSocketAddress( _Host, _Port );
	    _socket = new Socket();
	    _socket.connect( agentIPsoca, CONNECT_TIMEOUT );

	    if( handshake != null ) {
		//System.out.println( className + "." + cType + "Handshake timeout = " +  HANDSHAKE_TIMEOUT);
	        _socket.setSoTimeout( HANDSHAKE_TIMEOUT );
		//send some kind of handshake (usually client name or simple request):
		UFTimeStamp uft = new UFTimeStamp(handshake);
		statusCAR.showAction( cType + "Sending handshake...");

		if( uft.sendTo(_socket) <= 0 ) {
		    connectError(cType,"Handshake Send"); //also closes _socket.
		    return false;
		}

		//get response from agent
		UFProtocol ufp = null;

		if( (ufp = UFProtocol.createFrom(_socket)) == null ) {
		    connectError(cType,"Handshake Read"); //also closes _socket.
		    return false;
		}

		message = cType + ufp.name();
		if( ufp instanceof UFStrings ) message += ("\t" + ((UFStrings)ufp).valData(0));
		System.out.println( className + "." + message );
		statusCAR.showResponse( message );
	    }

	    InetAddress LocalInet = _socket.getLocalAddress();
	    _LocalHost = LocalInet.getHostName();
	    //set normal timeout for socket
	    _socket.setSoTimeout( _socTimeout );
	    message = cType + "Connected to " + serverName + " on port=" + _Port + " @ host = " + _Host;
	    System.out.println( className + "." + message );
	    statusCAR.showResponse( message );
	    _inpStream = new DataInputStream(_socket.getInputStream());
	    _outpStream = new DataOutputStream(_socket.getOutputStream());
	    setSocketStatus(0);
	    return true;
	}
	catch( Exception x ) {
	    String message = cType + x.toString();
	    System.err.println(className + "." + message);
	    statusCAR.showResponse("ERR: " + message);
	    Toolkit.getDefaultToolkit().beep();
	    close( cType );
	    return false;
	}
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#close
     * NOTE: boolean showCAR default is false because if close is called after an error,
     * the error message in statusCAR.showResponse(error) should not be overwritten.
     */
    protected boolean close(String cType) { return close( cType, false ); }

    protected boolean close(String cType, boolean showCAR)
    {
	if( _socket == null ) return true;
	boolean status;

	try {
	    String message = cType + " closing socket...";
	    System.out.println( className + "." +  message );
	    if( showCAR ) statusCAR.showAction( message );
	    _socket.close();
	    _socket = null;
	    setSocketStatus(-1);
	    if( showCAR ) statusCAR.showResponse( cType + "socket closed.");
	    status = true;
	}
	catch (IOException ioe) {
	    String message = cType + ioe.toString();
	    System.err.println( className + "." + message );
	    _socket = null;
	    setSocketStatus(-1);
	    statusCAR.showResponse( message );
	    status = false;
	}

	try { Thread.sleep(700);} catch( Exception _e ) {}
	return status;
    }
//-------------------------------------------------------------------------------

    protected void connectError( String cType, String errmsg )
    {
	String message = cType + errmsg + " ERROR";
	System.err.println( className + "." + message );
	statusCAR.showResponse( message );
	Toolkit.getDefaultToolkit().beep();
	close( cType );
    }
//-------------------------------------------------------------------------------

    protected void setSocketStatus( int status )
    {
	if( _socketStatus == status ) return; //do it only if changed.
	_socketStatus = status;

	if( _socketStatus > 0 ) {
	    connectButton.setBackground( Color.yellow ); //means attempting connection/communication
	    connectButton.setText("Connecting to " + serverName);
	}
	else if( _socketStatus < 0 ) {
	    connectButton.setBackground( Color.red );    //means connection/comm failed.
	    connectButton.setText("Re-Connect to " + serverName);
	}
	else {
	    connectButton.setBackground( Color.green );  //zero means connection/comm ok.
	    connectButton.setText("Connected to " + serverName);
	}
    }
//-------------------------------------------------------------------------------

    public void setHostAndPort( String host, int port ) {
	setHost( host );
	setPort( port );
    }

    public void setHost(String host) {
	_Host = host;
	hostPortPanel.setHost( host );
    }

    public void setPort(int port) {
	_Port = port;
	hostPortPanel.setPort( port );
    }

    public void setName(String name) { clientName = name; }

    public String getHost() { return _Host; }
    public int getPort() { return _Port; }

    public String getHostField() {
	_Host = hostPortPanel.getHostField();
	return _Host;
    }

    public int getPortField() {
	_Port = hostPortPanel.getPortField();
	return _Port;
    }

    public void setSocTimeout() { setSocTimeout(DEFAULT_TIMEOUT); }

    public void setSocTimeout(int timeout) {
	try {
	    _socket.setSoTimeout( timeout );
	    _socTimeout = timeout;
	    System.out.println( className + ".setSocTimeout> = " + _socTimeout );
	}
	catch (SocketException se) {
	    System.err.println( className + ".setSocTimeout> " + se.toString() );
	}
    }

    public void setRecvTimeout(int timeout) { _recvTimeout = timeout; }

//-------------------------------------------------------------------------------
    //send bytes to socket:

    public int send( String data ) { return send( data.getBytes() ); }

    public int send( byte[] data ) {
	try {
	    if( data != null && data.length > 0 ) {
		_outpStream.write( data );
		return data.length;
	    }
	}
	catch( EOFException eof ) {
	    System.err.println( className + ".send> " + eof.toString() );
	} 
	catch( IOException ioe ) {
	    System.err.println( className + ".send> " + ioe.toString() );
	}
	catch( Exception ex ) {
	    System.err.println( className + ".send> " + ex.toString() );
	}
	return(-1);
    }
//-------------------------------------------------------------------------------
    //read raw bytes from socket into a buffer and then transfer the read bytes to returned array.

    public byte[] recv()
    {
	if( _recvBuffer == null ) _recvBuffer = new byte[_bufferSize];
	else if( _recvBuffer.length != _bufferSize ) _recvBuffer = new byte[_bufferSize];
	try {
	    int nrb = 0;
	    int nav = _inpStream.available();
	    if( nav <= 0 ) {
		int ntry = -1, maxtry=3;
		while( nav <= 0 && ++ntry < maxtry ) {
		    try { Thread.sleep(_recvTimeout); } catch( Exception e ) { e.printStackTrace(); }
		    nav = _inpStream.available();
		}
		if( nav <= 0 ) {
		    System.err.println("*\n" + className + ".recv> nothing available after "
				       + (maxtry*_recvTimeout/1000.) + " sec.");
		    return(new byte[0]);
		}
	    }
	    while( nav > 0 ) {
		_inpStream.readFully( _recvBuffer, nrb, nav );
		nrb += nav;
		try { Thread.sleep(_recvTimeout); } catch( Exception _e ) {}
		nav = _inpStream.available();
	    }
	    byte[] data = new byte[nrb];
	    System.arraycopy( _recvBuffer, 0, data, 0, nrb );
	    return data;
	}
	catch( EOFException eof ) {
	    System.err.println( className + ".recv> " + eof.toString() );
	} 
	catch( IOException ioe ) {
	    System.err.println( className + ".recv> " + ioe.toString() );
	}
	catch( Exception ex ) {
	    System.err.println( className + ".recv> " + ex.toString() );
	}
	return(null);
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#sendRecvAgent
     *@param agentRequest : UFStrings object containing request to send to agent.
     *@param caller :       String = name of calling method (for informational purposes only).
     *@retval = UFStrings reply from agent.
     */
    public UFStrings sendRecvAgent( UFStrings agentRequest, String caller )
    {
	if( sendAgent( agentRequest, caller ) <= 0 ) return null;
	return recvAgent( caller );
    }

    public UFProtocol sendRecvServer( UFStrings agentRequest, String caller ) {
	if( sendServer( agentRequest, caller ) <= 0 ) return null;
	return recvServer( caller );
    }

    public UFProtocol sendRecvServer( UFTimeStamp agentRequest, String caller )
    {
	if( sendServer( agentRequest, caller ) <= 0 ) return null;
	return recvServer( caller );
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#sendAgent
     *@param agentRequest : UFStrings: object containing request to send to agent.
     *@param caller :       String:    name of calling method (for informational purposes only).
     *@retval = int: number of bytes sent.
     */
    public int sendAgent( UFStrings agentRequest, String caller )
    {
	if( _socket == null ) {
	    if( ! reConnect() )	return(-1);
	}

	agentRequest.rename( _LocalHost + ":" + agentRequest.name() );
	setSocketStatus(1);
	int nbsent = agentRequest.sendTo( _socket );

	if( nbsent <= 0 ) {
	    setSocketStatus(-1);
	    System.err.println( caller + "ERROR sending to agent");
	}

	return nbsent;
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#sendServer
     *@param servRequest : UFTimeStamp: object containing request to send to server.
     *@param caller :       String:    name of calling method (for informational purposes only).
     *@retval = int: number of bytes sent.
     */
    public int sendServer( UFProtocol servRequest, String caller )
    {
	if( _socket == null ) {
	    if( ! reConnect() )	return(-1);
	}

	statusCAR.showAction( servRequest.name() );
	setSocketStatus(1);
	int nbsent = servRequest.sendTo( _socket );

	if( nbsent <= 0 ) {
	    setSocketStatus(-1);
	    System.err.println( caller + "ERROR sending to server.");
	}

	return nbsent;
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#recvAgent
     *@param caller :       String = name of calling method (for informational purposes only).
     *@retval = UFStrings reply from agent.
     */
    public UFStrings recvAgent( String caller )
    {
	String imethod = className + ".recvAgent> " + caller + "> ";
	//recv response from agent:
	UFStrings agentReply = (UFStrings)UFProtocol.createFrom( _socket );

	if( agentReply == null ) {

	    //nothing recvd but try again...
	    agentReply = (UFStrings)UFProtocol.createFrom( _socket );

	    if( agentReply == null ) {
		setSocketStatus(-1);
		System.err.println( imethod + "Transaction ERROR: nothing recvd, giving up.");
	    }
	    else {
		setSocketStatus(0);
		//send OK msg to errlog because read timeout already posted error msg:
		System.err.println("OK: " + imethod + "finally got reply.");
	    }
	}
	else setSocketStatus(0);

	if( agentReply != null ) {
	    if( agentReply.elements() < 0 ) {
		String errmsg = "empty or corrupted object ? " + agentReply.description()
		    + ": Length=" + agentReply.length() + "\n name = " + agentReply.name() + ".";
		System.err.println( imethod + errmsg );
		statusCAR.showResponse( errmsg );
		return null;
	    }
	    else if( _verbose ) System.out.print( agentReply.toString() );
	}

	if( _socTimeout != DEFAULT_TIMEOUT ) setSocTimeout( DEFAULT_TIMEOUT );
	return agentReply;
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#recvServer
     *@param caller :       String = name of calling method (for informational purposes only).
     *@retval = UFProtocol reply from server.
     */
    public UFProtocol recvServer( String caller )
    {
	String imethod = className + ".recvServer> " + caller + "> ";
	//recv response from server:
	UFProtocol servReply = UFProtocol.createFrom( _socket );

	if( servReply == null ) {

	    //nothing recvd but try again...
	    servReply = UFProtocol.createFrom( _socket );

	    if( servReply == null ) {
		setSocketStatus(-1);
		System.err.println( imethod + "Transaction ERROR: nothing recvd, giving up.");
	    }
	    else {
		setSocketStatus(0);
		//send OK msg to errlog because read timeout already posted error msg:
		System.err.println("OK: " + imethod + "finally got reply.");
	    }
	}
	else setSocketStatus(0);

	if( servReply != null ) {
	    if( servReply.elements() < 0 ) {
		String errmsg = "empty or corrupted object ? " + servReply.description()
		    + ": Length=" + servReply.length() + "\n name = " + servReply.name() + ".";
		System.err.println( imethod + errmsg );
		statusCAR.showResponse( errmsg );
		return null;
	    }
	    else if( servReply instanceof UFStrings )
		statusCAR.showResponse( servReply.name() + " : " + ((UFStrings)servReply).valData(0));
	    else
		statusCAR.showResponse( servReply.name() );
	    if( _verbose ) System.out.print( servReply.toString() );
	}

	if( _socTimeout != DEFAULT_TIMEOUT ) setSocTimeout( DEFAULT_TIMEOUT );
	return servReply;
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#commandAgent
     *@param agentRequest : UFStrings object containing request to send to agent/server.
     */
    public void commandAgent( UFStrings agentRequest ) {
	commandAgent( agentRequest, clientName );
    }

    public void commandServer( UFStrings servRequest ) {
	commandAgent( servRequest, clientName );
    }

    public void commandServer( UFStrings servRequest, String callerName ) {
	commandAgent( servRequest, callerName );
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#commandAgent
     *@param agentRequest : UFStrings object containing request to send to agent/server.
     *@param callerName   : String = name of client invoking this method.
     */
    public void commandAgent( UFStrings agentRequest, String callerName )
    {
	final String panel = callerName + ":";
	final String action = agentRequest.name();
	agentRequest.rename( panel + action );
	final UFStrings AgentRequest = agentRequest;
	String msg = "sending " + action + " directive to " + serverName;
	System.out.println( panel + msg );
	statusCAR.showAction( msg );
	buttonsEnabled( false );

	if( _tandemPanel != null ) {
	    _tandemPanel.statusCAR.showAction( msg );
	    _socket = _tandemPanel._socket;
	}

	//create new task an put on the event thread queue
	// so it will be invoked after current action completes (so statusCAR will be repainted):

	Runnable cmd_agent_task = new Runnable() {
		public void run() {
		    try {
			String[] words = action.split(" ");
			String task = words[0] + " >  ";
			UFStrings agentReply = sendRecvAgent( AgentRequest, panel+task );

			if( agentReply == null ) {
			    String statmsg = "transaction ERR: recvd NO reply.";
			    statusCAR.showResponse( task + statmsg );
			    if( _tandemPanel != null ) _tandemPanel.statusCAR.showResponse( task + statmsg );
			}
			else {
			    if( _tandemPanel != null ) {
				String statmsg = _tandemPanel.decodeResponse( agentReply );
				if( statmsg == null ) statmsg = "ERR: failed decoding response ?";
				_tandemPanel.statusCAR.showResponse( task + statmsg );
				System.out.println(_tandemPanel.className + task + statmsg);
			    }

			    String statmsg = decodeResponse( agentReply );

			    if( statmsg == null ) statmsg = "WARN: failed decoding response ?";
			    statusCAR.showResponse( task + statmsg );
			    System.out.println( panel + task + statmsg );
			}
		    }
		    catch( Exception x ) { x.printStackTrace(); }

		    buttonsEnabled( true );
		}
	    };

	SwingUtilities.invokeLater( cmd_agent_task );
    }
//------------------------------------------------------------------------------------------
    /**
     * UFLibPanel#commandAgent
     *@param agentRequest : UFStrings object array containing multiple requests to send to agent.
     */
    public void commandAgent( UFStrings[] agentRequest ) {
	commandAgent( agentRequest, clientName, true );
    }
    public void commandAgent( UFStrings[] agentRequest, String callerName ) {
	commandAgent( agentRequest, callerName, true );
    }
    public void commandAgent( UFStrings[] agentRequest, boolean showCAR  ) {
	commandAgent( agentRequest, clientName, showCAR );
    }
//------------------------------------------------------------------------------------------
    //single UFStrings object variants:

    public void commandAgent( UFStrings agentRequest, boolean showCAR  ) {
	commandAgent( agentRequest, clientName, showCAR );
    }

    public void commandAgent( UFStrings agentRequest, String callerName, boolean showCAR )
    {
	UFStrings[] agentReqs = new UFStrings[1];
	agentReqs[0] = agentRequest;
	commandAgent( agentReqs, callerName, showCAR );
    }
//------------------------------------------------------------------------------------------
    /**
     * UFLibPanel#commandAgent (send vector of UFStrings objects, with option to use _tandemPanel)
     *@param agentRequest : UFStrings object containing request to send to agent/server.
     *@param callerName   : String = name of client invoking this method.
     *@param showCAR      : boolean = flag to show transaction in statusCAR object.
     */
    public void commandAgent( UFStrings[] agentRequest, String callerName, final boolean showCAR )
    {
	buttonsEnabled(false);
	final String panel = callerName + ":";
	final UFStrings agentReqs[] = agentRequest;
	String action = agentRequest[0].name();
	String msg = "sending " + action + " directive to " + serverName;
	System.out.println( panel + msg );
	statusCAR.showAction( msg, showCAR );
	buttonsEnabled( false );

	if( _tandemPanel != null ) {
	    _tandemPanel.statusCAR.showAction( msg );
	    _socket = _tandemPanel._socket;
	}
	//create new task and put on the event thread queue
	// so it will be invoked after current action completes (so above showAction etc. will be repainted):

	Runnable transAction = new Runnable() {
		public void run() {
		    try {
			for( int i=0; i < agentReqs.length; i++ ) {
			    String action = agentReqs[i].name();
			    agentReqs[i].rename( panel + action );

			    if( i > 0 ) { //info for i==0 already done above.
				String msg = "sending " + action + " directive to " + serverName;
				System.out.println( panel + msg );
				statusCAR.showAction( msg, showCAR );
				if( _tandemPanel != null ) _tandemPanel.statusCAR.showAction( msg, showCAR );
			    }

			    String[] words = action.split(" ");
			    String task = words[0] + " >  ";
			    if( task.indexOf("CONFIGURE") == 0 ) setSocTimeout(17000);
			    UFStrings agentReply = sendRecvAgent( agentReqs[i], panel+task );

			    if( agentReply == null ) {
				String statmsg = "transaction ERR: recvd NO reply.";
				statusCAR.showResponse( task + statmsg );
				if( _tandemPanel != null ) _tandemPanel.statusCAR.showResponse( task + statmsg );
				if( i == agentReqs.length-1 ) actionReConnect();
				break;
			    }
			    else {
				if( _tandemPanel != null ) {
				    String statmsg = _tandemPanel.decodeResponse( agentReply );
				    if( statmsg == null ) statmsg = "ERR: failed decoding response ?";
				    _tandemPanel.statusCAR.showResponse( task + statmsg, showCAR );
				    System.out.println(_tandemPanel.className + task + statmsg);
				}

				String statmsg = decodeResponse( agentReply );

				if( statmsg == null ) statmsg = "ERR: failed decoding response ?";
				statusCAR.showResponse( task + statmsg, showCAR );
				System.out.println( panel + task + statmsg );

				if( statmsg.indexOf("ERR") >=0 ||
				    statmsg.indexOf("WARN") >=0 ||
				    statmsg.indexOf("ALARM") >=0 ) {
				    indicateCmdError( statmsg );
				    break;
				}
			    }
			}
		    }
		    catch( Exception x ) { x.printStackTrace(); }

		    buttonsEnabled(true);
		}
	    };

	SwingUtilities.invokeLater( transAction );
    }
//-------------------------------------------------------------------------------------
    //override these methods:
    // to disable/enable GUI components during send-recv:

    protected void buttonsEnabled(boolean enable) { }

    // to re-connect socket:
    protected void actionReConnect() { reConnect(); }

    // to indicate an error, warning, or alarm:
    protected void indicateCmdError( String errmsg ) { }

//-------------------------------------------------------------------------------------
    /**
     * UFLibPanel#decodeResponse (normally overridden by child classes).
     * Parses response...
     *@param agentReply : UFStrings object containing reply from agent.
     *@retval = String, either OK or ERR.
     */
    public String decodeResponse( UFStrings agentReply )
    {
	if( agentReply == null ) return("WARNING: agent did not respond ?");

	for( int i=0; i < agentReply.numVals(); i++ )
	    {
		String paramVal = agentReply.valData(i);
		int eqpos = 0;

		if( (eqpos = paramVal.indexOf("==")) > 0 )
		    {
			String param = paramVal.substring(0,eqpos).trim();
			String pVal = paramVal.substring( eqpos+2, paramVal.length() ).trim();
			if( _verbose ) System.out.println( i + ":" + param + "=" + pVal );
		    }
	    }

	String replyStatus = agentReply.name().toUpperCase();

	if( replyStatus.indexOf("ERR") >= 0 ||
	    replyStatus.indexOf("WARN") >= 0 ||
	    replyStatus.indexOf("ALARM") >= 0 )
	    {
		if( agentReply.numVals() > 0 )
		    return agentReply.valData(0);
		else
		    return("ERR: invalid agent response ? " + agentReply.name() );
	    }
	else {
	    if( replyStatus.indexOf("OK") >= 0 )
		return("OK:  " + agentReply.valData(0));
	    else
		return("ERR: invalid agent response ?");
	}
    }
//-------------------------------------------------------------------------------
//utility functions:

    public int checkIntegerInput( UFTextPanel enterParam ) {

 	int number = -1;
	String entry = enterParam.getDesiredField().trim();

	if( entry.length() > 0 ) {
	    try {
		number = Integer.parseInt( entry );
	    }
	    catch (NumberFormatException nfe) {
		enterParam.setDesiredBackground( Color.yellow );
		enterParam.setActive("Invalid Entry");
		Toolkit.getDefaultToolkit().beep();
	    }
	}

	return number;
    }
//-------------------------------------------------------------------------------

    public float checkFloatInput( UFTextPanel enterParam ) {

 	float number = -1;
	String entry = enterParam.getDesiredField().trim();

	if( entry.length() > 0 ) {
	    try {
		number = Float.parseFloat( entry );
	    }
	    catch (NumberFormatException nfe) {
		enterParam.setDesiredBackground( Color.yellow );
		enterParam.setActive("Invalid Entry");
		Toolkit.getDefaultToolkit().beep();
	    }
	}

	return number;
    }
//-------------------------------------------------------------------------------

    public int checkIntegerInput( UFTextField enterParam ) {

 	int number = -1;
	String entry = enterParam.getText().trim();

	if( entry.length() > 0 ) {
	    try {
		number = Integer.parseInt( entry );
	    }
	    catch (NumberFormatException nfe) {
		enterParam.setBackground( Color.yellow );
		Toolkit.getDefaultToolkit().beep();
	    }
	}

	return number;
    }
//-------------------------------------------------------------------------------

    public float checkFloatInput( UFTextField enterParam ) {

 	float number = -1;
	String entry = enterParam.getText().trim();

	if( entry.length() > 0 ) {
	    try {
		number = Float.parseFloat( entry );
	    }
	    catch (NumberFormatException nfe) {
		enterParam.setBackground( Color.yellow );
		Toolkit.getDefaultToolkit().beep();
	    }
	}

	return number;
    }
}
