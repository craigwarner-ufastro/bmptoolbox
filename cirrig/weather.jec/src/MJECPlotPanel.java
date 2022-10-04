package wthjec;

/**
 * Title:        MJECPlotPanel.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  panel for MJEC plots 
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

public class MJECPlotPanel extends JPanel { 
    public static final
        String rcsID = "$Name:  $ $Id: MJECPlotPanel.java,v 1.1 2011/01/20 23:08:47 warner Exp $";

    protected WthjecFrame wthjec;
    protected String[] recsToPlot;
    protected MJECPlot thePlot;
    protected JTextField bgColorField, axesColorField;
    protected JTextField[] colorList;
    protected JCheckBox[] plotSensors;
    protected JButton colorChooser, saveColorButton;
    protected JComboBox unitsBox;
    protected JLabel x1l,x2l,y1l,y2l,dxl,dyl;
    protected UFColorCombo[] dragObjectBox;
    protected int numSensors;
    protected String[] sensorNames;

    public MJECPlotPanel(WthjecFrame wthjecFrame, String[] recNames, String[] labels) {
	super();
	wthjec = wthjecFrame;
	recsToPlot = recNames;
	thePlot = new MJECPlot(wthjec, recsToPlot); 
	numSensors = recNames.length;
	if (labels == null) {
	  sensorNames = new String[numSensors];
	  for (int j = 0; j < numSensors; j++) {
	    sensorNames[j] = recsToPlot[j].substring(recsToPlot[j].lastIndexOf(":")+1); 
	  }
	} else sensorNames = labels;
	setupComponents();
    }

    public MJECPlotPanel(WthjecFrame wthjecFrame, String[] recNames) {
      this(wthjecFrame, recNames, null);
    }

    public void setupComponents() {
        LinkedHashMap colors = new LinkedHashMap();
        setLayout(new BorderLayout());
        //setPreferredSize(new Dimension(924, 640));
        JPanel rightPanel = new JPanel();
        SpringLayout rightLayout = new SpringLayout();
        rightPanel.setLayout(rightLayout);

        /* Sensors / Colors */
        plotSensors = new JCheckBox[numSensors];
        colorList = new JTextField[numSensors];
        JLabel fileLabel = new JLabel("Sensors:");
        rightPanel.add(fileLabel);
        rightLayout.putConstraint(SpringLayout.WEST, fileLabel, 5, SpringLayout.WEST, rightPanel);
        rightLayout.putConstraint(SpringLayout.NORTH, fileLabel, 5, SpringLayout.NORTH, rightPanel);
        int maxLength = 0, nmax=0;
        for (int j = 0; j < numSensors; j++) {
           final int myJ = j;
           if (sensorNames[j].length() > maxLength) {
              maxLength = sensorNames[j].length();
              nmax = j;
           }
           plotSensors[j] = new JCheckBox(sensorNames[j], true);
           plotSensors[j].addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ev) {
                JCheckBox temp = (JCheckBox)ev.getSource();
                thePlot.setShowY(myJ, temp.isSelected());
                updatePlot();
              }
           });
           rightPanel.add(plotSensors[j]);
           rightLayout.putConstraint(SpringLayout.WEST, plotSensors[j], 5, SpringLayout.WEST, rightPanel);
           if (j == 0) {
              rightLayout.putConstraint(SpringLayout.NORTH, plotSensors[j], 0, SpringLayout.SOUTH, fileLabel);
           } else {
              rightLayout.putConstraint(SpringLayout.NORTH, plotSensors[j], 0, SpringLayout.SOUTH, plotSensors[j-1]);
           }
        }

        JLabel colorLabel = new JLabel("Colors (R,G,B):");
        rightPanel.add(colorLabel);
        rightLayout.putConstraint(SpringLayout.WEST, colorLabel, 10, SpringLayout.EAST, plotSensors[nmax]);
        rightLayout.putConstraint(SpringLayout.NORTH, colorLabel, 5, SpringLayout.NORTH, rightPanel);
        final String[] startingColors = {"0,0,0","255,0,0","0,255,0","0,0,255","180,180,0","180,0,180","0,180,180","128,128,128","0,155,0","255,155,0","155,0,0","0,0,155","255,0,155","0,155,255"};
        for (int j = 0; j < numSensors; j++) {
           colorList[j] = new JTextField(7);
           Color tempColor;
           if (colors.containsKey(sensorNames[j])) {
              String temp = (String)colors.get(sensorNames[j]);
              colorList[j].setText(temp);
              tempColor = getColor(temp);
           } else {
              colorList[j].setText(startingColors[j]);
              tempColor = getColor(startingColors[j]);
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
		   updatePlot();
                }
              }
           });
           rightPanel.add(colorList[j]);
           rightLayout.putConstraint(SpringLayout.WEST, colorList[j], 10, SpringLayout.EAST, plotSensors[nmax]);
           if (j == 0) {
              rightLayout.putConstraint(SpringLayout.NORTH, colorList[j], 4, SpringLayout.SOUTH, fileLabel);
           } else {
              rightLayout.putConstraint(SpringLayout.NORTH, colorList[j], 4, SpringLayout.SOUTH, plotSensors[j-1]);
           }
        }

        JLabel bgLabel = new JLabel("BG Color:");
        rightPanel.add(bgLabel);
        rightLayout.putConstraint(SpringLayout.WEST, bgLabel, 10, SpringLayout.WEST, rightPanel);
        rightLayout.putConstraint(SpringLayout.NORTH, bgLabel, 4, SpringLayout.SOUTH, plotSensors[numSensors-1]);
        bgColorField = new JTextField(7);
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
                updatePlot();
              }
           }
        });
        rightPanel.add(bgColorField);
        rightLayout.putConstraint(SpringLayout.WEST, bgColorField, 10, SpringLayout.EAST, plotSensors[nmax]);
        rightLayout.putConstraint(SpringLayout.NORTH, bgColorField, 4, SpringLayout.SOUTH, plotSensors[numSensors-1]);

        JLabel axesColorLabel = new JLabel("Axes Color:");
        rightPanel.add(axesColorLabel);
        rightLayout.putConstraint(SpringLayout.WEST, axesColorLabel, 10, SpringLayout.WEST, rightPanel);
        rightLayout.putConstraint(SpringLayout.NORTH, axesColorLabel, 4, SpringLayout.SOUTH, bgColorField);
        axesColorField = new JTextField(7);
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
                updatePlot();
              }
           }
        });
        rightPanel.add(axesColorField);
        rightLayout.putConstraint(SpringLayout.WEST, axesColorField, 10, SpringLayout.EAST, plotSensors[nmax]);
        rightLayout.putConstraint(SpringLayout.NORTH, axesColorField, 4, SpringLayout.SOUTH, bgColorField);


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
                           updatePlot();
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
                      updatePlot();
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
                      updatePlot();
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
        rightPanel.add(colorChooser);
        rightLayout.putConstraint(SpringLayout.WEST, colorChooser, 5, SpringLayout.WEST, rightPanel);
        rightLayout.putConstraint(SpringLayout.NORTH, colorChooser, 5, SpringLayout.SOUTH, axesColorField); 

        /* Units */
        JLabel unitsLabel = new JLabel("Units:");
        rightPanel.add(unitsLabel);
        rightLayout.putConstraint(SpringLayout.WEST, unitsLabel, 5, SpringLayout.WEST, rightPanel);
        rightLayout.putConstraint(SpringLayout.NORTH, unitsLabel, 14, SpringLayout.SOUTH, colorChooser); 
        String[] sunits = {"Seconds", "Minutes", "Hours", "Days"};
        unitsBox = new JComboBox(sunits);
        unitsBox.setSelectedItem("Minutes");
        unitsBox.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ev) {
              updatePlot();
           }
	});
        rightPanel.add(unitsBox);
        rightLayout.putConstraint(SpringLayout.WEST, unitsBox, 10, SpringLayout.EAST, unitsLabel);
        rightLayout.putConstraint(SpringLayout.NORTH, unitsBox, 9, SpringLayout.SOUTH, colorChooser);

        JScrollPane sp = new JScrollPane(rightPanel);
        sp.setPreferredSize(new Dimension(192, 330));
        add(thePlot, BorderLayout.CENTER);
        add(sp, BorderLayout.EAST);
    }

    public void startPlot() {
        //pass initial options to plot
        String opts = readOpts();
        String[] oPlotOpts = readOplotOpts(numSensors);
        thePlot.updatePlotOpts(opts, oPlotOpts);
        thePlot.updateUnits((String)(unitsBox.getSelectedItem()));
        //start reading file
        thePlot.startThread();
    }

    public void updatePlot() {
	String opts = readOpts();
	String[] oPlotOpts = readOplotOpts(numSensors);
	thePlot.updatePlotOpts(opts, oPlotOpts);
	thePlot.updateUnits((String)(unitsBox.getSelectedItem()));
	thePlot.updatePlot();
    }

    public String readOpts() {
        String s = "";
        String temp;
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

    public String removeWhitespace(String s) {
        while (s.indexOf("\t") != -1) s = s.replaceAll("\t"," ");
        while (s.indexOf("  ") != -1) {
           s = s.replaceAll("  "," ");
        }
        s = s.trim();
        return s;
    }

}
