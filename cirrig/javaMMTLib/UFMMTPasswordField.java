package javaMMTLib;
/**
 * Title:        UFMMTPasswordField.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends JPasswordField
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPasswordField;
import javax.swing.*;
import javaUFLib.*;
import java.util.*;

public class UFMMTPasswordField extends JPasswordField implements UFMMTComponent, FocusListener, KeyListener, MouseListener {
    public static final String rcsID = "$Name:  $ $Id: UFMMTPasswordField.java,v 1.2 2011/05/02 21:16:51 warner Exp $";
    
    String recName = null;
    long timeStamp = 0;
    String outVal = "", health = "", mess = "";

    String prev_text = "";
    String new_text = "";
    float minVal = Integer.MIN_VALUE, maxVal = Integer.MAX_VALUE;
    boolean marked = false;
    String command = null;
    Vector <UFMMTComponent> shadows;
    Vector <UFGUIRecord> clearRecs;

    UFMessageLog _log;

    public static final Color CHANGED_COLOR = new Color(0,255,255);

    public UFMMTPasswordField(String name) {
      recName = name;
      _log = new UFMessageLog(1000);
      setToolTipText();
      timeStamp = 0;
      addMouseListener(this);
      addFocusListener(this);
      addKeyListener(this);
      shadows = new Vector(1);
      clearRecs = new Vector(1);
    } 

    public long getTimeStamp() { return timeStamp; }

    public String toString() { return (new String(getPassword()));}


    //-------------------------------------------------------------------------------
    protected void setToolTipText() {
      String tip = "<html>"+recName + " = " + outVal;
      if (!health.equals("")) tip += "<br>Health: "+health;
      if (!mess.equals("")) tip += "<brMessage: "+mess;
      tip += "</html>";
      this.setToolTipText(tip);
    }

    public void setText(String newText) {
      timeStamp = System.currentTimeMillis();
      super.setText(newText);
      outVal = newText;
      _log.addMessage(newText);
      setToolTipText();
    }

    //-------------------------------------------------------------------------------

    public void clearShadows() {
      shadows = new Vector(1);
    }

    public void addShadow(UFMMTComponent shadow) {
      if (shadow instanceof UFMMTPasswordField) {
	shadows.add(shadow);
      }
    }

    //-------------------------------------------------------------------------------

    public String getVal() {
      if (isBlue()) return (new String(getPassword())).trim();
      return outVal;
    }

    public void setValue(String value) {
      outVal = value.trim();
      prev_text = value.trim();
      setText(outVal);
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
	UFMMTPasswordField shadow = (UFMMTPasswordField)shadows.get(j);
	shadow.clear();
        shadow.setText((new String(getPassword())).trim());
      }
      for (int j = 0; j < clearRecs.size(); j++) {
        UFGUIRecord clearRec = (UFGUIRecord)clearRecs.elementAt(j);
        clearRec.clear();
      }
    }

    public void clear() {
      marked = false;
      setText(prev_text);
      setBackground(Color.white);
    }

    public void apply() {
      marked = false;
      prev_text = getVal();
      setBackground(Color.white); 
    }

    public String getCommandValue() {
      return command+(new String(getPassword()));
    }

    public void setCommand(String command) {
      this.command = command;
    }

    public void setHealthAndMess(String health, String mess) {
      this.health = health;
      this.mess = mess;
    }

    //------------------------------------------------------------------
    public void mouseReleased(MouseEvent me) {}
    public void mousePressed(MouseEvent me) {}
    public void mouseExited(MouseEvent me) {}
    public void mouseEntered(MouseEvent me) {}
    public void mouseClicked(MouseEvent me) {
	if (me.getButton() == MouseEvent.BUTTON3) {
	    JPopupMenu popupMenu = new JPopupMenu();
	    JMenuItem viewLogSmall = new JMenuItem("View Log (small)");
	    JMenuItem viewLogLarge = new JMenuItem("View Log (bigger)");
	    JMenuItem viewLogHuge = new JMenuItem("View Log (full size)");
	    popupMenu.add(viewLogSmall);
	    popupMenu.add(viewLogLarge);
	    popupMenu.add(viewLogHuge);
	    
	    viewLogSmall.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) { _log.viewFrame(recName,600,200); } });
	    
	    viewLogLarge.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) { _log.viewFrame(recName,650,500); } });
	    
	    viewLogHuge.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) { _log.viewFrame(recName,700,900); } });
	    
	    
	    popupMenu.show(me.getComponent(), me.getX(), me.getY());
	}
    }

    //-------------------------------------------------------------------------------

    public void focusGained(FocusEvent e) 
    { 
	if( prev_text.trim().equals("") )
	    prev_text = (new String(this.getPassword())).trim();
    }


    public void focusLost(FocusEvent e) 
    { 
	new_text = (new String(getPassword())).trim();
	if( ! new_text.equals( prev_text ) ) {
	    setBackground( CHANGED_COLOR );
	}
    }

    public boolean isBlue() {
      if (getBackground() == CHANGED_COLOR) return true;
      return false;
    }

    //-------------------------------------------------------------------------------
    /**
     * Event Handling Method
     *@param ke KeyEvent: TBD
     */
    public void keyTyped (KeyEvent ke) {
    }

    //-------------------------------------------------------------------------------
    /**
     * Event Handling Method
     *@param ke KeyEvent: TBD
     */
    public void keyReleased (KeyEvent ke) {
    }

    //-------------------------------------------------------------------------------
    /**
     * Event Handling Method
     *@param ke KeyEvent: TBD
     */
    public void keyPressed (KeyEvent ke) {
	if( ke.getKeyChar() == '\n' ) {  //enter key
	  new_text = (new String(getPassword())).trim();
	  if (!checkMinMax()) return;
	  mark();
          prev_text = (new String(getPassword())).trim();
	  setText( prev_text );
	} else if( ke.getKeyChar() == 27 ) { //escape 
	  setText( prev_text );
	  setBackground(Color.white);
	} else setBackground(Color.white); 
    }


  //-------------------------------------------------------------------------------

  public void setMin(float min) {
    this.minVal = min;
  }

  public void setMax(float max) {
    this.maxVal = max;
  }

  public void setMinMax(float min, float max) {
    this.minVal = min;
    this.maxVal = max;
  }

  public boolean checkMinMax() {
    boolean isValid = true;
    float x = 0;
    try {
      x = Float.parseFloat(new_text);
    } catch(Exception e) {
      return isValid;
    }
    String validVals = "";
    if (minVal == Integer.MIN_VALUE && maxVal != Integer.MIN_VALUE) {
      validVals = "less than "+maxVal;
    } else if (minVal != Integer.MIN_VALUE && maxVal == Integer.MIN_VALUE) {
      validVals = "greater than "+minVal;
    } else if (minVal != Integer.MIN_VALUE && maxVal != Integer.MIN_VALUE) {
      validVals = "between "+minVal+" and "+maxVal;
    }
    if (minVal != Integer.MIN_VALUE && x < minVal) isValid = false;
    if (maxVal != Integer.MIN_VALUE && x > maxVal) isValid = false;
    if (!isValid) {
       JOptionPane.showMessageDialog(null,"<html>Error: "+new_text+" is an invalid value for record "+recName+"<br>Values must be "+validVals+".</html>", "Error", JOptionPane.ERROR_MESSAGE);
    }
    return isValid;
  }

  public void allowOnlyNums() {
    allowOnlyNums(false, false);
  }

  public void allowOnlyNums(final boolean allowNeg, final boolean allowDec) {
        //only allow numeric input
    addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent kev) {
            }
            public void keyReleased(KeyEvent kev) {}
            public void keyTyped(KeyEvent kev) {
		boolean validChar = false;
		if (Character.isDigit(kev.getKeyChar()) || kev.getKeyChar() == '\n' || kev.getKeyChar() == 27) validChar = true;
		if (allowNeg && kev.getKeyChar() == '-') validChar = true;
		if (allowDec && kev.getKeyChar() == '.') validChar = true;
		if (!validChar) {
                    kev.consume();
                }
            }
        });
  }

  public void allowOnlyAlphaNumeric() {
        //only allow alpha numeric input
    addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent kev) {
            }
            public void keyReleased(KeyEvent kev) {}
            public void keyTyped(KeyEvent kev) {
                boolean validChar = false;
                if (Character.isLetterOrDigit(kev.getKeyChar()) || kev.getKeyChar() == '\n' || kev.getKeyChar() == 27) validChar = true;
                if (kev.getKeyChar() == '-' || kev.getKeyChar() == '_') validChar = true;
                if (kev.getKeyChar() == '.') validChar = true;
                if (!validChar) {
                    kev.consume();
                }
            }
        });
  }

} //end of class UFMMTPasswordField

