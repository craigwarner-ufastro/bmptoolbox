package bmpjec;

/**
 * Title:        MJECPlot.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  plot panel for MJEC 
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

public class MJECPlot extends UFHelperPlot {
    public static final
        String rcsID = "$Name:  $ $Id: MJECPlot.java,v 1.1 2011/01/20 23:08:47 warner Exp $";

    protected MjecFrame mjec;
    protected String[] recsToPlot;
    protected String xMinVal="0", xMaxVal="", yMinVal="", yMaxVal="";

    public MJECPlot(MjecFrame mjecFrame, String[] recNames) {
	mjec = mjecFrame;
	recsToPlot = recNames;
	numSensors = recNames.length;
	vx = new Vector(100000,10000);
	vy = new Vector[numSensors];
	showY = new boolean[numSensors];
	sensorNames = new String[numSensors];
	oPlotOpts = new String[numSensors];
	for (int j = 0; j < numSensors; j++) {
	  vy[j] = new Vector(100000,10000);
	  showY[j] = true;
	  sensorNames[j] = recNames[j].substring(recNames[j].lastIndexOf(":")+1); 
	  oPlotOpts[j] = "";
        }
	setupPlot(604,330);
	addComponentListener(new ComponentAdapter() {
	  public void componentResized(ComponentEvent ev) {
	    resizePlot(getWidth(), getHeight());
	  }
	});
    }

    public void setupPlot(int xdim, int ydim) {
	super.setupPlot(xdim, ydim);
	ActionListener[] listeners = resetRangeItem.getActionListeners();
	resetRangeItem.removeActionListener(listeners[0]);
	resetRangeItem.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent ev) {
	    resetRange();
	    updatePlot();
	  }
	});
	resetRangeItem.setVisible(true);
    }

    public void startThread() {
        if (isThreadAlive())
            stopThread();
        graphThread = new MJECGraphThread();
        graphThread.start();
    }

    public void setShowY(int n, boolean checked) {
	showY[n] = checked; 
    }

    public void resetRange() {
	xMinVal = "0";
	xMaxVal = "";
	yMinVal = "";
	yMaxVal = "";
	updatePlot();
    }

    public void updateRange() {
	addOpts = "";
	if (!xMinVal.trim().equals("")) addOpts += ",*xminval=" +xMinVal;
        if (!xMaxVal.trim().equals("")) addOpts += ",*xmaxval=" +xMaxVal;
        if (!yMinVal.trim().equals("")) addOpts += ",*yminval=" +yMinVal;
        if (!yMaxVal.trim().equals("")) addOpts += ",*ymaxval=" +yMaxVal;
    }

    public void updatePlot() {
	updateRange();
	super.updatePlot();
    }

    public void calcZoom() {
      if (sxinit==0 || syinit==0 || sxfin==0 || syfin==0 ) return;
      if (Math.abs(sxinit-sxfin) < 3 && Math.abs(syinit-syfin) < 3) return;
      float x1 = (Math.min( sxinit, sxfin )-xoff)/xscale;
      float y1 = (Math.min( yoff-syinit, yoff-syfin ))/yscale;
      float x2 = (Math.max( sxinit, sxfin )-xoff)/xscale;
      float y2 = (Math.max( yoff-syinit, yoff-syfin ))/yscale;
      x1 = (float)(Math.floor(x1*1000)*.001);
      x2 = (float)(Math.floor(x2*1000)*.001);
      y1 = (float)(Math.floor(y1*1000)*.001);
      y2 = (float)(Math.floor(y2*1000)*.001);
      if ((""+x1).equals("NaN") || (""+y1).equals("NaN") || (""+x2).equals("NaN") || (""+y2).equals("NaN")) return;
      xMinVal = ""+x1;
      xMaxVal = ""+x2;
      yMinVal = ""+y1;
      yMaxVal = ""+y2;
      updatePlot();
    }

    //Class MJECGraphThread
    public class MJECGraphThread extends UFHelperPlot.GraphThread {

        public void run() {
          keepRunning = true; stillRunning = true;
	  double[] vals = new double[numSensors];
	  UFGUIRecord guiRec;
	  boolean hasData = false;
	  while (!hasData) {
	    try {
	      Thread.sleep(1000);
	    } catch(InterruptedException e) {}
	    guiRec = (UFGUIRecord)mjec.database.get(recsToPlot[0]);
	    if (guiRec == null) continue;
	    if (guiRec.getValue() == null) continue;
	    if (guiRec.getValue().trim().equals("")) continue;
	    hasData = true;
	  }
          long beginTimestamp = System.currentTimeMillis()/1000L;
          while (keepRunning) {
            title = "Relative to "+(new Date(beginTimestamp*1000L)).toString();
	    hasData = true;
	    for (int j = 0; j < numSensors; j++) {
	      try {
                guiRec = (UFGUIRecord)mjec.database.get(recsToPlot[j]);
                if (guiRec.getValue().equals("")) {
                  //Not yet connected to agent!
                  hasData = false;
                  break;
                }
		vals[j] = Double.parseDouble(guiRec.getValue());
	      } catch (Exception e) {
                System.out.println("MJECGraphThread> "+j+e.toString());
e.printStackTrace();
		hasData = false;
		break;
	      }
	    }
	    if (!hasData) continue;
	    vx.add(new Long(System.currentTimeMillis()/1000L-beginTimestamp));
	    for (int j = 0; j < numSensors; j++) {
	      vy[j].add(new Double(vals[j]));
	    }
	    updatePlot();
            try {
              Thread.sleep(3000);
            } catch(InterruptedException e) {}
	  }
	}
    }
}
