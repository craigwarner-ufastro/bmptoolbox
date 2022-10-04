package javaMMTLib;
/**
 * Title:        UFMMTImg.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  For reading and managing MMT .img files 
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Toolkit;
import javax.swing.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javaUFProtocol.*;
import javaUFLib.*;

public class UFMMTImg {

    public static final
        String rcsID = "$Name:  $ $Id: UFMMTImg.java,v 1.11 2011/10/24 18:03:02 warner Exp $";

    protected String _name = "UFMMTImg";
    protected static boolean _verbose = false;
    protected String _filename = null;
    public static final int MODE_REGULAR = 0; //send all frames
    public static final int MODE_PRE_POST = 1; //send first and last only
    public static final int MODE_FIRST_ONLY = 2; //send first only
    protected int nrecv = 0;

    protected UFMMTImgHeader header;
    protected Vector <UFProtocol> data;
    protected Vector <String> extHeader;

    public UFMMTImg() {
	/* Create empty object */
	header = new UFMMTImgHeader();
	data = new Vector();
    }

    public UFMMTImg(String filename) {
	/* Read from filename given */
	_filename = filename;
	if (filename.indexOf(":\\") != -1) {
	  _name = filename.substring(filename.lastIndexOf("\\")+1, filename.lastIndexOf(".")); 
	} else _name = filename.substring(filename.lastIndexOf("/")+1, filename.lastIndexOf("."));
	data = new Vector();
	readImgFile();
    }

    public UFMMTImg(String filename, int startIdx, int endIdx) {
        /* Read selected frames from filename given */
        _filename = filename;
        if (filename.indexOf(":\\") != -1) {
          _name = filename.substring(filename.lastIndexOf("\\")+1, filename.lastIndexOf("."));
        } else _name = filename.substring(filename.lastIndexOf("/")+1, filename.lastIndexOf("."));
        data = new Vector();
        readImgFile(_filename, startIdx, endIdx);
    }

    public UFMMTImg(UFBytes header) {
        /* Create object with header and empty data vector */
        this.header = new UFMMTImgHeader(header);
        this.data = new Vector();
	_name = header.name(); 
    }

    public UFMMTImg(UFMMTImgHeader header) {
        /* Create object with header and empty data vector */
        this.header = header;
        this.data = new Vector();
	_name = header.name();
    }

    public UFMMTImg(UFBytes header, UFProtocol data) {
	/* Reconstruct from UFStrings and UFProtocol
	* This is how data will be sent over network */
	this.header = new UFMMTImgHeader(header);
	this.data = new Vector();
	this.data.add(data);
	_name = header.name();
    }

    public UFMMTImg(UFMMTImgHeader header, UFProtocol data) {
        /* Reconstruct from UFMMTImgHeader and UFProtocol
        * This is how data will be sent over network */
        this.header = header; 
        this.data = new Vector();
        this.data.add(data);
	_name = header.name();
    }

    public UFMMTImg(UFBytes header, Vector <UFProtocol> data) {
        /* Reconstruct from UFStrings and UFProtocol
        * This is how data will be sent over network */
	this.header = new UFMMTImgHeader(header);
	this.data = data;
	_name = header.name();
    }

    public UFMMTImg(UFMMTImgHeader header, Vector <UFProtocol> data) {
        /* Reconstruct from UFMMTImgHeader and UFProtocol
        * This is how data will be sent over network */
        this.header = header;
        this.data = data;
	_name = header.name();
    }

