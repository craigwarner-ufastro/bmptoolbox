package CirrigPlc;
/**
 * Title:        ZoneGroup
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a Zone Group 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;
import CCROP.Zone;
import CCROP.ETZone;
import CCROP.LFZone;
import org.tiling.scheduling.*;

public class ZoneGroup {
    private final Scheduler scheduler = new Scheduler();

    String _mainClass = getClass().getName();
    protected CirrigIrrigator _irr;
    protected ArrayList<ZoneOutlet> _zoneoutlets;
    protected ArrayList<Counter> _counters;
    protected String name, ccropHost = "www.bmptoolbox.org";
    protected int ccropPort = 57002, weewxPort = 57006;
    protected int number, ncycles, cycleNumber, maxSimultaneous, nrunning, systemTestMinutes;
    protected boolean readyToIrrigate, marked = false, _local = false, _continuous = true, _cycleModeChanged=true;
    protected int irrigationDay; 
    protected ArrayList<IrrigationTask> _irrigationTasks;

    protected Socket ccropSocket, weewxSocket;
    protected int _greetTimeout = 6000, _reqTimeout = 20000;


    public ZoneGroup(CirrigIrrigator irr, String name, int num) { 
      _irr = irr;
      this.name = name;
      this.number = num;
      _zoneoutlets = new ArrayList();
      _counters = new ArrayList();
      _irrigationTasks = new ArrayList();
      ncycles = 0;
      cycleNumber = -1;
      irrigationDay = 0;
      maxSimultaneous = _irr.getMaxSimultaneous();
      nrunning = 0;
      systemTestMinutes = 2;
      readyToIrrigate = false;
      marked = false;
      _cycleModeChanged = true; //for first pass and any time mode changes
    }

    public void updateHostAndPort(String host, int port, int wport) {
      ccropHost = host;
      ccropPort = port;
      weewxPort = wport;
    }

    public void updateIrrigator(CirrigIrrigator irr) {
      _irr = irr;
    }

    public String getName() { return name; }
    public int getNumber() { return number; }
    public String getIrrigIP() { return _irr.getHost(); }
    public String getIrrigType() { return _irr.getType(); }
    public boolean isReadyToIrrigate() { return readyToIrrigate; }
    public int getNCycles() { return ncycles; }
    public String getCycleMode() { if (_continuous) return "continuous"; else return "daily"; }
    public int getCycleNumber() { return cycleNumber; }
    public int getMaxSimultaneous() { return maxSimultaneous; }
    public String getMode() { if (_local) return "local"; else return "remote"; }
    public int getSystemTestMinutes() { return systemTestMinutes; }

    public void rename(String newName) {
      this.name = newName;
    }

    public void setMaxSimultaneous(int max) { maxSimultaneous = max; }

    public void setNCycles(int n) { 
      ncycles = n;
      if (_irrigationTasks.size() > n) {
	//remove and cancel tasks if size decreased
	for (int i = _irrigationTasks.size()-1; i >= n; i--) {
	  IrrigationTask task = (IrrigationTask)_irrigationTasks.remove(i);
	  task.cancel();
	}
      }
    }

    public void setCycleMode(String mode) {
      if (!mode.equals(getCycleMode())) _cycleModeChanged = true;
      if (mode.equals("daily")) _continuous = false; else _continuous = true;
    }

    public void setMode(String mode) {
      if (mode.equals("local")) _local = true; else _local = false;
    }

    public void setSystemTestMinutes(int stmin) { systemTestMinutes = stmin; }

    public boolean checkAndUpdateTask(int cycle, int hour, int minute) {
      if (cycle >= _irrigationTasks.size()) {
	//add new task
	IrrigationTask task;
	if (cycle == 0) {
	  task = new FirstIrrigationTask(hour, minute);
	} else {
	  task = new AdditionalCycleIrrigationTask(hour, minute, cycle);
	}
        scheduler.schedule((SchedulerTask)task, new DailyIterator(hour, minute, 0));
        _irrigationTasks.add(task);
        //If it is currently schedule hour and minute, run manually
        Calendar c = new GregorianCalendar();
        System.out.println(_mainClass+"::checkAndUpdateTask | "+ctime()+"> Added new task, cycle "+cycle+" at "+hour+":"+minute+"; current time = "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
        if (c.get(Calendar.HOUR_OF_DAY) == hour && c.get(Calendar.MINUTE) == minute) task.run();
	return true; //set new task
      } else {
	IrrigationTask task = _irrigationTasks.get(cycle);
	if (task.getHour() == hour && task.getMinute() == minute) return false; //no need to updated
	//if we get here, hour and/or minute has been changed
	//cancel original task and start new one
	task.cancel();
        if (cycle == 0) {
          task = new FirstIrrigationTask(hour, minute);
        } else {
          task = new AdditionalCycleIrrigationTask(hour, minute, cycle);
        }
        scheduler.schedule((SchedulerTask)task, new DailyIterator(hour, minute, 0));
        _irrigationTasks.set(cycle, task);
        //If it is currently schedule hour and minute, run manually
        Calendar c = new GregorianCalendar();
        System.out.println(_mainClass+"::checkAndUpdateTask | "+ctime()+"> Replaced task, cycle "+cycle+" now at "+hour+":"+minute+"; current time = "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
        if (c.get(Calendar.HOUR_OF_DAY) == hour && c.get(Calendar.MINUTE) == minute) task.run();
        return true; //set new task
      }
    }

    public void clearMark() {
      marked = false;
    }

    public int getHour(int i) {
      if (i < _irrigationTasks.size()) return _irrigationTasks.get(i).getHour();
      return -1;
    }

    public int getMinute(int i) {
      if (i < _irrigationTasks.size()) return _irrigationTasks.get(i).getMinute();
      return -1;
    }

    public boolean isMarked() {
      return marked;
    }

    public void markToUpdateTasks() {
      marked = true;
    }

    public boolean addCounter(int counterNum) {
      //XML saves outlets by number
      if (!_irr.isCounterAvailable(counterNum)) return false; //outlet is in use
      synchronized(_counters) {
        Counter c = new Counter(counterNum);
        _counters.add(c);
      }
      return true;
    }

    public boolean addOutlet(String outletName) {
      //GUI refers to outlets by name
      int outletNum = _irr.getOutletNumber(outletName);
      if (outletNum == -1) return false; // could not find name
      if (!_irr.isOutletAvailable(outletNum)) return false; //outlet is in use 
      synchronized(_zoneoutlets) {
	int maxPriority = 0;
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
	  ZoneOutlet zo = (ZoneOutlet)i.next();
	  if (zo.getPriority() > maxPriority) maxPriority = zo.getPriority();
        }
        ZoneOutlet zo = new ZoneOutlet(_irr.getOutlet(outletNum), outletNum, _irr.getUid(), maxPriority+1); 
	_zoneoutlets.add(zo);
      }
      return true;
    }

    public boolean addOutlet(int outletNum) {
      //XML saves outlets by number
      if (!_irr.isOutletAvailable(outletNum)) return false; //outlet is in use
      synchronized(_zoneoutlets) {
        int maxPriority = 0;
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
          if (zo.getPriority() > maxPriority) maxPriority = zo.getPriority();
        }
        ZoneOutlet zo = new ZoneOutlet(_irr.getOutlet(outletNum), outletNum, _irr.getUid(), maxPriority+1);
        _zoneoutlets.add(zo);
      }
      return true;
    }

    public ArrayList<Counter> getCounters() {
      return _counters;
    }

    public ArrayList<String> getGroupInfo() {
      ArrayList<String> list = new ArrayList();
      list.add("ZoneGroup::"+getIrrigIP()+"::"+getName()+"::"+getNumber());
      synchronized(_zoneoutlets) {
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
	  //ZoneOutlet::192.168.1.1::GroupName::1::-1
	  list.add("ZoneOutlet::"+getIrrigIP()+"::"+getName()+"::"+_irr.getOutletName(zo.getOutletNumber())+"::"+zo.getOutletNumber()+"::"+zo.getId()+"::"+zo.getZoneName());
        }
      }
      return list;
    }

    public ArrayList<ZoneOutlet> getZoneOutletsByPriority() {
      synchronized(_zoneoutlets) {
        Collections.sort(_zoneoutlets, new Comparator<ZoneOutlet>() {
          @Override public int compare(ZoneOutlet zo1, ZoneOutlet zo2) {
            return zo1.getPriority() - zo2.getPriority(); // Ascending
          }
        });
      }
      readyToIrrigate = false;
      return _zoneoutlets;
    }

    public int getNRunning() {
      int nr = 0;
      synchronized(_zoneoutlets) {
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
	  if (zo.running()) nr++;
	}
      }
      return nr;
    }

    public ZoneOutlet getZoneOutletByName(String outletName) {
      int outletNum = _irr.getOutletNumber(outletName);
      if (outletNum == -1) return null; // could not find name
      return getZoneOutletByNumber(outletNum);
    }

    public ZoneOutlet getZoneOutletByNumber(int outletNum) {
      synchronized(_zoneoutlets) {
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
          if (zo.getOutletNumber() == outletNum) return zo; 
        }
      }
      return null;
    }

    public boolean hasCounter(int counterNum) {
      synchronized(_counters) {
        Iterator<Counter> i = _counters.iterator();
        while (i.hasNext()) {
          Counter c = (Counter)i.next();
          if (c.getCounterNumber() == counterNum) return true;
        }
      }
      return false;
    }

    public boolean hasOutlet(int outletNum) {
      synchronized(_zoneoutlets) {
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
          if (zo.getOutletNumber() == outletNum) return true;
        }
      }
      return false;
    }

    public boolean propagateRecord(int outlet, String recName, String value) {
      ZoneOutlet zo = getZoneOutletByNumber(outlet);
      if (zo == null) return false;
      if (recName.equals("defaultIrrigation")) {
	zo.setDefMethod(value);
	return true;
      } else if (recName.equals("manualDefault")) {
	try {
	  zo.setDefIrrig(Float.parseFloat(value));
	  return true;
	} catch(NumberFormatException nfe) {
	  return false;
        }
      } else if (recName.equals("priority")) {
	try {
	  zo.setPriority(Integer.parseInt(value));
	  return true;
        } catch(NumberFormatException nfe) {
          return false;
        }
      } else return false;
    }

    public boolean removeCounter(int counterNum) {
      synchronized(_counters) {
        Iterator<Counter> i = _counters.iterator();
        while (i.hasNext()) {
          Counter c = (Counter)i.next();
          if (c.getCounterNumber() == counterNum) {
            _counters.remove(c);
            return true;
          }
        }
      }
      return false;
    }

    public boolean removeOutlet(String outletName) {
      //GUI refers to outlets by name
      int outletNum = _irr.getOutletNumber(outletName);
      if (outletNum == -1) return false; // could not find name
      synchronized(_zoneoutlets) {
        Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
          if (zo.getOutletNumber() == outletNum) {
	    _zoneoutlets.remove(zo);
	    return true;
	  } 
        }
      }
      return false;
    }

    /* Helper methods to parse params */

    public String getReplyToken(UFStrings reply, int token) {
      return getReplyToken(reply, token, " ");
    }

    public String getReplyToken(UFStrings reply, int token, String delim) {
      String[] vals = reply.stringAt(0).split(delim);
      if (vals.length > token) return vals[token];
      System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
      return "null";
    }

    public String getReplyToken(String reply, int token) {
      return getReplyToken(reply, token, " ");
    }

    public String getReplyToken(String reply, int token, String delim) {
      String[] vals = reply.split(delim);
      if (vals.length > token) return vals[token];
      System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
      return "null";
    }

    protected boolean connectToCCrop() {
      try {
        //try connecting to ccrop
        System.out.println(_mainClass+"::connectToCcrop | "+ctime()+"> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
        ccropSocket = new Socket(ccropHost, ccropPort);
        ccropSocket.setSoTimeout(_greetTimeout); //timeout for greetings

        //send timestamp greeting
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
        greet.sendTo(ccropSocket);

        //receive response
        UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
        if (ufpr == null) {
          System.out.println(_mainClass+"::connectToCcrop | "+ctime()+"> received null object!  Closing socket!");
          ccropSocket.close();
	  return false;
        }
        String request = ufpr.name().toLowerCase();
        if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
          System.out.println(_mainClass+"::connectToCcrop | "+ctime()+"> connection established: "+request);
	  ccropSocket.setSoTimeout(_reqTimeout);
	  return true;
        } else {
          System.out.println(_mainClass+"::connectToCcrop | "+ctime()+"> received "+request+".  Closing socket!");
          ccropSocket.close();
        }
      } catch (IOException ioe) {
        System.out.println(_mainClass+"::connectToCcrop | "+ctime()+"> Error connecting: "+ioe.toString());
      }
      return false;
    }

    public boolean connectToWeewx() {
      try {
        //try connecting to weewx agent
        System.out.println(_mainClass+"::connectToWeewx | "+ctime()+"> Trying to connect to local weewx agent on port = "+weewxPort);
        weewxSocket = new Socket("localhost", weewxPort);
        weewxSocket.setSoTimeout(_greetTimeout); //timeout for greetings
        //send timestamp greeting
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
        greet.sendTo(weewxSocket);
        //receive response
        UFProtocol ufpr = UFProtocol.createFrom(weewxSocket);
        if (ufpr == null) {
          System.out.println(_mainClass+"::connectToWeewx | "+ctime()+"> ERROR: Failed connecting to weewx agent!");
          weewxSocket.close();
          return false;
        }
        String request = ufpr.name().toLowerCase();
        if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
          System.out.println(_mainClass+"::connectToWeewx | "+ctime()+"> connection established: "+request);
	  //4/2/19 set timeout to longer 20 second _reqTimeout
          weewxSocket.setSoTimeout(_reqTimeout);
	  return true;
        } else {
          System.out.println(_mainClass+"::connectToWeewx | "+ctime()+"> received "+request+".  Closing socket!");
          weewxSocket.close();
        }
      } catch (IOException ioe) {
        System.out.println(_mainClass+"::connectToWeewx | "+ctime()+"> Error connecting: "+ioe.toString());
        return false;
      }
      return false; 
    }

    protected void getIrrig() {
	//1) Setup connections, Calendar, and some vars
	//2) Loop over zone outlets
		//A) Disabled/Fixed
		//B) FixedWithRain - query rain
		//C) CCrop
		//D) Calc local / ccrop failed
	//3) Update zones 
        System.out.println(_mainClass+"::getIrrig> "+(new java.util.Date()).toString());
        int ntries = 0;
        boolean connected = false, weewxConnected = false, useWeewx = false;
        String lastCycleDate = null;
        if (_continuous) {
          SimpleDateFormat lastCycledf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Calendar c2 = new GregorianCalendar();
          c2.set(Calendar.SECOND, 0);
          if (cycleNumber == 0) {
            c2.add(Calendar.DAY_OF_MONTH, -1); //get yesterday's time
            c2.set(Calendar.HOUR_OF_DAY, getHour(ncycles-1));
            c2.set(Calendar.MINUTE, getMinute(ncycles-1));
          } else {
            c2.set(Calendar.HOUR_OF_DAY, getHour(cycleNumber-1));
            c2.set(Calendar.MINUTE, getMinute(cycleNumber-1));
          }
          lastCycleDate = lastCycledf.format(c2.getTime());
        }
        while (!_local && ntries < 3 && !connected) {
          ntries++;
          connected = connectToCCrop();
        }
        if (!connected && !_local) {
          //failed 3 times
          System.out.println(_mainClass+"::getIrrig | "+ctime()+"> ERROR: Failed connecting to "+ccropHost+" 3 times!");
        }
        if (!connected) {
          System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Will attempt to use local zone info and weather!");
          weewxConnected = connectToWeewx();
          if (!weewxConnected) {
            System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Could not connect to weewx locally.");
            //return;
          } else useWeewx = true; //2/25/19 need new boolean so that having fixedWithRain zones doesn't force all zones to use weewx
        }

	//setup ArrayDeques -- only need fixedWithRain and cirrig
	ArrayDeque<ZoneOutlet> fixedWithRainOutlets = new ArrayDeque();
	ArrayDeque<ZoneOutlet> cirrigOutlets = new ArrayDeque();
        boolean success = false; 
        synchronized(_zoneoutlets) {
          Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
            if (!zo.isEnabled()) {
              //shouldn't happen
              zo.setIrrig(0);
              zo.setUpdated(false);
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to None.");
            } else if (zo.isFixed()) {
              zo.setIrrig(zo.getDefIrrig());
              zo.setUpdated(true);
              zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to FIXED value of "+zo.getIrrig());
            } else if (zo.isFixedWithRain()) {
	      fixedWithRainOutlets.add(zo);
	    } else {
	      cirrigOutlets.add(zo);
	    }
	  }
	  //Nothing else to be done for !isEnabled or isFixed
	  //Next while still in synchroinzed block, handle fixedWithRain
	  processFixedWithRainOutlets(fixedWithRainOutlets, lastCycleDate, weewxConnected);
	  //Next process cirrigOutlets - try ccrop first
	  if (connected && !useWeewx) {
	    success = processCCropOutlets(cirrigOutlets, lastCycleDate);
	  }
          if (!weewxConnected && !success) {
            //for some reason, connected but didn't update, try connecting to weewx
            weewxConnected = connectToWeewx();
            if (!weewxConnected) {
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Could not connect to weewx locally.");
              useWeewx = false;
            } else useWeewx = true; //2/25/19 need new boolean so that having fixedWithRain zones doesn't force all zones to use weewx
          }
          if (useWeewx) {
	    success = processLocalOutlets(cirrigOutlets, lastCycleDate);
	  }
	}
	//do this outside of synchronized block because need to iterate over _zoneoutlets
        if (success) updateZones(); //if successful, try to update zones from CCrop Agent
        readyToIrrigate = true;
    }

    protected synchronized void processFixedWithRainOutlets(ArrayDeque<ZoneOutlet> fixedWithRainOutlets, String lastCycleDate, boolean weewxConnected) {
        if (fixedWithRainOutlets.size() == 0) return; //no fixedWithRainOutlets
        UFStrings req, reply;
        LinkedHashMap<String, String> fixedRainValues = new LinkedHashMap(3);
	Iterator<ZoneOutlet> i = fixedWithRainOutlets.iterator();
	while (i.hasNext()) {
	  ZoneOutlet zo = (ZoneOutlet)i.next();
          zo.setIrrig(zo.getDefIrrig());
          //try to connect if not already connected
          if (!weewxConnected) weewxConnected = connectToWeewx();
          if (weewxConnected) {
            //query weekly rain since last cycle time
            double rain_in = -1;
            //wsid not required so use -1
            int wsid = -1;
            String request = "GET_RAIN_SINCE::"+zo.getUid()+" "+wsid+" "+lastCycleDate;
            if (fixedRainValues.containsKey(request)) {
              //get previously obtained value
              System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Re-using rain value previously obtained for user "+zo.getUid()+"; id="+zo.getId()+"; date "+lastCycleDate);
              rain_in = Double.parseDouble(fixedRainValues.get(request));
            } else {
              req = new UFStrings(_mainClass+": actionRequest", request);
              System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending rain request for user "+zo.getUid()+"; id="+zo.getId());
              req.sendTo(weewxSocket);
              reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
              if (reply == null) {
                System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Warning: No response to weekly rain from weewx agent for zone "+zo.getId()+"!  Ignoring weekly_rain_thresh.");
              } else if (getReplyToken(reply, 0, "::").equals("RAIN")) {
                rain_in = Double.parseDouble(getReplyToken(reply, 1, "::"));
                //put value into LinkedHashMap
                fixedRainValues.put(request, getReplyToken(reply, 1, "::"));
              }
            }
            if (rain_in != -1) {
              double rain_cm = rain_in*2.54; //convert to CM
              if (rain_cm >= zo.getDefIrrig()) {
                zo.setIrrig(0); //rain equals or exceeds irrigation; don't irrigate!
                System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Rain = "+rain_cm+" equals or exceeds irrigation amount of "+zo.getDefIrrig()+"; Will NOT irrigate!.");
              } else System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Rain = "+rain_cm+" is under irrigation amount of "+zo.getDefIrrig()+".  Will irrigate full fixed amount.");
            } else {
              System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Warning: could not get rain value from weewx agent.  Will irrigate full fixed amount.");
            }
          } else {
            System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Warning: Can NOT connect to weewx agent to calculate rain since last cycle.  Will irrigate full fixed amount.");
          }

          zo.setUpdated(true);
          zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
          System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to FIXED value of "+zo.getIrrig());
	}
    }

    protected synchronized boolean processCCropOutlets(ArrayDeque<ZoneOutlet> cirrigOutlets, String lastCycleDate) {
	boolean success = true;
	if (cirrigOutlets.size() == 0) return true; //no cirrigOutlets
        UFStrings req, reply;
	//create Vector object for requests
	Vector<String> commandVec = new Vector();
        String day, currReply;
        float irrig = -1, irrigMin = -1, irrigRate = -1, defIrrig = -1, prevDeficit = 0;
        boolean updated = false;
        boolean useDefault = false, error = false;

        Iterator<ZoneOutlet> i = cirrigOutlets.iterator();
        //Request: GET_IRRIGATION::uid [Z|R] id defMethod
        //Response: SUCCESS irrig irrigMin irrigRate defIrrig histTime 
        //3/6/13 Changed irrigRate to cm/hour
        //11/15/16 -- also allow request CALCULATE_IRRIGATION::uid id continuous for real-time calculation
        //Response: SUCCESS irrig irrigMin irrigRate defIrrig histTime -- same response, but it also will update database on server with history
	//7/10/19 -- send all requests in one UFStrings object and parse all results
        while (i.hasNext()) {
          ZoneOutlet zo = (ZoneOutlet)i.next();
          //normal method of querying ccrop agent -- check this before querying localhost in case this fails
          if (_continuous) {
	    commandVec.add("CALCULATE_IRRIGATION::"+zo.getUid()+" "+zo.getId()+" continuous "+lastCycleDate);
	    System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending CALCULATE_IRRIGATION request for user "+zo.getUid()+"; id="+zo.getId());
          } else {
	    commandVec.add("GET_IRRIGATION::"+zo.getUid()+" Z "+zo.getId()+" "+zo.getDefMethod());
            req = new UFStrings(_mainClass+": actionRequest", "GET_IRRIGATION::"+zo.getUid()+" Z "+zo.getId()+" "+zo.getDefMethod());
            System.out.println(_mainClass+"::::processCCropOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending GET_IRRIGATION request for user "+zo.getUid()+"; id="+zo.getId()+"; default="+zo.getDefMethod());
          }
	}
	//Create UFStrings from Vector and send request
	//Replies will be in same order
	req = new UFStrings(_mainClass+": actionRequest", commandVec);
        req.sendTo(ccropSocket);
        reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
        if (reply == null) {
          //try one more time, try reconnecting to ccrop
          try {
            ccropSocket.close();
          } catch(IOException ioe) {}
          try {
            Thread.sleep(5000);
          } catch(InterruptedException e) {} //sleep 5s
          boolean connected = connectToCCrop();
          if (!connected) {
	    //failed to connect on second try - setUpdated = false and return false.  Try with weewx and local calculations
            i = cirrigOutlets.iterator();
            while (i.hasNext()) {
              ZoneOutlet zo = (ZoneOutlet)i.next();
              zo.setUpdated(false);
	    }
            System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Failed connecting to CCrop agent!");
            return false;
          }
          System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Attempt 2: sending "+commandVec.size()+" requests to CCROP Agent...");
          req.sendTo(ccropSocket);
          reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
          if (reply == null) {
	    //failed a second time - setUpdated = false and return false.  Try with weewx and local calculations
	    i = cirrigOutlets.iterator();
            while (i.hasNext()) {
              ZoneOutlet zo = (ZoneOutlet)i.next();
              zo.setUpdated(false);
	    }
	    return false;
          }
        }
	//check size of return reply
	if (reply.numVals() != cirrigOutlets.size()) {
	  System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> ERROR: Size mismatch between cirrig outlets ("+cirrigOutlets.size()+") and reply size ("+reply.numVals()+")!");
          //setUpdated = false and return false.  Try with weewx and local calculations
          i = cirrigOutlets.iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
            zo.setUpdated(false);
          }
	  return false;
	}
	//if same return size, iterate over outlets / replies
	int j = 0;
	i = cirrigOutlets.iterator();
	while (i.hasNext()) {
	  updated = false; //need to reset at each iteration through loop
	  ZoneOutlet zo = (ZoneOutlet)i.next();
	  currReply = reply.stringAt(j);
	  j++; //increment reply
	  if (getReplyToken(currReply, 0).equals("SUCCESS")) {
            day = getReplyToken(currReply, 5);
            try {
              //convert irrigation to cm!! ** 8/20/13 Only if not -1 **
              irrig = Float.parseFloat(getReplyToken(currReply, 1));
              if (irrig >= 0) irrig *= 2.54f;
              irrigMin = Float.parseFloat(getReplyToken(currReply, 2));
              //convert irrigation rate to cm/hr from in/hr
              irrigRate = Float.parseFloat(getReplyToken(currReply, 3));
              if (irrigRate >= 0) irrigRate *= 2.54f;
              if (_continuous) {
                prevDeficit = Float.parseFloat(getReplyToken(currReply, 4));
                Zone zone = zo.getZone();
                if (zone != null) {
                  zone.setPrevDeficit(prevDeficit);
                  zone.setLastRunDate(day);
                }
              } else {
                defIrrig = Float.parseFloat(getReplyToken(currReply, 4));
                if (defIrrig >= 0) defIrrig *= 2.54f;
                if (getReplyToken(currReply, 1).equals("-1")) useDefault = true;
              }
              updated = true;
            } catch (NumberFormatException nfe) {
              System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Invalid irrigation value: "+reply.stringAt(0));
              irrig = -1;
              irrigMin = -1;
              irrigRate = -1;
              defIrrig = -1;
              updated = false;
            }
            if (updated) {
              System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": received date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);
            }
          } else {
            System.out.println(_mainClass+"::processCCropOutlets> Error: "+reply.stringAt(0));
            irrig = -1;
            defIrrig = -1;
            updated = false;
            error = true;
          }
          zo.setIrrig(irrig);

          if (useDefault) {
            if (zo.getDefMethod().toLowerCase().equals("manual default")) {
              System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Using manual default value "+zo.getDefIrrig());
              //use manual default
              zo.setIrrig(zo.getDefIrrig());
            } else {
              //default given by ccrop agent
              System.out.println(_mainClass+"::processCCropOutlets | "+ctime()+"> Using default value "+defIrrig+" from method "+zo.getDefMethod());
              zo.setIrrig(defIrrig);
            }
          }
          //zo.setIrrigMin(irrigMin);
          zo.setIrrigRate(irrigRate);
          //zo.setDefIrrig(defIrrig);
          zo.setUpdated(updated);
          if (updated) zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
        }
	if (error) return false;
	return success;
    }

    protected synchronized boolean processLocalOutlets(ArrayDeque<ZoneOutlet> cirrigOutlets, String lastCycleDate) {
        boolean success = true;
        if (cirrigOutlets.size() == 0) return true; //no cirrigOutlets
        UFStrings req, reply;

        String day=null;
        float irrig = -1, irrigMin = -1, irrigRate = -1, defIrrig = -1, prevDeficit = 0;
        boolean updated = false;
        boolean useDefault = false, error = false;

        double[] tmax_hourly = new double[24];
        double[] tmin_hourly = new double[24];
        double[] solar_hourly = new double[24];
        double[] rain_hourly = new double[24];
        boolean[] hasHourlyData = new boolean[24];

        double weekly_rain_in = -1;

        //Iterate over outlets
        Iterator<ZoneOutlet> i = cirrigOutlets.iterator();
	int lastUid = -1, lastWsid = -1;
	String lastTestDate = "";
        while (i.hasNext()) {
          updated = false; //need to reset at each iteration through loop
          ZoneOutlet zo = (ZoneOutlet)i.next();
          Zone zone = zo.getZone();
	  if (zo.getUid() == lastUid && zo.getZone().getWsid() == lastWsid) {
	    //use previously obtained values - no need to update arrays
	    System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Re-using weather and weekly rain values previously obtained for user "+zo.getUid()+"; wsid="+zo.getZone().getWsid());
	  } else {
            req = new UFStrings(_mainClass+": actionRequest", "GET_WEATHER::"+zo.getUid()+" "+zo.getZone().getWsid());
            System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending weather request for user "+zo.getUid()+"; id="+zo.getId());
            req.sendTo(weewxSocket);
            reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
            if (reply == null) {
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: No response from weewx agent for zone "+zo.getId());
              zo.setUpdated(false);
	      error = true;
              continue;
            }
            if (getReplyToken(reply, 0, "::").equals("WEATHER")) {
              //handle weather here
              tmax_hourly = new double[24];
              tmin_hourly = new double[24];
              solar_hourly = new double[24];
              rain_hourly = new double[24];
              hasHourlyData = new boolean[24];
              day = null;
              for (int j = 0; j < 24; j++) {
                String param = getCmdParam(reply,j);
                hasHourlyData[j] = false;
                day = getReplyToken(param, 0)+" "+getReplyToken(param, 1);
                try {
                  solar_hourly[j] = Double.parseDouble(getReplyToken(param, 2));
                  tmax_hourly[j] = Double.parseDouble(getReplyToken(param, 3));
                  tmin_hourly[j] = Double.parseDouble(getReplyToken(param, 4));
                  rain_hourly[j] = Double.parseDouble(getReplyToken(param, 5));
                  hasHourlyData[j] = true;
                } catch(NumberFormatException nfe) {
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not parse weather for hour "+j+": "+param);
                  hasHourlyData[j] = false;
                }
              }
              //also query weekly rain
              req = new UFStrings(_mainClass+": actionRequest", "GET_WEEKLY_RAIN::"+zo.getUid()+" "+zo.getZone().getWsid());
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending weekly rain request for user "+zo.getUid()+"; id="+zo.getId());
              req.sendTo(weewxSocket);
              reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
              if (reply == null) {
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: No response to weekly rain from weewx agent for zone "+zo.getId()+"!  Ignoring weekly_rain_thresh.");
              } else if (getReplyToken(reply, 0, "::").equals("WEEKLY_RAIN")) {
                weekly_rain_in = Double.parseDouble(getReplyToken(reply, 1, "::"));
		//Got replies to both queries - update variables
		lastUid = zo.getUid();
		lastWsid = zo.getZone().getWsid();
              }
	    } else {
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not get local weather for past 24 hours from weewx agent for zone "+zo.getId()+". Respose = "+reply.toString());
              zo.setUpdated(false);
	      error = true;
              continue;
	    }
	  }
          //Accumulate hourly weather
          zone.setHourlyWeather(tmax_hourly, tmin_hourly, solar_hourly, rain_hourly, hasHourlyData);
          if (weekly_rain_in != -1) zone.setWeeklyRain(weekly_rain_in); //set weekly rain if found

	  if (zone.getZoneType().startsWith("LF")) {
            double[] tmax_lf = new double[24];
            double[] tmin_lf = new double[24];
            double[] solar_lf = new double[24];
            double[] rain_lf = new double[24];
            boolean[] hasHourlylf = new boolean[24];
            if (zo.getUid() == lastUid && zo.getZone().getWsid() == lastWsid && zo.getZone().getLFTestDate().equals(lastTestDate)) {
	      //first pass through lastTestDate won't match
              System.out.println(_mainClass+"::processFixedWithRainOutlets | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Re-using LF weather for date "+zo.getZone().getLFTestDate()+" previously obtained for user "+zo.getUid()+"; wsid="+zo.getZone().getWsid());
	      //update here
              zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
	    } else {
              //query weewx for weather on LF test date
              req = new UFStrings(_mainClass+": actionRequest", "GET_LF_WEATHER::"+zo.getUid()+" "+zo.getZone().getWsid()+" "+zo.getZone().getLFTestDate());
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending LF weather request for date "+zo.getZone().getLFTestDate());
              req.sendTo(weewxSocket);
              reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
              if (reply == null) {
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: No response from weewx agent for zone "+zo.getId()+". Using existing LF Test Date weather data.");
              } else if (getReplyToken(reply, 0, "::").equals("WEATHER")) {
                //handle weather here
                tmax_lf = new double[24];
                tmin_lf = new double[24];
                solar_lf = new double[24];
                rain_lf = new double[24];
                hasHourlylf = new boolean[24];
                day = null;
                for (int j = 0; j < 24; j++) {
                  String param = getCmdParam(reply,j);
                  hasHourlylf[j] = false;
                  day = getReplyToken(param, 0)+" "+getReplyToken(param, 1);
                  try {
                    solar_lf[j] = Double.parseDouble(getReplyToken(param, 2));
                    tmax_lf[j] = Double.parseDouble(getReplyToken(param, 3));
                    tmin_lf[j] = Double.parseDouble(getReplyToken(param, 4));
                    rain_lf[j] = Double.parseDouble(getReplyToken(param, 5));
                    hasHourlylf[j] = true;
                  } catch(NumberFormatException nfe) {
                    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not parse LF weather for hour "+j+": "+param);
                    hasHourlylf[j] = false;
                  }
                }
		//update lastTestDate
		lastTestDate = zo.getZone().getLFTestDate();
                zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
              }
            }
	  }

          if (_continuous && zone.getLastRunDate() == null) zone.setLastRunDate(lastCycleDate); //update last run date to by last cycle date
          System.out.println("Prev deficit: "+zone.getPrevDeficit()+" "+zone.getLastRunDate());
          double[] irrigVals = zone.getIrrigValue(_continuous);
          irrig = (float)irrigVals[0];
          irrigMin = (float)irrigVals[1];
          irrigRate = (float)zone.getIrrigRate();
          //12/11/16 need to convert to cm as above with GET_IRRIGATION!  irrig and irrigRate are in inches during calcs!
          if (irrig >= 0) irrig *= 2.54f;
          if (irrigRate >= 0) irrigRate *= 2.54f;
          double deficit = irrigVals[2]; //leave deficit in inches as inches are used in calcs
          zone.setPrevDeficit(deficit);
          if (_continuous) {
            SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String lastRunDate = sqldf.format(new java.util.Date());
            zone.setLastRunDate(lastRunDate);
          }
          updated = true;
          zo.setIrrig(irrig);
          zo.setIrrigRate(irrigRate);
          zo.setUpdated(updated);
          if (updated) zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
          System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": received date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);
        } 
	return success;
    }

    protected void getIrrig_old() {
	Calendar c = new GregorianCalendar();
	irrigationDay = c.get(Calendar.DAY_OF_YEAR); //set irrigationDay
	System.out.println(_mainClass+"::getIrrig> "+(new java.util.Date()).toString());
	int ntries = 0;
        boolean connected = false, weewxConnected = false, useWeewx = false;
	String lastCycleDate = null;
	if (_continuous) {
          SimpleDateFormat lastCycledf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  Calendar c2 = new GregorianCalendar();
          c2.set(Calendar.SECOND, 0);
	  if (cycleNumber == 0) {
	    c2.add(Calendar.DAY_OF_MONTH, -1); //get yesterday's time
	    c2.set(Calendar.HOUR_OF_DAY, getHour(ncycles-1));
	    c2.set(Calendar.MINUTE, getMinute(ncycles-1)); 
	  } else {
	    c2.set(Calendar.HOUR_OF_DAY, getHour(cycleNumber-1));
	    c2.set(Calendar.MINUTE, getMinute(cycleNumber-1)); 
	  }
	  lastCycleDate = lastCycledf.format(c2.getTime());
	}
	while (!_local && ntries < 3 && !connected) {
	  ntries++;
	  connected = connectToCCrop();
        }
	if (!connected && !_local) {
	  //failed 3 times
	  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> ERROR: Failed connecting to "+ccropHost+" 3 times!"); 
	}
	if (!connected) {
	  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Will attempt to use local zone info and weather!");
	  weewxConnected = connectToWeewx();
	  if (!weewxConnected) {
	    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Could not connect to weewx locally.");
	    //return;
	  } else useWeewx = true; //2/25/19 need new boolean so that having fixedWithRain zones doesn't force all zones to use weewx
        }
	//Request: GET_IRRIGATION::uid [Z|R] id defMethod
	//Response: SUCCESS irrig irrigMin irrigRate defIrrig histTime 
        //3/6/13 Changed irrigRate to cm/hour
	//11/15/16 -- also allow request CALCULATE_IRRIGATION::uid id continuous for real-time calculation
	//Response: SUCCESS irrig irrigMin irrigRate defIrrig histTime -- same response, but it also will update database on server with history

	UFStrings req, reply;
        String day, errMsg;
        float irrig = -1, irrigMin = -1, irrigRate = -1, defIrrig = -1, prevDeficit = 0;
	boolean updated = false;
	boolean useDefault = false, error = false;
	LinkedHashMap<String, String> fixedRainValues = new LinkedHashMap(3); 
        synchronized(_zoneoutlets) {
          Iterator<ZoneOutlet> i = _zoneoutlets.iterator();
          while (i.hasNext()) {
            ZoneOutlet zo = (ZoneOutlet)i.next();
	    if (!zo.isEnabled()) {
	      //shouldn't happen
	      zo.setIrrig(0);
	      zo.setUpdated(false);
	      System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to None."); 
	      continue;
	    }
	    if (zo.isFixed()) {
	      zo.setIrrig(zo.getDefIrrig());
	      zo.setUpdated(true);
	      zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to FIXED value of "+zo.getIrrig());
	      continue;
	    }
	    if (zo.isFixedWithRain()) {
              zo.setIrrig(zo.getDefIrrig());
	      //try to connect if not already connected
	      if (!weewxConnected) weewxConnected = connectToWeewx();
	      if (weewxConnected) {
                //query weekly rain since last cycle time
                double rain_in = -1;
		//wsid not required so use -1
		int wsid = -1;
	        String request = "GET_RAIN_SINCE::"+zo.getUid()+" "+wsid+" "+lastCycleDate;
	        if (fixedRainValues.containsKey(request)) {
		  //get previously obtained value
		  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Re-using rain value previously obtained for user "+zo.getUid()+"; id="+zo.getId()+"; date "+lastCycleDate);
		  rain_in = Double.parseDouble(fixedRainValues.get(request));
		} else {
                  req = new UFStrings(_mainClass+": actionRequest", request); 
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending rain request for user "+zo.getUid()+"; id="+zo.getId());
                  req.sendTo(weewxSocket);
                  reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
                  if (reply == null) {
                    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: No response to weekly rain from weewx agent for zone "+zo.getId()+"!  Ignoring weekly_rain_thresh.");
                  } else if (getReplyToken(reply, 0, "::").equals("RAIN")) {
                    rain_in = Double.parseDouble(getReplyToken(reply, 1, "::"));
		    //put value into LinkedHashMap
		    fixedRainValues.put(request, getReplyToken(reply, 1, "::"));
                  }
		}
                if (rain_in != -1) {
		  double rain_cm = rain_in*2.54; //convert to CM
		  if (rain_cm >= zo.getDefIrrig()) {
		    zo.setIrrig(0); //rain equals or exceeds irrigation; don't irrigate!
		    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Rain = "+rain_cm+" equals or exceeds irrigation amount of "+zo.getDefIrrig()+"; Will NOT irrigate!.");
		  } else System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Rain = "+rain_cm+" is under irrigation amount of "+zo.getDefIrrig()+".  Will irrigate full fixed amount.");
	        } else {
		  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: could not get rain value from weewx agent.  Will irrigate full fixed amount.");
		} 
	      } else {
		System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: Can NOT connect to weewx agent to calculate rain since last cycle.  Will irrigate full fixed amount.");
	      }

              zo.setUpdated(true);
              zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": irrigation is set to FIXED value of "+zo.getIrrig());
              continue;
	    }
	    if (connected && !weewxConnected) {
	      //normal method of querying ccrop agent -- check this before querying localhost in case this fails
	      if (_continuous) {
		req = new UFStrings(_mainClass+": actionRequest", "CALCULATE_IRRIGATION::"+zo.getUid()+" "+zo.getId()+" continuous "+lastCycleDate);
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending CALCULATE_IRRIGATION request for user "+zo.getUid()+"; id="+zo.getId());
	      } else {
                req = new UFStrings(_mainClass+": actionRequest", "GET_IRRIGATION::"+zo.getUid()+" Z "+zo.getId()+" "+zo.getDefMethod());
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending GET_IRRIGATION request for user "+zo.getUid()+"; id="+zo.getId()+"; default="+zo.getDefMethod());
	      }
              req.sendTo(ccropSocket);
	      reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
	      if (reply == null) {
	        //try one more time, try reconnecting to ccrop
		try {
		  ccropSocket.close();
		} catch(IOException ioe) {}
		try {
		  Thread.sleep(5000);
	 	} catch(InterruptedException e) {} //sleep 5s
                connected = connectToCCrop();
                if (!connected) {
                  zo.setUpdated(false);
		  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Failed connecting to CCrop agent!");
		  continue;
		}
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Attempt 2: sending request for user "+zo.getUid()+"; id="+zo.getId()+"; default="+zo.getDefMethod());
                req.sendTo(ccropSocket);
                reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
	        if (reply == null) {
		  zo.setUpdated(false);
		  continue;
	        }
	      }

	      if (getReplyToken(reply, 0).equals("SUCCESS")) {
	        day = getReplyToken(reply, 5);
                try {
                  //convert irrigation to cm!! ** 8/20/13 Only if not -1 **
                  irrig = Float.parseFloat(getReplyToken(reply, 1));
                  if (irrig >= 0) irrig *= 2.54f;
                  irrigMin = Float.parseFloat(getReplyToken(reply, 2));
                  //convert irrigation rate to cm/hr from in/hr
                  irrigRate = Float.parseFloat(getReplyToken(reply, 3));
                  if (irrigRate >= 0) irrigRate *= 2.54f;
		  if (_continuous) {
		    prevDeficit = Float.parseFloat(getReplyToken(reply, 4));
		    Zone zone = zo.getZone();
		    if (zone != null) {
		      zone.setPrevDeficit(prevDeficit);
		      zone.setLastRunDate(day);
		    }
		  } else {
                    defIrrig = Float.parseFloat(getReplyToken(reply, 4));
                    if (defIrrig >= 0) defIrrig *= 2.54f;
                    if (getReplyToken(reply, 1).equals("-1")) useDefault = true;
		  }
		  updated = true;
                } catch (NumberFormatException nfe) {
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Invalid irrigation value: "+reply.stringAt(0));
                  irrig = -1;
                  irrigMin = -1;
                  irrigRate = -1;
                  defIrrig = -1;
                  updated = false;
                }
		if (updated) {
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": received date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);

	          //If successful, also try to update Zone info here
	          try {
                    req = new UFStrings(_mainClass+": actionRequest", "GET_ZONE_INFO::"+zo.getUid()+" "+zo.getId());
                    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Sending GET_ZONE_INFO request.");
                    req.sendTo(ccropSocket);

                    ObjectInputStream inputStream = new ObjectInputStream(ccropSocket.getInputStream());
                    Zone z = (Zone)inputStream.readObject();
                    if (z == null) {
                      System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": ERROR: Received null Zone for zid "+zo.getId());
                    } else {
                      System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Successfully read Zone "+zo.getId()+", type="+z.getZoneType()+"; name="+z.getName()+"; num="+z.getZoneNumber());
                      if (z.getZoneType().startsWith("ET")) {
                        zo.setZone((ETZone)z);
                      } else if (z.getZoneType().startsWith("LF")) {
                        zo.setZone((LFZone)z);
                      } else {
                        System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": ERROR: Undefined zone type "+z.getZoneType());
                      }
                      zo.setZoneName("Zone "+z.getZoneNumber()+": "+z.getName()+" - "+z.getPlant());
		    }
	          } catch(Exception e) {
		    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error talking to CCropAgent: "+e.toString());
	          }
		}
              } else {
                System.out.println(_mainClass+"::getIrrig> Error: "+reply.stringAt(0));
                irrig = -1;
                defIrrig = -1;
                updated = false;
                error = true;
                errMsg = reply.stringAt(0);
              }
	      zo.setIrrig(irrig);
	      if (useDefault) {
		if (zo.getDefMethod().toLowerCase().equals("manual default")) {
		  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Using manual default value "+zo.getDefIrrig());
		  //use manual default
		  zo.setIrrig(zo.getDefIrrig());
		} else {
		  //default given by ccrop agent
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Using default value "+defIrrig+" from method "+zo.getDefMethod());
		  zo.setIrrig(defIrrig);
	        }
	      }
	      //zo.setIrrigMin(irrigMin);
	      zo.setIrrigRate(irrigRate);
	      //zo.setDefIrrig(defIrrig);
	      zo.setUpdated(updated);
	      if (updated) zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
	    }
	    if (!weewxConnected && !updated) {
              //for some reason, connected but didn't update, try connecting to weewx
              weewxConnected = connectToWeewx();
	      if (!weewxConnected) {
		System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Could not connect to weewx locally.");
		continue;
	      } else useWeewx = true; //2/25/19 need new boolean so that having fixedWithRain zones doesn't force all zones to use weewx
            }
            if (useWeewx) {
              req = new UFStrings(_mainClass+": actionRequest", "GET_WEATHER::"+zo.getUid()+" "+zo.getZone().getWsid());
              System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending weather request for user "+zo.getUid()+"; id="+zo.getId());
              req.sendTo(weewxSocket);
              reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
              if (reply == null) {
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: No response from weewx agent for zone "+zo.getId());
                zo.setUpdated(false);
                continue;
              }
              if (getReplyToken(reply, 0, "::").equals("WEATHER")) {
                //handle weather here
                Zone zone = zo.getZone();
                double[] tmax_hourly = new double[24];
                double[] tmin_hourly = new double[24];
                double[] solar_hourly = new double[24];
                double[] rain_hourly = new double[24];
                boolean[] hasHourlyData = new boolean[24];
                day = null;
                for (int j = 0; j < 24; j++) {
                  String param = getCmdParam(reply,j);
                  hasHourlyData[j] = false;
                  day = getReplyToken(param, 0)+" "+getReplyToken(param, 1);
                  try {
                    solar_hourly[j] = Double.parseDouble(getReplyToken(param, 2));
                    tmax_hourly[j] = Double.parseDouble(getReplyToken(param, 3));
                    tmin_hourly[j] = Double.parseDouble(getReplyToken(param, 4));
                    rain_hourly[j] = Double.parseDouble(getReplyToken(param, 5));
                    hasHourlyData[j] = true;
                  } catch(NumberFormatException nfe) {
                    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not parse weather for hour "+j+": "+param);
                    hasHourlyData[j] = false;
                  }
                }
                //Accumulate hourly weather
                zone.setHourlyWeather(tmax_hourly, tmin_hourly, solar_hourly, rain_hourly, hasHourlyData);

                //also query weekly rain
		double weekly_rain_in = -1;
		req = new UFStrings(_mainClass+": actionRequest", "GET_WEEKLY_RAIN::"+zo.getUid()+" "+zo.getZone().getWsid());
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending weekly rain request for user "+zo.getUid()+"; id="+zo.getId());
                req.sendTo(weewxSocket);
                reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
                if (reply == null) {
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: No response to weekly rain from weewx agent for zone "+zo.getId()+"!  Ignoring weekly_rain_thresh.");
                } else if (getReplyToken(reply, 0, "::").equals("WEEKLY_RAIN")) {
		  weekly_rain_in = Double.parseDouble(getReplyToken(reply, 1, "::"));
		}
		if (weekly_rain_in != -1) zone.setWeeklyRain(weekly_rain_in); //set weekly rain if found

		if (zone.getZoneType().startsWith("LF")) {
		  //query weewx for weather on LF test date
                  req = new UFStrings(_mainClass+": actionRequest", "GET_LF_WEATHER::"+zo.getUid()+" "+zo.getZone().getWsid()+" "+zo.getZone().getLFTestDate());
                  System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": Sending LF weather request for date "+zo.getZone().getLFTestDate()); 
                  req.sendTo(weewxSocket);
                  reply = (UFStrings)UFProtocol.createFrom(weewxSocket);
                  if (reply == null) {
                    System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Warning: No response from weewx agent for zone "+zo.getId()+". Using existing LF Test Date weather data.");
                  } else if (getReplyToken(reply, 0, "::").equals("WEATHER")) {
                    //handle weather here
                    double[] tmax_lf = new double[24];
                    double[] tmin_lf = new double[24];
                    double[] solar_lf = new double[24];
                    double[] rain_lf = new double[24];
                    boolean[] hasHourlylf = new boolean[24];
                    day = null;
                    for (int j = 0; j < 24; j++) {
                      String param = getCmdParam(reply,j);
                      hasHourlylf[j] = false;
                      day = getReplyToken(param, 0)+" "+getReplyToken(param, 1);
                      try {
                        solar_lf[j] = Double.parseDouble(getReplyToken(param, 2));
                        tmax_lf[j] = Double.parseDouble(getReplyToken(param, 3));
                        tmin_lf[j] = Double.parseDouble(getReplyToken(param, 4));
                        rain_lf[j] = Double.parseDouble(getReplyToken(param, 5));
                        hasHourlylf[j] = true;
                      } catch(NumberFormatException nfe) {
                        System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not parse LF weather for hour "+j+": "+param);
                        hasHourlylf[j] = false;
                      }
                    }
                    zone.setLFHourlyWeather(tmax_lf, tmin_lf, solar_lf, rain_lf, hasHourlylf);
		  }
		}

                if (_continuous && zone.getLastRunDate() == null) zone.setLastRunDate(lastCycleDate); //update last run date to by last cycle date
		System.out.println("Prev deficit: "+zone.getPrevDeficit()+" "+zone.getLastRunDate());
                double[] irrigVals = zone.getIrrigValue(_continuous);
                irrig = (float)irrigVals[0];
                irrigMin = (float)irrigVals[1];
                irrigRate = (float)zone.getIrrigRate();
		//12/11/16 need to convert to cm as above with GET_IRRIGATION!  irrig and irrigRate are in inches during calcs!
                if (irrig >= 0) irrig *= 2.54f;
                if (irrigRate >= 0) irrigRate *= 2.54f;
                double deficit = irrigVals[2]; //leave deficit in inches as inches are used in calcs
		zone.setPrevDeficit(deficit);
		if (_continuous) {
		  SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  String lastRunDate = sqldf.format(new java.util.Date());
		  zone.setLastRunDate(lastRunDate);
	        }
                updated = true;
                zo.setIrrig(irrig);
                zo.setIrrigRate(irrigRate);
                zo.setUpdated(updated);
                if (updated) zo.updateCycleForDailyTotals(cycleNumber, ncycles); //update cumulative daily total for HTML output
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Outlet "+_irr.getOutletName(zo.getOutletNumber())+": received date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);
              } else {
                System.out.println(_mainClass+"::getIrrig | "+ctime()+"> Error: Could not get local weather for past 24 hours from weewx agent for zone "+zo.getId()+". Respose = "+reply.toString());
                zo.setUpdated(false);
                continue;
              }
            }
	  }
	}
        try {
	  ccropSocket.close();
	  weewxSocket.close();
        } catch(Exception ioe) {
	}
	readyToIrrigate = true;
    }

