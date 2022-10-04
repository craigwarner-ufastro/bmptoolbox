package javaUFLib;

//Title:        UFTextPanel for Java Control Interface (JCI)
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  Creates 2 text fields with Label on left (default) or right, all in one Panel.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//===============================================================================
/**
 * Creates 2 text fields with Label on left (default) or right, all in one Panel,
 * together handling desired and active values, and changing color depending on state (diff or eq).
 * Active values are normally Logged by UFMessageLog in UFTextField (unless doMsgLog is passed = false).
 * Also has options for param name to be a JCheckBox or JButton.
 */

public class UFTextPanel extends JPanel implements KeyListener
{
    public static final
	String rcsID = "$Name:  $ $Id: UFTextPanel.java,v 1.59 2010/06/07 20:55:46 swanee Exp $";

    protected String _name, _units="";
    protected boolean nameOnRight = false;
    protected boolean nameIsButton = false;
    protected boolean _NOactiveField = false;
    protected boolean _NOdesiredField = false;
    protected double _fracField = 0;
    protected int _nsigDigits = 4;

    protected Border _defaultBorder;
    
    protected boolean _checkBox = false; //if true then Label will be a JCheckBox for enable/disable.
    protected boolean _checkBoxState = false; //initially the JCheckBox will NOT be checked.

    protected Color _normalColor = UFColor._greenWhite;

    protected JCheckBox _paramEnable = null;
    protected JButton _paramButton = null;
    protected JLabel _paramName = null;
    private JButton _applyDesired = null;

    protected String _desiredValt="", _desiredValue="";
    protected String _activeValt ="", _activeValue ="";

