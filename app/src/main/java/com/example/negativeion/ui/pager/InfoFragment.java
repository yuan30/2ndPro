package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.negativeion.ChartUI;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.model.NegativeIonModel;
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
    Button button1, button2, button3, button4;
    Button mBtnTime1, mBtnTime2, mBtnSearchTime;
    private List<NegativeIonModel> mNegativeIonList;
    private List<Temperature2Model> mTemperature2ModelList;
    private Handler mDataHandler; //for refresh screen
    private Runnable runGetDataSetFromDatas, runPeriodGetDatas;
    private Runnable runGetLastDataFromDatas;

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

        mDataHandler = new Handler();

        button1 =  view.findViewById(R.id.btn_tem);
        button2 =  view.findViewById(R.id.btn_hum);
        button3 =  view.findViewById(R.id.btn_neg);
        button4 =  view.findViewById(R.id.btn_pm25);

        mBtnTime1 = view.findViewById(R.id.btn_time1);
        mBtnTime2 = view.findViewById(R.id.btn_time2);
        mBtnSearchTime = view.findViewById(R.id.btn_searchTime);
        mBtnSearchTime.setEnabled(false);

        button1.setOnClickListener(v -> {
            processingData(1);
        });
        button2.setOnClickListener(v -> {
            processingData(2);
        });
        button3.setOnClickListener(v -> {
            processingData(3);
        });
        button4.setOnClickListener(v -> {
            processingData(4);
        });

        mBtnTime1.setOnClickListener(v -> dateAndTimePicker(v.getId(), mBtnTime1.getText().toString()));

        mBtnTime2.setOnClickListener(v -> {
            dateAndTimePicker(v.getId(), mBtnTime2.getText().toString());
            mBtnSearchTime.setEnabled(true);
        });

        mBtnSearchTime.setOnClickListener(v -> {
            if(mBtnTime1.getText() != "" && mBtnTime2.getText() != "") {
                String str = ""+mBtnTime1.getText();
                String[] strArray = str.split(" ");
                String str2 = ""+mBtnTime2.getText();
                String[] strArray2 = str2.split(" ");

                mMysqlConnect.setDateANDTime(strArray[0], strArray[1]);
                mMysqlConnect.setDate2ANDTime2(strArray2[0], strArray2[1]);
                new Thread(runGetDataSetFromDatas).start();
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

        initRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        //new Thread(runGetLastDataFromDatas).start();
        mLineChart.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDataHandler.removeCallbacks(runPeriodGetDatas);
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
                .setPositiveButton("OK", (dialog, which) -> {
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
                }).show();
    }
    
    private void initRunnable() {
        //此Runnable為了更新畫面上每個按鈕代表的數值，一點開畫面時會呼叫
        //目前不開放
        /*runGetLastDataFromDatas = () -> {
            mMysqlConnect.setIndex(0);
            mMysqlConnect.CONN();
            if(mMysqlConnect.getNegativeIonModelList() == null) {
                return;
            }
            final NegativeIonModel negativeIonModel =
                    mMysqlConnect.getNegativeIonModelList().get(mMysqlConnect.getNegativeIonModelList().size()-1);
            button1.postDelayed(() -> {
                button1.setText("溫度\n" + negativeIonModel.getTemperatureValue());
                button2.setText("濕度\n" + negativeIonModel.getHumidityValue());
                button3.setText("負離子\n" + negativeIonModel.getNegativeIonValue());
                button4.setText("PM2.5\n" + negativeIonModel.getPm25Value());
            },10);
        };*/
        
        //此Runnable為了更新畫面中間的數字與圖表
        runGetDataSetFromDatas = () -> {
            mMysqlConnect.CONN(); //NetworkOnMainThreadException 要新開Thread

            if(mMysqlConnect.getNegativeIonModelList() == null) {
                return;
            }
            mNegativeIonList.clear();
            mNegativeIonList.addAll(mMysqlConnect.getNegativeIonModelList());

            if(mLineChart.getData().getDataSetByIndex(1) != null)
                mLineChart.getData().getDataSetByIndex(1).clear();
            ChartUI.mpLineChart(mLineChart, mNegativeIonList, mMysqlConnect.getIndex());

            mLineChart.postDelayed(() -> {
                String value = "";
                switch (mMysqlConnect.getIndex()){
                    case 1:
                        value = mNegativeIonList.get(mNegativeIonList.size()-1).getTemperatureValue();
                        break;
                    case 2:
                        value = mNegativeIonList.get(mNegativeIonList.size()-1).getHumidityValue();
                        break;
                    case 3:
                        value = mNegativeIonList.get(mNegativeIonList.size()-1).getNegativeIonValue();
                        break;
                    case 4:
                        value = mNegativeIonList.get(mNegativeIonList.size()-1).getPm25Value();
                        break;
                }
                mTvShowData.setText(value);
                mLineChart.invalidate();//要在原生Thread才能使用
                //10秒更新一次，且確保是在更新值之後
                mDataHandler.postDelayed(runPeriodGetDatas,10000);
            }, 500);
        };

        runPeriodGetDatas = () -> {
            new Thread(runGetDataSetFromDatas).start();//for 刷新圖表
        };
    }

    public void processingData(int index) {
        //根據不同index執行不同sql語法片段(ex.1是溫度與時間、2是濕度與時間)
        mMysqlConnect.setIndex(index);
        new Thread(runGetDataSetFromDatas).start();
        //清除前次動作，下次動作在更新值之後
        mDataHandler.removeCallbacks(runPeriodGetDatas);
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
