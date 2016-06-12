package com.huskyyy.dailyweather.ui.func;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by Wang on 2016/6/11.
 */
public class MyYAxisValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;

    public MyYAxisValueFormatter () {
        mFormat = new DecimalFormat("##"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        // write your logic here
        // access the YAxis object to get more information
        return mFormat.format(value) + "°"; // e.g. append a dollar-sign
    }
}
