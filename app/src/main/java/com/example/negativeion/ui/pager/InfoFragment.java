package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.negativeion.ChartUI;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.model.NegativeIonModel;
import com.example.negativeion.R;
import com.example.negativeion.model.Temperature2Model;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class InfoFragment extends Fragment
                        implements OnChartValueSelectedListener
                        , OnChartGestureListener {

    final String TAG = InfoFragment.class.getSimpleName();
    MysqlConnect mMysqlConnect;
    LineChart mLineChart;
    TextView mTvShowData;
    Button button0, button1, button2, button3, button4;
    Button mBtnTime1, mBtnTime2, mBtnSearchTime;
    private List<NegativeIonModel> mNegativeIonList;
    private List<Temperature2Model> mTemperature2ModelList;
    private Handler mHandler;
    private Runnable CONNRunable;
    private Runnable rGetLastValueRunnable;

    int choose_year=0, choose_month, choose_day, choose_hour, choose_minute;
    public InfoFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        button0 =  view.findViewById(R.id.btn_2tem);
        button1 =  view.findViewById(R.id.btn_tem);
        button2 =  view.findViewById(R.id.btn_hum);
        button3 =  view.findViewById(R.id.btn_neg);
        button4 =  view.findViewById(R.id.btn_pm25);

        mBtnTime1 = view.findViewById(R.id.btn_time1);
        mBtnTime2 = view.findViewById(R.id.btn_time2);
        mBtnSearchTime = view.findViewById(R.id.btn_searchTime);
        mBtnSearchTime.setEnabled(false);
        button0.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                initRunnable2Tem();
                new Thread(CONNRunable).start();
            }
        });
        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //MysqlConnect.setIndex(1);
                mMysqlConnect.setIndex(1);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //MysqlConnect.setIndex(2);
                mMysqlConnect.setIndex(2);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //MysqlConnect.setIndex(3);
                mMysqlConnect.setIndex(3);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //MysqlConnect.setIndex(4);
                mMysqlConnect.setIndex(4);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });

        mBtnTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateAndTimePicker(v.getId(), mBtnTime1.getText().toString());

            }
        });

        mBtnTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateAndTimePicker(v.getId(), mBtnTime2.getText().toString());
                mBtnSearchTime.setEnabled(true);
            }
        });

        mBtnSearchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBtnTime1.getText() != "" && mBtnTime2.getText() != "") {
                    String str = ""+mBtnTime1.getText();
                    String[] strArray = str.split(" ");
                    String str2 = ""+mBtnTime2.getText();
                    String[] strArray2 = str2.split(" ");

                    //mBtnSearchTime.setText(strArray[0] + "哈" + strArray[1]);
                    mMysqlConnect.setDateANDTime(strArray[0], strArray[1]);
                    mMysqlConnect.setDate2ANDTime2(strArray2[0], strArray2[1]);
                    new Thread(CONNRunable).start();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTvShowData = getView().findViewById(R.id.tv_showData);
        mMysqlConnect = new MysqlConnect();
        mNegativeIonList = new ArrayList<>();
        mTemperature2ModelList = new ArrayList<>();

        mLineChart = getView().findViewById(R.id.chart_line);
        mLineChart.setFocusable(true);
        mLineChart.setOnChartValueSelectedListener(this);
        mLineChart.setOnChartGestureListener(this);

        mLineChart.getOnTouchListener().getLastGesture();
        ChartUI.mpLineChart(mLineChart, null,0);

        mHandler = new Handler();
        initRunnableFirst();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(rGetLastValueRunnable).start();
        mLineChart.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void dateAndTimePicker(final int BtnId, String str_btn){
        View view = View.inflate(getContext(), R.layout.datetime_picker, null);
        final DatePicker datePicker = view.findViewById(R.id.DPicker);
        final TimePicker timePicker = view.findViewById(R.id.TPicker);

        int year=0, month=0, day=0, hour=0, minute=0;
        Calendar c = Calendar.getInstance();

        /** 日期選擇器 */
        if(str_btn != null && str_btn != ""){
            year = choose_year;
            month = choose_month;
            day = choose_day;
        }else {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        datePicker.init(year, month, day ,null);
        /** 時間選擇器 */
        if(str_btn != null && !str_btn.equals("")) {
            hour = choose_hour;
            minute = choose_minute;
        }else {
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }
        timePicker.setIs24HourView(true);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        /** Dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view).setTitle("選時間")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strDateTime = ""; //"yyyy-MM-dd hh:mm:ss"
                        strDateTime += datePicker.getYear() + "-" + (datePicker.getMonth()+1) + "-"
                                + datePicker.getDayOfMonth();
                        strDateTime += " " + timePicker.getHour() + ":" + timePicker.getMinute() + ":00";
                        choose_year = datePicker.getYear();
                        choose_month = datePicker.getMonth();
                        choose_day = datePicker.getDayOfMonth();
                        choose_hour = timePicker.getHour();
                        choose_minute = timePicker.getMinute();

                        if(BtnId == R.id.btn_time1)
                            mBtnTime1.setText(strDateTime);
                        else if(BtnId == R.id.btn_time2)
                            mBtnTime2.setText(strDateTime);
                    }
                }).show();
    }
    private void initRunnableFirst() {
        rGetLastValueRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.setIndex(0);
                mMysqlConnect.CONN();
                if(mMysqlConnect.getNegativeIonModelList() == null) {
                    return;
                }
                final NegativeIonModel negativeIonModel =
                        mMysqlConnect.getNegativeIonModelList().get(mMysqlConnect.getNegativeIonModelList().size()-1);
                button1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        button1.setText("溫度\n" + negativeIonModel.getTemperatureValue());
                        button2.setText("濕度\n" + negativeIonModel.getHumidityValue());
                        button3.setText("負離子\n" + negativeIonModel.getNegativeIonValue());
                        button4.setText("PM2.5\n" + negativeIonModel.getPm25Value());
                    }
                },10);
            }
        };
    }
    private void initRunnable() {
        CONNRunable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.CONN(); //NetworkOnMainThreadException 要新開Thread

                if(mMysqlConnect.getNegativeIonModelList() == null) {
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //
                        }
                    });*///Toast.makeText(getActivity(), "資料讀取失敗", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNegativeIonList.clear();
                mNegativeIonList.addAll(mMysqlConnect.getNegativeIonModelList());

                if(mLineChart.getData().getDataSetByIndex(1) != null)
                    mLineChart.getData().getDataSetByIndex(1).clear();
                ChartUI.mpLineChart(mLineChart, mNegativeIonList, mMysqlConnect.getIndex());
                mLineChart.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String value = mNegativeIonList.
                                get(mNegativeIonList.size()-1).getTemperatureValue();
                        mTvShowData.setText(value);
                       mLineChart.invalidate();//要在原生Thread才能使用
                    }
                }, 500);

            }
        };
    }
    private void initRunnable2Tem() {
        CONNRunable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.connectTemperature(); //NetworkOnMainThreadException 要新開Thread

                if(mMysqlConnect.getTemperatureModelList() == null) {
                    return;
                }

                mTemperature2ModelList.addAll(mMysqlConnect.getTemperatureModelList());

                if(mLineChart.getData().getDataSetByIndex(1) != null) {
                   // mLineChart.getData().getDataSetByIndex(1).clear();
                    //mLineChart.getData().getDataSetByIndex(2).clear();
                }
                ChartUI.mpLineChart_2tem(mLineChart, mTemperature2ModelList);
                mLineChart.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String value = mTemperature2ModelList.
                                get(mTemperature2ModelList.size()-1).getTemperatureValue();
                        mTvShowData.setText(value);
                        mLineChart.invalidate();//要在原生Thread才能使用
                    }
                }, 500);
            }
        };
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        mTvShowData.setText(""+e.getY());

        mLineChart.getData().getDataSetByIndex(0).removeLast();
        Log.d(TAG, "onValueSelected:" + e.getX() + e.getY());
        Log.d(TAG, "onValueSelected:" + h.toString());

        mLineChart.getData().getDataSetByIndex(0).addEntry(e);
    }

    @Override
    public void onNothingSelected() {

    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.d(TAG, "onChartGestureStart");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.d(TAG, "onChartGestureEnd");
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.d(TAG, "onChartLongPressed");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.d(TAG, "onChartDoubleTapped");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.d(TAG, "onChartSingleTapped");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.d(TAG, "onChartFling");
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.d(TAG, "onChartScale:X=" + scaleX + "Y=" + scaleY);
        Log.d(TAG, "x軸Min:" + mLineChart.getXAxis().getAxisMinimum() + " Max:" + mLineChart.getXAxis().getAxisMaximum());
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        //Log.d(TAG, "onChartTranslate X:" + dX + " Y:" + dY);
        //Log.d(TAG, "onChartTranslate X:" + me.getX() + " Y:" + me.getY() );
        //Toast.makeText(getContext(), "X:" + dX + " Y:" + dY, Toast.LENGTH_SHORT).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
