package javaUFLib;

//Title:        UFexpMonitor for Java Control Interface (JCI) using UFLib Protocol
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2009
//Author:       Frank Varosi
//Company:      University of Florida, Dept. of Astronomy.
//Description:  For monitoring of CCD status and exposure progress from DAS status stream.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import javaUFProtocol.*;

//===============================================================================
/**
 * Exposure Status Monitor Panel by extending UFMonitorPanel.
 * Custom panel for status monitoring of Detector Control Agent or Data Acq. Server.
 * @author Frank Varosi
 */

public class UFexpMonitor extends UFMonitorPanel {

    public static final
	String rcsID = "$Name:  $ $Id: UFexpMonitor.java,v 1.21 2011/02/18 13:59:48 varosi Exp $";

    //used by recvNotifications() method:
    private DataInputStream _notifyStream;
    private int _nbeeps = 3;  //# of times to beep at end of exp.
    private int _expCount = 0;
    private boolean _expNow=false, _gotStart=false, _gotFinal=false;
    private boolean _newExp=false, _nearEnd=false, _newReadout=false, _showNoise=false;

    protected final int _maxProgBar = 100;
    private Color _colorProgBar;
    protected JProgressBar expProgressBar = new JProgressBar(0, _maxProgBar);
    protected JProgressBar rdoutProgressBar = new JProgressBar(0, _maxProgBar);
    public JPopupMenu popupMenu;
    protected UFImageConfig[] saveImageConfs = new UFImageConfig[32000];

    protected UFStrings _FITSheader;
    protected String _FITSfile;
    protected UFTextField acqServerStatus  = new UFTextField("Data Server:",false); //editable = false.

    protected UFTextPanel statExpDone =   new UFTextPanel("Exposure =", false, 0.66, false);
    protected UFTextPanel statRdoutDone = new UFTextPanel("Readout =", false, 0.66, false);

    protected UFTextPanel statExpTime =  new UFTextPanel(0.44,"Exp. Time (sec) =");
    protected UFTextPanel statAcqType =  new UFTextPanel(0.33,"Acq. Type =");
    protected UFTextPanel statReadMode = new UFTextPanel(0.33,"Rd. Mode =");

    protected UFTextPanel statShutter = new UFTextPanel(0.44,"Shutter =");
    protected UFTextPanel statCooler =  new UFTextPanel(0.44,"CryoCool =");

    //first arg. false means not editable but ctor make Panel with active field, no desired field:
    protected UFTextPanel statPMTavg = new UFTextPanel(false,0.44,"PMT avg. =");
    protected UFTextPanel statTempC = new UFTextPanel(false,0.44,"T (C) =");
    protected UFTextPanel statPress = new UFTextPanel(false,0.44,"P =");

    protected UFTextPanel statNoise = new UFTextPanel(false,0.44,"img.Noise=");
    protected UFTextPanel statMax = new UFTextPanel(false,0.44,"img. Max =");
    protected UFTextPanel statAvg = new UFTextPanel(false,0.44,"img. Avg =");
    protected UFTextPanel statMin = new UFTextPanel(false,0.44,"img. Min =");

    protected UFTextPanel statGrabCnt =  new UFTextPanel(0.33,"# images grabbed =");
    protected UFTextPanel statProCnt =   new UFTextPanel(0.33,"# images processed =");
    protected UFTextPanel statWriteCnt = new UFTextPanel(0.33,"# images written =");

    protected JPanel statusPanel = new JPanel();
    protected JPanel statusPan2 = new JPanel();

    private HashMap statusObjects = new HashMap(4);  //container of following UFTextField objects:
    protected UFTextField observStatus = new UFTextField("observStatus",false);
    protected UFTextField dcState      = new UFTextField("dcState",false);
    protected UFTextField detType      = new UFTextField("detType",false);
    protected UFTextField dataFile     = new UFTextField("FITS_FileName",false);

    protected UFLibPanel _MasterPanel;   //used to get new params status.
    protected UFLibPanel _DetectorPanel; //used to get same host and port as in Detector Panel.
    protected UFLibPanel _DataDisplay;   //option for quick-look data display updates.

    private String[] _closeOpen = {"CLOSED","OPEN"};
    private String[] _offOn = {"OFF","ON"};
    private double _alarmT = -91.0;  // CCD Temperature Alarm max threshold
    private double _alarmP = 0.01;   // CCD Pressure Alarm max threshold
//-------------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public UFexpMonitor() {
	super( "kepler", 42008 ); //to give default host and port to UFLibPanel.
	_init();
    } 
