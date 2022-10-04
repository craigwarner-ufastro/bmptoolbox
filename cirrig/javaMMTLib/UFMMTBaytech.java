package javaMMTLib;
/**
 * Title:        UFMMTBaytech.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner, Frank Varosi
 * Company:      University of Florida
 * Description:  Controls baytech RPC-3; Uses telnet port 23 communications,
 *               has 8 powered outlets.
 */

import javaUFLib.*;
import javaUFProtocol.*;
import java.util.*;
import java.net.*;
import java.io.*;

//=======================================================================================
/**
 * Extension of ULibPanel to communicate with BayTech Remote Power Control (RPC) device.
 * Used by CanariCam Interface Server (CIS) (see CancamIfcServer.java).
 * BayTech RPC supports telnet (port=23) communications, and has 8 controlled power outlets.
 * @author Frank Varosi
 */

public class UFMMTBaytech { 

    public static final
	String rcsID = "$Name:  $ $Id: UFMMTBaytech.java,v 1.3 2015/06/22 19:36:45 circe Exp $";

    protected String _mainClass = getClass().getName();
    private String _time = dateTime();
    private float _temperature = 0;
    private String _breaker = "?", _breaker2 = "?";
    private float _Watts = 0, _Watts2 = 0;
    private float _Volts = 0, _Volts2 = 0;
    private float _Amps  = 0, _Amps2  = 0;
    private float _maxAmps = 0, _maxAmps2  = 0;
    private boolean _error = false;
    private String _errmsg = "";
    private boolean _baytechOld = false;
    private boolean _baytechNew = false;
    private boolean _isCelsius = true;
    private String _user = "mmtpol", _pass=".uf.mmt;";

    //from UFLibPanel
    protected Socket _socket = null;
    protected String _Host = "";
    protected int _Port = 0;
    protected boolean _verbose = false;
    protected int _socketStatus = -2;
    protected int _hardwareStatus = -2;
    protected String _serverHandshake = null, _connType="connect> ";
    public String serverName = "Agent";

    protected static final int REPLY_TIMEOUT     =  5000;  //milliseconds.
    protected static final int RECV_TIMEOUT      =  1000;  //milliseconds.
    protected static final int DEFAULT_TIMEOUT   = 12000;
    protected static final int CONNECT_TIMEOUT   = 13000;
    protected static final int HANDSHAKE_TIMEOUT = 14000;
    private int _socTimeout = DEFAULT_TIMEOUT;
    private int _recvTimeout = RECV_TIMEOUT; //within a receive
    private int _replyTimeout = REPLY_TIMEOUT; //waiting for reply

    protected DataOutputStream _outpStream = null;
    protected DataInputStream _inpStream = null;
    private byte[] _recvBuffer = null;
    private int _bufferSize = 1024*16; //why was buffer so big?
    
    protected UFBaytechOutlet[] _powerOutLets;

    public UFMMTBaytech() {
        this("ufbaytech");
    }

    public UFMMTBaytech(String hostname) {
        setHost(hostname);
	setPort(23);
	_init( 8 );
    }

    public UFMMTBaytech( String hostname, int numOutlets ) {
        setHost(hostname);
        setPort(23);
	_init( numOutlets );
    }