//----------------------------------------------------------------------------------------------------

    // write Img header and data to a file:

    public int writeImgFile( UFProtocol data, String filename, String[] moreHeaderInfo )
    {
/*
	if( data == null ) {
	    System.err.println("UFMMTImg::writeImgFile> data object is empty.");
	    return 0;
	}

	if( moreHeaderInfo != null ) addrecs( moreHeaderInfo );
	this.end();

	if( filename == null ) return 0;

	FileOutputStream foutps = writeToFITS( filename );

	if( foutps != null ) {
	    if(_verbose) System.out.println("UFMMTImg::writeImgFile> writing data...");
	    try {
		int nbwritten = data.sendData( new DataOutputStream( foutps ) );
		foutps.close();
		System.out.println("UFMMTImg::writeImgFile> " + nbwritten +
				   " bytes of data written to: " + filename);
		return nbwritten;
	    }
	    catch(IOException ioe) {
		System.err.println("UFMMTImg::writeImgFile> " + ioe.toString());
		ioe.printStackTrace();
		try{ foutps.close(); } catch(Exception ex) {}
		return 0;
	    }
	}
	else {
	    System.err.println("UFMMTImg::writeImgFile> File Output Stream is NULL.");
	    return 0;
	}
*/
	return 0;
    }

    public int writeImgFile( int[] data, String dataname, String filename, String[] moreHeaderInfo )
    {
	return writeImgFile( new UFInts(dataname, data), filename, moreHeaderInfo );
    }

    public int writeImgFile( int[] data, String filename, String[] moreHeaderInfo )
    {
	return writeImgFile( data, "data", filename, moreHeaderInfo );
    }

//----------------------------------------------------------------------------------------------------

    public int recvImages(Socket clientSocket, int imageMode) {
      int nframes = 0;
      int depth = header.getBitDepth();
      int nfread = 0;
      if (imageMode == MODE_FIRST_ONLY) {
	nframes = 1;
      } else if (imageMode == MODE_PRE_POST) {
	nframes = 2;
	if (header.getNFrames() < 2) nframes = header.getNFrames();
      } else if (imageMode == MODE_REGULAR) {
	nframes = header.getNFrames();
      }
      for (int j = 0; j < nframes; j++) {
	UFProtocol framedata = UFProtocol.createFrom(clientSocket); 
        if (framedata == null) {
	  System.out.println("UFMMTimg::recvImages> Received null object!");
	  return -1;
	}
	System.out.println("UFMMTImg::recvImages> received frame "+(j+1)+" of "+nframes+": "+framedata.name());
	boolean valid = false;
	if (depth == 16 && framedata instanceof UFunsignShorts) valid = true;
	if (depth == 32 && framedata instanceof UFInts) valid = true;
	if (depth == -32 && framedata instanceof UFFloats) valid = true;
	if (valid) {
	  //Add this frame to the vector
	  data.add(framedata);
	  nfread++;
	} else {
          System.out.println("UFMMTimg::recvImages> Received invalid object!");
          return -1;
	}
      }
      return nfread;
    }

//----------------------------------------------------------------------------------------------------

    public int recvNFrames(int imageMode) {
      int nframes = 0;
      int depth = header.getBitDepth();
      int nfread = 0;
      if (imageMode == MODE_FIRST_ONLY) {
        nframes = 1;
      } else if (imageMode == MODE_PRE_POST) {
        nframes = 2;
        if (header.getNFrames() < 2) nframes = header.getNFrames();
      } else if (imageMode == MODE_REGULAR) {
        nframes = header.getNFrames();
      }
      return nframes;
    }

//----------------------------------------------------------------------------------------------------


    public int recvFirstFrame(Socket clientSocket, int imageMode) {
      int nframes = 0;
      int depth = header.getBitDepth();
      int nfread = 0;
      if (imageMode == MODE_FIRST_ONLY) {
        nframes = 1;
      } else if (imageMode == MODE_PRE_POST) {
        nframes = 2;
        if (header.getNFrames() < 2) nframes = header.getNFrames();
      } else if (imageMode == MODE_REGULAR) {
        nframes = header.getNFrames();
      }
      UFProtocol framedata = UFProtocol.createFrom(clientSocket);
      if (framedata == null) {
        System.out.println("UFMMTimg::recvFirstFrame> Received null object!");
        return -1;
      }
      nrecv = 1;
      System.out.println("UFMMTImg::recvFirstFrame> received frame "+nrecv+" of "+nframes+": "+framedata.name());
      boolean valid = false;
      if (depth == 16 && framedata instanceof UFunsignShorts) valid = true;
      if (depth == 32 && framedata instanceof UFInts) valid = true;
      if (depth == -32 && framedata instanceof UFFloats) valid = true;
      if (valid) {
        //Add this frame to the vector
        data.add(framedata);
        nfread++;
      } else {
        System.out.println("UFMMTimg::recvFirstFrame> Received invalid object!");
        return -1;
      }
      return nframes;
    }

