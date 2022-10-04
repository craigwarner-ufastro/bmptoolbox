package javaUFLib;

//Title:        UFTextMinMax for Java Control Interface (JCI)
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-7
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  for monitoring statistics of CanariCam infrared camera system.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//===============================================================================
/**
 * Creates 3 text fields with Label all in one Panel: text fields for average, min, and max values.
 * With methods for set/getting values for convenience, and yellow color indicates average hit min/max.
 */

public class UFTextMinMax extends JPanel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFTextMinMax.java,v 1.13 2007/06/22 20:59:36 varosi Exp $";

    protected double avgVal = 0;
    protected double avgTotal = 0;
    protected int Nvals = 0;
    protected double minVal = 0;
    protected double maxVal = 0;
    protected double minMinVal = Double.MAX_VALUE;
    protected double maxMaxVal = Double.MIN_VALUE;

    private int _naDecDigits = 1;

    protected int minInt = 0;
    protected int maxInt = 0;
    protected int minMinInt = Integer.MAX_VALUE;
    protected int maxMaxInt = Integer.MIN_VALUE;

    private int _alarmMaxInt = Integer.MAX_VALUE;
    private double _alarmMaxVal = Double.MAX_VALUE;

    private boolean _avgBkgNeedsReset = false;
    private boolean _minBkgNeedsReset = false;
    private boolean _maxBkgNeedsReset = false;

    protected String avgText = "";
    protected String minText = "";
    protected String maxText = "";

    private String _name, _units;
    protected JLabel paramName;
    protected boolean userEvent = false;
    protected Color _normalColor = UFColor._redWhite;

    public JTextField avgField = new JTextField();
    public JTextField minField = new JTextField();
    public JTextField maxField = new JTextField();

