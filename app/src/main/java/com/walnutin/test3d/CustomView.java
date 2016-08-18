package com.walnutin.test3d;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by chenliu on 16/8/16.
 */
public class CustomView extends ViewGroup {


    private int mWidth;
    private int mHeight;
    private int mStartScreen;
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private float mDownY;
    private float resistance = 1.8f;//滑动阻力
    private int mCurScreen = 1;
    private IStereoListener iStereoListener;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        scrollTo(0, mStartScreen * mHeight);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childTop = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0, childTop, child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            childTop += child.getMeasuredHeight();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        float y = event.getY();


        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                if(!mScroller.isFinished()){
                    //点击时停止滚动
                    mScroller.setFinalY(mScroller.getCurrY());
                    mScroller.abortAnimation();
                    scrollTo(0,getScrollY());
                }
                mDownY = y;

                break;
            case MotionEvent.ACTION_MOVE:
                int yDelta = (int)(mDownY - y);     //疑问
                mDownY = y;
                if(mScroller.isFinished()){
                    recycleMove(yDelta);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }


        return super.onTouchEvent(event);
    }


    private void recycleMove(int delta) {
        delta = delta % mHeight;
        delta = (int) (delta / resistance);



        //不需要轮回
        if (Math.abs(delta) > mHeight / 4) {
            return;
        }



        scrollBy(0, delta);
        if (getScrollY() < 5 && mStartScreen != 0) {
            addPre();
            scrollBy(0, mHeight);
        } else if (getScrollY() > (getChildCount() - 1) * mHeight - 5) {
            addNext();
            scrollBy(0, -mHeight);
        }

    }




    public void addPre(){
        //获取当前第几屏
        mCurScreen = ((mCurScreen - 1) + getChildCount()) % getChildCount();

        int childCount = getChildCount();
        View view = getChildAt(childCount - 1);   //拿到最后一个孩子
        removeViewAt(childCount - 1);  //删除最后一个孩子
        addView(view, 0);           //放在第一个
        if (iStereoListener != null) {
            iStereoListener.toPre(mCurScreen);
        }

    }

    public void addNext() {
        mCurScreen = (mCurScreen + 1) % getChildCount();
        int childCount = getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, childCount - 1);
        if (iStereoListener != null) {
            iStereoListener.toNext(mCurScreen);
        }
    }

    public interface IStereoListener {
        //上滑一页时回调
        void toPre(int curScreen);

        //下滑一页时回调
        void toNext(int curScreen);
    }

    public void setiStereoListener(IStereoListener iStereoListener) {
        this.iStereoListener = iStereoListener;
    }



}
