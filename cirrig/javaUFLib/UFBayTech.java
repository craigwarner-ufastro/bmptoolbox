package javaUFLib;
import java.util.StringTokenizer;

//Title:        UFBayTech
//Version:      (see rcsID)
//Author:       Frank Varosi
//Copyright:    Copyright (c) 2003-7
//Company:      University of Florida, Dept. of Astronomy.
//Description:  Extension of UFLibPanel to communicate with BayTech Remote Power Control device.

//=======================================================================================
/**
 * Extension of ULibPanel to communicate with BayTech Remote Power Control (RPC) device.
 * Used by CanariCam Interface Server (CIS) (see CancamIfcServer.java).
 * BayTech RPC supports telnet (port=23) communications, and has 8 controlled power outlets.
 * @author Frank Varosi
 */

public class UFBayTech extends UFLibPanel {

    public static final
	String rcsID = "$Name:  $ $Id: UFBayTech.java,v 1.36 2011/05/09 19:43:54 varosi Exp $";

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
    
    protected UFBayTechOutlet[] _powerOutLets;
//-----------------------------------------------------------------------------------------------------------

    public UFBayTech() {
	super("ufccbaytech",23);
	_init( 8 );
    }

    public UFBayTech(String hostname) {
	super( hostname, 23 );
	_init( 8 );
    }

    public UFBayTech( String hostname, int numOutlets ) {
	super( hostname, 23 );
	_init( numOutlets );
    }

    private void _init( int numOutlets )
    {
	_powerOutLets = new UFBayTechOutlet[numOutlets];
	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) _powerOutLets[ipo] = new UFBayTechOutlet();
    }
//-----------------------------------------------------------------------------------------------------------

    public boolean initialize()
    {
	String imethod = "initialize";
	String imsg = className + "." + imethod + "> ";

	if( connect() ) {
	    
	    _error = false;
	    _sleepSec(1);
	    //upon connection the BayTech gives Login and/or status info so recv it:
	    byte[] bstat = recv();

	    if( bstat.length > 0 ) {
		String reply = new String(bstat);
		System.out.println( imsg + reply );

		if( reply.indexOf("in use") < 0 ) {

		    if(reply.indexOf("Outlet Control") > 0) {
			_baytechOld = true;
			reply = _sendRecv("1\r", imethod );
			System.out.println( imsg + reply );
		    }
		    else if(reply.indexOf("login") > 0) {
			_baytechNew = true;
			reply = _sendRecv("root\r", imethod );
			System.out.println( imsg + reply );
			if(reply.indexOf("Password") >= 0) {
			    reply = _sendRecv("baytech\r", imethod );
			    System.out.println( imsg );
			    System.out.println( reply );
			}
			else {
			    _error = true;
			    _errmsg = "ERR: Failed to login to Baytech device";
			    System.err.println( imsg + _errmsg );
			    close();
			    _sleepSec(1);
			    return false;
			}
		    }
		    return true;
		}
		else {
		    _clearRootConnection();
		    _errmsg = "ERR: RPC is in use by another client: closing connection....";
		}
	    }
	    else _errmsg = "ERR: Failed recving status reply.";
	}
	else _errmsg = "ERR: failed connecting to BayTech RPC.";

	_error = true;
	System.err.println( imsg + _errmsg );
	close();
	_sleepSec(1);
	return false;
    }
