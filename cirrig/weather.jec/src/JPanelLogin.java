package wthjec;

/**
 * Title:        JPanelLogin.java
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
    WthjecFrame wthjec;
    JTextField usernameField;
    JPasswordField passwordField;
    JLabel statusLabel;
    UFColorButton loginButton, logoutButton, refreshButton;

    SubsysHeartThread _thread;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelLogin(WthjecFrame wthjecFrame) {
	this.setLayout(new RatioLayout());
        wthjec = wthjecFrame;
        setPreferredSize(new Dimension(648,472));
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
		wthjec.apply(commandVec);
		updateState();
	    }
	});

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                logoutButton.setEnabled(false);
                String logoutString = "LOGOUT::"+wthjec.uid+" "+wthjec.sid;
                Vector <String> commandVec = new Vector();
                commandVec.add(logoutString);
                wthjec.apply(commandVec);
                updateState();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                refreshButton.setEnabled(false);
                String refreshString = "REFRESH::"+wthjec.uid+" "+wthjec.sid;
                Vector <String> commandVec = new Vector();
                commandVec.add(refreshString);
                wthjec.apply(commandVec);
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

        add("0.01,0.01;0.64,0.49",loginPanel);
	updateState();

	_thread = new SubsysHeartThread();
	_thread.start();
    } 

    protected String getRestoreString() {
      String passwd = "none";
      try {
          passwd = AeSimpleSHA1.SHA1(new String(passwordField.getPassword()));
      } catch (Exception e) {
          System.out.println(_mainClass+"::getLoginString> SHA1 Error: "+e.toString());
          System.out.println(_mainClass+"::getLoginString> Password can't be sent!");
          return null;
      }
      String loginString = "RESTORE_SESSION::"+usernameField.getText()+" "+passwd;
      return loginString;
    }

    protected void updateState() {
	boolean isLoggedIn = wthjec.isLoggedIn;
	usernameField.setEnabled(!isLoggedIn);
	passwordField.setEnabled(!isLoggedIn);
	loginButton.setEnabled(!isLoggedIn);
	refreshButton.setEnabled(isLoggedIn);
	logoutButton.setEnabled(isLoggedIn);
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
	      if (recName.equals("username")) {
		usernameField.setText(recVal);
	      } else if (recName.equals("password")) {
		passwordField.setText(recVal);
	      }
	    }
	  } catch (Exception e) {
	    e.printStackTrace();
	    System.err.println(_mainClass+"::loadFields> "+e.toString()+" "+recNode.toString());
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
