package javaUFProtocol;

//Title:        UFProtocol.java
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-7
//Authors:      David Hon, David Rashkin, Frank Varosi
//Company:      University of Florida, Dept. of Astronomy.
//Description:  Define basic abstract class for UFProtocol objects.

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteOrder;
//========================================================================================================

public abstract class UFProtocol extends Object
{    
    final String rcsID = "$Name:  $ $Id: UFProtocol.java,v 1.33 2019/01/06 09:07:49 varosi Exp $";

    // _MinLength_ should be equal to _minLength() for empty _name string:
    // sizeof(int _length) + sizeof(int _type) + sizeof(int _elem) + ... 
    // ... + _timestamp.length() + sizeof(int nameLen) = 4 + 4 + 4 + 2 + 2 + 4 + 24 + 4 = 48
    public static final int _MinLength_ = 48;
    public static final int _maxNameLen =  84;
    public static final int _maxDataBytes = 256*1024*1024; // 256 MB.
    public static final int _MaxLength_ = _MinLength_ + _maxNameLen + _maxDataBytes; 
    
    // All protocol message packets have following protected attributes,
    // in following sequence, followed by an array of Data Values (if _elem > 0) :

    // total length of message (byte count)
    protected int _length = _MinLength_;
    
    // type must be one enumation MsgTyp
    protected int _type = 0;

    // number of elements (values): _elem == 0 for Timestamp, _elem >= 1 for others
    protected int _elem = 0;
    
    // Sequence count allows grouping of multiple protocol objects,
    //  but ***NOTE*** that these are sent/recvd as unsigned shorts (2 bytes each):
    protected int _seqCnt = 1; 
    protected int _seqTot = 1; 

    // duration of data in seconds (optional)
    protected float _duration = 0;

    // timesamp: "yyyy:ddd:hh:mm:ss.uuuuuu" format must be a 24 char string
    protected String _timestamp="yyyy:ddd:hh:mm:ss.uuuuuu";
    public final int _tsLen = 24;

    // optional name string (note that in C++ code _name is currently limited to 83 bytes).
    protected String _name="";
    private static boolean _verbose = false;
//========================================================================================================

    // default ctor needed for containers and "virtual creation"
    public UFProtocol() { }
    
    protected UFProtocol(String name, int elem) {
	_elem = elem;
	_name = new String(name);
	_length = _minLength();
    }
//-----------------------------------------------------------------------------------------------
    // run-time typing for client side:
    // since enum's are not part of the java language, use inner classes

    public static class MsgTyp {
	public static final int _MsgError_=-1;
	public static final int _TimeStamp_=0;
	public static final int _Strings_=1;
	public static final int _Bytes_=2;
        public static final int _Shorts_=3;
        public static final int _UnsignShorts_=-3;
	public static final int _Ints_=4;
	public static final int _Floats_=5;
	public static final int _ImageConfig_=7;
        public static final int _ObsConfig_=8;
	public static final int _FrameConfig_=9;
	public static final int _ByteFrames_=11;
	public static final int _IntFrames_=13;
	public static final int _FloatFrames_=14;
    }
//-----------------------------------------------------------------------------------------------    
    // Following abstract methods must be defined in each type of UFProtocol child class
    // to recv/send the data values of that object (except for UFTimeStamp which has no data).
    // Invoked by methods recvFrom() and sendTo() below.

    public abstract int recvData(DataInputStream inpStrm);

    public int recvData(DataInputStream inpStrm, ByteOrder byteOrder) {
	//Only needs to be implemented in Unsigned Shorts, Shorts, Ints
	return recvData(inpStrm);
    }

    public abstract int sendData(DataOutputStream outpStrm);

    protected void _mapValues() {}  // this is optional, overriden in class UFObsConfig

