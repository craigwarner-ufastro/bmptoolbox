package CirrigPlc; 
/**
 * Title:        CirrigPlcAgent
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for java agents to override
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.sql.*;
import javax.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;
import CCROP.Zone;

//=====================================================================================================

public class CirrigPlcAgent extends UFMMTThreadedAgent { 

    public static final
	String rcsID = "$Name:  $ $Id: CirrigPlcAgent.java,v 1.1 2017/02/10 22:58:57 mybmp Exp $";

    protected String _mainClass = getClass().getName();
    protected String ccropHost = "www.bmptoolbox.org";
    protected boolean _initialized = false, _sqlConnected = false;
    protected int ccropPort = 57002, weewxPort = 57006;
    protected IrrigatorDatabase _irrDatabase;
    protected String _logdir = UFExecCommand.getEnvVar("HOME")+"/bmplogs";
    protected Vector <Integer> userList;
    protected LinkedHashMap <String, IrrigPollingThread> irrigPollingThreadDb;
    protected LinkedHashMap <String, IrrigationMonitoringThread> irrigationMonitoringThreadDb;

    protected String dbName = "irrigHistory";
    protected String dbUrl = "jdbc:sqlite:"+UFExecCommand.getEnvVar("HOME")+"/irrigHistory.sdb";
    protected String dbClass = "org.sqlite.JDBC";
    protected Connection _dbCon; //database connection
    protected String sqlUser = "", sqlPass = "";


//----------------------------------------------------------------------------------------

    public CirrigPlcAgent( int serverPort, String[] args )
    {
	super(serverPort, args);
        options(args);
	installDir = UFExecCommand.getEnvVar("BMPINSTALL");
	//Windows
	if (installDir == null) installDir = "..";
	if (installDir.startsWith("null")) installDir = "..";
	if (_logdir == null || _logdir.startsWith("null")) {
	  _logdir = "../bmplogs";
	  File dir = new File(_logdir);
	  if (!dir.exists()) dir.mkdirs();
	}
	_irrDatabase = new IrrigatorDatabase();
        userList = new Vector(10); 
        irrigPollingThreadDb = new LinkedHashMap(10);
	irrigationMonitoringThreadDb = new LinkedHashMap(10);
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      super.options(args);
      /* Handle any options specific to this agent here */
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().indexOf("-ccrophost") != -1) {
          if (args.length > j+1) ccropHost = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-ccropport") != -1) {
	  if (args.length > j+1) try {
	    ccropPort = Integer.parseInt(args[j+1]);
	  } catch(NumberFormatException nfe) {
	    ccropPort = 57002;
	  }
        } else if (args[j].toLowerCase().indexOf("-weewxport") != -1) {
          if (args.length > j+1) try {
            weewxPort = Integer.parseInt(args[j+1]);
          } catch(NumberFormatException nfe) {
            weewxPort = 57002;
          }
	} else if (args[j].toLowerCase().indexOf("-logdir") != -1) {
	  if (args.length > j+1) _logdir = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-dbpath") != -1) {
          if (args.length > j+1) dbUrl = "jdbc:sqlite:"+args[j+1]+"?autoReconnect=true";
        } else if (args[j].toLowerCase().indexOf("-dbname") != -1) {
          if (args.length > j+1) dbName = args[j+1];
        }
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void _startupAncillaryThread()
    {
        /* Start the ancillary thread.  This method should be overriden by subclasses */

        System.out.println(_mainClass + "> starting Ancillary thread...");
        _ancillary = new AncillaryThread();
        _ancillary.start();
    }

//----------------------------------------------------------------------------------------

    /** Setup database */
    protected void setDefaults() {
      super.setDefaults();
      /* Database records specific to this agent */
      addMMTRecord(_mainClass+":lastMessage", UFRecord.TYPE_STRING, "None");
      return;
    }

//-----------------------------------------------------------------------------------------------------

    /** Save irrigator values */

    protected void saveDefaults(String ipToSave) {
      saveDefaults(ipToSave, false);
    }

    protected void saveDefaults(String irrigIP, boolean delIrrig) {
      LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigators();
      try {
        PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/cirrigList.xml"));
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<cirrigList>");
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
	  CirrigIrrigator irr = (CirrigIrrigator)irrigList.get(key);
	  if (delIrrig && irrigIP.equals(irr.getHost())) continue; //skip if deleting this irrigator
	  pw.print("  <irrigator type=\"" + irr.getType() + "\" uid=\"" + irr.getUid() + "\" ip=\"" + irr.getHost() + "\" ");
	  pw.println("noutlets=\"" + irr.getNOutlets() + "\" register=\"" + irr.getConfig().getFirstRegister()+"\" stride=\"" + irr.getConfig().getStride() + "\" ncounters=\"" + irr.getNCounters() + "\">");
	  for (Iterator<ZoneGroup> zi = irr.getZoneGroups().iterator(); zi.hasNext();) {
            ZoneGroup zg = (ZoneGroup)zi.next();
	    pw.println(zg.getXML()); //use getXML() method to write out xml for individual zone groups including zoneoutlets and irrigtasks
	  }
	  pw.println("  </irrigator>");
	}  
        pw.println("</cirrigList>");
        pw.close();

        ArrayList<String> currentValues = new ArrayList();
        currentValues.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        currentValues.add("<database>");
        String xmlFile = installDir+"/etc/databaseStartupValues.xml";
        String[] recsToSave = { "id", "name", "hour", "minute", "sleepTime", "maxIrrig", "minIrrig", "allowZero", "irrigationRate", "irrigatorLog", "HTMLLog", "defaultIrrigation", "manualDefault", "maxSimultaneous", "allowMultipleGroups", "ncycles", "cycleMode", "mode", "systemTestMinutes"};
        Document doc = null;
        File file = new File(xmlFile);
	boolean defaultsExist = false;
	String key;
	if (file.exists()) {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(file);
          doc.getDocumentElement().normalize();

          Element root = doc.getDocumentElement();
          NodeList nlist;
          Element elem;
          nlist = root.getElementsByTagName("agent");
          for (int j = 0; j < nlist.getLength(); j++) {
            Node fstNode = nlist.item(j);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
              Element fstElmnt = (Element) fstNode;
              if (fstElmnt.hasAttribute("name")) {
		currentValues.add("  <agent name=\"" + fstElmnt.getAttribute("name") + "\">");
                if (_mainClass.toLowerCase().indexOf(fstElmnt.getAttribute("name").trim().toLowerCase()) == -1) {
		  //defaults for another agent, simply copy them all
                  NodeList reclist = fstElmnt.getElementsByTagName("record");
                  for (int i = 0; i < reclist.getLength(); i++) {
                    try {
                      Node recNode = reclist.item(i);
		      if (recNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element recElmnt = (Element)recNode;
                        NodeList recnameList = recElmnt.getElementsByTagName("name");
                        elem = (Element)recnameList.item(0);
                        String recName = elem.getFirstChild().getNodeValue().trim();
                        NodeList recvalList = recElmnt.getElementsByTagName("value");
                        elem = (Element)recvalList.item(0);
                        String recVal = elem.getFirstChild().getNodeValue().trim();
			currentValues.add("    <record>");
			currentValues.add("      <name>"+recName+"</name>");
			currentValues.add("      <value>"+recVal+"</value>");
			currentValues.add("    </record>");
		      }
                    } catch (Exception e) {
                      System.out.println(e.toString());
                    }
                  }
	        } else {
		  //this is default section for CirrigPlcAgent
		  defaultsExist = true;
                  NodeList reclist = fstElmnt.getElementsByTagName("record");
                  for (int i = 0; i < reclist.getLength(); i++) {
                    try {
                      Node recNode = reclist.item(i);
                      if (recNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element recElmnt = (Element)recNode;
                        NodeList recnameList = recElmnt.getElementsByTagName("name");
                        elem = (Element)recnameList.item(0);
                        String recName = elem.getFirstChild().getNodeValue().trim();
                        NodeList recvalList = recElmnt.getElementsByTagName("value");
                        elem = (Element)recvalList.item(0);
                        String recVal = elem.getFirstChild().getNodeValue().trim();
			if (!recName.startsWith(irrigIP)) {
			  //different irrigator -- copy records
                          currentValues.add("    <record>");
                          currentValues.add("      <name>"+recName+"</name>");
                          currentValues.add("      <value>"+recVal+"</value>");
                          currentValues.add("    </record>");
			}
                      }
                    } catch (Exception e) {
                      System.out.println(e.toString());
                    }
                  }
		  //now add in recs for this irrigIP if !delIrrig
		  if (!delIrrig) {
		    synchronized(database) {
		      for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
			key = (String)i.next();
			if (key.startsWith(_mainClass+":"+irrigIP)) {
			  for (int l = 0; l < recsToSave.length; l++) {
			    if (key.indexOf(recsToSave[l]) != -1) {
			      //write this record
                              currentValues.add("    <record>");
                              currentValues.add("      <name>"+key.substring(key.indexOf(irrigIP))+"</name>");
                              currentValues.add("      <value>"+database.get(key).getValue()+"</value>");
                              currentValues.add("    </record>");
			      break;
			    }
			  } 
			} 
		      }
		    }
		  }
		}
		currentValues.add("  </agent>");
              }
            }
          }
	} if (!defaultsExist) {
	  //must add agent tag
	  currentValues.add("  <agent name=\"" + _mainClass + "\">");
	  //add in recs for this irrigIP if !delIrrig
          if (!delIrrig) {
            synchronized(database) {
              for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
                key = (String)i.next();
                if (key.startsWith(_mainClass+":"+irrigIP)) {
                  for (int l = 0; l < recsToSave.length; l++) {
                    if (key.indexOf(recsToSave[l]) != -1) {
                      //write this record
                      currentValues.add("    <record>");
                      currentValues.add("      <name>"+key.substring(key.indexOf(irrigIP))+"</name>");
                      currentValues.add("      <value>"+database.get(key).getValue()+"</value>");
                      currentValues.add("    </record>");
                      break;
                    }
                  }
                }
              }
	    }
	  }
	  currentValues.add("  </agent>");
	}
        currentValues.add("</database>");

        pw = new PrintWriter(new FileOutputStream(installDir+"/etc/databaseStartupValues.xml"));
	for (int j = 0; j < currentValues.size(); j++) {
	  pw.println(currentValues.get(j));
	}
        pw.close();
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }

//-----------------------------------------------------------------------------------------------------

    /** Read XML irrigList.xml file and add CirrigIrrigators */
    protected void readIrrigatorList() {
      String irrigFile = installDir+"/etc/cirrigList.xml";
      Document doc = null;
      try {
        File file = new File(irrigFile);
	if (!file.exists()) return;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(file);
      } catch(Exception e) {
        System.out.println(e.toString());
      }
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("irrigator");
      for (int j = 0; j < nlist.getLength(); j++) {
        int uid = -1, nOutlets = 16, pwrReg=1088, stride = 8, nCounters = 0;
	String irrigIP = ""; 
	String type = "";
        Node fstNode = nlist.item(j);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	  try {
	    Element fstElmnt = (Element) fstNode;
            if (fstElmnt.hasAttribute("type")) {
              type = fstElmnt.getAttribute("type").trim();
            }
            if (fstElmnt.hasAttribute("uid")) {
	      uid = Integer.parseInt(fstElmnt.getAttribute("uid").trim());
	    }
	    if (fstElmnt.hasAttribute("ip")) {
	      irrigIP = fstElmnt.getAttribute("ip").trim();
	    }
	    if (fstElmnt.hasAttribute("noutlets")) nOutlets = Integer.parseInt(fstElmnt.getAttribute("noutlets"));
            if (fstElmnt.hasAttribute("register")) pwrReg = Integer.parseInt(fstElmnt.getAttribute("register"));
            if (fstElmnt.hasAttribute("stride")) stride = Integer.parseInt(fstElmnt.getAttribute("stride"));
            if (fstElmnt.hasAttribute("ncounters")) nCounters = Integer.parseInt(fstElmnt.getAttribute("ncounters"));
	    CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride, nCounters);
	    System.out.println(_mainClass+"::readIrrigatorList | " + ctime() + "> Adding CirrigIrrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
            System.out.println("\tConfig = "+config.toString());
	    addIrrigator(type, irrigIP, uid, config);
	    //get irrigator here for use with ZoneGroups below
	    CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	    //now look for zone groups
            NodeList grouplist = fstElmnt.getElementsByTagName("group");
	    for (int i = 0; i < grouplist.getLength(); i++) {
	      Node groupNode = grouplist.item(i);
	      if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
	        Element groupElmnt = (Element)groupNode;
		if (groupElmnt.hasAttribute("name") && groupElmnt.hasAttribute("number")) {
		  String groupName = groupElmnt.getAttribute("name").trim();
		  int groupNum = Integer.parseInt(groupElmnt.getAttribute("number").trim());
		  ZoneGroup zg = new ZoneGroup(irr, groupName, groupNum);
		  System.out.println("\tAdding ZoneGroup "+groupName);
		  zg.updateHostAndPort(ccropHost, ccropPort, weewxPort);
		  boolean success = zg.updateFromXML(groupElmnt);
		  if (success) {
	  	    irr.addZoneGroup(zg);
		    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".ncycles", UFRecord.TYPE_INT, String.valueOf(zg.getNCycles()));
		    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".cycleMode", UFRecord.TYPE_STRING, zg.getCycleMode());
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".mode", UFRecord.TYPE_STRING, zg.getMode());
		    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(zg.getMaxSimultaneous()));
		    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".systemTestMinutes", UFRecord.TYPE_INT, String.valueOf(zg.getSystemTestMinutes()));
		    for (int l = 0; l < zg.getNCycles(); l++) {
		      int hour = zg.getHour(l);
		      if (hour != -1) addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".hour_"+l, UFRecord.TYPE_INT, String.valueOf(hour));
		      int minute = zg.getMinute(l);
		      if (minute != -1) {
		        String minuteStr = String.valueOf(minute);
		        if (minute < 10) minuteStr = "0"+minuteStr;
                        addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".minute_"+l, UFRecord.TYPE_INT, minuteStr); 
		      }
		    }
		  }
		}
	      }
	    }
	  } catch (Exception e) {
	    System.out.println(_mainClass+"::readIrrigatorList | "+ctime()+"> ERROR: "+e.toString());
	    continue;
	  }
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    /** Add users to userList on startup */
    protected void setUserList() {
      LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigators();
      String irrigIP, key;
      Integer uid;
      for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
	key = (String)i.next();
	irrigIP = getReplyToken(key, 0, "::");
        uid = new Integer(getReplyToken(key, 1, "::"));
	if (userList.contains(uid)) continue;
	if (_verbose) System.out.println(_mainClass+":setUserList | " + ctime() + "> Adding user "+uid);
	userList.add(uid);
      }
    }

    protected void propagateRecords() {
      synchronized(database) {
        String irrigIP, key, recName;
        CirrigIrrigator irr;
        String[] recsToPropagate = {"defaultIrrigation", "manualDefault", "irrigationRate" };
        LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigators();
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          key = (String)i.next();
          irrigIP = getReplyToken(key, 0, "::");
          irr = (CirrigIrrigator)irrigList.get(key);
	  recName = _mainClass+":"+irrigIP+".allowMultipleGroups";
	  if (database.containsKey(recName)) {
	    if (getMMTValue(recName).toLowerCase().equals("yes")) irr.setMultipleGroups(true); else irr.setMultipleGroups(false);
	  }
	  recName = _mainClass+":"+irrigIP+".maxSimultaneous";
	  if (database.containsKey(recName)) irr.setMaxSimultaneous(getMMTIntValue(recName));
	  for (int j = 0; j < irr.getNOutlets(); j++) {
	    for (int l = 0; l < recsToPropagate.length; l++) {
	      recName = _mainClass+":"+irrigIP+"."+j+"."+recsToPropagate[l]; 
	      if (database.containsKey(recName)) irr.propagateRecord(j, recsToPropagate[l], getMMTValue(recName));
	    }
	  }
        }
      }
    }

//-----------------------------------------------------------------------------------------------------
    /** Connect to device and exec agent
      * Subclasses should override this class with options specific to
      * connecting to the specific device.
      */
    protected void init() {
      /* set up database */
      setDefaults();
      //read CirrigIrrigator list first 
      readIrrigatorList();
      readXMLDefaults(); //read XML default values
      setUserList(); //setup user list
      propagateRecords(); //propagate records to individual ZoneOutlets
      _initialized = false;
      updateDatabase(_mainClass+":status", "INIT");
      if (_simMode) {
        System.out.println(_mainClass+"::init> Simulating connection to database...");
      } else {
	int n = 0;
        while (!_sqlConnected & n < 3) {
          //connect to sqlite database
          _sqlConnected = connectToSql();
          if (_sqlConnected) {
            System.out.println(_mainClass+"::init> Successfully connected to sqlite database");
          } else {
	    n++;
            System.out.println(_mainClass+"::init> Error!  Unable to connect to sqlite database");
          }
          hibernate();
        }
      }

      _initialized = true;
      updateDatabase(_mainClass+":status", "IDLE");
    }


