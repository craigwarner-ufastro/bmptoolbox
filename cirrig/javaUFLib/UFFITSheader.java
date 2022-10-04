package javaUFLib;
/**
 * Title:        UFFITSheader.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2005
 * Author:       Frank Varosi,  Craig Warner,  and Shaun McDowell
 * Company:      University of Florida
 * Description:  For creating and managing a FITS header, and reading/writing of FITS files.
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javaUFProtocol.*;

public class UFFITSheader extends LinkedList {

    public static final
	String rcsID = "$Name:  $ $Id: UFFITSheader.java,v 1.38 2019/03/19 21:36:46 cwarner Exp $";

    protected static boolean _verbose = false;
    protected static File _path = null;
    protected String _filename = null;

    protected String  _FITSrecord = "";
    protected String  _FITSkey = "";
    protected UFFrameConfig _frameConfig;

    protected byte[] _blanks = new byte[80];
    protected String _blankLine;
    protected String _name = "FITSheader";

    protected boolean _unsigned = false;

    public Vector<UFFITSheader> _extHeaders;

    public static final int CAST_AS_INTS = 1; 
    public static final int CAST_AS_FLOATS = 2;
    
    //to use method readFITSfile() just create default UFFITSheader() and invoke.

    public UFFITSheader() {
	_init();
	_FITSrecord = "";
    }

    public UFFITSheader(String comment, boolean simple) {
	_init();
	_start(comment,simple);
    }

    public UFFITSheader(UFFrameConfig fc, String comment, boolean simple) {
	_init();
	_start( comment, simple );
	addrec("BITPIX",   fc.depth,  "Bits per pixel");
	addrec("NAXIS" ,   2,         " ");
	addrec("NAXIS1",   fc.width,  "X dimension of array");
	addrec("NAXIS2",   fc.height, "Y dimension of array");
	addrec("BUFCOADD", fc.coAdds, "# of saveset coadds in frame buffer");
	date();
	_frameConfig = new UFFrameConfig( fc.width, fc.height, fc.depth );
    }

    public UFFITSheader(UFFrameConfig fc, UFObsConfig oc, String comment, boolean simple) {
	_init();
	_start(comment, simple);
	addrec("BITPIX", fc.depth,     "Bits per pixel");
	addrec("NAXIS" , 6,            " ");
	addrec("NAXIS1", fc.width,     "X dimension of array");
	addrec("NAXIS2", fc.height,    "Y dimension of array");
	addrec("NAXIS3", oc.chopBeams(), "Number of chop positions");
	addrec("NAXIS4", oc.saveSets(),  "Number of savesets per nod phase");
	addrec("NAXIS5", oc.nodBeams(),  "Number of nod positions");
	addrec("NAXIS6", oc.nodSets(),   "Number of nod cycles");
	addrec("BUFCOADD", fc.coAdds,    "# of saveset coadds in frame buffer");
	date();
	_frameConfig = new UFFrameConfig( fc.width, fc.height, fc.depth );
    }

    public String description() { return new String("UFFITSheader"); }

    public void verbose( boolean verbose ) { _verbose = verbose; }

    private void _init() {
	for( int i=0; i < _blanks.length; i++ ) _blanks[i] = 32;
	_blankLine = new String( _blanks );
    }

    public UFFrameConfig getFrameCon()
    {
	int bitpix = getInt("BITPIX");
	int nax = getInt("NAXIS");
	int npx = getInt("NAXIS1");
	int npy = 1;
	if( nax > 1 ) npy = getInt("NAXIS2");

	_frameConfig = new UFFrameConfig( npx, npy, bitpix );
	return _frameConfig;
    }
    
    public UFImageConfig getImageConfig() {
  	UFImageConfig imageConfig = new UFImageConfig(4096, 4096, 16);
	imageConfig.expSeconds = getFloat("EXPTIME");
	imageConfig.expTime = (int)(imageConfig.expSeconds * 1000);
	imageConfig.imgMin = getInt("IMGMIN");
	imageConfig.imgMax = getInt("IMGMAX");
	imageConfig.imgAverage = getFloat("IMGAVG");

	int imageCnt = getInt("IMAGECNT");
	imageConfig.grabCnt = imageCnt;
	imageConfig.proCnt = imageCnt;
	imageConfig.writeCnt = imageCnt;
	imageConfig.imageID = getInt("IMAGE_ID");
	return imageConfig;
    }

    public UFFrameConfig fullFrameCon()
    {
	if( _frameConfig == null ) getFrameCon();

	_frameConfig.frameCoadds = getInt("FRMCOADD");
	_frameConfig.chopCoadds = getInt("CHPCOADD");
	_frameConfig.frameTime = getFloat("FRMTIME");
	_frameConfig.savePeriod = 1/getFloat("SAVEFREQ");
	_frameConfig.coAdds = getInt("BUFCOADD");
	if( _frameConfig.coAdds < 1 ) _frameConfig.coAdds = 1;

	return _frameConfig;
    }

    public UFFrameConfig getLastFrameCon() {
	/* For use with MEFs where getFrameCon() won't work because it
	 * overwrites the _frameConfig that was created when reading
	 * the extensions */
        return _frameConfig;
    }

