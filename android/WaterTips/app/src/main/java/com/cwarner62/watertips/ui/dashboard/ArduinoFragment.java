package com.cwarner62.watertips.ui.dashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cwarner62.watertips.ArduinoDatabaseHandler;
import com.cwarner62.watertips.ArduinoUnit;
import com.cwarner62.watertips.BluetoothLeInterface;
import com.cwarner62.watertips.R;
import com.cwarner62.watertips.TipHistory;
import com.cwarner62.watertips.TipHistoryDatabaseHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/* IrrigHistorySendThread - sends a GET command, waits until timeout or global received count has
incremented.  onSerialReceived - match HIST date with last sent date.
Then check hasHistory before addHistory.
 */

public class ArduinoFragment extends Fragment {

    public static final int DOWNLOAD_HISTORY = 1;
    public static final int ADD_UNIT = 2;
    public static final int DISCONNECTED = 3;

    private ArduinoViewModel arduinoViewModel;

    private Button buttonScan, buttonDownloadHist;
    private Button refreshButton, dateButton;
    private TextView arduinoMAC, tipper1Text, tipper2Text, tipper3Text, tipper4Text;
    private TextView dateLabel, dateField;
    private EditText arduinoNameInput, arduinoDescInput, arduinoIrrigRate;
    private EditText arduinoLFTarget, arduinoRuntime, arduinoContDiam;
    private TextView arduinoContDiamLabel;
    private Spinner arduinoUnitType;
    protected StringBuffer sbuffer = new StringBuffer();

    SimpleDateFormat arduinoDF = new SimpleDateFormat("MMdd");
    SimpleDateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat arduinoTimeDF = new SimpleDateFormat("HH:mm:ss");

    private int _mode = DISCONNECTED;
    private boolean _isConnected = false;

    private ArduinoUnit currentUnit;
    ArduinoDatabaseHandler arduinodb;
    TipHistoryDatabaseHandler tipHistdb;

    protected String lastSentDate = null;
    private long _timeout = 15000;
    protected int _receivedCount = 0, _connectCount = 0;
    protected boolean _abortHistTransfer;
    ProgressDialog mProgressBar;

    private BluetoothLeInterface mainActivityListener;

    public TextView serialText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        arduinoViewModel =
                new ViewModelProvider(this).get(ArduinoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_arduino, container, false);

        arduinodb = new ArduinoDatabaseHandler(getActivity());
        tipHistdb = new TipHistoryDatabaseHandler(getActivity());
        mProgressBar = new ProgressDialog(getActivity());

        arduinoMAC = root.findViewById(R.id.arduinoMAC);
        //final TextView arduinoMAC = root.findViewById(R.id.arduinoMAC);
        /*

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                arduinoMAC.setText(s);
            }
        });

         */

        dateLabel = (TextView) root.findViewById(R.id.arduinoDateLabel);
        dateField = (TextView) root.findViewById(R.id.arduinoDateField);

        tipper1Text=(TextView) root.findViewById(R.id.tipper1CountsLabel);	//initial the EditText of the received data
        tipper2Text=(TextView) root.findViewById(R.id.tipper2CountsLabel);
        tipper3Text=(TextView) root.findViewById(R.id.tipper3CountsLabel);
        tipper4Text=(TextView) root.findViewById(R.id.tipper4CountsLabel);

        arduinoNameInput=(EditText) root.findViewById(R.id.arduinoNameInput);
        arduinoDescInput=(EditText) root.findViewById(R.id.arduinoDescInput);
        arduinoUnitType=(Spinner) root.findViewById(R.id.arduinoUnitType);
        arduinoIrrigRate=(EditText) root.findViewById(R.id.arduinoIrrigRate);
        arduinoLFTarget=(EditText) root.findViewById(R.id.arduinoTargetLF);
        arduinoRuntime=(EditText) root.findViewById(R.id.arduinoRuntime);
        arduinoContDiam=(EditText) root.findViewById(R.id.arduinoContDiam);
        arduinoContDiamLabel=(TextView)root.findViewById(R.id.arduinoContDiamLabel);

