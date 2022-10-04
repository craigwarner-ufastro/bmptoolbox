package javaUFProtocol;

import java.io.*;
import java.util.*; 

public class UFFrameConfig extends UFTimeStamp
{
    public static final
	String rcsID = "$Name:  $ $Id: UFFrameConfig.java,v 1.14 2010/03/29 22:00:38 varosi Exp $";

    // UFFrameConfig has no _values attribute either,
    // but it does have following attributes:

    public int width; public int height; public int depth;
    public int DMAcnt;
    public int frameGrabCnt;
    public int frameProcCnt;
    public int frameObsTotal;
    public int frameWriteCnt;
    public int frameSendCnt;
    public int coAdds;
    public int ChopBeam;
    public int SaveSet;
    public int NodBeam;
    public int NodSet;
    public int frameCoadds;
    public int chopCoadds;
    public int chopSettleFrms;
    public float frameTime;
    public float savePeriod;
    public int pixelSort, littleEnd;
    public int pixCnt, offset;

    public int wellADUmin,  wellADUmax,  wellADUavg;
    public int clampADUmin, clampADUmax, clampADUavg;
    public int oclmpADUmin, oclmpADUmax;

    public float sigmaWellMin,  sigmaWellMax,  sigmaWellNoise;
    public float sigmaClampMin, sigmaClampMax, sigmaClampNoise;
    public float sigmaOclmpMin, sigmaOclmpMax;


    protected int _Nelements = 37;

    private void _init() {
	_currentTime();
	_type = MsgTyp._FrameConfig_;
	_elem = _Nelements;
	_name = new String("UFFrameConfig");
	_length = _minLength() + _elem*4;
    }

    public UFFrameConfig() {
	_init();
    }

    public UFFrameConfig(int length) {
	_init();
	_length = length;
    }

    public UFFrameConfig(int w, int h) {
	_init();
	width = w;
	height = h;
    }

    public UFFrameConfig(int w, int h, int d) {
	_init();
	width = w;
	height = h;
	depth = d;
    }

    public UFFrameConfig(String name, int w, int h, int d) {
	this(w,h,d);
	_name = new String(name);
	_length = _minLength() + _elem*4;
    }

    public UFFrameConfig(int w, int h, int d, boolean little, int DMAcnt, int frameGrabCnt,
			 int coAdds, int pixelSort, int frameProcCnt, int frameObsTotal,
			 int ChopBeam, int SaveSet, int NodBeam, int NodSet) {
	this(w,h);
	this.depth=d; this.littleEnd = (little ? 1 : 0);
	this.DMAcnt = DMAcnt; this.frameGrabCnt = frameGrabCnt;
	this.coAdds = coAdds; this.pixelSort = pixelSort; this.frameProcCnt = frameProcCnt;
	this.frameObsTotal = frameObsTotal; this.ChopBeam = ChopBeam; this.SaveSet = SaveSet;
	this.NodBeam = NodBeam; this.NodSet = NodSet;
    }
//------------------------------------------------------------------------------------------

    public String description() { return("UFFrameConfig"); }

    // return size of an element:
    public int valSize() { return 4; }
    public int valSize(int elemIdx) { return 4; }

    public int getUpdatedBuffNames(Vector bufnames, String delim){
	StringTokenizer st = new StringTokenizer(_name,delim);
	int i=0;
	bufnames.clear();
	while (st.hasMoreTokens()) {
	    bufnames.add( new String(st.nextToken()));
	}
	return bufnames.size();
    }

    public int getUpdatedBuffNames(Vector bufnames){
	return getUpdatedBuffNames(bufnames,"^");
    }

    // chop/nod buffers in the form "instrum:bufname" -- for use in client
    public int getUpdatedBuffNames( String instrum, Vector bufnames,  String delim){
	int n = getUpdatedBuffNames(bufnames, delim);
	if (n <= 0)
	    return 0;

	for (int i=0; i<n; ++i)
	    bufnames.set(i,new String(instrum + ":" + (String)bufnames.get(i)));

	return n;
    }

    public int getUpdatedBuffNames( String instrum, Vector bufnames){
	return getUpdatedBuffNames(instrum,bufnames,"^");
    }

    // set the names -- for use in server
    public int setUpdatedBuffNames( String[] bufnames,  String delim){
	int cnt = bufnames.length;
	if (cnt <= 0)
	    return 0;
	String newname = bufnames[0];
	for (int i=1; i<cnt; i++)
	    newname += delim + bufnames[i];
	_length -= _name.length();
	_name = new String(newname);
	_length += _name.length();
	return cnt;
    }

    public int setUpdatedBuffNames( String[] bufnames){
	return setUpdatedBuffNames(bufnames,"^");
    }

    public int setUpdatedBuffNames(Vector bufnames) {
	return setUpdatedBuffNames((String[])bufnames.toArray(),"^");
    }

    public int setUpdatedBuffNames(Vector bufnames, String delim) {
	return setUpdatedBuffNames((String[])bufnames.toArray(),delim);
    }
//------------------------------------------------------------------------------------------

