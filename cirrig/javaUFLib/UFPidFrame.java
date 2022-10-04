package javaUFLib;

//Title:        UFPidFrame class for Java Control Interface (JCI)
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2008-2010
//Author:       Shaun McDowell
//Company:      University of Florida
//Description:  for display and changing of PID loop parameters

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javaUFProtocol.*;
//============================================================================================================

public class UFPidFrame extends JFrame {

    public final String rcsID = "$Name:  $ $Id: UFPidFrame.java,v 1.14 2014/02/13 02:11:00 varosi Exp $";

    protected JFrame _parentFrame;
    protected UFLibPanel _ifcePanel;
    protected String[] _LoopNames;

    protected HashMap< String, UFTextPanel > paramsMap = new HashMap< String, UFTextPanel >();
    private float _tpFrac = 0.55f;
    protected UFTextPanel setPointPanel = new UFTextPanel( true, "PID Setpoint", _tpFrac );
    protected UFTextPanel pPanel = new UFTextPanel( true, "Proportion", _tpFrac );
    protected UFTextPanel iPanel = new UFTextPanel( true, "Integral", _tpFrac );
    protected UFTextPanel dPanel = new UFTextPanel( true, "Derivative", _tpFrac );
    protected UFTextPanel biasPanel = new UFTextPanel( true, "Output Bias", _tpFrac );
    protected UFTextPanel maxPowerPanel = new UFTextPanel( true, "Max Power", _tpFrac );
    protected UFTextPanel filterWeightPanel = new UFTextPanel( true, "Filter Weight", _tpFrac );
    protected UFTextPanel manualPanel = new UFTextPanel( true, "Manual Mode Power", _tpFrac );
    protected UFTextPanel tProbesPanel = new UFTextPanel( true, "T Probe Indices", _tpFrac );

    protected UFButton applyButton = new UFButton( "Apply All" );
    protected UFButton resetButton = new UFButton( "Reset PID" );
    protected UFButton cancelButton = new UFButton( "Close" );
    protected UFButton onButton = new UFButton( "Loop is OFF", Color.lightGray );
    protected UFButton verboseButton = new UFButton( "Verbose is OFF", Color.lightGray );
    protected UFButton manualModeButton = new UFButton( "Manual Mode is OFF", Color.lightGray );

    protected JPanel parametersPanel = new JPanel( new GridLayout( 0, 1, 5, 5 ) );
    protected JPanel pidLoopSelecPanel = new JPanel( new RatioLayout());
    protected JComboBox pidLoopChooser = null;
    protected boolean _tProbes = false;
//============================================================================================================
    
    public UFPidFrame( UFLibPanel ifcePanel ) {
	this( ifcePanel, null, false );
    }

    public UFPidFrame( UFLibPanel ifcePanel, JFrame parent ) {
	this( ifcePanel, parent, false);
    }
    
