package com.huskyyy.dailyweather.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huskyyy.dailyweather.R;
import com.huskyyy.dailyweather.model.CityInfo;
import com.huskyyy.dailyweather.model.CityInfoWrapper;
import com.huskyyy.dailyweather.model.CityResponse;
import com.huskyyy.dailyweather.net.HttpHelper;
import com.huskyyy.dailyweather.ui.DividerItemDecoration;
import com.huskyyy.dailyweather.ui.adapter.CityListAdapter;
import com.huskyyy.dailyweather.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CityActivity extends BaseActivity {

    public static final int RESULT_OK = 1;
    public static final int RESULT_FAILURE = 0;
    public static final String CITY_ID = "com.huskyyy.dailyweather.ui.activity.ID";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_city) RecyclerView cityListView;

    private CityListAdapter cityListAdapter;
    private List<CityInfo> cityInfoList;

    private Unbinder unbinder;

    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        cityInfoList = new ArrayList<>();
        setupRecycleView();
        setupSwipeRefreshLayout();
    }

    @Override
    protected void onNewIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            doSearch(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.requestFocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(unbinder != null)
            unbinder.unbind();
    }

    private void setupRecycleView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        cityListView.setLayoutManager(layoutManager);
        cityListAdapter = new CityListAdapter(this, cityInfoList);
        cityListAdapter.setOnItemClickListener(getOnItemClickListener());
        cityListView.setAdapter(cityListAdapter);
        cityListView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    private CityListAdapter.OnItemClickListener getOnItemClickListener(){
        return new CityListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, CityInfo w) {
                Intent res = new Intent();
                res.putExtra(CITY_ID, w.cityId);
                setResult(RESULT_OK, res);
                finish();
            }
        };
    }

    private void setupSwipeRefreshLayout(){
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRefreshProgress1,
                R.color.colorRefreshProgress2, R.color.colorRefreshProgress3);
    }

    private void doSearch(String area){

        startRefreshing();

        if(!HttpHelper.isNetworkAvailable(this)) {
            ToastUtils.showShort(R.string.network_unavailable);
            stopRefreshing();
            return;
        }

        Observer<List<CityInfo>> observer = new Observer<List<CityInfo>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort(R.string.update_failure);
            }

            @Override
            public void onNext(List<CityInfo> cityInfos) {
                stopRefreshing();
                cityInfoList.clear();
                cityInfoList.addAll(cityInfos);
                cityListAdapter.notifyDataSetChanged();
                if(cityInfos.size() == 0){
                    ToastUtils.showShort(R.string.search_no_result);
                }
            }
        };

        Subscription subscription = HttpHelper.getCityApi()
                .getCityInfo(area)
                .map(new Func1<CityResponse, List<CityInfo>>() {
                    @Override
                    public List<CityInfo> call(CityResponse cityResponse) {
                        List<CityInfo> res = new ArrayList<CityInfo>();
                        for(CityInfoWrapper wrapper : cityResponse.results.cities){
                            res.add(wrapper.cityInfo);
                        }
                        return res;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        addSubscription(subscription);
    }

    private void startRefreshing(){
        refreshing = true;
        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void stopRefreshing(){
        refreshing = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(refreshing == false && swipeRefreshLayout != null){
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        }, 1000);
    }
}