    protected JTextField _desiredField = new JTextField();
    protected UFTextField _activeField; //normally created with UFMessageLog enabled.

//-------------------------------------------------------------------------------
    /**
     *Default Constructor
     */
    public UFTextPanel() {
	try  {
	    _createPanel("parameter", true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     */
    public UFTextPanel(String description) {
	try  {
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description    String: Information regarding the field
     *@param editable      boolean: indicates if desired field should be editable.
     */
    public UFTextPanel( String description, boolean editable ) {
	try  {
	    _createPanel( description, editable, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param checkBoxState  boolean: if present a check box is created on left with starting state.
     *@param description    String: Information regarding the field
     */
    public UFTextPanel( boolean checkBoxState, String description ) {
	try  {
	    //JCheckBox always starts dis-abled, but initial value of _checkBoxState determines behaviour:
	    //  true means un-check to enable,
	    //  false means check to enable,
	    _checkBox = true;
	    _checkBoxState = checkBoxState;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param checkBoxState  boolean: if present a check box is created on left with starting state.
     *@param description    String: Information regarding the field
     *@param fracFields    double:   value specifies fraction of panel the desired & active fields use.
     */
    public UFTextPanel( boolean checkBoxState, String description, double fracFields ) {
	try  {
	    //JCheckBox always starts dis-abled, but initial value of _checkBoxState determines behaviour:
	    //  true means un-check to enable,
	    //  false means check to enable,
	    _checkBox = true;
	    _checkBoxState = checkBoxState;
	    _fracField = fracFields;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param checkBoxState  boolean: if present a check box is created on left with starting state.
     *@param description    String: Information regarding the field
     *@param nameRight      boolean: true causes param name to appear on right instead of left.
     */
    public UFTextPanel( boolean checkBoxState, String description, boolean nameRight ) {
	try  {
	    this.nameOnRight = nameRight;
	    //JCheckBox always starts dis-abled, but initial value of _checkBoxState determines behaviour:
	    //  true means un-check to enable,
	    //  false means check to enable,
	    _checkBox = true;
	    _checkBoxState = checkBoxState;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String: Information regarding the field
     *@param nameRight     boolean: true causes param name to appear on right instead of left.
     *@param nameIsButton  boolean: true causes param name to appear as JButton paramButton.
     */
    public UFTextPanel( String description, boolean nameRight, boolean nameIsButton ) {
	try  {
	    this.nameOnRight = nameRight;
	    this.nameIsButton = nameIsButton;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String:  Information regarding the field
     *@param fracFields    double:  value specifies fraction of panel the desired & active fields use.
     */
    public UFTextPanel( String description, double fracFields ) {
	try  {
	    this._fracField = fracFields;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String:  Information regarding the field
     *@param editable      boolean: indicates if desired field should be editable.
     *@param fracFields    double:  value specifies fraction of panel the desired & active fields use.
     */
    public UFTextPanel( String description, boolean editable, double fracFields ) {
	try  {
	    this._fracField = fracFields;
	    _createPanel( description, editable, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description  String:  Information regarding the field
     *@param editable     boolean: indicates if the desired field should be editable.
     *@param fracFields   double:  value specifies fraction of panel the desired & active fields use.
     *@param doMsgLog     boolean: passed to ctor of _activeField to enable/disable UFMessageLog.
     */
    public UFTextPanel( String description, boolean editable, double fracFields, boolean doMsgLog ) {
	try  {
	    this._fracField = fracFields;
	    _createPanel( description, editable, doMsgLog );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String:  Information regarding the field
     *@param fracFields    double:  value specifies fraction of panel the desired & active fields use.
     *@param nameIsButton  boolean: true causes param name to appear as JButton paramButton.
     */
    public UFTextPanel( String description, double fracFields, boolean nameIsButton ) {
	try  {
	    this._fracField = fracFields;
	    this.nameIsButton = nameIsButton;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String:  Information regarding the field
     *@param fracFields    double:  value specifies fraction of panel the desired & active fields use.
     *@param nameRight     boolean: true causes param name to appear on right instead of left.
     *@param nameIsButton  boolean: true causes param name to appear as JButton paramButton.
     */
    public UFTextPanel( String description, double fracFields, boolean nameRight, boolean nameIsButton ) {
	try  {
	    this._fracField = fracFields;
	    this.nameOnRight = nameRight;
	    this.nameIsButton = nameIsButton;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param checkBoxState boolean: if present a check box is created on left with starting state.
     *@param description   String:  Information regarding the field
     *@param fracFields    double:  value specifies fraction of panel the desired & active fields use.
     *@param nameIsButton  boolean: true causes param name to appear as JButton paramButton.
     */    
    public UFTextPanel( boolean checkBoxState, String description, double fracFields, boolean nameIsButton ) {
	try {
	    this._fracField = fracFields;
	    this.nameIsButton = nameIsButton;
	    _checkBox = true;
	    _checkBoxState = checkBoxState;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param numFields    integer: if < 2 then NO active field is created.
     *@param description  String:  Information regarding the field
     */
    public UFTextPanel( int numFields, String description ) {
	try  {
	    if( numFields < 2 ) this._NOactiveField = true;
	    _createPanel( description, true, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param fracField    double:  NO active field, and value specifies fraction of panel desiredField uses.
     *@param description  String: Information regarding the field
     */
    public UFTextPanel( double fracField, String description ) {
	try  {
	    this._NOactiveField = true;
	    this._fracField = fracField;
	    _createPanel( description, false, false );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param fracField    double:  NO active field, and value specifies fraction of panel desiredField uses.
     *@param description  String: Information regarding the field
     *@param editable     boolean.
     */
    public UFTextPanel( double fracField, String description, boolean editable ) {
	try  {
	    this._NOactiveField = true;
	    this._fracField = fracField;
	    _createPanel( description, editable, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param editable     boolean.
     *@param fracField    double:  NO desired field, and value specifies fraction of panel activeField uses.
     *@param description  String: Information regarding the field
     */
    public UFTextPanel( boolean editable, double fracField, String description ) {
	try  {
	    this._NOdesiredField = true;
	    this._fracField = fracField;
	    _createPanel( description, editable, true );
	}
	catch(Exception ex) {
	    System.out.println("Error in creating UFTextPanel: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization
     *@param description  String: Information regarding the field
     *@param editable     boolean: indicates if the desired field should be editable.
     *@param doMsgLog     boolean: passed to ctor of _activeField to do or not-to-do UFMessageLog.
     */
    private void _createPanel( String description, boolean editable, boolean doMsgLog ) throws Exception
    {
	if( description.indexOf("(") > 0 ) {
	    int pos = description.indexOf("(");
	    this._name = description.substring( 0, pos ).trim();
	    if( description.indexOf(")") > 0 )
		this._units = description.substring( pos+1, description.indexOf(")") ).trim();
	}
	else if( description.indexOf(":") > 0 )
	    this._name = description.substring( 0, description.indexOf(":") ).trim();
	else if( description.indexOf("=") > 0 )
	    this._name = description.substring( 0, description.indexOf("=") ).trim();
	else
	    this._name = description.trim();
	
	if( nameIsButton ) {
	    _paramButton = new JButton( _name );
	    _paramButton.setHorizontalTextPosition( SwingConstants.LEFT );
	}
	else _paramName = new JLabel( description );

	if( editable ) {
	    _desiredField.setEditable(true);
	    _desiredField.setBorder(new BevelBorder( BevelBorder.LOWERED ));
	    _desiredField.addKeyListener( this );
	    _normalColor = UFColor._greenWhite;
	}
	else {
	    _desiredField.setEditable(false);
	    _desiredField.setBorder(new LineBorder( Color.gray, 1, true ));
	    _normalColor = UFColor._redWhite;
	}

	if( _checkBox ) {
	    _paramEnable = new JCheckBox( );
	    _paramEnable.setSelected(_checkBoxState);
	    _paramEnable.setBackground( UFColor.panel_Color );
	    _defaultBorder = _desiredField.getBorder();
	    setEnabled( false );
	    //always start dis-abled, but initial value of _checkBoxState determines behaviour:

	    _paramEnable.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			if( _checkBoxState )
			    setEnabled( !_paramEnable.isSelected() );
			else
			    setEnabled( _paramEnable.isSelected() );
		    }
		});
	}
	
	_desiredField.setBackground( _normalColor );
	

	this.setBackground( UFColor.panel_Color );
	this.setLayout(new RatioLayout());
	double paramLoc, paramWidth;
	String deFieldLoc, acFieldLoc;

	if( nameOnRight )
	    {
		if( _NOactiveField ) {
		    if( _fracField > 0 && _fracField < 1 ) {
			paramLoc = _fracField+0.01;
			paramWidth = 1-_fracField-0.01;
			deFieldLoc = "0.01,0.01;" + _fracField + ",0.99";
		    }
		    else {
			paramLoc = 0.51f;
			paramWidth = 0.49f;
			deFieldLoc = "0.01,0.01;0.49,0.99";
		    }
		    this.add( deFieldLoc, _desiredField );
		}
		else {
		    if( _fracField > 0 && _fracField < 1 ) {
			paramLoc = _fracField+0.01;
			paramWidth = 1-_fracField-0.01;
			deFieldLoc = "0.01,0.01;" + _fracField/2 + ",0.99";
			acFieldLoc = (_fracField/2+0.01) + ",0.01;" + _fracField/2 + ",0.99";
		    }
		    else {
			paramLoc = 0.59f;
			paramWidth = 0.41f;
			deFieldLoc = "0.01,0.01;0.28,0.99";
			acFieldLoc = "0.30,0.01;0.28,0.99";
		    }
		    _activeField = new UFTextField( description, false, doMsgLog );
		    this.add( acFieldLoc, _activeField );
		    this.add( deFieldLoc, _desiredField );
		}
	    }
	else  //default is name (Label) on Left:
	    {
		if( _NOactiveField ) {
		    if( _fracField > 0 && _fracField < 1 ) {
			paramLoc = 0.01f;
			paramWidth = 1-_fracField-0.02;
			deFieldLoc = ( 1-_fracField-0.01 ) + ",0.01;" + _fracField + ",0.99";
		    }
		    else {
			paramLoc = 0.01f;
			paramWidth = 0.59f;
			deFieldLoc = "0.60,0.01;0.39,0.99";
		    }
		    this.add( deFieldLoc, _desiredField );
		}
		else if( _NOdesiredField ) {
		    if( _fracField > 0 && _fracField < 1 ) {
			paramLoc = 0.01f;
			paramWidth = 1-_fracField-0.02;
			acFieldLoc = ( 1-_fracField-0.01 ) + ",0.01;" + _fracField + ",0.99";
		    }
		    else {
			paramLoc = 0.01f;
			paramWidth = 0.59f;
			acFieldLoc = "0.60,0.01;0.39,0.99";
		    }
		    _activeField = new UFTextField( description, false, doMsgLog );
		    this.add( acFieldLoc, _activeField );
		}
		else {
		    if( _fracField > 0 && _fracField < 1 ) {
			paramLoc = 0.01f;
			paramWidth = 1-_fracField-0.03;
			deFieldLoc = ( 1-_fracField-0.01 ) + ",0.01;" + (_fracField-.01)/2 + ",0.99";
			acFieldLoc = ( 1-(_fracField+.01)/2 ) + ",0.01;" + (_fracField-.01)/2 + ",0.99";
		    }
		    else {
			paramLoc = 0.01f;
			paramWidth = 0.40f;
			deFieldLoc = "0.41,0.01;0.285,0.99";
			acFieldLoc = "0.705,0.01;0.285,0.99";
		    }
		    _activeField = new UFTextField( description, false, doMsgLog );
		    this.add( acFieldLoc, _activeField );
		    this.add( deFieldLoc, _desiredField );
		}
	    }

	if( _paramEnable != null ) {
	    this.add( paramLoc + ",0.01;0.09,0.99", _paramEnable );
	    paramLoc += 0.09;
	    paramWidth -= 0.09;
	}
	String paramLocStr = paramLoc + ",0.01;" + paramWidth + ",0.99";
	if( _paramButton != null ) this.add( paramLocStr, _paramButton );
	else if( _paramName != null )   this.add( paramLocStr, _paramName );
    }
//-------------------------------------------------------------------------------

    public void addButtonAction( ActionListener actionListener )
    {
	if( _paramButton != null )
	    _paramButton.addActionListener( actionListener );
    }
//-------------------------------------------------------------------------------

    public void addApplyButton( JButton applyDesired ) { _applyDesired = applyDesired; }

//-------------------------------------------------------------------------------

    public boolean isSelected()
    {
	if( _checkBox )
	    return _paramEnable.isSelected();
	else
	    return true;
    }

    public boolean isLocked()
    {
	if( _checkBox ) {
	    if( _checkBoxState )
		return( _paramEnable.isSelected() );
	    else
		return( !_paramEnable.isSelected() );
	}
	else return false;
    }

    public void setSelected( boolean check )
    {
	if( _checkBox ) _paramEnable.setSelected(check);
    }

    public void setLocked()
    {
	if( _checkBox ) {
	    if( _checkBoxState )
		_paramEnable.setSelected(true);
	    else
		_paramEnable.setSelected(false);
	}
	setEnabled(false);
    }
//-------------------------------------------------------------------------------

    public void setFont( Font newFont )
    {
	if( newFont != null ) {
	    super.setFont( newFont );
	    if( _desiredField != null ) _desiredField.setFont( newFont );
	    if( _activeField != null ) _activeField.setFont( newFont );
	}
    }
//-------------------------------------------------------------------------------

    public void newDuplicates( boolean newDups ) {
	if( _activeField != null ) _activeField.newDuplicates( newDups );
    }

    public void addDuplicates( boolean addDups ) {
	if( _activeField != null ) _activeField.addDuplicates( addDups );
    }
//-------------------------------------------------------------------------------

    public void setHorizontalAlignment( int alignment )
    {
	if( _desiredField != null ) _desiredField.setHorizontalAlignment( alignment );
	if( _activeField != null ) _activeField.setHorizontalAlignment( alignment );
    }
//-------------------------------------------------------------------------------
    public void setNumSigDigits( int ndig ) { _nsigDigits = ndig; }
//-------------------------------------------------------------------------------

    public String getActive() {	return _activeValt; }
    public String getDesired() { return _desiredValt; }

//-------------------------------------------------------------------------------

    public String getActiveField() { return _activeField.getText().trim(); }
    public String getDesiredField() { return _desiredField.getText().trim(); }

//-------------------------------------------------------------------------------

    public String setgetDesired()
    {
	setDesired( _desiredField.getText() );
	return _desiredValt;
    }
//-------------------------------------------------------------------------------

    public void setDesired( String newVal )
    {
	_desiredValue = newVal.trim();
	_desiredValt = truncate( _desiredValue );

	if( !_desiredValt.equals( _activeValt ) ) _desiredField.setBackground( Color.yellow );

	if( !_desiredField.getText().trim().equals( _desiredValt ) )
	    _desiredField.setText( " " + _desiredValt );
    }
//-------------------------------------------------------------------------------

    public void setActive( String newVal )
    {
	_activeValue = newVal.trim();
	_activeValt = truncate( _activeValue );
	_activeField.setNewState( _activeValt );

	if( _desiredValt.equals( _activeValt ) ) _desiredField.setBackground( _normalColor );
    }

    public void setActive( int newVal ) { setActive( Integer.toString(newVal) ); }
    public void setActive( float newVal ) { setActive( Float.toString(newVal) ); }
    public void setActive( double newVal ) { setActive( Double.toString(newVal) ); }

//-------------------------------------------------------------------------------

    public String truncate( String value )
    {
	if( value.indexOf("E") > 0 ) return value;

	int nc = value.length();
	int ppz = value.indexOf(".0");

	if( ppz > 0 ) {
	    //truncate the ".0" to the rounded integer:
	    if( ppz == nc-2 ) return value.substring(0,ppz);
	}

	int nsd = _nsigDigits + 1;
	if( value.indexOf("-") >= 0 ) ++nsd;

	if( _nsigDigits > 1 && nc > nsd ) {
	    int pp = value.indexOf(".");
	    if( pp > 0 ) {
		if( pp > nsd ) nsd = pp;
		return value.substring(0,nsd);
	    }
	}
	
	return value.trim();
    }
//-------------------------------------------------------------------------------

    //for cases of _NOactiveField = true, set text without checking for change.

    public void setValue( String newVal )
    {
	_desiredValue = newVal.trim();
	_desiredValt = truncate( _desiredValue );
	_desiredField.setBackground( _normalColor );
	_desiredField.setText( " " + _desiredValt );
    }

    public void setValue( int newVal ) { setValue( Integer.toString(newVal) ); }
    public void setValue( float newVal ) { setValue( Float.toString(newVal) ); }
    public void setValue( double newVal ) { setValue( Double.toString(newVal) ); }

//-------------------------------------------------------------------------------

    //for cases of activeField used just for display, set text without checking:

    public void setValue2( String newVal, String units )
    {
	_activeValue = newVal.trim();
	_activeValt = truncate( _activeValue );
	//_activeField.setBackground( _normalColor );

	if( units != null )
		_activeField.setText(" " + _activeValt + " " + units);
	else
		_activeField.setText(" " + _activeValt);
    }

    public void setValue2(int newV, String units) { setValue2( Integer.toString(newV), units); }
    public void setValue2(float newV, String units) { setValue2( Float.toString(newV), units); }
    public void setValue2(double newV, String units) { setValue2( Double.toString(newV), units); }

    public void setValue2( String newVal ) { 
	if(_units.trim().equals(""))
	    setValue2( newVal, null );
	else
	    setValue2( newVal, _units);
    }
    public void setValue2( int newVal ) { setValue2( Integer.toString(newVal) ); }
    public void setValue2( float newVal ) { setValue2( Float.toString(newVal) ); }
    public void setValue2( double newVal ) { setValue2( Double.toString(newVal) ); }

//-------------------------------------------------------------------------------

    public String getUnits() { return _units; }
    public void setUnits(String units) { _units = units; }
    public JCheckBox getLockBox() { return _paramEnable; }
    
    public String name() { return _name; }
    public String nameUp() { return _name.toUpperCase(); }
    public String nameLow() { return _name.toLowerCase(); }

//-------------------------------------------------------------------------------

    public void setDesiredBackground( Color color )
    {
	_normalColor = color;
	this._desiredField.setBackground( _normalColor );
    }
    
//-------------------------------------------------------------------------------

    public void setActiveBackground( Color color )
    {
	_normalColor = color;
	this._activeField.setBackground( color );
    }

//-------------------------------------------------------------------------------

    public void setEditable( boolean editable )
    {
	this._desiredField.setEditable( editable );

	if( editable ) {
	    this._desiredField.setBorder(new BevelBorder( BevelBorder.LOWERED ));
	    this._desiredField.addKeyListener( this );
	    _normalColor = UFColor._greenWhite;
	}
	else {
	    this._desiredField.removeKeyListener( this );
	    _normalColor = UFColor._redDark;
	}

	this._desiredField.setBackground( _normalColor );
    }
//-------------------------------------------------------------------------------

    public void setEnabled( boolean enable )
    {
	this._desiredField.setEnabled( enable );

	if( enable ) {
	    this._desiredField.setBorder(new BevelBorder( BevelBorder.LOWERED ));
	    this._desiredField.addKeyListener( this );
	    _normalColor = UFColor._greenWhite;
	}
	else {
	    this._desiredField.setBorder(_defaultBorder);
	    _normalColor = UFColor._redDark;
	}

	this._desiredField.setBackground( _normalColor );
    }
//-------------------------------------------------------------------------------
    /**
     * Key Listener Event Handling Methods
     */
    public void keyTyped(KeyEvent ke) { }
    public void keyReleased(KeyEvent ke) { }

    public void keyPressed(KeyEvent ke)
    {
	this._desiredField.setBackground( Color.yellow );

	if( ke.getKeyChar() == '\n' )    // we got the enter key
	    {
		String newText = this._desiredField.getText().trim();

		if( newText.equals( this._desiredValt ) )
		    this._desiredField.setBackground( _normalColor );

		if( _applyDesired != null ) _applyDesired.doClick(100);
	    }
	else if( ke.getKeyChar() == 27 )  // we got the escape key
	    {
		this._desiredField.setText( " " + this._desiredValt );
		this._desiredField.setBackground( _normalColor );
	    }
    }
} //end of class UFTextPanel