//-----------------------------------------------------------------------------------------------------

    /** Add a new CirrigIrrigator */
    protected boolean addIrrigator(String type, String irrigIP, int uid, CirrigPLCConfig config) {
      CirrigIrrigator irr = null;
      if (type.toLowerCase().equals("cirrigplc")) {
	irr = new CirrigPLC(irrigIP, uid, config);
      } else if (type.toLowerCase().equals("simcirrigplc")) {
        irr = new SimCirrigPLC(irrigIP, uid, config);
      } else if (type.toLowerCase().equals("cirrigbatteryplc")) {
	irr = new CirrigBatteryPLC(irrigIP, uid, config);
      } else {
        System.out.println(_mainClass+"::addIrrigator | "+ctime()+"> ERROR: invalid irrigator type: "+type);
	return false;
      }
      if (_verbose) irr.verbose(_verbose);
      boolean success = connectToIrrigator(irr, 3);
      //ensure that all outlets in the irrigator are powered off.
      if (success) irr.powerOffAll();
      _irrDatabase.addIrrigator(irr);
      /* Database records specific to this CirrigIrrigator */
      addMMTRecord(_mainClass+":"+irrigIP+".status", UFRecord.TYPE_STRING, "Disconnected");
      addMMTRecord(_mainClass+":"+irrigIP+".irrigatorLog", UFRecord.TYPE_STRING, "irrigHistory."+irrigIP+".log");
      addMMTRecord(_mainClass+":"+irrigIP+".HTMLLog", UFRecord.TYPE_STRING, "irrigHistory."+irrigIP+".html");
      addMMTRecord(_mainClass+":"+irrigIP+".dailyHTMLLog", UFRecord.TYPE_STRING, "irrigHistory_daily."+irrigIP+".html");
      addMMTRecord(_mainClass+":"+irrigIP+".lastMessage", UFRecord.TYPE_STRING, "None");
      addMMTRecord(_mainClass+":"+irrigIP+".sleepTime", UFRecord.TYPE_INT, "1");
      addMMTRecord(_mainClass+":"+irrigIP+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(irr.getMaxSimultaneous()));
      addMMTRecord(_mainClass+":"+irrigIP+".allowMultipleGroups", UFRecord.TYPE_STRING, "No");
      /* Database records specific to outlets in this CirrigIrrigator */
      for (int j = 0; j < config.getNOutlets(); j++) {
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".id", UFRecord.TYPE_INT, "-1");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".name", UFRecord.TYPE_STRING, "None");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".power", UFRecord.TYPE_STRING, "Off");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".timerStat", UFRecord.TYPE_STRING, "Stopped");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".timer", UFRecord.TYPE_FLOAT, "0");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".irrigation", UFRecord.TYPE_FLOAT, "0");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".maxIrrig", UFRecord.TYPE_FLOAT, "10");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".minIrrig", UFRecord.TYPE_FLOAT, "0.01");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".allowZero", UFRecord.TYPE_STRING, "Yes");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".timerValue", UFRecord.TYPE_FLOAT, "0");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".timerStatusLabel", UFRecord.TYPE_STRING, "Timer Off");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".defaultIrrigation", UFRecord.TYPE_STRING, "5-day Max");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".manualDefault", UFRecord.TYPE_FLOAT, "0.0");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".irrigationRate", UFRecord.TYPE_FLOAT, "1.5");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".lastMessage", UFRecord.TYPE_STRING, "None");
        addMMTRecord(_mainClass+":"+irrigIP+"."+j+".lastOn", UFRecord.TYPE_INT, "0");
	addMMTRecord(_mainClass+":"+irrigIP+"."+j+".priority", UFRecord.TYPE_INT, "-1");

        irr.propagateRecord(j, "defaultIrrigation", "5-day Max");
        irr.propagateRecord(j, "manualDefault", "0");
	irr.propagateRecord(j, "irrigationRate", "1.5");
      }
      for (int j = 0; j < config.getNCounters(); j++) {
	addMMTRecord(_mainClass+":"+irrigIP+"."+j+".count", UFRecord.TYPE_INT, "-1");
      }
      return success; 
    }

    protected boolean deleteIrrigator(String irrigIP, int uid) {
      String key = irrigIP+"::"+uid;
      if (_irrDatabase.hasIrrigator(key)) {
	_irrDatabase.removeIrrigator(key);
        if (irrigPollingThreadDb.containsKey(key)) {
	  //remove ipt too
          IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
	  ipt.shutdown();
          irrigPollingThreadDb.remove(key);
	}
	if (irrigationMonitoringThreadDb.containsKey(key)) {
	  //remove imt also
	  IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(key);
	  imt.shutdown();
	  irrigationMonitoringThreadDb.remove(key);
	}
	//remove records from database
	synchronized(database) {
          for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
            key = (String)i.next();
	    if (key.startsWith(_mainClass+":"+irrigIP)) i.remove(); //use iterator remove
	  }
        }
	return true;
      }
      return false;
    }

    protected boolean changeIrrigator(String oldIP, String irrigIP, int uid) {
      String key = oldIP+"::"+uid;
      if (_irrDatabase.hasIrrigator(key)) {
	//save zone groups
	ArrayList<ZoneGroup> zglist = _irrDatabase.getIrrigator(key).getZoneGroups();
        CirrigPLCConfig config = _irrDatabase.getIrrigator(key).getConfig(); 
	String type = _irrDatabase.getIrrigatorType(key);
	_irrDatabase.removeIrrigator(key);
        if (irrigPollingThreadDb.containsKey(key)) {
          //remove ipt too
          IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
          ipt.shutdown();
          irrigPollingThreadDb.remove(key);
        }
        if (irrigationMonitoringThreadDb.containsKey(key)) {
          //remove imt also
          IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(key);
          imt.shutdown();
          irrigationMonitoringThreadDb.remove(key);
        }
        //remove records from database
	synchronized(database) {
          for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
            key = (String)i.next();
            if (key.startsWith(_mainClass+":"+irrigIP)) database.remove(key);
          }
	}
	boolean success = addIrrigator(type, irrigIP, uid, config);
	if (success) {
	  //get irrigator
	  CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  //add zone groups
          for (Iterator<ZoneGroup> zi = zglist.iterator(); zi.hasNext();) {
            ZoneGroup zg = (ZoneGroup)zi.next();
	    irr.addZoneGroup(zg);
	    //add database values associated with zone groups
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".ncycles", UFRecord.TYPE_INT, String.valueOf(zg.getNCycles()));
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".cycleMode", UFRecord.TYPE_STRING, zg.getCycleMode());
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".mode", UFRecord.TYPE_STRING, zg.getMode());
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(zg.getMaxSimultaneous()));
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".systemTestMinutes", UFRecord.TYPE_INT, String.valueOf(zg.getSystemTestMinutes()));
	    for (int i = 0; i < zg.getNCycles(); i++) {
              int hour = zg.getHour(i);
	      if (hour != -1) addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".hour_"+i, UFRecord.TYPE_INT, String.valueOf(hour));
              int minute = zg.getMinute(i);
              if (minute != -1) {
                String minuteStr = String.valueOf(minute);
                if (minute < 10) minuteStr = "0"+minuteStr;
                addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".minute_"+i, UFRecord.TYPE_INT, minuteStr);
	      }
	    }
          }
	}
	return success;
      }
      return false;
    }

//-----------------------------------------------------------------------------------------------------
    protected String exportConfig(String uid) {
      String exportString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cirrig><cirrigList></cirrigList><database></database></cirrig>";
      String key, irrigIP;
      LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigatorsWithUid(uid);
      try {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<cirrig>");
        pw.println("<cirrigList>");
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          key = (String)i.next();
          CirrigIrrigator irr = (CirrigIrrigator)irrigList.get(key);
          pw.print("  <irrigator type=\"" + irr.getType() + "\" uid=\"" + irr.getUid() + "\" ip=\"" + irr.getHost() + "\" ");
          pw.println("noutlets=\"" + irr.getNOutlets() + "\" register=\"" + irr.getConfig().getFirstRegister()+"\" stride=\"" + irr.getConfig().getStride() + "\" ncounters=\"" + irr.getNCounters() + "\">");
          for (Iterator<ZoneGroup> zi = irr.getZoneGroups().iterator(); zi.hasNext();) {
            ZoneGroup zg = (ZoneGroup)zi.next();
            pw.println(zg.getXML()); //use getXML() method to write out xml for individual zone groups including zoneoutlets and irrigtasks
          }
          pw.println("  </irrigator>");
        }
        pw.println("</cirrigList>");

        String[] recsToSave = { "id", "name", "hour", "minute", "sleepTime", "maxIrrig", "minIrrig", "allowZero", "irrigationRate", "irrigatorLog", "HTMLLog", "defaultIrrigation", "manualDefault", "maxSimultaneous", "allowMultipleGroups", "ncycles", "cycleMode", "mode", "systemTestMinutes"};
        pw.println("<database>");
        //must add agent tag
        pw.println("  <agent name=\"" + _mainClass + "\">");
        //add in recs
        synchronized(database) {
	  for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
            key = (String)i.next();
            irrigIP = getReplyToken(key, 0, "::");
            for (Iterator idb = database.keySet().iterator(); idb.hasNext(); ) {
              key = (String)idb.next();
              if (key.startsWith(_mainClass+":"+irrigIP)) {
                for (int l = 0; l < recsToSave.length; l++) {
                  if (key.indexOf(recsToSave[l]) != -1) {
                    //write this record
                    pw.println("    <record>");
                    pw.println("      <name>"+key.substring(key.indexOf(irrigIP))+"</name>");
                    pw.println("      <value>"+database.get(key).getValue()+"</value>");
                    pw.println("    </record>");
                    break;
		  }
                }
              }
            }
          }
        }
        pw.println("  </agent>");
        pw.println("</database>");
        pw.println("</cirrig>");
        pw.close();
        exportString = stringWriter.toString();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return exportString;
    }


    protected boolean importConfig(String uidString, String importString) {
      Element root;
      try {
        //create XML Element
        root =  DocumentBuilderFactory
          .newInstance()
          .newDocumentBuilder()
          .parse(new ByteArrayInputStream(importString.getBytes()))
          .getDocumentElement();
      } catch(Exception e) {
        System.out.println(e.toString());
	e.printStackTrace();
	return false;
      }
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("irrigator");
      for (int j = 0; j < nlist.getLength(); j++) {
        int uid = -1, nOutlets = 16, pwrReg=1088, stride = 8, nCounters = 0;
        String irrigIP = "";
        String type = "";
        Node fstNode = nlist.item(j);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
          try {
            Element fstElmnt = (Element) fstNode;
            if (fstElmnt.hasAttribute("type")) {
              type = fstElmnt.getAttribute("type").trim();
            }
            if (fstElmnt.hasAttribute("uid")) {
	      if (!fstElmnt.getAttribute("uid").trim().equals(uidString)) {
		System.out.println(_mainClass+"::importConfig | " + ctime() + "> ERROR: uid in imported config file does not match user uid of "+uidString);
		return false;
	      }
              uid = Integer.parseInt(fstElmnt.getAttribute("uid").trim());
            }
            if (fstElmnt.hasAttribute("ip")) {
              irrigIP = fstElmnt.getAttribute("ip").trim();
            }
            if (fstElmnt.hasAttribute("noutlets")) nOutlets = Integer.parseInt(fstElmnt.getAttribute("noutlets"));
            if (fstElmnt.hasAttribute("register")) pwrReg = Integer.parseInt(fstElmnt.getAttribute("register"));
            if (fstElmnt.hasAttribute("stride")) stride = Integer.parseInt(fstElmnt.getAttribute("stride"));
            if (fstElmnt.hasAttribute("ncounters")) nCounters = Integer.parseInt(fstElmnt.getAttribute("ncounters"));
            CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride, nCounters);
	    if (_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
	      System.out.println(_mainClass+"::importConfig | " + ctime() + "> Updating CirrigIrrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
	    } else {
              System.out.println(_mainClass+"::importConfig | " + ctime() + "> Adding CirrigIrrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
              System.out.println("\tConfig = "+config.toString());
              addIrrigator(type, irrigIP, uid, config);
	    }
            //get irrigator here for use with ZoneGroups below
            CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
            //now look for zone groups
            NodeList grouplist = fstElmnt.getElementsByTagName("group");
            for (int i = 0; i < grouplist.getLength(); i++) {
              Node groupNode = grouplist.item(i);
              if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                Element groupElmnt = (Element)groupNode;
                if (groupElmnt.hasAttribute("name") && groupElmnt.hasAttribute("number")) {
                  String groupName = groupElmnt.getAttribute("name").trim();
                  int groupNum = Integer.parseInt(groupElmnt.getAttribute("number").trim());
                  if (irr.hasZoneGroup(groupName) && irr.hasZoneGroup(groupNum) && irr.getZoneGroup(groupName) == irr.getZoneGroup(groupNum)) {
                    System.out.println(_mainClass+"::importConfig | " + ctime() + "> Removing (to re-add) Zone Group "+groupNum+": "+groupName); 
		    irr.removeZoneGroup(groupName);
		  }
                  ZoneGroup zg = new ZoneGroup(irr, groupName, groupNum);
                  System.out.println(_mainClass+"::importConfig | " + ctime() + "> Adding ZoneGroup "+groupNum+": "+groupName);
                  zg.updateHostAndPort(ccropHost, ccropPort, weewxPort);
                  boolean success = zg.updateFromXML(groupElmnt);
                  if (success) {
                    irr.addZoneGroup(zg);
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".ncycles", UFRecord.TYPE_INT, String.valueOf(zg.getNCycles()));
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".cycleMode", UFRecord.TYPE_STRING, zg.getCycleMode());
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".mode", UFRecord.TYPE_STRING, zg.getMode());
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(zg.getMaxSimultaneous()));
                    addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".systemTestMinutes", UFRecord.TYPE_INT, String.valueOf(zg.getSystemTestMinutes()));
                    for (int l = 0; l < zg.getNCycles(); l++) {
                      int hour = zg.getHour(l);
                      if (hour != -1) addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".hour_"+l, UFRecord.TYPE_INT, String.valueOf(hour));
                      int minute = zg.getMinute(l);
                      if (minute != -1) {
                        String minuteStr = String.valueOf(minute);
                        if (minute < 10) minuteStr = "0"+minuteStr;
                        addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".minute_"+l, UFRecord.TYPE_INT, minuteStr);
                      }
                    }
                  } else return false;
                }
              }
            }
          } catch (Exception e) {
            System.out.println(_mainClass+"::importConfig | "+ctime()+"> ERROR: "+e.toString());
	    return false;
          }
        }
      }

      //now parse database records
      nlist = root.getElementsByTagName("agent");
      for (int j = 0; j < nlist.getLength(); j++) {
        Node fstNode = nlist.item(j);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
        Element fstElmnt = (Element) fstNode;
          if (fstElmnt.hasAttribute("name")) {
            if (_mainClass.toLowerCase().indexOf(fstElmnt.getAttribute("name").trim().toLowerCase()) == -1) continue;
            NodeList reclist = fstElmnt.getElementsByTagName("record");
            for (int l = 0; l < reclist.getLength(); l++) {
              try {
                Node recNode = reclist.item(l);
                if (recNode.getNodeType() == Node.ELEMENT_NODE) {
                  Element recElmnt = (Element)recNode;
                  NodeList recnameList = recElmnt.getElementsByTagName("name");
                  elem = (Element)recnameList.item(0);
                  String recName = elem.getFirstChild().getNodeValue().trim();
                  NodeList recvalList = recElmnt.getElementsByTagName("value");
                  elem = (Element)recvalList.item(0);
                  String recVal = elem.getFirstChild().getNodeValue().trim();
                  updateDatabase(_mainClass+":"+recName, recVal);
                  if (_verbose) System.out.println(_mainClass+"::importConfig> "+_mainClass+":"+recName+" = "+recVal);
                }
              } catch (Exception e) {
                System.out.println(e.toString());
              }
            }
            return true;
          }
        }
      }
      return false;
    }

