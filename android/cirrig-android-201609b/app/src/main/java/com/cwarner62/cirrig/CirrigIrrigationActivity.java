package com.cwarner62.cirrig;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by qianyi on 9/27/2016.
 */
public class CirrigIrrigationActivity extends CirrigMainActivity {

    ActionBar actionBar;
    TableRow tvOverrideRow;
/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.weather_past_24hours_screen);

        setupOverrideOption();
        updateWeatherTable();
        updateIrrigTable();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Weather-Past 24 Hours");
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                updateIrrigTable();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupOverrideOption() {
        locTimeView2 = (TextView) findViewById(R.id.Location_Time);
        tvRuntimeOverride = (TextView)findViewById(R.id.TextView_RunTime_Override);
        tvOverrideRow = (TableRow)findViewById(R.id.OverrideRow);

        //Select FAWN station
        tvFawnStation = (TextView) findViewById(R.id.TextView_Settings_FAWN);
        tvFawnStation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // selected
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        CirrigIrrigationActivity.this);
                // get list of fawn station names
                final String[] fawnStations = fawn.getListOfStations();
                // find current fawn station in list
                String currStation = fawn.getFawnStationName(fawnid);
                int idx = 0;
                for (int j = 0; j < fawnStations.length; j++) {
                    if (fawnStations[j].equals(currStation)) {
                        idx = j;
                        break;
                    }
                }
                // set title
                builder.setTitle("Choose a FAWN station");
                builder.setSingleChoiceItems(fawnStations, idx,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // The 'which' argument contains the index
                                // position
                                // of the selected item
                                tvFawnStation.setText(fawnStations[which]);
                                fawnid = fawn.getFawnId(fawnStations[which]);
                                savePrefs();
                                checkLastDownload();
                                useGPS = false;
                                switch_gps.setChecked(false);
                                dialog.dismiss();

                            }
                        });
                builder.setNegativeButton("Cancel", null);
                // create alert dialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // Initialize over ETO field
