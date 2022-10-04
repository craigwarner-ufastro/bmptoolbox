package javaMMTLib;
/**
 * Title:        UFMMTImgHeader.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  For reading and managing MMT .img headers 
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javaUFProtocol.*;

public class UFMMTImgHeader {
  public static final
      String rcsID = "$Name:  $ $Id: UFMMTImgHeader.java,v 1.8 2013/12/09 22:07:54 warner Exp $";

  protected String _name = "UFMMTImgHeader";
  protected static boolean _verbose = false;
  protected byte[] byteHeader;

  short ByteSwap; // LSB is first if 0x0000, MSB is first if 0xffff
  short Xsize; // number of pixels in a line (# of columns)
  short Ysize; // number of lines (# of rows)
  short SampleSize; // # of bytes/pixel
  short RowColOrder; // 0000 if column major, ffff if row major
  short Yorigin; // 0000 if lower left, ffff if upper left
  short Year; // Date and time of start of capture
  short Month;
  short Day;
  short Hour;
  short Minute;
  short Second;
  short IntensityCold; // Average ADC at low flux (rounded to nearest integer)
  float TemperatureCold; // Temperature entered by user for low flux
  short IntensityHot; // Average ADC at high flux (rounded to nearest integer)
  float TemperatureHot; // Temperature entered by user for high flux
  float TargetGain; // Target gain setting (normally 1.0)
  short Revision; // Image header revision number, Rev 12 after TBD 2006
  short Reserved1; // was # of image frames in file in Rev 10 or earlier, now 0
  short UL_Row; // Upper left point row# for where data was extracted from frame
  short UL_Col; // Upper left point col# for where data was extracted from frame
  short LR_Row; // Lower right point row# for where data was extracted from frame
  short LR_Col; // Lower right point col# for where data was extracted from frame
  short NumFramesSummed; // # frames summed in each image
  float FrameRate; // Frame rate of sensor
  float IntegrationTime; // Integration time for Tint 1 (if valid)
  float IntegrationTime2; // Integration time for Tint 2 (if used)
  float IntegrationTime3; // Integration time for Tint 3 (if used)
  float IntegrationTime4; // Integration time for Tint 4 (if used)
  char[] UserField = new char[18]; // User defined field
  short DataMin; // minimum valid integer for pixel value
  short DataMax; // maximum valid integer for pixel value
   // for frame sum data multiply these numbers
   // by the number of frames summed
  short Gain1; // Global gain setting channel 1
  short Offset1; // Global offset channel 1
  short Gain2; // Global gain setting channel 2
  short Offset2; // Global offset channel 2
  short PixelType;// data type for pixel
   //0, 16 bit signed integer
   //1, 8 bit unsigned integer
   //2, 32 bit signed integer
   //3, 32 bit float
   //4, 64 bit float
  // These entries are used for radiometry calculations
  float CalIntensity1; // average ADC for low flux
  float CalIntenisty2; // average ADC for high flux
  float Photon1; // Photons/cm^2sec at low flux
  float Photon2; // Photons/cm^2sec at high flux
  float Power1; // Watts/cm^2 at low flux
  float Power2; // Watts/cm^2 at high flux
  short LinearFlag; //0, response calibration invalid
   //1, detector response linear to photons
   //2, detector response linear to power
  short PxlSubst; // 0- no pixel substitution
  char[] UserField2 = new char[100]; //User defined field
  byte[] Comment = new byte[80]; // User comment
  short Gain3; // Global gain setting channel 3
  short Offset3; // Global offset channel 3
  short Gain4; // Global gain setting channel 4
  short Offset4; // Global offset channel 4
  short FPAGain; // FPA gain setting in video setup
   // actual FPA gain setting is setup dependent
  short FileType; // 0 binary image file
  short NUCflag; // non-zero if 2 pt correction loaded
  short NoDigCor; // non-zero if uniformity gain/offset is loaded
  float MinVolt; // Input referred voltage for Datamin
  float MaxVolt; // Input referred voltage for Datamax
  int NumFramesEx; // has number of frames in Version, Rev 11 and later
  // Rev 12 and Later Extended Format
  short ExtendedFormat; // 0 = 512 Hdr followed by frame data,
   // 1 = 512 byte hdr followed by frame data and 512 byte gps record per frame
   // i.e. Offset to frame N is 512 hdr + (framesize + 512) * N (where N = 0, 1)
  short IntEveryNframes; // Number of streaming frames between FB40 interrupts for sequential save.
   // This affects the tagging of the high performance clock data in the FB40 driver
  short SkipEveryNframes; // Number of frames between FB40 frames for "sequential" save, which effectively
   // reduces the data rate saved into the resulting file. Value of -1 means not used.

  ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

  public UFMMTImgHeader() {
    /* Empty Header */
    this(new byte[512]);
  }

  public UFMMTImgHeader(UFBytes header) {
    this(header.values());
  }

  public UFMMTImgHeader(byte[] header) {
    byteHeader = header;
    ByteBuffer bb = ByteBuffer.wrap(header);
    ByteSwap = bb.getShort();
    if (ByteSwap == -1) {
      byteOrder = ByteOrder.LITTLE_ENDIAN;
    } else byteOrder = ByteOrder.BIG_ENDIAN; 
    bb.order(byteOrder);
    Xsize = bb.getShort();
    Ysize = bb.getShort();
    SampleSize = bb.getShort();
    RowColOrder = bb.getShort();
    Yorigin = bb.getShort();
    Year = bb.getShort();
    Month = bb.getShort();
    Day = bb.getShort();
    Hour = bb.getShort();
    Minute = bb.getShort();
    Second = bb.getShort();
    IntensityCold = bb.getShort();
    TemperatureCold = bb.getFloat();
    IntensityHot = bb.getShort();
    TemperatureHot = bb.getFloat();
    TargetGain = bb.getFloat();
    Revision = bb.getShort();
    Reserved1 = bb.getShort();
    UL_Row = bb.getShort();
    UL_Col = bb.getShort();
    LR_Row = bb.getShort();
    LR_Col = bb.getShort();
    NumFramesSummed = bb.getShort();
    FrameRate = bb.getFloat();
    IntegrationTime = bb.getFloat();
    IntegrationTime2 = bb.getFloat();
    IntegrationTime3 = bb.getFloat();
    IntegrationTime4 = bb.getFloat();
    for (int j = 0; j < 18; j++) UserField[j] = bb.getChar();
    DataMin = bb.getShort();
    DataMax = bb.getShort();
    Gain1 = bb.getShort();
    Offset1 = bb.getShort();
    Gain2 = bb.getShort();
    Offset2 = bb.getShort();
    PixelType = bb.getShort();
    CalIntensity1 = bb.getFloat();
    CalIntenisty2 = bb.getFloat();
    Photon1 = bb.getFloat();
    Photon2 = bb.getFloat();
    Power1 = bb.getFloat();
    Power2 = bb.getFloat();
    LinearFlag = bb.getShort();
    PxlSubst = bb.getShort();
    for (int j = 0; j < 100; j++) UserField2[j] = bb.getChar();
    for (int j = 0; j < 80; j++) Comment[j] = bb.get();
    Gain3 = bb.getShort();
    Offset3 = bb.getShort();
    Gain4 = bb.getShort();
    Offset4 = bb.getShort();
    FPAGain = bb.getShort();
    FileType = bb.getShort();
    NUCflag = bb.getShort();
    NoDigCor = bb.getShort();
    MinVolt = bb.getFloat();
    MaxVolt = bb.getFloat();
    NumFramesEx = bb.getInt();
    ExtendedFormat = bb.getShort();
    IntEveryNframes = bb.getShort();
    SkipEveryNframes = bb.getShort();
  }

  public String name() { return _name; }

  public void setName(String name) {
    _name = name;
  }

  public ByteOrder getByteOrder() {
    return byteOrder;
  }

  public void setByteOrder(ByteOrder byteOrder) {
    this.byteOrder = byteOrder;
  }

  public int getNFrames() {
    if (Reserved1 != 0) {
      return Reserved1;
    }
    return NumFramesEx;
  }

  public int getNpix() {
    return Xsize*Ysize;
  }

  public String getDate() {
    String retVal = ""+Year+"-";
    if (Month < 10) retVal += "0";
    retVal+=Month+"-";
    if (Day < 10) retVal+="0";
    retVal+=Day;
    return retVal;
  }

  public String getTime() {
    String retVal = "";
    if (Hour < 10) retVal+="0";
    retVal += Hour+":";
    if (Minute < 10) retVal+="0";
    retVal += Minute+":";
    if (Second < 10) retVal+="0";
    retVal += Second;
    return retVal;
  }

  public int getBitDepth() {
    switch(PixelType) {
      case 0:
	return 16;
      case 1:
	return 8;
      case 2:
	return 32;
      case 3:
	return -32;
      case 4:
	return -64;
      default:
	return 16;
    }
  }

  public int sendTo(Socket soc) {
    UFBytes header = new UFBytes("UFMMTImgHeader:"+_name, byteHeader);
    int retVal = header.sendTo(soc);
    System.out.println("UFMMTImgHeader::sendTo> sent "+header.name()+"; "+retVal+" bytes of header info.");
    return retVal;
  }

  //-----------------------------------------------------------------------------

  public String fitsKey(String key, int value) {
    return fitsKey(key, ""+value, null, false);
  }

  public String fitsKey(String key, float value) {
    return fitsKey(key, ""+value, null, false);
  }

  public String fitsKey(String key, String value) {
    return fitsKey(key, value, null, true);
  }

  public String fitsKey(String key, int value, String comment) {
    return fitsKey(key, ""+value, comment, false);
  }

  public String fitsKey(String key, float value, String comment) {
    return fitsKey(key, ""+value, comment, false);
  }

  public String fitsKey(String key, String value, boolean quote) {
    return fitsKey(key, value, null, quote);
  }

  public String fitsKey(String key, String value, String comment, boolean quote) {
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

  //-----------------------------------------------------------------------------

  public Vector<String> getPrimaryFitsHeader() {
    Vector<String> fitsHead = new Vector(20);
    fitsHead.add(fitsKey("SIMPLE", "T", "conforms to FITS standard", false));
    fitsHead.add(fitsKey("BITPIX",8,"array data type"));
    fitsHead.add(fitsKey("NAXIS", 0, "number of array dimensions"));
    fitsHead.add(fitsKey("EXTEND","T",false));
    fitsHead.add(fitsKey("DATE-OBS",getDate()));
    fitsHead.add(fitsKey("TIME-OBS",getTime()));
    fitsHead.add(fitsKey("NSUMMED",NumFramesSummed));
    fitsHead.add(fitsKey("FRM_RATE",FrameRate));
    //fitsHead.add(fitsKey("EXP_TIME",IntegrationTime));
    //fitsHead.add(fitsKey("EXPTIME2",IntegrationTime2));
    //fitsHead.add(fitsKey("EXPTIME3",IntegrationTime3));
    //fitsHead.add(fitsKey("EXPTIME4",IntegrationTime4));
    fitsHead.add(fitsKey("DATA_MIN",DataMin));
    fitsHead.add(fitsKey("DATA_MAX",DataMax));
    return fitsHead; 
  }

  public Vector<String> getExtensionHeader() {
    Vector<String> fitsHead = new Vector(8);
    fitsHead.add(fitsKey("XTENSION","IMAGE","Image extension", true));
    //fitsHead.add(fitsKey("BITPIX", getBitDepth(), "array data type"));
    fitsHead.add(fitsKey("BITPIX", 32, "array data type"));
    fitsHead.add(fitsKey("NAXIS", 2, "number of array dimensions"));
    fitsHead.add(fitsKey("NAXIS1", Xsize));
    fitsHead.add(fitsKey("NAXIS2",Ysize));
    fitsHead.add(fitsKey("PCOUNT",0,"number of parameters"));
    fitsHead.add(fitsKey("GCOUNT",1,"number of groups"));
    return fitsHead;
  }
}
