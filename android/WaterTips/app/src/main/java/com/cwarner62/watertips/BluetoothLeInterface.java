package com.cwarner62.watertips;

import androidx.fragment.app.Fragment;

public interface BluetoothLeInterface {
    void doSerialSend(String theString);
    void doButtonScanOnClickProcess();
    void setFragment(Fragment theFragment);
    int getDefaultUnitType();
    float getDefaultIrrigRate();
    float getDefaultTargetLF();
    float getDefaultRuntime();
    float getDefaultContDiam();
    float getDefaultIrrigRateGPH();
    void applySettings(int unitType, float irrigRate, float lf, float runtime, float contDiam, float irrigRateGPH);
}
