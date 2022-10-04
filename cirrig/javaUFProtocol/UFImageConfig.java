package javaUFProtocol;

//Title:        UFImageConfig.java
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2009
//Authors:      Frank Varosi
//Company:      University of Florida, Dept. of Astronomy.
//Description:  class to store & communicate status and config of a CCD or NIR array.

import java.io.*;
import java.util.*; 

public class UFImageConfig extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFImageConfig.java,v 1.5 2010/12/16 00:48:12 varosi Exp $";

    // UFImageConfig has no _values attribute either,
    // but it does have following attributes:

    public int width;
    public int height;
    public int depth;
    public int expTime;  //expTime is milliseconds.
    public int readMode;
    public int acqMode;
    public int acqType;
    public int expDone;
    public int readoutDone;
    public int posReadout;
    public int xsubpos;
    public int ysubpos;
    public int imageID;
    public int errCode;
    public int shutter;
    public int cooler,   heatCCD; // recv/send in same location (double duty option).
    public float temperature;
    public float pressure;
    public int expTelapsed;
    public int grabCnt;
    public int proCnt;
    public int writeCnt;
    public float expSeconds;
    public int sumPMTcnts;
    public int centerPMTsecs;   //center time of PMT flux history.
    public int imgMin;
    public int imgMax;
    public float imgAverage;
    public float imgNoise;     // recv/send in same location as float pressure

    protected int _Nelements = 28;

    private void _init() {
	_currentTime();
	_type = MsgTyp._ImageConfig_;
	_elem = _Nelements;
	_name = new String("UFImageConfig");
	_length = _minLength() + _elem*4;
    }

    public UFImageConfig() { _init(); }

    public UFImageConfig(int length) {
	_init();
	_length = length;
    }

    public UFImageConfig(int w, int h) {
	_init();
	width = w;
	height = h;
    }

    public UFImageConfig(int w, int h, int d) {
	_init();
	width = w;
	height = h;
	depth = d;
    }

    public UFImageConfig(String name, int w, int h, int d) {
	this(w,h,d);
	_name = new String(name);
	_length = _minLength() + _elem*4;
    }
//------------------------------------------------------------------------------------------

    public String description() { return("UFImageConfig"); }

    // return size of an element:
    public int valSize() { return 4; }
    public int valSize(int elemIdx) { return 4; }

    public void imgNoise( float noise ) { imgNoise = noise;  pressure = noise; }
    public void headCCD( int heat ) { heatCCD = heat;  cooler = heat; }

//------------------------------------------------------------------------------------------

    public int recvData(DataInputStream inp) {
	// read data values (length, type, and header have already been read)
	int nbytes=0;
	try {
	    if (_elem != _Nelements) {
		System.err.println("Bad # elements for ImageConfig object: expected: "
				   + _Nelements + ", received " + _elem);
		if( _elem < _Nelements )
		    return (-1);
		else System.err.println("Excess data will be discarded.");
	    }
	    //elements must be recvd in following order:
	    this.width         = inp.readInt();
	    this.height        = inp.readInt();
	    this.depth         = inp.readInt();
	    this.expTime       = inp.readInt();  //expTime is milliseconds.
	    this.readMode      = inp.readInt();
	    this.acqMode       = inp.readInt();
	    this.acqType       = inp.readInt();
	    this.expDone       = inp.readInt();
	    this.readoutDone   = inp.readInt();
	    this.posReadout    = inp.readInt();
	    this.xsubpos       = inp.readInt();
	    this.ysubpos       = inp.readInt();
	    this.imageID       = inp.readInt();
	    this.errCode       = inp.readInt();
	    this.shutter       = inp.readInt();
	    this.cooler        = inp.readInt();       this.heatCCD = this.cooler;
	    this.temperature   = inp.readFloat();
	    this.pressure      = inp.readFloat();     this.imgNoise = this.pressure;
	    this.expTelapsed   = inp.readInt();
	    this.grabCnt       = inp.readInt();
	    this.proCnt        = inp.readInt();
	    this.writeCnt      = inp.readInt();
	    this.expSeconds    = inp.readFloat();
	    this.sumPMTcnts    = inp.readInt();
	    this.centerPMTsecs = inp.readInt();   //center time of PMT flux history.
	    this.imgMin        = inp.readInt();
	    this.imgMax        = inp.readInt();
	    this.imgAverage    = inp.readFloat();
	    nbytes = _Nelements*4;
	}
	catch(EOFException eof) {
	    System.err.println("UFImageConfig::recvData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFImageConfig::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFImageConfig::recvData> "+e.toString());
	}
	return nbytes;
    }

    public int sendData(DataOutputStream out) {
	// write out data values (header already sent):
	int nbytes=0;
	try {
	    //elements must be sent in following order:
	    out.writeInt(this.width);
	    out.writeInt(this.height);
	    out.writeInt(this.depth); 
	    out.writeInt(this.expTime);
	    out.writeInt(this.readMode);
	    out.writeInt(this.acqMode);
	    out.writeInt(this.acqType);
	    out.writeInt(this.expDone);
	    out.writeInt(this.readoutDone);
	    out.writeInt(this.posReadout);
	    out.writeInt(this.xsubpos);
	    out.writeInt(this.ysubpos);
	    out.writeInt(this.imageID);
	    out.writeInt(this.errCode);
	    out.writeInt(this.shutter);
	    out.writeInt(this.cooler);
	    out.writeFloat(this.temperature);
	    out.writeFloat(this.pressure);
	    out.writeInt(this.expTelapsed);
	    out.writeInt(this.grabCnt);
	    out.writeInt(this.proCnt);
	    out.writeInt(this.writeCnt);
	    out.writeFloat(this.expSeconds);
	    out.writeInt(this.sumPMTcnts);
	    out.writeInt(this.centerPMTsecs);
	    out.writeInt(this.imgMin);
	    out.writeInt(this.imgMax);
	    out.writeFloat(this.imgAverage);
	    nbytes = _Nelements*4;
	}
	catch(EOFException eof) {
	    System.err.println("UFImageConfig::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFImageConfig::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFImageConfig::sendData> "+e.toString());
	}
	return nbytes;
  }
}
    
    