//-----------------------------------------------------------------------------------------------------

    /** Helper method to connect to irrigator 
      */
    protected boolean connectToIrrigator(CirrigIrrigator irr, int maxtries) {
      int ntries = 0;
      String irrigIP = irr.getHost();
      boolean success = false;
      while (!irr.isConnected() && ntries < maxtries) {
	success = irr.connect();
	if (success) {
	  System.out.println(_mainClass+"::connectToIrrigator | " + ctime() + "> Successfully connected to irrigator "+irrigIP+" at "+ctime());
	  updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>Successfully connected to irrigator "+irrigIP+" at "+ctime()+"</html>");
	  _health = "GOOD";
	  updateDatabase(_mainClass+":health", _health, "GOOD", "Reconnected to irrigator "+irrigIP+" at "+ctime());
	} else {
	  System.out.println(_mainClass+"::connectToIrrigator | " + ctime() + "> Error!  Unable to connect to irrigator "+irrigIP);
	}
	hibernate();
	ntries++;
      }
      if (!success && ntries >= maxtries) {
        _health = "BAD";
        updateDatabase(_mainClass+":health", _health, "BAD", "Could not connect to irrigator "+irrigIP+"!");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>"+ctime()+": Could not connect to irrigator "+irrigIP+"!</html>");
        System.out.println(_mainClass+"::connectToIrrigator | " + ctime() + "> ERROR: Could not connect to irrigator "+irrigIP+"!  Failed "+maxtries+" times! "+ctime());
	return false;
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    protected boolean connectToSql() {
      boolean retVal = false;
      try {
        Class.forName(dbClass);
        // SQL statement for creating a new table
        String query = "CREATE TABLE IF NOT EXISTS irrigHistory (\n"
                + "    hid integer NOT NULL UNIQUE PRIMARY KEY,\n"
		+ "    dateTime integer NOT NULL,\n"
		+ "    irrigIP text,\n"
		+ "    zoneGroup integer,\n"
		+ "    plc text,\n"
		+ "    outlet integer,\n"
		+ "    zoneNumber integer,\n"
                + "    zoneName text,\n"
		+ "    plant text,\n"
		+ "    runTime real,\n"
                + "    water real,\n"
		+ "    units text\n"
                + ");";

	try ( Connection dbCon = DriverManager.getConnection(dbUrl);
	    Statement stmt = dbCon.createStatement(); ) {
	  //create new table if necessary
	  stmt.execute(query);
        } catch (SQLException e) {
          System.out.println(_mainClass+"::connectToSql> ERROR: "+e.getMessage());
	  return false;
        }
        // SQL statement for creating new counter table 
        query = "CREATE TABLE IF NOT EXISTS counterHistory (\n"
                + "    cid integer NOT NULL UNIQUE PRIMARY KEY,\n"
                + "    dateTime integer NOT NULL,\n"
                + "    irrigIP text,\n"
                + "    zoneGroup integer,\n"
                + "    plc text,\n"
                + "    counterNumber integer,\n"
                + "    counts integer\n"
                + ");";

        try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            Statement stmt = dbCon.createStatement(); ) {
          //create new table if necessary
          stmt.execute(query);
        } catch (SQLException e) {
          System.out.println(_mainClass+"::connectToSql> ERROR: "+e.getMessage());
          return false;
        }
        retVal = true;
      } catch(ClassNotFoundException e) {
        e.printStackTrace();
	return false;
      }
      return retVal;
    }

//-----------------------------------------------------------------------------------------------------



    /* This method is where commands are processed and sent to the device sockets */
    protected boolean action(UFMMTClientThread ct) {
      /* First increment count */
      synchronized(mutex) { count++; }
      UFStrings req = ct.getRequest();
      UFStrings reply = null;
      int nreq = 0;
      if (req == null) {
        System.err.println(_mainClass+"::action | "+ctime()+"> Received null request!");
        return false;
      }
      String clientName = req.name().substring(0, req.name().indexOf(":"));
      boolean success = false;
      updateDatabase(_mainClass+":status", "BUSY");

      nreq = req.numVals();
      System.out.println(_mainClass+"::action | "+ctime()+"> Received new request.");
      System.out.println("\tClient Thread: "+ct.getThreadName());
      System.out.println("\tRequest Name: "+req.name());
      System.out.println("\tRequest Size: "+nreq);
      for (int j = 0; j < nreq; j++) {
        System.out.println("\tRequest "+(j+1)+": "+req.stringAt(j));
      }

      success = true;
      Vector <String> responses = new Vector(nreq);
      /* First check for ABORTS! */
      if (nreq == 1) {
        int j = 0;
        if (getCmdName(req,j).equals("ABORT")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
	  String soutlet = getReplyToken(param, 2, "::");
          /* ABORT turns off output and timer! */
	  if (soutlet.equals("ALL")) {
	    success = abortAll(irrigIP, uid);
	  } else {
	    int outlet = -1;
	    try {
	      outlet = Integer.parseInt(soutlet);
	    } catch(NumberFormatException nfe) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
              reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
              success = false;
              if (ct._send(reply) <= 0) ct._terminate();
              return success;
            }
            success = abort(irrigIP, uid, outlet); 
	  }
          if (success) {
            responses.add("SUCCESS sent ABORT command!");
            reply = new UFStrings(_mainClass+": actionResponse", responses);
          } else {
            reply = new UFStrings("ERROR", "Error sending ABORT command!");
          }
          if (reply != null) {
            if (ct._send(reply) <= 0) {
              ct._terminate();
              return false;
            }
          }
          _health = "GOOD";
          updateDatabase(_mainClass+":health", _health, "GOOD", "");
          updateDatabase(_mainClass+":status", "IDLE");
          /* Return here on an abort */
          return success;
        }
      }

      /* Then check for INIT -- this resets the lock in case of error */
      if (nreq == 1) {
        int j = 0;
        if (getCmdName(req,j).equals("INIT")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          System.out.println(_mainClass+"::action | "+ctime()+"> performing INIT for CirrigIrrigator "+irrigIP);
	  if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
	    reply = new UFStrings("ERROR", "Error sending INIT command for "+irrigIP);
	    success = false;
            if (ct._send(reply) <= 0) ct._terminate(); 
	    return success;
	  }
	  CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  if (!_simMode) {
            irr.closeConnection();
            success = connectToIrrigator(irr, 3);
          }
          hibernate(2500);
          if (success) {
            responses.add("SUCCESS: Initted");
            reply = new UFStrings(_mainClass+": actionResponse", responses);
          } else {
            //If error, break out of loop
            reply = new UFStrings("ERROR", "Error Initting!");
          }
          if (ct._send(reply) <= 0) {
            ct._terminate();
            return false;
          }
          return success;
        }
      }

      if (!_initialized) {
        System.out.println(_mainClass+"::action | "+ctime()+"> Received command but not initialized!");
        reply = new UFStrings("WARNING", "Warning: Received command but not initialized yet!");
        if (ct._send(reply) <= 0) {
          ct._terminate();
          return false;
        }
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Received command but not initialized");
        updateDatabase(_mainClass+":status", "INIT");
        return false;
      }

      /* Must obtain lock first */
      _hasLock = getLock();
      if (!_hasLock) {
        System.err.println(_mainClass+"::action | "+ctime()+"> ERROR: Received request but unable to obtain lock!");
        System.err.println("\tClient Thread: "+ct.getThreadName());
        System.err.println("\tRequest Name: "+req.name());
        reply = new UFStrings("ERROR", "Unable to obtain lock");
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "WARNING", "Unable to obtain lock for request: "+req.name());
        /* If this thread somehow owns lock (dropped packet problem?) release it here */
        if (hasLock()) releaseLock();
        if (ct._send(reply) <= 0) ct._terminate();
        return false;
      }

      //if ncycles, hour, or minute is changed for a zone group, these
      //vars will be set to reflect which irrigator should be updated
      boolean updateTasks = false;
      String irrigKey = "";
      /* Do work here */
      for (int j = 0; j < nreq; j++) {
	if (getCmdName(req,j).equals("ADD_ZONE_GROUP")) {
	  //ADD_ZONE_GROUP::irrigIP::uid::groupName::groupNum
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
	  String groupName = getReplyToken(param, 2, "::");
          int groupNum = -1;
	  try {
	    groupNum = Integer.parseInt(getReplyToken(param, 3, "::"));
	  } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group number "+param);
            reply = new UFStrings("ERROR", "Invalid zone group number: "+param);
            success = false;
            break;
	  } 
	  if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
	    reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
	    success = false;
	    break;
	  }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  ZoneGroup zg = new ZoneGroup(irr, groupName, groupNum);
	  zg.updateHostAndPort(ccropHost, ccropPort, weewxPort);
	  irr.addZoneGroup(zg);
          addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".ncycles", UFRecord.TYPE_INT, "0");
          addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".cycleMode", UFRecord.TYPE_STRING, "continuous");
          addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".mode", UFRecord.TYPE_STRING, "remote");
	  addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(irr.getMaxSimultaneous()));
          addMMTRecord(_mainClass+":"+irrigIP+":"+groupName+".systemTestMinutes", UFRecord.TYPE_INT, "2"); 
          //Save defaults
          saveDefaults(irrigIP);
	  responses.add("SUCCESS added zonegroup::"+irrigIP+"::"+groupName+"::"+groupNum);
	  System.out.println(_mainClass+"::action | "+ctime()+"> Added zone group "+groupName+" to irrigator "+irrigIP);
	  success = true;
	} else if (getCmdName(req,j).equals("DELETE_ZONE_GROUP")) {
	  //DELETE_ZONE_GROUP::irrigIP::uid::groupName
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  irr.removeZoneGroup(groupName);
	  //remove records from database
          synchronized(database) {
            for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
              String key = (String)i.next();
              if (key.startsWith(_mainClass+":"+irrigIP+":"+groupName)) i.remove(); //use iterator remove 
            }
          }
          //Save defaults
          saveDefaults(irrigIP);
          responses.add("SUCCESS removed zonegroup::"+irrigIP+"::"+groupName);
          System.out.println(_mainClass+"::action | "+ctime()+"> Removed zone group "+groupName+" to irrigator "+irrigIP);
          success = true;
        } else if (getCmdName(req,j).equals("RENAME_ZONE_GROUP")) {
	  //RENAME_ZONE_GROUP::irrigIP::uid::oldname::newname
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  String newName = getReplyToken(param, 3, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  ZoneGroup zg = irr.getZoneGroup(groupName);
	  if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
	  }
	  zg.rename(newName);
          //rename records from database
          synchronized(database) {
            for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
              String key = (String)i.next();
              if (key.startsWith(_mainClass+":"+irrigIP+":"+groupName)) {
		System.out.println(key+" = "+database.get(key));
		//get record and add it with a new key
		//addMMTRecord(key.replaceAll(_mainClass+":"+irrigIP+":"+groupName, _mainClass+":"+irrigIP+":"+newName), database.get(key));
	        i.remove();//use iterator remove to remove old key 
	      }
            }

            //add database values associated with zone groups
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".ncycles", UFRecord.TYPE_INT, String.valueOf(zg.getNCycles()));
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".cycleMode", UFRecord.TYPE_STRING, zg.getCycleMode());
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".mode", UFRecord.TYPE_STRING, zg.getMode());
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".maxSimultaneous", UFRecord.TYPE_INT, String.valueOf(zg.getMaxSimultaneous()));
            addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".systemTestMinutes", UFRecord.TYPE_INT, String.valueOf(zg.getSystemTestMinutes()));
            for (int i = 0; i < zg.getNCycles(); i++) {
              int hour = zg.getHour(i);
              if (hour != -1) addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".hour_"+i, UFRecord.TYPE_INT, String.valueOf(hour));
              int minute = zg.getMinute(i);
              if (minute != -1) {
                String minuteStr = String.valueOf(minute);
                if (minute < 10) minuteStr = "0"+minuteStr;
                addMMTRecord(_mainClass+":"+irrigIP+":"+zg.getName()+".minute_"+i, UFRecord.TYPE_INT, minuteStr);
              }
            }
	  }

          //Save defaults
          saveDefaults(irrigIP);

	  responses.add("SUCCESS renamed zonegroup::"+irrigIP+"::"+groupName+"::"+newName);
          System.out.println(_mainClass+"::action | "+ctime()+">Renamed zone group "+groupName+" to "+newName);
	  //add zone group to reply
          responses.addAll(zg.getGroupInfo());
          success = true;
        } else if (getCmdName(req,j).equals("ADD_COUNTER_TO_ZONE_GROUP")) {
          //ADD_COUNTER_TO_ZONE_GROUP::irrigIP::uid::groupName::counterNum
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int counterNum = -1;
          try {
            counterNum = Integer.parseInt(getReplyToken(param, 3, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid counter number "+param);
            reply = new UFStrings("ERROR", "Invalid counter number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = zg.addCounter(counterNum);
          if (success) {
            responses.add("SUCCESS added counter::"+irrigIP+"::"+groupName+"::"+counterNum);
            System.out.println(_mainClass+"::action | "+ctime()+"> Added counter "+counterNum+" to "+groupName+" for IP "+irrigIP);
            //Save defaults
            saveDefaults(irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not add counter "+param);
            reply = new UFStrings("ERROR", "Error could not add counter "+param);
            break;
          }
        } else if (getCmdName(req,j).equals("ADD_OUTLET_TO_ZONE_GROUP")) {
	  //ADD_OUTLET_TO_ZONE_GROUP::irrigIP::uid::groupName::outletName 
	  String param = getCmdParam(req, j);
	  String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          String outletName = getReplyToken(param, 3, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  ZoneGroup zg = irr.getZoneGroup(groupName);
	  if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
	  success = zg.addOutlet(outletName);
	  if (success) {
	    int outletNum = irr.getOutletNumber(outletName);
	    //propagate defaultIrrigation from database to outlet 
            irr.propagateRecord(outletNum, "defaultIrrigation", getMMTValue(_mainClass+":"+irrigIP+"."+outletNum+".defaultIrrigation"));
	    //assing priority from outlet to database
	    updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".priority", zg.getZoneOutletByName(outletName).getPriority());
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "None");
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", "-1");
	    responses.add("SUCCESS added outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum);
            System.out.println(_mainClass+"::action | "+ctime()+"> Added outlet "+outletName+" ("+outletNum+") to "+groupName+" for IP "+irrigIP); 
            //Save defaults
            saveDefaults(irrigIP);
	  } else { 
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not add outlet "+param);
            reply = new UFStrings("ERROR", "Error could not add outlet "+param);
            break;
          }
        } else if (getCmdName(req,j).equals("REMOVE_COUNTER_FROM_ZONE_GROUP")) {
          //REMOVE_COUNTER_FROM_ZONE_GROUP::irrigIP::uid::groupName::counterNum
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int counterNum = -1;
          try {
            counterNum = Integer.parseInt(getReplyToken(param, 3, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid counter number "+param);
            reply = new UFStrings("ERROR", "Invalid counter number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = zg.removeCounter(counterNum);
          if (success) {
            responses.add("SUCCESS removed counter::"+irrigIP+"::"+groupName+"::"+counterNum);
            System.out.println(_mainClass+"::action | "+ctime()+"> Removed counter "+counterNum+" from "+groupName+" for IP "+irrigIP);
            //Save defaults
            saveDefaults(irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not remove counter "+param);
            reply = new UFStrings("ERROR", "Error could not remove counter "+param);
            break;
          }
        } else if (getCmdName(req,j).equals("REMOVE_OUTLET_FROM_ZONE_GROUP")) {
          //REMOVE_OUTLET_FROM_ZONE_GROUP::irrigIP::uid::groupName::outletName
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          String outletName = getReplyToken(param, 3, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = zg.removeOutlet(outletName);
          if (success) {
            int outletNum = irr.getOutletNumber(outletName);
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "None");
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", "-1");
            responses.add("SUCCESS removed outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum);
            System.out.println(_mainClass+"::action | "+ctime()+"> Removed outlet "+outletName+" from "+groupName+" for IP "+irrigIP);
            //Save defaults
            saveDefaults(irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not remove outlet "+param);
            reply = new UFStrings("ERROR", "Error could not remove outlet "+param);
            break;
          }
        } else if (getCmdName(req,j).equals("GET_FREE_COUNTERS")) {
          //GET_FREE_COUNTERS::irrigIP::uid
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          String freeCounters = irr.getFreeCounters();
          responses.add("SUCCESS FREE_COUNTERS::"+irrigIP+"::"+freeCounters);
          System.out.println(_mainClass+"::action | "+ctime()+"> IP "+irrigIP+" free counters = "+freeCounters);
          success = true;
	} else if (getCmdName(req,j).equals("GET_FREE_OUTLETS")) {
	  //GET_FREE_OUTLETS::irrigIP::uid
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  String freeOutlets = irr.getFreeOutlets();
	  responses.add("SUCCESS FREE_OUTLETS::"+irrigIP+"::"+freeOutlets);
	  System.out.println(_mainClass+"::action | "+ctime()+"> IP "+irrigIP+" free outlets = "+freeOutlets);
	  success = true;
        } else if (getCmdName(req,j).equals("SET_TIMER")) {
	  String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int outlet = -1;
	  float sec = 0;
	  try {
	    outlet = Integer.parseInt(getReplyToken(param, 2));
	    sec = Float.parseFloat(getReplyToken(param, 3));
	  } catch(NumberFormatException nfe) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid timer value "+param);
            reply = new UFStrings("ERROR", "Invalid timer value: "+param);
            success = false;
            break;
	  }
	  if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
	  }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  success = setTimer(sec, irr, outlet);
	  if (success) {
            responses.add("SUCCESS outlet "+outlet+" timer = "+param);
	  } else {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!  Timer not set for outlet "+outlet);
	    reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"! Timer not set for outlet "+outlet);
	    break;
	  }
	} else if (getCmdName(req,j).equals("SET_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
	  float cm = 0;
	  try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
	    cm = Float.parseFloat(getReplyToken(param, 3));
	  } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
	  }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  success = setIrrigation(cm, irr, outlet); 
          if (success) {
            responses.add("SUCCESS outlet "+outlet+" irrigation = "+param);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!  Timer not set for outlet "+outlet);
            reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"! Timer not set for outlet "+outlet);
            break;
          }
	} else if (getCmdName(req,j).equals("SET_MAX_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int outlet = -1;
          float maxIrr = 0;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
            maxIrr = Float.parseFloat(getReplyToken(param, 3));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
          }
	  updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".maxIrrig", maxIrr);
          saveDefaults(irrigIP);
	  responses.add("SUCCESS outlet "+outlet+" max irrigation = "+maxIrr);
        } else if (getCmdName(req,j).equals("SET_MIN_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int outlet = -1;
          float minIrr = 0;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
            minIrr = Float.parseFloat(getReplyToken(param, 3));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
          }
          updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".minIrrig", minIrr);
          saveDefaults(irrigIP);
          responses.add("SUCCESS outlet "+outlet+" min irrigation = "+minIrr);
        } else if (getCmdName(req,j).equals("SET_ALLOW_ZERO")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String allowZero = getReplyToken(param, 3);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet"+param);
            reply = new UFStrings("ERROR", "Invalid outlet: "+param);
            success = false;
            break;
          }
	  if (allowZero.equals("Yes") || allowZero.equals("No")) {
	    updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".allowZero", allowZero);
            saveDefaults(irrigIP);
	    responses.add("SUCCESS outlet "+outlet+" allow zero = "+allowZero);
	  } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_ALLOW_ZERO: "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_ALLOW_ZERO: "+param);
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("SET_IRRIGATION_RATE")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          float rate = 0; 
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
            rate = Float.parseFloat(getReplyToken(param, 3));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid irrigation rate "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation rate: "+param);
            success = false;
            break;
          }
          updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate", rate);
          saveDefaults(irrigIP);
          responses.add("SUCCESS outlet "+outlet+" irrigation rate = "+rate);
        } else if (getCmdName(req,j).equals("POWER_ON")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
	  CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  if (_simMode) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Simulating Power ON "+irrigIP+" outlet "+outlet);
	    updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
	  } else { 
	    success = irr.powerOn(outlet);
	    System.out.println(_mainClass+"::action | "+ctime()+"> Power ON "+irrigIP+" outlet "+outlet+" response: "+success);
	    if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
	    }
	    pollIrrigator(irrigIP, uid);
	    updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
	  }
          responses.add("SUCCESS power "+outlet+" = on");
	} else if (getCmdName(req,j).equals("POWER_OFF")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating Power OFF "+irrigIP+" outlet "+outlet);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "Off");
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
          } else {
            success = irr.powerOff(outlet);
            System.out.println(_mainClass+"::action | "+ctime()+"> Power OFF "+irrigIP+" outlet "+outlet+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
          }
	  responses.add("SUCCESS power "+outlet+" = off");
        } else if (getCmdName(req,j).equals("POWER_OFF_ALL")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating Power OFF ALL "+irrigIP);
	    for (int i = 0; i < irr.getNOutlets(); i++) {
              updateDatabase(_mainClass+":"+irrigIP+"."+i+".power", "Off");
              updateDatabase(_mainClass+":"+irrigIP+"."+i+".timerStat", "Stopped");
	    }
          } else {
            //need to get IrrigationMonitoringThread to remove outlets already in queue but not running
            String irrKey = irrigIP+"::"+uid;
            IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(irrKey);
            //remove all zones from queue
            imt.removeAllOutletsFromIrrigationQueue();
	    //now power off
            success = irr.powerOffAll();
            System.out.println(_mainClass+"::action | "+ctime()+"> Power OFF ALL "+irrigIP+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
	    for (int i = 0; i < irr.getNOutlets(); i++) updateDatabase(_mainClass+":"+irrigIP+"."+i+".timerStat", "Stopped");
          }
          responses.add("SUCCESS power ALL = off");
        } else if (getCmdName(req,j).equals("START_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating START TIMER "+irrigIP+" outlet "+outlet);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
          } else {
	    success = irr.startTimer(outlet);
            System.out.println(_mainClass+"::action | "+ctime()+"> START TIMER "+irrigIP+" outlet "+outlet+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"! At "+ctime());
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
          }
          responses.add("SUCCESS timer started outlet "+outlet);
        } else if (getCmdName(req,j).equals("START_ZONE_GROUP")) {
          //START_ZONE_GROUP::irrigIP::uid::groupName
	  //uses systemTestMinutes record for duration
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  int seconds = 0;
          try {
	    seconds = getMMTIntValue(_mainClass+":"+irrigIP+":"+groupName+".systemTestMinutes")*60;
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid minutes for system test "+param);
            reply = new UFStrings("ERROR", "Invalid minutes for system test: "+param);
            success = false;
            break;
          }
          int outlet = -1;
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = true;
          Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
            outlet = zo.getOutletNumber();
	    //check this zone outlet to see if it currently is running or is a system test
	    if (zo.running()) {
	      System.out.println(_mainClass+"::action | "+ctime()+"> Error: Outlet "+outlet+" (zone "+zo.getZoneName()+") is already running!");
	      reply = new UFStrings("ERROR", "Outlet "+outlet+" (zone "+zo.getZoneName()+") is already running!");
	      success = false;
	      break;
	    } else if (zo.isSystemTest()) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error: Outlet "+outlet+" (zone "+zo.getZoneName()+") is already set as a system test!");
              reply = new UFStrings("ERROR", "Outlet "+outlet+" (zone "+zo.getZoneName()+") is already set as a system test!");
              success = false;
              break;
	    }
            outlet = zo.getOutletNumber();
            success = setTimer(seconds, irr, outlet);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
          }
	  if (!success) break; //break here on an error in above while loop
          pollIrrigator(irrigIP, uid);
	  String irrKey = irrigIP+"::"+uid;
          IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(irrKey);
          success = imt.addZoneGroupToQueueAsSystemTest(zg);
	  if (!success) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: One or more outlets are already running, set as a system test, or in the queue..."); 
	    reply = new UFStrings("ERROR", "One or more outlets are already running, set as a system test, or in the queue...");
	    break;
	  }
          System.out.println(_mainClass+"::action | " + ctime() + "> Zone group "+zg.getName()+" is ready to irrigate for "+seconds+" seconds as a SYSTEM TEST...");
	  responses.add("SUCCESS timer set to "+seconds+" seconds for all outlets for zone group "+groupName); 
        } else if (getCmdName(req,j).equals("PAUSE_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating PAUSE TIMER "+irrigIP+" outlet "+outlet);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "Off");
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Paused");
          } else {
	    success = irr.pauseTimer(outlet);
            System.out.println(_mainClass+"::action | "+ctime()+"> PAUSE TIMER "+irrigIP+" outlet "+outlet+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Paused");
          }
          responses.add("SUCCESS timer paused outlet "+outlet);
        } else if (getCmdName(req,j).equals("PAUSE_ZONE_GROUP")) {
          //PAUSE_ZONE_GROUP::irrigIP::uid::groupName
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  int outlet = -1;
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
	  success = true;
          Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
          while (i.hasNext()) {
	    ZoneOutlet zo = (ZoneOutlet)i.next();
	    outlet = zo.getOutletNumber();
            //if not Running, skip this outlet
	    if (!getMMTValue(_mainClass+":"+irrigIP+"."+outlet+".timerStat").equals("Running")) continue;
	    if (_simMode) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Simulating PAUSE TIMER "+irrigIP+" outlet "+outlet);
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "Off");
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Paused");
	    } else {
              success = irr.pauseTimer(outlet);
              System.out.println(_mainClass+"::action | "+ctime()+"> PAUSE TIMER "+irrigIP+" outlet "+outlet+" response: "+success);
              if (!success) {
                System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
                reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
                break;
              }
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Paused");
	    }
	  }
          pollIrrigator(irrigIP, uid);
          responses.add("SUCCESS timer paused all outlets for zone group "+groupName);
        } else if (getCmdName(req,j).equals("RESUME_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating RESUME TIMER "+irrigIP+" outlet "+outlet);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
          } else {
            success = irr.resumeTimer(outlet);
            System.out.println(_mainClass+"::action | "+ctime()+"> RESUME TIMER "+irrigIP+" outlet "+outlet+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
          }
          responses.add("SUCCESS timer resumed");
        } else if (getCmdName(req,j).equals("RESUME_ZONE_GROUP")) {
          //RESUME_ZONE_GROUP::irrigIP::uid::groupName
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int outlet = -1;
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = true;
          Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
            outlet = zo.getOutletNumber();
            //if not Paused, skip this outlet
            if (!getMMTValue(_mainClass+":"+irrigIP+"."+outlet+".timerStat").equals("Paused")) continue;
            if (_simMode) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Simulating RESUME TIMER "+irrigIP+" outlet "+outlet);
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "On");
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
            } else {
              success = irr.resumeTimer(outlet);
              System.out.println(_mainClass+"::action | "+ctime()+"> RESUME TIMER "+irrigIP+" outlet "+outlet+" response: "+success);
              if (!success) {
                System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
                reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
                break;
              }
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Running");
            }
          }
          if (!success) break; //break here on an error in above while loop
          pollIrrigator(irrigIP, uid);
          responses.add("SUCCESS timer resumed for all outlets for zone group "+groupName);
        } else if (getCmdName(req,j).equals("STOP_ZONE_GROUP")) {
          //STOP_ZONE_GROUP::irrigIP::uid::groupName
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int outlet = -1;
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          success = true;
	  //need to get IrrigationMonitoringThread to remove outlets already in queue but not running
          String irrKey = irrigIP+"::"+uid;
          IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(irrKey);
          Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
            outlet = zo.getOutletNumber();
            if (_simMode) {
              System.out.println(_mainClass+"::action | "+ctime()+"> Simulating Power OFF "+irrigIP+" outlet "+outlet);
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "Off");
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
            } else {
	      //attempt to remove from queue before powering off
	      imt.removeOutletFromIrrigationQueue(zo);
              success = irr.powerOff(outlet);
              System.out.println(_mainClass+"::action | "+ctime()+"> Power OFF "+irrigIP+" outlet "+outlet+" response: "+success);
              if (!success) {
                System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!");
                reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
                break;
              }
              updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
            }
          }
          if (!success) break; //break here on an error in above while loop
          pollIrrigator(irrigIP, uid);
          responses.add("SUCCESS timer stopped for all outlets for zone group "+groupName);
	} else if (getCmdName(req,j).equals("SET_NCYCLES")) {
	  //SET_NCYCLES::irigIP::uid::groupName::ncycles
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  int ncycles = -1;
          try {
            ncycles = Integer.parseInt(getReplyToken(param, 3, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid ncycles "+param);
            reply = new UFStrings("ERROR", "Invalid ncycles: "+param);
            success = false;
            break;
          }
	  if (ncycles < 0 || ncycles > 10) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid ncycles "+ncycles);
            reply = new UFStrings("ERROR", "Invalid ncycles: "+ncycles);
            success = false;
            break;
	  }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
	  zg.setNCycles(ncycles);
	  zg.markToUpdateTasks(); //mark zone group here for tasks to be updated below
	  updateTasks = true;
	  irrigKey = irrigIP+"::"+uid;
          updateDatabase(_mainClass+":"+irrigIP+":"+groupName+".ncycles", ncycles);
          responses.add("SUCCESS ncycles = "+ncycles+" for group "+groupName+", IP "+irrigIP);
          saveDefaults(irrigIP);
          System.out.println(_mainClass+"::action | "+ctime()+"> Set ncycles = "+ncycles+" for group "+groupName+", IP "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_CYCLE_MODE")) {
          //SET_CYCLES_MODE::irigIP::uid::groupName::[continuous|daily]
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  String cycleMode = getReplyToken(param, 3, "::").toLowerCase();
          if (!cycleMode.equals("continuous") && !cycleMode.equals("daily")) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid cycle mode "+cycleMode);
            reply = new UFStrings("ERROR", "Invalid cycle mode: "+cycleMode);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          zg.setCycleMode(cycleMode);
          irrigKey = irrigIP+"::"+uid;
          updateDatabase(_mainClass+":"+irrigIP+":"+groupName+".cycleMode", cycleMode);
          saveDefaults(irrigIP);
          responses.add("SUCCESS cycle mode = "+cycleMode+" for group "+groupName+", IP "+irrigIP);
          System.out.println(_mainClass+"::action | "+ctime()+"> Set cycle mode = "+cycleMode+" for group "+groupName+", IP "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_MODE")) {
          //SET_MODE::irigIP::uid::groupName::[local|remote]
          String param = getCmdParam(req, j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          String mode = getReplyToken(param, 3, "::").toLowerCase();
          if (!mode.equals("local") && !mode.equals("remote")) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid mode "+mode);
            reply = new UFStrings("ERROR", "Invalid mode: "+mode);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          zg.setMode(mode);
          irrigKey = irrigIP+"::"+uid;
          updateDatabase(_mainClass+":"+irrigIP+":"+groupName+".mode", mode);
          saveDefaults(irrigIP);
          responses.add("SUCCESS mode = "+mode+" for group "+groupName+", IP "+irrigIP);
          System.out.println(_mainClass+"::action | "+ctime()+"> Set mode = "+mode+" for group "+groupName+", IP "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_HOUR")) {
	  //SET_HOUR::irrigIP::uid::groupName::cycleNum::hour
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
	  int cycle = -1, hour = -1;
          try {
	    cycle = Integer.parseInt(getReplyToken(param, 3, "::"));
            hour = Integer.parseInt(getReplyToken(param, 4, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid cycle/hour "+param);
            reply = new UFStrings("ERROR", "Invalid hour: "+param);
            success = false;
            break;
          }
          if (hour < 0 || hour > 23 || cycle < 0) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid cycle/hour "+cycle+"/"+hour);
            reply = new UFStrings("ERROR", "Invalid cycle/hour: "+cycle+"/"+hour);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          zg.markToUpdateTasks(); //mark zone group here for tasks to be updated below
          updateTasks = true;
          irrigKey = irrigIP+"::"+uid;
	  String key = _mainClass+":"+irrigIP+":"+groupName+".hour_"+cycle;
	  if (database.containsKey(key)) {
            updateDatabase(key, hour);
	  } else {
	    addMMTRecord(key, UFRecord.TYPE_INT, String.valueOf(hour)); 
	  }
          saveDefaults(irrigIP);
          responses.add("SUCCESS hour = "+hour+" for cycle "+cycle+" of group "+groupName+", IP "+irrigIP);
          System.out.println(_mainClass+"::action | "+ctime()+"> Set hour = "+hour+" for cycle "+cycle+" of group "+groupName+", IP "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_MINUTE")) {
          //SET_MINUTE::irrigIP::uid::groupName::cycleNum::minute
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int cycle = -1, minute = -1;
          try {
            cycle = Integer.parseInt(getReplyToken(param, 3, "::"));
            minute = Integer.parseInt(getReplyToken(param, 4, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid cycle/minute "+param);
            reply = new UFStrings("ERROR", "Invalid minute: "+param);
            success = false;
            break;
          }
          if (minute < 0 || minute > 59 || cycle < 0) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid cycle/minute "+cycle+"/"+minute);
            reply = new UFStrings("ERROR", "Invalid cycle/minute: "+cycle+"/"+minute);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          zg.markToUpdateTasks(); //mark zone group here for tasks to be updated below
          updateTasks = true;
          irrigKey = irrigIP+"::"+uid;
          String key = _mainClass+":"+irrigIP+":"+groupName+".minute_"+cycle;
	  String minuteStr = String.valueOf(minute);
          if (minute < 10) minuteStr = "0"+minuteStr;
          if (database.containsKey(key)) {
            updateDatabase(key, minuteStr);
          } else {
            addMMTRecord(key, UFRecord.TYPE_INT, String.valueOf(minuteStr));
          }
          saveDefaults(irrigIP);
          responses.add("SUCCESS minute = "+minute+" for cycle "+cycle+" of group "+groupName+", IP "+irrigIP);
          System.out.println(_mainClass+"::action | "+ctime()+"> Set minute = "+minute+" for cycle "+cycle+" of group "+groupName+", IP "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_MAX_SIMULTANEOUS")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
	  int maxs = 0;
	  try {
	    maxs = Integer.parseInt(getReplyToken(param, 2, "::"));
	  } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_MAX_SIMULTANEOUS "+param); 
            reply = new UFStrings("ERROR", "Invalid value for SET_MAX_SIMULTANEOUS: "+param); 
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  if (maxs > 0 && maxs < irr.getNOutlets()) {
	    irr.setMaxSimultaneous(maxs);
            System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" max simultaneous outlets to "+maxs); 
            updateDatabase(_mainClass+":"+irrigIP+".maxSimultaneous", maxs);
            saveDefaults(irrigIP);
            responses.add("SUCCESS irrigator "+irrigIP+" max simultaneous = "+maxs);
	  } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_MAX_SIMULTANEOUS "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_MAX_SIMULTANEOUS: "+param);
            success = false;
            break;
	  }
        } else if (getCmdName(req,j).equals("SET_MAX_SIMULTANEOUS_ZG")) {
	  //max simultaneous for this zone group
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
	  String groupName = getReplyToken(param, 2, "::");
          int maxs = 0;
          try {
            maxs = Integer.parseInt(getReplyToken(param, 3, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_MAX_SIMULTANEOUS_ZG "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_MAX_SIMULTANEOUS_ZG: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          if (maxs > 0 && maxs < irr.getNOutlets()) {
            zg.setMaxSimultaneous(maxs);
            System.out.println(_mainClass+"::action | "+ctime()+"> Updating max simultaneous outlets to "+maxs+" for group "+groupName+", irrigator "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+":"+groupName+".maxSimultaneous", maxs);
            saveDefaults(irrigIP);
            responses.add("SUCCESS max simultaneous = "+maxs+" for group "+groupName+", irrigator "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_MAX_SIMULTANEOUS_ZG "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_MAX_SIMULTANEOUS_ZG: "+param);
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("SET_SYSTEM_TEST_MINUTES")) {
          //system test minutes for this zone group
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          int stmin = 2;
          try {
            stmin = Integer.parseInt(getReplyToken(param, 3, "::"));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_SYSTEM_TEST_MINUTES "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_SYSTEM_TEST_MINUTES: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
          if (stmin > 0) {
            zg.setSystemTestMinutes(stmin);
            System.out.println(_mainClass+"::action | "+ctime()+"> Updating system test duration to "+stmin+" minutes for group "+groupName+", irrigator "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+":"+groupName+".systemTestMinutes", stmin);
            saveDefaults(irrigIP);
            responses.add("SUCCESS system test minutes = "+stmin+" for group "+groupName+", irrigator "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_SYSTEM_TEST_MINUTES "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_SYSTEM_TEST_MINUTES: "+param);
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("SET_ALLOW_MULTIPLE_GROUPS")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
	  String allowmg = getReplyToken(param, 2, "::");
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (allowmg.equals("Yes") || allowmg.equals("No")) {
	    if (allowmg.equals("Yes")) irr.setMultipleGroups(true); else irr.setMultipleGroups(false);
            updateDatabase(_mainClass+":"+irrigIP+".allowMultipleGroups", allowmg);
            saveDefaults(irrigIP);
            responses.add("SUCCESS irrigator "+irrigIP+" allow multiple groups = "+allowmg);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid value for SET_ALLOW_MULTIPLE_GROUPS: "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_ALLOW_MULTIPLE_GROUPS: "+param);
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("SET_IRRIGATOR_LOG")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String logName = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" history log to "+logName);
	  updateDatabase(_mainClass+":"+irrigIP+".irrigatorLog", logName);
          responses.add("SUCCESS irrigator "+irrigIP+" history log = "+logName);
        } else if (getCmdName(req,j).equals("SET_HTML_LOG")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String logName = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" HTML log to "+logName);
          updateDatabase(_mainClass+":"+irrigIP+".HTMLLog", logName);
          responses.add("SUCCESS irrigator "+irrigIP+" HTML log = "+logName);
        } else if (getCmdName(req,j).equals("SET_DAILY_HTML_LOG")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String logName = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" daily HTML log to "+logName);
          updateDatabase(_mainClass+":"+irrigIP+".dailyHTMLLog", logName);
          responses.add("SUCCESS irrigator "+irrigIP+" daily HTML log = "+logName);
        } else if (getCmdName(req,j).equals("SET_DEFAULT_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int outlet = -1;
	  try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
	  String defIrrig = getReplyTokens(param, 3);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" default irrigation method to: "+defIrrig);
	  if (defIrrig.toLowerCase().equals("yesterday") || defIrrig.toLowerCase().equals("3-day avg") || defIrrig.toLowerCase().equals("5-day max") || defIrrig.toLowerCase().equals("none") || defIrrig.toLowerCase().equals("manual default")) {
            updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".defaultIrrigation", defIrrig);
	    success = irr.propagateRecord(outlet, "defaultIrrigation", defIrrig);
            saveDefaults(irrigIP);
	    responses.add("SUCCESS CirrigIrrigator "+irrigIP+" outlet "+outlet+" default irrigation = "+defIrrig);
	  } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid default irrigation method "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation method: "+param);
            success = false;
            break;
	  }
        } else if (getCmdName(req,j).equals("SET_MANUAL_DEFAULT")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          String manIrrig = getReplyToken(param, 3);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" manual default irrigation to: "+manIrrig);
          updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".manualDefault", manIrrig);
	  success = irr.propagateRecord(outlet, "manualDefault", manIrrig);
          saveDefaults(irrigIP);
          responses.add("SUCCESS CirrigIrrigator "+irrigIP+" outlet "+outlet+" manual default irrigation = "+manIrrig);
        } else if (getCmdName(req,j).equals("SET_PRIORITY")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int outlet = -1;
          try {
            outlet = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid outlet number "+param);
            reply = new UFStrings("ERROR", "Invalid outlet number: "+param);
            success = false;
            break;
          }
          String priority = getReplyToken(param, 3);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" outlet "+outlet+" priority irrigation to: "+priority);
          updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".priority", priority);
          success = irr.propagateRecord(outlet, "priority", priority);
          saveDefaults(irrigIP);
          responses.add("SUCCESS CirrigIrrigator "+irrigIP+" outlet "+outlet+" priority = "+priority);
	} else if (getCmdName(req,j).equals("LOGIN")) {
          /* LOGIN::user pass */
          String param = getCmdParam(req,j);
          String user = getReplyToken(param, 0);
          String pass = getReplyToken(param, 1);
	  String uid = "-1";
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating LOGIN");
            responses.add("SIM LoggedIn -1");
	    responses.add("CirrigIrrigator::127.0.0.1");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant");
          } else {
	    Socket ccropSocket = null;
	    try {
	      System.out.println(_mainClass+"::action | "+ctime()+"> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
	      ccropSocket = new Socket(ccropHost, ccropPort);
              ccropSocket.setSoTimeout(_timeout);
              UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
              greet.sendTo(ccropSocket);
              UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
              ccropSocket.setSoTimeout(0); //infinite timeout
              if (ufpr == null) {
                System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received null object!  Closing socket!");
		reply = new UFStrings("ERROR", "Could not connect to CCROP agent!");
                ccropSocket.close();
		success = false;
		break;
              } else {
                String request = ufpr.name().toLowerCase();
                if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
                  System.out.println(_mainClass+"::action | "+ctime()+"> connection established: "+request);
                } else {
                  System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received "+request+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received invalid response "+request+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
                }
		/* First get UID */
		UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_UID::"+user+" "+pass);
                System.out.println(_mainClass+"::action | "+ctime()+"> Sending GET_UID request for user "+user);
                ccropReq.sendTo(ccropSocket);
                UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                if (getReplyToken(ccropReply, 0).equals("SUCCESS") && getReplyToken(ccropReply, 1).equals("UID")) {
		  uid = getReplyToken(ccropReply, 2);
		  responses.add("Success LoggedIn "+uid);
		  /* Add to user login map */
		  if (!userList.contains(new Integer(uid))) userList.add(new Integer(uid));
		} else {
                  System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received "+ccropReply.stringAt(0)+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(0)+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
		}
		/* Second get any CirrigIrrigators associated with this UID */
		if (_irrDatabase.hasIrrigatorWithUid(uid)) {
		  responses.addAll(_irrDatabase.getIrrigatorInfo(uid));
		}
		/* Third request Zones */
                ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONES::"+uid);
                System.out.println(_mainClass+"::action | "+ctime()+"> Sending GET_ZONES request.");
                ccropReq.sendTo(ccropSocket);
                ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                for (int i = 0; i < ccropReply.numVals(); i++) {
                  if (getReplyToken(ccropReply, 0, "::").equals("ZONE")) {
                    System.out.println(_mainClass+"::action | "+ctime()+"> Received "+ccropReply.stringAt(i));
                    responses.add(ccropReply.stringAt(i));
                  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
                     System.out.println(_mainClass+"::action | "+ctime()+"> Received "+ccropReply.stringAt(i));
                  } else {
                    System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
                  }
                }
		ccropSocket.close();
	      }
            } catch (IOException ioe) {
              System.err.println(_mainClass+"::action | "+ctime()+"> ERROR: "+ioe.toString());
              reply = new UFStrings("ERROR", "Error talking to CCROP Agent: "+ioe.toString());
              try {
		ccropSocket.close();
	      } catch (Exception e) {}
              success = false;
            }
            /* break here on error after statement and resultset are closed */
            if (success == false) break;
          }
        } else if (getCmdName(req,j).equals("REFRESH")) {
          /* REFRESH::uid */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating REFRESH");
            responses.add("SIM Refreshed -1");
            responses.add("CirrigIrrigator::127.0.0.1");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant");
          } else {
            Socket ccropSocket = null;
            try {
              System.out.println(_mainClass+"::action | "+ctime()+"> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
              ccropSocket = new Socket(ccropHost, ccropPort);
              ccropSocket.setSoTimeout(_timeout);
              UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
              greet.sendTo(ccropSocket);
              UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
              ccropSocket.setSoTimeout(0); //infinite timeout
              if (ufpr == null) {
                System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received null object!  Closing socket!");
                reply = new UFStrings("ERROR", "Could not connect to CCROP agent!");
                ccropSocket.close();
                success = false;
                break;
              } else {
                String request = ufpr.name().toLowerCase();
                if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
                  System.out.println(_mainClass+"::action | "+ctime()+"> connection established: "+request);
                } else {
                  System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received "+request+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received invalid response "+request+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
                }
		responses.add("SUCCESS Refreshed "+uid);
                /* Second get any CirrigIrrigators associated with this UID */
                if (_irrDatabase.hasIrrigatorWithUid(uid)) {
                  responses.addAll(_irrDatabase.getIrrigatorInfo(uid));
                }
                /* Third request Zones */
                UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONES::"+uid);
                System.out.println(_mainClass+"::action | "+ctime()+"> Sending GET_ZONES request.");
                ccropReq.sendTo(ccropSocket);
                UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                for (int i = 0; i < ccropReply.numVals(); i++) {
                  if (getReplyToken(ccropReply, 0, "::").equals("ZONE")) {
                    System.out.println(_mainClass+"::action | "+ctime()+"> Received "+ccropReply.stringAt(i));
                    responses.add(ccropReply.stringAt(i));
                  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
                     System.out.println(_mainClass+"::action | "+ctime()+"> Received "+ccropReply.stringAt(i));
                  } else {
                    System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
                  }
                }
                ccropSocket.close();
              }
            } catch (IOException ioe) {
              System.err.println(_mainClass+"::action | "+ctime()+"> ERROR: "+ioe.toString());
              reply = new UFStrings("ERROR", "Error talking to CCROP Agent: "+ioe.toString());
              try {
                ccropSocket.close();
              } catch (Exception e) {}
              success = false;
            }
            /* break here on error after statement and resultset are closed */
            if (success == false) break;
          }
        } else if (getCmdName(req,j).equals("LOGOUT")) {
          /* LOGOUT::uid */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
	  userList.remove(new Integer(uid));
          System.out.println(_mainClass+"::action | "+ctime()+"> Success! Logged out uid = "+uid);
          responses.add("SUCCESS LoggedOut "+uid);
        } else if (getCmdName(req, j).equals("ADD_IRRIGATOR")) {
          /* ADD_IRRIGATOR::uid ip type [noutlets pwrReg stride ncounters]*/
          String param = getCmdParam(req,j);
          int uid = -1, nOutlets = 16, pwrReg=1088, stride = 8, nCounters = 0;
	  String irrigIP = getReplyToken(param, 1);
	  String type = getReplyToken(param, 2);
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
	  try {
	    nOutlets = Integer.parseInt(getReplyToken(param, 3));
	    pwrReg = Integer.parseInt(getReplyToken(param, 4));
	    stride = Integer.parseInt(getReplyToken(param, 5));
	  } catch(Exception e) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not parse nOutlets, pwrReg, and stride.  Using default values 16, 1088, 8.");
	    nOutlets = 16;
	    pwrReg = 1088;
	    stride = 8;
	  }
	  try {
	    nCounters = Integer.parseInt(getReplyToken(param, 6));
	  } catch(Exception e) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Could not parse nCounters.  Using default value 0.");
            nCounters = 0;
	  }
	  CirrigPLCConfig config = new CirrigPLCConfig(nOutlets, pwrReg, stride, nCounters);
          System.out.println(_mainClass+"::action | "+ctime()+"> Adding CirrigIrrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
	  System.out.println("\t\tConfig = "+config.toString()); 
	  /* Add irrigator */
	  success = addIrrigator(type, irrigIP, uid, config);
          if (success) {
            /* Save defaults */
            saveDefaults(irrigIP);
            CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
            responses.add("SUCCESS Added CirrigIrrigator "+type+" "+irrigIP+" "+nOutlets+" "+irr.getAllOutletNames());
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error adding CirrigIrrigator "+irrigIP);
            reply = new UFStrings("ERROR", "Error adding CirrigIrrigator "+irrigIP);
            break;
          }
        } else if (getCmdName(req, j).equals("DELETE_IRRIGATOR")) {
          /* DELETE_IRRIGATOR:uid ip */
          String param = getCmdParam(req,j);
          int uid = -1;
          String irrigIP = getReplyToken(param, 1);
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action | "+ctime()+"> Deleting CirrigIrrigator with IP = "+irrigIP+", uid = "+uid);
          /* delete irrigator */
          success = deleteIrrigator(irrigIP, uid);
          if (success) {
	    /* Save defaults */
	    saveDefaults(irrigIP, true);
            responses.add("SUCCESS Deleted CirrigIrrigator "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error deleting CirrigIrrigator "+irrigIP);
            reply = new UFStrings("ERROR", "Error deleting CirrigIrrigator "+irrigIP);
            break;
          }
        } else if (getCmdName(req, j).equals("CHANGE_IRRIGATOR")) {
          /* CHANGE_IRRIGATOR:uid old_ip new_ip */
          String param = getCmdParam(req,j);
          int uid = -1;
          String oldIP = getReplyToken(param, 1);
          String irrigIP = getReplyToken(param, 2);
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action | "+ctime()+"> Changing Irrigator IP "+ oldIP + " to "+irrigIP+", uid = "+uid);
          /* change irrigator */
          success = changeIrrigator(oldIP, irrigIP, uid);
          if (success) {
            CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
            responses.add("SUCCESS Changed CirrigIrrigator "+oldIP+" "+irrigIP+" "+irr.getNOutlets()+" "+irr.getAllOutletNames());
	    //add in zone groups to reply
	    for (Iterator zi = irr.getZoneGroups().iterator(); zi.hasNext(); ) {
	      ZoneGroup zg = (ZoneGroup)zi.next();
	      zg.updateIrrigator(irr);
	      responses.addAll(zg.getGroupInfo());
	    }
            //Save defaults
            saveDefaults(oldIP, true);
            saveDefaults(irrigIP);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error changing CirrigIrrigator IP "+oldIP+" to "+irrigIP); 
            reply = new UFStrings("ERROR", "Error changing CirrigIrrigator IP "+oldIP+" to "+irrigIP); 
            break;
          }
        } else if (getCmdName(req,j).equals("ASSIGN_ZONE_TO_OUTLET")) {
          //ASSIGN_ZONE_TO_OUTLET::irrigIP::uid::groupName::outletName::zone_id
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0, "::");
          String uid = getReplyToken(param, 1, "::");
          String groupName = getReplyToken(param, 2, "::");
          String outletName = getReplyToken(param, 3, "::");
	  int zid = -1;
	  try {
	    zid = Integer.parseInt(getReplyToken(param, 4, "::"));
	  } catch(NumberFormatException nfe) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not parse zone id: "+param);
	    reply = new UFStrings("ERROR", "Could not parse zone id: "+param);
	    success = false;
	    break;
	  }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: Could not find irrigator "+irrigIP+" with uid "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" with uid "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          ZoneGroup zg = irr.getZoneGroup(groupName);
	  int outletNum = irr.getOutletNumber(outletName);
          if (zg == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid zone group name: "+param);
            reply = new UFStrings("ERROR", "Invalid zone group name: "+param);
            success = false;
            break;
          }
	  ZoneOutlet zo = zg.getZoneOutletByName(outletName);
	  if (zo == null) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find zone outlet "+outletName); 
            reply = new UFStrings("ERROR", "could not find zone outlet "+outletName);
            success = false;
            break;
          }
	  if (zid == -1) {
	    //Clear this zone
	    updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "None"); 
	    updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", "-1");
	    zo.setId(-1);
	    zo.setZoneName("None");
	    System.out.println(_mainClass+"::action | "+ctime()+"> Assigned None to outlet "+outletName+" (group "+groupName+") for IrrigIP "+irrigIP); 
	    responses.add("SUCCESS assigned outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum+"::-1::None");
	  } else if (zid == -2) {
	    //Fixed manual default irrigation
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "Fixed Manual Default");
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", "-2");
	    zo.setId(-2);
	    zo.setZoneName("Fixed");
            System.out.println(_mainClass+"::action | "+ctime()+"> Assigned Fixed Manual Default to outlet "+outletName+" (group "+groupName+") for IrrigIP "+irrigIP);
            responses.add("SUCCESS assigned outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum+"::-2::Fixed");
          } else if (zid == -3) {
            //Fixed manual default with rain threshold 
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "Fixed With Rain");
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", "-3");
            zo.setId(-3);
            zo.setZoneName("FixedWithRain");
            System.out.println(_mainClass+"::action | "+ctime()+"> Assigned Fixed With Rain to outlet "+outletName+" (group "+groupName+") for IrrigIP "+irrigIP);
            responses.add("SUCCESS assigned outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum+"::-3::FixedWithRain");
	  } else if (_simMode) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Simulating ASSIGN_ZONE_TO_OUTLET "+param);
	    zo.setId(zid);
	    zo.setZoneName("Sim");
            responses.add("SUCCESS assigned outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum+"::"+zid+"::Sim");
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", String.valueOf(zid));
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "Sim "+zid);
          } else {
	    zo.setId(zid);
	    zo.setZoneName("Unknown Name"); //set to "Unknown Name" until zone is downloaded
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".id", String.valueOf(zid));
            updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", "Unknown Name");
	    success = zo.updateZone(ccropHost, ccropPort);
	    if (success) {
              updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".name", zo.getZoneName());
              responses.add("SUCCESS assigned outlet::"+irrigIP+"::"+groupName+"::"+outletName+"::"+outletNum+"::"+zid+"::"+zo.getZoneName());
	    } else {
	      reply = new UFStrings("ERROR", zo.getErrMsg());
	      break;
	    }
	  }
          //Save defaults
          saveDefaults(irrigIP);
        } else if (getCmdName(req,j).equals("RESET_COUNTER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  int counter = -1;
          try {
            counter = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid counter "+param);
            reply = new UFStrings("ERROR", "Invalid counter: "+param);
            success = false;
            break;
          }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  if (counter == -1) {
	    //-1 == ALL
	    for (int i = 0; i < irr.getNCounters(); i++) {
	      success = irr.resetCounter(i);
	      if (!success) {
	         break;
	      }
	    }
	  } else success = irr.resetCounter(counter);
          if (success) {
            responses.add("SUCCESS reset counter "+counter);
          } else {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error connecting to irrigator "+irrigIP+"!  Counter "+counter+" not reset.");
            reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"! Counter "+counter+" not reset.");
            break;
          }
        } else if (getCmdName(req, j).equals("SAVE_DEFAULTS")) {
          /* SAVE_DEFAULTS::uid ip */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          String irrigIP = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
	  saveDefaults(irrigIP);
	  System.out.println(_mainClass+"::action | "+ctime()+"> Saving defaults for "+irrigIP);
          responses.add("SUCCESS saved defaults for "+irrigIP);
        } else if (getCmdName(req, j).equals("EXPORT_CONFIG")) {
	  //EXPORT_CONFIG::uid
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
	  if (!_irrDatabase.hasIrrigatorWithUid(uid)) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: could not find any irrigator for user "+uid);
            reply = new UFStrings("ERROR", "Could not find any irrigator for user "+uid);
            success = false;
            break;
          }
	  String exportString = exportConfig(uid);
	  System.out.println(_mainClass+"::action | "+ctime()+"> Exporting config for uid "+uid);
	  responses.add("SUCCESS EXPORT_CONFIG::"+exportString); 
	} else if (getCmdName(req, j).equals("IMPORT_CONFIG")) {
	  //IMPORT_CONFIG::uid::<?xml...>
          String param = getCmdParam(req,j);
	  String uid = getReplyToken(param, 0, "::");
	  String importString = getReplyToken(param, 1, "::");
	  success = importConfig(uid, importString);
	  if (!success) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Error importing config for user "+uid);
	    reply = new UFStrings("ERROR", "Error importing config for user "+uid);
	    break;
	  } 
	  System.out.println(_mainClass+"::action | "+ctime()+"> Successfully imported config for user "+uid);
          //responses.add("SUCCESS IMPORT_CONFIG::"+uid);
        } else if (getCmdName(req,j).equals("SET_SLEEP_TIME")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int sleepTime = 1;
          try {
            sleepTime = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid sleep time "+param);
            reply = new UFStrings("ERROR", "Invalid sleep time value: "+param);
            success = false;
            break;
          }
	  if (sleepTime < 1) {
            System.out.println(_mainClass+"::action | "+ctime()+"> Error: invalid sleep time "+param);
            reply = new UFStrings("ERROR", "Invalid sleep time value: "+param);
            success = false;
            break;
	  }
          System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigator "+irrigIP+" sleep time to: "+sleepTime);
          updateDatabase(_mainClass+":"+irrigIP+".sleepTime", sleepTime);
          responses.add("SUCCESS CirrigIrrigator "+irrigIP+" sleep time = "+sleepTime);
	} else {
          /* Invalid command */
          success = false;
          String cmd = getCmdName(req,j);
          reply = new UFStrings("ERROR", "Unknown command: "+cmd);
          System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: Unknown command: "+cmd);
          break;
	}
      }

      //Update zone group scheduled task time here as long as no errors
      if (success && updateTasks && _irrDatabase.hasIrrigator(irrigKey)) {
        CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigKey);
        Iterator<ZoneGroup> i = irr.getZoneGroups().iterator();
	String irrigIP = irr.getHost();
        while (i.hasNext()) {
          ZoneGroup zg = (ZoneGroup)i.next();
          if (zg.isMarked()) {
	    System.out.println(_mainClass+"::action | "+ctime()+"> Updating irrigation tasks for group "+zg.getName()+" of irrig IP "+irrigIP);
	    //loop over ncycles
	    for (int j = 0; j < zg.getNCycles(); j++) {
	      String hourKey = _mainClass+":"+irrigIP+":"+zg.getName()+".hour_"+j;
	      String minuteKey = _mainClass+":"+irrigIP+":"+zg.getName()+".minute_"+j;
	      if (!database.containsKey(hourKey) || !database.containsKey(minuteKey)) {
                System.out.println(_mainClass+"::action | "+ctime()+"> ERROR: could not find hour and minute for cycle "+j+" of group "+zg.getName()+", IP "+irrigIP); 
                reply = new UFStrings("ERROR", "could not find hour and minute for cycle "+j+" of group "+zg.getName()+", IP "+irrigIP);
		success = false;
	        break;
	      }
	      int hour = getMMTIntValue(hourKey);
	      int minute = getMMTIntValue(minuteKey);
	      boolean isUpdated = zg.checkAndUpdateTask(j, hour, minute);
	      if (isUpdated) System.out.println(_mainClass+"::action | "+ctime()+"> Updated cycle "+j+" to run at "+hour+":"+minute);
	    }
	    zg.clearMark();
            //Save defaults
            saveDefaults(irrigIP);
          }
	  if (!success) break; //break out of loop on error
        }
      }

      /* Release lock */
      if (releaseLock()) {
        _hasLock = false;
      } else {
        System.err.println(_mainClass+"::action | "+ctime()+"> ERROR: Unable to release lock!  This should not happen!");
        System.err.println("\tClient Thread: "+ct.getThreadName());
        System.err.println("\tRequest Name: "+req.name());
        reply = new UFStrings("ERROR", "Unable to release lock");
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "WARNING", "Unable to release lock for request: "+req.name());
        if (ct._send(reply) <= 0) ct._terminate();
        return false;
      }

      if (success) {
        reply = new UFStrings(_mainClass+": actionResponse", responses);
        _health = "GOOD";
        updateDatabase(_mainClass+":health", _health, "GOOD", "");
        updateDatabase(_mainClass+":status", "IDLE");
      } else {
        _health = "BAD";
        String msg = "";
        if (reply != null) msg = reply.stringAt(0);
        updateDatabase(_mainClass+":health", _health, "BAD", msg);
        updateDatabase(_mainClass+":status", "ERROR");
      }

      if (reply != null) {
        if (ct._send(reply) <= 0) {
          ct._terminate();
          return false;
        }
      }
      return success;
    }