//----------------------------------------------------------------------------------------------------

    /* This method receives a UFProtocol object and appends data to a FITS file given by filename */

    public boolean appendToFITS(String filename, Socket clientSocket, int imageMode) {
      int nframes = 0;
      int depth = header.getBitDepth();
      int nfread = 0;
      if (imageMode == MODE_FIRST_ONLY) {
        nframes = 1;
      } else if (imageMode == MODE_PRE_POST) {
        nframes = 2;
        if (header.getNFrames() < 2) nframes = header.getNFrames();
      } else if (imageMode == MODE_REGULAR) {
        nframes = header.getNFrames();
      }
      UFProtocol framedata = UFProtocol.createFrom(clientSocket);
      if (framedata == null) {
        System.out.println("UFMMTimg::appendToFITS> Received null object!");
        return false;
      }
      nrecv++;
      System.out.println("UFMMTImg::appendToFITS> received frame "+nrecv+" of "+nframes+": "+framedata.name());

      boolean valid = false;
      if (depth == 16 && framedata instanceof UFunsignShorts) {
	UFunsignShorts framedata_us = (UFunsignShorts)framedata;
	framedata = new UFInts(framedata_us.name(), UFArrayOps.castAsInts(framedata_us.values()));
	valid = true;
      }
      if (depth == 32 && framedata instanceof UFInts) valid = true;
      if (depth == -32 && framedata instanceof UFFloats) valid = true;
      if (!valid) {
        System.out.println("UFMMTimg::appendToFITS> Received invalid object!");
        return false;
      }

      byte[] _blanks = new byte[80];
      String _blankLine;
      for( int i=0; i < _blanks.length; i++ ) _blanks[i] = 32;
      _blankLine = new String( _blanks );

      int nheadRecs = 0;
      int nbytes = 0;
      try {
	File theFile = new File( filename );
	/* Open file output stream for append */
	FileOutputStream foutps = new FileOutputStream(theFile, true);

	/* Write data extensions */
	nheadRecs = 0;
        /* Get extension header and add END */
        Vector<String> fitsHead = header.getExtensionHeader();
        fitsHead.add("END");
	for (int j = 0; j < fitsHead.size(); j++) {
	  String frec = (String)fitsHead.elementAt(j);
	  if (frec.length() > 80)
	    foutps.write( frec.getBytes(), 0, 80 );
	  else {
	    foutps.write( frec.getBytes() );
	    if( frec.length() < 80 ) foutps.write( _blanks, 0, 80 - frec.length() );
	  }
	  nheadRecs++;
	}
	nbytes += nheadRecs*80;
	if (_verbose) System.out.println("UFMMTImg::appendToFITS> wrote " + nheadRecs + " header records for ext. "+nrecv);
	/* Pad with blank recs */
	if( nheadRecs % 36 > 0 ) {
	  int nextra = 36 - nheadRecs % 36;
	  for (int j=0; j < nextra; j++ ) foutps.write( _blanks );
	  if (_verbose) System.out.println("UFMMTImg::appendToFITS> wrote "+nextra+" blank recs for ext. "+nrecv);
	  nbytes += nextra*80;
	}
	int nbdata = framedata.sendData(new DataOutputStream(foutps));
	if (_verbose) System.out.println("UFMMTImg::appendToFITS> wrote "+nbdata+" bytes of data for ext. "+nrecv);
	nbytes += nbdata;
	if (nbdata % 2880 > 0) {
	  /* Pad data with blank recs */
	  int nextra = 2880 - nbdata % 2880;
	  byte[] dblanks = new byte[nextra];
	  for (int j = 0; j < dblanks.length; j++) dblanks[j] = 0;
	  foutps.write(dblanks);
	  if (_verbose) System.out.println("UFMMTImg::appendToFITS> wrote "+nextra+" bytes of blank data for ext. "+nrecv);
	  nbytes += nextra;
	}
	foutps.close();
      }
      catch( Exception e ) {
	System.err.println("UFMMTImg::appendToFITS> "+e.toString());
	return false;
      }
      return true;
    }

