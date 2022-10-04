package javaUFProtocol;

import java.io.*;
import javax.imageio.stream.*;
import java.nio.ByteOrder;

public class UFunsignShorts extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFunsignShorts.java,v 1.5 2010/03/29 22:01:45 varosi Exp $";

    protected char[] _values = null;    //these are unsigned 16-bits in Java.

    protected boolean _imageStreamWorks = true;

    protected char _min = 65535;
    protected char _max = 0;
    protected int _ixMin = -1;
    protected int _ixMax = -1;

    private void _init() {
	_currentTime();
	_type = MsgTyp._UnsignShorts_;
    }

    public UFunsignShorts() {
	_init();
    }

    public UFunsignShorts(int length) {
	_init();
	_length = length;
    }

    public UFunsignShorts(String name) {
	_init();
	_name = new String(name);
	_length = _minLength();
    }

    public UFunsignShorts(String name, int nelem) {
	_init();
	_name = new String(name);
	_elem = nelem;
	_length = _minLength() + 2*_elem;
    }

    public UFunsignShorts(String name, char[] vals) {
	_init();
	_name = new String(name);
	_elem = vals.length;
	_values = vals;
	_length = _minLength() + 2*vals.length;
    }

    public UFunsignShorts(UFShorts shorts) {
	_init();
	_name = new String(shorts.name());
	_elem = shorts.elements();
	_length = _minLength() + 2*_elem;
	_values = new char[_elem];
	
	short[] sval = shorts.values();

	for(int i = 0; i < _elem; i++)
	    _values[i] = (char)(sval[i] - 32768);
    }

    // all methods declared abstract by UFProtocal can be defined here

    public String description() { return new String("UFunsignShorts"); }
 
    // return size of an element:

    public int valSize() { return 2; }

    public int valSize(int elemIdx) {
	if( _values != null ) {
	    if( elemIdx >= _values.length ) return 0;
	}
	return 2;
    }

    public char[] values() { return _values; }
    public char valData(int index) { return _values[index]; }

    public char maxVal() {
	char max=0;
	if( _max > max ) return _max;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] > max ) max = _values[i];

	_max = max;
	return max;
    }

    public char minVal() {
	char min=65535;
	if( _min < min ) return _min;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] < min ) min = _values[i];

	_min = min;
	return min;
    }

    public void calcMinMax()
    {
	for( int i=0; i<_values.length; i++ ) {
	    char val = _values[i];
	    if( val < _min ) _min = val;
	    if( val > _max ) _max = val;
	}
    }

    public void calcMinMax( boolean Locs )
    {
	for( int i=0; i<_values.length; i++ ) {
	    char val = _values[i];
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

    protected void _copyNameAndVals(String s, char[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new char[_elem];
	_length = _minLength();
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	    _length += 2;
	}
    }

    public void setNameAndVals(String s, char[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new char[vals.length];
	_length = _minLength();
	for (int i=0; i<_elem; i++) {
	    _values[i] = vals[i];
	    _length += 2;
	}
    }
 
    // recv data values (length, type, and header have already been read)
    public int recvData(DataInputStream inps) {
      return recvData(inps, null);
    }

    // recv data values (length, type, and header have already been read)
    public int recvData(DataInputStream inps, ByteOrder byteOrder) {

	if( inps == null ) {
	    System.err.println("UFunsignShorts::recvData> NULL input stream!");
	    return 0;
	}

	if( _elem < 1 ) {
	    System.err.println("UFunsignShorts::recvData> # elements is zero!");
	    return 0;
	}

	try {
	    _values = new char[_elem];

	    if( _imageStreamWorks ) {
		MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(inps);
		if (byteOrder != null) mciis.setByteOrder(byteOrder);
		mciis.readFully( _values, 0, _elem );
		mciis.close();
		return 2*_elem;
	    }
	    else {
		byte[] byteStream = new byte[2*_elem];
		inps.readFully( byteStream );
		int bi = 0;
		//shift and mask each 2 byte group into a short integer:

		for( int i=0; i<_elem; i++ ) {
		    Integer val = new Integer( (( (char)byteStream[bi++] << 8 ) & 0xff00 ) |
					       (  (char)byteStream[bi++]        & 0x00ff ) );
		    _values[i] = (char)val.intValue();
		}

		return byteStream.length;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFunsignShorts::recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFunsignShorts::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFunsignShorts::recvData> "+e.toString());
	}
	return 0;
    }

    // send data values (header already sent):

    public int sendData(DataOutputStream outps)
    {
	if( outps == null ) {
	    System.err.println("UFunsignShorts::sendData> NULL output stream!");
	    return 0;
	}

	if( _values == null || _values.length < 1 ) {
	    System.err.println("UFunsignShorts::sendData> _values array is empty!");
	    return 0;
	}

	try {
	    _elem = _values.length;

	    if( _imageStreamWorks ) {
		MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outps);
		mcios.writeChars( _values, 0, _elem );
		mcios.close();
		return 2*_elem;
	    }
	    else {
		byte[] byteStream = new byte[2*_elem];
		int bi = 0;
		//shift and mask each 16-bit integer into a 2 byte group:
	    
		for( int ie=0; ie < _elem; ie++ ) {
		    char vi = _values[ie];
		    byteStream[bi++] = (byte)( (vi & 0x0000ff00) >> 8 );
		    byteStream[bi++] = (byte)(  vi & 0x000000ff );
		}
		int bcp = outps.size();
		outps.write( byteStream, 0, byteStream.length );
		outps.flush();
		return outps.size() - bcp;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFunsignShorts::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFunsignShorts::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFunsignShorts::sendData> "+e.toString());
	}
	return 0;
    }
}

