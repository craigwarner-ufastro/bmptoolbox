package javaUFLib;
/**
 * Title:        UFPLCifce.java
 * Version:      (see rcsID)
 * Authors:      Frank Varosi and Julian van Eyken
 * Company:      University of Florida
 * Description:  Provide Modbus/TCP/IP interface to a PLC (DL06).
 */
import java.util.*;
import java.net.*;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;

//====================================================================================================
//Class for Modbus/TCP communications with a PLC.

public class UFPLCifce {

    public static final
	String rcsID = "$Name:  $ $Id: UFPLCifce.java,v 1.5 2008/09/12 07:31:18 varosi Exp $";

    protected int                        _plcTimeout = 7000;     // default 7 seconds timeout on recv.
    protected String                     _plcHost = "";
    private InetAddress                _plcAddr = null;
    private int                        _port = Modbus.DEFAULT_PORT;
    protected WriteSingleRegisterRequest _writeReq = null;
    protected ReadInputRegistersRequest  _readReq = null;
    private TCPMasterConnection        _conPLC = null;
    private ModbusTransaction          _trans = null;
    private int                        _ref = 0;
    private int _nErr = 0;
    private int _maxErr = 3;
    private boolean _plcCommErr = false; // Flag in case of error in PLC communications.
    protected boolean _verbose = false;
    private String _className = getClass().getName() + ".";

    public UFPLCifce() {
	_writeReq = new WriteSingleRegisterRequest(); //To be used for sending write requests to PLC.
	_readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
    }

    public synchronized int readRegister( int register ) {

	_readReq.setReference( register );
	_readReq.setUnitID(0);
	_readReq.setWordCount(1);

	try{
	   _trans.setRequest(_readReq);
	   _trans.execute();
	   ReadInputRegistersResponse res = (ReadInputRegistersResponse) _trans.getResponse();
	   int wordIn = res.getRegisterValue(0);
	   if( _verbose ) System.out.println(_className + "readRegister> "+register+" = "+wordIn);
	   return( wordIn );
	}
	catch (Exception ex) { respondToCommErr(ex); }

	return(-1);
    }

    public synchronized boolean writeRegister( int register, int newVal )
    {
	return writeRegister( register, new SimpleRegister( newVal ) );
    }

    public synchronized boolean writeRegister( int register, SimpleRegister newVal ) {

	_writeReq.setReference( register );
	_writeReq.setRegister( newVal );
	_writeReq.setUnitID(0);
	if (_verbose) System.out.println(_className + "writeRegister> " + _writeReq.getHexMessage());

	try {
	    _trans.setRequest(_writeReq);
	    _trans.execute();
	    ModbusResponse modres = _trans.getResponse(); //get response
	    if (_verbose) System.out.println(_className + "writeRegister> Response: "
					     + modres.getHexMessage());
	    return true;
	}
	catch (Exception ex) {
	    respondToCommErr(ex);
	    return false;
	}
    }

    public synchronized int[] readRegisters( int[] registers ) {

	int[] rvals = new int[ registers.length ];

	for( int i=0; i < registers.length; i++ ) rvals[i] = this.readRegister( registers[i] );

	return rvals;
    }

    public synchronized void writeRegisters( int[] registers, SimpleRegister[] newVals ) {

	for( int i=0; i < registers.length; i++ ) this.writeRegister( registers[i], newVals[i] );
    }

    public void respondToCommErr(Exception ex) {
	System.err.println(_className + ex.toString());
	ex.printStackTrace();
	_plcCommErr = true;
	if( ++_nErr > _maxErr ) {
	    _plcCommErr = true;
	    closeConnection();
	    _writeReq = new WriteSingleRegisterRequest();
	    _readReq = new ReadInputRegistersRequest();   //Ditto for read requests.
	    _connectToPLC();
	}
    }
        
