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

public class Zone implements Serializable {

  protected String _mainClass = getClass().getName();

  double contDiam, irrig_in_hr, irrig_uniformity, elev_ft, lat_deg, long_deg, irrig_gal_hr;
  String zoneName, irrig_capture, spacingStr, irrigSchedule, productionArea, zoneType, plant;
  double plantHt, plantWd, pctCover, contSpacing, spacing, shade, lf, availableWater, thresholdFactor;
  double hourly_rain_thresh_in, weekly_rain_thresh_in;
  int uid, zid, wsid, fixedDays, minIrrig, ncycles, doy, firstHour, zoneNumber;

  double tmax_f, tmin_f, solar_wmhr, rain_in, prevDeficit;

  double[] tmax_hourly, tmin_hourly, solar_hourly, rain_hourly;
  boolean[] hasHourlyData;
  int nhourly = 0;
  double weekly_rain_in = -1;

  String lfTestDate, lastRunDate;
  double lfTestRuntime, lfTestPct, lfTargetPct, et_fac;

  double apot, cum, contETin, cf, contETcdrin; //globals

  public Zone() {
    //constructor for sim mode
    zid = -1;
    uid = -1;
    wsid = -1;
    zoneName = "None";
  }

  public Zone(ResultSet rs) {
    setCommonInfo(rs);
  }

  public Zone(Element elmnt) {
    createFromXML(elmnt);
  }

  public Zone(String xmlString) {
    createFromXMLString(xmlString);
  }

  public int getId() {
    return this.zid;
  }

  public int getUid() {
    return this.uid;
  }

  public int getWsid() {
    return this.wsid;
  }

  public String getLastRunDate() {
    return lastRunDate;
  }

