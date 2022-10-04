package com.cwarner62.cirrig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

/* Base class to be extended by CirrigMainActivity and CirrigSplashAcivity.
 * Contains preferences and other things that will be accessed by both 
 * main and splash activities such as weather downloading from FAWN.

 * Contains menu with about as well
 */

public class CirrigActivity extends AppCompatActivity {

    // prefs
    int spacingArr1, canopyDensity1;/*irrigCaptureAbility1;*/
    int spacingArr3, canopyDensity3, irrigCaptureAbility3;
    int fawnid, containerType, irrigCaptureAbility;
    float containerDiam1, spacing, irrig_in_per_hr1;
    float containerDiam3, contSpacing3, irrig_in_per_hr3;
    float plantWidth1, irrigRate;
    boolean overrideIrrigRate = false, overrideRain = false, useGPS;
    float overrideIrrigVal, overrideRainVal;
    long lastDownloadTime = 0, lastdbTime;
    String etoOverride, rainOverride, solarOverride, tmaxOverride, tminOverride;
    String rainPast24Value, solarPast24Value, tmaxPast24Value, tminPast24Value;
    boolean etoFlag, solarFlag, tmaxFlag, tminFlag, rainFlag;

    public final int MENU_PREFS = 0;
    public final int MENU_ABOUT = 1;

    WeatherDatabaseHandler weatherdb;
    ProgressDialog ringProgressDialog;

    boolean _downloading = false, _weatherUpdated = false;
    boolean override = false, isInFL = true, canGetLocation = true;

    String currentRuntime;

    /**
     * Allow subclasses to override onCreate
     */

    public void savePrefs() {
        // Save user preferences. We need an Editor object to
        // make changes. All objects are from android.content.Context
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("containerDiam1", containerDiam1);
        editor.putFloat("spacing", spacing);
        editor.putInt("spacingArr1", spacingArr1);
        editor.putFloat("plantWidth1", plantWidth1);
        editor.putInt("canopyDensity1", canopyDensity1);
        //editor.putInt("irrigCaptureAbility1", irrigCaptureAbility1);
        editor.putFloat("irrig_in_per_hr1", irrig_in_per_hr1);
        editor.putFloat("containerDiam3", containerDiam3);
        editor.putFloat("contSpacing3", contSpacing3);
        editor.putInt("spacingArr3", spacingArr3);
        editor.putInt("canopyDensity3", canopyDensity3);
        editor.putInt("irrigCaptureAbility3", irrigCaptureAbility3);
        editor.putFloat("irrig_in_per_hr3", irrig_in_per_hr3);
        editor.putInt("fawnid", fawnid);
        //editor.putInt("containerType", containerType);
        editor.putInt("irrigCaptureAbility", irrigCaptureAbility);
        editor.putBoolean("overrideIrrigRate", overrideIrrigRate);
        editor.putBoolean("overrideRain", overrideRain);
        editor.putFloat("overrideIrrigVal", overrideIrrigVal);
        editor.putFloat("overrideRainVal", overrideRainVal);
        editor.putBoolean("useGPS", useGPS);
        editor.putFloat("irrigRate", irrigRate);
        editor.putString("RunTime", currentRuntime);
        editor.putBoolean("OverrideWeather", override);
        editor.putString("EtoOverride", etoOverride);
        editor.putString("SolarOverride", solarOverride);
        editor.putString("TmaxOverride", tmaxOverride);
        editor.putString("TminOverride", tminOverride);
        editor.putString("RainOverride", rainOverride);
        editor.putBoolean("EtoFlag", etoFlag);
        editor.putBoolean("SolarFlag", solarFlag);
        editor.putBoolean("TmaxFlag", tmaxFlag);
        editor.putBoolean("TminFlag", tminFlag);
        editor.putBoolean("RainFlat", rainFlag);
        editor.putString("RainPast24", rainPast24Value);
        editor.putString("SolarPast24", solarPast24Value);
        editor.putString("TmaxPast24", tmaxPast24Value);
        editor.putString("TminPast24", tminPast24Value);
        // Don't forget to commit your edits!!!
        editor.commit();
    }

