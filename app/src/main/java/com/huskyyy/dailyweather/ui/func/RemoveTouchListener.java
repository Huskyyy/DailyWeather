package com.huskyyy.dailyweather.ui.func;

import android.animation.Animator;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
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

    private final int STATE_IDLE = 0;
    private final int STATE_SCROLL = 1;
    private final int STATE_SWIPE = 2;
    private final int STATE_LONG_PRESS = 3;
    private final int STATE_DRAG = 4;
    private final int STATE_ANIMATING = 5;
    private final int STATE_ANIMATING_HALF = 6;

    private final int TRANSLATION_DURATION = 120;

    private int touchSlop;

    private int state;

    private float xDown, yDown, xMove, yMove;

    private RecyclerView recyclerView;
    private View view;
    // 假设每个子View的高度都相同
    private int height;
    private int pos = -1;
    // 记录拖动某个view时与其交换的view的位置
    private int swapPos = -1;

    private VelocityTracker velocityTracker;
    private float defaultFlingVelocity;
    private float currentFlingVelocity;

    private final int MESSAGE_LONG_PRESS = 1;

    private final MyHandler handler = new MyHandler();

    public RemoveTouchListener(RecyclerView rv){
        recyclerView = rv;
        ViewConfiguration vc = ViewConfiguration.get(rv.getContext());
        defaultFlingVelocity = vc.getScaledMinimumFlingVelocity();
        touchSlop = vc.getScaledTouchSlop();
        state = STATE_IDLE;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        // 防止在动画结束之前再次触发
        if(rv.isComputingLayout() || rv.isAnimating()) {
            return false;
        }

        if(state == STATE_ANIMATING){
            return false;
        }

        int action = e.getAction();
        // 如果按着不动（很少发生），则action up事件不会继续传到onTouchEvent中
        if(view != null
                && (state == STATE_LONG_PRESS || state == STATE_SWIPE || state == STATE_DRAG)) {
            if(action == MotionEvent.ACTION_UP){
                if(velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                recoverView(view);
                // 这里返回true是为了避免触发点击事件
                return true;
            }else{
                return true;
            }
        }

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

                    Message msg = new Message();
                    msg.what = MESSAGE_LONG_PRESS;
                    handler.sendMessageDelayed(msg, 500);

                    pos = rv.getChildAdapterPosition(view);
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                    // 高度需要考虑上下margin
                    height = view.getHeight() + lp.topMargin + lp.bottomMargin;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                xMove = e.getX();
                yMove = e.getY();
                // 如果位移超过slop，则认定state为移动而非点击或者长按。
                if(xMove - xDown > touchSlop || Math.abs(yMove - yDown) > touchSlop){
                    handler.removeMessages(MESSAGE_LONG_PRESS);
                }
                if(view == null || velocityTracker == null)
                    return false;
                // 第一次上下移动则屏蔽右移
                if((Math.abs(yMove - yDown) >= xMove - xDown && Math.abs(yMove - yDown) > touchSlop)
                        || state == STATE_SCROLL) {
                    state = STATE_SCROLL;
                    return false;
                }else if(xMove - xDown > Math.abs(yMove - yDown) && xMove - xDown > touchSlop){
                    velocityTracker.addMovement(e);
                    state = STATE_SWIPE;
                    disableSwipeRefresh();
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                handler.removeMessages(MESSAGE_LONG_PRESS);
                if(velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                state = STATE_IDLE;
                return false;
            default:;

        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        xMove = e.getX();
        yMove = e.getY();
        int action = e.getAction();

        if(state == STATE_SWIPE) { // 滑动删除的逻辑
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    float dis = Math.max(xMove - xDown, 0);
                    view.setTranslationX(dis);
                    view.setAlpha(1 - dis / view.getWidth());
                    velocityTracker.addMovement(e);
                    velocityTracker.computeCurrentVelocity(500);
                    currentFlingVelocity = velocityTracker.getXVelocity();
                    break;
                case MotionEvent.ACTION_UP:
                    enableSwipeRefresh();
                    velocityTracker.recycle();
                    velocityTracker = null;
                    deleteOrRecoverItem(rv, view, pos);
                    break;
                // 在swipeRefreshLayout中，由于下拉刷新的缘故会导致该事件
//            case MotionEvent.ACTION_CANCEL:
                default:
                    enableSwipeRefresh();
                    velocityTracker.recycle();
                    velocityTracker = null;
                    recoverView(view);
                    break;
            }
        }else if(state == STATE_LONG_PRESS || state == STATE_DRAG) { // 拖动排序的逻辑
            View swapView = null;
            if(swapPos != -1)
                swapView = rv.findViewHolderForAdapterPosition(swapPos).itemView;

            switch(action) {
                case MotionEvent.ACTION_MOVE:
                    float dis = yMove - yDown;
                    if(state == STATE_LONG_PRESS || state == STATE_DRAG){
                        // 恢复到原位时，将上次的交换子View恢复原位
                        if(dis == 0){
                            if(swapPos != -1)
                                swapView.setTranslationY(0);
                            return;
                        }
                        // 对于首位的两个view，设定其边界
                        // 由于第一个子View为定位用，故特殊对待
                        if(pos == rv.getAdapter().getItemCount() - 1 && dis > 0
                                || pos == 1 && dis < 0
                                || pos == 0){
                            return;
                        }
                        float offset = 0;
                        int currSwapPos;
                        if(dis > 0) { // 在原位置的下方
                            currSwapPos = pos + (int) (dis + height) / height;
                            // 如果当前位置超出了最后一个位置，则设置被拖动子View和交换子View的translationY
                            if(currSwapPos >= rv.getAdapter().getItemCount()){
                                offset = height + swapView.getTranslationY();
                                swapView.setTranslationY(-height);
                                view.setTranslationY(view.getTranslationY()+offset);
                                return;
                            }
                            offset = dis - (currSwapPos - pos - 1) * height;
                            // 当前交换的子View和上一次交换的子View不同，即越过边界的情况
                            // 需要把上次交换的子View的位置设置好（有可能离正常的位置差一点）
                            if(currSwapPos - swapPos == 2 && swapPos != -1){ // 先往上，再往下的情况(swapPos = pos - 1, currSwapPos = pos + 1)
                                swapView.setTranslationY(0);
                            }else if(currSwapPos > swapPos && swapPos != -1){ // 往下拖动
                                swapView.setTranslationY(-height);
                            }else if(currSwapPos < swapPos && swapPos != -1){ // 往上拖动
                                swapView.setTranslationY(0);
                            }
                        }else { // 在原位置的上方
                            currSwapPos = pos + (int) (dis - height) / height;
                            // 如果当前位置超出了第一个位置，则设置被拖动子View和交换子View的translationY
                            if(currSwapPos <= 0){
                                offset = height - swapView.getTranslationY();
                                swapView.setTranslationY(height);
                                view.setTranslationY(view.getTranslationY() - offset);
                                return;
                            }
                            offset = dis - (currSwapPos - pos + 1) * height;
                            // 当前交换的子View和上一次交换的子View不同，即越过边界的情况
                            // 需要把上次交换的子View的位置设置好（有可能离正常的位置差一点）
                            if(currSwapPos - swapPos == -2 && swapPos != -1){ // 先往下，再往上的情况(swapPos = pos + 1, currSwapPos = pos - 1)
                                swapView.setTranslationY(0);
                            }else if(currSwapPos > swapPos && swapPos != -1){ // 往下拖动
                                swapView.setTranslationY(0);
                            }else if(currSwapPos < swapPos && swapPos != -1){ // 往上拖动
                                swapView.setTranslationY(height);
                            }
                        }
                        swapPos = currSwapPos;
                        rv.findViewHolderForAdapterPosition(swapPos).itemView.setTranslationY(-offset);
                        view.setTranslationY(dis);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    enableSwipeRefresh();
                    if(swapPos == -1 || yMove == yDown){
                        recoverView(view);
                        return;
                    }
                    float offset = swapView.getTranslationY();
                    if(yMove - yDown > 0){ // 偏下
                        if(offset < -height / 2){ // 偏过中心线，交换到swapPos
                            swapView(rv,
                                    view, swapView,
                                    view.getTranslationY(), view.getTranslationY() + height + offset,
                                    swapView.getTranslationY(), -height,
                                    pos, swapPos);
                        }else { // 否则交换到(swapPos - 1)
                            swapView(rv,
                                    view, swapView,
                                    view.getTranslationY(), view.getTranslationY() + offset,
                                    swapView.getTranslationY(), 0,
                                    pos, swapPos - 1);
                        }
                    }else{ // 偏上
                        if(offset > height / 2){ // 偏过中心线，交换到swapPos
                            swapView(rv,
                                    view, swapView,
                                    view.getTranslationY(), view.getTranslationY() - (height - offset),
                                    swapView.getTranslationY(), height,
                                    pos, swapPos);
                        }else{ // 否则交换到(swapPos + 1)
                            swapView(rv,
                                    view, swapView,
                                    view.getTranslationY(), view.getTranslationY() + offset,
                                    swapView.getTranslationY(), 0,
                                    pos, swapPos + 1);
                        }
                    }
                    swapPos = -1;
                    break;
                default:;
            }
        }

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

    public void deleteOrRecoverItem(final RecyclerView rv, final View v, final int pos){
        int width = v.getWidth();
        //第一个Item为定位地点，不可删除
        if((currentFlingVelocity >= defaultFlingVelocity || xMove - xDown > width / 2) && pos != 0) {

            long duration = TRANSLATION_DURATION;
            if(currentFlingVelocity > 0){
                duration = Math.min(duration,
                        (long)((width - (xMove - xDown)) / currentFlingVelocity * 1000));
            }
            state = STATE_ANIMATING;
            v.animate()
                    .setDuration(duration)
                    .translationX(width)
                    .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    ((WeatherListAdapter)rv.getAdapter()).removeItem(pos);
                    state = STATE_IDLE;
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            }).start();
        }else{
            recoverView(v);
        }
    }

    private void recoverView(View v){
        state = STATE_ANIMATING;
        v.animate()
                .alpha(1)
                .translationX(0)
                .translationY(0)
                .translationZ(0)
                .setDuration(TRANSLATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        state = STATE_IDLE;
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void swapView(final RecyclerView rv,
                          View v1, View v2,
                          float translationY1from, float translationY1to,
                          float translationY2from, float translationY2to,
                          final int fromPos, final int toPos){
        state = STATE_ANIMATING;
        v1.animate()
                .translationY(translationY1to)
                .translationZ(0)
                .setDuration(TRANSLATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (state == STATE_ANIMATING){
                            state = STATE_ANIMATING_HALF;
                        }else{
                            ((WeatherListAdapter)rv.getAdapter()).dragSort(fromPos, toPos);
                            state = STATE_IDLE;
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
        v2.animate()
                .translationY(translationY2to)
                .setDuration(TRANSLATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (state == STATE_ANIMATING){
                            state = STATE_ANIMATING_HALF;
                        }else{
                            ((WeatherListAdapter)rv.getAdapter()).dragSort(fromPos, toPos);
                            state = STATE_IDLE;
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();

        // 用AnimatorSet动画效果会卡顿
//        AnimatorSet set = new AnimatorSet();
//        ValueAnimator translation1 = ObjectAnimator
//                .ofFloat(v1, "translationY", translationY1from, translationY1to)
//                .ofFloat(v1, "translationZ", v1.getTranslationZ(), 0);
//        translation1.setDuration(TRANSLATION_DURATION)
//                .setInterpolator(new AccelerateDecelerateInterpolator());
//        ValueAnimator translation2 = ObjectAnimator
//                .ofFloat(v2, "translationY", translationY2from, translationY2to);
//        translation2.setDuration(TRANSLATION_DURATION)
//                .setInterpolator(new AccelerateDecelerateInterpolator());
//        set.play(translation1)
//                .with(translation2);
//        set.setDuration(TRANSLATION_DURATION)
//                .setInterpolator(new AccelerateDecelerateInterpolator());
//        set.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {}
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                ((WeatherListAdapter)rv.getAdapter()).dragSort(fromPos, toPos);
//                state = STATE_IDLE;
//            }
//            @Override
//            public void onAnimationCancel(Animator animation) {}
//            @Override
//            public void onAnimationRepeat(Animator animation) {}
//        });
//        set.start();
    }

    // 防止触发SwipeRefreshLayout的下拉刷新
    private void disableSwipeRefresh(){
        if(recyclerView.getParent().getClass() == SwipeRefreshLayout.class
                && ((SwipeRefreshLayout)recyclerView.getParent()).isEnabled()){
            ((SwipeRefreshLayout)recyclerView.getParent()).setEnabled(false);
        }
    }

    // 恢复SwipeRefreshLayout
    private void enableSwipeRefresh(){
        if (recyclerView.getParent().getClass() == SwipeRefreshLayout.class) {
            ((SwipeRefreshLayout) recyclerView.getParent()).setEnabled(true);
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_LONG_PRESS:
                    state = STATE_LONG_PRESS;
                    disableSwipeRefresh();
                    if(view != null) {
                        view.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .translationZ(20)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {}
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        state = STATE_DRAG;
                                    }
                                    @Override
                                    public void onAnimationCancel(Animator animation) {}
                                    @Override
                                    public void onAnimationRepeat(Animator animation) {}
                                })
                                .start();
                    }
                    break;
                default:
                    break;

            }
            super.handleMessage(msg);
        }
    }
}
