package com.huskyyy.dailyweather.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huskyyy.dailyweather.R;
import com.huskyyy.dailyweather.model.CityInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wang on 2016/6/5.
 */
public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> {

    private List<CityInfo> mList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public CityListAdapter(Context context, List<CityInfo> list){
        mList = list;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_city, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CityInfo info = mList.get(position);
        holder.cityInfo = info;
        if(info.level.equals("3")){
            holder.nameTextView.setText(info.name);
            holder.inTextView.setVisibility(View.VISIBLE);
            String region = info.provName;
            if(!info.provName.equals(info.cityName))
                region += "  " + info.cityName;
            holder.regionTextView.setText(region);
            holder.regionTextView.setVisibility(View.VISIBLE);
        }else{
            if(info.cityName.equals(info.provName)){
                holder.nameTextView.setText(info.cityName);
                holder.inTextView.setVisibility(View.GONE);
                holder.regionTextView.setVisibility(View.GONE);
            }else{
                holder.nameTextView.setText(info.cityName);
                holder.inTextView.setVisibility(View.VISIBLE);
                holder.regionTextView.setText(info.provName);
                holder.regionTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.tv_name) TextView nameTextView;
        @BindView(R.id.tv_region_name) TextView regionTextView;
        @BindView(R.id.tv_in) TextView inTextView;
        View itemView;
        CityInfo cityInfo;

        public ViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if(mOnItemClickListener != null)
                mOnItemClickListener.onClick(v, cityInfo);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(View v, CityInfo w);
    }
}
