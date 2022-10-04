package com.cwarner62.watertips.ui.home;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cwarner62.watertips.ArduinoDatabaseHandler;
import com.cwarner62.watertips.ArduinoUnit;
import com.cwarner62.watertips.LFHistory;
import com.cwarner62.watertips.LFHistoryDatabaseHandler;
import com.cwarner62.watertips.R;
import com.cwarner62.watertips.TipHistory;
import com.cwarner62.watertips.TipHistoryDatabaseHandler;
import com.cwarner62.watertips.MainActivity;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    ArduinoDatabaseHandler arduinodb;
    TipHistoryDatabaseHandler tipHistDb;
    LFHistoryDatabaseHandler lfHistDb;
    protected List<ArduinoUnit> arduinoUnitList;
    TableLayout arduinoTableLayout, tipsTableLayout, lfTableLayout;
    ScrollView scrollViewArduinoTable, scrollViewArduinoView, scrollViewTipHistory, scrollViewLFHistory, scrollViewEditAdruino;
    RelativeLayout relativeLayoutArduinoTable, relativeLayoutArduinoView, relativeLayoutTipHistory, relativeLayoutLFHistory, relativeLayoutEditArduino;
    ConstraintLayout constraintLayoutArduinoTable, constraintLayoutTipHistory, constraintLayoutLFHistory;
    ActionBar actionBar;
    ViewFlipper flipper; // flip between Views
    View root;
    int currentUnitId = 0; //actual AID number
    int currentUnitIdx = 0; //index / row
    private int _currentScreen = 0;
    private int _sortby = 1;
    TipHistory tipHist;
    AlertDialog manualTipDialog;

    float tipAvg, lfpct, rtt, rt;

    int verySmallTextSize;
    int smallTextSize;
    int mediumTextSize;
    int largeTextSize;

    private boolean dtChanged = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        //final TextView textView = root.findViewById(R.id.text_home);
        /*
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
       */
        /* Get all arduinos from database
        Add header and arduino columns for id, name, mac?, desc?, [View], [History]
         */
