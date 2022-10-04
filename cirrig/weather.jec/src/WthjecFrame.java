package wthjec;

/**
 * Title:        WthjecFrame.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  main JFrame for MJEC
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.geom.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFLib.*;
import javaUFProtocol.*;
import javaMMTLib.*;

public class WthjecFrame extends javax.swing.JFrame {
  Container content;
  String _mainClass = getClass().getName();
  String hostname, isHostname, logname;
  LinkedHashMap <String, UFGUIRecord> database;
  Socket commandSocket;
  StatusThread statusThread;
  AgentConfig agentConfig;
  Vector <JButton> buttons;
  ReconnectThread _reconnect;
  PingThread _ping;
  boolean _verbose = false, _ready = true, _connecting = false;
  UFMessageLog _history;
  UFTail _logTail;
  protected int _counter = 0;
  protected boolean isLoggedIn = false;
  protected long sid = -1;
  protected int uid = -1;
  protected String installDir = "..";

  /* Menu Items */
  JMenuBar menuBar;
  JMenu menuFile, menuOptions, menuHelp;
  JMenuItem menuFileSaveAuto, menuFileClearAuto, menuFileReconnect, menuFileExit;
  JMenuItem menuOptionsErrors, menuOptionsHistory, menuOptionsLog;
  JMenuItem menuHelpAbout;

  /* Panels */
  JPanel statusPanel;
  JTabbedPane wthjecTabs;
  JPanelLogin loginPanel;
  JPanelWeather[] weatherPanels;
  BorderLayout borderLayout;

  /* Status panel */
  JLabel messageLabel, hostLabel;
  JCheckBox panelLock;
  UFMMTLabel heartLabel, healthLabel, statusLabel;
  
  protected int _timeout = 60000, _greetTimeout = 6000;

  public WthjecFrame(String hostname, String isHostname, String[] args) {
    super("WthJEC Beta 1");
    setSize(688,600);
    this.hostname = hostname;
    this.isHostname = isHostname;
    setOptions(args);

    database = new LinkedHashMap(100);
    buttons = new Vector(20);

    setDefaultCloseOperation(3);
    content = getContentPane();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    _history = new UFMessageLog("Wthjec History",1000);
    setupGUI();
    setupAgentConfig();
    _connecting = true;
    hibernate(1000);
    connectToServers();
    hibernate(3000);
    _connecting = false;
    _startReconnectThread();
    _startPingThread();
    checkForAutoLogin();
  }

  public void setOptions(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v")) {
	_verbose = true;
      } else if (args[i].equals("-log")) {
	if (args.length > i+1) logname = args[i+1];
      }
    }
  }

  public void registerButton(JButton theButton) {
    buttons.add(theButton);
  }

  public void apply(final Vector <String> commandVec) {
    apply(commandVec, null);
  }

  public void apply(final Vector <String> commandVec, final JPanelWeather wpanel) {
    if (wthjec.mode == wthjec.MODE_STATUS) {
      System.out.println(_mainClass+"::apply> in STATUS MODE!");
      return;
    }

    setButtonsEnabled(false);

    /* Run in a new thread */
    Thread t = new Thread() {
      public void run() {
        while (!_ready) {
          try {
            Thread.sleep(100);
          } catch(InterruptedException e) { }
        }
        _ready = false;
	UFStrings req;
        if (commandVec.size() == 0) return; 

	/* Print out commands to history */
        for (Iterator icmds = commandVec.iterator(); icmds.hasNext(); ) {
	  String command = (String)icmds.next();
	  if (!command.equals("")) { 
	    _history.addMessage("weatherAgent:> "+command);
	    _history.showAllMessages();
	    if (_verbose) System.out.println(_mainClass+"::apply> weatherAgent:> "+command);
	  }
        }

	if (!agentConfig.doConnect()) return;
	System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to weatherAgent."); 
	if (!agentConfig.isConnected()) {
	  System.out.println(_mainClass+"::apply> Error: Not connected to weatherAgent.");
	  mjecError.show("Error: Not connected to weatherAgent.");
	  agentConfig.disconnect();
	  return;
	}
        try {
          commandSocket.setSoTimeout(_timeout);
          req = new UFStrings(_mainClass+": actionRequest", commandVec);
          int nbytes = req.sendTo(commandSocket);
          if( nbytes <= 0 ) {
            System.out.println( _mainClass + "::apply> zero bytes sent.");
            commandSocket.setSoTimeout(0);
            return;
          }
          if (_verbose) System.out.println(_mainClass+"::apply> Sent "+nbytes+" bytes at "+ctime());
	  _counter++;
          waitForReply(commandSocket, "weatherAgent");
          UFStrings reply;
          synchronized(commandSocket) {
            reply = (UFStrings)UFProtocol.createFrom(commandSocket);
          }
          commandSocket.setSoTimeout(0);
          if (reply == null) {
            System.out.println(_mainClass+"::apply> Error: received null response from weatherAgent at "+ctime());
            mjecError.show("Error: received null response from weatherAgent.");
	    _counter--;
            return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from weatherAgent at "+ctime());
	  if (reply.name().toLowerCase().indexOf("error") != -1) {
	    if (reply.stringAt(0).equals("Not logged in.")) {
	      //try to restore session 
	      String restoreString = loginPanel.getRestoreString()+" "+sid;
	      if (restoreString != null) {
		commandVec.add(0, restoreString);
		req = new UFStrings(_mainClass+": actionRequest", commandVec);
		System.out.println(_mainClass+"::apply> Attempting restore session");
	      }
              commandSocket.setSoTimeout(_timeout);
              nbytes = req.sendTo(commandSocket);
              if( nbytes <= 0 ) {
                System.out.println( _mainClass + "::apply2> zero bytes sent.");
                commandSocket.setSoTimeout(0);
                return;
              }
              if (_verbose) System.out.println(_mainClass+"::apply2> Sent "+nbytes+" bytes at "+ctime());
              synchronized(commandSocket) {
                reply = (UFStrings)UFProtocol.createFrom(commandSocket);
              }
              commandSocket.setSoTimeout(0);
              if (reply == null) {
                System.out.println(_mainClass+"::apply2> Error: received null response from weatherAgent at "+ctime());
                mjecError.show("Error: received null response from weatherAgent.");
                _counter--;
                return;
              }
	    }
	  }
          boolean success = processResponse(reply);
	  if (success && wpanel != null) {
	    wpanel.updateLastDate();
	  }
        } catch (IOException ioe) {
          System.err.println(_mainClass+"::apply> "+ioe.toString());
	  ioe.printStackTrace();
          mjecError.show(ioe.toString());
          if (ioe.toString().indexOf("Socket") != -1) agentConfig.disconnect();
        }
	_counter--;
        /* Wait for any abort/stop commands */
        while (_counter > 0) {
          try {
            Thread.sleep(25);
          } catch(InterruptedException e) { }
        }
        setButtonsEnabled(true);
        _ready = true;
      }
    };
    t.start();
  }

  public void setButtonsEnabled(boolean enabled) {
    JButton theButton;
    for (int i = 0; i < buttons.size(); i++) {
      theButton = (JButton)buttons.elementAt(i);
      /* Leave only aborts and stop enabled during apply */
      if (theButton.getText().equals("Abort")) continue;
      theButton.setEnabled(enabled);
    }
  }

  public boolean isLocked() {
    if (!panelLock.isSelected()) return false;
    return true;
  }

  public boolean processResponse(UFStrings reply) {
    boolean success = true;
    System.out.println(_mainClass+"::processResponse> "+reply.toString());
    if (reply.name().toLowerCase().indexOf("error") != -1) {
      /* We received an error! */
      success = false;
      for (int j = 0; j < reply.numVals(); j++) {
	mjecError.show(reply.stringAt(j));
      }
    }
    for (int j = 0; j < reply.numVals(); j++) {
      _history.addMessage("Received response:> "+reply.stringAt(j));
      /* Check for errors */
      if (reply.stringAt(j).toLowerCase().indexOf("error") != -1) {
	success = false;
      }
    }
    _history.showAllMessages();
    messageLabel.setText(reply.stringAt(0));
    String firstReply = reply.stringAt(0);
    if (isLogin(firstReply)) {
      isLoggedIn = true;
      try {
	uid = Integer.parseInt(getReplyToken(firstReply, 2));
	sid = Long.parseLong(getReplyToken(firstReply, 3));
      } catch(NumberFormatException nfe) {
	System.out.println(_mainClass+"::processResponse> Error receiving login info: "+firstReply);      
	mjecError.show("Error in login info: "+firstReply);
	isLoggedIn = false;
	uid = -1;
	sid = -1;
	return false;
      }
    }
    if (isLogout(firstReply)) {
      isLoggedIn = false;
      uid = -1;
      sid = -1;
    }
    if (isWeatherList(firstReply)) {
      int tabCount = wthjecTabs.getTabCount();
      while(tabCount > 1) {
	wthjecTabs.remove(tabCount-1);
	tabCount = wthjecTabs.getTabCount();
      }
      weatherPanels = new JPanelWeather[reply.numVals()-1];
      for (int j = 1; j < reply.numVals(); j++) {
	String weatherString = reply.stringAt(j);
	weatherPanels[j-1] = new JPanelWeather(this, weatherString);
	wthjecTabs.add(weatherPanels[j-1], weatherPanels[j-1].weatherStation);
      }
    }
    return success;
  }

  public boolean isLogin(String loginString) {
    if (!getReplyToken(loginString, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(loginString, 1).toLowerCase().equals("loggedin")) return true;
    return false;
  }

  public boolean isWeatherList(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 1).toLowerCase().equals("loggedin") || getReplyToken(response, 1).toLowerCase().equals("refreshed")) return true;
    return false;
  }

  public boolean isLogout(String loginString) {
    if (!getReplyToken(loginString, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(loginString, 1).toLowerCase().equals("loggedout")) return true;
    return false;
  }

  public String getReplyToken(String reply, int token) {
    return getReplyToken(reply, token, " ");
  }

  public String getReplyToken(String reply, int token, String delim) {
    String[] vals = reply.split(delim);
    if (vals.length > token) return vals[token];
    System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
    return "null";
  }

  public void setupGUI() {
    menuBar = new JMenuBar();
    menuFile = new JMenu("File");

    menuFileSaveAuto = new JMenuItem("Save Auto Login");
    menuFileSaveAuto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        Object[] saveOptions = {"Save","Cancel"};
        int n = JOptionPane.showOptionDialog(WthjecFrame.this, "Save current values and auto-login?", "Save Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, saveOptions, saveOptions[0]);
        if (n == 1) return;
        try {
          PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/wthjecStartupValues.xml"));
          pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          pw.println("<autoLogin>");
	  loginPanel.saveFields(pw);
	  for (int j = 0; j < weatherPanels.length; j++) {
	    weatherPanels[j].saveFields(pw);
	  } 
          pw.println("</autoLogin>");
          pw.close();
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println(_mainClass+"::setupGUI> ERROR: Could not create auto-login file!");
          return;
        }
	System.out.println(_mainClass+"::setupGUI> Successfully created auto-login file!");
      }
    });
    menuFile.add(menuFileSaveAuto);

    menuFileClearAuto = new JMenuItem("Clear Auto Login");
    menuFileClearAuto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        Object[] clearOptions = {"Clear","Cancel"};
        int n = JOptionPane.showOptionDialog(WthjecFrame.this, "Clear auto-login option?", "Clear Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, clearOptions, clearOptions[0]);
        if (n == 1) return;
        try {
	  File startupFile = new File(installDir+"/etc/wthjecStartupValues.xml");
	  boolean success = startupFile.delete();
	  if (success) {
	    System.out.println(_mainClass+"::setupGUI> Successfully cleared auto-login!"); 
	  } else { 
            System.out.println(_mainClass+"::setupGUI> ERROR: Could not clear auto-login!"); 
	  }
	} catch (Exception e) {
          e.printStackTrace();
          return;
        }
      }
    });
    menuFile.add(menuFileClearAuto);
    menuFile.add(new JSeparator());

    menuFileReconnect = new JMenuItem("Reconnect");
    menuFileReconnect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	agentConfig.disconnect();
	hibernate(100);
      }
    });
    menuFile.add(menuFileReconnect);
    menuFile.add(new JSeparator());

    menuFileExit = new JMenuItem("Exit");
    menuFileExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	Object[] exitOptions = {"Exit","Cancel"};
        int n = JOptionPane.showOptionDialog(WthjecFrame.this, "Are you sure you want to quit?", "Exit MJEC?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, exitOptions, exitOptions[1]);
        if (n == 0) {
	  System.exit(0);
        }
      }
    });
    menuFile.add(menuFileExit);
    menuBar.add(menuFile);

    menuOptions = new JMenu("Options");
    menuOptionsErrors = new JMenuItem("Errors");
    menuOptionsErrors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	mjecError.show();
      }
    });
    menuOptionsHistory = new JMenuItem("History");
    menuOptionsHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	_history.viewFrame(700,700);
	_history.showAllMessages();
      }
    });
    menuOptionsLog = new JMenuItem("Log");
    menuOptionsLog.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	if (_logTail == null) {
	  _logTail = new UFTail(logname);
	}
        _logTail.viewFrame(logname);
      }
    });
    menuOptions.add(menuOptionsErrors);
    menuOptions.add(menuOptionsHistory);
    menuOptions.add(menuOptionsLog);
    menuBar.add(menuOptions);

    menuHelp = new JMenu("Help");
    menuHelpAbout = new JMenuItem("About");
    menuHelp.add(menuHelpAbout);
    menuBar.add(menuHelp);
    setJMenuBar(menuBar);

    statusPanel = new JPanel();
    wthjecTabs = new JTabbedPane();

    /* Setup status panel */
    statusPanel.setLayout(new RatioLayout());
    heartLabel = new UFMMTLabel("Weather.WeatherAgent:heartbeat");
    heartLabel.registerComponent(database, false);
    healthLabel = new UFMMTLabel("Weather.WeatherAgent:health");
    healthLabel.registerComponent(database, false);
    healthLabel.addDisplayValue("GOOD","GOOD", Color.GREEN);
    healthLabel.addDisplayValue("WARNING","WARNING", Color.ORANGE);
    healthLabel.addDisplayValue("BAD","BAD", Color.RED);
    statusLabel = new UFMMTLabel("Weather.WeatherAgent:status");
    statusLabel.registerComponent(database, false);
    statusLabel.addDisplayValue("IDLE","IDLE", Color.GREEN);
    statusLabel.addDisplayValue("BUSY","BUSY", Color.BLUE);
    statusLabel.addDisplayValue("ERROR","ERROR", Color.RED);
    statusLabel.addDisplayValue("INIT","INIT", Color.ORANGE);
    statusLabel.addDisplayValue("CONNECT","CONNECT", Color.MAGENTA);
    hostLabel = new JLabel("Host: "+hostname);    
    messageLabel = new JLabel();
    panelLock = new JCheckBox("Lock");
    panelLock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	//setAllInputsEnabled(!panelLock.isSelected());
      }
    });
    
    statusPanel.setPreferredSize(new Dimension(648, 56));
    statusPanel.add("0.01,0.02;0.98,0.02",new JSeparator());
    statusPanel.add("0.01,0.10;0.14,0.43", new JLabel("Heartbeat:"));
    statusPanel.add("0.15,0.10;0.18,0.43", heartLabel);
    statusPanel.add("0.36,0.10;0.10,0.43", new JLabel("Health:")); 
    statusPanel.add("0.47,0.10;0.16,0.43", healthLabel);
    statusPanel.add("0.67,0.10;0.12,0.43", new JLabel("Status:"));
    statusPanel.add("0.79,0.10;0.20,0.43", statusLabel);
    statusPanel.add("0.01,0.55;0.25,0.43", hostLabel);
    statusPanel.add("0.28,0.55;0.60,0.43", messageLabel);
    statusPanel.add("0.89,0.55;0.10,0.43", panelLock);

    loginPanel = new JPanelLogin(this);

    /* Add to JTabbedPane */
    wthjecTabs.add(loginPanel, "Login");

    if (wthjec.mode == wthjec.MODE_STATUS) {
      panelLock.doClick();
      panelLock.setEnabled(false);
    }

    /* Add components to this frame */
    content.add(wthjecTabs, 0);
    content.add(statusPanel, 1);
    pack();
    setVisible(true);
  }

  public void setupAgentConfig() {
    agentConfig = new AgentConfig("WeatherAgent", hostname, wthjec.wthPort);
  }

  public void connectToServers() {
    hibernate(1000);
    if (_verbose) agentConfig.setVerbosity(_verbose);
    if (!agentConfig.doConnect()) return; 
    System.out.println(_mainClass + "::connectToServers> starting Status thread.");
    statusThread = new StatusThread(this, agentConfig);
    statusThread.verbose(_verbose);
    new Thread(statusThread).start();

    if (wthjec.mode == wthjec.MODE_STATUS) {
      System.out.println(_mainClass+"::connectToServers> connecting in STATUS MODE only!"); 
      return;
    }
    System.out.println(_mainClass+"::connectToServers> connecting to weatherAgent as full client");
    try {
      commandSocket = agentConfig.connect();
      if (commandSocket == null) {
	if (_verbose) System.out.println(_mainClass+"::connectToServers> ERROR: Null socket to weatherAgent!");
	return;
      }
      commandSocket.setSoTimeout(_greetTimeout); //timeout for greetings
      UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
      greet.sendTo(commandSocket);
      UFProtocol ufpr = UFProtocol.createFrom(commandSocket);
      commandSocket.setSoTimeout(0); //infinite timeout
      if (ufpr == null) {
	System.out.println(_mainClass+"::connectToServers> received null object!  Closing socket!");
	agentConfig.disconnect();
	return;
      } else {
	String request = ufpr.name().toLowerCase();
	if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
	  System.out.println(_mainClass+"::connectToServer> connection established: "+request);
	} else {
	  System.out.println(_mainClass+"::connectToServers> received "+request+".  Closing socket!");
          agentConfig.disconnect();
	  return;
	}
      }
    } catch(Exception ioe) {
      System.out.println(_mainClass+"::connectToServers> "+ioe.toString());
      mjecError.show(ioe.toString());
      agentConfig.disconnect();
    }
  }

  public void checkForAutoLogin() {
    try {
      String path = WthjecFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      installDir = URLDecoder.decode(path, "UTF-8");
      installDir = installDir.substring(0, installDir.indexOf("wthjec"));
    } catch(Exception e) {
      installDir = "..";
    }
    String xmlFile = installDir+"/etc/wthjecStartupValues.xml";
    Document doc = null;
    try {
      File file = new File(xmlFile);
      if (!file.exists()) {
	System.out.println(_mainClass+"::checkForAutoLogin> No XML auto login file found!");
	return;
      }
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(file);
    } catch(Exception e) {
      System.out.println(e.toString());
    }
    System.out.println(_mainClass+"::checkForAutoLogin> Parsing XML auto login file "+xmlFile);
    doc.getDocumentElement().normalize();
    Element root = doc.getDocumentElement();
    NodeList nlist;
    Element elem;
    nlist = root.getElementsByTagName("panel");
    //if (weatherPanels == null) return;
    for (int j = 0; j < nlist.getLength(); j++) {
      Node fstNode = nlist.item(j);
      if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	Element fstElmnt = (Element) fstNode;
	if (fstElmnt.hasAttribute("name")) {
          NodeList reclist = fstElmnt.getElementsByTagName("record");
          if (fstElmnt.getAttribute("name").trim().equals("LoginPanel")) {
	    loginPanel.loadFields(reclist);
	    int ntries = 0;
	    while (ntries < 10 && weatherPanels == null) {
	      System.err.println(ntries);
	      hibernate(500);
	      ntries++;
	    }
	  } else for (int i = 0; i < weatherPanels.length; i++) { 
	    hibernate(1000);
	    if (fstElmnt.getAttribute("name").trim().equals(weatherPanels[i].weatherStation)) {
	      weatherPanels[i].loadFields(reclist);
	    }
	  } 
        }
      }
    }
  }

  public String ctime() {
      String date = new Date( System.currentTimeMillis() ).toString();
      return( date.substring(4,19) + " LT");
  }

  protected void _startReconnectThread() {
    /* Create and start a reconnect thread to look for disconnects */
    System.out.println(_mainClass + "::_startReconnectThread> starting Reconnect thread to monitor connections...");
    _reconnect = new ReconnectThread();
    _reconnect.start();
  }

  protected void _startPingThread() {
    /* Create and start a ping thread to keep the command socket alive */
    System.out.println(_mainClass + "::_startPingThread> starting Ping thread to keep command socket alive...");
    _ping = new PingThread();
    _ping.start();
  }

  protected synchronized boolean updateDatabase(UFRecord rec) {
    String key = rec.name();
    UFGUIRecord guiRec;
    if (database.containsKey(key)) {
      synchronized(database) {
        guiRec = (UFGUIRecord)database.get(key);
        guiRec.updateRecord(rec);
      }
      return true;
    } else {
      synchronized(database) {
        guiRec = new UFGUIRecord(rec);
        database.put(key, guiRec);
      }
    }
    return false;
  }

  protected void hibernate(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {}
  }

  public void waitForReply(Socket commSoc, String name) {
    try {
      while(commSoc.getInputStream().available() <= 0) {
        try {
	  Thread.sleep(25);
        } catch(InterruptedException e) { }
      }
    } catch(IOException e) {
      System.out.println(_mainClass+"::waitForReply> Error: "+e.toString());
      mjecError.show(e.toString());
      e.printStackTrace();
      agentConfig.disconnect();
    }
  }

  protected class PingThread extends Thread {
    protected boolean _shutdown = false;
    protected boolean _error = false;
    private String _className = getClass().getName();
    protected int _sleepPeriod = 10000; //ping every 10s

    public PingThread() {}

    public void shutdown() { _shutdown = true; }

    public void reset() {
      System.out.println(_className+"::reset> Attempting to reset Ping Thread..."); 
      if (agentConfig.isConnected()) agentConfig.disconnect();
      try {
	commandSocket.close();
      } catch(Exception e) {
        System.out.println(_className+"::reset> "+e.toString());
      }
      commandSocket = null;
    }

    public void run() {
      long npings = 0;
      while (!_shutdown) {
	if (_error) {
	  //Check to see if status thread is currently disconnecting...
	  if (agentConfig.isConnected()) agentConfig.disconnect();
	  //Clear error state
	  _error = false;
	}
	hibernate(_sleepPeriod);
	//Don't send ping while waiting for command response
	if (!_ready) continue;
	if (commandSocket == null) continue;
	if (!agentConfig.isConnected()) continue;
	if (_connecting) continue;
	synchronized(commandSocket) {
	  try {
	    npings++;
	    commandSocket.setSoTimeout(_sleepPeriod);
	    UFStrings pingReq = new UFStrings(_className+": ping","ping "+npings);
	    int nbytes = pingReq.sendTo(commandSocket);
	    if( nbytes <= 0 ) {
	      System.out.println( _className + "::run> zero bytes sent.");
	      _error = true;
	      //Release lock on commandSocket to avoid deadlock
	      continue;
	    }
	    if (_verbose) System.out.println(_className+"::run> Sent ping "+npings+": "+nbytes+" bytes at "+ctime());
	    UFStrings reply = (UFStrings)UFProtocol.createFrom(commandSocket);
	    commandSocket.setSoTimeout(0);
	    if (reply == null) {
	      System.out.println(_className+"::run> Error: received null response from weatherAgent at "+ctime());
	      mjecError.show("Error: received null response from weatherAgent.");
              //Release lock on commandSocket to avoid deadlock
	      _error = true;
	      continue;
	    }
            if (_verbose) System.out.println(_className+"::run> Received response "+reply.name()+" "+reply.stringAt(0)+" from weatherAgent at "+ctime());
	  } catch (IOException ioe) {
	    System.out.println(_className+"::run> "+ioe.toString());
	    ioe.printStackTrace();
	    mjecError.show(ioe.toString());
            //Release lock on commandSocket to avoid deadlock
	    _error = true;
	    continue;
	  }
        }
      }
    } /* End run */
  } /* End PingThread */


  protected class ReconnectThread extends Thread {
    protected boolean _shutdown = false;
    private String _className = getClass().getName();
    protected int _sleepPeriod = 2000;

    public ReconnectThread() {}

    public void shutdown() { _shutdown = true; }

    public void run() {
      long _lastHeart = 0;
      int _ntries = 0;
      while (!_shutdown) {
        try {
          Thread.sleep(_sleepPeriod);
        } catch (InterruptedException e) { }
	try {
	  long currHeart = Long.parseLong(heartLabel.getVal());
	  if (currHeart > _lastHeart) {
	    _lastHeart = currHeart;
	    _ntries = 0;
	    if (messageLabel.getText().equals("Heartbeat not updating!")) messageLabel.setText("");
	  } else _ntries++;
	} catch(NumberFormatException nfe) {
	  _ntries++;
	}
        if (_ntries >= 2 && _connecting) {
          healthLabel.setValue("WARNING");
          statusLabel.setValue("CONNECT");
	} else if (_ntries >= 5) {
	  messageLabel.setText("Heartbeat not updating!");
	  healthLabel.setValue("BAD");
	  statusLabel.setValue("ERROR");
	}
	
	if (!agentConfig.doConnect()) continue;
        if (wthjec.mode == wthjec.MODE_STATUS) continue;
	if (agentConfig.isConnected()) continue;
	/* If we get here, socket has been disconnected via status thread */	   
	try {
	  /* try resetting ping thread in case of weirdness */
	  _ping.reset();
	  commandSocket = null;
	  hibernate(1000);
          /* Try restarting current status thread. */
          System.out.println(_className + "::run> restarting status thread "+ctime());
          _connecting = true;
          statusThread.restart();
	  hibernate(1000);
	  while (statusThread.isConnecting() && !statusThread.isConnected()) {
	    /* Waiting on status thread to connect.  Network may be down */
	    hibernate(1000);
	  }
	  /* Start new command socket */
          System.out.println(_className+"::run> REconnecting to weatherAgent as full client "+ctime());
	  commandSocket = agentConfig.connect();
	  if (commandSocket == null) {
	    if (_verbose) System.out.println(_className+"::connectToServers> ERROR: Null socket to weatherAgent.");
	    continue;
	  }
	  commandSocket.setSoTimeout(_greetTimeout); //timeout for greetings
	  UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
	  greet.sendTo(commandSocket);
	  UFProtocol ufpr = UFProtocol.createFrom(commandSocket);
          commandSocket.setSoTimeout(0); //infinite timeout
	  _connecting = false;
          if (ufpr == null) {
	    System.out.println(_className+"::run> received null object!  Closing socket!");
            agentConfig.disconnect();
            continue;
          } else {
            String request = ufpr.name().toLowerCase();
            if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
              System.out.println(_className+"::run> connection established "+ctime()+": "+request);
	      _ready = true;
            } else {
              System.out.println(_className+"::run> received "+request+".  Closing socket!");
              agentConfig.disconnect();
              continue;
            }
          }
	} catch(Exception ioe) {
	  System.out.println(_className+"::run> "+ioe.toString());
	  ioe.printStackTrace();
	  mjecError.show(ioe.toString());
	  agentConfig.disconnect();
	  _connecting = false;
	  continue;
	}
      }
    } /* End run */
  } /* End ReconnectThread */
}
