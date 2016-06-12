package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Wang on 2016/5/11.
 */
public class Forcast implements Serializable {

    @SerializedName("3hourForcast") public List<HourForcast> hourForcasts;
    @SerializedName("air_press") public String airPressure;
    @SerializedName("day_air_temperature") public String dayTemperature;
    @SerializedName("day_weather") public String dayWeather;
    @SerializedName("day_weather_pic") public String dayWeatherPic;
    @SerializedName("night_air_temperature") public String nightTemperature;
    @SerializedName("jiangshui") public String rainProb;
    public int weekday;
    @SerializedName("ziwaixian") public String uvLight;

}
