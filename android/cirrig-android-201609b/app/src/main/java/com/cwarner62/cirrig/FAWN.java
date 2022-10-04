package com.cwarner62.cirrig;

import android.location.Location;

import java.util.Iterator;
import java.util.LinkedHashMap;

/* This class contains hard-coded values of fawn id #s (used in URLs), station names, longitudes, latitudes, and elevations.
 * It also contains the inner class FAWNStation 
 */
public class FAWN {
  LinkedHashMap <Integer, FAWNStation> fawnStations;

  public FAWN() {
    fawnStations = new LinkedHashMap(34);
    fawnStations.put(new Integer(260), new FAWNStation(260, "Alachua", 29.803, -82.41, 38));
    fawnStations.put(new Integer(320), new FAWNStation(320, "Apopka", 28.642, -81.55, 18));
    fawnStations.put(new Integer(490), new FAWNStation(490, "Arcadia", 27.22, -81.838, 22));
    fawnStations.put(new Integer(304), new FAWNStation(304, "Avalon", 28.477, -81.64, 50));
    fawnStations.put(new Integer(350), new FAWNStation(350, "Balm", 27.76, -82.223, 44));
    fawnStations.put(new Integer(410), new FAWNStation(410, "Belle Glade", 26.668, -80.632, 5));
    fawnStations.put(new Integer(230), new FAWNStation(230, "Bronson", 29.402, -82.587, 23));
    fawnStations.put(new Integer(150), new FAWNStation(150, "Carrabelle", 29.843, -84.695, 7));
    fawnStations.put(new Integer(250), new FAWNStation(250, "Citra", 29.41, -82.17, 23));
    fawnStations.put(new Integer(405), new FAWNStation(405, "Clewiston", 26.739, -81.053, 9));
    fawnStations.put(new Integer(311), new FAWNStation(311, "Dade City", 28.3497, -82.2086, 30));
    fawnStations.put(new Integer(360), new FAWNStation(360, "Dover", 28.017, -82.233, 30));
    fawnStations.put(new Integer(420), new FAWNStation(420, "Fort Lauderdale", 26.087, -80.242, 8));
//    fawnStations.put(new Integer(430), new FAWNStation(430, "Fort Pierce", 27.427, -80.402, 7));
    fawnStations.put(new Integer(390), new FAWNStation(390, "Frostproof", 27.76, -81.54, 55));
    fawnStations.put(new Integer(270), new FAWNStation(270, "Hastings", 29.693, -81.445, 9));
    fawnStations.put(new Integer(440), new FAWNStation(440, "Homestead", 25.51, -80.498, 9));
    fawnStations.put(new Integer(450), new FAWNStation(450, "Immokalee", 26.462, -81.44, 15));
    fawnStations.put(new Integer(110), new FAWNStation(110, "Jay", 30.775, -87.14, 68));
    fawnStations.put(new Integer(340), new FAWNStation(340, "Kenansville", 27.963, -81.05, 25));
    fawnStations.put(new Integer(330), new FAWNStation(330, "Lake Alfred", 28.102, -81.712, 53));
    fawnStations.put(new Integer(170), new FAWNStation(170, "Live Oak", 30.303, -82.9, 35));
    fawnStations.put(new Integer(180), new FAWNStation(180, "Macclenny", 30.282, -82.138, 24));
    fawnStations.put(new Integer(130), new FAWNStation(130, "Marianna", 30.85, -85.165, 43));
    fawnStations.put(new Integer(160), new FAWNStation(160, "Monticello", 30.538, -83.917, 49));
    fawnStations.put(new Integer(480), new FAWNStation(480, "North Port", 27.148, -82.193, 16));
    fawnStations.put(new Integer(280), new FAWNStation(280, "Ocklawaha", 29.02, -81.968, 26));
    fawnStations.put(new Integer(303), new FAWNStation(303, "Okahumpka", 28.682, -81.887, 37));
    fawnStations.put(new Integer(380), new FAWNStation(380, "Ona", 27.398, -81.94, 28));
    fawnStations.put(new Integer(460), new FAWNStation(460, "Palmdale", 26.925, -81.402, 17));
    fawnStations.put(new Integer(290), new FAWNStation(290, "Pierson", 29.223, -81.448, 21));
    fawnStations.put(new Integer(240), new FAWNStation(240, "Putnam Hall", 29.697, -81.983, 35));
    fawnStations.put(new Integer(140), new FAWNStation(140, "Quincy", 30.545, -84.597, 73));
    fawnStations.put(new Integer(470), new FAWNStation(470, "Sebring", 27.422, -81.402, 36));
    fawnStations.put(new Integer(302), new FAWNStation(302, "Umatilla", 28.92, -81.632, 39));
  }

  //return FAWNStation by id
  public FAWNStation getFawnStation(int id) {
    return fawnStations.get(new Integer(id));
  }

  //return FAWNStation by name
  public FAWNStation getFawnStation(String name) {
    for (Iterator i = fawnStations.keySet().iterator(); i.hasNext(); ) {
      Integer key  = (Integer)i.next();
      FAWNStation currStation = (FAWNStation)fawnStations.get(key);
      if (currStation.getName().equals(name)) return currStation;
    }
    return null;
  }

  //get list of all FAWN station names
  public String[] getListOfStations() {
    String[] stationList = new String[fawnStations.size()];
    int n = 0;
    for (Iterator i = fawnStations.keySet().iterator(); i.hasNext(); ) {
      Integer key  = (Integer)i.next();
      FAWNStation currStation = (FAWNStation)fawnStations.get(key);
      stationList[n] = currStation.getName();
      n++;
    }
    return stationList;
  }

  //convenience methods
  public String getFawnStationName(int id) {
    return (fawnStations.get(new Integer(id))).getName();
  }

  public int getFawnId(String name) {
    for (Iterator i = fawnStations.keySet().iterator(); i.hasNext(); ) {
      Integer key  = (Integer)i.next();
      FAWNStation currStation = (FAWNStation)fawnStations.get(key);
      if (currStation.getName().equals(name)) return currStation.getId();
    }
    return -1;
  }

  //return the station closest to the given longitude and latitude
  public FAWNStation getClosestStation(double latitude, double longitude) {
    FAWNStation closest = null;
    float distance = 25000.f;
    for (Iterator i = fawnStations.keySet().iterator(); i.hasNext(); ) {
      Integer key  = (Integer)i.next();
      FAWNStation currStation = (FAWNStation)fawnStations.get(key);
      float currDist = currStation.distanceFrom(latitude, longitude);
      if (currDist < distance) {
	    distance = currDist;
        closest = currStation;
      }
    }
    return closest;
  }

  /* Class representing an individual FAWN station.  Contains id, name, elevation, longitude, latitude, and a method to calculate the distance from a given long/lat */
  public class FAWNStation {
    private int id;
    private String name;
    private double latitude, longitude, elevationM;

    public FAWNStation(int id, String name, double latitude, double longitude, double elevationM) {
      this.id = id;
      this.name = name;
      this.latitude = latitude;
      this.longitude = longitude;
      this.elevationM = elevationM;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getElevation() { return elevationM; }

    public float distanceFrom(double pointLatitude, double pointLongitude) {
      float[] result = new float[1];
      Location.distanceBetween(latitude, longitude, pointLatitude, pointLongitude, result);
      return result[0];
    }
  }
}
