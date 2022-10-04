package javaUFLib;

/**
 * Title:        NewFlam2Helper.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2006
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  flam2helper main class
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;

public class NewFlam2Helper {
    public static final
        String rcsID = "$Name:  $ $Id: NewFlam2Helper.java,v 1.4 2017/01/19 20:09:56 cwarner Exp $";

    protected JFrame mainFrame ;
    protected UFHelperPlot [] graphPanels;
    protected String logName = "/share/data/environment/current", prefsName = ".ufflam2helper";
    protected String defPath = ".";
    protected UFHelperPlotPanel[] thePanels;
    protected int logType, npanels;
    protected String[] args, panelTitle;
    protected JTabbedPane jtpGraph, jtpMain;
    protected JPanel overallGraphPanel;
    protected UFTail tail = null;

    public NewFlam2Helper() { 
	super();
    }

    public NewFlam2Helper(String [] args) {
	this.args = args;
        readLog(logName);
	setupGUI();
	setPlots();
	mainFrame.setTitle("Helpy Helperton");
    }

    public void setupGUI() {
        mainFrame = new JFrame();
        jtpMain = new JTabbedPane();
        jtpGraph = new JTabbedPane();

        overallGraphPanel = new JPanel();
        overallGraphPanel.setLayout(new BorderLayout());
        overallGraphPanel.add(jtpGraph, BorderLayout.CENTER);
    }

    public void setPlots() {
        jtpMain.removeAll();
        jtpGraph.removeAll();
        for (int j = 0; j < npanels; j++) {
          jtpGraph.add(thePanels[j], panelTitle[j]);
        }

        String[] logNames = {logName};
        tail = new UFTail(logNames);
        jtpMain.add(tail,"Tail");
        if (args != null && args.length > 0) {
          jtpMain.add(new UFTail(args),"Agents");
        }
        jtpMain.add(overallGraphPanel,"Graphs");
        jtpMain.setSelectedComponent(overallGraphPanel);

        mainFrame.setContentPane(jtpMain);
        mainFrame.setSize(942,726);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(3);
    }

    public void readLog(String logName) {
      int tokens = 29;
      String[] strs = new String[2]; 
      panelTitle = ("Pressure,Cam/Mos").split(",");
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
      } catch (IOException e) { npanels = 4; }
      if (tokens < 21) {
        logType = 0;
        npanels = 2;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det";
      } else if (tokens == 21) {
        logType = 1;
        npanels = 3;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt,CamA %Pwr,CamB %Pwr";
        strs[2] = "LVDT Volts,Focus";
        panelTitle = ("Pressure,Cam/Mos,LVDT").split(",");
      } else if (tokens == 29) {
        logType = 2;
        npanels = 4;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt";
        strs[2] = "LVDT Volts,Focus";
        strs[3] = "CamA P,CamA I,CamA D,CamA %Pwr,CamA APwr,Cam P,CamB I,CamB D,CamB %Pwr,CamB APwr";
        panelTitle = ("Pressure,Cam/Mos,LVDT,PIDs").split(",");
      } else if (tokens == 31) {
        logType = 3;
        npanels = 5;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt";
        strs[2] = "LVDT Volts,Focus";
        strs[3] = "CamA P,CamA I,CamA D,CamA %Pwr,CamA APwr,Cam P,CamB I,CamB D,CamB %Pwr,CamB APwr";
        strs[4] = "oiwfsPrb,oiwfsCcd";
        panelTitle = ("Pressure,Cam/Mos,LVDT,PIDs,OIWFS").split(",");
      } else if (tokens == 32) {
        logType = 3;
        npanels = 5;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt";
        strs[2] = "LVDT Volts,Focus";
        strs[3] = "CamA P,CamA I,CamA D,CamA %Pwr,CamA APwr,Cam P,CamB I,CamB D,CamB %Pwr,CamB APwr";
        strs[4] = "oiwfsPrb,oiwfsCcd,oiwfsls3";
        panelTitle = ("Pressure,Cam/Mos,LVDT,PIDs,OIWFS").split(",");
      } else if (tokens == 33) {
        logType = 3;
        npanels = 5;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt";
        strs[2] = "LVDT Volts,Focus";
        strs[3] = "CamA P,CamA I,CamA D,CamA %Pwr,CamA APwr,Cam P,CamB I,CamB D,CamB %Pwr,CamB APwr";
        strs[4] = "oiwfsPrb,oiwfsCcd,baffle1,baffle2";
        panelTitle = ("Pressure,Cam/Mos,LVDT,PIDs,OIWFS/Baffle").split(",");
      } else if (tokens == 34) {
        logType = 3;
        npanels = 5;
        strs = new String[npanels];
        strs[0] = "MosVac,CamVac";
        strs[1] = "1 = Stage 1,2 = Stage 2,3 = Bench,4 = Bench (LS Diode),5 = Stage 1,6 = Bench,7 = Bench,8 = Mini Board,A = Bench,B = Det,CamASetPnt,CamBSetPnt";
        strs[2] = "LVDT Volts,Focus";
        strs[3] = "CamA P,CamA I,CamA D,CamA %Pwr,CamA APwr,Cam P,CamB I,CamB D,CamB %Pwr,CamB APwr";
        strs[4] = "oiwfsPrb,oiwfsCcd,oiwfsls3,baffle1,baffle2";
        panelTitle = ("Pressure,Cam/Mos,LVDT,PIDs,OIWFS/Baffle").split(",");
      }
      graphPanels = new UFHelperPlot[npanels];
      thePanels = new UFHelperPlotPanel[npanels];

      int n = 0;
      for (int j = 0; j < npanels; j++) {
        graphPanels[j] = new UFHelperPlot(strs[j].split(","), n, logName);
        n+=strs[j].split(",").length;
      }

      String[] labels = {"MOS: LS-218 Channels", "Camera: LS-218 Channels", "Camera: LS-232 Channels"};
      int[] labelPos = {0, 4, 8};

      thePanels[0] = new UFHelperPlotPanel(graphPanels, 0);
      thePanels[1] = new UFHelperPlotPanel(graphPanels, 1, labels, labelPos);

      for (int j = 2; j < npanels; j++) {
        thePanels[j] = new UFHelperPlotPanel(graphPanels, j);
      }
    }

    protected void processWindowEvent(WindowEvent wev) {
        System.exit(0);
    }


    public class UFHelperPlotPanel extends JPanel {
      protected JTextField xMinField, xMaxField, yMinField, yMaxField;
      protected JTextField bgColorField, axesColorField, logField;
      protected JTextField[] colorList;
      protected JCheckBox[] plotSensors;
      protected JButton plotButton, colorChooser, logBrowse, saveColorButton;
      protected UFHelperPlot thePlot;
      protected UFHelperPlot[] thePlots;
      protected JComboBox unitsBox;
      protected JLabel x1l,x2l,y1l,y2l,dxl,dyl;
      protected UFColorCombo[] dragObjectBox;
      protected int numSensors;
      protected String[] sensorNames, labels;
      protected int[] labelPos;
      protected JCheckBox ylogBox;

      public UFHelperPlotPanel() {
	super();
      }

      public UFHelperPlotPanel(final UFHelperPlot[] plots, int n) {
	this(plots, n, null, null);
      }

      public UFHelperPlotPanel(final UFHelperPlot[] plots, int n, String[] labels, int[] labelPos) {
	super();
        thePlot = plots[n];
        thePlot.setPlotPanel(this);
        thePlots = plots;
        numSensors = thePlot.numSensors;
	if (numSensors == 0) return;
        sensorNames = thePlot.getSensorNames();
	if (labels != null && labelPos != null) {
	   if (labels.length != labelPos.length) {
	      labels = null;
	      labelPos = null;
	   }
	}
        this.labels = labels;
        this.labelPos = labelPos;
	setupComponents();
	drawComponents();
	startPlot();
      }

      public void setupComponents() {
	LinkedHashMap colors = new LinkedHashMap();
        // see if colors are stored in config file
        try {
            String home = UFExecCommand.getEnvVar("HOME");
            File f = new File(home+"/"+prefsName);
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while (br.ready()) {
                    String s = br.readLine();
                    if (s != null && !s.trim().equals("")) {
                        StringTokenizer st = new StringTokenizer(s);
                        if (st.countTokens() == 2) {
                            String sname = st.nextToken().trim();
			    String rgb = st.nextToken().trim();
			    colors.put(sname.replaceAll("_"," "), rgb);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NewFlam2Helper> "+e.toString());
        }
        thePlot.updateLogFile(logName);

	plotButton = new JButton("Plot");
	plotButton.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      String opts = readOpts();
	      String[] oPlotOpts = readOplotOpts(numSensors);
	      thePlot.updatePlotOpts(opts, oPlotOpts);
	      thePlot.updateUnits((String)(unitsBox.getSelectedItem()));
	      thePlot.updatePlot();
	   }
	});

	/* Color Chooser */
	colorChooser = new JButton("Color Chooser");
	colorChooser.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      final JDialog retVal = new JDialog();
              retVal.setModal(false);
              retVal.setAlwaysOnTop(true);
              retVal.setSize(200,40*(numSensors+2)+10);
              retVal.setLayout(new GridLayout(0,1));
              for (int i=0; i<numSensors; i++) {
		final int myI = i;
		final JLabel showLabel = new JLabel(sensorNames[i]);
		final JButton colorButton = new JButton();
		final Color tempColor = getColor(colorList[i].getText());
		colorButton.setBackground(tempColor);
		colorButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ae) {
                        Color c = JColorChooser.showDialog(retVal,"Choose Color",tempColor);
                        if (c != null) {
			   colorList[myI].setForeground(c);
			   colorList[myI].setText(""+c.getRed()+","+c.getGreen()+","+c.getBlue());
			   colorButton.setBackground(c);
                           getParent().repaint();
                           plotButton.doClick();
			}
                    }
                });
		
		JPanel pan = new JPanel();
		pan.setLayout(new RatioLayout());
		pan.add("0.01,0.01;0.80,0.99",showLabel);
		pan.add("0.81,0.01;0.19,0.99",colorButton);
		retVal.add(pan);
		retVal.setVisible(true);
	      }
	      final JLabel bgshowLabel = new JLabel("BG Color");
	      final JButton bgcolorButton = new JButton();
	      final Color bgtempColor = getColor(bgColorField.getText());
	      bgcolorButton.setBackground(bgtempColor);
	      bgcolorButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		   Color c = JColorChooser.showDialog(retVal,"Choose Color",bgtempColor);
		   if (c != null) {
		      bgColorField.setText(""+c.getRed()+","+c.getGreen()+","+c.getBlue());
		      bgcolorButton.setBackground(c);
		      getParent().repaint();
                      plotButton.doClick();
		   }
		}
	      });
              JPanel bgpan = new JPanel();
              bgpan.setLayout(new RatioLayout());
              bgpan.add("0.01,0.01;0.80,0.99",bgshowLabel);
              bgpan.add("0.81,0.01;0.19,0.99",bgcolorButton);
              retVal.add(bgpan);
              final JLabel axshowLabel = new JLabel("Axes Color");
              final JButton axcolorButton = new JButton();
              final Color axtempColor = getColor(axesColorField.getText());
              axcolorButton.setBackground(axtempColor);
              axcolorButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                   Color c = JColorChooser.showDialog(retVal,"Choose Color",axtempColor);
                   if (c != null) {
                      axesColorField.setText(""+c.getRed()+","+c.getGreen()+","+c.getBlue());
                      axcolorButton.setBackground(c);
                      getParent().repaint();
		      plotButton.doClick();
		   }
                }
              });
              JPanel axpan = new JPanel();
              axpan.setLayout(new RatioLayout());
              axpan.add("0.01,0.01;0.80,0.99",axshowLabel);
              axpan.add("0.81,0.01;0.19,0.99",axcolorButton);
              retVal.add(axpan);
              retVal.setVisible(true);
	   }
	});

	/* Sensors / Colors */
	plotSensors = new JCheckBox[numSensors];
        colorList = new JTextField[numSensors];
	for (int j = 0; j < numSensors; j++) {
	   final int myJ = j;
	   plotSensors[j] = new JCheckBox(sensorNames[j], true);
	   plotSensors[j].addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ev) {
		JCheckBox temp = (JCheckBox)ev.getSource();
		thePlot.showY[myJ] = temp.isSelected();
		thePlot.updatePlot();
	      }
	   });
	}

        final String[] startingColors = {"0,0,0","255,0,0","0,255,0","0,0,255","180,180,0","180,0,180","0,180,180","128,128,128","0,155,0","255,155,0","155,0,0","0,0,155","255,0,155","0,155,255"};
        for (int j = 0; j < numSensors; j++) {
           colorList[j] = new JTextField(8);
	   Color tempColor;
	   if (colors.containsKey(sensorNames[j])) {
	      String temp = (String)colors.get(sensorNames[j]);
	      colorList[j].setText(temp);
	      tempColor = getColor(temp);
	   } else {
 	      colorList[j].setText(startingColors[j%startingColors.length]);
 	      tempColor = getColor(startingColors[j%startingColors.length]);
	   }
	   if (tempColor != null) colorList[j].setForeground(tempColor);
	   final JTextField tempColorList = colorList[j];
	   colorList[j].addFocusListener(new FocusListener() {
	      public void focusGained(FocusEvent fe) {
              }

              public void focusLost(FocusEvent fe) {
		Color tempColor = getColor(tempColorList.getText());
		if (tempColor != null) {
		   tempColorList.setForeground(tempColor);
		   plotButton.doClick();
		}
              }
	   });
	}

	bgColorField = new JTextField(8);
	if (colors.containsKey("BG_Color")) {
	   String temp = (String)colors.get("BG_Color");
	   bgColorField.setText(temp);
	} else bgColorField.setText("255,255,255");
	bgColorField.addFocusListener(new FocusListener() {
	   public void focusGained(FocusEvent fe) {
	   }

	   public void focusLost(FocusEvent fe) {
	      Color tempColor = getColor(bgColorField.getText());
	      if (tempColor != null) {
		plotButton.doClick();
	      }
	   }
	});

	axesColorField = new JTextField(8);
        if (colors.containsKey("Axes_Color")) {
           String temp = (String)colors.get("Axes_Color");
           axesColorField.setText(temp);
        } else axesColorField.setText("0,0,0");
        axesColorField.addFocusListener(new FocusListener() {
           public void focusGained(FocusEvent fe) {
           }

           public void focusLost(FocusEvent fe) {
              Color tempColor = getColor(axesColorField.getText());
              if (tempColor != null) {
                plotButton.doClick();
              }
           }
        });

	/* Save Color Button */
	saveColorButton = new JButton("Save Colors");
	saveColorButton.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      try {
		String home = UFExecCommand.getEnvVar("HOME");
		File f = new File(home+"/"+prefsName);
		LinkedHashMap tempcolors = new LinkedHashMap();
		if (f.exists()) {
                   BufferedReader br = new BufferedReader(new FileReader(f));
		   while (br.ready()) {
		      String s = br.readLine();
		      if (s != null && !s.trim().equals("")) {
			StringTokenizer st = new StringTokenizer(s);
			if (st.countTokens() == 2) {
                            String sname = st.nextToken().trim();
                            String rgb = st.nextToken().trim();
                            tempcolors.put(sname, rgb);
                        }
		      }
		   }
		}
		String key, temp;
		for (int j = 0; j < numSensors; j++) {
		   temp = colorList[j].getText();
		   if (!temp.trim().equals("")) {
		      temp = removeWhitespace(temp);
		      if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
		   }
		   tempcolors.put(sensorNames[j].replaceAll(" ","_"), temp); 
		}
		temp = bgColorField.getText();
		if (!temp.trim().equals("")) {
		   temp = removeWhitespace(temp);
                   if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
		}
		tempcolors.put("BG_Color", temp);
                temp = axesColorField.getText();
                if (!temp.trim().equals("")) {
                   temp = removeWhitespace(temp);
                   if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
                }
                tempcolors.put("Axes_Color", temp);
		PrintWriter pw = new PrintWriter(new FileOutputStream(f));
		for (Iterator i = tempcolors.keySet().iterator(); i.hasNext(); ) {
		   key = (String)i.next();
		   temp = (String)tempcolors.get(key);
		   pw.println(key+" "+temp);
		}
		pw.close();
		JOptionPane.showMessageDialog(null, "The colors have been saved.", "Colors Saved", JOptionPane.INFORMATION_MESSAGE);
	     } catch (Exception e) {
	      System.err.println("NewFlam2Helper> "+e.toString());
	     }
	   }
	});

	/* Ranges */
	xMinField = new JTextField(5);
	xMinField.setText("0");
        xMaxField = new JTextField(5);

	yMinField = new JTextField(5);
	yMaxField = new JTextField(5);

	ylogBox = new JCheckBox("Log");

	/* Units */
        String[] sunits = {"Seconds", "Minutes", "Hours", "Days"};
	unitsBox = new JComboBox(sunits);
	unitsBox.setSelectedItem("Hours");

	/* Browse log */
        logField = new JTextField(40);
	logField.setText(logName);
	logBrowse = new JButton("Browse");
	logBrowse.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      JFileChooser jfc = new JFileChooser(defPath);
	      int returnVal = jfc.showOpenDialog((Component)ev.getSource());
	      if (returnVal == JFileChooser.APPROVE_OPTION) {
		defPath = jfc.getCurrentDirectory().getAbsolutePath();
                logName = jfc.getSelectedFile().getAbsolutePath();
		
		readLog(logName);
		setPlots();
	      }
           }
	});

	/* Drag object pulldowns */
        String[] dragObjectOptions = {"Hidden", "H Line", "V Line", "+", "X", "Circle"};
        int[] dragObjectColor = {UFColorCombo.COLOR_SCHEME_RED, UFColorCombo.COLOR_SCHEME_ORANGE, UFColorCombo.COLOR_SCHEME_GREEN, UFColorCombo.COLOR_SCHEME_BLUE};
        dragObjectBox = new UFColorCombo[4];
        for (int j = 0; j < 4; j++) {
            dragObjectBox[j] = new UFColorCombo(dragObjectOptions, dragObjectColor[j]);
            final int objectNum = j;
            dragObjectBox[j].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    thePlot.dragObjectStatus[objectNum]=(String)dragObjectBox[objectNum].getSelectedItem();
                    thePlot.repaint();
                }
            });
        }

	x1l = new JLabel("X1:");
	x2l = new JLabel("X2:");
	dxl = new JLabel("");
	y1l = new JLabel("Y1:");
	y2l = new JLabel("Y2:");
	dyl = new JLabel("");
      }

      public void drawComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(924, 640));
        JPanel leftPanel = new JPanel();
        SpringLayout leftLayout = new SpringLayout();
        leftPanel.setLayout(leftLayout);

	/* Plot Button */
        leftPanel.add(plotButton);
        leftLayout.putConstraint(SpringLayout.WEST, plotButton, 5, SpringLayout.WEST, leftPanel);
        leftLayout.putConstraint(SpringLayout.NORTH, plotButton, 5, SpringLayout.NORTH, leftPanel);

	/* Color Chooser */
        leftPanel.add(colorChooser);
        leftLayout.putConstraint(SpringLayout.WEST, colorChooser, 10, SpringLayout.EAST, plotButton);
        leftLayout.putConstraint(SpringLayout.NORTH, colorChooser, 5, SpringLayout.NORTH, leftPanel);

	/* Sensors / Colors */
        JLabel fileLabel = new JLabel("Sensors:");
        leftPanel.add(fileLabel);
        leftLayout.putConstraint(SpringLayout.WEST, fileLabel, 5, SpringLayout.WEST, leftPanel);
        leftLayout.putConstraint(SpringLayout.NORTH, fileLabel, 10, SpringLayout.SOUTH, plotButton);
        int maxLength = 0, nmax=0;
        boolean[] hasLabel = new boolean[numSensors];
        JLabel tempLabel = null;
        for (int j = 0; j < numSensors; j++) {
           final int myJ = j;
           hasLabel[j] = false;
           if (labels != null) for (int l = 0; l < labelPos.length; l++) {
              if (myJ == labelPos[l]) {
                tempLabel = new JLabel(labels[l]);
                tempLabel.setBorder(new EtchedBorder());
                leftPanel.add(tempLabel);
                leftLayout.putConstraint(SpringLayout.WEST, tempLabel, 5, SpringLayout.WEST, leftPanel);
                if (j == 0) {
                   leftLayout.putConstraint(SpringLayout.NORTH, tempLabel, 10, SpringLayout.SOUTH, fileLabel);
                } else {
                   leftLayout.putConstraint(SpringLayout.NORTH, tempLabel, 10, SpringLayout.SOUTH, plotSensors[j-1]);
                }
                hasLabel[j] = true;
              }
           }
           if (sensorNames[j].length() > maxLength) {
              maxLength = sensorNames[j].length();
              nmax = j;
           }
           leftPanel.add(plotSensors[j]);
           leftLayout.putConstraint(SpringLayout.WEST, plotSensors[j], 5, SpringLayout.WEST, leftPanel);
           if (hasLabel[j]) {
              leftLayout.putConstraint(SpringLayout.NORTH, plotSensors[j], 10, SpringLayout.SOUTH, tempLabel);
           } else if (j == 0) {
              leftLayout.putConstraint(SpringLayout.NORTH, plotSensors[j], 0, SpringLayout.SOUTH, fileLabel);
           } else {
              leftLayout.putConstraint(SpringLayout.NORTH, plotSensors[j], 0, SpringLayout.SOUTH, plotSensors[j-1]);
           }
        }

        JLabel colorLabel = new JLabel("Colors (R,G,B):");
        leftPanel.add(colorLabel);
        leftLayout.putConstraint(SpringLayout.WEST, colorLabel, 10, SpringLayout.EAST, plotSensors[nmax]);
        leftLayout.putConstraint(SpringLayout.NORTH, colorLabel, 10, SpringLayout.SOUTH, plotButton);
        for (int j = 0; j < numSensors; j++) {
           leftPanel.add(colorList[j]);
           leftLayout.putConstraint(SpringLayout.WEST, colorList[j], 10, SpringLayout.EAST, plotSensors[nmax]);
           if (hasLabel[j]) {
              leftLayout.putConstraint(SpringLayout.NORTH, colorList[j], 4, SpringLayout.NORTH, plotSensors[j]);
           } else if (j == 0) {
              leftLayout.putConstraint(SpringLayout.NORTH, colorList[j], 4, SpringLayout.SOUTH, fileLabel);
           } else {
              leftLayout.putConstraint(SpringLayout.NORTH, colorList[j], 4, SpringLayout.SOUTH, plotSensors[j-1]);
           }
        }

        JLabel bgLabel = new JLabel("BG Color:");
        leftPanel.add(bgLabel);
        leftLayout.putConstraint(SpringLayout.WEST, bgLabel, 5, SpringLayout.WEST, leftPanel);
        leftLayout.putConstraint(SpringLayout.NORTH, bgLabel, 10, SpringLayout.SOUTH, plotSensors[numSensors-1]);
        leftPanel.add(bgColorField);
        leftLayout.putConstraint(SpringLayout.WEST, bgColorField, 10, SpringLayout.EAST, plotSensors[nmax]);
        leftLayout.putConstraint(SpringLayout.NORTH, bgColorField, 10, SpringLayout.SOUTH, plotSensors[numSensors-1]);

        JLabel axesColorLabel = new JLabel("Axes Color:");
        leftPanel.add(axesColorLabel);
        leftLayout.putConstraint(SpringLayout.WEST, axesColorLabel, 5, SpringLayout.WEST, leftPanel);
        leftLayout.putConstraint(SpringLayout.NORTH, axesColorLabel, 10, SpringLayout.SOUTH, bgColorField);
        leftPanel.add(axesColorField);
        leftLayout.putConstraint(SpringLayout.WEST, axesColorField, 10, SpringLayout.EAST, plotSensors[nmax]);
        leftLayout.putConstraint(SpringLayout.NORTH, axesColorField, 10, SpringLayout.SOUTH, bgColorField);

	/* Save Color Button */
        leftPanel.add(saveColorButton);
        leftLayout.putConstraint(SpringLayout.WEST, saveColorButton, 5, SpringLayout.WEST, leftPanel);
        leftLayout.putConstraint(SpringLayout.NORTH, saveColorButton, 10, SpringLayout.SOUTH, axesColorField);
        if (numSensors > 10) {
           leftLayout.putConstraint(SpringLayout.SOUTH, leftPanel, 10, SpringLayout.SOUTH, saveColorButton);
        }

	/* Ranges */
        JPanel bottomPanel = new JPanel();
        SpringLayout bottomLayout = new SpringLayout();
        bottomPanel.setLayout(bottomLayout);
        bottomPanel.setPreferredSize(new Dimension(640, 128));

        JLabel xRangeLabel = new JLabel("x-range:");
        bottomPanel.add(xRangeLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, xRangeLabel, 224, SpringLayout.WEST, bottomPanel);
        bottomLayout.putConstraint(SpringLayout.NORTH, xRangeLabel, 10, SpringLayout.NORTH, bottomPanel);
        bottomPanel.add(xMinField);
        bottomLayout.putConstraint(SpringLayout.WEST, xMinField, 5, SpringLayout.EAST, xRangeLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, xMinField, 8, SpringLayout.NORTH, bottomPanel);
        JLabel xToLabel = new JLabel("to");
        bottomPanel.add(xToLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, xToLabel, 5, SpringLayout.EAST, xMinField);
        bottomLayout.putConstraint(SpringLayout.NORTH, xToLabel, 10, SpringLayout.NORTH, bottomPanel);
        bottomPanel.add(xMaxField);
        bottomLayout.putConstraint(SpringLayout.WEST, xMaxField, 5, SpringLayout.EAST, xToLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, xMaxField, 8, SpringLayout.NORTH, bottomPanel);

        JLabel yRangeLabel = new JLabel("y-range:");
        bottomPanel.add(yRangeLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, yRangeLabel, 10, SpringLayout.EAST, xMaxField);
        bottomLayout.putConstraint(SpringLayout.NORTH, yRangeLabel, 10, SpringLayout.NORTH, bottomPanel);
        bottomPanel.add(yMinField);
        bottomLayout.putConstraint(SpringLayout.WEST, yMinField, 5, SpringLayout.EAST, yRangeLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, yMinField, 8, SpringLayout.NORTH, bottomPanel);
        JLabel yToLabel = new JLabel("to");
        bottomPanel.add(yToLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, yToLabel, 5, SpringLayout.EAST, yMinField);
        bottomLayout.putConstraint(SpringLayout.NORTH, yToLabel, 10, SpringLayout.NORTH, bottomPanel);
        bottomPanel.add(yMaxField);
        bottomLayout.putConstraint(SpringLayout.WEST, yMaxField, 5, SpringLayout.EAST, yToLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, yMaxField, 8, SpringLayout.NORTH, bottomPanel);

	/* Units */
        JLabel unitsLabel = new JLabel("Units:");
        bottomPanel.add(unitsLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, unitsLabel, 15, SpringLayout.EAST, yMaxField);
        bottomLayout.putConstraint(SpringLayout.NORTH, unitsLabel, 10, SpringLayout.NORTH, bottomPanel);
        bottomPanel.add(unitsBox);
        bottomLayout.putConstraint(SpringLayout.WEST, unitsBox, 5, SpringLayout.EAST, unitsLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, unitsBox, 5, SpringLayout.NORTH, bottomPanel);

	//log
	bottomPanel.add(ylogBox);
        bottomLayout.putConstraint(SpringLayout.WEST, ylogBox, 15, SpringLayout.EAST, unitsBox);
        bottomLayout.putConstraint(SpringLayout.NORTH, ylogBox, 5, SpringLayout.NORTH, bottomPanel);
        ylogBox.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ev) {
              plotButton.doClick();
           }
        });

	/* 2nd plot button */
        JButton plotButton2 = new JButton("Plot");
        bottomPanel.add(plotButton2);
        bottomLayout.putConstraint(SpringLayout.WEST, plotButton2, 15, SpringLayout.EAST, ylogBox);
        bottomLayout.putConstraint(SpringLayout.NORTH, plotButton2, 5, SpringLayout.NORTH, bottomPanel);
        plotButton2.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ev) {
              plotButton.doClick();
           }
        });

	/* Add browse log */
        JLabel logLabel = new JLabel("Logfile:");
        bottomPanel.add(logLabel);
        bottomLayout.putConstraint(SpringLayout.WEST, logLabel, 224, SpringLayout.WEST, bottomPanel);
        bottomLayout.putConstraint(SpringLayout.NORTH, logLabel, 15, SpringLayout.SOUTH, xMinField);
        bottomPanel.add(logField);
        bottomLayout.putConstraint(SpringLayout.WEST, logField, 5, SpringLayout.EAST, logLabel);
        bottomLayout.putConstraint(SpringLayout.NORTH, logField, 13, SpringLayout.SOUTH, xMinField);
        bottomPanel.add(logBrowse);
        bottomLayout.putConstraint(SpringLayout.WEST, logBrowse, 5, SpringLayout.EAST, logField);
        bottomLayout.putConstraint(SpringLayout.NORTH, logBrowse, 8, SpringLayout.SOUTH, xMinField);

	/* Add drag objects */
        for (int j = 0; j < 4; j++) {
            bottomPanel.add(dragObjectBox[j]);
            if (j == 0) {
                bottomLayout.putConstraint(SpringLayout.WEST, dragObjectBox[j], 224, SpringLayout.WEST, bottomPanel);
            } else {
                bottomLayout.putConstraint(SpringLayout.WEST, dragObjectBox[j], 10, SpringLayout.EAST, dragObjectBox[j-1]);
            }
            bottomLayout.putConstraint(SpringLayout.NORTH, dragObjectBox[j], 15, SpringLayout.SOUTH, logField);
        }


	/* Add drag object xy labels */
        bottomPanel.add(x1l);
        bottomLayout.putConstraint(SpringLayout.WEST, x1l, 10, SpringLayout.EAST, dragObjectBox[3]);
        bottomLayout.putConstraint(SpringLayout.NORTH, x1l, 10, SpringLayout.SOUTH, logField);
        bottomPanel.add(x2l);
        bottomLayout.putConstraint(SpringLayout.WEST, x2l, 10, SpringLayout.EAST, x1l);
        bottomLayout.putConstraint(SpringLayout.NORTH, x2l, 10, SpringLayout.SOUTH, logField);
        bottomPanel.add(dxl);
        bottomLayout.putConstraint(SpringLayout.WEST, dxl, 10, SpringLayout.EAST, x2l);
        bottomLayout.putConstraint(SpringLayout.NORTH, dxl, 10, SpringLayout.SOUTH, logField);
        bottomPanel.add(y1l);
        bottomLayout.putConstraint(SpringLayout.WEST, y1l, 10, SpringLayout.EAST, dragObjectBox[3]);
        bottomLayout.putConstraint(SpringLayout.NORTH, y1l, 10, SpringLayout.SOUTH, x1l);
        bottomPanel.add(y2l);
        bottomLayout.putConstraint(SpringLayout.WEST, y2l, 10, SpringLayout.EAST, y1l);
        bottomLayout.putConstraint(SpringLayout.NORTH, y2l, 10, SpringLayout.SOUTH, x1l);
        bottomPanel.add(dyl);
        bottomLayout.putConstraint(SpringLayout.WEST, dyl, 10, SpringLayout.EAST, y2l);
        bottomLayout.putConstraint(SpringLayout.NORTH, dyl, 10, SpringLayout.SOUTH, x1l);

        JScrollPane sp = new JScrollPane(leftPanel);
        sp.setPreferredSize(new Dimension(284, 512));
        add(thePlot, BorderLayout.CENTER);
        add(sp, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
      }

      public void startPlot() {
        //pass initial options to plot
        thePlot.updateLogFile(logField.getText().trim());
        String opts = readOpts();
        String[] oPlotOpts = readOplotOpts(numSensors);
        thePlot.updatePlotOpts(opts, oPlotOpts);
        thePlot.updateUnits((String)(unitsBox.getSelectedItem()));
        //start reading file
        thePlot.startThread();
      }

      public String removeWhitespace(String s) {
	while (s.indexOf("\t") != -1) s = s.replaceAll("\t"," ");
	while (s.indexOf("  ") != -1) {
	   s = s.replaceAll("  "," ");
	}
	s = s.trim();
	return s;
      }

      public String readOpts() {
	String s = "";
        String temp;
        temp = xMinField.getText();
        if (!temp.trim().equals("")) s+="*xminval="+temp+", ";
        temp = xMaxField.getText();
        if (!temp.trim().equals("")) s+="*xmaxval="+temp+", ";
        temp = yMinField.getText();
        if (!temp.trim().equals("")) s+="*yminval="+temp+", ";
        temp = yMaxField.getText();
        if (!temp.trim().equals("")) s+="*ymaxval="+temp+", ";
	temp = bgColorField.getText();
	if (!temp.trim().equals("")) {
	   temp = removeWhitespace(temp);
	   if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
	   s+="*background="+temp+", ";
	   int colorLen = temp.split(",").length;
	   if (colorLen < 3) {
	      for (int l = 0; l < 3-colorLen; l++) s+=temp.substring(temp.lastIndexOf(",")+1)+", ";
	   }
	}
        temp = axesColorField.getText();
        if (!temp.trim().equals("")) {
           temp = removeWhitespace(temp);
           if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
           s+="*axescolor="+temp+", ";
           int colorLen = temp.split(",").length;
           if (colorLen < 3) {
              for (int l = 0; l < 3-colorLen; l++) s+=temp.substring(temp.lastIndexOf(",")+1)+", ";
           }
        }
	if (ylogBox.isSelected()) s+="*ylog, "; else s+="*ylinear, ";
	return s;
      }

      public String[] readOplotOpts(int numSensors) {
        String temp;
        String[] oPlotOpts = new String[numSensors];
        for (int j = 0; j < numSensors; j++) {
           oPlotOpts[j] = "";
           temp = colorList[j].getText();
           if (!temp.trim().equals("")) {
              temp = removeWhitespace(temp);
              if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
              oPlotOpts[j]+="*color="+temp+", ";
              int colorLen = temp.split(",").length;
              if (colorLen < 3) {
                for (int l = 0; l < 3-colorLen; l++) oPlotOpts[j]+=temp.substring(temp.lastIndexOf(",")+1)+", ";
              }
           }
	   if (ylogBox.isSelected()) oPlotOpts[j] += "*ylog, ";
        }
        return oPlotOpts;
      }

      public Color getColor(String temp) {
	if (!temp.trim().equals("")) {
	   temp = removeWhitespace(temp);
	}
	if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ", ",");
	String[] temprgb = temp.split(",");
	if (temprgb.length < 3) return Color.BLACK;
	int r = Integer.parseInt(temprgb[0].trim());
        int g = Integer.parseInt(temprgb[1].trim());
        int b = Integer.parseInt(temprgb[2].trim());
	return new Color(r,g,b);
      }

      public void plot(String s) {
	thePlot.updatePlot();
      }
    }

    public static void main(String [] args) {
	new NewFlam2Helper(args);
    }
}
