package javaUFLib;

//Title:        UFHostPortPanel for Java Control Interface (JCI)
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  for control and monitor of CanariCam infrared camera system.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//===============================================================================
/**
 * Creates 2 text fields with Labels on left for entering and storing Hostname and Port #,
 * all in one Panel, with methods for accessing and setting the values.
 */

public class UFHostPortPanel extends JPanel
{
    public static final
	String rcsID = "$Name:  $ $Id: UFHostPortPanel.java,v 1.12 2008/02/01 01:20:06 varosi Exp $";

    private String _host = "";
    private int _port = 0;

    protected UFTextField hostName;
    protected UFTextField portNum;

    protected JCheckBox checkBoxHost = new JCheckBox("Host:",false);
    protected JCheckBox checkBoxPort = new JCheckBox("Port:",false);

    private Color _disabled = new Color(240,230,220);
//-------------------------------------------------------------------------------
    /**
     *Default Constructor
     */
    public UFHostPortPanel() {
	try  {
	    _init();
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFHostPortPanel: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor with values.
     */
    public UFHostPortPanel( String hostname, int portnum ) {
	try  {
	    _host = hostname;
	    _port = portnum;
	    _init();
	}
	catch(Exception ex) {
	    System.out.println("Error creating UFHostPortPanel: " + ex.toString());
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization
     */
    private void _init() throws Exception
    {
	hostName = new UFTextField( "Host", _host, 4 );
	portNum = new UFTextField( "Port", Integer.toString( _port ), 4 );

	hostName.setEditable(false);
	hostName.setBackground(_disabled);
	portNum.setEditable(false);
	portNum.setBackground(_disabled);

	checkBoxHost.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if( checkBoxHost.isSelected() ) {
			hostName.setBackground( hostName._normalColor );
			hostName.setEditable(true);
		    }
		    else {
			hostName.setEditable(false);
			hostName.setNewState();
			hostName.setBackground(_disabled);
		    }
		}
	    });

	checkBoxPort.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if( checkBoxPort.isSelected() ) {
			portNum.setBackground( portNum._normalColor );
			portNum.setEditable(true);
		    }
		    else {
			portNum.setEditable(false);
			portNum.setNewState();
			portNum.setBackground(_disabled);
		    }
		}
	    });

	this.setLayout(new RatioLayout());
	this.add("0.00,0.1;0.25,0.8", checkBoxHost );
	this.add("0.25,0.1;0.23,0.8", hostName );
	this.add("0.52,0.1;0.24,0.8", checkBoxPort );
	this.add("0.76,0.1;0.23,0.8", portNum );
    }
//-------------------------------------------------------------------------------

    public void setHostAndPort( String host, int port ) {
	setHost( host );
	setPort( port );
    }

    public void setHost(String host) {
	if( host != null ) {
	    if( host.trim().length() > 0 ) {
		_host = host.trim();
		hostName.setNewState( _host );
		if( !checkBoxHost.isSelected() ) hostName.setBackground(_disabled);
	    }
	}
    }

    public void setPort(int port) {
	if( port > 0 ) {
	    _port = port;
	    portNum.setNewState( Integer.toString( _port ) );
	    if( !checkBoxPort.isSelected() ) portNum.setBackground(_disabled);
	}
    }

    public String getHost() { return _host; }
    public int getPort() { return _port; }

    public String getHostField() {
	_host = hostName.getText().trim();
	return _host;
    }

    public int getPortField() {
	_port = Integer.parseInt( portNum.getText().trim() );
	return _port;
    }
}
