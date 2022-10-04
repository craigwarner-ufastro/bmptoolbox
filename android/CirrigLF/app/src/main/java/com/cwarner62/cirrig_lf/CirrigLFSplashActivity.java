package com.cwarner62.cirrig_lf;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class CirrigLFSplashActivity extends CirrigLFActivity {
    boolean first = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        getPrefs();
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    //display for 2 seconds
                } catch (InterruptedException e) {
                }
                //start main activity
                first = false;
                startActivity(new Intent(CirrigLFSplashActivity.this, CirrigLFMainActivity.class));
                CirrigLFSplashActivity.this.finish();
            }
        };
        t.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //allow user to touch screen to continue.  Use boolean first to make sure that
        //multiple copies of CirrigLFMainActivity are not started
        if (first) {
            startActivity(new Intent(CirrigLFSplashActivity.this, CirrigLFMainActivity.class));

            CirrigLFSplashActivity.this.finish();
            first = false;
        }
        return true;
    }
}
