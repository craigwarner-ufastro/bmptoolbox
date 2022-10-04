package javaUFLib;

//Title:        UFComboPanel
//Version:      2.0
//Copyright:    Copyright (c) 2003
//Author:       Frank Varosi, 2003
//Company:      University of Florida
//Description:  Combine UFComboBox and UFTextField in one panel (with labels)

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

//===============================================================================
/**
 * Combine UFComboBox and UFTextField in one panel (with labels),
 * with vertical (default) or horizontal arrangement,
 * together dealing with desired and active selections, and changes color depending state.
 */

public class UFComboPanel extends JPanel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFComboPanel.java,v 1.45 2010/01/08 20:44:34 swanee Exp $";

    public UFComboBox desiredSelect;
    public UFTextField activeField;

    private String _name, _units;
    private String _device;
    private double _fracFields = 0;

    protected String desiredItem = "";
    protected String activeItem = "";

    protected JLabel paramName;
    protected JCheckBox paramEnable;

    private boolean _checkBoxUsed = false;  //if true then use paramEnable for enabling/disabling selection.
    private boolean _checkBoxState = false; //initially the JCheckBox will NOT be checked (selected).

    private boolean _Vertical = false;
    private boolean _useIndex = false;
    private boolean _compareWithSelection = true; //default is to compare activeItem with desiredSelect
                                                  // but if false, then compare with desiredItem directly.
    protected Vector _alertItems = new Vector();
    protected String[] _items;
