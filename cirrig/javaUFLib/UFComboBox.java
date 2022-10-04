package javaUFLib;

//Title:        Extension of JAVA ComboBox class, using String items only.
//Version:      2.0
//Copyright:    Copyright (c) Frank Varosi and David Rashkin
//Author:       Frank Varosi and David Rashkin, 2003
//Company:      University of Florida
//Description:  Combo box keeps track of selections and sets color based on prev/curr selection.

import java.awt.*;
import java.awt.event.*;
import javax.swing.JComboBox;
import javax.swing.border.BevelBorder;
import java.util.Vector;

//===============================================================================
/**
 * Extension of JComboBox assuming String array of items,
 * with item Listener that sets its color depending on selection,
 * and can return either item or the index of item selected.
 * Optional mode to return String( of item index ) instead.
 */

public class UFComboBox extends JComboBox implements ItemListener
{
    static final String _rcsid = "$Name:  $ $Id: UFComboBox.java,v 1.29 2016/03/21 19:49:42 varosi Exp $";

    public static final int INDEX = 0;
    public static final int ITEM = 1;
    protected int item_OR_index = ITEM;

    protected String selected_item = "";
    protected String prev_item = "";
    protected String _name = "";

    protected String[] _items;
    protected Vector _alertItems = new Vector();

    protected Color _greenWhite = new Color(245,249,245);
    protected Color _nearWhite = new Color(245,245,245);
    protected Color _normalColor = _greenWhite;
//-------------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public UFComboBox()
    {
	try {
	    initComponent();
	}
	catch(Exception x) { System.out.println("Error creating UFComboBox: " + x.toString());	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param items     Array of Strings: list of items to show in ComboBox
     */
    public UFComboBox( String[] items )
    {
	this( items, "noname" );
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param items     Array of Strings: list of items to show in ComboBox
     */
    public UFComboBox( String[] items, String name )
    {
	super( items );
	_name = name;

	try {
	    initComponent();
	    registerItems();
	}
	catch(Exception x) { System.out.println("Error creating UFComboBox("+name+"): " + x.toString());	}
    }
//-------------------------------------------------------------------------------

    private void initComponent() throws Exception
    {
	this.setMaximumRowCount(17);
	this.setBorder(new BevelBorder( BevelBorder.LOWERED ));
	this.setBackground( _normalColor );
	this.addItemListener(this);
    }
//-------------------------------------------------------------------------------
    /**
     *UFComboBox#registerItems()
     * This method needs to be called if list of items is changed or added after creation,
     * to update the internal list use for approximate method of setSelectedItem(...).
     */
    public void registerItems()
    {
	_items = new String[ getItemCount() ];

	for( int i=0; i < _items.length; i++ ) _items[i] = (String)getItemAt(i);
    }
//-------------------------------------------------------------------------------
    /**
     *UFComboBox#setSelectedItem( String )
     *@param nearItem   String: the first few or all characters of desired item to set.
     */
    public void setSelectedItem( String nearItem )
    {
	//first try exact case matching :

	for( int i=0; i < _items.length; i++ ) {
	    if( _items[i].equals( nearItem ) ) {
		super.setSelectedItem( _items[i] );
		return;
	    }
	}

	// then try exact case-independent matching:

	for( int i=0; i < _items.length; i++ ) {
	    if( _items[i].toLowerCase().equals( nearItem.toLowerCase() ) ) {
		super.setSelectedItem( _items[i] );
		return;
	    }
	}

	//next try same characters matching :

	for( int i=0; i < _items.length; i++ ) {
	    if( _items[i].indexOf( nearItem ) == 0 ) {
		super.setSelectedItem( _items[i] );
		return;
	    }
	}

	// then try same chars. case-independent matching:

	for( int i=0; i < _items.length; i++ ) {
	    if( _items[i].toLowerCase().indexOf( nearItem.toLowerCase() ) == 0 ) {
		super.setSelectedItem( _items[i] );
		return;
	    }
	}
    }
//-------------------------------------------------------------------------------
    /**
     *UFComboBox#setAlertValue
     *@param alertVal         String: item value for which red color should be indicated if selected.
     */
    public void setAlertValue( String alertVal ) { if( alertVal != null ) _alertItems.add( alertVal ); }

//-------------------------------------------------------------------------------

    public void setIndexMethod() { item_OR_index = INDEX; }
    public void setItemMethod() { item_OR_index = ITEM; }

    public boolean indexMethod() { return( item_OR_index == INDEX ); }
    public String getSelection() { return selected_item; }

//-------------------------------------------------------------------------------
    /**
     * Event Handling Method
     *@param ie ItemEvent
     */
    public void itemStateChanged(ItemEvent ie)
    {
      if( ie.getStateChange() == ItemEvent.SELECTED )
	  {
	      selected_item = _getSelectedItem();
	      boolean alertSet = false;

	      for( int i=0; i < _alertItems.size(); i++ ) {
		  if( selected_item.equalsIgnoreCase( (String)_alertItems.elementAt(i) ) ) {
		      alertSet = true;
		      setBackground( Color.orange );
		  }
	      }

	      if( !alertSet ) {
		  setForeground( Color.black );

		  if( selected_item.equals( prev_item ) )
		      setBackground( Color.white );
		  else if( prev_item.length() > 0 )
		      setBackground( Color.yellow );
		  else
		      prev_item = selected_item;
	      }
	  }
    }
//-------------------------------------------------------------------------------
    /**
     * Returns the String name of the selected item in the combo box,
     * with comments after blank removed, 
     * or, returns the integer index of the selected item in the combo box, as a string.
     */
    private String _getSelectedItem()
    {
	if( item_OR_index == ITEM )
	    {
		String item = (String)getSelectedItem();
		if (item == null) item = "";
		if( item.indexOf(" ") > 0 ) //anything after a blank is a comment so eliminate it:
		    item = item.substring( 0, item.indexOf(" ") );
		return item.trim();
	    }
	else return String.valueOf( getSelectedIndex() ).trim();
    }
//-------------------------------------------------------------------------------

    public void setNewState( String newValue )
    {
	if( selected_item.equals( newValue.trim() ) )
	    {
		setBackground( Color.white );
		prev_item = selected_item;
	    }
	else setBackground( Color.yellow );
    }

    public void setNewState()
    {
	setBackground( Color.white );
	prev_item = selected_item;
    }
//-------------------------------------------------------------------------------
    //Variation to first parse string for stuff after delimeter and then setNewState:

    public void setNewState( String statusString, String delimeter )
    {
	String[] words = statusString.split( delimeter );
	setNewState( words[words.length-1] );
    }
} //end of class UFComboBox
