package com.huskyyy.dailyweather.ui.func;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.huskyyy.dailyweather.ui.adapter.WeatherListAdapter;

/**
 * Created by Wang on 2016/6/20.
 */
public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final float DRAG_TRANSLATION_Z = 20f;
    private final int DRAG_TRANSLATION_Z_DURATION = 100;
    private WeatherListAdapter mAdapter;

    public MyItemTouchHelperCallback(WeatherListAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if(viewHolder.getAdapterPosition() == 0)
            return makeMovementFlags(0, 0);
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if(source.getAdapterPosition() == 0 || target.getAdapterPosition() == 0)
            return false;

        mAdapter.dragSort(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
        if(holder.getAdapterPosition() == 0)
            return;
        mAdapter.removeItem(holder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {

        if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder.itemView.animate()
                    .translationZ(DRAG_TRANSLATION_Z)
                    .setDuration(DRAG_TRANSLATION_Z_DURATION)
                    .start();
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        viewHolder.itemView.animate()
                .translationZ(0)
                .setDuration(DRAG_TRANSLATION_Z_DURATION)
                .start();
        super.clearView(recyclerView, viewHolder);
    }




}
