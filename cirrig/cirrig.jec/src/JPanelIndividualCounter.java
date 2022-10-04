package cjec;

/**
 * Title:        JPanelIndividualCounter.java
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
public class JPanelIndividualCounter extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelIndividualCounter.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;
    JPanel counterPanel;
    JCheckBox counterEnableBox;
    UFMMTLabel counterLabel;

    MJECButton resetCounterButton, removeCounterButton;

    String _ip, _counterName;
    int _counterNum;
    JPanelZoneGroup _zgPanel;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelIndividualCounter(CjecFrame cjecFrame, String plcIP, JPanelZoneGroup zgPanel, String counterName, int counterNum) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
        _zgPanel = zgPanel;
        _counterName = counterName;
        _counterNum = counterNum;
        cjec = cjecFrame;
        setPreferredSize(new Dimension(320,28));
        double height;

        counterLabel = new UFMMTLabel("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_counterNum+".count");
        counterLabel.registerComponent(cjec.database, false);

        resetCounterButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_counterNum+".count", "Reset", "RESET_COUNTER::"+_ip+" "+cjec.getUid()+" "+_counterNum, MJECButton.COLOR_SCHEME_RED, cjec);
	removeCounterButton = new MJECButton("CirrigPlc.CirrigPlcAgent:"+_ip+"."+_counterNum+".count", "Remove", "REMOVE_COUNTER_FROM_ZONE_GROUP::"+_ip+"::"+cjec.getUid()+"::"+getGroupName()+"::"+_counterNum, MJECButton.COLOR_SCHEME_RED, cjec);

	counterEnableBox = new JCheckBox();
	counterEnableBox.setSelected(true);
        counterEnableBox.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent ae) {
	    Component[] components = counterPanel.getComponents();
	    for (int j = 0; j < components.length; j++) {
	      if (components[j] != counterEnableBox) components[j].setEnabled(counterEnableBox.isSelected());
	    }
	  }
	});

	counterPanel = new JPanel();
	counterPanel.setLayout(new RatioLayout());

        counterPanel.add("0.01,0.01;0.08,0.99", counterEnableBox);
	counterPanel.add("0.10,0.01;0.09,0.99", new JLabel(counterName));
        counterPanel.add("0.20,0.01;0.20,0.99", new JLabel("Counts"));
        counterPanel.add("0.41,0.01;0.16,0.99", counterLabel); 
        counterPanel.add("0.60,0.01;0.19,0.99", resetCounterButton);
	counterPanel.add("0.80,0.01;0.19,0.99", removeCounterButton);
        counterPanel.setBorder(new EtchedBorder(0));

        add("0.01,0.01;0.99,0.99",counterPanel);
    } 

    public String getIp() {
      return _ip;
    }

    public String getGroupName() {
      return _zgPanel.getGroupName(); 
    }

    public String getCounterName() {
      return _counterName;
    }

} //end of class JPanelIndividualCounter