//---------------------------------------------------------------------------------------------

    protected void _clearRootConnection()
    {
	String imethod = "_clearRootConnection";
	String imsg = className + "." + imethod + "> ";
	String reply = _sendRecv("S\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("3\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("T\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("2\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("\r", imethod );
	System.err.println( imsg + reply );
	reply = _sendRecv("T\r", imethod );
	System.err.println( imsg + reply );
    }
//---------------------------------------------------------------------------------------------

    public String[] checkStatus()
    {
	String statusReply = getStatus();
	boolean parsed = false;

	if( _baytechOld )      parsed = parseStatusOld( statusReply );
	else if( _baytechNew ) parsed = parseStatusNew( statusReply );
	else	               parsed = parseStatus( statusReply );

	if( !parsed ) {  //error found and socket was closed so try just one more time...
	    statusReply = getStatus();
	    if( _baytechOld )      parseStatusOld( statusReply );
	    else if( _baytechNew ) parseStatusNew( statusReply );
	    else                   parseStatus( statusReply );
	}

	return statusInfo();
    }

    public String getStatus()
    {
	String statusReply = _sendRecv("status\r","getStatus");
	//if an error occurred try just one more time (automatically reconnects):
	if( _error )
	    return _sendRecv("status\r","getStatus");
	else
	    return statusReply;
    }
//-----------------------------------------------------------------------------------------------------------

    public boolean parseStatus( String btStatus )
    {
	String imethod = "parseStatus";
	String imsg = className + "." + imethod + "> ";

	if( btStatus.indexOf("ERR") >= 0 ) {
	    _error = true;
	    _errmsg = btStatus;
	    System.err.println( imsg + _errmsg );
	    close();
	    _sleepSec(1);
	    return false;
	}

	_time = dateTime();
	_error = false;
	_errmsg = "";
	String[] statLines = btStatus.split("\n");

	if( statLines.length < 4 ) {
	    _error = true;
	    _errmsg = "ERR: less than 4 items in status response from BayTech RPC.";
	    System.err.println( imsg + _errmsg );
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
			System.err.println( imsg + nfe.toString() );
			System.err.println( imsg + "("+i+") " + statrec );
		    }
		    catch( Exception ex ) {
			System.err.println( imsg + ex.toString() );
			System.err.println( imsg + "("+i+") " + statrec );
		    }
		}
	    }
	}

	if( ipo <= 0 ) {
	    _error = true;
	    _errmsg = "did not find any power outlet status strings.";
	    System.err.println( imsg + _errmsg );
	    _clearRootConnection();
	    close();
	    _sleepSec(1);
	    return false;
	}
	else return true;
    }
//-----------------------------------------------------------------------------------------------------------

    public boolean parseStatusOld( String btStatus )
    {
	String imethod = "parseStatusOld";
	String imsg = className + "." + imethod + "> ";

	if( btStatus.indexOf("ERR") >= 0 ) {
	    _error = true;
	    _errmsg = btStatus;
	    System.err.println( imsg + _errmsg );
	    close();
	    _sleepSec(1);
	    return false;
	}

	_time = dateTime();
	_error = false;
	_errmsg = "";
	String[] statLines = btStatus.split("\n");

	if( statLines.length < 4 ) {
	    _error = true;
	    _errmsg = "ERR: less than 4 items in status response from BayTech RPC.";
	    System.err.println( imsg + _errmsg );
	}

	//parse the status reply from BayTech RPC: (int ipo counts the power outlets in reply)
	int ipo = 0;

	for( int i=0; i < statLines.length; i++ ) {
	    String statrec = statLines[i];
	    try {
		if(statrec.length() < 15) continue;
		else if(statrec.indexOf("True RMS current") >= 0) {
		    int si = statrec.indexOf(":") + 1;
		    int ei = statrec.indexOf("Amps");
		    if(si < ei)
			_Amps = Float.parseFloat(statrec.substring(si, ei).trim());
		}
		else if(statrec.indexOf("Maximum Detected") >= 0) {
		    int si = statrec.indexOf(":") + 1;
		    int ei = statrec.indexOf("amps");
		    if(si < ei)
			_maxAmps = Float.parseFloat(statrec.substring(si, ei).trim());
		}
		else if(statrec.indexOf("Internal Temperature") >= 0) {
		    int si = statrec.indexOf(":") + 1;
		    int ei = statrec.indexOf("C");
		    if(si < ei)
			_temperature = Float.parseFloat(statrec.substring(si, ei).trim());
		}
		else if(statrec.indexOf("Circuit Breaker") >= 0) {
		    int si = statrec.indexOf(":") + 1;
		    _breaker = statrec.substring(si).trim();
		}
		else if(statrec.indexOf("Status") >= 0) {
		    while( ++i < statLines.length ) {
			statrec = statLines[i];
			if( _powerOutLets[ipo].parseOld(statrec) ) ++ipo;
			else System.err.println( imsg + "failed to parse: " + statrec);
		    }
		    break;
		}			
	    }
	    catch(NumberFormatException nfe) {
		System.err.println( imsg + nfe.toString() );
		System.err.println( imsg + "("+i+") " + statrec );
	    }
	    catch( Exception ex ) {
		System.err.println( imsg + ex.toString() );
		System.err.println( imsg + "("+i+") " + statrec );
	    }
	}

	if( ipo <= 0 ) {
	    _error = true;
	    _errmsg = "did not find any power outlet status strings.";
	    System.err.println( imsg + _errmsg );
	    close();
	    _sleepSec(1);
	    return false;
	}
	else return true;
    }
