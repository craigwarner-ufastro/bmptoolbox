package BMPToolbox; 
/**
 * Title:        BmpPlcAgent
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for java agents to override
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/* for e-mail support */
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

//=====================================================================================================

public class BmpPlcAgent extends UFMMTThreadedAgent { 

    public static final
	String rcsID = "$Name:  $ $Id: BmpPlcAgent.java,v 1.6 2011/01/24 19:36:31 warner Exp $";

    protected String _mainClass = getClass().getName();
    protected String ccropHost = "www.bmptoolbox.org";
    protected boolean _initialized = false;
    protected int ccropPort = 57002;
    protected LinkedHashMap <String, IrrigRealTime> irrigDatabase;
    protected IrrigatorDatabase _irrDatabase;
    protected IrrigThread _irrigThread;
    protected String _logdir = UFExecCommand.getEnvVar("HOME")+"/bmplogs";
    protected Vector <Integer> userList;
    protected LinkedHashMap <String, IrrigPollingThread> irrigPollingThreadDb;
    protected Vector<String> emails;

//----------------------------------------------------------------------------------------

    public BmpPlcAgent( int serverPort, String[] args )
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
        irrigDatabase = new LinkedHashMap(20);
	_irrDatabase = new IrrigatorDatabase();
        userList = new Vector(10); 
        irrigPollingThreadDb = new LinkedHashMap(10);
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      emails = new Vector(10);
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
	} else if (args[j].toLowerCase().indexOf("-logdir") != -1) {
	  if (args.length > j+1) _logdir = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-email") != -1) {
	  if (args.length > j+1) emails.add(args[j+1]);
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
      LinkedHashMap <String, Irrigator> irrigList = _irrDatabase.getIrrigators();
      try {
        PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/irrigList.xml"));
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<irrigList>");
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
	  Irrigator irr = (Irrigator)irrigList.get(key);
	  pw.println("  <irrigator type=\"" + irr.getType() + "\" uid=\"" + irr.getUid() + "\" ip=\"" + irr.getHost() + "\"/>");
	}  
        pw.println("</irrigList>");
        pw.close();

        ArrayList<String> currentValues = new ArrayList();
        currentValues.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        currentValues.add("<database>");
        String xmlFile = installDir+"/etc/databaseStartupValues.xml";
        String[] recsToSave = { "id", "name", "hour", "minute", "lagTime", "sleepTime", "maxIrrig", "minIrrig", "allowZero", "irrigationRate", "irrigatorLog", "defaultIrrigation", "manualDefault", "external"};

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
                  //this is default section for BmpPlcAgent 
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
          currentValues.add("    <agent name=\"" + _mainClass + "\">");
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
          currentValues.add("    </agent>");
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

    /** Read XML irrigList.xml file and add Irrigators */
    protected void readIrrigatorList() {
      String irrigFile = installDir+"/etc/irrigList.xml";
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
	int uid = -1;
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
	    System.out.println(_mainClass+"::readIrrigatorList> Adding Irrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
	    addIrrigator(type, irrigIP, uid);
	  } catch (Exception e) {
	    System.out.println(e.toString());
	    continue;
	  }
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    /** Add users to userList on startup */
    protected void setUserList() {
      LinkedHashMap <String, Irrigator> irrigList = _irrDatabase.getIrrigators();
      String irrigIP, key, type, id;
      Integer uid;
      for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
	key = (String)i.next();
	irrigIP = getReplyToken(key, 0, "::");
        uid = new Integer(getReplyToken(key, 1, "::"));
	if (userList.contains(uid)) continue;
        id = getMMTValue(_mainClass+":"+irrigIP+".id");
	if (_verbose) System.out.println(_mainClass+":setUserList> Adding user "+uid);
	userList.add(uid);
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
      /* read Irrigator list first */
      readIrrigatorList();
      /* read XML defaults */
      readXMLDefaults();
      /* update IrrigRealTimes */
      updateIrrigDatabase();
      /* setup User list */
      setUserList();
      _initialized = false;
      updateDatabase(_mainClass+":status", "INIT");
      if (_simMode) {
        System.out.println(_mainClass+"::init> Simulating connection to irrigator...");
      }
      _initialized = true;
      updateDatabase(_mainClass+":status", "IDLE");
    }


//-----------------------------------------------------------------------------------------------------

    /** Add a new Irrigator */
    protected boolean addIrrigator(String type, String irrigIP, int uid) {
      Irrigator irr = null;
      if (type.toLowerCase().equals("bmpplc")) {
	irr = new BMPPLC(irrigIP, uid);
      } else if (type.toLowerCase().equals("simplc")) {
	irr = new SimPLC(irrigIP, uid);
      } else if (type.toLowerCase().equals("csvoutput")) {
	irr = new CSVOutput(irrigIP, uid);
      } else {
        System.out.println(_mainClass+"::addIrrigator> ERROR: invalid irrigator type: "+type);
	return false;
      }
      if (_verbose) irr.verbose(_verbose);
      boolean success = connectToIrrigator(irr, 3);
      //ensure that irrigator is powered off.
      if (success) irr.powerOff();
      if (success && _verbose && emails.size() > 0) sendEmail("Connected to irrigator IP "+irrigIP+" at "+ctime()+".");
      _irrDatabase.addIrrigator(irr);
      /* Database records specific to this Irrigator */
      addMMTRecord(_mainClass+":"+irrigIP+".id", UFRecord.TYPE_STRING, "None");
      addMMTRecord(_mainClass+":"+irrigIP+".name", UFRecord.TYPE_STRING, "None");
      addMMTRecord(_mainClass+":"+irrigIP+".external", UFRecord.TYPE_STRING, "None");
      addMMTRecord(_mainClass+":"+irrigIP+".status", UFRecord.TYPE_STRING, "Disconnected");
      addMMTRecord(_mainClass+":"+irrigIP+".power", UFRecord.TYPE_STRING, "Off");
      addMMTRecord(_mainClass+":"+irrigIP+".timerStat", UFRecord.TYPE_STRING, "Stopped");
      addMMTRecord(_mainClass+":"+irrigIP+".timer", UFRecord.TYPE_FLOAT, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".irrigation", UFRecord.TYPE_FLOAT, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".maxIrrig", UFRecord.TYPE_FLOAT, "10");
      addMMTRecord(_mainClass+":"+irrigIP+".minIrrig", UFRecord.TYPE_FLOAT, "0.01");
      addMMTRecord(_mainClass+":"+irrigIP+".allowZero", UFRecord.TYPE_STRING, "Yes");
      addMMTRecord(_mainClass+":"+irrigIP+".hour", UFRecord.TYPE_INT, "6");
      addMMTRecord(_mainClass+":"+irrigIP+".minute", UFRecord.TYPE_INT, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".lagTime", UFRecord.TYPE_INT, "120");
      addMMTRecord(_mainClass+":"+irrigIP+".timerValue", UFRecord.TYPE_FLOAT, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".timerStatusLabel", UFRecord.TYPE_STRING, "Timer Off");
      addMMTRecord(_mainClass+":"+irrigIP+".irrigatorLog", UFRecord.TYPE_STRING, "irrigHistory."+irrigIP.replaceAll("/","_")+".log");
      addMMTRecord(_mainClass+":"+irrigIP+".defaultIrrigation", UFRecord.TYPE_STRING, "5-day Max");
      addMMTRecord(_mainClass+":"+irrigIP+".manualDefault", UFRecord.TYPE_FLOAT, "0.0");
      addMMTRecord(_mainClass+":"+irrigIP+".irrigationRate", UFRecord.TYPE_FLOAT, "1.5");
      addMMTRecord(_mainClass+":"+irrigIP+".lastMessage", UFRecord.TYPE_STRING, "None");
      addMMTRecord(_mainClass+":"+irrigIP+".startTime", UFRecord.TYPE_LONG, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".sleepTime", UFRecord.TYPE_INT, "1");
      addMMTRecord(_mainClass+":"+irrigIP+".lastOn", UFRecord.TYPE_INT, "0");
      addMMTRecord(_mainClass+":"+irrigIP+".ncycles", UFRecord.TYPE_INT, "1");
      return success; 
    }

    protected boolean deleteIrrigator(String irrigIP, int uid) {
      String key = irrigIP+"::"+uid;
      if (_irrDatabase.hasIrrigator(key)) {
	_irrDatabase.removeIrrigator(key);
	updateIrrigDatabase(irrigIP+"::"+uid);
        if (irrigPollingThreadDb.containsKey(key)) {
	  //remove ipt too
          IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
	  ipt.shutdown();
          irrigPollingThreadDb.remove(key);
	}
	return true;
      }
      updateIrrigDatabase(irrigIP+"::"+uid);
      return false;
    }

    protected boolean changeIrrigator(String oldIP, String irrigIP, int uid) {
      String key = oldIP+"::"+uid;
      if (_irrDatabase.hasIrrigator(key)) {
	String type = _irrDatabase.getIrrigatorType(key);
	_irrDatabase.removeIrrigator(key);
	updateIrrigDatabase(irrigIP+"::"+uid);
        if (irrigPollingThreadDb.containsKey(key)) {
          //remove ipt too
          IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
          ipt.shutdown();
          irrigPollingThreadDb.remove(key);
        }
        return addIrrigator(type, irrigIP, uid);
      }
      return false;
    }

//-----------------------------------------------------------------------------------------------------


    /** Helper method to connect to irrigator 
      */
    protected boolean connectToIrrigator(Irrigator irr, int maxtries) {
      int ntries = 0;
      String irrigIP = irr.getHost();
      boolean success = false;
      while (!irr.isConnected() && ntries < maxtries) {
	success = irr.connect();
	if (success) {
	  System.out.println(_mainClass+"::connectToIrrigator> Successfully connected to irrigator "+irrigIP+" at "+ctime());
	  updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>Successfully connected to irrigator "+irrigIP+" at "+ctime()+"</html>");
	  _health = "GOOD";
	  updateDatabase(_mainClass+":health", _health, "GOOD", "Reconnected to irrigator "+irrigIP+" at "+ctime());
	} else {
	  System.out.println(_mainClass+"::connectToIrrigator> Error!  Unable to connect to irrigator "+irrigIP);
	}
	hibernate();
	ntries++;
      }
      if (!success && ntries >= maxtries) {
        _health = "BAD";
        updateDatabase(_mainClass+":health", _health, "BAD", "Could not connect to irrigator "+irrigIP+"!");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>"+ctime()+": Could not connect to irrigator "+irrigIP+"!</html>");
        System.out.println(_mainClass+"::connectToIrrigator> ERROR: Could not connect to irrigator "+irrigIP+"!  Failed "+maxtries+" times! "+ctime());
	return false;
      }
      return true;
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
        System.err.println(_mainClass+"::action> Received null request!");
        return false;
      }
      String clientName = req.name().substring(0, req.name().indexOf(":"));
      boolean success = false;
      updateDatabase(_mainClass+":status", "BUSY");

      nreq = req.numVals();
      System.out.println(_mainClass+"::action> Received new request.");
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
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          /* ABORT turns off output and timer! */ 
          success = abort(irrigIP, uid); 
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
          System.out.println(_mainClass+"::action> performing INIT for Irrigator "+irrigIP);
	  if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
	    System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
	    reply = new UFStrings("ERROR", "Error sending INIT command for "+irrigIP);
	    success = false;
	    return success;
	  }
	  Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
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
        System.out.println(_mainClass+"::action> Received command but not initialized!");
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
        System.err.println(_mainClass+"::action> ERROR: Received request but unable to obtain lock!");
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

      /* Do work here */
      for (int j = 0; j < nreq; j++) {
        if (getCmdName(req,j).equals("SET_TIMER")) {
	  String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  float sec = 0;
	  try {
	    sec = Float.parseFloat(getReplyToken(param, 2));
	  } catch(NumberFormatException nfe) {
	    System.out.println(_mainClass+"::action> Error: invalid timer value "+param);
            reply = new UFStrings("ERROR", "Invalid timer value: "+param);
            success = false;
            break;
	  }
	  if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
	  }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  success = setTimer(sec, irr);
	  if (success) {
            responses.add("SUCCESS timer = "+param);
	  } else {
	    System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!  Timer not set!");
	    reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
	    break;
	  }
	} else if (getCmdName(req,j).equals("SET_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  float cm = 0;
	  try {
	    cm = Float.parseFloat(getReplyToken(param, 2));
	  } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
	  }
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  success = setIrrigation(cm, irr); 
          if (success) {
            responses.add("SUCCESS irrigation = "+param);
          } else {
            System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!  Timer not set!");
            reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
            break;
          }
	} else if (getCmdName(req,j).equals("SET_MAX_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          float maxIrr = 0;
          try {
            maxIrr = Float.parseFloat(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
          }
	  updateDatabase(_mainClass+":"+irrigIP+".maxIrrig", maxIrr);
	  responses.add("SUCCESS max irrigation = "+maxIrr);
        } else if (getCmdName(req,j).equals("SET_MIN_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          float minIrr = 0;
          try {
            minIrr = Float.parseFloat(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: invalid irrigation value "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation value: "+param);
            success = false;
            break;
          }
          updateDatabase(_mainClass+":"+irrigIP+".minIrrig", minIrr);
          responses.add("SUCCESS min irrigation = "+minIrr);
        } else if (getCmdName(req,j).equals("SET_ALLOW_ZERO")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String allowZero = getReplyToken(param, 2);
	  if (allowZero.equals("Yes") || allowZero.equals("No")) {
	    updateDatabase(_mainClass+":"+irrigIP+".allowZero", allowZero);
	    responses.add("SUCCESS allow zero = "+allowZero);
	  } else {
            System.out.println(_mainClass+"::action> Error: invalid value for SET_ALLOW_ZERO: "+param);
            reply = new UFStrings("ERROR", "Invalid value for SET_ALLOW_ZERO: "+param);
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("SET_IRRIGATION_RATE")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          float rate = 0; 
          try {
            rate = Float.parseFloat(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: invalid irrigation rate "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation rate: "+param);
            success = false;
            break;
          }
          updateDatabase(_mainClass+":"+irrigIP+".irrigationRate", rate);
          responses.add("SUCCESS irrigation rate = "+rate);
        } else if (getCmdName(req,j).equals("POWER_ON")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
	  Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
	  if (_simMode) {
	    System.out.println(_mainClass+"::action> Simulating Power ON "+irrigIP);
	    updateDatabase(_mainClass+":"+irrigIP+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
	  } else { 
	    success = irr.powerOn();
	    System.out.println(_mainClass+"::action> Power ON "+irrigIP+" response: "+success);
	    if (!success) {
              System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
	    }
	    pollIrrigator(irrigIP, uid);
	    updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
	  }
          responses.add("SUCCESS power = on");
	} else if (getCmdName(req,j).equals("POWER_OFF")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating Power OFF "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+".power", "Off");
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
          } else {
            success = irr.powerOff();
            System.out.println(_mainClass+"::action> Power OFF "+irrigIP+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
          }
	  responses.add("SUCCESS power = off");
        } else if (getCmdName(req,j).equals("START_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating START TIMER "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
          } else {
	    success = irr.startTimer();
            System.out.println(_mainClass+"::action> START TIMER "+irrigIP+" response: "+success+" at "+ctime());
            if (!success) {
              System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"! At "+ctime());
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
          }
          responses.add("SUCCESS timer started");
        } else if (getCmdName(req,j).equals("PAUSE_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating PAUSE TIMER "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+".power", "Off");
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Paused");
          } else {
	    success = irr.pauseTimer();
            System.out.println(_mainClass+"::action> PAUSE TIMER "+irrigIP+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Paused");
          }
          responses.add("SUCCESS timer paused");
        } else if (getCmdName(req,j).equals("RESUME_TIMER")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
          Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating RESUME TIMER "+irrigIP);
            updateDatabase(_mainClass+":"+irrigIP+".power", "On");
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
          } else {
            success = irr.resumeTimer();
            System.out.println(_mainClass+"::action> RESUME TIMER "+irrigIP+" response: "+success);
            if (!success) {
              System.out.println(_mainClass+"::action> Error connecting to irrigator "+irrigIP+"!");
              reply = new UFStrings("ERROR", "Error connecting to irrigator "+irrigIP+"!");
              break;
            }
            pollIrrigator(irrigIP, uid);
            updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
          }
          responses.add("SUCCESS timer resumed");
        } else if (getCmdName(req,j).equals("SET_HOUR")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String hour = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" hour to "+hour);
          updateDatabase(_mainClass+":"+irrigIP+".hour", hour);
          success = updateIrrigDatabase(irrigIP+"::"+uid);
          responses.add("SUCCESS hour = "+hour);
        } else if (getCmdName(req,j).equals("SET_MINUTE")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String minute = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" minute to "+minute);
          updateDatabase(_mainClass+":"+irrigIP+".minute", minute);
          success = updateIrrigDatabase(irrigIP+"::"+uid);
          responses.add("SUCCESS minute = "+minute);
        } else if (getCmdName(req,j).equals("SET_LAGTIME")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String lagTime = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" lag time to "+lagTime);
          String timerStat = getMMTValue(_mainClass+":"+irrigIP+".timerStat");
          if (timerStat.equals("Set")) {
	    int oldLag = getMMTIntValue(_mainClass+":"+irrigIP+".lagTime");
	    int newLag = Integer.parseInt(lagTime);
	    long startTime = getMMTLongValue(_mainClass+":"+irrigIP+".startTime");
	    startTime += (newLag-oldLag)*1000;
	    updateDatabase(_mainClass+":"+irrigIP+".startTime", startTime); 
	  } 
          updateDatabase(_mainClass+":"+irrigIP+".lagTime", lagTime);
          responses.add("SUCCESS lag time = "+lagTime);
        } else if (getCmdName(req,j).equals("SET_IRRIGATOR_LOG")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String logName = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" history log to "+logName);
	  updateDatabase(_mainClass+":"+irrigIP+".irrigatorLog", logName);
          responses.add("SUCCESS irrigator "+irrigIP+" history log = "+logName);
        } else if (getCmdName(req,j).equals("SET_DEFAULT_IRRIGATION")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String defIrrig = getReplyTokens(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" default irrigation method to: "+defIrrig);
	  if (defIrrig.toLowerCase().equals("yesterday") || defIrrig.toLowerCase().equals("3-day avg") || defIrrig.toLowerCase().equals("5-day max") || defIrrig.toLowerCase().equals("none") || defIrrig.toLowerCase().equals("manual default")) {
            updateDatabase(_mainClass+":"+irrigIP+".defaultIrrigation", defIrrig);
            success = updateIrrigDefault(irrigIP+"::"+uid);
	    responses.add("SUCCESS Irrigator "+irrigIP+" default irrigation = "+defIrrig);
	  } else {
            System.out.println(_mainClass+"::action> Error: invalid default irrigation method "+param);
            reply = new UFStrings("ERROR", "Invalid irrigation method: "+param);
            success = false;
            break;
	  }
        } else if (getCmdName(req,j).equals("SET_MANUAL_DEFAULT")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          String manIrrig = getReplyToken(param, 2);
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" manual default irrigation to: "+manIrrig);
          updateDatabase(_mainClass+":"+irrigIP+".manualDefault", manIrrig);
          responses.add("SUCCESS Irrigator "+irrigIP+" manual default irrigation = "+manIrrig);
	} else if (getCmdName(req,j).equals("LOGIN")) {
          /* LOGIN::user pass */
          String param = getCmdParam(req,j);
          String user = getReplyToken(param, 0);
          String pass = getReplyToken(param, 1);
	  String uid = "-1";
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating LOGIN");
            responses.add("SIM LoggedIn -1");
	    responses.add("Irrigator::127.0.0.1");
	    responses.add("RT::-1::Test Run");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant");
          } else {
	    Socket ccropSocket = null;
	    try {
	      System.out.println(_mainClass+"::action> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
	      ccropSocket = new Socket(ccropHost, ccropPort);
              ccropSocket.setSoTimeout(_timeout);
              UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
              greet.sendTo(ccropSocket);
              UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
              ccropSocket.setSoTimeout(0); //infinite timeout
              if (ufpr == null) {
                System.out.println(_mainClass+"::action> ERROR: received null object!  Closing socket!");
		reply = new UFStrings("ERROR", "Could not connect to CCROP agent!");
                ccropSocket.close();
		success = false;
		break;
              } else {
                String request = ufpr.name().toLowerCase();
                if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
                  System.out.println(_mainClass+"::action> connection established: "+request);
                } else {
                  System.out.println(_mainClass+"::action> ERROR: received "+request+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received invalid response "+request+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
                }
		/* First get UID */
		UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_UID::"+user+" "+pass);
                System.out.println(_mainClass+"::action> Sending GET_UID request for user "+user);
                ccropReq.sendTo(ccropSocket);
                UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                if (getReplyToken(ccropReply, 0).equals("SUCCESS") && getReplyToken(ccropReply, 1).equals("UID")) {
		  uid = getReplyToken(ccropReply, 2);
		  responses.add("Success LoggedIn "+uid);
		  /* Add to user login map */
		  if (!userList.contains(new Integer(uid))) userList.add(new Integer(uid));
		} else {
                  System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(0)+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(0)+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
		}
		/* Second get any Irrigators associated with this UID */
		if (_irrDatabase.hasIrrigatorWithUid(uid)) {
		  responses.addAll(_irrDatabase.getIrrigatorIPs(uid));
		}
		/* Third request RT Runs */
		ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_RT_RUNS::"+uid);
                System.out.println(_mainClass+"::action> Sending GET_RT request.");
                ccropReq.sendTo(ccropSocket);
                ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
		for (int i = 0; i < ccropReply.numVals(); i++) {
		  if (getReplyToken(ccropReply, 0, "::").equals("RT")) {
		    System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
		    responses.add(ccropReply.stringAt(i));
		  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
		     System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
		  } else {
                    System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
		  }
                }
		/* Fourth request Zones */
                ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONES::"+uid);
                System.out.println(_mainClass+"::action> Sending GET_ZONES request.");
                ccropReq.sendTo(ccropSocket);
                ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                for (int i = 0; i < ccropReply.numVals(); i++) {
                  if (getReplyToken(ccropReply, 0, "::").equals("ZONE")) {
                    System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                    responses.add(ccropReply.stringAt(i));
                  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
                     System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                  } else {
                    System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
                  }
                }
		ccropSocket.close();
	      }
            } catch (IOException ioe) {
              System.err.println(_mainClass+"::action> ERROR: "+ioe.toString());
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
            System.out.println(_mainClass+"::action> Simulating REFRESH");
            responses.add("SIM Refreshed -1");
            responses.add("Irrigator::127.0.0.1");
            responses.add("RT::-1::Test Run");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant");
          } else {
            Socket ccropSocket = null;
            try {
              System.out.println(_mainClass+"::action> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
              ccropSocket = new Socket(ccropHost, ccropPort);
              ccropSocket.setSoTimeout(_timeout);
              UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
              greet.sendTo(ccropSocket);
              UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
              ccropSocket.setSoTimeout(0); //infinite timeout
              if (ufpr == null) {
                System.out.println(_mainClass+"::action> ERROR: received null object!  Closing socket!");
                reply = new UFStrings("ERROR", "Could not connect to CCROP agent!");
                ccropSocket.close();
                success = false;
                break;
              } else {
                String request = ufpr.name().toLowerCase();
                if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
                  System.out.println(_mainClass+"::action> connection established: "+request);
                } else {
                  System.out.println(_mainClass+"::action> ERROR: received "+request+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received invalid response "+request+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
                }
		responses.add("SUCCESS Refreshed "+uid);
                /* Second get any Irrigators associated with this UID */
                if (_irrDatabase.hasIrrigatorWithUid(uid)) {
                  responses.addAll(_irrDatabase.getIrrigatorIPs(uid));
                }
                /* Third request RT Runs */
                UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_RT_RUNS::"+uid);
                System.out.println(_mainClass+"::action> Sending GET_RT request.");
                ccropReq.sendTo(ccropSocket);
                UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                for (int i = 0; i < ccropReply.numVals(); i++) {
                  if (getReplyToken(ccropReply, 0, "::").equals("RT")) {
                    System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                    responses.add(ccropReply.stringAt(i));
                  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
                     System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                  } else {
                    System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
                  }
                }
                /* Fourth request Zones */
                ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONES::"+uid);
                System.out.println(_mainClass+"::action> Sending GET_ZONES request.");
                ccropReq.sendTo(ccropSocket);
                ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
                for (int i = 0; i < ccropReply.numVals(); i++) {
                  if (getReplyToken(ccropReply, 0, "::").equals("ZONE")) {
                    System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                    responses.add(ccropReply.stringAt(i));
                  } else if (getReplyToken(ccropReply, 0, "::").equals("NONE")) {
                     System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(i));
                  } else {
                    System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(i)+".  Closing socket!");
                    reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(i)+" from CCROP Agent");
                    ccropSocket.close();
                    success = false;
                    break;
                  }
                }
                ccropSocket.close();
              }
            } catch (IOException ioe) {
              System.err.println(_mainClass+"::action> ERROR: "+ioe.toString());
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
          System.out.println(_mainClass+"::action> Success! Logged out uid = "+uid);
          responses.add("SUCCESS LoggedOut "+uid);
        } else if (getCmdName(req, j).equals("ADD_IRRIGATOR")) {
          /* ADD_IRRIGATOR::uid ip type*/
          String param = getCmdParam(req,j);
          int uid = -1;
	  String irrigIP = getReplyToken(param, 1);
	  String type = getReplyToken(param, 2);
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action> Adding Irrigator of type "+type+", IP = "+irrigIP+", uid = "+uid);
	  /* Add irrigator */
	  success = addIrrigator(type, irrigIP, uid);
          if (success) {
            /* Save defaults */
            saveDefaults(irrigIP);
            responses.add("SUCCESS Added Irrigator "+type+" "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action> Error adding Irrigator "+irrigIP);
            reply = new UFStrings("ERROR", "Error adding Irrigator "+irrigIP);
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
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action> Deleting Irrigator with IP = "+irrigIP+", uid = "+uid);
          /* delete irrigator */
          success = deleteIrrigator(irrigIP, uid);
          if (success) {
	    /* Save defaults */
	    saveDefaults(irrigIP, true);
            responses.add("SUCCESS Deleted Irrigator "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action> Error deleting Irrigator "+irrigIP);
            reply = new UFStrings("ERROR", "Error deleting Irrigator "+irrigIP);
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
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action> Changing Irrigator IP "+ oldIP + " to "+irrigIP+", uid = "+uid);
          /* change irrigator */
          success = changeIrrigator(oldIP, irrigIP, uid);
          if (success) {
            /* Save defaults */
            saveDefaults(oldIP, true);
            saveDefaults(irrigIP);
            responses.add("SUCCESS Changed Irrigator "+oldIP+" "+irrigIP);
          } else {
            System.out.println(_mainClass+"::action> Error changing Irrigator IP "+oldIP+" to "+irrigIP); 
            reply = new UFStrings("ERROR", "Error changing Irrigator IP "+oldIP+" to "+irrigIP); 
            break;
          }
        } else if (getCmdName(req,j).equals("SET_ZONE")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
	  String type = getReplyToken(param, 2);
	  String id = getReplyToken(param, 3);
	  if (type.toLowerCase().equals("none") || id.equals("0")) {
	    /* Clear this zone */
	    updateDatabase(_mainClass+":"+irrigIP+".name", type); 
	    updateDatabase(_mainClass+":"+irrigIP+".id", type+" "+id);
            responses.add("SUCCESS "+irrigIP+" = "+type+" "+id); 
	    success = updateIrrigDatabase(irrigIP+"::"+uid);
	  } else if (type.toLowerCase().equals("fixed") || id.equals("-2")) {
	    /* Fixed manual default irrigation */
            updateDatabase(_mainClass+":"+irrigIP+".name", "Fixed Manual Default");
            updateDatabase(_mainClass+":"+irrigIP+".id", type+" "+id);
            updateDatabase(_mainClass+":"+irrigIP+".external", "Fixed Manual Default");
            responses.add("SUCCESS "+irrigIP+" = "+type+" "+id);
            success = updateIrrigDatabase(irrigIP+"::"+uid);
	  } else if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating SET_ZONE");
            responses.add("SUCCESS "+irrigIP+" = "+type+" "+id);
            updateDatabase(_mainClass+":"+irrigIP+".id", type+" "+id);
            updateDatabase(_mainClass+":"+irrigIP+".name", type+" "+id);
          } else {
            Socket ccropSocket = null;
            try {
              System.out.println(_mainClass+"::action> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
              ccropSocket = new Socket(ccropHost, ccropPort);
              ccropSocket.setSoTimeout(_timeout);
              UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
              greet.sendTo(ccropSocket);
              UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
              ccropSocket.setSoTimeout(0); //infinite timeout
              if (ufpr == null) {
                System.out.println(_mainClass+"::action> ERROR: received null object!  Closing socket!");
                reply = new UFStrings("ERROR", "Could not connect to CCROP agent!");
                ccropSocket.close();
                success = false;
                break;
              } else {
                String request = ufpr.name().toLowerCase();
                if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
                  System.out.println(_mainClass+"::action> connection established: "+request);
                } else {
                  System.out.println(_mainClass+"::action> ERROR: received "+request+".  Closing socket!");
                  reply = new UFStrings("ERROR", "Received invalid response "+request+" from CCROP Agent");
                  ccropSocket.close();
                  success = false;
                  break;
                }
                UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_NAME::"+uid+" "+type+" "+id);
		if (type.toLowerCase().equals("all")) {
		  ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONE_REFS::"+uid);
		  System.out.println(_mainClass+"::action> Sending GET_ZONE_REFS request.");
		} else {
		  System.out.println(_mainClass+"::action> Sending GET_NAME request.");
		}
                ccropReq.sendTo(ccropSocket);
                UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
		if (getReplyToken(ccropReply, 0, "::").equals("RT") || getReplyToken(ccropReply, 0, "::").equals("ZONE")) {
		  System.out.println(_mainClass+"::action> Received "+ccropReply.stringAt(0));
		  updateDatabase(_mainClass+":"+irrigIP+".id", type+" "+id);
		  String[] temp = ccropReply.stringAt(0).split("::");
		  if (temp[0].equals("RT")) {
		    updateDatabase(_mainClass+":"+irrigIP+".name", "RT Run "+temp[1]+": "+temp[2]);
		  } else if (temp[0].equals("ZONE")) {
		    updateDatabase(_mainClass+":"+irrigIP+".name",  "Zone "+temp[2]+": "+temp[3]+" - "+temp[4]);
                    updateDatabase(_mainClass+":"+irrigIP+".ncycles", temp[5]);
		    updateDatabase(_mainClass+":"+irrigIP+".external", temp[6]);
		  }
		  success = updateIrrigDatabase(irrigIP+"::"+uid);
		  responses.add("SUCCESS "+ccropReply.stringAt(0));
		} else if (getReplyToken(ccropReply, 0, "::").equals("ZONE_REF")) {
		  String idString = "M "+getReplyToken(ccropReply, 1, "::"); 
		  String ncyclesString = getReplyToken(ccropReply, 2, "::");
		  String refString = getReplyToken(ccropReply, 3, "::"); 
		  for (int i = 1; i < ccropReply.numVals(); i++) {
		    String[] temp = ccropReply.stringAt(i).split("::");
		    idString += ","+temp[1];
		    ncyclesString += ","+temp[2];
		    refString += "::"+temp[3];
		  } 
		  updateDatabase(_mainClass+":"+irrigIP+".name", "All Zones");
		  updateDatabase(_mainClass+":"+irrigIP+".id", idString);
		  updateDatabase(_mainClass+":"+irrigIP+".ncycles", ncyclesString);
		  updateDatabase(_mainClass+":"+irrigIP+".external", refString);
		  success = updateIrrigDatabase(irrigIP+"::"+uid);
		  responses.add("SUCCESS All Zones");
		} else {
		  System.out.println(_mainClass+"::action> ERROR: received "+ccropReply.stringAt(0)+".  Closing socket!");
		  reply = new UFStrings("ERROR", "Received "+ccropReply.stringAt(0)+" from CCROP Agent");
		  ccropSocket.close();
		  success = false;
		  break;
		}
                ccropSocket.close();
              }
            } catch (IOException ioe) {
              System.err.println(_mainClass+"::action> ERROR: "+ioe.toString());
              reply = new UFStrings("ERROR", "Error talking to CCROP Agent: "+ioe.toString());
              try {
                ccropSocket.close();
              } catch (Exception e) {}
              success = false;
            }
            /* break here on error after statement and resultset are closed */
            if (success == false) break;
	  }
        } else if (getCmdName(req, j).equals("SAVE_DEFAULTS")) {
          /* SAVE_DEFAULTS::uid ip */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          String irrigIP = getReplyToken(param, 1);
          if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
            System.out.println(_mainClass+"::action> Error: could not find irrigator "+irrigIP+" for user "+uid);
            reply = new UFStrings("ERROR", "Could not find irrigator "+irrigIP+" for user "+uid);
            success = false;
            break;
          }
	  saveDefaults(irrigIP);
	  System.out.println(_mainClass+"::action> Saving defaults for "+irrigIP);
          responses.add("SUCCESS saved defaults for "+irrigIP);
        } else if (getCmdName(req,j).equals("SET_SLEEP_TIME")) {
          String param = getCmdParam(req,j);
          String irrigIP = getReplyToken(param, 0);
          String uid = getReplyToken(param, 1);
          int sleepTime = 1;
          try {
            sleepTime = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: invalid sleep time "+param);
            reply = new UFStrings("ERROR", "Invalid sleep time value: "+param);
            success = false;
            break;
          }
	  if (sleepTime < 1) {
            System.out.println(_mainClass+"::action> Error: invalid sleep time "+param);
            reply = new UFStrings("ERROR", "Invalid sleep time value: "+param);
            success = false;
            break;
	  }
          System.out.println(_mainClass+"::action> Updating irrigator "+irrigIP+" sleep time to: "+sleepTime);
          updateDatabase(_mainClass+":"+irrigIP+".sleepTime", sleepTime);
          responses.add("SUCCESS Irrigator "+irrigIP+" sleep time = "+sleepTime);
	} else {
          /* Invalid command */
          success = false;
          String cmd = getCmdName(req,j);
          reply = new UFStrings("ERROR", "Unknown command: "+cmd);
          System.out.println(_mainClass+"::action> ERROR: Unknown command: "+cmd);
          break;
	}
      }
      /* Release lock */
      if (releaseLock()) {
        _hasLock = false;
      } else {
        System.err.println(_mainClass+"::action> ERROR: Unable to release lock!  This should not happen!");
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

    protected boolean setIrrigation(float cm, Irrigator irr) { 
      String irrigIP = irr.getHost();
      float maxIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".maxIrrig");
      float minIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".minIrrig");
      String allowZero = getMMTValue(_mainClass+":"+irrigIP+".allowZero");
      if (cm > maxIrr) {
	System.out.println(_mainClass+":"+irrigIP+".:setIrrigation> WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!");
	System.out.println("\t\tUsing maximum irrigation of "+maxIrr+" cm.");
	cm = maxIrr;
	updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!</html>");
      } else if (cm > 0 && cm < minIrr && allowZero.equals("Yes")) {
        System.out.println(_mainClass+":"+irrigIP+".:setIrrigation> WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!");
        System.out.println("\t\tUsing nonzero minimum irrigation of "+minIrr+" cm.");
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!</html>");
        cm = minIrr;
      } else if (cm < minIrr && allowZero.equals("No")) {
        System.out.println(_mainClass+":"+irrigIP+".:setIrrigation> WARNING: Requested irrigation of "+cm+" cm is below MINIMUM!");
        System.out.println("\t\tUsing minimum irrigation of "+minIrr+" cm.");
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm is below MINIMUM!</html>");
        cm = minIrr;
      }
      float rate = getMMTFloatValue(_mainClass+":"+irrigIP+".irrigationRate");
      float sec = cm*3600/rate;
      System.out.println(_mainClass+":"+irrigIP+".:setIrrigation> Setting irrigation to "+cm+" cm; timer = "+sec+" sec");
      updateIrrigatorLog("Setting irrigation to "+cm+" cm; timer = "+sec+" sec", irrigIP);
      if (_simMode) {
        updateDatabase(_mainClass+":"+irrigIP+".timer", sec);
        updateDatabase(_mainClass+":"+irrigIP+".irrigation", cm);
        return true;
      }
      boolean success = irr.setTimer(sec);
      return success;
    }

    protected boolean setTimer(float sec, Irrigator irr) {
      String irrigIP = irr.getHost();
      float rate = getMMTFloatValue(_mainClass+":"+irrigIP+".irrigationRate");
      float cm = (sec/3600)*rate;
      float maxIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".maxIrrig");
      float minIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".minIrrig");
      if (cm > maxIrr) {
        System.out.println(_mainClass+":"+irrigIP+".:setTimer> WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!");
        System.out.println("\t\tUsing maximum irrigation of "+maxIrr+" cm.");
        cm = maxIrr;
	sec = cm*3600/rate;
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm exceeds MAXIMUM!</html>");
      } else if (cm > 0 && cm < minIrr) {
        System.out.println(_mainClass+":"+irrigIP+".:setTimer> WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!");
        System.out.println("\t\tUsing nonzero minimum irrigation of "+minIrr+" cm.");
        cm = minIrr;
	sec = cm*3600/rate;
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm is below nonzero MINIMUM!</html>");
      }
      System.out.println(_mainClass+":"+irrigIP+".:setTimer> Setting irrigation to "+cm+" cm; timer = "+sec+" sec");
      if (_simMode) {
        updateDatabase(_mainClass+":"+irrigIP+".timer", sec);
        updateDatabase(_mainClass+":"+irrigIP+".irrigation", cm);
	return true;
      }
      boolean success = irr.setTimer(sec);
      return success;
    }

