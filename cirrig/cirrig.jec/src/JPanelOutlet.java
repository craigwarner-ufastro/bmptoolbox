package cjec;

/**
 * Title:        JPanelOutlet.java
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
public class JPanelOutlet extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelOutlet.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;
    JPanel outletPanel;
    JCheckBox outletEnableBox;
    JLabel timerUnitsLabel, irrigUnitsLabel, irrigRateUnitsLabel;
    UFMMTLabel statusLabel;
    UFMMTLabel pwrLabel, timerLabel;

    UFMMTIntegratedTextField timerInput, irrigInput, usernameInput, maxIrrigInput, minIrrigInput, lagTimeInput;
    UFMMTIntegratedTextField manualDefaultInput, irrigatorLogInput, irrigRateInput, priorityInput;
    UFMMTIntegratedComboBox defaultIrrigationInput, allowZeroInput;
    MJECButton pwrOnButton, pwrOffButton, startTimerButton, pauseTimerButton, resumeTimerButton, removeOutletButton;
    UFMMTLabel timerValLabel, timerStatusLabel;
    UFMMTLabel lastMessageLabel;

    UFMMTIntegratedComboBox runZoneInput;

    String tabTitle, _ip, _outletName;
    int _outletNum;
    JPanelZoneGroup _zgPanel;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelOutlet(CjecFrame cjecFrame, String plcIP, JPanelZoneGroup zgPanel, String outletName, int outletNum) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
        _zgPanel = zgPanel;
        _outletName = outletName;
        _outletNum = outletNum;
        cjec = cjecFrame;
        setPreferredSize(new Dimension(960,84));
        double height;

        int n = cjec._zones.size()+3;
        String[] runZoneKeys = new String[n];
        String[] runZoneVals = new String[n];
        String key, val;
        runZoneKeys[0] = "-1";
        runZoneVals[0] = "None";
	runZoneKeys[1] = "-2"; 
	runZoneVals[1] = "Fixed Manual Default";
        runZoneKeys[2] = "-3";
        runZoneVals[2] = "Fixed With Rain";
        int idx = 3;
        for (Iterator i = cjec._zones.keySet().iterator(); i.hasNext(); ) {
          key = (String)i.next();
          val = (String)cjec._zones.get(key);
          runZoneKeys[idx] = key;
          runZoneVals[idx] = val;
          idx++;
        }
        runZoneInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".id", runZoneVals, runZoneKeys, UFMMTIntegratedComboBox.REFERENCE);
        runZoneInput.registerComponent(cjec.database, true);
	//ASSIGN_ZONE_TO_OUTLET::irrigIP::uid::groupName::outletName::zone_id
        runZoneInput.setCommand("ASSIGN_ZONE_TO_OUTLET::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::"+getOutletName()+"::");

        pwrLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".power");
        pwrLabel.addDisplayValue("On","On",new Color(0,180,0));
        pwrLabel.addDisplayValue("Off","Off",new Color(180,0,0));
        pwrLabel.registerComponent(cjec.database, false);

        timerLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timerStat");
        timerLabel.addDisplayValue("Running","Running",new Color(0,180,0));
        timerLabel.addDisplayValue("Paused","Paused",new Color(180,180,0));
        timerLabel.addDisplayValue("Set","Set",new Color(0,0,180));
        timerLabel.addDisplayValue("Stopped","Stopped",new Color(180,0,0));
        timerLabel.registerComponent(cjec.database, false);

        pwrOnButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".power", "On", "POWER_ON::"+_ip+" "+cjec.getUid()+" "+_outletNum, MJECButton.COLOR_SCHEME_GREEN, cjec);
        pwrOffButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".power", "Off", "POWER_OFF::"+_ip+" "+cjec.getUid()+" "+_outletNum, MJECButton.COLOR_SCHEME_RED, cjec);
        startTimerButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timerStat", "Start", "START_TIMER::"+_ip+" "+cjec.getUid()+" "+_outletNum, MJECButton.COLOR_SCHEME_GREEN, cjec);
        pauseTimerButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timerStat", "Pause", "PAUSE_TIMER::"+_ip+" "+cjec.getUid()+" "+_outletNum, MJECButton.COLOR_SCHEME_ORANGE, cjec);
        resumeTimerButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timerStat", "Resume", "RESUME_TIMER::"+_ip+" "+cjec.getUid()+" "+_outletNum, MJECButton.COLOR_SCHEME_ROYAL, cjec);
	removeOutletButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".power", "Remove Outlet", "REMOVE_OUTLET_FROM_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::"+getOutletName(), MJECButton.COLOR_SCHEME_RED, cjec);

	outletEnableBox = new JCheckBox();
	outletEnableBox.setSelected(true);
        outletEnableBox.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent ae) {
	    Component[] components = outletPanel.getComponents();
	    for (int j = 0; j < components.length; j++) {
	      if (components[j] != outletEnableBox) components[j].setEnabled(outletEnableBox.isSelected());
	    }
	  }
	});

        timerInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timer");
        timerInput.setCommand("SET_TIMER::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        timerInput.allowOnlyNums(false, true);
        timerInput.registerComponent(cjec.database, true);
	timerUnitsLabel = new JLabel("sec");

        irrigInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".irrigation");
        irrigInput.setCommand("SET_IRRIGATION::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        irrigInput.allowOnlyNums(false, true);
        irrigInput.registerComponent(cjec.database, true);
	irrigUnitsLabel = new JLabel("cm");

        maxIrrigInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".maxIrrig");
        maxIrrigInput.setCommand("SET_MAX_IRRIGATION::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        maxIrrigInput.allowOnlyNums(false, true);
        maxIrrigInput.registerComponent(cjec.database, true);

        minIrrigInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".minIrrig");
        minIrrigInput.setCommand("SET_MIN_IRRIGATION::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        minIrrigInput.allowOnlyNums(false, true);
        minIrrigInput.registerComponent(cjec.database, true);

        irrigRateInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".irrigationRate");
        irrigRateInput.setCommand("SET_IRRIGATION_RATE::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        irrigRateInput.allowOnlyNums(false, true);
        irrigRateInput.registerComponent(cjec.database, true);
	irrigRateUnitsLabel = new JLabel("Rate (cm/hr)");

        priorityInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".priority");
        priorityInput.setCommand("SET_PRIORITY::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow neither decimal nor -
        priorityInput.allowOnlyNums(false, false);
        priorityInput.registerComponent(cjec.database, true);

        String[] zeroOpts = { "Yes", "No" };
        allowZeroInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".allowZero", zeroOpts, UFMMTIntegratedComboBox.ITEM);
        allowZeroInput.registerComponent(cjec.database, true);
        allowZeroInput.setCommand("SET_ALLOW_ZERO::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");

        pwrOnButton.setEnabled(false);
        pwrOffButton.setEnabled(false);

	startTimerButton.setEnabled(false);
	pauseTimerButton.setEnabled(false);
	resumeTimerButton.setEnabled(false);

        String[] defaultOpts = { "Yesterday", "3-day Avg", "5-day Max", "None", "Manual Default" }; 
        defaultIrrigationInput = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".defaultIrrigation", defaultOpts, UFMMTIntegratedComboBox.ITEM);
        defaultIrrigationInput.registerComponent(cjec.database, true);
        defaultIrrigationInput.setCommand("SET_DEFAULT_IRRIGATION::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");

        manualDefaultInput = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".manualDefault");
        manualDefaultInput.setCommand("SET_MANUAL_DEFAULT::"+_ip+" "+cjec.getUid()+" "+outletNum+" ");
        //allow decimal but not -
        manualDefaultInput.allowOnlyNums(false, true);
        manualDefaultInput.registerComponent(cjec.database, true);

        timerValLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".timerValue");
        timerValLabel.registerComponent(cjec.database, false);

	outletPanel = new JPanel();
	outletPanel.setLayout(new RatioLayout());

        outletPanel.add("0.01,0.01;0.02,0.33", outletEnableBox);
	outletPanel.add("0.03,0.01;0.04,0.33", new JLabel(outletName));
	outletPanel.add("0.10,0.01;0.12,0.33", removeOutletButton);
	outletPanel.add("0.01,0.33;0.03,0.33", new JLabel("Pri"));
	outletPanel.add("0.04,0.33;0.05,0.33", priorityInput);
	outletPanel.add("0.10,0.33;0.11,0.33", new JLabel("Assign a Zone", JLabel.CENTER));
	outletPanel.add("0.01,0.67;0.21,0.33", runZoneInput);

        outletPanel.add("0.23,0.01;0.09,0.33", pwrLabel);
        outletPanel.add("0.23,0.34;0.09,0.33", pwrOnButton);
        outletPanel.add("0.23,0.67;0.09,0.33", pwrOffButton);

        outletPanel.add("0.33,0.01;0.09,0.33", startTimerButton);
        outletPanel.add("0.33,0.34;0.09,0.33", pauseTimerButton);
        outletPanel.add("0.33,0.67;0.09,0.33", resumeTimerButton);

        outletPanel.add("0.43,0.01;0.09,0.33", new JLabel("Timer", JLabel.CENTER));
        outletPanel.add("0.43,0.34;0.09,0.33", timerLabel);
        outletPanel.add("0.43,0.67;0.09,0.33", timerValLabel);

        outletPanel.add("0.53,0.01;0.08,0.33", new JLabel("Irrigation", JLabel.CENTER));
        outletPanel.add("0.53,0.34;0.08,0.33", timerInput);
	outletPanel.add("0.61,0.34;0.03,0.33", timerUnitsLabel);
        outletPanel.add("0.53,0.67;0.08,0.33", irrigInput);
        outletPanel.add("0.61,0.67;0.03,0.33", irrigUnitsLabel);

	outletPanel.add("0.66,0.01;0.10,0.33", irrigRateUnitsLabel); 
        outletPanel.add("0.77,0.01;0.08,0.33", irrigRateInput);
        outletPanel.add("0.66,0.34;0.06,0.33", new JLabel("Default"));
        outletPanel.add("0.73,0.34;0.12,0.33", defaultIrrigationInput);
        outletPanel.add("0.66,0.67;0.10,0.33", new JLabel("Manual Def"));
        outletPanel.add("0.77,0.67;0.08,0.33", manualDefaultInput);

	outletPanel.add("0.86,0.01;0.06,0.33", new JLabel("Min"));
        outletPanel.add("0.92,0.01;0.07,0.33", minIrrigInput);
        outletPanel.add("0.86,0.34;0.06,0.33", new JLabel("Max"));
        outletPanel.add("0.92,0.34;0.07,0.33", maxIrrigInput);
        outletPanel.add("0.86,0.67;0.06,0.33", new JLabel("Allow 0"));
        outletPanel.add("0.92,0.67;0.07,0.33", allowZeroInput);

        outletPanel.setBorder(new EtchedBorder(0));


	lastMessageLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_outletNum+".lastMessage");
	lastMessageLabel.registerComponent(cjec.database, false);

        add("0.01,0.01;0.99,0.99",outletPanel);
    } 

    public String getIp() {
      return _ip;
    }

    public String getGroupName() {
      return _zgPanel.getGroupName(); 
    }

    public String getOutletName() {
      return _outletName;
    }

    public void updateFactorInches(boolean inches) {
      double fac = 1;
      if (inches) fac = 1./2.54;
      irrigInput.updateFactor(fac);
      minIrrigInput.updateFactor(fac);
      maxIrrigInput.updateFactor(fac);
      manualDefaultInput.updateFactor(fac);
      irrigRateInput.updateFactor(fac);
      if (inches) {
        irrigUnitsLabel.setText("in");
        irrigRateUnitsLabel.setText("Rate (in/hr)");
      } else {
        irrigUnitsLabel.setText("cm");
        irrigRateUnitsLabel.setText("Rate (cm/hr)");
      }
    }

    public void updateFactorMinutes(boolean minutes) {
      double fac = 1;
      if (minutes) fac = 1./60;
      timerInput.updateFactor(fac);
      if (minutes) {
        timerUnitsLabel.setText("min");
      } else {
	timerUnitsLabel.setText("sec");
      }
    }

    public void updateZones() {
      int n = cjec.getZones().size()+3;
      String[] runZoneKeys = new String[n];
      String[] runZoneVals = new String[n];
      String key, val;
      runZoneKeys[0] = "-1";
      runZoneVals[0] = "None";
      int idx = 1;
      runZoneKeys[idx] = "-2";
      runZoneVals[idx] = "Fixed Manual Default";
      idx++;
      runZoneKeys[idx] = "-3";
      runZoneVals[idx] = "Fixed With Rain";
      idx++;
      for (Iterator i = cjec.getZones().keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        val = (String)cjec.getZones().get(key);
        runZoneKeys[idx] = key;
        runZoneVals[idx] = val;
        idx++;
      }
      runZoneInput.updateArrays(runZoneVals, runZoneKeys);
    }

} //end of class JPanelOutlet
