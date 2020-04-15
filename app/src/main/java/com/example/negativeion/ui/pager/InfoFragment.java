package com.example.negativeion.ui.pager;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.negativeion.ChartUI;
import com.example.negativeion.IMovingChart;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.NegativeIonModel;
import com.example.negativeion.R;
import com.example.negativeion.TemperatureModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;


public class InfoFragment extends Fragment
                        implements OnChartValueSelectedListener, IMovingChart
                        , OnChartGestureListener {

    final String TAG = InfoFragment.class.getSimpleName();
    MysqlConnect mMysqlConnect;
    LineChart mLineChart;
    TextView mTvShowData;

    private List<NegativeIonModel> mNegativeIonList;
    private List<TemperatureModel> mTemperatureModelList;
    private Handler mHandler;
    private Runnable CONNRunable;

    public InfoFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTvShowData = getView().findViewById(R.id.tv_showData);
        mMysqlConnect = new MysqlConnect();
        mNegativeIonList = new ArrayList<>();
        mTemperatureModelList = new ArrayList<>();

        mLineChart = getView().findViewById(R.id.chart_line);
        mLineChart.setFocusable(true);
        mLineChart.setOnChartValueSelectedListener(this);
        mLineChart.setOnChartGestureListener(this);

        mLineChart.getOnTouchListener().getLastGesture();
        ChartUI.mpLineChart(mLineChart, null);
        ChartUI.init(this);

        mHandler = new Handler();
        initRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(CONNRunable).start();
        mLineChart.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

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
                }/*
                mNegativeIonList.addAll(mMysqlConnect.getNegativeIonModelList());
                ChartUI.mpLineChart(mLineChart, mNegativeIonList);
                mLineChart.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String value = mNegativeIonList.
                                get(mNegativeIonList.size()-1).getTemperatureValue();
                        mTvShowData.setText(value);
                       mLineChart.invalidate();//要在原生Thread才能使用
                    }
                }, 500);*/
                mTemperatureModelList.addAll(mMysqlConnect.getTemperatureModelList());
                ChartUI.mpLineChart(mLineChart, mTemperatureModelList);
                mLineChart.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String value = mTemperatureModelList.
                                get(mTemperatureModelList.size()-1).getTemperatureValue();
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
    public void MovingPoint() {
        Entry e = mLineChart.getData().getDataSetByIndex(1).getEntryForIndex(
                (int)mLineChart.getData().getDataSetByIndex(0).getEntryForIndex(0).getX()
        );
        mTvShowData.setText(""+e.getY());
        mLineChart.getData().getDataSetByIndex(0).removeLast();

        mLineChart.getData().getDataSetByIndex(0).addEntry(e);

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
        Log.d(TAG, "onChartScale");
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
