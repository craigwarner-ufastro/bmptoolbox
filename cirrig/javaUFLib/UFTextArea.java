package javaUFLib;

//Title:        UFTextArea: extension of JTextArea with built in auto-limit of memory usage.
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2005-2008
//Authors:      Frank Varosi
//Company:      University of Florida
//Description:  If document size exceeds limit then cut in half, also scroll to bottom.

import java.awt.*;
import javax.swing.*;
import javaUFProtocol.*;

//===============================================================================
/**
 * Extends the JTextArea class to add some simple useful options,
 * mainly, to guard against running out of memory by imposing limit on document size,
 * and also to automatically scroll to bottom of area after appending new text.
 * Also has a ctor that will display a UFStrings object (like FITS header).
 */

public class UFTextArea extends JTextArea
{
    public static final
	String rcsID = "$Name:  $ $Id: UFTextArea.java,v 1.9 2011/05/23 06:02:12 varosi Exp $";

    protected final int _MB = 1024*1024;
    protected int _maxDocLength = _MB, _NmsgDisp=0;
    protected boolean _scrollToBottom = true;

    private JFrame _textFrame;

//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  nMegaBytes  int: max number of Mega-Bytes allowed for document length.
     */
    public UFTextArea(int nMegaBytes) {
	if( nMegaBytes < 1 ) nMegaBytes = 1;
	this._maxDocLength = nMegaBytes*_MB;
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  nMegaBytes  int: max number of Mega-Bytes allowed for document length.
     */
    public UFTextArea(int nMegaBytes, int rows, int cols) {
	super( rows, cols );
	if( nMegaBytes < 1 ) nMegaBytes = 1;
	this._maxDocLength = nMegaBytes*_MB;
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor to display a sequence of strings (UFStrings, assume FITS header).
     *@param  ufstrings  UFStrings: typically a FITS header.
     *@param  fontSize   int      : point size of Courier font.
     */
    public UFTextArea( UFStrings ufstrings ) { this( ufstrings, 14, false, 690, 520, true ); }

    public UFTextArea( UFStrings ufstrings, int xsize, int ysize ) { this( ufstrings, 14, false, xsize, ysize, true ); }
    
    public UFTextArea( UFStrings ufstrings, int xsize, int ysize, boolean vis ) {
	this( ufstrings, 14, false, xsize, ysize, vis ); }

    public UFTextArea( UFStrings ufstrings, int fontSize ) { this( ufstrings, fontSize, false, 690, 520, true ); }

    public UFTextArea( UFStrings ufstrings, int fontSize, boolean fontBold, int xsize, int ysize, boolean vis ) {

	super( 64, 80 );
	if( fontSize < 12 ) fontSize = 12;
	if( fontBold )
	    this.setFont(new Font("Courier", Font.BOLD, fontSize));
	else
	    this.setFont(new Font("Courier", Font.PLAIN, fontSize));
	String title = "?";

	if( ufstrings != null ) {
	    for( int i=0; i < ufstrings.numVals(); i++ ) super.append(" " + ufstrings.stringAt(i) + "\n");
	    title = ufstrings.name();
	}

	JScrollPane textScroll = new JScrollPane(this);
	this.setCaretPosition(0);
	this.setEditable(false);
	_textFrame = new JFrame(title);
	_textFrame.setSize(new Dimension( xsize, ysize ));
	_textFrame.getContentPane().add( textScroll );
	_textFrame.setVisible(vis);
    }
    
//-------------------------------------------------------------------------------

    public void redisplay( UFStrings ufstrings ) {
	this.reLoad( ufstrings );
	_textFrame.setVisible(true);
    }
//-------------------------------------------------------------------------------

    public void reLoad( UFStrings ufstrings ) {

	if( ufstrings == null ) {
	    System.out.println("UFTextArea.redisplay> ufstrings arg. is null.");
	    return;
	}

	if( _textFrame == null ) {
	    JScrollPane textScroll = new JScrollPane(this);
	    _textFrame = new JFrame( ufstrings.name() );
	    _textFrame.setSize(new Dimension(690,440));
	    _textFrame.getContentPane().add( textScroll );
	}

	_textFrame.setTitle( ufstrings.name() );
	super.setText("");
	for( int i=0; i < ufstrings.numVals(); i++ ) super.append(" " + ufstrings.stringAt(i) + "\n");
	this.setCaretPosition(0);
	this.setEditable(false);
    }
//-------------------------------------------------------------------------------

    public void reShow() { _textFrame.setVisible(true); }
    public void noShow() { _textFrame.setVisible(false); }

//-------------------------------------------------------------------------------

    public void setMaxDocLength(int nMB) {
	if( nMB < 1 ) nMB = 1;
	this._maxDocLength = nMB*_MB;
    }

    public void autoScrollToBottom(boolean b) { _scrollToBottom = b; }

    public int NmsgDisp() { return _NmsgDisp; }

//-------------------------------------------------------------------------------
    /**
     * Override append method to check document length and limit it,
     * and optionall scroll to bottom of area.
     */
    public void append(String newText)
    {
	if( this.getDocument().getLength() > _maxDocLength ) {
	    //cut text area contents in half to stay within limit:
	    String oldText = this.getText();
	    this.setText( oldText.substring( oldText.length()/2 ) );
	    _NmsgDisp /= 2;
	    oldText = null;
	}

	super.append( newText );
	++_NmsgDisp;
	if( _scrollToBottom ) this.setCaretPosition( this.getDocument().getLength() );
    }
} //end of class UFTextArea