//-------------------------------------------------------------------------------
    /**
     * Constructor with host and port.
     */
    public UFexpMonitor( String hostName, int portNum )
    {
	super( hostName, portNum );
	_init();
    }
//-------------------------------------------------------------------------------
    /**
     * Special constructor to get pointer to data display/access client of JDD.
     */
    public UFexpMonitor( UFLibPanel dataDispClient )
    {
	super( dataDispClient );
	this._DataDisplay = dataDispClient;
	_init();
    }
//-------------------------------------------------------------------------------
    /**
     * Special constructor to get pointers to Master and Detector panels in JCI.
     */
    public UFexpMonitor( UFLibPanel masterPanel, UFLibPanel detectorPanel )
    {
	super( detectorPanel );
	this._MasterPanel = masterPanel;
	this._DetectorPanel = detectorPanel;
	_init();
    }
//-------------------------------------------------------------------------------
    private void _init() {
	try {
	    _createMonitorPanel();
	    _monSocState = dcState; //used by method UFMonitorPanel.monSocketError()
	    createMonitorStream();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("UFexpMonitor> Failed creating Exp.Monitor display panel!");
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization called by super class UFMonitorPanel.
     */
    protected void _createMonitorPanel() throws Exception
    {
	expProgressBar.setValue(0);
	expProgressBar.setBackground( Color.white );
	_colorProgBar = expProgressBar.getForeground();

	statusObjects.put( observStatus.name(), observStatus );
	statusObjects.put( dcState.name(), dcState );
	statusObjects.put( detType.name(), detType );
	statusObjects.put( dataFile.name(), dataFile );

	statusPanel.setLayout(new GridLayout(0,1));
	statusPanel.setBorder(new EtchedBorder(0));
	statusPanel.add(statExpTime);
	statusPanel.add(statExpDone);
	statusPanel.add(statRdoutDone);
	statusPanel.add(statGrabCnt);
	statusPanel.add(statProCnt);
	statusPanel.add(statWriteCnt);

	statusPan2.setLayout(new GridLayout(0,1));
	statusPan2.setBorder(new EtchedBorder(0));
	statusPan2.add(statPMTavg);
	statusPan2.add(statTempC);
	statusPan2.add(statPress);
	statusPan2.add(statMax);
	statusPan2.add(statAvg);
	statusPan2.add(statMin);

	this.setLayout(new RatioLayout());
	this.add("0.01,0.02;0.09,0.14", new JLabel("Obs. STATUS:") );
	this.add("0.10,0.02;0.52,0.14", observStatus );
	this.add("0.01,0.18;0.09,0.14", new JLabel("Exp. Progress:") );
	this.add("0.10,0.18;0.52,0.14", expProgressBar );
	this.add("0.01,0.34;0.09,0.14", new JLabel("Readout Prog.:") );
	this.add("0.10,0.34;0.52,0.14", rdoutProgressBar );
	this.add("0.01,0.50;0.09,0.14", new JLabel("CCD state :") );
	this.add("0.10,0.50;0.52,0.14", dcState );
	this.add("0.01,0.68;0.09,0.14", new JLabel(acqServerStatus.Label()) );
	this.add("0.10,0.68;0.52,0.14", acqServerStatus );
	this.add("0.01,0.84;0.09,0.14", new JLabel("FITS  File:") );
	this.add("0.10,0.84;0.52,0.14", dataFile );
	this.add("0.63,0.01;0.22,0.99", statusPanel );
	this.add("0.86,0.01;0.14,0.99", statusPan2 );

	statPMTavg.newDuplicates(false);
	statMax.newDuplicates(false);
	statAvg.newDuplicates(false);
	statMin.newDuplicates(false);

	// Add the connect status socket action to Detector Panel connect button,
	// so when user re-connects to DC agent it will do both cmdSocket and statusSocket:

	if( _DetectorPanel != null ) {
	    serverName = "Det.Con. Agent";
	    _DetectorPanel.connectButton.addActionListener( new ActionListener()
		{ public void actionPerformed(ActionEvent e) { createMonitorStream(); } });
	}
	else if( _DataDisplay != null ) {
	    serverName = "Data Acq. Server";
	    _DataDisplay.connectButton.addActionListener( new ActionListener()
		{ public void actionPerformed(ActionEvent e) { createMonitorStream(); } });
	}

	popupMenu = _createPopupMenu();
	expProgressBar.setToolTipText("Click right button for Menu.");

	expProgressBar.addMouseListener( new MouseListener() {
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
    }
//--------------------------------------------------------------------------------------------------

    public void switchToShowNoise() {
	statusPan2.remove(statPMTavg);
	statusPan2.remove(statTempC);
	statusPan2.remove(statPress);
	statusPan2.remove(statMax);
	statusPan2.remove(statAvg);
	statusPan2.remove(statMin);
	_showNoise = true;
	statusPan2.add(statPMTavg);
	statusPan2.add(statTempC);
	statusPan2.add(statNoise);
	statusPan2.add(statMax);
	statusPan2.add(statAvg);
	statusPan2.add(statMin);
    }

    public void setAlarmT( double alarmT ) { _alarmT = alarmT; }

//--------------------------------------------------------------------------------------------------

    private JPopupMenu _createPopupMenu()
    {
	JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem setNumBeeps = new JMenuItem("Set number of beeps");
        JMenuItem setSoundBeep =  new JMenuItem("Set sound to beeping");
        JMenuItem setSoundMonty =  new JMenuItem("Set sound to Monty Python");
        popupMenu.add(setNumBeeps);
        popupMenu.add(setSoundBeep);
        popupMenu.add(setSoundMonty);

        setNumBeeps.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { actionSetNumBeeps(); } });

        setSoundBeep.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { actionSetSound(0); } });

        setSoundMonty.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { actionSetSound(1); } });

	return popupMenu;
    }
