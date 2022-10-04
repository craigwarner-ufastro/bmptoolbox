package javaUFLib;

//Title:        UFLabel.java
//Version:      see rcsID
//Copyright:    Copyright (c) Frank Varosi
//Author:       Frank Varosi, 2003-6
//Company:      University of Florida
//Description:  Extension of JLabel class.

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.*;

//===============================================================================
/**
 * Creates text Labels with always same prefix text and color blue.
 * Beeps and changes color to red if text "ERR" or "WARN" is displayed.
 */
public class UFLabel extends JLabel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFLabel.java,v 1.29 2013/01/10 00:18:09 varosi Exp $";

    private static boolean _beepAlarms = true;
    private static long _beepTime = 0;
    private static long _beepInterval = 4000;

    protected String _prefix = "";
    protected boolean _doBeep = true;
    protected boolean _needReset = false;
    protected int _minDecDigits = 1;
    protected int _maxDecDigits = 4;
    public static final Color _darkRed = new Color(155,0,0);
    public static final Color _darkBlue = new Color(0,0,144); //default color is dark blue (almost black).
    public static final Color _darkGreen = new Color(0,77,0);
    public static final Color _darkYellow = new Color(177,111,0);
    protected Color _color = _darkBlue;

    protected UFMessageLog _Log = null;
    public JPopupMenu popupMenu;
