package cjec;

/**
 * Title:        MJECButton.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Buttons for MJEC 
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
import javaUFLib.*;
import javaUFProtocol.*;
import javaMMTLib.*;

public class MJECButton extends UFColorButton {

    public static final
        String rcsID = "$Name:  $ $Id: MJECButton.java,v 1.1 2010/06/25 18:39:41 warner Exp $";

    String recName = null;
    String command = null;

    protected CjecFrame cjec;
    Vector <UFGUIRecord> markedVec;
    protected String title;
    protected int COLOR_SCHEME;
    protected boolean doApply, applyFirst;
    protected boolean showPopup = false;

    public MJECButton(String name, String theTitle, String command, int COLOR_SCHEME, CjecFrame cjecFrame) {
      super(theTitle, COLOR_SCHEME);
      title = theTitle;
      recName = name;
      cjec = cjecFrame;
      this.command = command;
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          doApply = true;
	  if (showPopup) {
	    Object[] popupOptions = {title,"Cancel"};
            int n = JOptionPane.showOptionDialog(new JFrame(), "Are you sure you want to "+title, "Perform "+title+"?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, popupOptions, popupOptions[1]);
	    if (n != 0) {
	      System.out.println("Cancelling "+title+"...");
	      return;
	    }
	  }
          checkForMarkedComponents();
	  if (applyFirst) {
	    cjec.apply();
	  }
          if (doApply) {
            cjec.apply(MJECButton.this);
          }
        }
      });
      setToolTipText();
      cjec.registerButton(this);
    }

    public MJECButton(String name, String title, String command, int COLOR_SCHEME, CjecFrame cjecFrame, boolean showPopup) {
      this(name, title, command, COLOR_SCHEME, cjecFrame);
      this.showPopup = showPopup;
    }


    public String getCommand() { return command; }
    public String getRecName() { return recName; }

    public void setCommand(String command) {
      this.command = command;
      setToolTipText();
    }

    public void setPopup(boolean showPopup) {
      this.showPopup = showPopup;
    }

    public void checkForMarkedComponents() {
      applyFirst = false;
      markedVec = new Vector();
      String key;
      UFGUIRecord guiRec;
      for (Iterator i = cjec.database.keySet().iterator(); i.hasNext(); ) {
	key = (String)i.next();
	guiRec = (UFGUIRecord)cjec.database.get(key);
	if (guiRec.isMarked()) markedVec.add(guiRec);
      }
      if (markedVec.size() == 0) return;
      JPanel dialogPanel = new JPanel(new RatioLayout());
      final JList dialogList = new JList(markedVec);
      JScrollPane jsp = new JScrollPane(dialogList);
      final JDialog jd = new JDialog();

      JButton putAndApplyButton = new JButton("Put values then apply "+title);
      putAndApplyButton.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
	  applyFirst = true;
	  jd.setVisible(false);
        }
      });

      JButton clearApplyButton = new JButton("Clear fields and apply "+title);
      clearApplyButton.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
          for (int j = 0; j < markedVec.size(); j++) {
            UFGUIRecord guiRec = (UFGUIRecord)markedVec.elementAt(j);
	    guiRec.clear();
          }
          jd.setVisible(false);
	}
      });

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
	  doApply = false;
          jd.setVisible(false);
	}
      });

      dialogPanel.add("0.01,0.01;0.99,0.10",new JLabel("Oh bugger! You have marked some fields without applying the values..."));
      dialogPanel.add("0.01,0.21;0.99,0.50",jsp);
      dialogPanel.add("0.01,0.81;0.49,0.10",putAndApplyButton);
      dialogPanel.add("0.51,0.81;0.49,0.10",clearApplyButton);
      dialogPanel.add("0.51,0.91;0.49,0.10",cancelButton);

      jd.setContentPane(dialogPanel);
      jd.setModal(true);
      jd.setAlwaysOnTop(true);
      jd.setSize(500,400);
      jd.setVisible(true);
      jd.dispose();
      markedVec = null;
    }

  private void setToolTipText() {
    String tip = "<html>"+recName + " = " + command;
    tip += "</html>";
    this.setToolTipText(tip);
  }
}
