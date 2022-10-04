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
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ETZone extends Zone implements Serializable {

  protected String _mainClass = getClass().getName();

  public ETZone(ResultSet rs) {
    super(rs);
    //setCommonInfo(rs);
  }

  public ETZone(Element elmnt) {
    super(elmnt);
  }

  public ETZone(String xmlString) {
    super(xmlString);
  }

  public double[] getIrrigValue() { return getIrrigValue(false); }

  public double[] getIrrigValue(boolean continuous) {
    //in java version, getIrrigation returns SQL statement in string format
    //processIrrigSchedule is called from within this method
    double solar = solar_wmhr*0.0864;
    double tmax = (tmax_f-32)/1.8;
    double tmin = (tmin_f-32)/1.8;

    //Adjust for shade
    solar *= (1.0 - shade/100.0);

    double cdr = 22.2+11.1*Math.sin(0.0172*(doy-80));
    apot = Math.pow((contDiam*2.54)/2, 2)*Math.PI;
    double atot = Math.pow((contDiam+contSpacing)*2.54, 2)*spacing;
    double aratio = atot/apot;
    double gimpFGC = pctCover*0.01;

    double lat = lat_deg*0.01745;
    double sinlat = Math.sin(lat);
    double coslat = Math.cos(lat);
    double elev = elev_ft*0.3049;

    double press = (101.0-0.0107*elev)/101.0;
    double escor = 1.0-0.016733*Math.cos(0.0172*(doy-1.0));
    double om = 0.017202*(doy-3.244);

    double theta = om+0.03344*Math.sin(om)*(1.0-0.15*Math.sin(om))-1.3526;
    double sindec = 0.3978*Math.sin(theta);
    double cosdec = Math.sqrt(1.0-Math.pow(sindec, 2));
    double sinf = sindec*sinlat;
    double cosf = cosdec*coslat;
    double hrang = Math.acos(-1*sinf/cosf);
    double etr = 37.21/Math.pow(escor, 2)*(hrang*sinf+cosf*Math.sin(hrang));
    double h2 = sinf+cosf*Math.cos(hrang/2);
    double airmass = press*(-16.886*Math.pow(h2, 3)+36.137*Math.pow(h2, 2)-27.462*h2+8.7412);

    double tarcd = 0.87-0.0025*tmin;
    double cdr2 = etr*Math.pow(tarcd, airmass);
    double fcdr = Math.min(solar/cdr2, 1);
    double drf = Math.max(1.33*(fcdr-0.25), 0);
    double parb = Math.min(solar*0.5, cdr2*0.5*0.7);
    double brad = solar*drf*Math.exp(-3.0*Math.pow(gimpFGC, 0.9))*(1.0-apot/atot);
    double tmaxb = tmax+0.6*brad;
    double btmean = tmaxb*0.75+tmin*0.25;
    double lh = 25.01-btmean/42.3;

    double gamma = 0.0674*press;
    double delta = 2503.0*Math.exp(17.27*btmean/(btmean+237.3))/Math.pow(btmean+237.3, 2);
    double radcom = delta/(delta+gamma)*solar*0.6/lh;
    double radcom_cdr = delta/(delta+gamma)*cdr*0.6/lh;
    double vpd = 0.6108*Math.exp(17.27*btmean/(btmean+237.3))-0.6108*Math.exp(17.27*tmin/(tmin+237.3));

    double cropec = 1.0;
    double aerocomP = (cropec*Math.pow(vpd, 1.5))/lh;
    double et0 = radcom+aerocomP;
    double et_cdr0 = radcom_cdr+aerocomP;
    double et = et0*0.9*Math.pow(gimpFGC, 0.73);
    double etcdr = et_cdr0*0.9*Math.pow(gimpFGC, 0.73);

    double contETcm = et*aratio+0.2;
    double contETcdrcm = etcdr*aratio+0.2;
    contETin = contETcm/2.54;
    contETcdrin = contETcdrcm/2.54;

    //double vplt = 4/3.*Math.PI*Math.pow((plantWd*2.54)/2, 2)*plantHt*2.54/2;
    double cf1 = 0;
    if (irrig_capture.equals("high")) {
      cf1 = 2.5;
    } else if (irrig_capture.equals("medium")) {
      cf1 = 2.0;
    } else if (irrig_capture.equals("low")) {
      cf1 = 1.5;
    } else if (irrig_capture.equals("negative")) {
      cf1 = 0.75;
    }
    //double cf2 = -1*cf1*(apot*7.39-716)+apot;
    //double cfmax = Math.max(1, (cf1*vplt+cf2)/apot);
    //double cf = Math.min(cfmax, 0.9*atot/apot+0.1);
    double plantSize = (plantHt+plantWd)/2;
    cf = Math.min(Math.max(1, Math.min(1+(plantSize-contDiam)/contDiam*(cf1-1), 1.1*cf1)), 0.9*aratio+0.1);
    if (cf1 < 1 && cf1 != 0) {
      cf = Math.max(cf1, Math.min(1,1-(plantSize-contDiam)/contDiam*(1-cf1)));
    }
    //cf = 1 for non et-sprinkler 
    if (!zoneType.equals("ET-sprinkler")) {
      cf = 1.0;
    }
    //do this conversion in all constructors
    if (zoneType.equals("ET-micro") || zoneType.equals("LF-micro")) {
      irrig_in_hr = irrig_gal_hr*1490.157/apot;
    }
    double irrig_in = contETin/cf*100.0/irrig_uniformity;
    double irrig_time = irrig_in/irrig_in_hr*60.0;

    //Add in rain
    Calendar c = new GregorianCalendar();
    int firstHour = c.get(Calendar.HOUR_OF_DAY)+1;
    int lastHour = firstHour+24;
    int todaydoy = c.get(Calendar.DAY_OF_YEAR);

    //if continuous mode, find hour of lastRunDate
    if (continuous && lastRunDate != null) {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      try {
        //calculate firstHour to be hour after lastRunDate -- e.g. if it was last run at 10:05am, firstHour would be 11.
        c.setTime(df.parse(lastRunDate));
        firstHour = c.get(Calendar.HOUR_OF_DAY)+1;
        if (lastHour < firstHour) lastHour += 24;
        if (lastHour-firstHour > 24) {
          if (c.get(Calendar.DAY_OF_YEAR) == todaydoy) {
            System.out.println("ETZone::getIrrigation> last hour = "+lastHour+"; first hour = "+firstHour+"; adjusting by 24 hours...");
            lastHour -= 24;
          } else{
            System.out.println("ETZone::getIrrigation> last hour = "+lastHour+"; first hour = "+firstHour+"; limiting to 24 hours...");
            firstHour = lastHour-24;
          }
        }
      } catch(ParseException pe) {
        System.out.println("ETZone::getIrrigation> Zone "+this.zid+": "+this.zoneName+" => Error parsing last run date "+lastRunDate+".  Using last 24 hours.");
      }
    }

    double scaledET, rainPerPot;
    cum = prevDeficit*2.54; //cum is in cm!!
    //Loop over hours to find cumulative deficit
    //use lastHour variable 11/15/16
System.out.println("CUM = "+cum);
    if (solar_wmhr == 0) solar_wmhr = 0.01; //avoid divide by zero error
    for (int h = firstHour; h < lastHour; h++) {
      scaledET = solar_hourly[h%24] / (24*solar_wmhr) * contETin*2.54;
      //Changed from (1+cf)/2.0 to cf 8/19/13
      rainPerPot = rain_hourly[h%24] * 2.54 * cf;
      //nullify rain for prod area = plastic
      if (productionArea.equals("plastic")) rainPerPot = 0;
      //check rain against thresholds.  Note that rainPerPot is in cm here so multiply hourly threshold by 2.54.
      //8/12/17 decrement rainPerPot by hourly_rain_thresh if it exceeds it.
      //if weekly_rain_in is not set, value is -1 and ignore thresholds
      if (rainPerPot < hourly_rain_thresh_in*2.54) rainPerPot = 0; else rainPerPot -= hourly_rain_thresh_in*2.54;
      if (weekly_rain_in != -1 && weekly_rain_in < weekly_rain_thresh_in) rainPerPot = 0;
      cum += scaledET - rainPerPot;
      if (cum < 0) cum = 0;
System.out.println("HOUR = "+h+"; cum = "+cum);
    }
    //Re-calculate irrig_in and irrig_time
    //Apply LF here too
    //irrig_in = cum/2.54/cf*100.0/irrig_uniformity;
    cum /= 2.54; //Convert to inches
    irrig_in = (cum+lf*cum/(1.0-lf))/cf*100.0/irrig_uniformity;
    irrig_time = irrig_in/irrig_in_hr*60.0;
    //if (ncycles != 1) irrig_time /= ncycles;

    double[] retVal = processIrrigSchedule(irrig_in, irrig_time); 
    return retVal;
  }

  public String getIrrigation() {
    return getIrrigation(2, false);
  }

  public String getIrrigation(int source, boolean continuous) {
    double[] retVal = getIrrigValue(continuous);

    String  zoneQuery = "insert into zoneHistory(zid, uid, histTime, pctCover, plantHeight_in, plantWidth_in, containerSpacing_in, spacing, irrig_in, irrig_minutes, etc_in, cf, irrig_uniformity, irrig_in_per_hr, solar_radiation, max_temp, min_temp, rain_in, wsid, deficit_in, source, ncycles)";
    double irrig_in = retVal[0];
    double irrig_time = retVal[1];
    double deficit_in = retVal[2];

    SimpleDateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String sqlDate = sqldf.format(new java.util.Date());

    zoneQuery += " VALUES ("+zid + ", " + uid + ", '" + sqlDate + "', " + pctCover + ", " + plantHt + ", " + plantWd + ", " +contSpacing + ", '" + spacingStr + "', " + irrig_in + ", " + irrig_time + ", " + contETin + ", " + cf + ", " + irrig_uniformity + ", " + irrig_in_hr + ", " + solar_wmhr + ", " + tmax_f + ", " + tmin_f + ", " + rain_in + ", " + wsid +", " + deficit_in + ", " + source + ", "+ncycles+");";
    return zoneQuery;
  }

  public double[] processIrrigSchedule(double irrig_in, double irrig_time) {
    double[] retVal = new double[3]; 
    double deficit_in = 0;
    Calendar c = new GregorianCalendar();
    //process different irrig schedules and min irrig here
    System.out.println(_mainClass+"::processIrrigSchedule> "+irrigSchedule+" irrigation: "+irrig_in+" inches; "+irrig_time+" minutes.");
    if (irrigSchedule.equals("daily")) {
      if (irrig_time <= minIrrig) {
	System.out.println(_mainClass+"::processIrrigSchedule> Under minimum irrigation time of "+minIrrig+" minutes.  Will carry over deficit of "+irrig_in+" inches.");
	deficit_in = irrig_in;
	irrig_in = 0;
	irrig_time = 0;
      }
    } else if (irrigSchedule.equals("odd days")) {
      int dom = c.get(Calendar.DAY_OF_MONTH);
      if (dom % 2 == 0) {
        //even day
        System.out.println(_mainClass+"::processIrrigSchedule> Even day.  Carrying over deficit of "+irrig_in+" inches.");
        deficit_in = irrig_in;
        irrig_in = 0;
        irrig_time = 0;
      } else if (irrig_time <= minIrrig) {
        System.out.println(_mainClass+"::processIrrigSchedule> Under minimum irrigation time of "+minIrrig+" minutes.  Will carry over deficit of "+irrig_in+" inches.");
        deficit_in = irrig_in;
        irrig_in = 0;
        irrig_time = 0;
      }
    } else if (irrigSchedule.equals("fixed days")) {
      boolean[] dayset = new boolean[7];
      int fx = fixedDays;
      for (int j = 6; j >= 0; j--) {
        if (fx >= Math.pow(2,j)) {
          //this day is set, e.g Sat = 64, Fri = 32
          dayset[j] = true;
          fx -= Math.pow(2,j);
        } else dayset[j] = false;
      }
      int dow = c.get(Calendar.DAY_OF_WEEK);
      if (!dayset[dow-1]) {
        //not set for today
        System.out.println(_mainClass+"::processIrrigSchedule> Irrigation not set for DOW "+dow+". Carrying over deficit of "+irrig_in+" inches.");
        deficit_in = irrig_in;
        irrig_in = 0;
        irrig_time = 0;
      } else if (irrig_time <= minIrrig) {
        System.out.println(_mainClass+"::processIrrigSchedule> Under minimum irrigation time of "+minIrrig+" minutes.  Will carry over deficit of "+irrig_in+" inches.");
        deficit_in = irrig_in;
        irrig_in = 0;
        irrig_time = 0;
      }
    } else if (irrigSchedule.equals("threshold")) {
      //total available water (in)
      double awin = 0.0132*Math.pow(contDiam, 2.7506)*1000.0*availableWater/2.54/apot;
      if (cum+contETcdrin >= thresholdFactor*awin) {
        System.out.println(_mainClass+"::processIrrigSchedule> Threshold of "+(thresholdFactor*awin)+" reached.  Will irrigate "+irrig_in+" inches.");
      } else {
        System.out.println(_mainClass+"::processIrrigSchedule> Threshold of "+(thresholdFactor*awin)+" is NOT reached.  Will carry over deficit of "+irrig_in+" inches.");
        deficit_in = irrig_in;
        irrig_in = 0;
        irrig_time = 0;
      }
    } else if (irrigSchedule.equals("none")) {
      System.out.println(_mainClass+"::processIrrigSchedule> Will NOT irrigate or carry over deficit of "+irrig_in+" inches.");
      irrig_in = 0;
      irrig_time = 0;
    } else {
      System.out.println(_mainClass+"::processIrrigSchedule> ERROR: could not process irrigation scedule - "+irrigSchedule);
    }
    retVal[0] = irrig_in;
    retVal[1] = irrig_time;
    retVal[2] = deficit_in;
    return retVal;
  }
}
