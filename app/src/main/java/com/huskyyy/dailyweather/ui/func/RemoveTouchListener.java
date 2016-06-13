package com.huskyyy.dailyweather.ui.func;

import android.animation.Animator;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.huskyyy.dailyweather.ui.adapter.WeatherListAdapter;

/**
 * Created by Wang on 2016/6/10.
 */
public class RemoveTouchListener implements RecyclerView.OnItemTouchListener {

    private float xDown, yDown, xMove, yMove;
    private boolean xMoveFlag, yMoveFlag;
    private static final int translationDuration = 120;
    private View view;
    private VelocityTracker velocityTracker;
    private float defaultFlingVelocity;
    private float currentFlingVelocity;

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        // 防止在动画结束之前再次触发
        if(rv.isComputingLayout() || rv.isAnimating()) {
            Log.e("isComputingLayout",""+rv.isComputingLayout());
            Log.e("isAnimating",""+rv.isAnimating());
            return false;
        }
        int action = e.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                xDown = e.getX();
                yDown = e.getY();
                xMove = xDown;
                yMove = yDown;
                view = rv.findChildViewUnder(xDown, yDown);
                if(view != null) {
                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(e);
                    defaultFlingVelocity = ViewConfiguration.get(rv.getContext())
                            .getScaledMinimumFlingVelocity();
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                xMove = e.getX();
                yMove = e.getY();
                if(xMove == xDown && yMove == yDown || view == null || velocityTracker == null)
                    return false;
                // 第一次上下移动则屏蔽右移
                if(Math.abs(yMove - yDown) >= xMove - xDown || yMoveFlag) {
                    yMoveFlag = true;
                    return false;
                }else {
                    velocityTracker.addMovement(e);
                    xMoveFlag = true;
                    // 防止触发SwipeRefreshLayout的下拉刷新
                    if(rv.getParent().getClass() == SwipeRefreshLayout.class){
                        ((SwipeRefreshLayout)rv.getParent()).setEnabled(false);
                    }
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if(velocityTracker != null){
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                xMoveFlag = false;
                yMoveFlag = false;
                return false;
        }
        if(xMoveFlag)
            return true;
        else
            return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        xMove = e.getX();
        yMove = e.getY();
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float dis = Math.max(xMove - xDown, 0);
                view.setTranslationX(dis);
                view.setAlpha(1 - dis / view.getWidth());
                velocityTracker.addMovement(e);
                velocityTracker.computeCurrentVelocity(1000);
                currentFlingVelocity = velocityTracker.getXVelocity();
                break;
            case MotionEvent.ACTION_UP:
                xMoveFlag = false;
                yMoveFlag = false;
                // 恢复SwipeRefreshLayout
                if(rv.getParent().getClass() == SwipeRefreshLayout.class){
                    ((SwipeRefreshLayout)rv.getParent()).setEnabled(true);
                }
                velocityTracker.recycle();
                velocityTracker = null;
                deleteItem(rv,view);
                break;
            //在swipeRefreshLayout中，由于下拉刷新的缘故会导致该事件
//            case MotionEvent.ACTION_CANCEL:
            default:
                xMoveFlag = false;
                yMoveFlag = false;
                if(rv.getParent().getClass() == SwipeRefreshLayout.class){
                    ((SwipeRefreshLayout)rv.getParent()).setEnabled(false);
                }
                velocityTracker.recycle();
                velocityTracker = null;
                recoverView(view);
        }

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void deleteItem(final RecyclerView rv, final View v){
        int width = v.getWidth();
        final int pos = rv.getChildAdapterPosition(v);
        //第一个Item为定位地点，不可删除
        if((currentFlingVelocity >= defaultFlingVelocity || xMove - xDown > width / 2) && pos != 0) {

            long duration = translationDuration;
            if(currentFlingVelocity > 0){
                duration = Math.min(duration,
                        (long)((width - (xMove - xDown)) / currentFlingVelocity * 1000));
            }

            v.animate()
                    .setDuration(duration)
                    .translationX(width)
                    .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((WeatherListAdapter)rv.getAdapter()).removeItem(pos);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }else{
            recoverView(v);
        }
    }

    public void recoverView(View v){
        v.animate()
                .alpha(1)
                .translationX(0).
                setDuration(translationDuration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
}
