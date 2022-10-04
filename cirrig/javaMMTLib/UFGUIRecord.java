package javaMMTLib;

import java.io.*;
import java.util.Vector;
import javax.swing.JPanel;
import java.awt.Component;

public class UFGUIRecord {
    public static final
        String rcsID = "$Name:  $ $Id: UFGUIRecord.java,v 1.9 2016/04/25 19:47:47 warner Exp $";

    protected UFRecord rec;
    protected Vector <UFMMTComponent> outComponents;
    protected Vector <UFMMTComponent> inComponents;
    protected Vector <UFMMTComponent> updateComponents;

    public UFGUIRecord(UFRecord rec) {
      this.rec = rec;
      outComponents = new Vector(2);
      inComponents = new Vector(2);
      updateComponents = new Vector(2);
    }

    public UFGUIRecord(String name) {
      rec = new UFRecord(name, "");
      outComponents = new Vector(2);
      inComponents = new Vector(2);
      updateComponents = new Vector(2);
    }

    public String getName() {
      return rec.name();
    }

    public String getValue() {
      return rec.getValue();
    }

    public void updateRecord(UFRecord newRec) {
      rec = newRec;
      for (int j = 0; j < outComponents.size(); j++) {
	UFMMTComponent component = (UFMMTComponent)outComponents.elementAt(j);
        component.setHealthAndMess(rec.getHealth(), rec.getMess());
	if (component instanceof UFMMTIntegratedComboBox) {
	  UFMMTIntegratedComboBox cb = (UFMMTIntegratedComboBox)component;
          cb.setValueWithoutMarking(rec.getValue());
	} else {
	  component.setValue(rec.getValue());
        }
      }
      for (int j = 0; j < updateComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)updateComponents.elementAt(j);
	component.setValue(component.getVal());
      }
      for (int j = 0; j < inComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
	if (component instanceof UFMMTComboBox) {
	  UFMMTComboBox cb = (UFMMTComboBox)component;
	  if (!isMarked()) {
	    cb.prevVal = rec.getValue();
	    cb.clear();
	  }
	} else if (component instanceof UFMMTIntegratedComboBox) {
          UFMMTIntegratedComboBox cb = (UFMMTIntegratedComboBox)component;
          if (!isMarked()) {
            cb.prevVal = rec.getValue();
            cb.clear();
          }
	}
      }
    }

    public void addOutComponent(UFMMTComponent component) {
      outComponents.add(component);
    }

    public void addInComponent(UFMMTComponent component) {
      inComponents.add(component);
      if (inComponents.size() > 1) {
	for (int j = 0; j < inComponents.size(); j++) {
	  UFMMTComponent currComp = (UFMMTComponent)inComponents.elementAt(j);
	  currComp.clearShadows();
	  for (int l = 0; l < inComponents.size(); l++) {
	    if (l != j) currComp.addShadow((UFMMTComponent)inComponents.elementAt(l));
	  }
	}
      }
    }

    public void addUpdateComponent(UFMMTComponent component) {
      updateComponents.add(component);
    }


    public boolean isChildOf(JPanel parentPanel) {
      for (int j = 0; j < inComponents.size(); j++) {
        Component component = (Component)inComponents.elementAt(j);
        if (parentPanel.isAncestorOf(component)) return true;
      }
      return false;
    }

    public boolean isMarked() {
      for (int j = 0; j < inComponents.size(); j++) {
	UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
	if (component.isMarked()) return true;
      }
      return false;
    }

    public void clear() {
      for (int j = 0; j < inComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
        component.clear(); 
      }
    }

    public void apply() {
      for (int j = 0; j < inComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
        component.apply();
      }
    }

    public void mark(int i) {
      if (inComponents.size() > i) {
	UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(i);
	component.mark();
      }
    }

    public void mark() {
      mark(0);
    }

    public String getInValue(int i) {
      if (inComponents.size() > i) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(i);
	return component.getVal();
      }
      return null;
    }

    public String getInValue() {
      return getInValue(0);
    }

    public void setInValue(int i, String val) {
      if (inComponents.size() > i) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(i);
        component.setValue(val);
      }
    }

    public void setInValue(String val) {
      setInValue(0, val);
    }

    public String getCommandValue() {
      //in components will be mirrored so just get first one
      if (inComponents.size() == 0) return null;
      UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(0);
      return component.getCommandValue();
    }

    public int blueIndex() { 
      int blue = -1;
      for (int j = 0; j < inComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
	if (component instanceof UFMMTTextField) {
	  UFMMTTextField field = (UFMMTTextField)component;
	  if (field.isBlue()) blue = j; 
	} else if (component instanceof UFMMTIntegratedTextField) {
	  UFMMTIntegratedTextField field = (UFMMTIntegratedTextField)component;
          if (field.isBlue()) blue = j;
	} else if (component instanceof UFMMTPasswordField) {
	  UFMMTPasswordField field = (UFMMTPasswordField)component;
	  if (field.isBlue()) blue = j;
	}
      }
      return blue;
    }

    public void setEnabled(boolean enabled) {
      for (int j = 0; j < inComponents.size(); j++) {
        UFMMTComponent component = (UFMMTComponent)inComponents.elementAt(j);
        component.setEnabled(enabled);
      }
    }

    public String toString() {
      String s = getName()+" = ";
      if (blueIndex() != -1) {
	s += getInValue(blueIndex());
      } else if (isMarked()) {
	s += getInValue();
      } else s+= getValue();
      return s;
    }
}
