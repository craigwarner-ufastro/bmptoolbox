package Weather; 
/**
 * Title:        WeatherAgent
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

public class WeatherAgent extends UFMMTThreadedAgent { 

    public static final
	String rcsID = "$Name:  $ $Id: WeatherAgent.java,v 1.6 2011/01/24 19:36:31 warner Exp $";

    protected String _mainClass = getClass().getName();
    protected String dbName = "mybmp";
    protected String dbHost = "localhost";
    protected boolean _sqlConnected = false;
    protected boolean _initialized = false;
    protected String dbUrl = "jdbc:mysql://localhost/mybmp?autoReconnect=true"; //set autoReconnect
    protected String sqlUser = "", sqlPass = "";
    protected String dbClass = "com.mysql.jdbc.Driver";
    protected Connection _dbCon; //database connection
    protected LinkedHashMap <Long, Integer> sessions;
    protected LinkedHashMap <Long, Integer> restoredSessions;
    protected Vector <String> _sqlUpdates;

//----------------------------------------------------------------------------------------

    public WeatherAgent( int serverPort, String[] args )
    {
	super(serverPort, args);
	_sqlUpdates = new Vector(10);
	options(args);
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
      //synchronized(database) {
        /* Database records specific to this agent */
      //}
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
          hibernate();
        }
	/* Send any queued up sql queries */
	sendSqlUpdates();
      }
      sessions = new LinkedHashMap(20);
      restoredSessions = new LinkedHashMap(20);
      readSessions();
      _initialized = true;
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

    protected void sendSqlUpdates() {
      if (_sqlUpdates == null) return;
      if (_sqlUpdates.size() == 0) return;
      boolean success = true;
      synchronized(_sqlUpdates) {
	Statement stmt = null;
	ResultSet rs = null;
	String query = null;
	for (int j = 0; j < _sqlUpdates.size(); j++) {
	  query = (String)_sqlUpdates.get(j);
          System.out.println(_mainClass+"::sendSqlUpdates> Sql query: "+query);
          try {
            stmt = _dbCon.createStatement();
            stmt.executeUpdate(query);
          } catch(SQLException e) {
            e.printStackTrace();
            _sqlConnected = false;
	    success = false;
	    break;
          } finally {
            if (stmt != null) try {
              stmt.close();
            } catch(Exception e) { _sqlConnected = false; }
          }
	}
        if (success) _sqlUpdates.clear();
      }
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
        if (getCmdName(req,j).equals("LOGIN")) {
	  /* LOGIN::user pass */
          String param = getCmdParam(req,j);
          String user = getReplyToken(param, 0);
          String pass = getReplyToken(param, 1);
	  int uid = -1;
	  long sid = -1;
	  if (_simMode) {
	    System.out.println(_mainClass+"::action> Simulating LOGIN");
	    responses.add("SIM LoggedIn -1 -1");
	    responses.add("Test Weather::-1::120.00::29.803::-82.410");
	    synchronized(sessions) {
	      sessions.put(new Long(-1), new Integer(-1));
	    }
	  } else {
	    String query = "select * FROM users where username='" + user + "' AND password='" + pass + "'";
	    Statement stmt = null;
	    ResultSet rs = null;
            try {
	      stmt = _dbCon.createStatement();
	      rs = stmt.executeQuery(query);
	      if (rs.next()) {
		uid = rs.getInt("uid");
		sid = System.currentTimeMillis();
                String[] tokens = param.split(" ");
		if (tokens.length > 2 && !getReplyToken(param, 2).equals("null")) sid = Long.parseLong(getReplyToken(param, 2));
                System.out.println(_mainClass+"::action> Success: uid = "+uid+"; sid = "+sid); 
		responses.add("SUCCESS LoggedIn "+uid+" "+sid);
	      } else {
		System.out.println(_mainClass+"::action> Error: username or password incorrect!");
		reply = new UFStrings("ERROR", "Username or password incorrect!");
		success = false;
	      }
	    } catch(SQLException e) {
	      System.out.println(_mainClass+"::action> Received SQL error "+e.toString()+" at "+ctime()+"; trying one more time!");
	      try {
		if (stmt != null) stmt.close();
		if (rs != null) rs.close();
                stmt = _dbCon.createStatement();
                rs = stmt.executeQuery(query);
                if (rs.next()) {
                  uid = rs.getInt("uid");
                  sid = System.currentTimeMillis();
                  System.out.println(_mainClass+"::action> Success: uid = "+uid+"; sid = "+sid);
                  responses.add("SUCCESS LoggedIn "+uid+" "+sid);
                } else {
                  System.out.println(_mainClass+"::action> Error: username or password incorrect!");
                  reply = new UFStrings("ERROR", "Username or password incorrect!");
                  success = false;
                }
	      } catch(SQLException e2) {
                System.out.println(_mainClass+"::action> Received SQL error "+e2.toString()+" at "+ctime()+"!  Failed login!");
	        e2.printStackTrace();
                reply = new UFStrings("ERROR", e.toString()); 
	        success = false;
	        _sqlConnected = false;
	      }
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
	    query = "select * from weatherStations where uid=" + uid;
	    try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
              while (rs.next()) {
		String weatherString = rs.getString("location")+"::"+rs.getInt("wsid")+"::"+rs.getString("elevation_ft")+"::"+rs.getString("lattitude")+"::"+rs.getString("longitude");
		responses.add(weatherString);
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
	    /* Update sessions hashmap */
	    synchronized(sessions) {
	      sessions.put(new Long(sid), new Integer(uid));
	    }
	  }
	  writeSessions();
	} else if (getCmdName(req,j).equals("RESTORE_SESSION")) {
          /* RESTORE_SESSION::user pass sid */
          String param = getCmdParam(req,j);
          String user = getReplyToken(param, 0);
          String pass = getReplyToken(param, 1);
          int uid = -1;
          long sid = -1;
	  try {
	    sid = Long.parseLong(getReplyToken(param, 2));
	  } catch(Exception e) {
            System.out.println(_mainClass+"::action> Error: username or password incorrect!");
            reply = new UFStrings("ERROR", "Username or password incorrect!");
            success = false;
	    break;
          }
          if (_simMode) {
            System.out.println(_mainClass+"::action> Simulating RESTORE_SESSION");
	    synchronized(sessions) {
              sessions.put(new Long(-1), new Integer(-1));
	    }
          } else {
            String query = "select * FROM users where username='" + user + "' AND password='" + pass + "'";
            Statement stmt = null;
            ResultSet rs = null;
            try {
              stmt = _dbCon.createStatement();
              rs = stmt.executeQuery(query);
              if (rs.next()) {
                uid = rs.getInt("uid");
                System.out.println(_mainClass+"::action> Success: uid = "+uid+"; sid = "+sid);
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
            if (success == false) break;
            /* Update sessions hashmap */
	    synchronized(sessions) {
              sessions.put(new Long(sid), new Integer(uid));
	    }
	  }
          writeSessions();
	} else if (getCmdName(req,j).equals("REFRESH")) {
          /* REFRESH::uid sid */
          String param = getCmdParam(req,j);
          int uid = -1;
          long sid = -1;
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
            sid = Long.parseLong(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          if (!sessions.containsKey(new Long(sid)) && restoredSessions.containsKey(new Long(sid))) {
	    //restore this session
	    System.out.println(_mainClass+"::action> Restoring session "+sid);
	    synchronized(sessions) {
	      sessions.put(new Long(sid), restoredSessions.get(sid));
	    }
	    writeSessions();
	  }
          if (!sessions.containsKey(new Long(sid))) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          } else {
            if (!sessions.get(sid).equals(new Integer(uid))) {
              System.out.println(_mainClass+"::action> Error: session id and user id don't match.");
              reply = new UFStrings("ERROR", "session id and user id don't match.");
              success = false;
              break;
            }
          }
          System.out.println(_mainClass+"::action> Success: uid = "+uid+"; sid = "+sid);
          responses.add("SUCCESS Refreshed "+uid+" "+sid);
          String query = "select * from weatherStations where uid=" + uid;
          Statement stmt = null;
          ResultSet rs = null;
          try {
            stmt = _dbCon.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
              String weatherString = rs.getString("location")+"::"+rs.getInt("wsid")+"::"+rs.getString("elevation_ft")+"::"+rs.getString("lattitude")+"::"+rs.getString("longitude");
              responses.add(weatherString);
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
        } else if (getCmdName(req,j).equals("LOGOUT")) {
          /* LOGOUT::uid sid */
          String param = getCmdParam(req,j);
          int uid = -1;
          long sid = -1;
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
            sid = Long.parseLong(getReplyToken(param, 1));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          if (!sessions.containsKey(new Long(sid)) && restoredSessions.containsKey(new Long(sid))) {
            //restore this session
            System.out.println(_mainClass+"::action> Restoring session "+sid);
	    synchronized(sessions) {
              sessions.put(new Long(sid), restoredSessions.get(sid));
	    }
            writeSessions();
          }
          if (!sessions.containsKey(new Long(sid))) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          } else {
            if (!sessions.get(sid).equals(new Integer(uid))) {
              System.out.println(_mainClass+"::action> Error: session id and user id don't match.");
              reply = new UFStrings("ERROR", "session id and user id don't match.");
              success = false;
              break;
            }
          }
	  synchronized(sessions) {
	    sessions.remove(new Long(sid));
	  }
	  writeSessions();
          System.out.println(_mainClass+"::action> Success! Logged out uid = "+uid+"; sid = "+sid);
          responses.add("SUCCESS LoggedOut "+uid+" "+sid);
          if (success == false) break;
        } else if (getCmdName(req, j).equals("ADD_WEATHER")) {
	  /* ADD_WEATHER::uid sid wsid date hour solar_radiation max_temp min_temp rain_in */
          String param = getCmdParam(req,j);
          int uid = -1;
          long sid = -1;
	  int wsid = -1;
	  String hour = getReplyToken(param, 3)+" "+getReplyToken(param, 4);
          String solarRad = getReplyToken(param, 5);
          String maxTemp = getReplyToken(param, 6);
          String minTemp = getReplyToken(param, 7);
          String rain = getReplyToken(param, 8);
          try {
            uid = Integer.parseInt(getReplyToken(param, 0));
            sid = Long.parseLong(getReplyToken(param, 1));
            wsid = Integer.parseInt(getReplyToken(param, 2));
          } catch(NumberFormatException nfe) {
            System.out.println(_mainClass+"::action> Error: Not logged in."); 
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
          }
          if (!sessions.containsKey(new Long(sid)) && restoredSessions.containsKey(new Long(sid))) {
            //restore this session
            System.out.println(_mainClass+"::action> Restoring session "+sid);
	    synchronized(sessions) {
              sessions.put(new Long(sid), restoredSessions.get(sid));
	    }
            writeSessions();
          }
	  if (!sessions.containsKey(new Long(sid))) {
            System.out.println(_mainClass+"::action> Error: Not logged in.");
            reply = new UFStrings("ERROR", "Not logged in.");
            success = false;
            break;
	  } else {
	    if (!sessions.get(sid).equals(new Integer(uid))) {
              System.out.println(_mainClass+"::action> Error: session id and user id don't match.");
              reply = new UFStrings("ERROR", "session id and user id don't match.");
              success = false;
              break;
	    }
	  }
	  try {
	    float solarRad_float = Float.parseFloat(solarRad);
	    float maxTemp_float = Float.parseFloat(maxTemp);
            float minTemp_float = Float.parseFloat(minTemp);
            float rain_float = Float.parseFloat(rain);
	  } catch (NumberFormatException nfe) {
	    System.out.println(_mainClass+"::action> Error in data format: solar_radiation="+solarRad+", max_temp="+maxTemp+", min_temp="+minTemp+", rain_in="+rain+"; skipping this line!");
	    responses.add("ERROR in data format: solar_radiation="+solarRad+", max_temp="+maxTemp+", min_temp="+minTemp+", rain_in="+rain+"; skipping this line!");
	    continue;
	  }
	  boolean update = false;
	  int hwid = -1;
	  String query = "select * from hourlyWeather where wsid="+wsid+" AND uid="+ uid+" AND hour='"+hour+"';";
	  Statement stmt = null;
	  ResultSet rs = null;
          try {
	    stmt = _dbCon.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
	      update = true;
	      hwid = rs.getInt("hwid");
              System.out.println(_mainClass+"::action> Update of weather entry with hwid="+hwid); 
            }
	  } catch(SQLException e) {
	    e.printStackTrace();
	    responses.add("ERROR "+e.toString());
            _sqlConnected = false;
          } finally {
            if (stmt != null) try {
              stmt.close();
            } catch(Exception e) { _sqlConnected = false; }
            if (rs != null) try {
              rs.close();
            } catch(Exception e) { _sqlConnected = false; }
          }
	  if (update) {
	    query = "update hourlyWeather set solar_radiation="+solarRad+", max_temp="+maxTemp+", min_temp="+minTemp+", rain_in="+rain+" where hwid="+hwid+";";
          } else {
            query = "insert into hourlyWeather(wsid, uid, hour, solar_radiation, max_temp, min_temp, rain_in) values ("+wsid+", "+uid+", '"+hour+"', "+solarRad+", "+maxTemp+", "+minTemp+", "+rain+");";
          }
	  System.out.println(_mainClass+"::action> Sql query: "+query);
          try {
            stmt = _dbCon.createStatement();
            stmt.executeUpdate(query);
            responses.add("SUCCESS "+query);
	  } catch(SQLException e) {
            e.printStackTrace();
            responses.add("ERROR "+e.toString());
            _sqlConnected = false;
	    _sqlUpdates.add(query);
          } finally {
            if (stmt != null) try {
              stmt.close();
            } catch(Exception e) { _sqlConnected = false; }
            if (rs != null) try {
              rs.close();
            } catch(Exception e) { _sqlConnected = false; }
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

    protected void writeSessions() {
	//loop over sessions and remove any that are more than 48 hours old
	long currTime = System.currentTimeMillis();
	synchronized(sessions) {
          for (Iterator i = sessions.keySet().iterator(); i.hasNext(); ) {
            Long sid = (Long)i.next();
	    //if (currTime - sid.longValue() > 86400L*2*1000) i.remove(); 
	  }
	  //now print remaining ones
          try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(installDir+"/etc/sessions"));
	    for (Iterator i = sessions.keySet().iterator(); i.hasNext(); ) {
	      Long sid = (Long)i.next();
	      Integer uid = (Integer)sessions.get(sid);
	      pw.println(sid.toString()+"::"+uid.toString());
            }
            pw.close();
          } catch (IOException e) {
            e.printStackTrace();
            System.out.println(_mainClass+"::writeSessions> ERROR: Could not create sessions file!");
            return;
          }
          System.out.println(_mainClass+"::writeSessions> Successfully saved sessions file!");
	}
    }

//-----------------------------------------------------------------------------------------------------

    protected void readSessions() {
        long currTime = System.currentTimeMillis();
        try {
          BufferedReader br = new BufferedReader(new FileReader(installDir+"/etc/sessions"));
	  while (br.ready()) {
	    String currLine = br.readLine();
	    if (currLine != null) {
	      String[] strs = currLine.split("::");
	      long sid = Long.parseLong(strs[0]);
	      if (currTime - sid <= 86400L*2*1000) {
		//less than 2 days
	        int uid = Integer.parseInt(strs[1]);
	        restoredSessions.put(new Long(sid), new Integer(uid));
	        System.out.println(_mainClass+"::readSessions> Restoring session "+sid+" from user "+uid);
	      }
	    }
	  }
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println(_mainClass+"::readSessions> ERROR: Could not read sessions file!");
          return;
        }
        System.out.println(_mainClass+"::readSessions> Successfully read sessions file!");
    }

//-----------------------------------------------------------------------------------------------------

    protected void checkSql() {
      if (_sqlConnected) return;
      init();
    } 


//-----------------------------------------------------------------------------------------------------

    protected void checkPingClients() {
      synchronized(_clientThreads) {
	for (int j = 0; j < _clientThreads.size(); j++) {
	  WeatherClientThread wct = (WeatherClientThread)_clientThreads.elementAt(j);
	  if (wct.fullClient() && !wct.getPingStatus()) {
	    /* This is a full client whose ping has not been received for 30 sec.  Network down? */
	    String host = wct.hostname();
	    for (int i = 0; i < _clientThreads.size(); i++) {
	      UFMMTClientThread ct = _clientThreads.elementAt(i);
	      /* Terminate all client threads from this host! */
	      System.out.println(ct.getThreadName()+": "+ct.hostname()+" "+host);
	      if (ct.hostname().equals(host)) {
                System.out.println(_mainClass+"::checkPingClients> Terminating client "+ct.getThreadName());
	        ct._terminate();
	      }
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
      return new WeatherClientThread(clsoc, clientCount, simMode);
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
		/* Check command socket ping statuses */
		checkPingClients();
                /* Check for new records */
                updateDatabase();
	        /* Check sql connection */
	        checkSql();
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

} //end of class WeatherAgent