  public double getPrevDeficit() {
    return prevDeficit;
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

  public double getIrrigRate() {
    return this.irrig_in_hr;
  }

  public double getIrrigRateGalHr() {
    return this.irrig_gal_hr;
  }

  public String getIrrigRateUnits() {
    return "inch/hr";
  }

  public String getIrrigSchedule() {
    return this.irrigSchedule;
  }

  public String getName() {
    return this.zoneName;
  }

  public int getZoneNumber() {
    return this.zoneNumber;
  }

  public String getPlant() {
    return this.plant;
  }

  public double getRain() {
    return this.rain_in;
  }

  public double getSolar() {
    return this.solar_wmhr;
  }

  public double getTmax() {
    return this.tmax_f;
  }

  public double getTmin() {
    return this.tmin_f;
  }

  public String getZoneType() {
    return this.zoneType;
  }

  public String getLFTestDate() {
    return this.lfTestDate;
  }

  public double[] processIrrigSchedule(double irrig_in, double irrig_time) {
    double[] retVal = new double[3]; 
    retVal[0] = irrig_in;
    retVal[1] = irrig_time;
    retVal[2] = 0;
    return retVal;
  }

  public void setCommonInfo(ResultSet rs) {
    try {
      this.zoneName = rs.getString("zoneName");
      this.uid = rs.getInt("uid");
      this.zid = rs.getInt("zid");

      zoneNumber = rs.getInt("zoneNumber");
      plant = rs.getString("plant");

      wsid = rs.getInt("wsid");
      contDiam = rs.getDouble("containerDiam_in");;
      irrig_in_hr = rs.getDouble("irrig_in_per_hr");
      irrig_capture = rs.getString("irrigCaptureAbility");
      irrig_uniformity = rs.getDouble("irrig_uniformity");
      elev_ft = rs.getDouble("elevation_ft");
      lat_deg = rs.getDouble("lattitude");
      long_deg = rs.getDouble("longitude");

      plantHt = rs.getDouble("plantHeight_in");
      plantWd = rs.getDouble("plantWidth_in");
      pctCover = rs.getDouble("pctCover");
      contSpacing = rs.getDouble("containerSpacing_in");
      spacingStr = rs.getString("spacing");
      spacing = 1; //square 
      if (spacingStr.equals("offset")) {
          spacing = 0.866; //triangular 
      }
      shade = rs.getDouble("shade");
      lf = rs.getDouble("leachingFraction")/100.0;
      availableWater = rs.getDouble("availableWater")/100.0;
      thresholdFactor = rs.getDouble("thresholdFactor")/100.0;

      irrig_gal_hr = rs.getDouble("irrig_gal_per_hr");
      zoneType = rs.getString("zoneType");
      productionArea = rs.getString("productionArea");
      irrigSchedule = rs.getString("irrigSchedule");
      fixedDays = rs.getInt("fixedDays");
      minIrrig = rs.getInt("minIrrig");
      ncycles = rs.getInt("ncycles");

      hourly_rain_thresh_in = rs.getDouble("hourly_rain_thresh_in");
      weekly_rain_thresh_in = rs.getDouble("weekly_rain_thresh_in");
    } catch(SQLException ex) {
      System.out.println(_mainClass+"::setCommonInfo> "+ex.toString());
      ex.printStackTrace();
    }

    Calendar c = new GregorianCalendar();
    this.doy = c.get(Calendar.DAY_OF_YEAR);
    this.firstHour = c.get(Calendar.HOUR_OF_DAY)+1;
    this.prevDeficit = 0;
  }

  public void setFirstHour(int firstHour) {
    this.firstHour = firstHour;
  }

  public void setHourlyWeather(double[] tmax_hourly, double[] tmin_hourly, double[] solar_hourly, double[] rain_hourly, boolean[] hasHourlyData) { 
    this.tmax_hourly = tmax_hourly;
    this.tmin_hourly = tmin_hourly;
    this.solar_hourly = solar_hourly;
    this.rain_hourly = rain_hourly;
    //Accumulate hourly weather
    this.tmax_f = tmax_hourly[0];
    this.tmin_f = tmin_hourly[0];
    this.solar_wmhr = solar_hourly[0];
    this.rain_in = rain_hourly[0]; 
    for (int j = 1; j < 24; j++) {
      if (!hasHourlyData[j]) continue;
      this.tmax_f = Math.max(this.tmax_f,  tmax_hourly[j]); 
      this.tmin_f = Math.min(this.tmin_f, tmin_hourly[j]); 
      this.solar_wmhr += solar_hourly[j]; 
      this.rain_in += rain_hourly[j];
    }
    this.solar_wmhr /= 24.0;
  }

  public void setPrevDeficit(double prevDeficit) {
    this.prevDeficit = prevDeficit;
  }

  public void setLastRunDate(String runDate) {
    this.lastRunDate = runDate;
  }

  public void setWeeklyRain(double rain) {
    this.weekly_rain_in = rain;
  }

  public void updateUserWeather(double tmax_f, double tmin_f, double solar_wmhr, double rain_in) {
    this.tmax_f = tmax_f;
    this.tmin_f = tmin_f;
    this.solar_wmhr = solar_wmhr;
    this.rain_in = rain_in;
  }

  public void setLFHourlyWeather(double[] tmax_lf, double[] tmin_lf, double[] solar_lf, double[] rain_lf, boolean[] hasHourlyData) {
  }

  public String getXML() {
    String xml = "      <zone name=\"" + zoneName + "\" zid=\"" + zid + "\" uid=\"" + uid + "\" wsid=\"" + wsid + "\" number=\"" + zoneNumber + "\" type=\"" + zoneType + "\">"; 
    xml += "\n        <property name=\"plant\" value=\"" + plant + "\"/>";
    xml += "\n        <property name=\"containerDiam_in\" value=\"" + contDiam + "\"/>";
    xml += "\n        <property name=\"irrig_in_per_hr\" value=\"" + irrig_in_hr + "\"/>";
    xml += "\n        <property name=\"irrigCaptureAbility\" value=\"" + irrig_capture + "\"/>";
    xml += "\n        <property name=\"irrig_uniformity\" value=\"" + irrig_uniformity + "\"/>";
    xml += "\n        <property name=\"elevation_ft\" value=\"" + elev_ft + "\"/>";
    xml += "\n        <property name=\"lattitude\" value=\"" + lat_deg + "\"/>";
    xml += "\n        <property name=\"longitude\" value=\"" + long_deg + "\"/>";
    xml += "\n        <property name=\"plantHeight_in\" value=\"" + plantHt + "\"/>";
    xml += "\n        <property name=\"plantWidth_in\" value=\"" + plantWd + "\"/>";
    xml += "\n        <property name=\"pctCover\" value=\"" + pctCover + "\"/>";
    xml += "\n        <property name=\"containerSpacing_in\" value=\"" + contSpacing + "\"/>";
    xml += "\n        <property name=\"spacing\" value=\"" + spacingStr + "\"/>";
    xml += "\n        <property name=\"shade\" value=\"" + shade + "\"/>";
    xml += "\n        <property name=\"leachingFraction\" value=\"" + (lf*100) + "\"/>";
    xml += "\n        <property name=\"availableWater\" value=\"" + (availableWater*100) + "\"/>";
    xml += "\n        <property name=\"thresholdFactor\" value=\"" + (thresholdFactor*100) + "\"/>";
    xml += "\n        <property name=\"irrig_gal_per_hr\" value=\"" + irrig_gal_hr + "\"/>";
    xml += "\n        <property name=\"productionArea\" value=\"" + productionArea + "\"/>";
    xml += "\n        <property name=\"irrigSchedule\" value=\"" + irrigSchedule + "\"/>";
    xml += "\n        <property name=\"fixedDays\" value=\"" + fixedDays + "\"/>";
    xml += "\n        <property name=\"minIrrig\" value=\"" + minIrrig + "\"/>";
    xml += "\n        <property name=\"ncycles\" value=\"" + ncycles + "\"/>";
    xml += "\n        <property name=\"hourly_rain_thresh_in\" value=\"" + hourly_rain_thresh_in + "\"/>";
    xml += "\n        <property name=\"weekly_rain_thresh_in\" value=\"" + weekly_rain_thresh_in + "\"/>";
    xml += "\n        <property name=\"prevDeficit\" value=\"" + prevDeficit + "\"/>";
    if (lastRunDate != null) xml += "\n        <property name=\"lastRunDate\" value=\"" + lastRunDate + "\"/>";
    if (zoneType.startsWith("LF")) {
      xml += "\n        <property name=\"lfTestDate\" value=\"" + lfTestDate + "\"/>";
      xml += "\n        <property name=\"lfTestRuntime\" value=\"" + lfTestRuntime + "\"/>";
      xml += "\n        <property name=\"lfTestPct\" value=\"" + lfTestPct + "\"/>";
      xml += "\n        <property name=\"lfTargetPct\" value=\"" + lfTargetPct + "\"/>";
      xml += "\n        <property name=\"et_fac\" value=\"" + et_fac + "\"/>";
    }

    xml += "\n      </zone>";
    return xml;
  }

  public boolean createFromXML(Element elmnt) {
    boolean success = true;
    try {
      if (elmnt.hasAttribute("name")) zoneName = elmnt.getAttribute("name").trim();
      if (elmnt.hasAttribute("uid")) uid = Integer.parseInt(elmnt.getAttribute("uid").trim());
      if (elmnt.hasAttribute("zid")) zid = Integer.parseInt(elmnt.getAttribute("zid").trim());
      if (elmnt.hasAttribute("wsid")) wsid = Integer.parseInt(elmnt.getAttribute("wsid").trim());
      if (elmnt.hasAttribute("number")) zoneNumber = Integer.parseInt(elmnt.getAttribute("number").trim());
      if (elmnt.hasAttribute("type")) zoneType = elmnt.getAttribute("type").trim();

      //look for properties
      NodeList proplist = elmnt.getElementsByTagName("property");
      for (int i = 0; i < proplist.getLength(); i++) {
        Node propNode = proplist.item(i);
        if (propNode.getNodeType() == Node.ELEMENT_NODE) {
          Element propElmnt = (Element)propNode;
          if (propElmnt.hasAttribute("name") && propElmnt.hasAttribute("value")) {
	    String propName = propElmnt.getAttribute("name").trim();
	    String propValue = propElmnt.getAttribute("value").trim();
	    if (propName.equals("plant")) plant = propValue;
            if (propName.equals("containerDiam_in")) contDiam = Double.parseDouble(propValue);
            if (propName.equals("irrig_in_per_hr")) irrig_in_hr = Double.parseDouble(propValue);
            if (propName.equals("irrigCaptureAbility")) irrig_capture = propValue;
            if (propName.equals("irrig_uniformity")) irrig_uniformity = Double.parseDouble(propValue);
            if (propName.equals("elevation_ft")) elev_ft = Double.parseDouble(propValue);
            if (propName.equals("lattitude")) lat_deg = Double.parseDouble(propValue);
            if (propName.equals("longitude")) long_deg = Double.parseDouble(propValue);
            if (propName.equals("plantHeight_in")) plantHt = Double.parseDouble(propValue);
            if (propName.equals("plantWidth_in")) plantWd = Double.parseDouble(propValue);
            if (propName.equals("pctCover")) pctCover = Double.parseDouble(propValue);
            if (propName.equals("containerSpacing_in")) contSpacing = Double.parseDouble(propValue);
            if (propName.equals("spacing")) spacingStr = propValue;
            if (propName.equals("shade")) shade = Double.parseDouble(propValue);
            if (propName.equals("leachingFraction")) lf = Double.parseDouble(propValue)/100.0;
            if (propName.equals("availableWater")) availableWater = Double.parseDouble(propValue)/100.0;
            if (propName.equals("thresholdFactor")) thresholdFactor = Double.parseDouble(propValue)/100.0;
            if (propName.equals("irrig_gal_per_hr")) irrig_gal_hr = Double.parseDouble(propValue);
            if (propName.equals("productionArea")) productionArea = propValue;
            if (propName.equals("irrigSchedule")) irrigSchedule = propValue;
            if (propName.equals("fixedDays")) fixedDays = Integer.parseInt(propValue);
            if (propName.equals("minIrrig")) minIrrig = Integer.parseInt(propValue);
            if (propName.equals("ncycles")) ncycles = Integer.parseInt(propValue);
	    if (propName.equals("hourly_rain_thresh_in")) hourly_rain_thresh_in = Double.parseDouble(propValue);
            if (propName.equals("weekly_rain_thresh_in")) hourly_rain_thresh_in = Double.parseDouble(propValue);
            if (propName.equals("prevDeficit")) prevDeficit = Double.parseDouble(propValue);
            if (propName.equals("lastRunDate")) lastRunDate = propValue;
            if (propName.equals("lfTestDate")) lfTestDate= propValue;
            if (propName.equals("lfTestRuntime")) lfTestRuntime = Double.parseDouble(propValue);
            if (propName.equals("lfTestPct")) lfTestPct = Double.parseDouble(propValue);
            if (propName.equals("lfTargetPct")) lfTargetPct = Double.parseDouble(propValue);
	    if (propName.equals("et_fac")) et_fac = Double.parseDouble(propValue);
	  }
	}
      }
      spacing = 1; //square 
      if (spacingStr.equals("offset")) {
        spacing = 0.866; //triangular 
      }
    } catch(Exception e) {
      System.out.println(_mainClass+"::createFromXML> ERROR: "+e.toString());
      return false;
    }
    return success;
  }

  public boolean createFromXMLString(String xml) {
    //this is a string representation of XML, presumably obtained over socket
    //connection to serialize object, replacing need for Serializeable
    Element elmnt;
    try {
      //create XML Element
      elmnt =  DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(new ByteArrayInputStream(xml.getBytes()))
        .getDocumentElement();
      //use createFromXML to parse Element
      return createFromXML(elmnt);
    } catch(Exception e) {
      System.out.println(_mainClass+"::createFromXMLString> ERROR: "+e.toString());
      return false;
    }
  }

}
