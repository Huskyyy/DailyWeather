package com.huskyyy.dailyweather.net.api;

import com.huskyyy.dailyweather.model.WeatherResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Wang on 2016/5/16.
 */
public interface WeatherApi {

    @GET("9-5?needMoreDay=1&need3HourForcast=1")
    Observable<WeatherResponse> getWeatherByCoordinates(@Query("from") String from,
                                                        @Query("lng") String lng,
                                                        @Query("lat") String lat);

    @GET("9-4?needMoreDay=1&need3HourForcast=1")
    Observable<WeatherResponse> getWeatherByIp();

    @GET("9-2?needMoreDay=1&need3HourForcast=1")
    Observable<WeatherResponse> getWeatherById(@Query("areaid") String areaId);


}
