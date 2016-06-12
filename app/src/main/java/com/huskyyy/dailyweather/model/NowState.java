package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Wang on 2016/5/11.
 */
public class NowState implements Serializable {

    public int aqi;
    public AqiDetail aqiDetail;
    public String sd;
    public String temperature;
    @SerializedName("temperature_time") public String temperatureTime;
    public String weather;
    @SerializedName("weather_code") public String weatherCode;
    @SerializedName("weather_pic") public String weatherPic;
    @SerializedName("wind_direction") public String windDirection;
    @SerializedName("wind_power") public String windPower;


}
