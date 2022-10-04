package wthjec;

/**
 * Title:        JPanelWeather.java
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
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFLib.*;
import javaMMTLib.*;

//===============================================================================
/**
 *This Class handles the executive tabbed pane 
 */
public class JPanelWeather extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelWeather.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    WthjecFrame wthjec;
    String _mainClass = getClass().getName();
    String weatherStation, elev_ft, longitude, lattitude;
    int wsid = -1, wc = WeatherFileConfig.CUSTOM;
    JCheckBox linesAutoBox, usbCheckBox; 
    JTextField lskipField, delOtherField, weatherFileField;
    JTextField colDate, colTime, colMinTemp, colMaxTemp, colSolarRad, colRain;
    JLabel statusLabel;
    JComboBox weatherTypeBox, minuteBox;
    JRadioButton delWhitespace, delComma, delTab, delOther;
    JRadioButton unitsEnglish, unitsMetric;
    ButtonGroup units, delimiters;
    boolean isEnabled = false, checkUsb=false;

    UFColorButton enableButton, disableButton;
    JButton browseButton;

    Date lastDate = null;
    WeatherThread _thread;
    WeatherRealTime wrt;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelWeather(WthjecFrame wthjecFrame, String weatherString) {
	String[] info = weatherString.split("::");
	weatherStation = info[0]; 
	try {
	  wsid = Integer.parseInt(info[1]);
	} catch (NumberFormatException nfe) {
	  wsid = -1;
	}
	elev_ft = info[2];
	longitude = info[3];
	lattitude = info[4];
	String header = weatherStation+": long="+longitude+"; latt="+lattitude+"; elev="+elev_ft+" ft";
	this.setLayout(new RatioLayout());
        wthjec = wthjecFrame;
        setPreferredSize(new Dimension(648,472));
        double height;

        /* Weather Panel */
	JPanel weatherPanel = new JPanel();
        weatherPanel.setLayout(new RatioLayout());

	String[] wthTypes = {"Custom", "Davis"};
        weatherTypeBox = new JComboBox(wthTypes);

        weatherTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
	      String wtype = (String)weatherTypeBox.getSelectedItem();
	      if (wtype.equals("Davis")) {
		wc = WeatherFileConfig.DAVIS;
	      } else {
		wc = WeatherFileConfig.CUSTOM;
	      }
	      updateWeatherConfig(new WeatherFileConfig(wc));
            }
        });

	lskipField = new JTextField(3);
	linesAutoBox = new JCheckBox("Auto-detect");
	linesAutoBox.setBackground(Color.CYAN);

	delimiters = new ButtonGroup();
	delWhitespace = new JRadioButton("Whitespace");
	delComma = new JRadioButton("Comma");
        delTab = new JRadioButton("Tab");
        delOther = new JRadioButton("Other");
	delOtherField = new JTextField(3);
	delimiters.add(delWhitespace);
	delimiters.add(delComma);
	delimiters.add(delTab);
	delimiters.add(delOther);
	delWhitespace.setBackground(Color.CYAN);
	delComma.setBackground(Color.CYAN);
	delTab.setBackground(Color.CYAN);
	delOther.setBackground(Color.CYAN);

	units = new ButtonGroup();
	unitsEnglish = new JRadioButton("English");
	unitsMetric = new JRadioButton("Metric");
	units.add(unitsEnglish);
	units.add(unitsMetric);
	unitsEnglish.setBackground(Color.CYAN);
	unitsMetric.setBackground(Color.CYAN);

	colDate = new JTextField(10);
	colTime = new JTextField(10);
        colMinTemp = new JTextField(10);
        colMaxTemp = new JTextField(10);
	colSolarRad = new JTextField(10);
        colRain = new JTextField(10);

	weatherFileField = new JTextField(25);
	enableButton = new UFColorButton("Enable", UFColorButton.COLOR_SCHEME_GREEN);
	disableButton = new UFColorButton("Disable", UFColorButton.COLOR_SCHEME_RED);
        browseButton = new JButton("Browse");

        browseButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ev) {
              JFileChooser jfc = new JFileChooser();
              int returnVal = jfc.showOpenDialog((Component)ev.getSource());
              if (returnVal == JFileChooser.APPROVE_OPTION) {
		weatherFileField.setText(jfc.getSelectedFile().getAbsolutePath());
              }
           }
        });

        enableButton.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent ae) {
	    setWeatherEnabled(true);
	  }
	});

	disableButton.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
	    setWeatherEnabled(false);
          }
        });

        String[] minutes = new String[60];
        for (int j = 0; j < minutes.length; j++) {
          if (j < 10) minutes[j] = "0"+j; else minutes[j] = ""+j;
        }
        minuteBox = new JComboBox(minutes);

        usbCheckBox = new JCheckBox("Auto-detect USB disconnections", true);
        usbCheckBox.setBackground(Color.CYAN);

	height = Math.floor(960./17.5)/1000.;
        weatherPanel.add("0.01,0.01;0.98,"+height, new JLabel(header, JLabel.CENTER));
        weatherPanel.add("0.01,"+(0.01+height)+";0.98,0.04", new JSeparator());
        weatherPanel.add("0.01,"+(1*height+0.04)+";0.49,"+height, new JLabel("Weather Station Type"));
        weatherPanel.add("0.50,"+(1*height+0.04)+";0.25,"+height, weatherTypeBox);
        weatherPanel.add("0.01,"+(3*height+0.04)+";0.34,"+height, new JLabel("# of header lines to skip"));
        weatherPanel.add("0.35,"+(3*height+0.04)+";0.10,"+height, lskipField);
        weatherPanel.add("0.45,"+(3*height+0.04)+";0.25,"+height, linesAutoBox);
        weatherPanel.add("0.01,"+(4*height+0.04)+";0.19,"+height, new JLabel("Delimiter"));
        weatherPanel.add("0.20,"+(4*height+0.04)+";0.20,"+height, delWhitespace);
        weatherPanel.add("0.40,"+(4*height+0.04)+";0.15,"+height, delComma);
        weatherPanel.add("0.55,"+(4*height+0.04)+";0.15,"+height, delTab);
        weatherPanel.add("0.70,"+(4*height+0.04)+";0.15,"+height, delOther);
        weatherPanel.add("0.85,"+(4*height+0.04)+";0.15,"+height, delOtherField);

        weatherPanel.add("0.01,"+(5*height+0.04)+";0.19,"+height, new JLabel("Units"));
        weatherPanel.add("0.20,"+(5*height+0.04)+";0.20,"+height, unitsEnglish);
        weatherPanel.add("0.40,"+(5*height+0.04)+";0.20,"+height, unitsMetric);

        weatherPanel.add("0.01,"+(7*height+0.04)+";0.74,"+height, new JLabel("Column numbers or headers", JLabel.CENTER));
        weatherPanel.add("0.01,"+(8*height+0.04)+";0.14,"+height, new JLabel("Date"));
        weatherPanel.add("0.15,"+(8*height+0.04)+";0.20,"+height, colDate);
        weatherPanel.add("0.41,"+(8*height+0.04)+";0.14,"+height, new JLabel("Time"));
        weatherPanel.add("0.55,"+(8*height+0.04)+";0.20,"+height, colTime);
        weatherPanel.add("0.01,"+(9*height+0.04)+";0.14,"+height, new JLabel("Min Temp"));
        weatherPanel.add("0.15,"+(9*height+0.04)+";0.20,"+height, colMinTemp);
        weatherPanel.add("0.41,"+(9*height+0.04)+";0.14,"+height, new JLabel("Max Temp"));
        weatherPanel.add("0.55,"+(9*height+0.04)+";0.20,"+height, colMaxTemp);
        weatherPanel.add("0.01,"+(10*height+0.04)+";0.14,"+height, new JLabel("Solar Rad"));
        weatherPanel.add("0.15,"+(10*height+0.04)+";0.20,"+height, colSolarRad);
        weatherPanel.add("0.41,"+(10*height+0.04)+";0.14,"+height, new JLabel("Rain"));
        weatherPanel.add("0.55,"+(10*height+0.04)+";0.20,"+height, colRain);

        weatherPanel.add("0.01,"+(12*height+0.04)+";0.19,"+height, new JLabel("Weather File"));
	weatherPanel.add("0.20,"+(12*height+0.04)+";0.40,"+height, weatherFileField);
        weatherPanel.add("0.60,"+(12*height+0.04)+";0.15,"+height, browseButton);
        weatherPanel.add("0.01,"+(13*height+0.04)+";0.49,"+height, new JLabel("Minutes after hour to read and upload"));
        weatherPanel.add("0.50,"+(13*height+0.04)+";0.10,"+height, minuteBox); 
        weatherPanel.add("0.01,"+(14*height+0.04)+";0.49,"+height, usbCheckBox);

	weatherPanel.add("0.25,"+(15.2*height+0.04)+";0.20,"+(1.8*height), enableButton);
        weatherPanel.add("0.55,"+(15.2*height+0.04)+";0.20,"+(1.8*height), disableButton);


        weatherPanel.setBorder(new EtchedBorder(0));
        weatherPanel.setBackground(Color.cyan);

	/* Progress bar */
