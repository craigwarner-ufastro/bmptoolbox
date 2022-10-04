package bmpjec;

/**
 * Title:        JPanelLogin.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  login panel for bmpjec 
 */

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFLib.*;
import javaMMTLib.*;

//===============================================================================
/**
 *This Class handles the executive tabbed pane 
 */
public class JPanelLogin extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelLogin.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    String _mainClass = getClass().getName();
    MjecFrame mjec;
    JTextField usernameField, plcIpField;
    JComboBox plcTypeBox;
    JPasswordField passwordField;
    JLabel statusLabel, irrigatorLabel;
    UFColorButton loginButton, logoutButton, refreshButton, addPLCButton;
    UFMMTLabel[] heartLabels, healthLabels, statusLabels;

    SubsysHeartThread _thread;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelLogin(MjecFrame mjecFrame) {
	this.setLayout(new RatioLayout());
        mjec = mjecFrame;
        setPreferredSize(new Dimension(1008,506));
        double height;

        /* Login Panel */
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new RatioLayout());

        usernameField = new JTextField(12); 
        passwordField = new JPasswordField(12);

        loginButton = new UFColorButton("Login", UFColorButton.COLOR_SCHEME_GREEN);
        logoutButton = new UFColorButton("Logout", UFColorButton.COLOR_SCHEME_RED);
        refreshButton = new UFColorButton("Refresh", UFColorButton.COLOR_SCHEME_AQUA);

	loginButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		loginButton.setEnabled(false);
		String passwd = "none";
		try {
		    passwd = AeSimpleSHA1.SHA1(new String(passwordField.getPassword()));
		} catch (Exception e) {
		    System.out.println(_mainClass+"::action> SHA1 Error: "+e.toString());
		    System.out.println(_mainClass+"::action> Password can't be sent!");
		    return;
		}
		String loginString = "LOGIN::"+usernameField.getText()+" "+passwd;
		Vector <String> commandVec = new Vector();
		commandVec.add(loginString);
		mjec.apply(commandVec);
		updateState();
	    }
	});

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                logoutButton.setEnabled(false);
                String logoutString = "LOGOUT::"+mjec.uid;
                Vector <String> commandVec = new Vector();
                commandVec.add(logoutString);
                mjec.apply(commandVec);
                updateState();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                refreshButton.setEnabled(false);
                String refreshString = "REFRESH::"+mjec.uid;
                Vector <String> commandVec = new Vector();
                commandVec.add(refreshString);
                mjec.apply(commandVec);
                updateState();
            }
        });


        height = Math.floor(960./5.)/1000.;
        loginPanel.add("0.01,0.01;0.98,"+height, new JLabel("Login", JLabel.CENTER));
        loginPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
        loginPanel.add("0.01,"+(height+0.04)+";0.49,"+height, new JLabel("Username"));
        loginPanel.add("0.50,"+(height+0.04)+";0.49,"+height, usernameField);
        loginPanel.add("0.01,"+(2*height+0.04)+";0.49,"+height, new JLabel("Password"));
        loginPanel.add("0.50,"+(2*height+0.04)+";0.49,"+height, passwordField);
        loginPanel.add("0.03,"+(3.5*height+0.04)+";0.28,"+(1.5*height), loginButton);
        loginPanel.add("0.36,"+(3.5*height+0.04)+";0.28,"+(1.5*height), refreshButton);
        loginPanel.add("0.69,"+(3.5*height+0.04)+";0.28,"+(1.5*height), logoutButton);
        loginPanel.setBorder(new EtchedBorder(0));
        loginPanel.setBackground(Color.green);

        /* Status Panel */
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new RatioLayout());
        int nAgents = mjec.agentNames.length;
        height = Math.floor(960./(nAgents+2))/1000.;
        statusPanel.add("0.01,0.01;0.98,"+height, new JLabel("STATUS", JLabel.CENTER));
        statusPanel.add("0.01,"+(0.01+height)+";0.33,"+height,new JLabel("Agent",JLabel.CENTER));
        statusPanel.add("0.34,"+(0.01+height)+";0.24,"+height,new JLabel("Heartbeat",JLabel.CENTER));
        statusPanel.add("0.58,"+(0.01+height)+";0.24,"+height,new JLabel("Health",JLabel.CENTER));
        statusPanel.add("0.82,"+(0.01+height)+";0.18,"+height,new JLabel("Status",JLabel.CENTER));
        statusPanel.add("0.01,"+(0.01+2*height)+";0.98,0.02", new JSeparator());
        heartLabels = new UFMMTLabel[nAgents];
        healthLabels = new UFMMTLabel[nAgents];
        statusLabels = new UFMMTLabel[nAgents];
        for (int j = 0; j < nAgents; j++) {
          heartLabels[j] = new UFMMTLabel(mjec.agentNames[j]+":heartbeat");
          heartLabels[j].registerComponent(mjec.database, false);
          healthLabels[j] = new UFMMTLabel(mjec.agentNames[j]+":health");
          healthLabels[j].registerComponent(mjec.database, false);
          healthLabels[j].addDisplayValue("GOOD","GOOD", Color.GREEN);
          healthLabels[j].addDisplayValue("WARNING","WARNING", Color.ORANGE);
          healthLabels[j].addDisplayValue("BAD","BAD", Color.RED);
          statusLabels[j] = new UFMMTLabel(mjec.agentNames[j]+":status");
          statusLabels[j].registerComponent(mjec.database, false);
          statusLabels[j].addDisplayValue("IDLE","IDLE", Color.GREEN);
          statusLabels[j].addDisplayValue("INIT","INIT", Color.ORANGE);
          statusLabels[j].addDisplayValue("BUSY","BUSY", Color.BLUE);
          statusLabels[j].addDisplayValue("ERROR","ERROR", Color.RED);
          String ssname = mjec.agentNames[j].substring(mjec.agentNames[j].indexOf(".")+1);
          statusPanel.add("0.01,"+((j+2)*height+0.04)+";0.33,"+height, new JLabel(ssname));
          statusPanel.add("0.34,"+((j+2)*height+0.04)+";0.24,"+height, heartLabels[j]);
          statusPanel.add("0.58,"+((j+2)*height+0.04)+";0.24,"+height, healthLabels[j]);
          statusPanel.add("0.82,"+((j+2)*height+0.04)+";0.18,"+height, statusLabels[j]);
        }
        statusPanel.setBorder(new EtchedBorder(0));

        /* PLC Panel */
        JPanel plcPanel = new JPanel();
        plcPanel.setLayout(new RatioLayout());

	irrigatorLabel = new JLabel("IP Address");
	irrigatorLabel.setVisible(false);
        plcIpField = new JTextField(16);
	plcIpField.setVisible(false);
        String[] plcTypeList = {"Select One", "BMPPLC", "CSVOutput", "SimPLC"};
	plcTypeBox = new JComboBox(plcTypeList);

	plcTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
		if (plcTypeBox.getSelectedItem().equals("BMPPLC") || plcTypeBox.getSelectedItem().equals("SimPLC")) {
		  irrigatorLabel.setText("IP Address");
		  irrigatorLabel.setVisible(true);
		  plcIpField.setVisible(true);
                  addPLCButton.setEnabled(true);
		} else if (plcTypeBox.getSelectedItem().equals("CSVOutput")) {
                  irrigatorLabel.setText("Filename");
                  irrigatorLabel.setVisible(true);
                  plcIpField.setVisible(true);
                  addPLCButton.setEnabled(true);
		} else {
		  irrigatorLabel.setVisible(false);
		  plcIpField.setVisible(false);
		  addPLCButton.setEnabled(false);
		}
            }
	});

        addPLCButton = new UFColorButton("Add Irrigator", UFColorButton.COLOR_SCHEME_GREEN);
        addPLCButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                addPLCButton.setEnabled(false);
                String plcString = "ADD_IRRIGATOR::"+mjec.getUid()+" "+plcIpField.getText()+" "+plcTypeBox.getSelectedItem();
                Vector <String> commandVec = new Vector();
                commandVec.add(plcString);
                mjec.apply(commandVec);
                updateState();
            }
        });
        addPLCButton.setEnabled(false);

        height = Math.floor(960./5.)/1000.;
        plcPanel.add("0.01,0.01;0.98,"+height, new JLabel("Add a new Irrigator", JLabel.CENTER));
        plcPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
        plcPanel.add("0.01,"+(height+0.04)+";0.49,"+height, new JLabel("Irrigator Type"));
        plcPanel.add("0.50,"+(height+0.04)+";0.49,"+height, plcTypeBox);
        plcPanel.add("0.01,"+(2*height+0.04)+";0.49,"+height, irrigatorLabel); 
        plcPanel.add("0.50,"+(2*height+0.04)+";0.49,"+height, plcIpField);
        plcPanel.add("0.25,"+(3.25*height+0.04)+";0.50,"+(1.5*height), addPLCButton); 
        plcPanel.setBorder(new EtchedBorder(0));
        plcPanel.setBackground(Color.CYAN);

        add("0.01,0.01;0.46,0.49",loginPanel);
        add("0.01,0.60;0.46,0.20",statusPanel);
        add("0.51,0.01;0.33,0.30",plcPanel);
	updateState();

	_thread = new SubsysHeartThread();
	_thread.start();
    } 

    protected void updateState() {
	boolean isLoggedIn = mjec.isLoggedIn;
	usernameField.setEnabled(!isLoggedIn);
	passwordField.setEnabled(!isLoggedIn);
	loginButton.setEnabled(!isLoggedIn);
	refreshButton.setEnabled(isLoggedIn);
	logoutButton.setEnabled(isLoggedIn);
	plcIpField.setEnabled(isLoggedIn);
	plcTypeBox.setEnabled(isLoggedIn);
    } 

    public void saveFields(PrintWriter pw) {
	try {
          pw.println("  <panel name=\"LoginPanel\">");
          pw.println("    <record>");
          pw.println("      <name>username</name>");
          pw.println("      <value>"+usernameField.getText()+"</value>");
          pw.println("    </record>");

          pw.println("    <record>");
          pw.println("      <name>password</name>");
          pw.println("      <value>"+new String(passwordField.getPassword())+"</value>");
          pw.println("    </record>");
          pw.println("  </panel>");
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
    }

    public void loadFields(NodeList reclist) {
	Element elem;
	for (int l = 0; l < reclist.getLength(); l++) {
	  try {
	    Node recNode = reclist.item(l);
	    if (recNode.getNodeType() == Node.ELEMENT_NODE) {
	      Element recElmnt = (Element)recNode;
	      NodeList recnameList = recElmnt.getElementsByTagName("name");
	      elem = (Element)recnameList.item(0);
	      String recName = elem.getFirstChild().getNodeValue().trim();
	      NodeList recvalList = recElmnt.getElementsByTagName("value");
	      elem = (Element)recvalList.item(0);
	      String recVal = elem.getFirstChild().getNodeValue().trim();
	      if (recName.equals("username")) {
		usernameField.setText(recVal);
	      } else if (recName.equals("password")) {
		passwordField.setText(recVal);
	      }
	    }
	  } catch (Exception e) {
	    System.out.println(e.toString());
	  }
	}
	loginButton.doClick();
    }


  protected class SubsysHeartThread extends Thread {
    protected boolean _shutdown = false;
    private String _className = getClass().getName();
    protected int _sleepPeriod = 2000;

    public SubsysHeartThread() {}

    public void shutdown() { _shutdown = true; }

    public void run() {
      while (!_shutdown) {
        try {
          Thread.sleep(_sleepPeriod);
	  updateState();
        } catch (InterruptedException e) { }
      }
    } /* End run */
  } /* End SubsysHeartThread */

} //end of class JPanelLogin
