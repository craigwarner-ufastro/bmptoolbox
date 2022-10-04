package javaUFLib;

//Title:        UFLabelsPanel.java
//Version:      see rcsID
//Copyright:    Copyright (c) Frank Varosi
//Author:       Frank Varosi, 2003-6
//Company:      University of Florida
//Description:  Extension of JPanel class using UFLabel.

import java.awt.*;
import javax.swing.*;

//===============================================================================
/**
 * Creates JPanel with parameter Name (JLabel) = Value (UFLabel)
 * Value text always prefixed with "=" and uses color blue.
 * Beeps and changes color to red if text "ERR" or "WARN" is displayed.
 */
public class UFLabelsPanel extends JPanel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFLabelsPanel.java,v 1.6 2008/03/26 01:04:51 varosi Exp $";

    protected String _name;
    protected Color _color;
    protected JLabel paramName;
    protected UFLabel paramValue = new UFLabel(" =");

//-------------------------------------------------------------------------------
    /**
     * Basic Constructor
     *@param name String: Text to preceed record value in the label text
     */
    public UFLabelsPanel(String name) { this( name, JLabel.LEFT, false, Color.black, 0.55 ); }

    public UFLabelsPanel(String name, Color color) { this( name, JLabel.LEFT, false, color, 0.55 ); }

    public UFLabelsPanel(String name, boolean doLog) { this( name, JLabel.LEFT, doLog, Color.black, 0.55 ); }

    public UFLabelsPanel(String name, int place, Color color) { this( name, place, false, color, 0.55 ); }

    public UFLabelsPanel(String name, int place) { this( name, place, false, Color.black, 0.55 ); }

    public UFLabelsPanel(String name, int place, boolean doLog) {
	this( name, place, doLog, Color.black, 0.55 );
    }
    public UFLabelsPanel(String name, int place, boolean doLog, Color color) {
	this( name, place, doLog, color, 0.55 );
    }
    public UFLabelsPanel(String name, double fracName) {
	this( name, JLabel.LEFT, false, Color.black, fracName );
    }
    public UFLabelsPanel(String name, boolean doLog, double fracName) {
	this( name, JLabel.LEFT, doLog, Color.black, fracName );
    }
    public UFLabelsPanel(String name, int place, double fracName) {
	this( name, place, false, Color.black, fracName );
    }
    public UFLabelsPanel(String name, int place, boolean doLog, double fracName) {
	this( name, place, doLog, Color.black, fracName );
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param name      String:  Text to preceed record value in the label text.
     *@param placement int:     JLabel flag for placement of name in JLabel.
     *@param doLog     boolean: indicates if values should be logged via UFMessageLog.
     *@param color     Color:   color of name JLabel.
     *@param fracName  double:  the fraction of panel occupied by the param name.
     */
    public UFLabelsPanel( String name, int placement, boolean doLog, Color color, double fracName ) {
	try  {
	    paramName = new JLabel( name, placement );
	    paramName.setForeground( color );
	    _name = name;
	    _color = color;
	    String nameLoc = "0.01,0.0;"+(fracName-0.01)+",1.0";
	    String valuLoc = (fracName)+",0.0;"+(1-fracName)+",1.0";
	    this.setLayout(new RatioLayout());
	    this.add( nameLoc, paramName );
	    this.add( valuLoc, paramValue );
	    if( doLog ) paramValue.setupLogging( _name );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabelsPanel with name: "+name+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------

    public void setupLogging() { paramValue.setupLogging( _name ); }

//-------------------------------------------------------------------------------

    public void setText( String text )
    {
	paramValue.setText( text );
    }
//-------------------------------------------------------------------------------

    public void setText( String text, boolean noBeep )
    {
	paramValue.setText( text, noBeep );
    }
//-------------------------------------------------------------------------------

    public void setText( String text, Color color )
    {
	paramValue.setText( text, color );
    }
//-------------------------------------------------------------------------------

    public void setText( String text, int minDecDigits, int maxDecDigits )
    {
	paramValue.setText( text, minDecDigits, maxDecDigits );
    }
//-------------------------------------------------------------------------------

    public void setText( int value ) { paramValue.setText( value ); }

    public void setText( float value ) { paramValue.setText( value ); }

    public void setText( double value ) { paramValue.setText( value ); }

} //end of class UFLabelsPanel

