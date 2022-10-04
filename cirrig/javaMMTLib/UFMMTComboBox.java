package javaMMTLib;
/**
 * Title:        UFMMTComboBox.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JComboBox 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javaUFLib.*;
import java.util.*;

public class UFMMTComboBox extends JComboBox implements UFMMTComponent { 
    public static final String rcsID = "$Name:  $ $Id: UFMMTComboBox.java,v 1.9 2014/02/25 22:55:11 warner Exp $";

    public static final int INDEX = 0;
    public static final int ITEM = 1;
    public static final int REFERENCE = 2;
    public static final int RECORD = 3;

    int behavior = ITEM; 
  
    String recName = null;
    long timeStamp = 0;
    String outVal = "", health="", mess = "";

    String prevVal = "";
    Vector <String> refs;
    Vector <UFGUIRecord> records;
    boolean marked = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;
    Color background;

    String currSelected = null;
    boolean _popupCanceled = false;

    public UFMMTComboBox(String name, String[] items, String[] refVals, int bhv) {
      recName = name;
      timeStamp = 0;
      shadows = new Vector(1);
      clearRecs = new Vector(1);
      background = getBackground();
      behavior = bhv;

      if (items != null) {
	refs = new Vector(items.length);
	for (int i=0; i<items.length; i++) {
	  addItem(items[i].trim());
	  if (refVals != null && refVals.length > i) {
	    refs.add(refVals[i].trim());
	  } else refs.add(items[i].trim());
	}
      }

      if (behavior == RECORD && refVals != null) {
	refs = new Vector(refVals.length);
	records = new Vector(refVals.length);
	/* refs holds record names.  record values will go in items
	 * when registerContext is called */
	for (int i = 0; i < refVals.length; i++) {
	  refs.add(refVals[i].trim());
	}
      }

      if (behavior == INDEX) {
	outVal = ""+getSelectedIndex();
      } else if (behavior == ITEM) {
	outVal = (String)getSelectedItem();
      } else if (behavior == REFERENCE) {
	outVal = (String)refs.elementAt(getSelectedIndex());
      }
      prevVal = outVal;
      setToolTipText();
      addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
	  if (ae.getActionCommand().equals("updating")) return;
	  if (behavior == INDEX) {
	    outVal = ""+getSelectedIndex();
	  } else if (behavior == ITEM) {
	    outVal = (String)getSelectedItem();
	  } else if (behavior == REFERENCE) {
	    outVal = (String)refs.elementAt(getSelectedIndex());
	  } else if (behavior == RECORD) {
	    outVal = (String)getSelectedItem();
	  }
	  if (!ae.getActionCommand().equals("shadowing")) mark();
          setToolTipText();
        }
      });

      addPopupMenuListener(new PopupMenuListener() {
	public void popupMenuCanceled(PopupMenuEvent e) {
	  _popupCanceled = true;
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	  if (!_popupCanceled && currSelected.equals((String)getSelectedItem())) fireActionEvent();
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	  currSelected = (String)getSelectedItem();
	  _popupCanceled = false;
	}
      });
    }

    public UFMMTComboBox(String name, String[] items, String[] refVals, int bhv, String selected) {
      this(name, items, refVals, bhv);
      if (selected != null) {
        if (behavior == ITEM || behavior == INDEX) {
          setSelectedItem(selected);
        } else if (behavior == REFERENCE && refs.indexOf(selected) != -1) {
          setSelectedIndex(refs.indexOf(selected));
        }
      }
      apply();
      setToolTipText();
    }

    public UFMMTComboBox(String name, String[] items, String[] refVals, int bhv, int selected) {
      this(name, items, refVals, bhv);
      setSelectedIndex(selected);
      apply();
      setToolTipText();
    }

    public UFMMTComboBox(String name, String[] items, int behavior) {
      this(name, items, null, behavior);
    }

    public UFMMTComboBox(String name, String[] items, int behavior, String selected) {
      this(name, items, null, behavior, selected);
    }

    public UFMMTComboBox(String name, String[] items, int behavior, int selected) {
      this(name, items, null, behavior, selected);
    }

    public UFMMTComboBox(String name, String[] items) {
      this(name, items, null, ITEM);
    }

    public UFMMTComboBox(String name, String[] items, String selected) {
      this(name, items, null, ITEM, selected);
    }

    public UFMMTComboBox(String name, Vector<String> items, Vector<String> refVals, int behavior) {
      this(name, Arrays.copyOf(items.toArray(), items.size(), String[].class), Arrays.copyOf(refVals.toArray(), refVals.size(), String[].class), behavior);
      //this(name, (String[])(items.toArray()), (String[])(refVals.toArray()), behavior);
    }

    public UFMMTComboBox(String name, Vector<String> items, Vector<String> refVals, int behavior, String selected) {
      this(name, (String[])(items.toArray()), (String[])(refVals.toArray()), behavior, selected);
    }

    public UFMMTComboBox(String name, Vector<String> items, Vector<String> refVals, int behavior, int selected) {
      this(name, (String[])(items.toArray()), (String[])(refVals.toArray()), behavior, selected);
    }

    public UFMMTComboBox(String name, Vector<String> items, int behavior) {
      this(name, (String[])(items.toArray()), behavior);
    }

    public UFMMTComboBox(String name, Vector<String> items, int behavior, String selected) {
      this(name, (String[])(items.toArray()), behavior, selected);
    }

    public UFMMTComboBox(String name, Vector<String> items, int behavior, int selected) {
      this(name, (String[])(items.toArray()), behavior, selected);
    }

    public UFMMTComboBox(String name, Vector<String> items) {
      this(name, (String[])(items.toArray()));
    }

    public UFMMTComboBox(String name, Vector<String> items, String selected) {
      this(name, (String[])(items.toArray()), selected);
    }


    public long getTimeStamp () { return timeStamp; }

    public void updateArrays(String[] items, String[] refVals) {
      String ac = getActionCommand();
      setActionCommand("updating");
/*
      while (getItemCount() > 0) {
	removeItemAt(0);
      }
*/
      removeAllItems();
      refs = new Vector(items.length);

      if (items != null) {
        for (int i=0; i<items.length; i++) {
          addItem(items[i].trim());
          if (refVals != null && refVals.length > i) {
            refs.add(refVals[i].trim());
          } else refs.add(items[i].trim());
        }
      }
      setActionCommand(ac);
    }
    
