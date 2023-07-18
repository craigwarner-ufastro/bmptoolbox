package BMPToolbox; 
/**
 * Title:        IrrigRealTime
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a real-time run 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;
import org.tiling.scheduling.*;

public class IrrigRealTime {
    private final Scheduler scheduler = new Scheduler();

    String _mainClass = getClass().getName();
    protected String uid, plcIP, id; 
    protected int hour, minute, ccropPort = 57002, _greetTimeout = 6000;
    protected long timestamp;
    protected String name = "", ccropHost = "www.bmptoolbox.org"; 
    protected boolean updated = false, isZone = false, isMultiple=false, useDefault = false, error = false, isFixed = false;
    protected float irrig = -1, defIrrig = -1;
    protected float irrigMin = -1, irrigRate = -1;
    protected String day = "", defMethod="none", external="", ncyclesString="";
    protected String errMsg = "";
    protected Socket ccropSocket;
    protected SchedulerTask task = null;
    protected Vector<String> irrigStrings;
    protected boolean updateDb = false;

    public IrrigRealTime(String plcKey, String id, int hour, int minute) { 
      plcIP = getReplyToken(plcKey, 0, "::");
      uid = getReplyToken(plcKey, 1, "::");
      this.id = id;
      if (id.startsWith("Z")) isZone = true;
      if (id.startsWith("M")) isMultiple = true;
      if (id.startsWith("F")) isFixed = true;
      //id = [Z|R|M|F] id.  Can be comma separated list!
      this.hour = hour; 
      this.minute = minute; 
      this.timestamp = System.currentTimeMillis()/1000;
      defMethod = "none";
    }

    public void updateHostAndPort(String host, int port) {
      ccropHost = host;
      ccropPort = port;
    }

    public void start() {
	task = new SchedulerTask() {
            public void run() {
                getIrrig();
            }

            private void getIrrig() {
                Calendar c = new GregorianCalendar();
                int todayDoy = c.get(Calendar.DAY_OF_YEAR)+1000*c.get(Calendar.YEAR);
		if (isFixed) {
		  System.out.println(_mainClass+"::getIrrig> Using fixed manual default irrigation.");
		  irrig = 0;
		  irrigMin = 0;
                  updated = true;
		  /* We will get current manual default value when this returns */
		  return;
		}

		System.out.println((new java.util.Date()).toString());
		try {
		  System.out.println(_mainClass+"::getIrrig> Trying to connect to CCROP agent on "+ccropHost+", port = "+ccropPort);
		  ccropSocket = new Socket(ccropHost, ccropPort);
		  ccropSocket.setSoTimeout(_greetTimeout); //timeout for greetings
		  UFTimeStamp greet = new UFTimeStamp(_mainClass+": fullclient");
		  greet.sendTo(ccropSocket);
		  UFProtocol ufpr = UFProtocol.createFrom(ccropSocket);
		  ccropSocket.setSoTimeout(0); //infinite timeout
		  if (ufpr == null) {
		    System.out.println(_mainClass+"::getIrrig> received null object!  Closing socket!");
		    ccropSocket.close();
		    return;
		  } else {
		    String request = ufpr.name().toLowerCase();
		    if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
		      System.out.println(_mainClass+"::getIrrig> connection established: "+request);
		    } else {
		      System.out.println(_mainClass+"::getIrrig> received "+request+".  Closing socket!");
		      ccropSocket.close();
		      return;
		    }
		    if (isMultiple) {
		      /* RE-Ask CCrop Agent for zone ids and external references at this point! */
                      UFStrings ccropReq, ccropReply; 
                      ccropReq = new UFStrings(_mainClass+": actionRequest", "GET_ZONE_REFS::"+uid);
                      System.out.println(_mainClass+"::action> Sending GET_ZONE_REFS request.");
		      ccropReq.sendTo(ccropSocket);
                      ccropReply = (UFStrings)UFProtocol.createFrom(ccropSocket);
		      if (getReplyToken(ccropReply, 0, "::").equals("ZONE_REF")) {
			String idString = "M "+getReplyToken(ccropReply, 1, "::");
			String ncs = getReplyToken(ccropReply, 2, "::");
			String refString = getReplyToken(ccropReply, 3, "::");
			for (int i = 1; i < ccropReply.numVals(); i++) {
			  String[] temp = ccropReply.stringAt(i).split("::");
			  idString += ","+temp[1];
			  ncs += ","+temp[2];
			  refString += "::"+temp[3];
			}
			setExternal(refString);
			setNcyclesString(ncs);
			updateId(idString);
			System.out.println("Successfully updated id = "+idString+"; ref = "+refString);
		      }	

		      /* Used for CSV Output.  id = M 5,7,9 */
		      irrigStrings = new Vector();
		      UFStrings req, reply;
		      String[] zones = id.substring(2).split(",");
		      String[] ncycles = ncyclesString.split(",");
		      String[] externals = external.split("::");
		      if (zones.length != externals.length) {
                        System.out.println(_mainClass+"::getIrrig> Error: "+zones.length+" ids but "+externals.length+" externals."); 
                        irrig = -1;
                        updated = false;
                        error = true;
                        errMsg = "Mismatch between ids and externals";
			return;
		      }
		      for (int j = 0; j < zones.length; j++) {
			//reset useDefault for each zone!!!
			useDefault = false;
			req = new UFStrings(_mainClass+": actionRequest", "GET_IRRIGATION::"+uid+" Z "+zones[j]+" "+defMethod);
			System.out.println(_mainClass+"::getIrrig> Sending request for user "+uid+"; id=Z "+zones[j]+"; default="+defMethod);
			req.sendTo(ccropSocket);
			reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
			if (getReplyToken(reply, 0).equals("SUCCESS")) {
                          day = getReplyToken(reply, 5);
			  try {
                            /* convert irrigation to cm!! ** 8/20/13 Only if not -1 ** */
                            int nc = 1;
                            irrig = Float.parseFloat(getReplyToken(reply, 1));
			    if (irrig >= 0) irrig *= 2.54f;
                            irrigMin = Float.parseFloat(getReplyToken(reply, 2));
                            irrigRate = Float.parseFloat(getReplyToken(reply, 3));
			    if (irrigRate >=0) irrigRate *= 2.54f;
                            if (getReplyToken(reply, 1).equals("-1")) useDefault = true;
			    if (useDefault) {
			      /* convert irrigation rate to cm/hr from in/hr */
			      defIrrig = Float.parseFloat(getReplyToken(reply, 4));
			      if (defIrrig >= 0) defIrrig *= 2.54f;
			      irrigMin = defIrrig/irrigRate*60.0f; 
			      try {
				nc = Integer.parseInt(ncycles[j]);
				irrigMin /= nc;
			      } catch(NumberFormatException nfe) {
				System.out.println(_mainClass+"::getIrrig> Invalid ncycles: "+ncycles[j]);
			      }
                              //set flag -1
                              if (irrig < 0) nc = -1;
			      irrigStrings.add("\""+externals[j]+"\","+defIrrig+","+irrigMin+","+nc);
			    } else {
                              try {
                                nc = Integer.parseInt(ncycles[j]);
                                irrigMin /= nc;
                              } catch(NumberFormatException nfe) {
                                System.out.println(_mainClass+"::getIrrig> Invalid ncycles: "+ncycles[j]);
                              }
                              //set flag -1
                              if (irrig < 0) nc = -1;
			      irrigStrings.add("\""+externals[j]+"\","+irrig+","+irrigMin+","+nc);
			    }
                          } catch (NumberFormatException nfe) {
                            System.out.println(_mainClass+"::getIrrig> Invalid irrigation value: "+getReplyToken(reply, 1));
                            irrig = -1;
                            irrigMin = -1;
                            irrigRate = -1;
                            defIrrig = -1;
                            updated = false;
			    //error = true;
			    errMsg = "Invalid irrigation value: "+getReplyToken(reply, 1); 
                            //Do not break out of loop, just add a line with -1 for error flags
                            irrigStrings.add("\""+externals[j]+"\","+irrig+","+irrigMin+",-1");
                          }
                          System.out.println(_mainClass+"::getIrrig> date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);
			} else {
			  System.out.println(_mainClass+"::getIrrig> Error: "+reply.stringAt(0));
			  irrig = -1;
                          irrigMin = -1;
			  defIrrig = -1;
                          updated = false;
                          //error = true;
                          errMsg = reply.stringAt(0);
                          //Do not break out of loop, just add a line with -1 for error flags
                          irrigStrings.add("\""+externals[j]+"\","+irrig+","+irrigMin+",-1");
                        }
		      }
		      if (!error) updated = true;
		      ccropSocket.close();
		      return;
		    }
		    /* Request: GET_IRRIGATION::uid [Z|R] id defMethod
		     * Response: SUCCESS doy irrig defIrrig runName  OR  SUCCESS irrig irrigMin irrigRate defIrrig histTime 
		     * 3/6/13 Changed irrigRate to cm/hour
		     **/
		    useDefault = false; //reset useDefault in this case too!!!  8/26/16 CW
		    UFStrings req = new UFStrings(_mainClass+": actionRequest", "GET_IRRIGATION::"+uid+" "+id+" "+defMethod);
		    System.out.println(_mainClass+"::getIrrig> Sending request for user "+uid+"; id="+id+"; default="+defMethod);
		    req.sendTo(ccropSocket);
		    UFStrings reply = (UFStrings)UFProtocol.createFrom(ccropSocket);
		    ccropSocket.close();
		    if (getReplyToken(reply, 0).equals("SUCCESS")) {
		      if (isZone) {
                        day = getReplyToken(reply, 5);
                        try {
                          /* convert irrigation to cm!! ** 8/20/13 Only if not -1 ** */
                          irrig = Float.parseFloat(getReplyToken(reply, 1));
			  if (irrig >= 0) irrig *= 2.54f;
                          irrigMin = Float.parseFloat(getReplyToken(reply, 2));
			  /* convert irrigation rate to cm/hr from in/hr */
                          irrigRate = Float.parseFloat(getReplyToken(reply, 3));
			  if (irrigRate >= 0) irrigRate *= 2.54f;
			  defIrrig = Float.parseFloat(getReplyToken(reply, 4));
			  if (defIrrig >= 0) defIrrig *= 2.54f;
                          if (getReplyToken(reply, 1).equals("-1")) useDefault = true;
                        } catch (NumberFormatException nfe) {
                          System.out.println(_mainClass+"::getIrrig> Invalid irrigation value: "+getReplyToken(reply, 1));
                          irrig = -1;
                          irrigMin = -1;
                          irrigRate = -1;
			  defIrrig = -1;
                          updated = false;
                        }
                        updated = true;
                        System.out.println(_mainClass+"::getIrrig> date = "+day+"; Irrig = "+irrig+" cm; minutes = "+irrigMin+"; rate = "+irrigRate+"; default = "+defIrrig);
		      } else {
		        day = getReplyToken(reply, 1);
		        try {
			  irrig = Float.parseFloat(getReplyToken(reply, 2));
			  defIrrig = Float.parseFloat(getReplyToken(reply, 3));
                          if (getReplyToken(reply, 2).equals("-1")) useDefault = true;
		        } catch (NumberFormatException nfe) {
			  System.out.println(_mainClass+"::getIrrig> Invalid irrigation value: "+getReplyToken(reply, 2));
			  irrig = -1;
			  defIrrig = -1;
			  updated = false;
		        }
		        name = getReplyToken(reply, 4);
		        updated = true;
		        System.out.println(_mainClass+"::getIrrig> DOY = "+day+"; Irrig = "+irrig+";default = "+defIrrig+"; Run Name = "+name);
		      }
		    } else {
		      System.out.println(_mainClass+"::getIrrig> Error: "+reply.stringAt(0));
		      irrig = -1;
                      defIrrig = -1;
		      updated = false;
		      error = true;
		      errMsg = reply.stringAt(0);
		    }
		  }
		} catch (IOException ioe) {
		  System.err.println(_mainClass+"::getIrrig> "+ioe.toString());
		}
            }
        };
        scheduler.schedule(task, new DailyIterator(hour, minute, 0));
        /* If it is currently schedule hour and minute, run manually */
        Calendar c = new GregorianCalendar();
	System.out.println(c.get(Calendar.HOUR_OF_DAY)+" "+hour+" "+c.get(Calendar.MINUTE)+" "+minute);
	if (c.get(Calendar.HOUR_OF_DAY) == hour && c.get(Calendar.MINUTE) == minute) task.run();
    }

    public void cancel() {
	scheduler.cancel();
    }

    public String getName() {
      return name;
    }

    public String getDefMethod() {
      return defMethod;
    }

    public void setDefMethod(String method) {
      defMethod = method;
    }

    public String getExternal() {
      //remove whitespace
      return external.trim();
    }

    public String getNcyclesString() {
      return ncyclesString.trim();
    }

    protected void updateId(String id) {
      this.id = id;
      updateDb = true;
    }

    public boolean needsUpdate() {
      return updateDb;
    }

    public String getIdUpdate() {
      updateDb = false;
      return id;
    }

    public void setExternal(String ext) {
      external = ext; 
    }

    public void setNcyclesString(String ncs) {
      ncyclesString = ncs;
    }

    public int getHour() {
      return hour;
    }

    public int getMinute() {
      return minute;
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

    public boolean error() {
      return error;
    }

    public void clearError() {
      error = false;
    }

    public String getErrorMsg() {
      return errMsg;
    }

    public float getIrrig() {
      return irrig;
    }

    public Vector<String> getMultipleIrrig() {
      return irrigStrings;
    }

    public String getDay() {
      return day;
    }

    public float getDefault() {
      return defIrrig;
    }

    public boolean useDefault() {
      return useDefault;
    }

    public boolean isZone() {
      return isZone;
    }

    public boolean isMultiple() {
      return isMultiple;
    }

    public boolean isFixed() {
      return isFixed;
    }

    public float getIrrigRate() {
      return irrigRate;
    }

    public void reset() {
      updated = false;
      useDefault = false;  //reset useDefault on reset too! 8/26/16
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
}
