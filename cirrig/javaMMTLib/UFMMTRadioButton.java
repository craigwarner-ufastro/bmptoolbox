package javaMMTLib;
/**
 * Title:        UFMMTRadioButton.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JRadioButton
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.JTextField;
import javax.swing.*;
import javaUFLib.*;
import java.util.*;

public class UFMMTRadioButton extends JRadioButton implements UFMMTComponent { 

    public static final String rcsID = "$Name:  $ $Id: UFMMTRadioButton.java,v 1.5 2011/05/02 21:16:51 warner Exp $";

    String recName = null;
    String outVal = "", health = "", mess = "";

    String markVal = "";
    String clearVal = "";

    boolean marked = false;
    boolean prevState = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;
    Color backgroundColor = new JRadioButton().getBackground();

    boolean buttonBehavior = true;

    public UFMMTRadioButton(String name, boolean btnBhv, String valToMark, boolean checked) {
      recName = name;
      markVal = valToMark;
      buttonBehavior = btnBhv;
      shadows = new Vector(1);
      clearRecs = new Vector(1);
      setSelected(checked);
      prevState = checked;
      if (checked) outVal = markVal; else outVal = clearVal;
      setToolTipText();
      addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
          if (isSelected()) {
	    if (!marked || !buttonBehavior) {
	      outVal = markVal;
	      mark();
            } else if (marked && buttonBehavior) {
	      outVal = clearVal;
	      mark();
	      clear();
	      setSelected(false);
	    }
          } else {
            outVal = clearVal;
            mark();
            clear();
            setSelected(false);
	  }
          setToolTipText();
        }
      });
    }

    public UFMMTRadioButton(String name, boolean btnBhv, String valToMark) {
      this(name, btnBhv, valToMark, false);
    }

    public UFMMTRadioButton(String name,  String valToMark, boolean checked) {
      this(name, true, valToMark, checked);
    }

    public UFMMTRadioButton(String name, String valToMark) {
      this(name, true, valToMark, false);
    }

    //-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTRadioButton) {
        shadows.add(shadow);
      }
    }

    //-------------------------------------------------------------------------------

    public String getVal() {
      return outVal;
    }

    public void setValue(String value) {
      if (value.trim().equals(markVal)) {
        setSelected(true);
        outVal = value;
      } else { 
        setSelected(false);
        outVal = value;
      } 
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
      setBackground(Color.yellow);
      for (int j = 0; j < shadows.size(); j++) {
        UFMMTRadioButton shadow = (UFMMTRadioButton)shadows.get(j);
        shadow.clear();
        shadow.setValue(getVal());
      }
      for (int j = 0; j < clearRecs.size(); j++) {
        UFGUIRecord clearRec = (UFGUIRecord)clearRecs.elementAt(j);
        clearRec.clear();
      }
    }

    public void clear() {
      marked = false;
      setSelected(prevState);
      setBackground(backgroundColor);
    }

    public void apply() {
      marked = false;
      if (buttonBehavior) {
	clear();
      } else prevState = isSelected();
      setBackground(backgroundColor);
    }

    public String getCommandValue() {
      return command+outVal;
    }

    public void setCommand(String command) {
      this.command = command;
    }

    public void setHealthAndMess(String health, String mess) {
      this.health = health;
      this.mess = mess;
    }

    //------------------------------------------------------------------

  public void setBackgroundColor(Color c) {
    backgroundColor = c;
    setBackground(c);
  }

  public void updateValToMark(String valToMark) {
    markVal = valToMark;
  }

  private void setToolTipText() {
    String tip = "<html>"+recName + " = " + outVal;
    if (!health.equals("")) tip += "<br>Health: "+health;
    if (!mess.equals("")) tip += "<brMessage: "+mess;
    tip += "</html>";
    this.setToolTipText(tip);
  }


}
