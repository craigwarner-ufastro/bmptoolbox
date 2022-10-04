package javaUFLib;

//Title:        UFobsMonitor for Java Control Interface (JCI) using UFLib Protocol
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-7
//Author:       Frank Varosi
//Company:      University of Florida, Dept. of Astronomy.
//Description:  for control and monitoring of CanariCam infrared camera system.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import javaUFProtocol.*;
//=======================================================================================
// Create Observation Status Monitor Panel by extending UFMonitorPanel.
// Custom panel for obs status monitoring of Detector Control Agent or Data Acq. Server.
// @author Frank Varosi
//---------------------------------------------------------------------------------------

public class UFobsMonitor extends UFMonitorPanel
{
    static final String rcsID = "$Name:  $ $Id: UFobsMonitor.java,v 1.128 2019/03/16 02:35:43 varosi Exp $";

    //used by recvNotifications() method:
    private DataInputStream _notifyStream;
    private int _nbeeps = 3;  //# of times to beep at end of obs.
    private int _obsCount = 0;
    private boolean _obsNow=false, _newObs=true, _obsAborted=false, _obsStopped=false;
    private boolean _gotStart=false, _gotFinal=false, _gotFinFH=false, _gotFH=false, _reqFC=false;
    protected boolean _verbose = false, _NIR=false;

    protected final int _maxProgBar = 500;
    private Color _colorProgBar;
    protected JProgressBar obsProgressBar = new JProgressBar(0, _maxProgBar);
    public JPopupMenu popupMenu;

    protected UFFrameConfig[] seqFrameConfs = new UFFrameConfig[200];
    protected UFFrameConfig[] saveFrameConfs = new UFFrameConfig[99000];

    protected UFStrings _FITSheader;
    protected String _FITSfile;
    protected UFFITSheader _FITSHeadObj;

    protected UFObsConfig _ObsConfig;
    protected UFTextField acqServerStatus  = new UFTextField("Data Server:",false); //editable = false.

    protected UFTextPanel frmTotal = new UFTextPanel(0.36,"Frames to save:");
    protected UFTextPanel frmDMAcnt = new UFTextPanel(0.36,"# DMAs =");
    protected UFTextPanel frmGrabCnt = new UFTextPanel(0.36,"# grabbed =");
    protected UFTextPanel frmProcCnt = new UFTextPanel(0.36,"# processed =");
    protected UFTextPanel frmWritCnt = new UFTextPanel(0.36,"# written =");
    protected UFTextPanel frmSendCnt = new UFTextPanel(0.36,"# sent =");

    protected UFTextPanel remainFrms = new UFTextPanel(0.4,"# Rem:");
    protected UFTextPanel nodBeam = new UFTextPanel(0.3," Beam:");
    protected UFTextPanel nodSet = new UFTextPanel(0.5,"  Nod:");
    protected UFTextPanel nodTotal = new UFTextPanel(0.5," of:");
    private String[] BeamNames = {"A","B"};

    protected UFTextMinMax wellADUs = new UFTextMinMax("Well ADUs:");
    protected UFTextMinMax clampADUs = new UFTextMinMax("Clamp ADUs:");
    protected UFTextMinMax outClpADUs = new UFTextMinMax("OutC ADUs:");
    protected UFTextMinMax wellSigma = new UFTextMinMax("Well Noise:");
    protected UFTextMinMax clampSigma = new UFTextMinMax("Clamp Noise:");
    protected UFTextMinMax outClpSigma = new UFTextMinMax("OutC Noise:");

    // Max. values for UFTextMinMax at which background is turned yellow for warning:
    protected int _wellADUmax = 59000;
    protected int _clampADUmax = 20000;
    protected int _outClpADUmax = 11000;
    protected double _wellSigMax = 55.0;
    protected double _clampSigMax = 11.0;
    protected double _outClpSigMax = 3.5;