    // return size of the element's value (not it's name!)
    // either string length, or sizeof(float), sizeof(frame):
    public abstract int valSize(); 
    public abstract int valSize(int elemIdx); 
//-----------------------------------------------------------------------------------------------    
    // _length must be >= _minLength, which is size of header for send/recv.

    protected int _minLength() {
	int minlength = 5*4 + _tsLen + 4 + _name.length();
	return minlength;
    }
    
    // fetch type
    public int typeId() { return _type; }
    // fetch description
    public abstract String description();
    // fetch name
    public String name() { return _name; };
    // fetch timestamp
    public String timeStamp() { return _timestamp; };
    // fetch element count
    public int elements() { return _elem; }
    // total length of the message:
    public int length() { return _length; }

    public int seqCnt() { return _seqCnt; }
    public int seqTot() { return _seqTot; }

    public void setSeq( int seqCnt, int seqTot ) { _seqCnt = seqCnt; _seqTot = seqTot; }
    public void verbose( boolean verbosity ) { _verbose = verbosity; }

    public String rename(String newname) {
	String orig = _name;
	_length -= _name.length();
	_name = new String(newname);
	_length += _name.length();
	return orig;
    }
    
    // can support bundling a bunch of messages into one send/recv?:
    public static int sizeOf(Vector v) {
	int retval=0;
	for( Enumeration e = v.elements(); e.hasMoreElements(); ) {
	    retval += ((UFProtocol)e).length();
	}
	return retval;
    }
//-----------------------------------------------------------------------------------------------    
    //object used to recv first two elements of header:

    public static class LengthAndType {
	int length;
	int type;
	LengthAndType() { length = type = 0; }
	LengthAndType( int len, int typ ) { length = len; type = typ; }
    }
//-----------------------------------------------------------------------------------------------    
    // recv length and type from DataInputStream
    // (then used for both socket and file input):

