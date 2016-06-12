package com.huskyyy.dailyweather.net.api;

import com.huskyyy.dailyweather.model.CityResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Wang on 2016/5/16.
 */
public interface CityApi {

    @GET("9-3")
    Observable<CityResponse> getCityInfo(@Query("area") String area);

}
