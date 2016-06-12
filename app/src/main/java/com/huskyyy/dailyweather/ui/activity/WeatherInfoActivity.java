package com.huskyyy.dailyweather.ui.activity;

import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.huskyyy.dailyweather.R;
import com.huskyyy.dailyweather.model.HourForcast;
import com.huskyyy.dailyweather.model.WeatherInfo;
import com.huskyyy.dailyweather.model.WeatherResponse;
import com.huskyyy.dailyweather.net.HttpHelper;
import com.huskyyy.dailyweather.ui.func.MyValueFormatter;
import com.huskyyy.dailyweather.ui.func.MyYAxisValueFormatter;
import com.huskyyy.dailyweather.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Wang on 2016/6/11.
 */
public class WeatherInfoActivity extends BaseActivity {

    @BindView(R.id.iv_back) ImageView backButton;
    @BindView(R.id.tv_title) TextView titleText;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.tv_weather) TextView weatherText;
    @BindView(R.id.tv_temperature) TextView temperatureText;
    @BindView(R.id.tv_air_quality) TextView airQualityText;

    @BindView(R.id.chart_line) LineChart temperatureChart;

    @BindView(R.id.tv_humidity) TextView humidityText;
    @BindView(R.id.tv_wind_direction) TextView windDirectionText;
    @BindView(R.id.tv_wind_power) TextView windPowerText;
    @BindView(R.id.tv_rain) TextView rainText;
    @BindView(R.id.tv_uvlight) TextView uvlightText;
    @BindView(R.id.tv_aqi) TextView aqiText;
    @BindView(R.id.tv_air_pressure) TextView airPressureText;

    @BindView(R.id.tv_f2) TextView f2Text;
    @BindView(R.id.tv_f3) TextView f3Text;
    @BindView(R.id.tv_f4) TextView f4Text;
    @BindView(R.id.tv_f5) TextView f5Text;
    @BindView(R.id.tv_f6) TextView f6Text;
    @BindView(R.id.tv_f7) TextView f7Text;
    @BindView(R.id.iv_weather_today) ImageView todayWeatherImage;
    @BindView(R.id.iv_weather_f2) ImageView f2WeatherImage;
    @BindView(R.id.iv_weather_f3) ImageView f3WeatherImage;
    @BindView(R.id.iv_weather_f4) ImageView f4WeatherImage;
    @BindView(R.id.iv_weather_f5) ImageView f5WeatherImage;
    @BindView(R.id.iv_weather_f6) ImageView f6WeatherImage;
    @BindView(R.id.iv_weather_f7) ImageView f7WeatherImage;
    @BindView(R.id.tv_temperature_range_today) TextView todayTemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f2) TextView f2TemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f3) TextView f3TemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f4) TextView f4TemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f5) TextView f5TemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f6) TextView f6TemperatureRangeText;
    @BindView(R.id.tv_temperature_range_f7) TextView f7TemperatureRangeText;

    private Unbinder unbinder;

    private boolean refreshing;
    private WeatherInfo info;

    public final String[] weekdays = new String[]{"周日","周一","周二","周三","周四","周五","周六"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherinfo);
        unbinder = ButterKnife.bind(this);

        info = (WeatherInfo) getIntent().getSerializableExtra(MainActivity.WEATHER_INFO);

        setupActionBar();
        setupSwipeRefreshLayout();
        setupScrollView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(unbinder != null)
            unbinder.unbind();
    }

    private void setupActionBar(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleText.setText(info.cityInfo.name);
    }

    private void setupSwipeRefreshLayout(){
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRefreshProgress1,
                R.color.colorRefreshProgress2, R.color.colorRefreshProgress3);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });
    }

    private void updateData(){
        startRefreshing();

        if(!HttpHelper.isNetworkAvailable(this)) {
            ToastUtils.showShort(R.string.network_unavailable);
            stopRefreshing();
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date currentDate = new Date(System.currentTimeMillis());
        final String date = formatter.format(currentDate);

        if(info.time.substring(0, 8).equals(date.substring(0, 8))
                && info.nowState.temperatureTime.substring(0, 2).equals(date.substring(8, 10))){
            stopRefreshing();
            return;
        }

        Observer<WeatherInfo> observer = new Observer<WeatherInfo>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                stopRefreshing();
                ToastUtils.showShort(R.string.update_failure);
            }

            @Override
            public void onNext(WeatherInfo weatherInfo) {
                stopRefreshing();
                info = weatherInfo;
                setupScrollView();
            }
        };

        Subscription subscription = HttpHelper.getWeatherApi()
                .getWeatherById(info.cityInfo.cityId)
                .map(new Func1<WeatherResponse, WeatherInfo>() {
                    @Override
                    public WeatherInfo call(WeatherResponse weatherResponse) {
                        WeatherInfo res = weatherResponse.result;
                        res.orderNum = info.orderNum;
                        return res;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        addSubscription(subscription);
    }

    private void setupScrollView(){
        if(info != null){

            weatherText.setText(info.nowState.weather);
            temperatureText.setText(info.nowState.temperature);
            if(info.nowState.aqiDetail != null && info.nowState.aqiDetail.quality != null)
                airQualityText.setText(getResources().getString(R.string.air_quality)
                        + info.nowState.aqiDetail.quality);
            else
                airQualityText.setText(getResources().getString(R.string.air_quality)
                        + getResources().getString(R.string.data_not_updated));

            // 图表部分
            List<HourForcast> hourForcasts = info.todayForcast.hourForcasts;
            List<String> hours = new ArrayList<>();
            List<Entry> temperatures = new ArrayList<>();
            for(int i = 0; i < hourForcasts.size(); i++){
                HourForcast f = hourForcasts.get(i);
                String tmp = f.hour.split("-")[0];
                hours.add(tmp);
                Entry entry = new Entry(Float.valueOf(f.temperature), i);
                temperatures.add(entry);
            }

            XAxis xAxis = temperatureChart.getXAxis();
            xAxis.setValues(hours);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            YAxis leftAxis = temperatureChart.getAxisLeft();
            leftAxis.setValueFormatter(new MyYAxisValueFormatter());
            YAxis rightAxis = temperatureChart.getAxisRight();
            rightAxis.setEnabled(false);
            Legend legend = temperatureChart.getLegend();
            legend.setEnabled(false);

            LineDataSet dataSet =new LineDataSet(temperatures, "temperature");
            dataSet.setColor(getResources().getColor(R.color.colorWhite));
            dataSet.setCircleColor(getResources().getColor(R.color.colorWhite));
            dataSet.setValueTextColor(getResources().getColor(R.color.colorWhite));
            dataSet.setValueTextSize(10);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setValueFormatter(new MyValueFormatter());
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet);
            LineData data = new LineData(hours, dataSets);
            temperatureChart.setData(data);
            temperatureChart.setDescription(null);
            temperatureChart.invalidate();


            humidityText.setText(info.nowState.sd);
            windDirectionText.setText(info.nowState.windDirection);
            windPowerText.setText(info.nowState.windPower);
            aqiText.setText("" + info.nowState.aqi);
            if(info.todayForcast.rainProb == null)
                rainText.setText(getResources().getString(R.string.data_not_updated));
            else
                rainText.setText(info.todayForcast.rainProb);
            if(info.todayForcast.uvLight == null)
                uvlightText.setText(getResources().getString(R.string.data_not_updated));
            else
                uvlightText.setText(info.todayForcast.uvLight);
            if(info.todayForcast.airPressure == null)
                airPressureText.setText(getResources().getString(R.string.data_not_updated));
            else
                airPressureText.setText(info.todayForcast.airPressure);

            int weekday = info.todayForcast.weekday;
            f2Text.setText(weekdays[(weekday + 1) % 7]);
            f3Text.setText(weekdays[(weekday + 2) % 7]);
            f4Text.setText(weekdays[(weekday + 3) % 7]);
            f5Text.setText(weekdays[(weekday + 4) % 7]);
            f6Text.setText(weekdays[(weekday + 5) % 7]);
            f7Text.setText(weekdays[(weekday + 6) % 7]);
            todayTemperatureRangeText.setText(
                    info.todayForcast.nightTemperature + "°" + "/"
                    + info.todayForcast.dayTemperature + "°");
            f2TemperatureRangeText.setText(
                    info.secondDayForcast.nightTemperature + "°" + "/"
                            + info.secondDayForcast.dayTemperature + "°");
            f3TemperatureRangeText.setText(
                    info.thirdDayForcast.nightTemperature + "°" + "/"
                            + info.thirdDayForcast.dayTemperature + "°");
            f4TemperatureRangeText.setText(
                    info.fourthDayForcast.nightTemperature + "°" + "/"
                            + info.fourthDayForcast.dayTemperature + "°");
            f5TemperatureRangeText.setText(
                    info.fifthDayForcast.nightTemperature + "°" + "/"
                            + info.fifthDayForcast.dayTemperature + "°");
            f6TemperatureRangeText.setText(
                    info.sixthDayForcast.nightTemperature + "°" + "/"
                            + info.sixthDayForcast.dayTemperature + "°");
            f7TemperatureRangeText.setText(
                    info.seventhDayForcast.nightTemperature + "°" + "/"
                            + info.seventhDayForcast.dayTemperature + "°");

            Glide.with(this)
                    .load(info.todayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(todayWeatherImage);
            Glide.with(this)
                    .load(info.secondDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f2WeatherImage);
            Glide.with(this)
                    .load(info.thirdDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f3WeatherImage);
            Glide.with(this)
                    .load(info.fourthDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f4WeatherImage);
            Glide.with(this)
                    .load(info.fifthDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f5WeatherImage);
            Glide.with(this)
                    .load(info.sixthDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f6WeatherImage);
            Glide.with(this)
                    .load(info.seventhDayForcast.dayWeatherPic)
                    .fitCenter()
                    .into(f7WeatherImage);
        }
    }

    private void startRefreshing(){
        refreshing = true;
        if(swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void stopRefreshing(){
        refreshing = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(refreshing == false && swipeRefreshLayout != null){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);

    }
}