//-----------------------------------------------------------------------------------------------------

    /* No executive agent.  Override methods to return true. */

    protected synchronized boolean getLock() {
        return true;
    }

    protected synchronized boolean hasLock() {
        return true;
    }

    protected synchronized boolean releaseLock() {
        return true;
    }


//-----------------------------------------------------------------------------------------------------

    protected boolean setIrrigation(float cm, CirrigIrrigator irr, int outlet) { 
      String irrigIP = irr.getHost();
      float maxIrr = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".maxIrrig");
      float minIrr = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".minIrrig");
      String allowZero = getMMTValue(_mainClass+":"+irrigIP+"."+outlet+".allowZero");
      if (cm > maxIrr) {
	System.out.println(_mainClass+":"+irrigIP+".:setIrrigation | " + ctime() + "> WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!");
	System.out.println("\t\tUsing maximum irrigation of "+maxIrr+" cm.");
	cm = maxIrr;
	updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!</html>");
      } else if (cm > 0 && cm < minIrr && allowZero.equals("Yes")) {
        System.out.println(_mainClass+":"+irrigIP+".:setIrrigation | " + ctime() + "> WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!");
        System.out.println("\t\tUsing nonzero minimum irrigation of "+minIrr+" cm.");
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!</html>");
        cm = minIrr;
      } else if (cm < minIrr && allowZero.equals("No")) {
        System.out.println(_mainClass+":"+irrigIP+".:setIrrigation | " + ctime() + "> WARNING: Requested irrigation of "+cm+" cm is below MINIMUM!");
        System.out.println("\t\tUsing minimum irrigation of "+minIrr+" cm.");
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm is below MINIMUM!</html>");
        cm = minIrr;
      }
      float rate = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate");
      float sec = cm*3600/rate;
      System.out.println(_mainClass+":"+irrigIP+".:setIrrigation | " + ctime() + "> Setting irrigation outlet "+outlet+" to (name="+irr.getOutletName(outlet)+", zone="+irr.getZoneNameByOutletNumber(outlet)+") "+cm+" cm; timer = "+sec+" sec for IP "+irrigIP);
      updateIrrigatorLog("Setting irrigation outlet "+outlet+" (name="+irr.getOutletName(outlet)+", zone="+irr.getZoneNameByOutletNumber(outlet)+") to "+cm+" cm; timer = "+sec+" sec", irrigIP);
      //updateHTMLLog(irr, irrigIP, outlet, (int)sec, cm);
      if (_simMode) {
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timer", sec);
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".irrigation", cm);
        return true;
      }
      boolean success = irr.setTimer(outlet, sec);
      return success;
    }

    protected boolean setTimer(float sec, CirrigIrrigator irr, int outlet) {
      String irrigIP = irr.getHost();
      float rate = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate");
      float cm = (sec/3600)*rate;
      float maxIrr = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".maxIrrig");
      float minIrr = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".minIrrig");
      if (cm > maxIrr) {
        System.out.println(_mainClass+":"+irrigIP+".:setTimer | " + ctime() + "> WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!");
        System.out.println("\t\tUsing maximum irrigation of "+maxIrr+" cm.");
        cm = maxIrr;
	sec = cm*3600/rate;
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm exceeds MAXIMUM!</html>");
      } else if (cm > 0 && cm < minIrr) {
        System.out.println(_mainClass+":"+irrigIP+".:setTimer | " + ctime() + "> WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!");
        System.out.println("\t\tUsing nonzero minimum irrigation of "+minIrr+" cm.");
        cm = minIrr;
	sec = cm*3600/rate;
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm is below nonzero MINIMUM!</html>");
      }
      System.out.println(_mainClass+":"+irrigIP+".:setTimer | " + ctime() + "> Setting irrigation outlet "+outlet+" (name="+irr.getOutletName(outlet)+", zone="+irr.getZoneNameByOutletNumber(outlet)+") to "+cm+" cm; timer = "+sec+" sec for IP "+irrigIP);
      updateIrrigatorLog("Setting irrigation outlet "+outlet+" (name="+irr.getOutletName(outlet)+", zone="+irr.getZoneNameByOutletNumber(outlet)+") to "+cm+" cm; timer = "+sec+" sec", irrigIP);
      //updateHTMLLog(irr, irrigIP, outlet, (int)sec, cm);
      if (_simMode) {
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timer", sec);
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".irrigation", cm);
	return true;
      }
      boolean success = irr.setTimer(outlet, sec);
      return success;
    }

