package javaMMTLib;
/**
 * Title:        UFMMTCheckBox.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JCheckBox
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.JTextField;
import javax.swing.*;
import javaUFLib.*;
import java.util.*;

public class UFMMTCheckBox extends JCheckBox implements UFMMTComponent {

    public static final String rcsID = "$Name:  $ $Id: UFMMTCheckBox.java,v 1.4 2011/05/02 21:16:51 warner Exp $";
    
    String recName = null;
    String outVal = "", health="", mess="";

    String markVal = "";
    String clearVal = "";

    boolean marked = false;
    boolean prevState = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;

    public UFMMTCheckBox(String name,  String valToMark,  String valToClear, boolean checked) {
      recName = name;
      markVal = valToMark;
      clearVal = valToClear;
      shadows = new Vector(1);
      clearRecs = new Vector(1);
      setSelected(checked);
      prevState = checked;
      if (checked) outVal = markVal; else outVal = clearVal;
      setToolTipText();
      addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent ae) {
	  if (isSelected()) {
	    outVal = markVal;
	  } else {
	    outVal = clearVal;
	  }
	  mark();
	  setToolTipText();
	}
      });
    }

    public UFMMTCheckBox(String name,  String valToMark,  String valToClear) {
      this(name, valToMark, valToClear, false);
    }

    public UFMMTCheckBox(String name, boolean checked) {
      this(name, "1", "0", checked);
    }

    public UFMMTCheckBox(String name) {
      this(name, "1", "0", false);
    }


    //-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTCheckBox) {
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
      } else if (value.trim().equals(clearVal)) {
	setSelected(false);
	outVal = value;
      } else {
	setBackground(Color.RED);
	System.out.println("UFMMTCheckBox::setValue> Unmatched value for "+recName+": "+value);
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
        UFMMTCheckBox shadow = (UFMMTCheckBox)shadows.get(j);
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
      setBackground(new JCheckBox().getBackground());
    }

    public void apply() {
      marked = false;
      prevState = isSelected();
      setBackground(new JCheckBox().getBackground());
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
    

  private void setToolTipText() {
    String tip = "<html>"+recName + " = " + outVal;
    if (!health.equals("")) tip += "<br>Health: "+health;
    if (!mess.equals("")) tip += "<brMessage: "+mess;
    tip += "</html>";
    this.setToolTipText(tip);
  }

}
