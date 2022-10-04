package com.cwarner62.cirrig;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/* This dialog pops up when a numeric setting, e.g. container diameter, is clicked. */
public class NumericSettingsDialog extends Dialog implements OnClickListener {
    private Button applyButton, cancelButton;
    private EditText settingValueView;
    private TextView settingNameView;
    private SettingsListener listener;
    private String name;
    private float value;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.numeric_dialog);
        //set up views
        settingNameView = (TextView) findViewById(R.id.settingName);
        settingNameView.setText(name);
        applyButton = (Button) findViewById(R.id.applyButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        settingValueView = (EditText) findViewById(R.id.settingValue);
        settingValueView.setText(String.valueOf(value));
        settingValueView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        applyButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.applyButton:
                //callback to SettingsListener defined in CirrigMainActivity
                listener.onOkClick(settingValueView.getText().toString());
                dismiss();
                break;
            case R.id.cancelButton:
                cancel();
                break;
        }
    }

    /**
     * * @param context
     */
    public NumericSettingsDialog(Context context, SettingsListener listener, String name, float value) {
        super(context);
        setTitle("Edit Settings");
        this.listener = listener;
        this.name = name;
        this.value = value;
    }

}
