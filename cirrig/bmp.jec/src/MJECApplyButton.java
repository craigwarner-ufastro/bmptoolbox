package bmpjec;

/**
 * Title:        MJECApplyButton.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Apply button for MJEC 
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

public class MJECApplyButton extends UFColorButton {

    public static final
        String rcsID = "$Name:  $ $Id: MJECApplyButton.java,v 1.1 2010/05/20 21:12:55 warner Exp $";

    protected MjecFrame mjec;
    Vector <UFGUIRecord> blueVec;
    protected String title = "Apply";
    protected int COLOR_SCHEME = UFColorButton.COLOR_SCHEME_GREEN; 
    protected boolean doApply;

    public MJECApplyButton(MjecFrame mjecFrame) {
      this(mjecFrame, "Apply", UFColorButton.COLOR_SCHEME_GREEN);
    }

    public MJECApplyButton(MjecFrame mjecFrame, String title, int COLOR_SCHEME) {
      super(title, COLOR_SCHEME);
      doApply = true;
      mjec = mjecFrame;
      addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
	  doApply = true;
	  checkForBlueComponents();
	  if (doApply) {
	    mjec.apply(MJECApplyButton.this);
	    setEnabled(true);
	  }
	}
      });
    }

    public void checkForBlueComponents() {
      blueVec = new Vector();
      String key;
      UFGUIRecord guiRec;
      for (Iterator i = mjec.database.keySet().iterator(); i.hasNext(); ) {
	key = (String)i.next();
	guiRec = (UFGUIRecord)mjec.database.get(key);
	if (guiRec.blueIndex() != -1) blueVec.add(guiRec);
      }
      if (blueVec.size() == 0) return;
      JPanel dialogPanel = new JPanel(new RatioLayout());
      final JList dialogList = new JList(blueVec);
      JScrollPane jsp = new JScrollPane(dialogList);
      final JDialog jd = new JDialog();

      JButton putAndApplyButton = new JButton("Put selected values and Apply");
      putAndApplyButton.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
	  for (int j = 0; j < blueVec.size(); j++) {
	    UFGUIRecord guiRec = (UFGUIRecord)blueVec.elementAt(j);
	    int blue = guiRec.blueIndex();
	    if (blue != -1) {
	      guiRec.setInValue(blue, guiRec.getInValue(blue));
	      guiRec.mark(blue);
	    }
	  }
	  jd.setVisible(false);
        }
      });

      JButton clearApplyButton = new JButton("Clear these fields and Apply");
      clearApplyButton.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
          for (int j = 0; j < blueVec.size(); j++) {
            UFGUIRecord guiRec = (UFGUIRecord)blueVec.elementAt(j);
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

      dialogPanel.add("0.01,0.01;0.99,0.10",new JLabel("Oh bugger! You have edited some fields without setting the values..."));
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
      blueVec = null;
    }
}