//----------------------------------------------------------------------------------------------------

    /* Static method to read total exp time from image header */

    public static float getTotalExptime(String filename) {
	UFMMTImg img = new UFMMTImg();
        if( filename == null ) return -1;

        FileInputStream finps = img.readImgHeader( filename );
        int nframes = img.header.getNFrames();
        float frameRate = img.header.FrameRate;
	try {
	  finps.close();
	} catch (IOException e) {}
	return nframes/frameRate; 
    }

    /* Static method to read frame rate from image header */
    public static float getFrameRate(String filename) {
        UFMMTImg img = new UFMMTImg();
        if( filename == null ) return -1;

        FileInputStream finps = img.readImgHeader( filename );
        float frameRate = img.header.FrameRate;
        try {
          finps.close();
        } catch (IOException e) {}
        return frameRate;
    }

//----------------------------------------------------------------------------------------------------
    /* read Img header and data from a file:
     * store them internally -- return number of bytes read
     */
    public int readImgFile( String filename, int startFrame, int endFrame )
    {
	if( filename == null ) return -1;

	FileInputStream finps = readImgHeader( filename );
	header.setName(_name);
	int npix = header.getNpix(); 
	int nframes = header.getNFrames();
	int depth = header.getBitDepth(); 
	int nbread = 0;
	if (endFrame == -1) endFrame = nframes;

	if( finps != null ) try {

	    String dname = filename.substring(filename.lastIndexOf("/")+1);
	    for (int j = 0; j < nframes; j++) {
		UFProtocol framedata = null;

	    	switch( depth ) {
		    case 16:
			framedata = new UFunsignShorts( dname+":"+j, npix); break;
		    case 32:
		    	framedata = new UFInts( dname+":"+j, npix); break;
		    case -32:
		    	framedata = new UFFloats( dname+":"+j, npix); break;
		    default:
		    	framedata = new UFInts( dname+":"+j, npix);
	        }

	        System.out.println("UFMMTImg::readImgFile> reading frame "+j+": " + framedata.elements() + " of "
			       + depth + " bit pixels of data from file: " + filename + "...");
		
		nbread += framedata.recvData( new DataInputStream( finps ), header.getByteOrder());
		System.out.println("UFMMTImg::readImgFile> " + nbread +
				   " bytes of data read from: " + filename);
		//Add this frame to the vector
		if (j >= startFrame && j < endFrame) data.add((UFProtocol)framedata);
	    }
            finps.close();
	} catch(IOException ioe) {
	    System.err.println("UFMMTImg::readImgFile> " + ioe.toString());
	    ioe.printStackTrace();
	    try{ finps.close(); } catch(Exception ex) {}
	    return -1;
	}
	else {
	    System.err.println("UFMMTImg::readImgFile> File Input Stream is NULL.");
	    return -1;
	}
	return nbread;
    }

    public int readImgFile(String filename) {
      return readImgFile(filename, 0, -1);
    }

    public int readImgFile() { return readImgFile(_filename); }

//----------------------------------------------------------------------------------------------------

    public Vector <UFProtocol> getAllFrames() {
	return data;
    }

    public UFProtocol getFrame(int idx) {
	if (data == null) {
	    System.err.println("UFMMTImg::getFrame> Frames are all null!");
	    return null;
	}
	if (idx >= data.size()) {
	    System.err.println("UFMMTImg::getFrame> Requested frame "+idx+" but only "+data.size()+" frames exist!");
	    return null;
	}
	return data.get(idx);
    }

    public int getNFrames() {
      if (data == null) return 0;
      return data.size();
    }

    public UFFrameConfig getFrameCon() {
        int bitpix = header.getBitDepth(); 
        int npx = header.Xsize; 
        int npy = header.Ysize; 
        return new UFFrameConfig( npx, npy, bitpix );
    }