    public UFPidFrame( UFLibPanel ifcePanel, JFrame parent, boolean tProbes ) {
	super("PID parameters");

	_tProbes = tProbes;
	_parentFrame = parent;
	_ifcePanel = ifcePanel;

	paramsMap.put( "set point", setPointPanel );
	paramsMap.put( "p gain", pPanel );
	paramsMap.put( "i gain", iPanel );
	paramsMap.put( "d gain", dPanel );
	paramsMap.put( "output bias", biasPanel );
	paramsMap.put( "max power", maxPowerPanel );
	paramsMap.put( "filter weight", filterWeightPanel );
	paramsMap.put( "manual point", manualPanel );
	paramsMap.put( "t probes", tProbesPanel );

	for ( UFTextPanel tp : paramsMap.values() )
	    tp.setNumSigDigits( 6 );
	
	JPanel buttonPanel = new JPanel( new GridLayout(1,0,5,5) );
	JPanel statusPanel = new JPanel( new GridLayout(1,0,5,5) );
	JPanel titlePanel = new JPanel( new RatioLayout() );

	parametersPanel.setBackground( UFColor.panelColor() );
	titlePanel.setBackground( UFColor.panelColor() );
	statusPanel.setBackground( UFColor.panelColor() );
	buttonPanel.setBackground( UFColor.panelColor() );

	buttonPanel.add( applyButton );
	buttonPanel.add( cancelButton );
	buttonPanel.add( resetButton );

	statusPanel.add( manualModeButton );
	statusPanel.add( onButton );
	statusPanel.add( verboseButton );

	titlePanel.add( "0.01,0.01;0.44,0.98", new JLabel( "PID Parameters", JLabel.CENTER ) );
	titlePanel.add( "0.46,0.01;0.25,0.98", new JLabel( "Desired", JLabel.CENTER ) );
	titlePanel.add( "0.72,0.01;0.25,0.98", new JLabel( "Active", JLabel.CENTER ) );

	parametersPanel.add( titlePanel );
	parametersPanel.add( setPointPanel );
	parametersPanel.add( pPanel );
	parametersPanel.add( iPanel );
	parametersPanel.add( dPanel );
	parametersPanel.add( biasPanel );
	parametersPanel.add( filterWeightPanel );
	parametersPanel.add( manualPanel );

	if(_tProbes) {
	    parametersPanel.add( tProbesPanel );
	    parametersPanel.add( maxPowerPanel );
	}

	parametersPanel.add( buttonPanel );
	parametersPanel.add( statusPanel );
	parametersPanel.add( pidLoopSelecPanel );
	
	_setupActionListeners();
	this.setSize( 440, 440 );
	this.setContentPane( parametersPanel );
    }
//-----------------------------------------------------------------------------------------------------------
    // Center the PID Options Frame with respect to its parent frame

    public void setVisible( boolean vis ) {
	if( _parentFrame != null ) {
	    Point fLoc = _parentFrame.getLocationOnScreen();
	    this.setLocation( (int)fLoc.getX() + _parentFrame.getWidth() - this.getWidth(),
			      (int)fLoc.getY() - this.getHeight() );
	}
	if( vis ) {
	    _LoopNames = _requestLoopNames();
	    pidLoopChooser = new JComboBox( _LoopNames );
	    pidLoopChooser.setMaximumRowCount( 14 );
	    pidLoopChooser.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    _actionClearDesired();
		    _actionQueryPids();
		}
	    } );
	    pidLoopSelecPanel.removeAll();
	    pidLoopSelecPanel.add( "0.11,0.01;0.29,0.98", new JLabel("Select  Pid  Loop : ") );
	    pidLoopSelecPanel.add( "0.42,0.01;0.42,0.98", pidLoopChooser );
	    _actionQueryPids();
	}
	super.setVisible( vis );
    }
//-----------------------------------------------------------------------------------------------------------

    public void setParam( String param, float value ) {
	paramsMap.get( param ).setActive( value );
    }
    
    public void setParam( String param, String value ) {
	paramsMap.get( param ).setActive( value );
    }
//-----------------------------------------------------------------------------------------------------------

    public void setStatus( String button, String value ) {
	UFButton statusButton = null;
	if( button.equals( "manual" ) ) statusButton = manualModeButton;
	else if( button.equals( "verbose" ) ) statusButton = verboseButton;
	else if( button.equals( "onoff" ) ) statusButton = onButton;

	if( statusButton != null ) {
	    statusButton.setText( value );
	    statusButton.setBackground( ( value.indexOf( "OFF" ) >= 0 ) ? Color.lightGray : Color.green );
	}
    }
//-----------------------------------------------------------------------------------------------------------

    protected void _setupActionListeners() {
	applyButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		_actionApply();
	    }
	} );
	resetButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		_actionResetPid();
	    }
	} );
	manualModeButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		_actionManualMode();
	    }
	} );
	cancelButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		setVisible( false );
	    }
	} );
	verboseButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		_actionVerbose();
	    }
	});
	onButton.addActionListener( new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		_actionOnOff();
	    }
	});
	
    }
