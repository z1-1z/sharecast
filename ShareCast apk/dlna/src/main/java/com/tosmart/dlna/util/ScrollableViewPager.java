package com.tosmart.dlna.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * @author pc-001
 */
public class ScrollableViewPager extends ViewPager {
    private boolean mScrollable = true;

    public ScrollableViewPager(Context context) {
        super(context);
    }

    public ScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return mScrollable && super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return mScrollable && super.onInterceptTouchEvent(arg0);
    }

    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }
}