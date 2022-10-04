package javaUFLib;

//Title:        UFPidLoop class for agents to override
//Version:      (see _rcsid)
//Copyright:    Copyright (c) 2008-2010
//Author:       Shaun McDowell
//Company:      University of Florida
//Description:  for PID loop parameters

import java.util.*;

public class UFPidLoop {

    static final String _rcsid = "$Name:  $ $Id: UFPidLoop.java,v 1.6 2017/06/26 06:29:16 varosi Exp $";

    protected String _name = "loop";
    protected double _setPoint;
    protected double _outputSetPoint;
    protected double _manualSetPoint;
    protected double _outputBias;
    protected double _pGain = 1.0;
    protected double _iGain = 0.01;
    protected double _dGain = 0.0;
    protected double _filterWeight = 0.1;
    protected double _err = 0.0;
    protected double _lastErr = 0.0;
    protected double _integral = 0.0;
    protected boolean _isVerbose = false;
    protected boolean _isManual = false;
    protected boolean _isOn = true;
    protected boolean _isFrozen = false;
    protected int[] _TprobeIndices;  //Identifier(s) for associated thermal probe.
    protected double _maxPower = 49.0; // Watts, obtained from Heater object or set otherwise.
//=============================================================================================================

    public UFPidLoop() {}

    public UFPidLoop( String name ) { _name = name; }

    public synchronized void reset() { _integral = 0.0; }
    
    // Accessor and Mutator methods
    
    public String getName() {	return _name;    }
    
    public void setName(String name) {	_name = name;    }
    
    public synchronized float getSetPoint() {	return (float)_setPoint;    }

    public synchronized float getOutputSetPoint() {	return (float)_outputSetPoint;    }

    public synchronized float getManualSetPoint() {	return (float)_manualSetPoint;    }

    public synchronized float getOutputBias() {	return (float)_outputBias; }

    public synchronized float getMaxPower() {	return (float)_maxPower; }

    public synchronized float getPGain() {	return (float)_pGain;    }

    public synchronized float getIGain() {	return (float)_iGain;    }

    public synchronized float getDGain() {	return (float)_dGain;    }

    public synchronized float getFilterWeight() {	return (float)_filterWeight;    }

    public synchronized float getErr() {	return (float)_err;    }

    public synchronized float getIntegral() {	return (float)_integral;    }

    public synchronized boolean isManual() {	return _isManual;    }

    public synchronized boolean isVerbose() {	return _isVerbose;    }

    public synchronized boolean isOn() {	return _isOn;    }

    public synchronized void setSetPoint( float setP ) {	_setPoint = setP;    }

    public synchronized void setManualSetPoint( float manSetP ) {	_manualSetPoint = manSetP;    }

    public synchronized void setOutputBias( float bias ) {	_outputBias = bias;    }

    public synchronized void setMaxPower( float maxp ) {	_maxPower = maxp;    }

    public synchronized void setPGain( float p ) {	_pGain = p;    }

    public synchronized void setIGain( float i ) {	_iGain = i;    }

    public synchronized void setDGain( float d ) {	_dGain = d;    }

    public synchronized void setFilterWeight( float filtWeight ) {	_filterWeight = filtWeight;    }

    public synchronized void setManual( boolean mode ) {	_isManual = mode;    }

    public synchronized void setOn( boolean on ) {	_isOn = on;    }

    public synchronized void setVerbose( boolean verbose ) {	_isVerbose = verbose;    }
    
    public void setTProbeIndices(int[] probeIndices) {
	if(probeIndices.length > 0) {
	    _TprobeIndices = new int[probeIndices.length];
	    // shift by 1 because temp channels start at 1 and arrays holding them start at index 0
	    for(int i = 0; i < probeIndices.length; i++) _TprobeIndices[i] = probeIndices[i] - 1;
	}
    }

    public String getTProbeIndices() {
	if( _TprobeIndices == null ) return("none");
	int ntpi = _TprobeIndices.length;
	if( ntpi < 1 ) return("none");
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < ntpi-1; i++) {
	    sb.append(_TprobeIndices[i]+1);
	    sb.append(',');	    
	}
	sb.append(_TprobeIndices[ntpi-1]+1); // increase by 1 for change from array index to channel num
	return sb.toString();
    }
}