//-----------------------------------------------------------------------------------------------------------

    protected void _actionClearDesired() {
	setPointPanel.setValue(" ");
	pPanel.setValue(" ");
	iPanel.setValue(" ");
	dPanel.setValue(" ");
	biasPanel.setValue(" ");
	maxPowerPanel.setValue(" ");
	filterWeightPanel.setValue(" ");
	manualPanel.setValue(" ");
	tProbesPanel.setValue(" ");
    }
    
    protected void _actionQueryPids() {
	_actionLockPids();
	sendAgentComm("request values");
    }

    protected void _actionResetPid() {
	sendAgentComm("request restart");
    }
    
    protected void _actionVerbose() {
	if( verboseButton.getText().indexOf( "OFF" ) >= 0 ) sendAgentComm("request verbose on");
	else sendAgentComm("request verbose off" );
	_actionQueryPids();
    }
    
    protected void _actionOnOff() {
	if( onButton.getText().indexOf("OFF") >= 0 ) sendAgentComm("request turn on");
	else sendAgentComm("request turn off");
	_actionQueryPids();
    }

    protected void _actionLockPids() {
	setPointPanel.setLocked();
	pPanel.setLocked();
	iPanel.setLocked();
	dPanel.setLocked();
	biasPanel.setLocked();
	maxPowerPanel.setLocked();
	filterWeightPanel.setLocked();
	manualPanel.setLocked();
	tProbesPanel.setLocked();
    }

    protected void _actionApply() {
	for ( Map.Entry< String, UFTextPanel > e : paramsMap.entrySet() ) {
	    String param = e.getValue().getDesiredField();
	    if( param.length() > 0 && !param.equals( e.getValue().getActiveField() ) ) {
		sendAgentComm( "set " + e.getKey() + " = " + param );
		e.getValue().setValue2(" ");
		e.getValue().setDesired("");
	    }
	}
	_actionQueryPids();
    }

    protected void _actionManualMode() {
	if( manualModeButton.getText().indexOf( "OFF" ) >= 0 ) sendAgentComm("request manual control on");
	else sendAgentComm("request manual control off");

	_actionQueryPids();
    }
//-----------------------------------------------------------------------------------------------------------

    protected String[] _requestLoopNames()
    {
	String[] loopnames = sendAgentComm("loop names","PID:","UFPidFrame").split(",");
	Arrays.sort(loopnames);
	return loopnames;
    }
//-----------------------------------------------------------------------------------------------------------

    protected String sendAgentComm( String command )
    {
	return sendAgentComm( command, "PID: " + pidLoopChooser.getSelectedItem(), "pid options");
    }

    protected String sendAgentComm( String command, String pidLoop, String ctype ) {

	if( command == null ) return("");
	if( command.length() <= 0 ) return("");

	_ifcePanel.statusCAR.showAction( pidLoop + " > " + command );

	UFStrings ufpSend = new UFStrings( pidLoop, command );
	UFStrings ufpRecv = _ifcePanel.sendRecvAgent( ufpSend, ctype );

	if( ufpRecv == null ) {
	    _ifcePanel.statusCAR.showResponse( "ERROR: no reply from agent" );
	    return("");
	}

	if( ufpRecv instanceof UFStrings ) {
	    if( ufpRecv.elements() > 0 ) {

		String reply = ((UFStrings)ufpRecv).valData( 0 );

		if( ufpRecv.name().toUpperCase().indexOf("ERR") >= 0
		    || reply.toUpperCase().indexOf("ERR") >= 0
		    || reply.toUpperCase().indexOf("FAIL") >= 0 )
		    {
			if( reply.length() > 0 ) _ifcePanel.statusCAR.showResponse( reply );
			else _ifcePanel.statusCAR.showResponse( "ERR: empty response from agent" );
		    }
		else _ifcePanel.statusCAR.showResponse( reply );

		return reply;
	    }
	    else _ifcePanel.statusCAR.showResponse( "ERR: bad response from agent" );
	}

	return("");
    }
}