//-----------------------------------------------------------------------------------------------------------

    public boolean parseStatusNew( String btStatus ) {

	String imethod = "parseStatusNew";
	String imsg = className + "." + imethod + "> ";

	if( btStatus.indexOf("ERR") >= 0 ) {
	    _error = true;
	    _errmsg = btStatus;
	    System.err.println( imsg + _errmsg );
	    close();
	    _sleepSec(1);
	    return false;
	}

	_time = dateTime();
	_error = false;
	_isCelsius = false;
	_errmsg = "";
	String[] statLines = btStatus.split("\n");

	if( statLines.length < 4 ) {
	    _error = true;
	    _errmsg = "ERR: less than 4 items in status response from BayTech RPC.";
	    System.err.println( imsg + _errmsg );
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
			if( epos > cpos ) _temperature = Float.parseFloat(statrec.substring(cpos+1, epos).trim());
		    }
		}
		catch( NumberFormatException nfe ) {
		    System.err.println( imsg + nfe.toString() );
		    System.err.println( imsg + "("+i+") " + statrec );
		}
		catch( Exception ex ) {
		    System.err.println( imsg + ex.toString() );
		    System.err.println( imsg + "("+i+") " + statrec );
		}
	    }
	}

	if( ipo <= 0 ) {
	    _error = true;
	    _errmsg = "did not find any power outlet status strings.";
	    System.err.println( imsg + _errmsg );
	    _clearRootConnection();
	    close();
	    _sleepSec(1);
	    return false;
	}
	else return true;
    }
//-----------------------------------------------------------------------------------------------------------

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
	    if( _powerOutLets[ipo].device.equalsIgnoreCase( device ) )
		return powerON( _powerOutLets[ipo].number );
	}

	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) {
	    if( _powerOutLets[ipo].name.equalsIgnoreCase( device ) )
		return powerON( _powerOutLets[ipo].number );
	}

	_errmsg = "ERR: did not find outlet for: " + device;
	_error = true;
	System.err.println( className + ".powerON> " + _errmsg );
	return false;
    }

    public boolean powerOFF( String device )
    {
	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) {
	    if( _powerOutLets[ipo].device.equalsIgnoreCase( device ) )
		return powerOFF( _powerOutLets[ipo].number );
	}

	for( int ipo=0; ipo < _powerOutLets.length; ipo++ ) {
	    if( _powerOutLets[ipo].name.equalsIgnoreCase( device ) )
		return powerOFF( _powerOutLets[ipo].number );
	}

	_errmsg = "ERR: did not find outlet for: " + device;
	_error = true;
	System.err.println( className + ".powerOFF> " + _errmsg );
	return false;
    }
//---------------------------------------------------------------------------------------------

    public boolean powerON( int oNumber )
    {
	String powerCmd = "ON " + oNumber + "\r";
	String reply = _sendRecv( powerCmd, "powerON");

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }

    public boolean powerOFF( int oNumber )
    {
	String powerCmd = "OFF " + oNumber + "\r";
	String reply = _sendRecv( powerCmd, "powerOFF");

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

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }

    public boolean powerOFFall()
    {
	String powerCmd = "OFF all\r";
	String reply = _sendRecv( powerCmd, "powerOFFall");

	if( reply.indexOf("ERR") >= 0 )
	    return false;
	else
	    return true;
    }
   
//--------------------------------------------------------------------------------------------
 
    public byte[] recv() {

	byte[] btReply = super.recv();
	
	if( btReply != null && btReply.length > 0 ) {
	    int i = 0;
	    while( i < btReply.length && btReply[i] == -1 ) i += 3;
	
	    if( i > 0 ) {
		byte[] response = new byte[i];
		for( int j = 0; j < i; j++ ) {
		    if(btReply[j] == -3) response[j] = -4; // if DO say WONT
		    else if(btReply[j] == -5) response[j] = -2; // if WILL say DONT
		    else response[j] = btReply[j];
		}

		send(response);
	    }
	
	    if( i == btReply.length ) // recv got only option negotiations
		return recv();
	    else {
		byte[] data = new byte[btReply.length-i];
		for(int j = 0; j < btReply.length-i; j++) data[j] = btReply[j + i];
	        return data;
	    }
	}
	else return btReply;
    }

