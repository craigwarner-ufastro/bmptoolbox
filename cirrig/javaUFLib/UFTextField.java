package javaUFLib;

//Title:        UFTextField extension of JTextField with history log of messages (errors, status, etc.).
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-5
//Authors:      Frank Varosi and Craig Warner
//Company:      University of Florida
//Description:  Keeps track of state with color, uses UFMessageLog class for history of messages.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.text.NumberFormat;
//===============================================================================
/**
 * Extends the JTextField class to add some simple useful options,
 * like changing color depending on previous/current state.
 */

public class UFTextField extends JFormattedTextField implements MouseListener, FocusListener, KeyListener
{
    public static final
	String rcsID = "$Name:  $ $Id: UFTextField.java,v 1.69 2011/03/25 05:42:37 varosi Exp $";

    private static boolean _beepAlarms = true;
    private static long _beepTime = 0;
    private static long _beepInterval = 4000;

    protected String _newText = "", _prevText = "";
    protected String _name, _Label="", _units="";
    protected Vector _alertItems = new Vector();

    protected Color _normalColor = UFColor._greenWhite;

    private boolean _beepOnError = true;
    private boolean _commitOnEntry = false;
    private boolean _showInToolTip = false;
    private boolean _enableMsgLog = true;

    public JPopupMenu popupMenu = null;
    protected UFMessageLog _Log = null;

//-------------------------------------------------------------------------------
    /**
     *Default Constructor
     */
    public UFTextField() {
	try {
	    _createComponent("TextField", true);
	}
	catch(Exception ex) {
	    System.out.println("Error creating a UFTextField: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     */
    public UFTextField(String description) {
	try {
	    _createComponent( description, true );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Default Constructor
     */
    public UFTextField( NumberFormat nformat ) {
	super( nformat );
	try {
	    _createComponent("TextField", true);
	}
	catch(Exception ex) {
	    System.out.println("Error creating a UFTextField: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     */
    public UFTextField( String description, NumberFormat nformat ) {
	super( nformat );
	try {
	    _createComponent( description, true );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param text        String: text to set in field
     */
    public UFTextField( String description, String text ) {
	try {
	    _newText = text;
	    _prevText = text;
	    _createComponent( description, true );
	    this.setText( " " + text );
	    if( _Log != null ) _Log.appendMessage( text );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor: special case with specified # of columns and no focuslistener.
     *@param description String: Information regarding the field
     *@param text        String: text to set in field
     *@param Ncolumns    int: # of columns to set for field
     */
    public UFTextField( String description, String text, int Ncolumns ) {
	try {
	    if( Ncolumns > 0 ) this.setColumns( Ncolumns );
	    _newText = text;
	    _prevText = text;
	    _createComponent( description, true );
	    this.setText( " " + text );
	    if( _Log != null ) _Log.appendMessage(text);
	    removeFocusListener(this);
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param editable    boolean: state of editing
     */
    public UFTextField( String description, boolean editable ) {
	try {
	    _createComponent( description, editable );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param editable    boolean: state of editing
     *@param msgLog      boolean: state of UFMessagLog _Log
     */
    public UFTextField( String description, boolean editable, boolean msgLog ) {
	try {
	    _enableMsgLog = msgLog;
	    _createComponent( description, editable );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param editable    boolean: state of editing
     *@param Ncolumns    int: # of columns to set for field
     */
    public UFTextField( String description, boolean editable, int Ncolumns ) {
	try {
	    if( Ncolumns > 0 ) this.setColumns( Ncolumns );
	    _createComponent( description, editable );
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFTextField: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization
     *@param description String: Information regarding the field
     */
    private void _createComponent( String description, boolean editable ) throws Exception
    {
	if( description.indexOf("(") > 0 ) {
	    int pos = description.indexOf("(");
	    _name = description.substring( 0, pos ).trim();
	    _units = description.substring( pos+1, description.indexOf(")") ).trim();
	}
	else if( description.indexOf(":") > 0 )
	    _name = description.substring( 0, description.indexOf(":") ).trim();
	else if( description.indexOf("=") > 0 )
	    _name = description.substring( 0, description.indexOf("=") ).trim();
	else
	    _name = description.trim();
	
	_Label = description;
	this.addMouseListener(this);
	this.setEditable( editable );

	if( editable ) {
	    this.setBorder(new BevelBorder( BevelBorder.LOWERED ));
	    this.addKeyListener(this);
	    this.addFocusListener(this);
	    _normalColor = UFColor._greenWhite;
	}
	else {
	    this.setBorder(new LineBorder( Color.gray, 1, true ));
	    _normalColor = UFColor._redWhite;
	}

	this.setBackground( _normalColor );
	this.setToolTipText();

	if( _enableMsgLog ) {
	    this._Log = new UFMessageLog( description, 1000 );
	    this.popupMenu = _Log.createPopupMenu();
	}
    }
//-------------------------------------------------------------------------------

    public void mousePressed(MouseEvent mev) {
	if(( mev.getModifiers() & InputEvent.BUTTON3_MASK) != 0 ) {
	    if( mev.isPopupTrigger() ) {
		if( popupMenu != null )
		    popupMenu.show( mev.getComponent(), mev.getX(), mev.getY() );
	    }
	}
    }

    public void mouseClicked(MouseEvent mev) {}
    public void mouseReleased(MouseEvent mev) {}
    public void mouseEntered(MouseEvent mev) {}
    public void mouseExited(MouseEvent mev) {}

//-------------------------------------------------------------------------------
//methods to affect global (static) behaviour of alarm beeping:

    public static void setBeeping( boolean beepState ) { _beepAlarms = beepState; }
    public static void setBeepInterval( int beepIntvSec ) { _beepInterval = 1000*beepIntvSec; }

//-------------------------------------------------------------------------------
    /**
     *UFTextField#setAlertValue
     *@param alertVal     String: text value for which orange background color should be indicated.
     */
    public void setAlertValue( String alertVal ) { if( alertVal != null ) _alertItems.add( alertVal ); }

    public void setNormalColor( Color normCol ) { _normalColor = normCol; }

//-------------------------------------------------------------------------------
    /**
     *UFTextField#setCommitOnEnter
     */
    public void setCommitOnEnter(boolean commit) { _commitOnEntry = commit; }
    public void setCommitOnEnter() { _commitOnEntry = true; }

//-------------------------------------------------------------------------------
    /**
     * Sets the text for the tool tip -- returns no arguments
     */
    protected void setToolTipText() {
	if( _showInToolTip ) 
	    this.setToolTipText( _name + " = " + _newText + " " + _units );
	else
	    this.setToolTipText("");
    }

    public void showInToolTip(boolean show) {
	this._showInToolTip = show;
	this.setToolTipText();
    }

    public void showInToolTip() { showInToolTip(true); }

    public void setToolTipText(String text) {
	if( _enableMsgLog )
	    super.setToolTipText( text + " -- Click right button to view Log." );
	else
	    super.setToolTipText( text );
    }
//-------------------------------------------------------------------------------

    public void setEditable( boolean editable )
    {
	super.setEditable( editable );

	if( editable )
	    _normalColor = UFColor._greenWhite;
	else
	    _normalColor = UFColor._redDark; 

	this.setBackground( _normalColor );
    }
//-------------------------------------------------------------------------------

    public void newDuplicates( boolean newDups ) {
	if( _Log != null ) _Log.newDuplicates( newDups );
    }

    public void addDuplicates( boolean addDups ) {
	if( _Log != null ) _Log.addDuplicates( addDups );
    }
//-------------------------------------------------------------------------------

    public String name() { return _name; }
    public String nameUp() { return _name.toUpperCase(); }
    public String nameLow() { return _name.toLowerCase(); }
    public String Label() { return _Label; }
    public String getNewText() { return _newText; }
    public String getPrevText() { return _prevText; }

//-------------------------------------------------------------------------------

    public void setNewText( String newText ) { setNewText( newText, true ); }

    public void setNewText( String newText, boolean doLog )
    {
	_newText = newText.trim();
	setToolTipText();
	setText( " " + _newText );
	if( doLog && _Log != null ) _Log.appendMessage( _newText );

	if( _newText.equals( _prevText ) )
	    setBackground( _normalColor );
	else if( _prevText.length() > 0 )
	    setBackground( Color.yellow );
    }
//-------------------------------------------------------------------------------

    public void setNewState() {
	setBackground( _normalColor );
	setNewState(_beepOnError);
    }

    public void setNewState( boolean beepOnError ) { setNewState( beepOnError, true ); }

    public void setNewState( boolean beepOnError, boolean doLog )
    {
	boolean alertSet = false;

	for( int i=0; i < _alertItems.size(); i++ ) {
	    if( _newText.equalsIgnoreCase( (String)_alertItems.elementAt(i) ) ) {
		alertSet = true;
		setBackground( Color.orange );
	    }
	}

	if( doLog && _Log != null ) _Log.appendMessage( _newText );
	_prevText = _newText;
	setToolTipText();

	if( _newText.indexOf("ERR") >= 0 || _newText.indexOf("WARN") >= 0 ||
	    _newText.indexOf("FAIL") >= 0 || _newText.indexOf("Fail") >= 0 ||
	    _newText.indexOf("ALARM") >= 0 || _newText.indexOf("Alarm") >= 0 )
	    {
		if( beepOnError ) {
		    setBackground( Color.orange );     //try color changing from orange to yellow
		    if( _beepAlarms ) {
			if( System.currentTimeMillis() - _beepTime > _beepInterval ) {
			    _beepTime = System.currentTimeMillis();
			    Toolkit.getDefaultToolkit().beep();
			}
		    }
		    try { Thread.sleep(100);} catch ( Exception _e) {}
		}
		if( !alertSet ) setBackground( Color.yellow );
	    }
	else if( !alertSet )
	    {
		if( _newText.indexOf("Warn") >= 0 ||
		    _newText.indexOf("fail") >= 0 ) setBackground( Color.yellow );
	    }
    }

    public void setNewState( boolean doLog, String newText, boolean beepOnError )
    {
	_newText = newText.trim();
	setBackground( _normalColor );
	setText(" ");
	setNewState( beepOnError, doLog );
	setText( " " + _newText );
    }

    public void setNewState( String newText ) { setNewState( true, newText, _beepOnError ); }
    public void setNewState( String newText, boolean beep ) { setNewState( true, newText, beep ); }
    public void setNewState( boolean doLog, String newText ) { setNewState( doLog, newText, _beepOnError ); }

    public void setNewState( int newVal ) { setNewState( Integer.toString( newVal ) ); }
    public void setNewState( float newVal ) { setNewState( UFLabel.truncFormat( newVal ) ); }
    public void setNewState( double newVal ) { setNewState( UFLabel.truncFormat( newVal ) ); }

//-------------------------------------------------------------------------------
    /**
     * More Event Handling Methods
     */
    public void focusGained(FocusEvent fev) 
    { 
	if( _prevText.trim().equals("") ) _prevText = getText();
    }

    public void focusLost(FocusEvent fev) 
    { 
	_newText = getText();

	if( _commitOnEntry ) {
	    _prevText = _newText;
	    setToolTipText();
	}

	if( _newText.equals( _prevText ) )
	    setBackground( _normalColor );
	else
	    setBackground( Color.yellow );

    }
//-------------------------------------------------------------------------------

    public void keyTyped(KeyEvent kev) { }
    public void keyReleased(KeyEvent kev) { }

    public void keyPressed(KeyEvent kev)
    {
	setBackground( Color.yellow );

	if( kev.getKeyChar() == '\n' )    // we got the enter key
	    {
		_newText = getText();
		if( _Log != null ) _Log.appendMessage( _newText );

		if( _commitOnEntry ) {
		    _prevText = _newText;
		    setToolTipText();
		}

		if( _newText.equals( _prevText ) )
		    setBackground( _normalColor );
	    }
	else if( kev.getKeyChar() == 27 )  // we got the escape key
	    {
		setText( " " + _prevText );
		setBackground( _normalColor );
	    }
    }
} //end of class UFTextField
