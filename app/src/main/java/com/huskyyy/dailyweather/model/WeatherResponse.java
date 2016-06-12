package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wang on 2016/5/11.
 */
public class WeatherResponse {

    @SerializedName("showapi_res_code") public int resCode;
    @SerializedName("showapi_res_error") public String error;
    @SerializedName("showapi_res_body") public WeatherInfo result;

}