//----------------------------------------------------------------------------------------------------
    // write multiextension FITS file.  Can also write single ext file.
    // can handle multiple data types 

    public int writeFITSmef(String filename, Vector<UFProtocol> data, Vector<UFStrings> headers, int cast) {
      if (data == null) {
	System.err.println("UFFITSheader.writeFITSmef> data object is empty.");
        return 0;
      }

      int nbytes = 0, nheadRecs = 0;
      UFStrings currHeader = null;
      UFProtocol currData = null;
      String lastRec = null;

      //add first header if given
      if (headers != null && !headers.isEmpty()) {
	currHeader = headers.get(0);
	//remove end card before adding new records
	lastRec = (String)peekLast();
	if (lastRec.startsWith("END")) removeLast();
	//add new header info to primary header
	addrecs(currHeader);
      }
      //update bitpix if changing data type
      if (cast == CAST_AS_INTS) {
	replace("BITPIX",32);
      } else if (cast == CAST_AS_FLOATS) {
	replace("BITPIX",-32);
      }
      //add END card if necessary 
      
      lastRec = (String)peekLast();
      if (!lastRec.startsWith("END")) {
	System.out.println("ADDING END");
	this.end();
      }

      if (filename == null) {
	if ((filename = chooseFITSfileName((data.get(0)).name())) == null) return 0;
      }

      try {
        FileOutputStream foutps = writeFITSheader(filename);
	if (foutps != null) {
	  if(_verbose) System.out.println("UFFITSheader.writeFITSmef> writing FITS data...");
	} else {
          System.err.println("UFFITSheader.writeFITSfile> File Output Stream is NULL.");
          return 0;
	}
	if (data.size() == 1) {
	  //this is a single extension file
	  currData = data.get(0);
	  if (cast == CAST_AS_INTS && !(currData instanceof UFInts)) {
	    currData = castProtocolAsInts(currData);
	  } else if (cast == CAST_AS_FLOATS && !(currData instanceof UFFloats)) {
            currData = castProtocolAsFloats(currData);
	  }
	  int nbdata = currData.sendData(new DataOutputStream(foutps));
	  nbytes += nbdata;
          if (_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote "+nbdata+" bytes of data."); 
          if (nbdata % 2880 > 0) {
	    /* Pad data with blank recs */
	    int nextra = 2880 - nbdata % 2880;
	    byte[] dblanks = new byte[nextra];
	    for (int j = 0; j < dblanks.length; j++) dblanks[j] = 0;
	    foutps.write(dblanks);
	    if (_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote "+nextra+" bytes of blank data.");
            nbytes += nextra;
          }
	} else for (int i = 0; i < data.size(); i++) { 
	  //this is a MEF.  Extension 1 should be a blank primary header.  Extensions 2 - n
	  //are ext header + data
	  currData = data.get(i);
          if (cast == CAST_AS_INTS && !(currData instanceof UFInts)) {
            currData = castProtocolAsInts(currData);
          } else if (cast == CAST_AS_FLOATS && !(currData instanceof UFFloats)) {
            currData = castProtocolAsFloats(currData);
          }
	  //get extension FITS header
	  Vector<String> fitsHead = getBasicExtensionHeader(cast);
	  //add new recs if requested. header 0 = primary header.  header i+1 = header for data i
	  if (headers != null && headers.size() > i+1) {
	    currHeader = headers.get(i+1); 
	    for (int j = 0; j < currHeader.numVals(); j++) {
	      fitsHead.add(currHeader.stringAt(j));
	    }
	  }
	  //add END
          fitsHead.add("END");
          nheadRecs = 0;
	  //write out ext header and pad if necessary
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
          if(_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote " + nheadRecs + " header records.");
          //Pad with blank recs
          if( nheadRecs % 36 > 0 ) {
	    int nextra = 36 - nheadRecs % 36;
	    for (int j=0; j < nextra; j++ ) foutps.write( _blanks );
	    if (_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote "+nextra+" blank recs.");
	    nbytes += nextra*80;
          }
	  //write data
	  int nbdata = currData.sendData(new DataOutputStream(foutps));
          if (_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote "+nbdata+" bytes of data for ext. "+(i+1));
          nbytes += nbdata;
          if (nbdata % 2880 > 0) {
            /* Pad data with blank recs */
            int nextra = 2880 - nbdata % 2880;
            byte[] dblanks = new byte[nextra];
            for (int j = 0; j < dblanks.length; j++) dblanks[j] = 0;
            foutps.write(dblanks);
            if (_verbose) System.out.println("UFFITSheader::writeFITSmef> wrote "+nextra+" bytes of blank data for ext. "+(i+1));
            nbytes += nextra;
          }
	}
	foutps.close();
	return nbytes;
      } catch(IOException ioe) {
	System.err.println("UFFITSheader::writeFITSmef> ERROR: "+ioe.toString());
	return -0;
      }
    }

    public int writeFITSmef(String filename, Vector<UFProtocol> data) {
      return writeFITSmef(filename, data, null, 0);
    }

    public int writeFITSmef(String filename, Vector<UFProtocol> data, Vector<UFStrings> headers) {
      return writeFITSmef(filename, data, headers, 0);
    }

    public int writeFITSmef(String filename, Vector<UFProtocol> data, int cast) {
      return writeFITSmef(filename, data, null, cast);
    }

    //helper method to cast to Ints
    public UFInts castProtocolAsInts(UFProtocol ufp) {
      if (ufp instanceof UFBytes) {
	return new UFInts(ufp.name(), UFArrayOps.castAsInts(((UFBytes)ufp).values()));
      } else if (ufp instanceof UFShorts) {
        return new UFInts(ufp.name(), UFArrayOps.castAsInts(((UFShorts)ufp).values()));
      } else if (ufp instanceof UFFloats) {
	return new UFInts(ufp.name(), UFArrayOps.castAsInts(((UFFloats)ufp).values()));
      } else if (ufp instanceof UFInts) {
	return (UFInts)ufp;
      } else {
	System.out.println("UFFITSheader::castProtocolAsInts> Error: invalid type");
	return null;
      }
    }
	
    //helper method to cast to floats
    public UFFloats castProtocolAsFloats(UFProtocol ufp) {
      if (ufp instanceof UFShorts) {
        return new UFFloats(ufp.name(), UFArrayOps.castAsFloats(((UFShorts)ufp).values()));
      } else if (ufp instanceof UFInts) {
        return new UFFloats(ufp.name(), UFArrayOps.castAsFloats(((UFInts)ufp).values()));
      } else if (ufp instanceof UFFloats) {
        return (UFFloats)ufp;
      } else {
        System.out.println("UFFITSheader::castProtocolAsFloats> Error: invalid type");
        return null;
      }
    }

    //helper method to set up basic extension header
    public Vector<String> getBasicExtensionHeader(int cast) {
      int bitpix = _frameConfig.depth;
      if (cast == CAST_AS_INTS) {
	bitpix = 32;
      } else if (cast == CAST_AS_FLOATS) {
	bitpix = -32;
      }
      Vector<String> fitsHead = new Vector(8);
      fitsHead.add(fitsKey("XTENSION","IMAGE","Image extension", true));
      fitsHead.add(fitsKey("BITPIX", bitpix, "array data type"));
      fitsHead.add(fitsKey("NAXIS", 2, "number of array dimensions"));
      fitsHead.add(fitsKey("NAXIS1", _frameConfig.width, ""));
      fitsHead.add(fitsKey("NAXIS2",_frameConfig.height, ""));
      fitsHead.add(fitsKey("PCOUNT",0,"number of parameters"));
      fitsHead.add(fitsKey("GCOUNT",1,"number of groups"));
      return fitsHead;
    }

    //static helper methods for creating valid FITS 80 char line from key, value, comment 
    public static String fitsKey(String key, int value, String comment) {
      return fitsKey(key, ""+value, comment, false);
    }

    public static String fitsKey(String key, float value, String comment) {
      return fitsKey(key, ""+value, comment, false);
    }

    public static String fitsKey(String key, String value, String comment) {
      return fitsKey(key, value, comment, false);
    }

    public static String fitsKey(String key, String value, String comment, boolean quote) {
      while (key.length() < 8) key += " ";
      if (quote) {
        value = "'"+value;
        while(value.length() < 9) value += " ";
        value += "'";
        while(value.length() < 20) value += " ";
      } else {
        while (value.length() < 20) value = " "+value;
      }
      if (comment == null) {
        comment = " /";
      } else comment = " / "+comment;
      String retVal = key+"= "+value+comment;
      if (retVal.length() > 80) retVal = retVal.substring(0,80);
      while (retVal.length() < 80) retVal += " ";
      return retVal;
    }


//----------------------------------------------------------------------------------------------------
    // write FITS header and data to a file:

    public int writeFITSfile( UFInts data, String filename, String[] moreHeaderInfo )
    {
	if( data == null ) {
	    System.err.println("UFFITSheader.writeFITSfile> data object is empty.");
	    return 0;
	}

	if( moreHeaderInfo != null ) addrecs( moreHeaderInfo );
	this.end();

	if( filename == null )
	    if( (filename = chooseFITSfileName(data.name())) == null ) return 0;

	FileOutputStream foutps = writeFITSheader( filename );

	if( foutps != null ) {
	    if(_verbose) System.out.println("UFFITSheader.writeFITSfile> writing data...");
	    try {
		int nbwritten = data.sendData( new DataOutputStream( foutps ) );
		foutps.close();
		System.out.println("UFFITSheader.writeFITSfile> " + nbwritten +
				   " bytes of data written to: " + filename);
		return nbwritten;
	    }
	    catch(IOException ioe) {
		System.err.println("UFFITSheader.writeFITSfile> " + ioe.toString());
		ioe.printStackTrace();
		try{ foutps.close(); } catch(Exception ex) {}
		return 0;
	    }
	}
	else {
	    System.err.println("UFFITSheader.writeFITSfile> File Output Stream is NULL.");
	    return 0;
	}
    }

    public boolean rewriteFITSfile() {
	if(_filename == null)
	    if((_filename = chooseFITSfileName()) == null) return false;

	try {
	    File file = new File(_filename);
	    RandomAccessFile raFile = new RandomAccessFile(file, "rw");
	    raFile.seek(0);

	    for(int i = 0; i < this.size(); i++) 
		raFile.writeBytes((String)this.get(i));

	    raFile.seek(0);
	    raFile.close();
	    return true;
	}
	catch(FileNotFoundException fnfe) { System.err.println("File [" + _filename + "] not found."); }
	catch(IOException ioe) { System.err.println("UFFITSheader>rewriteFITSfile "); ioe.printStackTrace(); }
	catch(Exception e) { e.printStackTrace(); }	
	return false;
    }

    public int writeFITSfile( int[] data, String dataname, String filename, String[] moreHeaderInfo )
    {
	return writeFITSfile( new UFInts(dataname, data), filename, moreHeaderInfo );
    }

    public int writeFITSfile( int[] data, String filename, String[] moreHeaderInfo )
    {
	return writeFITSfile( data, "data", filename, moreHeaderInfo );
    }
//----------------------------------------------------------------------------------------------------

    // read FITS header and multiple data extension from a MEF file:
    // will also work on single ext FITS file and return Vector of length 1
    public Vector<UFProtocol> readFITSmef(String filename) {
	if (filename == null) return null;
	_filename = filename;
        FileInputStream finps = readFITSheader( filename );
	Vector<UFProtocol> data = new Vector();
	_extHeaders = new Vector(); //create new vector for extension headers
	int nframes = 0;
	boolean mef = false; 
        if (getParam("EXTEND").trim().toUpperCase().equals("T")) mef = true;
	if (getInt("NAXIS") > 0) mef = false;
	while (finps != null) {
	    try {
		if (finps.available() <= 0) {
		    finps.close();
		    break;
		}
	    } catch(IOException ioe) {
                System.err.println("UFFITSheader.readFITSmef> " + ioe.toString());
                ioe.printStackTrace();
                try{ finps.close(); } catch(Exception ex) {}
                break;
	    }
	    if (mef) _extHeaders.add(readFITSextension(finps)); else getFrameCon();
	    if (finps == null) {
		System.err.println("UFFITSheader.readFITSmef> File Input Stream is NULL.");
		break;
	    }
	    int npix =  _frameConfig.width * _frameConfig.height;
            String dname = filename.substring(filename.lastIndexOf("/")+1)+nframes;
            UFProtocol framedata = null;
            switch( _frameConfig.depth ) {
                case  8:
                    framedata = new UFBytes( dname, npix ); break;
                case 16:
                    framedata = new UFShorts( dname, npix ); break;
                case 32:
                    framedata = new UFInts( dname, npix ); break;
                case -32:
                    framedata = new UFFloats( dname, npix ); break;
                default:
                    framedata = new UFInts( dname, npix );
            }

            System.out.println("UFFITSheader.readFITSmef> reading "
                               + _frameConfig.width + " X " + _frameConfig.height + " = " + framedata.elements() + " of "
                               + _frameConfig.depth + " bit pixels from file: " + filename + ", extension "+nframes+"...");
            try {
                int nbread = framedata.recvData( new DataInputStream( finps ) );
                System.out.println("UFFITSheader.readFITSmef> " + nbread +
                                   " bytes of data read from: " + filename + ", extension "+nframes);
		nframes++;
                if(framedata instanceof UFShorts && _unsigned)
                    framedata = (UFProtocol)new UFunsignShorts( (UFShorts)framedata );

		data.add(framedata);
                if (nbread % 2880 > 0) {
                  /* data padded with blank recs */
                  int nextra = 2880 - nbread % 2880;
                  byte[] dblanks = new byte[nextra];
		  finps.read(dblanks);
                  if (_verbose) System.out.println("UFFITSheader::readFITSmef> read "+nextra+" bytes of blank data");
                }
                if (finps.available() <= 0) {
                    finps.close();
                    break;
                }
            }
            catch(IOException ioe) {
                System.err.println("UFFITSheader.readFITSmef> " + ioe.toString());
                ioe.printStackTrace();
                try{ finps.close(); } catch(Exception ex) {}
		break;
            }
	}
	System.out.println("UFFITSheader.readFITSmef> read "+nframes+" frames from "+filename);
	return data;
    }

//----------------------------------------------------------------------------------------------------

    // read FITS header and data from a file:

    public UFProtocol readFITSfile( String filename, String name )
    {
	if( filename == null )
	    if( (filename = chooseFITSfileName( name, true )) == null ) return null;

	_filename = filename;
	FileInputStream finps = readFITSheader( filename );
	getFrameCon();

	if(getParam("EXTEND").trim().toUpperCase().equals("T"))
	    readFITSextension(finps);

	if( finps != null ) {

	    int npix = _frameConfig.width * _frameConfig.height;
	    String dname = filename.substring(filename.lastIndexOf("/")+1);
	    UFProtocol data = null;

	    switch( _frameConfig.depth ) {
		case  8:
		    data = new UFBytes( dname, npix ); break;
		case 16:
		    data = new UFShorts( dname, npix ); break;
		case 32:
		    data = new UFInts( dname, npix ); break;
		case -32:
		    data = new UFFloats( dname, npix ); break;
		default:
		    data = new UFInts( dname, npix );
	    }

	    System.out.println("UFFITSheader.readFITSfile> reading "
			       + _frameConfig.width + " X " + _frameConfig.height + " = " + data.elements() + " of "
			       + _frameConfig.depth + " bit pixels from file: " + filename + "...");
	    try {
		int nbread = data.recvData( new DataInputStream( finps ) );
		finps.close();
		System.out.println("UFFITSheader.readFITSfile> " + nbread +
				   " bytes of data read from: " + filename);
		if(data instanceof UFShorts && _unsigned) 
		    return (UFProtocol)new UFunsignShorts( (UFShorts)data );
		    
		return (UFProtocol)data;
	    }
	    catch(IOException ioe) {
		System.err.println("UFFITSheader.readFITSfile> " + ioe.toString());
		ioe.printStackTrace();
		try{ finps.close(); } catch(Exception ex) {}
		return null;
	    }
	}
	else {
	    System.err.println("UFFITSheader.readFITSfile> File Input Stream is NULL.");
	    return null;
	}
    }

    public UFProtocol readFITSfile( String filename ) { return readFITSfile( filename, "data" ); }

    public UFProtocol readFITSfile() { return readFITSfile( null, "data" ); }

//----------------------------------------------------------------------------------------------------

    public String chooseFITSfileName() { return chooseFITSfileName("data", false); }
    public String chooseFITSfileName(String name) { return chooseFITSfileName( name, false ); }
    public String chooseFITSfileName(boolean toRead) { return chooseFITSfileName("data", toRead); }

    public String chooseFITSfileName(String name, boolean toRead)
    {
	String filename = null;
	JFileChooser jfc = new JFileChooser(_path);
	ExtensionFilter ftype = new ExtensionFilter("FITS files:", new String[]{".fit",".fits"});
	jfc.setFileFilter(ftype);
	int jfcRetVal;

	if( toRead ) {
	    jfc.setDialogTitle("Read > " + name + " < from FITS file:");
	    jfcRetVal = jfc.showOpenDialog(null);
	}
	else {
	    jfc.setDialogTitle("Write > " + name + " < to FITS file:");
	    jfcRetVal = jfc.showSaveDialog(null);
	}

	if( jfcRetVal == JFileChooser.APPROVE_OPTION ) {
	    _path = jfc.getCurrentDirectory();
	    filename = jfc.getSelectedFile().getAbsolutePath();
	    if( filename.length() < 1 ) filename = "tmp.fits";
	    if( !filename.endsWith(".fit") && !filename.endsWith(".fits") ) filename += ".fits";
	}
	else System.out.println("UFFITSheader.chooseFITSfileName> canceled...");

	return filename;
    }
//----------------------------------------------------------------------------------------------------

    public FileOutputStream writeFITSheader(String filename)
    {
	try {
	    File theFile = new File( filename );

	    if( !theFile.createNewFile() ) {
		System.err.println("UFFITSheader.writeFITSheader> failed creating file: " + filename
				   + ", does file already exist ?");
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null,"File [" + filename + "] already exists.",
					      "ERROR", JOptionPane.ERROR_MESSAGE);		
		return null;
	    }

	    FileOutputStream foutps = new FileOutputStream( theFile );
	    ListIterator Lit = this.listIterator();

	    while( Lit.hasNext() ) {
		String frec = (String)Lit.next();
		if( frec.length() > 80 )
		    foutps.write( frec.getBytes(), 0, 80 );
		else {
		    foutps.write( frec.getBytes() );
		    if( frec.length() < 80 ) foutps.write( _blanks, 0, 80 - frec.length() );
		}
	    }

	    if(_verbose) System.out.println("UFFITSheader.writeFITSheader> wrote "+size()+" records.");

	    if( this.size() % 36 > 0 ) {
		int nextra = 36 - this.size() % 36;
		for( int i=0; i < nextra; i++ ) foutps.write( _blanks );
		if(_verbose)
		    System.out.println("UFFITSheader.writeFITSheader> wrote "+nextra+" blank records.");
	    }

	    return foutps;
	}
	catch( Exception e ) {
	    System.err.println("UFFITSheader.writeFITSheader> "+e.toString());
	    return null;
	}
    }
//----------------------------------------------------------------------------------------------------
// method for choosing a file and just reading the FITS header, nothing else in file:

    public void readFITSheader() {

	if( (_filename = chooseFITSfileName("Header", true)) == null ) return;

	FileInputStream finps = readFITSheader( _filename );

	try { finps.close(); }
	catch(IOException ioe) { ioe.printStackTrace(); }
    }
//----------------------------------------------------------------------------------------------------

    public FileInputStream readFITSheader(String filename)
    {
	try {
	    File theFile = new File( filename );

	    if( !theFile.canRead() ) {
		System.err.println("UFFITSheader.readFITSheader> cannot read file: " + filename);
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null,"File [" + filename + "] is not readable.",
					      "ERROR", JOptionPane.ERROR_MESSAGE);		
		return null;
	    }
	    
	    FileInputStream finps = new FileInputStream( theFile );
	    return readFITSheader(finps);
	}
	catch( Exception e ) {
	    System.err.println("UFFITSheader.readFITSheader> "+e.toString());
	    return null;
	}
    }

    public FileInputStream readFITSheader(FileInputStream finps) {
	this.clear();
	try {
	    byte[] frec = new byte[80];
	    boolean end = false;
	    int nrec=0;
	    
	    while( !end ) {
		finps.read( frec );
		++nrec;
		String Line = new String( frec );
		if( Line.indexOf("END") == 0 ) end = true;
		
		if( end )
		    this.end();
		else if( this.addrec( Line ) < 0 ) {
		    System.err.println("UFFITSheader.readFITSheader> rec # " + nrec + " is bad ? ");
		    System.err.println("UFFITSheader.readFITSheader> rec = " + Line);
		}
	    }
	    
	    if( nrec % 36 > 0 ) {
		int nextra = 36 - nrec % 36;
		for( int i=0; i < nextra; i++ ) finps.read( frec );
		if(_verbose)
		    System.out.println("UFFITSheader.readFITSheader> read "+nextra+" blank recs.");
	    }
	}
	catch(EOFException eof) {
	    System.err.println("UFFITSheader.readFITSheader> " + eof.toString());
	}
	catch(IOException ioe) {
	    System.err.println("UFFITSheader.readFITSheader> "+ ioe.toString());
	}
	catch( Exception e ) {
	    System.err.println("UFFITSheader.readFITSheader> " + e.toString());
	}

	if(_verbose) System.out.println("UFFITSheader.readFITSheader> read " + size() + " FITS recs.");
	
	return finps;
    }

    public boolean isFile() { return _filename != null; }

    public String getFilename() { return _filename; }

//----------------------------------------------------------------------------------------------------

    public UFFITSheader readFITSextension(FileInputStream finps)
    {
	UFFITSheader extensionHeader = new UFFITSheader();
	extensionHeader.readFITSheader(finps);
	
	if(extensionHeader.getParam("XTENSION").indexOf("IMAGE") >= 0) {
	    int bitpix = extensionHeader.getInt("BITPIX");
	    int nax = extensionHeader.getInt("NAXIS");
	    int npx = extensionHeader.getInt("NAXIS1");
	    int npy = 1;
	    if( nax > 1 ) npy = extensionHeader.getInt("NAXIS2");
	    _frameConfig = new UFFrameConfig(npx, npy, bitpix);

	    if (extensionHeader.hasRecord("BZERO")) {
		if(extensionHeader.getInt("BZERO") == 32768) _unsigned = true;
	    }
	}
	return extensionHeader;
    }

//----------------------------------------------------------------------------------------------------
    // methods to add FITS records (keyword = value / comment) to the FITS header:

    public int addrec(String keyword, String value, String comment) {
	return _addrec( keyword, value, comment, true );
    }

    public int addrec(String keyword, int value, String comment) {
	return _addrec( keyword, value + "", comment, false );
    }

    public int addrec(String keyword, float value, String comment) {
	return _addrec( keyword, value + "", comment, false );
    }

    public int addrec(String keyword, double value, String comment) {
	return _addrec( keyword, value + "", comment, false );
    }

    public boolean replace(String keyword, String value, String comment, boolean quote) {
	_setrec( keyword, value, comment, quote );
	return _replace();
    }

    public boolean replace(String keyword, String value, String comment) {
	_setrec( keyword, value, comment, true );
	return _replace();
    }
    public boolean replace(String keyword, int value, String comment) {
	_setrec( keyword, value + "", comment, false );
	return _replace();
    }
    public boolean replace(String keyword, float value, String comment) {
	_setrec( keyword, value + "", comment, false );
	return _replace();
    }
    public boolean replace(String keyword, double value, String comment) {
	_setrec( keyword, value + "", comment, false );
	return _replace();
    }

    public boolean replace(String keyword, String value ) {
	_setrec( keyword, value, this.getComment(keyword), true );
	return _replace();
    }
    public boolean replace(String keyword, int value ) {
	_setrec( keyword, value + "", this.getComment(keyword), false );
	return _replace();
    }
    public boolean replace(String keyword, float value ) {
	_setrec( keyword, value + "", this.getComment(keyword), false );
	return _replace();
    }
    public boolean replace(String keyword, double value ) {
	_setrec( keyword, value + "", this.getComment(keyword), false );
	return _replace();
    }
//----------------------------------------------------------------------------------------------------

    ///helper for ctors: sets the value of _FITSrecord and _FITSkey.

    protected int _setrec(String keyword, String value, String comment, boolean quote)
    {
	_FITSkey = rmJunk(keyword).toUpperCase();
	String valq = rmJunk(value);
	String cmt = rmJunk(comment);
	int vlen = valq.length();
	int klen = _FITSkey.length();
	int clen = cmt.length();
	int cmax = 47;
	int vmax = 64;

	if( quote ) { //option for quoted string value that can be more than 20 chars:
	    if( vlen > vmax)
		valq = valq.substring(0,vmax);
	    valq = "'" + valq + "'";
	    vlen = valq.length();
	    if( vlen < 20)
		valq += _blankLine.substring( 0, 20 - vlen );
	    else
		cmax = 47 - vlen + 20;
	}
	else {
	    if( vlen > 20)
		valq = valq.substring(0,20);
	    else if( vlen < 20)
		valq = _blankLine.substring( 0, 20 - vlen ) + valq;
	}

	if( klen > 8 )
	    _FITSkey = _FITSkey.substring(0,8);
	else if( klen < 8 )
	    _FITSkey += _blankLine.substring( 0, 8 - klen );

	if( clen > cmax )
	    cmt = cmt.substring(0,cmax);
	else if( clen < cmax)
	    cmt += _blankLine.substring( 0, cmax - clen );

	_FITSrecord = _FITSkey + "= " + valq + " / " + cmt;

	if(_verbose) System.out.println("UFFITSheader._setrec> _FITSrecord=["
					+ _FITSrecord + "] " + _FITSrecord.length());

	return _FITSrecord.length();
    }
//----------------------------------------------------------------------------------------------------
//more helper funcs:

    protected int _addrec(String keyword, String value, String comment, boolean quote)
    {
	if( _setrec( keyword, value, comment, quote ) == 80 ) addLast( _FITSrecord );
	return this.size();
    }

    ///helper for ctors: adds one standard rec containing SIMPLE keyword:

    protected int _start(String comment, boolean simple)
    {
	this.clear();

	if( simple )
	    return _addrec("SIMPLE","T", comment, false );
	else
	    return _addrec("SIMPLE","F", comment, false );
    }

    ///helper for ctors: adds one standard rec containing EXTEND keyword = T:

    protected int _extend()
    {
	return _addrec("EXTEND","T","FITS dataset contains extensions.", false );
    }

    ///helper for replace funcs: uses current value of attrib _FITSkey:

    protected boolean _replace() {

	int cnt = this.size();

	for( int i=0; i < cnt; ++i ) {
	    String entry = (String)this.get(i);
	    if( entry.indexOf(_FITSkey) == 0 ) {
		remove(i);
		add( i, _FITSrecord );
		return true;
	    }
	}

	if(_verbose) System.out.println("UFFITSheader._replace> did not find _FITSkey=["+_FITSkey+"]");
	return false;
    }
//----------------------------------------------------------------------------------------------------

    public int addrec(Map valhash, Map comments) {
	if (valhash.isEmpty())
	    return this.size();
	Iterator i  = valhash.keySet().iterator();
	String key, val, comment;
	while (i.hasNext()) {
	    key = (String) i.next();
	    val = (String) valhash.get(key);
	    if (comments.containsKey(key))
		comment = (String) comments.get(key);
	    else
		comment = "no comment";
	    addrec(key, val, comment);
	}
	return this.size();
    }

    // assuming args are in FITS header entry format
    // trucation or extension to 80 chars may occur:

    public int addrec(String s) {

	if( s.indexOf("=") != 8 ) {
	  if (s.indexOf("HISTORY") != 0 && s.indexOf("HIERARCH") != 0) return(-1); // not a FITS header entry
	}
	int slen = s.length();

	if( slen > 80)
	    s = s.substring(0,80);
	else if( slen < 80)
	    s += _blankLine.substring( 0, 80 - slen );

	addLast(s);
	return this.size();
    }

    public int addrecs(String[] sar) {
	if( sar == null )
	    return this.size();
	
	for( int i=0; i < sar.length ; ++i ) addrec( sar[i] );
	return this.size();
    }

    public int addrecs( UFStrings ufs ) {

	if (ufs == null) return this.size();
	
	for( int i=0; i < ufs.numVals(); ++i ) addrec( (String)ufs.valData(i) );

	_name = ufs.name();
	return this.size();
    }

    public int addrecs(UFFITSheader fhrecs) {

	if (fhrecs == null) return this.size();

	ListIterator Lit = fhrecs.listIterator();
	while( Lit.hasNext() ) addLast( Lit.next() );

	return this.size();
    }

    public int end() { // this adds the "END" record to header
	_FITSrecord = "END";
	_FITSrecord += _blankLine.substring( 0, 80 - _FITSrecord.length() );
	addLast(_FITSrecord);
	return this.size();
    }

    public int date(String comment) {
	SimpleDateFormat df = new SimpleDateFormat("yyyy:DDD:HH:mm:ss");
	String time = df.format(new Date());
	return addrec("DATE_FH", time, comment);
    }

    public int date() { return this.date("UT of header creation (YYYY:DAY:HH:MM:SS)"); }

    public String name() { return _name; }

    // convert UFFITSheader object into UFStrings object for client/server transactions:

    public UFStrings Strings( String name ) {

	String[] fhrecs = new String[this.size()];
	ListIterator Lit = this.listIterator();
	int i=0;
	while( Lit.hasNext() ) fhrecs[i++] = (String)Lit.next();

	return new UFStrings( name, fhrecs );
    }

    public UFStrings Strings() { return this.Strings(_name); }

//----------------------------------------------------------------------------------------------------

    public static String parseKey( String rec ) {
	if( rec.length() != 80 ) {
	    System.err.println("UFFITSheader.parseKey> rec [" + rec + "] not valid size.");
	    return null;
	}

	if( rec.charAt(8) != '=' ) {
	    System.err.println("UFFITSheader.parseKey> rec [" + rec + "] not valid key.");
	    return null;
	}

	return rec.substring(0, 8).toUpperCase();
    }

    public static String parseValue( String rec ) {
	if( rec.length() != 80 ) {
	    System.err.println("UFFITSheader.parseComment> rec [" + rec + "] not valid size.");
	    return null;
	}

	int iqb = rec.indexOf("'");
	if( iqb > 7 ) {
	    int iqe = rec.lastIndexOf("'");
	    if( iqe < 0 ) iqe = 20 + iqb;
	    return rec.substring( iqb, iqe + 1);
	}
	else {
	    int ieq = rec.indexOf("=");
	    if( ++ieq > 1 )
		return rec.substring( ieq, 21+ieq ).trim();
	    else {
		System.err.println("UFFITSheader.parseValue> = not found in:[" + rec + "].");
		return("");
	    }
	}
    }
    
    public static String parseComment( String rec ) {
	if( rec.length() != 80 ) {
	    System.err.println("UFFITSheader.parseComment> rec [" + rec + "] not valid size.");
	    return null;
	}

	
	int z = rec.indexOf(" / ");
	while(z < 30 && z != -1)
	    z = rec.substring(z).indexOf(" / ");
	
	if(z >= 30)
	    return rec.substring(z+2).trim();
	return "";
    }

    public static boolean isQuote( String rec ) {
	if( rec.length() != 80 ) {
	    System.err.println("UFFITSheader.isQuote> rec [" + rec + "] not valid size.");
	}

	if(rec.indexOf("\"") != -1)
	    return true;
	return false;
    }

//----------------------------------------------------------------------------------------------------

    /* Check if record exists before asking for it sometimes */
    public boolean hasRecord( String key ) {
        String ukey = key.toUpperCase();
	//HIERARCH keywords allow lower case
	//if (key.indexOf("HIERARCH") == 0) ukey = key;

        int cnt = this.size();
        for(int i = this.size() - 1; i >= 0; i--) {
            String rec = (String)this.get(i);
	    if (key.indexOf("HIERARCH") == 0) rec = rec.toUpperCase(); //HIERARCH keywords allow lower case
            if( rec.indexOf( ukey ) == 0 ) return true;
        }

        return false;
    }

//----------------------------------------------------------------------------------------------------

    public String getRecord( String key ) {
	String ukey = key.toUpperCase();
        //HIERARCH keywords allow lower case
        //if (key.indexOf("HIERARCH") == 0) ukey = key;

	int cnt = this.size();
	for(int i = this.size() - 1; i >= 0; i--) {
	    String rec = (String)this.get(i);
	    if (key.indexOf("HIERARCH") == 0) rec = rec.toUpperCase(); //HIERARCH keywords allow lower case
	    if( rec.indexOf( ukey+" " ) == 0  || rec.indexOf( ukey+"=") == 0) return rec;
	}

	System.err.println("UFFITSheader.getRecord> key [" + ukey + "] not found.");
	return null;
    }

    public String getParam( String key ) {

	String rec = getRecord( key );

	if( rec != null ) {
	    int iqb = rec.indexOf("'");
	    if( ++iqb > 1 ) {
		int iqe = rec.lastIndexOf("'");
		if( iqe < 0 ) iqe = 20 + iqb;
		return rec.substring( iqb, iqe );
	    }
	    else {
		int ieq = rec.indexOf("=");
		if( ++ieq > 1 )
		    return rec.substring( ieq, 21+ieq ).trim();
		else {
		    System.err.println("UFFITSheader.getParam> = after key:[" + key + "] not found.");
		    return("");
		}
	    }
	}
	else return("");
    }

    public String getComment( String key ) {

	String rec = getRecord( key );

	if( rec != null ) {
	    int z = rec.indexOf(" / ");
	    while(z < 30 && z != -1)
		z = rec.substring(z).indexOf(" / ");
	    if(z >= 30)
		return rec.substring(z+2).trim();
	    else
		return "";
	}
	else
	    return("");
    }

    public int getInt( String key ) {

	String param = getParam( key );
	if( param == null ) return(-9999);

	try {
	    return Integer.parseInt( param );
	}
	catch( NumberFormatException nfe ) {
	    System.err.println("UFFITSheader.getInt> key=" + key +
			       ": param=" + param + ": " + nfe.toString() );
	    return(-9999);
	}
    }

    public float getFloat( String key ) {

	String param = getParam( key );
	if( param == null ) return(-9999);

	try {
	    return Float.parseFloat( param );
	}
	catch( NumberFormatException nfe ) {
	    System.err.println("UFFITSheader.getFloat> key=" + key +
			       ": param=" + param + ": " + nfe.toString() );
	    return(-9999);
	}
    }

    public double getDouble( String key ) {

	String param = getParam( key );
	if( param == null ) return(-9999);

	try {
	    return Double.parseDouble( param );
	}
	catch( NumberFormatException nfe ) {
	    System.err.println("UFFITSheader.getDouble> key=" + key +
			       ": param=" + param + ": " + nfe.toString() );
	    return(-9999);
	}
    }
//----------------------------------------------------------------------------------------------------
    //extended header functions

    public String getRecord( int i, String key) {
        if (_extHeaders.size() > i) return _extHeaders.get(i).getRecord(key);
        return null;
    }

    public int getInt( int i, String key) {
        if (_extHeaders.size() > i) return _extHeaders.get(i).getInt(key);
        return(-9999);
    }

    public float getFloat( int i, String key) {
        if (_extHeaders.size() > i) return _extHeaders.get(i).getFloat(key);
        return(-9999);
    }

    public double getDouble( int i, String key) {
        if (_extHeaders.size() <= i) return _extHeaders.get(i).getDouble(key);
	return(-9999);
    }

//----------------------------------------------------------------------------------------------------
    // convenience functions:

    public static UFStrings asStrings(Map valhash, Map comments) {
	UFFITSheader _fitshdr = new UFFITSheader();
	int sz = _fitshdr.addrec(valhash, comments);
	if (sz <= 0)
	    return null;
	return _fitshdr.Strings();
    }

    // convenience functions for client connection to an Agent to fetch FITS fragment
    public static UFStrings fetchFITS(String clientname, Socket soc) {
	if (soc == null)
	    return null;
	//formulate standard agent client key, request:
	String [] vs = { "STATUS", "FITS" };
	UFStrings req = new UFStrings(clientname, vs);
	req.sendTo(soc);
	UFStrings reply = (UFStrings) (UFProtocol.createFrom(soc));
	return reply;
    }

    public static String rmJunk(String st)
    {
	StringBuffer sb = new StringBuffer(st);
	if( sb.length() <= 0 ) {
	    System.err.println("UFFITSheader::rmJunk> empty string ?");
	    return "";
	}
	
	int pos = sb.toString().indexOf("\n");
	while( pos != -1 ) {
	    sb.replace(pos, pos+1, " "); // replace with space
	    pos = sb.toString().indexOf("\n", 1+pos);
	}
	pos = sb.toString().indexOf("\r");
	while( pos != -1 ) {
	    sb.replace(pos, pos+1, " "); // replace with space
	    pos = sb.toString().indexOf("\r", 1+pos);
	}
	
	// eliminate white (adjacent) multiples
	for( pos = 0; pos < sb.length(); ++pos ) {
	    while ( (sb.charAt(pos) == ' ' || sb.charAt(pos) == '\t') && pos+1 < sb.length()
		    && (sb.charAt(pos+1) == ' ' || sb.charAt(pos+1) == '\t'))
		sb.deleteCharAt(pos);
	}

	return sb.toString().trim(); // remove leading and trailing whitespace and return
    }
//------------------------------------------------------------------------------------------

    public static void main(String[] args) {

	boolean fixHeader = false, readData = false;
	boolean readmef = false;
	String file = null;

	for (int i = 0; i < args.length; i++) {
	    if( args[i].equals("-fix") ) fixHeader = true;
	    if( args[i].equals("-data") ) readData = true;
	    if (args[i].equals("-mef")) readmef = true;
	    if (args[i].equals("-file") ) file = args[i+1];
	}

	if (readmef && file != null) {
	  UFFITSheader fitsHeader = new UFFITSheader();
	  fitsHeader.readFITSmef(file);
	  System.exit(0);
	}

	final UFFITSheader fitsHeader = new UFFITSheader();

	if( readData )
	    fitsHeader.readFITSfile();
	else
	    fitsHeader.readFITSheader();

	final UFTextArea textArea = new UFTextArea(fitsHeader.Strings());

	if( fixHeader ) {
	    JFrame fixFrame = new JFrame("Fix FITS Header File");
	    JPanel labelPanel = new JPanel(new RatioLayout());
	    JPanel textPanel = new JPanel(new RatioLayout());
	    JPanel buttonPanel = new JPanel(new RatioLayout());
	    final JButton updateButton = new JButton("Update");
	    final JTextField keyText = new JTextField("");
	    final JTextField paramText = new JTextField("");
	    final JTextField commentText = new JTextField("");

	    keyText.setHorizontalAlignment(JTextField.CENTER);
	    paramText.setHorizontalAlignment(JTextField.CENTER);
	    commentText.setHorizontalAlignment(JTextField.CENTER);

	    paramText.setEnabled(false);
	    commentText.setEnabled(false);

	    keyText.addCaretListener(new CaretListener() { 
		    public void caretUpdate(CaretEvent e) {
			if(keyText.getText().length() > 0) {
			    String k = keyText.getText().trim().toUpperCase();
			    String rec = fitsHeader.getRecord(k);
			    if(rec != null && rec.indexOf("END") != 0 && UFFITSheader.parseKey(rec).trim().equals(k)) {
				paramText.setText(UFFITSheader.parseValue(rec));
				commentText.setText(UFFITSheader.parseComment(rec));
				paramText.setEnabled(true);
				commentText.setEnabled(true);
				updateButton.setEnabled(true);
			    }
			    else {
				paramText.setText("");
				commentText.setText("");
				paramText.setEnabled(false);
				commentText.setEnabled(false);
				updateButton.setEnabled(false);
			    }
			}
		    } });
	    
	    updateButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String k = keyText.getText().trim().toUpperCase();
			String c = commentText.getText().trim();
			String v = paramText.getText().trim();
			boolean quote = (v.indexOf("\'") >= 0);
			if(quote)
			    v = v.substring(1, v.length()-1);

			
			if(fitsHeader.replace(k, v, c, quote)) {
			    keyText.setText("");
			    paramText.setText("");
			    commentText.setText("");
			    paramText.setEnabled(false);
			    commentText.setEnabled(false);
			    updateButton.setEnabled(false);

			    if(fitsHeader.rewriteFITSfile())
				textArea.redisplay(fitsHeader.Strings());
			    else
				System.err.println("Failed to rewrite file!");
			}
			else
			    System.err.println("Failed to update: key=" + k + " val=" + v + " com=" + c);
		    } });

	    fixFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    fixFrame.setSize(700, 110);
	    fixFrame.setLayout(new GridLayout(3,1));
	    fixFrame.setResizable(false);
	   
	    labelPanel.add("0.00,0.00;0.13,1.00", new JLabel("Key", JLabel.CENTER));
	    labelPanel.add("0.13,0.00;0.28,1.00", new JLabel("Value", JLabel.CENTER));
	    labelPanel.add("0.41,0.00;0.59,1.00", new JLabel("Comment", JLabel.CENTER));

	    textPanel.add("0.00,0.00;0.13,1.00", keyText);
	    textPanel.add("0.13,0.00;0.28,1.00", paramText);
	    textPanel.add("0.41,0.00;0.59,1.00", commentText);

	    buttonPanel.add("0.40,0.00;0.20,1.00", updateButton);

	    fixFrame.add(labelPanel);
	    fixFrame.add(textPanel);
	    fixFrame.add(buttonPanel);
	    
	    fixFrame.setVisible(true);	   
	}
    }
}
