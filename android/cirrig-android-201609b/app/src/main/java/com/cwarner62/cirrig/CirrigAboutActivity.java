package com.cwarner62.cirrig;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

public class CirrigAboutActivity extends AppCompatActivity
        implements RadioGroup.OnCheckedChangeListener{
    private Button retButton;
    private RadioGroup navControl;
    private WebView webView;
    private ActionBar actionBar;
    private static final Map<Integer, String> URL_MAP = new HashMap<>();
    static {
        URL_MAP.put(R.id.info_about, "file:///android_asset/about.html");
        URL_MAP.put(R.id.info_concepts, "file:///android_asset/basicConcepts.html");
        URL_MAP.put(R.id.info_changelog, "file:///android_asset/change_log.html");
        URL_MAP.put(R.id.info_disclaimer, "file:///android_asset/disclaimer.html");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up layout
        setContentView(R.layout.activity_info);
        retButton = (Button) findViewById(R.id.info_ret);
        webView = (WebView) findViewById(R.id.info_web);

        navControl = (RadioGroup) findViewById(R.id.info_buttons);
        navControl.setOnCheckedChangeListener(this);

        //use webview to load html file as URL
        navControl.check(R.id.info_about);
        retButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setIcon(R.drawable.ab_icon);
        actionBar.setTitle("About");

        View ifasLogo = LayoutInflater.from(this).inflate(R.layout.ifas_logo, null);

//        ImageView ifasLogoView = new ImageView(this);
//        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
//        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
//        ifasLogoView.setLayoutParams(params);
//        Drawable ifasLogo = getResources().getDrawable(R.drawable.uf_ifas);
//        ifasLogoView.setImageDrawable(ifasLogo);
        actionBar.setCustomView(ifasLogo);
        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        webView.loadUrl(URL_MAP.get(checkedId));
    }
}
