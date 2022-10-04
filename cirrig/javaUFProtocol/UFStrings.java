package javaUFProtocol;

import java.util.Vector;
import java.io.*;

public class UFStrings extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFStrings.java,v 1.15 2010/03/29 22:03:15 varosi Exp $";

    // additional attributes & methods (beyond base class's):
    protected String[] _values=null;
    protected static int _maxStrings = 65000;

    private void _init() {
	_currentTime();
	_type = MsgTyp._Strings_;
    }

    public UFStrings() {
	_init();
    }

    public UFStrings(int length) {
	_init();
	_length=length;
    }

    public UFStrings(String name) {
	_init();
	_name = name;
	_length= _minLength();
    }

    public UFStrings(String inName, String inValue) {
	_init();
	_values = new String[1];
	_values[0] = inValue;
	_elem = 1;
	_name = inName;
	int count = 4;  // 4 for send/recv of the integer giving length of string
	if (inValue != null) count += inValue.length();
	_length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public UFStrings(String inName, float inValue) {
	_init();
	_values = new String[1];
	_values[0] = inValue + "";
	_elem = 1;
	_name = inName;
	int count = 4;  // 4 for send/recv of the integer giving length of string
	count += _values[0].length();
	_length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public UFStrings(String inName, String[] inValues) {
	_init();
	_values = inValues;
	_elem = inValues.length;
	_name = inName;
	int count = 0;
	for( int i = 0; i < _elem; i++ ) {
	    if( _values[i] != null ) count += _values[i].length();
	    count += 4;  //add 4 for each send/recv of integer length of string
	}
	_length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public UFStrings( String inName, Vector< String > inValues ) {
	_init();
	_name = inName;
	_elem = inValues.size();
	if(_elem > 0) {
	    _values = new String[_elem];
	    inValues.copyInto( _values );
	}
	int count = 0;
	for( int i = 0; i < _elem; i++ ) {
	    if( _values[i] != null ) count += _values[i].length();
	    count += 4;  //add 4 for each send/recv of integer length of string
	}
	_length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    // all methods declared abstract by UFProtocol should be defined here:

    public String description() { return new String("UFStrings"); }
 
    // return size of an element of a string (char):

    public int valSize() { return 1; }

    // return size of a particular element (string):

    public int valSize(int elemIdx) {
	if( _values == null ) return 0;
	if( elemIdx >= _values.length )
	    return 0;
	else
	    return _values[elemIdx].length();
    }

    // return number of Strings in object
    public int numVals() {
	if( _values == null ) return 0;
	return _values.length;
    }

    // return String at index passed in
    public String valData(int elemndx) {
	return _values[elemndx];
    }

    // return String at index:
    public String stringAt(int indx) {
	return _values[indx];
    }

    protected void _copyNameAndVals(String s, String[] vals) {
	_name = new String(s);
	_values = new String[vals.length];
	_elem = vals.length;
	_length = _minLength();
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = new String(vals[i]);
	    _length += 4; // each string length is recorded in protocol
	    _length += _values[i].length();
	}
    }

    public void setNameAndVals(String s, String[] vals) {
	_name = new String(s);
	_values = new String[vals.length];
	_elem = vals.length;
	_length = _minLength();
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	    _length += 4; // each string length is recorded in protocol
	    _length += _values[i].length();
	}
    }

    public int recvData(DataInputStream inp) {
	// read data values (length, type, and header have already been read)
	int retval=0;
	if( _elem > _maxStrings ) {
	    System.err.println("UFStrings.recvData> # of strings: " + _elem
			       + " > max allowed: " + _maxStrings);
	    return(-1);
	}
	try {
	    _values = new String[_elem];
	    int maxStrLen = _maxDataBytes/_elem;

	    for( int elem=0; elem<_elem; elem++ ) {
		int valLen = inp.readInt();
		retval += 4;
		if( valLen > maxStrLen ) {
		    System.err.println("UFStrings.recvData> string ("+elem+") Length: " + valLen
				       + " > max allowed: " + maxStrLen);
		    return(-2);
		}
		byte[] valbuf = new byte[valLen];
		if( valLen > 0 ) {
		    inp.readFully(valbuf, 0, valLen);
		    _values[elem] = new String(valbuf);
		    retval += valLen;
		}
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFStrings.recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFStrings.recvData> "+ioe.toString());
	}
	catch( Exception ex ) {
	    System.err.println("UFStrings.recvData> "+ ex.toString());
	}
	return retval;
    }

    public int sendData(DataOutputStream out) {
	// send data values (length, type, and header have already been sent)
	int retval=0;
	int slen=0;
	try {
	    for( int elem=0; elem<_elem; elem++ ) {
		slen = _values[elem].length();
		out.writeInt(slen);
		retval += 4;
		if( slen > 0 ) {
		    out.writeBytes(_values[elem]);
		    retval += slen; 
		}
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFStrings::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFStrings::sendData> "+ioe.toString());
	}
	catch( Exception ex ) {
	    System.err.println("UFStrings::sendData> "+ ex.toString());
	}
	return retval;
    }

    public String toString() { return toString( false ); }

    public String toString( boolean verbose )
    {
	StringBuffer sb = new StringBuffer();

	if( verbose ) {
	    sb.append("_length = " + _length + " | ");
	    sb.append("_minlength() = " + _minLength() + " | ");
	    sb.append("_type = " + _type + " | ");
	    sb.append("_elem = " + _elem + " | ");
	    sb.append("_timestamp = " + _timestamp + "\n");
	    sb.append("_name = " + _name + " | ");
	}
	else sb.append("_timestamp = " + _timestamp.substring(0,20) + " | " + _name + " | ");

        sb.append("_values =\n");
	if( _values != null )
	    for (int i = 0; i < _values.length; i++) sb.append(i + ">" + _values[i] + "\n");

	return new String(sb);
    }
}
