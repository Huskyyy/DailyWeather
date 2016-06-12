package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wang on 2016/5/16.
 */
public class CityResults {
    @SerializedName("list") public List<CityInfoWrapper> cities;
    @SerializedName("ret_code") public int retCode;
}