        buttonDownloadHist = (Button) root.findViewById(R.id.buttonDownloadHist);		//initial the button for sending the data

        serialText = (TextView) root.findViewById(R.id.serialText);

        arduinoUnitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (arduinoUnitType.getSelectedItemPosition() == ArduinoUnit.MODE_MICRO) {
                    arduinoContDiam.setVisibility(View.GONE);
                    arduinoContDiamLabel.setVisibility(View.GONE);
                } else {
                    arduinoContDiam.setVisibility(View.VISIBLE);
                    arduinoContDiamLabel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        buttonDownloadHist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //mainActivityListener.doSerialSend("G::0630");				//send the data to the BLUNO
                if (_mode == ADD_UNIT) {
                    String macAddress = arduinoMAC.getText().toString();
                    String arduinoName = arduinoNameInput.getText().toString();
                    String arduinoDesc = arduinoDescInput.getText().toString();
                    currentUnit = new ArduinoUnit(macAddress, arduinoName, arduinoDesc);
                    currentUnit.setType(arduinoUnitType.getSelectedItemPosition());
                    float irrigRate = Float.parseFloat(arduinoIrrigRate.getText().toString());
                    currentUnit.setIrrigRate(irrigRate);
                    float lfTarget = Float.parseFloat(arduinoLFTarget.getText().toString());
                    currentUnit.setTargetLF(lfTarget);
                    float runtime = Float.parseFloat(arduinoRuntime.getText().toString());
                    currentUnit.setRuntime(runtime);
                    if (currentUnit.getType() == ArduinoUnit.MODE_SPRINKLER) {
                        currentUnit.setContainerDiam(Float.parseFloat(arduinoContDiam.getText().toString()));
                    }
                    arduinodb.addArduinoUnit(currentUnit);
                    setMode(DOWNLOAD_HISTORY);
                } else if (_mode == DOWNLOAD_HISTORY) {
                    //mainActivityListener.doSerialSend("G::0630");
                    IrrigHistorySendThread sendThread = new IrrigHistorySendThread(currentUnit);
                    mProgressBar.setCancelable(false);
                    mProgressBar.setMessage("Downloading History..");
                    mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressBar.show();
                    sendThread.start();
                }
            }
        });

        buttonScan = (Button) root.findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mainActivityListener.doButtonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
            }
        });

        refreshButton = (Button) root.findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send get current tips request again
                mainActivityListener.doSerialSend("G::C");
                refreshButton.setEnabled(false);
            }
        });

        dateButton = (Button) root.findViewById(R.id.buttonUpdateDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send datetime formatted as D::S2022-01-31T15:35:00
                java.util.Date d = new Date();
                String command = "D::S"+sqlDF.format(d)+"T"+arduinoTimeDF.format(d);
                System.out.println("Command "+command);
                mainActivityListener.doSerialSend(command);
                dateButton.setEnabled(false);
            }
        });

        mainActivityListener.setFragment(this);

        return root;
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

    public void setButtonScanText(String text) {
        buttonScan.setText(text);
    }

    public void setButtonState(boolean state) {
        buttonDownloadHist.setEnabled(state);
    }

    public void resetDateAndButtons() {
        refreshButton.setEnabled(false);
        refreshButton.setVisibility(View.GONE);
        dateButton.setEnabled(false);
        dateButton.setVisibility(View.GONE);
        dateLabel.setVisibility(View.GONE);
        dateField.setText("");
        dateField.setVisibility(View.GONE);
    }

    public void setDateAndButtons() {
        refreshButton.setEnabled(true);
        refreshButton.setVisibility(View.VISIBLE);
        dateButton.setEnabled(true);
        dateButton.setVisibility(View.VISIBLE);
        dateLabel.setVisibility(View.VISIBLE);
        dateField.setVisibility(View.VISIBLE);
    }

    public void setMode(int mode) {
        _mode = mode;
        if (_mode == DOWNLOAD_HISTORY) {
            buttonDownloadHist.setText("Download History");
            setButtonState(true);
        } else if (_mode == ADD_UNIT) {
            buttonDownloadHist.setText("Add Unit");
            setButtonState(true);
        } else {
            //DISCONNECTED
            buttonDownloadHist.setText("Download History");
            setButtonState(false);
            resetDateAndButtons();
        }
    }

    public void setConnected(boolean isConnected) {
        _isConnected = isConnected;
    }

    public void setUnitId(String mac) {
        arduinoMAC.setText(mac);
        boolean hasUnit = arduinodb.hasArduinoWithMAC(mac);
        if (hasUnit) {
            setMode(DOWNLOAD_HISTORY);
            currentUnit = arduinodb.getArduinoUnitByMAC(mac);
            arduinoNameInput.setText(currentUnit.getName());
            arduinoDescInput.setText(currentUnit.getDesc());
            arduinoUnitType.setSelection(currentUnit.getType());
            arduinoIrrigRate.setText(String.valueOf(currentUnit.getIrrigRate()));
            arduinoLFTarget.setText(String.valueOf(currentUnit.getTargetLF()));
            arduinoRuntime.setText(String.valueOf(currentUnit.getRuntime()));
            if (currentUnit.getType() == ArduinoUnit.MODE_SPRINKLER) {
                arduinoContDiam.setText(String.valueOf(currentUnit.getContainerDiam()));
            }
            setComponentsEditable(false);
        } else {
            setMode(ADD_UNIT);
            //**** Set Edit Text values to defaults in main activity from settings ***/
            arduinoUnitType.setSelection(mainActivityListener.getDefaultUnitType());
            arduinoIrrigRate.setText(String.valueOf(mainActivityListener.getDefaultIrrigRate()));
            arduinoLFTarget.setText(String.valueOf(mainActivityListener.getDefaultTargetLF()));
            arduinoRuntime.setText(String.valueOf(mainActivityListener.getDefaultRuntime()));
            arduinoContDiam.setText(String.valueOf(mainActivityListener.getDefaultContDiam()));
            setComponentsEditable(true);
        }
        setDateAndButtons();
    }

    public void resetState() {
        setMode(DISCONNECTED);
        arduinoUnitType.setSelection(mainActivityListener.getDefaultUnitType());
        arduinoIrrigRate.setText(String.valueOf(mainActivityListener.getDefaultIrrigRate()));
        arduinoLFTarget.setText(String.valueOf(mainActivityListener.getDefaultTargetLF()));
        arduinoRuntime.setText(String.valueOf(mainActivityListener.getDefaultRuntime()));
        arduinoContDiam.setText(String.valueOf(mainActivityListener.getDefaultContDiam()));
        setComponentsEditable(true);
    }

    public void setComponentsEditable(boolean isEditable) {
        arduinoNameInput.setEnabled(isEditable);
        arduinoDescInput.setEnabled(isEditable);
        arduinoIrrigRate.setEnabled(isEditable);
        arduinoUnitType.setEnabled(isEditable);
        arduinoLFTarget.setEnabled(isEditable);
        arduinoRuntime.setEnabled(isEditable);
        arduinoContDiam.setEnabled(isEditable);
    }

    public void appendSerialText(String theString) {
        String st = ""+serialText.getText().toString();
        st += theString+"\n";
        //serialText.setText(st);
        System.out.println("SERIAL TEXT "+st);
    }

    public void verifyConnection() {
        VerifyThread vThread = new VerifyThread();
        mProgressBar.setCancelable(false);
        mProgressBar.setMessage("Connecting..");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.show();
        vThread.start();
    }

    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        if (theString.indexOf("=") > 0) {
            System.out.println("Received "+theString);
            return;
        }
        sbuffer.append(theString);
        String bufString = sbuffer.toString().trim();
        System.out.println("RECV: "+bufString);
        appendSerialText("RECV: "+bufString);
        if (bufString.endsWith("::E")) {
            System.out.println("waterTips:onReceived> " + bufString);
            String[] responses = bufString.split("::");
            System.out.println("WT "+responses[0]+" "+responses.length);
            if (responses[0].equals("T") && responses.length == 6) {
                //T::t0::t1::t2::t3::E
                tipper1Text.setText("Tip 1: " + responses[1]);
                tipper2Text.setText("Tip 2: " + responses[2]);
                tipper3Text.setText("Tip 3: " + responses[3]);
                tipper4Text.setText("Tip 4: " + responses[4]);
                sbuffer.setLength(0);
                //after receiving counts, send a D::C to get date
                mainActivityListener.doSerialSend("D::C");
                refreshButton.setEnabled(true);
            } else if (responses[0].equals("H") && responses.length == 8) {
                TipHistory th = new TipHistory(bufString);
                if (th.valid()) {
                    th.setAID(currentUnit.getID());
                    System.out.println("HISTORY: "+th.toString());
                    if (lastSentDate.equals(th.getDate())) {
                        //_receivedCount++;
                        System.out.println("RECEIVE SUCCESSFUL");
                        if (!tipHistdb.hasTipHistory(currentUnit.getID(), th.getDate())) {
                            tipHistdb.addTipHistory(th);
                        }
                    }
                } else {
                    System.out.println("INVALID");
                    _abortHistTransfer = true;
                }
                _receivedCount++;
                sbuffer.setLength(0);
            }
        } else if (bufString.endsWith("::OK")) {
            System.out.println("Everything is A-OK");
            _isConnected = true;
            _connectCount++;
            sbuffer.setLength(0);
        } else if (bufString.endsWith("::D")) {
            //date
            String[] responses = bufString.split("::");
            dateField.setText(responses[0]);
            sbuffer.setLength(0);
            dateButton.setEnabled(true);
        } else {
            System.out.println("GARBAGE");
            _connectCount++;
        }
    }

    protected class VerifyThread extends Thread {

        public VerifyThread() {}

        public void run() {
            _isConnected = false;
            int startCount = _connectCount;
            mainActivityListener.doSerialSend("C::C");
            int n = 0;
            while (startCount == _connectCount && n < 30) {
                n++;
                try { Thread.sleep(250); } catch(InterruptedException ie) {}
                System.out.println("N "+n);
            }
            if (!_isConnected) {
                //Try one more time
                mainActivityListener.doSerialSend("C::C");
                n = 0;
                while (startCount == _connectCount && n < 30) {
                    n++;
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ie) {
                    }
                    System.out.println("N " + n);
                }
            }
            if (_isConnected) {
                mainActivityListener.doSerialSend("G::C");
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setButtonScanText("Failed");
                        resetState();
                    }
                });
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mProgressBar.hide();
                }
            });
        }
    }

    protected class IrrigHistorySendThread extends Thread {
        protected ArduinoUnit currentUnit;
        protected int lastCount = 0;
        protected long lastTimeSent = 0L;
        public IrrigHistorySendThread(ArduinoUnit currentUnit) {
            this.currentUnit = currentUnit;
        }

        public void run() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            for (int j = 0; j < 30; j++) {
                calendar.add(Calendar.DATE, -1);
                if (!tipHistdb.hasTipHistory(currentUnit.getID(), sqlDF.format(calendar.getTime()))) {
                    String histCommand = "G::"+arduinoDF.format(calendar.getTime());
                    lastCount = _receivedCount;
                    lastSentDate = sqlDF.format(calendar.getTime());
                    lastTimeSent = System.currentTimeMillis();
                    mainActivityListener.doSerialSend(histCommand);
                    while (lastCount == _receivedCount && System.currentTimeMillis()-lastTimeSent < _timeout) {
                        try{
                            Thread.sleep(250);
                        } catch(InterruptedException ie) {}
                    }
                    if (lastCount == _receivedCount) {
                        System.out.println("ERROR: did not receive correct response");
                        break;
                    }
                    if (_abortHistTransfer) {
                        System.out.println("Warning: No more history.  Aborting further transfer.");
                        _abortHistTransfer = false;
                        break;
                    }
                } else {
                    System.out.println("Entry exists for this date.  Exiting.");
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mProgressBar.hide();
                }
            });
        }
    }
}