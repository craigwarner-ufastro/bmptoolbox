package javaUFLib;

//Title:        UFRadioButtons.java:  
//Version:      (see rcsID)
//Copyright:    Copyright (c) Frank Varosi
//Author:       Frank Varosi 2009
//Company:      University of Florida
//Description:  Extends ButtonGroup to group JRadioButtons and method getSelectedName();

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
//===================================================================================================

public class UFRadioButtons extends ButtonGroup {

    public static final
	String rcsID = "$Name:  $ $Id: UFRadioButtons.java,v 1.8 2009/04/16 02:25:56 varosi Exp $";

    protected JRadioButton[] _radioButtons;
    protected JPanel _buttonsPanel;
    private boolean _visibleEnable = true;
//---------------------------------------------------------------------------------------------

    public UFRadioButtons( int nrows, int ncols, String[] buttonNames ) {
	this( nrows, ncols, 2, 2, false, buttonNames, null );
    }

    public UFRadioButtons( int nrows, int ncols, int hgap, int vgap, String[] buttonNames ) {
	this( nrows, ncols, hgap, vgap, false, buttonNames, null );
    }

    public UFRadioButtons( int nrows, int ncols, String[] buttonNames, String title ) {
	this( nrows, ncols, 2, 2, false, buttonNames, title );
    }

    public UFRadioButtons( int nrows, int ncols,
			   int hgap, int vgap, boolean borders,
			   String[] buttonNames, String title ) {

	int nbuts = buttonNames.length;

	if( ncols == 1 ) nrows = 0;
	else if( nrows == 1 ) ncols = 0;
	else {
	    int nrc = nrows * ncols;
	    if( nbuts > nrc ) nbuts = nrc;
	}

	_radioButtons = new JRadioButton[nbuts];
	JLabel titLab = null;

	_buttonsPanel = new JPanel( new GridLayout( nrows, ncols, hgap, vgap ) );
	_buttonsPanel.setBorder( BorderFactory.createLineBorder(Color.black) );

	if( title != null ) {
	    if( title.length() > 0 ) {
		if( ncols == 1 )
		    _buttonsPanel.add( new JLabel(title, JLabel.CENTER) );
		else if( nrows == 1 )
		    _buttonsPanel.add( new JLabel(title) );
	    }
	}

	for( int i=0; i < nbuts; i++ ) {
	    _radioButtons[i] = new JRadioButton( buttonNames[i], (i==0) );
	    this.add( _radioButtons[i] );
	    JPanel buttonArea = new JPanel(new FlowLayout( FlowLayout.LEFT, 7, 5 ));
	    if( borders ) buttonArea.setBorder( BorderFactory.createEtchedBorder(1) );
	    buttonArea.add( _radioButtons[i] );
	    _buttonsPanel.add( buttonArea );
	}
    }
//---------------------------------------------------------------------------------------------

    public JPanel buttonsPanel() { return _buttonsPanel; }

    public void setEnabled(boolean enable) {
	for( int i=0; i < _radioButtons.length; i++ ) _radioButtons[i].setEnabled( enable );
	if( _visibleEnable ) _buttonsPanel.setVisible( enable );
    }

    public void setSelected( String name ) {
	for( int i=0; i < _radioButtons.length; i++ ) {
	    if( _radioButtons[i].getText().equalsIgnoreCase( name ) ) 
		_radioButtons[i].setSelected(true);
	}
    }

    public void setSelected( int bnum ) {
	if( bnum >= 0 && bnum < _radioButtons.length ) _radioButtons[bnum].setSelected(true);
    }
//---------------------------------------------------------------------------------------------

    public String getSelectedName() {

	for( Enumeration e = this.getElements(); e.hasMoreElements(); ) {
	    JRadioButton b = (JRadioButton)e.nextElement();
	    if( b.getModel() == this.getSelection() ) return b.getText();
	}
	return null;
    }

    public int getSelectedNumber() {

	for( int i=0; i < _radioButtons.length; i++ ) {
		if( _radioButtons[i].getModel() == this.getSelection() ) return(i);
	}
	return(-1);
    }

    public JRadioButton getButton( String name ) {

	for( Enumeration e = this.getElements(); e.hasMoreElements(); ) {
		JRadioButton rb = (JRadioButton)e.nextElement();
		if( rb.getText().equals( name.trim() ) ) return rb;
	}
	return null;
    }
//---------------------------------------------------------------------------------------------

    public void addActionListener( ActionListener actionListener ) {
	for( int i=0; i < _radioButtons.length; i++ )
	    _radioButtons[i].addActionListener( actionListener );
    }
}
