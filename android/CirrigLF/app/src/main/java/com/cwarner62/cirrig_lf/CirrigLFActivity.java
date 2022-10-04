package com.cwarner62.cirrig_lf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


/* Base class to be extended by CirrigMainActivity and CirrigSplashAcivity.
 * Contains preferences and other things that will be accessed by both 
 * main and splash activities such as weather downloading from FAWN.

 * Contains menu with about as well
 */

public class CirrigLFActivity extends AppCompatActivity {

    // prefs
    boolean units_s_kg, units_m_kg; //units for s and m, g if false, kg if true
    int nplants_s, nplants_m; //num of test plants for small and medium
    boolean use_cirrig; // interface with cirrig

    public final int MENU_PREFS = 0;
    public final int MENU_ABOUT = 1;

    ProgressDialog ringProgressDialog;

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
        editor.putBoolean("units_s_kg", units_s_kg);
        editor.putBoolean("units_m_kg", units_m_kg);
        editor.putInt("nplants_s", nplants_s);
        editor.putInt("nplants_m", nplants_m);
        editor.putBoolean("use_cirrig", use_cirrig);
        // Don't forget to commit your edits!!!
        editor.commit();
    }

    public void getPrefs() {
        // Get user prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        units_s_kg = prefs.getBoolean("units_s_kg", false);
        units_m_kg = prefs.getBoolean("units_m_kg", true);
        nplants_s = prefs.getInt("nplants_s", 4);
        nplants_m = prefs.getInt("nplants_m", 3);
        use_cirrig = prefs.getBoolean("use_cirrig", false);
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
        startActivity(new Intent(this, CirrigLFAboutActivity.class));
    }

    public void updateWeatherTable() { return; }

    public void updateIrrigTable() { return; }


}
