package CCROP;
/**
 * Title:        CCropRealTime
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a real-time run 
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
import org.tiling.scheduling.*;

public class CCropRealTime {
    private final Scheduler scheduler = new Scheduler();

    protected String _mainClass = getClass().getName();

    protected int uid = -1, _id = -1;
    protected String _name, _time;
    protected int hour, minute, zoneNumber;
    protected long timestamp;
    protected String runPath, userPath, command;
    protected boolean updated = false;
    protected float irrig = -1, yestIrrig = -1, avgIrrig = -1, fiveMax = -1;
    protected String doy = "", zoneQuery = null;
    protected Connection _dbCon = null;
    protected boolean zone = false;

    public CCropRealTime(int uid, int rid, String runName, String runTime, String runPath, String userPath, String command) {
      this.uid = uid;
      _id = rid;
      _name = runName;
      _time = runTime;
      this.runPath = runPath;
      this.userPath = userPath;
      this.command = command;
      int idx1 = runTime.indexOf(":");
      int idx2 = runTime.indexOf(":", idx1+1);
      hour = Integer.parseInt(runTime.substring(0, idx1));
      minute = Integer.parseInt(runTime.substring(idx1+1, idx2)); 
      timestamp = System.currentTimeMillis();
    }

    public CCropRealTime(int uid, int zid, String zoneName, int zoneNumber, String runTime, Connection dbCon) { 
      this.uid = uid;
      _id = zid;
      _name = zoneName;
      this.zoneNumber = zoneNumber;
      _time = runTime;
      _dbCon = dbCon;
      int idx1 = runTime.indexOf(":");
      int idx2 = runTime.indexOf(":", idx1+1);
      hour = Integer.parseInt(runTime.substring(0, idx1));
      minute = Integer.parseInt(runTime.substring(idx1+1, idx2));
      timestamp = System.currentTimeMillis();
      zone = true;
    }

    public void updateDatabaseLink(Connection dbCon) {
      _dbCon = dbCon;
    }

    public void start() {
        scheduler.schedule(new SchedulerTask() {
            public void run() {
	        if (zone) {
		  runCirrig();
		} else {
		  runCCrop();
		}
            }

	    /* Perform real-time ccrop run */
            private void runCCrop() {
		Calendar c = new GregorianCalendar();
		int todayDoy = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
                c.add(Calendar.DAY_OF_YEAR, -1);
                int yesterday = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
		c.add(Calendar.DAY_OF_YEAR, -2);
		int threeDaysAgo = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
		c.add(Calendar.DAY_OF_YEAR, -2);
                int fiveDaysAgo = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);

		System.out.println((new java.util.Date()).toString());
                System.out.println("Running: "+command+" -user "+userPath+" -run "+_name);
		System.out.println("\tfrom "+runPath);
		Vector v = new Vector();
		v.add(command);
		v.add("-user");
		v.add(userPath);
		v.add("-run");
		v.add(_name);
		ProcessBuilder pb = new ProcessBuilder(v);
		pb.directory(new File(runPath));
		try {
		  Process p = pb.start();
		  try {
		    p.waitFor();
		  } catch (InterruptedException e) { }
		  Vector <String> wthVec = new Vector();
		  BufferedReader br = new BufferedReader(new FileReader(runPath+userPath+_name+"b_weather.wth"));
		  while (br.ready()) {
		    wthVec.add(br.readLine());
		  }
		  wthVec.remove(wthVec.size()-1);
		  br.close();
		  String lastDay = (String)wthVec.lastElement();
		  System.out.println("\tInput file: "+userPath+_name+"_P_spec.txt");
		  br = new BufferedReader(new FileReader(runPath+userPath+_name+"_P_spec.txt"));
		  String wfname = "";
		  String s;
		  while (br.ready()) {
		    s = br.readLine();
		    if (s.startsWith("WFNAME")) {
		      s = br.readLine();
		      int idx = s.indexOf(" ");
		      if (idx == -1) idx = s.indexOf("\t");
		      wfname = s.substring(0, idx).replaceAll("\"", "");
		      break;
		    }
		  }
		  br.close();
		  System.out.println("\tWeather file: "+runPath+wfname);
		  br = new BufferedReader(new FileReader(runPath+wfname+".wth"));
		  boolean isNew = false;
		  while (br.ready()) {
		    s = br.readLine();
		    if (s.startsWith(lastDay.substring(0,7))) {
		      isNew = true;
		    } else if (isNew) {
		      wthVec.add(s);
		    }
		  }
		  br.close();
		  PrintWriter pw = new PrintWriter(new FileOutputStream(runPath+userPath+_name+"b_weather.wth"));
		  for (int j = 0; j < wthVec.size(); j++) {
		    pw.println(wthVec.elementAt(j));
		  }
		  lastDay = (String)wthVec.lastElement();
		  int ld = Integer.parseInt(lastDay.substring(0,7));
		  ld++;
		  lastDay = ""+ld+lastDay.substring(7);
		  pw.println(lastDay);
		  pw.close();

                  System.out.println("Running: "+command+" -user "+userPath+" -run "+_name+"b");
                  System.out.println("\tfrom "+runPath);
                  v = new Vector();
                  v.add(command);
                  v.add("-user");
                  v.add(userPath);
                  v.add("-run");
                  v.add(_name+"b");
                  pb = new ProcessBuilder(v);
                  pb.directory(new File(runPath));
                  p = pb.start();
                  try {
                    p.waitFor();
                  } catch (InterruptedException e) { }

                  br = new BufferedReader(new FileReader(runPath+userPath+_name+"b_dailyoutput.txt"));
		  String line = "";
		  for (int j = 0; j < 3; j++) line = br.readLine().trim();
		  String[] tokens = line.split("\\s+");
		  int ircol = -1;
		  for (int j = 0; j < tokens.length; j++) {
		    if (tokens[j].equals("Ir")) {
		      ircol = j;
		      break;
		    }
		  }
		  int ndays = 0;
		  float threeDayAvg = 0;
		  fiveMax = 0;
		  try {
                    while (br.ready()) {
                      line = br.readLine().trim();
		      tokens = line.split("\\s+");
		      int lineDoy = Integer.parseInt(tokens[1])+1000*Integer.parseInt(tokens[0]);
		      if (lineDoy >= fiveDaysAgo && lineDoy < todayDoy) {
			fiveMax = Math.max(fiveMax, Float.parseFloat(tokens[ircol]));
		      }
		      if (lineDoy >= threeDaysAgo && lineDoy < todayDoy) {
			ndays++;
			threeDayAvg += Float.parseFloat(tokens[ircol]);
		      }
		      if (lineDoy == yesterday) {
			yestIrrig = Float.parseFloat(tokens[ircol]);
		      }
                    }
		  } catch(Exception e) {
		    irrig = -1;
		    yestIrrig = -1;
		    avgIrrig = -1;
		    fiveMax = -1;
		    updated = false;
		    e.printStackTrace();
		    return;
		  }
		  if (ndays == 3) avgIrrig = threeDayAvg /= 3.0f;
                  br.close();
		  tokens = line.split("\\s+");
		  while (tokens[1].length() < 3) tokens[1] = "0"+tokens[1];
                  doy = tokens[0]+tokens[1];
		  try {
		    irrig = Float.parseFloat(tokens[ircol]);
		    updated = true;
		  } catch(NumberFormatException nfe) {
		    irrig = -1;
		    updated = false;
		  }
		  System.out.println(tokens[0]+tokens[1]+": "+irrig+"; Yesterday: "+yestIrrig+"; 3-day avg: "+avgIrrig+"; 5-day max: "+fiveMax);
		} catch (IOException e) {
		  e.printStackTrace();
		  return;
		}
            }

	    /* Perform cirrig calculations */ 
	    private void runCirrig() {
                Calendar c = new GregorianCalendar();
                int doy = c.get(Calendar.DAY_OF_YEAR);
		String zoneType;
		Zone zone = null;

		double[] tmax_hourly, tmin_hourly, solar_hourly, rain_hourly;
		boolean[] hasHourlyData; 
		int nhourly = 0;
		double weekly_rain_in = -1;

		/* Query zone first */
		String query = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zid = "+_id;
		Statement stmt = null;
		ResultSet rs = null;
                System.out.println((new java.util.Date()).toString());
		System.out.println("runCirrig> Running cirrig for zone "+_id);
		try {
		  synchronized(_dbCon) {
		    stmt = _dbCon.createStatement();
		    rs = stmt.executeQuery(query);
		  }
		  if (rs.next()) {
		    zoneType = rs.getString("zoneType");
		    if (zoneType.equals("ET-sprinkler") || zoneType.equals("ET-micro")) {
System.out.println("runCirrig> ET Query = "+query);
		      zone = new ETZone(rs);
                      rs.close();
                      stmt.close();
		    } else if (zoneType.equals("LF-sprinkler") || zoneType.equals("LF-micro")) {
System.out.println("runCirrig> LF Query = "+query);
		      zone = new LFZone(rs);
                      rs.close();
                      stmt.close();
		    } else {
                      System.out.println(_mainClass+"::runCirrig> Error!  Invalid zone type "+zoneType);
                      updated = false;
                      rs.close();
                      stmt.close();
                      return;
		    }
                  } else {
		    System.out.println(_mainClass+"::runCirrig> Error!  Could not find zone with id = "+_id);
		    updated = false;
                    rs.close();
                    stmt.close();
		    return;
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
		    for (int j = 0; j < 24; j++) {
		      if (hasHourlyData[j]) continue;
		      //Don't check uid to allow public access.  wsid is unique.
		      query = "select *, hour(hour) as h from hourlyWeather where wsid=" + zone.wsid + " AND HOUR(hour)=" + j + " ORDER BY hour DESC LIMIT 1;";
		      synchronized(_dbCon) {
			stmt = _dbCon.createStatement();
			rs = stmt.executeQuery(query);
		      }
		      if (rs.next()) {
			int h = rs.getInt("h");
			String[] dateAndTime = rs.getString("hour").split("\\s+");
			System.out.println(_mainClass+"::runCirrig> Could not find weather for "+dateAndTime[1]+". Using data from "+dateAndTime[0]+" instead.");
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
		} catch(SQLException e) {
		  System.out.println(_mainClass+"::runCirrig | "+ctime()+"> Error querying zone with id = "+_id);
		  e.printStackTrace();
		  updated = false;
		  return;
		} finally {
		  if (stmt != null) try {
		    stmt.close();
		  } catch(Exception e) {}
		  if (rs != null) try {
		    rs.close();
		  } catch(Exception e) {}
		}

		if (nhourly < 24) {
		  System.out.println(_mainClass+"::runCirrig> Error: Weather station wsid = "+zone.wsid+" only has "+nhourly+" datapoints!");
		  updated = false;
		  return;
		}
		// Accumulate hourly weather
		zone.setHourlyWeather(tmax_hourly, tmin_hourly, solar_hourly, rain_hourly, hasHourlyData);
                if (weekly_rain_in != -1) zone.setWeeklyRain(weekly_rain_in); //set weekly rain if found

		//Check prev deficit and set in zones
		double prevDeficit = 0;
		//query for any deficit in zone history from yesterday
                try {
                  query = "select histTime, deficit_in from zoneHistory where wsid=" + zone.wsid + " AND zid="+ zone.getId() + " AND uid=" + uid + " AND date(histTime) = curdate()-interval 1 day AND source=2 LIMIT 1;";
                  synchronized(_dbCon) {
                    stmt = _dbCon.createStatement();
                    rs = stmt.executeQuery(query);
                  }
                  if (rs.next()) {
                    //found deficit, start cum at this value.
		    prevDeficit = rs.getDouble("deficit_in");
                    System.out.println(_mainClass+"::runCirrig | "+ctime()+"> Found deficit of "+prevDeficit+" inches from zone "+_id+" at "+rs.getString("histTime"));
                  }
                } catch(SQLException e) {
                  System.out.println(_mainClass+"::runCirrig> Error querying zoneHistory with id = "+_id);
                  e.printStackTrace();
                  prevDeficit = 0;
                  //assume 0 deficit and continue in this case
                } finally {
                  if (stmt != null) try {
                    stmt.close();
                  } catch(Exception e) {}
                  if (rs != null) try {
                    rs.close();
                  } catch(Exception e) {}
                }
	        zone.setPrevDeficit(prevDeficit);
    
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
                    System.out.println(_mainClass+"::runCirrig | "+ctime()+"> Error querying zone with id = "+_id);
                    e.printStackTrace();
                    updated = false;
                    return;
                  } finally {
                    if (stmt != null) try {
                      stmt.close();
                    } catch(Exception e) {}
                    if (rs != null) try {
                      rs.close();
                    } catch(Exception e) {}
                  }
		}

		// get irrigation -- ET and LF zones will each handle proper calculations
		zoneQuery = zone.getIrrigation();
		updated = true;
		System.out.println(_mainClass+"::runCirrig | "+ctime()+"> Successfully updated zone "+_id+" with query "+zoneQuery);
	    }

        }, new DailyIterator(hour, minute, 0));
    }

    public void cancel() {
	scheduler.cancel();
    }

    public String getName() {
      return _name;
    }

    public void updateName(String newName) {
      _name = newName;
    }

    public int getUid() {
      return uid;
    }

    public int getZoneNumber() {
      return zoneNumber;
    }

    public String getTime() {
      return _time;
    }

    public void updateTime(String newTime) {
      _time = newTime;
      int idx1 = _time.indexOf(":");
      int idx2 = _time.indexOf(":", idx1+1);
      hour = Integer.parseInt(_time.substring(0, idx1));
      minute = Integer.parseInt(_time.substring(idx1+1, idx2));
    }

    public long getTimeStamp() {
      return timestamp;
    }

    public void updateTimeStamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public boolean readyToUpdate() {
      return updated;
    }

    public float getIrrig() {
      updated = false;
      return irrig;
    }

    public String getDoy() {
      return doy;
    }

    public int getId() {
      return _id;
    }

    public float getYest() {
      return yestIrrig;
    }

    public float getAvgIrrig() {
      return avgIrrig; 
    }

    public float getFiveMax() {
      return fiveMax;
    }

    public boolean isZone() {
      return zone;
    }

    public String getZoneHistoryQuery() {
      updated = false;
      return zoneQuery;
    }

    public String ctime() {
        String date = new java.util.Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }
}