//-----------------------------------------------------------------------------------------------------

    protected boolean abort(String irrigIP, String uid) {
      boolean success = true;
      if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
        System.out.println(_mainClass+"::abort> Error: could not find irrigator "+irrigIP+" for user "+uid);
        return false;
      }
      Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
      if (_simMode) {
	System.out.println(_mainClass+"::abort> Simulating ABORT");
        updateDatabase(_mainClass+":"+irrigIP+".power", "Off");
        updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
      } else {
	success = irr.powerOff();
	System.out.println(_mainClass+"::abort> Abort response: "+success);
        if (!success) {
          System.out.println(_mainClass+"::abort> Error connecting to irrigator "+irrigIP+"!");
        }
        pollIrrigator(irrigIP, uid);
        updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
	updateDatabase(_mainClass+":"+irrigIP+".startTime", 0);
      }
      return success;
    }

    protected void checkIrrigPollingThreads() {
      synchronized(irrigPollingThreadDb) {
        LinkedHashMap <String, Irrigator> irrigList = _irrDatabase.getIrrigators();
        for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
          String key = (String)i.next();
          Irrigator irr = (Irrigator)irrigList.get(key);
          String irrigIP = getReplyToken(key, 0, "::");
          String uid = getReplyToken(key, 1, "::");
	  if (irrigPollingThreadDb.containsKey(key)) {
	    IrrigPollingThread ipt = (IrrigPollingThread)irrigPollingThreadDb.get(key);
	    //Check if irrigation polling thread is alive.  Also check isConnecting because
	    //if PLC is down, it can hang here until it comes back up.  Don't want to start multiple threads here.
	    if (System.currentTimeMillis()/1000 - ipt.heart() > 60 && !ipt.isConnecting()) {
	      System.out.println(_mainClass+"::checkIrrigPollingThreads> "+ctime()+" Killing thread "+ipt.getName()+" that has not updated heartbeat in 60 seconds.");
	      ipt.shutdown();
	      irrigPollingThreadDb.remove(key);
	      addNewIrrigPollingThread(irr, irrigIP, uid);
	    } else if (System.currentTimeMillis()/1000 - ipt.heart() > 30 && !ipt.isConnecting()) {
              System.out.println(_mainClass+"::checkIrrigPollingThreads> "+ctime()+" reconnecting to irrigator "+irrigIP+" that has not updated heartbeat in 30 seconds.");
              boolean success = ipt.reconnect();
	      if (success) {
		System.out.println(_mainClass+"::checkIrrigPollingThreads> "+ctime()+" successfully reconnected to "+irrigIP);
	      } else {
		System.out.println(_mainClass+"::checkIrrigPollingThreads> Error: unable to reconnect to "+irrigIP+" at "+ctime());
	      }
	    }
	  } else {
	    addNewIrrigPollingThread(irr, irrigIP, uid);
	  }
	}
      }
    }

    protected void addNewIrrigPollingThread(Irrigator irr, String irrigIP, String uid) {
      IrrigPollingThread pollingThread = new IrrigPollingThread(irr, irrigIP, uid);
      irrigPollingThreadDb.put(irrigIP+"::"+uid, pollingThread);
      pollingThread.start();
    }

    protected void pollIrrigators() {
      LinkedHashMap <String, Irrigator> irrigList = _irrDatabase.getIrrigators();
      for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        Irrigator irr = (Irrigator)irrigList.get(key);
	String irrigIP = getReplyToken(key, 0, "::"); 
	String uid = getReplyToken(key, 1, "::");
	pollIrrigator(irrigIP, uid);
      }
    }

    protected boolean pollIrrigator(String irrigIP, String uid) {
      if (!_irrDatabase.hasIrrigator(irrigIP+"::"+uid)) {
        System.out.println(_mainClass+"::abort> Error: could not find irrigator "+irrigIP+" for user "+uid);
        return false;
      }
      Irrigator irr = _irrDatabase.getIrrigator(irrigIP+"::"+uid);
      if (_simMode) {
	/* Simulated */
	updateDatabase(_mainClass+":"+irrigIP+".status", "Simulated");
        long t = (long)(System.currentTimeMillis()/1000);
        if (t%2 != 0) {
          updateDatabase(_mainClass+":"+irrigIP+".power", "On");
        } else {
          updateDatabase(_mainClass+":"+irrigIP+".power", "Off");
        }
        return true;
      } else if (!irr.isConnected()) {
	/* Disconnected */
        updateDatabase(_mainClass+":"+irrigIP+".status", "Disconnected");
        long t = (long)(System.currentTimeMillis()/1000);
        if (t%2 != 0) {
          updateDatabase(_mainClass+":"+irrigIP+".power", "Irrigator");
        } else {
          updateDatabase(_mainClass+":"+irrigIP+".power", "Disconnected");
        }
	return false;
      }
      synchronized(irr) {
        try {
	  boolean err = irr.getErrorStatus();
	  if (err) {
	    //error already exists
            handleIrrigatorError(irr, irrigIP, irr.getErrorMessage());
	    return false;
	  }
	  //connected
	  updateDatabase(_mainClass+":"+irrigIP+".status", "Connected");

	  //read power status
	  boolean powerStat = irr.getPowerStatus();
	  //Check for an error
	  if (irr.getErrorStatus()) {
	    //error
            handleIrrigatorError(irr, irrigIP, "Could not read power status!");
            return false;
	  }
	  if (powerStat) {
	    if (!getMMTValue(_mainClass+":"+irrigIP+".power").toLowerCase().equals("on")) {
	      updateIrrigatorLog("Irrigation turned ON", irrigIP);
	      updateDatabase(_mainClass+":"+irrigIP+".lastOn", (int)(System.currentTimeMillis()/1000));
	    }
            updateDatabase(_mainClass+":"+irrigIP+".power", "On");
	  } else {
	    if (getMMTValue(_mainClass+":"+irrigIP+".power").toLowerCase().equals("on")) {
	      updateIrrigatorLog("Irrigation turned OFF", irrigIP);
	      int runTime = (int)(System.currentTimeMillis()/1000) - getMMTIntValue(_mainClass+":"+irrigIP+".lastOn"); 
	      updateIrrigatorLog("Total irrigation time = "+(runTime/60)+" minutes and "+(runTime%60)+" seconds.", irrigIP);
	    }
            updateDatabase(_mainClass+":"+irrigIP+".power", "Off");
	  }

	  //read timer
	  float sec = irr.getTimer();
	  if (sec == -1) {
	    handleIrrigatorError(irr, irrigIP, "Could not read timer!");
	    //error
            return false;
	  }
	  float rate = getMMTFloatValue(_mainClass+":"+irrigIP+".irrigationRate");
          float cm = (sec/3600)*rate;
	  updateDatabase(_mainClass+":"+irrigIP+".timer", sec);
	  updateDatabase(_mainClass+":"+irrigIP+".irrigation", cm);

	  //read timer status
	  int timerStatus = irr.getTimerStatus();
	  if (timerStatus == 1) {
	    updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Stopped");
	  } else if (timerStatus == -1) {
            handleIrrigatorError(irr, irrigIP, "Could not read timer status!");
            //error
            return false;
	  }

	  //read timer value
	  float timerVal = irr.getTimerValue();
	  if (timerVal == -1) {
            handleIrrigatorError(irr, irrigIP, "Could not read timer value!");
            //error
            return false;
          }
	  String timerStat = getMMTValue(_mainClass+":"+irrigIP+".timerStat");
	  if (timerStat.equals("Stopped")) {
	    updateDatabase(_mainClass+":"+irrigIP+".timerStatusLabel", "Timer Off");
	    updateDatabase(_mainClass+":"+irrigIP+".timerValue", 0);
	  } else if (timerStat.equals("Set")) {
	    updateDatabase(_mainClass+":"+irrigIP+".timerStatusLabel", "Timer will start in:");
	    updateDatabase(_mainClass+":"+irrigIP+".timerValue", (float)(getMMTLongValue(_mainClass+":"+irrigIP+".startTime")-System.currentTimeMillis())/1000);
	  } else {
	    updateDatabase(_mainClass+":"+irrigIP+".timerStatusLabel", "Timer value:");
            updateDatabase(_mainClass+":"+irrigIP+".timerValue", timerVal);
	  }

          err = irr.getErrorStatus();
          if (err) {
            handleIrrigatorError(irr, irrigIP, irr.getErrorMessage()); 
            //error
            return false;
          }
        } catch(Exception ex) {
	  System.out.println(_mainClass+":pollIrrigator> ERROR: "+ex.toString());
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
	System.out.println(_mainClass+"::updateIrrigatorLog> Updating log for "+irrigIP+": "+ message + " at "+ctime());
      } catch(IOException e) {
	e.printStackTrace();
	System.out.println(_mainClass+"::updateIrrigatorLog> WARNING: Could not open irrigator log file "+logfile);
        _health = "WARNING";
        updateDatabase(_mainClass+":health", _health, "Warning", "Could not open irrigator log file "+logfile);
        updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Could not open irrigator log file "+logfile+"</html>");
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected boolean updateIrrigDefault(String irrKey) {
      long tstamp = (long)(System.currentTimeMillis()/1000);
      String irrigIP = getReplyToken(irrKey, 0, "::");
      String defMethod = getMMTValue(_mainClass+":"+irrigIP+".defaultIrrigation");
      if (irrigDatabase.containsKey(irrKey)) {
        IrrigRealTime rt = (IrrigRealTime)irrigDatabase.get(irrKey);
	System.out.println(_mainClass+"::updateIrrigDefault> Updating default method for irrigator "+irrKey+" to "+defMethod);
        rt.setDefMethod(defMethod);
        rt.updateTimeStamp(tstamp);
	return true;
      }
      return false;
    }

    /* Add irrig real times for all Irrigators */
    protected void updateIrrigDatabase() {
      LinkedHashMap <String, Irrigator> irrigList = _irrDatabase.getIrrigators();
      String key; 
      for (Iterator i = irrigList.keySet().iterator(); i.hasNext(); ) {
        key = (String)i.next();
	updateIrrigDatabase(key);
      }
    }


    protected boolean updateIrrigDatabase(String irrKey) {
      long tstamp = (long)(System.currentTimeMillis()/1000);
      String irrigIP = getReplyToken(irrKey, 0, "::");
      String uid = getReplyToken(irrKey, 1, "::");
      int hour = getMMTIntValue(_mainClass+":"+irrigIP+".hour");
      int minute = getMMTIntValue(_mainClass+":"+irrigIP+".minute");
      String id = getMMTValue(_mainClass+":"+irrigIP+".id");
      String external = getMMTValue(_mainClass+":"+irrigIP+".external");
      String ncycles = getMMTValue(_mainClass+":"+irrigIP+".ncycles");
      String defMethod = getMMTValue(_mainClass+":"+irrigIP+".defaultIrrigation");
      if (id == null) return false;
      if (irrigDatabase.containsKey(irrKey)) {
	IrrigRealTime rt = (IrrigRealTime)irrigDatabase.get(irrKey);
        if (id.toLowerCase().startsWith("none") || !_irrDatabase.hasIrrigator(irrKey)) {
	  /* Cancel this run */
	  System.out.println(_mainClass+"::updateIrrigDatabase> Cancelling run "+rt.getName());
	  rt.cancel();
	  irrigDatabase.remove(irrKey); 
        } else {
	  /* Change run by cancelling and adding new run */ 
          System.out.println(_mainClass+"::updateIrrigDatabase> Cancelling run "+rt.getName());
          rt.cancel();
          rt = new IrrigRealTime(irrKey, id, hour, minute);
	  rt.setExternal(external);
	  rt.setNcyclesString(ncycles);
	  rt.setDefMethod(defMethod);
          irrigDatabase.put(irrKey, rt);
          rt.start();
          rt.updateTimeStamp(tstamp);
          rt.updateHostAndPort(ccropHost, ccropPort);
          System.out.println(_mainClass+"::updateIrrigDatabase> Adding run "+id+" for Irrigator "+irrKey+" at "+hour+":"+minute);
	}
      } else {
	/* Add new run */
	if (id.toLowerCase().startsWith("none") || !_irrDatabase.hasIrrigator(irrKey)) {
	  return true;
	}
	IrrigRealTime rt = new IrrigRealTime(irrKey, id, hour, minute);
	rt.setExternal(external);
	rt.setNcyclesString(ncycles);
	rt.setDefMethod(defMethod);
        irrigDatabase.put(irrKey, rt);
        rt.start();
        rt.updateTimeStamp(tstamp);
        rt.updateHostAndPort(ccropHost, ccropPort);
        System.out.println(_mainClass+"::updateIrrigDatabase> Adding run "+id+" for Irrigator "+irrKey+" at "+hour+":"+minute);
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    protected boolean updateIrrig(String irrKey) {
      String irrigIP = getReplyToken(irrKey, 0, "::");
      String uid = getReplyToken(irrKey, 1, "::");
      IrrigRealTime rt = (IrrigRealTime)irrigDatabase.get(irrKey);
      if (rt.error()) {
	System.out.println(_mainClass+"::updateIrrig> ERROR in Irrigator "+irrKey+": "+rt.getErrorMsg());
	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html><b><font color=\"red\">ERROR:</font></b> "+rt.getErrorMsg()+"</html>");
	rt.clearError();
	return false;
      }
      if (rt.readyToUpdate()) {
	if (!_irrDatabase.hasIrrigator(irrKey)) {
	  System.out.println(_mainClass+"::updateIrrig> ERROR: Could not find Irrigator "+irrKey);
	  rt.cancel();
	  irrigDatabase.remove(irrKey);
          _health = "BAD";
          updateDatabase(_mainClass+":health", _health, "BAD", "Error Could not find Irrigator "+irrKey+" at "+ctime());
          updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>Error not find Irrigator "+irrKey+" at "+ctime()+"</html>");
	  return false;
	}
        Irrigator irr = _irrDatabase.getIrrigator(irrKey);
        boolean success = false;
	if (irr.getType().equals("CSVOutput")) {
	  //Check if database needs updating!!
	  if (rt.needsUpdate()) {
	    updateDatabase(_mainClass+":"+irrigIP+".name", "All Zones");
	    updateDatabase(_mainClass+":"+irrigIP+".external", rt.getExternal());
	    updateDatabase(_mainClass+":"+irrigIP+".ncycles", rt.getNcyclesString());
	    updateDatabase(_mainClass+":"+irrigIP+".id", rt.getIdUpdate());
	    System.out.println(_mainClass+"::updateIrrig> Updated database ID and external reference.");
	  }
	  //Get irrigation from IrrigRealTime
	  Vector<String> irrigStrings;
	  if (rt.isMultiple()) {
	    irrigStrings = rt.getMultipleIrrig();
	  } else {
	    irrigStrings = new Vector();
	    float irrig = rt.getIrrig();
	    if (rt.useDefault()) irrig = rt.getDefault();
	    float irrigMin = irrig/rt.getIrrigRate()*60.0f;
	    if (rt.isFixed()) {
	      //Check for default value now
              irrig = getMMTFloatValue(_mainClass+":"+irrigIP+".manualDefault");
	      float rate = getMMTFloatValue(_mainClass+":"+irrigIP+".irrigationRate");
	      irrigMin = irrig/rate*60.0f; 
	    }
	    int ncycles = getMMTIntValue(_mainClass+":"+irrigIP+".ncycles");
	    if (ncycles != 1) irrigMin /= ncycles;
            irrigStrings.add("\""+rt.getExternal()+"\","+roundVal(irrig, 3)+","+roundVal(irrigMin, 3)+","+ncycles);
	    //irrigStrings.add("\""+rt.getExternal()+"\","+irrig+","+irrigMin+","+ncycles);
	  }
	  //Check for mix/max values
	  float maxIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".maxIrrig");
	  float minIrr = getMMTFloatValue(_mainClass+":"+irrigIP+".minIrrig");
	  String allowZero = getMMTValue(_mainClass+":"+irrigIP+".allowZero");
	  for (int j = 0; j < irrigStrings.size(); j++) {
	    String[] temp = irrigStrings.get(j).split(",");
	    try {
	      float cm = Float.parseFloat(temp[1]);
	      float cmPerMin = Float.parseFloat(temp[1])/Float.parseFloat(temp[2]);
	      float irrigMin = 0;
	      if (cm > maxIrr) {
		System.out.println(_mainClass+":"+irrigIP+".:updateIrrig> WARNING: Requested irrigation of "+cm+" cm for "+temp[0]+" exceeds MAXIMUM!");
		irrigMin = maxIrr/cmPerMin;
		System.out.println("\t\tUsing maximum irrigation of "+maxIrr+" cm ("+irrigMin+" minutes).");
		//update irrigStrings
		irrigStrings.set(j, temp[0]+","+maxIrr+","+irrigMin);
		updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
		updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm exceeds MAXIMUM!</html>");
	      } else if (cm > 0 && cm < minIrr && allowZero.equals("Yes")) {
		System.out.println(_mainClass+":"+irrigIP+".:updateIrrig> WARNING: Requested irrigation of "+cm+" cm for "+temp[0]+" is below nonzero MINIMUM!");
		irrigMin = minIrr/cmPerMin;
		System.out.println("\t\tUsing nonzero minimum irrigation of "+minIrr+" cm ("+irrigMin+" minutes).");
                //update irrigStrings
                irrigStrings.set(j, temp[0]+","+minIrr+","+irrigMin);
		updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
		updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING: Requested irrigation of "+cm+" cm is below nonzero MINIMUM!</html>");
	      } else if (cm < minIrr && allowZero.equals("No")) {
		System.out.println(_mainClass+":"+irrigIP+".:updateIrrig> WARNING: Requested irrigation of "+cm+" cm for "+temp[0]+" is below MINIMUM!");
		irrigMin = minIrr/cmPerMin;
        	System.out.println("\t\tUsing minimum irrigation of "+minIrr+" cm ("+irrigMin+" minutes).");
                //update irrigStrings
                irrigStrings.set(j, temp[0]+","+minIrr+","+irrigMin);
        	updateDatabase(_mainClass+":"+irrigIP+".status", "Warning");
        	updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>WARNING:Requested irrigation of "+cm+" cm is below MINIMUM!</html>");
      	      }
	      System.out.println(_mainClass+":"+irrigIP+".:updateIrrig> Setting irrigation "+irrigStrings.get(j));
	      updateIrrigatorLog("Setting irrigation to "+irrigStrings.get(j), irrigIP);
	    } catch(NumberFormatException nfe) {
	      System.out.println(_mainClass+":"+irrigIP+".:updateIrrig> ERROR parsing irrigation "+irrigStrings.get(j));
	      return false;
	    }
	  }
	  //Write file
	  CSVOutput csvo = (CSVOutput)irr;
	  success = csvo.writeOutput(irrigStrings);
          String lastMessage = "<html>";
	  if (success) {
	    lastMessage+="<b>SUCCESS</b>: Wrote file "+csvo.getHost()+" at "+ctime()+"</html>";
	    //set updated = false
	    rt.reset();
	  } else {
	    lastMessage+="<b>ERROR</b>: "+csvo.getErrorMessage()+" while writing file "+csvo.getHost()+" at "+ctime()+"</html>";
	  }
          updateDatabase(_mainClass+":"+irrigIP+".lastMessage", lastMessage);
	  return success;
	}
	try {
	  String lastMessage = "<html>";
          String day = rt.getDay();
          float irrig = rt.getIrrig();
	  float defIrrig = rt.getDefault();
	  boolean useDefault = rt.useDefault();
	  String defMethod = getMMTValue(_mainClass+":"+irrigIP+".defaultIrrigation");
	  String name = getMMTValue(_mainClass+":"+irrigIP+".name");
	  if (useDefault) {
	    if (defMethod.toLowerCase().equals("manual default")) {
	      defIrrig = getMMTFloatValue(_mainClass+":"+irrigIP+".manualDefault");
	    }
	    System.out.println(_mainClass+"::updateIrrig> Using default irrigation method "+defMethod+": "+defIrrig);
            lastMessage += "<b>WARNING:</b> Could not get today's irrigation!  Using method "+defMethod+": "+defIrrig+"<br/>";
	    irrig = defIrrig;
	  } else if (rt.isFixed()) {
            //Check for default value now
            irrig = getMMTFloatValue(_mainClass+":"+irrigIP+".manualDefault");
            System.out.println(_mainClass+"::updateIrrig> Using fixed manual default irrigation of "+irrig+" cm.");
            lastMessage += "Using fixed manual default irrigation: "+irrig+" cm.";
	  } else {
	    System.out.println(_mainClass+"::updateIrrig> Name: "+name+"; day: "+day+"; Irrig: "+irrig);
	    lastMessage += "Name: "+name+"; day: "+day+"; Irrig: "+irrig+"<br/>";
	  }
	  if (rt.isZone()) {
	    String rate = getMMTValue(_mainClass+":"+irrigIP+".irrigationRate");
	    if (!rate.equals(""+rt.getIrrigRate())) {
	      System.out.println(_mainClass+"::updateIrrig> WARNING: current irrigation rate of "+rate+" does not match zone history rate of "+rt.getIrrigRate());
              lastMessage += "<b>WARNING:</b> current irrigation rate of "+rate+" does not match zone history rate of "+rt.getIrrigRate()+"<br/>"; 
	    }
	  }
	  synchronized(irr) {
	    if (!_simMode) {
	      //Make sure it is set to off
	      success = irr.powerOff();
              if (!success) {
                System.out.println(_mainClass+"::updateIrrig> Error connecting to irrigator "+irrigIP+"!");
	        return false;
              }
	    }
            success = setIrrigation(irrig, irr);
	    //set updated = false only after irrigation is successfully set!
	    if (success) rt.reset();
	  }
	  if (irrig == 0) {
	    System.out.println(_mainClass+"::updateIrrig> Irrigation set to 0 -- timer will not be started.");
	    lastMessage += ctime()+": irrigation set to 0 -- timer will not be started.</html>";
	    updateDatabase(_mainClass+":"+irrigIP+".lastMessage", lastMessage);
	    return true;
	  }
          updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Set");
	  int lagTime = getMMTIntValue(_mainClass+":"+irrigIP+".lagTime");
	  System.out.println(_mainClass+"::updateIrrig> Starting irrigation in "+lagTime+" seconds!");
	  lastMessage += ctime()+": irrigation starting in "+lagTime+" seconds!</html>";
          updateDatabase(_mainClass+":"+irrigIP+".lastMessage", lastMessage);
	  updateDatabase(_mainClass+":"+irrigIP+".startTime", System.currentTimeMillis() + (long)(1000L*lagTime));
	  _irrigThread = new IrrigThread(irr, irrigIP, uid);
          _irrigThread.start();
	} catch(Exception ex) {
          System.out.println(_mainClass+":updateIrrig> ERROR: "+ex.toString());
          ex.printStackTrace();
          System.out.println(_mainClass+":updateIrrig> Error talking to irrigator "+irrigIP+" at "+ctime());
          irr.closeConnection();
          _health = "BAD";
          updateDatabase(_mainClass+":health", _health, "BAD", "Error talking to irrigator "+irrigIP+"!");
          updateDatabase(_mainClass+":"+irrigIP+".status", "Error");
          success = connectToIrrigator(irr, 3);
          if (success) {
            _health = "GOOD";
            updateDatabase(_mainClass+":health", _health, "GOOD", "Reconnected to irrigator "+irrigIP+" at "+ctime());
            updateDatabase(_mainClass+":"+irrigIP+".status", "Connected");
            return true;
          }
          return false;
        }
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    public void handleIrrigatorError(Irrigator irr, String irrigIP, String message) {
	System.out.println(_mainClass+"::handleIrrigatorError> ERROR: irrigator"+irrigIP+" at "+ctime()+": "+message);
	updateIrrigatorLog("Irrigation turned DISCONNECTED", irrigIP);
	irr.closeConnection();
	_health = "BAD";
	updateDatabase(_mainClass+":health", _health, "BAD", "Could not connect to irrigator "+irrigIP+"!");
	updateDatabase(_mainClass+":"+irrigIP+".status", "Error");
    }

//-----------------------------------------------------------------------------------------------------

    /* Send an email that this IP has disconnected */
    protected void sendEmail(String msgBody) {
      if (emails.size() == 0) return;
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);

      for (int i = 0; i < emails.size(); i++) {
	String emailTo = (String)emails.get(i);
	if (emailTo.equals("log")) {
	  System.out.println(_mainClass+"::sendEmail> LOGGING EMAIL "+msgBody);
	  continue;
	}
	try {
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress("admin@bmptoolbox.org", "BMP Toolbox"));
          msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo, emailTo));
          msg.setSubject("Warning from BMPToolbox");
          msg.setText(msgBody);
          Transport.send(msg);
        } catch (AddressException e) {
          e.printStackTrace();
        } catch (MessagingException e) {
          e.printStackTrace();
        } catch (java.io.UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    }

//-----------------------------------------------------------------------------------------------------

    /* This helper method should be overridden to return the proper subclass
     * UFMMTClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFMMTClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      return new BmpPlcClientThread(clsoc, clientCount, simMode);
    }

//=====================================================================================================

    protected class IrrigThread extends Thread {
	private String _className = getClass().getName()+"_"+getName()+"_"+getId();
	private Irrigator irr;
	private String irrigIP, uid;
	private int ntries = 0;

	public IrrigThread(Irrigator irr, String irrigIP, String uid) {
	  this.irr = irr;
	  this.irrigIP = irrigIP;
	  this.uid = uid;
	  System.out.println(_className+":> Created new IrrigThread at "+ctime()+" for Irrigator "+irrigIP+"; uid "+uid);
	  setName("IrrigationThread: "+irrigIP+"::"+uid);
	}

	public void run() {
	  boolean keepRunning = true;
	  boolean success = false; 
	  ntries = 0;
	  String timerStat = getMMTValue(_mainClass+":"+irrigIP+".timerStat");
	  System.out.println(_className+":run> Timer status = "+timerStat+" at "+ctime());
	  hibernate(2500);
	  while (keepRunning) {
	    timerStat = getMMTValue(_mainClass+":"+irrigIP+".timerStat");
	    if (timerStat.equals("Stopped")) {
	      System.out.println(_className+":run> Timer STOPPED at "+ctime());
	      return;
	    }
	    if (_verbose) System.out.println(_className+":run> Timer status = "+timerStat+" at "+ctime());
	    if (timerStat.equals("Set") && System.currentTimeMillis() > getMMTLongValue(_mainClass+":"+irrigIP+".startTime")) {
	      if (_simMode) {
		System.out.println(_className+":run> Simulating START TIMER");
		updateDatabase(_mainClass+":"+irrigIP+".power", "On");
		updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
	      } else {
		try {
		  synchronized(irr) {
		    success = irr.powerOff();
		    success = irr.startTimer();
		  }
		  System.out.println(_className+":run> START TIMER response: "+success+" at "+ctime());
		} catch (Exception e) {
		  System.out.println(_className+":run> ERROR: "+e.toString()+" at "+ctime());
		}
		if (!success) {
		  System.out.println(_className+":run> Error connecting to irrigator! At "+ctime());
		  _health = "BAD";
		  updateDatabase(_mainClass+":health", _health, "BAD", "Error connecting to irrigator! At "+ctime());
		  updateDatabase(_mainClass+":"+irrigIP+".lastMessage", "<html>Error connecting to irrigator! At "+ctime()+"</html>");
		  ntries++;
		  //after 3 tries, give up
		  if (ntries > 3) return;
		  //try again
        	  irr.closeConnection();
            	  success = connectToIrrigator(irr, 1);
		  continue;
		}
		System.out.println(_className+":run> Polling Irrigator at "+ctime());
		pollIrrigator(irrigIP, uid);
		updateDatabase(_mainClass+":"+irrigIP+".timerStat", "Running");
		hibernate(2500);
		keepRunning = false;
		return;
	      }
	    }
	    hibernate();
	  }
	}
    }


//=====================================================================================================

    protected class IrrigPollingThread extends Thread {
        private String _className = getClass().getName()+"_"+getName()+"_"+getId();
        private Irrigator irr;
        private String irrigIP, uid, irrKey;
	boolean error = false, success = true, settingTimer = false, _connecting = false;
	boolean _isRunning = true;
	protected long _irrigHeart = 0;

        public IrrigPollingThread(Irrigator irr, String irrigIP, String uid) {
	  _connecting = false;
	  _irrigHeart = System.currentTimeMillis()/1000;
          this.irr = irr;
          this.irrigIP = irrigIP;
          this.uid = uid;
	  irrKey = irrigIP+"::"+uid;
          System.out.println(_className+":> Created new Irrig Polling Thread at "+ctime()+" for Irrigator "+irrigIP+"; uid "+uid);
	  setName("IrrigPollingThread: "+irrKey);
        }

        public void run() {
          int retVal = 0;
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
	      System.out.println(_className+":run> Irrigator is busy - presumably setting timer.  Sleeping at "+ctime());
	      hibernate(sleepTime);
	      continue;
	    }
	    if (!irr.isConnected()) {
	      if (!error) {
		error = true;
		System.out.println(_className+":run1> Error polling irrigator "+irrigIP+" at "+ctime());
		if (emails.size() > 0) sendEmail("The Irrigator at IP: "+irrigIP+" disconnected at "+ctime()+".");
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
		updateIrrigatorLog("Irrigation turned RECONNECTED", irrigIP);
		if (emails.size() > 0) sendEmail("Successfully reconnected to "+irrigIP+" at "+ctime());
		error = false;
	      } else {
		//continue if unsuccessful so it doesn't try polling
		hibernate(reconnectTime);
		continue;
	      }
	    }
	    success = pollIrrigator(irrigIP, uid);
	    if (!success) {
	      //if first occurrance of error, send email if requested
	      if (!error && emails.size() > 0) sendEmail("The Irrigator at IP: "+irrigIP+" disconnected at "+ctime()+".");
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
	      //send email if requested
	      if (emails.size() > 0) sendEmail("The irrigator at IP: "+irrigIP+" reconnected at "+ctime()+".");
	    }
	    if (irrigDatabase.containsKey(irrKey)) {
	      success = updateIrrig(irrKey);
	      if (!success) {
		//if first occurrance of error, send email if requested
		if (!error && emails.size() > 0) sendEmail("The Irrigator at IP: "+irrigIP+" disconnected at "+ctime()+".");
		error = true;
		System.out.println(_className+":run> Error updating irrigator "+irrigIP+" at "+ctime());
		hibernate(sleepTime);
		continue;
	      }
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

} //end of class BmpPlcAgent