//-------------------------------------------------------------------------------
    /**
     * Basic Constructor
     *@param prefix String: Text to preceed record value in the label text
     */
    public UFLabel(String prefix) {
	try  {
	    super.setText( prefix );
	    super.setForeground( _color );
	    _prefix = prefix;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text.
     *@param color  Color:  Color to use for foreground.
     */
    public UFLabel( String prefix, Color color ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( color );
	    _prefix = prefix;
	    _color = color;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Basic Constructor
     *@param prefix String: Text to preceed record value in the label text
     *@param msgLog boolean: set to true to turn on saving text to UFMessageLog object.
     */
    public UFLabel(String prefix, boolean msgLog) {
	try  {
	    super.setText( prefix );
	    super.setForeground( _color );
	    _prefix = prefix;
	    if( msgLog ) setupLogging();
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text.
     *@param msgLog boolean: set to true to turn on saving text to UFMessageLog object.
     *@param color  Color:  Color to use for foreground.
     */
    public UFLabel( String prefix, boolean msgLog, Color color ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( color );
	    _prefix = prefix;
	    _color = color;
	    if( msgLog ) setupLogging();
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text
     */
    public UFLabel( String prefix, int minDecDigits ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( _color );
	    _prefix = prefix;
	    _minDecDigits = minDecDigits;
	    if( _minDecDigits < 0 ) _minDecDigits = 2;
	    if( _maxDecDigits < _minDecDigits ) _maxDecDigits = _minDecDigits;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text
     */
    public UFLabel( String prefix, int minDecDigits, Color color ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( color );
	    _prefix = prefix;
	    _color = color;
	    _minDecDigits = minDecDigits;
	    if( _minDecDigits < 0 ) _minDecDigits = 2;
	    if( _maxDecDigits < _minDecDigits ) _maxDecDigits = _minDecDigits;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text
     */
    public UFLabel( String prefix, int minDecDigits, int maxDecDigits ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( _color );
	    _prefix = prefix;
	    _minDecDigits = minDecDigits;
	    _maxDecDigits = maxDecDigits;
	    if( _minDecDigits < 0 ) _minDecDigits = 2;
	    if( _maxDecDigits < 0 ) _maxDecDigits = 3 + _minDecDigits;
	    if( _maxDecDigits < _minDecDigits ) _maxDecDigits = _minDecDigits;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param prefix String: Text to preceed record value in the label text
     */
    public UFLabel( String prefix, int minDecDigits, int maxDecDigits, Color color ) {
	try  {
	    super.setText( prefix );
	    super.setForeground( color );
	    _prefix = prefix;
	    _color = color;
	    _minDecDigits = minDecDigits;
	    _maxDecDigits = maxDecDigits;
	    if( _minDecDigits < 0 ) _minDecDigits = 2;
	    if( _maxDecDigits < 0 ) _maxDecDigits = 3 + _minDecDigits;
	    if( _maxDecDigits < _minDecDigits ) _maxDecDigits = _minDecDigits;
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFLabel with prefix "+prefix+": " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
//methods to affect global (static) behaviour of alarm beeping:

    public static void setBeeping( boolean beepState ) { _beepAlarms = beepState; }
    public static void setBeepInterval( int beepIntvSec ) { _beepInterval = 1000*beepIntvSec; }

    //control individual object beeping:

    public void setAlarmOn(boolean alarmOn) { _doBeep = alarmOn; }

//-------------------------------------------------------------------------------

    public void newPrefix( String prefix ) { _prefix = prefix; }

    public void resetColor() { _color = _darkBlue; setForeground( _color ); }

    public void newColor( Color color ) { _color = color; setForeground( _color ); }

    public void newColor( boolean error )
    {
	if( error )
	    _color = _darkRed;
	else
	    _color = _darkGreen;

	setForeground( _color );
    }
//-------------------------------------------------------------------------------
  /**
   * Override setText method to always use _prefix.
   */
    public void setText( String text ) { setText( true, text ); }

    public void setText( boolean doLog, String text )
    {
	if( text == null ) {
	    super.setText( _prefix + "(null)");
	    return;
	}

	if( text.indexOf("ERR") >= 0 || text.indexOf("WARN") >= 0 || text.indexOf("ALARM") >= 0 ||
	    text.indexOf("FAIL") >= 0 || text.indexOf("Fail") >= 0 )
	    {
		setForeground( _darkRed );
		_needReset = true;
		if( _doBeep && _beepAlarms ) {
		    if( System.currentTimeMillis() - _beepTime > _beepInterval ) {
			_beepTime = System.currentTimeMillis();
			Toolkit.getDefaultToolkit().beep();
		    }
		}
	    }
	else if( _needReset )
	    {
		setForeground( _color );
		_needReset = false;
	    }

	super.setText( _prefix + "  " + text );

	if( doLog && this._Log != null ) _Log.appendMessage( text.trim() );
    }
//-------------------------------------------------------------------------------

    public void setText( String text, boolean noBeep ) { setText( true, text, noBeep ); }

    public void setText( boolean doLog, String text, boolean noBeep )
    {
	if(_doBeep) {
	    if( noBeep ) _doBeep = false;
	    setText( doLog, text );
	    _doBeep = true;
	}
	else
	    setText( doLog, text );
    }
//-------------------------------------------------------------------------------

    public void setText( String text, Color color )
    {
	setForeground( color );
	_needReset = false;
	setText( text );
	_needReset = true;
    }
//-------------------------------------------------------------------------------

    public void setText( String text, int colorNum ) { setText( text, colorNum, false ); }

    public void setText( String text, int colorNum, boolean noBeep )
    {
	if( colorNum < 0 )
	    setForeground( _darkRed );
	else if( colorNum > 0 )
	    setForeground( _darkBlue );
	else
	    setForeground( _darkGreen );

	_needReset = false;
	setText( text, noBeep );
	_needReset = true;
    }
//-------------------------------------------------------------------------------

    public void setText( String text, int minDecDigits, int maxDecDigits )
    {
	this.setText( truncFormat( text.trim(), minDecDigits, maxDecDigits ) );
    }
//-------------------------------------------------------------------------------

    public String getSubText()
    {
	String text = super.getText();
	if( _prefix == null ) return text;
	return text.substring( _prefix.length() ).trim();
    }
//-------------------------------------------------------------------------------

    public void setText( int value ) { setText( Integer.toString( value ) ); }

    public void setText( float value ) { setText( truncFormat( value, _minDecDigits, _maxDecDigits ) ); }

    public void setText( double value ) { setText( truncFormat( value, _minDecDigits, _maxDecDigits ) ); }

//-------------------------------------------------------------------------------

    public void setupLogging() { setupLogging( _prefix ); }

    public void setupLogging(String name)
    {
	if( this._Log != null ) return;

	this._Log = new UFMessageLog( name, 1000 );
        this.popupMenu = _Log.createPopupMenu();

	this.addMouseListener(new MouseListener() {
		public void mousePressed(MouseEvent mev) {
		    if(( mev.getModifiers() & InputEvent.BUTTON3_MASK) != 0 ) {
			if( mev.isPopupTrigger() ) {
			    popupMenu.show( mev.getComponent(), mev.getX(), mev.getY() );
			}
		    }
		}
		public void mouseClicked(MouseEvent mev) {}
		public void mouseReleased(MouseEvent mev) {}
		public void mouseEntered(MouseEvent mev) {}
		public void mouseExited(MouseEvent mev) {}
	    });

	this.setToolTipText("Click right button to view Log.");
    }
//-------------------------------------------------------------------------------

    public static String truncFormat( double value ) { return truncFormat( value, 1 ); }

    public static String truncFormat( double value, int minDecDigits ) {
	return truncFormat( value, minDecDigits, 9 );
    }

    public static String truncFormat( double value, int minDecDigits, int maxDecDigits )
    {
	return truncFormat( Double.toString( value ).trim(), minDecDigits, maxDecDigits );
    }
//-------------------------------------------------------------------------------

    public static String truncFormat( String valTxt, int minDecDigits, int maxDecDigits )
    {
	int pdp = valTxt.indexOf(".");
	if( pdp < 0 ) return valTxt;

	int ndigits = pdp + minDecDigits + 1;
	int norder = pdp;
	if( valTxt.indexOf("-") == 0 ) --norder;
	int ndd = maxDecDigits - minDecDigits;
	if( norder < 3 && ndd > 0 ) ++ndigits;
	if( norder < 2 && ndd > 1 ) ++ndigits;
	if( norder < 1 && ndd > 2 ) ++ndigits;

	if( valTxt.indexOf("E") > 0 ) {
	    int epos = valTxt.indexOf("E");
	    if( ndigits < epos ) {
		String vText = valTxt.substring( 0, ndigits );
		return( vText + valTxt.substring( valTxt.indexOf("E"), valTxt.length() ) );
	    }
	    else return valTxt;
	}
	else if( ndigits < valTxt.length() ) return valTxt.substring( 0, ndigits );
	else if( ndigits > valTxt.length() ) {
	    int nzeros = ndigits - valTxt.length();
	    String zTxt = "0";
	    for( int i=1; i < nzeros; i++ ) zTxt += "0";
	    return( valTxt + zTxt );
	}
	else return valTxt;
    }
} //end of class UFLabel