    private void _init( int numOutlets )
    {
	_powerOutLets = new UFBaytechOutlet[numOutlets];
	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) _powerOutLets[ipo] = new UFBaytechOutlet();
    }

    public void setUserAndPass(String user, String pass) {
        _user = user;
        _pass = pass;
    }

    public boolean initialize()
    {
	if( connect() ) {
	    
	    //upon connection the BayTech immediately gives status info so recv it:
	    byte[] bstat = recv();
	    if( bstat.length > 0 ) {
		String reply = new String(bstat);
		System.out.println( _mainClass + ".initialize> " + reply );
		if( reply.indexOf("RPC in use") < 0 ) {
		    _error = false;

		    if(reply.indexOf("Outlet Control") > 0) {
			_baytechOld = true;
			reply = _sendRecv("1\r", "outletControl");
			System.out.println( _mainClass + ".initialize> " + reply );
		    }
		    else if(reply.indexOf("login") > 0) {
			_baytechNew = true;
			reply = _sendRecv(_user+"\r", "login");
			if(reply.indexOf("Password") >= 0) {
			    reply = _sendRecv(_pass+"\r", "pword");
			    System.out.println( _mainClass + ".initialize> \n" + reply );
			}
			else {
			    _error = true;
			    _errmsg = "ERR: Failed to login to Baytech device";
			    System.err.println("Failed to connect to BayTech device");
			    close();
			    return false;
			}
			if (reply.indexOf("MRP-01>") >= 0) return true;
			if (reply.indexOf("user name") >=0) {
			    reply = _sendRecv(_user+"\r", "user");
			} else {
                            _error = true;
                            _errmsg = "ERR: Failed to login to Baytech device";
                            System.err.println("Failed to connect to BayTech device");
                            close();
                            return false;
			}
                        if (reply.indexOf("Password") >=0) {
                            reply = _sendRecv(_pass+"\r", "pass");
                            System.out.println(_mainClass+"::initialize> \n"+reply);
                        } else {
                            _error = true;
                            _errmsg = "ERR: Failed to login to Baytech device";
                            System.err.println("Failed to connect to BayTech device");
                            close();
                            return false;
                        }
		    }
		    return true;
		}
		else {
		    _errmsg = "ERR: RPC is in use by another client: closing connection....";
		    _error = true;
		    System.err.println( _mainClass + ".initialize> " + _errmsg );
		    close();
		    return false;
		}
	    }
	    else {
		_errmsg = "ERR: Failed recving status reply.";
		_error = true;
		System.err.println( _mainClass + ".initialize> " + _errmsg );
		close();
		return false;
	    }
	}
	else {
	    _errmsg = "ERR: failed connecting to BayTech RPC.";
	    _error = true;
	    close();
	    return false;
	}
    }