//----------------------------------------------------------------------------------------------------

    /* This method writes the header(s) and data to a FITS file given by filename */

    public int writeToFITS(String filename)
    {
	byte[] _blanks = new byte[80];
	String _blankLine;
        for( int i=0; i < _blanks.length; i++ ) _blanks[i] = 32;
        _blankLine = new String( _blanks );

	int nheadRecs = 0;
	int nbytes = 0;
	try {
	    File theFile = new File( filename );

	    if( !theFile.createNewFile() ) {
		System.err.println("UFMMTImg::writeToFITS> failed creating file: " + filename
				   + ", does file already exist ?");
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null,"File [" + filename + "] already exists.",
					      "ERROR", JOptionPane.ERROR_MESSAGE);		
		return -1; 
	    }

	    /* Open file output stream */
	    FileOutputStream foutps = new FileOutputStream( theFile );
	    /* First look at primary header from .img file */ 
	    Vector<String> fitsHead = header.getPrimaryFitsHeader();
	    for (int j = 0; j < fitsHead.size(); j++) {
		String frec = (String)fitsHead.elementAt(j);
		if( frec.length() > 80 )
		    foutps.write( frec.getBytes(), 0, 80 );
		else {
		    foutps.write( frec.getBytes() );
		    if( frec.length() < 80 ) foutps.write( _blanks, 0, 80 - frec.length() );
		}
		nheadRecs++;
	    }
	    /* Create ext header if it doesn't exist */
	    if (extHeader == null) {
	      extHeader = new Vector(10);
	    }
	    /* Add END */
            extHeader.add("END");
	    /* Write ext header */
	    if (extHeader != null) for (int j = 0; j < extHeader.size(); j++) {
                String frec = (String)extHeader.elementAt(j);
                if( frec.length() > 80 )
                    foutps.write( frec.getBytes(), 0, 80 );
                else {
                    foutps.write( frec.getBytes() );
                    if( frec.length() < 80 ) foutps.write( _blanks, 0, 80 - frec.length() );
                }
                nheadRecs++;
	    }
	    nbytes = nheadRecs*80;
	    if(_verbose) System.out.println("UFMMTImg::writeToFITS> wrote " + nheadRecs + " header records.");
	    /* Pad with blank recs */
	    if( nheadRecs % 36 > 0 ) {
		int nextra = 36 - nheadRecs % 36;
		for (int i=0; i < nextra; i++ ) foutps.write( _blanks );
		if (_verbose) System.out.println("UFMMTImg::writeToFITS> wrote "+nextra+" blank recs.");
		nbytes += nextra*80;
	    }
	    /* Write data extensions */
	    if (data == null) return nbytes; 
	    for (int i = 0; i < data.size(); i++) {
		nheadRecs = 0;
		UFunsignShorts framedata_us = (UFunsignShorts)data.elementAt(i);
		UFInts framedata = new UFInts(framedata_us.name(), UFArrayOps.castAsInts(framedata_us.values()));
		/* Get extension header and add END */
		fitsHead = header.getExtensionHeader();
		fitsHead.add("END");
		for (int j = 0; j < fitsHead.size(); j++) {
		  String frec = (String)fitsHead.elementAt(j);
		  if( frec.length() > 80 )
		    foutps.write( frec.getBytes(), 0, 80 );
                  else {
                    foutps.write( frec.getBytes() );
                    if( frec.length() < 80 ) foutps.write( _blanks, 0, 80 - frec.length() );
                  }
                  nheadRecs++;
		}
		nbytes += nheadRecs*80;
		if (_verbose) System.out.println("UFMMTImg::writeToFITS> wrote " + nheadRecs + " header records for ext. "+(i+1));
		/* Pad with blank recs */
		if( nheadRecs % 36 > 0 ) {
		  int nextra = 36 - nheadRecs % 36;
                  for (int j=0; j < nextra; j++ ) foutps.write( _blanks );
                  if (_verbose) System.out.println("UFMMTImg::writeToFITS> wrote "+nextra+" blank recs for ext. "+(i+1));
                  nbytes += nextra*80;
            	}
		int nbdata = framedata.sendData(new DataOutputStream(foutps));
		if (_verbose) System.out.println("UFMMTImg::writeToFITS> wrote "+nbdata+" bytes of data for ext. "+(i+1));
		nbytes += nbdata;
		if (nbdata % 2880 > 0) {
		  /* Pad data with blank recs */
		  int nextra = 2880 - nbdata % 2880;
		  byte[] dblanks = new byte[nextra];
		  for (int j = 0; j < dblanks.length; j++) dblanks[j] = 0;
		  foutps.write(dblanks);
		  if (_verbose) System.out.println("UFMMTImg::writeToFITS> wrote "+nextra+" bytes of blank data for ext. "+(i+1));
		  nbytes += nextra;
		} 
	    }
	    foutps.close();
	}
	catch( Exception e ) {
	    System.err.println("UFMMTImg::writeToFITS> "+e.toString());
	    return -1;
	}
	System.out.println("UFMMTImg::writeToFITS> wrote "+nbytes+" bytes of data to "+filename+".");
	return nbytes;
    }
