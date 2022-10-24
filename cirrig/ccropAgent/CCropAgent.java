package CCROP; 
/**
 * Title:        CCropAgent
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

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

//=====================================================================================================

public class CCropAgent extends UFMMTThreadedAgent { 

    public static final
	String rcsID = "$Name:  $ $Id: CCropAgent.java,v 1.6 2011/01/24 19:36:31 warner Exp $";

    protected String _mainClass = getClass().getName();
    protected String dbName = "mybmp";
    protected String dbHost = "localhost";
    protected boolean _sqlConnected = false;
    protected boolean _initialized = false, _initializing = false;
    protected String dbUrl = "jdbc:mysql://localhost/mybmp";
    protected String sqlUser = "", sqlPass = "";
    protected String dbClass = "com.mysql.jdbc.Driver";
    protected String ccropPath = "../driver", ccropExe="runCcrop.py", runPath = "/home/mybmp/www/";
    protected Connection _dbCon; //database connection
    protected LinkedHashMap <String, CCropRealTime> runDatabase;

//----------------------------------------------------------------------------------------

    public CCropAgent( int serverPort, String[] args )
    {
	super(serverPort, args);
	options(args);
        runDatabase = new LinkedHashMap(20);
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      super.options(args);
      /* Handle any options specific to this agent here */
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().indexOf("-dbname") != -1) {
          if (args.length > j+1) dbName = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-dbhost") != -1) {
          if (args.length > j+1) dbHost = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-path") != -1) {
	  if (args.length > j+1) ccropPath = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-ccrop") != -1) {
          if (args.length > j+1) ccropExe = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-run") != -1) {
          if (args.length > j+1) runPath = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-sqluser") != -1) {
	  if (args.length > j+1) sqlUser = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-sqlpass") != -1) {
          if (args.length > j+1) sqlPass = args[j+1];
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
    /** MYSQL Specific Helper Methods */

//-----------------------------------------------------------------------------------------------------

    /** Setup database */
    protected void setDefaults() {
      super.setDefaults();
      synchronized(database) {
        /* Database records specific to this agent */
        addMMTRecord(_mainClass+":nscheduled", UFRecord.TYPE_INT, "0");
        addMMTRecord(_mainClass+":timer", UFRecord.TYPE_FLOAT, "0");
        addMMTRecord(_mainClass+":irrigation", UFRecord.TYPE_FLOAT, "0");
      }
      return;
    }

//-----------------------------------------------------------------------------------------------------
    /** Connect to device and exec agent
      * Subclasses should override this class with options specific to
      * connecting to the specific device.
      */
    protected void init() {
      /* set up database */
      setDefaults();
      /* read XML defaults */
      //readXMLDefaults();
      _initialized = false;
      _initializing = true;
      updateDatabase(_mainClass+":status", "INIT");
      /* Do not proceed until connected to exec agent */
      if (_simMode) {
        System.out.println(_mainClass+"::init> Simulating connection to database...");
      } else {
        while (!_sqlConnected) {
	  /* Connect to mysql database */
	  _sqlConnected = connectToSql();
          if (_sqlConnected) {
            System.out.println(_mainClass+"::init> Successfully connected to SQL database"); 
          } else {
            System.out.println(_mainClass+"::init> Error!  Unable to connect to SQL database"); 
          }
          hibernate(2500);
        }
      }
      //update database link in CCropRealTime instances
      synchronized(runDatabase) {
        for (Iterator i = runDatabase.keySet().iterator(); i.hasNext(); ) {
          String rdid = (String)i.next();
          CCropRealTime rt = (CCropRealTime)runDatabase.get(rdid);
	  rt.updateDatabaseLink(_dbCon);
	}
      }
      _initialized = true;
      _initializing = false;
      updateDatabase(_mainClass+":status", "IDLE");
    }


//-----------------------------------------------------------------------------------------------------

    protected boolean connectToSql() {
      boolean retVal = false;
      try {
	Class.forName("com.mysql.jdbc.Driver");
	_dbCon = DriverManager.getConnection(dbUrl, sqlUser, sqlPass);
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
      /* First check for ABORTS! */
      if (nreq == 1) {
        int j = 0;
        if (getCmdName(req,j).equals("ABORT")) {
          String param = getCmdParam(req,j);
          /* Right now ABORT DOES NOTHING */
          success = true;
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
          System.out.println(_mainClass+"::action> performing INIT...");
          if (!_simMode) {
            _sqlConnected = false;
          }
          hibernate(2500);
          init();
          success = true;
          if (!_simMode && !_isConnected) success = false;
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
        if (getCmdName(req,j).equals("GET_IRRIGATION")) {
	  /* GET_IRRIGATION::uid [Z|R] id method*/
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
	  String type = getReplyToken(param, 1);
	  String id = getReplyToken(param, 2);
	  String defMethod = getReplyTokens(param, 3);
          Calendar c = new GregorianCalendar();
	  if (_simMode) {
	    System.out.println(_mainClass+"::action> Simulating GET IRRIGATION");
	    responses.add("SIM 1999001 0 test");
	  } else {
	    if (type.equals("R")) {
	      String doy = "1999001", irrig = "0", runName = "test", yestIrrig = "-1", avgIrrig="-1", fiveMax="-1", defIrrig="-1";
	      String query = "select * FROM users INNER JOIN runs on users.uid = runs.uid where users.uid= " + uid + " AND auto=1 AND type='realtime' AND rid="+id;
	      Statement stmt = null;
	      ResultSet rs = null;
	      int todayDoy = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
              try {
	        stmt = _dbCon.createStatement();
	        rs = stmt.executeQuery(query);
	        if (rs.next()) {
		  doy = rs.getString("doy");
		  irrig = rs.getString("irrig");
		  runName = rs.getString("name");
		  yestIrrig = rs.getString("yestIrrig");
		  avgIrrig = rs.getString("threeAvgIrrig");
                  fiveMax = rs.getString("fiveMax");
                  System.out.println(_mainClass+"::action> Success: doy = "+doy+"; irrig = "+irrig+"; yest = "+yestIrrig+"; 3-day avg = "+avgIrrig+"; 5-day max = "+fiveMax+"; run = "+runName); 
		  //Check day of year
		  if (!doy.equals(""+todayDoy)) {
		    irrig = "-1";
		    System.out.println(_mainClass+"::action> WARNING: doy "+doy+" does not match today ("+todayDoy+")!");
		    if (defMethod.toLowerCase().equals("yesterday")) {
		      defIrrig = yestIrrig;
		    } else if (defMethod.toLowerCase().equals("3-day avg")) {
		      defIrrig = avgIrrig;
		    } else if (defMethod.toLowerCase().equals("5-day max")) {
		      defIrrig = fiveMax;
		    } else if (defMethod.toLowerCase().equals("none")) {
		      defIrrig = "0";
		    } else if (defMethod.toLowerCase().equals("manual default")) {
		      defIrrig = "0";
		    }
		    System.out.println(_mainClass+"::action> Using default irrigation method "+defMethod+": "+irrig);
		  }
		  responses.add("SUCCESS "+doy+" "+irrig+" "+defIrrig+" "+runName);
	        } else {
		  System.out.println(_mainClass+"::action> Error: user id incorrect or no run set to auto!");
		  reply = new UFStrings("ERROR", "User id incorrect or no run set to auto!");
		  success = false;
	        }
	      } catch(SQLException e) {
	        e.printStackTrace();
                responses.add("ERROR "+e.toString()); 
		_sqlConnected = false;
		success = false;
	      } finally {
	        if (stmt != null) try {
	          stmt.close();
	        } catch(Exception e) { _sqlConnected = false; }
	        if (rs != null) try {
		  rs.close();
	        } catch(Exception e) { _sqlConnected = false; }
	      }
	    } else {
              String histTime= "NULL", irrig = "0", irrigMin = "0", irrigRate = "-1", defIrrig = "-1";
              String query = "select * FROM users INNER JOIN zoneHistory on users.uid = zoneHistory.uid where users.uid=" + uid + " AND zid="+id+" AND date(histTime) = date(NOW()) AND source=2 ORDER BY histTime DESC LIMIT 1"; 
              Statement stmt = null;
              ResultSet rs = null;
	      boolean useDefault = false;
              try {
                stmt = _dbCon.createStatement();
                rs = stmt.executeQuery(query);
                if (rs.next()) {
		  histTime= rs.getString("histTime");
		  irrig = rs.getString("irrig_in");
		  irrigMin = rs.getString("irrig_minutes");
		  irrigRate = rs.getString("irrig_in_per_hr");
                  System.out.println(_mainClass+"::action> Success: histTime = "+histTime+"; irrig = "+irrig+"; minutes = "+irrigMin+"; rate = "+irrigRate);
                } else {
		  useDefault = true;
		  irrig = "-1";
		  System.out.println(_mainClass+"::action> WARNING: Could not get today's irrigation for zone "+id);
		}
                stmt.close();
                rs.close();
		if (useDefault) {
		  if (defMethod.toLowerCase().equals("yesterday")) {
		    query = "select * FROM users INNER JOIN zoneHistory on users.uid = zoneHistory.uid where users.uid=" + uid + " AND zid="+id+" AND date(histTime) = date(NOW()-INTERVAL 24 HOUR) AND source=2 ORDER BY histTime DESC LIMIT 1";
		    stmt = _dbCon.createStatement();
		    rs = stmt.executeQuery(query);
		    if (rs.next()) {
		      histTime= rs.getString("histTime");
                      defIrrig = rs.getString("irrig_in");
                      irrigMin = rs.getString("irrig_minutes");
                      irrigRate = rs.getString("irrig_in_per_hr");
                      System.out.println(_mainClass+"::action> Using yesterday's data: histTime = "+histTime+"; irrig = "+defIrrig+"; minutes = "+irrigMin+"; rate = "+irrigRate);
		    } else {
		      defIrrig = "-1";
		      System.out.println(_mainClass+"::action> ERROR: Could not get yesterday's irrigation for zone "+id);
		      reply = new UFStrings("ERROR", "Could not get today's or yesterday's irrigation for zone "+id);
		      success = false;
		    }
		    stmt.close();
		    rs.close();
                  } else if (defMethod.toLowerCase().equals("3-day avg")) {
                    query = "select avg(irrig_in) as 3DayAvg from (select irrig_in from  users INNER JOIN zoneHistory on users.uid = zoneHistory.uid where users.uid=" + uid + " AND zid="+id+" AND source=2 AND date(histTime) > date(NOW()-INTERVAL 3 DAY) GROUP BY date(histTime)) as t1";
                    stmt = _dbCon.createStatement();
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                      defIrrig = rs.getString("3DayAvg");
                      System.out.println(_mainClass+"::action> Using 3-day avg irrigation = "+defIrrig+" inches.");
                    } else {
                      defIrrig = "-1";
                      System.out.println(_mainClass+"::action> ERROR: Could not get 3-day avg irrigation for zone "+id);
		      reply = new UFStrings("ERROR", "Could not get today's irrigation or 3-day avg for zone "+id);
                      success = false;
                    }
                    stmt.close();
                    rs.close();
                  } else if (defMethod.toLowerCase().equals("5-day max")) {
		    query = "select max(irrig_in) as 5DayMax FROM users INNER JOIN zoneHistory on users.uid = zoneHistory.uid where users.uid=" + uid + " AND zid="+id+" AND source=2 AND date(histTime) > date(NOW()-INTERVAL 5 DAY)";
                    stmt = _dbCon.createStatement();
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                      defIrrig = rs.getString("5DayMax");
                      System.out.println(_mainClass+"::action> Using 5-day max irrigation = "+defIrrig+" inches.");
                    } else {
                      defIrrig = "-1";
                      System.out.println(_mainClass+"::action> ERROR: Could not get 5-day max irrigation for zone "+id);
		      reply = new UFStrings("ERROR", "Could not get today's irrigation or 5-day max for zone "+id);
		      success = false;
                    }
                    stmt.close();
                    rs.close();
                  } else if (defMethod.toLowerCase().equals("none")) {
                    defIrrig = "0";
		    System.out.println(_mainClass+"::action> Using default irrigation method = None");
                  } else if (defMethod.toLowerCase().equals("manual default")) {
                    System.out.println(_mainClass+"::action> Using default irrigation method = manual default");
                    defIrrig = "0";
                  }
		  if (irrigRate.equals("-1")) {
		    query = "select * FROM users INNER JOIN zoneHistory on users.uid = zoneHistory.uid where users.uid=" + uid + " AND zid="+id+" AND source=2 ORDER BY histTime DESC LIMIT 1";
		    stmt = _dbCon.createStatement();
		    rs = stmt.executeQuery(query);
		    if (rs.next()) {
		      histTime= rs.getString("histTime");
                      irrigRate = rs.getString("irrig_in_per_hr");
		      System.out.println(_mainClass+"::action> Using irrig rate of "+irrigRate+" from "+histTime);
		    }
		  }
		}
                if (defIrrig == null) {
                  defIrrig = "-1";
                  System.out.println(_mainClass+"::action> ERROR: default irrigation is null for zone "+id);
                } else {
                  try {
                    float ndef = Float.parseFloat(defIrrig);
                  } catch(NumberFormatException nfe) {
                    defIrrig = "-1";
                    System.out.println(_mainClass+"::action> ERROR: default irrigation is null for zone "+id);
                  }
                }
		if (success) responses.add("SUCCESS "+irrig+" "+irrigMin+" "+irrigRate+" "+defIrrig+" "+histTime);
              } catch(SQLException e) {
                e.printStackTrace();
                responses.add("ERROR "+e.toString());
		_sqlConnected = false;
		success = false;
              } finally {
                if (stmt != null) try {
                  stmt.close();
                } catch(Exception e) { _sqlConnected = false; }
                if (rs != null) try {
                  rs.close();
                } catch(Exception e) { _sqlConnected = false; }
              }
	    }
	  }
        } else if (getCmdName(req,j).equals("CALCULATE_IRRIGATION")) {
          /* CALCULATE_IRRIGATION::uid id [continuous | daily]*/
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          String id = getReplyToken(param, 1);
	  boolean continuous = false;
	  if (getReplyToken(param, 2).toLowerCase().equals("continuous")) continuous = true;
	  String lastCycleTime = getReplyToken(param, 3)+" "+getReplyToken(param, 4); //YYYY-MM-DD HH:mm:SS
          Calendar c = new GregorianCalendar();

	  String zoneType;
	  Zone zone = null;

	  double[] tmax_hourly, tmin_hourly, solar_hourly, rain_hourly;
	  boolean[] hasHourlyData; 
	  int nhourly = 0;
	  double weekly_rain_in = -1;

	  /* Query zone first */
	  String query = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zid = "+id;
	  Statement stmt = null;
	  ResultSet rs = null;
	  try {
	    synchronized(_dbCon) {
	      stmt = _dbCon.createStatement();
	      rs = stmt.executeQuery(query);
	    }
	    if (rs.next()) {
	      zoneType = rs.getString("zoneType");
	      if (zoneType.equals("ET-sprinkler") || zoneType.equals("ET-micro")) {
		zone = new ETZone(rs);
                rs.close();
                stmt.close();
	      } else if (zoneType.equals("LF-sprinkler") || zoneType.equals("LF-micro")) {
		zone = new LFZone(rs);
                rs.close();
                stmt.close();
	      } else {
                System.out.println(_mainClass+"::action> Error!  Invalid zone type "+zoneType);
		reply = new UFStrings("ERROR", "Invalid zone type "+zoneType);
		success = false;
                rs.close();
                stmt.close();
		break;
	      }
            } else {
	      System.out.println(_mainClass+"::action> Error!  Could not find zone with id = "+id);
	      reply = new UFStrings("ERROR", "Could not find zone with id = "+id);
	      success = false;
              rs.close();
              stmt.close();
              break;
	    }
	    /* Get weather */
            //Don't check uid to allow public access.  wsid is unique.
	    query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND hour > NOW()-INTERVAL 25 HOUR ORDER BY hour;";
	    tmax_hourly = new double[24];
            tmin_hourly = new double[24];
            solar_hourly = new double[24];
            rain_hourly = new double[24];
	    hasHourlyData = new boolean[24];
            synchronized(_dbCon) {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
            }
	    while (rs.next()) {
	      int h = rs.getInt("h");
	      if (!hasHourlyData[h]) {
	        nhourly++;
	        hasHourlyData[h] = true;
	      }
	      tmax_hourly[h] = rs.getDouble("max_temp");
	      tmin_hourly[h] = rs.getDouble("min_temp");
	      solar_hourly[h] = rs.getDouble("solar_radiation");
	      rain_hourly[h] = rs.getDouble("rain_in");
	    }
            rs.close();
            stmt.close();
	    /* Find replacement weather values */
	    if (nhourly < 24) {
	      for (int i = 0; i < 24; i++) {
	        if (hasHourlyData[i]) continue;
	        //Don't check uid to allow public access.  wsid is unique.
	        query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND HOUR(hour)=" + i + " ORDER BY hour DESC LIMIT 1;";
	        synchronized(_dbCon) {
	      	  stmt = _dbCon.createStatement();
	      	  rs = stmt.executeQuery(query);
	        }
	        if (rs.next()) {
	      	  int h = rs.getInt("h");
	      	  String[] dateAndTime = rs.getString("hour").split("\\s+");
	      	  System.out.println(_mainClass+"::action> Could not find weather for "+dateAndTime[1]+". Using data from "+dateAndTime[0]+" instead.");
	      	  nhourly++;
	      	  hasHourlyData[h] = true;
	      	  tmax_hourly[h] = rs.getDouble("max_temp");
	      	  tmin_hourly[h] = rs.getDouble("min_temp");
	      	  solar_hourly[h] = rs.getDouble("solar_radiation");
	      	  rain_hourly[h] = rs.getDouble("rain_in");
	        }
                rs.close();
                stmt.close();
	      }
	    }
            //also query weekly rain
            query = "select sum(rain_in) as weekly_rain from hourlyWeather where wsid=" + zone.wsid + " AND hour >= NOW()-INTERVAL 168 HOUR;";
	    synchronized(_dbCon) {
	      stmt = _dbCon.createStatement();
	      rs = stmt.executeQuery(query);
	    }
	    if (rs.next()) {
	      weekly_rain_in = rs.getDouble("weekly_rain");
	    }
	    rs.close();
	    stmt.close();
	  } catch(Exception e) {
	    System.out.println(_mainClass+"::action> Error querying zone with id = "+id);
	    e.printStackTrace();
	    reply = new UFStrings("ERROR", "Error querying zone with id = "+id);
	    _sqlConnected = false;
	    success = false;
	    break;
	  } finally {
	    if (stmt != null) try {
	      stmt.close();
	    } catch(Exception e) { _sqlConnected = false; }
	    if (rs != null) try {
	      rs.close();
	    } catch(Exception e) { _sqlConnected = false; }
	  }

	  if (nhourly < 24) {
	    System.out.println(_mainClass+"::action> Error: Weather station wsid = "+zone.wsid+" only has "+nhourly+" datapoints!");
            reply = new UFStrings("ERROR", "Weather station wsid = "+zone.wsid+" only has "+nhourly+" datapoints!");
	    success = false;
	    break;
	  }
	  // Accumulate hourly weather
	  zone.setHourlyWeather(tmax_hourly, tmin_hourly, solar_hourly, rain_hourly, hasHourlyData);
	  if (weekly_rain_in != -1) zone.setWeeklyRain(weekly_rain_in); //set weekly rain if found

	  //Check prev deficit and lastRunDate and set in zones
	  double prevDeficit = 0;
	  String lastRunDate = null;
	  //query for any deficit in zone history from yesterday
          try {
	    if (continuous) {
	      query = "select histTime, deficit_in from zoneHistory where wsid=" + zone.wsid + " AND zid="+ zone.getId() + " AND uid=" + uid + " AND histTime >= curdate()-interval 1 day AND source=3 ORDER BY histTime DESC LIMIT 1;";
	    } else {
              query = "select histTime, deficit_in from zoneHistory where wsid=" + zone.wsid + " AND zid="+ zone.getId() + " AND uid=" + uid + " AND date(histTime) = curdate()-interval 1 day AND source=2 LIMIT 1;";
	    }
            synchronized(_dbCon) {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
            }
            if (rs.next()) {
              //found deficit, start cum at this value.
	      prevDeficit = rs.getDouble("deficit_in");
	      lastRunDate = rs.getString("histTime");
              System.out.println(_mainClass+"::action> Found deficit of "+prevDeficit+" inches from zone "+id+" at "+lastRunDate);
            }
          } catch(SQLException e) {
            System.out.println(_mainClass+"::action> Error querying zoneHistory with id = "+id);
            e.printStackTrace();
            prevDeficit = 0;
	    _sqlConnected = false;
            //assume 0 deficit and continue in this case
          } finally {
            if (stmt != null) try {
              stmt.close();
            } catch(Exception e) { _sqlConnected = false; }
            if (rs != null) try {
              rs.close();
            } catch(Exception e) { _sqlConnected = false; }
          }
	  zone.setPrevDeficit(prevDeficit);
	  if (continuous) {
	    //set to lastRunDate if found; otherwise use lastCycleTime
	    if (lastRunDate != null && !lastRunDate.equals("null")) zone.setLastRunDate(lastRunDate); else zone.setLastRunDate(lastCycleTime);
	  }
	  System.out.println("Prev deficit = "+zone.getPrevDeficit()+"; lastRunDate = "+zone.getLastRunDate());
    
	  //For LF zones, get weather on day before LF test date
	  if (zone.getZoneType().equals("LF-sprinkler") || zone.getZoneType().equals("LF-micro")) {
	    //Don't check uid to allow public access.  wsid is unique.
	    try {
	      query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND hour <= '" + zone.lfTestDate + "' AND hour > '" + zone.lfTestDate + "' - INTERVAL 1 day ORDER BY hour;";
	      double[] tmax_lf = new double[24];
	      double[] tmin_lf = new double[24];
	      double[] solar_lf = new double[24];
	      double[] rain_lf = new double[24];
	      boolean[] hasHourlylf = new boolean[24];
	      int lfnhourly = 0;
	      synchronized(_dbCon) {
	        stmt = _dbCon.createStatement();
	        rs = stmt.executeQuery(query);
	      }
	      while (rs.next()) {
	        int h = rs.getInt("h");
                if (!hasHourlylf[h]) {
	          lfnhourly++;
                  hasHourlylf[h] = true;
	        }
                tmax_lf[h] = rs.getDouble("max_temp");
                tmin_lf[h] = rs.getDouble("min_temp");
                solar_lf[h] = rs.getDouble("solar_radiation");
                rain_lf[h] = rs.getDouble("rain_in");
              }
	      zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
            } catch(SQLException e) {
              System.out.println(_mainClass+"::action> Error querying zone with id = "+id);
	      reply = new UFStrings("ERROR", "Error querying zone with id = "+id);
              e.printStackTrace();
              success = false;
	      _sqlConnected = false;
              break;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
	  }

	  // get irrigation -- ET and LF zones will each handle proper calculations
	  double[] irrigVals = zone.getIrrigValue(continuous);
          SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String sqlDate = sqldf.format(new java.util.Date());
          if (success) {
	    responses.add("SUCCESS "+irrigVals[0]+" "+irrigVals[1]+" "+zone.getIrrigRate()+" "+irrigVals[2]+" "+sqlDate);
            String zoneQuery = zone.getIrrigation(3, continuous);
            try {
              stmt = _dbCon.createStatement();
              int nrows = stmt.executeUpdate(zoneQuery);
            } catch(SQLException e) {
              e.printStackTrace();
	      _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
	  } else {
	    System.out.println(_mainClass+"::action> ERROR querying zone with id = "+id);
	    reply = new UFStrings("ERROR", "Error querying zone with id = "+id);
	  }
	} else if (getCmdName(req,j).equals("GET_UID")) {
          /* GET_UID::user pass */
          String param = getCmdParam(req,j);
          String user = getReplyToken(param, 0);
          String pass = getReplyToken(param, 1);
          int uid = -1;
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating GET_UID");
            responses.add("SIM UID -1");
          } else {
            String query = "select * FROM users where username='" + user + "' AND password='" + pass + "'";
            Statement stmt = null;
            ResultSet rs = null;
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
              if (rs.next()) {
                uid = rs.getInt("uid");
                System.out.println(_mainClass+"::action> Success: uid = "+uid);
                responses.add("SUCCESS UID "+uid);
              } else {
                System.out.println(_mainClass+"::action> Error: username or password incorrect!");
                reply = new UFStrings("ERROR", "Username or password incorrect!");
                success = false;
              }
            } catch(SQLException e) {
              e.printStackTrace();
              reply = new UFStrings("ERROR", e.toString());
              success = false;
              _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
            /* break here on error after statement and resultset are closed */
            if (success == false) break;
	  }
	} else if (getCmdName(req,j).equals("GET_RT_RUNS")) {
	  /* GET_RT_RUNS::uid */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
	  if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating GET_RT_RUNS");
	    responses.add("RT::-1::Test Run");
	  } else {
            Statement stmt = null;
            ResultSet rs = null;
            String query = "select * from runs where uid="+uid+" AND type='realtime' AND auto=1";
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
	      int nruns = 0;
              while (rs.next()) {
		nruns++;
		String runString = "RT::"+rs.getString("rid")+"::"+rs.getString("name");
                responses.add(runString);
              }
	      if (nruns == 0) responses.add("NONE::-1::Test Run");
            } catch(SQLException e) {
              e.printStackTrace();
              reply = new UFStrings("ERROR", e.toString());
              success = false;
              _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
            if (success == false) break;
	  }
	} else if (getCmdName(req,j).equals("GET_ZONES")) {
          /* GET_ZONES::uid */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating GET_ZONES");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant");
	  } else {
            Statement stmt = null;
            ResultSet rs = null;
            String query = "select * from zones where uid="+uid+" AND auto=1";
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
	      int nzones = 0;
              while (rs.next()) {
		nzones++;
                String zoneString = "ZONE::"+rs.getString("zid")+"::"+rs.getString("zoneNumber")+"::"+rs.getString("zoneName")+"::"+rs.getString("plant");
                responses.add(zoneString);
              }
              if (nzones == 0) responses.add("NONE::-1::-1::Test Zone::Test Plant");
            } catch(SQLException e) {
              e.printStackTrace();
              reply = new UFStrings("ERROR", e.toString());
              success = false;
              _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
            if (success == false) break;
          }
        } else if (getCmdName(req,j).equals("GET_NAME")) {
          /* GET_NAME::uid [R | Z] id */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
	  String type = getReplyToken(param, 1);
	  String id = getReplyToken(param, 2);
	  //ZONE::zid::zoneNumber::zoneName::plant::ncycles::external
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating GET_NAME");
            responses.add("ZONE::-1::-1::Test Zone::Test Plant::1::Ext Reference");
          } else {
            Statement stmt = null;
            ResultSet rs = null;
            String query = null;
	    if (type.equals("Z")) {
	      query = "select * from zones where uid="+uid+" AND zid="+id+" AND auto=1";
	    } else {
	      query = "select * from runs where uid="+uid+" AND rid="+id+" AND type='realtime' AND auto=1";
	    }
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
              if (rs.next()) {
		if (type.equals("Z")) {
		  String zoneString = "ZONE::"+rs.getString("zid")+"::"+rs.getString("zoneNumber")+"::"+rs.getString("zoneName")+"::"+rs.getString("plant")+"::"+rs.getString("ncycles")+"::"+rs.getString("external");
		  //no external reference
		  if (zoneString.endsWith("::")) zoneString += "null";
		  responses.add(zoneString);
		} else {
                  String runString = "RT::"+rs.getString("rid")+"::"+rs.getString("name");
                  responses.add(runString);
		}
              }
            } catch(SQLException e) {
              e.printStackTrace();
              reply = new UFStrings("ERROR", e.toString());
              success = false;
              _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
            if (success == false) break;
          }
        } else if (getCmdName(req,j).equals("GET_ZONE_REFS")) {
          /* GET_ZONE_REFS::uid 
	     return ids and external references and ncycles for all zones */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating GET_ZONE_REFS");
            responses.add("ZONE_REF::-1::1::Ext Reference");
          } else {
            Statement stmt = null;
            ResultSet rs = null;
            String query = "select * from zones where uid="+uid+" AND auto=1";
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
              int nzones = 0;
              while (rs.next()) {
                nzones++;
                String zoneString = "ZONE_REF::"+rs.getString("zid")+"::"+rs.getString("ncycles")+"::"+rs.getString("external");
		//no external reference
                if (zoneString.endsWith("::")) zoneString += "null";
                responses.add(zoneString);
              }
              if (nzones == 0) responses.add("NONE::-1::1::Ext Reference");
            } catch(SQLException e) {
              e.printStackTrace();
              reply = new UFStrings("ERROR", e.toString());
              success = false;
              _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
              if (rs != null) try {
                rs.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
            if (success == false) break;
          }
        } else if (getCmdName(req,j).equals("GET_ZONE_INFO")) {
          /* GET_ZONE_INFO::uid id */
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          String id = getReplyToken(param, 1);
          System.out.println(_mainClass+"::action> Received GET_ZONE_INFO request for zone "+id);
	  Zone zone = new Zone(); //default constructor sets zid and uid to -1
          zone.zoneType = "None";
	  if (_simMode) {
	    System.out.println(_mainClass+"::action> Simulating GET_ZONE_INFO");
	    //use default Zone
	  } else {
	    Statement stmt = null;
	    ResultSet rs = null;
	    String query = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid="+uid+" AND zid="+id;
	    try {
              synchronized(_dbCon) {
                stmt = _dbCon.createStatement();
                rs = stmt.executeQuery(query);
              }
              if (rs.next()) {
                String zoneType = rs.getString("zoneType");
                if (zoneType.equals("ET-sprinkler") || zoneType.equals("ET-micro")) {
                  zone = new ETZone(rs);
                } else if (zoneType.equals("LF-sprinkler") || zoneType.equals("LF-micro")) {
                  zone = new LFZone(rs);
                } //else use default zone
	      }
              rs.close();
              stmt.close();
	    } catch(SQLException e) {
              System.out.println(_mainClass+"::action> Error querying zone with id = "+id);
              e.printStackTrace();
	      _sqlConnected = false;
	      //use default zone
	    }

            //For LF zones, get weather on day before LF test date
            if (zone.getZoneType().equals("LF-sprinkler") || zone.getZoneType().equals("LF-micro")) {
              //Don't check uid to allow public access.  wsid is unique.
              try {
                query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND hour <= '" + zone.lfTestDate + "' AND hour > '" + zone.lfTestDate + "' - INTERVAL 1 day ORDER BY hour;";
                double[] tmax_lf = new double[24];
                double[] tmin_lf = new double[24];
                double[] solar_lf = new double[24];
                double[] rain_lf = new double[24];
                boolean[] hasHourlylf = new boolean[24];
                int lfnhourly = 0;
                synchronized(_dbCon) {
                  stmt = _dbCon.createStatement();
                  rs = stmt.executeQuery(query);
                }
                while (rs.next()) {
                  int h = rs.getInt("h");
                  if (!hasHourlylf[h]) {
                    lfnhourly++;
                    hasHourlylf[h] = true;
                  }
                  tmax_lf[h] = rs.getDouble("max_temp");
                  tmin_lf[h] = rs.getDouble("min_temp");
                  solar_lf[h] = rs.getDouble("solar_radiation");
                  rain_lf[h] = rs.getDouble("rain_in");
                }
                zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
              } catch(SQLException e) {
                System.out.println(_mainClass+"::action> Error querying zone with id = "+id);
                e.printStackTrace();
		_sqlConnected = false;
                //use default zone
              } finally {
                if (stmt != null) try {
                  stmt.close();
                } catch(Exception e) { _sqlConnected = false; }
                if (rs != null) try {
                  rs.close();
                } catch(Exception e) { _sqlConnected = false; }
              }
            }
	  }
	  //cast as CCropClientThread and send zone
	  CCropClientThread cct = (CCropClientThread)ct;
	  success = cct.sendZone(zone);
	  //return here, don't write UFStrings object
	  return success;
        } else if (getCmdName(req,j).equals("GET_ZONES_INFO")) {
	  //NEW command ZONES plural!
	  //Keep old command for now for backwards compatibility
          //GET_ZONES_INFO::uid id1,id2,...,idn 
          String param = getCmdParam(req,j);
          String uid = getReplyToken(param, 0);
          String idlist = getReplyToken(param, 1);
          System.out.println(_mainClass+"::action> Received GET_ZONES_INFO request for zone list "+idlist);

	  ArrayList<Zone> zoneList = new ArrayList();

	  Zone zone = new Zone(); //default constructor sets zid and uid to -1
          zone.zoneType = "None";
	  if (_simMode) {
	    System.out.println(_mainClass+"::action> Simulating GET_ZONE_INFO");
	    //use default Zone
	    zoneList.add(zone);
	  } else {
	    Statement stmt = null;
	    ResultSet rs = null;
	    String query = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid="+uid+" AND zid IN ("+idlist+")";
	    try {
              synchronized(_dbCon) {
                stmt = _dbCon.createStatement();
                rs = stmt.executeQuery(query);
              }
	      while (rs.next()) {
                String zoneType = rs.getString("zoneType");
                if (zoneType.equals("ET-sprinkler") || zoneType.equals("ET-micro")) {
		  zoneList.add(new ETZone(rs));
                } else if (zoneType.equals("LF-sprinkler") || zoneType.equals("LF-micro")) {
		  zoneList.add(new LFZone(rs));
                } else {
		  //else use default zone
		  zoneList.add(zone);
		}
	      }
              rs.close();
              stmt.close();
	    } catch(SQLException e) {
              System.out.println(_mainClass+"::action> Error querying zone list with ids = "+idlist);
              e.printStackTrace();
	      _sqlConnected = false;
	      success = false;
              reply = new UFStrings("ERROR", e.toString());
	      break;
	    }
	  }
	
          double[] tmax_lf = new double[24];
          double[] tmin_lf = new double[24];
          double[] solar_lf = new double[24];
          double[] rain_lf = new double[24];
          boolean[] hasHourlylf = new boolean[24];
	  String lastDate = "NONE";
	  int lastWsid = -1;

	  //iterate over zoneList
	  Iterator<Zone> i = zoneList.iterator();
          while (i.hasNext()) {
	    zone = (Zone) i.next();
            //For LF zones, get weather on day before LF test date
            if (zone.getZoneType().equals("LF-sprinkler") || zone.getZoneType().equals("LF-micro")) {
	      if (zone.wsid == lastWsid && zone.lfTestDate.equals(lastDate)) {
		//same LF data 
		zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
	      } else {
		//Don't check uid to allow public access.  wsid is unique.
                Statement stmt = null;
                ResultSet rs = null;
                try {
                  String query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND hour <= '" + zone.lfTestDate + "' AND hour > '" + zone.lfTestDate + "' - INTERVAL 1 day ORDER BY hour;";
                  tmax_lf = new double[24];
                  tmin_lf = new double[24];
                  solar_lf = new double[24];
                  rain_lf = new double[24];
                  hasHourlylf = new boolean[24];
                  int lfnhourly = 0;
                  synchronized(_dbCon) {
                    stmt = _dbCon.createStatement();
                    rs = stmt.executeQuery(query);
                  }
                  while (rs.next()) {
                    int h = rs.getInt("h");
                    if (!hasHourlylf[h]) {
                      lfnhourly++;
                      hasHourlylf[h] = true;
                    }
                    tmax_lf[h] = rs.getDouble("max_temp");
                    tmin_lf[h] = rs.getDouble("min_temp");
                    solar_lf[h] = rs.getDouble("solar_radiation");
                    rain_lf[h] = rs.getDouble("rain_in");
                  }
                  zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
		  lastWsid = zone.wsid;
		  lastDate = zone.lfTestDate;
                } catch(SQLException e) {
                  System.out.println(_mainClass+"::action> Error querying hourly weather for zone with id = "+zone.zid);
                  e.printStackTrace();
                  _sqlConnected = false;
                  //use default zone
                } finally {
                  if (stmt != null) try {
                    stmt.close();
                  } catch(Exception e) { _sqlConnected = false; }
                  if (rs != null) try {
                    rs.close();
                  } catch(Exception e) { _sqlConnected = false; }
                }
              }
	    }
	    //now add zone to responses
	    responses.add(zone.getXML());
          }
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
    protected synchronized void queryDb() {
      long tstamp = (long)(System.currentTimeMillis()/1000);
      if (_simMode || !_sqlConnected) {
        if (tstamp%2 != 0) {
          updateDatabase(_mainClass+":nscheduled", "0");
	} else {
          updateDatabase(_mainClass+":nscheduled", "1");
	}
      } else {
	String uid, runTime, runName; 
	String query = "select * FROM runs INNER JOIN users on runs.uid = users.uid where auto=1 AND type='realtime'";
        Statement stmt = null;
	ResultSet rs = null;
	try {
	  /* First query real-time runs */
	  stmt = _dbCon.createStatement();
	  rs = stmt.executeQuery(query);
	  while (rs.next()) {
	    uid = rs.getString("uid");
	    runTime = rs.getString("autoRunTime");
	    runName = rs.getString("name");
	    String userPath = "data/"+rs.getString("username")+"/";
	    String cmd = ccropPath+"/"+ccropExe;
            int id = rs.getInt("uid");
            int rid = rs.getInt("rid");
	    synchronized(runDatabase) {
	      if (runDatabase.containsKey("R"+rid)) {
	        CCropRealTime rt = (CCropRealTime)runDatabase.get("R"+rid);
	        if (runName.equals(rt.getName())) {
		  if (runTime.equals(rt.getTime())) {
		    rt.updateTimeStamp(tstamp);
		  } else {
		    rt.cancel();
		    System.out.println("Cancelling real-time run with uid = "+uid+"; name = "+rt.getName()+" at "+ctime());
		    rt = new CCropRealTime(id, rid, runName, runTime, runPath, userPath, cmd);
                    runDatabase.put("R"+rid, rt);
		    rt.start();
                    rt.updateTimeStamp(tstamp);
                    System.out.println("Adding real-time run with uid = "+uid+"; name = "+runName+" at "+runTime+" at "+ctime());
		  }
	        } else {
                  rt.cancel();
                  System.out.println("Cancelling real-time run with uid = "+uid+"; name = "+rt.getName()+" at "+ctime());
                  rt = new CCropRealTime(id, rid, runName, runTime, runPath, userPath, cmd);
		  runDatabase.put("R"+rid, rt);
		  rt.start();
                  rt.updateTimeStamp(tstamp);
                  System.out.println("Adding real-time run with uid = "+uid+"; name = "+runName+" at "+runTime+" at "+ctime());
	        }
	      } else {
	        CCropRealTime rt = new CCropRealTime(id, rid, runName, runTime, runPath, userPath, cmd);
	        runDatabase.put("R"+rid, rt);
	        rt.start();
	        rt.updateTimeStamp(tstamp);
	        System.out.println("Adding real-time run with uid = "+uid+"; name = "+runName+" at "+runTime+" at "+ctime());
	      }
	    }
	  } 
	  stmt.close();
	  rs.close();

	  /* Next query zones */
	  query = "select * from zones INNER JOIN users on zones.uid = users.uid where auto=1";
	  String zoneName;
	  int zoneNumber;
          stmt = _dbCon.createStatement();
          rs = stmt.executeQuery(query);
          while (rs.next()) {
            uid = rs.getString("uid");
            runTime = rs.getString("autoRunTime");
            zoneName = rs.getString("zoneName");
            zoneNumber = rs.getInt("zoneNumber");
            int id = rs.getInt("uid");
            int zid = rs.getInt("zid");
	    if (runTime == null) {
	      System.out.println("ERROR: zone "+zid+", name = "+zoneName+" has null autoRunTime!");
	      continue;
	    }
	    synchronized(runDatabase) {
              if (runDatabase.containsKey("Z"+zid)) {
                CCropRealTime rt = (CCropRealTime)runDatabase.get("Z"+zid);
                if (zoneName.equals(rt.getName()) && zoneNumber == rt.getZoneNumber()) {
                  if (runTime.equals(rt.getTime())) {
                    rt.updateTimeStamp(tstamp);
                  } else {
                    rt.cancel();
                    System.out.println("Cancelling Cirrig zone "+rt.getZoneNumber()+" ("+rt.getName()+"); uid = "+uid+" at "+ctime());
                    rt = new CCropRealTime(id, zid, zoneName, zoneNumber, runTime, _dbCon);
                    runDatabase.put("Z"+zid, rt);
                    rt.start();
                    rt.updateTimeStamp(tstamp);
                    System.out.println("Adding Cirrig zone "+zoneNumber+" ("+zoneName+"); uid = "+uid+" at "+runTime+" at "+ctime());
                  }
                } else {
                  rt.cancel();
                  System.out.println("Cancelling Cirrig zone "+rt.getZoneNumber()+" ("+rt.getName()+"); uid = "+uid+" at "+ctime());
                  rt = new CCropRealTime(id, zid, zoneName, zoneNumber, runTime, _dbCon); 
                  runDatabase.put("Z"+zid, rt);
                  rt.start();
                  rt.updateTimeStamp(tstamp);
                  System.out.println("Adding Cirrig zone "+zoneNumber+" ("+zoneName+"); uid = "+uid+" at "+runTime+" at "+ctime());
                }
              } else {
                CCropRealTime rt = new CCropRealTime(id, zid, zoneName, zoneNumber, runTime, _dbCon);
                runDatabase.put("Z"+zid, rt);
                rt.start();
                rt.updateTimeStamp(tstamp);
                System.out.println("Adding Cirrig zone "+zoneNumber+" ("+zoneName+"); uid = "+uid+" at "+runTime+" at "+ctime());
              }
	    }
          }
          stmt.close();
          rs.close();
	} catch(SQLException e) {
	  e.printStackTrace();
	  _sqlConnected = false;
        } finally {
          if (stmt != null) try {
            stmt.close();
          } catch(Exception e) { _sqlConnected = false; }
          if (rs != null) try {
            rs.close();
          } catch(Exception e) { _sqlConnected = false; }
        }
	synchronized(runDatabase) {
	  for (Iterator i = runDatabase.keySet().iterator(); i.hasNext(); ) {
	    String rdid = (String)i.next();
            CCropRealTime rt = (CCropRealTime)runDatabase.get(rdid);
	    if (rt.getTimeStamp() != tstamp) {
	      if (rdid.startsWith("R")) {
	        System.out.println("Cancelling real-time run with uid = "+rt.getUid()+"; name = "+rt.getName()+" at "+ctime());
	      } else {
                System.out.println("Cancelling Cirrig zone "+rt.getZoneNumber()+" ("+rt.getName()+"); uid = "+rt.getUid()+" at "+ctime());
	      }
	      rt.cancel();
	      runDatabase.remove(rdid);
	    }
	  }
          updateDatabase(_mainClass+":nscheduled", runDatabase.size());
	}
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected synchronized void updateDb() {
      synchronized(runDatabase) {
        for (Iterator i = runDatabase.keySet().iterator(); i.hasNext(); ) {
	  String uid = (String)i.next();
	  CCropRealTime rt = (CCropRealTime)runDatabase.get(uid);
	  if (rt.readyToUpdate()) {
	    String query = null;
            Statement stmt = null;
	    if (rt.isZone()) {
	      query = rt.getZoneHistoryQuery();
	      System.out.println(_mainClass+"::updateDb | "+ctime()+"> Zone history query "+query);
	    } else {
	      String doy = rt.getDoy();
	      float irrig = rt.getIrrig();
	      int rid = rt.getId();
	      float yest = rt.getYest();
	      float avgIrrig = rt.getAvgIrrig();
	      float fiveMax = rt.getFiveMax();
	      query = "update runs set doy='" + doy + "', irrig=" + irrig + ", yestIrrig=" + yest + ", threeAvgIrrig="+avgIrrig+", fiveMax="+fiveMax+" where rid="+rid;
	      System.out.println(query);
	    }
	    try {
	      stmt = _dbCon.createStatement();
	      int nrows = stmt.executeUpdate(query);
	    } catch(SQLException e) {
	      e.printStackTrace();
	      _sqlConnected = false;
            } finally {
              if (stmt != null) try {
                stmt.close();
              } catch(Exception e) { _sqlConnected = false; }
            }
	  }
        }
      }
    }


//-----------------------------------------------------------------------------------------------------

    /* This helper method should be overridden to return the proper subclass
     * UFMMTClientThread.  It is used by inner class ListenThread so that
     * ListenThread does not have to be rewritten for each subclass
     */
    protected UFMMTClientThread getNewClientThread(Socket clsoc, int clientCount, boolean simMode) {
      return new CCropClientThread(clsoc, clientCount, simMode);
    }

    @Override
    protected void checkAncillaryThread() {
        /* Kill if heartbeat is 15 sec behind. AND NOT _initializing */
        if (_ancillaryRunning && _heartbeat < System.currentTimeMillis()/1000 - 15 && !_initializing) {
          System.out.println(_mainClass + "::checkAncillaryThread> Heartbeat is lagging 15 sec behind.  Killing ancillary thread at "+ctime());
          _ancillaryRunning = false;
          _ancillary.forceShutdown();
        }
        if (!_ancillary.isAlive()) {
          System.out.println(_mainClass + "::checkAncillaryThread> AncillaryThread "+_ancillary.getId()+" died!");
          _ancillary.shutdownLoops();
          _startupAncillaryThread();
          System.out.println(_mainClass + "::checkAncillaryThread> Starting new AncillaryThread: "+_ancillary.getId()+" at "+ctime());
        }
    }
//=====================================================================================================

    // Class AncillaryThread creates thread to poll device for status and send info to status clients

    protected class AncillaryThread extends UFMMTThreadedAgent.AncillaryThread {

        private String _className = getClass().getName();

        public AncillaryThread() {}

        public void run() {
          int n = 15;
          while(true) {
            while (_isRunning) {
              if (_shutdown) return;
              n++;
              if (n >= 5) {
                /* poll db params */
                n = 0;
                queryDb();
		updateDb();
		/* check sql connection */
		if (!_sqlConnected && !_initializing) {
		  System.out.println(_className+"::run> Attempting to re-establish connection to SQL database at "+ctime());
		  try {
		    _dbCon.close();
		  } catch(Exception e) {
		    System.out.println(_className+"::run> Error closing database connection.");
		    e.printStackTrace();
		  }
		  init();
		}
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

} //end of class CCropAgent