//---------------------------------------------------------------------------------------------

    public String[] checkStatus()
    {
	String stat = getStatus();
	parseStatus( stat );
	return statusInfo();
    }

    public String getStatus()
    {
	String status = _sendRecv("status\r","getStatus");
	//if an error occurred try just one more time (automatically reconnects):
	if( _error )
	    return _sendRecv("status\r","getStatus");
	else
	    return status;
    }

    public void parseStatus( String btStatus )
    {
	if( btStatus.indexOf("ERR") >= 0 ) {
	    _error = true;
	    _errmsg = btStatus;
	    return;
	}

	_time = dateTime();
	_error = false;
	_errmsg = "";
	String[] statLines = btStatus.split("\n");

	if( statLines.length < 4 ) {
	    _error = true;
	    _errmsg = "ERR: less than 4 items in status response from BayTech RPC.";
	}

	//parse the status reply from BayTech RPC: (int ipo counts the power outlets in reply)
	int ipo = 0;

	for( int i=0; i < statLines.length; i++ ) {

	    if( ipo >= _powerOutLets.length ) break;
	    String statrec = statLines[i];

	    if( _powerOutLets[ipo].parse( statrec ) ) ++ipo;
	    else {
		int cpos = statrec.indexOf(":");
		if( cpos > 0 ) {
		    int cpos2 = statrec.lastIndexOf(":");
		    ++cpos; ++cpos2;
		    try {
			if( statrec.indexOf("Power") > 0 ) {
			    int end = statrec.indexOf("Watts");
			    if( end > cpos ) _Watts = Float.parseFloat( statrec.substring(cpos,end).trim() );
			    if( cpos2 > cpos ) {
				end = statrec.lastIndexOf("Watts");
				if( end > cpos2 )
				    _Watts2 = Float.parseFloat( statrec.substring(cpos2,end).trim() );
			    }
			}
			else if( statrec.indexOf("Voltage") > 0 ) {
			    int end = statrec.indexOf("Volts");
			    if( end > cpos ) _Volts = Float.parseFloat( statrec.substring(cpos,end).trim() );
			    if( cpos2 > cpos ) {
				end = statrec.lastIndexOf("Volts");
				if( end > cpos2 )
				    _Volts2 = Float.parseFloat( statrec.substring(cpos2,end).trim() );
			    }
			}
			else if( statrec.indexOf("Current") > 0 ) {
			    int end = statrec.indexOf("Amps");
			    if( end > cpos ) _Amps = Float.parseFloat( statrec.substring(cpos,end).trim() );
			    if( cpos2 > cpos ) {
				end = statrec.lastIndexOf("Amps");
				if( end > cpos2 )
				    _Amps2 = Float.parseFloat( statrec.substring(cpos2,end).trim() );
			    }
			}
			else if( statrec.indexOf("Maximum") >= 0 ) {
			    int end = statrec.indexOf("Amps");
			    if( end > cpos )
				_maxAmps = Float.parseFloat( statrec.substring(cpos,end).trim() );
			    if( cpos2 > cpos ) {
				end = statrec.lastIndexOf("Amps");
				if( end > cpos2 )
				    _maxAmps2 = Float.parseFloat( statrec.substring(cpos2,end).trim() );
			    }
			}
			else if( statrec.indexOf("Breaker") > 0 ) {
			    String[] words = statrec.split(":");
			    if( words.length > 1 ) _breaker = words[1].trim();
			    if( words.length > 2 ) _breaker2 = words[2].trim();
			}
			else if( statrec.indexOf("Temp") > 0 ) {
			    int end = statrec.lastIndexOf("C");
			    if( end > cpos )
				_temperature = Float.parseFloat( statrec.substring(cpos,end).trim() );
			}
		    }
		    catch( NumberFormatException nfe ) {
			System.err.println( _mainClass + ".parse> " + nfe.toString() );
		    }
		}
	    }
	}
    }

    public void parseStatusNew( String btStatus ) {
	if( btStatus.indexOf("ERR") >= 0 ) {
	    _error = true;
	    _errmsg = btStatus;
	    return;
	}

	_time = dateTime();
	_error = false;
	_isCelsius = false;
	_errmsg = "";
	String[] statLines = btStatus.split("\n");

	if( statLines.length < 4 ) {
	    _error = true;
	    _errmsg = "ERR: less than 4 items in status response from BayTech RPC.";
	}

	//parse the status reply from BayTech RPC: (int ipo counts the power outlets in reply)
	int ipo = 0;

	for( int i=0; i < statLines.length; i++ ) {

	    if( ipo >= _powerOutLets.length ) break;
	    String statrec = statLines[i];

	    if( _powerOutLets[ipo].parse( statrec ) ) ++ipo;
	    else {
		try {
		    if(statrec.indexOf("Outlet") > 0 && statrec.indexOf("Amps") > 0 && statrec.indexOf("Volts") > 0) {
			String[] st = statrec.split("\\u007c"); // split on the | character
			_Amps = Float.parseFloat(st[2].substring(0, st[2].indexOf("Amps")).trim());
			_maxAmps = Float.parseFloat(st[3].substring(0, st[3].indexOf("Amps")).trim());
			_Volts = Float.parseFloat(st[4].substring(0, st[4].indexOf("Volts")).trim());
			_Watts = Float.parseFloat(st[5].substring(0, st[5].indexOf("Watts")).trim());
		    }
		    else if(statrec.indexOf("Temperature") > 0) {
			int cpos = statrec.indexOf(":");
			int epos = statrec.indexOf("F");
			if( epos < 0 ) epos = statrec.indexOf("C");
			if( epos > cpos )
			    _temperature = Float.parseFloat(statrec.substring(cpos + 1, epos).trim());
		    }
		}
		catch( NumberFormatException nfe ) {
		    System.err.println( _mainClass + ".parseNew> " + nfe.toString() );
		}
	    }
	}
    }

    public String[] statusInfo()
    {
	if( _error ) {
	    String[] statinfo = new String[_powerOutLets.length + 8];
	    statinfo[0] = "Power> Local Time = " + _time + " (Failure)";
	    statinfo[1] = "Power> Watts = ? " + _Watts + "\t  |-|  \t"  + _Watts2;
	    statinfo[2] = "Power> Volts = ? " + _Volts + "\t  |-|  \t"  + _Volts2;
	    statinfo[3] = "Power> Amps  = ? " + _Amps  + "\t  |-|  \t"  + _Amps2;
	    statinfo[4] = "Power> Max.Curr. = ? " + _maxAmps  + "\t  |-|  \t"  + _maxAmps2;
	    statinfo[5] = "Power> Breaker = ? " + _breaker  + "\t  |-|  \t"  + _breaker2;
	    statinfo[6] = "Power> Temp = ? " + _temperature + ((_isCelsius)? " C" : " F");
	    for( int ipo=0; ipo < _powerOutLets.length; ipo++ )
	                                       statinfo[ipo+7] = _powerOutLets[ipo].toString() + " ?";
	    statinfo[_powerOutLets.length + 7] = "Power> Error = " + _errmsg;
	    return statinfo;
	}
	else {
	    String[] statinfo = new String[_powerOutLets.length + 7];
	    statinfo[0] = "Power> Local Time = " + _time;
	    statinfo[1] = "Power> Watts = " + _Watts + "\t  |-|  \t"  + _Watts2;
	    statinfo[2] = "Power> Volts = " + _Volts + "\t  |-|  \t"  + _Volts2;
	    statinfo[3] = "Power> Amps  = " + _Amps  + "\t  |-|  \t"  + _Amps2;
	    statinfo[4] = "Power> Max.Curr. = " + _maxAmps  + "\t  |-|  \t"  + _maxAmps2;
	    statinfo[5] = "Power> Breaker = " + _breaker  + "\t  |-|  \t"  + _breaker2;
	    statinfo[6] = "Power> Temp  = " + _temperature + ((_isCelsius)? " C" : " F");
	    for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) statinfo[ipo+7] = _powerOutLets[ipo].toString();
	    return statinfo;
	}
    }
