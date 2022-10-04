package bmpjec;

/**
 * Title:        JPanelPLC.java
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
public class JPanelPLC extends JPanel {
    public static final String rcsID = "$Name:  $ $Id: JPanelPLC.java,v 1.6 2011/01/20 23:08:27 warner Exp $";
    
    MjecFrame mjec;
    JCheckBox pwrEnableBox, timerEnableBox;
    UFMMTLabel statusLabel;
    UFMMTLabel pwrLabel, timerLabel, timerOutput, irrigOutput;
    UFMMTLabel maxIrrigOutput, minIrrigOutput, lagTimeOutput, allowZeroOutput, irrigRateOutput;
    UFMMTLabel hourLabel, minuteLabel, sleepTimeLabel;

    UFMMTTextField timerInput, irrigInput, usernameInput, maxIrrigInput, minIrrigInput, lagTimeInput;
    UFMMTTextField manualDefaultInput, irrigatorLogInput, irrigRateInput;
    UFMMTPasswordField passwordInput;
    UFMMTComboBox hourInput, minuteInput, sleepTimeInput, defaultIrrigationInput, allowZeroInput;
    MJECButton pwrOnButton, pwrOffButton, startTimerButton, pauseTimerButton, resumeTimerButton;
    UFMMTLabel timerValLabel, timerStatusLabel;
    UFMMTLabel defaultIrrigationOutput, manualDefaultOutput, irrigatorLogOutput;
    UFMMTLabel lastMessageLabel;

    UFMMTComboBox runZoneInput;
    UFMMTLabel runZoneNameOutput;
    UFColorButton saveValuesButton;

    BmpJecButtonPanel _buttonPanel;
    String tabTitle, _ip, _type;

    //-------------------------------------------------------------------------------
    /**
     *Constructs the frame
     */
    public JPanelPLC(MjecFrame mjecFrame, String type, String plcIP) {
	this.setLayout(new RatioLayout());
        _ip = plcIP;
	_type = type;
        tabTitle = plcIP; 
        mjec = mjecFrame;
        setPreferredSize(new Dimension(1008,506));
        double height;

	/* Button panel */
	_buttonPanel = new BmpJecButtonPanel(mjec, _type, _ip);

	if (_type.equals("CSVOutput")) {
	  setupCSV();
	  return;
	}

	/* Top Panel */
	JPanel topPanel = new JPanel();
        topPanel.setLayout(new RatioLayout());

        int n = mjec.runsAndZones.size()+2;
        String[] runZoneKeys = new String[n];
        String[] runZoneVals = new String[n];
        String key, val;
        runZoneKeys[0] = "None 0";
        runZoneVals[0] = "None";
	runZoneKeys[1] = "Fixed -2"; 
	runZoneVals[1] = "Fixed Manual Default";
        int idx = 2;
        for (Iterator i = mjec.runsAndZones.keySet().iterator(); i.hasNext(); ) {
          key = (String)i.next();
          val = (String)mjec.runsAndZones.get(key);
          runZoneKeys[idx] = key;
          runZoneVals[idx] = val;
          idx++;
        }
        runZoneInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".id", runZoneVals, runZoneKeys, UFMMTComboBox.REFERENCE);
        runZoneInput.registerComponent(mjec.database, true);
        runZoneInput.setCommand("SET_ZONE::"+_ip+" "+mjec.getUid()+" ");

        runZoneNameOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".name");
        runZoneNameOutput.registerComponent(mjec.database, false);

        irrigatorLogInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".irrigatorLog");
        irrigatorLogInput.setCommand("SET_IRRIGATOR_LOG::"+_ip+" "+mjec.getUid()+" ");
	irrigatorLogInput.allowOnlyAlphaNumeric();
        irrigatorLogInput.registerComponent(mjec.database, true);
        irrigatorLogOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".irrigatorLog");
        irrigatorLogOutput.registerComponent(mjec.database, false);

	topPanel.add("0.01,0.01;0.27,0.45", new JLabel("Assign a Run or Zone", JLabel.CENTER));
	topPanel.add("0.01,0.51;0.27,0.45", runZoneInput);

	topPanel.add("0.28,0.01;0.25,0.45", new JLabel("Current Run/Zone", JLabel.CENTER));
	topPanel.add("0.28,0.51;0.25,0.45", runZoneNameOutput);

	topPanel.add("0.55,0.01;0.22,0.45", new JLabel("Assign History Log", JLabel.CENTER));
        topPanel.add("0.55,0.51;0.22,0.45", irrigatorLogInput);

        topPanel.add("0.77,0.01;0.22,0.45", new JLabel("Current History Log", JLabel.CENTER));
        topPanel.add("0.77,0.51;0.22,0.45", irrigatorLogOutput);
        topPanel.setBorder(new EtchedBorder(0));
	topPanel.setBackground(Color.LIGHT_GRAY);

        /* PLC Panel */
	JPanel plcPanel = new JPanel();
        plcPanel.setLayout(new RatioLayout());
        pwrEnableBox = new JCheckBox("Manual Power (Y1)", false);
	pwrEnableBox.setBackground(Color.CYAN);
        timerEnableBox = new JCheckBox("Manual Timer (Y1)", false);
        timerEnableBox.setBackground(Color.CYAN);

        pwrLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".power");
        pwrLabel.addDisplayValue("On","On",new Color(0,180,0));
        pwrLabel.addDisplayValue("Off","Off",new Color(180,0,0));
        pwrLabel.registerComponent(mjec.database, false);

        timerLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".timerStat");
        timerLabel.addDisplayValue("Running","Running",new Color(0,180,0));
        timerLabel.addDisplayValue("Paused","Paused",new Color(180,180,0));
        timerLabel.addDisplayValue("Set","Set",new Color(0,0,180));
        timerLabel.addDisplayValue("Stopped","Stopped",new Color(180,0,0));
        timerLabel.registerComponent(mjec.database, false);

        pwrOnButton = new MJECButton("BMPToolbox.BmpPlcAgent:"+_ip+".power", "On", "POWER_ON::"+_ip+" "+mjec.getUid()+" Y1", MJECButton.COLOR_SCHEME_GREEN, mjec);
        pwrOffButton = new MJECButton("BMPToolbox.BmpPlcAgent:"+_ip+".power", "Off", "POWER_OFF::"+_ip+" "+mjec.getUid()+" Y1", MJECButton.COLOR_SCHEME_RED, mjec);
        startTimerButton = new MJECButton("BMPToolbox.BmpPlcAgent:"+_ip+".timerStat", "Start", "START_TIMER::"+_ip+" "+mjec.getUid()+" Y1", MJECButton.COLOR_SCHEME_GREEN, mjec);
        pauseTimerButton = new MJECButton("BMPToolbox.BmpPlcAgent:"+_ip+".timerStat", "Pause", "PAUSE_TIMER::"+_ip+" "+mjec.getUid()+" Y1", MJECButton.COLOR_SCHEME_ORANGE, mjec);
        resumeTimerButton = new MJECButton("BMPToolbox.BmpPlcAgent:"+_ip+".timerStat", "Resume", "RESUME_TIMER::"+_ip+" "+mjec.getUid()+" Y1", MJECButton.COLOR_SCHEME_ROYAL, mjec);

        pwrEnableBox.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent ae) {
	    pwrOnButton.setEnabled(pwrEnableBox.isSelected());
            pwrOffButton.setEnabled(pwrEnableBox.isSelected());
	  }
	});

	timerEnableBox.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
            startTimerButton.setEnabled(timerEnableBox.isSelected());
            pauseTimerButton.setEnabled(timerEnableBox.isSelected());
            resumeTimerButton.setEnabled(timerEnableBox.isSelected());
          }
        });

        timerInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".timer");
        timerInput.setCommand("SET_TIMER::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        timerInput.allowOnlyNums(false, true);
        timerInput.registerComponent(mjec.database, true);
        timerOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".timer");
        timerOutput.registerComponent(mjec.database, false);

        irrigInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".irrigation");
        irrigInput.setCommand("SET_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        irrigInput.allowOnlyNums(false, true);
        irrigInput.registerComponent(mjec.database, true);
        irrigOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".irrigation");
        irrigOutput.registerComponent(mjec.database, false);

        maxIrrigInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".maxIrrig");
        maxIrrigInput.setCommand("SET_MAX_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        maxIrrigInput.allowOnlyNums(false, true);
        maxIrrigInput.registerComponent(mjec.database, true);
        maxIrrigOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".maxIrrig");
        maxIrrigOutput.registerComponent(mjec.database, false);

        minIrrigInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".minIrrig");
        minIrrigInput.setCommand("SET_MIN_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        minIrrigInput.allowOnlyNums(false, true);
        minIrrigInput.registerComponent(mjec.database, true);
        minIrrigOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".minIrrig");
        minIrrigOutput.registerComponent(mjec.database, false);

        irrigRateInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".irrigationRate");
        irrigRateInput.setCommand("SET_IRRIGATION_RATE::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        irrigRateInput.allowOnlyNums(false, true);
        irrigRateInput.registerComponent(mjec.database, true);
        irrigRateOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".irrigationRate");
        irrigRateOutput.registerComponent(mjec.database, false);

        String[] zeroOpts = { "Yes", "No" };
        allowZeroInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".allowZero", zeroOpts, UFMMTComboBox.ITEM);
        allowZeroInput.registerComponent(mjec.database, true);
        allowZeroInput.setCommand("SET_ALLOW_ZERO::"+_ip+" "+mjec.getUid()+" ");
        allowZeroOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".allowZero");
        allowZeroOutput.registerComponent(mjec.database, false);

        pwrOnButton.setEnabled(false);
        pwrOffButton.setEnabled(false);

	startTimerButton.setEnabled(false);
	pauseTimerButton.setEnabled(false);
	resumeTimerButton.setEnabled(false);

        String[] defaultOpts = { "Yesterday", "3-day Avg", "5-day Max", "None", "Manual Default" }; 
        defaultIrrigationInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".defaultIrrigation", defaultOpts, UFMMTComboBox.ITEM);
        defaultIrrigationInput.registerComponent(mjec.database, true);
        defaultIrrigationInput.setCommand("SET_DEFAULT_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        defaultIrrigationOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".defaultIrrigation");
        defaultIrrigationOutput.registerComponent(mjec.database, false);

        manualDefaultInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".manualDefault");
        manualDefaultInput.setCommand("SET_MANUAL_DEFAULT::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        manualDefaultInput.allowOnlyNums(false, true);
        manualDefaultInput.registerComponent(mjec.database, true);
        manualDefaultOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".manualDefault");
        manualDefaultOutput.registerComponent(mjec.database, false);

	height = Math.floor(960./12.)/1000.;
        plcPanel.add("0.01,0.01;0.98,"+height, new JLabel("PLC Info", JLabel.CENTER));
        plcPanel.add("0.50,"+(0.01+height)+";0.25,"+height, new JLabel("Input",JLabel.CENTER));
        plcPanel.add("0.75,"+(0.01+height)+";0.25,"+height, new JLabel("Currently",JLabel.CENTER));
        plcPanel.add("0.01,"+(0.01+2*height)+";0.98,0.04", new JSeparator());
        plcPanel.add("0.01,"+(2*height+0.04)+";0.49,"+height, new JLabel("Timer (seconds)"));
        plcPanel.add("0.50,"+(2*height+0.04)+";0.25,"+height, timerInput);
        plcPanel.add("0.75,"+(2*height+0.04)+";0.25,"+height, timerOutput);
        plcPanel.add("0.01,"+(3*height+0.04)+";0.49,"+height, new JLabel("Irrigation (cm)"));
        plcPanel.add("0.50,"+(3*height+0.04)+";0.25,"+height, irrigInput);
        plcPanel.add("0.75,"+(3*height+0.04)+";0.25,"+height, irrigOutput);
        plcPanel.add("0.01,"+(4*height+0.04)+";0.49,"+height, new JLabel("Max Irrigation (cm)"));
        plcPanel.add("0.50,"+(4*height+0.04)+";0.25,"+height, maxIrrigInput);
        plcPanel.add("0.75,"+(4*height+0.04)+";0.25,"+height, maxIrrigOutput);
        plcPanel.add("0.01,"+(5*height+0.04)+";0.49,"+height, new JLabel("Min Irrigation (cm)"));
        plcPanel.add("0.50,"+(5*height+0.04)+";0.25,"+height, minIrrigInput);
        plcPanel.add("0.75,"+(5*height+0.04)+";0.25,"+height, minIrrigOutput);
        plcPanel.add("0.01,"+(6*height+0.04)+";0.49,"+height, new JLabel("Allow Zero Irrigation?"));
        plcPanel.add("0.50,"+(6*height+0.04)+";0.25,"+height, allowZeroInput);
        plcPanel.add("0.75,"+(6*height+0.04)+";0.25,"+height, allowZeroOutput);
        plcPanel.add("0.01,"+(7*height+0.04)+";0.49,"+height, new JLabel("Irrig Rate (cm/hour)"));
        plcPanel.add("0.50,"+(7*height+0.04)+";0.25,"+height, irrigRateInput);
        plcPanel.add("0.75,"+(7*height+0.04)+";0.25,"+height, irrigRateOutput);

        plcPanel.add("0.01,"+(8*height+0.04)+";0.42,"+height, pwrEnableBox); 
        plcPanel.add("0.43,"+(8*height+0.04)+";0.16,"+height, pwrOnButton); 
        plcPanel.add("0.59,"+(8*height+0.04)+";0.16,"+height, pwrOffButton); 
        plcPanel.add("0.75,"+(8*height+0.04)+";0.25,"+height, pwrLabel); 
        plcPanel.add("0.01,"+(9*height+0.04)+";0.26,"+height, timerEnableBox);
        plcPanel.add("0.27,"+(9*height+0.04)+";0.16,"+height, startTimerButton);
        plcPanel.add("0.43,"+(9*height+0.04)+";0.16,"+height, pauseTimerButton);
        plcPanel.add("0.59,"+(9*height+0.04)+";0.16,"+height, resumeTimerButton);
        plcPanel.add("0.75,"+(9*height+0.04)+";0.25,"+height, timerLabel);

        plcPanel.add("0.01,"+(10*height+0.04)+";0.49,"+height, new JLabel("Default Irrig Method"));
        plcPanel.add("0.50,"+(10*height+0.04)+";0.25,"+height, defaultIrrigationInput);
        plcPanel.add("0.75,"+(10*height+0.04)+";0.25,"+height, defaultIrrigationOutput);
        plcPanel.add("0.01,"+(11*height+0.04)+";0.49,"+height, new JLabel("Manual Default (cm)"));
        plcPanel.add("0.50,"+(11*height+0.04)+";0.25,"+height, manualDefaultInput);
        plcPanel.add("0.75,"+(11*height+0.04)+";0.25,"+height, manualDefaultOutput);
        plcPanel.setBorder(new EtchedBorder(0));
        plcPanel.setBackground(Color.cyan);

        /* CCrop Panel */
        JPanel ccropPanel = new JPanel();
        ccropPanel.setLayout(new RatioLayout());

        String[] hours = new String[24];
	for (int j = 0; j < hours.length; j++) hours[j] = ""+j; 
        hourInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".hour", hours, UFMMTComboBox.ITEM);
        hourInput.registerComponent(mjec.database, true);
        hourInput.setCommand("SET_HOUR::"+_ip+" "+mjec.getUid()+" ");
        hourLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".hour");
        hourLabel.registerComponent(mjec.database, false);

        String[] minutes = new String[60];
        for (int j = 0; j < minutes.length; j++) {
	  if (j < 10) minutes[j] = "0"+j; else minutes[j] = ""+j;
	}
        minuteInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".minute", minutes, UFMMTComboBox.ITEM);
        minuteInput.registerComponent(mjec.database, true);
        minuteInput.setCommand("SET_MINUTE::"+_ip+" "+mjec.getUid()+" ");
        minuteLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".minute");
        minuteLabel.registerComponent(mjec.database, false);

        lagTimeInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".lagTime");
        lagTimeInput.setCommand("SET_LAGTIME::"+_ip+" "+mjec.getUid()+" ");
        //allow neither decimal nor - 
        lagTimeInput.allowOnlyNums(false, false);
        lagTimeInput.registerComponent(mjec.database, true);
        lagTimeOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".lagTime");
        lagTimeOutput.registerComponent(mjec.database, false);

	String[] sleepTimes = {"1", "2", "3", "5", "10"};
	sleepTimeInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".sleepTime", sleepTimes, UFMMTComboBox.ITEM);
        sleepTimeInput.registerComponent(mjec.database, true);
        sleepTimeInput.setCommand("SET_SLEEP_TIME::"+_ip+" "+mjec.getUid()+" ");
        sleepTimeLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".sleepTime");
        sleepTimeLabel.registerComponent(mjec.database, false);

        height = Math.floor(960./5.)/1000.;
        ccropPanel.add("0.01,0.01;0.98,"+height, new JLabel("Timer Info", JLabel.CENTER));
        ccropPanel.add("0.34,"+(0.01+height)+";0.33,"+height, new JLabel("Input",JLabel.CENTER));
        ccropPanel.add("0.67,"+(0.01+height)+";0.33,"+height, new JLabel("Currently",JLabel.CENTER));
        ccropPanel.add("0.01,"+(0.01+2*height)+";0.98,0.04", new JSeparator());
        ccropPanel.add("0.01,"+(2*height+0.04)+";0.33,"+height, new JLabel("Time"));
        ccropPanel.add("0.34,"+(2*height+0.04)+";0.15,"+height, hourInput);
	ccropPanel.add("0.49,"+(2*height+0.04)+";0.03,"+height, new JLabel(":", JLabel.CENTER));
        ccropPanel.add("0.52,"+(2*height+0.04)+";0.15,"+height, minuteInput);
        ccropPanel.add("0.67,"+(2*height+0.04)+";0.15,"+height, hourLabel);
        ccropPanel.add("0.82,"+(2*height+0.04)+";0.03,"+height, new JLabel(":", JLabel.CENTER));
        ccropPanel.add("0.85,"+(2*height+0.04)+";0.15,"+height, minuteLabel);
        ccropPanel.add("0.01,"+(3*height+0.04)+";0.33,"+height, new JLabel("Lag Time (sec)"));
        ccropPanel.add("0.34,"+(3*height+0.04)+";0.33,"+height, lagTimeInput);
        ccropPanel.add("0.67,"+(3*height+0.04)+";0.33,"+height, lagTimeOutput); 
        ccropPanel.add("0.01,"+(4*height+0.04)+";0.33,"+height, new JLabel("Sleep Time (sec)"));
        ccropPanel.add("0.34,"+(4*height+0.04)+";0.33,"+height, sleepTimeInput);
        ccropPanel.add("0.67,"+(4*height+0.04)+";0.33,"+height, sleepTimeLabel);

        ccropPanel.setBorder(new EtchedBorder(0));
        ccropPanel.setBackground(Color.green);


	/* Timer panel */
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new RatioLayout());

        timerStatusLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".timerStatusLabel");
        timerStatusLabel.registerComponent(mjec.database, false);
        timerLabel.addDisplayValue("Timer Off","Timer Off",new Color(0,0,180));
        timerLabel.addDisplayValue("Timer will start in:","Timer will start in:",new Color(180,0,0));
        timerLabel.addDisplayValue("Timer value:","Timer value:",new Color(0,180,0));

        timerValLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".timerValue");
        timerValLabel.registerComponent(mjec.database, false);

	timerPanel.add("0.01,0.01;0.65,0.98", timerStatusLabel);
	timerPanel.add("0.67,0.01;0.32,0.98", timerValLabel);
        timerPanel.setBorder(new EtchedBorder(0));
        timerPanel.setBackground(Color.orange);

	lastMessageLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".lastMessage");
	lastMessageLabel.registerComponent(mjec.database, false);

        saveValuesButton = new UFColorButton("Save Values", UFColorButton.COLOR_SCHEME_GREEN);
        saveValuesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String commandString = "SAVE_DEFAULTS::"+mjec.getUid()+" "+_ip;
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                mjec.apply(commandVec);
            }
	});

	/* Progress bar */