//        tvEtoOverride = (TextView) findViewById(R.id.TextView_ETo_Override);
//        tvEtoOverride.addTextChangedListener(new FieldWatcher(tvEtoOverride,
//                new FieldStateListener() {
//                    @Override public void onFieldStateChanged(boolean isEmpty) {
//                        //overrideIrrigRate = !isEmpty;
//                        //add a new method to check if user has override all input and update irrigation tabl
//                    }
//                }));
//
//        etoOverrideInput = new EditText(this);
//        etoOverrideInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        etoOverrideInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
//
//        etoOverrideDialog = new AlertDialog.Builder(CirrigIrrigationActivity.this)
//                .setTitle("Enter Override ETO Value (inch)")
//                .setView(etoOverrideInput)
//                .setCancelable(true)
//                .setNeutralButton("Cancel", null)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override public void onClick(DialogInterface dialog, int which) {
//                        if(!etoOverrideInput.getText().toString().equals("")){
//
//                            if(etoOverrideInput.getText().toString() == "0") {
//                                tvEtoOverride.setText("0.00");
//                                etoOverride = "0.00";
//                            }else {
//                                Float f = Float.parseFloat(etoOverrideInput.getText().toString());
//                                tvEtoOverride.setText(String.format("%.2f", f));
//                                etoOverride = String.format("%.2f", f);
//                            }
//                            //update eto value
//                            updateOverrideValue("ETo", etoOverrideInput.getText().toString());
//                            if(ValidateOverrideInputValue()) {
//                                calculateOverrideIrrig();
//                            }
//                            etoFlag = true;
//                            savePrefs();
//                        }
//                    }
//                })
//                .create();
//
//        etoOverrideInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if ((event != null
//                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
//                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                    etoOverrideDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        etoOverrideDialog.getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//
//        tvEtoOverride.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                if(override) {
//                    etoOverrideDialog.show();
//                }
//            }
//        });

        //initilize override solar value
        tvSolarOverride = (TextView)findViewById(R.id.TextView_SolarRad_Override);
        tvSolarOverride.addTextChangedListener(new FieldWatcher(tvSolarOverride,
                new FieldStateListener() {
                    @Override public void onFieldStateChanged(boolean isEmpty) {
                        //overrideIrrigRate = !isEmpty;
                        //update eto value
                    }
                }));

        solarOverrideInput = new EditText(this);
        solarOverrideInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        solarOverrideInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        solarOverrideDialog = new AlertDialog.Builder(CirrigIrrigationActivity.this)
                .setTitle("Enter Override Solar Radiance Value (W/m2)")
                .setView(solarOverrideInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!solarOverrideInput.getText().toString().equals("")){
                            tvSolarOverride.setText(solarOverrideInput.getText());
                            solarOverride = solarOverrideInput.getText().toString();
                            updateOverrideValue("Solar", solarOverrideInput.getText().toString());
//                            if(ValidateOverrideInputValue()) {
                                calculateOverrideIrrig();
//                            }
                            solarFlag = true;
                            savePrefs();
                        }
                    }
                })
                .create();

        solarOverrideInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    solarOverrideDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        solarOverrideDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        tvSolarOverride.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(override) {
                    solarOverrideDialog.show();
                }
            }
        });

        //initilize override Tmax value
        tvTmaxOverride = (TextView) findViewById(R.id.TextView_TMax_Override);
        tvTmaxOverride.addTextChangedListener(new FieldWatcher(tvTmaxOverride,
                new FieldStateListener() {
                    @Override public void onFieldStateChanged(boolean isEmpty) {
                        //overrideIrrigRate = !isEmpty;
                        //update eto value
                        //add a new method to check if user has override all input and update irrigation table
                    }
                }));

        tmaxOverrideInput = new EditText(this);
        tmaxOverrideInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tmaxOverrideInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        tmaxOverrideDialog = new AlertDialog.Builder(CirrigIrrigationActivity.this)
                .setTitle("Enter Override Tmax Value (F)")
                .setView(tmaxOverrideInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!tmaxOverrideInput.getText().toString().equals("")){
                            tvTmaxOverride.setText(tmaxOverrideInput.getText());
                            tmaxOverride = tmaxOverrideInput.getText().toString();
                            updateOverrideValue("Tmax", tmaxOverrideInput.getText().toString());
//                            if(ValidateOverrideInputValue()) {
                                calculateOverrideIrrig();
//                            }
                            tmaxFlag = true;
                            savePrefs();
                        }
                    }
                })
                .create();

        tmaxOverrideInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    tmaxOverrideDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        tmaxOverrideDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        tvTmaxOverride.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(override) {
                    tmaxOverrideDialog.show();
                }
            }
        });

        //initilize override Tmin value
        tvTminOverride = (TextView)findViewById(R.id.TextView_TMin_Override);
        tvTminOverride.addTextChangedListener(new FieldWatcher(tvTminOverride,
                new FieldStateListener() {
                    @Override public void onFieldStateChanged(boolean isEmpty) {
                        //update eto value
                        //add a new method to check if user has override all input and update irrigation table
                    }
                }));

        tminOverrideInput = new EditText(this);
        tminOverrideInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tminOverrideInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        tminOverrideDialog = new AlertDialog.Builder(CirrigIrrigationActivity.this)
                .setTitle("Enter Override Tmin Value (F)")
                .setView(tminOverrideInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!tminOverrideInput.getText().toString().equals("")){
                            tvTminOverride.setText(tminOverrideInput.getText());
                            tminOverride = tminOverrideInput.getText().toString();
                            updateOverrideValue("Tmin", tminOverrideInput.getText().toString());
//                            if(ValidateOverrideInputValue()) {
                                calculateOverrideIrrig();
//                            }
                            tminFlag = true;
                            savePrefs();
                        }
                    }
                })
                .create();

        tminOverrideInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    tminOverrideDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        tminOverrideDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        tvTminOverride.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(override) {
                    tminOverrideDialog.show();
                }
            }
        });

        //initilize override rain value
        tvRainOverride = (TextView)findViewById(R.id.TextView_Rain_Override);
        tvRainOverride.addTextChangedListener(new FieldWatcher(tvRainOverride,
                new FieldStateListener() {
                    @Override public void onFieldStateChanged(boolean isEmpty) {
                    }
                }));

        rainOverrideInput = new EditText(this);
        rainOverrideInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rainOverrideInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        rainOverrideDialog = new AlertDialog.Builder(CirrigIrrigationActivity.this)
                .setTitle("Enter Override Rain Value (inch)")
                .setView(rainOverrideInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        //tvRainOverride.setText(rainOverrideInput.getText());
                        //conver to 2 decimal point
                        if(!rainOverrideInput.getText().toString().equals("")){
                            if(rainOverrideInput.getText().toString() == "0") {
                                tvRainOverride.setText("0.00");
                                rainOverride = "0.00";
                            }else {
                                Float f = Float.parseFloat(rainOverrideInput.getText().toString());
                                tvRainOverride.setText(String.format("%.2f", f));
                                rainOverride = String.format("%.2f", f);
                            }
                            updateOverrideValue("Rain", rainOverrideInput.getText().toString());
//                            if(ValidateOverrideInputValue()) {
                                calculateOverrideIrrig();
//                            }
                            rainFlag = true;
                            savePrefs();
                        }
                    }
                })
                .create();

        rainOverrideInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    rainOverrideDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        rainOverrideDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        tvRainOverride.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(override) {
                    rainOverrideDialog.show();
                }
            }
        });

        //GPS option
        switch_gps= (Switch) findViewById(R.id.Switch_Use_GPS);
        switch_gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                useGPS = ((Switch) v).isChecked();
                if (useGPS){
                    setFAWNByGPS();
                    updateIrrigTable();
                    updateWeatherTable();
                }
            }
        });
        switch_gps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useGPS = b;
                if (useGPS){
                    setFAWNByGPS();
                    updateIrrigTable();
                    updateWeatherTable();
                }
            }
        });
        // restore previous settings if gps box was checked
        if (useGPS) {
            switch_gps.setChecked(true);
            setFAWNByGPS();
            updateWeatherTable();
            updateIrrigTable();
        }

        //override value
        switch_override = (Switch) findViewById(R.id.OverrideSwitch);
        switch_override.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                override = ((Switch) view).isChecked();
                savePrefs();
                if(override) {
                    tvOverrideRow.setVisibility(View.VISIBLE);
                }else {
                    tvOverrideRow.setVisibility(View.GONE);
                    ResetOverrideValue();
                }
            }
        });

        switch_override.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                override = b;
                savePrefs();
                if(override) {
                    tvOverrideRow.setVisibility(View.VISIBLE);
                }else {
                    tvOverrideRow.setVisibility(View.GONE);
                    ResetOverrideValue();
                }
            }
        });

        if(override) {
            switch_override.setChecked(true);
            if(etoFlag)  tvEtoOverride.setText(etoOverride);
            if(solarFlag)  tvSolarOverride.setText(solarOverride);
            if(tminFlag) tvTminOverride.setText(tminOverride);
            if(tmaxFlag) tvTmaxOverride.setText(tmaxOverride);
            if(rainFlag) tvRainOverride.setText(rainOverride);
//            if(ValidateOverrideInputValue()) {
                calculateOverrideIrrig();
//            }
        }else {
            tvOverrideRow.setVisibility(View.GONE);
        }
    }

    // find nearest FAWN station using GPS
    public void setFAWNByGPS() {
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) { // gps enabled} // return boolean true/false
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            FAWN.FAWNStation fawnstation = fawn.getClosestStation(latitude,
                    longitude);
            if (fawnstation == null) {
                Toast.makeText(CirrigIrrigationActivity.this,
                        "Warning: Unable to obtain location.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String stationName = fawnstation.getName();
            String output = "Latitude: " + gps.getLatitude() + "\nLongitude: "
                    + gps.getLongitude() + "\nClosest Station: " + stationName;
            Toast.makeText(CirrigIrrigationActivity.this, output, Toast.LENGTH_SHORT)
                    .show();
            tvFawnStation.setText(stationName);
            fawnid = fawnstation.getId();
            readFawnData();
        } else {
            Toast.makeText(CirrigIrrigationActivity.this,
                    "Warning: Unable to obtain location.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void UpdateDownloadedData() {
        Thread downloadThread = new Thread() {
            public void run() {
                //forcedDownload =true;
                readFawnData();
                _downloading = true;
                while (_downloading) {
                    // sleep 0.5s
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                    }
                }
//                if (_weatherUpdated) {
                    // found new weather. Update weather and irrigation
                    // tables. Must be done on UI thread
                    CirrigIrrigationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            updateWeatherTable();
                            updateIrrigTable();
                        }
                    });
//                }
                //forcedDownload = false;
            }
        };
        downloadThread.start();
    }

    public void checkLastDownload() {
        lastdbTime = weatherdb.getLastDate(fawnid);
        long currTime = System.currentTimeMillis() / 1000;
        if (currTime - lastdbTime < 900) {
            updateWeatherTable();
            updateIrrigTable();
        } else {
            UpdateDownloadedData();
        }
    }

    private class DownloadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            readFawnData();
            while(_downloading){
                System.out.println("Downloading");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            updateWeatherTable();
            updateIrrigTable();
        }
    }

    public void ResetOverrideValue() {
//        tvEtoOverride.setText("");
        tvSolarOverride.setText("");
        tvTmaxOverride.setText("");
        tvTminOverride.setText("");
        tvRainOverride.setText("");
        tvRuntimeOverride.setText("");
        //reset input field text
//        etoOverrideInput.setText("");
        solarOverrideInput.setText("");
        tmaxOverrideInput.setText("");
        tminOverrideInput.setText("");
        rainOverrideInput.setText("");
        //reset value in Shared Preference to past 24 hours value
//        etoOverride = "";
        solarOverride = solarPast24Value;
        tmaxOverride = tmaxPast24Value;
        tminOverride = tminPast24Value;
        rainOverride = rainPast24Value;
        currentRuntime ="";
        override = false;
        savePrefs();

    }
    */

    /**
     * TextWatcher *
     */
