package com.cwarner62.cirrig;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class CirrigSplashActivity extends CirrigActivity {
    boolean first = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        getPrefs();
        weatherdb = new WeatherDatabaseHandler(this);
        boolean success = readFawnData();
        //start thread to monitor when downloading FAWN data finishes
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    //display for at least 1/2 second
                } catch (InterruptedException e) {
                }
                while (_downloading) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                //start main activity
                first = false;
                startActivity(new Intent(CirrigSplashActivity.this, CirrigMainActivity.class));
                CirrigSplashActivity.this.finish();
            }
        };
        t.start();
    }

    @Override
    public void onDestroy() {
        weatherdb.close();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //allow user to touch screen to continue.  Use boolean first to make sure that
        //multiple copies of CirrigMainActivity are not started
        if (first) {
            startActivity(new Intent(CirrigSplashActivity.this, CirrigMainActivity.class));

            CirrigSplashActivity.this.finish();
            first = false;
        }
        return true;
    }
}
