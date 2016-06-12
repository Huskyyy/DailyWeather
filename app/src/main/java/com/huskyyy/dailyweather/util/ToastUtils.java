package com.huskyyy.dailyweather.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Wang on 2016/5/11.
 */
public class ToastUtils {

    public static Context mContext;

    private ToastUtils() {
    }

    public static void register(Context context){
        mContext = context;
    }

    public static void check(){
        if(mContext == null){
            throw new NullPointerException(
                    "Must initial call ToastUtils.register(Context context) in your " +
                            "<? " +
                            "extends Application class>");
        }
    }

    public static void showShort(int resId) {
        check();
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }


    public static void showShort(String message) {
        check();
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }


    public static void showLong(int resId) {
        check();
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }


    public static void showLong(String message) {
        check();
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }
}
