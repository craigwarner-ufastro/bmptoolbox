package bmpjec;

/**
 * Title:        BmpJecButtonPanel.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Bottom button panel of BmpJec 
 */

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javaUFLib.*;
import javaMMTLib.*;

public class BmpJecButtonPanel extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: BmpJecButtonPanel.java,v 1.2 2010/09/01 17:36:44 warner Exp $";
    
    MjecFrame mjec;
    MJECApplyButton applyButton;
    UFColorButton clearButton, changeButton;
    MJECButton abortButton, initButton, deleteButton;
    String _ip, _type, changeString;
    UFMMTLabel plcStatusLabel;

    public BmpJecButtonPanel(MjecFrame mjecFrame, String type, String plcIP) {
      mjec = mjecFrame;
      _type = type;
      _ip = plcIP;
      setLayout(new RatioLayout());
      setPreferredSize(new Dimension(1008, 54));
      applyButton = new MJECApplyButton(mjec);
      abortButton = new MJECButton("BmpPlcAgent:Abort "+_ip, "Abort", "ABORT::"+_ip+" "+mjec.getUid(), UFColorButton.COLOR_SCHEME_RED, mjec);
      clearButton = new UFColorButton("Clear", UFColorButton.COLOR_SCHEME_YELLOW);
      initButton = new MJECButton("BmpPlcAgent:Init "+_ip, "Init", "INIT::"+_ip+" "+mjec.getUid(), UFColorButton.COLOR_SCHEME_GREEN, mjec);

      plcStatusLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".status");
      plcStatusLabel.addDisplayValue("Connected","Connected",new Color(0,180,0));
      plcStatusLabel.addDisplayValue("Disconnected","Disconnected",new Color(225,180,0));
      plcStatusLabel.addDisplayValue("Warning","Warning",new Color(225,180,0));
      plcStatusLabel.addDisplayValue("Error","Error",new Color(180,0,0));
      plcStatusLabel.addDisplayValue("Simulated","Simulated",new Color(0,0,180));
      plcStatusLabel.registerComponent(mjec.database, false);

      JPanel plcStatusPanel = new JPanel();
      plcStatusPanel.setLayout(new GridLayout(0,1,4,4));
      plcStatusPanel.add(new JLabel("PLC Status", JLabel.CENTER));
      plcStatusPanel.add(plcStatusLabel);
      plcStatusPanel.setBorder(new EtchedBorder(0));

      deleteButton = new MJECButton("BmpPlcAgent:Delete "+_ip, "Delete Irrigator", "DELETE_IRRIGATOR::"+mjec.getUid()+" "+_ip, UFColorButton.COLOR_SCHEME_RED, mjec, true);
      changeButton = new UFColorButton("Change IP", UFColorButton.COLOR_SCHEME_AQUA); 

      if (_type.equals("CSVOutput")) {
	changeButton = new UFColorButton("Change File", UFColorButton.COLOR_SCHEME_AQUA);
	plcStatusPanel.setVisible(false);
	changeString = "Change Irrigator Filename";
      } else {
	changeString = "Change Irrigator IP Address";
      }

      clearButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	  mjec.clearAll();
	}
      });

      changeButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	  JTextField newIPField = new JTextField(16);
	  newIPField.setText(_ip);
	  JPanel panel = new JPanel(new GridLayout(0, 1));
	  panel.add(new JLabel("New IP address: "));
	  panel.add(newIPField);
	  int result = JOptionPane.showConfirmDialog(null, panel, changeString, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	  if (result == JOptionPane.OK_OPTION) {
	    String newIP = newIPField.getText();
	    String plcString = "CHANGE_IRRIGATOR::"+mjec.getUid()+" "+_ip+" "+newIP;
	    Vector <String> commandVec = new Vector();
	    commandVec.add(plcString);
	    mjec.apply(commandVec);
	  }
        }
      });

      JPanel midPanel = new JPanel();
      midPanel.setLayout(new GridLayout(1,0,8,8));
      midPanel.add(abortButton);
      midPanel.add(clearButton);
      midPanel.add(initButton);
      midPanel.add(plcStatusPanel);
      midPanel.add(deleteButton);
      midPanel.add(changeButton);

      add("0.01,0.04;0.16,0.92", applyButton);
      add("0.20,0.04;0.80,0.92", midPanel);
    } 

} //end of class BmpJecButtonPanel