/*
        actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Water Tips A");
*/
        flipper = (ViewFlipper) root.findViewById(R.id.profileFlipper);

        arduinoTableLayout = (TableLayout) root.findViewById(R.id.tableArduinos);
        tipsTableLayout = (TableLayout) root.findViewById(R.id.tableTipsHistory);
        lfTableLayout = (TableLayout) root.findViewById(R.id.tableLFHistory);
        arduinodb = new ArduinoDatabaseHandler(getActivity());
        tipHistDb = new TipHistoryDatabaseHandler(getActivity());
        lfHistDb = new LFHistoryDatabaseHandler(getActivity());

        scrollViewArduinoTable = (ScrollView) root.findViewById(R.id.ScrollView_ArduinoTable);
        scrollViewArduinoView = (ScrollView) root.findViewById(R.id.ScrollView_ArduinoView);
        scrollViewTipHistory = (ScrollView) root.findViewById(R.id.ScrollView_TipHistory);
        scrollViewLFHistory = (ScrollView) root.findViewById(R.id.ScrollView_LFHistory);
        scrollViewEditAdruino = (ScrollView) root.findViewById(R.id.ScrollView_EditArduino);

        relativeLayoutArduinoTable = (RelativeLayout) root.findViewById(R.id.RelativeLayout_ArduinoTable);
        relativeLayoutArduinoView = (RelativeLayout) root.findViewById(R.id.RelativeLayout_ArduinoView);
        relativeLayoutTipHistory = (RelativeLayout) root.findViewById(R.id.RelativeLayout_TipHistory);
        relativeLayoutLFHistory = (RelativeLayout) root.findViewById(R.id.RelativeLayout_LFHistory);
        relativeLayoutEditArduino = (RelativeLayout) root.findViewById(R.id.RelativeLayout_EditArduino);

        constraintLayoutArduinoTable = (ConstraintLayout)root.findViewById(R.id.ConstraintLayoutArduinoTable);
        constraintLayoutTipHistory = (ConstraintLayout)root.findViewById(R.id.ConstraintLayoutTipHistory);
        constraintLayoutLFHistory = (ConstraintLayout)root.findViewById(R.id.ConstraintLayoutLFHistory);

        verySmallTextSize = (int) getResources().getDimension(R.dimen.font_size_verysmall);
        smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);
        mediumTextSize = (int) getResources().getDimension(R.dimen.font_size_medium);
        largeTextSize = (int) getResources().getDimension(R.dimen.font_size_large);


        //simDatabase();
        arduinoUnitList = arduinodb.getAllArduinoUnits();
        setupGestureDetection();
        setFlipperView(0);

        return root;
    }

    public int getRandomInt(int min, int max) {
        return min + (int)(Math.random()*(max+1-min));
    }

    public void simDatabase() {
        int count = arduinodb.getArduinoUnitsCount();
        /*
        if (count < 2) {
            //sim some arduinos and histories
            Vector<ArduinoUnit> simUnits = new Vector();
            simUnits.add(new ArduinoUnit("AA:BB:CC:DD:EE:FF", "Test1", "Test Unit 1"));
            simUnits.add(new ArduinoUnit("ZZ:YY:XX:WW:VV:UU", "Test2", "Test Unit 2"));
            simUnits.add(new ArduinoUnit("11:22:33:44:55:66", "Test3", "Test Unit 3"));

            for (int i = 0; i < simUnits.size(); i++) {
                simUnits.get(i).setIrrigRate(getRandomInt(1, 4));
                simUnits.get(i).setRuntime(getRandomInt(5, 20));
                simUnits.get(i).setContainerDiam(getRandomInt(5, 20));
                simUnits.get(i).setTargetLF(getRandomInt(5, 25));
            }
            arduinodb.addArduinoUnits(simUnits);

            SimpleDateFormat simdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < simUnits.size(); i++) {
                Calendar c2 = new GregorianCalendar();
                ArduinoUnit theUnit = arduinodb.getArduinoUnitByMAC(simUnits.get(i).getMac());
                for (int j = 0; j < 3; j++) {
                    c2.add(Calendar.DATE, -1);
                    String theDate = simdf.format(c2.getTime());
                    tipHistDb.addTipHistory(new TipHistory(theUnit.getID(), theDate, getRandomInt(0,20), getRandomInt(0, 20), getRandomInt(0, 20), getRandomInt(0, 20)));
                }
            }
        }*/
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupGestureDetection() {
        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        System.out.println("DOwN1");
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        System.out.println("FLING1 "+velocityX+" "+velocityY);
                        if (e1 == null) {
                            System.out.println("E1 NULLER");
                            return false;
                        } else {
                            System.out.println("E1 "+e1.toString());
                        }
                        if (e2 == null) {
                            System.out.println("E2 NULLER");
                            return false;
                        } else {
                            System.out.println("E2 "+e2.toString());
                        }
                        int SWIPE_THRESHOLD = 100;
                        int SWIPE_VELOCITY_THRESHOLD = 100;
                        boolean result = false;
                        try {
                            float diffY = e2.getY() - e1.getY();
                            float diffX = e2.getX() - e1.getX();
                            if (Math.abs(diffX) > Math.abs(diffY)) {
                                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                    if (diffX > 0) {
                                        onSwipeRight();
                                    } else {
                                        onSwipeLeft();
                                    }
                                    result = true;
                                }
                            }
                            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                                if (diffY > 0) {
                                    onSwipeBottom();
                                } else {
                                    onSwipeTop();
                                }
                                result = true;
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        return result;
                        /*
                        System.out.println("onFling has been called!");
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                System.out.println("Right to Left");
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                System.out.println("Left to Right");
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                        */
                    }
                });

        final View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("TOUCH");
                System.out.println(v.toString());
                gesture.onTouchEvent(event);
                return false;
            }
        };

        final View.OnTouchListener constraintTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("TOUCHC");
                System.out.println(v.toString());
                gesture.onTouchEvent(event);
                return true;
            }
        };

        constraintLayoutArduinoTable.setOnTouchListener(constraintTouchListener);
        scrollViewArduinoTable.setOnTouchListener(touchListener);
        scrollViewArduinoView.setOnTouchListener(touchListener);
        constraintLayoutTipHistory.setOnTouchListener(constraintTouchListener);
        scrollViewTipHistory.setOnTouchListener(touchListener);
        constraintLayoutLFHistory.setOnTouchListener(constraintTouchListener);
        scrollViewLFHistory.setOnTouchListener(touchListener);
        scrollViewEditAdruino.setOnTouchListener(touchListener);
    }
    
    public void setFlipperView(int id) {
        if (currentUnitIdx >= 0 && currentUnitIdx < arduinoUnitList.size()) {
            currentUnitId = arduinoUnitList.get(currentUnitIdx).getID();
        }
        System.out.println("CURRENT UNIT IDX "+currentUnitIdx+"; "+currentUnitId);
        if (id == 0) {
            updateArduinoTable();
        } else if (id == 1) {
            updateArduinoUnit();
        } else if (id == 2) {
            updateTipHistoryTable();
        } else if (id == 3) {
            updateLFHistoryTable();
        } else if (id == 4) {
            updateEditArduinoScreen();
        }
        _currentScreen = id;
        flipper.setDisplayedChild(id);
    }

    public void updateArduinoTable() {
        // setup the table
        //arduinoTableLayout = (TableLayout) root.findViewById(R.id.tableArduinos);
        arduinoTableLayout.setStretchAllColumns(true);

        int leftRowMargin = 0;
        int topRowMargin = 0;
        int rightRowMargin = 0;
        int bottomRowMargin = 0;

        int buttonTextSize = (int) getResources().getDimension(R.dimen.font_size_18sp);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (_sortby == 0) {
            arduinoUnitList = arduinodb.getAllArduinoUnits();
        } else {
            arduinoUnitList = arduinodb.getAllArduinoUnitsByName();
        }

        int rows = arduinoUnitList.size();
        TextView textSpacer = null;

        //arduinoTableLayout.removeAllViews();
        arduinoTableLayout.removeViews(1, arduinoTableLayout.getChildCount()-1);

        Spinner sortBySpinner = (Spinner) root.findViewById(R.id.sortSpinner);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                _sortby = 1-position;
                setFlipperView(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        // -1 means heading row
        for (int i = 0; i < rows; i++) {
            ArduinoUnit currUnit = null;
            currUnit = arduinoUnitList.get(i);
            final int currId = currUnit.getID();
            final int myI = i;
            if (currentUnitId == 0) currentUnitId = currId;
            // data columns
            final TextView tvId = new TextView(getContext());
            tvId.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT));

            tvId.setGravity(Gravity.LEFT);

            tvId.setPadding(5, 15, 0, 15);
            tvId.setBackgroundColor(getResources().getColor(R.color.cell_bg_odd));
            tvId.setText(String.valueOf(currUnit.getID()));
            tvId.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);

            final TextView tvName = new TextView(getContext());
            tvName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            tvName.setGravity(Gravity.LEFT);
            tvName.setPadding(5, 15, 0, 15);
            tvName.setBackgroundColor(getResources().getColor(R.color.cell_bg_even));
            tvName.setTextColor(Color.BLACK);
            tvName.setText(currUnit.getName());

            final LinearLayout descLayout = new LinearLayout(getContext());
            descLayout.setOrientation(LinearLayout.VERTICAL);
            descLayout.setPadding(0, 10, 0, 10);
            descLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
            descLayout.setBackgroundColor(getResources().getColor(R.color.cell_bg_odd));

            final TextView tvMAC = new TextView(getContext());
            tvMAC.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
            tvMAC.setPadding(5, 0, 0, 5);
            tvMAC.setGravity(Gravity.TOP);
            
            tvMAC.setBackgroundColor(getResources().getColor(R.color.light_grey));
            tvMAC.setTextColor(Color.BLACK);
            tvMAC.setTextSize(TypedValue.COMPLEX_UNIT_PX, verySmallTextSize);
            tvMAC.setText(currUnit.getMac());
            descLayout.addView(tvMAC);

            final TextView tvDesc = new TextView(getContext());
            tvDesc.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
            tvDesc.setGravity(Gravity.RIGHT);
            tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            tvDesc.setPadding(5, 1, 0, 5);
            tvDesc.setTextColor(getResources().getColor(R.color.dark_grey));
            //tvDesc.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tvDesc.setText(currUnit.getDesc());
            descLayout.addView(tvDesc);

            final LinearLayout histLayout = new LinearLayout(getContext());
            histLayout.setOrientation(LinearLayout.HORIZONTAL);
            histLayout.setPadding(0, 10, 0, 10);
            histLayout.setBackgroundColor(getResources().getColor(R.color.cell_bg_even));

            final Button tipsButton = new MaterialButton(getContext());
            tipsButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
            tipsButton.setGravity(Gravity.CENTER);
            //tipsButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);
            tipsButton.setText("[Tips]");
            tipsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentUnitId = currId;
                    currentUnitIdx = myI;
                    setFlipperView(2);
                }
            });
            histLayout.addView(tipsButton);

            final Space spacer = new Space(getContext());
            spacer.setLayoutParams(new TableRow.LayoutParams(10, TableLayout.LayoutParams.MATCH_PARENT));
            histLayout.addView(spacer);

            final Button lfButton = new MaterialButton(getContext());
            lfButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            lfButton.setGravity(Gravity.CENTER);
            //lfButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            lfButton.setText("[LF]");
            lfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentUnitId = currId;
                    currentUnitIdx = myI;
                    setFlipperView(3);
                }
            });
            histLayout.addView(lfButton);


            // add table row
            final TableRow tr = new TableRow(getContext());
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0, 0, 0, 0);
            tr.setLayoutParams(trParams);
            if (i == currentUnitIdx) {
                tr.setBackgroundColor(Color.YELLOW);
                tvId.setBackgroundColor(Color.YELLOW);
                tvName.setBackgroundColor(Color.YELLOW);
                descLayout.setBackgroundColor(Color.YELLOW);
                histLayout.setBackgroundColor(Color.YELLOW);
            }


            tr.addView(tvId);
            tr.addView(tvName);
            tr.addView(descLayout);
            tr.addView(histLayout);

            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TableRow tr = (TableRow) v;
                    //do whatever action is needed
                    currentUnitId = currId;
                    currentUnitIdx = myI;
                    setFlipperView(1);
                }
            });

            arduinoTableLayout.addView(tr, trParams);

            // add separator row
            final TableRow trSep = new TableRow(getContext());
            TableLayout.LayoutParams trParamsSep = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
            trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            trSep.setLayoutParams(trParamsSep);
            TextView tvSep = new TextView(getContext());
            TableRow.LayoutParams tvSepLay = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
            tvSepLay.span = 4;
            tvSep.setLayoutParams(tvSepLay);
            //tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
            tvSep.setBackgroundColor(Color.RED);
            tvSep.setHeight(1);
            trSep.addView(tvSep);
            arduinoTableLayout.addView(trSep, trParamsSep);

        }
    }

    public void updateArduinoUnit() {
        updateArduinoUnit(0);
    }

    public void updateArduinoUnit(int tidx) {
        ArduinoUnit currentUnit = arduinodb.getArduinoUnit(currentUnitId);
        dtChanged = false;
        List<TipHistory> tipHistoryList = tipHistDb.getTipsByUnit(currentUnitId);
        List<String> tipHistoryDates = tipHistDb.getDatesByUnit(currentUnitId);
        tipHist = tipHistDb.getLatestTipsByUnit(currentUnitId);
        if (tidx > 0) tipHist = tipHistoryList.get(tidx);
        if (currentUnit == null) return;

        //get GUI components
        TextView tvId = root.findViewById(R.id.TextView_Unit_ID);
        TextView tvName = root.findViewById(R.id.TextView_Unit_Name);
        TextView tvDesc = root.findViewById(R.id.TextView_Unit_Desc);
        Spinner spDate = root.findViewById(R.id.Spinner_Unit_Date);
        TextView tvType = root.findViewById(R.id.TextView_Unit_Type);
        TextView tvIrrRate = root.findViewById(R.id.TextView_Unit_IrrRate);
        TextView tvDiamLabel = root.findViewById(R.id.TextView_Unit_Diam_Label);
        TextView tvDiam = root.findViewById(R.id.TextView_Unit_Diam);
        TextView tvTip1 = root.findViewById(R.id.TextView_Unit_Tip1);
        TextView tvTip2 = root.findViewById(R.id.TextView_Unit_Tip2);
        TextView tvTip3 = root.findViewById(R.id.TextView_Unit_Tip3);
        TextView tvTip4 = root.findViewById(R.id.TextView_Unit_Tip4);
        TextView tvTipAvg = root.findViewById(R.id.TextView_Unit_TipAvg);
        CheckBox cbTip1 = root.findViewById(R.id.TextView_Unit_Tip1_Label);
        CheckBox cbTip2 = root.findViewById(R.id.TextView_Unit_Tip2_Label);
        CheckBox cbTip3 = root.findViewById(R.id.TextView_Unit_Tip3_Label);
        CheckBox cbTip4 = root.findViewById(R.id.TextView_Unit_Tip4_Label);
        TextView tvLFTPct = root.findViewById(R.id.TextView_Unit_LFTPct);
        TextView tvLFPct = root.findViewById(R.id.TextView_Unit_LFPct);
        EditText tvRT = root.findViewById(R.id.TextView_Unit_RT);
        TextView tvRTT = root.findViewById(R.id.TextView_Unit_RTT);
        Button saveButton = root.findViewById(R.id.buttonSaveLF);
        Button deleteButton = root.findViewById(R.id.buttonDelete);
        Button editButton = root.findViewById(R.id.buttonEdit);
        Spinner spRT = root.findViewById(R.id.Spinner_RT_Select);

        float lastRtt = lfHistDb.getMostRecenRtt(currentUnitId, (String)spDate.getSelectedItem());
        System.out.println("RTT  "+lastRtt);

        cbTip1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (tipHist == null) return;
                tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
                tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
                currentUnit.setTipperDisableStatus(0, !cbTip1.isChecked());
                updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
            }
        });

        cbTip2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (tipHist == null) return;
                tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
                tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
                currentUnit.setTipperDisableStatus(1, !cbTip2.isChecked());
                updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
            }
        });

        cbTip3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (tipHist == null) return;
                tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
                tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
                currentUnit.setTipperDisableStatus(2, !cbTip3.isChecked());
                updateUnit(currentUnit, tvRT,tvLFPct, tvRTT);
            }
        });

        cbTip4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (tipHist == null) return;
                tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
                tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
                currentUnit.setTipperDisableStatus(3, !cbTip4.isChecked());
                updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
            }
        });

        cbTip1.setChecked(!currentUnit.getTipperDisableStatus(0));
        cbTip2.setChecked(!currentUnit.getTipperDisableStatus(1));
        cbTip3.setChecked(!currentUnit.getTipperDisableStatus(2));
        cbTip4.setChecked(!currentUnit.getTipperDisableStatus(3));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),   android.R.layout.simple_spinner_dropdown_item, tipHistoryDates);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spDate.setAdapter(spinnerArrayAdapter);
        spDate.setSelection(tidx);
        spDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //updateArduinoUnit(position);


                ((TextView) spDate.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) spDate.getChildAt(0)).setTextSize(24);

                tipHist = tipHistoryList.get(position);
                tvTip1.setText(String.valueOf(tipHist.getCounts(0)));
                tvTip2.setText(String.valueOf(tipHist.getCounts(1)));
                tvTip3.setText(String.valueOf(tipHist.getCounts(2)));
                tvTip4.setText(String.valueOf(tipHist.getCounts(3)));
                //do calcs here
                //float tipAvg = tipHist.getAvgCounts();
                tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
                tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
                updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
                float lastRtt = lfHistDb.getMostRecenRtt(currentUnitId, (String)spDate.getSelectedItem());
                System.out.println("RTT2  "+lastRtt);
                /*
                float tipToLf = 0.0017f; //conversion from tips to LF
                float lf = tipAvg*tipToLf;
                float irrLiters;
                if (currentUnit.getType() == ArduinoUnit.MODE_MICRO) {
                    irrLiters = currentUnit.getIrrigRate()*3.785f*currentUnit.getRuntime()/60.0f;
                } else {
                    float contRadCm = currentUnit.getContainerDiam()*2.54f/2.0f;
                    float contAreaCm = (float) (contRadCm*contRadCm*Math.PI);
                    irrLiters = currentUnit.getRuntime()/60.0f*currentUnit.getIrrigRate()*2.54f*contAreaCm/1000.0f;
                }
                lfpct = (lf/irrLiters)*100.0f;
                rtt = (100.0f-lfpct)/(100.0f- currentUnit.getTargetLF())*currentUnit.getRuntime();

                tvLFPct.setText(MainActivity.roundOutput(String.valueOf(lfpct), 2));
                tvRTT.setText(MainActivity.roundOutput(String.valueOf(rtt), 2)); */
                if (lfHistDb.hasLFHistory(currentUnit.getID(), tipHist.getID())) saveButton.setEnabled(false); else saveButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        tvRT.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float x = Float.parseFloat(tvRT.getText().toString());
                } catch(NumberFormatException nfe) { return; }
                updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
            }
        });

        spRT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    tvRT.setEnabled(false);
                    float lastRtt = lfHistDb.getMostRecenRtt(currentUnitId, (String)spDate.getSelectedItem());
                    if (lastRtt < 0) lastRtt = currentUnit.getRuntime();
                    tvRT.setText(MainActivity.roundOutput(String.valueOf(lastRtt), 1));
                } else if (position == 1) {
                    tvRT.setEnabled(false);
                    tvRT.setText(MainActivity.roundOutput(String.valueOf(currentUnit.getRuntime()), 1));
                } else tvRT.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

                //update GUI components from arduino
        tvId.setText(String.valueOf(currentUnit.getID()));
        tvName.setText(currentUnit.getName());
        tvDesc.setText(currentUnit.getDesc());
        if (currentUnit.getType() == ArduinoUnit.MODE_MICRO) {
            tvType.setText("Micro");
            tvIrrRate.setText(String.valueOf(currentUnit.getIrrigRate())+" GPH");
            tvDiamLabel.setVisibility(View.INVISIBLE);
            tvDiam.setVisibility(View.INVISIBLE);
        } else {
            tvType.setText("Sprinkler");
            tvIrrRate.setText(String.valueOf(currentUnit.getIrrigRate())+" in/hr");
            tvDiamLabel.setVisibility(View.VISIBLE);
            tvDiam.setText(String.valueOf(currentUnit.getContainerDiam()));
            tvDiam.setVisibility(View.VISIBLE);
        }
        tvLFTPct.setText(MainActivity.roundOutput(String.valueOf(currentUnit.getTargetLF()), 0));
        tvRT.setText(MainActivity.roundOutput(String.valueOf(currentUnit.getRuntime()), 1));

        //button logic -- save/edit/delete
        //edit needs new screen in view flipper
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFlipperView(4);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete from db - after dialog confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getContext());
                final String[] options = {"Delete", "Cancel"};
                // set title
                builder.setTitle("Delete Unit?");
                builder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                //delete unit and close dialog
                                tipHistDb.deleteTipHistoryByUnit(currentUnit.getID());
                                lfHistDb.deleteLFHistoryByUnit(currentUnit.getID());
                                arduinodb.deleteArduinoUnit(currentUnit);
                                dialog.cancel();
                                setFlipperView(0);
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

        if (tipHist == null) {
            saveButton.setEnabled(false);
            tvTip1.setText("N/A");
            tvTip2.setText("N/A");
            tvTip3.setText("N/A");
            tvTip4.setText("N/A");
            tvTipAvg.setText("N/A");
            return;
        }
        //update tips
        tvTip1.setText(String.valueOf(tipHist.getCounts(0)));
        tvTip2.setText(String.valueOf(tipHist.getCounts(1)));
        tvTip3.setText(String.valueOf(tipHist.getCounts(2)));
        tvTip4.setText(String.valueOf(tipHist.getCounts(3)));

        if (lfHistDb.hasLFHistory(currentUnit.getID(), tipHist.getID())) saveButton.setEnabled(false); else saveButton.setEnabled(true);

        //do calcs here
        tipAvg = getAvgCounts(tipHist.getCounts(0), tipHist.getCounts(1), tipHist.getCounts(2), tipHist.getCounts(3), cbTip1.isChecked(), cbTip2.isChecked(), cbTip3.isChecked(), cbTip4.isChecked());
        tvTipAvg.setText(MainActivity.roundOutput(String.valueOf(tipAvg), 0));
        //float tipAvg = tipHist.getAvgCounts();
        updateUnit(currentUnit, tvRT, tvLFPct, tvRTT);
        /*
        float tipToLf = 0.0017f; //conversion from tips to LF
        float lf = tipAvg*tipToLf;
        float irrLiters;
        if (currentUnit.getType() == ArduinoUnit.MODE_MICRO) {
            irrLiters = currentUnit.getIrrigRate()*3.785f*currentUnit.getRuntime()/60.0f;
        } else {
            float contRadCm = currentUnit.getContainerDiam()*2.54f/2.0f;
            float contAreaCm = (float) (contRadCm*contRadCm*Math.PI);
            irrLiters = currentUnit.getRuntime()/60.0f*currentUnit.getIrrigRate()*2.54f*contAreaCm/1000.0f;
        }
        lfpct = (lf/irrLiters)*100.0f;
        rtt = (100.0f-lfpct)/(100.0f- currentUnit.getTargetLF())*currentUnit.getRuntime();

        tvLFPct.setText(MainActivity.roundOutput(String.valueOf(lfpct), 2));
        tvRTT.setText(MainActivity.roundOutput(String.valueOf(rtt), 2)); */

        saveButton.setEnabled(true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LFHistory hist = new LFHistory(currentUnit.getID());
                hist.setTID(tipHist.getID());
                hist.setDate(tipHist.getDate());
                hist.setLfTPct(currentUnit.getTargetLF());
                hist.setLfPct(lfpct);
                //hist.setRt(currentUnit.getRuntime());
                hist.setRt(rt);
                hist.setRtT(rtt);
                lfHistDb.addLFHistory(hist);
                if (dtChanged) {
                    arduinodb.updateArduinoUnit(currentUnit);
                }
                setFlipperView(3);
            }
        });
    }

    public float getAvgCounts(int tip1, int tip2, int tip3, int tip4, boolean use1, boolean use2, boolean use3, boolean use4) {
        float tips = 0.0f;
        int divisor = 0;
        if (use1) {
            tips += tip1;
            divisor+=1;
        }
        if (use2) {
            tips += tip2;
            divisor+=1;
        }
        if (use3) {
            tips += tip3;
            divisor+=1;
        }
        if (use4) {
            tips += tip4;
            divisor+=1;
        }
        if (divisor == 0) return 0;
        return tips/divisor;
    }

    public void updateUnit(ArduinoUnit currentUnit, EditText tvRT, TextView tvLFPct, TextView tvRTT) {
        float tipToLf = 0.0017f; //conversion from tips to LF
        float lf = tipAvg*tipToLf;
        rt = Float.parseFloat(tvRT.getText().toString());
        float irrLiters;
        if (currentUnit.getType() == ArduinoUnit.MODE_MICRO) {
            //irrLiters = currentUnit.getIrrigRate()*3.785f*currentUnit.getRuntime()/60.0f;
            irrLiters = currentUnit.getIrrigRate()*3.785f*rt/60.0f;
        } else {
            float contRadCm = currentUnit.getContainerDiam()*2.54f/2.0f;
            float contAreaCm = (float) (contRadCm*contRadCm*Math.PI);
            //irrLiters = currentUnit.getRuntime()/60.0f*currentUnit.getIrrigRate()*2.54f*contAreaCm/1000.0f;
            irrLiters = rt/60.0f*currentUnit.getIrrigRate()*2.54f*contAreaCm/1000.0f;
        }
        lfpct = (lf/irrLiters)*100.0f;
        //rtt = (100.0f-lfpct)/(100.0f- currentUnit.getTargetLF())*currentUnit.getRuntime();
        rtt = (100.0f-lfpct)/(100.0f- currentUnit.getTargetLF())*rt;

        tvLFPct.setText(MainActivity.roundOutput(String.valueOf(lfpct), 0));
        tvRTT.setText(MainActivity.roundOutput(String.valueOf(rtt), 1));
        dtChanged = true;
    }

    public void updateTipHistoryTable() {
        //db query
        List<TipHistory> tipHistoryList = tipHistDb.getTipsByUnit(currentUnitId);

        // setup the table
        tipsTableLayout.setStretchAllColumns(true);

        int leftRowMargin = 0;
        int topRowMargin = 0;
        int rightRowMargin = 0;
        int bottomRowMargin = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        int rows = tipHistoryList.size();
        TextView textSpacer = null;

        tipsTableLayout.removeViews(1, tipsTableLayout.getChildCount()-1);
        TextView tvId = root.findViewById(R.id.TextView_Tips_ID);
        TextView tvName = root.findViewById(R.id.TextView_Tips_Name);
        TextView tvDesc = root.findViewById(R.id.TextView_Tips_Desc);
        Button addManualTipsButton = root.findViewById(R.id.buttonAddTips);

        ArduinoUnit unit = arduinodb.getArduinoUnit(currentUnitId);
        tvId.setText(String.valueOf(currentUnitId));
        if (unit != null) {
            tvName.setText(unit.getName());
            tvDesc.setText(unit.getDesc());
        }

        addManualTipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete from db - after dialog confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getContext());
                builder.setTitle("Add Tips");

                // set the custom layout
                final View customLayout = getLayoutInflater().inflate(R.layout.tip_dialog,null);
                builder.setView(customLayout);
                EditText addTip1 = customLayout.findViewById(R.id.EditText_AddTips_Counter1);
                EditText addTip2 = customLayout.findViewById(R.id.EditText_AddTips_Counter2);
                EditText addTip3 = customLayout.findViewById(R.id.EditText_AddTips_Counter3);
                EditText addTip4 = customLayout.findViewById(R.id.EditText_AddTips_Counter4);

                EditText tipDate=(EditText) customLayout.findViewById(R.id.EditText_AddTips_Date);
                tipDate.setInputType(InputType.TYPE_NULL);
                tipDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar cldr = Calendar.getInstance();
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);
                        // date picker dialog
                        DatePickerDialog picker = new DatePickerDialog(builder.getContext(),
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String dateStr = String.valueOf(year)+"-";
                                        if (monthOfYear+1 < 10) dateStr += "0";
                                        dateStr += (monthOfYear+1)+"-";
                                        if (dayOfMonth < 10) dateStr += "0";
                                        dateStr += dayOfMonth;
                                        tipDate.setText(dateStr);
                                        if (manualTipDialog != null && !tipHistDb.hasTipHistory(currentUnitId, dateStr)) {
                                            manualTipDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        }
                                    }
                                }, year, month, day);
                        picker.show();
                    }
                });

                // add a button
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (addTip1.getText().toString().equals("")) addTip1.setText("0");
                        if (addTip2.getText().toString().equals("")) addTip2.setText("0");
                        if (addTip3.getText().toString().equals("")) addTip3.setText("0");
                        if (addTip4.getText().toString().equals("")) addTip4.setText("0");

                        int tips1 = Integer.parseInt(addTip1.getText().toString());
                        int tips2 = Integer.parseInt(addTip2.getText().toString());
                        int tips3 = Integer.parseInt(addTip3.getText().toString());
                        int tips4 = Integer.parseInt(addTip4.getText().toString());
                        TipHistory manualHist = new TipHistory(currentUnitId, tipDate.getText().toString(), tips1, tips2, tips3, tips4);
                        System.out.println("HIST: "+manualHist.toString());
                        tipHistDb.addTipHistory(manualHist);
                        dialog.cancel();
                        setFlipperView(_currentScreen);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // create and show
                // create alert dialog
                manualTipDialog= builder.create();
                manualTipDialog.show();
                manualTipDialog.setCanceledOnTouchOutside(false);
                manualTipDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        // -1 means heading row
        for (int i = 0; i < rows; i++) {
            final TipHistory currHist = tipHistoryList.get(i);
            final int myI = i;
            // data columns

            final TextView tvDate = new TextView(getContext());
            tvDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvDate.setPadding(5, 0, 0, 5);
            tvDate.setGravity(Gravity.CENTER);

            tvDate.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tvDate.setTextColor(Color.parseColor("#000000"));
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            tvDate.setText(currHist.getDate());

            final TextView[] tvTips = new TextView[4];
            for (int j=0; j < 4; j++) {
                tvTips[j] = new TextView(getContext());
                tvTips[j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
                tvTips[j].setPadding(5, 0, 20, 5);
                tvTips[j].setGravity(Gravity.RIGHT);
                tvTips[j].setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);
                tvTips[j].setBackgroundColor(Color.parseColor("#ffffff"));
                tvTips[j].setTextColor(Color.parseColor("#000000"));
                tvTips[j].setText(String.valueOf(currHist.getCounts(j)));
            }

            final Button deleteButton = new MaterialButton(getContext());
            deleteButton.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
            deleteButton.setGravity(Gravity.CENTER);
            deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            deleteButton.setText("[X]");

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //delete from db - after dialog confirm
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getContext());
                    final String[] options = {"Delete", "Cancel"};
                    // set title
                    builder.setTitle("Delete History?");
                    builder.setPositiveButton("Delete",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    //delete unit and close dialog
                                    tipHistDb.deleteTipHistory(currHist);
                                    dialog.cancel();
                                    setFlipperView(_currentScreen);
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

            // add table row
            final TableRow tr = new TableRow(getContext());
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0, 0, 0, 0);
            tr.setLayoutParams(trParams);

            if (lfHistDb.hasLFHistory(currHist.getAID(), currHist.getID())) {
                for (int j = 0; j < 4; j++) tvTips[j].setBackgroundColor(Color.YELLOW);
                tr.setBackgroundColor(Color.YELLOW);
            }

            tr.addView(tvDate);
            for (int j = 0; j < 4; j++) tr.addView(tvTips[j]);
            tr.addView(deleteButton);

            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TableRow tr = (TableRow) v;
                    //do whatever action is needed
                    setFlipperView(1);
                    updateArduinoUnit(myI);
                }
            });

            tipsTableLayout.addView(tr, trParams);

            // add separator row
            final TableRow trSep = new TableRow(getContext());
            TableLayout.LayoutParams trParamsSep = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            trSep.setLayoutParams(trParamsSep);
            TextView tvSep = new TextView(getContext());
            TableRow.LayoutParams tvSepLay = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tvSepLay.span = 6;
            tvSep.setLayoutParams(tvSepLay);
            tvSep.setBackgroundColor(Color.RED);
            tvSep.setHeight(1);
            trSep.addView(tvSep);
            tipsTableLayout.addView(trSep, trParamsSep);

        }
    }

    public void updateLFHistoryTable() {
        //db query
        List<LFHistory> lfHistoryList = lfHistDb.getLFHistoryByUnit(currentUnitId);

        // setup the table
        lfTableLayout.setStretchAllColumns(true);

        int leftRowMargin = 0;
        int topRowMargin = 0;
        int rightRowMargin = 0;
        int bottomRowMargin = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        int rows = lfHistoryList.size();
        TextView textSpacer = null;

        lfTableLayout.removeViews(1, lfTableLayout.getChildCount()-1);
        TextView tvId = root.findViewById(R.id.TextView_LF_ID);
        TextView tvName = root.findViewById(R.id.TextView_LF_Name);
        TextView tvDesc = root.findViewById(R.id.TextView_LF_Desc);

        ArduinoUnit unit = arduinodb.getArduinoUnit(currentUnitId);
        tvId.setText(String.valueOf(currentUnitId));
        if (unit != null) {
            tvName.setText(unit.getName());
            tvDesc.setText(unit.getDesc());
        }

        // -1 means heading row
        for (int i = 0; i < rows; i++) {
            final LFHistory currHist = lfHistoryList.get(i);
            // data columns

            final TextView tvDate = new TextView(getContext());
            tvDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvDate.setPadding(5, 0, 0, 5);
            tvDate.setGravity(Gravity.CENTER);
            tvDate.setBackgroundColor(getResources().getColor(R.color.cell_bg_odd));
            tvDate.setTextColor(Color.BLACK);
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            tvDate.setText(currHist.getDate());

            final TextView tvLFT = new TextView(getContext());
            tvLFT.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvLFT.setPadding(5, 0, 0, 5);
            tvLFT.setGravity(Gravity.CENTER);
            tvLFT.setBackgroundColor(getResources().getColor(R.color.cell_bg_even));
            tvLFT.setTextColor(Color.BLACK);
            tvLFT.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);
            tvLFT.setText(MainActivity.roundOutput(String.valueOf(currHist.getLfTPct()), 0));

            final TextView tvLFPct = new TextView(getContext());
            tvLFPct.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvLFPct.setPadding(5, 0, 0, 5);
            tvLFPct.setGravity(Gravity.CENTER);
            tvLFPct.setBackgroundColor(getResources().getColor(R.color.cell_bg_odd));
            tvLFPct.setTextColor(Color.BLACK);
            tvLFPct.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);
            tvLFPct.setText(MainActivity.roundOutput(String.valueOf(currHist.getLfPct()), 0));

            final TextView tvRT = new TextView(getContext());
            tvRT.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvRT.setPadding(5, 0, 0, 5);
            tvRT.setGravity(Gravity.CENTER);
            tvRT.setBackgroundColor(getResources().getColor(R.color.cell_bg_even));
            tvRT.setTextColor(Color.BLACK);
            tvRT.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);
            tvRT.setText(MainActivity.roundOutput(String.valueOf(currHist.getRt()), 1));

            final TextView tvRTT = new TextView(getContext());
            tvRTT.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvRTT.setPadding(5, 0, 20, 5);
            tvRTT.setGravity(Gravity.CENTER);
            tvRTT.setBackgroundColor(getResources().getColor(R.color.cell_bg_odd));
            tvRTT.setTextColor(Color.BLACK);
            tvRTT.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeTextSize);
            tvRTT.setText(MainActivity.roundOutput(String.valueOf(currHist.getRTT()), 1));

            final Button deleteButton = new MaterialButton(getContext());
            deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            deleteButton.setGravity(Gravity.CENTER);
            //deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            deleteButton.setText("[X]");

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //delete from db - after dialog confirm
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getContext());
                    final String[] options = {"Delete", "Cancel"};
                    // set title
                    builder.setTitle("Delete History?");
                    builder.setPositiveButton("Delete",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    //delete unit and close dialog
                                    lfHistDb.deleteLFHistory(currHist);
                                    dialog.cancel();
                                    setFlipperView(_currentScreen);
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

            // add table row
            final TableRow tr = new TableRow(getContext());
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(0,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0, 0, 0, 0);
            tr.setLayoutParams(trParams);

            tr.addView(tvDate);
            tr.addView(tvLFT);
            tr.addView(tvLFPct);
            tr.addView(tvRT);
            tr.addView(tvRTT);
            tr.addView(deleteButton);

            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TableRow tr = (TableRow) v;
                    //do whatever action is needed

                }
            });

            lfTableLayout.addView(tr, trParams);

            // add separator row
            final TableRow trSep = new TableRow(getContext());
            TableLayout.LayoutParams trParamsSep = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            trSep.setLayoutParams(trParamsSep);
            TextView tvSep = new TextView(getContext());
            TableRow.LayoutParams tvSepLay = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tvSepLay.span = 6;
            tvSep.setLayoutParams(tvSepLay);
            tvSep.setBackgroundColor(Color.RED);
            tvSep.setHeight(4);
            trSep.addView(tvSep);
            lfTableLayout.addView(trSep, trParamsSep);

        }

    }

    public void updateEditArduinoScreen() {
        ArduinoUnit currentUnit = arduinodb.getArduinoUnit(currentUnitId);
        System.out.println("CURRENT UNIT "+currentUnit);
        if (currentUnit == null) return;

        TextView editAdruinoMAC = (TextView) root.findViewById(R.id.editArduinoMAC);
        EditText editArduinoNameInput=(EditText) root.findViewById(R.id.editArduinoNameInput);
        EditText editArduinoDescInput=(EditText) root.findViewById(R.id.editArduinoDescInput);
        Spinner editArduinoUnitType=(Spinner) root.findViewById(R.id.editArduinoUnitType);
        EditText editArduinoIrrigRate=(EditText) root.findViewById(R.id.editArduinoIrrigRate);
        EditText editArduinoLFTarget=(EditText) root.findViewById(R.id.editArduinoTargetLF);
        EditText editArduinoRuntime=(EditText) root.findViewById(R.id.editArduinoRuntime);
        EditText editArduinoContDiam=(EditText) root.findViewById(R.id.editArduinoContDiam);
        TextView editArduinoContDiamLabel=(TextView)root.findViewById(R.id.editArduinoContDiamLabel);
        Button saveButton = (Button) root.findViewById(R.id.buttonEditSave);
        Button cancelButton = (Button) root.findViewById(R.id.buttonEditCancel);
        Button deleteButton = (Button) root.findViewById(R.id.buttonEditDelete);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save changes to db
                currentUnit.setName(editArduinoNameInput.getText().toString());
                currentUnit.setDesc(editArduinoDescInput.getText().toString());
                currentUnit.setType(editArduinoUnitType.getSelectedItemPosition());
                currentUnit.setIrrigRate(Float.parseFloat(editArduinoIrrigRate.getText().toString()));
                currentUnit.setTargetLF(Float.parseFloat(editArduinoLFTarget.getText().toString()));
                currentUnit.setRuntime(Float.parseFloat(editArduinoRuntime.getText().toString()));
                if (currentUnit.getType() == ArduinoUnit.MODE_SPRINKLER) {
                    currentUnit.setContainerDiam(Float.parseFloat(editArduinoContDiam.getText().toString()));
                }
                arduinodb.updateArduinoUnit(currentUnit);
                setFlipperView(1);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFlipperView(1);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete from db - after dialog confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getContext());
                final String[] options = {"Delete", "Cancel"};
                // set title
                builder.setTitle("Delete Unit?");
                builder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                //delete unit and close dialog
                                tipHistDb.deleteTipHistoryByUnit(currentUnit.getID());
                                lfHistDb.deleteLFHistoryByUnit(currentUnit.getID());
                                arduinodb.deleteArduinoUnit(currentUnit);
                                dialog.cancel();
                                setFlipperView(0);
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


        editArduinoUnitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (editArduinoUnitType.getSelectedItemPosition() == ArduinoUnit.MODE_MICRO) {
                    editArduinoContDiam.setVisibility(View.GONE);
                    editArduinoContDiamLabel.setVisibility(View.GONE);
                } else {
                    editArduinoContDiam.setVisibility(View.VISIBLE);
                    editArduinoContDiamLabel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        editAdruinoMAC.setText(currentUnit.getMac());
        editArduinoNameInput.setText(currentUnit.getName());
        editArduinoDescInput.setText(currentUnit.getDesc());
        editArduinoUnitType.setSelection(currentUnit.getType());
        editArduinoIrrigRate.setText(String.valueOf(currentUnit.getIrrigRate()));
        editArduinoLFTarget.setText(String.valueOf(currentUnit.getTargetLF()));
        editArduinoRuntime.setText(String.valueOf(currentUnit.getRuntime()));
        if (currentUnit.getType() == ArduinoUnit.MODE_SPRINKLER) {
            editArduinoContDiam.setText(String.valueOf(currentUnit.getContainerDiam()));
        }

    }

    public void onSwipeRight() {
        System.out.println("MOVE RIGHT");
        int id = _currentScreen+1;
        if (id > 4) id = 0;
        setFlipperView(id);
    }

    public void onSwipeLeft() {
        System.out.println("MOVE LEFT");
        int id = _currentScreen-1;
        if (id < 0) id = 4;
        setFlipperView(id);
    }

    public void onSwipeTop() {
        System.out.println("MOVE UP");
        if (currentUnitIdx > 0) {
            currentUnitIdx--;
            setFlipperView(_currentScreen);
        }
    }

    public void onSwipeBottom() {
        System.out.println("MOVE DOWN "+(arduinoUnitList.size()-1));
        if (currentUnitIdx < arduinoUnitList.size()-1) {
            currentUnitIdx++;
            setFlipperView(_currentScreen);
        }
    }


}