/* ================= Private classes for scheduled tasks ================ */

    interface IrrigationTask {
      public void run();
      public boolean cancel();
      public int getHour();
      public int getMinute();
      public int getCycle();
    }

    private class FirstIrrigationTask extends SchedulerTask implements IrrigationTask {
      private String _className = getClass().getName()+"-"+name;
      private int hour, minute, cycle;

      public FirstIrrigationTask(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
	this.cycle = 0;
      }

      public int getHour() { return hour; }
      public int getMinute() { return minute; }
      public int getCycle() { return cycle; }

      public void run() {
	_cycleModeChanged = false; //update cycleModeChanged
	cycleNumber = getCycle(); // cycle 0 
	getIrrig();
      }
    }

    private class AdditionalCycleIrrigationTask extends SchedulerTask implements IrrigationTask {
      private String _className = getClass().getName()+"-"+name;
      private int hour, minute, cycle;

      public AdditionalCycleIrrigationTask(int hour, int minute, int cycle) {
	this.hour = hour;
	this.minute = minute;
	this.cycle = cycle;
      }

      public int getHour() { return hour; }
      public int getMinute() { return minute; }
      public int getCycle() { return cycle; }

      public void run() {
	if (cycleNumber == -1 || _continuous || _cycleModeChanged) {
	  //-1 => no cycles have run yet. _continuous always calls getIrrig().  _cycleModeChanged means mode has changed.
	  _cycleModeChanged = false; //update cycleModeChanged
	  cycleNumber = getCycle(); //get cycle number from this task here
	  getIrrig();
        } else {
	  cycleNumber = getCycle(); //get cycle number from this task here
	  updateIrrig();
	}
      }

      protected void updateIrrig() {
        Calendar c = new GregorianCalendar();
        int doy = c.get(Calendar.DAY_OF_YEAR);
        System.out.println(_className+"::getIrrig> "+(new java.util.Date()).toString());
	if (doy == irrigationDay && cycleNumber > 0) { 
	  //FirstIrrigationTask has been run today for this ZoneGroup; irrigation is correct
	  readyToIrrigate = true;
	  System.out.println(_className+"::getIrrig | " + ctime() + "> Zone Group "+getName()+" is ready for irrigation cycle number "+cycleNumber); 
	} else {
	  //shouldn't happen
	  System.out.println(_className+"::getIrrig | " + ctime() + "> Error: Zone Group "+getName()+" failed irrigation cycle number "+cycleNumber+" today = "+doy+"; irrigation obtained doy = "+irrigationDay);
	  readyToIrrigate = false;
        }
      }
    }

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public String getXML() {
      String xml = "    <group name=\"" + name + "\" number=\"" + number + "\" ncycles=\"" + ncycles + "\" max=\"" + maxSimultaneous + "\" cycleMode=\"" + getCycleMode() + "\" mode=\"" +getMode() + "\">";
      for (Iterator<ZoneOutlet> i = _zoneoutlets.iterator(); i.hasNext();) {
	ZoneOutlet zo = (ZoneOutlet)i.next();
	xml += "\n      <zoneoutlet outlet=\"" + zo.getOutletNumber() + "\" zid=\"" + zo.getId() + "\" uid=\"" + zo.getUid() + "\" priority=\"" + zo.getPriority() + "\"/>";
      }
      for (Iterator<Counter> i = _counters.iterator(); i.hasNext();) {
	Counter c = (Counter)i.next();
	xml += "\n      <counter number=\"" + c.getCounterNumber() + "\"/>";
      }
      for (int i = 0; i < _irrigationTasks.size(); i++) {
	IrrigationTask task = _irrigationTasks.get(i); 
	xml += "\n      <irrigtask cycle=\""+i+"\" hour=\"" + task.getHour() + "\" minute=\"" + task.getMinute() + "\"/>";
      }
      for (Iterator<ZoneOutlet> i = _zoneoutlets.iterator(); i.hasNext();) {
        ZoneOutlet zo = (ZoneOutlet)i.next();
	if (zo.getZone() != null) xml += "\n"+zo.getZone().getXML();
      }
      xml += "\n    </group>";
      return xml;
    }

    public boolean updateFromXML(Element groupElmnt) {
      boolean success = true;
      try {
        if (groupElmnt.hasAttribute("ncycles")) setNCycles(Integer.parseInt(groupElmnt.getAttribute("ncycles").trim()));
        if (groupElmnt.hasAttribute("max")) setMaxSimultaneous(Integer.parseInt(groupElmnt.getAttribute("max").trim()));
	if (groupElmnt.hasAttribute("cycleMode")) setCycleMode(groupElmnt.getAttribute("cycleMode").trim());
	if (groupElmnt.hasAttribute("mode")) setMode(groupElmnt.getAttribute("mode").trim());
        //look for zone outlets
        NodeList outletlist = groupElmnt.getElementsByTagName("zoneoutlet");
	for (int i = 0; i < outletlist.getLength(); i++) {
	  Node outletNode = outletlist.item(i);
          if (outletNode.getNodeType() == Node.ELEMENT_NODE) {
	    Element outletElmnt = (Element)outletNode;
            if (outletElmnt.hasAttribute("outlet")) {
              int outletNum = Integer.parseInt(outletElmnt.getAttribute("outlet").trim());
              success &= addOutlet(outletNum);
              if (success) {
                ZoneOutlet zo = getZoneOutletByNumber(outletNum);
                if (outletElmnt.hasAttribute("zid")) {
                  zo.setId(Integer.parseInt(outletElmnt.getAttribute("zid").trim()));
                  System.out.println("\t\tAdding ZoneOutlet #"+outletNum+": zid = "+zo.getId());
                  //zo.updateZone(ccropHost, ccropPort);
                }
                if (outletElmnt.hasAttribute("priority")) zo.setPriority(Integer.parseInt(outletElmnt.getAttribute("priority").trim()));
              }
            }
          }
        }
	//next look for counters
	NodeList counterlist = groupElmnt.getElementsByTagName("counter");
        for (int i = 0; i < counterlist.getLength(); i++) {
          Node counterNode = counterlist.item(i);
          if (counterNode.getNodeType() == Node.ELEMENT_NODE) {
            Element counterElmnt = (Element)counterNode;
            if (counterElmnt.hasAttribute("number")) {
              int counterNum = Integer.parseInt(counterElmnt.getAttribute("number").trim());
              success &= addCounter(counterNum);
	      if (success) System.out.println("\t\tAdding Counter #"+counterNum);
            }
          }
	}
	//now look for irrig tasks
	NodeList tasklist = groupElmnt.getElementsByTagName("irrigtask");
	for (int i = 0; i < tasklist.getLength(); i++) {
	  Node taskNode = tasklist.item(i);
	  if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
	    Element taskElmnt = (Element)taskNode;
	    if (taskElmnt.hasAttribute("cycle") && taskElmnt.hasAttribute("hour") && taskElmnt.hasAttribute("minute")) {
	      int cycle = Integer.parseInt(taskElmnt.getAttribute("cycle").trim());
	      int hour = Integer.parseInt(taskElmnt.getAttribute("hour").trim());
              int minute = Integer.parseInt(taskElmnt.getAttribute("minute").trim());
	      success &= checkAndUpdateTask(cycle, hour, minute);
	    }
	  }
	}

	//now update zones in batch from socket connection to CCrop agent
	updateZones();

	//finally update zones
        NodeList zonelist = groupElmnt.getElementsByTagName("zone");
	Zone zone;
        for (int i = 0; i < zonelist.getLength(); i++) {
          Node zoneNode = zonelist.item(i);
          if (zoneNode.getNodeType() == Node.ELEMENT_NODE) {
            Element zoneElmnt = (Element)zoneNode;
            if (zoneElmnt.hasAttribute("type")) {
	      String zoneType = zoneElmnt.getAttribute("type").trim();
	      if (zoneType.startsWith("ET")) {
		zone = new ETZone(zoneElmnt);
              } else if (zoneType.startsWith("LF")) {
		zone = new LFZone(zoneElmnt);
              } else {
                System.out.println(_mainClass+"::updateFromXML | "+ctime()+"> ERROR: Undefined zone type "+zoneType);
		continue;
              }
	      //loop over outlets and find if one matches the zid and uid of this zone
	      Iterator<ZoneOutlet> zi = _zoneoutlets.iterator();
              while (zi.hasNext()) {
		ZoneOutlet zo = (ZoneOutlet)zi.next();
		if (zo.getId() == zone.getId() && zo.getUid() == zone.getUid()) {
		  if (zo.getZone() == null) {
		    //update full zone
		    zo.setZone(zone);
		    System.out.println("\t\tUpdating zone "+zo.getId()+" from XML...");
		  } else {
		    //zone has been read over socket above, just update prevDeficit and lastRunTime
		    zo.getZone().setPrevDeficit(zone.getPrevDeficit());
		    zo.getZone().setLastRunDate(zone.getLastRunDate());
                    System.out.println("\t\tUpdating zone "+zo.getId()+" prevDeficit = "+zone.getPrevDeficit()+", lastRunDate = "+zone.getLastRunDate()+" from XML...");
		  }
		  break;
		}
	      }
            }
          }
        }
      } catch(Exception e) {
        System.out.println(_mainClass+"::updateFromXML | "+ctime()+"> ERROR: "+e.toString());
	return false;
      }
      return success;
    }

    public boolean updateZones() {
      String cmd = "GET_ZONES_INFO::"+_irr.getUid()+" ";
      int nzones = 0;
      int _timeout = 6000;
      Socket ccropSocket = null;

      //loop over outlets, generate command and count nzones
      Iterator<ZoneOutlet> zi = _zoneoutlets.iterator();
      while (zi.hasNext()) {
        ZoneOutlet zo = (ZoneOutlet)zi.next();
        if (zo.getUid() != _irr.getUid()) continue;
        if (!zo.isEnabled() || zo.isFixed() || zo.isFixedWithRain()) continue; 
	if (nzones == 0) cmd += zo.getId(); else cmd += ","+zo.getId();
	nzones++;
      }

      if (nzones == 0) return true; //all zones are fixed

      try {
        //connect to CCropAgent to download zone info
        System.out.println(_mainClass+"::updateZones | "+ctime()+"> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
        ccropSocket = new Socket(ccropHost, ccropPort);
        ccropSocket.setSoTimeout(_timeout);
        UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
        greet.sendTo(ccropSocket);
        UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
        if (ufpr == null) {
          System.out.println(_mainClass+"::updateZones | "+ctime()+"> ERROR: received null object!  Closing socket!");
          ccropSocket.close();
          return false;
        } else {
          String request = ufpr.name().toLowerCase();
          if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
            System.out.println(_mainClass+"::updateZones | "+ctime()+"> connection established: "+request);
          } else {
            System.out.println(_mainClass+"::updateZones | "+ctime()+"> ERROR: received invalid response: "+request+".  Closing socket!");
            ccropSocket.close();
            return false;
          }
	  //send GET_ZONES_INFO request
          UFStrings ccropReq = new UFStrings(_mainClass+": actionRequest", cmd); 
          System.out.println(_mainClass+"::updateZones | "+ctime()+"> Sending request: "+cmd);
          ccropReq.sendTo(ccropSocket);
	  //receive response
	  UFStrings ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
	  if (ccropReply == null) {
            System.out.println(_mainClass+"::updateZones | "+ctime()+"> ERROR: received null reply!  Closing socket!");
            ccropSocket.close();
	    return false;
	  }
	  System.out.println(_mainClass+"::updateZones | "+ctime()+"> Received "+ccropReply.numVals()+" replies.");
	  for (int i = 0; i < ccropReply.numVals(); i++) {
	    //create a new zone
	    Zone z = new Zone(ccropReply.stringAt(i));
	    if (z == null) {
              System.out.println(_mainClass+"::updateZones | "+ctime()+"> ERROR: Received null Zone for index "+i);
	      continue;
	    } else if (z.getZoneType().startsWith("ET")) {
	      z = new ETZone(ccropReply.stringAt(i));
	    } else if (z.getZoneType().startsWith("LF")) {
	      z = new LFZone(ccropReply.stringAt(i));
	    } else {
              System.out.println(_mainClass+"::updateZones | "+ctime()+"> ERROR: Undefined zone type "+z.getZoneType());
              break;
	    }
	    //find its corresponding outlet
            zi = _zoneoutlets.iterator();
            while (zi.hasNext()) {
	      ZoneOutlet zo = (ZoneOutlet)zi.next();
              if (zo.getId() == z.getId() && zo.getUid() == z.getUid()) {
		//this zone outlet matches this zone
		System.out.println(_mainClass+"::updateZones | "+ctime()+"> Successfully read Zone "+z.getId()+", type="+z.getZoneType()+"; name="+z.getName()+"; num="+z.getZoneNumber());
		zo.setZone(z);
		zo.setZoneName("Zone "+z.getZoneNumber()+": "+z.getName()+" - "+z.getPlant());
	      }
	    }
	  }
	  //close socket
	  ccropSocket.close();
        }
      } catch (Exception e) {
        System.err.println(_mainClass+"::updateZones | "+ctime()+"> ERROR talking to CCROP Agent: "+e.toString());
        e.printStackTrace();
        try {
          ccropSocket.close();
        } catch (Exception e2) {}
        return false;
      }
      return true;
    }

    //========== helper methods ===========//
    public String getCmdParam(UFStrings cmd, int idx) {
      String param = cmd.stringAt(idx);
      param = param.substring(param.indexOf("::")+2).trim();
      return param;
    }
}
