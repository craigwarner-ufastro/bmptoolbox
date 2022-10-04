package javaUFLib;

/**
 * Title:        UFExecCommand
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2005
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  JPanel executing and showing results of a shell command
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class UFFlam2Starter extends javax.swing.JFrame implements Runnable {
  public static final String rcsID = "$Name:  $ $Id: UFFlam2Starter.java,v 1.1 2010/03/10 21:33:58 warner Exp $";
  public int xdim, ydim;
  public JTabbedPane tabs;
  public UFExecCommand ufstartPanel, fjecPanel, jddPanel, dhsPanel;
  public UFExecCommand tailPanel, helpeyPanel, edtPanel, dhsBackupPanel;
  public UFExecCommand ufgtakePanel;
  public UFColorButton startButton;
  public JCheckBox cbFjec, cbJdd, cbTail, cbHelpey;
  public JRadioButton realMode, simMode;
  public JRadioButton flam, foo, fu, f2;
  public JRadioButton dhs, ufgtake;
  public JRadioButton remoteHost, localHost;
  public JTextField hostName;
  public JTextField dhsHost;
  public JLabel statusLabel;
  public boolean forReal = true, useDhs = true, useRsh = true, isRunning = false;
  public boolean fjec = true, jdd = true, uftail = false, helpey = false;
  public Container content;
  public String ufinstallDir = UFExecCommand.getEnvVar("UFINSTALL");
  public Thread runThread;

  public UFFlam2Starter() {
    this(1024,768);
  }

  public UFFlam2Starter(int xdim, int ydim) {
    super("UFFlam2Starter");
    this.xdim = xdim;
    this.ydim = ydim;
    setSize(xdim, ydim);
    setDefaultCloseOperation(3);
    content = getContentPane();
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
	UFFlam2Starter.this.dispose();
      }
    });
    SpringLayout layout = new SpringLayout();
    content.setLayout(layout);
    tabs = new JTabbedPane();
    tabs.setPreferredSize(new Dimension(960,560));
    tabs.add("Start Agents", ufstartPanel);
    tabs.add("FJEC", fjecPanel);
    tabs.add("JDD", jddPanel);

    add(tabs);
    layout.putConstraint(SpringLayout.WEST, tabs, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, tabs, 10, SpringLayout.NORTH,this);

    JLabel modeLabel = new JLabel("Mode:");
    realMode = new JRadioButton("Real", true);
    simMode = new JRadioButton("Simulation");
    ButtonGroup bgMode = new ButtonGroup();
    bgMode.add(realMode);
    bgMode.add(simMode);
    add(modeLabel);
    add(realMode);
    add(simMode);
    layout.putConstraint(SpringLayout.WEST, modeLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, modeLabel, 10, SpringLayout.SOUTH, tabs);
    layout.putConstraint(SpringLayout.WEST, realMode, 10, SpringLayout.EAST, modeLabel);
    layout.putConstraint(SpringLayout.NORTH, realMode, 8, SpringLayout.SOUTH, tabs);
    layout.putConstraint(SpringLayout.WEST, simMode, 10, SpringLayout.EAST, realMode);
    layout.putConstraint(SpringLayout.NORTH, simMode, 8, SpringLayout.SOUTH, tabs);

    JLabel dbLabel = new JLabel("Database:");
    flam = new JRadioButton("flam", true);
    foo = new JRadioButton("foo");
    fu = new JRadioButton("fu");
    f2 = new JRadioButton("f2");
    ButtonGroup dbMode = new ButtonGroup();
    dbMode.add(flam);
    dbMode.add(foo);
    dbMode.add(fu);
    dbMode.add(f2);
    add(dbLabel);
    add(flam);
    add(foo);
    add(fu);
    add(f2);
    layout.putConstraint(SpringLayout.WEST, dbLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, dbLabel, 10, SpringLayout.SOUTH, modeLabel);
    layout.putConstraint(SpringLayout.WEST, flam, 10, SpringLayout.EAST, dbLabel);
    layout.putConstraint(SpringLayout.NORTH, flam, 8, SpringLayout.SOUTH, modeLabel);
    layout.putConstraint(SpringLayout.WEST, foo, 10, SpringLayout.EAST, flam);
    layout.putConstraint(SpringLayout.NORTH, foo, 8, SpringLayout.SOUTH, modeLabel);
    layout.putConstraint(SpringLayout.WEST, fu, 10, SpringLayout.EAST, foo);
    layout.putConstraint(SpringLayout.NORTH, fu, 8, SpringLayout.SOUTH, modeLabel);
    layout.putConstraint(SpringLayout.WEST, f2, 10, SpringLayout.EAST, fu);
    layout.putConstraint(SpringLayout.NORTH, f2, 8, SpringLayout.SOUTH, modeLabel);

    JLabel acqModeLabel = new JLabel("Acquisition Mode:");
    dhs = new JRadioButton("DHS", true);
    ufgtake = new JRadioButton("ufgtake");
    ButtonGroup bgAcq = new ButtonGroup();
    bgAcq.add(dhs);
    bgAcq.add(ufgtake);
    add(acqModeLabel);
    add(dhs);
    add(ufgtake);
    layout.putConstraint(SpringLayout.WEST, acqModeLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, acqModeLabel, 10, SpringLayout.SOUTH, dbLabel);
    layout.putConstraint(SpringLayout.WEST, dhs, 10, SpringLayout.EAST, acqModeLabel);
    layout.putConstraint(SpringLayout.NORTH, dhs, 8, SpringLayout.SOUTH, dbLabel);
    layout.putConstraint(SpringLayout.WEST, ufgtake, 10, SpringLayout.EAST, dhs);
    layout.putConstraint(SpringLayout.NORTH, ufgtake, 8, SpringLayout.SOUTH, dbLabel);

    JLabel hostLabel = new JLabel("Host:");
    localHost = new JRadioButton("Local");
    remoteHost = new JRadioButton("Remote", true);
    ButtonGroup bgHost = new ButtonGroup();
    bgHost.add(localHost);
    bgHost.add(remoteHost);
    hostName = new JTextField("flam2sparc", 15);
    add(hostLabel);
    add(localHost);
    add(remoteHost);
    add(hostName);
    layout.putConstraint(SpringLayout.WEST, hostLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, hostLabel, 10, SpringLayout.SOUTH, acqModeLabel);
    layout.putConstraint(SpringLayout.WEST, localHost, 10, SpringLayout.EAST, hostLabel);
    layout.putConstraint(SpringLayout.NORTH, localHost, 8, SpringLayout.SOUTH, acqModeLabel);
    layout.putConstraint(SpringLayout.WEST, remoteHost, 10, SpringLayout.EAST, localHost);
    layout.putConstraint(SpringLayout.NORTH, remoteHost, 8, SpringLayout.SOUTH, acqModeLabel);
    layout.putConstraint(SpringLayout.WEST, hostName, 5, SpringLayout.EAST, remoteHost);
    layout.putConstraint(SpringLayout.NORTH, hostName, 8, SpringLayout.SOUTH, acqModeLabel);

    JLabel dhsHostLabel = new JLabel("DHS Host:");
    dhsHost = new JTextField("sbfdhstest01", 15);
    add(dhsHostLabel);
    add(dhsHost);
    layout.putConstraint(SpringLayout.WEST, dhsHostLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, dhsHostLabel, 10, SpringLayout.SOUTH, hostLabel);
    layout.putConstraint(SpringLayout.WEST, dhsHost, 10, SpringLayout.EAST, dhsHostLabel);
    layout.putConstraint(SpringLayout.NORTH, dhsHost, 8, SpringLayout.SOUTH, hostLabel);


    JLabel startLabel = new JLabel("Start:");
    cbFjec = new JCheckBox("FJEC", true);
    cbJdd = new JCheckBox("JDD", true);
    cbTail = new JCheckBox("uftail", false);
    cbHelpey = new JCheckBox("Helpey", false);
    add(startLabel);
    add(cbFjec);
    add(cbJdd);
    add(cbTail);
    add(cbHelpey);
    layout.putConstraint(SpringLayout.WEST, startLabel, 10, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, startLabel, 10, SpringLayout.SOUTH, dhsHostLabel);
    layout.putConstraint(SpringLayout.WEST, cbFjec, 10, SpringLayout.EAST, startLabel);
    layout.putConstraint(SpringLayout.NORTH, cbFjec, 8, SpringLayout.SOUTH, dhsHostLabel);
    layout.putConstraint(SpringLayout.WEST, cbJdd, 10, SpringLayout.EAST, cbFjec);
    layout.putConstraint(SpringLayout.NORTH, cbJdd, 8, SpringLayout.SOUTH, dhsHostLabel);
    layout.putConstraint(SpringLayout.WEST, cbTail, 10, SpringLayout.EAST, cbJdd);
    layout.putConstraint(SpringLayout.NORTH, cbTail, 8, SpringLayout.SOUTH, dhsHostLabel);
    layout.putConstraint(SpringLayout.WEST, cbHelpey, 10, SpringLayout.EAST, cbTail);
    layout.putConstraint(SpringLayout.NORTH, cbHelpey, 8, SpringLayout.SOUTH, dhsHostLabel);

    startButton = new UFColorButton("Start Agents", UFColorButton.COLOR_SCHEME_GREEN);
    startButton.setFont(new Font("Dialog", 0, 20));
    startButton.setPreferredSize(new Dimension(160,60));
    startButton.setToolTipText("Start agents and other indicated programs");
    add(startButton);
    layout.putConstraint(SpringLayout.WEST, startButton, 25, SpringLayout.EAST, hostName);
    layout.putConstraint(SpringLayout.NORTH, startButton, 25, SpringLayout.SOUTH, tabs);

    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
	start();
      }
    });

    statusLabel = new JLabel("Status:");
    statusLabel.setFont(new Font("Dialog", 1, 14));
    add(statusLabel);
    layout.putConstraint(SpringLayout.WEST, statusLabel, 40, SpringLayout.EAST, startButton);
    layout.putConstraint(SpringLayout.NORTH, statusLabel, 50, SpringLayout.SOUTH, tabs);

    pack();
    setVisible(true);
  }

  public void start() {
    runThread = new Thread(this);
    runThread.start();
  }

  public void run() {
	startButton.setEnabled(false);
        String command, options;
        String ldLibPath = UFExecCommand.getEnvVar("LD_LIBRARY_PATH");
        ldLibPath = ldLibPath.replaceAll("jdk","junk");
        String rshHost = hostName.getText().trim();

          forReal = realMode.isSelected();
          useDhs = dhs.isSelected();
          useRsh = remoteHost.isSelected();
          fjec = cbFjec.isSelected();
          jdd = cbJdd.isSelected();
          uftail = cbTail.isSelected();
          helpey = cbHelpey.isSelected();

	  command = "ufstop";
	  options = "-a";
	  ufstartPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/", useRsh, rshHost); 
          if (useRsh) {
            System.out.println("rsh "+rshHost+"; "+command+" "+options);
          } else System.out.println(command +" "+options);
	  ufstartPanel.dismiss.setVisible(false);
	  tabs.removeAll();
          tabs.add("Start Agents", ufstartPanel);
          ufstartPanel.start();
	  statusLabel.setText("Status: Stopping agents...");

	  if (fjec) {
	    command = "pkill";
            options = "-9 -f fjec.jar";
	    fjecPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            fjecPanel.dismiss.setVisible(false);
            tabs.add("FJEC", fjecPanel);
            fjecPanel.start();
	  }

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  if (jdd) {
	    command = "pkill";
	    options = "-9 -f jdd.jar";
            jddPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            jddPanel.dismiss.setVisible(false);
            tabs.add("JDD", jddPanel);
            jddPanel.start();
	  }

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

          if (useDhs) {
            command = "pkill";
            options = "-9 -f dhsbackup.py";
            dhsBackupPanel = new UFExecCommand(command, options, ufinstallDir+"/scripts/");
            dhsBackupPanel.dismiss.setVisible(false);
            tabs.add("DHS Backup", dhsBackupPanel);
            dhsBackupPanel.start();
          }

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  if (uftail && !useRsh) {
            command = "pkill";
            options = "-9 -f UFTail";
            tailPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            tailPanel.dismiss.setVisible(false);
            tabs.add("uftail", tailPanel);
            tailPanel.start();
	  }

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

          command = "pkill";
          options = "-9 ufgtake";
          ufgtakePanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
          ufgtakePanel.dismiss.setVisible(false);
          tabs.add("ufgtake", ufgtakePanel);
          ufgtakePanel.start();

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  if (helpey) {
            command = "pkill";
            options = "-9 -f NewFlam2Helper";
            helpeyPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            helpeyPanel.dismiss.setVisible(false);
            tabs.add("Helpey", helpeyPanel);
            helpeyPanel.start();
	  }

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

          dhsPanel = new UFExecCommand("dhsSysBoot", "stop", ufinstallDir+"/bin", useRsh, rshHost);
          if (useRsh) {
            System.out.println("rsh "+rshHost+"; dhsSysBoot stop");
          } else System.out.println("dhsSysBoot stop");
          dhsPanel.dismiss.setVisible(false);
          tabs.add("Gemini DHS", dhsPanel);
          dhsPanel.start();
          statusLabel.setText("Status: Stopping Gemini DHS system...");

          //wait 1 sec
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  try {
	    if (ufstartPanel.p != null)
	      ufstartPanel.p.waitFor();
	  } catch(InterruptedException e) {
	    System.out.println("UFFlam2Starter> "+e.toString());
	  }
	  //wait 3 sec to ensure ufstop kills everything
	  try {
	    Thread.sleep(3000);
	  } catch(InterruptedException e) {
            System.out.println(e.toString());
	  }

          command = "ufstop";
          options = "-a";
          ufstartPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/", useRsh, rshHost);
          if (useRsh) {
            System.out.println("rsh "+rshHost+"; "+command+" "+options);
          } else System.out.println(command +" "+options);
          ufstartPanel.dismiss.setVisible(false);
          tabs.remove(tabs.indexOfTab("Start Agents"));
	  tabs.add("Start Agents", ufstartPanel);
          ufstartPanel.start();
          statusLabel.setText("Status: Sending agents the stop signal again...");

          try {
            if (ufstartPanel.p != null)
              ufstartPanel.p.waitFor();
          } catch(InterruptedException e) {
            System.out.println("UFFlam2Starter> "+e.toString());
          }
          //wait 2 sec to ensure ufstop kills everything
          try {
            Thread.sleep(2000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }
	  statusLabel.setText("Status: Agents stopped");

	if (!isRunning) {
	  forReal = realMode.isSelected();
	  useDhs = dhs.isSelected();
          useRsh = remoteHost.isSelected();
	  fjec = cbFjec.isSelected();
	  jdd = cbJdd.isSelected();
	  uftail = cbTail.isSelected();
	  helpey = cbHelpey.isSelected();
          String epicsDb = "flam";
	  if (foo.isSelected()) epicsDb = "foo";
          if (fu.isSelected()) epicsDb = "fu";
          if (f2.isSelected()) epicsDb = "f2";

          tabs.removeAll();

	  boolean startDhs = checkDhs(useRsh, rshHost);
	  if (startDhs) {
	    dhsPanel = new UFExecCommand("dhsSysBoot", "start", ufinstallDir+"/bin", useRsh, rshHost); 
	    if (useRsh) {
	      System.out.println("rsh "+rshHost+"; dhsSysBoot start");
	    } else System.out.println("dhsSysBoot start");
	    dhsPanel.dismiss.setVisible(false);
	    tabs.add("Gemini DHS", dhsPanel);
	    dhsPanel.start();
	    statusLabel.setText("Status: Starting Gemini DHS system...");
	    try {
	      //let DHS begin startup before doing a ufstart
	      while (dhsPanel.isRunning) Thread.sleep(500);
	    } catch (InterruptedException e) {
	      System.out.println("UFFlam2Starter> "+e.toString());
	    }
	  }

	  command = "ufstart";
	  options = "-a -epics "+epicsDb;
	  if (!forReal) options += " -sim";
	  if (forReal) {
	    if (!useDhs) options += " -ufgtake";
	  } 
	  if (useDhs) options += " -dhs "+dhsHost.getText();

	  ufstartPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/", useRsh, rshHost); 
	  if (useRsh) {
	    System.out.println("rsh "+rshHost+"; "+command+" "+options);
	  } else System.out.println(command +" "+options);

	  ufstartPanel.setEnv("LD_LIBRARY_PATH",ldLibPath);
          if (useRsh) {
	    ufstartPanel.setEnv("EPICS_CA_ADDR_LIST", rshHost); 
          } else {
            ufstartPanel.setEnv("EPICS_CA_ADDR_LIST", "localhost");
          }
	  ufstartPanel.dismiss.setVisible(false);
	  tabs.add("Start Agents", ufstartPanel);
	  ufstartPanel.start();
	  statusLabel.setText("Status: Starting agents...");

          //wait 10 sec for agents to start 
          try {
            Thread.sleep(10000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  if (forReal) {
	    command = "initcam";
	    options = "-f "+ufinstallDir+"/etc/boeing_2048.cfg";
	    edtPanel = new UFExecCommand(command, options, "/opt/EDTpdv/", useRsh, rshHost); 
            edtPanel.setEnv("LD_LIBRARY_PATH",ldLibPath);
            if (useRsh) {
              edtPanel.setEnv("EPICS_CA_ADDR_LIST", rshHost);
            } else {
              edtPanel.setEnv("EPICS_CA_ADDR_LIST", "localhost");
            }
            edtPanel.dismiss.setVisible(false);
            tabs.add("edtflam", edtPanel);
            edtPanel.start();
	    if (useDhs) {
	      command = "dhsbackup.py";
	      options = "/nfs/flam2sparc/data/staging/perm /home/flam/Data/2009";
	      dhsBackupPanel = new UFExecCommand(command, options, ufinstallDir+"/scripts/");
	      dhsBackupPanel.dismiss.setVisible(false);
	      tabs.add("DHS Backup", dhsBackupPanel);
	      dhsBackupPanel.start();
	    }
	  }

          //wait 10 more sec for agents to start
          try {
            Thread.sleep(10000);
          } catch(InterruptedException e) {
            System.out.println(e.toString());
          }

	  if (fjec) {
	    command = "uffjec";
	    options = epicsDb+":";
	    fjecPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            if (useRsh) {
              fjecPanel.setEnv("EPICS_CA_ADDR_LIST", rshHost); 
            } else {
              fjecPanel.setEnv("EPICS_CA_ADDR_LIST", "localhost");
            }
	    fjecPanel.dismiss.setVisible(false);
	    tabs.add("FJEC", fjecPanel);
	    fjecPanel.start();
	  }

	  if (jdd) {
	    command = "ufjdd";
	    options = "-epics "+epicsDb;
	    if (useDhs) {
	      options+=" -host ";
	      if (useRsh) options+=rshHost; else options+="localhost";
	    }
            jddPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            if (useRsh) {
              jddPanel.setEnv("EPICS_CA_ADDR_LIST", rshHost); 
            } else {
              jddPanel.setEnv("EPICS_CA_ADDR_LIST", "localhost");
            }
            jddPanel.dismiss.setVisible(false);
            tabs.add("JDD", jddPanel);
            jddPanel.start();
	    if (!useDhs) {
	      JOptionPane.showMessageDialog(null, "To take images, switch JDD to ufgtake mode!", "ufgtake must be started", JOptionPane.INFORMATION_MESSAGE);
	    }
	  }

	  if (uftail) {
	    command = "uftail";
	    options = "-a";
	    if (useRsh) {
	      JOptionPane.showMessageDialog(null, "<html>Cannot start uftail over rsh.  Open a terminal and<br>ssh -Y "+rshHost+"<br>uftail -a</html>", "uftail must be started manually", JOptionPane.INFORMATION_MESSAGE);
	    } else {
	      //wait for ufstart
	      try {
		while (ufstartPanel.isRunning) Thread.sleep(500);
	      } catch(InterruptedException e) {
		System.out.println(e.toString());
	      }
	      tailPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
	      tabs.add("uftail",tailPanel);
	      tailPanel.start();
	    }
	  }

	  if (helpey) {
            //wait for ufstart
            try {
              Thread.sleep(5000);
            } catch(InterruptedException e) {
              System.out.println(e.toString());
            }
            command = "ufflam2helper";
            options = "-"; 
            helpeyPanel = new UFExecCommand(command, options, ufinstallDir+"/bin/");
            helpeyPanel.dismiss.setVisible(false);
            tabs.add("Helpey", helpeyPanel);
            helpeyPanel.start();
          }

	  statusLabel.setText("Status: Agents running");
	} else {

	}
        isRunning = !isRunning;
        setEnabledState(!isRunning);
	startButton.setEnabled(true);
  }

  public void setEnabledState(boolean state) {
    if (state) {
      startButton.setColorGradient(UFColorButton.COLOR_SCHEME_GREEN);
      startButton.setText("Start Agents");
      startButton.setToolTipText("Start agents and other indicated programs");
    } else {
      startButton.setColorGradient(UFColorButton.COLOR_SCHEME_RED);
      startButton.setText("Stop Agents");
      startButton.setToolTipText("Stop agents and all other programs");
    }
    realMode.setEnabled(state);
    simMode.setEnabled(state);
    dhs.setEnabled(state);
    ufgtake.setEnabled(state);
    localHost.setEnabled(state);
    remoteHost.setEnabled(state);
    hostName.setEnabled(state);
    cbFjec.setEnabled(state);
    cbJdd.setEnabled(state);
    cbTail.setEnabled(state);
    cbHelpey.setEnabled(state);
    flam.setEnabled(state);
    foo.setEnabled(state);
    fu.setEnabled(state);
    f2.setEnabled(state);
    dhsHost.setEnabled(state);
  }

  public boolean checkDhs(boolean useRsh, String rshHost) {
    if (!useDhs) return false;
    String os = checkOS(useRsh, rshHost);
    if (!os.equals("sun")) return false;
    boolean running = isRunning("dhsData", useRsh, rshHost);
    if (running) return false; else return true;
  }

  public String checkOS(boolean useRsh, String rshHost) {
    try {
      String s = "";
      Process unamep;
      if (useRsh)
	unamep = new ProcessBuilder("rsh",rshHost,"/bin/uname").start();
      else
	unamep = new ProcessBuilder("/bin/uname").start();
      BufferedReader unamebr = new BufferedReader(new InputStreamReader(unamep.getInputStream()));
      s = unamebr.readLine();
      unamebr.close();
      if (s.toLowerCase().indexOf("linux") != -1) {
	return "linux";
      } else if (s.toLowerCase().indexOf("sun") != -1) {
	return "sun";
      } else return "unknown"; 
    } catch (Exception e) {
      return "unknown";
    }
  }

  public boolean isRunning(String cmd, boolean useRsh, String rshHost) {
    boolean currentlyRunning = false;
    try {
      Process checkps;
      if (useRsh)
	checkps = new ProcessBuilder("rsh",rshHost,"/bin/ps","-A").start();
      else
	checkps = new ProcessBuilder("/bin/ps","-A").start();
      BufferedReader brps = new BufferedReader(new InputStreamReader(checkps.getInputStream()));
      String s = "";
      while (s != null) {
	s = brps.readLine();
	if (s == null) break;
	if (s.endsWith(" "+cmd)) currentlyRunning = true;
      }
    }  catch (Exception e) {
      System.err.println("UFFlam2Starter::isRunning> "+e.toString());
    }
    return currentlyRunning;
  }

  public static void main(String[] args) {
    UFFlam2Starter theStarter = new UFFlam2Starter();
  }
}