//---------------------------------------------------------------------------------------------

    public boolean powerON( String device )
    {
	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) {
	    if( _powerOutLets[ipo].name.equalsIgnoreCase( device ) )
		return powerON( _powerOutLets[ipo].number );
	}

	_errmsg = "ERR: did not find outlet for: " + device;
	_error = true;
	System.err.println( _mainClass + ".powerON> " + _errmsg );
	return false;
    }

    public boolean powerOFF( String device )
    {
	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) {
	    if( _powerOutLets[ipo].name.equalsIgnoreCase( device ) )
		return powerOFF( _powerOutLets[ipo].number );
	}

	_errmsg = "ERR: did not find outlet for: " + device;
	_error = true;
	System.err.println( _mainClass + ".powerOFF> " + _errmsg );
	return false;
    }
//---------------------------------------------------------------------------------------------

    public boolean powerON( int oNumber )
    {
	String powerCmd = "ON " + oNumber + "\r";
	String reply = _sendRecv( powerCmd, "powerON");
	parseStatus(reply);

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }

    public boolean powerOFF( int oNumber )
    {
	String powerCmd = "OFF " + oNumber + "\r";
	String reply = _sendRecv( powerCmd, "powerOFF");
	parseStatus(reply);

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }
//---------------------------------------------------------------------------------------------

    public boolean powerONall()
    {
	String powerCmd = "ON all\r";
	String reply = _sendRecv( powerCmd, "powerONall");
	parseStatus(reply);

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }

    public boolean powerOFFall()
    {
	String powerCmd = "OFF all\r";
	String reply = _sendRecv( powerCmd, "powerOFFall");
	parseStatus(reply);

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }
   
//--------------------------------------------------------------------------------------------
 
    public byte[] recv() {
	byte[] btReply = _recv(); //recv from UFLibPanel
	
	if(btReply != null && btReply.length > 0) {
	    int i = 0;
	    while(i < btReply.length && btReply[i] == -1) i += 3;
	
	    if(i > 0) {
		byte[] response = new byte[i];
		for(int j = 0; j < i; j++) {
		    if(btReply[j] == -3) response[j] = -4; // if DO say WONT
		    else if(btReply[j] == -5) response[j] = -2; // if WILL say DONT
		    else response[j] = btReply[j];
		}

		send(response);
	    }
	
	    if(i == btReply.length) // recv got only option negotiations
		return recv();
	    else {
		byte[] data = new byte[btReply.length-i];
		for(int j = 0; j < btReply.length-i; j++)
		    data[j] = btReply[j + i];
	        return data;
	    }
	}
	else return btReply;
    }