    public static UFProtocol.LengthAndType recvLengthAndType(DataInputStream inpStrm)
    {
	try { //try reading input stream first to see if exceptions trigger, then returning null...

	    int len = inpStrm.readInt();
	    if( len < _MinLength_ ) {
		System.err.println("UFProtocol.recvLengthAndType> ERR: Length ("+len+") < min.");
		return null;
	    }
	    else if( len > _MaxLength_ ) {
		System.err.println("UFProtocol.recvLengthAndType> ERR: Length ("+len+") > max.");
		return null;
	    }

	    int typ = inpStrm.readInt();
	    if( Math.abs( typ ) > MsgTyp._FrameConfig_ ) {
		System.err.println("UFProtocol.recvLengthAndType> ERR: Type ("+typ+") invalid.");
		return null;
	    }

	    UFProtocol.LengthAndType LTobj = new UFProtocol.LengthAndType( len, typ );
	    return LTobj;
	}
	catch(EOFException eof) {
	    System.err.println("UFProtocol.recvLengthAndType> " +eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFProtocol.recvLengthAndType> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFProtocol.recvLengthAndType> " +e.toString());
	}
	return null;
    }
//-----------------------------------------------------------------------------------------------        
    // read length and type from file descriptor:

    public static UFProtocol.LengthAndType readLengthAndType(File f)
    {
	try {
	    DataInputStream inpStrm = new DataInputStream(new FileInputStream(f));
	    return recvLengthAndType(inpStrm);
	} 
	catch(IOException ioe) {
	    System.err.println("UFProtocol.readLengthAndType(file)> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFProtocol.readLengthAndType(file)> "+e.toString());
	}
	return null;
    }
//-----------------------------------------------------------------------------------------------    
    // recv length and type from socket:

    public static UFProtocol.LengthAndType recvLengthAndType(Socket soc)
    {
	try {
	    DataInputStream inpStrm = new DataInputStream(soc.getInputStream());
	    return recvLengthAndType(inpStrm);
	}
	catch(SocketException sex) {
	    System.err.println("UFProtocol.recvLengthAndType(sock)> "+sex.toString());
	    if(_verbose) sex.printStackTrace();
	}
	catch(IOException ioe) {
	    if(_verbose) System.err.println("UFProtocol.recvLengthAndType(sock)> "+ioe.toString());
	}
	catch( Exception e ) {
	    if(_verbose) System.err.println("UFProtocol.recvLengthAndType(sock)> "+e.toString());
	}
	return null;
    }
//-----------------------------------------------------------------------------------------------
    // Creates a new instance of UFProtocol object on heap.
    // Note that _length is defined by specific object ctors in switch statement below.
    // must have knowledge of all possible descendents in hierarchy:

    protected static UFProtocol _create( UFProtocol.LengthAndType LTobj )
    {
	if( LTobj == null ) return null;
	UFProtocol ufpObj=null;

	switch(LTobj.type)
	    {
	    case MsgTyp._Strings_:	ufpObj = new UFStrings(LTobj.length);
		break;
	    
	    case MsgTyp._Bytes_:	ufpObj = new UFBytes(LTobj.length);
		break;
	    
	    case MsgTyp._Shorts_:	ufpObj = new UFShorts(LTobj.length);
		break;
	    
	    case MsgTyp._UnsignShorts_:	ufpObj = new UFunsignShorts(LTobj.length);
		break;
	    
	    case MsgTyp._Ints_:		ufpObj = new UFInts(LTobj.length);
		break;
	    
	    case MsgTyp._Floats_:	ufpObj = new UFFloats(LTobj.length);
		break;

	    case MsgTyp._ImageConfig_:	ufpObj = new UFImageConfig(LTobj.length);
		break;

	    case MsgTyp._FrameConfig_:	ufpObj = new UFFrameConfig(LTobj.length);
		break;

	    case MsgTyp._ObsConfig_:	ufpObj = new UFObsConfig(LTobj.length);
		break;    

	    case MsgTyp._TimeStamp_:	ufpObj = new UFTimeStamp(LTobj.length);
		break;

	    default:
		System.err.println("UFProtocol._create> unknown msg.type: "+LTobj.type);
		break;
	    }
	
	return ufpObj;
    }
//-----------------------------------------------------------------------------------------------    
    // creates a new instance of UFProtocol object on heap, reading from a socket,

    public static UFProtocol createFrom(Socket soc)
    {
	UFProtocol ufpObj = _create( recvLengthAndType(soc) );
	if( ufpObj != null ) {
	    if( ufpObj.recvFrom(soc) > 0 ) {
		if( ufpObj instanceof UFObsConfig ) ufpObj._mapValues();
		return ufpObj;
	    }
	}
	return null;
    }
    
    // creates a new instance of UFProtocol object from a file,

    public static UFProtocol createFrom(File f)
    {
      	UFProtocol ufpObj = _create( readLengthAndType(f) );
	if( ufpObj != null ) {
	    if( ufpObj.readFrom(f) > 0 ) {
		if( ufpObj instanceof UFObsConfig ) ufpObj._mapValues();
		return ufpObj;
	    }
	}
	return null;
    }
//-----------------------------------------------------------------------------------------------
    // recv rest of object header and data values from socket
    //(assumes that recvLengthAndType(soc) was already invoked):

    public int recvFrom(Socket soc) {
	try {
	    DataInputStream inpStrm = new DataInputStream(soc.getInputStream());
	    return recvFrom(inpStrm);
	}
	catch(SocketException sex) {
	    System.err.println("UFProtocol.recvFrom(Socket)> "+sex.toString());
	    if(_verbose) sex.printStackTrace();
	    return(-2);
	}
	catch(IOException ioe) {
	    System.err.println("UFProtocol.recvFrom(Socket)> "+ioe.toString());
	    return 0;
	}
    }
  
    protected int readFrom(File f) {
	try {
	    DataInputStream inpStrm = new DataInputStream(new FileInputStream(f));
	    return recvFrom(inpStrm);
	}
	catch( Exception e ) {
	    System.err.println("UFProtocol.readFrom(File)> "+e.toString());
	    return 0;
	}
    }
//-----------------------------------------------------------------------------------------------    
    // method assumes that abstract recvData( inpStrm ) is defined:

    public int recvFrom(DataInputStream inpStrm)
    {
	int nbHead = recvHeader(inpStrm);

	if( nbHead < _MinLength_ ) {
	    System.err.println("UFProtocol.recvFrom> for object: " + description());
	    System.err.println("UFProtocol.recvFrom> failed reading header info!");
	    return 0;
	}

	int nbToRead = _length - nbHead;
	//check integrity of header packet parameters:

	if( _type == MsgTyp._TimeStamp_ ) {
	    if( _elem != 0 ) {
		System.err.println("UFProtocol.recvFrom> for object: " + description());
		System.err.println("UFProtocol.recvFrom> inconsistent header: # elems.=" + _elem);
	    }
	    if( nbToRead != 0 ) {
		System.err.println("UFProtocol.recvFrom> for object: " + description());
		System.err.println("UFProtocol.recvFrom> inconsistent header: # bytes to read ="+nbToRead);
	    }
	    return nbHead;
	}

	if( nbToRead <= 0 || nbToRead > _maxDataBytes ) {
	    System.err.println("UFProtocol.recvFrom> for object: " + description());
	    System.err.println("UFProtocol.recvFrom> corrupted header: # bytes to read =" + nbToRead);
	    return(-1);
	}

	if( _elem <= 0 || (valSize()*_elem) > _maxDataBytes || (valSize()*_elem) > nbToRead ) {
	    System.err.println("UFProtocol.recvFrom> for object: " + description());
	    System.err.println("UFProtocol.recvFrom> bad # of elements specified by header: " + _elem);
	    System.err.println("UFProtocol.recvFrom> Length specified by header = " + _length);
	    System.err.println("UFProtocol.recvFrom> # bytes to read = " + nbToRead);
	    if( _elem <= 0 || (valSize()*_elem) > _maxDataBytes ) return(-2);
	}

	int numRead = recvData( inpStrm );

	if (numRead <= 0) {
	    System.err.println("UFProtocol.recvFrom> for object: " + description());
	    System.err.println("UFProtocol.recvFrom> no data values available:"+
			       " Expected: " + _elem + " values = " + nbToRead + " bytes.");
	    return(-3);
	}
	else if( numRead > nbToRead ) {
	    System.err.println("UFProtocol.recvFrom> for object: "+description());
	    System.err.println("UFProtocol.recvFrom> Read more than length specified in header:"
			       +" Bytes Read = "+numRead+". Expected: "+nbToRead);
	}
	else if( numRead < nbToRead ) {
	    System.err.println("UFProtocol.recvFrom> for object: "+description());
	    System.err.println("UFProtocol.recvFrom> Bytes Read = "+numRead+". Expected: " + nbToRead);
	    System.err.println("UFProtocol.recvFrom> Attempting to read extra data and discard.");
	    try {
		byte[] byteStream = new byte[nbToRead-numRead];
		inpStrm.readFully( byteStream );
	    }
	    catch (IOException ioe) {
		System.err.println("UFProtocol.recvFrom> failed trying to read more: " + ioe.toString());
	    }
	}

	return nbHead + numRead;
    }
//-----------------------------------------------------------------------------------------------
    // recv rest of protocol header, assuming length & type have already been recvd:

    protected int recvHeader(DataInputStream inpStrm) {
	int nbytes = 8;
	// Length and type should already have already been read, so assume start with 8 bytes.
	try {
	    _elem = inpStrm.readInt();
	    nbytes += 4;
	    // seqcnt and seqtot (unsigned short ints):
	    _seqCnt = inpStrm.readShort() & 0xFFFF;
	    _seqTot = inpStrm.readShort() & 0xFFFF;
	    nbytes += 4;
	    _duration = inpStrm.readFloat();
	    nbytes += 4;
	    byte[] tbuf = new byte[_tsLen];
	    inpStrm.readFully(tbuf);
	    nbytes += tbuf.length;
	    _timestamp = new String(tbuf);
	    int nameLen = inpStrm.readInt(); //read length of name string
	    nbytes += 4;
	    if( nameLen > 0 ) {
		if( nameLen > _maxNameLen ) {
		    _name = "ERR: invalid Header NAME field: Len=" + nameLen;
		    System.err.println("UFProtocol.recvHeader> " + _name);
		    return(-9);
		}
		else {
		    byte[] namebuf = new byte[nameLen];
		    inpStrm.readFully(namebuf);
		    nbytes +=  nameLen;
		    _name = new String(namebuf);
		}
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFProtocol.recvHeader> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFProtocol.recvHeader> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFProtocol.recvHeader> "+e.toString());
	}
	return nbytes;
    }
//-----------------------------------------------------------------------------------------------    
    //send protocol header attributes (including length & type) to output stream:

    protected int sendHeader(DataOutputStream outpStrm) {
	int nbsent=0;
	try {
	    outpStrm.writeInt(_length);
	    nbsent += 4;
	    outpStrm.writeInt(_type);
	    nbsent += 4;
	    outpStrm.writeInt(_elem);
	    nbsent += 4;
	    outpStrm.writeShort((short)(_seqCnt)); // 2 shorts: seqcnt, seqtot
	    nbsent += 2;
	    outpStrm.writeShort((short)(_seqTot));
	    nbsent += 2;
	    outpStrm.writeFloat(_duration);
	    nbsent += 4;
	    if( _timestamp.length() > _tsLen ) _timestamp = _timestamp.substring(0,_tsLen);
	    outpStrm.writeBytes(_timestamp);
	    nbsent += _timestamp.length();
	    if( _timestamp.length() < _tsLen ) {
		int more = _tsLen - _timestamp.length();
		outpStrm.write( new byte[more] );
		nbsent += more;
	    }
	    outpStrm.writeInt(_name.length());
	    nbsent += 4;
	    if( _name.length() > 0 ) {
		outpStrm.writeBytes(_name);
		nbsent += _name.length();
	    }
	    outpStrm.flush();
	}
	catch(EOFException eof) {
	    System.out.println("UFProtocol.sendHeader> "+eof);
	} 
	catch(IOException ioe) {
	    System.out.println("UFProtocol.sendHeader> "+ioe);
	}
	catch( Exception e ) {
	    System.out.println("UFProtocol.sendHeader> "+e);
	}
	return nbsent;
    }
//-----------------------------------------------------------------------------------------------
    // send msg on output stream, returns <= 0 on failure, num. bytes on success
    // method assumes that abstract sendData( outpStrm ) is defined:

    public int sendTo(DataOutputStream outpStrm)
    {
	int nbHead = sendHeader( outpStrm );

	if( nbHead < _MinLength_ ) {
	    System.err.println("UFProtocol.sendTo> failed sending header info!");
	    return 0;
	}

	if( _elem > 0 ) {
	    int numSent = sendData( outpStrm );
	    int nbToSend = _length - nbHead;
	    if (numSent == 0) {
		System.err.println("UFProtocol.sendTo> for object: "+description());
		System.err.println("UFProtocol.sendTo> no data values sent:"+
				   " Expected: " + _elem + " values = " + nbToSend + " bytes.");
	    }
	    else if( numSent > nbToSend ) {
		System.err.println("UFProtocol.sendTo> for object: "+description());
		System.err.println("UFProtocol.sendTo> Sent more than length specified in header:"
				   +" Bytes Sent = "+numSent+" > "+nbToSend);
	    }
	    else if( numSent < nbToSend ) {
		System.err.println("UFProtocol.sendTo> for object: "+description());
		System.err.println("UFProtocol.sendTo> Sent less than length specified in header:"
				   +" Bytes Sent = "+numSent+" < "+nbToSend);
	    }
	    return nbHead + numSent;
	}
	else return nbHead;
    }

    // send msg out on socket, returns <= 0 on failure, num. bytes on success

    public int sendTo(Socket soc) {
	try {
	    DataOutputStream outpStrm = new DataOutputStream(soc.getOutputStream());
	    return sendTo(outpStrm);
	}
	catch(SocketException sex) {
	    System.err.println("UFProtocol.sendTo(Socket)> "+sex.toString());
	    if(_verbose) sex.printStackTrace();
	    return(-2);
	}
	catch(IOException ioe) {
	    System.err.println("UFProtocol.sendTo(Socket)> "+ioe.toString());
	    if(_verbose) ioe.printStackTrace();
	    return 0;
	}
	catch(Exception x) {
	    System.err.println("UFProtocol.sendTo(Socket)> "+x.toString());
	    if(_verbose) x.printStackTrace();
	    return(-1);
	}
    }

    // write (serialize) msg to file, returns <= 0 on failure, num. bytes output on success

    public int writeTo(File f) {
	try {
	    DataOutputStream outpStrm = new DataOutputStream(new FileOutputStream(f));
	    return sendTo(outpStrm);
	}
	catch( IOException ioe ) {
	    System.err.println("UFProtocol.writeTo(File)> "+ioe.toString());
	    if(_verbose) ioe.printStackTrace();
	    return 0;
	}
	catch( Exception e ) {
	    System.err.println("UFProtocol.writeTo(File)> "+e.toString());
	    if(_verbose) e.printStackTrace();
	    return(-1);
	}
    }
//-------------------------more static methods---------------------------------------------------

    public static String recvString(DataInputStream inpStrm) {
        try {
            int len = inpStrm.readInt();
            if( len > 0 ) {
                byte[] buf = new byte[len];
                inpStrm.readFully(buf);
                return new String(buf);
            }
	}
	catch(EOFException eof) {
	    System.err.println("UFProtocol.recvString> "+eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFProtocol.recvString> "+ioe.toString());
        }
	catch (Exception e) {
            System.err.println("UFProtocol.recvString> "+e.toString());
        }
        return "";
    }

    public static String recvString(Socket soc) {
        try {
            return recvString(new DataInputStream(soc.getInputStream()));
        }
	catch(SocketException sex) {
	    System.err.println("UFProtocol.recvString(soc)> "+sex.toString());
	    if(_verbose) sex.printStackTrace();
	    return("");
	}
	catch (Exception e) {
            System.err.println("UFProtocol.recvString(soc)> "+e.toString());
            return("");
        }
    }

    public static int sendString(String tosend, DataOutputStream outpStrm) {
        int nbsent = 0;
        try {
            outpStrm.writeInt(tosend.length());
            nbsent += 4;
            if( tosend.length() > 0 ) {
                outpStrm.writeBytes(tosend);
                nbsent += tosend.length();
            }
            outpStrm.flush();
	}
	catch(IOException ioe) {
	    System.err.println("UFProtocol.sendString> "+ioe.toString());
        }
	catch (Exception e) {
            System.err.println("UFProtocol.sendString> "+e.toString());
        }
        return nbsent;
    }

    public static int sendString(String tosend, Socket soc) {
        try {
            return sendString(tosend,new DataOutputStream(soc.getOutputStream()));
        }
	catch(SocketException sex) {
	    System.err.println("UFProtocol.sendString(soc)> "+sex.toString());
	    if(_verbose) sex.printStackTrace();
	    return(-2);
	}
	catch (Exception e) {
            System.err.println("UFProtocol.sendString(soc)> "+e.toString());
            return(-1);
        }
    }
} // UFProtocol