//-------------------------------------------------------------------------------

    public void actionSetNumBeeps()
    {
	String numBeeps = JOptionPane.showInputDialog("Enter number of times to beep or repeat sound :");

	if( numBeeps != null ) {
	    try {
		_nbeeps = Integer.parseInt( numBeeps.trim() );
		if( _nbeeps > 9 ) _nbeeps = 9;
		System.out.println( className+": at end of exp. # of beeps = " + _nbeeps );
	    }
	    catch (Exception ex) { ex.printStackTrace(); }
	}
    }

    public void actionSetSound(int soundNum)
    {
    }
//-------------------------------------------------------------------------------

    public boolean reConnect() {
	if( _DetectorPanel != null ) {
	    _DetectorPanel.connectToAgent();
	}
	else if( _DataDisplay != null ) {
	    _DataDisplay.connectToServer();
	}

	//this is important call to UFMonitorPanel method:
	return createMonitorStream();
    }
//-------------------------------------------------------------------------------
    //override the generic method in UFMonitorPanel:

    protected boolean configMonitor() throws Exception
    {
	//must send DC agent a timestamp with string "status" in it,
	// and then DC agent will automatically send status stream,
	// and status monitor thread will recv the stream:
	UFTimeStamp uft = new UFTimeStamp(clientName + ":STATUS");

	//but if talking to Data Acq.Server first request the CameraType instead,
	// and status stream notification is requested afterwards (see below):
	if( _DataDisplay != null ) uft.rename("CT");

	if( uft.sendTo(_monitorSocket) <= 0 ) {
	    monSocketError("configMonitor","Handshake Send");
	    return false;
	}

	//Note that error reporting method monSocketError() also closes the socket.
	//get response from agent
	UFProtocol ufp = null;

	if( (ufp = UFProtocol.createFrom(_monitorSocket)) == null ) {
	    monSocketError("configMonitor","Handshake Read");
	    return false;
	}

	String message = className + ".connectMonitor> " + ufp.name();

	if( _DataDisplay != null ) { //special case: request notification stream of ImageConfigs:
	    message = "CameraType = " + ufp.name();
	    dcState.setNewState( message );
	    //first request current image configs and status monitor thread will recv:
	    uft.rename("IC+CAMSTAT");
	    if( uft.sendTo(_monitorSocket) <= 0 ) {
		monSocketError("configMonitor","ImageConfig request");
		return false;
	    }
	    //Request notification stream of ImageConfig updates
	    // and _statusMonitor thread will recv the stream:
	    uft.rename("NOTIFY");
	    if( uft.sendTo(_monitorSocket) <= 0 ) {
		monSocketError("configMonitor","Notify request");
		return false;
	    }
	    //create input stream object for recvNotifies() method to check for available notifies:
	    _notifyStream = new DataInputStream( _monitorSocket.getInputStream() );
	}

	System.out.println( message );
	return true;
    }
