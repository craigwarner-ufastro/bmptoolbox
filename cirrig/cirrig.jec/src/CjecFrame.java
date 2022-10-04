//todo - reading in at start sets boolean for units, this is passed to JPanelIrrigator when created, stored there, passed to JPanelZoneGroup when created, etc.
package cjec;

/**
 * Title:        CjecFrame.java
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

public class CjecFrame extends javax.swing.JFrame {
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
  public static String[] agentNames = {"CirrigPlc.CirrigPlcAgent"};
  protected int _counter = 0;
  protected boolean isLoggedIn = false, inches=false, minutes=false;
  protected int uid = -1;
  protected LinkedHashMap <String, String> _zones;

  /* Menu Items */
  JMenuBar menuBar;
  JMenu menuFile, menuOptions, menuUnits, menuHelp;
  JMenuItem menuFileSaveAuto, menuFileClearAuto, menuFileReconnect, menuFileExit;
  JMenuItem menuFileExport, menuFileImport;
  JMenuItem menuOptionsErrors, menuOptionsHistory, menuOptionsLog;
  JMenuItem menuUnitsCm, menuUnitsInches, menuUnitsSec, menuUnitsMin;
  JMenuItem menuHelpAbout;

  /* Panels */
  JPanel statusPanel;
  JTabbedPane cjecTabs;
  JPanelLogin loginPanel;
  LinkedHashMap <String, JPanelIrrigator> irrPanels;
  BorderLayout borderLayout;
  MJECApplyButton applyButton;

  /* Status panel */
  JLabel messageLabel, hostLabel;
  JCheckBox panelLock;
  UFMMTLabel heartLabel, healthLabel, statusLabel;
  
  protected int _timeout = 60000, _greetTimeout = 6000;
  protected boolean _autoLogin = false;

  public CjecFrame(String hostname, String isHostname, String[] args) {
    super("CirrigJEC 1.4.1 4/14/21");
    setSize(1020,720);
    this.hostname = hostname;
    this.isHostname = isHostname;
    setOptions(args);

    database = new LinkedHashMap(100);
    buttons = new Vector(20);
    _zones = new LinkedHashMap(10); 

    setDefaultCloseOperation(3);
    content = getContentPane();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    _history = new UFMessageLog("CirrigJEC History",1000);
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
    if (cjec.mode == cjec.MODE_STATUS) {
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
	String key = "CirrigPlcAgent", command = "";
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
	  cjecError.show("Error: Not connected to "+key);
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
            cjecError.show("Error: received null response from "+key);
	    _counter--;
            theButton.setEnabled(true);
	    finishApply();
	    return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from "+key+" at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
          cjecError.show(ioe.toString());
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
    if (cjec.mode == cjec.MODE_STATUS) {
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
        String key = "CirrigPlcAgent", guiKey, command = "";
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
          cjecError.show("Error: Not connected to "+key);
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
            cjecError.show("Error: received null response from "+key);
            _counter--;
            finishApply();
	    return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from "+key+" at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
	  cjecError.show(ioe.toString());
          if (ioe.toString().indexOf("Socket") != -1) agentConfig.disconnect();
	}
        _counter--;
	finishApply();
      }
    };
    t.start();
  }

  public void apply(final Vector <String> commandVec) {
    if (cjec.mode == cjec.MODE_STATUS) {
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
            _history.addMessage("CirrigPlcAgent:> "+command);
            _history.showAllMessages();
            System.out.println(_mainClass+"::apply> CirrigPlcAgent:> "+command);
          }
        }

        if (!agentConfig.doConnect()) return;
        System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to CirrigPlcAgent.");
        if (!agentConfig.isConnected()) {
          System.out.println(_mainClass+"::apply> Error: Not connected to CirrigPlcAgent.");
          cjecError.show("Error: Not connected to CirrigPlcAgent.");
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
          waitForReply(commandSocket, "CirrigPlcAgent");
          UFStrings reply;
          synchronized(commandSocket) {
            reply = (UFStrings)UFProtocol.createFrom(commandSocket);
          }
          commandSocket.setSoTimeout(0);
          if (reply == null) {
            System.out.println(_mainClass+"::apply> Error: received null response from CirrigPlcAgent at "+ctime());
            cjecError.show("Error: received null response from CirrigPlcAgent.");
            _counter--;
            return;
          }
          System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from CirrigPlcAgent at "+ctime());
          processResponse(reply);
        } catch (IOException ioe) {
          System.out.println(_mainClass+"::apply> "+ioe.toString());
          ioe.printStackTrace();
          cjecError.show(ioe.toString());
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
    System.out.println(_mainClass+":clearAll> Clearing all records - record name: ismarked blue");
    for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
      guiKey = (String)i.next();
      guiRec = (UFGUIRecord)database.get(guiKey);
      System.out.println(guiKey+": "+guiRec.isMarked()+" "+guiRec.blueIndex());
      if (guiRec.isMarked() || guiRec.blueIndex() != -1) guiRec.clear();
    }
    System.out.println(_mainClass+":clearAll> Done clearing.");
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
        cjecError.show(reply.stringAt(j));
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
        cjecError.show("Error in login info: "+firstReply);
        isLoggedIn = false;
        uid = -1;
        return;
      }
    }

    if (isLogout(firstReply)) {
      isLoggedIn = false;
      uid = -1;
      _zones.clear();
      int tabCount = cjecTabs.getTabCount();
      while(tabCount > 1) {
        cjecTabs.remove(tabCount-1);
        tabCount = cjecTabs.getTabCount();
      }
    } 

    if (isZoneAndPlcList(firstReply)) {
      _zones.clear();
      int tabCount = cjecTabs.getTabCount();
      while(tabCount > 1) {
        cjecTabs.remove(tabCount-1);
        tabCount = cjecTabs.getTabCount();
      }
      int npanels = 0;
      for (int j = 1; j < reply.numVals(); j++) {
	if (reply.stringAt(j).startsWith("Irrigator")) {
	  npanels++;
	}
      }
      irrPanels = new LinkedHashMap(npanels);
      for (int j = 1; j < reply.numVals(); j++) {
	String[] temp = reply.stringAt(j).split("::");
	if (temp[0].equals("Irrigator")) {
	  String type = temp[1];
	  String plcString = temp[2]; 
	  int noutlets = Integer.parseInt(temp[3]);
	  String outletNames = temp[4];
	  int ncounters = 0;
          if (temp.length > 5) {
	    ncounters = Integer.parseInt(temp[5]);
	  }
	  JPanelIrrigator currPanel = new JPanelIrrigator(this, type, plcString, noutlets, outletNames, ncounters); 
	  currPanel.updateFactorInches(inches);
	  currPanel.updateFactorMinutes(minutes);
	  irrPanels.put(plcString, currPanel);
	  cjecTabs.add(currPanel, currPanel.tabTitle);
	} else if (temp[0].equals("ZoneGroup")) {
	  String irrigIP = temp[1];
	  String groupName = temp[2];
	  int groupNumber = Integer.parseInt(temp[3]);
	  JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
	  irrPanel.addZoneGroupPanel(groupName, groupNumber);
	} else if (temp[0].equals("ZoneOutlet")) {
          String irrigIP = temp[1]; 
          String groupName = temp[2]; 
          String outletName = temp[3]; 
          int outletNum = Integer.parseInt(temp[4]);
          JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
          irrPanel.getZoneGroupPanel(groupName).addOutlet(outletName, outletNum);
	} else if (temp[0].equals("Counter")) {
          String irrigIP = temp[1];
          String groupName = temp[2];
          String counterName = temp[3];
          int counterNum = Integer.parseInt(temp[4]);
          JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
          irrPanel.getZoneGroupPanel(groupName).addCounter(counterName, counterNum);
	} else if (temp[0].equals("ZONE")) {
	  _zones.put(String.valueOf(temp[1]), "Zone "+temp[2]+": "+temp[3]+" - "+temp[4]);
	}
      }
      for (Iterator i = irrPanels.keySet().iterator(); i.hasNext(); ) {
	String key = (String)i.next();
	JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.get(key);
	currPanel.updateZones();
      }
    }

    if (isFreeCounterResponse(firstReply)) {
      String irrigIP = getReplyToken(firstReply, 1, "::");
      String freeCounters = getReplyToken(firstReply, 2, "::");
      JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
      irrPanel.updateFreeCounters(freeCounters);
      return;
    }

    if (isFreeOutletResponse(firstReply)) {
      String irrigIP = getReplyToken(firstReply, 1, "::");
      String freeOutlets = getReplyToken(firstReply, 2, "::");
      JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
      irrPanel.updateFreeOutlets(freeOutlets);
      return;
    }

    if (isIrrigatorCommand(firstReply)) {
      if (getReplyToken(firstReply, 1).toLowerCase().equals("added")) {
	//added plc
	String type = getReplyToken(firstReply, 3);
	String irrigIP = getReplyToken(firstReply, 4);
	int noutlets = Integer.parseInt(getReplyToken(firstReply, 5));
	String outletNames = getReplyToken(firstReply, 6);
        JPanelIrrigator currPanel = new JPanelIrrigator(this, type, irrigIP, noutlets, outletNames);
        currPanel.updateFactorInches(inches);
	currPanel.updateFactorMinutes(minutes);
        irrPanels.put(irrigIP, currPanel);
        cjecTabs.add(currPanel, currPanel.tabTitle);
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("deleted")) {
	//deleted plc
        String irrigIP = getReplyToken(firstReply, 3);
	JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.remove(irrigIP);
	cjecTabs.remove(currPanel);
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("changed")) {
	//changed plc ip address
	String oldIP = getReplyToken(firstReply, 3);
        String newIP = getReplyToken(firstReply, 4);
        int noutlets = Integer.parseInt(getReplyToken(firstReply, 5));
        String outletNames = getReplyToken(firstReply, 6);
	
	JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.remove(oldIP);
	int i = cjecTabs.indexOfComponent(currPanel);
        String type = currPanel.getType();
	cjecTabs.remove(currPanel);
	JPanelIrrigator newPanel = new JPanelIrrigator(this, type, newIP, noutlets, outletNames);
        currPanel.updateFactorInches(inches);
        currPanel.updateFactorMinutes(minutes);
	irrPanels.put(newIP, newPanel);
	cjecTabs.add(newPanel, newPanel.tabTitle, i);
	cjecTabs.setSelectedComponent(newPanel);
	//add back zone groups and outlets
        for (int j = 1; j < reply.numVals(); j++) {
          String[] temp = reply.stringAt(j).split("::");
          if (temp[0].equals("ZoneGroup")) {
	    if (temp[1].equals(newIP)) {
              String groupName = temp[2];
              int groupNumber = Integer.parseInt(temp[3]);
              newPanel.addZoneGroupPanel(groupName, groupNumber);
	    }
	  } else if (temp[0].equals("ZoneOutlet")) {
	    if (temp[1].equals(newIP)) {
              String groupName = temp[2];
              String outletName = temp[3];
              int outletNum = Integer.parseInt(temp[4]);
	      newPanel.getZoneGroupPanel(groupName).addOutlet(outletName, outletNum);
            }
          }
        }
      }
    } 

    if (isZoneGroupCommand(firstReply)) {
      if (getReplyToken(firstReply, 1).toLowerCase().equals("added")) {
        //added zone group
        String irrigIP = getReplyToken(firstReply, 1, "::");
	String groupName = getReplyToken(firstReply, 2, "::");
        int groupNum = Integer.parseInt(getReplyToken(firstReply, 3, "::"));
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP); 
        irrPanel.addZoneGroupPanel(groupName, groupNum);
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("removed")) {
        //deleted zone group
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String groupName = getReplyToken(firstReply, 2, "::");
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
	irrPanel.removeZoneGroupPanel(groupName);
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("renamed")) {
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String oldName = getReplyToken(firstReply, 2, "::");
        String newName = getReplyToken(firstReply, 3, "::");
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
	irrPanel.renameZoneGroupPanel(oldName, newName, reply);
      }
    }

    if (isCounterCommand(firstReply)) {
      if (getReplyToken(firstReply, 1).toLowerCase().equals("added")) {
        //SUCCESS added counter::"+irrigIP+"::"+groupName+"::"+counterName+"::"+counterNum
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String groupName = getReplyToken(firstReply, 2, "::");
        String counterName = getReplyToken(firstReply, 3, "::");
        int counterNum = Integer.parseInt(getReplyToken(firstReply, 4, "::"));
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
        irrPanel.getZoneGroupPanel(groupName).addCounter(counterName, counterNum);
        irrPanel.requestFreeCounters(); //request new list of free counters 
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("removed")) {
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String groupName = getReplyToken(firstReply, 2, "::");
        String counterName = getReplyToken(firstReply, 3, "::");
        int counterNum = Integer.parseInt(getReplyToken(firstReply, 4, "::"));
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
        irrPanel.getZoneGroupPanel(groupName).removeCounter(counterName);
        irrPanel.requestFreeCounters(); //request new list of free outlets
      }
    }

    if (isOutletCommand(firstReply)) {
      if (getReplyToken(firstReply, 1).toLowerCase().equals("added")) {
	//SUCCESS added outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String groupName = getReplyToken(firstReply, 2, "::");
	String outletName = getReplyToken(firstReply, 3, "::");
        int outletNum = Integer.parseInt(getReplyToken(firstReply, 4, "::"));
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
	irrPanel.getZoneGroupPanel(groupName).addOutlet(outletName, outletNum);
        irrPanel.requestFreeOutlets(); //request new list of free outlets
      } else if (getReplyToken(firstReply, 1).toLowerCase().equals("removed")) {
        String irrigIP = getReplyToken(firstReply, 1, "::");
        String groupName = getReplyToken(firstReply, 2, "::");
        String outletName = getReplyToken(firstReply, 3, "::");
        int outletNum = Integer.parseInt(getReplyToken(firstReply, 4, "::"));
        JPanelIrrigator irrPanel = irrPanels.get(irrigIP);
        irrPanel.getZoneGroupPanel(groupName).removeOutlet(outletName);
        irrPanel.requestFreeOutlets(); //request new list of free outlets
      }
    }

    if (isXMLString(firstReply)) {
      JFileChooser jfc = new JFileChooser();
      ExtensionFilter ftype = new ExtensionFilter("XML files:", new String[]{".xml"});
      jfc.setFileFilter(ftype);

      int returnVal = jfc.showSaveDialog(CjecFrame.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String filename = jfc.getSelectedFile().getAbsolutePath();
        File f = new File(filename);
        if (f.exists()) {
          String[] saveOptions = {"Overwrite","Cancel"};
          int n = JOptionPane.showOptionDialog(CjecFrame.this, filename+" already exists.", "File exists!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, saveOptions, saveOptions[1]);
          if (n == 1) {
             return;
          }
        }
	try {
          PrintWriter pw = new PrintWriter(new FileOutputStream(f));
	  pw.println(getReplyToken(firstReply, 1, "::"));
	  pw.close();
        } catch(IOException e) {
          System.out.println(_mainClass+"::processResponse | "+ctime()+"> Error exporting "+filename);
	  e.printStackTrace();
          cjecError.show("Error exporting "+filename);
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
    if (getReplyToken(response, 2).toLowerCase().equals("cirrigirrigator")) return true;
    return false;
  }

  public boolean isZoneGroupCommand(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 2).toLowerCase().startsWith("zonegroup")) return true;
    return false;
  }

  public boolean isCounterCommand(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 2).toLowerCase().startsWith("counter")) return true;
    return false;
  }

  public boolean isOutletCommand(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 2).toLowerCase().startsWith("outlet")) return true;
    return false;
  }

  public boolean isFreeCounterResponse(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 1).toLowerCase().startsWith("free_counters")) return true;
    return false;
  }

  public boolean isFreeOutletResponse(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 1).toLowerCase().startsWith("free_outlets")) return true;
    return false;
  }

  public boolean isXMLString(String response) {
    if (getReplyToken(response, 0, "::").toLowerCase().equals("success export_config")) return true;
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

  public void loadPrefs(NodeList reclist) {
    Element elem;
    Node recNode = null;

    for (int l = 0; l < reclist.getLength(); l++) {
      try {
        recNode = reclist.item(l);
        if (recNode.getNodeType() == Node.ELEMENT_NODE) {
          Element recElmnt = (Element)recNode;
          NodeList recnameList = recElmnt.getElementsByTagName("name");
          elem = (Element)recnameList.item(0);
          String recName = elem.getFirstChild().getNodeValue().trim();
          NodeList recvalList = recElmnt.getElementsByTagName("value");
          elem = (Element)recvalList.item(0);
          if (elem.getFirstChild() == null) continue;
          String recVal = elem.getFirstChild().getNodeValue().trim();
          if (recName.equals("units")) {
	    if (recVal.equals("inches")) menuUnitsInches.doClick();
	    if (recVal.equals("min")) menuUnitsMin.doClick();
	  }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(_mainClass+"::loadPrefs> "+e.toString()+" "+recNode.toString());
      }
    }
  }

  public void savePrefs(PrintWriter pw) {
    try {
      pw.println("  <panel name=\"PrefsPanel\">");

      pw.println("    <record>");
      pw.println("      <name>units</name>");
      if (menuUnitsCm.getText().equals("cm (x)")) {
	pw.println("      <value>cm</value>");
      } else pw.println("      <value>inches</value>");
      pw.println("    </record>");

      pw.println("    <record>");
      pw.println("      <name>units</name>");
      if (menuUnitsSec.getText().equals("sec (x)")) {
        pw.println("      <value>sec</value>");
      } else pw.println("      <value>min</value>");
      pw.println("    </record>");

      pw.println("  </panel>");
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public void updatePrefs() {
    try {
      PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/cjecStartupValues.xml"));
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.println("<autoLogin>");
      if (_autoLogin) loginPanel.saveFields(pw);
      savePrefs(pw);
      pw.println("</autoLogin>");
      pw.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println(_mainClass+"::updatePrefs> ERROR: Could not create auto-login file!");
      return;
    }
  }

  public void setupGUI() {
    menuBar = new JMenuBar();
    menuFile = new JMenu("File");

    menuFileSaveAuto = new JMenuItem("Save Auto Login");
    menuFileSaveAuto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        Object[] saveOptions = {"Save","Cancel"};
        int n = JOptionPane.showOptionDialog(CjecFrame.this, "Save current values and auto-login?", "Save Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, saveOptions, saveOptions[0]);
        if (n == 1) return;
        try {
          PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/cjecStartupValues.xml"));
          pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          pw.println("<autoLogin>");
          loginPanel.saveFields(pw);
	  _autoLogin = true; 
	  savePrefs(pw);
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
        int n = JOptionPane.showOptionDialog(CjecFrame.this, "Clear auto-login option?", "Clear Auto Login?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, clearOptions, clearOptions[0]);
        if (n == 1) return;
        try {
          File startupFile = new File(installDir+"/etc/cjecStartupValues.xml");
          boolean success = startupFile.delete();
          if (success) {
            System.out.println(_mainClass+"::setupGUI> Successfully cleared auto-login!");
          } else {
            System.out.println(_mainClass+"::setupGUI> ERROR: Could not clear auto-login!");
          }
	  _autoLogin = false;
	  //Still write prefs
          PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/cjecStartupValues.xml"));
          pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          pw.println("<autoLogin>");
          savePrefs(pw);
          pw.println("</autoLogin>");
          pw.close();
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

    menuFileExport = new JMenuItem("Export Config");
    menuFileExport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        String exportString = "EXPORT_CONFIG::"+uid;
        Vector <String> commandVec = new Vector();
        commandVec.add(exportString);
        apply(commandVec);
      }
    });
    menuFile.add(menuFileExport);

    menuFileImport = new JMenuItem("Import Config");
    menuFileImport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        JFileChooser jfc = new JFileChooser();
        ExtensionFilter ftype = new ExtensionFilter("XML files:", new String[]{".xml"});
        jfc.setFileFilter(ftype);
        int returnVal = jfc.showOpenDialog((Component)ev.getSource());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	  String xmlName = jfc.getSelectedFile().getAbsolutePath();
          String importString = "IMPORT_CONFIG::"+uid+"::";
	  try {
	    if (!(new File(xmlName)).exists()) {
              System.out.println(_mainClass+"::importConfig> ERROR: File "+xmlName+" does not exist!");
              return;
	    }
	    BufferedReader br = new BufferedReader(new FileReader(xmlName));
	    String currLine = br.readLine();
	    if (currLine == null || !currLine.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
              System.out.println(_mainClass+"::importConfig> ERROR: File "+xmlName+" is wrong format!");
	      return;
	    }
	    importString += currLine+"\n";
	    currLine = br.readLine();
            if (currLine == null || !currLine.startsWith("<cirrig>")) {
              System.out.println(_mainClass+"::importConfig> ERROR: File "+xmlName+" is wrong format!");
              return;
            }
            importString += currLine+"\n";
            while (currLine != null) {
              currLine = br.readLine();
	      if (currLine != null) importString += currLine+"\n"; 
            }
          } catch(IOException e) {
            System.out.println(_mainClass+"::importConfig> Error Reading From File "+xmlName);
	    e.printStackTrace();
	    return;
          }
	  Vector <String> commandVec = new Vector();
          commandVec.add(importString);
          //add refresh afterwards
          commandVec.add("REFRESH::"+uid);
          apply(commandVec);
	}
      }
    });
    menuFile.add(menuFileImport);

    menuFile.add(new JSeparator());

    menuFileExit = new JMenuItem("Exit");
    menuFileExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	Object[] exitOptions = {"Exit","Cancel"};
        int n = JOptionPane.showOptionDialog(CjecFrame.this, "Are you sure you want to quit?", "Exit CirrigJEC?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, exitOptions, exitOptions[1]);
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
	cjecError.show();
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

    menuUnits = new JMenu("Units");
    menuUnitsCm = new JMenuItem("cm (x)");
    menuUnitsCm.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	menuUnitsCm.setText("cm (x)");
	menuUnitsInches.setText("inches");
	inches = false;
        for (Iterator i = irrPanels.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.get(key);
          currPanel.updateFactorInches(false);
	  updatePrefs();
        }
      }
    });
    menuUnitsInches = new JMenuItem("inches");
    menuUnitsInches.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        menuUnitsCm.setText("cm");
        menuUnitsInches.setText("inches (x)");
	inches = true;
        for (Iterator i = irrPanels.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.get(key);
          currPanel.updateFactorInches(true);
	  updatePrefs();
        }
      }
    });
    menuUnitsSec = new JMenuItem("sec (x)");
    menuUnitsSec.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        menuUnitsSec.setText("sec (x)");
        menuUnitsMin.setText("min");
	minutes = false;
        for (Iterator i = irrPanels.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.get(key);
          currPanel.updateFactorMinutes(false);
	  updatePrefs();
        }
      }
    });
    menuUnitsMin = new JMenuItem("min");
    menuUnitsMin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        menuUnitsSec.setText("sec");
        menuUnitsMin.setText("min (x)");
	minutes = true;
        for (Iterator i = irrPanels.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          JPanelIrrigator currPanel = (JPanelIrrigator)irrPanels.get(key);
          currPanel.updateFactorMinutes(true);
	  updatePrefs();
        }
      }
    });
    menuUnits.add(menuUnitsCm);
    menuUnits.add(menuUnitsInches);
    menuUnits.add(menuUnitsSec);
    menuUnits.add(menuUnitsMin);
    menuBar.add(menuUnits);

    menuHelp = new JMenu("Help");
    menuHelpAbout = new JMenuItem("About");
    menuHelp.add(menuHelpAbout);
    menuBar.add(menuHelp);
    setJMenuBar(menuBar);

    statusPanel = new JPanel();
    cjecTabs = new JTabbedPane();
    applyButton = new MJECApplyButton(this); 

    /* Setup status panel */
    statusPanel.setLayout(new RatioLayout());
    heartLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:heartbeat");
    heartLabel.registerComponent(database, false);
    healthLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:health");
    healthLabel.registerComponent(database, false);
    healthLabel.addDisplayValue("GOOD","GOOD", Color.GREEN);
    healthLabel.addDisplayValue("WARNING","WARNING", Color.ORANGE);
    healthLabel.addDisplayValue("BAD","BAD", Color.RED);
    statusLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:status");
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
    cjecTabs.add(loginPanel, "Login");

    if (cjec.mode == cjec.MODE_STATUS) {
      panelLock.doClick();
      panelLock.setEnabled(false);
    }

    /* Add components to this frame */
    content.add(cjecTabs, 0);
    content.add(statusPanel, 1);
    pack();
    setVisible(true);
  }

  public void setupAgentConfig() {
    agentConfig = new AgentConfig("CirrigPlcAgent", hostname, cjec.cirrigPort);
  }

  public void connectToServers() {
    hibernate(1000);
    if (_verbose) agentConfig.setVerbosity(_verbose);
    if (!agentConfig.doConnect()) return;
    System.out.println(_mainClass + "::connectToServers> starting Status thread.");
    statusThread = new StatusThread(this, agentConfig);
    statusThread.verbose(_verbose);
    new Thread(statusThread).start();

    if (cjec.mode == cjec.MODE_STATUS) {
      System.out.println(_mainClass+"::connectToServers> connecting in STATUS MODE only!");
      return;
    }
    System.out.println(_mainClass+"::connectToServers> connecting to CirrigPlcAgent as full client");
    try {
      commandSocket = agentConfig.connect();
      if (commandSocket == null) {
        if (_verbose) System.out.println(_mainClass+"::connectToServers> ERROR: Null socket to CirrigPlcAgent!");
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
      cjecError.show(ioe.toString());
      agentConfig.disconnect();
    }
  }

  public void checkForAutoLogin() {
    try {
      String path = CjecFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      installDir = URLDecoder.decode(path, "UTF-8");
      installDir = installDir.substring(0, installDir.indexOf("cjec"));
    } catch(Exception e) {
      installDir = "..";
    }
    String xmlFile = installDir+"/etc/cjecStartupValues.xml";
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
	    _autoLogin = true;
          } else if (fstElmnt.getAttribute("name").trim().equals("PrefsPanel")) {
	    loadPrefs(reclist);
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

  public LinkedHashMap<String, String> getZones() {
    return _zones;
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
      cjecError.show(e.toString());
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
        if (cjec.mode == cjec.MODE_STATUS) continue;
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
          System.out.println(_className+"::run> REconnecting to CirrigPlcAgent as full client "+ctime());
          commandSocket = agentConfig.connect();
          if (commandSocket == null) {
            if (_verbose) System.out.println(_className+"::connectToServers> ERROR: Null socket to CirrigPlcAgent.");
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
          cjecError.show(ioe.toString());
          agentConfig.disconnect();
          _connecting = false;
          continue;
        }
      }
    } /* End run */
  } /* End ReconnectThread */
}
