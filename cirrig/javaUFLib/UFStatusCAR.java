package javaUFLib;

//Title:        UFStatusCAR for Java Control Interface (JCI)
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  Creates 2 UFLabels for Command Action and Response, using UFMessageLog class for history.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//===============================================================================
/**
 * Creates 2 UFLabels vertically aligned, for Command Action and Response fields,
 * and uses UFMessageLog class for history of Actions & Responses.
 */

public class UFStatusCAR extends JPanel implements MouseListener
{
    public static final
	String rcsID = "$Name:  $ $Id: UFStatusCAR.java,v 1.7 2009/05/01 20:13:32 varosi Exp $";

    // for Command Action & Response info:
    protected UFLabel statusAction   = new UFLabel("Action :");
    protected UFLabel statusResponse = new UFLabel("Response :", new Color(0,99,0) ); //dark green.

    protected String Name;
    protected JPopupMenu popupMenu;
    protected UFMessageLog _Log;

//-------------------------------------------------------------------------------
    /**
     *Default Constructor
     */
    public UFStatusCAR() {
	try {
	    createComponent("CAR");
	}
	catch(Exception ex) {
	    System.out.println("Error creating a UFStatusCAR: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param description String: Information regarding the field
     */
    public UFStatusCAR(String description) {
	try {
	    createComponent(description);
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFStatusCAR: " + description + " : " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization
     *@param description String: Information regarding the CAR
     */
    private void createComponent( String description ) throws Exception
    {
	this.Name = description.trim();
	this.setLayout(new RatioLayout());
	this.setBorder(new EtchedBorder());
	this.add("0.01,0.0;0.99,0.5", statusAction );
	this.add("0.01,0.5;0.99,0.5", statusResponse );

	this._Log = new UFMessageLog( description + "  Cmd. Action > Response", 1000 );
        this.popupMenu = _Log.createPopupMenu();
	this.setToolTipText("Click right mouse button to view Action > Response Log.");
	this.addMouseListener(this);
    }
//-------------------------------------------------------------------------------

    public void mousePressed(MouseEvent mevt) {
	if(( mevt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 ) {
	    if( mevt.isPopupTrigger() ) {
		popupMenu.show( mevt.getComponent(), mevt.getX(), mevt.getY() );
	    }
	}
    }

    public void mouseClicked(MouseEvent mevt) {}
    public void mouseReleased(MouseEvent mevt) {}
    public void mouseEntered(MouseEvent mevt) {}
    public void mouseExited(MouseEvent mevt) {}

//-------------------------------------------------------------------------------

    public void showAction( String action )
    {
	statusAction.setText( action );
	statusResponse.setText(" waiting...", UFLabel._darkYellow);
	_Log.appendMessage("Action :\t" + action);
    }

    public void showAction( String action, boolean postit )
    {
	if( postit ) {
	    statusAction.setText( action );
	    statusResponse.setText(" waiting...", UFLabel._darkYellow);
	}
	_Log.appendMessage("Action :\t" + action);
    }

    public void showAction( boolean doLog, String action )
    {
	statusAction.setText( action );
	if( doLog ) _Log.appendMessage("Action :\t" + action);
    }
//-------------------------------------------------------------------------------

    public void showResponse( String response )
    {
	statusResponse.setText( response );
	_Log.appendMessage("Response :\t" + response);
    }

    public void showResponse( String response, boolean postit )
    {
	if( postit ) statusResponse.setText( response );
	_Log.appendMessage("Response :\t" + response);
    }

    public void showResponse( String response, Color color )
    {
	statusResponse.setText( response, color );
	_Log.appendMessage("Response :\t" + response);
    }
}
