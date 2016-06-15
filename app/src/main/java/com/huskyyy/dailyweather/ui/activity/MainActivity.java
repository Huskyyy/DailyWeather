package com.huskyyy.dailyweather.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huskyyy.dailyweather.App;
import com.huskyyy.dailyweather.R;
import com.huskyyy.dailyweather.model.WeatherResponse;
import com.huskyyy.dailyweather.model.WeatherInfo;
import com.huskyyy.dailyweather.net.HttpHelper;
import com.huskyyy.dailyweather.ui.adapter.WeatherListAdapter;
import com.huskyyy.dailyweather.ui.func.MyAnimator;
import com.huskyyy.dailyweather.ui.func.RemoveTouchListener;
import com.huskyyy.dailyweather.util.ToastUtils;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    public static final int REQUEST_CITY_ID = 0;
    public static final String WEATHER_INFO = "com.huskyyy.dailyweather.ui.activity.INFO";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_weather) RecyclerView weatherListView;

    private WeatherListAdapter weatherListAdapter;
    private List<WeatherInfo> weatherInfoList;

    private Unbinder unbinder;

    private boolean fromAddCityActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        weatherInfoList = new ArrayList<>();
        setupRecyclerView();
        setupSwipeRefreshLayout();

        QueryBuilder queryBuilder = new QueryBuilder(WeatherInfo.class);
        queryBuilder.appendOrderAscBy("orderNum");
        weatherInfoList.addAll(App.mDb.query(queryBuilder));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(fromAddCityActivity == false){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData();
                }
            }, 400);
        }else{
            fromAddCityActivity = false;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.add_city){
            Intent intent = new Intent(this, CityActivity.class);
            startActivityForResult(intent, REQUEST_CITY_ID);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        fromAddCityActivity = true;
        if(requestCode == REQUEST_CITY_ID){
            if(resultCode == CityActivity.RESULT_OK){
                addCityWeather(data.getStringExtra(CityActivity.CITY_ID));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(unbinder != null)
            unbinder.unbind();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        weatherListView.setLayoutManager(layoutManager);
        weatherListAdapter = new WeatherListAdapter(this, weatherInfoList, weatherListView);
        weatherListAdapter.setOnItemClickListener(getOnItemClickListener());
        weatherListView.setAdapter(weatherListAdapter);
        weatherListView.setItemAnimator(new MyAnimator());
        weatherListView.addOnItemTouchListener(new RemoveTouchListener(weatherListView));
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRefreshProgress1,
                R.color.colorRefreshProgress2, R.color.colorRefreshProgress3);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadData();
                    }
                });
    }

    private WeatherListAdapter.OnItemClickListener getOnItemClickListener(){
        return new WeatherListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, WeatherInfo w) {
                Intent intent = new Intent(MainActivity.this, WeatherInfoActivity.class);
                intent.putExtra(WEATHER_INFO, w);
                startActivity(intent);
            }
        };
    }

    // 初始化当前位置天气信息
    private void initCurrentPositionWeather() {

        startRefreshing();

        Observer<WeatherInfo> observer = new Observer<WeatherInfo>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                stopRefreshing();
                ToastUtils.showShort(R.string.update_failure);
            }

            @Override
            public void onNext(WeatherInfo weatherInfo) {
                weatherInfoList.add(weatherInfo);
                stopRefreshing();
                weatherListAdapter.notifyDataSetChanged();
            }
        };

        Subscription subscription = HttpHelper.getWeatherApi()
                .getWeatherByIp()
                .map(new Func1<WeatherResponse, WeatherInfo>() {
                    @Override
                    public WeatherInfo call(WeatherResponse weatherResponse) {
                        WeatherInfo res = weatherResponse.result;
                        res.orderNum = 0;
                        return res;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        addSubscription(subscription);
    }

    private void loadData() {

        if(weatherInfoList.size() == 0){
            if(HttpHelper.isNetworkAvailable(this)){
                initCurrentPositionWeather();
            }else {
                ToastUtils.showShort(R.string.network_unavailable);
                stopRefreshing();
            }
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date currentDate = new Date(System.currentTimeMillis());
        final String date = formatter.format(currentDate);

        final List<WeatherInfo> list = new ArrayList<>();
        for(WeatherInfo info : weatherInfoList){
            if(!info.time.substring(0, 8).equals(date.substring(0, 8))
                    || !info.nowState.temperatureTime.substring(0, 2).equals(date.substring(8, 10)))
                list.add(info);
        }
        if(list.size() == 0){
            stopRefreshing();
            return;
        }
        if(!HttpHelper.isNetworkAvailable(this)){
            ToastUtils.showShort(R.string.network_unavailable);
            stopRefreshing();
            return;
        }
        Log.e("startRefresh","xx");
        startRefreshing();
        final int[] updateCount = new int[1];

        Observer<WeatherInfo> observer = new Observer<WeatherInfo>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                stopRefreshing();
                ToastUtils.showShort(R.string.update_failure);
                e.printStackTrace();
            }

            @Override
            public void onNext(WeatherInfo weatherInfo) {

                weatherInfoList.set(weatherInfo.orderNum, weatherInfo);
                updateCount[0]++;
                if(updateCount[0] == list.size()) { //此时已更新完所有天气信息
                    stopRefreshing();
                    Log.e("stopRefresh","xx");
                    weatherListAdapter.notifyDataSetChanged();
                }
            }
        };

        Observable.from(list)
                .flatMap(new Func1<WeatherInfo, Observable<WeatherInfo>>() {
                    @Override
                    public Observable<WeatherInfo> call(final WeatherInfo info) {
                        if(info.orderNum == 0)
                            return HttpHelper.getWeatherApi()
                                    .getWeatherByIp()
                                    .map(new Func1<WeatherResponse, WeatherInfo>() {
                                        @Override
                                        public WeatherInfo call(WeatherResponse weatherResponse) {
                                            WeatherInfo res = weatherResponse.result;
                                            res.orderNum = info.orderNum;
                                            return res;
                                        }
                                    });
                        else
                            return HttpHelper.getWeatherApi()
                                    .getWeatherById(info.cityInfo.cityId)
                                    .map(new Func1<WeatherResponse, WeatherInfo>() {
                                        @Override
                                        public WeatherInfo call(WeatherResponse weatherResponse) {
                                            WeatherInfo res = weatherResponse.result;
                                            res.orderNum = info.orderNum;
                                            return res;
                                        }
                                    });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }



    private void addCityWeather(String areaId){

        for(int i = 0; i < weatherInfoList.size(); i++){
            WeatherInfo info = weatherInfoList.get(i);
            if(info.cityInfo.cityId.equals(areaId)){
                weatherListView.smoothScrollToPosition(i);
                return;
            }
        }

        startRefreshing();
        Observer<WeatherInfo> observer = new Observer<WeatherInfo>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                stopRefreshing();
                ToastUtils.showShort(R.string.update_failure);
            }

            @Override
            public void onNext(WeatherInfo weatherInfo) {
                weatherInfoList.add(weatherInfo);
                stopRefreshing();
                weatherListAdapter.notifyItemInserted(weatherInfoList.size() - 1);
                weatherListView.smoothScrollToPosition(weatherInfoList.size() - 1);
            }
        };

        Subscription subscription = HttpHelper.getWeatherApi()
                .getWeatherById(areaId)
                .map(new Func1<WeatherResponse, WeatherInfo>() {
                    @Override
                    public WeatherInfo call(WeatherResponse weatherResponse) {
                        WeatherInfo res = weatherResponse.result;
                        res.orderNum = weatherInfoList.size();
                        return res;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        addSubscription(subscription);
    }

    private void saveData(){
        App.mDb.deleteAll(WeatherInfo.class);
        App.mDb.insert(weatherInfoList);
    }

    private void startRefreshing(){
        if(swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing(){
        if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

}