//-----------------------------------------------------------------------------------------------------

    protected boolean abort(String irrigIP, String uid, int outlet) {
      boolean success = true;
      if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
        System.out.println(_mainClass+"::abort | " + ctime() + "> Error: could not find irrigator "+irrigIP+" for user "+uid);
        return false;
      }
      CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
      if (_simMode) {
	System.out.println(_mainClass+"::abort | " + ctime() + "> Simulating ABORT");
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".power", "Off");
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
      } else {
	success = irr.powerOff(outlet);
	System.out.println(_mainClass+"::abort | " + ctime() + "> Abort response: "+success);
        if (!success) {
          System.out.println(_mainClass+"::abort | " + ctime() + "> Error connecting to irrigator "+irrigIP+"!");
        }
        pollIrrigator(irrigIP, uid);
        updateDatabase(_mainClass+":"+irrigIP+"."+outlet+".timerStat", "Stopped");
      }
      return success;
    }

    protected boolean abortAll(String irrigIP, String uid) {
      boolean success = true;
      if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
        System.out.println(_mainClass+"::abort | " + ctime() + "> Error: could not find irrigator "+irrigIP+" for user "+uid);
        return false;
      }
      CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
      if (_simMode) {
        System.out.println(_mainClass+"::abort | " + ctime() + "> Simulating ABORT ALL");
	for (int j = 0; j < irr.getNOutlets(); j++) {
          updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "Off");
          updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStat", "Stopped");
        }
      } else {
        //need to get IrrigationMonitoringThread to remove outlets already in queue but not running
        String irrKey = irrigIP+"::"+uid;
        IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(irrKey);
	//remove all zones from queue
	imt.removeAllOutletsFromIrrigationQueue();
        success = irr.powerOffAll();
        System.out.println(_mainClass+"::abort | " + ctime() + "> Abort response: "+success);
        if (!success) {
          System.out.println(_mainClass+"::abort | " + ctime() + "> Error connecting to irrigator "+irrigIP+"!");
        }
        pollIrrigator(irrigIP, uid);
	for (int j = 0; j < irr.getNOutlets(); j++) {
          updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStat", "Stopped");
        }
      }
      return success;
    }

    protected void checkIrrigPollingThreads() {
      synchronized(irrigPollingThreadDb) {
        LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigators();
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          CirrigIrrigator irr = (CirrigIrrigator)irrigList.get(key);
          String irrigIP = getReplyToken(key, 0, "::");
          String uid = getReplyToken(key, 1, "::");
	  if (irrigPollingThreadDb.containsKey(key)) {
	    IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
	    //Check if irrigation polling thread is alive.  Also check isConnecting because
	    //if PLC is down, it can hang here until it comes back up.  Don't want to start multiple threads here.
	    if (System.currentTimeMillis()/1000 - ipt.heart() > 60 && !ipt.isConnecting()) {
	      System.out.println(_mainClass+"::checkIrrigPollingThreads | "+ctime()+"> Killing thread "+ipt.getName()+" that has not updated heartbeat in 60 seconds.");
	      ipt.shutdown();
	      irrigPollingThreadDb.remove(key);
	      addNewIrrigPollingThread(irr, irrigIP, uid);
	    } else if (System.currentTimeMillis()/1000 - ipt.heart() > 30 && !ipt.isConnecting()) {
              System.out.println(_mainClass+"::checkIrrigPollingThreads | "+ctime()+"> reconnecting to irrigator "+irrigIP+" that has not updated heartbeat in 30 seconds.");
              boolean success = ipt.reconnect();
	      if (success) {
		System.out.println(_mainClass+"::checkIrrigPollingThreads | "+ctime()+"> successfully reconnected to "+irrigIP);
	      } else {
		System.out.println(_mainClass+"::checkIrrigPollingThreads | " + ctime() + "> Error: unable to reconnect to "+irrigIP+" at "+ctime());
	      }
	    }
	  } else {
	    addNewIrrigPollingThread(irr, irrigIP, uid);
	  }
	}
      }
    }

