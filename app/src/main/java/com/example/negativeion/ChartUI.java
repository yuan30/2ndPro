package com.example.negativeion;

import android.graphics.Color;
import android.util.Log;

import com.example.negativeion.NegativeIonModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartUI {

    private final static String TAG = ChartUI.class.getSimpleName();
    private List<NegativeIonModel> mNegativeIonList;

    static String mTimeStr[];

    //MP Bar Chart
    public static void mpBarChart(BarChart barChart, float time, float value) {
        BarData barData = barChart.getData(); //範型 Template
        if (barData == null) {
            barData = new BarData();
            barChart.setData(barData);

            barChart.setTouchEnabled(true);
        } else {
            IBarDataSet iBarDataSet = barData.getDataSetByIndex(0);
            if (iBarDataSet == null) {
                iBarDataSet = createBarDataSet();
                barData.addDataSet(iBarDataSet);
            }
            iBarDataSet.addEntry(new BarEntry(time, value));
            barData.notifyDataChanged();
            barChart.notifyDataSetChanged();
            barChart.moveViewToX(time);
        }
    }

    private static BarDataSet createBarDataSet() {
        List<BarEntry> barEntryList = new ArrayList<>();
        BarDataSet barDataSet = new BarDataSet(barEntryList, "First data");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setHighLightColor(Color.parseColor("#FF0000"));
        return barDataSet;
    }

    public static void mpLineChart(LineChart lineChart, List<NegativeIonModel> negativeIonList)
    {
        LineData lineData = lineChart.getData();
        if(lineData == null) {
            lineData = new LineData();
            lineChart.setData(lineData);
            lineChart.setTouchEnabled(true);
        }
        else{
            ILineDataSet iLineDataSet = lineData.getDataSetByIndex(0);
            if(iLineDataSet == null){
                iLineDataSet = createLineDataSet(negativeIonList);
                lineData.addDataSet(iLineDataSet);
                XAxis xAxis = lineChart.getXAxis();

                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        System.out.println(value);

                        return mTimeStr[(int)value];
                    }
                });
                xAxis.setLabelRotationAngle(-45);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                //lineChart.setMaxVisibleValueCount(9);
            }

            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            //lineChart.moveViewToX(lineChart.getX());

        }
    }

    private static LineDataSet createLineDataSet(List<NegativeIonModel> negativeIonList) {

        ArrayList<Entry> entries = new ArrayList<>();
        int length = 0;
        for(NegativeIonModel negativeIonModel : negativeIonList) {
            if(negativeIonModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(negativeIonModel.getTemperatureValue()) != 0) {
                length++;
            }
        }
        mTimeStr = new String[length];
        System.out.println(length);
        int i=0;
        for(NegativeIonModel negativeIonModel : negativeIonList){
            if(negativeIonModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(negativeIonModel.getTemperatureValue()) != 0) {
                mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);


                Log.d(TAG, negativeIonModel.getTimeValue());
                entries.add(new Entry(i//Float.valueOf(negativeIonModel.getTimeValue().substring(14).replace(':', '.'))
                        , Float.valueOf(negativeIonModel.getTemperatureValue())));
                i++;
            }/*else
                Log.d(TAG, negativeIonModel.getTimeValue().substring(5,7).compareTo("03") + "");*/
        }//mTimeStr[i] = "\n";

        LineDataSet set = new LineDataSet(entries, "Line DataSet:2020-04-07" );
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.BLUE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }
}