//---------------------------------------------------------------------------------------------

    private synchronized String _sendRecv( String btCmd, String caller )
    {
	if( _socket == null ) {
	    System.out.println("*\n" + _mainClass + "._sendRecv(" + caller + ")> re-connecting...");
	    if( !initialize() ) {
		System.err.println( _mainClass + "._sendRecv(" + caller + ")> " + _errmsg );
		return new String(_errmsg);
	    }
	}

	if( send( btCmd ) > 0 ) {

	    byte[] btReply = recv();

	    if( btReply.length > 0 ) {
		String reply = new String(btReply);
		if( reply.indexOf("(Y/N)") > 0) {
		    send("Y\r");
		    btReply = recv();
		    reply = new String(btReply);
		}
		if(_verbose) System.out.println( _mainClass + "._sendRecv("+caller+")> " + reply );
		_error = false;
		return reply;
	    }
	    else {
		_errmsg = "ERR: Failed recving reply for: " + caller;
		_error = true;
		System.err.println( _mainClass + "._sendRecv> " + _errmsg );
		close();
		return new String(_errmsg);
	    }
	}
	else {
	    _errmsg = "ERR: Failed sending request for: " + caller;
	    _error = true;
	    System.err.println( _mainClass + "._sendRecv> " + _errmsg );
	    close();
	    return new String(_errmsg);
	}
    }

    public String getOutletName(int n) {
      for (int j = 0; j < _powerOutLets.length; j++) {
	if (_powerOutLets[j].number == n) return _powerOutLets[j].getName();
      }
      return null;
    }

    public boolean getOutletStatus(int n) {
      for (int j = 0; j < _powerOutLets.length; j++) {
        if (_powerOutLets[j].number == n) return _powerOutLets[j].getStatus();
      }
      return false;
    }

    public static void main(String[] args) {
	UFMMTBaytech bt = new UFMMTBaytech("mmtbaytech");
	bt.setUserAndPass("mmtpol",".uf.mmt;");
	bt.initialize();
	bt.checkStatus();
	for (int j = 1; j < 9; j++) {
	  System.out.println(j+" "+bt.getOutletName(j)+" "+bt.getOutletStatus(j));
	}
    }
//========================UFLibPanel methods copied here =================================
    public String dateTime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT.");
    }

    public void setHostAndPort( String host, int port ) {
        setHost( host );
        setPort( port );
    }

    public void setHost(String host) {
        _Host = host;
    }

    public void setPort(int port) {
        _Port = port;
    }

    public String getHost() { return _Host; }
    public int getPort() { return _Port; }

    public boolean connect() { return connect(_connType, _serverHandshake); }
    public boolean close()   { return close(_connType); }
//-------------------------------------------------------------------------------
    /**
     *  Connect via socket to the Agent or Server.
     */
    protected boolean connect(String cType, String handshake)
    {
        close( cType );

        try {
            _Host = getHost();
            _Port = getPort();
            String message = cType + "port=" + _Port + " @ host = " + _Host;
            System.out.println( _mainClass + "." + message );
            setSocketStatus(1);
            InetSocketAddress agentIPsoca = new InetSocketAddress( _Host, _Port );
            _socket = new Socket();
            _socket.connect( agentIPsoca, CONNECT_TIMEOUT );

            if( handshake != null ) {
                //System.out.println( _mainClass + "." + cType + "Handshake timeout = " +  HANDSHAKE_TIMEOUT);
                _socket.setSoTimeout( HANDSHAKE_TIMEOUT );
                //send some kind of handshake (usually client name or simple request):
                UFTimeStamp uft = new UFTimeStamp(handshake);

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
                System.out.println( _mainClass + "." + message );
            }
            InetAddress LocalInet = _socket.getLocalAddress();
            //set normal timeout for socket
            _socket.setSoTimeout( _socTimeout );
            message = cType + "Connected to " + serverName + " on port=" + _Port + " @ host = " + _Host;
            System.out.println( _mainClass + "." + message );
            _inpStream = new DataInputStream(_socket.getInputStream());
            _outpStream = new DataOutputStream(_socket.getOutputStream());
            setSocketStatus(0);
            return true;
        }
        catch( Exception x ) {
            String message = cType + x.toString();
            System.err.println(_mainClass + "." + message);
            close( cType );
            return false;
        }
    }
//-------------------------------------------------------------------------------
    /**
     * UFLibPanel#close
     */
    protected boolean close(String cType) { 
        if( _socket == null ) return true;
        boolean status;

        try {
            String message = cType + " closing socket...";
            System.out.println( _mainClass + "." +  message );
            _socket.close();
            _socket = null;
            setSocketStatus(-1);
            status = true;
        }
        catch (IOException ioe) {
            String message = cType + ioe.toString();
            System.err.println( _mainClass + "." + message );
            _socket = null;
            setSocketStatus(-1);
            status = false;
        }

        try { Thread.sleep(700);} catch( Exception _e ) {}
        return status;
    }