    public void getPrefs() {
        // Get user prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        containerDiam1 = prefs.getFloat("containerDiam1", 6);
        spacing = prefs.getFloat("spacing", 6);
        spacingArr1 = prefs.getInt("spacingArr1", Zone.SPACING_OFFSET);
        plantWidth1 = prefs.getFloat("plantWidth1", 6f);
        canopyDensity1 = prefs
                .getInt("canopyDensity1", Zone.CANOPY_DENSITY_MED);
//        irrigCaptureAbility1 = prefs.getInt("irrigCaptureAbility1",
//                Zone.IRRIG_CAPTURE_LOW);
        irrig_in_per_hr1 = prefs.getFloat("irrig_in_per_hr1", 0.5f);
        containerDiam3 = prefs.getFloat("containerDiam3", 10);
        contSpacing3 = prefs.getFloat("contSpacing3", 10);
        spacingArr3 = prefs.getInt("spacingArr3", Zone.SPACING_OFFSET);
        canopyDensity3 = prefs
                .getInt("canopyDensity3", Zone.CANOPY_DENSITY_MED);
        irrigCaptureAbility3 = prefs.getInt("irrigCaptureAbility3",
                Zone.IRRIG_CAPTURE_LOW);
        irrig_in_per_hr3 = prefs.getFloat("irrig_in_per_hr3", 0.5f);
        fawnid = prefs.getInt("fawnid", 260);
        containerType = prefs.getInt("containerType", 1);
        irrigCaptureAbility = prefs.getInt("irrigCaptureAbility",
                Zone.IRRIG_CAPTURE_LOW);
        overrideIrrigRate = prefs.getBoolean("overrideIrrigRate", false);
        irrigRate = prefs.getFloat("irrigRate", 0.5f);
        overrideRain = prefs.getBoolean("overrideRain", false);
        overrideIrrigVal = prefs.getFloat("overrideIrrigVal", 0.5f);
        overrideRainVal = prefs.getFloat("overrideRainVal", 0);
        useGPS = prefs.getBoolean("useGPS", false);
        currentRuntime = prefs.getString("RunTime", "");
        override = prefs.getBoolean("OverrideWeather", false);
        etoOverride = prefs.getString("EtoOverride", "0.00");
        solarOverride = prefs.getString("SolarOverride", "0");
        tmaxOverride = prefs.getString("TmaxOverride", "0");
        tminOverride = prefs.getString("TminOverride", "0");
        rainOverride = prefs.getString("RainOverride", "0.00");
        etoFlag = prefs.getBoolean("EtoFlag", false);
        solarFlag = prefs.getBoolean("SolarFlag", false);
        tmaxFlag = prefs.getBoolean("TmaxFlag", false);
        tminFlag = prefs.getBoolean("TminFlag", false);
        rainFlag = prefs.getBoolean("RainFlat", false);
        rainPast24Value = prefs.getString("RainPast24", "0.00");
        solarPast24Value = prefs.getString("SolarPast24", "0");
        tmaxPast24Value = prefs.getString("TmaxPast24", "0") ;
        tminPast24Value = prefs.getString("TminPast24", "0");
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.add(0, MENU_PREFS, 0,
        // "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, MENU_ABOUT, 0, "About").setIcon(
                android.R.drawable.ic_menu_info_details);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ABOUT:
                about();
                return true;
        }
        return false;
    }

    /* Pop up InfoDialog */
    public void about() {
        startActivity(new Intent(this, CirrigAboutActivity.class));
    }

    public boolean readFawnData() {
        /*
         * set up async task to download fawn data for currently selected
		 * station
		 */

        new Throwable().printStackTrace();
        _downloading = true;
        // get timestamp of last weather datapoint in database for selected
        // fawnid

        lastdbTime = weatherdb.getLastDate(fawnid);
        long currTime = System.currentTimeMillis() / 1000;
        if (currTime - lastdbTime < 900) {
            _downloading = false;
            updateWeatherTable();
            updateIrrigTable();
            return true;
        }

        // setup async task to download
        DownloadFawnData downloader = new DownloadFawnData();
        downloader.execute(fawnid);
        return true;
    }

    public void updateWeatherTable() { return; }

    public void updateIrrigTable() { return; }

    // async task to download data
    private class DownloadFawnData extends AsyncTask<Integer, Void, Integer> {

        protected Integer doInBackground(Integer... fawnid) {
            BufferedReader fawnIn;
            String currLine = "";
            int result = 0;
            String response = "";
            URL url;
            WeatherDatapoint currWeather;
            Vector<WeatherDatapoint> weatherToAdd = new Vector();
            boolean readWeek = true;
            long currTimestamp, last96Timestamp = 0;
            // dialog must be run on UI thread
            CirrigActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ringProgressDialog = ProgressDialog.show(
                            CirrigActivity.this, "Please wait ...",
                            "Downloading Weather Data ...", true);
                    ringProgressDialog.setCancelable(true);
                }
            });

            try {
                // URL for FAWN .csv file containing last 96 datapoints (24
                // hours)
                url = new URL(
                        "https://fawn.ifas.ufl.edu/controller.php/today/last96/"
                                + fawnid[0] + ";csv");
                fawnIn = new BufferedReader(new InputStreamReader(
                        url.openStream()));
                while (currLine != null) {
                    currLine = fawnIn.readLine();
                    if (currLine == null)
                        break;
                    if (!currLine.startsWith(String.valueOf(fawnid[0])))
                        continue; // presumably header line
                    // create WeatherDatapoint out of current line
                    currWeather = new WeatherDatapoint(currLine);
                    currTimestamp = currWeather.getTimestamp();
                    if (currTimestamp > lastdbTime) {
                        // this is newer than last datapoint in database for
                        // this fawn id
                        weatherToAdd.add(currWeather);
                        last96Timestamp = currTimestamp;
                        result++;
                    } else {
                        // this datapoint is already in database. Mark readWeek
                        // as false and break
                        // we don't need to go any farther back in time
                        readWeek = false;
                        break;
                    }
                }
                fawnIn.close();
                currLine = "";
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (readWeek) {
                try {
                    // if all 96 datapoints above were new, continue to load
                    // more weather by reading last week of data from fawn
                    url = new URL(
                            "https://fawn.ifas.ufl.edu/controller.php/week/obs/"
                                    + fawnid[0] + ";csv");
                    fawnIn = new BufferedReader(new InputStreamReader(
                            url.openStream()));
                    while (currLine != null) {
                        currLine = fawnIn.readLine();
                        if (currLine == null)
                            break;
                        if (!currLine.startsWith(String.valueOf(fawnid[0])))
                            continue; // presumably header line
                        // create WeatherDatapoint out of current line
                        currWeather = new WeatherDatapoint(currLine);
                        currTimestamp = currWeather.getTimestamp();
                        if (currTimestamp > lastdbTime) {
                            // this is newer than last datapoint in database for
                            // this fawn id
                            if (currTimestamp < last96Timestamp) {
                                // also compare to the earliest timestamp of the
                                // last 24 hours from above so as not to add
                                // duplicate datapoint
                                weatherToAdd.add(currWeather);
                                result++;
                            }
                        } else
                            break; // this datapoint is already in database.
                        // break here.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // add weather to database
            weatherdb.addWeather(weatherToAdd);
            return result;
        }

        protected void onPostExecute(Integer result) {
            // dismiss dialog
            ringProgressDialog.dismiss();
            // short dialog that says how many records were downloaded
            Toast.makeText(CirrigActivity.this,
                    "Downloaded " + result + " records.", Toast.LENGTH_SHORT)
                    .show();
            System.out.println("Downloaded " + result + " records.");
            // delete weather older than 10 days for this weather station
            int delrows = weatherdb.deleteOldWeather();
            System.out.println("Deleted " + delrows + " records.");
            lastDownloadTime = System.currentTimeMillis();
            _downloading = false;
            _weatherUpdated = true;
            updateWeatherTable();
            updateIrrigTable();
        }
    }
}