/*
	progressBar = new UFMMTProgressBar("ufmmtExecAgent.ExecutiveAgent:currentFrame", 2, "Frame", "");
	progressBar.registerComponent(mjec.database, false);
	progressBar.registerContext(mjec.database, "ufmmtExecAgent.ExecutiveAgent:totalFrames");
*/

	add("0.01,0.01;0.99,0.10",topPanel);
        add("0.01,0.11;0.54,0.69",plcPanel);
        add("0.56,0.11;0.43,0.30",ccropPanel);
	add("0.56,0.43;0.43,0.10",timerPanel);
	add("0.85,0.55;0.14,0.10",saveValuesButton);
	add("0.56,0.61;0.15,0.05",new JLabel("Last Message:"));
	add("0.56,0.66;0.43,0.20",lastMessageLabel);
	//add("0.46,0.90;0.52,0.03",new JLabel("OBSERVATION PROGRESS", JLabel.CENTER));
	//add("0.46,0.93;0.52,0.05",progressBar);
	add("0.01,0.90;0.99,0.10",_buttonPanel);
    } 

    public String getIp() {
      return _ip;
    }

    public String getType() {
      return _type;
    }

    public void setupCSV() {
        double height;

        /* Top Panel */
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new RatioLayout());

        int n = mjec.runsAndZones.size()+3;
        String[] runZoneKeys = new String[n];
        String[] runZoneVals = new String[n];
        String key, val;
        runZoneKeys[0] = "None 0";
        runZoneVals[0] = "None";
	runZoneKeys[1] = "All -1";
	runZoneVals[1] = "All Zones";
        runZoneKeys[2] = "Fixed -2";
        runZoneVals[2] = "Fixed Manual Default";
        int idx = 3;
        for (Iterator i = mjec.runsAndZones.keySet().iterator(); i.hasNext(); ) {
          key = (String)i.next();
          val = (String)mjec.runsAndZones.get(key);
          runZoneKeys[idx] = key;
          runZoneVals[idx] = val;
          idx++;
        }
        runZoneInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".id", runZoneVals, runZoneKeys, UFMMTComboBox.REFERENCE);
        runZoneInput.registerComponent(mjec.database, true);
        runZoneInput.setCommand("SET_ZONE::"+_ip+" "+mjec.getUid()+" ");

        runZoneNameOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".name");
        runZoneNameOutput.registerComponent(mjec.database, false);

        irrigatorLogInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".irrigatorLog");
        irrigatorLogInput.setCommand("SET_IRRIGATOR_LOG::"+_ip+" "+mjec.getUid()+" ");
        irrigatorLogInput.allowOnlyAlphaNumeric();
        irrigatorLogInput.registerComponent(mjec.database, true);
        irrigatorLogOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".irrigatorLog");
        irrigatorLogOutput.registerComponent(mjec.database, false);

        topPanel.add("0.01,0.01;0.27,0.45", new JLabel("Assign a Run or Zone", JLabel.CENTER));
        topPanel.add("0.01,0.51;0.27,0.45", runZoneInput);

        topPanel.add("0.28,0.01;0.25,0.45", new JLabel("Current Run/Zone", JLabel.CENTER));
        topPanel.add("0.28,0.51;0.25,0.45", runZoneNameOutput);

        topPanel.add("0.55,0.01;0.22,0.45", new JLabel("Assign History Log", JLabel.CENTER));
        topPanel.add("0.55,0.51;0.22,0.45", irrigatorLogInput);

        topPanel.add("0.77,0.01;0.22,0.45", new JLabel("Current History Log", JLabel.CENTER));
        topPanel.add("0.77,0.51;0.22,0.45", irrigatorLogOutput);
        topPanel.setBorder(new EtchedBorder(0));
        topPanel.setBackground(Color.LIGHT_GRAY);

        /* CIRRIG Panel */
        JPanel cirrigPanel = new JPanel();
        cirrigPanel.setLayout(new RatioLayout());

        String[] hours = new String[24];
        for (int j = 0; j < hours.length; j++) hours[j] = ""+j;
        hourInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".hour", hours, UFMMTComboBox.ITEM);
        hourInput.registerComponent(mjec.database, true);
        hourInput.setCommand("SET_HOUR::"+_ip+" "+mjec.getUid()+" ");
        hourLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".hour");
        hourLabel.registerComponent(mjec.database, false);

        String[] minutes = new String[60];
        for (int j = 0; j < minutes.length; j++) {
          if (j < 10) minutes[j] = "0"+j; else minutes[j] = ""+j;
        }
        minuteInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".minute", minutes, UFMMTComboBox.ITEM);
        minuteInput.registerComponent(mjec.database, true);
        minuteInput.setCommand("SET_MINUTE::"+_ip+" "+mjec.getUid()+" ");
        minuteLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".minute");
        minuteLabel.registerComponent(mjec.database, false);

        maxIrrigInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".maxIrrig");
        maxIrrigInput.setCommand("SET_MAX_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        maxIrrigInput.allowOnlyNums(false, true);
        maxIrrigInput.registerComponent(mjec.database, true);
        maxIrrigOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".maxIrrig");
        maxIrrigOutput.registerComponent(mjec.database, false);

        minIrrigInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".minIrrig");
        minIrrigInput.setCommand("SET_MIN_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        minIrrigInput.allowOnlyNums(false, true);
        minIrrigInput.registerComponent(mjec.database, true);
        minIrrigOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".minIrrig");
        minIrrigOutput.registerComponent(mjec.database, false);

        irrigRateInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".irrigationRate");
        irrigRateInput.setCommand("SET_IRRIGATION_RATE::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        irrigRateInput.allowOnlyNums(false, true);
        irrigRateInput.registerComponent(mjec.database, true);
        irrigRateOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".irrigationRate");
        irrigRateOutput.registerComponent(mjec.database, false);

        String[] zeroOpts = { "Yes", "No" };
        allowZeroInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".allowZero", zeroOpts, UFMMTComboBox.ITEM);
        allowZeroInput.registerComponent(mjec.database, true);
        allowZeroInput.setCommand("SET_ALLOW_ZERO::"+_ip+" "+mjec.getUid()+" ");
        allowZeroOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".allowZero");
        allowZeroOutput.registerComponent(mjec.database, false);

        String[] defaultOpts = { "Yesterday", "3-day Avg", "5-day Max", "None", "Manual Default" };
        defaultIrrigationInput = new UFMMTComboBox("BMPToolbox.BmpPlcAgent:"+_ip+".defaultIrrigation", defaultOpts, UFMMTComboBox.ITEM);
        defaultIrrigationInput.registerComponent(mjec.database, true);
        defaultIrrigationInput.setCommand("SET_DEFAULT_IRRIGATION::"+_ip+" "+mjec.getUid()+" ");
        defaultIrrigationOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".defaultIrrigation");
        defaultIrrigationOutput.registerComponent(mjec.database, false);

        manualDefaultInput = new UFMMTTextField("BMPToolbox.BmpPlcAgent:"+_ip+".manualDefault");
        manualDefaultInput.setCommand("SET_MANUAL_DEFAULT::"+_ip+" "+mjec.getUid()+" ");
        //allow decimal but not -
        manualDefaultInput.allowOnlyNums(false, true);
        manualDefaultInput.registerComponent(mjec.database, true);
        manualDefaultOutput = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".manualDefault");
        manualDefaultOutput.registerComponent(mjec.database, false);

        height = Math.floor(960./9.)/1000.;
        cirrigPanel.add("0.01,0.01;0.98,"+height, new JLabel("CIRRIG Info", JLabel.CENTER));
        cirrigPanel.add("0.50,"+(0.01+height)+";0.25,"+height, new JLabel("Input",JLabel.CENTER));
        cirrigPanel.add("0.75,"+(0.01+height)+";0.25,"+height, new JLabel("Currently",JLabel.CENTER));
        cirrigPanel.add("0.01,"+(0.01+2*height)+";0.98,0.04", new JSeparator());
        cirrigPanel.add("0.01,"+(2*height+0.04)+";0.33,"+height, new JLabel("Time to Output"));
        cirrigPanel.add("0.38,"+(2*height+0.04)+";0.15,"+height, hourInput);
        cirrigPanel.add("0.53,"+(2*height+0.04)+";0.03,"+height, new JLabel(":", JLabel.CENTER));
        cirrigPanel.add("0.56,"+(2*height+0.04)+";0.15,"+height, minuteInput);
        cirrigPanel.add("0.73,"+(2*height+0.04)+";0.12,"+height, hourLabel);
        cirrigPanel.add("0.85,"+(2*height+0.04)+";0.03,"+height, new JLabel(":", JLabel.CENTER));
        cirrigPanel.add("0.88,"+(2*height+0.04)+";0.12,"+height, minuteLabel);
        cirrigPanel.add("0.01,"+(4*height+0.04)+";0.49,"+height, new JLabel("Max Irrigation (cm)"));
        cirrigPanel.add("0.50,"+(4*height+0.04)+";0.25,"+height, maxIrrigInput);
        cirrigPanel.add("0.75,"+(4*height+0.04)+";0.25,"+height, maxIrrigOutput);
        cirrigPanel.add("0.01,"+(5*height+0.04)+";0.49,"+height, new JLabel("Min Irrigation (cm)"));
        cirrigPanel.add("0.50,"+(5*height+0.04)+";0.25,"+height, minIrrigInput);
        cirrigPanel.add("0.75,"+(5*height+0.04)+";0.25,"+height, minIrrigOutput);
        cirrigPanel.add("0.01,"+(6*height+0.04)+";0.49,"+height, new JLabel("Allow Zero Irrigation?"));
        cirrigPanel.add("0.50,"+(6*height+0.04)+";0.25,"+height, allowZeroInput);
        cirrigPanel.add("0.75,"+(6*height+0.04)+";0.25,"+height, allowZeroOutput);

        cirrigPanel.add("0.01,"+(7*height+0.04)+";0.49,"+height, new JLabel("Default Irrig Method"));
        cirrigPanel.add("0.50,"+(7*height+0.04)+";0.25,"+height, defaultIrrigationInput);
        cirrigPanel.add("0.75,"+(7*height+0.04)+";0.25,"+height, defaultIrrigationOutput);
        cirrigPanel.add("0.01,"+(8*height+0.04)+";0.49,"+height, new JLabel("Manual Default (cm)"));
        cirrigPanel.add("0.50,"+(8*height+0.04)+";0.25,"+height, manualDefaultInput);
        cirrigPanel.add("0.75,"+(8*height+0.04)+";0.25,"+height, manualDefaultOutput);
        cirrigPanel.setBorder(new EtchedBorder(0));
        cirrigPanel.setBackground(Color.cyan);

        lastMessageLabel = new UFMMTLabel("BMPToolbox.BmpPlcAgent:"+_ip+".lastMessage");
        lastMessageLabel.registerComponent(mjec.database, false);

        saveValuesButton = new UFColorButton("Save Values", UFColorButton.COLOR_SCHEME_GREEN);
        saveValuesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String commandString = "SAVE_DEFAULTS::"+mjec.getUid()+" "+_ip;
                Vector <String> commandVec = new Vector();
                commandVec.add(commandString);
                mjec.apply(commandVec);
            }
        });

        add("0.01,0.01;0.99,0.10",topPanel);
        add("0.01,0.11;0.54,0.50",cirrigPanel);
        add("0.85,0.55;0.14,0.10",saveValuesButton);
        add("0.56,0.61;0.15,0.05",new JLabel("Last Message:"));
        add("0.56,0.66;0.43,0.20",lastMessageLabel);
        add("0.01,0.90;0.99,0.10",_buttonPanel);
    }

    public void updateRunsAndZones() {
      int n = mjec.runsAndZones.size()+2;
      if (_type.equals("CSVOutput")) n++; 
      String[] runZoneKeys = new String[n];
      String[] runZoneVals = new String[n];
      String key, val;
      runZoneKeys[0] = "None 0";
      runZoneVals[0] = "None";
      int idx = 1;
      if (_type.equals("CSVOutput")) {
        runZoneKeys[1] = "All -1";
        runZoneVals[1] = "All Zones";
        idx = 2;
      }
      runZoneKeys[idx] = "Fixed -2";
      runZoneVals[idx] = "Fixed Manual Default";
      idx++;
      for (Iterator i = mjec.runsAndZones.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
        val = (String)mjec.runsAndZones.get(key);
        runZoneKeys[idx] = key;
        runZoneVals[idx] = val;
        idx++;
      }
      runZoneInput.updateArrays(runZoneVals, runZoneKeys);
    } 

} //end of class JPanelPLC
