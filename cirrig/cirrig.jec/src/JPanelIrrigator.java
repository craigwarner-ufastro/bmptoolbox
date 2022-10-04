package cjec;

/**
 * Title:        JPanelIrrigator.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  executive panel for MJEC 
 */

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javaUFLib.*;
import javaUFProtocol.*;
import javaMMTLib.*;

//===============================================================================
/**
 *This Class handles the executive tabbed pane 
 */
public class JPanelIrrigator extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelIrrigator.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;
    JTabbedPane _tabs;
    JPanelIrrigatorOptions optionsPanel;
    LinkedHashMap<String, JPanelZoneGroup> zoneGroupPanels;

    CirrigJecButtonPanel _buttonPanel;
    String tabTitle, _ip, _type;

    int noutlets, ncounters;
    String[] outletNames;
    String[] freeOutlets;
    String[] freeCounters;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelIrrigator(CjecFrame cjecFrame, String type, String plcIP, int noutlets, String outletNames, int ncounters) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
	_type = type;
        tabTitle = plcIP; 
        this.noutlets = noutlets;
        this.ncounters = ncounters;
        this.outletNames = outletNames.split(",");
	this.freeOutlets = outletNames.split(","); //set free outlets to all at first
        this.freeCounters = new String[1];
	freeCounters[0] = "None";
        cjec = cjecFrame;
        setPreferredSize(new Dimension(1008,626));
        double height;

	zoneGroupPanels = new LinkedHashMap();

        //Tabbed pane
	_tabs = new JTabbedPane();
	optionsPanel = new JPanelIrrigatorOptions(cjecFrame, type, plcIP, this);
	_tabs.add(optionsPanel, "Options");

	//Buttton panel
	_buttonPanel = new CirrigJecButtonPanel(cjec, _type, _ip);

	add("0.01,0.01;0.99,0.91", _tabs);
	add("0.01,0.92;0.99,0.08",_buttonPanel);
        requestFreeOutlets();//now request which ones are free
	requestFreeCounters();//also request free counters
    } 

    public JPanelIrrigator(CjecFrame cjecFrame, String type, String plcIP, int noutlets, String outletNames) {
      this(cjecFrame, type, plcIP, noutlets, outletNames, 0);
    }

    public String getIp() {
      return _ip;
    }

    public String getType() {
      return _type;
    }

    public int getNOutlets() {
      return noutlets;
    }

    public int getNCounters() {
      return ncounters;
    }

    public String getOutletName(int i) {
      return outletNames[i];
    }

    public JPanelZoneGroup getZoneGroupPanel(String groupName) {
      return zoneGroupPanels.get(groupName);
    }

    public void addZoneGroupPanel(String groupName, int groupNum) {
      JPanelZoneGroup groupPanel = new JPanelZoneGroup(cjec, _ip, this, groupName, groupNum);
      groupPanel.updateFactorInches(cjec.inches);
      groupPanel.updateFactorMinutes(cjec.minutes);
      zoneGroupPanels.put(groupName, groupPanel);
      _tabs.add(groupPanel, groupName);
    }

    public void removeZoneGroupPanel(String groupName) {
      JPanelZoneGroup groupPanel = zoneGroupPanels.remove(groupName);
      _tabs.remove(groupPanel);
    }

    public void renameZoneGroupPanel(String oldName, String newName, UFStrings reply) {
      JPanelZoneGroup groupPanel = zoneGroupPanels.remove(oldName);
      int i = _tabs.indexOfComponent(groupPanel);
      int nc = groupPanel.ncycles;
      _tabs.remove(groupPanel);
      //add back in zone group and outlets from reply
      for (int j = 1; j < reply.numVals(); j++) {
        String[] temp = reply.stringAt(j).split("::");
        if (temp[0].equals("ZoneGroup")) {
          if (temp[1].equals(this.getIp()) && temp[2].equals(newName)) {
            int groupNumber = Integer.parseInt(temp[3]);
	    //add new panel at same tab index, set selected
	    groupPanel = new JPanelZoneGroup(cjec, _ip, this, newName, groupNumber);
            groupPanel.updateFactorInches(cjec.inches);
            groupPanel.updateFactorMinutes(cjec.minutes);
	    zoneGroupPanels.put(newName, groupPanel);
          }
        } else if (temp[0].equals("ZoneOutlet")) {
          if (temp[1].equals(this.getIp()) && temp[2].equals(newName)) {
            String outletName = temp[3];
            int outletNum = Integer.parseInt(temp[4]);
            this.getZoneGroupPanel(newName).addOutlet(outletName, outletNum);
          }
        }
        _tabs.add(groupPanel, groupPanel.tabTitle, i);
        _tabs.setSelectedComponent(groupPanel);
      }
    }

    public String[] getFreeCounters() {
      return freeCounters;
    }

    public String[] getFreeOutlets() {
      return freeOutlets;
    }

    public void requestFreeCounters() {
      //send request to CirrigPlcAgent - processResponse should call updateFreeCounters
      //GET_FREE_COUNTERS::irrigIP::uid
      String commandString = "GET_FREE_COUNTERS::"+_ip+"::"+cjec.getUid();
      Vector <String> commandVec = new Vector();
      commandVec.add(commandString);
      cjec.apply(commandVec);
    }

    public void requestFreeOutlets() {
      //send request to CirrigPlcAgent - processResponse should call updateFreeOutlets
      //GET_FREE_OUTLETS::irrigIP::uid
      String commandString = "GET_FREE_OUTLETS::"+_ip+"::"+cjec.getUid();
      Vector <String> commandVec = new Vector();
      commandVec.add(commandString);
      cjec.apply(commandVec);
    }

    public void updateFreeCounters(String freeCounters) {
      this.freeCounters = freeCounters.split(",");
      String key;
      JPanelZoneGroup zgPanel;
      for (Iterator i = zoneGroupPanels.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        zgPanel = (JPanelZoneGroup)zoneGroupPanels.get(key);
        zgPanel.updateFreeCounters(getFreeCounters());
      }
    }

    public void updateFreeOutlets(String freeOutlets) {
      this.freeOutlets = freeOutlets.split(",");
      String key;
      JPanelZoneGroup zgPanel;
      for (Iterator i = zoneGroupPanels.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        zgPanel = (JPanelZoneGroup)zoneGroupPanels.get(key);
	zgPanel.updateFreeOutlets(getFreeOutlets());
      }
    }

    public void updateFactorInches(boolean inches) {
      String key;
      JPanelZoneGroup zgPanel;
      for (Iterator i = zoneGroupPanels.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        zgPanel = (JPanelZoneGroup)zoneGroupPanels.get(key);
        zgPanel.updateFactorInches(inches);
      }
    }

    public void updateFactorMinutes(boolean minutes) {
      String key;
      JPanelZoneGroup zgPanel;
      for (Iterator i = zoneGroupPanels.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        zgPanel = (JPanelZoneGroup)zoneGroupPanels.get(key);
        zgPanel.updateFactorMinutes(minutes);
      }
    }

    public void updateZones() {
      for (Iterator i = zoneGroupPanels.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        JPanelZoneGroup zgPanel = (JPanelZoneGroup)zoneGroupPanels.get(key);
	zgPanel.updateZones();
      }
    }

} //end of class JPanelIrrigator
