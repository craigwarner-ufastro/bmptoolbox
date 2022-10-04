package javaMMTLib;

import java.util.Vector;
import java.io.*;
import javaUFProtocol.*;

public class UFRecord extends UFStrings {
    public static final
        String rcsID = "$Name:  $ $Id: UFRecord.java,v 1.6 2016/06/08 17:14:20 warner Exp $";

    public static final int NELEM = 9;
    public static final int TYPE_STRING = 0;
    public static final int TYPE_INT = 1;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_LONG = 3;
 
    private void _init() {
        _currentTime();
        _type = MsgTyp._Strings_;
    }

    /* Static helper method to determine if object is a UFRecord */
    public static boolean isUFRecord(UFProtocol ufp) {
      if (ufp instanceof UFStrings && ufp.elements() == NELEM) {
	if (((UFStrings)ufp).stringAt(4).equals("UFRecord")) return true;
      }
      return false; 
    }

    /* UFRecord is a special UFStrings object with an 9-element string array.
     * The array contains the string representation of the value,
     * The native datatype, the health status, and an optional message.
     * It of course also contains a timestamp.  It also includes methods
     * to cleanly update these values and fetch native datatypes.
     */

    public UFRecord(String name, int type, String value, String fitsKey) {
      _init();
      _name = name;
      _values = new String[NELEM];
      _values[0] = value; // current value
      // _values[1] = datatype
      if (type == TYPE_INT) {
	_values[1] = "int";
      } else if (type == TYPE_FLOAT) {
	_values[1] = "float";
      } else if (type == TYPE_LONG) {
	_values[1] = "long";
      } else _values[1] = "string";
      _values[2] = "GOOD"; //health
      _values[3] = ""; //message
      _values[4] = "UFRecord"; //UFRecord
      _values[5] = "0"; //time of update
      _values[6] = ""; //previous value
      _values[7] = "0"; //time of previous value;
      _values[8] = fitsKey; //FITS keyword
      _elem = NELEM;
      int count = 0;
      for( int i = 0; i < _elem; i++ ) {
	if( _values[i] != null ) count += _values[i].length();
	count += 4;  //add 4 for each send/recv of integer length of string
      }
      _length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public UFRecord(String name, int type, String value) {
      this(name, type, value, "");
    }

    public UFRecord(String name, String value, String fitsKey) {
      this(name, TYPE_STRING, value, fitsKey);
    }

    public UFRecord(String name, String value) {
      this(name, TYPE_STRING, value, "");
    }

    public UFRecord(UFProtocol ufp) {
      _init();
      _name = ufp.name();
      _values = new String[NELEM];
      _elem = NELEM;
      if (ufp instanceof UFStrings && ufp.elements() == NELEM) {
	for (int j = 0; j < NELEM; j++) {
	  _values[j] = ((UFStrings)ufp).stringAt(j);
	  if (_values[j] == null) _values[j] = "";
	}
      } else {
	System.err.println("UFRecord:> This protocol object does not seem to be a UFRecord.  Object value will be null.");
	_values[0] = "null";
	_values[1] = "string";
	_values[2] = "BAD";
	_values[3] = "misformatted data";
	_values[4] = "UFRecord";
        _values[5] = "0";
        _values[6] = "";
        _values[7] = "0";
	_values[8] = "";
      } 
      int count = 0;
      for( int i = 0; i < _elem; i++ ) {
        if( _values[i] != null ) count += _values[i].length();
        count += 4;  //add 4 for each send/recv of integer length of string
      }
      _length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public String description() { return new String("UFRecord"); }

    private void _updateLength() {
      int count = 0;
      for( int i = 0; i < _elem; i++ ) {
        if( _values[i] != null ) count += _values[i].length();
        count += 4;  //add 4 for each send/recv of integer length of string
      }
      _length = _minLength() + count; //recall this is the send/recv number of bytes.
    }

    public boolean isNew() {
      if (_values[5].equals("0")) return true;
      return false;
    }

    public long getLastUpdate() {
      return Long.parseLong(_values[5]);
    }

    public boolean isNewer(UFRecord otherRec) {
      if (getLastUpdate() > otherRec.getLastUpdate()) return true;
      return false;
    }

    public String getValue() { return _values[0]; }

    public int getInt() {
      int retVal = Integer.MIN_VALUE; 
      if (_values[1].equals("int")) {
	try {
	  retVal = Integer.parseInt(_values[0]);
	} catch (NumberFormatException e) { }
      }
      return retVal;
    }

    public long getLong() {
      long retVal = (long)Integer.MIN_VALUE;
      if (_values[1].equals("long")) {
        try {
          retVal = Long.parseLong(_values[0]);
        } catch (NumberFormatException e) { }
      }
      return retVal;
    }

    public float getFloat() {
      float retVal = (float)Integer.MIN_VALUE;
      if (_values[1].equals("float")) {
        try {
          retVal = Float.parseFloat(_values[0]);
        } catch (NumberFormatException e) { }
      }
      return retVal;
    }

    public String getType() { return _values[1]; }

    public int getTypeEnum() {
      if (_values[1].equals("int")) return TYPE_INT;
      if (_values[1].equals("float")) return TYPE_FLOAT;
      if (_values[1].equals("long")) return TYPE_LONG;
      return TYPE_STRING;
    }

    public String getHealth() { return _values[2]; }

    public String getMess() { return _values[3]; }

    public void updateValue(String value, String health, String mess) {
      if (_values[0].equals(value) && _values[2].equals(health) && _values[3].equals(mess)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = value;
      _values[2] = health;
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }

    public void updateValue(String value, String health) {
      if (_values[0].equals(value) && _values[2].equals(health)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = value;
      _values[2] = health;
      _currentTime();
      _updateLength();
    }

    public void updateValue(String value) {
      if (_values[0].equals(value)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = value;
      _currentTime();
      _updateLength();
    }

    public void updateValue(int value, String health, String mess) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health) && _values[3].equals(mess)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }

    public void updateValue(int value, String health) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _currentTime();
      _updateLength();
    }

    public void updateValue(int value) {
      if (_values[0].equals(String.valueOf(value))) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _currentTime();
      _updateLength();
    }

    public void updateValue(long value, String health, String mess) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health) && _values[3].equals(mess)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }

