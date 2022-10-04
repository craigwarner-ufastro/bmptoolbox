package javaUFProtocol;

import java.util.*; 

public class UFObsConfig extends UFunsignShorts
{
    public static final
	String rcsID = "$Name:  $ $Id: UFObsConfig.java,v 1.21 2019/03/14 23:00:27 varosi Exp $";

    public class ObsConfig {
	int coaddsPerSave;   // # readout frames coadded to create one saved frame
	int framesPerChop;   // # readout frames sent by controller to DAS per chop beam
	int dropsPerChop;    // # readouts discarded per chop beam
	int savesPerBeam;    // # saved frames per chop beam (or # saved per cycle if not chopping)
	int ChopBeams;       // 2 = chopping
	int ChopCycles;      // # chop cycles per nod beam (used to be saveSets)
	int nHWPangles;      // # of HWP angles if in Polarimetry mode. (default = NO = 0)
	int NodBeams;        // 2 = nodding
	int NodSets;         // # of nod cycles: each cycle is either AB or BA
	int NodType;         // 0 = na, 1 = A-BB-A, 2 = A-B-A-B    (NodPattern)
	int samplesPerPixel; // 1 = S1, 2 = S1R1_CR, 4 = S1R3 (ReadoutMode)
	int extraExpTime;    // milli-seconds exposure time added to readout time = frametime
	String ReadoutMode;
	String NodPattern;
    }
    
    protected ObsConfig _obsConfig = null;

    private void _init() {
	_currentTime();
	_type = MsgTyp._ObsConfig_;
	_obsConfig = new ObsConfig();
    }

    public UFObsConfig() { 
	_init();
    }

    public UFObsConfig(int length) {
	_init();
	_length = length;
    }

    // method _mapValues is called in UFProtocal::createFrom immediately after values are recvd:

    protected void _mapValues() {
	if( _elem > 0 ) _obsConfig.coaddsPerSave = _values[0];
	if( _elem > 1 ) _obsConfig.framesPerChop = _values[1];
	if( _elem > 2 ) _obsConfig.dropsPerChop = _values[2];
	if( _elem > 3 ) _obsConfig.savesPerBeam = _values[3];
	if( _elem > 4 ) _obsConfig.ChopBeams = _values[4];
	if( _elem > 5 ) _obsConfig.ChopCycles = _values[5];
	if( _elem > 6 ) _obsConfig.nHWPangles = _values[6];
	if( _elem > 7 ) _obsConfig.NodBeams = _values[7];
	if( _elem > 8 ) _obsConfig.NodSets = _values[8];
	if( _elem > 9 ) _obsConfig.NodType = _values[9];
	if( _elem > 10 ) _obsConfig.samplesPerPixel = _values[10];
	if( _elem > 11 ) _obsConfig.extraExpTime = _values[11];
	_setNameModes( _obsConfig.samplesPerPixel, _obsConfig.NodType );
    }

    // protected helper function to set values in name() field:

    void _setNameModes( int spp, int ntyp )
    {
	_obsConfig.ReadoutMode = "S1";
	if( spp == 2 ) _obsConfig.ReadoutMode = "S1R1_CR";
	if( spp == 4 ) _obsConfig.ReadoutMode = "S1R3";
	_obsConfig.NodPattern = "na";
	if( ntyp == 1 ) _obsConfig.NodPattern = "A-BB-A";
	else if( ntyp == 2 ) _obsConfig.NodPattern = "A-B-A-B";
	String dLab = dataLabel();
	rename("||" + _obsConfig.ReadoutMode +"|"+ _obsConfig.NodPattern +"||"+ dLab);
    }

    public String dataLabel() {
	String nm = name();
	int posR = nm.lastIndexOf("||");
	if( posR != -1 && posR+2 < nm.length() )
	    return nm.substring(posR+2);
	else
	    return ("?");
    }

    public int reLabel( String newLabel ){
	rename("||" + _obsConfig.ReadoutMode +"|"+ _obsConfig.NodPattern +"||"+ newLabel );
	return name().length();
    }

    // override the inherited UFShorts description:
    public String description() { return new String("UFObsConfig"); }

    // convenience functions:

    public int totalSavedFrames()
    {
	return _obsConfig.savesPerBeam * _obsConfig.ChopBeams
	    * _obsConfig.ChopCycles * _obsConfig.NodBeams * _obsConfig.NodSets;
    }

    public int totFrameCnt() { return totalSavedFrames(); }

    public String obsMode() {
	if( _obsConfig.ChopBeams > 1 ) {
	    if( _obsConfig.NodBeams > 1 )
		return("chop-nod");
	    else
		return("chop");
	}
	else if( _obsConfig.NodBeams > 1 ) return("nod");
	else return("stare");
    }

    public int coaddsPerSave() { return _obsConfig.coaddsPerSave;    }
    public int coaddsPerFrm() {	 return _obsConfig.coaddsPerSave;    }

    public int framesPerChop() { return _obsConfig.framesPerChop;    }
    public int dropsPerChop() {  return _obsConfig.dropsPerChop;    }

    public int savesPerBeam() {  return _obsConfig.savesPerBeam;    }

    public int chopBeams() {	return _obsConfig.ChopBeams;    }

    public int chopCycles() {	return _obsConfig.ChopCycles;    }
    public int saveSets() {	return _obsConfig.ChopCycles;    }

    public int nHWPangles()   { return _obsConfig.nHWPangles;  }
    public int extraExpTime() { return _obsConfig.extraExpTime; }

    public int nodBeams() {	return _obsConfig.NodBeams;    }
    public int nodSets() {	return _obsConfig.NodSets;    }

    public int nodType()       { return _obsConfig.NodType;    }
    public String nodPattern() { return _obsConfig.NodPattern;    }

    public int samplesPerPixel() { return _obsConfig.samplesPerPixel;    }
    public String readoutMode()  { return _obsConfig.ReadoutMode;    }

    public void setChopBeams(int cb)  { _obsConfig.ChopBeams = cb;    }
    public void setChopCycles(int cc) { _obsConfig.ChopCycles = cc;    }
    public void setSaveSets(int ss)   { _obsConfig.ChopCycles = ss;    }
    public void setNodBeams(int nb)   { _obsConfig.NodBeams = nb;    }
    public void setNodSets(int ns)    { _obsConfig.NodSets = ns;    }

    public void setExtraExpTime( int mseet )  { _obsConfig.extraExpTime = mseet; }

    //define alternate accessors for NIR exposure readouts :

    public int groups() { return _obsConfig.ChopBeams;    }
    public int reads()  {  return _obsConfig.ChopCycles;    }
    public int ramps()  {  return _obsConfig.NodSets;    }
}
    
    
