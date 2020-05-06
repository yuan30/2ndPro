package com.example.negativeion;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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
import java.util.HashSet;
import java.util.List;

public class ChartUI {

    private final static String TAG = ChartUI.class.getSimpleName();
    private List<NegativeIonModel> mNegativeIonList;
    private static IMovingChart iMovingChart;
    static String mTimeStr[];

    public static void init(IMovingChart iMovingChart1){
        iMovingChart = iMovingChart1;
    }
    //original version
    public static void mpLineChart(final LineChart lineChart, List<NegativeIonModel> negativeIonList, int index)
    {
        LineData lineData = lineChart.getData();
        if(lineData == null) {
            lineData = new LineData();
            lineChart.setData(lineData);
            lineChart.setTouchEnabled(true);

            lineChart.getAxisRight().setDrawLabels(false);
            //lineChart.setVisibleXRangeMaximum(70);
        }
        else{
            ILineDataSet iLineDataSet1;
            iLineDataSet1 = createEmptyLineDataSet();
            lineData.addDataSet(iLineDataSet1); //for draw one point circle

            ILineDataSet iLineDataSet = lineData.getDataSetByIndex(1);
            if(iLineDataSet == null){
                iLineDataSet = createLineDataSet(negativeIonList, index);
                lineData.addDataSet(iLineDataSet);

                iLineDataSet1.addEntry(iLineDataSet.getEntryForIndex(iLineDataSet.getEntryCount()-1));
                XAxis xAxis = lineChart.getXAxis();

                xAxis.setAvoidFirstLastClipping(true);
                //xAxis.setAxisMaximum(240);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if((int)value >= mTimeStr.length)
                            return "";
                        System.out.println(value + "chart getX:" );
                        //iMovingChart.MovingPoint();
                        return mTimeStr[(int)value];
                    }
                });
                xAxis.setLabelRotationAngle(-45);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                //lineChart.setMaxVisibleValueCount(9);
            }else{
                iLineDataSet.clear();
                iLineDataSet = createLineDataSet(negativeIonList, index);
                iLineDataSet1.addEntry(iLineDataSet.getEntryForIndex(iLineDataSet.getEntryCount()-1));
            }
            //lineChart.highlightValue(new Highlight(30f,0),true);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            //lineChart.moveViewToX(lineChart.getX());

        }
    }/*
    private static LineDataSet createEmptyLineDataSet(){
        LineDataSet set = new LineDataSet(new ArrayList<Entry>(), "" );
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        return set;
    }*/

    private static LineDataSet createLineDataSet(List<NegativeIonModel> negativeIonList, int index) {

        ArrayList<Entry> entries = new ArrayList<>();
        int length = 0;
        for(NegativeIonModel negativeIonModel : negativeIonList) {
            //if(negativeIonModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(negativeIonModel.getTemperatureValue()) != 0) {
                length++;
            //}
        }
        mTimeStr = new String[length];
        System.out.println(length);
        int i=0;
        for(NegativeIonModel negativeIonModel : negativeIonList) {
            if (index == 1) {
                //mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);
                entries.add(new Entry(i, Float.valueOf(negativeIonModel.getTemperatureValue())));
                //i++;
            } else if (index == 2) {
                //mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);
                entries.add(new Entry(i, Float.valueOf(negativeIonModel.getHumidityValue())));
                //i++;
            } else if (index == 3){
                //mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);
                entries.add(new Entry(i, Float.valueOf(negativeIonModel.getNegativeIonValue())));
            }
            else if(index == 4){
                //mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);
                entries.add(new Entry(i, Float.valueOf(negativeIonModel.getPm25Value())));
            }
            mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);
            i++;
            /*if(negativeIonModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(negativeIonModel.getTemperatureValue()) != 0) {
                mTimeStr[i] = negativeIonModel.getTimeValue().substring(11);


                Log.d(TAG, negativeIonModel.getTimeValue());
                entries.add(new Entry(i//Float.valueOf(negativeIonModel.getTimeValue().substring(14).replace(':', '.'))
                        , Float.valueOf(negativeIonModel.getTemperatureValue())));
                i++;
            }/*else
                Log.d(TAG, negativeIonModel.getTimeValue().substring(5,7).compareTo("03") + "");*/
        }//mTimeStr[i] = "\n";

        LineDataSet set = new LineDataSet(entries, "Line DataSet:1" );
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        //set.setDrawCircleHole(true);

        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.BLUE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

    public static void mpLineChart_2tem(final LineChart lineChart, List<Temperature2Model> temperature2ModelList)
    {
        LineData lineData = lineChart.getData();
        if(lineData == null) {
            lineData = new LineData();
            lineChart.setData(lineData);
            lineChart.setTouchEnabled(true);

            lineChart.getAxisRight().setDrawLabels(false);
            //lineChart.setVisibleXRangeMaximum(70);
        }
        else{
            ILineDataSet iLineDataSet1;
            iLineDataSet1 = createEmptyLineDataSet();
            lineData.addDataSet(iLineDataSet1); //for draw one point circle

            int length=0;
            for(Temperature2Model temperature2Model : temperature2ModelList) {
                if(Float.valueOf(temperature2Model.getTemperatureValue()) != 0) {
                    length++;
                }
            }int i=0;   mTimeStr = new String[length];
            for(Temperature2Model temperature2Model : temperature2ModelList) {
                if (Float.valueOf(temperature2Model.getTemperatureValue()) != 0) {
                    mTimeStr[i] = temperature2Model.getTimeValue().substring(11);
                }
                i++;
            }



            HashSet hashSet = new HashSet();
            for (Temperature2Model temperature2Model : temperature2ModelList) {
                //Log.d(TAG, temperature2Model.getTId());
                hashSet.add(temperature2Model.getTId());
                Log.d(TAG, "比對"+ temperature2Model.getTId().equals(String.valueOf(0)) + " ");
            }Log.d(TAG, hashSet.size()+"雜湊長度");
            Log.d(TAG, length+"");
            for(int j = 0; j<hashSet.size(); j++){
                ILineDataSet iLineDataSet = lineData.getDataSetByIndex(j+1);
                if(iLineDataSet == null){
                    iLineDataSet = createLineDataSet_2tem(temperature2ModelList, j);
                    lineData.addDataSet(iLineDataSet);
                }
            }
            //ILineDataSet iLineDataSet = lineData.getDataSetByIndex(1);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setAvoidFirstLastClipping(true);
            //xAxis.setAxisMaximum(240);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if((int)value >= mTimeStr.length)
                        return "";
                    System.out.println(value + "chart getX:" );
                    //iMovingChart.MovingPoint();
                    return mTimeStr[(int)value];
                }
            });
            xAxis.setLabelRotationAngle(-45);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            //lineChart.setMaxVisibleValueCount(9);
            //lineChart.highlightValue(new Highlight(30f,0),true);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            //lineChart.moveViewToX(lineChart.getX());
        }
    }
    private static LineDataSet createEmptyLineDataSet(){
        LineDataSet set = new LineDataSet(new ArrayList<Entry>(), "" );
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        return set;
    }

    private static LineDataSet createLineDataSet_2tem(List<Temperature2Model> temperature2ModelList, int index) {

        ArrayList<Entry> entries = new ArrayList<>();

        int i=0;
        for(Temperature2Model temperature2Model : temperature2ModelList){
            if(temperature2Model.getTimeValue().substring(5,7).compareTo("04") >= 0 && temperature2Model.getTId().equals(String.valueOf(index))) {


                //Log.d(TAG, temperature2Model.getTimeValue() + index);
                entries.add(new Entry(i//Float.valueOf(temperature2Model.getTimeValue().substring(14).replace(':', '.'))
                        , Float.valueOf(temperature2Model.getTemperatureValue())));
                i++;
            }/*else
                Log.d(TAG, temperature2Model.getTimeValue().substring(5,7).compareTo("03") + "");*/
        }//mTimeStr[i] = "\n";

        LineDataSet set = new LineDataSet(entries, "Line DataSet:" + temperature2ModelList.get(0).getTimeValue().substring(0,10)  );
        if(index == 0)
            set.setColor(Color.rgb(240, 238, 70));
        else
            set.setColor(Color.rgb(240, 100, 100));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        //set.setDrawCircleHole(true);

        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.BLUE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

/*
    private static LineDataSet createLineDataSet(List<Temperature2Model> temperatureModelList) {

        ArrayList<Entry> entries = new ArrayList<>();
        int length = 0;
        for(Temperature2Model temperatureModel : temperatureModelList) {
            if(temperatureModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(temperatureModel.getTemperatureValue()) != 0) {
                length++;
            }
        }
        mTimeStr = new String[length];
        System.out.println(length);
        int i=0;
        for(Temperature2Model temperatureModel : temperatureModelList){
            if(temperatureModel.getTimeValue().substring(5,7).compareTo("04") >= 0 &&  Float.valueOf(temperatureModel.getTemperatureValue()) != 0) {
                mTimeStr[i] = temperatureModel.getTimeValue().substring(11);


                Log.d(TAG, temperatureModel.getTimeValue());
                entries.add(new Entry(i//Float.valueOf(temperatureModel.getTimeValue().substring(14).replace(':', '.'))
                        , Float.valueOf(temperatureModel.getTemperatureValue())));
                i++;
            }/*else
                Log.d(TAG, temperatureModel.getTimeValue().substring(5,7).compareTo("03") + "");
        }//mTimeStr[i] = "\n";

        LineDataSet set = new LineDataSet(entries, "Line DataSet:2020-04-07" );
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        //set.setDrawCircleHole(true);

        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.BLUE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }*/
}