//============= methods to check on threads and start new ones, called from ancillary ============//

    protected void checkIrrigationMonitoringThreads() {
      synchronized(irrigationMonitoringThreadDb) {
        LinkedHashMap <String, CirrigIrrigator> irrigList = _irrDatabase.getIrrigators();
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          CirrigIrrigator irr = (CirrigIrrigator)irrigList.get(key);
          String irrigIP = getReplyToken(key, 0, "::");
          String uid = getReplyToken(key, 1, "::");
          if (irrigationMonitoringThreadDb.containsKey(key)) {
            IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(key);
            //Check if irrigation monitoring thread is alive.  Also check isConnecting because
            //if PLC is down, it can hang here until it comes back up.  Don't want to start multiple threads here.
            if (System.currentTimeMillis()/1000 - imt.heart() > 60 && !imt.isConnecting()) {
              System.out.println(_mainClass+"::checkIrrigationMonitoringThreads | "+ctime()+"> Killing thread "+imt.getName()+" that has not updated heartbeat in 60 seconds.");
              imt.shutdown();
              irrigationMonitoringThreadDb.remove(key);
              addNewIrrigationMonitoringThread(irr, irrigIP, uid);
            }
          } else {
	    addNewIrrigationMonitoringThread(irr, irrigIP, uid);
          }
        }
      }
    }

    protected void addNewIrrigPollingThread(CirrigIrrigator irr, String irrigIP, String uid) {
      IrrigPollingThread pollingThread = new IrrigPollingThread(irr, irrigIP, uid);
      irrigPollingThreadDb.put(irrigIP+"::"+uid, pollingThread);
      pollingThread.start();
    }

    protected void addNewIrrigationMonitoringThread(CirrigIrrigator irr, String irrigIP, String uid) {
      IrrigationMonitoringThread imt = new IrrigationMonitoringThread(irr, irrigIP, uid);
      irrigationMonitoringThreadDb.put(irrigIP+"::"+uid, imt);
      imt.start();
    }