//----------------------------------------------------------------------------------------------------

    public FileInputStream readImgHeader(String filename)
    {
	try {
	    File theFile = new File( filename );

	    if( !theFile.canRead() ) {
		System.err.println("UFMMTImg::readImgHeader> cannot read file: " + filename);
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null,"File [" + filename + "] is not readable.",
					      "ERROR", JOptionPane.ERROR_MESSAGE);		
		return null;
	    }

	    FileInputStream finps = new FileInputStream( theFile );

	    try {
		byte[] head = new byte[512];
		finps.read(head);
		header = new UFMMTImgHeader(head);
	    }
	    catch(EOFException eof) {
		System.err.println("UFMMTImg::readImgHeader> " + eof.toString());
	    }
	    catch(IOException ioe) {
		System.err.println("UFMMTImg::readImgHeader> "+ ioe.toString());
	    }
	    catch( Exception e ) {
		System.err.println("UFMMTImg::readImgHeader> " + e.toString());
	    }

	    if(_verbose) System.out.println("UFMMTImg::readImgHeader> read 512 bytes.");

	    return finps;
	}
	catch( Exception e ) {
	    System.err.println("UFMMTImg::readImgHeader> "+e.toString());
	    return null;
	}
    }

//----------------------------------------------------------------------------------------------------

    public int sendTo(Socket soc, int mode) {
	//Send header as UFBytes object then data as UFProtocol
	int retVal = header.sendTo(soc); 
	switch(mode) {
	  case MODE_REGULAR:
	    /* Send all frames, one at a time */
	    for (int j = 0; j < data.size(); j++) {
		retVal += data.get(j).sendTo(soc);
	    }
	    break;
	  case MODE_PRE_POST:
	    /* Send first and last only to jdd */
	    if (data.size() > 0) retVal += data.get(0).sendTo(soc);
	    if (data.size() > 1) retVal += data.get(data.size()-1).sendTo(soc);
	    break;
	  case MODE_FIRST_ONLY:
            /* Send first only */
            if (data.size() > 0) retVal += data.get(0).sendTo(soc);
	    break;
	  default:
	    System.err.println("UFMMTImg::sendTo> Invalid mode.  Only sent header!");
	    break;
	}
	System.out.println("UFMMTImg::sendTo> Sent "+retVal+" bytes.");
	return retVal; 
    }

    public int sendTo(Socket soc) {
	return sendTo(soc, MODE_REGULAR);
    }

    public String name() { return _name; }

//----------------------------------------------------------------------------------------------------

    public void addExtHeader(UFStrings fitsHead) {
	extHeader = new Vector(fitsHead.numVals());
	for (int j = 0; j < fitsHead.numVals(); j++) {
	  extHeader.add(fitsHead.stringAt(j));
	}
    }

//----------------------------------------------------------------------------------------------------


    public static void main(String[] args) {
      UFMMTImg mmt = new UFMMTImg(args[0]);
    }
}