//---------------------------------------------------------------------------------------------

    private synchronized String _sendRecv( String btCmd, String caller )
    {
	String imethod = className + "._sendRecv(" + caller + ")> ";

	if( _socket == null ) {
	    System.out.println("*\n" + imethod + "re-connecting...");
	    if( !initialize() ) {
		System.err.println( imethod + _errmsg );
		return new String(_errmsg);
	    }
	}

	if( send( btCmd ) > 0 ) {

	    _sleepSec(2);
	    byte[] btReply = recv();

	    if( btReply.length > 0 ) {
		String reply = new String(btReply);
		if( reply.indexOf("(Y/N)") > 0) {
		    send("Y\r");
		    btReply = recv();
		    reply = new String(btReply);
		}
		if(_verbose) System.out.println( imethod + reply );
		_error = false;
		return reply;
	    }
	    else _errmsg = "ERR: Failed recving reply for: " + caller;
	}
	else _errmsg = "ERR: Failed sending request for: " + caller;

	close();
	_error = true;
	System.err.println( imethod + _errmsg );
	_sleepSec(2);
	return new String(_errmsg);
    }

    private synchronized void _sleepSec( int nsec ) {
	try { Thread.sleep( nsec * 1000 ); } catch( Exception x ) { System.err.println( x.toString() ); }
    }
//=======================================================================================

    public class UFBayTechOutlet
    {
	int number = 0;
	String name = "";
	String thisName = "UFBayTechOutlet";
	String device = "";
	boolean power = false;

	public UFBayTechOutlet() {}

	protected boolean parse( String statusRec )
	{
	    int rppos = statusRec.indexOf(")");

	    if( rppos > 0 ) {
		try {
		    int fppos = statusRec.indexOf("(");
		    if( fppos < 0 ) fppos = 0; else ++fppos;
		    this.number = Integer.parseInt( statusRec.substring(fppos,rppos).trim() );
		}
		catch( NumberFormatException nfe ) {
		    System.err.println( thisName + ".parse> " + nfe.toString() );
		    //	    return false;
		}

		int cpos = statusRec.indexOf(": O");
		if( cpos > 0 ) {

		    int ppp = statusRec.indexOf("...");
		    if( ppp < 0 ) ppp = ++rppos; else ppp += 3;
		    this.name = statusRec.substring( ppp, cpos ).trim();
		    String onoff = statusRec.substring( ++cpos ).trim().toUpperCase();

		    if( onoff.equals("ON") )
			this.power = true;
		    else if( onoff.equals("OFF") )
			this.power = false;
		    else {
			this.power = false;
			System.out.println( thisName + ".parse> did not find ON or OFF in:" + statusRec );
		    }

		    int bfpos = this.name.indexOf("[");
		    if( bfpos >= 0 ) {
			int bepos = this.name.indexOf("]");
			if( bepos > bfpos ) {
			    this.device = this.name.substring( ++bfpos, bepos ).trim().toUpperCase();
			    this.name = this.name.substring( ++bepos ).trim();
			    if(_verbose) System.out.println( thisName + ".parse> " + this.toString() );
			    return true;
			}
			else System.out.println( thisName + ".parse> did not find [DeviceName] in:" + statusRec );
		    }
		    else System.out.println( thisName + ".parse> did not find Device Name in:" + statusRec );
		}
		else if(_verbose) System.out.println( thisName + ".parse> did not find : in: "+statusRec );
	    }
	    else if(_verbose) System.out.println( thisName + ".parse> did not find ) in: "+statusRec );

	    return false;
	}

	protected boolean parseOld( String statusRec )
	{
	    StringTokenizer st = new StringTokenizer(statusRec);
	    if(st.countTokens() == 4) {
		st.nextToken(); // kill the first token
		this.name = st.nextToken();
		try {
		    this.number = Integer.parseInt( st.nextToken() );
		}
		catch( NumberFormatException nfe ) {
		    System.err.println( thisName + ".parseOld> " + nfe.toString() );
		}
		this.power = (st.nextToken().equalsIgnoreCase("on"))? true : false;
		
		if(_verbose) System.out.println( thisName + ".parseOld> " + this.toString() );
		return true;
	    }
	    
	    return false;
	}

	public String toString() {
	    String status = "Power> (" + number + ") [" + device + "] " + name + "\t : ";
	    if( this.power )
		status += "ON";
	    else
		status += "OFF";
	    return status;
	}
    }
}
