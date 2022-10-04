package Weewx; 
/**
 * Title:        WeewxAgent
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

//=====================================================================================================

public class WeewxAgent extends UFMMTThreadedAgent { 
//select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime')) as dt, count(*), min(outTemp), max(outTemp), sum(rain), avg(radiation) from archive where d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime')) group by strftime('%Y-%m-%d %H:00:00', d);
//Notes - and dt > datetime(lastdate), lastdate = current time - 24 hours on startup but this is configurable with command line option.  Must have count = 12.
//Keep ArrayDeque of last 24 (+) hours always.  If > 24 hours and has been sent (< lastSendDate), pop out.
//Separately keep ArrayList/Vector of unsent datapoints
    public static final
	String rcsID = "$Name:  $ $Id: WeewxAgent.java,v 1.6 2011/01/24 19:36:31 warner Exp $";

    protected String _mainClass = getClass().getName();
    protected boolean _sqlConnected = false, _initialized = false, _wthConnected=false, _config = false, _firstQuery = true, _ready = true, _loggedIn = false;
    protected String dbName = "archive";
    protected String dbUrl = "jdbc:sqlite:/var/lib/weewx/weewx.sdb";
    protected String sqlUser = "", sqlPass = "";
    protected String wthHost = "www.bmptoolbox.org";
    protected int wthPort = 57003;
    protected String dbClass = "org.sqlite.JDBC";
    protected Connection _dbCon; //database connection
    protected Socket wthSocket;
    protected long sid = -1;
    protected int uid = -1, wsid = -1, hours = 24;
    protected String lastDateTime = "";
    protected ArrayDeque<String> last24;
    protected ArrayList<String> unsentWeather;
    protected String[] hourlyWeather;
    protected WeatherThread _thread;
    protected PingThread _ping;
    private String username = "", password = "", wsname = "";
    private Statement stmt;
    private ResultSet rs;
    

//----------------------------------------------------------------------------------------

    public WeewxAgent( int serverPort, String[] args )
    {
	super(serverPort, args);
	options(args);
	last24 = new ArrayDeque();
	unsentWeather = new ArrayList();
	hourlyWeather = new String[24];
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      super.options(args);
      /* Handle any options specific to this agent here */
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().indexOf("-dbpath") != -1) {
          if (args.length > j+1) dbUrl = "jdbc:sqlite:"+args[j+1]+"?autoReconnect=true";
        } else if (args[j].toLowerCase().indexOf("-dbname") != -1) {
          if (args.length > j+1) dbName = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-user") != -1) {
	  if (args.length > j+1) username = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-host") != -1) {
          if (args.length > j+1) wthHost = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-port") != -1) {
          if (args.length > j+1) wthPort = Integer.parseInt(args[j+1]);
	} else if (args[j].toLowerCase().indexOf("-wsid") != -1) {
          if (args.length > j+1) wsid = Integer.parseInt(args[j+1]);
	} else if (args[j].toLowerCase().indexOf("-wsname") != -1) {
	  if (args.length > j+1) wsname = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-config") != -1) {
	  _config = true;
	} else if (args[j].toLowerCase().indexOf("-hours") != -1) {
	  if (args.length > j+1) hours = Integer.parseInt(args[j+1]);
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

//-----------------------------------------------------------------------------------------------------

    protected void _startPingThread() {
      /* Create and start a ping thread to keep the command socket alive */
      System.out.println(_mainClass + "::_startPingThread> starting Ping thread to keep command socket alive...");
      if (_ping != null) _ping.shutdown();
      _ping = new PingThread();
      _ping.start();
    }

//-----------------------------------------------------------------------------------------------------

    protected void _startWeatherThread() {
      //Create and start a weather thread 
      System.out.println(_mainClass + "::_startWeatherThread> starting Weather thread...");
      if (_thread != null) _thread.shutdown();
      _thread = new WeatherThread();
      _thread.start();
    }

//----------------------------------------------------------------------------------------
    /** MYSQL Specific Helper Methods */

//-----------------------------------------------------------------------------------------------------

    /** Setup database */
    protected void setDefaults() {
      super.setDefaults();
      synchronized(database) {
        /* Database records specific to this agent */
        addMMTRecord(_mainClass+":"+"username", UFRecord.TYPE_STRING, username);
        addMMTRecord(_mainClass+":"+"password", UFRecord.TYPE_STRING, password);
        addMMTRecord(_mainClass+":"+"wsid", UFRecord.TYPE_INT, String.valueOf(wsid));
        addMMTRecord(_mainClass+":"+"wsname", UFRecord.TYPE_STRING, wsname);
      }
      return;
    }

//-----------------------------------------------------------------------------------------------------

    protected void saveDefaults() {
      //save username and password
      try {
        ArrayList<String> currentValues = new ArrayList();
        currentValues.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        currentValues.add("<database>");
        String xmlFile = installDir+"/etc/databaseStartupValues.xml";
        String[] recsToSave = { "username", "password", "wsid", "wsname" };

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
                  //this is default section for weewx agent 
                  defaultsExist = true;
                  //now add in recs for weewx agent 
                  synchronized(database) {
                    for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
                      key = (String)i.next();
                      for (int l = 0; l < recsToSave.length; l++) {
                        if (key.indexOf(recsToSave[l]) != -1) {
                          //write this record
                          currentValues.add("    <record>");
                          currentValues.add("      <name>"+recsToSave[l]+"</name>");
                          currentValues.add("      <value>"+database.get(key).getValue()+"</value>");
                          currentValues.add("    </record>");
                          break;
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
          //add in recs for weewx agent 
          synchronized(database) {
            for (Iterator i = database.keySet().iterator(); i.hasNext(); ) {
              key = (String)i.next();
              for (int l = 0; l < recsToSave.length; l++) {
                if (key.indexOf(recsToSave[l]) != -1) {
                  //write this record
                  currentValues.add("    <record>");
                  currentValues.add("      <name>"+recsToSave[l]+"</name>");
                  currentValues.add("      <value>"+database.get(key).getValue()+"</value>");
                  currentValues.add("    </record>");
                  break;
                }
              }
            }
          }
          currentValues.add("  </agent>");
        }
        currentValues.add("</database>");

        PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/databaseStartupValues.xml"));
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
    /** Connect to device and exec agent
      * Subclasses should override this class with options specific to
      * connecting to the specific device.
      */
    protected void init() {
      /* set up database */
      setDefaults();
      readXMLDefaults(); //read XML defaults
      checkPassword(); //check for password if not encrypted in XML file
      _initialized = false;
      updateDatabase(_mainClass+":status", "INIT");
      /* Do not proceed until connected to exec agent */
      if (_simMode) {
        System.out.println(_mainClass+"::init> Simulating connection to database...");
      } else {
	connectToWeatherAgent();
        while (!_sqlConnected) {
	  //connect to sqlite database
	  _sqlConnected = connectToSql();
          if (_sqlConnected) {
            System.out.println(_mainClass+"::init> Successfully connected to sqlite database"); 
          } else {
            System.out.println(_mainClass+"::init> Error!  Unable to connect to sqlite database"); 
          }
          hibernate();
        }
      }
      _startWeatherThread();
      _startPingThread();
      _initialized = true;
      updateDatabase(_mainClass+":status", "IDLE");
    }

//-----------------------------------------------------------------------------------------------------
    public void checkPassword() {
      if (username.equals("") || username.equals(getMMTValue(_mainClass+":username"))) {
	//if user is not given or matches defaults file, use password if found in defaults file
        if (!_config && !getMMTValue(_mainClass+":password").equals("")) return;
      }
      //else prompt
      try {
        Console console = System.console();
	System.out.println(console);
        if (_config) {
          console.printf("Please enter your username: ");
	  username = console.readLine().trim();
        }
        if (!username.equals("")) updateDatabase(_mainClass+":username", username); //update database
        console.printf("Please enter your password: ");
        char[] passwordChars = console.readPassword();
	String passwd = AeSimpleSHA1.SHA1(new String(passwordChars));
	updateDatabase(_mainClass+":password", passwd);
      } catch (Exception e) {
        System.out.println(_mainClass+"::action> SHA1 Error: "+e.toString());
        System.out.println(_mainClass+"::action> Password can't be sent!");
      } 
    }

//-----------------------------------------------------------------------------------------------------

    public boolean connectToWeatherAgent() {
      _ready = false; //don't ping while connecting
      if (getMMTValue(_mainClass+":password").equals("")) return false;
      String loginString = "LOGIN::"+getMMTValue(_mainClass+":username")+" "+getMMTValue(_mainClass+":password");
      System.out.println(_mainClass+"::connectToWeatherAgent | " + ctime() + "> Attempting to login to weather agent with login "+loginString); 
      Vector <String> commandVec = new Vector();
      commandVec.add(loginString);
      try {
        System.out.println(_mainClass+"::connectToWeatherAgent | " + ctime() + "> Trying to connect to weather agent on "+wthHost+", port = "+wthPort);
        wthSocket = new Socket(wthHost, wthPort);
        wthSocket.setSoTimeout(_timeout); //timeout for greetings
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
        greet.sendTo(wthSocket);
        UFProtocol ufpr = UFProtocol.createFrom(wthSocket);
        wthSocket.setSoTimeout(0); //infinite timeout
        if (ufpr == null) {
          System.out.println(_mainClass+"::connectToWeatherAgent | " + ctime() + "> received null object!  Closing socket!");
	  wthSocket.close();
	  _wthConnected = false;
          return false;
        } else {
          String request = ufpr.name().toLowerCase();
          if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
            System.out.println(_mainClass+"::connectToServer> connection established: "+request);
          } else {
            System.out.println(_mainClass+"::connectToWeatherAgent | " + ctime() + "> received "+request+".  Closing socket!");
            wthSocket.close();
            _wthConnected = false;
            return false;
          }
        }

        //login to weather agent
        wthSocket.setSoTimeout(_timeout);
        UFStrings req = new UFStrings(_mainClass+": actionRequest", commandVec);
        int nbytes = req.sendTo(wthSocket);
        if( nbytes <= 0 ) {
          System.out.println( _mainClass + "::connectToWeatherAgent> zero bytes sent.");
          wthSocket.setSoTimeout(0);
          _wthConnected = false;
          return false;
        }
        if (_verbose) System.out.println(_mainClass+"::connectToWeatherAgent> Sent "+nbytes+" bytes at "+ctime());
        UFStrings reply;
        synchronized(wthSocket) {
          reply = (UFStrings)UFProtocol.createFrom(wthSocket);
        }
        wthSocket.setSoTimeout(0);
        if (reply == null) {
          System.out.println(_mainClass+"::connectToWeatherAgent> Error: received null response from weatherAgent at "+ctime());
          _wthConnected = false;
          return false;
        }
        System.out.println(_mainClass+"::connectToWeatherAgent> Received response "+reply.name()+" from weatherAgent at "+ctime());
        if (reply.name().toLowerCase().indexOf("error") != -1) {
          if (reply.stringAt(0).equals("Not logged in.")) {
            //try to restore session 
            String restoreString = getRestoreString()+" "+sid;
            if (restoreString != null) {
              commandVec.add(0, restoreString);
              req = new UFStrings(_mainClass+": actionRequest", commandVec);
              System.out.println(_mainClass+"::connectToWeatherAgent> Attempting restore session");
            }
            wthSocket.setSoTimeout(_timeout);
            nbytes = req.sendTo(wthSocket);
            if( nbytes <= 0 ) {
              System.out.println( _mainClass + "::connectToWeatherAgent2> zero bytes sent.");
              wthSocket.setSoTimeout(0);
              _wthConnected = false;
              return false;
            }
            if (_verbose) System.out.println(_mainClass+"::connectToWeatherAgent2> Sent "+nbytes+" bytes at "+ctime());
            synchronized(wthSocket) {
              reply = (UFStrings)UFProtocol.createFrom(wthSocket);
            }
            wthSocket.setSoTimeout(0);
            if (reply == null) {
              System.out.println(_mainClass+"::connectToWeatherAgent2> Error: received null response from weatherAgent at "+ctime());
              _wthConnected = false;
              return false;
            }
          }
        }
        boolean success = processWeatherResponse(reply);
	if (!success) {
          System.out.println(_mainClass+"::connectToWeatherAgent> Error: could not login to user account "+getMMTValue(_mainClass+":user")+" and wsid "+getMMTValue(_mainClass+":wsid")+" or wsname "+getMMTValue(_mainClass+":wsname")); 
	  _wthConnected = false;
          return false;
        }
	saveDefaults(); //save username and password here
        _loggedIn = true;
        _ready = true; //allow pings
        return true;
      } catch(Exception ioe) {
        System.out.println(_mainClass+"::connectToWeatherAgent | " + ctime() + "> "+ioe.toString());
	try {
          wthSocket.close();
	} catch(Exception e) {}
        _wthConnected = false;
        return false;
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected String getRestoreString() {
      String loginString = "RESTORE_SESSION::"+getMMTValue(_mainClass+":username")+" "+getMMTValue(_mainClass+":password");
      return loginString;
    }

//-----------------------------------------------------------------------------------------------------

    public boolean processWeatherResponse(UFStrings reply) {
      boolean success = true;
      System.out.println(_mainClass+"::processWeatherResponse> Received response: "+reply.toString());
      if (reply.name().toLowerCase().indexOf("error") != -1) {
	System.out.println(_mainClass+"::processWeatherResponse> ERROR at "+ctime());
	success = false; //we received an error 
        _wthConnected = false;
      }
      for (int j = 0; j < reply.numVals(); j++) {
        //Check for errors
        if (reply.stringAt(j).toLowerCase().indexOf("error") != -1) success = false; 
	System.out.println(_mainClass+"::processWeatherResponse> "+reply.stringAt(j));
      }
      if (!success) _wthConnected = false;
      String firstReply = reply.stringAt(0);
      if (isLogin(firstReply)) {
        _wthConnected = true; 
        try {
          uid = Integer.parseInt(getReplyToken(firstReply, 2));
          sid = Long.parseLong(getReplyToken(firstReply, 3));
        } catch(NumberFormatException nfe) {
          System.out.println(_mainClass+"::processWeatherResponse> Error receiving login info: "+firstReply);
	  _wthConnected = false;
          uid = -1;
          sid = -1;
          return false;
        }
      }
      if (_config && isWeatherList(firstReply)) {
	for (int j = 1; j < reply.numVals(); j++) {
          String weatherString = reply.stringAt(j);
          String[] info = weatherString.split("::");
	  System.out.println("Weather station: "+info[0]+"; id = "+info[1]);
	}
        Console console = System.console();
        console.printf("Please enter a weather station name or id: ");
        String ws = console.readLine().trim();
	for (int j = 1; j < reply.numVals(); j++) {
          String weatherString = reply.stringAt(j);
          String[] info = weatherString.split("::");
	  if (ws.equals(info[0]) || ws.equals(info[1])) {
	    System.out.println("Configuring using weather station "+info[0]+"; id = "+info[1]+"...");
	    updateDatabase(_mainClass+":wsname", info[0]);
	    updateDatabase(_mainClass+":wsid", info[1]);
	    saveDefaults();
	    System.out.println("...DONE.  Please restart ufWeewxAgent.");
	    System.exit(0);
	  }
	}
	System.out.println("ERROR: Could not find weather station "+ws+"...");
	System.out.println("...DONE.  Please try again.");
	System.exit(0);
      }
      if (isWeatherList(firstReply)) {
	success = false;
	String currwsname;
	int currwsid = -1;
        for (int j = 1; j < reply.numVals(); j++) {
          String weatherString = reply.stringAt(j);
          String[] info = weatherString.split("::");
          currwsname = info[0];
          try {
            currwsid = Integer.parseInt(info[1]);
          } catch (NumberFormatException nfe) {
            currwsid = -1;
	    return false;
          }
	  if ((currwsid == getMMTIntValue(_mainClass+":wsid") && currwsid != -1) || currwsname.equals(getMMTValue(_mainClass+":wsname"))) {
	    wsid = currwsid;
	    wsname = currwsname;
	    updateDatabase(_mainClass+":wsid", wsid);
	    updateDatabase(_mainClass+":wsname", wsname);
	    System.out.println(_mainClass+"::processWeatherResponse> Successfully connected as weather station "+wsname+", wsid = "+wsid);
	    success = true;
	    break;
	  }
        }
      }
      return success;
    }

  public boolean isLogin(String loginString) {
    if (!getReplyToken(loginString, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(loginString, 1).toLowerCase().equals("loggedin")) return true;
    return false;
  }

  public boolean isWeatherList(String response) {
    if (!getReplyToken(response, 0).toLowerCase().equals("success")) return false;
    if (getReplyToken(response, 1).toLowerCase().equals("loggedin") || getReplyToken(response, 1).toLowerCase().equals("refreshed")) return true;
    return false;
  }


//-----------------------------------------------------------------------------------------------------

    protected boolean connectToSql() {
      boolean retVal = false;
      try {
	Class.forName(dbClass);
	_dbCon = DriverManager.getConnection(dbUrl);
        _dbCon.close(); //close connection, just make sure we can connect
        retVal = true;
      } catch(ClassNotFoundException e) {
	e.printStackTrace();
      } catch(SQLException e) {
	e.printStackTrace();
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
        if (getCmdName(req,j).equals("GET_WEATHER")) {
	  //GET_WEATHER::uid wsid
          String param = getCmdParam(req,j);
          int req_uid = -1;
	  int req_wsid = -2;
          try {
            req_uid = Integer.parseInt(getReplyToken(param, 0));
            req_wsid = Integer.parseInt(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Invalid uid or wsid."); 
            reply = new UFStrings("ERROR", "Invalid uid or wsid.");
            success = false;
            break;
          }
	  if (req_uid == uid && req_wsid == -1) {
	    System.out.println(_mainClass+"::action> Warning: no wsid given!");
	  } else if (req_uid != uid || req_wsid != wsid) {
	    System.out.println(_mainClass+"::action> Error: uid or wsid does not match!");
	    reply = new UFStrings("ERROR", "uid or wsid does not match!");
	    success = false;
	    break;
	  }

	  //parse dates from last24
	  boolean uselast24 = false;
          SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          try {
            java.util.Date firstDate = sqldf.parse(last24.getFirst());
	    java.util.Date lastDate = sqldf.parse(last24.getLast());
	    long diff = lastDate.getTime()-firstDate.getTime();
	    long diff_hr = diff/3600/1000; 
	    if (diff_hr == 23) uselast24 = true;
	  } catch(Exception e) {
	    System.out.println(_mainClass+"::action> Error: Could not parse dates in last24!");
	  }
	  if (uselast24) {
	    //use hourlyWeather anyway so that data is in hour order already
            for (int i = 0; i < hourlyWeather.length; i++) {
              responses.add("WEATHER::"+hourlyWeather[i]);
              System.out.println("\tDatapoint "+i+": "+hourlyWeather[i]);
            }
/*
	    for (Iterator i = last24.iterator(); i.hasNext();) {
	      responses.add("WEATHER::"+i.next());
	      System.out.println("\tDatapoint "+responses.lastElement());
	    }
*/
	    break;
	  }
	  boolean has24 = true;
	  for (int i = 0; i < hourlyWeather.length; i++) {
	    if (hourlyWeather[i] == null) {
	      System.out.println(_mainClass+"::action> Error: Do not have weather for hour "+i);
	      has24 = false;
	    }
	  }
	  if (!has24) {
	    reply = new UFStrings("ERROR", "Missing weather!");
	    success = false;
	    break;
	  }
	  System.out.println(_mainClass+"::action> Warning: Using previous weather data for one or more hourly datapoints!");
	  for (int i = 0; i < hourlyWeather.length; i++) {
	    responses.add("WEATHER::"+hourlyWeather[i]);
	    System.out.println("\tDatapoint "+hourlyWeather[i]);
	  }
        } else if (getCmdName(req,j).equals("GET_WEATHER_ON_DATE")) {
          //GET_WEATHER_ON_DATE::uid wsid date
          String param = getCmdParam(req,j);
          int req_uid = -1;
          int req_wsid = -2;
	  String date = getReplyToken(param, 2);
          try {
            req_uid = Integer.parseInt(getReplyToken(param, 0));
            req_wsid = Integer.parseInt(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Invalid uid or wsid.");
            reply = new UFStrings("ERROR", "Invalid uid or wsid.");
            success = false;
            break;
          }
          if (req_uid == uid && req_wsid == -1) {
            System.out.println(_mainClass+"::action> Warning: no wsid given!");
          } else if (req_uid != uid || req_wsid != wsid) {
            System.out.println(_mainClass+"::action> Error: uid or wsid does not match!");
            reply = new UFStrings("ERROR", "uid or wsid does not match!");
            success = false;
            break;
          }
	  ArrayDeque<String> weatherVals = queryWeatherOnDate(date); 
	  if (weatherVals.size() != 24) {
            System.out.println(_mainClass+"::action> Error: Only found "+weatherVals.size()+" hourly weather datapoints on "+date);
            reply = new UFStrings("ERROR", "Only found "+weatherVals.size()+" hourly weather datapoints on "+date);
	    success = false;
	    break;
	  }
          for (Iterator i = weatherVals.iterator(); i.hasNext();) {
            responses.add("WEATHER::"+i.next());
            System.out.println("\tDatapoint "+responses.lastElement());
          }
        } else if (getCmdName(req,j).equals("GET_LF_WEATHER")) {
          //GET_LF_WEATHER::uid wsid date time
          String param = getCmdParam(req,j);
          int req_uid = -1;
          int req_wsid = -2;
          String datetime = getReplyToken(param, 2)+" "+getReplyToken(param, 3);
          try {
            req_uid = Integer.parseInt(getReplyToken(param, 0));
            req_wsid = Integer.parseInt(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Invalid uid or wsid.");
            reply = new UFStrings("ERROR", "Invalid uid or wsid.");
            success = false;
            break;
          }
          if (req_uid == uid && req_wsid == -1) {
            System.out.println(_mainClass+"::action> Warning: no wsid given!");
          } else if (req_uid != uid || req_wsid != wsid) {
            System.out.println(_mainClass+"::action> Error: uid or wsid does not match!");
            reply = new UFStrings("ERROR", "uid or wsid does not match!");
            success = false;
            break;
          }
          ArrayDeque<String> weatherVals = queryLFWeather(datetime);
          if (weatherVals.size() != 24) {
            System.out.println(_mainClass+"::action> Error: Only found "+weatherVals.size()+" hourly weather datapoints for "+datetime);
            reply = new UFStrings("ERROR", "Only found "+weatherVals.size()+" hourly weather datapoints for "+datetime);
            success = false;
            break;
          }
          for (Iterator i = weatherVals.iterator(); i.hasNext();) {
            responses.add("WEATHER::"+i.next());
            System.out.println("\tDatapoint "+responses.lastElement());
          }
        } else if (getCmdName(req,j).equals("GET_WEEKLY_RAIN")) {
          //GET_WEEKLY_RAIN::uid wsid
          String param = getCmdParam(req,j);
          int req_uid = -1;
          int req_wsid = -2;
	  double weekly_rain_in = -1;
          try {
            req_uid = Integer.parseInt(getReplyToken(param, 0));
            req_wsid = Integer.parseInt(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Invalid uid or wsid.");
            reply = new UFStrings("ERROR", "Invalid uid or wsid.");
            success = false;
            break;
          }
          if (req_uid == uid && req_wsid == -1) {
            System.out.println(_mainClass+"::action> Warning: no wsid given!");
          } else if (req_uid != uid || req_wsid != wsid) {
            System.out.println(_mainClass+"::action> Error: uid or wsid does not match!");
            reply = new UFStrings("ERROR", "uid or wsid does not match!");
            success = false;
            break;
          }

	  String query = "select sum(rain) as weekly_rain from archive where dateTime > strftime('%s', 'now', '-168 hours');";
	  //String query = "select sum(rain) as weekly_rain from archive where datetime(dateTime, 'unixepoch', 'localtime') >= datetime('now', 'localtime', '-168 hours');";
          String result = executeSingleColumnSQLQuery(query, "weekly_rain");
          success = false;
          if (result != null) {
            try {
              weekly_rain_in = Double.parseDouble(result);
	      success = true;
	    } catch(Exception ex) {
              ex.printStackTrace();
              success = false;
	    } 
	  }
	  if (!success) {
	    System.out.println(_mainClass+"::action> Error: Could not find weekly rain total for wsid = "+req_wsid+"!");
	    reply = new UFStrings("ERROR", "Could not find weekly rain total for wsid = "+req_wsid+"!");
	    break;
	  }
          responses.add("WEEKLY_RAIN::"+weekly_rain_in);
        } else if (getCmdName(req,j).equals("GET_RAIN_SINCE")) {
          //GET_RAIN_SINCE::uid wsid date time
          String param = getCmdParam(req,j);
          int req_uid = -1;
          int req_wsid = -2;
          String date = getReplyToken(param, 2)+" "+getReplyToken(param, 3);
          double rain_in = -1;
          try {
            req_uid = Integer.parseInt(getReplyToken(param, 0));
            req_wsid = Integer.parseInt(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Invalid uid or wsid.");
            reply = new UFStrings("ERROR", "Invalid uid or wsid.");
            success = false;
            break;
          }
          if (req_uid == uid && req_wsid == -1) {
	    //no wsid => warning only
            System.out.println(_mainClass+"::action> Warning: no wsid given!");
          } else if (req_uid != uid || req_wsid != wsid) {
            System.out.println(_mainClass+"::action> Error: uid or wsid does not match!");
            reply = new UFStrings("ERROR", "uid or wsid does not match!");
            success = false;
            break;
          }

          //String query = "select sum(rain) as rain_in from archive where datetime(dateTime, 'unixepoch', 'localtime') >= datetime('"+date+"');";
	  String query = "select sum(rain) as rain_in from archive where dateTime >= strftime('%s', '"+date+"')+(strftime('%s', 'now')-strftime('%s', 'now', 'localtime'));";
          String result = executeSingleColumnSQLQuery(query, "rain_in");
          success = false;
          if (result != null) {
            try {
              rain_in = Double.parseDouble(result);
              success = true; 
            } catch(Exception ex) {
              ex.printStackTrace();
              success = false;
            }
          }
          if (!success) {
            System.out.println(_mainClass+"::action> Error: Could not find rain total for wsid = "+req_wsid+"!");
            reply = new UFStrings("ERROR", "Could not find rain total for wsid = "+req_wsid+"!");
            break;
          }
          responses.add("RAIN::"+rain_in);
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

    protected int checkConnections(int failCount) {
      if (!_wthConnected) {
	boolean successWth = connectToWeatherAgent(); 
      }
      if (_wthConnected) failCount = 0; else failCount++;
      return failCount;
    } 

//-----------------------------------------------------------------------------------------------------

    public void updateWeather() {
      boolean hasWeather = queryWeather();
      if (!hasWeather) return; 
      if (!_loggedIn) return;
      if (unsentWeather.size() == 0) {
        System.out.println(_mainClass+"::updateWeather> "+ctime()+": WARNING: no new weather datapoints!");
        return;
      }
      System.out.println(_mainClass+"::updateWeather> "+ctime()+": sending "+unsentWeather.size()+" new hourly weather datapoints.");
      String currLine;
      Vector <String> commandVec = new Vector();
      for (int j = 0; j < unsentWeather.size(); j++) {
        currLine = "ADD_WEATHER::"+uid+" "+sid+" "+wsid+" "+unsentWeather.get(j);
	commandVec.add(currLine);
        System.out.println("\t\t"+currLine);
      }
      boolean success = apply(commandVec);
      if (success) {
	unsentWeather.clear();
      } else System.out.println(_mainClass+"::updateWeather> "+ctime()+": ERROR sending hourly weather datapoints.  Will retry in 2 minutes.");
    }


//-----------------------------------------------------------------------------------------------------

    public boolean queryWeather() {
      //d = actual datetime, dt = rouned up to next hour, ct = count.  Group by dt to take max/min temp for hour and sum rain and avg radiation
      //grab datapoints before the truncated current hour having ct = 12
      //if first query, grab datapoints after current time - n hours, otherwise after last hour successfully obtained
      if (unsentWeather.size() > 0 && _wthConnected) return true; //return true here if previous attempt unsuccessful (unless problem is connection to weather agent)
      String subquery = "";
      if (_firstQuery) {
	subquery = "dateTime > strftime('%s', 'now', '-"+hours+" hours', '-55 minute') and dt > datetime('now', 'localtime', '-"+hours+" hours')";
	//subquery = "dt > datetime('now', 'localtime', '-"+hours+" hours')";
      } else {
	subquery = "dateTime > strftime('%s', '"+lastDateTime+"') and dt > datetime('"+lastDateTime+"')";
	//subquery = "dt > datetime('"+lastDateTime+"')";
      }
      String query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, count(*) as ct from archive where "+subquery+";";
      boolean success = false;
      String result = executeSingleColumnSQLQuery(query, "ct");
      if (result != null) {
	success = true;
        try {
	  int count = Integer.parseInt(result);
	  if (count < 12) success = false;
	} catch(Exception ex) {
	  ex.printStackTrace();
	  success = false;
	}
      }
      if (!success) return false;

      //if we get here, we expect to have new datapoints
      query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where "+subquery+" and d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) group by dt having ct = 12;";
      //query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) and "+subquery+" group by dt having ct = 12;";
      System.out.println(_mainClass+"::queryWeather> "+ctime()+": sending query "+query);

      ArrayDeque<String> weatherVals = executeWeatherQuery(query, true);
      success = weatherVals.size() > 0;
      return success;
    }

//-----------------------------------------------------------------------------------------------------

    public ArrayDeque<String> queryWeatherOnDate(String date) {
      //d = actual datetime, dt = rouned up to next hour, ct = count.  Group by dt to take max/min temp for hour and sum rain and avg radiation
      //grab datapoints before the truncated current hour having ct = 12
      //String subquery = "date(dt) = date('"+date+"')";
      String subquery = "dateTime > strftime('%s', '"+date+"', '-1 hour') and date(dt) = date('"+date+"')"; 
      //String query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) and "+subquery+" group by dt having ct = 12;";
      String query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where "+subquery+" and d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) group by dt having ct = 12;";
      System.out.println(_mainClass+"::queryWeatherOnDate> "+ctime()+": sending query "+query);
      ArrayDeque<String> weatherVals = executeWeatherQuery(query, false);
      return weatherVals; 
    }

//-----------------------------------------------------------------------------------------------------

    public ArrayDeque<String> queryLFWeather(String datetime) {
      //d = actual datetime, dt = rouned up to next hour, ct = count.  Group by dt to take max/min temp for hour and sum rain and avg radiation
      //grab datapoints before the truncated current hour having ct = 12
      //String subquery = "dt <= datetime('"+datetime+"') AND dt > datetime('"+datetime+"', '-24 hours')";
      String subquery = "dateTime > strftime('%s', '"+datetime+"', '-25 hours') and dt <= datetime('"+datetime+"') AND dt > datetime('"+datetime+"', '-24 hours')";
      //String query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) and "+subquery+" group by dt having ct = 12;";
      String query = "select datetime(dateTime, 'unixepoch', 'localtime') as d, strftime('%Y-%m-%d %H:00:00', datetime(dateTime, 'unixepoch', 'localtime', '+55 minute')) as dt, strftime('%H', datetime(dateTime, 'unixepoch', 'localtime')) as hr, count(*) as ct, min(outTemp) as mint, max(outTemp) as maxt, sum(rain) as sumrain, avg(radiation) as avgrad from archive where "+subquery+" and d < strftime('%Y-%m-%d %H:00:00', datetime('now', 'localtime', '+1 hour')) group by dt having ct = 12;";
      System.out.println(_mainClass+"::queryLFWeather> "+ctime()+": sending query "+query);
      ArrayDeque<String> weatherVals = executeWeatherQuery(query, false);
      return weatherVals;
    }

//-----------------------------------------------------------------------------------------------------

    public boolean apply(Vector<String> commandVec) {
      _ready = false; //don't allow pings while sending weather
      //if apply fails, _ready remains false until Ancillary thread reconnects
      boolean success = true;
      UFStrings req;
      if (commandVec.size() == 0) return false;
      if (wthSocket == null) return false;
      System.out.println(_mainClass+"::apply> Sending "+commandVec.size()+" command action request to weatherAgent.");
      try {
        wthSocket.setSoTimeout(_timeout);
        req = new UFStrings(_mainClass+": actionRequest", commandVec);
        int nbytes = req.sendTo(wthSocket);
        if( nbytes <= 0 ) {
          System.out.println( _mainClass + "::apply> zero bytes sent.");
          wthSocket.setSoTimeout(0);
          _wthConnected = false;
          return false;
        }
        if (_verbose) System.out.println(_mainClass+"::apply> Sent "+nbytes+" bytes at "+ctime());
        UFStrings reply;
        synchronized(wthSocket) {
          reply = (UFStrings)UFProtocol.createFrom(wthSocket);
        }
        wthSocket.setSoTimeout(0);
        if (reply == null) {
          System.out.println(_mainClass+"::apply> Error: received null response from weatherAgent at "+ctime());
          _wthConnected = false;
          return false;
        }
        System.out.println(_mainClass+"::apply> Received response "+reply.name()+" from weatherAgent at "+ctime());
        success = processWeatherResponse(reply);
      } catch (IOException ioe) {
        System.err.println(_mainClass+"::apply> "+ioe.toString());
        ioe.printStackTrace();
        _wthConnected = false;
        return false;
      }
      _ready = true;
      return success;
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized String executeSingleColumnSQLQuery(String query, String column) {
      String retVal = null;
      long t = System.currentTimeMillis();
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
	    PreparedStatement stmt = dbCon.prepareStatement(query);
	    ResultSet rs = stmt.executeQuery(); ) {
	if (rs == null) {
	  System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Received null ResultSet for query: "+query);
	} else {
	  if (rs.next()) retVal = rs.getString(column);
	  System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Successfully executed query ("+(System.currentTimeMillis()-t)+" ms): "+query);
	  return retVal;
	}
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
      System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Waiting 5 sec and retrying...");
      hibernate(5000);

      t = System.currentTimeMillis();
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = dbCon.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(); ) {
        if (rs == null) {
          System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Received null ResultSet for query: "+query);
	} else {
          if (rs.next()) retVal = rs.getString(column);
          System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Successfully executed query ("+(System.currentTimeMillis()-t)+" ms): "+query);
          return retVal;
	}
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::executeSingleColumnSQLQuery | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
      return retVal;
    }

    protected synchronized ArrayDeque<String> executeWeatherQuery(String query, boolean current) {
      ArrayDeque<String> weatherVals = new ArrayDeque();
      long t = System.currentTimeMillis();
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = dbCon.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(); ) {
        if (rs == null) {
          System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Received null ResultSet for query: "+query);
        } else {
          while (rs.next()) {
            String wthString = rs.getString("dt")+" "+rs.getFloat("avgrad")+" "+rs.getFloat("maxt")+" "+rs.getFloat("mint")+" "+rs.getFloat("sumrain");
            //append wthString to lists
	    weatherVals.add(wthString);
            if (current) {
              //update lastDateTime and _firstQuery
              lastDateTime = rs.getString("dt");
              _firstQuery = false;
	      last24.add(wthString);
              while (last24.size() > 24) last24.remove(); //last24 should only contain up to 24 datapoints
	      unsentWeather.add(wthString);
              int hour = rs.getInt("hr");
              hourlyWeather[hour] = wthString;
              System.out.println("hour "+hour+": "+wthString);
            } else System.out.println(wthString);
	  }
	  System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Successfully executed query ("+(System.currentTimeMillis()-t)+" ms): "+query);
	  return weatherVals;
	}
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
      System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Waiting 5 sec and retrying...");
      hibernate(5000);

      weatherVals.clear();
      t = System.currentTimeMillis();
      try ( Connection dbCon = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = dbCon.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(); ) {
        if (rs == null) {
          System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Received null ResultSet for query: "+query);
        } else {
          while (rs.next()) {
            String wthString = rs.getString("dt")+" "+rs.getFloat("avgrad")+" "+rs.getFloat("maxt")+" "+rs.getFloat("mint")+" "+rs.getFloat("sumrain");
            //append wthString to lists
            weatherVals.add(wthString);
            if (current) {
              //update lastDateTime and _firstQuery
              lastDateTime = rs.getString("dt");
              _firstQuery = false;
              while (weatherVals.size() > 24) weatherVals.remove(); //last24 should only contain up to 24 datapoints
              unsentWeather.add(wthString);
              int hour = rs.getInt("hr");
              hourlyWeather[hour] = wthString;
              System.out.println("hour "+hour+": "+wthString);
            } else System.out.println(wthString);
          }
          System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Successfully executed query ("+(System.currentTimeMillis()-t)+" ms): "+query);
          return weatherVals;
        }
      } catch (SQLException ex) {
        System.out.println(_mainClass+"::executeWeatherQuery | "+ctime()+"> Error executing query: "+query);
        ex.printStackTrace();
      }
      return weatherVals;
    }

//-----------------------------------------------------------------------------------------------------

    /* This helper method should be overridden to return the proper subclass
     * UFMMTClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFMMTClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      int nclients = 0;
      String hostname = clsoc.getInetAddress().toString(); //don't use getHostName() anymore due to slowness;
      synchronized(_clientThreads) {
        for (int j = _clientThreads.size()-1; j >= 0; j--) {
          UFMMTClientThread ct = _clientThreads.elementAt(j);
	  if (ct.hostname().equals(hostname)) {
	    nclients++;
	    if (nclients > 4) {
	      System.out.println(_mainClass+"::getNewClientThread> Killing thread "+ct.getThreadName());
	      ct._terminate();
	      _clientThreads.remove(j);
	    }
	  }
        }
      }
      return new WeewxClientThread(clsoc, clientCount, simMode);
    }

//=====================================================================================================

    // Class AncillaryThread creates thread to poll device for status and send info to status clients

    protected class AncillaryThread extends UFMMTThreadedAgent.AncillaryThread {

        private String _className = getClass().getName();

        public AncillaryThread() {
	  setName("Ancillary Thread");
	}

        public void run() {
          int n = 15;
          int retryCount = 0;
	  int failCount = 0;
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
	      try {
                /* update heartbeat */
                heartbeat();
                /* Send new database values to status client threads.
                  Actual sending will occur in each UFMMTClientThread */
                updateStatusClients();
                /* Check for new records */
                updateDatabase();
	        //check sql and weather agent connections 
		if (!_wthConnected) {
		  retryCount++;
		  if (retryCount >= 15*failCount) {
                    //set ancillary running to false so thread doesn't get killed if init takes more than 15 sec
                    _ancillaryRunning = false;
		    failCount = checkConnections(failCount);
		    heartbeat(); //need to update heartbeat before setting ancillary running to true
                    _ancillaryRunning = true;
                    //double failCount and hence time to reconnect until 7.5 minutes 
                    if (failCount >= 3 && failCount < 30) failCount *= 2;
		    if (failCount >= 30) retryCount = 0; //reset retry count to keep checking every 7.5 minutes 
		  }
		}
	      } catch (Exception e) {
                System.out.println(_className+"::run> Error...");
		e.printStackTrace();
	      }
              /* Sleep 1 second */
              hibernate();
              /* Update _ancillaryRunning at end of loop */
              _ancillaryRunning = true;
            }
            hibernate();
          }
        }
    }

//=====================================================================================================


    protected class WeatherThread extends Thread {
      protected boolean _shutdown = false;
      private String _className = getClass().getName();
      protected int _sleepPeriod = 120000; //2 minutes
 
      public WeatherThread() {}

      public void shutdown() { _shutdown = true; }

      public void run() {
        while (!_shutdown) {
          updateWeather();
          try {
            Thread.sleep(_sleepPeriod);
          } catch (InterruptedException e) { }
        }
      } /* End run */
    } /* End WeatherThread */


//=====================================================================================================

  protected class PingThread extends Thread {
    protected boolean _shutdown = false;
    protected boolean _error = false;
    private String _className = getClass().getName();
    protected int _sleepPeriod = 10000; //ping every 10s

    public PingThread() {}

    public void shutdown() { _shutdown = true; }

    public void reset() {
      System.out.println(_className+"::reset> Attempting to reset Ping Thread...");
      if (_wthConnected) _wthConnected = false;
      try {
        wthSocket.close();
      } catch(Exception e) {
        System.out.println(_className+"::reset> "+e.toString());
      }
      wthSocket = null;
    }

    public void run() {
      long npings = 0;
      while (!_shutdown) {
        if (_error) {
          //Check to see if status thread is currently disconnecting...
          if (_wthConnected) _wthConnected = false;
          //Clear error state
          _error = false;
        }
        hibernate(_sleepPeriod);
        //Don't send ping while waiting for command response
        if (!_ready) continue;
        if (wthSocket == null) continue;
        if (!_wthConnected) continue;
        synchronized(wthSocket) {
          try {
            npings++;
            wthSocket.setSoTimeout(_sleepPeriod);
            UFStrings pingReq = new UFStrings(_className+": ping","ping "+npings);
            int nbytes = pingReq.sendTo(wthSocket);
            if( nbytes <= 0 ) {
              System.out.println( _className + "::run> zero bytes sent.");
              _error = true;
              //Release lock on wthSocket to avoid deadlock
              continue;
            }
            if (_verbose) System.out.println(_className+"::run> Sent ping "+npings+": "+nbytes+" bytes at "+ctime());
            UFStrings reply = (UFStrings)UFProtocol.createFrom(wthSocket);
            wthSocket.setSoTimeout(0);
            if (reply == null) {
              System.out.println(_className+"::run> Error: received null response from weatherAgent at "+ctime());
              //Release lock on wthSocket to avoid deadlock
              _error = true;
              continue;
            }
            if (_verbose) System.out.println(_className+"::run> Received response "+reply.name()+" "+reply.stringAt(0)+" from weatherAgent at "+ctime());
          } catch (IOException ioe) {
            System.out.println(_className+"::run> "+ioe.toString());
            ioe.printStackTrace();
            //Release lock on wthSocket to avoid deadlock
            _error = true;
            continue;
          }
        }
      }
    } /* End run */
  } /* End PingThread */
} //end of class WeewxAgent

