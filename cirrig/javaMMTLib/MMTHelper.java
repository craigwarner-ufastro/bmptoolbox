package javaMMTLib;
/**
 * Title:        MMTHelper.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Extends NewFlam2Helper to plot environment logs 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;
import java.text.*;
import javaUFLib.*;

public class MMTHelper extends NewFlam2Helper {
    public static final
        String rcsID = "$Name:  $ $Id: MMTHelper.java,v 1.1 2011/01/20 23:07:41 warner Exp $";

    public static String installDir = UFExecCommand.getEnvVar("UFMMTINSTALL");
    protected DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
    protected Date today = new Date();

    public MMTHelper(String [] args) {
	logName = installDir+"/envlogs/envlog.mmt."+dfm.format(today);
	for (int j = 0; j < args.length; j++) {
	  if (args[j].equals("-log")) {
	    if (args.length > j) logName = args[j+1];
	  }
	}
	prefsName = ".mmthelper";
        readLog(logName);
        setupGUI();
	setPlots();
        mainFrame.setTitle("MMT Helper");
    }

    public void readLog(String logName) {
      int tokens = 7;
      String[] strs = new String[1];
      panelTitle = new String[1]; 
      panelTitle[0] = "MMT-Pol Temperatures";
      try {
        BufferedReader br = new BufferedReader(new FileReader(logName));
        String temp = br.readLine();
        temp = br.readLine();
        temp = br.readLine();
        if (temp != null) {
           StringTokenizer sttemp = new StringTokenizer(temp,",");
           tokens = sttemp.countTokens();
        }
        br.close();
      } catch (IOException e) { npanels = 1; }
      if (tokens == 7) {
        logType = 0;
        npanels = 1;
        strs = new String[npanels];
        strs[0] = "Det Temp,Det SetPt,Worksurface,77k Stage,Motor Support";
      }
      graphPanels = new UFHelperPlot[npanels];
      thePanels = new UFHelperPlotPanel[npanels];

      int n = 0;
      for (int j = 0; j < npanels; j++) {
        graphPanels[j] = new UFHelperPlot(strs[j].split(","), n, logName);
	graphPanels[j].setMinTokens(tokens);
	graphPanels[j].setReqChar("-");
        n+=strs[j].split(",").length;
        thePanels[j] = new UFHelperPlotPanel(graphPanels, j);
      }
    }

    public static void main(String [] args) {
        new MMTHelper(args);
    }
}
