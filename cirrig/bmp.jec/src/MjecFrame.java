package bmpjec;

/**
 * Title:        MjecFrame.java
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

public class MjecFrame extends javax.swing.JFrame {
  Container content;
  String _mainClass = getClass().getName();
  String hostname, isHostname, logname;
  LinkedHashMap <String, UFGUIRecord> database;
  Socket commandSocket;
  StatusThread statusThread;
  AgentConfig agentConfig;
  Vector <MJECButton> buttons;
  ReconnectThread _reconnect;
  boolean _verbose = false, _ready = true, _connecting = false;
  UFMessageLog _history;
  UFTail _logTail;
  public static String installDir = ".."; 
  public static String[] agentNames = {"BMPToolbox.BmpPlcAgent"};
  protected int _counter = 0;
  protected boolean isLoggedIn = false;
  protected int uid = -1;
  LinkedHashMap <String, String> runsAndZones;

  /* Menu Items */
  JMenuBar menuBar;
  JMenu menuFile, menuOptions, menuHelp;
  JMenuItem menuFileSaveAuto, menuFileClearAuto, menuFileReconnect, menuFileExit;
  JMenuItem menuOptionsErrors, menuOptionsHistory, menuOptionsLog;
  JMenuItem menuHelpAbout;

  /* Panels */
  JPanel statusPanel;
  JTabbedPane mjecTabs;
  JPanelLogin loginPanel;
  Vector <JPanelPLC> plcPanels;
  BorderLayout borderLayout;
  MJECApplyButton applyButton;

  /* Status panel */
  JLabel messageLabel, hostLabel;
  JCheckBox panelLock;
  UFMMTLabel heartLabel, healthLabel, statusLabel;
  
  protected int _timeout = 60000, _greetTimeout = 6000;

  public MjecFrame(String hostname, String isHostname, String[] args) {
    super("BMPJEC 2.0");
    setSize(1020,600);
    this.hostname = hostname;
    this.isHostname = isHostname;
    setOptions(args);

    database = new LinkedHashMap(100);
    buttons = new Vector(20);
    runsAndZones = new LinkedHashMap(10); 

    setDefaultCloseOperation(3);
    content = getContentPane();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    _history = new UFMessageLog("BMPJEC History",1000);
    setupGUI();
    setupAgentConfig();
    _connecting = true;
    hibernate(1000);
    connectToServers();
    hibernate(3000);
    _connecting = false;
    _startReconnectThread();
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

  public void registerButton(MJECButton theButton) {
    buttons.add(theButton);
  }

  public void apply(final MJECButton theButton) {
    if (mjec.mode == mjec.MODE_STATUS) {
      System.out.println(_mainClass+"::apply> in STATUS MODE!");
      return;
    }

    theButton.setEnabled(false);
    setButtonsEnabled(false);

    /* Run in a new thread */
    Thread t = new Thread() {
      public void run() {
        while (!_ready && !theButton.title.equals("Abort") && !theButton.title.equals("Stop")) {
          try {
            Thread.sleep(100);
          } catch(InterruptedException e) { }
        }
        _ready = false;
	String key = "BmpPlcAgent", command = "";
	UFGUIRecord guiRec;
	UFStrings req;

	Vector <String> commandVec = new Vector();
	if (theButton.getRecName().indexOf(key) != -1 || theButton.getRecName().indexOf("All:") == 0) {
	  /* This command should be sent to the current agent. */ 
	  command = theButton.getCommand().trim();
	  if (!command.equals("")) { 
	    commandVec.add(command);
	    _history.addMessage(key+":> "+command);
	    _history.showAllMessages();
	    if (_verbose) System.out.println(_mainClass+"::apply> "+key+":> "+command);
	  }
        }
	if (commandVec.size() == 0) {
          theButton.setEnabled(true);
          finishApply();
          return;
        }
	if (!agentConfig.doConnect()) {
          theButton.setEnabled(true);
          finishApply();
          return;
        }
	System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to "+key); 
	if (!agentConfig.isConnected()) {
	  System.out.println(_mainClass+"::apply> Error: Not connected to "+key);
	  mjecError.show("Error: Not connected to "+key);
	  agentConfig.disconnect();
          theButton.setEnabled(true);
          finishApply();
          return;
	}

	try {
	  commandSocket.setSoTimeout(_timeout);
	  req = new UFStrings(_mainClass+": actionRequest", commandVec);
	  int nbytes = req.sendTo(commandSocket);
	  if( nbytes <= 0 ) {
	    System.out.println( _mainClass + "::apply> zero bytes sent.");
	    commandSocket.setSoTimeout(0);
            theButton.setEnabled(true);
	    finishApply();
	    return;
	  }
	  if (_verbose) System.out.println(_mainClass+"::apply> Sent "+nbytes+" bytes at "+ctime());
	  /* Mark this agent as waiting for reply */
	  _counter++;
          waitForReply(commandSocket, key);
          UFStrings reply;
          synchronized(commandSocket) {
            reply = (UFStrings)UFProtocol.createFrom(commandSocket);
          }
          commandSocket.setSoTimeout(0);
          if (reply == null) {
            System.out.println(_mainClass+"::apply> Error: received null response from "+key+" at "+ctime());
            mjecError.show("Error: received null response from "+key);
	    _counter--;
            theButton.setEnabled(true);
	    finishApply();
	    return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from "+key+" at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
          mjecError.show(ioe.toString());
          if (ioe.toString().indexOf("Socket") != -1) agentConfig.disconnect();
        }
        _counter--;
	theButton.setEnabled(true);
	finishApply();
      }
    };
    t.start();
  }

  public void apply() {
    apply(applyButton);
  }

  public void apply(final MJECApplyButton applyButton) {
    if (mjec.mode == mjec.MODE_STATUS) {
      System.out.println(_mainClass+"::apply> in STATUS MODE!");
      return;
    }

    applyButton.setEnabled(false);
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
        String key = "BmpPlcAgent", guiKey, command = "";
	UFGUIRecord guiRec;

	UFStrings req;
        Vector <String> commandVec = new Vector();
        for (Iterator irecs = database.keySet().iterator(); irecs.hasNext(); ) {
          guiKey = (String)irecs.next();
          guiRec = (UFGUIRecord)database.get(guiKey);
          if (!guiRec.isMarked()) continue;
          if (guiRec.getName().indexOf(key) != -1) {
            /* This record is marked and matches the current agent name -
               get command value and add to vector */
            command = guiRec.getCommandValue().trim();
            if (!command.equals("")) {
              commandVec.add(command);
              _history.addMessage(guiRec.getName()+":> "+command);
	      _history.showAllMessages();
              if (_verbose) System.out.println(_mainClass+"::apply> "+guiRec.getName()+":> "+command);
            }
            guiRec.apply();
          }
        }
        if (commandVec.size() == 0) {
	  finishApply();
	  return;
	}
        if (!agentConfig.doConnect()) {
          finishApply();
          return;
        }
        System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to "+key);
        if (!agentConfig.isConnected()) {
          System.out.println(_mainClass+"::apply> Error: Not connected to "+key);
          mjecError.show("Error: Not connected to "+key);
	  agentConfig.disconnect();
          finishApply();
          return;
        }
        try {
          commandSocket.setSoTimeout(_timeout);
          req = new UFStrings(_mainClass+": actionRequest", commandVec);
          int nbytes = req.sendTo(commandSocket);
          if( nbytes <= 0 ) {
            System.out.println( _mainClass + "::apply> zero bytes sent.");
            commandSocket.setSoTimeout(0);
	    finishApply();
	    return;
          }
          if (_verbose) System.out.println(_mainClass+"::apply> Sent "+nbytes+" bytes at "+ctime());
          /* Mark this agent as waiting for reply */
          _counter++;
          waitForReply(commandSocket, command);
          UFStrings reply;
          synchronized(commandSocket) {
            reply = (UFStrings)UFProtocol.createFrom(commandSocket);
          }
          commandSocket.setSoTimeout(0);
          if (reply == null) {
            System.out.println(_mainClass+"::apply> Error: received null response from "+key+" at "+ctime());
            mjecError.show("Error: received null response from "+key);
            _counter--;
            finishApply();
	    return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from "+key+" at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
	  mjecError.show(ioe.toString());
          if (ioe.toString().indexOf("Socket") != -1) agentConfig.disconnect();
	}
        _counter--;
	finishApply();
      }
    };
    t.start();
  }

  public void apply(final Vector <String> commandVec) {
    if (mjec.mode == mjec.MODE_STATUS) {
      System.out.println(_mainClass+"::apply> in STATUS MODE!");
      return;
    }

    applyButton.setEnabled(false);
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
            _history.addMessage("BmpPlcAgent:> "+command);
            _history.showAllMessages();
            System.out.println(_mainClass+"::apply> BmpPlcAgent:> "+command);
          }
        }

        if (!agentConfig.doConnect()) return;
        System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to BmpPlcAgent.");
        if (!agentConfig.isConnected()) {
          System.out.println(_mainClass+"::apply> Error: Not connected to BmpPlcAgent.");
          mjecError.show("Error: Not connected to BmpPlcAgent.");
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
          waitForReply(commandSocket, "BmpPlcAgent");
          UFStrings reply;
          synchronized(commandSocket) {
            reply = (UFStrings)UFProtocol.createFrom(commandSocket);
          }
          commandSocket.setSoTimeout(0);
          if (reply == null) {
            System.out.println(_mainClass+"::apply> Error: received null response from BmpPlcAgent at "+ctime());
            mjecError.show("Error: received null response from BmpPlcAgent.");
            _counter--;
            return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from BmpPlcAgent at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
          ioe.printStackTrace();
          mjecError.show(ioe.toString());
          if (ioe.toString().indexOf("Socket") != -1) agentConfig.disconnect();
        }
        _counter--;
	finishApply();
      }
    };
    t.start();
  }

  public void finishApply() {
    /* Wait for any abort/stop commands */
    while (_counter > 0) {
      try {
	Thread.sleep(25);
      } catch(InterruptedException e) { }
    }
    setButtonsEnabled(true);
    applyButton.setEnabled(true);
    _ready = true;
  }

  public void clearAll() { 
    String guiKey;
    UFGUIRecord guiRec;
    for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
      guiKey = (String)i.next();
      guiRec = (UFGUIRecord)database.get(guiKey);
      if (guiRec.isMarked() || guiRec.blueIndex() != -1) guiRec.clear();
    }
  }

  public void clearSubsys(JPanel parentPanel) {
    String guiKey;
    UFGUIRecord guiRec;
    for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
      guiKey = (String)i.next();
      guiRec = (UFGUIRecord)database.get(guiKey);
      //if (guiRec.getName().indexOf(agentName) == -1) continue; 
      if (!guiRec.isChildOf(parentPanel)) continue;
      if (guiRec.isMarked() || guiRec.blueIndex() != -1) guiRec.clear();
    }
  }


  public void setAllInputsEnabled(boolean enabled) {
    String guiKey;
    UFGUIRecord guiRec;
    MJECButton theButton;
    for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
      guiKey = (String)i.next();
      guiRec = (UFGUIRecord)database.get(guiKey);
      guiRec.setEnabled(enabled);
    }
    for (int i = 0; i < buttons.size(); i++) {
      theButton = (MJECButton)buttons.elementAt(i);
      theButton.setEnabled(enabled);
    }
    applyButton.setEnabled(enabled);
  }

  public void setButtonsEnabled(boolean enabled) {
    MJECButton theButton;
    for (int i = 0; i < buttons.size(); i++) {
      theButton = (MJECButton)buttons.elementAt(i);
      /* Leave only aborts and stop enabled during apply */
      if (theButton.title.equals("Abort")) continue;
      theButton.setEnabled(enabled);
    }
    applyButton.setEnabled(enabled);
  }

  public boolean isLocked() {
    if (applyButton.isEnabled() && !panelLock.isSelected()) return false;
    return true;
  }

  public void processResponse(UFStrings reply) {
    System.out.println(_mainClass+"::processResponse> "+reply.toString());
    if (reply.name().toLowerCase().indexOf("error") != -1) {
      /* We received an error! */
      for (int j = 0; j < reply.numVals(); j++) {
        mjecError.show(reply.stringAt(j));
      }
    }
    for (int j = 0; j < reply.numVals(); j++) _history.addMessage("Received response:> "+reply.stringAt(j));
    _history.showAllMessages();
    messageLabel.setText(reply.stringAt(0));
    String firstReply = reply.stringAt(0);
    if (isLogin(firstReply)) {
      isLoggedIn = true;
      try {
        uid = Integer.parseInt(getReplyToken(firstReply, 2));
      } catch(NumberFormatException nfe) {
        System.out.println(_mainClass+"::processResponse> Error receiving login info: "+firstReply);
        mjecError.show("Error in login info: "+firstReply);
        isLoggedIn = false;
        uid = -1;
        return;
      }
    }
    if (isLogout(firstReply)) {
      isLoggedIn = false;
      uid = -1;
      runsAndZones.clear();
      int tabCount = mjecTabs.getTabCount();
      while(tabCount > 1) {
        mjecTabs.remove(tabCount-1);
        tabCount = mjecTabs.getTabCount();
      }
    }
    if (isZoneAndPlcList(firstReply)) {
      runsAndZones.clear();
      int tabCount = mjecTabs.getTabCount();
      while(tabCount > 1) {
        mjecTabs.remove(tabCount-1);
        tabCount = mjecTabs.getTabCount();
      }
      int npanels = 0;
      for (int j = 1; j < reply.numVals(); j++) {
	if (reply.stringAt(j).startsWith("Irrigator")) {
	  npanels++;
	}
      }
      plcPanels = new Vector(npanels);
      for (int j = 1; j < reply.numVals(); j++) {
	String[] temp = reply.stringAt(j).split("::");
	if (temp[0].equals("Irrigator")) {
	  String type = temp[1];
	  String plcString = "";
	  if (temp.length > 2) plcString = temp[2]; 
	  JPanelPLC currPanel = new JPanelPLC(this, type, plcString); 
	  plcPanels.add(currPanel);
	  mjecTabs.add(currPanel, currPanel.tabTitle);
	} else if (temp[0].equals("RT")) {
	  runsAndZones.put("R "+temp[1], "RT Run "+temp[1]+": "+temp[2]);
	} else if (temp[0].equals("ZONE")) {
	  runsAndZones.put("Z "+temp[1], "Zone "+temp[2]+": "+temp[3]+" - "+temp[4]);
	}
      }
      for (int j = 0; j < plcPanels.size(); j++) {
	JPanelPLC currPanel = plcPanels.get(j);
	currPanel.updateRunsAndZones();
      }
    }
    if (isIrrigatorCommand(firstReply)) {
      if (getReplyToken(firstReply, 1).toLowerCase().equals("added")) {
	//added plc
	String type = getReplyToken(firstReply, 3);
	String plcIP = getReplyToken(firstReply, 4);
        JPanelPLC currPanel = new JPanelPLC(this, type, plcIP);
        plcPanels.add(currPanel);
        mjecTabs.add(currPanel, currPanel.tabTitle);
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("deleted")) {
	//deleted plc
        String plcIP = getReplyToken(firstReply, 3);
	for (int j = 0; j < plcPanels.size(); j++) {
	  JPanelPLC currPanel = plcPanels.get(j);
	  if (currPanel.getIp().equals(plcIP)) {
	    plcPanels.remove(currPanel);
	    mjecTabs.remove(currPanel);
	    break;
	  }
	}
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("changed")) {
	//changed plc ip address
	String oldIP = getReplyToken(firstReply, 3);
        String newIP = getReplyToken(firstReply, 4);
        for (int j = 0; j < plcPanels.size(); j++) {
          JPanelPLC currPanel = plcPanels.get(j);
          if (currPanel.getIp().equals(oldIP)) {
	    String type = currPanel.getType();
            plcPanels.remove(currPanel);
            mjecTabs.remove(currPanel);
	    JPanelPLC newPanel = new JPanelPLC(this, type, newIP);
	    plcPanels.add(j, newPanel);
	    mjecTabs.add(newPanel, newPanel.tabTitle, j+1);
	    mjecTabs.setSelectedComponent(newPanel);
            break;
          }
        }
      }
    }
  }

  public int getUid() {
    return uid;
  }

  public boolean isLogin(String loginString) {
    if (!getReplyToken(loginString, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(loginString, 1).toLowerCase().equals("loggedin")) return true;
    return false;
  }

  public boolean isZoneAndPlcList(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 1).toLowerCase().equals("loggedin") || getReplyToken(response, 1).toLowerCase().equals("refreshed")) return true;
    return false;
  }

  public boolean isLogout(String loginString) {
    if (!getReplyToken(loginString, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(loginString, 1).toLowerCase().equals("loggedout")) return true;
    return false;
  }

  public boolean isIrrigatorCommand(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 2).toLowerCase().equals("irrigator")) return true;
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
        int n = JOptionPane.showOptionDialog(MjecFrame.this, "Save current values and auto-login?", "Save Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, saveOptions, saveOptions[0]);
        if (n == 1) return;
        try {
          PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/bmpjecStartupValues.xml"));
          pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          pw.println("<autoLogin>");
          loginPanel.saveFields(pw);
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
        int n = JOptionPane.showOptionDialog(MjecFrame.this, "Clear auto-login option?", "Clear Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, clearOptions, clearOptions[0]);
        if (n == 1) return;
        try {
          File startupFile = new File(installDir+"/etc/bmpjecStartupValues.xml");
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
        int n = JOptionPane.showOptionDialog(MjecFrame.this, "Are you sure you want to quit?", "Exit BMPJEC?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, exitOptions, exitOptions[1]);
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
	for (int i = 0; i < agentNames.length; i++) {
	  String key = agentNames[i]+":logname";
	  if (database.containsKey(key)) {
	    UFGUIRecord guiRec;
	    synchronized(database) {
	      guiRec = (UFGUIRecord)database.get(key);
	    }
	    String logname = guiRec.getValue();
	    if (!_logTail.hasTail(logname)) _logTail.addNewTail(logname);
	  }
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
    mjecTabs = new JTabbedPane();
    applyButton = new MJECApplyButton(this); 

    /* Setup status panel */
    statusPanel.setLayout(new RatioLayout());
    heartLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:heartbeat");
    heartLabel.registerComponent(database, false);
    healthLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:health");
    healthLabel.registerComponent(database, false);
    healthLabel.addDisplayValue("GOOD","GOOD", Color.GREEN);
    healthLabel.addDisplayValue("WARNING","WARNING", Color.ORANGE);
    healthLabel.addDisplayValue("BAD","BAD", Color.RED);
    statusLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:status");
    statusLabel.registerComponent(database, false);
    statusLabel.addDisplayValue("IDLE","IDLE", Color.GREEN);
    statusLabel.addDisplayValue("BUSY","BUSY", Color.BLUE);
    statusLabel.addDisplayValue("ERROR","ERROR", Color.RED);
    statusLabel.addDisplayValue("INIT","INIT", Color.ORANGE);
    statusLabel.addDisplayValue("CONNECT","CONNECT", Color.MAGENTA);
    hostLabel = new JLabel("Host: "+hostname);    
    messageLabel = new JLabel();
    panelLock = new JCheckBox("Panel Lock");
    panelLock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	setAllInputsEnabled(!panelLock.isSelected());
      }
    });
    
    statusPanel.setPreferredSize(new Dimension(1008, 48));
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
    mjecTabs.add(loginPanel, "Login");

    if (mjec.mode == mjec.MODE_STATUS) {
      panelLock.doClick();
      panelLock.setEnabled(false);
    }

    /* Add components to this frame */
    content.add(mjecTabs, 0);
    content.add(statusPanel, 1);
    pack();
    setVisible(true);
  }

  public void setupAgentConfig() {
    agentConfig = new AgentConfig("BmpPlcAgent", hostname, mjec.bmpPort);
  }

  public void connectToServers() {
    hibernate(1000);
    if (_verbose) agentConfig.setVerbosity(_verbose);
    if (!agentConfig.doConnect()) return;
    System.out.println(_mainClass + "::connectToServers> starting Status thread.");
    statusThread = new StatusThread(this, agentConfig);
    statusThread.verbose(_verbose);
    new Thread(statusThread).start();

    if (mjec.mode == mjec.MODE_STATUS) {
      System.out.println(_mainClass+"::connectToServers> connecting in STATUS MODE only!");
      return;
    }
    System.out.println(_mainClass+"::connectToServers> connecting to BmpPlcAgent as full client");
    try {
      commandSocket = agentConfig.connect();
      if (commandSocket == null) {
        if (_verbose) System.out.println(_mainClass+"::connectToServers> ERROR: Null socket to BmpPlcAgent!");
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
      String path = MjecFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      installDir = URLDecoder.decode(path, "UTF-8");
      installDir = installDir.substring(0, installDir.indexOf("bmpjec"));
    } catch(Exception e) {
      installDir = "..";
    }
    String xmlFile = installDir+"/etc/bmpjecStartupValues.xml";
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
    for (int j = 0; j < nlist.getLength(); j++) {
      Node fstNode = nlist.item(j);
      if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
        Element fstElmnt = (Element) fstNode;
        if (fstElmnt.hasAttribute("name")) {
          NodeList reclist = fstElmnt.getElementsByTagName("record");
          if (fstElmnt.getAttribute("name").trim().equals("LoginPanel")) {
            loginPanel.loadFields(reclist);
            hibernate(500);
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
    }
  }

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
        if (mjec.mode == mjec.MODE_STATUS) continue;
        if (agentConfig.isConnected()) continue;
        /* If we get here, socket has been disconnected via status thread */
        try {
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
          System.out.println(_className+"::run> REconnecting to BmpPlcAgent as full client "+ctime());
          commandSocket = agentConfig.connect();
          if (commandSocket == null) {
            if (_verbose) System.out.println(_className+"::connectToServers> ERROR: Null socket to BmpPlcAgent.");
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
              System.out.println(_className+"::run> connection established: "+request);
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
