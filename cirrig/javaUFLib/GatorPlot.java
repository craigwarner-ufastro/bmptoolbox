package javaUFLib;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.border.*;

public class GatorPlot extends javax.swing.JFrame {
   public UFPlotPanel thePlot;
   public JPanel leftPanel, bottomPanel;
   public int xdim, ydim;
   JPopupMenu menu;
   JMenuItem printItem;
   float[] x, y, oldX, oldY, tabX, tabY;
   String opts, oldOpts;
   public JButton plotButton, quitButton, browseFiles, addText, UFButton;
   JButton multiButton, optsButton, histButton, linFitButton;
   JButton colorChooser;
   JRadioButton plotData, plotFiles, xLin, xLog, yLin, yLog;
   JRadioButton plotTable;
   ButtonGroup plotType, xLinLog, yLinLog;
   JTextArea fileList, psymList, colorList;
   JTextField titleField, xtitleField, ytitleField, xcolField, ycolField;
   public JTextField charSizeField, xMinField, xMaxField, yMinField, yMaxField;
   JTextField bgColorField, histField, nbins, binsize;
   JCheckBox isFilled, showOPlot;
   int xticks=0, yticks=0, xminor=0, yminor=0;
   float xtickInt=0, ytickInt=0, xtickLen=0, ytickLen=0, symsize=0;
   float[] xtickVals, ytickVals, xmargin, ymargin, position;
   String[] xtickNames, ytickNames;
   String fontName="", axesColor="";
   public boolean showPanels = true, autoOPlot = false;
   //float[][] oxs, oys;
   Vector oxs, oys, oOpts;
   String[] psymOpts, colorOpts;
   int nplot = 0;
   boolean autoSetVisible = true;

   public GatorPlot() {
      this(640, 512, "GatorPlot");
   }

   public GatorPlot(String title) {
      this(640, 512, title);
   }

   public GatorPlot(int xdim, int ydim) {
      this(xdim, ydim, "GatorPlot");
   }

