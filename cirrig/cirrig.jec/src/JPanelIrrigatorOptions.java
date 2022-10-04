package cjec;

/**
 * Title:        JPanelIrrigatorOptions.java
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
public class JPanelIrrigatorOptions extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelIrrigatorOptions.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;

    UFMMTIntegratedComboBox multipleZoneGroups;
    UFMMTIntegratedTextField maxSimultaneous, irrigatorLog;

    JTextField zgNameField, zgNumberField; 
    UFColorButton addZoneGroupButton;

    MJECButton[] onButtons, offButtons, resetCounterButtons;
    UFMMTLabel[] statusLabels, counterLabels;
    MJECButton allOffButton, resetAllCountersButton;

    String tabTitle, _ip, _type;
    JPanelIrrigator irrPanel; //parent panel

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelIrrigatorOptions(CjecFrame cjecFrame, String type, String plcIP, JPanelIrrigator irrPanel) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
	_type = type;
        tabTitle = plcIP; 
        this.irrPanel = irrPanel;
        cjec = cjecFrame;
        setPreferredSize(new Dimension(1008,626));
        double height;

	maxSimultaneous = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+".maxSimultaneous");
        maxSimultaneous.setCommand("SET_MAX_SIMULTANEOUS::"+_ip+"::"+cjec.getUid()+"::");
        maxSimultaneous.allowOnlyNums();
        maxSimultaneous.registerComponent(cjec.database, true);

        irrigatorLog = new UFMMTIntegratedTextField("CirrigPlc.CirrigPlcAgent:"+_ip+".irrigatorLog");
        irrigatorLog.setCommand("SET_IRRIGATOR_LOG::"+_ip+" "+cjec.getUid()+" ");
        irrigatorLog.allowOnlyAlphaNumeric();
        irrigatorLog.registerComponent(cjec.database, true);

        String[] multipleOpts = { "Yes", "No" };
        multipleZoneGroups = new UFMMTIntegratedComboBox("CirrigPlc.CirrigPlcAgent:"+_ip+".allowMultipleGroups", multipleOpts, UFMMTIntegratedComboBox.ITEM);
        multipleZoneGroups.setCommand("SET_ALLOW_MULTIPLE_GROUPS::"+_ip+"::"+cjec.getUid()+"::");
        multipleZoneGroups.registerComponent(cjec.database, true);

	zgNameField = new JTextField(12);
	zgNumberField = new JTextField(4);
	zgNumberField.addKeyListener(new KeyListener() {
	    //allow only numbers and backspace
            public void keyPressed(KeyEvent kev) {
            }
            public void keyReleased(KeyEvent kev) {}
            public void keyTyped(KeyEvent kev) {
                boolean validChar = false;
                if (Character.isDigit(kev.getKeyChar()) || kev.getKeyChar() == 27) validChar = true;
                if (!validChar) {
                    kev.consume();
                }
            }
	});

        addZoneGroupButton = new UFColorButton("Add Zone Group", UFColorButton.COLOR_SCHEME_GREEN);

        addZoneGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
		String name = zgNameField.getText();
		String num = zgNumberField.getText();
		if (name.equals("") || num.equals("")) {
		  JOptionPane.showMessageDialog(new JFrame(), "Error: zone name or zone number is blank!");
		  return;
		}
                addZoneGroupButton.setEnabled(false);
                //ADD_ZONE_GROUP::irrigIP::uid::groupName::groupNum
		String commandString = "ADD_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+zgNameField.getText()+"::"+zgNumberField.getText();
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                cjec.apply(commandVec);
		addZoneGroupButton.setEnabled(true);
		zgNameField.setText("");
		zgNumberField.setText("");
            }
        });

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new RatioLayout());
        height = Math.floor(960./8.)/1000.;
        optionsPanel.add("0.01,0.01;0.98,"+height, new JLabel("Irrigator Options", JLabel.CENTER));
        optionsPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
        optionsPanel.add("0.01,"+(height+0.04)+";0.56,"+height, new JLabel("Max Simultaneous"));
        optionsPanel.add("0.59,"+(height+0.04)+";0.41,"+height, maxSimultaneous);
        optionsPanel.add("0.01,"+(2*height+0.04)+";0.56,"+height, new JLabel("Irrigator Log"));
        optionsPanel.add("0.59,"+(2*height+0.04)+";0.41,"+height, irrigatorLog);
        optionsPanel.add("0.01,"+(3*height+0.04)+";0.56,"+height, new JLabel("Allow Multiple Groups"));
        optionsPanel.add("0.59,"+(3*height+0.04)+";0.41,"+height, multipleZoneGroups);

        optionsPanel.add("0.25,"+(4*height+0.04)+";0.50,"+(1.8*height), addZoneGroupButton);
        optionsPanel.add("0.01,"+(6*height+0.04)+";0.56,"+height, new JLabel("Group Name")); 
        optionsPanel.add("0.59,"+(6*height+0.04)+";0.41,"+height, zgNameField); 
        optionsPanel.add("0.01,"+(7*height+0.04)+";0.56,"+height, new JLabel("Group Number"));
        optionsPanel.add("0.59,"+(7*height+0.04)+";0.41,"+height, zgNumberField);

        optionsPanel.setBorder(new EtchedBorder(0));


	int noutlets = irrPanel.getNOutlets();
	System.out.println("JPanelIrrigatorOptions:> "+plcIP+" noutlets = "+noutlets);
	onButtons = new MJECButton[noutlets];
	offButtons = new MJECButton[noutlets];
	statusLabels = new UFMMTLabel[noutlets];

	for (int j = 0; j < noutlets; j++) {
          onButtons[j] = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+j+".power", "On", "POWER_ON::"+_ip+" "+cjec.getUid()+" "+j, MJECButton.COLOR_SCHEME_GREEN, cjec);
          offButtons[j] = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+j+".power", "Off", "POWER_OFF::"+_ip+" "+cjec.getUid()+" "+j, MJECButton.COLOR_SCHEME_RED, cjec);
	  statusLabels[j] = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+j+".power"); 
	  statusLabels[j].addDisplayValue("On","On",new Color(0,180,0));
	  statusLabels[j].addDisplayValue("Off","Off",new Color(180,0,0));
	  statusLabels[j].registerComponent(cjec.database, false);
	}

	JPanel powerPanel = new JPanel();
	powerPanel.setLayout(new RatioLayout());
	height = Math.floor(960./(noutlets+2))/1000.;
	powerPanel.add("0.01,0.01;0.98,"+height, new JLabel("Irrigator Outlet Power", JLabel.CENTER));
	if (noutlets <= 24) {
          powerPanel.add("0.01,"+(0.01+height)+";0.24,"+height, new JLabel("Outlet",JLabel.CENTER));
          powerPanel.add("0.26,"+(0.01+height)+";0.24,"+height, new JLabel("On",JLabel.CENTER));
          powerPanel.add("0.51,"+(0.01+height)+";0.24,"+height, new JLabel("Off",JLabel.CENTER));
          powerPanel.add("0.76,"+(0.01+height)+";0.24,"+height, new JLabel("Status",JLabel.CENTER));
          powerPanel.add("0.01,"+(0.01+2*height)+";0.98,0.04", new JSeparator());
	  for (int j = 0; j < noutlets; j++) {
	    powerPanel.add("0.01,"+((j+2)*height+0.04)+";0.24,"+height, new JLabel(irrPanel.getOutletName(j), JLabel.CENTER));
	    powerPanel.add("0.26,"+((j+2)*height+0.04)+";0.24,"+height, onButtons[j]);
            powerPanel.add("0.51,"+((j+2)*height+0.04)+";0.24,"+height, offButtons[j]);
            powerPanel.add("0.76,"+((j+2)*height+0.04)+";0.24,"+height, statusLabels[j]);
	  }
	} else {
          height = Math.floor(960./(noutlets/2+2))/1000.;
          powerPanel.add("0.01,"+(0.01+height)+";0.12,"+height, new JLabel("Outlet",JLabel.CENTER));
          powerPanel.add("0.13,"+(0.01+height)+";0.12,"+height, new JLabel("On",JLabel.CENTER));
          powerPanel.add("0.25,"+(0.01+height)+";0.12,"+height, new JLabel("Off",JLabel.CENTER));
          powerPanel.add("0.37,"+(0.01+height)+";0.12,"+height, new JLabel("Status",JLabel.CENTER));
          powerPanel.add("0.51,"+(0.01+height)+";0.12,"+height, new JLabel("Outlet",JLabel.CENTER));
          powerPanel.add("0.63,"+(0.01+height)+";0.12,"+height, new JLabel("On",JLabel.CENTER));
          powerPanel.add("0.75,"+(0.01+height)+";0.12,"+height, new JLabel("Off",JLabel.CENTER));
          powerPanel.add("0.87,"+(0.01+height)+";0.12,"+height, new JLabel("Status",JLabel.CENTER));
          powerPanel.add("0.01,"+(0.01+2*height)+";0.98,0.04", new JSeparator());
          for (int j = 0; j < noutlets/2; j++) {
            powerPanel.add("0.01,"+((j+2)*height+0.04)+";0.12,"+height, new JLabel(irrPanel.getOutletName(j), JLabel.CENTER));
            powerPanel.add("0.13,"+((j+2)*height+0.04)+";0.12,"+height, onButtons[j]);
            powerPanel.add("0.25,"+((j+2)*height+0.04)+";0.12,"+height, offButtons[j]);
            powerPanel.add("0.37,"+((j+2)*height+0.04)+";0.12,"+height, statusLabels[j]);
            powerPanel.add("0.51,"+((j+2)*height+0.04)+";0.12,"+height, new JLabel(irrPanel.getOutletName(j+noutlets/2), JLabel.CENTER));
            powerPanel.add("0.63,"+((j+2)*height+0.04)+";0.12,"+height, onButtons[j+noutlets/2]);
            powerPanel.add("0.75,"+((j+2)*height+0.04)+";0.12,"+height, offButtons[j+noutlets/2]);
            powerPanel.add("0.87,"+((j+2)*height+0.04)+";0.12,"+height, statusLabels[j+noutlets/2]);
          }
	}
        powerPanel.setBorder(new EtchedBorder(0));

	JPanel counterPanel = new JPanel();
	int ncounters = irrPanel.getNCounters();
	if (ncounters > 0) {
	  counterLabels = new UFMMTLabel[ncounters];
	  counterPanel.setLayout(new RatioLayout());
	  resetCounterButtons = new MJECButton[ncounters];
          height = Math.floor(960./((ncounters+2)/3+2))/1000.;
          counterPanel.add("0.01,0.01;0.98,"+height, new JLabel("Irrigator Counters", JLabel.CENTER));
	  int cnum = 0;
	  for (int j = 0; j < ncounters; j++) {
	    counterLabels[j] = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+j+".count");
	    counterLabels[j].registerComponent(cjec.database, false);
	    resetCounterButtons[j] = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+j+".count", "0", "RESET_COUNTER::"+_ip+" "+cjec.getUid()+" "+j, MJECButton.COLOR_SCHEME_RED, cjec);
	    if (j < (ncounters+2)/3) {
	      counterPanel.add("0.01,"+((j+2)*height)+";0.07,"+height, new JLabel("C"+cnum, JLabel.CENTER));
              counterPanel.add("0.08,"+((j+2)*height)+";0.17,"+height, counterLabels[j]); 
	      counterPanel.add("0.26,"+((j+2)*height)+";0.07,"+height, resetCounterButtons[j]); 
	    } else if (j < (ncounters*2+1)/3) {
              counterPanel.add("0.35,"+((j-(ncounters+2)/3+2)*height)+";0.07,"+height, new JLabel("C"+cnum, JLabel.CENTER));
              counterPanel.add("0.42,"+((j-(ncounters+2)/3+2)*height)+";0.17,"+height, counterLabels[j]);
              counterPanel.add("0.60,"+((j-(ncounters+2)/3+2)*height)+";0.07,"+height, resetCounterButtons[j]);
	    } else {
              counterPanel.add("0.68,"+((j-(ncounters*2+1)/3+2)*height)+";0.09,"+height, new JLabel("C"+cnum, JLabel.CENTER));
              counterPanel.add("0.77,"+((j-(ncounters*2+1)/3+2)*height)+";0.16,"+height, counterLabels[j]);
              counterPanel.add("0.93,"+((j-(ncounters*2+1)/3+2)*height)+";0.07,"+height, resetCounterButtons[j]);
	    }
	    cnum++;
	    if (cnum == 24) cnum = 100;
	    if (cnum % 10 == 8) cnum += 2;
	  }
	  counterPanel.setBorder(new EtchedBorder(0));
	  resetAllCountersButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+".0.count", "Reset All Counters", "RESET_COUNTER::"+_ip+" "+cjec.getUid()+" -1", MJECButton.COLOR_SCHEME_RED, cjec);
	}

        allOffButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+".power", "All Off", "POWER_OFF_ALL::"+_ip+" "+cjec.getUid(), MJECButton.COLOR_SCHEME_RED, cjec);

	float powerHeight = (noutlets+2)*0.054f;
	if (powerHeight > 0.99) powerHeight = 0.99f;
	add("0.01,0.01;0.39,0.39",optionsPanel);
	//add("0.51,0.01;0.49,"+((noutlets+2)*0.054),new JScrollPane(powerPanel));
	if (noutlets <= 24) {
	  add("0.51,0.01;0.49,"+powerHeight, powerPanel);
        } else {
          add("0.41,0.01;0.59,"+powerHeight, powerPanel);
	}
	if (ncounters > 0) {
	  add("0.01,0.42;0.39,0.39", counterPanel);
	  add("0.01,0.84;0.18,0.08", resetAllCountersButton);
          add("0.21,0.84;0.18,0.08", allOffButton);
	} else add("0.15,0.60;0.20,0.08",allOffButton);
    } 

    public String getIp() {
      return _ip;
    }

    public String getType() {
      return _type;
    }

} //end of class JPanelIrrigatorOptions