//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     */
    public UFTextMinMax(String description) {
	try  {
	    _compInit( description );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextMinMax: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization
     *@param description String: Information regarding the field
     */
    private void _compInit( String description ) throws Exception
    {
	if( description.indexOf("(") > 0 ) {
	    int pos = description.indexOf("(");
	    this._name = description.substring( 0, pos ).trim();
	    this._units = description.substring( pos+1, description.indexOf(")") ).trim();
	}
	else if( description.indexOf(":") > 0 )
	    this._name = description.substring( 0, description.indexOf(":") ).trim();
	else
	    this._name = description.trim();
	
	paramName = new JLabel( description );

	avgField.setBackground(_normalColor);
	avgField.setEditable(false);
	avgField.setBorder(new LineBorder( Color.gray, 1, true ));
	avgField.setHorizontalAlignment( JTextField.RIGHT );

	minField.setBackground(_normalColor);
	minField.setEditable(false);
	minField.setBorder(new LineBorder( Color.gray, 1, true ));
	minField.setHorizontalAlignment( JTextField.RIGHT );

	maxField.setBackground(_normalColor);
	maxField.setEditable(false);
	maxField.setBorder(new LineBorder( Color.gray, 1, true ));
	maxField.setHorizontalAlignment( JTextField.RIGHT );

	this.setLayout(new RatioLayout());
	this.add("0.01,0.01;0.30,0.98", paramName);
	this.add("0.31,0.01;0.22,0.98", avgField );
	this.add("0.54,0.01;0.22,0.98", minField );
	this.add("0.77,0.01;0.22,0.98", maxField );
    }
//-------------------------------------------------------------------------------
    /**
     * Sets the alarm value that if exceeded will cause fields to turn yellow.
     */
    void setAlarmMax(int alarmMax) { _alarmMaxInt = alarmMax; }

    void setAlarmMax(double alarmMax) { _alarmMaxVal = alarmMax; }

//-------------------------------------------------------------------------------
    /**
     * Sets the text for the tool tip -- returns no arguments
     */
    void setToolTipText() {
	String avgAvgText = UFLabel.truncFormat( avgTotal/Nvals, _naDecDigits, _naDecDigits );
	String minMinText, maxMaxText;

	if( minMinInt < Integer.MAX_VALUE ) {
	    minMinText = Integer.toString( minMinInt );
	    maxMaxText = Integer.toString( maxMaxInt );
	    avgAvgText = Integer.toString( (int)Math.round(avgTotal/Nvals) );
	}
	else {
	    minMinText = UFLabel.truncFormat( minMinVal, _naDecDigits, _naDecDigits );
	    maxMaxText = UFLabel.truncFormat( maxMaxVal, _naDecDigits, _naDecDigits );
	}

	this.setToolTipText( _name + ":  avgAvg = " + avgAvgText
			     + ",  minMin = " + minMinText + ",  maxMax = " + maxMaxText );
    }
//-------------------------------------------------------------------------------

    public void reset()
    {
	minMinVal = Double.MAX_VALUE;
	maxMaxVal = Double.MIN_VALUE;
	minMinInt = Integer.MAX_VALUE;
	maxMaxInt = Integer.MIN_VALUE;
	avgTotal = 0;
	Nvals = 0;
	clear();
    }

    public void clear()
    {
	avgField.setBackground(_normalColor);
	avgField.setText(null);
	minField.setBackground(_normalColor);
	minField.setText(null);
	maxField.setBackground(_normalColor);
	maxField.setText(null);
    }
//-------------------------------------------------------------------------------

    public void setMin( String newVal )
    {
	minText = newVal;
	minField.setText( minText + " ");
    }

    public void setMin( int newVal )
    {
	if( newVal > _alarmMaxInt ) {
	    minField.setBackground( Color.yellow );
	    _minBkgNeedsReset = true;
	}
	else if( _minBkgNeedsReset ) {
	    minField.setBackground( _normalColor );
	    _minBkgNeedsReset = false;
	}

	minInt = newVal;
	minVal = newVal;

	if( minInt < minMinInt ) {
	    minMinInt = minInt;
	    minMinVal = minVal;
	}
	setMin( Integer.toString(newVal) );
    }

    public void setMin( float newVal ) { setMin( (double)newVal ); }

    public void setMin( double newVal )
    {
	if( newVal > _alarmMaxVal ) {
	    minField.setBackground( Color.yellow );
	    _minBkgNeedsReset = true;
	}
	else if( _minBkgNeedsReset ) {
	    minField.setBackground( _normalColor );
	    _minBkgNeedsReset = false;
	}
	minVal = newVal;
	if( minVal < minMinVal ) minMinVal = minVal;
	setMin( UFLabel.truncFormat( newVal, _naDecDigits, _naDecDigits ) );
    }
//-------------------------------------------------------------------------------

    public void setMax( String newVal )
    {
	maxText = newVal;
	maxField.setText( maxText + " ");
    }

    public void setMax( int newVal )
    {
	if( newVal > _alarmMaxInt ) {
	    maxField.setBackground( Color.yellow );
	    _maxBkgNeedsReset = true;
	}
	else if( _maxBkgNeedsReset ) {
	    maxField.setBackground( _normalColor );
	    _maxBkgNeedsReset = false;
	}

	maxInt = newVal;
	maxVal = newVal;

	if( maxInt > maxMaxInt ) {
	    maxMaxInt = maxInt;
	    maxMaxVal = maxVal;
	}
	setMax( Integer.toString(newVal) );
    }

    public void setMax( float newVal ) { setMax( (double)newVal ); }

    public void setMax( double newVal )
    {
	if( newVal > _alarmMaxVal ) {
	    maxField.setBackground( Color.yellow );
	    _maxBkgNeedsReset = true;
	}
	else if( _maxBkgNeedsReset ) {
	    maxField.setBackground( _normalColor );
	    _maxBkgNeedsReset = false;
	}
	maxVal = newVal;
	if( maxVal > maxMaxVal ) maxMaxVal = maxVal;
	setMax( UFLabel.truncFormat( newVal, _naDecDigits, _naDecDigits ) );
    }
//-------------------------------------------------------------------------------

    public void setAvg( String newVal )
    {
	avgText = newVal;
	avgField.setText( avgText + " ");
    }

    public void setAvg( int newVal )
    {
	if( newVal > _alarmMaxInt ) {
	    avgField.setBackground( Color.yellow );
	    _avgBkgNeedsReset = true;
	}
	else if( _avgBkgNeedsReset ) {
	    avgField.setBackground( _normalColor );
	    _avgBkgNeedsReset = false;
	}

	avgVal = newVal;
	avgTotal += avgVal;
	++Nvals;
	setAvg( Integer.toString(newVal) );
    }

    public void setAvg( float newVal ) { setAvg( (double)newVal ); }

    public void setAvg( double newVal )
    {
	if( newVal > _alarmMaxVal ) {
	    avgField.setBackground( Color.yellow );
	    _avgBkgNeedsReset = true;
	}
	else if( _avgBkgNeedsReset ) {
	    avgField.setBackground( _normalColor );
	    _avgBkgNeedsReset = false;
	}

	avgVal = newVal;
	avgTotal += avgVal;
	++Nvals;
	setAvg( UFLabel.truncFormat( newVal, _naDecDigits, _naDecDigits ) );
    }
//-------------------------------------------------------------------------------

    public void setAvgMinMax( int avgval, int minval, int maxval )
    {
	setMin( minval );
	setMax( maxval );
	setAvg( avgval );
	setToolTipText();
    }

    public void setAvgMinMax( float avgval, float minval, float maxval )
    {
	setMin( minval );
	setMax( maxval );
	setAvg( avgval );
	setToolTipText();
    }

    public void setAvgMinMax( double avgval, double minval, double maxval )
    {
	setMin( minval );
	setMax( maxval );
	setAvg( avgval );
	setToolTipText();
    }
//-------------------------------------------------------------------------------

    public String name() { return _name; }
    public String nameUp() { return _name.toUpperCase(); }
    public String nameLow() { return _name.toLowerCase(); }

//-------------------------------------------------------------------------------

    public String getAvg() { return avgText.trim(); }
    public String getMax() { return maxText.trim(); }
    public String getMin() { return minText.trim(); }

} //end of class UFTextMinMax

