package javaUFProtocol;

import java.io.*;
import java.text.*;
import java.util.*; 

public class UFTimeStamp extends UFProtocol
{
    public static final
	String rcsID = "$Name:  $ $Id: UFTimeStamp.java,v 1.8 2010/03/29 22:04:52 varosi Exp $";

    // UFTimeStamp has no _values attribute.

    private void _init() {
	_currentTime();
	_type = MsgTyp._TimeStamp_;
    }

    public UFTimeStamp() {
	_init();
    }

    public UFTimeStamp(int length) {
	_init();
	_length = length;
    }

    public UFTimeStamp(String name) {
	_init();
	_name = new String(name);
	_length = _minLength();
    }

    // all methods declared abstract by UFProtocol can be defined here:

    public String description() { return new String("UFTimeStamp"); }
 
    // return size of one of the elements in values (in this object there are none):

    public int valSize() { return 0; }
    public int valSize(int elemIdx) { return 0; }
 
    // since TimeStamp has no data, just return zero:
    public int recvData(DataInputStream inpStrm) { return 0; }
    public int sendData(DataOutputStream outpStrm) { return 0; }

    protected void _currentTime() { 
	SimpleDateFormat df = new SimpleDateFormat("yyyy:DDD:HH:mm:ss.SSS");
	_timestamp = df.format(new Date()) + "000"; // microseconds == 000
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("description = " + description() + " | ");
	sb.append("_length = " + _length + " | ");
	sb.append("_minlength() = " + _minLength() + " | ");
	sb.append("_type = " + _type + " | ");
	sb.append("_timestamp = " + _timestamp + "\n");
	sb.append("_elem = " + _elem + " | ");
	sb.append("_name = " +  _name + " | ");
	return new String(sb);
    }
}

