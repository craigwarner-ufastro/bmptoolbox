package com.cwarner62.cirrig;

import java.text.SimpleDateFormat;
import java.util.Date;

//Class for an individual weather datapoint
public class WeatherDatapoint {
  int _id, wsid;
  long timestamp;
  float temp2m, solarRad, rain, windSpeed, rh;
  SimpleDateFormat fawndf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  public WeatherDatapoint() {
  }

  //create from specified components
  public WeatherDatapoint(int id, long timestamp, int wsid, float temp2m,
                          float solarRad, float rain, float windSpeed, float rh) {
    this._id = id;
    this.timestamp = timestamp;
    this.wsid = wsid;
    this.temp2m = temp2m;
    this.solarRad = solarRad;
    this.rain = rain;
    this.windSpeed = windSpeed;
    this.rh = rh;
  }

  //create from a line in FAWN csv file
  public WeatherDatapoint(String csvLine) {
    String[] tokens = csvLine.split(",");
    wsid = Integer.parseInt(tokens[0]);
    try {
      Date d = fawndf.parse(tokens[1]);
      timestamp = d.getTime()/1000L;
    } catch(Exception e) {
      e.printStackTrace();
    }
    solarRad = Float.parseFloat(tokens[2]);
    temp2m = Float.parseFloat(tokens[4]);
    windSpeed = Float.parseFloat(tokens[8]);
    rain = Float.parseFloat(tokens[11]);
    rh = Float.parseFloat(tokens[7]);
  }

  /* getters and setters */

  public int getID() { return this._id; }

  public void setID(int id) { this._id = id; }

  public int getWsid() { return this.wsid; }

  public void setWsid(int wsid) { this.wsid = wsid; }

  public long getTimestamp() { return this.timestamp; }

  public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

  public float getTemp() { return this.temp2m; }

  public void setTemp(float temp2m) { this.temp2m = temp2m; }

  public float getSolarRad() { return this.solarRad; }

  public void setSolarRad(float solarRad) { this.solarRad = solarRad; }

  public float getRain() { return this.rain; }

  public void setRain(float rain) { this.rain = rain; }

  public float getWindSpeed() { return this.windSpeed; }

  public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }

  public float getRelativeHumidity() { return this.rh; }

  public void setRelativeHumidity(float rh) { this.rh = rh; }

  public String toString() {
    String s = "Weather Datapoint [id=" + _id + "; timestamp="
            + fawndf.format(new Date(timestamp));
    s += "; wsid=" + wsid + "; temp2m=" + temp2m + "; solarRad=" + solarRad
            + "; rain=" + rain + "; windSpeed=" + windSpeed + "; rh=" + rh
            + "]";
    return s;
  }
}