/*
    private static class FieldWatcher implements TextWatcher {

        private final TextView field;
        private FieldStateListener listener;
        private boolean isEmpty;

        FieldWatcher(TextView field, FieldStateListener listener) {
            this.field = field;
            this.listener = listener;
            isEmpty = TextUtils.isEmpty(field.getText());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isNowEmpty = TextUtils.isEmpty(s);
            // if state has changed, update background, notify listeners, and update isEmpty
            if (isEmpty != isNowEmpty) {
                isEmpty = isNowEmpty;

                if (listener != null) {
                    listener.onFieldStateChanged(isEmpty);
                }
            }
        }
    }

    private interface FieldStateListener {
        // notifies the listener when the field transitions between having no content and having content
        void onFieldStateChanged(boolean isEmpty);
    }

    public void calculateOverrideIrrig() {
        Zone zoneOverride = zonedb.getZone(1);
        tvRuntimeOverride.setText(String.valueOf(calculateIrrigation(zoneOverride, true)));
        savePrefs();
        updateWeatherTable();
        updateIrrigTable(true);
    }
    */

//    public boolean ValidateOverrideInputValue() {
//        if(tvSolarOverride.getText().toString().equals("") || tvTmaxOverride.getText().toString().equals("") || tvTminOverride.getText().toString().equals("") || tvRainOverride.getText().toString().equals("")) {
//            return false;
//        }else {
//            return true;
//        }
//    }

    /*
    public void updateOverrideValue(String overrideField, String value) {
        Zone zone1 = zonedb.getZone(1);

        switch(overrideField) {
            case "ETo":
                zone1.setEto(unformatWeather("ETo", Float.parseFloat(value)));
                break;
            case "Solar":
                zone1.setSolar(unformatWeather("Solar", Float.parseFloat(value)));
                break;
            case "Tmax":
                zone1.setTmax(unformatWeather("Tmax", Float.parseFloat(value)));
                break;
            case "Tmin":
                zone1.setTmin(unformatWeather("Tmin", Float.parseFloat(value)));
                break;
            case "Rain":
                zone1.setRain(unformatWeather("Rain", Float.parseFloat(value)));
                break;
        }
        zonedb.updateZone(zone1);
    }

    public void useLast24HoursWeatherData() {
        Zone zone1 = zonedb.getZone(1);

        double[] weather = weatherdb.getWeatherSummaryLast24(fawnid);
        // calculate ETo
        double eto = calculateEto(weather);
        // format weather for display
        String[] formattedWeather = formatWeather(weather, eto);
        tvLast24Hours.setText("Past\n24 Hours");
        tvSolarOverride.setText(formattedWeather[0]);
        tvTmaxOverride.setText(formattedWeather[1]);
        tvTminOverride.setText(formattedWeather[2]);
        tvRainOverride.setText(formattedWeather[3]);
        tvEtoOverride.setText(formattedWeather[4]);

        zone1.setSolar((float)weather[0]);
        zone1.setTmax((float)weather[1]);
        zone1.setTmin((float)weather[2]);
        zone1.setRain((float)weather[3]);
        zone1.setEto((float)weather[4]);
        zonedb.updateZone(zone1);

        currentRuntime =String.valueOf(calculateIrrigation(zone1, false));
        savePrefs();
    }

    public float unformatWeather(String field, Float value) {
        float calculatedValue = value;

        switch(field) {
            case "Solar":
                calculatedValue = (float)(value-0.5);
                break;
            case "Tmax":
                calculatedValue = (float)((value -32.5)/1.8);
                break;
            case "Tmin":
                calculatedValue = (float)((value -32.5)/1.8);
                break;
            case "Rain":
                calculatedValue = (float)(value-0.005);
                break;
            case "ETo":
                calculatedValue = (float)(value-0.05);
                break;
        }

        return calculatedValue;
    }
*/
}