    //made synchronized method 7/10/14
    public synchronized boolean setBit( int register, int bit, boolean val ) {

	//Sets or clears one bit in a register on the PLC.
	//Read in current value of complete word, set required bit to val, and write back out.
	String imethod = _className + "setBit> ";

	_readReq.setReference(register);
	_readReq.setUnitID(0);
	_readReq.setWordCount(1);

	try{
	    _trans.setRequest(_readReq);
	    _trans.execute();
	    ReadInputRegistersResponse res = (ReadInputRegistersResponse)_trans.getResponse();
	    int wordIn = res.getRegisterValue(0), wordOut;
	    int mask = 1 << bit;
	    if (val) wordOut = wordIn | mask;  else  wordOut = wordIn & (~mask);
	    if(_verbose)
	    	System.out.println(imethod+"register: "+register+
					", wordIn: "+wordIn+", wordOut: "+wordOut);
	    _writeReq.setReference(register);
	    _writeReq.setUnitID(0);
	    _writeReq.setRegister(new SimpleRegister(wordOut));
	    _trans.setRequest(_writeReq);
	    _trans.execute();
            ModbusResponse modres = _trans.getResponse(); //get response
            if (_verbose) System.out.println(_className + "setBit> Response: " + modres.getHexMessage());
	    return true;
	} catch(ClassCastException ce) {
	    System.err.println(_className + ce.toString());
	    //try one more time
	    try {
              _trans.setRequest(_readReq);
              _trans.execute();
	      ReadInputRegistersResponse res = (ReadInputRegistersResponse)_trans.getResponse();
              int wordIn = res.getRegisterValue(0), wordOut;
	      int mask = 1 << bit;
	      if (val) wordOut = wordIn | mask;  else  wordOut = wordIn & (~mask);
	      if(_verbose)
		System.out.println(imethod+"register: "+register+
                                        ", wordIn: "+wordIn+", wordOut: "+wordOut);
              _writeReq.setReference(register);
              _writeReq.setUnitID(0);
              _writeReq.setRegister(new SimpleRegister(wordOut));
              _trans.setRequest(_writeReq);
              _trans.execute();
              ModbusResponse modres = _trans.getResponse(); //get response
              if (_verbose) System.out.println(_className + "setBit> Response: " + modres.getHexMessage());
              return true;
	    } catch(Exception ex) {
		respondToCommErr(ex);
		return false;
	    }
	} catch (Exception ex) {
	    respondToCommErr(ex);
	    return false;
	}
    }
    
    public void connectToPLC( String host ) {
	_plcHost = host;
	_connectToPLC();
    }

    public void connectToPLC( String host, int timeout ) {
	_plcHost = host;
	_plcTimeout = timeout;
	_connectToPLC();
    }

    private synchronized void _connectToPLC() {

	String imethod = _className + "_connectToPLC> ";

	if( _conPLC == null ) {
	    try{
		System.out.println( imethod + "creating interface to PLC host: "+_plcHost);
		_plcAddr = InetAddress.getByName(_plcHost);
		_conPLC = new TCPMasterConnection(_plcAddr);
		_plcCommErr = false;
	 	_nErr = 0;
	    }
	    catch (UnknownHostException he) {
		System.err.println( imethod + "Unable to find PLC hostname: "+_plcHost);
		he.printStackTrace();
		_plcCommErr = true;
	    }
	    catch (Exception ex) {
		System.err.println(ex.toString());
		System.err.println( imethod + "Unable to connect to PLC: "+_plcHost);
		_plcCommErr = true;
	    }
	}

	try{
	    System.out.println( imethod + "connecting to PLC host: "+_plcHost);
	    _conPLC.setPort(_port);
	    _conPLC.connect();
	    _conPLC.setTimeout(_plcTimeout);
	    if (_trans == null) _trans = new ModbusTCPTransaction(_conPLC);

	    System.out.println( imethod + "Connected to " + _plcAddr.toString() + ":" + _conPLC.getPort());
	    System.out.println( imethod + "Timeout: " + _conPLC.getTimeout());
	    System.out.println( imethod + "# retries for connection on transaction: "+_trans.getRetries());
	    _plcCommErr = false;
	    _nErr = 0;
	}
	catch (Exception e) {
	    System.err.println( imethod + e.toString());
	    System.err.println( imethod + "Unable to connect to PLC: "+_plcHost);
	    _plcCommErr = true;
	}
    }

    public void closeConnection() {
	if( _conPLC != null ) {
	   System.out.println(_className + "closeConnections> closing connection to PLC: "+_plcHost);
	   _conPLC.close();
	}
	else System.out.println(_className + "closeConnections> no connection to PLC: "+_plcHost);
	_trans = null;
    }

    public boolean getErrStatus() {return _plcCommErr;}
    public void clearErr() {_plcCommErr = false;}
    public void verbose( boolean verbose ) { _verbose = verbose; }
}
