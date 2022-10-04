package javaUFProtocol;

//Title:        UFBytes.java
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-7
//Authors:      David Hon, David Rashkin, Frank Varosi
//Company:      University of Florida, Dept. of Astronomy.
//Description:  Extend UFTimeStamp to store byte array. Can be used to send/recv strings/bytes..

import java.io.*;
import java.net.*;

public class UFBytes extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFBytes.java,v 1.12 2010/03/29 21:58:52 varosi Exp $";

    protected byte[] _values = null;

    protected byte _min = 127;
    protected byte _max = -128;
    protected int _ixMin = -1;
    protected int _ixMax = -1;

    public UFBytes() {
	_init();
    }

    public UFBytes(int length) {
	_init();
	_length = length;
    }

    public UFBytes(String name) {
	_init();
	_name = new String(name);
	_length = _minLength();
    }

    public UFBytes(String name, int nelem) {
	_init();
	_name = new String(name);
	_elem = nelem;
	_length = _minLength() + _elem;
    }

    public UFBytes(String name, byte[] vals) {
	_init();
	_name = new String(name);
	_elem = vals.length;
	_values = vals;
	_length = _minLength() + vals.length;
    }

    public UFBytes(String name, String inString) {
	_init();
	_name = new String(name);
	_elem = inString.length();
	_values = inString.getBytes();
	_length = _minLength() + _elem;
    }

    private void _init() {
	_currentTime();
	_type = MsgTyp._Bytes_;
    }

    // all methods declared abstract by UFProtocol can be defined here:

    public String description() { return new String("UFBytes"); }
 
    // return size of an element:

    public int valSize() { return 1; }

    public int valSize(int elemIdx) {
	if( _values != null ) {
	    if( elemIdx >= _values.length ) return 0;
	}
	return 1;
    }

    public byte[] values() { return _values; }
    public byte valData(int index) { return _values[index]; }
    public String valString() { return new String(_values); }

    public byte maxVal() {
	byte max=-128;
	if( _max > max ) return _max;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i]>max ) max = _values[i];

	_max = max;
	return max;
    }

    public byte minVal() {
	byte min=127;
	if( _min < min ) return _min;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] < min ) min = _values[i];

	_min = min;
	return min;
    }

    public void calcMinMax()
    {
	for( int i=0; i<_values.length; i++ ) {
	    byte val = _values[i];
	    if( val < _min ) _min = val;
	    if( val > _max ) _max = val;
	}
    }

    public void calcMinMax( boolean Locs )
    {
	for( int i=0; i<_values.length; i++ ) {
	    byte val = _values[i];
	    if( val < _min ) {
		_min = val;
		_ixMin = i;
	    }
	    if( val > _max ) {
		_max = val;
		_ixMax = i;
	    }
	}
    }

    public int numVals() {
	if (_values != null)
	    return _values.length;
	else
	    return 0;
    }

    protected void _copyNameAndVals(String s, byte[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new byte[vals.length];
	_length = _minLength() + vals.length;
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	}
    }

    public void setNameAndVals(String s, byte[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new byte[vals.length];
	_length = _minLength() + vals.length;
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	}
    }

    // receive data values:
    // if # elements is not set then check stream for # bytes available.
    // assume length, type, and header have already been read,
    // or can be used to just read bytes from a stream.

    public int recvData(DataInputStream inpStrm) {
	try {
	    if( _elem <= 0 ) {
		_elem = inpStrm.available();
		if( _elem <= 0 ) {
		    System.err.println("UFBytes::recvData> nothing available.");
		    return _elem;
		}
	    }
	    _values = new byte[_elem];
	    inpStrm.readFully(_values);
	    return _values.length;
	}
	catch(EOFException eof) {
	    System.err.println("UFBytes::recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFBytes::recvData> "+ioe.toString());
	}
	catch( Exception ex ) {
	    System.err.println("UFBytes::recvData> "+ex.toString());
	}
	return 0;
    }

    // send data values (assume header already sent, or just sending data):

    public int sendData(DataOutputStream outpStrm) {
	try {
	    if (_values != null && _values.length > 0) {
		outpStrm.write(_values);
		return _values.length;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFBytes::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFBytes::sendData> "+ioe.toString());
	}
	catch( Exception ex ) {
	    System.err.println("UFBytes::sendData> "+ex.toString());
	}
	return 0;
    }

//--------------------------------------------------------------------------------------
    //socket versions of recv/send methods:

    public int recvData(Socket soc) {
	try {
	    DataInputStream inpStrm = new DataInputStream(soc.getInputStream());
	    return recvData(inpStrm);
	}
	catch(IOException ioe) {
	    System.err.println("UFBytes.recvData(Socket)> "+ioe.toString());
	    return 0;
	}
    }

    public int sendData(Socket soc) {
	try {
	    DataOutputStream outpStrm = new DataOutputStream(soc.getOutputStream());
	    return sendData(outpStrm);
	}
	catch(IOException ioe) {
	    System.err.println("UFBytes.sendData(Socket)> "+ioe.toString());
	    ioe.printStackTrace();
	    return 0;
	}
	catch(Exception x) {
	    System.err.println("UFBytes.sendData(Socket)> "+x.toString());
	    x.printStackTrace();
	    return(-1);
	}
    }
}