//-------------------------------------------------------------------------------
    /**
     * Method used by UFMonitorPanel.recvStatus() to process the status info object.
     */
    protected void procStatusInfo( UFProtocol ufp )
    {
	if( ufp == null ) return;

	if( ufp instanceof UFImageConfig )
	    {
		procImageConfig( (UFImageConfig)ufp );
	    }
	else if( ufp instanceof UFStrings )
	    {
		UFStrings ufs = (UFStrings)ufp;
		UFTextField statusToUpdate = (UFTextField)statusObjects.get( ufs.name() );

		if( statusToUpdate == null )
		    {
			String name = ufs.name();

			if( name.indexOf("getStatus") >= 0 ) //check if new DC params available:
			    {
				String what = ufs.valData(0);
				if( what.indexOf("Param") > 0 ) {
				    if( _MasterPanel != null )
					_MasterPanel.getNewParams( clientName );
				    else if( _DetectorPanel != null )
					_DetectorPanel.getNewParams( clientName );
				    return;
				}
			    }
			else if( name.indexOf("HEADER") >= 0 )
			    {
				_FITSheader = ufs;

				if( name.indexOf("/") > 0 )
				    _FITSfile = name.substring( name.indexOf("/") );
				else
				    _FITSfile = "none";

				dataFile.setNewState( _FITSfile );

				if( _DataDisplay != null ) {
				    _FITSheader.rename( _FITSfile );
				    _DataDisplay.updateObsInfo( _FITSheader );
				}
				return;
			    }
			else if( ufs.elements() == 4 )
			    {
				String msg = observStatus.getText() + "  (" + ufs.valData(3) + ")";
				System.out.println( msg );
				observStatus.setNewState( msg );
			    }
			else System.out.println( className+"::recvStatus> unknown UFStrings:\n"
						 +" name = "+ufs.name()+", value = "+ufs.valData(0) );
		    }
		else statusToUpdate.setNewState( ufs.valData(0) );

		if( _MasterPanel != null ) {
		    if( ufs.name().toUpperCase().indexOf("FILE") >= 0 )
			_MasterPanel.updateObsInfo( ufs );
		}
	    }
	else {
	    String errmsg = className + "::recvStatus> unexpected object:	"
		+ ufp.description() + ": Length=" + ufp.length() + "\n name = " + ufp.name() + ".";
	    System.err.println( errmsg );
	}
    }
//-------------------------------------------------------------------------------
    private void _signalNearEndofExp()
    {
	if( !_nearEnd ) for( int i=0; i < 2*_nbeeps; i++ ) Toolkit.getDefaultToolkit().beep();
	_nearEnd = true;
    }
//-------------------------------------------------------------------------------
    private void _signalEndofExp()
    {
	if( _newExp ) {
	    for( int i=0; i < _nbeeps; i++ ) Toolkit.getDefaultToolkit().beep();
	    _newExp = false;
	    expProgressBar.setForeground( Color.green );
	}
    }
