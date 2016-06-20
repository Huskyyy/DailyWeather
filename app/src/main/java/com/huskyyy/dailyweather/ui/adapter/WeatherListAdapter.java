package com.huskyyy.dailyweather.ui.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huskyyy.dailyweather.R;
import com.huskyyy.dailyweather.model.WeatherInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wang on 2016/5/29.
 */
public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private List<WeatherInfo> mList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public WeatherListAdapter(Context context, List<WeatherInfo> list){
        mList = list;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.list_item_weather, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherInfo w = mList.get(position);
        holder.weatherInfo = w;
        holder.temperatureTextView.setText(w.nowState.temperature + "°");
        holder.cityTextView.setText(w.cityInfo.name);
        holder.weatherTextView.setText(w.nowState.weather);
        Glide.with(mContext)
                .load(w.nowState.weatherPic)
                .fitCenter()
                .into(holder.weatherImage);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.tv_temperature) TextView temperatureTextView;
        @BindView(R.id.tv_city) TextView cityTextView;
        @BindView(R.id.tv_weather) TextView weatherTextView;
        @BindView(R.id.iv_weather) ImageView weatherImage;
        CardView cardView;
        WeatherInfo weatherInfo;

        public ViewHolder(CardView itemView){
            super(itemView);
            cardView = itemView;
            ButterKnife.bind(this, itemView);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if(mOnItemClickListener != null)
                mOnItemClickListener.onClick(v, weatherInfo);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(View v, WeatherInfo w);
    }

    public void removeItem(int pos){
        mList.remove(pos);
        for(int i = pos; i < mList.size(); i++){
            mList.get(i).orderNum--;
        }
        notifyItemRemoved(pos);
    }

    public void dragSort(int fromPos, int toPos){

        if(fromPos == toPos){
            return;
        }

        WeatherInfo info = mList.get(fromPos);
        mList.remove(fromPos);
        mList.add(toPos, info);

        int min = Math.min(fromPos, toPos), max = Math.max(fromPos, toPos);
        for(int i = min; i  <= max; i++) {
            mList.get(i).orderNum = i;
        }

        notifyItemMoved(fromPos, toPos);
    }
}
