package javaUFProtocol;

import java.io.*;
import javax.imageio.stream.*;
import java.nio.ByteOrder;

public class UFInts extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFInts.java,v 1.30 2014/12/11 15:12:45 warner Exp $";

    protected int[] _values=null;

    protected boolean _imageStreamWorks = true;

    protected int _min = Integer.MAX_VALUE;
    protected int _max = Integer.MIN_VALUE;
    protected int _ixMin = -1;
    protected int _ixMax = -1;

    private void _init() {
	_currentTime();
	_type = MsgTyp._Ints_;
    }

    public UFInts() {
	_init();
    }

    public UFInts(int length) {
	_init();
	_length = length;
    }

    public UFInts(String name) {
	_init();
	_name = new String(name);
	_length = _minLength();
    }

    public UFInts(String name, int nelem) {
	_init();
	_name = new String(name);
	_elem = nelem;
	_length = _minLength() + 4*_elem;
    }

    public UFInts(String name, int[] vals) {
	_init();
	_name = new String(name);
	_elem = vals.length;
	_values = vals;
	_length = _minLength() + 4*_elem;
    }

    // all methods declared abstract by UFProtocal can be defined here:

    public String description() { return new String("UFInts"); }
 
    // return size of an element:

    public int valSize() { return 4; }

    public int valSize(int elemIdx) {
	if( _values != null ) {
	    if( elemIdx >= _values.length ) return 0;
	}
	return 4;
    }

    public int[] values() { return _values; }
    public int valData(int index) { return _values[index]; }

    public int maxVal() {
	int max = Integer.MIN_VALUE;
	if( _max > max ) return _max;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] > max ) max = _values[i];

	_max = max;
	return max;
    }

    public int minVal() {
	int min = Integer.MAX_VALUE;
	if( _min < min ) return _min;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] < min ) min = _values[i];

	_min = min;
	return min;
    }

    public void calcMinMax()
    {
	for( int i=0; i<_values.length; i++ ) {
	    int val = _values[i];
	    if( val < _min ) _min = val;
	    if( val > _max ) _max = val;
	}
    }

    public void calcMinMax( boolean Locs )
    {
	for( int i=0; i<_values.length; i++ ) {
	    int val = _values[i];
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

    public void setNameAndVals(String s, int[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_length = _minLength();
	_values = new int[vals.length];
	for( int i=0; i<_elem; i++ ) _values[i] = vals[i];
	_length += (4*_elem);
    }

    // recv data values (length, type, and header have already been read)
    public int recvData(DataInputStream inps) {
      return recvData(inps, null);
    }

    // recv data values (length, type, and header have already been read)
    public int recvData(DataInputStream inps, ByteOrder byteOrder)
    {
	if( inps == null ) {
	    System.err.println("UFInts::recvData> NULL input stream!");
	    return 0;
	}

	if( _elem < 1 ) {
	    System.err.println("UFInts::recvData> # elements is zero!");
	    return 0;
	}

	try {
	    _values = new int[_elem];

	    if( _imageStreamWorks ) {
		MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(inps);
                if (byteOrder != null) mciis.setByteOrder(byteOrder);
		mciis.readFully( _values, 0, _elem );
		mciis.close();
		return 4*_elem;
	    }
	    else {
		byte[] byteStream = new byte[4*_elem];
		inps.readFully( byteStream );
		int bi = 0;
		//shift and mask each 4 byte group into a 32-bit integer:

		for( int i=0; i<_elem; i++ ) {
		    _values[i] =
			(( (int)byteStream[bi++] << 24 ) & 0xff000000 ) |
			(( (int)byteStream[bi++] << 16 ) & 0x00ff0000 ) |
			(( (int)byteStream[bi++] <<  8 ) & 0x0000ff00 ) |
			(  (int)byteStream[bi++]         & 0x000000ff );
		}

		return byteStream.length;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFInts::recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFInts::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFInts::recvData> "+e.toString());
	}

	return 0;
    }

    // send data values (header already sent):

    public int sendData(DataOutputStream outps)
    {
	if( outps == null ) {
	    System.err.println("UFInts::sendData> NULL output stream!");
	    return 0;
	}

	if( _values == null || _values.length < 1 ) {
	    System.err.println("UFInts::sendData> _values array is empty!");
	    return 0;
	}

	try {
	    _elem = _values.length;

	    if( _imageStreamWorks ) {
		MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outps);
		mcios.writeInts( _values, 0, _elem );
		mcios.close();
		return 4*_elem;
	    }
	    else {
		byte[] byteStream = new byte[4*_elem];
		int bi = 0;
		//shift and mask each 32-bit integer into a 4 byte group:
	    
		for( int ie=0; ie < _elem; ie++ ) {
		    int vi = _values[ie];
		    byteStream[bi++] = (byte)( (vi & 0xff000000) >> 24 );
		    byteStream[bi++] = (byte)( (vi & 0x00ff0000) >> 16 );
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
	    System.err.println("UFInts::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFInts::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFInts::sendData> "+e.toString());
	}

	return 0;
    }

    // additional methods (beyond base class's):
    // flip the image frame by reversing the order of rows:

    public void flipFrame( UFFrameConfig fc )
    {
	int nrow = fc.height;
	int ncol = fc.width;

	for( int irow=0; irow < (nrow/2); irow++ )
	    {
		int iflip = nrow - irow - 1;
		int kp = irow * ncol;
		int kf = iflip * ncol;

		for( int k=0; k<ncol; k++ ) {
		    int pixval = _values[kp];
		    _values[kp++] = _values[kf];
		    _values[kf++] = pixval;
		}
	    }
    }

    public void plusEquals(UFInts rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] += rhs._values[0];
	else
	    for(int i=0; i<Math.min( this._elem, rhs._elem ); i++) 
		this._values[i] += rhs._values[i];
    }

    public void minusEquals(UFInts rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] -= rhs._values[0];
	else
	    for(int i=0; i<Math.min( this._elem, rhs._elem ); i++) 
		this._values[i] -= rhs._values[i];
    }

    public void multiplyEquals(UFInts rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] *= rhs._values[0];
	else
	    for(int i=0; i<Math.min( this._elem, rhs._elem ); i++) 
		this._values[i] *= rhs._values[i];
    }

    public void divideEquals(UFInts rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] /= rhs._values[0];
	else
	    for(int i=0; i<Math.min( this._elem, rhs._elem ); i++) 
		this._values[i] /= rhs._values[i];
    }

    public void plusEquals(int rhs) {
	for (int i=0; i<this._elem; i++) this._values[i] += rhs;
    }

    public void minusEquals(int rhs) {
	for (int i=0; i<this._elem; i++) this._values[i] -= rhs;
    }

    public void multiplyEquals(int rhs) {
	for (int i=0; i<this._elem; i++) this._values[i] *= rhs;
    }

    public void divideEquals(int rhs) {
	if( rhs != 0 )
	    for (int i=0; i<this._elem; i++) this._values[i] /= rhs;
	else
	    System.err.println("UFInts::divideEquals> divide by zero not allowed.");
    }

    public void plusEqualsWithThreshold(int rhs, int threshold, int behavior) {
	//add rhs to all values matching threshold and behavoir
	//behavior = -1 => all values < threshold
	//behavior = 0 => all values == threshold
	//behavior = 1 => all valus > threshold
	if (behavior == -1) {
	    for (int i = 0; i<this._elem; i++) if (this._values[i] < threshold) this._values[i] += rhs; 
	} else if (behavior == 0) {
            for (int i = 0; i<this._elem; i++) if (this._values[i] == threshold) this._values[i] += rhs;
	} else if (behavior == 1) {
            for (int i = 0; i<this._elem; i++) if (this._values[i] > threshold) this._values[i] += rhs;
	} else {
	    System.err.println("UFInts::plusEqualsWithThreshold> invalid beahvior "+behavior);
	}
    }

    public int sum(UFInts Left, UFInts Right) {
	if( Left._values == null || 
	    Right._values == null ) return 0;

	int [] viL = Left._values;
	int [] viR = Right._values;
	int elem = Math.min(Left._elem, Right._elem);

	if( this._values == null || _elem <= 0 ) this._values = new int[elem];

	for (int i=0; i<elem; i++) _values[i] = viL[i] + viR[i];

	return elem;
    }

    public int diff(UFInts Left, UFInts Right) {
	if( Left._values == null || 
	    Right._values == null ) return 0;

	int [] viL = Left._values;
	int [] viR = Right._values;
	int elem = Math.min(Left._elem, Right._elem);

	if( this._values == null || _elem <= 0 ) this._values = new int[elem];

	for (int i=0; i<elem; i++) _values[i] = viL[i] - viR[i];

	return elem;
    }
}