//-------------------------------------------------------------------------------
    private void _signalEndofReadout()
    {
	if( _newReadout ) {
	    for( int i=0; i < _nbeeps; i++ ) Toolkit.getDefaultToolkit().beep();
	    _newReadout = false;
	    rdoutProgressBar.setForeground( Color.green );
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Method used by  procStatusInfo() to process a UFImageConfig object.
     */
    protected void procImageConfig( UFImageConfig newIC )
    {
	float minsRemain = (100-newIC.expDone) * newIC.expSeconds/100/60;
	float minsExp = newIC.expSeconds/60;

	if( newIC.imageID < 0 ) {
	    _signalEndofExp();
	    expProgressBar.setForeground( Color.orange );
	}
	else if( newIC.readoutDone > 0 && _newExp ) {
	    _signalEndofExp();
	}
	else if( newIC.readoutDone == 100 && _newReadout ) {
	    _signalEndofReadout();
	}
	else if( newIC.readoutDone == 0 && !_newExp ) {
	    _newExp = true;
	    _newReadout = true;
	    _nearEnd = false;
	    expProgressBar.setForeground( _colorProgBar );
	    rdoutProgressBar.setForeground( _colorProgBar );
	}
	else if( minsExp > 5.1 && minsRemain < 5 ) _signalNearEndofExp();

	expProgressBar.setValue( ( _maxProgBar * newIC.expDone )/100 );
	rdoutProgressBar.setValue( ( _maxProgBar * newIC.readoutDone )/100 );

	statExpDone.setValue( newIC.expDone + " %");
	statRdoutDone.setValue( newIC.readoutDone + " %");

	statExpDone.setValue2( minsRemain, "min");
	// readMode can indicate read freq. or read port (then freq. is constant):
	float readTime = 88;
	if( newIC.expTelapsed < 0 ) readTime = 44 * (4 - newIC.readMode);
	else if( newIC.readMode == 4 ) readTime = 22;
	statRdoutDone.setValue2( (100-newIC.readoutDone) * readTime/100, "sec");

	acqServerStatus.setNewState( newIC.name() );

	statExpTime.setValue( newIC.expSeconds );
	statAcqType.setValue( newIC.acqType );
	statReadMode.setValue( newIC.readMode );
	statShutter.setValue( newIC.shutter );
	statCooler.setValue( newIC.cooler );
	statGrabCnt.setValue( newIC.grabCnt );
	statProCnt.setValue( newIC.proCnt );
	statWriteCnt.setValue( newIC.writeCnt );

	if( newIC.sumPMTcnts > 0 ) {
	    if( newIC.expSeconds > 0 )
		statPMTavg.setActive( newIC.sumPMTcnts / newIC.expSeconds );
	    else statPMTavg.setActive(0);
	    statTempC.setActive( newIC.temperature );
	    if( _showNoise )
		statNoise.setActive( newIC.imgNoise );
	    else
		statPress.setActive( newIC.pressure );
	    if( newIC.imgMax > 0 ) {
		statMin.setActive( newIC.imgMin );
		statMax.setActive( newIC.imgMax );
		statAvg.setActive( newIC.imgAverage );
	    }
	}
	else {
	    statPMTavg.setActive(0);
	    statMin.setValue2( newIC.imgMin );
	    statMax.setValue2( newIC.imgMax );
	    statAvg.setValue2( newIC.imgAverage );
	    statTempC.setValue2( newIC.temperature );
	    if( _showNoise )
		statNoise.setValue2( newIC.imgNoise );
	    else
		statPress.setValue2( newIC.pressure );
	}

	if( _DataDisplay != null ) {
	    _DataDisplay.updateImage( newIC );
	    String dcs;
	    if( newIC.shutter < 0 )
		dcs = "Shutter= unknown   :   Cooler= unknown   :   T= ? C   :   P= ? Torr";
	    else {
		int shutter = newIC.shutter;
		if( shutter > 1 ) shutter = 1;
		if( _showNoise ) {
		    dcs = "Shutter=" + _closeOpen[shutter];
		    if( newIC.temperature > _alarmT ) dcs += " :   ALARM (T)";
		    dcs += ("   :   T= " + newIC.temperature + " C   :   Noise="
			    + UFLabel.truncFormat( newIC.imgNoise, 1, 2 ) + " ADU");
		}
		else {
		    int cooler = newIC.cooler;
		    if( cooler > 1 ) cooler = 1;
		    if( cooler < 0 ) cooler = 0;
		    dcs = "Shutter=" + _closeOpen[shutter] + "   :   Cooler=" + _offOn[cooler];
		    if( newIC.temperature > _alarmT ) dcs += " :   ALARM (T)";
		    dcs += ("   :   T= " + newIC.temperature + " C   :   P=" + newIC.pressure + " Torr");
		}
	    }
	    dcState.setNewState( dcs );
	    String status = newIC.name();
	    int apos = status.indexOf(">");
	    if( apos > 0 ) status = status.substring(apos);
	    observStatus.setNewState( status );
	}
	else if( _MasterPanel != null ) _MasterPanel.updateImage( newIC );
    }
//-------------------------------------------------------------------------------
//***** special public accessors for desirable objects: *****
//-------------------------------------------------------------------------------

    public JProgressBar expProgressBar() { return this.expProgressBar;  }
    public UFTextField acqServerStatus() { return this.acqServerStatus; }
    public UFTextField observStatus()    { return this.observStatus;    }
    public UFTextField dataFile()        { return this.dataFile;        }
    public UFTextField dcState()         { return this.dcState;         }
    public UFTextField detType()         { return this.detType;         }
    public UFStrings FITSheader()        { return this._FITSheader;     }
    public String FITSfile()             { return this._FITSfile;       }
}
// end of UFexpMonitor class.
