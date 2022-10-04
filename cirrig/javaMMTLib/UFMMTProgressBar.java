package javaMMTLib;
/**
 * Title:        UFMMTProgressBar.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JProgressBar 
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.*;
import java.util.*;
import javaUFLib.*;

public class UFMMTProgressBar extends JProgressBar implements UFMMTComponent {
    public static final String rcsID = "$Name:  $ $Id: UFMMTProgressBar.java,v 1.2 2011/05/02 21:16:51 warner Exp $";
    
    String recName = null;
    long timeStamp = 0;
    String outVal = "", health = "", mess = "";

    UFGUIRecord contextRec = null;
    boolean marked = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;
    int outLevel = 0;
    String prefix, suffix;

    public UFMMTProgressBar(String name) {
      this(name, 0, "", "");
    }

    public UFMMTProgressBar(String name, int outLevel, String prefix, String suffix) {
      super();
      setMinimum(0);
      setValue(0);
      setString("");
      setStringPainted(true);
      recName = name;
      timeStamp = 0;
      shadows = new Vector(1);
      clearRecs = new Vector(1);

      this.outLevel = outLevel;
      this.prefix = prefix;
      this.suffix = suffix;
    }


//-------------------------------------------------------------------------------


    private void setToolTipText() {
	String tip = "<html>"+recName + " = " + outVal;
        if (!health.equals("")) tip += "<br>Health: "+health;
        if (!mess.equals("")) tip += "<brMessage: "+mess;
	tip += "</html>";
	this.setToolTipText(tip);
    }


//-------------------------------------------------------------------------------

    public long getTimeStamp() { return timeStamp; }

//-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTProgressBar) {
        shadows.add(shadow);
      }
    }

//-------------------------------------------------------------------------------

    public String getVal() {
      return outVal;
    }

    public void setValue(String value) {
      outVal = value.trim();
      int val = 0, maxVal = 0;
      try {
	val = Integer.parseInt(outVal);
      } catch (NumberFormatException nfe) { }
      if (contextRec != null) {
        String context = contextRec.getValue();
	try {
	  maxVal = Integer.parseInt(context);
	} catch (NumberFormatException nfe) { }
	if (maxVal > 0) setMaximum(maxVal);
      }

      setValue(val);
      if (outLevel == 0) {
	setString(outVal);
      } else if (outLevel == 1) {
	setString(prefix+" "+outVal+" "+suffix);
      } else if (outLevel == 2) {
	setString(prefix+" "+outVal+" of "+getMaximum()+" "+suffix);
      }

      timeStamp = System.currentTimeMillis();
      setToolTipText();
    }

    public void registerComponent(UFGUIRecord guiRec, boolean isInput) {
      if (isInput) {
        guiRec.addInComponent(this);
      } else {
	guiRec.addOutComponent(this);
	setValue(guiRec.getValue());
      }
    }

    public void registerComponent(LinkedHashMap <String, UFGUIRecord> database, boolean isInput) {
      synchronized(database) {
	UFGUIRecord guiRec = null;
	if (database.containsKey(recName)) {
	  guiRec = (UFGUIRecord)database.get(recName);
	} else {
	  guiRec = new UFGUIRecord(recName);
	  database.put(recName, guiRec);
	}
	registerComponent(guiRec, isInput);
      }
    }

    public void registerContext(LinkedHashMap <String, UFGUIRecord> database, String contextName) {
      synchronized(database) {
        contextRec = null;
        if (database.containsKey(contextName)) {
          contextRec = (UFGUIRecord)database.get(contextName);
        } else {
          contextRec = new UFGUIRecord(contextName);
          database.put(contextName, contextRec);
        }
      }
      contextRec.addUpdateComponent(this);
    }

    public void registerClearRec(LinkedHashMap <String, UFGUIRecord> database, String clearName) {
      synchronized(database) {
	UFGUIRecord clearRec = null;
        if (database.containsKey(clearName)) {
          clearRec = (UFGUIRecord)database.get(clearName);
        } else {
          clearRec = new UFGUIRecord(clearName);
          database.put(clearName, clearRec);
        }
	clearRecs.add(clearRec);
      }
    }

    public boolean isMarked() {
      return marked;
    }

    public void mark() {
      marked = true;
    }

    public void clear() {
      marked = false;
    }

    public void apply() {
      marked = false;
    }

    public String getCommandValue() {
      //Progress bars have no command to send
      return null;
    }

    public void setCommand(String command) {
      this.command = command;
    }

    public void setHealthAndMess(String health, String mess) {
      this.health = health;
      this.mess = mess;
    }

} 

