package com.huskyyy.dailyweather.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.huskyyy.dailyweather.net.api.CityApi;
import com.huskyyy.dailyweather.net.api.WeatherApi;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Wang on 2016/5/11.
 */
public class HttpHelper {

    public static final String BASE_URL = "http://route.showapi.com/";
    public static final String APP_ID = "YourAppId";
    public static final String APP_SIGN = "YourApSign";
    private static WeatherApi weatherApi;
    private static CityApi cityApi;
    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();


    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new LoginInterceptor())
            .build();

    public static WeatherApi getWeatherApi(){
        if(weatherApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            weatherApi = retrofit.create(WeatherApi.class);
        }
        return weatherApi;
    }

    public static CityApi getCityApi(){
        if(cityApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            cityApi = retrofit.create(CityApi.class);
        }
        return cityApi;
    }

    public static class LoginInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException{
            HttpUrl url = chain.request().url()
                    .newBuilder()
                    .addQueryParameter("showapi_appid", APP_ID)
                    .addQueryParameter("showapi_sign", APP_SIGN)
                    .build();
            Request request = chain.request().newBuilder().url(url).build();
            return chain.proceed(request);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

}