    public void updateValue(long value, String health) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _currentTime();
      _updateLength();
    }

    public void updateValue(long value) {
      if (_values[0].equals(String.valueOf(value))) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _currentTime();
      _updateLength();
    }

    public void updateValue(float value, String health, String mess) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health) && _values[3].equals(mess)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }

    public void updateValue(float value, String health) {
      if (_values[0].equals(String.valueOf(value)) && _values[2].equals(health)) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _values[2] = health;
      _currentTime();
      _updateLength();
    }

    public void updateValue(float value) {
      if (_values[0].equals(String.valueOf(value))) return; //no update needed
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[0] = ""+value;
      _currentTime();
      _updateLength();
    }

    public void updateHealth(String health, String mess) {
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[2] = health;
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }

    public void updateHealth(String health) {
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[2] = health;
      _currentTime();
      _updateLength();
    }

    public void updateMess(String mess) {
      _values[6] = _values[0]; // copy previous value
      _values[7] = _values[5]; // copy timestamp
      _values[5] = ""+System.currentTimeMillis(); //new timestamp
      _values[3] = mess;
      _currentTime();
      _updateLength();
    }


    public void updateFitsKey(String fitsKey) {
      _values[8] = fitsKey; //update fits key
    }

    public boolean hasFitsKey() {
      if (_values[8].length() > 1) return true;
      return false;
    }

    public String getFitsKey() {
      /* Construct a FITS keyword */
      if (!hasFitsKey()) return null;
      String key = _values[8];
      String value = getValue();
      while (key.length() < 8) key += " ";
      
      if (getType().equals("string")) {
	value = "'"+value;
	while(value.length() < 9) value += " ";
	value += "'";
	while(value.length() < 20) value += " ";
      } else {
	while (value.length() < 20) value = " "+value;
      }

      String comment = " /"+getMess();
      String retVal = key+"= "+value+comment;
      if (retVal.length() > 80) retVal = retVal.substring(0,80);
      while (retVal.length() < 80) retVal += " ";
      return retVal;
    }

}
