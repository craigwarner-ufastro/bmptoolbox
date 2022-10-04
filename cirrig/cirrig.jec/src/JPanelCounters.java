package cjec;

/**
 * Title:        JPanelCounters.java
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
import javaUFLib.*;
import javaMMTLib.*;

//===============================================================================
/**
 *This Class handles the executive tabbed pane 
 */
public class JPanelCounters extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelCounters.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    CjecFrame cjec;
    ArrayList<JPanelIndividualCounter> individualCounterPanels;
    JPanel countersPanel;

    String _ip;
    JPanelZoneGroup _zgPanel;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelCounters(CjecFrame cjecFrame, String plcIP, JPanelZoneGroup zgPanel) {
	this.setLayout(new GridLayout(6,3,4,4));
        _ip = plcIP;
        _zgPanel = zgPanel;
        cjec = cjecFrame;
        setPreferredSize(new Dimension(960,168));

        individualCounterPanels = new ArrayList();
    } 

    public String getIp() {
      return _ip;
    }

    public String getGroupName() {
      return _zgPanel.getGroupName(); 
    }

    public boolean isEmpty() {
      if (individualCounterPanels.size() == 0) return true; else return false;
    }

    public void addCounter(String counterName, int counterNum) {
      JPanelIndividualCounter individualCounterPanel = new JPanelIndividualCounter(cjec, _ip, _zgPanel, counterName, counterNum);
      individualCounterPanels.add(individualCounterPanel);
      this.add(individualCounterPanel);
      revalidate();
      repaint();
    }

    public void removeCounter(String counterName) {
      for (Iterator i = individualCounterPanels.iterator(); i.hasNext(); ) {
        JPanelIndividualCounter individualCounterPanel = (JPanelIndividualCounter)i.next();
        if (individualCounterPanel.getCounterName().equals(counterName)) {
          i.remove();
          redrawCountersPanel();
          break;
        }
      }
    }

    public void redrawCountersPanel() {
      this.removeAll();
      synchronized(individualCounterPanels) {
        for (int i = 0; i < individualCounterPanels.size(); i++) {
	  this.add(individualCounterPanels.get(i));
        }
      }
      revalidate();
      repaint();
    }

} //end of class JPanelCounters