   public GatorPlot(int xdim, int ydim, String title) {
      super(title);
      this.xdim = xdim;
      this.ydim = ydim;
      this.opts = "";
      this.xtickVals = new float[1];
      this.ytickVals = new float[1];
      this.xmargin = new float[1];
      this.ymargin = new float[1];
      this.position = new float[1];
      this.xtickNames = new String[1];
      this.xtickNames[0] = "";
      this.ytickNames = new String[1];
      this.ytickNames[0] = "";
      this.oxs = new Vector();
      this.oys = new Vector();
      this.oOpts = new Vector();
      setSize(xdim + 160, ydim + 128);
      Container content = getContentPane();
      content.setBackground(Color.black);
      content.setLayout(new BorderLayout());
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
	   GatorPlot.this.dispose();
        }
      });
      addComponentListener(new ComponentAdapter() {
	public void componentResized(ComponentEvent ev) {
	   GatorPlot ufp = (GatorPlot)ev.getSource();
	   if (showPanels) ufp.thePlot.resizePlot(ufp.getWidth()-164, ufp.getHeight()-154);
	   else ufp.thePlot.resizePlot(ufp.getWidth()-4, ufp.getHeight()-26);
        }
      });
      leftPanel = new JPanel();
      leftPanel.setPreferredSize(new Dimension(160, ydim));
      SpringLayout leftLayout = new SpringLayout();
      leftPanel.setLayout(leftLayout);
      plotButton = new JButton("Plot");
      plotButton.setToolTipText("Click to plot the selected data.  To do a HISTOGRAM, click the Histogram button.");

      plotButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   if (plotData.isSelected()) {
	      if (y != null) {
		plot("*nodata, " + readOpts() + opts);
		String temp;
                temp = psymList.getText();
                if (!temp.trim().equals("")) {
                   psymOpts = temp.trim().split("\n");
                } else {
                   psymOpts = new String[1];
                   psymOpts[0] = "";
                }
                temp = colorList.getText();
                if (!temp.trim().equals("")) {
                   colorOpts = temp.trim().split("\n");
                } else {
                   colorOpts = new String[1];
                   colorOpts[0] = "";
                }
		overplot(x, y, readOPlotOpts(0) + opts);
		if (showOPlot.isSelected()) autoOPlot = true; else autoOPlot = false;
                if (autoOPlot) {
		   for (int j = 0; j < oxs.size(); j++) {
		      overplot((float[])oxs.get(j), (float[])oys.get(j), (String)oOpts.get(j));
		   }
                }
	      } else {
		float[] tempy = {0, 1};
		String tempOpts = opts;
		plot(tempy, "*nodata");
		opts = tempOpts;
		y = null;
	 	xyouts(0.5f, 0.5f, "No Internal Data", "*normal");
	      }
	   } else if (plotFiles.isSelected()) {
	      if (fileList.getText().equals("")) {
                float[] tempy = {0, 1};
                String tempOpts = opts;
                plot(tempy, "*nodata");
                opts = tempOpts;
                xyouts(0.5f, 0.5f, "No Files Listed", "*normal");
	      }
	      else {
		oldX = x;
		oldY = y;
		oldOpts = opts;
		String[] files = fileList.getText().split("\n");
		String temp;
		int xcol, ycol;
		temp = xcolField.getText();
		if (temp.equals("")) xcol = -1;
		else xcol = Integer.parseInt(temp)-1;
		temp = ycolField.getText();
		if (temp.equals("")) ycol = 0;
		else ycol = Integer.parseInt(temp)-1;
		readFile(files[0], xcol, ycol);
		if (! new File(files[0]).exists()) return;
		plot("*nodata, "+readOpts());
		temp = psymList.getText();
	        if (!temp.trim().equals("")) {
		   psymOpts = temp.trim().split("\n");
		} else {
		   psymOpts = new String[1];
		   psymOpts[0] = "";
		}
                temp = colorList.getText();
                if (!temp.trim().equals("")) {
                   colorOpts = temp.trim().split("\n");
                } else {
		   colorOpts = new String[1];
		   colorOpts[0] = "";
		}
		for (int j = 0; j < files.length; j++) {
                   readFile(files[j], xcol, ycol);
		   overplot(x, y, readOPlotOpts(j));
		}
	      }
	      x = oldX;
	      y = oldY;
	      opts = oldOpts;
	   }
	   else if (plotTable.isSelected()) {
	      final JFrame tableFrame = new JFrame("Data Table");
	      final JTextArea data = new JTextArea(25,40);
	      JScrollPane spData = new JScrollPane(data);
	      if (tabY != null) {
                for (int j = 0; j < tabY.length; j++) {
                   if (tabX != null) data.append(""+tabX[j]+"\t");
                   data.append(""+tabY[j]);
                   data.append("\n");
                }
              }
	      JButton bPlot = new JButton("Plot");
	      bPlot.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
		   oldX = x;
		   oldY = y;
		   oldOpts = opts;
		   if (data.getText().trim() == "") {
		     tabX = null;
		     tabY = null;
		     tableFrame.dispose();
		     return;
		   }
		   String[] s = data.getText().split("\n");
		   if (s[0].trim().equals("")) {
		      tabX = null;
		      tabY = null;
		      tableFrame.dispose();
		      return;
		   }
		   String[] row;
		   int n = s.length;
		   x = new float[n];
		   y = new float[n];
		   for (int j = 0; j < n; j++) {
		      s[j] = removeWhitespace(s[j]);
		      row = s[j].split(" ");
		      if (row.length == 1) {
			x[j] = j;
			y[j] = Float.parseFloat(row[0]);
		      } else {
			x[j] = Float.parseFloat(row[0]);
			y[j] = Float.parseFloat(row[1]);
		      }
		   }
                   tableFrame.dispose();
		   plot("*nodata, "+readOpts());
		   String temp;
		   temp = psymList.getText();
                   if (!temp.trim().equals("")) {
		      psymOpts = temp.trim().split("\n");
                   } else {
                      psymOpts = new String[1];
                      psymOpts[0] = "";
                   }
                   temp = colorList.getText();
                   if (!temp.trim().equals("")) {
                      colorOpts = temp.trim().split("\n");
                   } else {
                      colorOpts = new String[1];
                      colorOpts[0] = "";
                   }
                   overplot(x, y, readOPlotOpts(0));
		   tabX = x;
		   tabY = y;
		   x = oldX;
		   y = oldY;
		   opts = oldOpts;
                }
              });
	      JButton bInit = new JButton("Show Init Data");
	      bInit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                   data.setText("");
		   if (y != null) {
		      for (int j = 0; j < y.length; j++) {
			if (x != null) data.append(""+x[j]+"\t");
                   	data.append(""+y[j]);
                   	data.append("\n");
		      }
		   }
                }
              });
	      JButton bClear = new JButton("Clear");
	      bClear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		   data.setText("");
		}
	      });
	      JButton bCancel = new JButton("Cancel");
	      bCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
		   tableFrame.dispose();
                }
              });
	      SpringLayout tableLayout = new SpringLayout();
	      Container content = tableFrame.getContentPane();
	      content.setLayout(tableLayout);
	      content.add(spData);
              tableLayout.putConstraint(SpringLayout.WEST, spData, 5, SpringLayout.WEST, content);
              tableLayout.putConstraint(SpringLayout.NORTH, spData, 5, SpringLayout.NORTH, content);
	      content.add(bPlot);
              tableLayout.putConstraint(SpringLayout.WEST, bPlot, 15, SpringLayout.WEST, content);
              tableLayout.putConstraint(SpringLayout.NORTH, bPlot, 15, SpringLayout.SOUTH, spData);
              content.add(bInit);
              tableLayout.putConstraint(SpringLayout.WEST, bInit, 15, SpringLayout.EAST, bPlot);
              tableLayout.putConstraint(SpringLayout.NORTH, bInit, 15, SpringLayout.SOUTH, spData);
              content.add(bClear);
              tableLayout.putConstraint(SpringLayout.WEST, bClear, 15, SpringLayout.EAST, bInit);
              tableLayout.putConstraint(SpringLayout.NORTH, bClear, 15, SpringLayout.SOUTH, spData);
              content.add(bCancel);
              tableLayout.putConstraint(SpringLayout.WEST, bCancel, 15, SpringLayout.EAST, bClear);
              tableLayout.putConstraint(SpringLayout.NORTH, bCancel, 15, SpringLayout.SOUTH, spData);
              tableLayout.putConstraint(SpringLayout.EAST, content, 5, SpringLayout.EAST, spData);
              tableLayout.putConstraint(SpringLayout.SOUTH, content, 10, SpringLayout.SOUTH, bPlot);
	      tableFrame.pack();
	      tableFrame.setVisible(true);
	   }
	}
      });

      quitButton = new JButton("Quit");

      quitButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	  GatorPlot.this.dispose();
	}
      });

      plotData = new JRadioButton("Initial Data", true);
      plotFiles = new JRadioButton("Plot File(s)");
      plotTable = new JRadioButton("Data Table");
      plotType = new ButtonGroup();
      plotType.add(plotData);
      plotType.add(plotFiles);
      plotType.add(plotTable);
      fileList = new JTextArea(5, 14);
      browseFiles = new JButton("Browse");

      browseFiles.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   JFileChooser jfc = new JFileChooser(".");
	   jfc.setMultiSelectionEnabled(true);
	   int returnVal = jfc.showOpenDialog((Component)ev.getSource());
	   if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File[] f = jfc.getSelectedFiles();
	      for (int j = 0; j < f.length; j++)
		fileList.append(f[j].getAbsolutePath() + "\n");
	   }
	}
      });

      titleField = new JTextField(10);
      xtitleField = new JTextField(9);
      ytitleField = new JTextField(9);
      psymList = new JTextArea(5,4);
      colorList = new JTextArea(5,8);
      xcolField = new JTextField(2);
      ycolField = new JTextField(2);

      leftPanel.add(plotButton);
      leftLayout.putConstraint(SpringLayout.WEST, plotButton, 15, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, plotButton, 5, SpringLayout.NORTH, leftPanel);
      leftPanel.add(quitButton);
      leftLayout.putConstraint(SpringLayout.WEST, quitButton, 10, SpringLayout.EAST, plotButton);
      leftLayout.putConstraint(SpringLayout.NORTH, quitButton, 5, SpringLayout.NORTH, leftPanel);
      leftPanel.add(plotData);
      leftLayout.putConstraint(SpringLayout.WEST, plotData, 15, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, plotData, 4, SpringLayout.SOUTH, plotButton);
      leftPanel.add(plotFiles);
      leftLayout.putConstraint(SpringLayout.WEST, plotFiles, 15, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, plotFiles, 0, SpringLayout.SOUTH, plotData);
      leftPanel.add(plotTable);
      leftLayout.putConstraint(SpringLayout.WEST, plotTable, 15, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, plotTable, 0, SpringLayout.SOUTH, plotFiles);
      JLabel filesLabel = new JLabel("File(s):");
      leftPanel.add(filesLabel);
      leftLayout.putConstraint(SpringLayout.WEST, filesLabel, 12, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, filesLabel, 10, SpringLayout.SOUTH, plotTable);
      leftPanel.add(browseFiles);
      leftLayout.putConstraint(SpringLayout.WEST, browseFiles, 6, SpringLayout.EAST, filesLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, browseFiles, 4, SpringLayout.SOUTH, plotTable);
      JScrollPane spFileList = new JScrollPane(fileList);
      leftPanel.add(spFileList);
      leftLayout.putConstraint(SpringLayout.WEST, spFileList, 2, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, spFileList, 6, SpringLayout.SOUTH, browseFiles);
      JLabel xcolLabel = new JLabel("x-col:");
      leftPanel.add(xcolLabel);
      leftLayout.putConstraint(SpringLayout.WEST, xcolLabel, 5, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, xcolLabel, 6, SpringLayout.SOUTH, spFileList);
      leftPanel.add(xcolField);
      leftLayout.putConstraint(SpringLayout.WEST, xcolField, 5, SpringLayout.EAST, xcolLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, xcolField, 6, SpringLayout.SOUTH, spFileList);
      JLabel ycolLabel = new JLabel("y-col:");
      leftPanel.add(ycolLabel);
      leftLayout.putConstraint(SpringLayout.WEST, ycolLabel, 12, SpringLayout.EAST, xcolField);
      leftLayout.putConstraint(SpringLayout.NORTH, ycolLabel, 6, SpringLayout.SOUTH, spFileList);
      leftPanel.add(ycolField);
      leftLayout.putConstraint(SpringLayout.WEST, ycolField, 5, SpringLayout.EAST, ycolLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, ycolField, 6, SpringLayout.SOUTH, spFileList);
      JLabel titleLabel = new JLabel("Title:");
      leftPanel.add(titleLabel);
      leftLayout.putConstraint(SpringLayout.WEST, titleLabel, 5, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, titleLabel, 10, SpringLayout.SOUTH, xcolLabel);
      leftPanel.add(titleField);
      leftLayout.putConstraint(SpringLayout.WEST, titleField, 5, SpringLayout.EAST, titleLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, titleField, 10, SpringLayout.SOUTH, xcolLabel);
      JLabel xtitleLabel = new JLabel("x-title:");
      leftPanel.add(xtitleLabel);
      leftLayout.putConstraint(SpringLayout.WEST, xtitleLabel, 5, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, xtitleLabel, 10, SpringLayout.SOUTH, titleLabel);
      leftPanel.add(xtitleField);
      leftLayout.putConstraint(SpringLayout.WEST, xtitleField, 5, SpringLayout.EAST, xtitleLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, xtitleField, 10, SpringLayout.SOUTH, titleLabel);
      JLabel ytitleLabel = new JLabel("y-title:");
      leftPanel.add(ytitleLabel);
      leftLayout.putConstraint(SpringLayout.WEST, ytitleLabel, 5, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, ytitleLabel, 10, SpringLayout.SOUTH, xtitleLabel);
      leftPanel.add(ytitleField);
      leftLayout.putConstraint(SpringLayout.WEST, ytitleField, 5, SpringLayout.EAST, ytitleLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, ytitleField, 10, SpringLayout.SOUTH, xtitleLabel);
      JLabel psymLabel = new JLabel("Psym: ");
      leftPanel.add(psymLabel);
      leftLayout.putConstraint(SpringLayout.WEST, psymLabel, 10, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, psymLabel, 10, SpringLayout.SOUTH, ytitleLabel);
      JLabel colorLabel = new JLabel("Colors (R,G,B):");
      leftPanel.add(colorLabel);
      leftLayout.putConstraint(SpringLayout.WEST, colorLabel, 15, SpringLayout.EAST, psymLabel);
      leftLayout.putConstraint(SpringLayout.NORTH, colorLabel, 10, SpringLayout.SOUTH, ytitleLabel);
      JScrollPane spPsymList = new JScrollPane(psymList);
      leftPanel.add(spPsymList);
      leftLayout.putConstraint(SpringLayout.WEST, spPsymList, 5, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, spPsymList, 5, SpringLayout.SOUTH, psymLabel);
      JScrollPane spColorList = new JScrollPane(colorList);
      leftLayout.putConstraint(SpringLayout.WEST, spColorList, 10, SpringLayout.EAST, spPsymList);
      leftLayout.putConstraint(SpringLayout.NORTH, spColorList, 5, SpringLayout.SOUTH, psymLabel);
      leftPanel.add(spColorList);

      linFitButton = new JButton("Lin Fit");
      leftPanel.add(linFitButton);
      leftLayout.putConstraint(SpringLayout.WEST, linFitButton, 3, SpringLayout.WEST, leftPanel);
      leftLayout.putConstraint(SpringLayout.NORTH, linFitButton, 12, SpringLayout.SOUTH, spPsymList);
      linFitButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   if (thePlot.x != null && thePlot.y != null) {
	      double[] coeffs = UFMathLib.LinLsqFit(thePlot.x, thePlot.y);
	      String intcpt = (""+coeffs[0]);
	      if (intcpt.indexOf(".") != -1 && intcpt.length() > intcpt.indexOf(".")+4) intcpt = intcpt.substring(0, intcpt.indexOf(".")+4);
              String slope = (""+coeffs[1]);
              if (slope.indexOf(".") != -1 && slope.length() > slope.indexOf(".")+4) slope = slope.substring(0, slope.indexOf(".")+4);
	      String out = "Y = "+intcpt+" ";
	      if (!slope.startsWith("-")) out+="+ ";
	      out+=slope+" X";
              xyouts(0.12f, 0.1f, out, "*normal");
	      float[] fitY = UFArrayOps.addArrays(UFArrayOps.multArrays(thePlot.x, (float)coeffs[1]), (float)coeffs[0]);
	      overplot(thePlot.x, fitY, readOPlotOpts(1));
	   }
	}
      });

      colorChooser = new JButton("Colors");
      leftPanel.add(colorChooser);
      leftLayout.putConstraint(SpringLayout.WEST, colorChooser, 4, SpringLayout.EAST, linFitButton);
      leftLayout.putConstraint(SpringLayout.NORTH, colorChooser, 12, SpringLayout.SOUTH, spPsymList);
      colorChooser.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   final JDialog retVal = new JDialog(GatorPlot.this,"Color Chooser");
           retVal.setModal(false);
           //retVal.setAlwaysOnTop(true);
           retVal.setLayout(new GridLayout(0,1));
	   if (plotData.isSelected() || plotTable.isSelected()) {
              retVal.setSize(200,40*3);
              String temp = colorList.getText();
	      String[] colorOpts;
	      if (!temp.trim().equals("")) {
		colorOpts = temp.trim().split("\n");
	      } else {
		colorOpts = new String[1];
		colorOpts[0] = "";
	      }
	      final JLabel plotshowLabel = new JLabel("Plot Color");
              final JButton plotcolorButton = new JButton();
              final Color plottempColor = getColor(colorOpts[0]);
              plotcolorButton.setBackground(plottempColor);
              plotcolorButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		   Color c = JColorChooser.showDialog(retVal,"Choose Color",plottempColor);
		   if (c != null) {
		      colorList.setText(""+c.getRed()+","+c.getGreen()+","+c.getBlue());
		      plotcolorButton.setBackground(c);
                      plotButton.doClick();
		   }
		}
	      });
	      JPanel plotpan = new JPanel();
              plotpan.setLayout(new GridLayout());
              plotpan.add(plotshowLabel);
              plotpan.add(plotcolorButton);
              retVal.add(plotpan);
	   } else if (plotFiles.isSelected()) {
              String[] files = fileList.getText().split("\n");
	      retVal.setSize(200,40*(files.length+2));
              String temp = colorList.getText();
              String[] colorOpts;
              if (!temp.trim().equals("")) {
                colorOpts = temp.trim().split("\n");
              } else {
                colorOpts = new String[1];
                colorOpts[0] = "";
              }
	      for (int i = 0; i < files.length; i++) {
                final int myI = i;
                final JLabel showLabel = new JLabel("Plot "+myI);
                final JButton colorButton = new JButton();
		String tempc = "";
		if (colorOpts.length > myI) tempc = colorOpts[myI];
                final Color tempColor = getColor(tempc);
                colorButton.setBackground(tempColor);
                colorButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ae) {
                        Color c = JColorChooser.showDialog(retVal,"Choose Color" ,tempColor);
                        if (c != null) {
	                   String temp = colorList.getText();
			   String[] newColorOpts;
			   if (!temp.trim().equals("")) {
			      newColorOpts = temp.trim().split("\n");
			   } else {
			      newColorOpts = new String[1];
			      newColorOpts[0] = "";
			   }
			   String newColor = "";
			   for (int j = 0; j < myI; j++) {
			      if (j < newColorOpts.length) newColor+=newColorOpts[j]+"\n"; else newColor+="\n";
			   }
			   newColor+=""+c.getRed()+","+c.getGreen() +","+c.getBlue()+"\n";
			   for (int j = myI+1; j < newColorOpts.length; j++) {
                              newColor+=newColorOpts[j]+"\n";
			   }
			   colorList.setText(newColor);
                           colorButton.setBackground(c);
                           plotButton.doClick();
                        }
                    }
                });
		JPanel plotpan = new JPanel();
                plotpan.setLayout(new GridLayout());
                plotpan.add(showLabel);
		plotpan.add(colorButton);
		retVal.add(plotpan);
	      }
	   }

	   final JLabel bgshowLabel = new JLabel("BG Color");
           final JButton bgcolorButton = new JButton();
	   final Color bgtempColor;
	   if (bgColorField.getText().trim().equals("")) {
	      bgtempColor = Color.WHITE;
	   } else {
	      bgtempColor = getColor(bgColorField.getText());
	   }
           bgcolorButton.setBackground(bgtempColor);
           bgcolorButton.addActionListener(new ActionListener(){
	      public void actionPerformed(ActionEvent ae) {
		Color c = JColorChooser.showDialog(retVal,"Choose Color",bgtempColor);
		if (c != null) {
		   bgColorField.setText(""+c.getRed()+","+c.getGreen()+","+c.getBlue());
		   bgcolorButton.setBackground(c);
		   plotButton.doClick();
		}
	      }
	   });
           JPanel bgpan = new JPanel();
	   bgpan.setLayout(new GridLayout());
           bgpan.add(bgshowLabel);
           bgpan.add(bgcolorButton);
           retVal.add(bgpan);
           final JLabel axshowLabel = new JLabel("Axes Color");
           final JButton axcolorButton = new JButton();
           final Color axtempColor = getColor(axesColor);
           axcolorButton.setBackground(axtempColor);
           axcolorButton.addActionListener(new ActionListener(){
	      public void actionPerformed(ActionEvent ae) {
		Color c = JColorChooser.showDialog(retVal,"Choose Color",axtempColor);
		if (c != null) {
		   axesColor = ""+c.getRed()+","+c.getGreen()+","+c.getBlue();
                   axcolorButton.setBackground(c);
                   plotButton.doClick();
		}
	      }
	   });
           JPanel axpan = new JPanel();
           axpan.setLayout(new GridLayout());
           axpan.add(axshowLabel);
           axpan.add(axcolorButton);
           retVal.add(axpan);
           retVal.setVisible(true);
        }
      });

      this.thePlot = new UFPlotPanel(xdim, ydim, this);
      bottomPanel = new JPanel();
      SpringLayout bottomLayout = new SpringLayout();
      bottomPanel.setLayout(bottomLayout);
      bottomPanel.setPreferredSize(new Dimension(xdim, 128));

      charSizeField = new JTextField(2);
      xMinField = new JTextField(5);
      xMaxField = new JTextField(5);
      yMinField = new JTextField(5);
      yMaxField = new JTextField(5);
      addText = new JButton("Add Text");
      xLin = new JRadioButton("Linear", true);
      xLog = new JRadioButton("Log");
      xLinLog = new ButtonGroup();
      xLinLog.add(xLin);
      xLinLog.add(xLog);
      yLin = new JRadioButton("Linear", true);
      yLog = new JRadioButton("Log");
      yLinLog = new ButtonGroup();
      yLinLog.add(yLin);
      yLinLog.add(yLog);
      bgColorField = new JTextField(8);
      //bgColorField.setText("255,255,255");
      multiButton = new JButton("Multiplot");

      multiButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   final JFrame multiFrame = new JFrame("Multiplot");
	   multiFrame.setSize(180, 70);
	   JPanel multiPanel = new JPanel();
	   multiPanel.setPreferredSize(new Dimension(180, 70));
	   multiPanel.add(new JLabel("Rows:"));
	   final JTextField rowsField = new JTextField(2);
	   multiPanel.add(rowsField);
	   multiPanel.add(Box.createRigidArea(new Dimension(20, 5)));
	   multiPanel.add(new JLabel("Cols:"));
	   final JTextField colsField = new JTextField(2);
	   multiPanel.add(colsField);
	   JButton apply = new JButton("Apply");
           apply.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ev) {
		int rows = 1, cols = 1;
		if (!rowsField.getText().equals(""))
		   rows = Integer.parseInt(rowsField.getText());
                if (!colsField.getText().equals(""))
                   cols = Integer.parseInt(colsField.getText());
		multi(0, cols, rows);
                multiFrame.dispose();
              }
           });
	   multiPanel.add(apply);
	   JButton cancel = new JButton("Cancel");
           cancel.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ev) {
		multiFrame.dispose();
	      }
	   });
	   multiPanel.add(cancel);
	   multiFrame.getContentPane().add(multiPanel);
	   multiFrame.pack();
	   multiFrame.setVisible(true);
	}
      });

      optsButton = new JButton("Options");
      optsButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   final JFrame optsFrame = new JFrame("Options");
	   optsFrame.setSize(480, 320);
	   JPanel optsPanel = new JPanel();
	   SpringLayout optsLayout = new SpringLayout();
           optsPanel.setLayout(optsLayout);
	   JLabel xTicksLabel = new JLabel("X ticks:");
 	   optsPanel.add(xTicksLabel);
	   optsLayout.putConstraint(SpringLayout.WEST, xTicksLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, xTicksLabel, 5, SpringLayout.NORTH, optsPanel);
	   final JTextField optsXTicks = new JTextField(2);
	   optsXTicks.setText(""+xticks);
	   optsPanel.add(optsXTicks);
           optsLayout.putConstraint(SpringLayout.WEST, optsXTicks, 5, SpringLayout.EAST, xTicksLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXTicks, 5, SpringLayout.NORTH, optsPanel);
           JLabel yTicksLabel = new JLabel("Y ticks:");
           optsPanel.add(yTicksLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yTicksLabel, 10, SpringLayout.EAST, optsXTicks);
           optsLayout.putConstraint(SpringLayout.NORTH, yTicksLabel, 5, SpringLayout.NORTH, optsPanel);
           final JTextField optsYTicks = new JTextField(2);
           optsYTicks.setText(""+yticks);
           optsPanel.add(optsYTicks);
           optsLayout.putConstraint(SpringLayout.WEST, optsYTicks, 5, SpringLayout.EAST, yTicksLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYTicks, 5, SpringLayout.NORTH, optsPanel);
	   JLabel xTickIntLabel = new JLabel("X tick interval:");
	   optsPanel.add(xTickIntLabel);
           optsLayout.putConstraint(SpringLayout.WEST, xTickIntLabel, 10, SpringLayout.EAST, optsYTicks);
           optsLayout.putConstraint(SpringLayout.NORTH, xTickIntLabel, 5, SpringLayout.NORTH, optsPanel);
           final JTextField optsXTickInt = new JTextField(4);
	   optsXTickInt.setText(""+xtickInt);
           optsPanel.add(optsXTickInt);
           optsLayout.putConstraint(SpringLayout.WEST, optsXTickInt, 5, SpringLayout.EAST, xTickIntLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXTickInt, 5, SpringLayout.NORTH, optsPanel);
           JLabel yTickIntLabel = new JLabel("Y tick interval:");
           optsPanel.add(yTickIntLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yTickIntLabel, 10, SpringLayout.EAST, optsXTickInt);
           optsLayout.putConstraint(SpringLayout.NORTH, yTickIntLabel, 5, SpringLayout.NORTH, optsPanel);
           final JTextField optsYTickInt = new JTextField(4);
           optsYTickInt.setText(""+ytickInt);
           optsPanel.add(optsYTickInt);
           optsLayout.putConstraint(SpringLayout.WEST, optsYTickInt, 5, SpringLayout.EAST, yTickIntLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYTickInt, 5, SpringLayout.NORTH, optsPanel);

	   JLabel xTickValuesLabel = new JLabel("X tick values:");
           optsPanel.add(xTickValuesLabel);
           optsLayout.putConstraint(SpringLayout.WEST, xTickValuesLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, xTickValuesLabel, 10, SpringLayout.SOUTH, optsXTicks);
           final JTextArea optsXTickVals = new JTextArea(4,9);
	   for (int j = 0; j < xtickVals.length; j++)
	      optsXTickVals.append(""+xtickVals[j]+"\n");
	   JScrollPane spOptsXTickVals = new JScrollPane(optsXTickVals);
	   optsPanel.add(spOptsXTickVals);
           optsLayout.putConstraint(SpringLayout.WEST, spOptsXTickVals, 5, SpringLayout.EAST, xTickValuesLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, spOptsXTickVals, 10, SpringLayout.SOUTH, optsXTicks);
           JLabel yTickValuesLabel = new JLabel("Y tick values:");
           optsPanel.add(yTickValuesLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yTickValuesLabel, 25, SpringLayout.EAST, spOptsXTickVals);
           optsLayout.putConstraint(SpringLayout.NORTH, yTickValuesLabel, 10, SpringLayout.SOUTH, optsYTicks);
           final JTextArea optsYTickVals = new JTextArea(4,9);
           for (int j = 0; j < ytickVals.length; j++)
              optsYTickVals.append(""+ytickVals[j]+"\n");
           JScrollPane spOptsYTickVals = new JScrollPane(optsYTickVals);
           optsPanel.add(spOptsYTickVals);
           optsLayout.putConstraint(SpringLayout.WEST, spOptsYTickVals, 5, SpringLayout.EAST, yTickValuesLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, spOptsYTickVals, 10, SpringLayout.SOUTH, optsYTicks);

           JLabel xTickNamesLabel = new JLabel("X tick names:");
           optsPanel.add(xTickNamesLabel);
           optsLayout.putConstraint(SpringLayout.WEST, xTickNamesLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, xTickNamesLabel, 10, SpringLayout.SOUTH, spOptsXTickVals);
           final JTextArea optsXTickNames = new JTextArea(4,9);
           for (int j = 0; j < xtickNames.length; j++)
              optsXTickNames.append(xtickNames[j]+"\n");
           JScrollPane spOptsXTickNames = new JScrollPane(optsXTickNames);
           optsPanel.add(spOptsXTickNames);
           optsLayout.putConstraint(SpringLayout.WEST, spOptsXTickNames, 5, SpringLayout.EAST, xTickNamesLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, spOptsXTickNames, 10, SpringLayout.SOUTH, spOptsXTickVals);
           JLabel yTickNamesLabel = new JLabel("Y tick names:");
           optsPanel.add(yTickNamesLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yTickNamesLabel, 25, SpringLayout.EAST, spOptsXTickNames);
           optsLayout.putConstraint(SpringLayout.NORTH, yTickNamesLabel, 10, SpringLayout.SOUTH, spOptsYTickVals);
           final JTextArea optsYTickNames = new JTextArea(4,9);
           for (int j = 0; j < ytickNames.length; j++)
              optsYTickNames.append(ytickNames[j]+"\n");
           JScrollPane spOptsYTickNames = new JScrollPane(optsYTickNames);
           optsPanel.add(spOptsYTickNames);
           optsLayout.putConstraint(SpringLayout.WEST, spOptsYTickNames, 5, SpringLayout.EAST, yTickNamesLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, spOptsYTickNames, 10, SpringLayout.SOUTH, spOptsYTickVals);

	   JLabel xTickLengthLabel = new JLabel("X tick length:");
	   optsPanel.add(xTickLengthLabel);
           optsLayout.putConstraint(SpringLayout.WEST, xTickLengthLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, xTickLengthLabel, 10, SpringLayout.SOUTH, spOptsXTickNames);
           final JTextField optsXTickLen = new JTextField(3);
	   optsXTickLen.setText(""+xtickLen);
           optsPanel.add(optsXTickLen);
           optsLayout.putConstraint(SpringLayout.WEST, optsXTickLen, 5, SpringLayout.EAST, xTickLengthLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXTickLen, 10, SpringLayout.SOUTH, spOptsXTickNames);
           JLabel yTickLengthLabel = new JLabel("Y tick length:");
           optsPanel.add(yTickLengthLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yTickLengthLabel, 10, SpringLayout.EAST, optsXTickLen);
           optsLayout.putConstraint(SpringLayout.NORTH, yTickLengthLabel, 10, SpringLayout.SOUTH, spOptsXTickNames);
           final JTextField optsYTickLen = new JTextField(3);
           optsYTickLen.setText(""+ytickLen);
           optsPanel.add(optsYTickLen);
           optsLayout.putConstraint(SpringLayout.WEST, optsYTickLen, 5, SpringLayout.EAST, yTickLengthLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYTickLen, 10, SpringLayout.SOUTH, spOptsXTickNames);
	   JLabel xMinorLabel = new JLabel("X minor:");
	   optsPanel.add(xMinorLabel);
           optsLayout.putConstraint(SpringLayout.WEST, xMinorLabel, 10, SpringLayout.EAST, optsYTickLen);
           optsLayout.putConstraint(SpringLayout.NORTH, xMinorLabel, 10, SpringLayout.SOUTH, spOptsXTickNames);
           final JTextField optsXMinor = new JTextField(2);
	   optsXMinor.setText(""+xminor);
           optsPanel.add(optsXMinor);
           optsLayout.putConstraint(SpringLayout.WEST, optsXMinor, 5, SpringLayout.EAST, xMinorLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXMinor, 10, SpringLayout.SOUTH, spOptsXTickNames);
           JLabel yMinorLabel = new JLabel("Y minor:");
           optsPanel.add(yMinorLabel);
           optsLayout.putConstraint(SpringLayout.WEST, yMinorLabel, 10, SpringLayout.EAST, optsXMinor);
           optsLayout.putConstraint(SpringLayout.NORTH, yMinorLabel, 10, SpringLayout.SOUTH, spOptsXTickNames);
           final JTextField optsYMinor = new JTextField(2);
           optsYMinor.setText(""+yminor);
           optsPanel.add(optsYMinor);
           optsLayout.putConstraint(SpringLayout.WEST, optsYMinor, 5, SpringLayout.EAST, yMinorLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYMinor, 10, SpringLayout.SOUTH, spOptsXTickNames);

	   JLabel symSizeLabel = new JLabel("Symbol Size:");
           optsPanel.add(symSizeLabel);
           optsLayout.putConstraint(SpringLayout.WEST, symSizeLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, symSizeLabel, 10, SpringLayout.SOUTH, optsXTickLen);
	   final JTextField optsSymSize = new JTextField(3);
	   optsSymSize.setText(""+symsize);
	   optsPanel.add(optsSymSize);
           optsLayout.putConstraint(SpringLayout.WEST, optsSymSize, 5, SpringLayout.EAST, symSizeLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsSymSize, 10, SpringLayout.SOUTH, optsXTickLen);
	   JLabel fontLabel = new JLabel("Font:");
	   optsPanel.add(fontLabel);
           optsLayout.putConstraint(SpringLayout.WEST, fontLabel, 10, SpringLayout.EAST, optsSymSize);
           optsLayout.putConstraint(SpringLayout.NORTH, fontLabel, 10, SpringLayout.SOUTH, optsXTickLen);
	   final JTextField optsFont = new JTextField(8);
	   optsFont.setText(fontName);
	   optsPanel.add(optsFont);
           optsLayout.putConstraint(SpringLayout.WEST, optsFont, 5, SpringLayout.EAST, fontLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsFont, 10, SpringLayout.SOUTH, optsXTickLen);
	   JLabel axesColorLabel = new JLabel("Axes Color:");
	   optsPanel.add(axesColorLabel);
           optsLayout.putConstraint(SpringLayout.WEST, axesColorLabel, 10, SpringLayout.EAST, optsFont);
           optsLayout.putConstraint(SpringLayout.NORTH, axesColorLabel, 10, SpringLayout.SOUTH, optsXTickLen);
           final JTextField optsAxesColor = new JTextField(9);
           optsAxesColor.setText(axesColor);
           optsPanel.add(optsAxesColor);
           optsLayout.putConstraint(SpringLayout.WEST, optsAxesColor, 5, SpringLayout.EAST, axesColorLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsAxesColor, 10, SpringLayout.SOUTH, optsXTickLen);

	   JLabel lMarginLabel = new JLabel("X margin: L");
           optsPanel.add(lMarginLabel);
           optsLayout.putConstraint(SpringLayout.WEST, lMarginLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, lMarginLabel, 10, SpringLayout.SOUTH, optsSymSize);
           final JTextField optsXMargin1 = new JTextField(3);
           final JTextField optsXMargin2 = new JTextField(3);
	   if (xmargin.length == 2) {
	      optsXMargin1.setText(""+xmargin[0]);
	      optsXMargin2.setText(""+xmargin[1]);
	   }
           optsPanel.add(optsXMargin1);
           optsLayout.putConstraint(SpringLayout.WEST, optsXMargin1, 5, SpringLayout.EAST, lMarginLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXMargin1, 10, SpringLayout.SOUTH, optsSymSize);
	   JLabel rMarginLabel = new JLabel("R");
	   optsPanel.add(rMarginLabel);
           optsLayout.putConstraint(SpringLayout.WEST, rMarginLabel, 5, SpringLayout.EAST, optsXMargin1);
           optsLayout.putConstraint(SpringLayout.NORTH, rMarginLabel, 10, SpringLayout.SOUTH, optsSymSize);
           optsPanel.add(optsXMargin2);
           optsLayout.putConstraint(SpringLayout.WEST, optsXMargin2, 5, SpringLayout.EAST, rMarginLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsXMargin2, 10, SpringLayout.SOUTH, optsSymSize);
           JLabel tMarginLabel = new JLabel("Y margin: T");
           optsPanel.add(tMarginLabel);
           optsLayout.putConstraint(SpringLayout.WEST, tMarginLabel, 30, SpringLayout.EAST, optsXMargin2);
           optsLayout.putConstraint(SpringLayout.NORTH, tMarginLabel, 10, SpringLayout.SOUTH, optsSymSize);
           final JTextField optsYMargin1 = new JTextField(3);
           final JTextField optsYMargin2 = new JTextField(3);
           if (ymargin.length == 2) {
              optsYMargin1.setText(""+ymargin[0]);
              optsYMargin2.setText(""+ymargin[1]);
           }
           optsPanel.add(optsYMargin1);
           optsLayout.putConstraint(SpringLayout.WEST, optsYMargin1, 5, SpringLayout.EAST, tMarginLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYMargin1, 10, SpringLayout.SOUTH, optsSymSize);
           JLabel bMarginLabel = new JLabel("B");
           optsPanel.add(bMarginLabel);
           optsLayout.putConstraint(SpringLayout.WEST, bMarginLabel, 5, SpringLayout.EAST, optsYMargin1);
           optsLayout.putConstraint(SpringLayout.NORTH, bMarginLabel, 10, SpringLayout.SOUTH, optsSymSize);
           optsPanel.add(optsYMargin2);
           optsLayout.putConstraint(SpringLayout.WEST, optsYMargin2, 5, SpringLayout.EAST, bMarginLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsYMargin2, 10, SpringLayout.SOUTH, optsSymSize);

	   JLabel lPositionLabel = new JLabel("Position: L");
           optsPanel.add(lPositionLabel);
           optsLayout.putConstraint(SpringLayout.WEST, lPositionLabel, 5, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, lPositionLabel, 10, SpringLayout.SOUTH, optsXMargin1);
           final JTextField optsPos1 = new JTextField(3);
           final JTextField optsPos2 = new JTextField(3);
           final JTextField optsPos3 = new JTextField(3);
           final JTextField optsPos4 = new JTextField(3);
	   if (position.length == 4) {
	      optsPos1.setText(""+position[0]);
              optsPos2.setText(""+position[1]);
              optsPos3.setText(""+position[2]);
              optsPos4.setText(""+position[3]);
	   }
	   optsPanel.add(optsPos1);
           optsLayout.putConstraint(SpringLayout.WEST, optsPos1, 5, SpringLayout.EAST, lPositionLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsPos1, 10, SpringLayout.SOUTH, optsXMargin1);
	   JLabel tPositionLabel = new JLabel("T");
	   optsPanel.add(tPositionLabel);
           optsLayout.putConstraint(SpringLayout.WEST, tPositionLabel, 5, SpringLayout.EAST, optsPos1);
           optsLayout.putConstraint(SpringLayout.NORTH, tPositionLabel, 10, SpringLayout.SOUTH, optsXMargin1);
           optsPanel.add(optsPos2);
           optsLayout.putConstraint(SpringLayout.WEST, optsPos2, 5, SpringLayout.EAST, tPositionLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsPos2, 10, SpringLayout.SOUTH, optsXMargin1);
           JLabel rPositionLabel = new JLabel("R");
           optsPanel.add(rPositionLabel);
           optsLayout.putConstraint(SpringLayout.WEST, rPositionLabel, 5, SpringLayout.EAST, optsPos2);
           optsLayout.putConstraint(SpringLayout.NORTH, rPositionLabel, 10, SpringLayout.SOUTH, optsXMargin1);
           optsPanel.add(optsPos3);
           optsLayout.putConstraint(SpringLayout.WEST, optsPos3, 5, SpringLayout.EAST, rPositionLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsPos3, 10, SpringLayout.SOUTH, optsXMargin1);
           JLabel bPositionLabel = new JLabel("B");
           optsPanel.add(bPositionLabel);
           optsLayout.putConstraint(SpringLayout.WEST, bPositionLabel, 5, SpringLayout.EAST, optsPos3);
           optsLayout.putConstraint(SpringLayout.NORTH, bPositionLabel, 10, SpringLayout.SOUTH, optsXMargin1);
           optsPanel.add(optsPos4);
           optsLayout.putConstraint(SpringLayout.WEST, optsPos4, 5, SpringLayout.EAST, bPositionLabel);
           optsLayout.putConstraint(SpringLayout.NORTH, optsPos4, 10, SpringLayout.SOUTH, optsXMargin1);

	   JButton setPrefs = new JButton("Set Options");
           setPrefs.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ev) {
		String temp;
		String[] temps;
		try {
		   temp = optsXTicks.getText();
		   if (!temp.equals("")) xticks = Integer.parseInt(temp);
                   temp = optsYTicks.getText();
                   if (!temp.equals("")) yticks = Integer.parseInt(temp);
                   temp = optsXTickInt.getText();
                   if (!temp.equals("")) xtickInt = Float.parseFloat(temp);
                   temp = optsYTickInt.getText();
                   if (!temp.equals("")) ytickInt = Float.parseFloat(temp);
                   temp = optsXTickLen.getText();
                   if (!temp.equals("")) xtickLen = Float.parseFloat(temp);
                   temp = optsYTickLen.getText();
                   if (!temp.equals("")) ytickLen = Float.parseFloat(temp);
                   temp = optsXMinor.getText();
                   if (!temp.equals("")) xminor = Integer.parseInt(temp);
                   temp = optsYMinor.getText();
                   if (!temp.equals("")) yminor = Integer.parseInt(temp);
		   temp = optsSymSize.getText();
		   if (!temp.equals("")) symsize = Float.parseFloat(temp);
		   temp = optsFont.getText();
		   if (!temp.equals("")) fontName = temp;
		   temp = optsAxesColor.getText();
		   if (!temp.equals("")) axesColor = temp;
		} catch(Exception e) { System.err.println(e.toString()); }
		temps = optsXTickVals.getText().split("\n");
		if (temps.length != 0)
		   if (!(temps.length == 1 && temps[0].equals(""))) {
		      xtickVals = new float[temps.length];
		      for (int j = 0; j < temps.length; j++)
		        try { xtickVals[j] = Float.parseFloat(temps[j]); }
			catch(Exception e) { System.err.println(e.toString()); }
		   } 
		if (temps.length != 0)
                   temps = optsYTickVals.getText().split("\n");
                   if (!(temps.length == 1 && temps[0].equals(""))) {
                      ytickVals = new float[temps.length];
                      for (int j = 0; j < temps.length; j++)
                        try { ytickVals[j] = Float.parseFloat(temps[j]); }
                        catch(Exception e) { System.err.println(e.toString()); }
                   }
		if (temps.length != 0)
                   temps = optsXTickNames.getText().split("\n");
                   if (!(temps.length == 1 && temps[0].equals(""))) {
                      xtickNames = temps; 
                   } else {
		      xtickNames = new String[1];
		      xtickNames[0] = "";
		   }
		if (temps.length != 0)
                   temps = optsYTickNames.getText().split("\n");
                   if (!(temps.length == 1 && temps[0].equals(""))) {
                      ytickNames = temps;
		   } else {
		      ytickNames = new String[1];
		      ytickNames[0] = "";
		   }
		if (!optsXMargin1.getText().equals("") && !optsXMargin2.getText().equals("")) {
		   xmargin = new float[2];
		   try {
		      xmargin[0] = Float.parseFloat(optsXMargin1.getText());
		      xmargin[1] = Float.parseFloat(optsXMargin2.getText());
		   } catch(Exception e) { System.err.println(e.toString()); }
		} else xmargin = new float[1];
                if (!optsYMargin1.getText().equals("") && !optsYMargin2.getText().equals("")) {
                   ymargin = new float[2];
		   try {
		      ymargin[0] = Float.parseFloat(optsYMargin1.getText());
                      ymargin[1] = Float.parseFloat(optsYMargin2.getText());
		   } catch(Exception e) { System.err.println(e.toString()); }
                } else ymargin = new float[1];
		if (!optsPos1.getText().equals("") && !optsPos2.getText().equals("") && !optsPos3.getText().equals("") && !optsPos4.getText().equals("")) {
		   position = new float[4];
		   try { 
		      position[0] = Float.parseFloat(optsPos1.getText());
                      position[1] = Float.parseFloat(optsPos2.getText());
                      position[2] = Float.parseFloat(optsPos3.getText());
                      position[3] = Float.parseFloat(optsPos4.getText());
		   } catch(Exception e) { System.err.println(e.toString()); }
		} else position = new float[1];
                optsFrame.dispose();
              }
           });
	   JButton cancelPrefs = new JButton("Cancel");
           cancelPrefs.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ev) {
                optsFrame.dispose();
              }
           });
	   optsPanel.add(setPrefs);
           optsLayout.putConstraint(SpringLayout.WEST, setPrefs, 135, SpringLayout.WEST, optsPanel);
           optsLayout.putConstraint(SpringLayout.NORTH, setPrefs, 20, SpringLayout.SOUTH, optsPos1);
	   optsPanel.add(cancelPrefs);
           optsLayout.putConstraint(SpringLayout.WEST, cancelPrefs, 15, SpringLayout.EAST, setPrefs);
           optsLayout.putConstraint(SpringLayout.NORTH, cancelPrefs, 20, SpringLayout.SOUTH, optsPos1);

	   optsPanel.setPreferredSize(new Dimension(480, 340));
	   optsFrame.getContentPane().add(optsPanel);
	   optsFrame.pack();
	   optsFrame.setVisible(true);
	}
      });

      histButton = new JButton("Histogram");
      histButton.setToolTipText("Click to create a Histogram.");

      histButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   if (plotData.isSelected()) {
              String tempOpts = readOpts();
              String[] colorOpts;
              String temp;
            if (!nbins.getText().trim().equals(""))
                tempOpts+="*nbins="+nbins.getText()+", ";
            if (!binsize.getText().trim().equals(""))
                tempOpts+="*binsize="+binsize.getText()+", ";
            if (isFilled.isSelected()) tempOpts+="*fill, ";
              temp = colorList.getText();
              if (!temp.trim().equals("")) {
		colorOpts = temp.trim().split("\n");
                colorOpts[0] = removeWhitespace(colorOpts[0]);
                if (colorOpts[0].indexOf(",") == -1) colorOpts[0] = colorOpts[0].replaceAll(" ",",");
                tempOpts+="*color="+colorOpts[0]+", ";
                int colorLen = colorOpts[0].split(",").length;
                if (colorLen < 3) {
		   for (int l = 0; l < 3-colorLen; l++) tempOpts+=colorOpts[0].substring(colorOpts[0].lastIndexOf(",")+1)+", ";
		}
              } else {
                colorOpts = new String[1];
                colorOpts[0] = "";
              }
	      float[] h = hist(tempOpts+opts);
              if (showOPlot.isSelected()) autoOPlot = true; else autoOPlot = false;
	      if (autoOPlot) {
                for (int j = 0; j < oxs.size(); j++) {
                   tempOpts = "";
                   if (colorOpts.length > j+1) {
                      if (!colorOpts[j+1].equals("")) {
	                colorOpts[j+1] = removeWhitespace(colorOpts[j+1]);
                        if (colorOpts[j+1].indexOf(",") == -1) colorOpts[j+1] = colorOpts[j+1].replaceAll(" ",",");
                        tempOpts+="*color="+colorOpts[j+1]+", ";
                        int colorLen = colorOpts[j+1].split(",").length;
                        if (colorLen < 3) {
                           for (int l = 0; l < 3-colorLen; l++) tempOpts+=colorOpts[j+1].substring(colorOpts[j+1].lastIndexOf(",")+1)+", ";
                        }
                      }
                   }
                   overplot((float[])oxs.get(j), (float[])oys.get(j), tempOpts+(String)oOpts.get(j));
                }
	      }
	   } else if (plotFiles.isSelected()) {
	    int col = 0;
	    oldX = x;
	    oldY = y;
	    oldOpts = opts;
            String temp = histField.getText();
            if (!temp.trim().equals("")) col = Integer.parseInt(temp)-1;
            String file = fileList.getText().split("\n")[0];
	    readFile(file, -1, col);
	    String histOpts = "";
	    if (!nbins.getText().trim().equals(""))
		histOpts+="*nbins="+nbins.getText()+", ";
	    if (!binsize.getText().trim().equals(""))
                histOpts+="*binsize="+binsize.getText()+", ";
	    if (isFilled.isSelected()) histOpts+="*fill, ";
            temp = colorList.getText();
	    String[] colorOpts;
	    if (!temp.trim().equals("")) {
		colorOpts = temp.trim().split("\n");
           	colorOpts[0] = removeWhitespace(colorOpts[0]);
		if (colorOpts[0].indexOf(",") == -1) colorOpts[0] = colorOpts[0].replaceAll(" ",",");
		histOpts+="*color="+colorOpts[0]+", ";
		int colorLen = colorOpts[0].split(",").length;
		if (colorLen < 3) {
		   for (int l = 0; l < 3-colorLen; l++) histOpts+=colorOpts[0].substring(colorOpts[0].lastIndexOf(",")+1)+", ";
		}
	    }
	    float[] h = hist(y, histOpts + readOpts()); 
	    x = oldX;
	    y = oldY;
	    opts = oldOpts;
	   } else if (plotTable.isSelected()) {
	      final JFrame tableFrame = new JFrame("Data Table");
	      final JTextArea data = new JTextArea(25,40);
	      JScrollPane spData = new JScrollPane(data);
	      if (tabY != null) {
                for (int j = 0; j < tabY.length; j++) {
                   data.append(""+tabY[j]);
                   data.append("\n");
                }
              }
	      JButton bPlot = new JButton("Plot");
	      bPlot.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
		   oldY = y;
		   oldOpts = opts;
		   if (data.getText().trim() == "") {
		     tabY = null;
		     tableFrame.dispose();
		     return;
		   }
		   String[] s = data.getText().split("\n");
		   if (s[0].trim().equals("")) {
		      tabY = null;
		      tableFrame.dispose();
		      return;
		   }
		   String[] row;
		   int n = s.length;
		   x = new float[n];
		   y = new float[n];
		   for (int j = 0; j < n; j++) {
		      s[j] = removeWhitespace(s[j]);
		      row = s[j].split(" ");
		      y[j] = Float.parseFloat(row[0]);
		   }
                   tableFrame.dispose();
		   String temp;
		   String histOpts = "";
                   if (!nbins.getText().trim().equals(""))
		      histOpts+="*nbins="+nbins.getText()+", ";
                   if (!binsize.getText().trim().equals(""))
                      histOpts+="*binsize="+binsize.getText()+", ";
                   if (isFilled.isSelected()) histOpts+="*fill, ";
                   temp = colorList.getText();
                   if (!temp.trim().equals("")) {
		      colorOpts = temp.trim().split("\n");
                      colorOpts[0] = removeWhitespace(colorOpts[0]);
                      if (colorOpts[0].indexOf(",") == -1) colorOpts[0] = colorOpts[0].replaceAll(" ",",");
                      histOpts+="*color="+colorOpts[0]+", ";
                      int colorLen = colorOpts[0].split(",").length;
                      if (colorLen < 3) {
			for (int l = 0; l < 3-colorLen; l++) histOpts+=colorOpts[0].substring(colorOpts[0].lastIndexOf(",")+1)+", ";
		      }
                   }
		   float[] h = hist(y, histOpts + readOpts());
		   tabY = y;
		   y = oldY;
		   opts = oldOpts;
                }
	      });
	      JButton bInit = new JButton("Show Init Data");
	      bInit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                   data.setText("");
		   if (y != null) {
		      for (int j = 0; j < y.length; j++) {
			if (x != null) data.append(""+x[j]+"\t");
                   	data.append(""+y[j]);
                   	data.append("\n");
		      }
		   }
                }
              });
	      JButton bClear = new JButton("Clear");
	      bClear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		   data.setText("");
		}
	      });
	      JButton bCancel = new JButton("Cancel");
	      bCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
		   tableFrame.dispose();
                }
              });
	      SpringLayout tableLayout = new SpringLayout();
	      Container content = tableFrame.getContentPane();
	      content.setLayout(tableLayout);
	      content.add(spData);
              tableLayout.putConstraint(SpringLayout.WEST, spData, 5, SpringLayout.WEST, content);
              tableLayout.putConstraint(SpringLayout.NORTH, spData, 5, SpringLayout.NORTH, content);
	      content.add(bPlot);
              tableLayout.putConstraint(SpringLayout.WEST, bPlot, 15, SpringLayout.WEST, content);
              tableLayout.putConstraint(SpringLayout.NORTH, bPlot, 15, SpringLayout.SOUTH, spData);
              content.add(bInit);
              tableLayout.putConstraint(SpringLayout.WEST, bInit, 15, SpringLayout.EAST, bPlot);
              tableLayout.putConstraint(SpringLayout.NORTH, bInit, 15, SpringLayout.SOUTH, spData);
              content.add(bClear);
              tableLayout.putConstraint(SpringLayout.WEST, bClear, 15, SpringLayout.EAST, bInit);
              tableLayout.putConstraint(SpringLayout.NORTH, bClear, 15, SpringLayout.SOUTH, spData);
              content.add(bCancel);
              tableLayout.putConstraint(SpringLayout.WEST, bCancel, 15, SpringLayout.EAST, bClear);
              tableLayout.putConstraint(SpringLayout.NORTH, bCancel, 15, SpringLayout.SOUTH, spData);
              tableLayout.putConstraint(SpringLayout.EAST, content, 5, SpringLayout.EAST, spData);
              tableLayout.putConstraint(SpringLayout.SOUTH, content, 10, SpringLayout.SOUTH, bPlot);
	      tableFrame.pack();
	      tableFrame.setVisible(true);
	   }
	}
      });

      histField = new JTextField(2);
      URL url = GatorPlot.class.getResource("gator_small.gif");
      if (url == null) UFButton = new JButton("UF");
      else UFButton = new JButton("UF", new ImageIcon(url));

      JPanel histPanel = new JPanel();
      SpringLayout histLayout = new SpringLayout();
      histPanel.setLayout(histLayout);
      histPanel.setPreferredSize(new Dimension(150,120));
      histPanel.add(histButton);
      histLayout.putConstraint(SpringLayout.WEST, histButton, 20, SpringLayout.WEST, histPanel);
      histLayout.putConstraint(SpringLayout.NORTH, histButton, 5, SpringLayout.NORTH, histPanel);
      JLabel histColumnLabel = new JLabel("col:");
      histPanel.add(histColumnLabel);
      histLayout.putConstraint(SpringLayout.WEST, histColumnLabel, 5, SpringLayout.WEST, histPanel);
      histLayout.putConstraint(SpringLayout.NORTH, histColumnLabel, 12, SpringLayout.SOUTH, histButton);
      histPanel.add(histField);
      histLayout.putConstraint(SpringLayout.WEST, histField, 5, SpringLayout.EAST, histColumnLabel);
      histLayout.putConstraint(SpringLayout.NORTH, histField, 10, SpringLayout.SOUTH, histButton); 
      JLabel nbinsLabel = new JLabel("Num. of bins: ");
      histPanel.add(nbinsLabel);
      histLayout.putConstraint(SpringLayout.WEST, nbinsLabel, 5, SpringLayout.WEST, histPanel);
      histLayout.putConstraint(SpringLayout.NORTH, nbinsLabel, 8, SpringLayout.SOUTH, histField);
      nbins = new JTextField(3);
      histPanel.add(nbins);
      histLayout.putConstraint(SpringLayout.WEST, nbins, 5, SpringLayout.EAST, nbinsLabel);
      histLayout.putConstraint(SpringLayout.NORTH, nbins, 6, SpringLayout.SOUTH, histField);
      JLabel binsizeLabel = new JLabel("Bin size: ");
      histPanel.add(binsizeLabel);
      histLayout.putConstraint(SpringLayout.WEST, binsizeLabel, 5, SpringLayout.WEST, histPanel);
      histLayout.putConstraint(SpringLayout.NORTH, binsizeLabel, 8, SpringLayout.SOUTH, nbins);
      binsize = new JTextField(4);
      histPanel.add(binsize);
      histLayout.putConstraint(SpringLayout.WEST, binsize, 5, SpringLayout.EAST, binsizeLabel);
      histLayout.putConstraint(SpringLayout.NORTH, binsize, 6, SpringLayout.SOUTH, nbins);
      isFilled = new JCheckBox("Filled");
      histPanel.add(isFilled);
      histLayout.putConstraint(SpringLayout.WEST, isFilled, 15, SpringLayout.EAST, histField);
      histLayout.putConstraint(SpringLayout.NORTH, isFilled, 8, SpringLayout.SOUTH, histButton);
      histPanel.setBorder(new EtchedBorder(Color.GRAY, Color.BLACK));


      bottomPanel.add(histPanel);
      bottomLayout.putConstraint(SpringLayout.WEST, histPanel, 5, SpringLayout.WEST, bottomPanel);
      bottomLayout.putConstraint(SpringLayout.NORTH, histPanel, 0, SpringLayout.NORTH, bottomPanel);

      JLabel charSizeLabel = new JLabel("CharSize:");
      bottomPanel.add(charSizeLabel);
      bottomLayout.putConstraint(SpringLayout.WEST, charSizeLabel, 160, SpringLayout.WEST, bottomPanel);
      bottomLayout.putConstraint(SpringLayout.NORTH, charSizeLabel, 10, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(charSizeField);
      bottomLayout.putConstraint(SpringLayout.WEST, charSizeField, 5, SpringLayout.EAST, charSizeLabel);
      bottomLayout.putConstraint(SpringLayout.NORTH, charSizeField, 8, SpringLayout.NORTH, bottomPanel);
      JLabel xRangeLabel = new JLabel("x-range:");
      bottomPanel.add(xRangeLabel);
      bottomLayout.putConstraint(SpringLayout.WEST, xRangeLabel, 10, SpringLayout.EAST, charSizeField);
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

      JButton plotButton2 = new JButton("Plot");
      bottomPanel.add(plotButton2);
      bottomLayout.putConstraint(SpringLayout.WEST, plotButton2, 30, SpringLayout.EAST, yMaxField);
      bottomLayout.putConstraint(SpringLayout.NORTH, plotButton2, 5, SpringLayout.NORTH, bottomPanel);
      plotButton2.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   plotButton.doClick();
	}
      });

      bottomPanel.add(UFButton);
      bottomLayout.putConstraint(SpringLayout.EAST, UFButton, -10, SpringLayout.EAST, bottomPanel);
      bottomLayout.putConstraint(SpringLayout.NORTH, UFButton, 80, SpringLayout.NORTH, bottomPanel);

      bottomPanel.add(addText);
      bottomLayout.putConstraint(SpringLayout.WEST, addText, 160, SpringLayout.WEST, bottomPanel);
      bottomLayout.putConstraint(SpringLayout.NORTH, addText, 40, SpringLayout.NORTH, bottomPanel);

      addText.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   final XYFrame xyFrame = new XYFrame(thePlot); 
	   thePlot.addxyFrame(xyFrame);
	}
      });

      JLabel xLogLabel = new JLabel("X-axis:");
      bottomPanel.add(xLogLabel);
      bottomLayout.putConstraint(SpringLayout.WEST, xLogLabel, 30, SpringLayout.EAST, addText);
      bottomLayout.putConstraint(SpringLayout.NORTH, xLogLabel, 46, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(xLin);
      bottomLayout.putConstraint(SpringLayout.WEST, xLin, 5, SpringLayout.EAST, xLogLabel);
      bottomLayout.putConstraint(SpringLayout.NORTH, xLin, 43, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(xLog);
      bottomLayout.putConstraint(SpringLayout.WEST, xLog, 5, SpringLayout.EAST, xLin);
      bottomLayout.putConstraint(SpringLayout.NORTH, xLog, 43, SpringLayout.NORTH, bottomPanel);
      JLabel yLogLabel = new JLabel("Y-axis:");
      bottomPanel.add(yLogLabel);
      bottomLayout.putConstraint(SpringLayout.WEST, yLogLabel, 25, SpringLayout.EAST, xLog);
      bottomLayout.putConstraint(SpringLayout.NORTH, yLogLabel, 46, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(yLin);
      bottomLayout.putConstraint(SpringLayout.WEST, yLin, 5, SpringLayout.EAST, yLogLabel);
      bottomLayout.putConstraint(SpringLayout.NORTH, yLin, 43, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(yLog);
      bottomLayout.putConstraint(SpringLayout.WEST, yLog, 5, SpringLayout.EAST, yLin);
      bottomLayout.putConstraint(SpringLayout.NORTH, yLog, 43, SpringLayout.NORTH, bottomPanel);

      JLabel bgLabel = new JLabel("BG Color (R,G,B):");
      bottomPanel.add(bgLabel);
      bottomLayout.putConstraint(SpringLayout.WEST, bgLabel, 160, SpringLayout.WEST, bottomPanel);
      bottomLayout.putConstraint(SpringLayout.NORTH, bgLabel, 86, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(bgColorField);
      bottomLayout.putConstraint(SpringLayout.WEST, bgColorField, 5, SpringLayout.EAST, bgLabel);
      bottomLayout.putConstraint(SpringLayout.NORTH, bgColorField, 84, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(multiButton);
      bottomLayout.putConstraint(SpringLayout.WEST, multiButton, 10, SpringLayout.EAST, bgColorField);
      bottomLayout.putConstraint(SpringLayout.NORTH, multiButton, 80, SpringLayout.NORTH, bottomPanel);
      bottomPanel.add(optsButton);
      bottomLayout.putConstraint(SpringLayout.WEST, optsButton, 10, SpringLayout.EAST, multiButton);
      bottomLayout.putConstraint(SpringLayout.NORTH, optsButton, 80, SpringLayout.NORTH, bottomPanel);
      showOPlot = new JCheckBox("Show Overplots", true);
      bottomPanel.add(showOPlot);
      bottomLayout.putConstraint(SpringLayout.WEST, showOPlot, 10, SpringLayout.EAST, optsButton);
      bottomLayout.putConstraint(SpringLayout.NORTH, showOPlot, 80, SpringLayout.NORTH, bottomPanel);
      showOPlot.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   if (showOPlot.isSelected()) autoOPlot = true; else autoOPlot = false;
	}
      });

      //bottomPanel2.add(UFButton);
      UFButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   float[] x = {0, 1};
	   float[] y = {0, 1};
	   thePlot.plot(x, y, "*nodata, *color=255, 255, 255");
	   xyouts(0.1f, 0.2f, "Go Gators!", "*normal, *color=255, 150, 10, *charsize=48");
           xyouts(0.4f, 0.5f, "Go Gators!", "*normal, *color=0, 0, 255, *charsize=48");
	}
      });

      content.add(thePlot, BorderLayout.CENTER);
      content.add(leftPanel, BorderLayout.WEST);
      content.add(bottomPanel, BorderLayout.SOUTH);
      pack();
      setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
   }

   private void setVisibility() { if( autoSetVisible ) this.setVisible(true); }

   public void autoSetVisible(boolean autoSet) { autoSetVisible = autoSet; }

   public void initOpts(String s) {
      this.opts = s;
   }

   public void showPlot(boolean sp) {
      this.showPanels = sp;
      if (!sp) {
	this.remove(leftPanel);
	this.remove(bottomPanel);
	this.setSize(xdim + 4, ydim + 26);
	thePlot.gpPanelItem.setText("Show Option Panels");
	this.validate();
	this.repaint();
      }
      this.setVisible(true);
   }

   public void showPlotPanels(boolean sp) {
      this.showPanels = sp;
      if (!sp) {
	this.remove(leftPanel);
	this.remove(bottomPanel);
	this.setSize(xdim + 4, ydim + 26);
	thePlot.gpPanelItem.setText("Show Option Panels");
	this.validate();
	this.repaint();
      }
   }

   public void plot(String s) {
      this.setVisibility();
      if (x == null) {
	x = new float[y.length];
	for (int j = 0; j < x.length; j++) x[j] = j;
      }
      thePlot.plot(x, y, s);
   }

   public void plot(float[] x, float[] y, String s) {
      this.setVisibility();
      this.x = x;
      this.y = y;
      this.opts = s;
      thePlot.plot(x, y, readOpts() + readOPlotOpts(0) + s);
      this.nplot = 0;
   }

   public void plot(float[] y, String s) {
      this.setVisibility();
      float[] x = new float[y.length];
      for (int j = 0; j < x.length; j++) x[j] = j;
      this.x = x;
      this.y = y;
      this.opts = s;
      thePlot.plot(x, y, readOpts() +readOPlotOpts(0) + s);
      this.nplot = 0;
   }

   public void plot(int[] x, String s) {
      float[] x2 = new float[x.length];
      for (int j = 0; j < x.length; j++) x2[j] = (float)x[j];
      this.plot(x2, s);
   }

   public void plot(int[] x, int[] y, String s) {
      float[] x2 = new float[x.length];
      float[] y2 = new float[y.length];
      for (int j = 0; j < x.length; j++) x2[j] = (float)x[j];
      for (int j = 0; j < y.length; j++) y2[j] = (float)y[j];
      this.plot(x2, y2, s);
   }

   public void plot(String file, int xcol, int ycol, String s) {
      this.setVisibility();
      readFile(file, xcol, ycol);
      this.opts = s;
      thePlot.plot(x, y, readOpts() + readOPlotOpts(0) + s);
      this.nplot = 0;
   }

   public void plot(String file, int ycol, String s) {
      this.plot(file, -1, ycol, s);
   }

   public void usersym(int[] usymxs, int[] usymys) {
      thePlot.usersym(usymxs, usymys);
   }

   public void overplot(float[] x, float[] y, String s, boolean keep) {
      if (keep) {
        oxs.add(x);
        oys.add(y);
        oOpts.add(s);
        autoOPlot = true;
      }
      overplot(x, y, s);
   }

   public void overplot(float[] x, float[] y, String s) {
      this.nplot++;
      thePlot.overplot(x, y, readOPlotOpts(this.nplot) + s);
   }

   public void xyouts(float xc, float yc, String text, String s) {
      thePlot.xyouts(xc, yc, text, s);
   }

   public void multi(int curr, int col, int row) {
      thePlot.multi(curr, col, row);
   }

   public void removeOPlot(int n) { 
      oxs.remove(n);
      oys.remove(n);
      oOpts.remove(n);
   }

   public void removeOPlot(float[] x, float[] y, String s) {
      oxs.remove(x);
      oys.remove(y);
      oOpts.remove(s);
   }

   public void setOPlots(boolean autoOPlot) {
      this.autoOPlot = autoOPlot;
   }

   public void resetOPlots() {
      oxs = new Vector();
      oys = new Vector();
      oOpts = new Vector();
   }

   public float[] hist(String s) {
      this.setVisibility();
      this.nplot = 0;
      if (y == null) return null;
      return thePlot.hist(y, readOPlotOpts(0) + readOpts() + s);
   }

   public float[] hist(float[] x, String s) {
      this.setVisibility();
      this.y = x;
      this.opts = s;
      this.nplot = 0;
      return thePlot.hist(x, readOPlotOpts(0) + readOpts() + s);
   }

   public float[] hist(float[][] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length*x[0].length];
      for (int j = 0; j < x.length; j++) {
	for (int l = 0; l < x[0].length; l++) y[j*x[0].length+l] = x[j][l];
      }
      this.y = y;
      this.opts = s;
      this.nplot = 0;
      return thePlot.hist(y, readOPlotOpts(0) + readOpts() + s);
   }

   public float[] hist(double[][] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length*x[0].length];
      for (int j = 0; j < x.length; j++) {
	for (int l = 0; l < x[0].length; l++) y[j*x[0].length+l] = (float)x[j][l];
      }
      this.y = y;
      this.opts = s;
      this.nplot = 0;
      return thePlot.hist(y, readOPlotOpts(0) + readOpts() + s);
   }

   public float[] hist(int[] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length];
      for (int j = 0; j < x.length; j++) y[j] = (float)x[j];
      this.y = y;
      this.opts = s;
      this.nplot = 0;
      return thePlot.hist(y, readOPlotOpts(0) + readOpts() + s);
   }

   public float[] hist(int[][] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length*x[0].length];
      for (int j = 0; j < x.length; j++) {
        for (int l = 0; l < x[0].length; l++)
	   y[j*x[0].length+l] = (float)x[j][l];
      }
      this.y = y;
      this.opts = s;
      this.nplot = 0;
      return thePlot.hist(y, readOPlotOpts(0) + readOpts() + s);
   }

   public float getYmax() {
      return thePlot.getYmax();
   }

   public void readFile(String file, int xcol, int ycol) {
      float[] x, y;
      Vector v = new Vector();
      String currLine = " ";
      String[] temp;
      int n;
      try {
        BufferedReader r = new BufferedReader(new FileReader(file));
        while (currLine != null) {
           currLine = r.readLine();
           if (currLine != null) v.add(currLine);
        }
      } catch(IOException e) {
        System.out.println("Error Reading From File.");
      }
      n = v.size();
      if (n == 0) {
        System.out.println("File is Empty");
        return;
      }
      x = new float[n];
      y = new float[n];
      for (int j = 0; j < n; j++) {
        currLine = (String)v.remove(0);
        currLine = currLine.replaceAll("\t", " ");
        while (currLine.indexOf("  ") != -1)
           currLine=currLine.replaceAll("  ", " ");
        temp = currLine.trim().split(" ");
        if (xcol != -1) x[j] = Float.parseFloat(temp[xcol]);
        else x[j] = j;
        y[j] = Float.parseFloat(temp[ycol]);
      }
      this.x = x;
      this.y = y;
   }

   public String readOpts() {
      String s = "";
      String temp;
      temp = titleField.getText();
      if (!temp.equals("")) {
	s+="*title="+temp+", ";
	this.setTitle("GatorPlot: " + temp);
      }
      temp = xtitleField.getText();
      if (!temp.equals("")) s+="*xtitle="+temp+", ";
      temp = ytitleField.getText();
      if (!temp.equals("")) s+="*ytitle="+temp+", ";
      temp = charSizeField.getText();
      if (!temp.trim().equals("")) s+="*charsize="+temp+", ";
      temp = xMinField.getText();
      if (!temp.trim().equals("")) s+="*xminval="+temp+", ";
      temp = xMaxField.getText();
      if (!temp.trim().equals("")) s+="*xmaxval="+temp+", ";
      temp = yMinField.getText();
      if (!temp.trim().equals("")) s+="*yminval="+temp+", ";
      temp = yMaxField.getText();
      if (!temp.trim().equals("")) s+="*ymaxval="+temp+", ";
      if (xLog.isSelected()) s+="*xlog, ";
      if (yLog.isSelected()) s+="*ylog, ";
      if (xLin.isSelected()) s+="*xlinear, ";
      if (yLin.isSelected()) s+="*ylinear, ";
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
      if (xticks != 0) s+="*xticks="+xticks+", ";
      if (yticks != 0) s+="*yticks="+yticks+", ";
      if (xminor != 0) s+="*xminor="+xminor+", ";
      if (yminor != 0) s+="*yminor="+yminor+", ";
      if (xtickInt != 0) s+="*xtickinterval="+xtickInt+", ";
      if (ytickInt != 0) s+="*ytickinterval="+ytickInt+", ";
      if (xtickLen != 0) s+="*xticklen="+xtickLen+", ";
      if (ytickLen != 0) s+="*yticklen="+ytickLen+", ";
      if (!fontName.equals("")) s+="*font="+fontName+", ";
      if (xtickVals.length != 0)
	if (!(xtickVals.length == 1 && xtickVals[0] == 0)) {
	   s+="*xtickv=[";
	   for (int j = 0; j < xtickVals.length-1; j++) s+=xtickVals[j]+",";
	   s+=xtickVals[xtickVals.length-1]+"], ";
        }
      if (ytickVals.length != 0)
        if (!(ytickVals.length == 1 && ytickVals[0] == 0)) {
           s+="*ytickv=[";
           for (int j = 0; j < ytickVals.length-1; j++) s+=ytickVals[j]+",";
           s+=ytickVals[ytickVals.length-1]+"], ";
        }
      if (xtickNames.length != 0)
        if (!(xtickNames.length == 1 && xtickNames[0].trim().equals(""))) {
           s+="*xtickname=[";
           for (int j = 0; j < xtickNames.length-1; j++) s+=xtickNames[j]+",";
           s+=xtickNames[xtickNames.length-1]+"], ";
        }
      if (ytickNames.length != 0)
        if (!(ytickNames.length == 1 && ytickNames[0].trim().equals(""))) {
           s+="*ytickname=[";
           for (int j = 0; j < ytickNames.length-1; j++) s+=ytickNames[j]+",";
           s+=ytickNames[ytickNames.length-1]+"], ";
        }
      if (xmargin.length == 2) s+="*xmargin=["+xmargin[0]+","+xmargin[1]+"], ";
      if (ymargin.length == 2) s+="*ymargin=["+ymargin[0]+","+ymargin[1]+"], ";
      if (position.length == 4) {
	s+="*position=["+position[0]+","+position[1]+","+position[2]+","+position[3]+"], ";
      }
      if (!axesColor.trim().equals("")) {
        if (axesColor.indexOf(",") == -1)
           axesColor = removeWhitespace(axesColor);
	   axesColor = axesColor.replaceAll(" ", ",");
        s+="*axescolor="+axesColor+", ";
      }

      return s;
   }

   public String readOPlotOpts(int j) {
      String oplotOpts = "";
      psymOpts = psymList.getText().trim().split("\n");
      colorOpts = colorList.getText().trim().split("\n");
      if (psymOpts.length > j) {
	if (!psymOpts[j].equals(""))
	   oplotOpts+="*psym="+psymOpts[j]+", ";
	if (symsize != 0)
	   oplotOpts+="*symsize="+symsize+", ";
      }
      if (colorOpts.length > j) {
	if (!colorOpts[j].equals("")) {
	   colorOpts[j] = removeWhitespace(colorOpts[j]);
	   if (colorOpts[j].indexOf(",") == -1) colorOpts[j] = colorOpts[j].replaceAll(" ",",");
           oplotOpts+="*color="+colorOpts[j]+", ";
           int colorLen = colorOpts[j].split(",").length;
           if (colorLen < 3) {
	      for (int l = 0; l < 3-colorLen; l++) oplotOpts+=colorOpts[j].substring(colorOpts[j].lastIndexOf(",")+1)+", ";
	   }
	}
      }
      return oplotOpts;
   }

   public static String removeWhitespace(String s) {
      while (s.indexOf("\t") != -1) s = s.replaceAll("\t"," ");
      while (s.indexOf("  ") != -1) {
	s = s.replaceAll("  "," ");
      }
      s = s.trim();
      return s;
   }

   public void addPanel(UFPlotPanel thePanel) {
      Container content = getContentPane();
      content.remove(thePlot);
      thePlot = thePanel;
      content.add(thePanel);
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

   public static void main(String[] args) {
      int[] x = {1, 2, 3, 4, 5};
      int[] y = {1, 5, 7, 14, 19};
      GatorPlot p = new GatorPlot();
      p.plot(x, y, "");
      p.showPlot(true);
   }

} 
