package com.example.negativeion.ui.pager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.negativeion.ChartUI;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.NegativeIonModel;
import com.example.negativeion.R;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.List;


public class InfoFragment extends Fragment {

    MysqlConnect mMysqlConnect;
    LineChart mLineChart;
    private List<NegativeIonModel> mNegativeIonList;

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

        mMysqlConnect = new MysqlConnect();
        mNegativeIonList = new ArrayList<>();

        mLineChart = getView().findViewById(R.id.chart_line);
        ChartUI.mpLineChart(mLineChart, null);

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
                }
                mNegativeIonList.addAll(mMysqlConnect.getNegativeIonModelList());
                ChartUI.mpLineChart(mLineChart, mNegativeIonList);
                mLineChart.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       mLineChart.invalidate();//要在原生Thread才能使用
                    }
                }, 500);

            }
        };
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
