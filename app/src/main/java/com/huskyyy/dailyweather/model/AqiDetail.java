package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Wang on 2016/5/11.
 */
public class AqiDetail implements Serializable{

    @SerializedName("primary_pollutant") public String primaryPollutant;
    public String quality;

}