//-------------------------------------------------------------------------------
    public void setBehavior(int behavior) { this.behavior = behavior; }

    public void setRef(int idx, String ref) {
      refs.set(idx, ref);
    }

    public void setRef(String item, String ref) {
      for (int j = 0; j < getItemCount(); j++) {
        if (item.equals((String)getItemAt(j))) {
	  refs.set(j, ref);
	  break;
	}
      }
    }

    public void addNewItem(String item) {
      addItem(item.trim());
      refs.add(item.trim());
      for (int j = 0; j < shadows.size(); j++) {
        UFMMTComboBox shadow = (UFMMTComboBox)shadows.get(j);
        shadow.addNewShadowItem(item);
      }
    }

    public void addNewItem(String item, String ref) {
      addItem(item.trim());
      refs.add(ref.trim());
      for (int j = 0; j < shadows.size(); j++) {
        UFMMTComboBox shadow = (UFMMTComboBox)shadows.get(j);
	shadow.addNewShadowItem(item, ref);
      }
    }

    public void addNewShadowItem(String item) {
      addItem(item.trim());
      refs.add(item.trim());
    }

    public void addNewShadowItem(String item, String ref) {
      addItem(item.trim());
      refs.add(ref.trim());
    }


//-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTComboBox) {
        shadows.add(shadow);
      }
    }

    //-------------------------------------------------------------------------------

    public String getVal() {
      return outVal;
    }

    public void setValue(String value) {
      if (value.equals("null")) return;
      if (behavior == INDEX) {
	try {
	  int x = Integer.parseInt(value);
	  setSelectedIndex(x);
	  prevVal = value;
	  outVal = value;
	} catch(Exception e) {
	  System.out.println("UFMMTComboBox::setValue("+recName+")> Illegal index: "+value);
	  setBackground(Color.RED);
	}
      } else if (behavior == ITEM) {
	setSelectedItem(value);
	if (!((String)getSelectedItem()).equals(value)) {
          System.out.println("UFMMTComboBox::setValue("+recName+")> Illegal item: "+value);
          setBackground(Color.RED);
	} else {
	  prevVal = value;
	  outVal = value;
	}
      } else if (behavior == REFERENCE) {
	int idx = refs.indexOf(value);
	if (idx == -1) {
          System.out.println("UFMMTComboBox::setValue("+recName+")> Illegal value: "+value);
          setBackground(Color.RED);
	} else {
	  setSelectedIndex(idx);
          prevVal = value;
          outVal = value;
	}
      } else if (behavior == RECORD) {
	UFGUIRecord contextRec = null;
	for (int j = 0; j < records.size(); j++) {
	  contextRec = (UFGUIRecord)records.elementAt(j);
	  if (!contextRec.getValue().equals(getItemAt(j))) {
	    String ac = getActionCommand();
	    setActionCommand("shadowing");
	    int n = getSelectedIndex();
	    removeItemAt(j);
	    insertItemAt(contextRec.getValue(), j);
	    setSelectedIndex(n);
	    setActionCommand(ac);
	    return;
	  }
	}
	if (!((String)getSelectedItem()).equals(value)) setSelectedItem(value);
        if (!((String)getSelectedItem()).equals(value)) {
          System.out.println("UFMMTComboBox::setValue("+recName+")> Illegal item: "+value);
          setBackground(Color.RED);
        } else {
          prevVal = value;
          outVal = value;
        }
      }
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

    public void registerContext(LinkedHashMap <String, UFGUIRecord> database) {
      synchronized(database) {
	String contextName = null;
        UFGUIRecord contextRec = null;
	String ac = getActionCommand();
	setActionCommand("shadowing");
	for (int j = 0; j < refs.size(); j++) {
	  contextName = (String)refs.elementAt(j);
	  if (database.containsKey(contextName)) {
	    contextRec = (UFGUIRecord)database.get(contextName);
	  } else {
	    contextRec = new UFGUIRecord(contextName);
	    database.put(contextName, contextRec);
	  }
	  addItem(contextRec.getValue());
	  contextRec.addUpdateComponent(this);
	  records.add(contextRec);
	}
	setActionCommand(ac);
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
        UFMMTComboBox shadow = (UFMMTComboBox)shadows.get(j);
	String ac = shadow.getActionCommand();
	shadow.setActionCommand("shadowing");
        shadow.clear();
        shadow.setValue(getVal());
	shadow.setActionCommand(ac);
      }
      for (int j = 0; j < clearRecs.size(); j++) {
        UFGUIRecord clearRec = (UFGUIRecord)clearRecs.elementAt(j);
        clearRec.clear();
      }
    }

    public void clear() {
      marked = false;
      String ac = getActionCommand();
      setActionCommand("shadowing");
      setValue(prevVal);
      setActionCommand(ac);
      setBackground(background);
    }

    public void apply() {
      marked = false;
      prevVal = getVal();
      setBackground(background);
    }

    public String getCommandValue() {
      return command+getVal();
    }

    public void setCommand(String command) {
      this.command = command;
    }

    public void setHealthAndMess(String health, String mess) {
      this.health = health;
      this.mess = mess;
    }

  private void setToolTipText() {
    String tip = "<html>"+recName + " = " + outVal;
    if (!health.equals("")) tip += "<br>Health: "+health;
    if (!mess.equals("")) tip += "<brMessage: "+mess;
    tip += "</html>";
    this.setToolTipText(tip);
  }

} //end of class UFMMTComboBox
