package com.cwarner62.cirrig;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class CirrigMainActivity extends CirrigActivity {

    ActionBar actionBar;
    ViewFlipper flipper; // flip between Views
    FAWN fawn; // FAWN stations
    ZoneDatabaseHandler zonedb; // WeatherDatabaseHandler already defined in
    // base class
    // GUI components
    TextView tvFawnStation, tvContDiam1, tvSpacing1,
            tvContSpacing1, tvPlantWidth;
    TextView tvCanopyDensity1, tvIrrigRate1;
    TextView tvEtoOverride, tvSolarOverride, tvTmaxOverride, tvTminOverride, tvRainOverride, tvRuntimeOverride, tvLast24Hours;
    TextView locTimeView, locTimeView2;
    TextView longitudeViewHeader, latitudeViewHeader, elevationViewHeader, longitudeView, latitudeView, elevationView;
    RelativeLayout relativeLayoutCoords, relativeLayoutUpdateLocation;
    Switch switch_gps, switch_override;
    EditText etoOverrideInput, solarOverrideInput, tmaxOverrideInput, tminOverrideInput, rainOverrideInput;
    EditText longitudeInput, latitudeInput, elevationInput;
    Button btnRunTime, updateLocationButton;
    RadioGroup irrigCaptureChooser, navControl;
    AlertDialog etoOverrideDialog, solarOverrideDialog, tmaxOverrideDialog, tminOverrideDialog, rainOverrideDialog;
    AlertDialog longitudeDialog, latitudeDialog, elevationDialog;
    String runtimeValue = "";

    TableRow tvOverrideRow;

    private static final SparseIntArray IRRIG_CAPTURE_MAP = new SparseIntArray(5);

    static {
        IRRIG_CAPTURE_MAP.put(R.id.Button_IrrigCapture_Negative, Zone.IRRIG_CAPTURE_NEGATIVE);
        IRRIG_CAPTURE_MAP.put(R.id.Button_IrrigCapture_Nil, Zone.IRRIG_CAPTURE_NIL);
        IRRIG_CAPTURE_MAP.put(R.id.Button_IrrigCapture_Low, Zone.IRRIG_CAPTURE_LOW);
        IRRIG_CAPTURE_MAP.put(R.id.Button_IrrigCapture_Medium, Zone.IRRIG_CAPTURE_MEDIUM);
        IRRIG_CAPTURE_MAP.put(R.id.Button_IrrigCapture_High, Zone.IRRIG_CAPTURE_HIGH);
    }

    // arrays of weather values from database
    protected double[] weatherSummary, rain_96dataPoints, solar_96dataPoints;
    String lastWeatherDate;

    // keep track of previous irrig rate, rain
    double prevIrrigRate = 0, prevRain = 0;
    /**
     * ATTENTION: This    setupDisplay(); was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        getPrefs();
        fawn = new FAWN();
        zonedb = new ZoneDatabaseHandler(this);
        weatherdb = new WeatherDatabaseHandler(this);

        GPSTracker gps = new GPSTracker(this);
        isInFL = gps.isInFlorida();
        canGetLocation = gps.canGetLocation();

        setupDisplay();
        setupOverrideOption();
        updateZones();
        updateWeatherTable();
        updateIrrigTable();
        startDataDownloadThread();

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ab_icon);
        actionBar.setTitle("Irrigation");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // close database
        weatherdb.close();
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

    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Read preferences here
        getPrefs();
        updateIrrigField();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        getPrefs();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePrefs();
    }



    public void setupDisplay() {
        flipper = (ViewFlipper) findViewById(R.id.profileFlipper);
        //assign runtime textview

        tvLast24Hours = (TextView) findViewById(R.id.TextView_Weather_Past24);

        SimpleDateFormat weatherdf = new SimpleDateFormat(" EEE  dd-MMM  h:mm a");
        lastWeatherDate = weatherdf.format(new Date(weatherdb
                .getLastDate(fawnid) * 1000L));

        locTimeView = (TextView) findViewById(R.id.TextView_Location_Time);
        locTimeView2 = (TextView) findViewById(R.id.Location_Time);
        locTimeView.setText(fawn.getFawnStationName(fawnid) + ", "
                + lastWeatherDate);


        /** Nav buttons */
        navControl = (RadioGroup) findViewById(R.id.RadioGroup_navControl);
        navControl.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.Button_Irrig:
                        updateIrrigTable();
                        flipper.setDisplayedChild(0);
                        actionBar.setTitle("Irrigation");
                        break;
                    case R.id.Button_Calendar_Weather:
                        updateWeatherTable();
                        flipper.setDisplayedChild(1);
                        actionBar.setTitle("Weather - Past 24 Hours");
                }
            }
        });

        // Init irrig capture ability chooser
        irrigCaptureChooser = (RadioGroup) findViewById(R.id.RadioGroup_IrrigCapture);

        switch (irrigCaptureAbility) {
            case 0:
                irrigCaptureChooser.check(R.id.Button_IrrigCapture_Low);
                break;
            case 1:
                irrigCaptureChooser.check(R.id.Button_IrrigCapture_Medium);
                break;
            case 2:
                irrigCaptureChooser.check(R.id.Button_IrrigCapture_High);
                break;
            case 3:
                irrigCaptureChooser.check(R.id.Button_IrrigCapture_Nil);
                break;
            case 4:
                irrigCaptureChooser.check(R.id.Button_IrrigCapture_Negative);
                break;
        }

        irrigCaptureChooser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                irrigCaptureAbility = IRRIG_CAPTURE_MAP.get(checkedId);
                updateZones();
                updateWeatherTable();
                updateIrrigTable();
            }
        });

        btnRunTime = (Button) findViewById(R.id.TextView_RunTimeNumber);
        btnRunTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                navControl.check(R.id.Button_Calendar_Weather);
                //updateWeatherTable();
                //flipper.setDisplayedChild(1);
                //actionBar.setTitle("Weather - Past 24 Hours");
            }
        });

        /**
         * Settings text views Set up all settings text views in table and
         * assign OnClickListener to each that will pop up a settings dialog Use
         * a NumericSettingsDialog for numerical prefs and AlertDialog.Builder
         * for settings chosen from list
         */
        tvFawnStation = (TextView) findViewById(R.id.TextView_Settings_FAWN);

        tvContDiam1 = (TextView) findViewById(R.id.TextView_ContDiam1);
        tvContDiam1.setText(Float.toString(containerDiam1));
        tvContDiam1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // create SettingsListener to pass to NumericSettingsDialog to
                // take value entered and apply it
                SettingsListener listener = new SettingsListener() {
                    public void onOkClick(String value) {
                        try {
                            containerDiam1 = Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            Toast.makeText(CirrigMainActivity.this,
                                    "Invalid entry.", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        updateZones();
                        updateIrrigTable();
                        updateInputField();
                    }

                    public void onCancelClick() {
                    }
                };

                NumericSettingsDialog dialog = new NumericSettingsDialog(
                        CirrigMainActivity.this, listener,
                        "Container Diameter (in)", containerDiam1);
                dialog.show();
            }
        });

        tvSpacing1 = (TextView) findViewById(R.id.TextView_Spacing1);
        tvSpacing1.setText(Float.toString(spacing));
        tvSpacing1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // create SettingsListener to pass to NumericSettingsDialog to
                // take value entered and apply it
                SettingsListener listener = new SettingsListener() {
                    public void onOkClick(String value) {
                        try {
                            spacing = Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            Toast.makeText(CirrigMainActivity.this,
                                    "Invalid entry.", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        updateZones();
                        updateIrrigTable();
                        updateInputField();
                    }

                    public void onCancelClick() {
                    }
                };

                NumericSettingsDialog dialog = new NumericSettingsDialog(
                        CirrigMainActivity.this, listener,
                        "Spacing (inch between)", spacing);
                dialog.show();
            }
        });

        tvContSpacing1 = (TextView) findViewById(R.id.TextView_SpacingArr1);
        tvContSpacing1.setText(Zone.getSpacingString(spacingArr1));
        tvContSpacing1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        CirrigMainActivity.this);
                final String[] options = Zone.getSpacingStrings();
                // set title
                builder.setTitle("Spacing Arrangement");
                builder.setSingleChoiceItems(options, spacingArr1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // The 'which' argument contains the index
                                // position
                                // of the selected item
                                spacingArr1 = which;
                                updateZones();
                                updateIrrigTable();
                                updateInputField();
                                dialog.cancel();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Added plant width
        tvPlantWidth = (TextView) findViewById(R.id.TextView_PlantWid1);
        tvPlantWidth.setText(Float.toString(plantWidth1));
        tvPlantWidth.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // create SettingsListener to pass to NumericSettingsDialog to
                // take value entered and apply it
                SettingsListener listener = new SettingsListener() {
                    public void onOkClick(String value) {
                        try {
                            plantWidth1 = Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            Toast.makeText(CirrigMainActivity.this,
                                    "Invalid entry.", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        updateZones();
                        updateIrrigTable();
                        updateInputField();
                    }

                    public void onCancelClick() {
                    }
                };

                NumericSettingsDialog dialog = new NumericSettingsDialog(
                        CirrigMainActivity.this, listener,
                        "Plant Width (inch)", plantWidth1);
                dialog.show();
            }
        });

        tvCanopyDensity1 = (TextView) findViewById(R.id.TextView_CanopyDensity1);
        tvCanopyDensity1.setText(Zone.getCanopyDensityString(canopyDensity1));
        tvCanopyDensity1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        CirrigMainActivity.this);
                final String[] options = Zone.getCanopyDensityStrings();
                // set title
                builder.setTitle("Plant Canopy Density");
                builder.setSingleChoiceItems(options, canopyDensity1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // The 'which' argument contains the index
                                // position
                                // of the selected item
                                canopyDensity1 = which;
                                updateZones();
                                updateIrrigTable();
                                updateInputField();
                                dialog.cancel();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        tvIrrigRate1 = (TextView) findViewById(R.id.TextView_IrrigRate1);
        tvIrrigRate1.setText(Float.toString(irrig_in_per_hr1));
        tvIrrigRate1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // create SettingsListener to pass to NumericSettingsDialog to
                // take value entered and apply it
                SettingsListener listener = new SettingsListener() {
                    public void onOkClick(String value) {
                        try {
                            irrig_in_per_hr1 = Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            Toast.makeText(CirrigMainActivity.this,
                                    "Invalid entry.", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        updateZones();
                        updateIrrigTable();
                        updateInputField();
                    }

                    public void onCancelClick() {
                    }
                };

                NumericSettingsDialog dialog = new NumericSettingsDialog(
                        CirrigMainActivity.this, listener,
                        "Irrigation Rate (inch/hr)", irrig_in_per_hr1);
                dialog.show();
            }
        });

        relativeLayoutCoords = (RelativeLayout) findViewById(R.id.RelativeLayout_Coords);
        relativeLayoutUpdateLocation = (RelativeLayout) findViewById(R.id.RelativeLayout_Update_Location);
        longitudeViewHeader = (TextView) findViewById(R.id.TextView_LongitudeHeader);
        longitudeView = (TextView) findViewById(R.id.TextView_Longitude);

        longitudeInput = new EditText(this);
        longitudeInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        longitudeInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        longitudeDialog = new AlertDialog.Builder(CirrigMainActivity.this)
                .setTitle("Enter Longitude")
                .setView(longitudeInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!longitudeInput.getText().toString().equals("")){
                            longitudeView.setText(longitudeInput.getText());
                        }
                    }
                })
                .create();

        longitudeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    longitudeDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        longitudeDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        longitudeView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(!canGetLocation) {
                    longitudeDialog.show();
                }
            }
        });

        latitudeViewHeader = (TextView) findViewById(R.id.TextView_LatitudeHeader);
        latitudeView = (TextView) findViewById(R.id.TextView_Latitude);

        latitudeInput = new EditText(this);
        latitudeInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        latitudeInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        latitudeDialog = new AlertDialog.Builder(CirrigMainActivity.this)
                .setTitle("Enter Longitude")
                .setView(latitudeInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!latitudeInput.getText().toString().equals("")){
                            latitudeView.setText(latitudeInput.getText());
                        }
                    }
                })
                .create();

        latitudeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    latitudeDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        latitudeDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        latitudeView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(!canGetLocation) {
                    latitudeDialog.show();
                }
            }
        });

        elevationViewHeader = (TextView) findViewById(R.id.TextView_ElevationHeader);
        elevationView = (TextView) findViewById(R.id.TextView_Elevation);

        elevationInput = new EditText(this);
        elevationInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        elevationInput.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);

        elevationDialog = new AlertDialog.Builder(CirrigMainActivity.this)
                .setTitle("Enter Longitude")
                .setView(elevationInput)
                .setCancelable(true)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(!elevationInput.getText().toString().equals("")){
                            elevationView.setText(elevationInput.getText());
                        }
                    }
                })
                .create();

        elevationInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    elevationDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        elevationDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        elevationView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(!canGetLocation) {
                    elevationDialog.show();
                }
            }
        });

        updateLocationButton = (Button) findViewById(R.id.Button_Update_Location);
        updateLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation();
            }
        });
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
                        CirrigMainActivity.this);
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
                                readFawnData();
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

        solarOverrideDialog = new AlertDialog.Builder(CirrigMainActivity.this)
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

        tmaxOverrideDialog = new AlertDialog.Builder(CirrigMainActivity.this)
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

        tminOverrideDialog = new AlertDialog.Builder(CirrigMainActivity.this)
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

        rainOverrideDialog = new AlertDialog.Builder(CirrigMainActivity.this)
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
                    //tvEtoOverride.setText(etoOverride);
                    tvSolarOverride.setText(solarOverride);
                    tvTminOverride.setText(tminOverride);
                    tvTmaxOverride.setText(tmaxOverride);
                    tvRainOverride.setText(rainOverride);
                    calculateOverrideIrrig();
                    updateIrrigTable();
                    updateWeatherTable();
                }else {
                    tvOverrideRow.setVisibility(View.GONE);
                    ResetOverrideValue();
                    updateIrrigTable();
                    updateWeatherTable();
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
                    //tvEtoOverride.setText(etoOverride);
                    tvSolarOverride.setText(solarOverride);
                    tvTminOverride.setText(tminOverride);
                    tvTmaxOverride.setText(tmaxOverride);
                    tvRainOverride.setText(rainOverride);
                    calculateOverrideIrrig();
                    updateIrrigTable();
                    updateWeatherTable();
                }else {
                    tvOverrideRow.setVisibility(View.GONE);
                    ResetOverrideValue();
                    updateIrrigTable();
                    updateWeatherTable();
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

    /*
     * Update weather table with last 7 days of weather and past24 hours for
     * current FAWN station. Past 1 days is from past 24 to past 48 hours.
     */
    public void updateWeatherTable() {
        // get TextViews
        tvFawnStation.setText(fawn.getFawnStationName(fawnid));

        double[] weather = weatherdb.getWeatherSummaryLast24(fawnid);
        // calculate ETo
        double eto = calculateEto(weather);
        // format weather for display
        String[] formattedWeather = formatWeather(weather, eto);
        TextView weatherTitle = (TextView) findViewById(R.id.TextView_Weather_Title);
        weatherTitle.setText("FAWN\n" + fawn.getFawnStationName(fawnid));


        TextView solarRadPast24 = (TextView) findViewById(R.id.TextView_SolarRad_Past24);
        TextView tMaxPast24 = (TextView) findViewById(R.id.TextView_TMax_Past24);
        TextView tMinPast24 = (TextView) findViewById(R.id.TextView_TMin_Past24);
        TextView rainPast24 = (TextView) findViewById(R.id.TextView_Rain_Past24);
        TextView etoPast24 = (TextView) findViewById(R.id.TextView_ETo_Past24);
        // get last 24 hour summary for current fawn station
        solarRadPast24.setText(formattedWeather[0]);
        tMaxPast24.setText(formattedWeather[1]);
        tMinPast24.setText(formattedWeather[2]);
        rainPast24.setText(formattedWeather[3]);
        etoPast24.setText(formattedWeather[4]);

        //save past 24 hours into shared preference
        solarPast24Value = formattedWeather[0];
        tmaxPast24Value = formattedWeather[1];
        tminPast24Value = formattedWeather[2];
        rainPast24Value = formattedWeather[3];
        savePrefs();

        // Loop over last 7 days and set TextViews to display day, date, and
        // weather
        TextView currTextView;
        SimpleDateFormat dayAndMonthdf = new SimpleDateFormat("EEE dd-MMM");
        String[] weatherStrs = {"SolarRad", "TMax", "TMin", "Rain", "ETo"};
        Field f;
        int id;
        try {
            for (int i = 1; i <= 6; i++) {
                Date currDate = new Date(System.currentTimeMillis() - 86400000L
                        * i);
                // use reflection to be able to loop over last 7 days
                f = R.id.class.getDeclaredField("TextView_Weather_Date" + i);
                id = f.getInt(f);
                currTextView = (TextView) findViewById(id);
                currTextView.setText(dayAndMonthdf.format(currDate));

                // get weather for this day and current fawn station
                //CW - 4/9/18 - update index to i not i+1 -- days were off by 1
                //also use getWeatherForDay and not 24 hour based weather

                weather = weatherdb.getWeatherForDay(fawnid, i);

                // calculate ETo for this day
                eto = calculateEto(weather);
                // format weather for display
                formattedWeather = formatWeather(weather, eto);

                for (int j = 0; j < 5; j++) {
                    f = R.id.class.getDeclaredField("TextView_"
                            + weatherStrs[j] + "_Day" + i);
                    id = f.getInt(f);
                    currTextView = (TextView) findViewById(id);
                    currTextView.setText(formattedWeather[j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateLocation(); //call updateLocation here to check if is in FL or not
    }

    public void updateLocation() {
        if (switch_override == null) return; //not initialized yet
        GPSTracker gps = new GPSTracker(this);
        isInFL = gps.isInFlorida();
        canGetLocation = gps.canGetLocation();

        int visibility = View.VISIBLE;
        if (!isInFL) {
            visibility = View.GONE;
            switch_override.setChecked(true); //make sure override is turned ON
            TextView weatherTitle = (TextView) findViewById(R.id.TextView_Weather_Title);
            weatherTitle.setText("Outside\nof Florida");
        }
        TableRow currRow = (TableRow) findViewById(R.id.TableRowWeatherPast24);
        currRow.setVisibility(visibility);

        Field f;
        int id;
        try {
            for (int i = 1; i <= 6; i++) {
                f = R.id.class.getDeclaredField("TableRowWeatherDay" + i);
                id = f.getInt(f);
                currRow = (TableRow) findViewById(id);
                currRow.setVisibility(visibility);
            }
        } catch(Exception e) { e.printStackTrace(); }

        //set top bars visible/gone
        RelativeLayout tb = (RelativeLayout)findViewById(R.id.TopBarOne);
        tb.setVisibility(visibility);
        tb = (RelativeLayout)findViewById(R.id.TopBarTwo);
        tb.setVisibility(visibility);

        //update coord fields
        visibility = View.VISIBLE;
        if (isInFL) {
            visibility = View.GONE;
        } else if (canGetLocation) {
            longitudeView.setText(String.valueOf(gps.getLongitude()));
            latitudeView.setText(String.valueOf(gps.getLatitude()));
            elevationView.setText(String.valueOf(gps.getAltitude()));
        }
        relativeLayoutCoords.setVisibility(visibility);
        relativeLayoutUpdateLocation.setVisibility(visibility);

    }

    /*
     * Update zones with current settings (or set up zones on first time app is
     * run)
     */
    public void updateZones() {

        if (zonedb.getZonesCount() == 0) {
            // zones don't exist. Either database has been deleted or this is
            // first time running app
            double[] weather;
            double eto;

            // set up zones
            for (int i = 1; i <= 7; i++) {
                if (i == 1) {
                    weather = weatherdb.getWeatherSummaryLast24(fawnid);

                    zonedb.addZone(new Zone(i, containerDiam1, spacing, plantWidth1, irrig_in_per_hr1, (float) weather[0], (float) weather[1], (float) weather[2], (float) weather[3], (float) weather[4]));
                } else {
                    int j = i - 1;
                    Date currDate = new Date(System.currentTimeMillis() - 86400000L
                            * j);
                    // get weather for this day and current fawn station
                    System.out.println("UPDATE ZONE "+j);
                    weather = weatherdb.getWeatherForDay(fawnid, j);

                    zonedb.addZone(new Zone(i, containerDiam1, spacing, plantWidth1, irrig_in_per_hr1, (float) weather[0], (float) weather[1], (float) weather[2], (float) weather[3], (float) weather[4]));
                }

            }

            return;
        }

        // update zones if they already exist
        ArrayList<Zone> zoneList = (ArrayList) zonedb.getAllZones();
        for (Zone zone : zoneList) {
            zone.updateZone(containerDiam1, spacing, spacingArr1, plantWidth1, canopyDensity1, irrig_in_per_hr1, irrigCaptureAbility);
            zonedb.updateZone(zone);
        }
    }

    /*
     * Update irrigation table to display irrigation amounts for all zones and
     * current settings
     */

    public void updateInputField() {
        tvContDiam1.setText(String.valueOf(containerDiam1));
        tvSpacing1.setText(String.valueOf(spacing));
        tvContSpacing1.setText(Zone.getSpacingString(spacingArr1));
        tvPlantWidth.setText(String.valueOf(plantWidth1));
        tvCanopyDensity1.setText(Zone.getCanopyDensityString(canopyDensity1));
        tvIrrigRate1.setText(String.valueOf(irrig_in_per_hr1));
    }

    public void updateIrrigTable() {
        // get ArrayList of 7 zones based on container diameter selected
        ArrayList<Zone> zoneList;
        zoneList = (ArrayList<Zone>) zonedb.getAllZones();
        TextView currTextView;

        // loop over TextViews in table and set plant width and irrigation value
        // for each zone
        Field f;
        int id;
        int i = 1;

        try {
            //Seven zones in total
            for (Zone zone : zoneList) {

                System.out.println("UPDATE IRRIG "+i);
                getWholeDayDatapoints(i);
                runtimeValue = String.valueOf(calculateIrrigation(zone, false));


                //Set up run time on irrigation page
                if (i == 1) {
                    btnRunTime.setText(runtimeValue);
                    currentRuntime = runtimeValue;
                    locTimeView2.setText(lastWeatherDate);
                    if (override) {
                        //update with override value
                        currentRuntime = String.valueOf(calculateIrrigation(zone, override));
                        tvRuntimeOverride.setText(currentRuntime);
                        btnRunTime.setText(currentRuntime);
                        locTimeView.setText("Override, "+lastWeatherDate);
                    } else {
                        locTimeView.setText(fawn.getFawnStationName(fawnid) + ", " + lastWeatherDate);
                    }
                    savePrefs();
                }
                f = R.id.class.getDeclaredField("TextView_RunTime_Zone" + i);
                id = f.getInt(f);
                currTextView = (TextView) findViewById(id);
                // do irrigation calculations heref
                currTextView.setText(runtimeValue);
                i++;
                if (i == 8) break; //ensure no more than 7 zones
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* update weather  -- no longer used 4/10/18
    public void getLatestWeather() {
        // no need to requery database if nothing has changed
        if (!_weatherUpdated && weatherSummary != null)
            return;
        SimpleDateFormat weatherdf = new SimpleDateFormat("EEE  dd-MMM  h:mm a");
        lastWeatherDate = weatherdf.format(new Date(weatherdb
                .getLastDate(fawnid) * 1000L));
        // weather = avg solar, max, min temp, rain sum for last 24
        weatherSummary = weatherdb.getWeatherSummaryLast24(fawnid);
        // get all rain and solar radiation datapoints for last 24 hours (96
        // datapoints)
        rain_96dataPoints = weatherdb.getAllRainLast24(fawnid);
        solar_96dataPoints = weatherdb.getAllSolarRadLast24(fawnid);
        _weatherUpdated = false;
    }
    */

    public void getWholeDayDatapoints(int pastDay) {
        SimpleDateFormat weatherdf = new SimpleDateFormat("EEE  dd-MMM  h:mm a");
        lastWeatherDate = weatherdf.format(new Date(weatherdb
                .getLastDate(fawnid) * 1000L));
        if (pastDay > 1) {
            weatherSummary = weatherdb.getWeatherForDay(fawnid, pastDay-1);
            // get all rain and solar radiation datapoints for last 24 hours (96
            // datapoints)
            //getAll... methods are deprecated 4/10/18 CW - we want per calendar day
            //rain_96dataPoints = weatherdb.getAllRainForSpecificDay(fawnid, pastDay-1);
            //solar_96dataPoints = weatherdb.getAllSolarRadForSpecificDay(fawnid, pastDay-1);
            rain_96dataPoints = weatherdb.getRainForDay(fawnid, pastDay-1);
            solar_96dataPoints = weatherdb.getSolarRadForDay(fawnid, pastDay-1);
            double rain = 0;
            for (int j = 0; j < rain_96dataPoints.length; j++) rain+= rain_96dataPoints[j];
            System.out.println("DAY "+pastDay+"; RAIN TOTAL "+rain);
        } else {
            weatherSummary = weatherdb.getWeatherSummaryLast24(fawnid);
            // get all rain and solar radiation datapoints for last 24 hours (96
            // datapoints)
            rain_96dataPoints = weatherdb.getAllRainLast24(fawnid);
            solar_96dataPoints = weatherdb.getAllSolarRadLast24(fawnid);
        }
        _weatherUpdated = false;
    }

    /*
     * Start a new thread to run in the background and download new FAWN data.
     * Check every 5 minutes for new weather datapoints on FAWN
     */
    public void startDataDownloadThread() {
        Thread downloadThread = new Thread() {
            public void run() {
                // update lastDownloadTime on start
                lastDownloadTime = System.currentTimeMillis();
                while (true) {
                    // every 300 seconds and while not downloading
                    while (System.currentTimeMillis() - lastDownloadTime < 300000
                            || _downloading) {
                        // sleep 10s
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ie) {
                        }
                    }
                    _downloading = true;
                    readFawnData();
                    while (_downloading) {
                        // sleep 0.5s
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                        }
                    }
                    if (_weatherUpdated) {
                        // found new weather. Update weather and irrigation
                        // tables. Must be done on UI thread
                        CirrigMainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                updateWeatherTable();
                                updateIrrigTable();
                            }
                        });
                    }
                }
            }
        };
        downloadThread.start();
    }

    /* Actual irrigation calculations */
    public int calculateIrrigation(Zone zone, boolean overrideValue) {

        Calendar c = new GregorianCalendar();
        int doy = c.get(Calendar.DAY_OF_YEAR);

        double contDiam, irrig_in_hr, irrig_uniformity, elev, lat_deg, long_deg;
        double plantHt, plantWd, pctCover, contSpacing, spacingValue, shade, lf;
        double solar_wmhr, solar, tmax, tmin, rain;

        // get current fawn station for long, lat, elev
        FAWN.FAWNStation fawnstation = fawn.getFawnStation(fawnid);

        contDiam = zone.getContainerDiam();

        irrig_in_hr = zone.getIrrigRate();
        prevIrrigRate = irrig_in_hr;

        irrig_uniformity = zone.getIrrigUniformity();
        elev = fawnstation.getElevation();
        lat_deg = fawnstation.getLatitude();
        long_deg = fawnstation.getLongitude();

        System.out.println("ELEV "+elev+"; LAT "+lat_deg+"; LONG "+long_deg);
        if (!isInFL) {
            GPSTracker gps = new GPSTracker(this);
            if (gps.canGetLocation()) { // gps enabled} // return boolean true/false
                lat_deg = gps.getLatitude();
                long_deg = gps.getLongitude();
                elev = gps.getAltitude();
                System.out.println("ELEV "+elev+"; LAT "+lat_deg+"; LONG "+long_deg);
            } else {
                try {
                    long_deg = Double.parseDouble(longitudeView.getText().toString());
                    lat_deg = Double.parseDouble(latitudeView.getText().toString());
                    elev = Double.parseDouble(elevationView.getText().toString());
                } catch(NumberFormatException nfe) {
                    Toast.makeText(CirrigMainActivity.this, "Could not compute longitude, latitude, and elevation.  Please enable location!", Toast.LENGTH_SHORT).show();
                    return 0;
                }
                System.out.println("ELEV "+elev+"; LAT "+lat_deg+"; LONG "+long_deg);
            }
        }

        plantHt = zone.getPlantHeight();
        plantWd = zone.getPlantWidth();
        pctCover = zone.getPctCover();
        contSpacing = zone.getContSpacing();
        spacingValue = zone.getSpacingValue();
        // convert from percent here
        lf = zone.getLeachingFraction() / 100.0f;

        if (overrideValue) {
            solar_wmhr = zone.getSolar();
            solar = solar_wmhr * 0.0864;
            tmax = zone.getTmax();
            tmin = zone.getTmin();
            rain = zone.getRain();
        } else {
            solar_wmhr = weatherSummary[0];
            solar = solar_wmhr * 0.0864;
            tmax = weatherSummary[1];
            tmin = weatherSummary[2];
            rain = weatherSummary[3];
        }

        System.out.println("Zone "+zone.getID());
        System.out.println(solar_wmhr);
        System.out.println(tmax);
        System.out.println(tmin);
        System.out.println(rain);

        prevRain = rain;

        double cdr = 22.2 + 11.1 * Math.sin(0.0172 * (doy - 80));
        double apot = Math.pow((contDiam * 2.54) / 2, 2) * Math.PI;
        double atot = Math.pow((contDiam + contSpacing) * 2.54, 2) * spacingValue;
        double aratio = atot / apot;
        double gimpFGC = pctCover * 0.01;

        double lat = lat_deg * 0.01745;
        double sinlat = Math.sin(lat);
        double coslat = Math.cos(lat);

        double press = (101.0 - 0.0107 * elev) / 101.0;
        double escor = 1.0 - 0.016733 * Math.cos(0.0172 * (doy - 1.0));
        double om = 0.017202 * (doy - 3.244);

        double theta = om + 0.03344 * Math.sin(om)
                * (1.0 - 0.15 * Math.sin(om)) - 1.3526;
        double sindec = 0.3978 * Math.sin(theta);
        double cosdec = Math.sqrt(1.0 - Math.pow(sindec, 2));
        double sinf = sindec * sinlat;
        double cosf = cosdec * coslat;
        double hrang = Math.acos(-1 * sinf / cosf);
        double etr = 37.21 / Math.pow(escor, 2)
                * (hrang * sinf + cosf * Math.sin(hrang));
        double h2 = sinf + cosf * Math.cos(hrang / 2);
        double airmass = press
                * (-16.886 * Math.pow(h2, 3) + 36.137 * Math.pow(h2, 2)
                - 27.462 * h2 + 8.7412);

        double tarcd = 0.87 - 0.0025 * tmin;
        double cdr2 = etr * Math.pow(tarcd, airmass);
        double fcdr = Math.min(solar / cdr2, 1);
        double drf = Math.max(1.33 * (fcdr - 0.25), 0);
        double parb = Math.min(solar * 0.5, cdr2 * 0.5 * 0.7);
        double brad = solar * drf * Math.exp(-3.0 * Math.pow(gimpFGC, 0.9))
                * (1.0 - apot / atot);
        double tmaxb = tmax + 0.6 * brad;
        double btmean = tmaxb * 0.75 + tmin * 0.25;
        double lh = 25.01 - btmean / 42.3;

        double gamma = 0.0674 * press;
        double delta = 2503.0 * Math.exp(17.27 * btmean / (btmean + 237.3))
                / Math.pow(btmean + 237.3, 2);
        double radcom = delta / (delta + gamma) * solar * 0.6 / lh;
        double radcom_cdr = delta / (delta + gamma) * cdr * 0.6 / lh;
        double vpd = 0.6108 * Math.exp(17.27 * btmean / (btmean + 237.3))
                - 0.6108 * Math.exp(17.27 * tmin / (tmin + 237.3));

        double cropec = 1.0;
        double aerocomP = (cropec * Math.pow(vpd, 1.5)) / lh;
        double et0 = radcom + aerocomP;
        double et_cdr0 = radcom_cdr + aerocomP;
        double et = et0 * 0.9 * Math.pow(gimpFGC, 0.73);
        double etcdr = et_cdr0 * 0.9 * Math.pow(gimpFGC, 0.73);

        double contETcm = et * aratio + 0.2;
        double contETcdrcm = etcdr * aratio + 0.2;
        double contETin = contETcm / 2.54;
        double contETcdrin = contETcdrcm / 2.54;

        // use current selected capture ability
        //double cf1 = Zone.getIrrigCaptureValue(irrigCaptureAbility);
        double cf1 = Zone.getIrrigCaptureValue(zone.getIrrigCapture());

        double plantSize = (plantHt + plantWd) / 2;
        double cf = Math.min(Math.max(1, Math.min(1+(plantSize-contDiam)/contDiam*(cf1-1), 1.1*cf1)), 0.9*aratio+0.1);
        if (cf1 < 1 && cf1 != 0) {
            cf = Math.max(
                    cf1,
                    Math.min(1, 1 - (plantSize - contDiam) / contDiam
                            * (1 - cf1)));
        }
        double irrig_in = contETin / cf * 100.0 / irrig_uniformity;
        double irrig_time = irrig_in / irrig_in_hr * 60.0;

        System.out.println("IN1 "+irrig_in+"; "+irrig_time);

        if (overrideValue) {
            // apply rain override here
            double rainPerPot_in = rain * (1 + cf) / 2.;
            // cum in inches
            double cum = contETin - rainPerPot_in;
            if (cum < 0) cum = 0;
            irrig_in = (cum + lf * cum / (1.0 - lf)) / cf * 100.0
                    / irrig_uniformity;
            irrig_time = irrig_in / irrig_in_hr * 60.0;
            // RETURN here if override rain = true. Do not do hourly cumulative
            // deficit calculations!
            System.out.println("OR "+rainPerPot_in+" "+cum+" "+irrig_time);
            return (int) (irrig_time + 0.5);
        }

        // start with 0 cumulative deficit 11/13/13
        double cum = 0;

        // Add in rain
        double scaledET, rainPerPot;
        // Loop over hours to find cumulative deficit
        int nweather = rain_96dataPoints.length;
        for (int i = 0; i < nweather; i++) {
            scaledET = solar_96dataPoints[i] / (nweather * solar_wmhr) * contETin * 2.54;
            // Changed from (1+cf)/2.0 to cf 8/19/13
            rainPerPot = rain_96dataPoints[i] * 2.54 * cf;
            cum += scaledET - rainPerPot;
            if (cum < 0) cum = 0;
        }
        // Re-calculate irrig_in and irrig_time
        // Apply LF here too
        // irrig_in = cum/2.54/cf*100.0/irrig_uniformity;
        cum /= 2.54; // Convert to inches
        irrig_in = (cum + lf * cum / (1.0 - lf)) / cf * 100.0
                / irrig_uniformity;
        irrig_time = irrig_in / irrig_in_hr * 60.0;

        System.out.println("IN "+irrig_in+" "+irrig_time);

        // return rounded minutes
        return (int) (irrig_time + 0.5);
    }

    /* Formulas to calculate ETo */
    public double calculateEto(double[] weather) {
        double solar_wmhr = weather[0];
        double solar = solar_wmhr * 0.0864;
        double tmax = weather[1];
        double tmin = weather[2];
        double ws = weather[4];
        ws = ws * 0.447; // convert to m/s
        ws = ws * 4.87 / (Math.log(67.8 * 10 - 5.42)); // conversion to 2m
        double rhmax = weather[5];
        double rhmin = weather[6];
        // get current fawn station for long, lat, elev
        FAWN.FAWNStation fawnstation = fawn.getFawnStation(fawnid);
        double elevM = fawnstation.getElevation();
        Calendar c = new GregorianCalendar();
        int doy = c.get(Calendar.DAY_OF_YEAR);
        double albedoGrass = 0.34;

        double tmean = (tmax + tmin) / 2;
        double i = 4098 * (0.6108 * Math.exp(17.27 * tmean / (tmean + 273.3)))
                / Math.pow(tmean + 273.3, 2); // slope of saturation vapor
        // pressure curve
        double press = 101.3 * Math.pow((293 - 0.0065 * elevM) / 293, 5.26); // atm
        // pressure
        double y = 0.000665 * press; // psychrometric constant
        double dt = i / (i + y * (1 + 0.34 * ws)); // delta term
        double pt = y / (i + y * (1 + 0.34 * ws)); // psi term
        double tt = (900 / (tmean + 273)) * ws; // temperature term

        double etmax = 0.6108 * Math.exp(17.27 * tmax / (tmax + 237.3)); // saturation
        // vapor
        // pressure
        // at
        // Tmax
        double etmin = 0.6108 * Math.exp(17.27 * tmin / (tmin + 237.3)); // saturation
        // vapor
        // pressure
        // at
        // Tmin
        double es = (etmax + etmin) / 2; // mean saturation vapor pressure
        double ea = (etmin * rhmax / 100.0 + etmax * rhmin / 100.0) / 2.0; // actual vapor pressure
        double dr = 1 + 0.033 * Math.cos(2 * Math.PI * doy / 365); // earth sun
        // distance
        double dec = 0.409 * Math.sin(2 * Math.PI * doy / 365 - 1.39); // solar
        // declination
        double phi = Math.PI * fawnstation.getLatitude() / 180; // latitude in
        // radians
        double omega = Math.acos(-Math.tan(phi) * Math.tan(dec)); // sunset hour
        // angle
        double ra = 24
                * 60
                / Math.PI
                * 0.082
                * dr
                * ((omega * Math.sin(phi) * Math.sin(dec)) + (Math.cos(phi)
                * Math.cos(dec) * Math.sin(omega))); // extraterrestrial
        // radiation
        double rso = (0.75 + 0.00002 * elevM) * ra; // clear sky solar radiation
        double rns = (1 - albedoGrass) * solar; // net short wave radiation
        double rnl = 4.903e-9
                * (Math.pow(tmax + 273.16, 4) + Math.pow(tmin + 273.16, 4)) / 2
                * (0.34 - 0.14 * Math.sqrt(ea)) * (1.35 * solar / rso - 0.35); // net
        // long
        // wave
        // radiation
        if (rnl < 0) rnl = 0; // do not allow negative values
        double rn = rns - rnl; // net radiation;
        double rng = 0.408 * rn; // net radiation in mm evaporation
        double etrad = dt * rng; // radiation term
        double etwind = pt * tt * (es - ea); // wind term
        double eto = (etrad + etwind) * 0.1; // reference ET
        double eto_in = eto / 2.54; // convert to inches
        return eto_in;
    }

    public String[] formatWeather(double[] weather, double eto) {
        // convenience function for getting string array of rounded weather
        // values including eto
        String[] retVal = new String[5];
        retVal[0] = String.valueOf((int) (weather[0] + 0.5)); // solar radiation
        // - round to int
        retVal[1] = String.valueOf((int) (weather[1] * 1.8 + 32.5)); // max temp
        // convert to F and round to int
        retVal[2] = String.valueOf((int) (weather[2] * 1.8 + 32.5)); // min temp
        // convert to F and round to int
        retVal[3] = String.valueOf(weather[3] + 0.005); // rain - round to
        // hundredths. Already
        // in inches CW 4/16/15
        retVal[3] = retVal[3].substring(0, retVal[3].indexOf(".") + 3);
        retVal[4] = String.valueOf(eto + 0.005); // eto - round to hundredths
        retVal[4] = retVal[4].substring(0, retVal[4].indexOf(".") + 3);
        return retVal;
    }

    public void updateIrrigField() {
        if (override) {
            locTimeView.setText("Override, "+lastWeatherDate);
        } else {
            locTimeView.setText(fawn.getFawnStationName(fawnid) + ", "
                    + lastWeatherDate);
        }
        btnRunTime.setText(currentRuntime);
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
                Toast.makeText(CirrigMainActivity.this,
                        "Warning: Unable to obtain location.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String stationName = fawnstation.getName();
            String output = "Latitude: " + gps.getLatitude() + "\nLongitude: "
                    + gps.getLongitude() + "\nClosest Station: " + stationName;
            Toast.makeText(CirrigMainActivity.this, output, Toast.LENGTH_SHORT)
                    .show();
            tvFawnStation.setText(stationName);
            fawnid = fawnstation.getId();
            readFawnData();
        } else {
            Toast.makeText(CirrigMainActivity.this,
                    "Warning: Unable to obtain location.", Toast.LENGTH_SHORT)
                    .show();
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

        Zone zone1 = zonedb.getZone(1);
        zone1.setEto(unformatWeather("ETo", Float.parseFloat(etoOverride)));
        zone1.setSolar(unformatWeather("Solar", Float.parseFloat(solarOverride)));
        zone1.setTmax(unformatWeather("Tmax", Float.parseFloat(tmaxOverride)));
        zone1.setTmin(unformatWeather("Tmin", Float.parseFloat(tminOverride)));
        zone1.setRain(unformatWeather("Rain", Float.parseFloat(rainOverride)));
        zonedb.updateZone(zone1);

        currentRuntime ="";
        override = false;
        savePrefs();

    }

    /**
     * TextWatcher *
     */

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
        updateIrrigTable();
    }

//    public boolean ValidateOverrideInputValue() {
//        if(tvSolarOverride.getText().toString().equals("") || tvTmaxOverride.getText().toString().equals("") || tvTminOverride.getText().toString().equals("") || tvRainOverride.getText().toString().equals("")) {
//            return false;
//        }else {
//            return true;
//        }
//    }

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

    /**
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
     */

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

}
