package com.cwarner62.watertips;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import com.cwarner62.watertips.ui.dashboard.ArduinoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedHashMap;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends BlunoLibrary implements BluetoothLeInterface {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 457;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final int PERMISSION_REQUEST_BLUETOOTH_SCAN = 3;
    private static final int PERMISSION_REQUEST_BLUETOOTH_CONNECT = 4;
    private static final int PERMISSION_REQUEST_BLUETOOTH_ADVERTISE = 5;

    private ArduinoFragment arduinoFragment;

    ArduinoDatabaseHandler arduinodb;
    TipHistoryDatabaseHandler tipHistdb;
    LFHistoryDatabaseHandler lfHistdb;

    // prefs
    //global settings for unit type, irrig rate, target LF, runtime should be settings not database
    int default_unit_type;
    float default_irrig_rate, default_targetLF, default_runtime, default_contDiam, default_irrig_rate_gph;

    public final int MENU_PREFS = 0;
    public final int MENU_ABOUT = 1;

    public void savePrefs() {
        // Save user preferences. We need an Editor object to
        // make changes. All objects are from android.content.Context
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("default_unit_type", default_unit_type);
        editor.putFloat("default_irrig_rate", default_irrig_rate);
        editor.putFloat("default_targetLF", default_targetLF);
        editor.putFloat("default_runtime", default_runtime);
        editor.putFloat("default_contDiam", default_contDiam);
        editor.putFloat("default_irrig_rate_gph", default_irrig_rate_gph);
        // Don't forget to commit your edits!!!
        editor.commit();
    }

    public void getPrefs() {
        // Get user prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        default_unit_type = prefs.getInt("default_unit_type", 1);
        default_irrig_rate = prefs.getFloat("default_irrig_rate", 10.0f);
        default_targetLF = prefs.getFloat("default_targetLF", 25);
        default_runtime = prefs.getFloat("default_runtime", 12);
        default_contDiam = prefs.getFloat("default_contDiam", 10);
        default_irrig_rate_gph = prefs.getFloat("default_irrig_rate_gph", 3.0f);
    }

    //OVERRIDE Interface methods
    @Override
    public void doSerialSend(String theString) {
        System.out.println("SENDING> "+theString);
        //arduinoFragment.appendSerialText("SEND: "+theString);
        serialSend(theString);
    }

    @Override
    public void doButtonScanOnClickProcess() {
        buttonScanOnClickProcess();
    }

    @Override
    public void setFragment(Fragment theFragment) { arduinoFragment = (ArduinoFragment)theFragment; System.out.println("mService D"+ arduinoFragment);}

    @Override
    public int getDefaultUnitType() { return default_unit_type; }

    @Override
    public float getDefaultIrrigRate() { return default_irrig_rate; }

    @Override
    public float getDefaultTargetLF() { return default_targetLF; }

    @Override
    public float getDefaultRuntime() { return default_runtime; }

    @Override
    public float getDefaultContDiam() { return default_contDiam; }

    @Override
    public float getDefaultIrrigRateGPH() { return default_irrig_rate_gph; }

    @Override
    public void applySettings(int unitType, float irrigRate, float lf, float runtime, float contDiam, float irrigRateGPH) {
        default_unit_type = unitType;
        default_irrig_rate = irrigRate;
        default_targetLF = lf;
        default_runtime = runtime;
        default_contDiam = contDiam;
        default_irrig_rate_gph = irrigRateGPH;
        savePrefs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
                        this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                        this.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                 System.out.println("REQUESTING PERMISSIONS");

                 String[] permissions = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE,
                 Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH};
                 int permissionsCode = 42;
                 requestPermissions(permissions, permissionsCode);

             }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }


/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

           System.out.println("AAA");
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        PERMISSION_REQUEST_BLUETOOTH_SCAN);
            }

            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                System.out.println("CCC");
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        PERMISSION_REQUEST_BLUETOOTH_CONNECT);
            }

        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            System.out.println("FFF");
            int permissionsCode = 42;
            String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE};
           requestPermissions(permissions, permissionsCode);
            System.out.println("FFF2");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_BACKGROUND_LOCATION);
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_arduino, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        getPrefs();
        arduinodb = new ArduinoDatabaseHandler(this);
        tipHistdb = new TipHistoryDatabaseHandler(this);
        lfHistdb = new LFHistoryDatabaseHandler(this);
        onCreateProcess();														//onCreate Process by BlunoLibrary

        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200
    }

    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        if (arduinoFragment != null) arduinoFragment.resetState();
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        if (arduinoFragment != null) arduinoFragment.resetState();
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }
    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        if (arduinoFragment == null) {
            System.out.println("mService DASHBOARD NULLER");
            return;
        }
        arduinoFragment.setButtonState(false);
        arduinoFragment.resetDateAndButtons();
        updateDeviceNameList();
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                arduinoFragment.setButtonScanText("Connected");
                arduinoFragment.setUnitId(mDeviceAddress);
                arduinoFragment.verifyConnection();
                break;
            case isConnecting:
                arduinoFragment.setButtonScanText("Connecting");
                break;
            case isToScan:
                arduinoFragment.setButtonScanText("Scan");
                arduinoFragment.resetState();
                break;
            case isScanning:
                arduinoFragment.setButtonScanText("Scanning");
                break;
            case isDisconnecting:
                arduinoFragment.setButtonScanText("isDisconnecting");
                arduinoFragment.resetState();
                break;
            default:
                break;
        }
        if (theConnectionState != connectionStateEnum.isConnected) {
            arduinoFragment.setUnitId("Not Connected");
            arduinoFragment.resetState();
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        if (arduinoFragment != null) {
            arduinoFragment.onSerialReceived(theString);
        } else System.out.println("DB NULLER");
    }


    public static String roundOutput(String inputText, int decs) {
        if (inputText.indexOf(".") == -1) return inputText;
        if (inputText.indexOf("e") != -1) {
            int endIdx = inputText.indexOf(".")+decs+1;
            int eidx = inputText.indexOf("e");
            if (endIdx > eidx) endIdx = eidx;
            String outputText = inputText.substring(0, endIdx);
            outputText += inputText.substring(inputText.indexOf("e"));
            return outputText;
        }
        int endIdx = inputText.indexOf(".")+decs+1;
        if (decs == 0) endIdx = inputText.indexOf(".");
        if (endIdx > inputText.length()) endIdx = inputText.length();
        return inputText.substring(0, endIdx);
    }

    public void updateDeviceNameList() {
        if (arduinodb == null) {
            System.out.println("NULL ARDB1");
            return;
        }
        List<ArduinoUnit> unitList = arduinodb.getAllArduinoUnits();
        LinkedHashMap<String, String> unitMap = new LinkedHashMap<>();
        for (int i = 0; i < unitList.size(); i++) {
            ArduinoUnit currUnit = unitList.get(i);
            unitMap.put(currUnit.getMac(), currUnit.getName());
        }
        updateDeviceNameList(unitMap);
    }

}