    private HashMap statusDisplay = new HashMap(4);  //container of following UFTextField objects:
    protected UFTextField observStatus = new UFTextField("observStatus",false);
    protected UFTextField dcState      = new UFTextField("dcState",false);
    protected UFTextField detType      = new UFTextField("detType",false);
    protected UFTextField archiveFile  = new UFTextField("FITS_FileName",false);

    protected UFLibPanel _MasterPanel;   //used to get new params status.
    protected UFLibPanel _DetectorPanel; //used to get same host and port as in Detector Panel.
    protected UFLibPanel _BiasPanel;     //used to set status of bias power and levels.
    protected UFLibPanel _DataDisplay;   //option for quick-look data display updates.
//-------------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public UFobsMonitor() {
	super( "kepler", 52008 ); //to give default host and port to UFLibPanel.
	_init();
    } 
//-------------------------------------------------------------------------------
    /**
     * Constructor with host and port.
     */
    public UFobsMonitor( String hostName, int portNum )
    {
	super( hostName, portNum );
	_init();
    }
//-------------------------------------------------------------------------------
    /**
     * Special constructor to get pointer to data display/access client of JDD.
     */
    public UFobsMonitor( UFLibPanel dataDispClient )
    {
	super( dataDispClient );
	this._DataDisplay = dataDispClient;
	_init();
    }

    public UFobsMonitor( UFLibPanel dataDispClient, boolean forNIR )
    {
	super( dataDispClient );
	this._DataDisplay = dataDispClient;
	_NIR = forNIR;
	_init();
    }
//-------------------------------------------------------------------------------
    /**
     * Special constructor to get pointers to Detector and Bias panels in JCI.
     */
    public UFobsMonitor( UFLibPanel detectorPanel, UFLibPanel biasPanel )
    {
	super( detectorPanel );
	this._DetectorPanel = detectorPanel;
	this._BiasPanel = biasPanel;
	_init();
    }
//-------------------------------------------------------------------------------
    /**
     * Special constructor to get pointers to Master, Detector and Bias panels in JCI.
     */
    public UFobsMonitor( UFLibPanel masterPanel, UFLibPanel detectorPanel, UFLibPanel biasPanel )
    {
	super( detectorPanel );
	this._MasterPanel = masterPanel;
	this._DetectorPanel = detectorPanel;
	this._BiasPanel = biasPanel;
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
	    System.err.println("UFobsMonitor> Failed creating Obs.Monitor display panel!");
	}
    }