    public int recvData(DataInputStream inp) {
	// read data values (length, type, and header have already been read)
	int nbytes=0;
	try {
	    if (_elem != _Nelements) {
		System.err.println("Bad # elements for FrameConfig object: expected: "
				   + _Nelements + ", received " + _elem);
		if( _elem < _Nelements )
		    return (-1);
		else System.err.println("Excess data will be discarded.");
	    }
	    //elements must be recvd in following order:
	    this.width          = inp.readInt();
	    this.height         = inp.readInt();
	    this.depth          = inp.readInt();
	    this.littleEnd      = inp.readInt();
	    this.DMAcnt         = inp.readInt();
	    this.frameGrabCnt   = inp.readInt();
	    this.coAdds         = inp.readInt();
	    this.pixelSort      = inp.readInt();
	    this.frameProcCnt   = inp.readInt();
	    this.frameObsTotal  = inp.readInt();
	    this.ChopBeam       = inp.readInt();
	    this.SaveSet        = inp.readInt();
	    this.NodBeam        = inp.readInt();
	    this.NodSet         = inp.readInt();
	    this.frameWriteCnt  = inp.readInt();
	    this.frameSendCnt   = inp.readInt();
	    this.wellADUavg     = inp.readInt();
	    this.sigmaWellNoise = inp.readFloat();
	    this.sigmaOclmpMin  = inp.readFloat();
	    this.clampADUavg    = inp.readInt();
	    this.sigmaClampNoise = inp.readFloat();
	    this.sigmaOclmpMax  = inp.readFloat();
	    this.frameCoadds    = inp.readInt();
	    this.chopSettleFrms = inp.readInt();
	    this.chopCoadds     = inp.readInt();
	    this.frameTime      = inp.readFloat();
	    this.savePeriod     = inp.readFloat();
	    this.oclmpADUmin    = inp.readInt();
	    this.oclmpADUmax    = inp.readInt();
	    this.wellADUmin     = inp.readInt();
	    this.wellADUmax     = inp.readInt();
	    this.sigmaWellMin   = inp.readFloat();
	    this.sigmaWellMax   = inp.readFloat();
	    this.clampADUmin    = inp.readInt();
	    this.clampADUmax    = inp.readInt();
	    this.sigmaClampMin  = inp.readFloat();
	    this.sigmaClampMax  = inp.readFloat();
	    nbytes = _Nelements*4;
	}
	catch(EOFException eof) {
	    System.err.println("UFFrameConfig::recvData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFFrameConfig::recvData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFFrameConfig::recvData> "+e.toString());
	}
	return nbytes;
    }

    public int sendData(DataOutputStream out) {
	// write out data values (header already sent):
	int nbytes=0;
	try {
	    //elements must be sent in following order:
	    out.writeInt(this.width);  out.writeInt(this.height);       out.writeInt(this.depth); 
	    out.writeInt(this.littleEnd);
	    out.writeInt(this.DMAcnt); out.writeInt(this.frameGrabCnt); out.writeInt(this.coAdds);
	    out.writeInt(this.pixelSort);
	    out.writeInt(this.frameProcCnt);  out.writeInt(this.frameObsTotal);
	    out.writeInt(this.ChopBeam);
	    out.writeInt(this.SaveSet);
	    out.writeInt(this.NodBeam);
	    out.writeInt(this.NodSet);
	    out.writeInt(this.frameWriteCnt); out.writeInt(this.frameSendCnt);
	    out.writeInt(this.wellADUavg);
	    out.writeFloat(this.sigmaWellNoise);  out.writeFloat(this.sigmaOclmpMin);
	    out.writeInt(this.clampADUavg);
	    out.writeFloat(this.sigmaClampNoise); out.writeFloat(this.sigmaOclmpMax);
	    out.writeInt(this.frameCoadds);
	    out.writeInt(this.chopSettleFrms); 
	    out.writeInt(this.chopCoadds);
	    out.writeFloat(this.frameTime);
	    out.writeFloat(this.savePeriod);
	    out.writeInt(this.oclmpADUmin);
	    out.writeInt(this.oclmpADUmax);
	    out.writeInt(this.wellADUmin);
	    out.writeInt(this.wellADUmax);
	    out.writeFloat(this.sigmaWellMin);
	    out.writeFloat(this.sigmaWellMax);
	    out.writeInt(this.clampADUmin);
	    out.writeInt(this.clampADUmax);
	    out.writeFloat(this.sigmaClampMin);
	    out.writeFloat(this.sigmaClampMax);
	    nbytes = _Nelements*4;
	}
	catch(EOFException eof) {
	    System.err.println("UFFrameConfig::sendData> "+eof.toString());
	} 
	catch(IOException ioe) {
	    System.err.println("UFFrameConfig::sendData> "+ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFFrameConfig::sendData> "+e.toString());
	}
	return nbytes;
  }
}
    
    