//-------------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public UFComboPanel()
    {
	try { createPanel( "", null ); }
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param items       Array of Strings: list of values to be passed to the record field
     */
    public UFComboPanel( String description, String[] items )
    {
	try {
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param items       Array of Strings: list of values to be passed to the record field
     *@param fracFields    float:   value specifies fraction of panel the desired & active fields use.
     */
    public UFComboPanel( String description, String[] items, double fracFields )
    {
	try {
	    this._fracFields = fracFields;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param items       Array of Strings: list of values to be passed to the record field
     */
    public UFComboPanel( String description, String[] items1, String[] items2 )
    {
	try {
	    String[] items = new String[ items1.length + items2.length ];
	    for( int i=0; i < items1.length; i++ ) items[i] = items1[i];
	    for( int i=0; i < items2.length; i++ ) items[i+items1.length] = items2[i];
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String: Information regarding the field
     *@param items         Array of Strings: list of values to be passed to the record field
     *@param Vertical      boolean: set true for Vertical orientation (default is Horizontal).
     */
    public UFComboPanel( String description, String[] items, boolean Vertical )
    {
	try {
	    _Vertical = Vertical;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String: Information regarding the field
     *@param items         Array of Strings: list of values to be passed to the record field
     *@param Vertical      boolean: set true for Vertical orientation (default is Horizontal).
     *@param useIndex      boolean: set true to put UFComboBox into INDEX state (instead of ITEM).
     */
    public UFComboPanel( String description, String[] items, boolean Vertical, boolean useIndex )
    {
	try {
	    _useIndex = useIndex;
	    _Vertical = Vertical;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String: Information regarding the field
     *@param checkBoxState boolean: if present Label is JCheckBox with init check state specified.
     *@param items         Array of Strings: list of values to be passed to the record field
     */
    public UFComboPanel( String description, boolean checkBoxState, String[] items )
    {
	try {
	    _checkBoxUsed = true;
	    _checkBoxState = checkBoxState;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param checkBoxState boolean: if present Label is JCheckBox with init check state specified.
     *@param items       Array of Strings: list of values to be passed to the record field
     *@param fracFields    float:   value specifies fraction of panel the desired & active fields use.
     */
    public UFComboPanel( String description, boolean checkBoxState, String[] items, double fracFields )
    {
	try {
	    _checkBoxUsed = true;
	    _checkBoxState = checkBoxState;
	    _fracFields = fracFields;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     *@param checkBoxState boolean: if present Label is JCheckBox with init check state specified.
     *@param items	   Array of Strings: list of values to be passed to the record field
     *@param fracFields    float:   value specifies fraction of panel the desired & active fields use.
     *@param vertical	   boolean: set true for Vertical orientation (default is Horizontal).
     *@param index         boolean: set true to put UFComboBox into INDEX state (instead of ITEM).
     */
    public UFComboPanel( String description, boolean checkBoxState, String[] items, double fracFields, boolean vertical, boolean index )
    {
	try {
	    _checkBoxUsed = true;
	    _checkBoxState = checkBoxState;
	    _fracFields = fracFields;
	    _Vertical = vertical;
	    _useIndex = index;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description   String: Information regarding the field
     *@param checkBoxState boolean: if present Label is JCheckBox with init check state specified.
     *@param items         Array of Strings: list of values to be passed to the record field
     *@param Vertical      boolean: set true for Vertical orientation (default is Horizontal).
     */
    public UFComboPanel( String description, boolean checkBoxState, String[] items, boolean Vertical )
    {
	try {
	    _checkBoxUsed = true;
	    _checkBoxState = checkBoxState;
	    _Vertical = Vertical;
	    createPanel( description, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param desc          String: Information regarding the field
     *@param checkBoxState boolean: if present Label is JCheckBox with init check state specified.
     *@param items         Array of Strings: list of values to be passed to the record field
     *@param Vert          boolean: set true for Vertical orientation (default is Horizontal).
     *@param index         boolean: set true to put UFComboBox into INDEX state (instead of ITEM).
     */
    public UFComboPanel( String desc, boolean checkBoxState, String[] items, boolean Vert, boolean index )
    {
	try {
	    _useIndex = index;
	    _checkBoxUsed = true;
	    _checkBoxState = checkBoxState;
	    _Vertical = Vert;
	    createPanel( desc, items );
	}
	catch(Exception x) { System.out.println("Error creating UFComboPanel: " + x.toString()); }
    }
//-------------------------------------------------------------------------------
    /**
     *Component creation -- returns no arguments
     *@param description   String: Information regarding the field, used for Label and Name.
     *@param items         Array of Strings: list of items in the combo box
     */
    protected void createPanel( String descrip, String[] items ) throws Exception
    {
	if( descrip.indexOf("(") > 0 ) {
	    int pos = descrip.indexOf("(");
	    this._name = descrip.substring( 0, pos ).trim();
	    this._units = descrip.substring( pos+1, descrip.indexOf(")") ).trim();
	    this._device = _units;
	}
	else if( descrip.indexOf(":") > 0 )
	    this._name = descrip.substring( 0, descrip.indexOf(":") ).trim();
	else
	    this._name = descrip.trim();

	if( _useIndex ) {
	    _items = items;
	    desiredSelect = new UFComboBox( items );
	    desiredSelect.setIndexMethod();
	}
	else {//make copy of items and set first item to be always a blank:	
	    _items = new String[items.length+1];
	    _items[0] = "";
	    for( int i=0; i < items.length; i++ ) _items[i+1] = items[i];
	    desiredSelect = new UFComboBox( _items );
	}

	this.activeField = new UFTextField( descrip, false );
	this.activeField.setFont(new Font("Bold12", Font.BOLD, 12));
	this.setLayout(new RatioLayout());
	this.setBackground( UFColor.panel_Color );
	JPanel paramPanel = new JPanel(new BorderLayout());
	paramPanel.setBackground( UFColor.panel_Color );

	if( _checkBoxUsed )
	    {
		paramEnable = new JCheckBox(" " + _name, _checkBoxState );
		paramEnable.setBackground( UFColor.panel_Color );
		desiredSelect.setEnabled( false );
		paramPanel.add( paramEnable, BorderLayout.WEST );
		if( _units == null ) _units = ": ";
		paramPanel.add( new JLabel(_units+" "), BorderLayout.EAST );

		paramEnable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    if( _checkBoxState )
				desiredSelect.setEnabled( !paramEnable.isSelected() );
			    else
				desiredSelect.setEnabled( paramEnable.isSelected() );
			}
		    });
	    }
	else {
	    paramName = new JLabel( descrip + " ", JLabel.RIGHT );
	    paramPanel.add( paramName, BorderLayout.EAST );
	}

	if( _Vertical )
	    {
		this.add("0.0,0.03;0.5,0.46", paramPanel );
		this.add("0.5,0.03;0.5,0.46", desiredSelect );
		this.add("0.0,0.52;0.5,0.40", new JLabel("Active Value= ",JLabel.RIGHT) );
		this.add("0.5,0.52;0.5,0.40", activeField );
	    }
	else {	
	    String paramLoc = "0.00,0.02;0.32,0.96";
	    String deFieldLoc = "0.32,0.02;0.36,0.96";
	    String acFieldLoc = "0.69,0.02;0.30,0.96";

	    if( _fracFields > 0 && _fracFields < 1 ) {
		paramLoc = "0.01,0.02;" + (1-_fracFields-0.03) +",0.96";
		deFieldLoc = (1-_fracFields-0.01) + ",0.02;" + (_fracFields/2) + ",0.96";
		acFieldLoc = (1-_fracFields/2) + ",0.02;" + (_fracFields/2-0.01) + ",0.96";
	    }

	    this.add( paramLoc, paramPanel );
	    this.add( deFieldLoc, desiredSelect );
	    this.add( acFieldLoc, activeField );
	}
    }
//-------------------------------------------------------------------------------
    /**
     *UFComboPanel#setAlertValue
     *@param alertValue       String: item value for which red color should be indicated if selected.
     */
    public void replaceItems( String[] items )
    {
	desiredSelect.removeAllItems();

	if( _useIndex ) {
	    _items = items;
	    for( int i=0; i < items.length; i++ ) desiredSelect.addItem( items[i] );
	}
	else {//make copy of items and set first item to be always a blank:	
	    _items = new String[items.length+1];
	    _items[0] = "";
	    desiredSelect.addItem( _items[0] );
	    for( int i=0; i < items.length; i++ ) {
		_items[i+1] = items[i];
		desiredSelect.addItem( _items[i+1] );
	    }
	}

	desiredSelect.registerItems();
    }

    public void replaceItems( String[] items1, String[] items2 )
    {
	String[] items = new String[ items1.length + items2.length ];
	for( int i=0; i < items1.length; i++ ) items[i] = items1[i];
	for( int i=0; i < items2.length; i++ ) items[i+items1.length] = items2[i];
	replaceItems( items );
    }
//-------------------------------------------------------------------------------
    /**
     *UFComboPanel#setAlertValue
     *@param alertValue       String: item value for which red color should be indicated if selected.
     */
    public void setAlertValue( String alertValue )
    {
	if( alertValue != null ) {
	    _alertItems.add( alertValue );
	    desiredSelect.setAlertValue( alertValue );
	    activeField.setAlertValue( alertValue );
	}
    }
//-------------------------------------------------------------------------------

    public void addCheckListener(ActionListener acList)
    {
	if( _checkBoxUsed ) paramEnable.addActionListener( acList );
    }
//-------------------------------------------------------------------------------

    public void setEnabled(boolean state)
    {
	if( _checkBoxUsed ) {
	    if( !_checkBoxState ) 
		paramEnable.setSelected( state );
	    else
		paramEnable.setSelected( !state );
	    
	    desiredSelect.setEnabled( state );
	    
	    //paramEnable.setEnabled( state );
	}
	else desiredSelect.setEnabled( state );
    }
//-------------------------------------------------------------------------------

    public boolean isSelected()
    {
	if( _checkBoxUsed )
	    return paramEnable.isSelected();
	else
	    return true;
    }
//-------------------------------------------------------------------------------

    public void setSelected(boolean state)
    {
	if( _checkBoxUsed ) {
	    paramEnable.setSelected( state );
	    desiredSelect.setEnabled( state );
	}
    }
//-------------------------------------------------------------------------------

    public void compareWithSelection( boolean compare ) { _compareWithSelection = compare; }
    public void setIndexMethod() { desiredSelect.setIndexMethod(); }

    public String getActive() {	return activeItem.trim(); }
    public String getDesired() { return desiredItem.trim(); }
    public String getSelection() { return desiredSelect.getSelection(); }
    public String getSelectedItem() { return (String)desiredSelect.getSelectedItem(); }
    public int getSelectedIndex() { return desiredSelect.getSelectedIndex(); }
    public void setSelectedIndex(int index) { desiredSelect.setSelectedIndex(index); }
    public void setSelectedItem(String item) { desiredSelect.setSelectedItem(item); }

//-------------------------------------------------------------------------------

    public void blankActive() { activeField.setText(" "); }

    public void setActive( String newItem ) { setActive( true, newItem, true ); }
    public void setActive( boolean doLog, String newItem ) { setActive( doLog, newItem, true ); }
    public void setActive( String newItem, boolean beepOnError ) { setActive( true, newItem, beepOnError ); }

    public void setActive( boolean doLog, String newItem, boolean beepOnError )
    {
	if( desiredSelect.indexMethod() )
	    {
		if( newItem.length() > 0 ) {
		    try {
			int newIndex = Integer.parseInt( newItem );
			activeItem = _items[newIndex];
		    }
		    catch( NumberFormatException nfe ) {
			System.err.println("Unknown value [" + newItem + "] for " + _name);
			activeItem = "?";
		    }
		}
		else activeItem = "?";
	    }
	else activeItem = newItem.trim();

	compareItems();

	if( ! activeField.getText().equals( activeItem ) )
	    activeField.setNewState( doLog, activeItem, beepOnError );
    }
//-------------------------------------------------------------------------------

    public void setDesired( String newItem )
    {
	if( desiredSelect.indexMethod() )
	    {
		if( newItem.length() > 0 ) {
		    int newIndex = Integer.parseInt( newItem );
		    desiredItem = _items[newIndex];
		}
		else desiredItem = "?";
	    }
	else desiredItem = newItem.trim();

	desiredSelect.setNewState( desiredItem );
	compareItems();
    }
//-------------------------------------------------------------------------------

    public void setDesiredSel( String newItem )
    {
	setDesired( newItem );
	desiredSelect.setSelectedItem( newItem );
    }
//-------------------------------------------------------------------------------

    void compareItems()
    {
	if( _compareWithSelection )
	    {
		desiredItem = (String)desiredSelect.getSelectedItem();
		if( desiredItem.length() > 0 ) setColor();
	    }
	else setColor(); //compare activeItem directly to desiredItem (from setDesired).
    }
//-------------------------------------------------------------------------------

    void setColor()
    {
	boolean alertSet = false;

	for( int i=0; i < _alertItems.size(); i++ ) {
	    if( desiredItem.equalsIgnoreCase( (String)_alertItems.elementAt(i) ) ) {
		alertSet = true;
		desiredSelect.setBackground( Color.orange );
	    }
	}

	if( !alertSet ) {
	    desiredSelect.setForeground( Color.black );

	    if( desiredItem.toUpperCase().equals( activeItem.toUpperCase() ) )
		desiredSelect.setBackground( desiredSelect._normalColor );
	    else
		desiredSelect.setBackground( Color.yellow );
	}
    }
//-------------------------------------------------------------------------------

    public String name() { return _name; }
    public String nameUp() { return _name.toUpperCase(); }
    public String nameLow() { return _name.toLowerCase(); }
    public String units() { return _units; }
    public String device() { return _device; }

} //end of class UFComboPanel