/*
	progressBar = new UFMMTProgressBar("ufmmtExecAgent.ExecutiveAgent:currentFrame", 2, "Frame", "");
	progressBar.registerComponent(wthjec.database, false);
	progressBar.registerContext(wthjec.database, "ufmmtExecAgent.ExecutiveAgent:totalFrames");
*/

        add("0.01,0.01;0.98,0.98",weatherPanel);
	//add("0.46,0.93;0.52,0.05",progressBar);

	_thread = new WeatherThread();
	_thread.start();
    } 

    public void updateWeatherConfig(WeatherFileConfig config) {
      lskipField.setText(""+config.lskip);
      linesAutoBox.setSelected(config.lines_auto);
      colDate.setText(""+config.col_date);
      colTime.setText(""+config.col_time);
      colMinTemp.setText(""+config.col_minTemp);
      colMaxTemp.setText(""+config.col_maxTemp);
      colSolarRad.setText(""+config.col_solarRad);
      colRain.setText(""+config.col_rain);
      if (config.metric) unitsMetric.setSelected(true); else unitsEnglish.setSelected(true);
      if (config.delimiter.equals("\\s+")) {
	delWhitespace.setSelected(true);
      } else if (config.delimiter.equals(",")) {
	delComma.setSelected(true);
      } else if (config.delimiter.equals("\t")) {
	delTab.setSelected(true);
      } else {
	delOther.setSelected(true);
	delOtherField.setText(config.delimiter);
      }
      usbCheckBox.setSelected(config.usb);
    }

    public void setWeatherEnabled(boolean enabled) {
      isEnabled = enabled;
      updateState();
      if (isEnabled) {
	WeatherFileConfig config = new WeatherFileConfig(wc);
	String delim = "";
	if (delWhitespace.isSelected()) {
	  delim = "\\s+";
	} else if (delComma.isSelected()) {
	  delim = ",";
	} else if (delTab.isSelected()) {
	  delim = "\t";
	} else delim = delOtherField.getText();
	config.setDelim(delim);
	config.setMetric(unitsMetric.isSelected());
	config.setAutoSkip(linesAutoBox.isSelected());
	config.setUsb(usbCheckBox.isSelected());
	try {
	  int lines = Integer.parseInt(lskipField.getText());
          config.setLinesToSkip(lines);
	} catch (NumberFormatException nfe) {}
	try {
	  int date = Integer.parseInt(colDate.getText());
	  int time = Integer.parseInt(colTime.getText());
          int minTemp = Integer.parseInt(colMinTemp.getText());
          int maxTemp = Integer.parseInt(colMaxTemp.getText());
          int solarRad = Integer.parseInt(colSolarRad.getText());
          int rain = Integer.parseInt(colRain.getText());
	  config.setCols(date, time, minTemp, maxTemp, solarRad, rain);
	} catch (NumberFormatException nfe) {}
	
	String wthFile = weatherFileField.getText();
	int minute = minuteBox.getSelectedIndex(); 
	wrt = new WeatherRealTime(wsid, wthFile, minute, config, lastDate);
	wrt.start();
      } else {
	if (wrt != null) {
	  lastDate = wrt.getLastDate();
	  wrt.cancel();
	}
      }
    }

    protected void updateState() {
	enableButton.setEnabled(!isEnabled);
	disableButton.setEnabled(isEnabled);
	linesAutoBox.setEnabled(!isEnabled);
	lskipField.setEnabled(!isEnabled);
	delOtherField.setEnabled(!isEnabled);
	weatherFileField.setEnabled(!isEnabled);
	colDate.setEnabled(!isEnabled);
	colTime.setEnabled(!isEnabled);
	colMinTemp.setEnabled(!isEnabled);
	colMaxTemp.setEnabled(!isEnabled);
	colSolarRad.setEnabled(!isEnabled);
	colRain.setEnabled(!isEnabled);
	weatherTypeBox.setEnabled(!isEnabled);
	minuteBox.setEnabled(!isEnabled);
	delWhitespace.setEnabled(!isEnabled);
	delComma.setEnabled(!isEnabled);
	delTab.setEnabled(!isEnabled);
	delOther.setEnabled(!isEnabled);
	unitsEnglish.setEnabled(!isEnabled);
	unitsMetric.setEnabled(!isEnabled);
	usbCheckBox.setEnabled(!isEnabled);
    }

    public void updateWeather() {
      if (wrt == null) return;
      if (!wrt.readyToUpdate()) return;
      Vector <String> commandVec = wrt.getWeather();
      if (commandVec.size() == 0) {
	System.out.println(_mainClass+"::updateWeather> "+ctime()+": WARNING: no new weather datapoints!");
	mjecError.show("WARNING: no new weather datapoints at "+ctime());
	if (usbCheckBox.isSelected()) {
          System.out.println(_mainClass+"::updateWeather> "+ctime()+": Examining WeatherLink logs...");
	  String wthFile = weatherFileField.getText();
	  String logFileName = "download.log";
	  if (wthFile.indexOf("WeatherLink") != -1) {
	    logFileName = wthFile.substring(0, wthFile.lastIndexOf("WeatherLink")+12)+"download.log";
	  } else if (wthFile.indexOf("\\") != -1) {
	    logFileName = wthFile.substring(0, wthFile.lastIndexOf("\\")+1)+"download.log";
	  } else if (wthFile.indexOf("/") != -1) {
            logFileName = wthFile.substring(0, wthFile.lastIndexOf("/")+1)+"download.log";
	  }
	  File logFile = new File(logFileName);
	  if (!logFile.exists()) {
	    System.out.println(_mainClass+"::updateWeather> "+ctime()+" Could not find " +logFileName);
	    return;
	  }
	  boolean err = false;
          String line = "";
	  try {
	    BufferedReader br = new BufferedReader(new FileReader(logFile));
	    int n = 0;
	    while (line != null && n < 3) {
	      line = br.readLine();
	      if (line.indexOf("canceled") != -1 || line.indexOf("error") != -1) {
		//error found
		err = true;
		break;
	      } 
	      n++;
	    }
	    if (!err) {
	      System.out.println(_mainClass+"::updateWeather> "+ctime()+" Could not find error in "+logFileName);
	      return;
	    }
          } catch (Exception ioe) {
            System.out.println(_mainClass+"::updateWeather3> "+ctime()+" ERROR: "+ioe.toString());
            ioe.printStackTrace();
          }
	  /* If we get here, usb is in error state */
	  System.out.println(_mainClass+"::updateWeather> "+ctime()+": Attempting to reset USB!");
          Vector <String> usbParams = new Vector();
          usbParams.add("cmd.exe");
          usbParams.add("\\c");
          usbParams.add("C:\\Program Files\\i386\\devcon.exe");
          usbParams.add("remove");
          usbParams.add("@usb\\*");
          System.out.println(_mainClass+"::updateWeather> Running devcon to shut down USB");
          ProcessBuilder pb = new ProcessBuilder(usbParams);
          Process p = null;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = "";
/*
            while (line != null) {
              line = procReader.readLine();
              if (line != null) System.out.println(_mainClass+"::updateWeather> "+line);
            }
*/
	    System.out.println("WAITING 10s");
	    Thread.sleep(10000);
/*
            p.waitFor();
	    System.out.println("DONE WAITING");
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::updateWeather> shut down USB successfully."); 
            } else {
              System.out.println(_mainClass+"::updateWeather> WARNING: USB shutdown terminated abnormally with "+p.exitValue());
            }
*/
            System.out.println(_mainClass+"::updateWeather> shut down USB (successfully?)");
          } catch (Exception ioe) {
            System.out.println(_mainClass+"::updateWeather1> ERROR: "+ioe.toString());
	    p.destroy();
	    ioe.printStackTrace();
          }
	  /* Now try to rescan USB */
          System.out.println(_mainClass+"::updateWeather> "+ctime()+": Attempting to rescan USB!");
          usbParams = new Vector();
	  usbParams.add("cmd.exe");
	  usbParams.add("\\c");
          usbParams.add("C:\\Program Files\\i386\\devcon.exe");
          usbParams.add("rescan");
          System.out.println(_mainClass+"::updateWeather> Running devcon to rescan USB");
          pb = new ProcessBuilder(usbParams);
          p = null;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = "";
/*
            while (line != null) {
              line = procReader.readLine();
              if (line != null) System.out.println(_mainClass+"::updateWeather> "+line);
            }
*/
            System.out.println(_mainClass+"::updateWeather> rescanned (successfully?)");
/*
            p.waitFor();
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::updateWeather> rescanned USB successfully.");
            } else {
              System.out.println(_mainClass+"::updateWeather> WARNING: USB rescan terminated abnormally with "+p.exitValue());
            }
*/
          } catch (Exception ioe) {
            System.out.println(_mainClass+"::updateWeather2> ERROR: "+ioe.toString());
            p.destroy();
	    ioe.printStackTrace();
            return;
          }
          System.out.println(logFileName);
	}
	return;
      }
      System.out.println(_mainClass+"::updateWeather> "+ctime()+": sending "+commandVec.size()+" new hourly weather datapoints."); 
      String currLine;
      for (int j = 0; j < commandVec.size(); j++) {
	currLine = "ADD_WEATHER::"+wthjec.uid+" "+wthjec.sid+" "+wsid+" "+commandVec.get(j);
	commandVec.setElementAt(currLine, j);
	System.out.println("\t\t"+currLine);
      }
      wthjec.apply(commandVec, this);
    }

    public void updateLastDate() {
      System.out.println(_mainClass+"::updateLastDate> "+ctime()+": successfully updated weather.");
      if (wrt != null) {
	wrt.updateLastDate();
        lastDate = wrt.getLastDate();
      }
    }

    public void saveFields(PrintWriter pw) {
        try {
          pw.println("  <panel name=\"" + weatherStation + "\">");
	  String delim = "";
	  if (delWhitespace.isSelected()) {
	    delim = "\\s+";
	  } else if (delComma.isSelected()) {
	    delim = ",";
	  } else if (delTab.isSelected()) {
	    delim = "\t";
	  } else delim = delOtherField.getText();

	  JTextField[] textFields = { lskipField, colDate, colTime, colMinTemp, colMaxTemp, colSolarRad, colRain, weatherFileField };
	  String[] fieldNames = { "linesToSkip", "dateCol", "timeCol", "minTempCol", "maxTempCol", "solarRadCol", "rainCol", "weatherFile" };

          String wtype = (String)weatherTypeBox.getSelectedItem();

          pw.println("    <record>");
          pw.println("      <name>type</name>");
          pw.println("      <value>"+wtype+"</value>");
          pw.println("    </record>");

	  pw.println("    <record>");
          pw.println("      <name>delimiter</name>");
          pw.println("      <value>"+delim+"</value>");
          pw.println("    </record>");

          pw.println("    <record>");
          pw.println("      <name>metric</name>");
          pw.println("      <value>"+unitsMetric.isSelected()+"</value>");
          pw.println("    </record>");

          pw.println("    <record>");
          pw.println("      <name>autoSkip</name>");
          pw.println("      <value>"+linesAutoBox.isSelected()+"</value>");
          pw.println("    </record>");

	  for (int j = 0; j < fieldNames.length; j++) {
            pw.println("    <record>");
            pw.println("      <name>"+fieldNames[j]+"</name>");
            pw.println("      <value>"+textFields[j].getText()+"</value>");
            pw.println("    </record>");
	  }

	  pw.println("    <record>");
          pw.println("      <name>minute</name>");
          pw.println("      <value>"+minuteBox.getSelectedIndex()+"</value>");
          pw.println("    </record>");

          pw.println("    <record>");
          pw.println("      <name>autoUsb</name>");
          pw.println("      <value>"+usbCheckBox.isSelected()+"</value>");
          pw.println("    </record>");

          pw.println("    <record>");
          pw.println("      <name>enabled</name>");
          pw.println("      <value>"+isEnabled+"</value>");
          pw.println("    </record>");

          pw.println("  </panel>");
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
    }

    public void loadFields(NodeList reclist) {
	Element elem;
	Node recNode = null;
        JTextField[] textFields = { lskipField, colDate, colTime, colMinTemp, colMaxTemp, colSolarRad, colRain, weatherFileField };
        String[] fieldNames = { "linesToSkip", "dateCol", "timeCol", "minTempCol", "maxTempCol", "solarRadCol", "rainCol", "weatherFile" };
	boolean autoEnable = false;

        for (int l = 0; l < reclist.getLength(); l++) {
          try {
            recNode = reclist.item(l);
            if (recNode.getNodeType() == Node.ELEMENT_NODE) {
              Element recElmnt = (Element)recNode;
              NodeList recnameList = recElmnt.getElementsByTagName("name");
              elem = (Element)recnameList.item(0);
              String recName = elem.getFirstChild().getNodeValue().trim();
              NodeList recvalList = recElmnt.getElementsByTagName("value");
              elem = (Element)recvalList.item(0);
	      if (elem.getFirstChild() == null) continue;
              String recVal = elem.getFirstChild().getNodeValue().trim();
              if (recName.equals("type")) {
		weatherTypeBox.setSelectedItem(recVal);
              } else if (recName.equals("metric")) {
		if (recVal.equals("true")) unitsMetric.setSelected(true); else unitsEnglish.setSelected(true);
	      } else if (recName.equals("autoSkip")) {
		if (recVal.equals("true")) linesAutoBox.setSelected(true); else linesAutoBox.setSelected(false);
	      } else if (recName.equals("delimiter")) {
		if (recVal.equals("\\s+")) {
		  delWhitespace.setSelected(true);
		} else if (recVal.equals(",")) {
		  delComma.setSelected(true);
		} else if (recVal.equals("\t")) {
		  delTab.setSelected(true);
		} else {
		  delOther.setSelected(true);
		  delOtherField.setText(recVal);
		}
	      } else if (recName.equals("minute")) {
		minuteBox.setSelectedIndex(Integer.parseInt(recVal));
              } else if (recName.equals("autoUsb")) {
                if (recVal.equals("false")) usbCheckBox.setSelected(false); else usbCheckBox.setSelected(true);
	      } else if (recName.equals("enabled")) {
		autoEnable = Boolean.valueOf(recVal.trim());
	      } else {
		for (int j = 0; j < fieldNames.length; j++) {
		  if (recName.equals(fieldNames[j])) textFields[j].setText(recVal);
		}
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.err.println(_mainClass+"::loadFields> "+e.toString()+" "+recNode.toString());
          }
        }
	setWeatherEnabled(autoEnable);
    }

    public String ctime() {
      String date = new Date( System.currentTimeMillis() ).toString();
      return( date.substring(4,19) + " LT");
    }


  protected class WeatherThread extends Thread {
    protected boolean _shutdown = false;
    private String _className = getClass().getName();
    protected int _sleepPeriod = 2000;

    public WeatherThread() {}

    public void shutdown() { _shutdown = true; }

    public void run() {
      while (!_shutdown) {
	updateState();
	updateWeather();
        try {
          Thread.sleep(_sleepPeriod);
        } catch (InterruptedException e) { }
      }
    } /* End run */
  } /* End WeatherThread */

} //end of class JPanelWeather