//-------------------------------------------------------------------------------
    /**
     *Component initialization called by super class UFMonitorPanel.
     */
    protected void _createMonitorPanel() throws Exception
    {
	obsProgressBar.setValue(0);
	obsProgressBar.setBackground( Color.white );
	_colorProgBar = obsProgressBar.getForeground();

	statusDisplay.put( observStatus.name(), observStatus );
	statusDisplay.put( dcState.name(), dcState );
	statusDisplay.put( detType.name(), detType );
	statusDisplay.put( archiveFile.name(), archiveFile );

	JPanel frmCntsPanel = new JPanel();
	frmCntsPanel.setLayout(new GridLayout(0,1));
	frmCntsPanel.setBorder(new EtchedBorder(0));

	if( _NIR ) {
	    frmTotal = new UFTextPanel(0.36,"Total Reads =");
	    frmDMAcnt = new UFTextPanel(0.36,"# readouts =");
	    frmCntsPanel.add(frmTotal);
	    frmCntsPanel.add(frmDMAcnt);
	    frmCntsPanel.add(frmGrabCnt);
	    frmCntsPanel.add(frmProcCnt);
	    frmCntsPanel.add(frmWritCnt);
	}
	else {
	    frmGrabCnt = new UFTextPanel(0.36,"# readouts =");
	    frmCntsPanel.add(frmGrabCnt);
	    frmCntsPanel.add(frmDMAcnt);
	    frmCntsPanel.add(frmTotal);
	    frmCntsPanel.add(frmProcCnt);
	    frmCntsPanel.add(frmWritCnt);
	    //frmCntsPanel.add(frmSendCnt);
	}

	this.setLayout(new RatioLayout());

	if( _NIR ) {
	    this.add("0.01,0.03;0.11,0.17", new JLabel("Exp. Status:") );
	    this.add("0.01,0.23;0.11,0.17", new JLabel("Progress:") );
	    this.add("0.01,0.43;0.11,0.17", new JLabel("DC - STATE:") );
	    this.add("0.01,0.63;0.11,0.17", new JLabel(acqServerStatus.Label()) );
	    this.add("0.01,0.83;0.11,0.17", new JLabel("FITS  File:") );
	    this.add("0.12,0.03;0.69,0.17", observStatus );
	    this.add("0.12,0.23;0.69,0.17", obsProgressBar );
	    this.add("0.12,0.43;0.69,0.17", dcState );
	    this.add("0.12,0.63;0.69,0.17", acqServerStatus );
	    this.add("0.12,0.83;0.69,0.17", archiveFile );
	    this.add("0.82,0.01;0.18,0.99", frmCntsPanel );
	}
	else {
	    this.add("0.01,0.03;0.09,0.17", new JLabel("obs. STATUS:") );
	    this.add("0.01,0.23;0.09,0.17", new JLabel("obs.Progress:") );
	    this.add("0.01,0.43;0.09,0.17", new JLabel("DC - STATE:") );
	    this.add("0.01,0.63;0.09,0.17", new JLabel(acqServerStatus.Label()) );
	    this.add("0.01,0.83;0.09,0.17", new JLabel("FITS  File:") );
	    this.add("0.09,0.03;0.43,0.17", observStatus );
	    this.add("0.09,0.23;0.43,0.17", obsProgressBar );
	    this.add("0.09,0.43;0.43,0.17", dcState );
	    this.add("0.09,0.63;0.43,0.17", acqServerStatus );
	    this.add("0.09,0.83;0.43,0.17", archiveFile );
	    this.add("0.53,0.01;0.16,0.99", frmCntsPanel );

	    JPanel nodbeamPanel = new JPanel();
	    nodbeamPanel.setLayout(new GridLayout(1,0));
	    nodbeamPanel.add( remainFrms );
	    nodbeamPanel.add( nodBeam );
	    nodbeamPanel.add( nodSet );
	    nodbeamPanel.add( nodTotal );
	    this.add("0.70,0.01;0.30,0.18", nodbeamPanel );

	    JPanel statisticsPanel = new JPanel();
	    statisticsPanel.setLayout(new GridLayout(0,1));
	    statisticsPanel.setBorder(new EtchedBorder(0));
	    JPanel titlePanel = new JPanel();
	    titlePanel.setLayout(new RatioLayout());
	    titlePanel.add("0.01,0.01;0.30,0.98", new JLabel("Statistics:") );
	    titlePanel.add("0.31,0.01;0.22,0.98", new JLabel("average") );
	    titlePanel.add("0.54,0.01;0.22,0.98", new JLabel("chan-min") );
	    titlePanel.add("0.77,0.01;0.22,0.98", new JLabel("chan-max") );
	    statisticsPanel.add( titlePanel );
	    statisticsPanel.add( wellADUs );
	    statisticsPanel.add( clampADUs );
	    statisticsPanel.add( wellSigma );
	    statisticsPanel.add( clampSigma );
	    this.add("0.70,0.19;0.30,0.81", statisticsPanel );
	}

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

	wellADUs.setAlarmMax( _wellADUmax );
	clampADUs.setAlarmMax( _clampADUmax );
	outClpADUs.setAlarmMax( _outClpADUmax );
	wellSigma.setAlarmMax( _wellSigMax );
	clampSigma.setAlarmMax( _clampSigMax );
	outClpSigma.setAlarmMax( _outClpSigMax );

	popupMenu = _createPopupMenu();
	obsProgressBar.setToolTipText("Click right button for Menu.");

	obsProgressBar.addMouseListener( new MouseListener() {
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
		if( _nbeeps > 3 ) _nbeeps = 3;
		System.out.println( className+": at end of obs. # of beeps = " + _nbeeps );
	    }
	    catch (Exception ex) { ex.printStackTrace(); }
	}
    }

    public void actionSetSound(int soundNum) {}

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

	String message = className + ".configMonitor> " + ufp.name();

	if( _DataDisplay != null ) { //special case: request notification stream of FrameConfigs:
	    message = "CameraType = " + ufp.name();
	    dcState.setNewState( message );
	    //first request current obs & frame configs and status monitor thread will recv:
	    uft.rename("OC");
	    if( uft.sendTo(_monitorSocket) <= 0 ) {
		monSocketError("configMonitor","ObsConfig request");
		return false;
	    }
	    uft.rename("FH");
	    if( uft.sendTo(_monitorSocket) <= 0 ) {
		monSocketError("configMonitor","FITS header request");
		return false;
	    }
	    uft.rename("FC");
	    if( uft.sendTo(_monitorSocket) <= 0 ) {
		monSocketError("configMonitor","FrameConfig request");
		return false;
	    }
	    //Request notification stream of FrameConfig updates
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

	if( _verbose ) {
	    String pmsg = className + ".procStatusInfo> recvd. object:  "
		+ ufp.description() + ": Len=" + ufp.length() + ": name= " + ufp.name() + ".";
	    System.out.println( pmsg );
	}

	if( ufp instanceof UFFrameConfig )
	    {
		if( _DataDisplay == null )
		    procFrameConfig( (UFFrameConfig)ufp );
		else
		    recvNotifications( (UFFrameConfig)ufp );
	    }
	else if( ufp instanceof UFObsConfig )
	    {
		_ObsConfig = (UFObsConfig)ufp;
		nodTotal.setValue( _ObsConfig.nodSets() );

		if( _DataDisplay != null ) {
		    System.out.println(" ObsConfig(" + _obsCount + "): " + _ObsConfig.name());
		    String obsinfo;
		    String msg;
		    if( _NIR ) {
			obsinfo = "Exp.Mode = " + _ObsConfig.readoutMode()
			    + " : # reads = " + _ObsConfig.reads()
			    + " : # groups = " + _ObsConfig.groups()
			    + " : # ramps = " + _ObsConfig.ramps();
			msg = "STARTED new exp:  " + obsinfo;
		    }
		    else {
			obsinfo = "ObsMode = " + _ObsConfig.obsMode()
			    + "   :   ReadoutMode = " + _ObsConfig.readoutMode()
			    + "   :    NodPattern = " + _ObsConfig.nodPattern();
			msg = "STARTED new obs:  " + obsinfo;
		    }
		    observStatus.setNewState( msg );
		    dcState.setNewState( obsinfo );
		    _DataDisplay.updateObsConfig( _ObsConfig );
		}
	    }
	else if( ufp instanceof UFStrings )
	    {
		UFStrings ufs = (UFStrings)ufp;
		String sname = ufs.name();
		UFTextField statusToUpdate = (UFTextField)statusDisplay.get( sname );

		if( statusToUpdate == null )
		    {

			if( sname.indexOf("getStatus") >= 0 ) //check if new DC params available:
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
			else if( _BiasPanel != null && sname.indexOf("dcBias") >= 0 )
			    {
				_BiasPanel.setNewStatus( ufs ); //status is for Bias Panel display
				return;
			    }
			else if( sname.indexOf("HEADER") >= 0 )
			    {
				_FITSheader = ufs;
				_FITSHeadObj = new UFFITSheader();

				for( int i=0; i < _FITSheader.numVals()-1; i++ )
				    _FITSHeadObj.addrec( _FITSheader.stringAt(i) );

				String _FITSfile = _FITSHeadObj.getParam("FILENAME");
				String fileDir = _FITSHeadObj.getParam("FILEDIR");
				archiveFile.setNewState( fileDir + _FITSfile );

				if( _DataDisplay != null ) {
				    _FITSheader.rename("FITS file: " + _FITSfile);
				    _DataDisplay.updateObsInfo( _FITSheader );
				}
				return;
			    }
			else if( ufs.elements() == 4 )     //this is reply to IOSTAT request:
			    {
				ufs.rename("IOSTAT> " + observStatus.getText() );
				String msg = observStatus.getText() + "  (" + ufs.valData(3) + ")";
				System.out.println( msg );
				observStatus.setNewState( msg );
				if( _DataDisplay != null ) _DataDisplay.updateObsInfo( ufs );
			    }
			else System.out.println( className+".procStatusInfo> unknown UFStrings:\n"
						 +" name = " + sname + ", value = " + ufs.valData(0) );
		    }
		else statusToUpdate.setNewState( ufs.valData(0) );
	    }
	else {
	    String errmsg = className + ".procStatusInfo> unexpected object:	"
		+ ufp.description() + ": Length=" + ufp.length() + "\n name = " + ufp.name() + ".";
	    System.err.println( errmsg );
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Check notify stream from Data/Frame Acq.Server for more FrameConfigs.
     * Input arg [ UFFrameConfig frameConf ] is the first notification, check for more,
     *  find unique set of updated buffer names and process them.
     * If it is begining or end of an obs., then just process the notification and return.
     * For case of NIR camera just process counters right away, since frame readout is not so fast.
     */
    protected void recvNotifications( UFFrameConfig frameConf )
    {
	if( checkObsStartOrEnd( frameConf ) ) {
	    procFrameConfig( frameConf );
	    return;
	}

	int notifyCnt = 0;
	seqFrameConfs[notifyCnt++] = frameConf;

	// Check for more FrameConfigs that may be queued in notification stream:

	try {
	    while( _notifyStream.available() > UFProtocol._MinLength_ && notifyCnt < seqFrameConfs.length )
		{
		    UFProtocol ufp = UFProtocol.createFrom( _monitorSocket );

		    if( ufp == null ) {
			System.err.println( className+"::recvNotifications> null object!" );
			break;
		    }

		    if( ufp instanceof UFFrameConfig )
			seqFrameConfs[notifyCnt++] = (UFFrameConfig)ufp;
		    else
			procStatusInfo( ufp );
		}
	}
	catch( IOException ioe ) {
	    System.err.println( className+"::recvNotifications>"+ioe.toString() );
	}
	catch( Exception x ) {
	    System.err.println( className+"::recvNotifications>" + x.toString() );
	}

	if( notifyCnt == 1 ) {
	    procFrameConfig( frameConf );
	    seqFrameConfs[0] = null;
	    return;
	}
	else if( notifyCnt >= seqFrameConfs.length )
	    System.err.println( className + "::recvNotifications> notifyCnt=" + notifyCnt );

	// check for duplicate name() of each and process only the final one
	// (since name() tells which frames are updated):
	for( int i=0; i < notifyCnt-1; i++ ) {
	    UFFrameConfig fc = seqFrameConfs[i];
	    for( int j=i+1; j < notifyCnt; j++ ) {
		if( seqFrameConfs[j].name().equals( fc.name() ) ) {
		    seqFrameConfs[i] = null;
		    break;
		}
	    }
	}

	// Process the sequence of FrameConfig notifications.

	for( int i=0; i < notifyCnt; i++ ) {
	    UFFrameConfig fc = seqFrameConfs[i];
	    if( fc != null ) {
		procFrameConfig( fc );
		checkObsStartOrEnd( fc );
		seqFrameConfs[i] = null;
	    }
	}
    }
//-------------------------------------------------------------------------------
    /**
     * Method used by recvNotifications() to check the FrameConfig for Start/Abort/End of an obs.
     * Return boolean value just indicates whether FrameConfig status should be immediately indicated.
     * Return false indicates to continue receiving and processing FrameConfig stream normally.
     * Note that further requests to FAS/DAS for objects may be sent to _monitorSocket,
     *  and then response is automatically processed by recvStatus().
     */
    protected synchronized boolean checkObsStartOrEnd( UFFrameConfig fc )
    {
	if( fc.frameProcCnt >= 0 &&
	    fc.frameProcCnt < saveFrameConfs.length ) saveFrameConfs[fc.frameProcCnt] = fc;

	String imethod = className + "::checkObsStartOrEnd> ";

	if( fc.frameObsTotal <= 0 ) {

	    if( !_reqFC ) { //request FrameConfig object to get type of end: abort or stop.
		try{ Thread.sleep(1000); } catch( Exception ex ) {}
		UFTimeStamp uft = new UFTimeStamp("FC");
		if( uft.sendTo(_monitorSocket) > 0 ) _reqFC = true;
		else monSocketError("checkObsStartOrEnd","FC request");
		return true;
	    }

	    // Wait for FrameConfig object that gives observation status: aborted or stopped.
	    String obsEnd = "ENDED";
	    String statusObs = fc.name().toUpperCase();

	    if( statusObs.indexOf("OBSERVATION") >= 0 || statusObs.indexOf("EXPOSURE") >= 0 ) {

		if( statusObs.indexOf("STOP") >= 0 ) {
		    _obsStopped = true;
		    obsEnd = "STOPPED";
		}
		else if( statusObs.indexOf("ABORT") >= 0 ) {
		    _obsAborted = true;
		    obsEnd = "ABORTED";
		}

		if( !_gotFinal ) {
		    _gotFinal = true;
		    _gotStart = false;
		    _gotFH = false;
		    observStatus.setNewState( obsEnd );
		    //wait a sec. for counters to update:
		    try{ Thread.sleep(2000); } catch( Exception ex ) {}
		    System.out.println( obsEnd + ": requesting IOSTAT...");
		    UFTimeStamp uft = new UFTimeStamp("IOSTAT");
		    if( uft.sendTo(_monitorSocket) <= 0 )
			monSocketError("checkObsStartOrEnd","IOSTAT request");
		}
	    }

	    if( _obsAborted || (_obsStopped && fc.frameProcCnt == fc.frameGrabCnt) ) {
		if( !_gotFinFH ) {
		    System.out.println("Processing of Frames Completed: requesting FITS header...");
		    //wait a sec. to make sure final FITS header is defined:
		    try{ Thread.sleep(1000); } catch( Exception ex ) {}
		    UFTimeStamp uft = new UFTimeStamp("FH");
		    if( uft.sendTo(_monitorSocket) > 0 ) _gotFinFH = true;
		    else monSocketError("checkObsStartOrEnd","FITS header request");
		}
		return true;
	    }
	    else return false;
	}
	else if( fc.frameGrabCnt >= fc.frameObsTotal ) {

	    if( !_gotFinal ) {
		_gotFinal = true;
		_gotStart = false;
		_gotFH = false;
		observStatus.setNewState("COMPLETED.");
		System.out.println("Obs. Completed: requesting IOSTAT after 2 secs...");
		//wait a sec. for counters to update:
		try{ Thread.sleep(2000); } catch( Exception ex ) {}
		UFTimeStamp uft = new UFTimeStamp("IOSTAT");
		if( uft.sendTo(_monitorSocket) <= 0 )
		    monSocketError("checkObsStartOrEnd","IOSTAT request");
	    }

	    if( fc.frameProcCnt == fc.frameGrabCnt && !_gotFinFH ) {
		System.out.println("Processing of Frames Completed: requesting FITS header...");
		//wait a sec. to make sure final FITS header is defined:
		try{ Thread.sleep(1000); } catch( Exception ex ) {}
		UFTimeStamp uft = new UFTimeStamp("FH");
		if( uft.sendTo(_monitorSocket) > 0 ) _gotFinFH = true;
		else monSocketError("checkObsStartOrEnd","FITS header request");
		//if writing of frames is not finished the wait a sec and request final frameConfig:
		if( fc.frameWriteCnt < fc.frameProcCnt && fc.frameWriteCnt > 0 ) {
		    try{ Thread.sleep(1500); } catch( Exception ex ) {}
		    uft.rename("FC");
		    if( uft.sendTo(_monitorSocket) > 0 ) _reqFC = true;
		    else monSocketError("checkObsStartOrEnd","FC request");
		}
	    }

	    return true;
	}
	else if( (!_NIR && (fc.frameProcCnt==1 || fc.frameGrabCnt==1)) || (_NIR && fc.frameGrabCnt > 0) ) {

	    if( !_gotStart ) {

		if( fc.frameObsTotal > 0 ) _gotStart = true;
		++_obsCount;
		String msg = "STARTED new observation...";
		observStatus.setNewState(msg);
		System.out.println( imethod + msg + "requesting ObsConfig and FITS header...");
		UFTimeStamp uft = new UFTimeStamp("OC");
		if( uft.sendTo(_monitorSocket) <= 0 ) monSocketError("checkObsStartOrEnd","ObsConfig request");

		if( !_gotFH ) {
		    uft.rename("FH");
		    if( uft.sendTo(_monitorSocket) > 0 ) _gotFH = true;
		    else monSocketError("checkObsStartOrEnd","FITS header request");
		}
		//mark memory for de-allocation:
		for( int i=2; i < saveFrameConfs.length; i++ ) saveFrameConfs[i] = null;
	    }

	    _gotFinal = false;
	    _gotFinFH = false;
	    _reqFC = false;
	    _obsStopped = false;
	    _obsAborted = false;
	    return true;
	}
	else if( fc.frameGrabCnt > 1 && !_NIR ) {

	    if( _ObsConfig != null ) { //check for nodding...
		if( _ObsConfig.nodBeams() > 1 ) {

		    int nfsave = _ObsConfig.chopBeams() * _ObsConfig.saveSets();

		    if( (fc.frameGrabCnt % nfsave) == 0 ) {
			_obsNow = false;
			observStatus.setNewState("Nod Beam-switch...");
			return false;
		    }
		}
	    }

	    if( !_obsNow ) {
		_obsNow = true;
		observStatus.setNewState("Observing...");
	    }
	}

	return false;
    }
//-------------------------------------------------------------------------------
    private synchronized void _signalEndofObs()
    {
	if( _newObs ) for( int i=0; i < _nbeeps; i++ ) Toolkit.getDefaultToolkit().beep();
	_newObs = false;
    }
//-------------------------------------------------------------------------------
    /**
     * Method used by recvNotifications() and procStatusInfo() to process a UFFrameConfig object.
     */
    protected synchronized void procFrameConfig( UFFrameConfig newFC )
    {
	if( _verbose ) {
	    String pmsg = className + ".procFrameConfig> " + newFC.timeStamp().substring(0,21)
		+ "> fgc=" + newFC.frameGrabCnt	+ ", fpc=" + newFC.frameProcCnt
		+ ", fwc=" + newFC.frameWriteCnt + ", ftot=" + newFC.frameObsTotal;
	    System.out.println( pmsg );
	}

	int totalFrames = newFC.frameObsTotal;
	int frameCount = newFC.frameProcCnt;
	if( _NIR ) frameCount = newFC.frameGrabCnt;

	if( totalFrames < 0 ) {
	    totalFrames = -totalFrames;
	    obsProgressBar.setForeground( Color.orange );
	    _signalEndofObs();
	}
	else if( frameCount == totalFrames ) {
	    obsProgressBar.setForeground( Color.green );
	    _signalEndofObs();
	}
	else {
	    _newObs = true;
	    obsProgressBar.setForeground( _colorProgBar );
	}

	//new setup for mid-IR obs (upgrade to use ARC det.controller 2019):
	//   grabs are readouts whereas processed is count of coadded readouts then saved frames:

	if( totalFrames > 0 ) {
	    if(_NIR)
		obsProgressBar.setValue( (_maxProgBar * Math.abs( newFC.frameGrabCnt ))/totalFrames );
	    else
		obsProgressBar.setValue( (_maxProgBar * Math.abs( newFC.frameProcCnt ))/totalFrames );
	}

	frmTotal.setValue( newFC.frameObsTotal );
	frmDMAcnt.setValue( newFC.DMAcnt );
	frmGrabCnt.setValue( newFC.frameGrabCnt );
	frmProcCnt.setValue( newFC.frameProcCnt );
	frmWritCnt.setValue( newFC.frameWriteCnt );
	frmSendCnt.setValue( newFC.frameSendCnt );

	if( !_NIR ) {
	    if( _ObsConfig != null ) {
		int nfsave = _ObsConfig.chopBeams() *_ObsConfig.chopCycles() *_ObsConfig.savesPerBeam();
		int nfremain = nfsave - (newFC.frameProcCnt % nfsave);
		if( nfremain == nfsave ) nfremain = 0;
		remainFrms.setValue( nfremain );
	    }

	    if( newFC.NodBeam >= 0 && newFC.NodBeam < BeamNames.length )
		nodBeam.setValue( BeamNames[newFC.NodBeam] );
	    else nodBeam.setValue("?");

	    nodSet.setValue( newFC.NodSet );

	    if( newFC.frameProcCnt <= 1 || newFC.frameGrabCnt <= 1 ) {
		wellADUs.reset();
		clampADUs.reset();
		outClpADUs.reset();
		wellSigma.reset();
		clampSigma.reset();
		outClpSigma.reset();
	    }
	    else if( newFC.sigmaWellNoise > 0 || newFC.sigmaWellMin > 0 || newFC.sigmaWellMax > 0 ) {
		wellSigma.setAvgMinMax( newFC.sigmaWellNoise, newFC.sigmaWellMin, newFC.sigmaWellMax );
		clampSigma.setAvgMinMax( newFC.sigmaClampNoise, newFC.sigmaClampMin, newFC.sigmaClampMax );
		outClpSigma.setAvgMinMax( (2*newFC.sigmaOclmpMin + newFC.sigmaOclmpMax)/3,
					  newFC.sigmaOclmpMin, newFC.sigmaOclmpMax );
	    }

	    if( newFC.wellADUavg >= 0 || newFC.wellADUmin >= 0 || newFC.wellADUmax >= 0 ) {
		wellADUs.setAvgMinMax( newFC.wellADUavg, newFC.wellADUmin, newFC.wellADUmax );
		clampADUs.setAvgMinMax( newFC.clampADUavg, newFC.clampADUmin, newFC.clampADUmax );
		outClpADUs.setAvgMinMax( (newFC.oclmpADUmin + newFC.oclmpADUmax)/2,
					 newFC.oclmpADUmin, newFC.oclmpADUmax );
	    }
	}

	if( _DataDisplay != null ) {
	    acqServerStatus.setNewState( newFC.timeStamp().substring(0,21) + " # " +
					 newFC.frameProcCnt + " : " + newFC.name() );
	    _DataDisplay.updateFrames( newFC );
	}
	else {
	    acqServerStatus.setNewState( newFC.name() );
	    if( _MasterPanel != null ) _MasterPanel.updateFrames( newFC );
	}
    }
//-------------------------------------------------------------------------------
//***** special public accessors for desirable objects: *****
//-------------------------------------------------------------------------------

    public JProgressBar obsProgressBar() { return this.obsProgressBar;  }
    public UFTextField acqServerStatus() { return this.acqServerStatus; }
    public UFTextField observStatus()    { return this.observStatus;    }
    public UFTextField archiveFile()     { return this.archiveFile;     }
    public UFTextField dcState()         { return this.dcState;         }
    public UFTextField detType()         { return this.detType;         }
    public UFStrings FITSheader()        { return this._FITSheader;     }
    public String FITSfile()             { return this._FITSfile;       }
}
// end of UFobsMonitor class.
