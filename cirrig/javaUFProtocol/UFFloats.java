package javaUFProtocol;

import java.io.*;
import javax.imageio.stream.*;

public class UFFloats extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFFloats.java,v 1.12 2010/03/29 21:59:40 varosi Exp $";

    protected float[] _values=null;

    protected float _min = 1.0e38f;
    protected float _max = -1.0e38f;
    protected int _ixMin = -1;
    protected int _ixMax = -1;

    private void _init() {
	_currentTime();
	_type = MsgTyp._Floats_;
    }

    public UFFloats() {
	_init();
    }

    public UFFloats(int length) {
	_init();
	_length = length;
    }

    public UFFloats(String name, int nelem) {
	_init();
	_name = new String(name);
	_elem = nelem;
	_length = _minLength() + 4*_elem;
    }

    public UFFloats(String name, float[] vals) {
	_init();	
	_name = new String(name);
	_elem = vals.length;
	_values = vals;
	_length = _minLength() + 4*vals.length;
    }

    // all methods declared abstract by UFProtocol can be defined here:

    public String description() { return new String("UFFloats"); }
 
    // return size of an element:

    public int valSize() { return 4; }

    public int valSize(int elemIdx) {
	if( _values != null ) {
	    if( elemIdx >= _values.length ) return 0;
	}
	return 4;
    }

    public float[] values() { return _values; }
    public float valData(int index) { return _values[index]; }

    public float maxVal() {
	float max= -1.0e38f;
	if( _max > max ) return _max;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] > max ) max = _values[i];

	_max = max;
	return max;
    }

    public float minVal() {
	float min= 1.0e38f;
	if( _min < min ) return _min;

	for( int i=0; i<_values.length; i++ )
	    if( _values[i] < min ) min = _values[i];

	_min = min;
	return min;
    }

    public void calcMinMax()
    {
	for( int i=0; i<_values.length; i++ ) {
	    float val = _values[i];
	    if( val < _min ) _min = val;
	    if( val > _max ) _max = val;
	}
    }

    public void calcMinMax( boolean Locs )
    {
	for( int i=0; i<_values.length; i++ ) {
	    float val = _values[i];
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

    protected void _copyNameAndVals(String s, float[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_length = _minLength();
	_values = new float[vals.length];
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	    _length += 4;
	}
    }
 
    public void setNameAndVals(String s, float[] vals) {
	_name = new String(s);
	_elem = vals.length;
	_length = _minLength();
	_values = new float[vals.length];
	for( int i=0; i<_elem; i++ ) {
	    _values[i] = vals[i];
	    _length += 4;
	}
    }
 
    // recv data values (length, type, and header have already been read)

    public int recvData(DataInputStream inps) {
	try {
	    _values = new float[_elem];
	    MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(inps);
	    mciis.readFully( _values, 0, _elem );
	    mciis.close();
	    return 4*_elem;
	}
	catch(EOFException eof) {
	    System.err.println("UFFloats::recvData> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFFloats::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFFloats::recvData> "+e.toString());
	}
	return 0;
    }

    // send data values (header already sent):

    public int sendData(DataOutputStream outps) {
	try {
	    if( _values != null && _values.length > 0 ) {
		_elem = _values.length;
		MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outps);
		mcios.writeFloats( _values, 0, _elem );
		mcios.close();
		return 4*_elem;
	    }
	    else {
		System.err.println("UFFloats::sendData> _values array is empty!");
		return 0;
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFFloats::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFFloats::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFFloats::sendData> "+e.toString());
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
		    float pixval = _values[kp];
		    _values[kp++] = _values[kf];
		    _values[kf++] = pixval;
		}
	    }
    }

    public void plusEquals(UFFloats rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] += rhs._values[0];
	else
	    for(int i=0; i<Math.min(this._elem,rhs._elem); i++) 
		this._values[i] += rhs._values[i];
    }

    public void minusEquals(UFFloats rhs) {
	if (rhs._elem == 1)
	    for (int i=0; i<this._elem; i++) 
		this._values[i] -= rhs._values[0];
	else
	    for(int i=0; i<Math.min(this._elem,rhs._elem); i++) 
		this._values[i] -= rhs._values[i];
    }

    public void plusEquals(float rhs) {
	for (int i=0; i<this._elem; i++) this._values[i] += rhs;
    }

    public void minusEquals(float rhs) {
	for (int i=0; i<this._elem; i++) this._values[i] -= rhs;
    }

    public int sum(UFFloats Left, UFFloats Right) {
	if( Left._values == null || 
	    Right._values == null ) return 0;

	float[] viL = Left._values;
	float[] viR = Right._values;
	int elem = Math.min(Left._elem, Right._elem);

	if( this._values == null || _elem <= 0 ) this._values = new float[elem];

	for (int i=0; i<elem; i++) _values[i] = viL[i] + viR[i];

	return elem;
    }

    public int diff(UFFloats Left, UFFloats Right) {
	if( Left._values == null || 
	    Right._values == null ) return 0;

	float[] viL = Left._values;
	float[] viR = Right._values;
	int elem = Math.min(Left._elem, Right._elem);

	if( this._values == null || _elem <= 0 ) this._values = new float[elem];

	for (int i=0; i<elem; i++) _values[i] = viL[i] - viR[i];

	return elem;
    }
}
