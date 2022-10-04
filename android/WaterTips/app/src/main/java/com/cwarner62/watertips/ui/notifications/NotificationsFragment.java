package com.cwarner62.watertips.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cwarner62.watertips.ArduinoUnit;
import com.cwarner62.watertips.BluetoothLeInterface;
import com.cwarner62.watertips.R;

public class NotificationsFragment extends Fragment {


    private NotificationsViewModel notificationsViewModel;

    private EditText settingsIrrigRate, settingsLFTarget, settingsRuntime, settingsContDiam;
    private TextView labelIrrigRate, labelContDiam;
    private Spinner settingsUnitType;
    private float irrigRate, irrigRateGPH;
    private int currType;

    private BluetoothLeInterface mainActivityListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        //addPreferencesFromResource(R.layout.settings_screen);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        /*
        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */
        labelIrrigRate = (TextView) root.findViewById(R.id.settingsIrrigRateLabel);
        labelContDiam = (TextView) root.findViewById(R.id.settingsContDiamLabel);

        settingsUnitType=(Spinner) root.findViewById(R.id.settingsUnitType);
        settingsIrrigRate=(EditText) root.findViewById(R.id.settingsIrrigRate);
        settingsLFTarget=(EditText) root.findViewById(R.id.settingsTargetLF);
        settingsRuntime=(EditText) root.findViewById(R.id.settingsRuntime);
        settingsContDiam=(EditText) root.findViewById(R.id.settingsContDiam);

        readSettings();

        settingsUnitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                changeMode(settingsUnitType.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        Button saveButton = (Button) root.findViewById(R.id.buttonSettingsSave);
        Button cancelButton = (Button) root.findViewById(R.id.buttonSettingsCancel);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currType = settingsUnitType.getSelectedItemPosition();
                if (currType == ArduinoUnit.MODE_SPRINKLER) {
                    irrigRate = Float.parseFloat(settingsIrrigRate.getText().toString());
                } else if (currType == ArduinoUnit.MODE_MICRO) {
                    irrigRateGPH = Float.parseFloat(settingsIrrigRate.getText().toString());
                }
                float lf = Float.parseFloat(settingsLFTarget.getText().toString());
                float runtime = Float.parseFloat(settingsRuntime.getText().toString());
                float contDiam = Float.parseFloat(settingsContDiam.getText().toString());
                mainActivityListener.applySettings(currType, irrigRate, lf, runtime, contDiam, irrigRateGPH);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSettings();
            }
        });
        return root;
    }

    public void changeMode(int mode) {
        if (mode == ArduinoUnit.MODE_SPRINKLER) {
            labelIrrigRate.setText("Irrig Rate (in/hr):");
            settingsIrrigRate.setText(String.valueOf(irrigRate));
            labelContDiam.setVisibility(View.VISIBLE);
            settingsContDiam.setVisibility(View.VISIBLE);
        } else if (mode == ArduinoUnit.MODE_MICRO) {
            labelIrrigRate.setText("Irrig Rate (gph):");
            settingsIrrigRate.setText(String.valueOf(irrigRateGPH));
            labelContDiam.setVisibility(View.GONE);
            settingsContDiam.setVisibility(View.GONE);
        }
    }

    public void readSettings() {
        irrigRate = mainActivityListener.getDefaultIrrigRate();
        irrigRateGPH = mainActivityListener.getDefaultIrrigRateGPH();
        currType = mainActivityListener.getDefaultUnitType();
        settingsUnitType.setSelection(currType);
        changeMode(currType);
        settingsLFTarget.setText(String.valueOf(mainActivityListener.getDefaultTargetLF()));
        settingsRuntime.setText(String.valueOf(mainActivityListener.getDefaultRuntime()));
        settingsContDiam.setText(String.valueOf(mainActivityListener.getDefaultContDiam()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mainActivityListener = (BluetoothLeInterface) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

}