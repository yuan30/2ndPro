package com.example.negativeion.ui.pager;

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
import android.widget.TextView;

import com.example.negativeion.ChartUI;
import com.example.negativeion.IMovingChart;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.NegativeIonModel;
import com.example.negativeion.R;
import com.example.negativeion.Temperature2Model;
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
    TextView mTvTestSQL;

    private List<NegativeIonModel> mNegativeIonList;
    private List<Temperature2Model> mTemperature2ModelList;
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
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        Button button0 =  view.findViewById(R.id.btn_2tem);
        Button button1 =  view.findViewById(R.id.btn_tem);
        Button button2 =  view.findViewById(R.id.btn_hum);
        Button button3 =  view.findViewById(R.id.btn_neg);
        Button button4 =  view.findViewById(R.id.btn_pm25);
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
                MysqlConnect.setIndex(1);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MysqlConnect.setIndex(2);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MysqlConnect.setIndex(3);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        button4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MysqlConnect.setIndex(4);
                initRunnable();
                new Thread(CONNRunable).start();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTvTestSQL = getView().findViewById(R.id.tv_testSQL);
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
        ChartUI.init(this);

        mHandler = new Handler();
        //initRunnable();
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
                }
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
                    mLineChart.getData().getDataSetByIndex(1).clear();
                    mLineChart.getData().getDataSetByIndex(2).clear();
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
