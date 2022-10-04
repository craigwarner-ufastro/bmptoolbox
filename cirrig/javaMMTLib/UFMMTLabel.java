package javaMMTLib;
/**
 * Title:        UFMMTLabel.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JLabel 
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.*;
import java.util.*;
import javaUFLib.*;

public class UFMMTLabel extends JLabel implements UFMMTComponent {
    public static final String rcsID = "$Name:  $ $Id: UFMMTLabel.java,v 1.10 2019/12/13 21:33:53 pi Exp $";

    String recName = null;
    long timeStamp = 0;
    String outVal = "", health = "", mess = "";
    int parse=0;
    float unitFactor=1;
    HashMap displays, colors, healthColors;
    int maxdec = -1;

    boolean marked = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;

    UFGUIRecord contextRec = null;
    String[] contextVals;
    Color[] contextColors;

    public UFMMTLabel(String name) {
      recName = name;
      timeStamp = 0;
      displays = new HashMap();
      colors = new HashMap();
      healthColors = new HashMap();
      shadows = new Vector(1);
      clearRecs = new Vector(1);

      setForeground(new JTextField().getForeground());
      setHorizontalAlignment(JLabel.LEFT);
      setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

//-------------------------------------------------------------------------------


    private void setToolTipText() {
	String tip = "<html>"+recName + " = " + outVal;
	if (!health.equals("")) tip += "<br>Health: "+health;
        if (!mess.equals("")) tip += "<br>Message: "+mess;
	tip += "</html>";
	this.setToolTipText(tip);
    }


//-------------------------------------------------------------------------------

    public long getTimeStamp() { return timeStamp; }

    public void setTokenNum(int tokenNum) {
	parse = tokenNum;
    }


//-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTLabel) {
        shadows.add(shadow);
      }
    }

//-------------------------------------------------------------------------------

    public String getVal() {
      return outVal;
    }

    public void setValue(String value) {
      boolean inError = false;
      if (this.getText().indexOf("WARN") >= 0 || this.getText().indexOf("ERR") >= 0) inError = true;
      outVal = value.trim();
      if (parse != 0) {
	StringTokenizer st = new StringTokenizer(outVal,",");
	String stemp = null;
	int tokens = 0;
	while (tokens < parse && st.hasMoreElements()) {
	  stemp = st.nextToken();
	  tokens++;
	}
	if (stemp != null) outVal = stemp;
      }
      if (unitFactor != 1) {
	try {
	  double tempd = Double.parseDouble(outVal);
	  outVal = ""+UFLabel.truncFormat((unitFactor*tempd),1,5);
	} catch(Exception e) {}
      }
      if (healthColors.containsKey(this.health)) this.setForeground((Color)healthColors.get(this.health));
      if (colors.containsKey(outVal)) this.setForeground((Color)colors.get(outVal));
      if (displays.containsKey(outVal)) outVal = (String)displays.get(outVal);
      if (contextRec != null) {
	String context = contextRec.getValue();
	for (int j = 0; j < contextVals.length; j++) {
	  if (context.equals(contextVals[j])) {
	    this.setForeground(contextColors[j]);
	    break;
	  }
	}
      }
      if (maxdec >= 0 && outVal.indexOf(".") != -1 && outVal.length() > outVal.indexOf(".")+maxdec) {
        outVal = outVal.substring(0, outVal.indexOf(".")+maxdec+1);
      }
      if (outVal.startsWith("<html>")) {
	this.setText(outVal);
      } else this.setText("  " + outVal + "   ");

      if (this.getText().indexOf("WARN") >= 0 && !inError) Toolkit.getDefaultToolkit().beep();
      if (this.getText().indexOf("ERR") >= 0 && !inError) {
	Toolkit.getDefaultToolkit().beep();
	Toolkit.getDefaultToolkit().beep();
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

    public void registerContext(LinkedHashMap <String, UFGUIRecord> database, String contextName, String[] vals, Color[] colors) { 
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
      contextVals = vals;
      contextColors = colors;
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
      //Labels have no command to send
      return null;
    }

    public void setCommand(String command) {
      this.command = command;
    }

    public void setHealthAndMess(String health, String mess) {
      this.health = health;
      this.mess = mess;
    }       
    
//-------------------------------------------------------------------------------

  public void updateFactor(float newFactor) {
    try {
	double tempd = Double.parseDouble(this.getText());
	setText(UFLabel.truncFormat((tempd*newFactor/unitFactor),1,5)+"   ");
    } catch(Exception e) {}
    this.unitFactor = newFactor;
  }

  public void addDisplayValue(String input, String display) {
    displays.put(input, display);
  }

  public void addDisplayValue(String input, String display, Color color) {
    displays.put(input, display);
    colors.put(input, color);
  }

  public void addHealthColor(String health, Color color) {
    healthColors.put(health, color);
  }

  public void setMaxDecimals(int dec) {
    this.maxdec = dec;
  }

} 

