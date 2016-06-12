package com.huskyyy.dailyweather;

import android.app.Application;
import android.content.Context;

import com.huskyyy.dailyweather.util.ToastUtils;
import com.litesuits.orm.LiteOrm;

/**
 * Created by Wang on 2016/5/11.
 */
public class App extends Application {

    public static final String DB_NAME = "weather.db";
    public static Context mContext;
    public static LiteOrm mDb;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        ToastUtils.register(this);
        mDb = LiteOrm.newSingleInstance(this, DB_NAME);
        mDb.setDebugged(true);
    }
}