//-------------------------------------------------------------------------------

    protected void connectError( String cType, String errmsg )
    {
        String message = cType + errmsg + " ERROR";
        System.err.println( _mainClass + "." + message );
        close( cType );
    }

//-------------------------------------------------------------------------------

    protected void setSocketStatus( int status )
    {
        if( _socketStatus == status ) return; //do it only if changed.
        _socketStatus = status;
    }

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
            System.err.println( _mainClass + ".send> " + eof.toString() );
        }
        catch( IOException ioe ) {
            System.err.println( _mainClass + ".send> " + ioe.toString() );
        }
        catch( Exception ex ) {
            System.err.println( _mainClass + ".send> " + ex.toString() );
        }
        return(-1);
    }
//-------------------------------------------------------------------------------
    //read raw bytes from socket into a buffer and then transfer the read bytes to returned array.

    public byte[] _recv()
    {
        if( _recvBuffer == null ) _recvBuffer = new byte[_bufferSize];
        else if( _recvBuffer.length != _bufferSize ) _recvBuffer = new byte[_bufferSize];
        try {
            int nrb = 0;
            int nav = _inpStream.available();
System.out.println("Buffer size: "+_bufferSize+"; nav: "+nav);
            if( nav <= 0 ) {
                int ntry = -1, maxtry=12; //try 12 times in 1 second intervals
                while( nav <= 0 && ++ntry < maxtry ) {
                    try { Thread.sleep(_recvTimeout); } catch( Exception e ) { e.printStackTrace(); }
                    nav = _inpStream.available();
                }
                if( nav <= 0 ) {
                    System.err.println("*\n" + _mainClass + ".recv> nothing available after "
                                       + (maxtry*_recvTimeout/1000.) + " sec.");
                    return(new byte[0]);
                }
            }
            while( nav > 0 ) {
                _inpStream.readFully( _recvBuffer, nrb, nav );
                nrb += nav;
System.out.println("Buffer size: "+_bufferSize+"; nav: "+nav+"; nrb: "+nrb);
                try { Thread.sleep(_recvTimeout); } catch( Exception _e ) {}
                nav = _inpStream.available();
            }
            byte[] data = new byte[nrb];
            System.arraycopy( _recvBuffer, 0, data, 0, nrb );
            return data;
        }
        catch( EOFException eof ) {
            System.err.println( _mainClass + ".recv> " + eof.toString() );
        }
        catch( IOException ioe ) {
            System.err.println( _mainClass + ".recv> " + ioe.toString() );
        }
        catch( Exception ex ) {
            System.err.println( _mainClass + ".recv> " + ex.toString() );
        }
        return(null);
    }

//=======================================================================================

    public class UFBaytechOutlet {
        int number = 0;
        String name = "";
        boolean power = false;

        public UFBaytechOutlet() {}

        protected boolean parse( String statusRec ) {
            int rppos = statusRec.indexOf(")...");
            if( rppos > 0 ) {
                try {
                    this.number = Integer.parseInt( statusRec.substring(0,rppos).trim() );
                } catch(NumberFormatException nfe) {
                    System.err.println( _mainClass + ".parse> " + nfe.toString() );
                }
                int cpos = statusRec.indexOf(":");
                if( cpos > 0 ) {

                    this.name = statusRec.substring( rppos+4, cpos ).trim();
                    String onoff = statusRec.substring( ++cpos ).trim().toUpperCase();

                    if( onoff.equals("ON") )
                        this.power = true;
                    else
                        this.power = false;

                    if (_verbose) System.out.println( _mainClass + ".parse> " + this.toString() );
                    return true;
                }
                else if (_verbose) System.out.println( _mainClass+".parse> did not find : in: "+statusRec );
            }
            else if (_verbose) System.out.println( _mainClass+".parse> did not find )... in: "+statusRec );

            return false;
        }

        public String toString() {
            String status = "Power> (" + number + ") " + name + "\t : ";
            if( this.power )
                status += "ON";
            else
                status += "OFF";
            return status;
        }

	public String getName() {
	  return name;
	}

	public boolean getStatus() {
	  return power;
	}
    }

}
