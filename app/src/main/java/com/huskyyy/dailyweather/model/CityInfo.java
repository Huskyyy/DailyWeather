package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Wang on 2016/5/11.
 */
public class CityInfo implements Serializable{

    @SerializedName("c1") public String cityId;
    @SerializedName("c3") public String name;
    @SerializedName("c5") public String cityName;
    @SerializedName("c7") public String provName;
    @SerializedName("c10") public String level;

}
