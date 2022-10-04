package cjec;

/**
 * Title:        JPanelZoneGroup.java
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
import javaMMTLib.*;

//===============================================================================
/**
 *This Class handles the executive tabbed pane 
 */
public class JPanelZoneGroup extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelZoneGroup.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;
    ArrayList<JPanelOutlet> outletPanels;
    JPanelCounters countersPanel;

    UFMMTIntegratedTextField maxSimultaneous;
    JComboBox outletMenu, counterMenu;
    UFColorButton addOutletButton, addCounterButton;
    MJECButton deleteButton;

    UFMMTIntegratedComboBox ncyclesBox, cycleModeBox, modeBox;
    ArrayList<UFMMTIntegratedComboBox> hourBoxes, minuteBoxes;
    ArrayList<JLabel> hourLabels, minuteLabels;
    String tabTitle, _ip, _groupName;
    int _groupNumber, ncycles;
    String[] hourStrs, minuteStrs;

    UFColorButton startTestButton, pauseTestButton, resumeTestButton, stopTestButton;
    UFMMTIntegratedComboBox testMinutesBox;

    JPanelIrrigator irrPanel;
    JLabel nameLabel;

    JPanel outletsPanel, prefsPanel, systemTestPanel;

    protected boolean _hasCounters = false;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelZoneGroup(CjecFrame cjecFrame, String plcIP, JPanelIrrigator irrPanel, String groupName, int groupNumber) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
        this.irrPanel = irrPanel;
        _groupName = groupName;
        _groupNumber = groupNumber;
        tabTitle = groupName; 
        cjec = cjecFrame;
        setPreferredSize(new Dimension(1008,626));
	ncycles = 0;
        double height;
	outletPanels = new ArrayList();

	//outlets panel
	outletsPanel = new JPanel();
        outletsPanel.setLayout(new BoxLayout(outletsPanel, BoxLayout.Y_AXIS));
        //outletsPanel.setLayout(new RatioLayout());
        height = Math.floor(960./10.)/1000.;

        String nameStr = "Zone Group "+_groupNumber+": "+_groupName;
	if (cjecFrame.inches) nameStr += " (units = inches)"; else nameStr += " (units = cm)";
        nameLabel = new JLabel(nameStr, JLabel.CENTER);
	//nameLabel = new JLabel("Zone Group "+_groupNumber+": "+_groupName, JLabel.CENTER);
        //outletsPanel.add("0.01,0.01;0.98,"+height, nameLabel); 
        //outletsPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
	outletsPanel.add(nameLabel, 0);
	outletsPanel.add(new JSeparator(), 1);
        outletsPanel.setBorder(new EtchedBorder(0));

        maxSimultaneous = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".maxSimultaneous");
        maxSimultaneous.setCommand("SET_MAX_SIMULTANEOUS_ZG::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::");
        maxSimultaneous.allowOnlyNums();
        maxSimultaneous.registerComponent(cjec.database, true);

        String[] cycleVals = new String[7];
        for (int j = 0; j < cycleVals.length; j++) cycleVals[j] = String.valueOf(j); 
	ncyclesBox = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".ncycles", cycleVals, UFMMTIntegratedComboBox.ITEM);
	ncyclesBox.setCommand("SET_NCYCLES::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::");
	ncyclesBox.registerComponent(cjec.database, true);
	ncyclesBox.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		int nc = Integer.parseInt((String)ncyclesBox.getSelectedItem());
		updateNCycles(nc, false);
	    }
	});
	hourLabels = new ArrayList();
	minuteLabels = new ArrayList();
	hourBoxes = new ArrayList();
	minuteBoxes = new ArrayList();

        hourStrs = new String[24];
        for (int j = 0; j < hourStrs.length; j++) hourStrs[j] = String.valueOf(j); 
        minuteStrs = new String[60];
        for (int j = 0; j < minuteStrs.length; j++) {
          if (j < 10) minuteStrs[j] = "0"+j; else minuteStrs[j] = String.valueOf(j); 
        }

        String[] cycleModeVals = {"continuous", "daily"};
        cycleModeBox = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".cycleMode", cycleModeVals, UFMMTIntegratedComboBox.ITEM);
	cycleModeBox.setCommand("SET_CYCLE_MODE::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::");
        cycleModeBox.registerComponent(cjec.database, true);

        String[] modeVals = {"remote", "local"};
        modeBox = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".mode", modeVals, UFMMTIntegratedComboBox.ITEM);
        modeBox.setCommand("SET_MODE::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::");
        modeBox.registerComponent(cjec.database, true);

	outletMenu = new JComboBox(irrPanel.getFreeOutlets());
        addOutletButton = new UFColorButton("Add Outlet", UFColorButton.COLOR_SCHEME_GREEN);

        addOutletButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                addOutletButton.setEnabled(false);
	        //ADD_OUTLET_TO_ZONE_GROUP::irrigIP::uid::groupName::outletName
                String commandString = "ADD_OUTLET_TO_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::"+outletMenu.getSelectedItem();
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                cjec.apply(commandVec);
                addOutletButton.setEnabled(true);
            }
        });

        counterMenu = new JComboBox(irrPanel.getFreeCounters());
        addCounterButton = new UFColorButton("Add Counter", UFColorButton.COLOR_SCHEME_GREEN);

        addCounterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                addCounterButton.setEnabled(false);
                //ADD_COUNTER_TO_ZONE_GROUP::irrigIP::uid::groupName::counterName
                String commandString = "ADD_COUNTER_TO_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::"+counterMenu.getSelectedItem();
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                cjec.apply(commandVec);
                addCounterButton.setEnabled(true);
            }
        });

        UFColorButton editButton = new UFColorButton("Rename Zone Group", UFColorButton.COLOR_SCHEME_AQUA);
	deleteButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".name", "Delete Zone Group", "DELETE_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName, MJECButton.COLOR_SCHEME_RED, cjec);

        editButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            JTextField groupNameField = new JTextField(16);
            groupNameField.setText(_groupName);
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("New zone group name:"));
            panel.add(groupNameField);
            int result = JOptionPane.showConfirmDialog(null, panel, "Enter new Zone Group name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
              String newName = groupNameField.getText();
	      String commandString = "RENAME_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::"+newName;
              Vector <String> commandVec = new Vector();
              commandVec.add(commandString);
              cjec.apply(commandVec);
            }
          }
        });

	//System test panel
        String[] testMinVals = new String[15];
        for (int j = 0; j < testMinVals.length; j++) testMinVals[j] = String.valueOf(j+1);
        testMinutesBox = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".systemTestMinutes", testMinVals, UFMMTIntegratedComboBox.ITEM);
        testMinutesBox.setCommand("SET_SYSTEM_TEST_MINUTES::"+_ip+"::"+cjec.getUid()+"::"+_groupName+"::");
        testMinutesBox.registerComponent(cjec.database, true);

	startTestButton = new UFColorButton("Start", UFColorButton.COLOR_SCHEME_GREEN);

        startTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                startTestButton.setEnabled(false);
                //START_ZONE_GROUP::irrigIP::uid::groupName::minutes
                String commandString = "START_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName;
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                cjec.apply(commandVec);
                startTestButton.setEnabled(true);
            }
        });
 
	pauseTestButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".pause", "Pause", "PAUSE_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName, MJECButton.COLOR_SCHEME_ORANGE, cjec);
        resumeTestButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".resume", "Resume", "RESUME_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName, MJECButton.COLOR_SCHEME_ROYAL, cjec);
        stopTestButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+":"+_groupName+".stop", "Stop", "STOP_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+_groupName, MJECButton.COLOR_SCHEME_RED, cjec);

	systemTestPanel = new JPanel();
        systemTestPanel.setLayout(new RatioLayout());
        systemTestPanel.add("0.01,0.01;0.27,0.99", new JLabel("System Check (min)"));
        systemTestPanel.add("0.29,0.01;0.10,0.99", testMinutesBox);
	systemTestPanel.add("0.42,0.01;0.12,0.99", startTestButton);
        systemTestPanel.add("0.57,0.01;0.12,0.99", pauseTestButton);
        systemTestPanel.add("0.72,0.01;0.12,0.99", resumeTestButton);
        systemTestPanel.add("0.87,0.01;0.12,0.99", stopTestButton);
        systemTestPanel.setBorder(new EtchedBorder(0));

	//prefs panel
        prefsPanel = new JPanel();
        prefsPanel.setLayout(new RatioLayout());
	height = Math.floor(920./6.)/1000.; //9/14/18 adjusted height to be 920/1000 instead of 960/1000 for system check panel
        prefsPanel.add("0.01,0.01;0.98,"+height, new JLabel("Group Preferences", JLabel.CENTER));
        prefsPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
        prefsPanel.add("0.01,"+(height+0.04)+";0.16,"+height, new JLabel("Max Simultaneous"));
        prefsPanel.add("0.17,"+(height+0.04)+";0.10,"+height, maxSimultaneous);
        prefsPanel.add("0.01,"+(2*height+0.04)+";0.16,"+height, new JLabel("Free Outlets"));
        prefsPanel.add("0.17,"+(2*height+0.04)+";0.10,"+height, outletMenu);
	prefsPanel.add("0.10,"+(3*height+0.04)+";0.17,"+(1.8*height), addOutletButton);

        prefsPanel.add("0.29,"+(height+0.04)+";0.09,"+height, new JLabel("N cycles"));
        prefsPanel.add("0.43,"+(height+0.04)+";0.07,"+height, ncyclesBox);
        prefsPanel.add("0.29,"+(2*height+0.04)+";0.09,"+height, new JLabel("Cycle Mode"));
        prefsPanel.add("0.38,"+(2*height+0.04)+";0.12,"+height, cycleModeBox);
        prefsPanel.add("0.29,"+(3*height+0.04)+";0.09,"+height, new JLabel("Mode"));
        prefsPanel.add("0.38,"+(3*height+0.04)+";0.12,"+height, modeBox);

        prefsPanel.add("0.80,"+(height+0.04)+";0.20,"+(1.8*height), editButton);
        prefsPanel.add("0.80,"+(3*height+0.04)+";0.20,"+(1.8*height), deleteButton);

        prefsPanel.add("0.80,"+(5.1*height+0.04)+";0.07,"+height, counterMenu);
	prefsPanel.add("0.88,"+(5*height+0.04)+";0.12,"+(1.2*height), addCounterButton);

	prefsPanel.add("0.01,"+(4.9*height+0.04)+";0.61,"+(1.3*height), systemTestPanel);
        prefsPanel.setBorder(new EtchedBorder(0));

	add("0.01,0.01;0.99,0.74",new JScrollPane(outletsPanel));
	add("0.01,0.75;0.99,0.25",prefsPanel);

        int nc = Integer.parseInt((String)ncyclesBox.getSelectedItem());
        updateNCycles(nc, true); //updateNCycles here after initial read of value from agent
    } 

    public String getIp() {
      return _ip;
    }

    public String getGroupName() {
      return _groupName;
    }

    public int getGroupNumber() {
      return _groupNumber;
    }

    public void addCounter(String counterName, int counterNum) {
      if (!_hasCounters) {
	countersPanel = new JPanelCounters(cjec, _ip, this);
	countersPanel.addCounter(counterName, counterNum);
	int n = outletPanels.size();
	outletsPanel.add(countersPanel, n+1);
	_hasCounters = true;
      } else {
        countersPanel.addCounter(counterName, counterNum);
      }  
      revalidate();
      repaint();
    }

    public void addOutlet(String outletName, int outletNum) {
      double height = Math.floor(960./10.)/1000.;
      JPanelOutlet outletPanel = new JPanelOutlet(cjec, _ip, this, outletName, outletNum); 
      outletPanel.updateFactorInches(cjec.inches);
      outletPanel.updateFactorMinutes(cjec.minutes);
      outletPanels.add(outletPanel);
      int n = outletPanels.size();
      //outletsPanel.add("0.01,"+(n*height+0.04)+";0.99,"+height, outletPanel);
      outletsPanel.add(outletPanel, n+1);
      revalidate();
      repaint();
    }

    public void removeCounter(String counterName) {
      countersPanel.removeCounter(counterName);
      if (countersPanel.isEmpty()) {
        outletsPanel.remove(countersPanel);
        _hasCounters = false;
      }
      redrawOutletsPanel();
    }

    public void removeOutlet(String outletName) {
      for (Iterator i = outletPanels.iterator(); i.hasNext(); ) { 
	JPanelOutlet outletPanel = (JPanelOutlet)i.next();
	if (outletPanel.getOutletName().equals(outletName)) {
	  i.remove();
	  redrawOutletsPanel();
	  break;
	} 
      }
    }

    public void rename(String newName) {
      this._groupName = newName;
    }

    public void redrawOutletsPanel() {
      double height = Math.floor(960./10.)/1000.;
      outletsPanel.removeAll();
      outletsPanel.add("0.01,0.01;0.98,"+height, nameLabel);
      outletsPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
      synchronized(outletPanels) {
	for (int i = 0; i < outletPanels.size(); i++) {
	  outletsPanel.add("0.01,"+((i+1)*height+0.04)+";0.99,"+height, outletPanels.get(i));
	}
        if (_hasCounters) outletsPanel.add("0.01,"+((outletPanels.size()+1)*height+0.04)+";0.99,"+height, countersPanel);
      }
      revalidate();
      repaint();
    }

    public void updateFreeCounters(String[] freeCounters) {
      if (counterMenu != null) {
        counterMenu.setModel(new DefaultComboBoxModel(freeCounters));
      }
    }

    public void updateFreeOutlets(String[] freeOutlets) {
      if (outletMenu != null) {
	outletMenu.setModel(new DefaultComboBoxModel(freeOutlets));
      }
    }

    public void updateNCycles(int nc, boolean initialSet) {
      double height = Math.floor(920./6.)/1000.;
      boolean resize = false;
      if (nc > 4) {
	height = Math.floor(920./(nc+2))/1000.;
	if (nc > ncycles) resize = true;
      } else if (ncycles > 4) resize = true; 
      if (ncycles == 0 && nc > 0) {
	//add first hour/minute
	if (hourLabels.size() == 0) {
	  hourLabels.add(new JLabel("Cycle 1 Time:"));
	  UFMMTIntegratedComboBox hourInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+getGroupName()+".hour_0", hourStrs, UFMMTIntegratedComboBox.ITEM);
	  hourInput.registerComponent(cjec.database, true);
	  //SET_HOUR::irrigIP::uid::groupName::cycleNum::hour
          hourInput.setCommand("SET_HOUR::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::0::");
	  hourBoxes.add(hourInput);

	  minuteLabels.add(new JLabel(":", JLabel.CENTER));
          UFMMTIntegratedComboBox minuteInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+getGroupName()+".minute_0", minuteStrs, UFMMTIntegratedComboBox.ITEM);
          minuteInput.registerComponent(cjec.database, true);
          //SET_MINUTE::irrigIP::uid::groupName::cycleNum::minute
          minuteInput.setCommand("SET_MINUTE::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::0::");
          minuteBoxes.add(minuteInput);
	}
	if (!initialSet) {
	  hourBoxes.get(0).setSelectedIndex(6); //use 6am as default
	  minuteBoxes.get(0).setSelectedIndex(0);
	}
        prefsPanel.add("0.52,"+(height+0.04)+";0.10,"+height, hourLabels.get(0)); 
        prefsPanel.add("0.63,"+(height+0.04)+";0.06,"+height, hourBoxes.get(0));
        prefsPanel.add("0.69,"+(height+0.04)+";0.02,"+height, minuteLabels.get(0));
        prefsPanel.add("0.71,"+(height+0.04)+";0.06,"+height, minuteBoxes.get(0));
	ncycles = 1;
      }
      if (resize) {
	for (int j = 0; j < ncycles; j++) {
          prefsPanel.remove(hourLabels.get(j));
          prefsPanel.remove(hourBoxes.get(j));
          prefsPanel.remove(minuteLabels.get(j));
          prefsPanel.remove(minuteBoxes.get(j));
	}
	for (int j = 0; j < ncycles; j++) {
          prefsPanel.add("0.52,"+((j+1)*height+0.04)+";0.10,"+height, hourLabels.get(j));
          prefsPanel.add("0.63,"+((j+1)*height+0.04)+";0.06,"+height, hourBoxes.get(j));
          prefsPanel.add("0.69,"+((j+1)*height+0.04)+";0.02,"+height, minuteLabels.get(j));
          prefsPanel.add("0.71,"+((j+1)*height+0.04)+";0.06,"+height, minuteBoxes.get(j));
	}
      }
      while (nc > ncycles) {
	if (hourLabels.size() <= ncycles) {
          hourLabels.add(new JLabel("Cycle "+(ncycles+1)+" Time:"));
          UFMMTIntegratedComboBox hourInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+getGroupName()+".hour_"+ncycles, hourStrs, UFMMTIntegratedComboBox.ITEM);
          hourInput.registerComponent(cjec.database, true);
          //SET_HOUR::irrigIP::uid::groupName::cycleNum::hour
          hourInput.setCommand("SET_HOUR::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::"+ncycles+"::");
          hourBoxes.add(hourInput);

          minuteLabels.add(new JLabel(":", JLabel.CENTER));
          UFMMTIntegratedComboBox minuteInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+":"+getGroupName()+".minute_"+ncycles, minuteStrs, UFMMTIntegratedComboBox.ITEM);
          minuteInput.registerComponent(cjec.database, true);
          //SET_MINUTE::irrigIP::uid::groupName::cycleNum::minute
          minuteInput.setCommand("SET_MINUTE::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::"+ncycles+"::");
          minuteBoxes.add(minuteInput);
	}
	if (!initialSet) {
	  int hourIdx = hourBoxes.get(ncycles-1).getSelectedIndex()+1;
	  int minIdx = minuteBoxes.get(ncycles-1).getSelectedIndex();
	  if (hourIdx >= 24) {
	    hourIdx = 23;
	    if (minIdx <= 58) minIdx += 1;
	  }
          hourBoxes.get(ncycles).setSelectedIndex(hourIdx); //use 1 hour later than previous as default 
          minuteBoxes.get(ncycles).setSelectedIndex(minIdx); //use 1 hour later than previous as default 
	}
        prefsPanel.add("0.52,"+((ncycles+1)*height+0.04)+";0.10,"+height, hourLabels.get(ncycles));
        prefsPanel.add("0.63,"+((ncycles+1)*height+0.04)+";0.06,"+height, hourBoxes.get(ncycles));
        prefsPanel.add("0.69,"+((ncycles+1)*height+0.04)+";0.02,"+height, minuteLabels.get(ncycles));
        prefsPanel.add("0.71,"+((ncycles+1)*height+0.04)+";0.06,"+height, minuteBoxes.get(ncycles));
	ncycles++;
      }

      //now handle case where cycles are removed
      while (nc < ncycles) {
	ncycles--;
	hourBoxes.get(ncycles).clear();
	minuteBoxes.get(ncycles).clear();
	prefsPanel.remove(hourLabels.get(ncycles));
	prefsPanel.remove(hourBoxes.get(ncycles));
	prefsPanel.remove(minuteLabels.get(ncycles));
	prefsPanel.remove(minuteBoxes.get(ncycles));
      }
      revalidate();
      repaint();
    }

    public void updateFactorInches(boolean inches) {
      for (Iterator i = outletPanels.iterator(); i.hasNext(); ) {
        JPanelOutlet outletPanel = (JPanelOutlet)i.next();
	outletPanel.updateFactorInches(inches);
      }
      if (inches) {
	nameLabel.setText("Zone Group "+_groupNumber+": "+_groupName+" (units = inches)");
      } else {
        nameLabel.setText("Zone Group "+_groupNumber+": "+_groupName+" (units = cm)");
      }
    }

    public void updateFactorMinutes(boolean minutes) {
      for (Iterator i = outletPanels.iterator(); i.hasNext(); ) {
        JPanelOutlet outletPanel = (JPanelOutlet)i.next();
        outletPanel.updateFactorMinutes(minutes);
      }
    }

    public void updateZones() {
      for (Iterator i = outletPanels.iterator(); i.hasNext(); ) {
        JPanelOutlet outletPanel = (JPanelOutlet)i.next();
        outletPanel.updateZones();
      }
    }

} //end of class JPanelZoneGroup