//=================================================================================================================//

    protected boolean pollIrrigator(String irrigIP, String uid) {
      if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
        System.out.println(_mainClass+"::pollIrrigator | "+ctime()+"> Error: could not find irrigator "+irrigIP+" for user "+uid);
        return false;
      }
      CirrigIrrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
      if (_simMode) {
	/* Simulated */
        updateDatabase(_mainClass+":"+irrigIP+".status", "Simulated");//status of irrigator overall
	for (int j = 0; j < irr.getNOutlets(); j++) {
          long t = (long)(System.currentTimeMillis()/1000);
          if (t%2 != 0) {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "On");
          } else {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "Off");
          }
	}
        for (int j = 0; j < irr.getNCounters(); j++) {
          long t = (long)(System.currentTimeMillis()/1000);
          if (t%2 != 0) {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".count", 0);
          } else {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".count", 1);
          }
        }
        return true;
      } else if (!irr.isConnected()) {
	/* Disconnected */
        updateDatabase(_mainClass+":"+irrigIP+".status", "Disconnected");//status of irrigator overall
        long t = (long)(System.currentTimeMillis()/1000);
	for (int j = 0; j < irr.getNOutlets(); j++) {
          if (t%2 != 0) {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "Irrigator");
          } else {
            updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "Disconnected");
          }
	}
	return false;
      }

      float sec, rate, cm, timerVal;
      int timerStatus;
      String timerStat;
      synchronized(irr) {
        try {
	  boolean err = irr.getErrorStatus();
	  if (err) {
	    //error already exists
            handleIrrigatorError(irr, irrigIP, irr.getErrorMessage());
	    return false;
	  }
	  //connected
	  updateDatabase(_mainClass+":"+irrigIP+".status", "Connected");//status of irrigator overall

	  //read power status
	  boolean[] powerStat = irr.getPowerStatus();
	  //Check for an error
	  if (irr.getErrorStatus()) {
	    //error
            handleIrrigatorError(irr, irrigIP, "Could not read power status!");
            return false;
	  }
	  for (int j = 0; j < irr.getNOutlets(); j++) {
	    String irrString = "outlet "+j+" (name="+irr.getOutletName(j)+", zone="+irr.getZoneNameByOutletNumber(j)+")";
	    if (powerStat[j]) {
	      if (!getMMTValue(_mainClass+":"+irrigIP+"."+j+".power").toLowerCase().equals("on")) {
	        //updateIrrigatorLog("Irrigation outlet "+j+" turned ON", irrigIP);
		updateIrrigatorLog("Irrigation "+irrString+" turned ON", irrigIP);
	        updateDatabase(_mainClass+":"+irrigIP+"."+j+".lastOn", (int)(System.currentTimeMillis()/1000));
	      }
              updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "On");
	    } else {
	      if (getMMTValue(_mainClass+":"+irrigIP+"."+j+".power").toLowerCase().equals("on")) {
                updateIrrigatorLog("Irrigation "+irrString+" turned OFF", irrigIP);
	        //updateIrrigatorLog("Irrigation outlet "+j+" turned OFF", irrigIP);
	        int runTime = (int)(System.currentTimeMillis()/1000) - getMMTIntValue(_mainClass+":"+irrigIP+"."+j+".lastOn"); 
		try {
                  //updateIrrigatorLog("Total irrigation time outlet "+j+" = "+(runTime/60)+" minutes and "+(runTime%60)+" seconds.", irrigIP);
		  updateIrrigatorLog("Total irrigation time "+irrString+" = "+(runTime/60)+" minutes and "+(runTime%60)+" seconds.", irrigIP);
	          updateHTMLLog(irr, irrigIP, j, runTime);
		} catch (Exception e) { 
		  System.out.println(_mainClass+"::pollIrrigator | "+ctime()+"> Error writing log: "+e.toString());
		}
                try {
                  updateIrrigHistory(irr, irrigIP, j, runTime);
                } catch (Exception e) {
                  System.out.println(_mainClass+"::pollIrrigator | "+ctime()+"> Error writing database: "+e.toString());
                }
	      }
              updateDatabase(_mainClass+":"+irrigIP+"."+j+".power", "Off");
	    }
	  }

	  for (int j = 0; j < irr.getNOutlets(); j++) {
	    //read timer
	    sec = irr.getTimer(j);
	    if (sec == -1) {
	      handleIrrigatorError(irr, irrigIP, "Could not read timer for outlet "+j+" (name="+irr.getOutletName(j)+", zone="+irr.getZoneNameByOutletNumber(j)+")!");
	      //error
              return false;
	    }
	    rate = getMMTFloatValue(_mainClass+":"+irrigIP+"."+j+".irrigationRate");
            cm = (sec/3600)*rate;
	    if (sec != getMMTFloatValue(_mainClass+":"+irrigIP+"."+j+".timer")) {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timer", sec);
	    }
	    if (cm != getMMTFloatValue(_mainClass+":"+irrigIP+"."+j+".irrigation")) {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".irrigation", cm);
	    }

	    //read timer status
	    timerStatus = irr.getTimerStatus(j);
	    if (timerStatus == 1) {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStat", "Stopped");
	    } else if (timerStatus == -1) {
              handleIrrigatorError(irr, irrigIP, "Could not read timer status for outlet "+j+" (name="+irr.getOutletName(j)+", zone="+irr.getZoneNameByOutletNumber(j)+")!");
              //error
              return false;
	    }

	    //read timer value
	    timerVal = irr.getTimerValue(j);
	    if (timerVal == -1) {
              handleIrrigatorError(irr, irrigIP, "Could not read timer value for outlet "+j+" (name="+irr.getOutletName(j)+", zone="+irr.getZoneNameByOutletNumber(j)+")!");
              //error
              return false;
            }

	    timerStat = getMMTValue(_mainClass+":"+irrigIP+"."+j+".timerStat");
	    if (timerStat.equals("Stopped")) {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStatusLabel", "Timer Off");
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerValue", 0);
	    } else if (timerStat.equals("Set")) {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStatusLabel", "Timer is set");
	    } else {
	      updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerStatusLabel", "Timer value:");
              updateDatabase(_mainClass+":"+irrigIP+"."+j+".timerValue", timerVal);
	    }

            err = irr.getErrorStatus();
            if (err) {
              handleIrrigatorError(irr, irrigIP, irr.getErrorMessage()); 
              //error
              return false;
            }
	  }
	  for (int j = 0; j < irr.getNCounters(); j++) {
	    //read counters
	    int cnt = irr.getCounterValue(j);
	    if (cnt == -1) {
              handleIrrigatorError(irr, irrigIP, "Could not read counter "+j+"!");
              //error
              return false;
	    }
	    updateDatabase(_mainClass+":"+irrigIP+"."+j+".count", cnt);
	  }
        } catch(Exception ex) {
	  System.out.println(_mainClass+":pollIrrigator | "+ctime()+"> ERROR: "+ex.toString());
	  ex.printStackTrace();
	  handleIrrigatorError(irr, irrigIP, "while polling irrigator");
	  return false;
        }
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    protected void updateIrrigatorLog(String message, String irrigIP) {
      //open log for append
      String logfile = _logdir+"/"+getMMTValue(_mainClass+":"+irrigIP+".irrigatorLog");
      try {
        PrintWriter pw = new PrintWriter(new FileOutputStream(logfile, true));
        pw.println(ctime()+": "+message);
        pw.close();
	System.out.println(_mainClass+"::updateIrrigatorLog | "+ctime()+"> Updating log for "+irrigIP+": "+ message + " at "+ctime());
      } catch(IOException e) {
	e.printStackTrace();
	System.out.println(_mainClass+"::updateIrrigatorLog | "+ctime()+"> WARNING: Could not open irrigator log file "+logfile);
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not open irrigator log file "+logfile);
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not open irrigator log file "+logfile+"</html>");
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized void updateHTMLLog(CirrigIrrigator irr, String irrigIP, int outlet, int runTime) {
      //open log for append
      String logfile = _logdir+"/"+getMMTValue(_mainClass+":"+irrigIP+".HTMLLog");
      File f = new File(logfile);
      ArrayList<String> html = new ArrayList();
      String line = "";
      boolean isNullZone = false;
      boolean isFixedZone = false;
      int zid = getMMTIntValue(_mainClass+":"+irrigIP+"."+outlet+".id");
      if (irr.getZoneGroupByOutlet(outlet) == null) isNullZone = true; else {
	if (zid == -2 || zid == -3) isFixedZone = true;
	if (irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone() == null) isFixedZone = true;
      }
      try {
        if (f.exists()) {
	  //read existing HTML
	  BufferedReader br = new BufferedReader(new FileReader(logfile));
	  while ((line = br.readLine()) != null) {
	    html.add(line);
	  }
	  br.close();
        } if (html.size() == 0) { 
	  html.add("<html><table border=1><tr><th>Day</th><th>Date</th><th>Group</th><th>Time Finished</th><th>PLC</th><th>Outlet</th><th>Zone</th><th>Name</th><th>Plant</th><th>RT (min)</th><th>Water</th></tr>");
	  html.add("</table></html>");
        } 
	
	//construct current line before printing in case of error
        String date = new java.util.Date( System.currentTimeMillis() ).toString();
	line = "<tr><td>"+date.substring(0,3)+"</td><td>"+date.substring(4,10)+"</td>";
	String runTimeStr = String.valueOf(runTime/60.0f);
        if (runTimeStr.indexOf(".") > 0 && runTimeStr.length() > runTimeStr.indexOf(".")+2) {
	  runTimeStr = runTimeStr.substring(0, runTimeStr.indexOf(".")+3);
	}
	if (isNullZone) {
          line += "<td>--</td><td>"+date.substring(11,19)+"</td>";
          line += "<td>"+irr.getOutletName(outlet)+"</td><td>"+outlet+"</td>";
          line += "<td>--</td><td>--</td>";
          line += "<td>--</td>";
          //line += "<td>"+(runTime/60)+"m "+(runTime%60)+"s</td>";
          line += "<td>"+runTimeStr+"</td>";
          line += "<td>--</td></tr>";
        } else if (isFixedZone) {
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getNumber()+"</td><td>"+date.substring(11,19)+"</td>";
          line += "<td>"+irr.getOutletName(outlet)+"</td><td>"+outlet+"</td>";
	  if (zid == -2) line += "<td>--</td><td>Fixed</td>"; else if (zid == -3) line += "<td>--</td><td>FixedWithRain</td>"; else line += "<td>--</td><td>Manual</td>";
          line += "<td>--</td>";
          //line += "<td>"+(runTime/60)+"m "+(runTime%60)+"s</td>";
	  line += "<td>"+runTimeStr+"</td>";
          float irrigRateCm = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate");
          float inches = irrigRateCm/2.54f*runTime/3600.0f;
	  String inchstr = String.valueOf(inches);
	  if (inchstr.length() > 5) {
            if (inchstr.indexOf("E-") == -1) {
	      inchstr = inchstr.substring(0,5)+" in";
	    } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" in";
          } else inchstr = inchstr + " in";
          line += "<td>"+inchstr+"</td></tr>";
          //update run time for daily total
          irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).updateDailyTotals(runTime);
	} else {
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getNumber()+"</td><td>"+date.substring(11,19)+"</td>";
          line += "<td>"+irr.getOutletName(outlet)+"</td><td>"+outlet+"</td>";
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getZoneNumber()+"</td><td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getName()+"</td>";
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getPlant()+"</td>";
          //line += "<td>"+(runTime/60)+"m "+(runTime%60)+"s</td>";
          line += "<td>"+runTimeStr+"</td>";
	  //update run time for daily total
	  irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).updateDailyTotals(runTime);
          Zone z = irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone();
          double inches = z.getIrrigRate()*runTime/3600.0; 
	  System.out.println("Runtime "+runTime+" "+z.getIrrigRate()+" "+inches);
          String inchstr = String.valueOf(inches);
          if (inchstr.length() > 5) {
            if (inchstr.indexOf("E-") == -1) {
              inchstr = inchstr.substring(0,5)+" in";
            } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" in";
          } else inchstr = inchstr + " in";
          if (z.getZoneType().equals("ET-micro") || z.getZoneType().equals("LF-micro")) {
	    double gal = z.getIrrigRateGalHr()*runTime/3600.0;
	    inchstr = String.valueOf(gal);
            if (inchstr.length() > 5) {
              if (inchstr.indexOf("E-") == -1) {
                inchstr = inchstr.substring(0,5)+" gal";
              } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" gal";
            } else inchstr = inchstr + " gal";
          }
          line += "<td>"+inchstr+"</td></tr>";
	}
        PrintWriter pw = new PrintWriter(new FileOutputStream(logfile));
        //print header
        pw.println(html.remove(0));
	pw.println(line);
	//print remaining lines and footer
	while (!html.isEmpty()) pw.println(html.remove(0));
	pw.close();
        System.out.println(_mainClass+"::updateHTMLLog | "+ctime()+"> Updating HTML log for "+irrigIP+": "+ line + " at "+ctime());
      } catch(IOException e) {
        e.printStackTrace();
        System.out.println(_mainClass+"::updateHTMLLog | "+ctime()+"> WARNING: Could not open irrigator HTML log file "+logfile);
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not open irrigator HTML log file "+logfile);
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not open irrigator HTML log file "+logfile+"</html>");
      }

      if (isNullZone) return; // no need to update daily log if a manual on/off
      //also update daily HTML log here if applicable
      ZoneOutlet zo = irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet);
      if (!zo.hasFinishedLastCycle()) return; 
      int totRunTime = zo.getTotalDailyIrrig();
      //open log for append
      logfile = _logdir+"/"+getMMTValue(_mainClass+":"+irrigIP+".dailyHTMLLog");
      f = new File(logfile);
      html = new ArrayList();
      line = "";
      try {
        if (f.exists()) {
          //read existing HTML
          BufferedReader br = new BufferedReader(new FileReader(logfile));
          while ((line = br.readLine()) != null) {
            html.add(line);
          }
          br.close();
        } if (html.size() == 0) {
          html.add("<html><table border=1><tr><th>Day</th><th>Date</th><th>Group</th><th>Zone</th><th>Name</th><th>Plant</th><th>Tot RT (min)</th><th>Water</th><th>PLC</th><th>Outlet</th></tr>");
          html.add("</table></html>");
        }

        //construct current line before printing in case of error
        String date = new java.util.Date( System.currentTimeMillis() ).toString();
        line = "<tr><td>"+date.substring(0,3)+"</td><td>"+date.substring(4,10)+"</td>";
        line += "<td>"+irr.getZoneGroupByOutlet(outlet).getNumber()+"</td>";
        String totRunTimeStr = String.valueOf(totRunTime/60.0f);
        if (totRunTimeStr.indexOf(".") > 0 && totRunTimeStr.length() > totRunTimeStr.indexOf(".")+2) {
          totRunTimeStr = totRunTimeStr.substring(0, totRunTimeStr.indexOf(".")+3);
        }
        if (isFixedZone) {
          line += "<td>--</td><td>Fixed</td>";
          line += "<td>--</td>";
          //line += "<td>"+(totRunTime/60)+"m "+(totRunTime%60)+"s</td>";
          line += "<td>"+totRunTimeStr+"</td>";
          float irrigRateCm = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate");
          float inches = irrigRateCm/2.54f*totRunTime/3600.0f; // use totRunTime
          String inchstr = String.valueOf(inches);
          if (inchstr.length() > 5) {
            if (inchstr.indexOf("E-") == -1) {
              inchstr = inchstr.substring(0,5)+" in";
            } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" in";
          } else inchstr = inchstr + " in";
          line += "<td>"+inchstr+"</td>";
          line += "<td>"+irr.getOutletName(outlet)+"</td><td>"+outlet+"</td>";
          line += "</tr>";
        } else {
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getZoneNumber()+"</td><td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getName()+"</td>";
          line += "<td>"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getPlant()+"</td>";
          //line += "<td>"+(totRunTime/60)+"m "+(totRunTime%60)+"s</td>";
          line += "<td>"+totRunTimeStr+"</td>";
          Zone z = irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone();
          double inches = z.getIrrigRate()*totRunTime/3600.0;
          System.out.println("Total daily runtime "+totRunTime+" "+z.getIrrigRate()+" "+inches);
          String inchstr = String.valueOf(inches);
          if (inchstr.length() > 5) {
            if (inchstr.indexOf("E-") == -1) {
              inchstr = inchstr.substring(0,5)+" in";
            } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" in";
          } else inchstr = inchstr + " in";
          if (z.getZoneType().equals("ET-micro") || z.getZoneType().equals("LF-micro")) {
            double gal = z.getIrrigRateGalHr()*totRunTime/3600.0;
            inchstr = String.valueOf(gal);
            if (inchstr.length() > 5) {
              if (inchstr.indexOf("E-") == -1) {
                inchstr = inchstr.substring(0,5)+" gal";
              } else inchstr = inchstr.substring(0,4)+inchstr.substring(inchstr.indexOf("E-"))+" gal";
            } else inchstr = inchstr + " gal";
          }
          line += "<td>"+inchstr+"</td>";
          line += "<td>"+irr.getOutletName(outlet)+"</td><td>"+outlet+"</td>";
	  line += "</tr>";
	}
        PrintWriter pw = new PrintWriter(new FileOutputStream(logfile));
        //print header
	html.remove(0);
	pw.println("<html><table border=1><tr><th>Day</th><th>Date</th><th>Group</th><th>Zone</th><th>Name</th><th>Plant</th><th>Tot RT (min)</th><th>Water</th><th>PLC</th><th>Outlet</th></tr>");
        //pw.println(html.remove(0));
        pw.println(line);
        //print remaining lines and footer
        while (!html.isEmpty()) pw.println(html.remove(0));
        pw.close();
        System.out.println(_mainClass+"::updateHTMLLog | "+ctime()+"> Updating daily HTML log for "+irrigIP+": "+ line + " at "+ctime());
      } catch(IOException e) {
        e.printStackTrace();
        System.out.println(_mainClass+"::updateHTMLLog | "+ctime()+"> WARNING: Could not open irrigator daily HTML log file "+logfile);
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not open irrigator daily HTML log file "+logfile);
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not open irrigator daily HTML log file "+logfile+"</html>");
      } 
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized void updateIrrigHistory(CirrigIrrigator irr, String irrigIP, int outlet, int runTime) {
      String query = "INSERT INTO irrigHistory(";
      String fields = "dateTime, irrigIP";
      String values = " VALUES("+String.valueOf(System.currentTimeMillis()/1000)+", '"+irrigIP+"'";

      boolean isNullZone = false;
      boolean isFixedZone = false;
      int zid = getMMTIntValue(_mainClass+":"+irrigIP+"."+outlet+".id");
      if (irr.getZoneGroupByOutlet(outlet) == null) isNullZone = true; else {
        if (zid == -2 || zid == -3) isFixedZone = true;
        if (irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone() == null) isFixedZone = true;
      }
      try {
        String runTimeStr = String.valueOf(runTime); //store as seconds
	if (isNullZone) {
	  fields += ", plc, outlet, runTime)";
	  values += ", '"+irr.getOutletName(outlet)+"', "+outlet+", "+runTimeStr+")";
        } else if (isFixedZone) {
	  fields += ", zoneGroup, plc, outlet, zoneName, runTime, water, units)";
	  values += ", "+irr.getZoneGroupByOutlet(outlet).getNumber()+", '"+irr.getOutletName(outlet)+"', "+outlet+", ";
	  if (zid == -2) values += "'Fixed', "; else if (zid == -3) values += "'FixedWithRain', "; else values += "'Manual', ";
          float irrigRateCm = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outlet+".irrigationRate");
          float inches = irrigRateCm/2.54f*runTime/3600.0f;
          String waterStr = String.valueOf(inches);
          if (waterStr.length() > 5) {
            if (waterStr.indexOf("E-") == -1) {
              waterStr = waterStr.substring(0,5);
            } else waterStr = waterStr.substring(0,4)+waterStr.substring(waterStr.indexOf("E-"));
          }
	  values += runTimeStr+", "+waterStr+", 'in')";
	} else {
	  fields += ", zoneGroup, plc, outlet, zoneNumber, zoneName, plant, runTime, water, units)";
	  values += ", "+irr.getZoneGroupByOutlet(outlet).getNumber()+", '"+irr.getOutletName(outlet)+"', "+outlet+", ";
	  values += irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getZoneNumber()+", '"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getName()+"' ,";
	  values += "'"+irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone().getPlant()+"', "+runTimeStr+", "; 

	  String units = "'in'";
          Zone z = irr.getZoneGroupByOutlet(outlet).getZoneOutletByNumber(outlet).getZone();
          double inches = z.getIrrigRate()*runTime/3600.0;
          System.out.println("Runtime "+runTime+" "+z.getIrrigRate()+" "+inches);
          String waterStr = String.valueOf(inches);
          if (waterStr.length() > 5) {
            if (waterStr.indexOf("E-") == -1) {
            } else waterStr = waterStr.substring(0,4)+waterStr.substring(waterStr.indexOf("E-"));
          }
          if (z.getZoneType().equals("ET-micro") || z.getZoneType().equals("LF-micro")) {
            double gal = z.getIrrigRateGalHr()*runTime/3600.0;
            waterStr = String.valueOf(gal);
            if (waterStr.length() > 5) {
              if (waterStr.indexOf("E-") == -1) {
              } else waterStr = waterStr.substring(0,4)+waterStr.substring(waterStr.indexOf("E-"));
            }
	    units = "'gal'";
          }
	  values += waterStr+", "+units+")";
	}
      } catch(Exception e) {
        e.printStackTrace();
        System.out.println(_mainClass+"::updateIrrigHistory | "+ctime()+"> WARNING: Could not update irrigHistory database.");
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not update irrigHistory database."); 
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not update irrigHistory database.</html>");
      }

      query += fields+values;
      System.out.println("QUERY: "+query);
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = dbCon.prepareStatement(query); ) {
	stmt.execute();
	System.out.println(_mainClass+"::updateIrrigHistory | "+ctime()+"> Updated irrigation history database for "+irrigIP+" outlet "+outlet); 
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::updateIrrigHistory | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized void updateCounterHistory(CirrigIrrigator irr, String irrigIP, int counter) {
      String query = "INSERT INTO counterHistory(";
      String fields = "dateTime, irrigIP";
      String values = " VALUES("+String.valueOf(System.currentTimeMillis()/1000)+", '"+irrigIP+"'";

      try {
	String counterValue = String.valueOf(irr.getCounterValue(counter));
	String counterName = irr.getCounterName(counter);
	String zoneGroup = String.valueOf(irr.getZoneGroupByCounter(counter).getNumber());
        fields += ", zoneGroup, plc, counterNumber, counts)";
	values += ", "+zoneGroup+", '"+counterName+"', "+counter+", "+counterValue+")";
      } catch(Exception e) {
        e.printStackTrace();
        System.out.println(_mainClass+"::updateCounterHistory | "+ctime()+"> WARNING: Could not update counterHistory database.");
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not update counterHistory database.");
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not update counterHistory database.</html>");
      }
      query += fields+values;
      System.out.println("QUERY: "+query);
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = dbCon.prepareStatement(query); ) {
        stmt.execute();
        System.out.println(_mainClass+"::updateCounterHistory | "+ctime()+"> Updated counter history database for "+irrigIP+" counter "+counter);
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::updateCounterHistory | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void checkIrrigZoneGroups(String irrKey) {
      String irrigIP = getReplyToken(irrKey, 0, "::");
      String uid = getReplyToken(irrKey, 1, "::");
      CirrigIrrigator irr = _irrDatabase.getIrrigator(irrKey);
      Iterator<ZoneGroup> i = irr.getZoneGroups().iterator();
      while (i.hasNext()) {
	ZoneGroup zg = (ZoneGroup)i.next();
	if (zg.isReadyToIrrigate()) {
	  synchronized(this) {
	    //update XML file here within synchronized block
            saveDefaults(irrigIP);
	  }
	  logAndResetCounters(irr, irrigIP, zg);
          IrrigationMonitoringThread imt = (IrrigationMonitoringThread)irrigationMonitoringThreadDb.get(irrKey);
	  boolean success = imt.addZoneGroupToQueue(zg);
	  if (success) {
	    System.out.println(_mainClass+"::checkIrrigZoneGroups | " + ctime() + "> Zone group "+zg.getName()+" is ready to irrigate cycle number "+zg.getCycleNumber()+"..."); 
	  } else {
            System.out.println(_mainClass+"::checkIrrigZoneGroups | " + ctime() + "> ERROR: Zone group "+zg.getName()+" failed to be added to queue!");
            _health = "BAD";
            updateDatabase(_mainClass+":health", _health, "BAD", "Zone group "+zg.getName()+" failed to be added to queue!");
            updateDatabase(_mainClass+":"+irrigIP+".status", "Error");
	  }
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void logAndResetCounters(CirrigIrrigator irr, String irrigIP, ZoneGroup zg) {
      ArrayList<Counter> counters = zg.getCounters();
      Iterator<Counter> i = counters.iterator();
      while (i.hasNext()) {
	Counter c = (Counter)i.next();
	updateCounterHistory(irr, irrigIP, c.getCounterNumber()); //log counts
	System.out.println(_mainClass+"::logAndResetCounters | " + ctime() + "> Logged counts "+irr.getCounterValue(c.getCounterNumber())+" for counter "+c.getCounterNumber()+" on PLC "+irrigIP+" and reset.");
	irr.resetCounter(c.getCounterNumber()); //reset counts
      }
    }

//-----------------------------------------------------------------------------------------------------

    public void handleIrrigatorError(CirrigIrrigator irr, String irrigIP, String message) {
	System.out.println(_mainClass+"::handleIrrigatorError | "+ctime()+"> ERROR: irrigator"+irrigIP+" at "+ctime()+": "+message);
	updateIrrigatorLog("Irrigator turned DISCONNECTED.  ErrMsg = "+message, irrigIP);
	irr.closeConnection();
	_health = "BAD";
	updateDatabase(_mainClass+":health", _health, "BAD", "Could not connect to irrigator "+irrigIP+"!");
	updateDatabase(_mainClass+":"+irrigIP+".status", "Error");
    }

//-----------------------------------------------------------------------------------------------------

    /* This helper method should be overridden to return the proper subclass
     * UFMMTClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFMMTClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      return new CirrigPlcClientThread(clsoc, clientCount, simMode);
    }

//=====================================================================================================

    protected class IrrigationMonitoringThread extends Thread {
        private String _className = getClass().getName()+"_"+getName()+"_"+getId();
        private CirrigIrrigator irr;
        private String irrigIP, uid;
        private int ntries = 0;
        boolean error = false, success = true, settingTimer = false, _connecting = false;
        boolean _isRunning = true;
        protected long _irrigHeart = 0;

        private ArrayDeque<ZoneOutlet> _irrigQueue; //queue of Zone Outlets to irrigate!
        private ArrayList<ZoneOutlet> _runningOutlets; //list of ZoneOutlets currently running

        public IrrigationMonitoringThread(CirrigIrrigator irr, String irrigIP, String uid) {
          this.irr = irr;
          this.irrigIP = irrigIP;
          this.uid = uid;
          _irrigQueue = new ArrayDeque();
	  _runningOutlets = new ArrayList();
          System.out.println(_className+":> Created new IrrigationMonitoringThread at "+ctime()+" for CirrigIrrigator "+irrigIP+"; uid "+uid);
          setName("IrrigationMonitoringThread: "+irrigIP+"::"+uid);
        }

        public void run() {
          int sleepTime = 1000;
          boolean keepRunning = true;
          boolean success = false;
          ntries = 0;
          hibernate(2500);
          while (_isRunning) {
            hibernate(sleepTime);
            //update the heartbeat
            _irrigHeart = System.currentTimeMillis()/1000;
	    if (error && !irr.isConnected()) continue; //nothing to do if irrigator is not connected
	    if (error && irr.isConnected()) {
	      //clear error
	      error = false;
	      ntries = 0;
	      System.out.println(_className+"::run | " + ctime() + "> Reconnected to irrigator "+irrigIP);
	    }
	    if (!_irrigQueue.isEmpty() && _runningOutlets.size() < irr.getMaxSimultaneous()) {
	      //a ZoneOutlet is in the irrig queue and the number of running outlets is under the max
	      ZoneOutlet zo = _irrigQueue.peek(); //first peek at zone outlet to check
	      ZoneGroup zg = irr.getZoneGroupByOutlet(zo.getOutletNumber());
	      boolean allowToRun = true;
	      if (!irr.allowMultipleGroups() && !_runningOutlets.isEmpty()) {
		if (zg != irr.getZoneGroupByOutlet(_runningOutlets.get(0).getOutletNumber())) allowToRun = false; //at least one outlet from another zone group is running
	      }
	      if (zg == null) {
		allowToRun = false;
	      } else if (zg.getNRunning() >= zg.getMaxSimultaneous()) {
		allowToRun = false;
	      }
	      if (allowToRun) {
		//Ready to run but don't remove from queue yet until successful start of irrigation
		int outletNum = zo.getOutletNumber();
		if (zo.isSystemTest()) {
		  //this is a system test; timer has already been set, just need to start and then continue to next cycle in loop
                  if (_simMode) {
                    System.out.println(_className+":run | " + ctime() + "> Simulating START TIMER "+irrigIP+"."+outletNum);
                    updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".power", "On");
                    updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".timerStat", "Running");
                  } else {
                    //try starting irrigator
                    try {
                      synchronized(irr) {
                        success = irr.powerOff(outletNum);
                        success = irr.startTimer(outletNum); //start timer
                      }
                      System.out.println(_className+":run | " + ctime() + "> START TIMER "+irrigIP+"."+outletNum+" response: "+success);
                      updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".timerStat", "Running");
                    } catch (Exception e) {
                      System.out.println(_className+":run | " + ctime() + "> ERROR starting timer "+irrigIP+"."+outletNum+": "+e.toString());
                    }
                  }
                  if (success) {
                    //successfully started irrigation (or just power off for irrigCm=0 or sim mode)
                    //must synchronize 9/5/18 because addZoneGroupToQueue can be called from another thread
                    synchronized(_irrigQueue) {
                      zo = _irrigQueue.remove(); //now remove zone outlet
                      System.out.println(_className+"::run | " + ctime() + "> Removing zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") from irrigation queue (queue length = "+_irrigQueue.size()+") and STARTING irrigation...");

                    }
                    zo.setRunning(true); //set its state
                    _runningOutlets.add(zo); //add to queue of running outlets
		    zo.setSystemTest(false); //unset flag for system test
                  }
		  continue; //continue to next loop iteration
		}
                String lastMessage = "<html>";
		float irrigCm = zo.getIrrig()/zg.getNCycles(); //cm to irrigate = total irrigation / number of cycles
		if (zg.getCycleMode().equals("continuous")) {
		  irrigCm = zo.getIrrig(); //cm to irrigate = total irrigation if continuous cycle mode
		  System.out.println(_className+"::run | " + ctime() + "> Continuous cycle mode...");
		}
                System.out.println(_className+"::run | " + ctime() + "> Starting irrigation for zone "+zo.getZoneName()+" (outlet "+outletNum+") = "+irrigCm+" cm");
		lastMessage += "Starting irrigation for zone "+zo.getZoneName()+" (outlet "+outletNum+") = "+irrigCm+" cm at "+ctime()+"<br/>";
		float histIrrigRate = zo.getIrrigRate();
		float rate = getMMTFloatValue(_mainClass+":"+irrigIP+"."+outletNum+".irrigationRate");
		if (rate != histIrrigRate) {
		  System.out.println(_className+"::run | " + ctime() + "> WARNING: current irrigation rate of "+rate+" does not match zone history rate of "+histIrrigRate);
                  lastMessage += "<b>WARNING:</b> current irrigation rate of "+rate+" does not match zone history rate of "+histIrrigRate+"<br/>";
		}
		
		success = setIrrigation(irrigCm, irr, outletNum); //sec or cm?  Rate? 
		updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".timerStat", "Set");

		//special case
		if (irrigCm == 0) {
		  System.out.println(_className+"::run | " + ctime() + "> Irrigation set to 0 -- timer will not be started.");
		  lastMessage += "Irrigation set to 0 -- timer will not be started.";
		  //update HTML log for zero irrigation since log won't be updated otherwise as it never powers off
		  try {
                    updateHTMLLog(irr, irrigIP, outletNum, 0);
		  } catch (Exception e) {
                    System.out.println(_className+"::run | "+ctime()+"> Error writing log: "+e.toString());
                  }
                  try {
                    updateIrrigHistory(irr, irrigIP, outletNum, 0);
                  } catch (Exception e) {
                    System.out.println(_className+"::run | "+ctime()+"> Error writing database: "+e.toString());
                  }
                }
		lastMessage += "</html>";
                updateDatabase(_mainClass+":"+irrigIP+".lastMessage", lastMessage);

                if (_simMode && irrigCm != 0) {
                  System.out.println(_className+":run | " + ctime() + "> Simulating START TIMER "+irrigIP+"."+outletNum);
                  updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".power", "On");
                  updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".timerStat", "Running");
                } else {
		  //try starting irrigator
                  try {
                    synchronized(irr) {
                      success = irr.powerOff(outletNum);
		      if (irrigCm != 0) success = irr.startTimer(outletNum); //start timer if non-zero irrigation requested!
                    }
		    if (irrigCm != 0) {
                      System.out.println(_className+":run | " + ctime() + "> START TIMER "+irrigIP+"."+outletNum+" response: "+success);
                      updateDatabase(_mainClass+":"+irrigIP+"."+outletNum+".timerStat", "Running");
		    }
                  } catch (Exception e) {
                    System.out.println(_className+":run | " + ctime() + "> ERROR starting timer "+irrigIP+"."+outletNum+": "+e.toString());
                  }
		}
		if (success) {
		  //successfully started irrigation (or just power off for irrigCm=0 or sim mode)
		  //must synchronize 9/5/18 because addZoneGroupToQueue can be called from another thread
		  synchronized(_irrigQueue) {
		    zo = _irrigQueue.remove(); //now remove zone outlet
                    System.out.println(_className+"::run | " + ctime() + "> Removing zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") from irrigation queue (queue length = "+_irrigQueue.size()+") and STARTING irrigation...");

		  }
		  if (irrigCm != 0) {
                    zo.setRunning(true); //set its state
		    _runningOutlets.add(zo); //add to queue of running outlets
		  }
		} else {
		  //Comm error
                  System.out.println(_className+":run | " +ctime() + "> Error talking to irrigator "+irrigIP); 
                  _health = "BAD";
                  updateDatabase(_mainClass+":health", _health, "BAD", "Error talking to irrigator "+irrigIP); 
                  updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>Error connecting to irrigator! At "+ctime()+"</html>");
                  ntries++;
                  //after 3 tries, set error=true so that it will give up until reconnected
                  if (ntries >= 3) error = true;
                  //try again
                  irr.closeConnection();
                  success = connectToIrrigator(irr, 1);
                  continue;
		}
	      }
	    }
	    //now iterate over runningOutlets and check for any whose timerStat has 
	    Iterator<ZoneOutlet> i = _runningOutlets.iterator();
	    while (i.hasNext()) {
	      ZoneOutlet zo = (ZoneOutlet)i.next();
              int outletNum = zo.getOutletNumber();
	      String timerStat = getMMTValue(_mainClass+":"+irrigIP+"."+outletNum+".timerStat");
	      if (timerStat.equals("Stopped")) {
                System.out.println(_className+":run | " +ctime() +"> Timer "+irrigIP+"."+outletNum+" STOPPED");
		zo.setRunning(false); //update its state
		//remove from _runningOutlets
		i.remove(); //use iterator remove
              }
	    }
          }
        }

	public synchronized boolean addZoneGroupToQueue(ZoneGroup zg) {
	  synchronized(_irrigQueue) {
	    Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
	    while (i.hasNext()) {
	      ZoneOutlet zo = (ZoneOutlet)i.next();
              //check this zone outlet to see if it currently is running or in queue 
              if (zo.running()) {
                System.out.println(_className+"::addZoneGroupToQueue | " + ctime() + "> Error: zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is currently already running!");
                return false;
	      } else if (_irrigQueue.contains(zo)) {
                System.out.println(_className+"::addZoneGroupToQueue | " + ctime() + "> Error: zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is already in the queue!");
                return false;
              }
	      if (!zo.isUpdated()) {
                System.out.println(_className+"::addZoneGroupToQueue | " + ctime() + "> Zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is not updated.  Skipping...");
	      }
	      if (zo.isEnabled()) {
	        _irrigQueue.add(zo);
	        System.out.println(_className+"::addZoneGroupToQueue | " + ctime() + "> Added zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") to irrigation queue (queue length = "+_irrigQueue.size()+")..."); 
	      }
            }
	  }
	  return true;
        }

        public synchronized boolean addZoneGroupToQueueAsSystemTest(ZoneGroup zg) {
          synchronized(_irrigQueue) {
            Iterator<ZoneOutlet> i = zg.getZoneOutletsByPriority().iterator();
            while (i.hasNext()) {
              ZoneOutlet zo = (ZoneOutlet)i.next();
              //check this zone outlet to see if it currently is running or is a system test
              if (zo.running()) {
                System.out.println(_className+"::addZoneGroupToQueueAsSystemTest | " + ctime() + "> Error: zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is currently already running!"); 
		return false;	
              } else if (zo.isSystemTest()) {
                System.out.println(_className+"::addZoneGroupToQueueAsSystemTest | " + ctime() + "> Error: zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is already set as a system test!"); 
		return false;
	      } else if (_irrigQueue.contains(zo)) {
                System.out.println(_className+"::addZoneGroupToQueueAsSystemTest | " + ctime() + "> Error: zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") is already in the queue!");
		return false;
	      }
	      zo.setSystemTest(true);
	      _irrigQueue.add(zo);
              System.out.println(_className+"::addZoneGroupToQueueAsSystemTest | " + ctime() + "> Added zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") to irrigation queue for system test (queue length = "+_irrigQueue.size()+")...");
            }
          }
	  return true;
        }

        public synchronized void removeAllOutletsFromIrrigationQueue() {
	    //clear irrigQueue for this entire irrigator (e.g. ABORT, ALL OFF)
	    synchronized(_irrigQueue) {
	      _irrigQueue.clear();
              System.out.println(_className+"::removeAllOutletsFromIrrigationQueue | " + ctime() + "> Removed ALL zones from irrigation queue (queue length = "+_irrigQueue.size()+") and CANCELLING irrigation...");
	    }
        }

	public synchronized void removeOutletFromIrrigationQueue(ZoneOutlet zo) {
	    //remove a single zone outlet from the queue (used with Stop in system test)
	    synchronized(_irrigQueue) {
	      if (_irrigQueue.contains(zo)) {
                System.out.println(_className+"::removeOutletFromIrrigationQueue | " + ctime() + "> Removing zone "+zo.getZoneName()+" (outlet "+zo.getOutletNumber()+", pri="+zo.getPriority()+") from irrigation queue (queue length = "+_irrigQueue.size()+") and CANCELLING irrigation...");
		zo.setSystemTest(false); //in case a system test, unset here
		_irrigQueue.remove(zo);
	      }
	    }
	}

        public void shutdown() {
          _isRunning = false;
        }

        public boolean error() {
          return error;
        }

        public long heart() {
          return _irrigHeart;
        }

        public boolean isConnecting() {
          return _connecting;
        }
      }

//=====================================================================================================

    protected class IrrigPollingThread extends Thread {
        private String _className = getClass().getName()+"_"+getName()+"_"+getId();
        private CirrigIrrigator irr;
        private String irrigIP, uid, irrKey;
	boolean error = false, success = true, settingTimer = false, _connecting = false;
	boolean _isRunning = true;
	protected long _irrigHeart = 0;

        public IrrigPollingThread(CirrigIrrigator irr, String irrigIP, String uid) {
	  _connecting = false;
	  _irrigHeart = System.currentTimeMillis()/1000;
          this.irr = irr;
          this.irrigIP = irrigIP;
          this.uid = uid;
	  irrKey = irrigIP+"::"+uid;
          System.out.println(_className+":> Created new Irrig Polling Thread at "+ctime()+" for CirrigIrrigator "+irrigIP+"; uid "+uid);
	  setName("IrrigPollingThread: "+irrKey);
        }

        public void run() {
          int sleepTime = 1000;
	  int reconnectTime = 30000;
	  int fails = 0; //fails will catch problem when it can connect to irrigator but gets an error code and keeps reconnecting every 2 seconds
          while (_isRunning) {
	    /* Sleep time can be set for each irrigator */
	    sleepTime = getMMTIntValue(_mainClass+":"+irrigIP+".sleepTime")*1000;
	    //update the heartbeat
	    _irrigHeart = System.currentTimeMillis()/1000;
	    //busy flag is set by set timer
	    if (irr.busy()) {
	      System.out.println(_className+":run> CirrigIrrigator is busy - presumably setting timer.  Sleeping at "+ctime());
	      hibernate(sleepTime);
	      continue;
	    }
	    if (!irr.isConnected()) {
	      if (!error) {
		error = true;
		System.out.println(_className+":run1> Error polling irrigator "+irrigIP+" at "+ctime());
	      }
	      if (fails >= 5) {
		//at this point, INIT must be done manually to reset irrigator connection
		if (fails == 5) {
		  System.out.println(_className+":run> Failed 5 times in a row.  Must now manaully INIT to reconnect to "+irrigIP+" at "+ctime());
		  fails++;
		}
		continue;
	      }
	      //try reconnecting to irrigator
	      success = reconnect();
	      if (success) {
		System.out.println(_className+":run> Successfully reconnected to "+irrigIP+" at "+ctime());
		updateIrrigatorLog("Irrigator turned RECONNECTED", irrigIP);
		error = false;
	      } else {
		//continue if unsuccessful so it doesn't try polling
		hibernate(reconnectTime);
		continue;
	      }
	    }
	    success = pollIrrigator(irrigIP, uid);
	    if (!success) {
	      //first occurrance of error
	      error = true;
	      fails++; //increment fails
	      System.out.println(_className+":run> Error polling irrigator "+irrigIP+" at "+ctime());
	      hibernate(sleepTime);
	      continue;
	    }
	    fails = 0; //reset fails if it polls successfully, whether or not previous state was error!  7/10/14
	    if (success && error) {
	      //irrigator was polled successfully this time without reconnecting, clear error
	      error = false;
	    }
	    if (_irrDatabase.hasIrrigator(irrKey)) {
	      checkIrrigZoneGroups(irrKey); //check zone groups in this irrigator and see if any are ready to irrigate
	    }
	    error = false;
	    hibernate(sleepTime);
          }
        }

	public void shutdown() {
	  _isRunning = false;
	}

	public boolean error() {
	  return error;
	}

	public long heart() {
	  return _irrigHeart;
	}

	public boolean isConnecting() {
	  return _connecting;
	}

	public boolean reconnect() {
	  _connecting = true;
	  boolean success = false;
          //Set connecting to true so that new threads don't get restarted if it is hanging on connecting because PLC is down
          System.out.println(_className+":reconnect> Attempting to reconnect to irrigator "+irrigIP+" at "+ctime());
	  irr.closeConnection();
          success = connectToIrrigator(irr, 1);
          _irrigHeart = System.currentTimeMillis()/1000;
          _connecting = false;
	  return success;
	}
    }

//=====================================================================================================

    // Class AncillaryThread creates thread to poll device for status and send info to status clients

    protected class AncillaryThread extends UFMMTThreadedAgent.AncillaryThread {

        private String _className = getClass().getName();

        public AncillaryThread() {}

        public void run() {
          int n = 0;
          while(true) {
            if (_shutdown) {
              System.out.println(_className+"::run> Shutting down ancillary thread...");
              return;
            }
            while (_isRunning) {
              if (_shutdown) {
                System.out.println(_className+"::run> Shutting down ancillary thread...");
                return;
              }
              n++;
	      if (n >= 5) {
		//unset _ancillaryRunning so that it doesn't kill ancillary thread
		//if reconnect attempt takes more than 5s. 
		_ancillaryRunning = false;
		checkIrrigPollingThreads();
		checkIrrigationMonitoringThreads();
	 	n = 0;
	      }
              /* update heartbeat */
              heartbeat();
              /* Send new database values to status client threads.
                Actual sending will occur in each UFMMTClientThread */
	      updateStatusClients();
              /* Check for new records */
              updateDatabase();
              /* Sleep 1 second */
              hibernate();
              /* Update _ancillaryRunning at end of loop */
              _ancillaryRunning = true;
            }
            hibernate();
          }
        }
    }

} //end of class CirrigPlcAgent



