package javaUFProtocol;

import java.io.*;
import javax.imageio.stream.*;
import java.nio.ByteOrder;

public class UFShorts extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFShorts.java,v 1.15 2010/03/29 22:01:15 varosi Exp $";

    protected short[] _values=null;

    protected boolean _imageStreamWorks = true;

    protected short _min = 32767;
    protected short _max = -32768;
    protected int _ixMin = -1;
    protected int _ixMax = -1;

    private void _init() {
	_currentTime();
	_type = MsgTyp._Shorts_;
    }

    public UFShorts() {
	_init();
    }

    public UFShorts(int length) {
	_init();
	_length = length;
    }

    public UFShorts(String name) {
	_init();
	_name = new String(name);
	_length = _minLength();
    }

    public UFShorts(String name, int nelem) {
	_init();
	_name = new String(name);
	_elem = nelem;
	_length = _minLength() + 2*_elem;
    }

    public UFShorts(String name, short[] vals) {
	_init();
	_name = new String(name);
	_elem = vals.length;
	_values = vals;
	_length = _minLength() + 2*vals.length;
    }

    // all methods declared abstract by UFProtocal can be defined here

    public String description() { return new String("UFShorts"); }
 
    // return size of an element:

    public int valSize() { return 2; }

    public int valSize(int elemIdx) {
	if( _values != null ) {
	    if( elemIdx >= _values.length ) return 0;
	}
	return 2;
    }

    public short[] values() { return _values; }
    public short valData(int index) { return _values[index]; }

    public short maxVal() {
	short max=-32768;
	if( _max > max ) return _max;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] > max ) max = _values[i];

	_max = max;
	return max;
    }

    public short minVal() {
	short min=32767;
	if( _min < min ) return _min;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] < min ) min = _values[i];

	_min = min;
	return min;
    }

    public void calcMinMax()
    {
	for( int i=0; i<_values.length; i++ ) {
	    short val = _values[i];
	    if( val < _min ) _min = val;
	    if( val > _max ) _max = val;
	}
    }

    public void calcMinMax( boolean Locs )
    {
	for( int i=0; i<_values.length; i++ ) {
	    short val = _values[i];
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

    protected void _copyNameAndVals(String s, short[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new short[_elem];
	_length = _minLength();
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	    _length += 2;
	}
    }

    public void setNameAndVals(String s, short[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_values = new short[vals.length];
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
	    System.err.println("UFShorts::recvData> NULL input stream!");
	    return 0;
	}

	if( _elem < 1 ) {
	    System.err.println("UFShorts::recvData> # elements is zero!");
	    return 0;
	}

	try {
	    _values = new short[_elem];

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
		    Integer val = new Integer( (( (short)byteStream[bi++] << 8 ) & 0xff00 ) |
					       (  (short)byteStream[bi++]        & 0x00ff ) );
		    _values[i] = val.shortValue();
		}

		return byteStream.length;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFShorts::recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFShorts::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFShorts::recvData> "+e.toString());
	}
	return 0;
    }

    // send data values (header already sent):

    public int sendData(DataOutputStream outps)
    {
	if( outps == null ) {
	    System.err.println("UFShorts::sendData> NULL output stream!");
	    return 0;
	}

	if( _values == null || _values.length < 1 ) {
	    System.err.println("UFShorts::sendData> _values array is empty!");
	    return 0;
	}

	try {
	    _elem = _values.length;

	    if( _imageStreamWorks ) {
		MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outps);
		mcios.writeShorts( _values, 0, _elem );
		mcios.close();
		return 2*_elem;
	    }
	    else {
		byte[] byteStream = new byte[2*_elem];
		int bi = 0;
		//shift and mask each 16-bit integer into a 2 byte group:
	    
		for( int ie=0; ie < _elem; ie++ ) {
		    short vi = _values[ie];
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
	    System.err.println("UFShorts::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFShorts::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFShorts::sendData> "+e.toString());
	}
	return 0;
    }
}

