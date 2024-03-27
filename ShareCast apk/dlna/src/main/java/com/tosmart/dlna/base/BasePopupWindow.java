package com.tosmart.dlna.base;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by xxx on 2019/2/21.
 */
public abstract class BasePopupWindow<T extends ViewDataBinding> extends PopupWindow {

    protected Context mContext = null;
    protected T mViewDataBinding = null;
    protected View mContentView;
    private float mStartAlpha = 1.0f;;
    private float mEndAlpha = 1.0f;
    private long mDuration = 300;
    private static final String TAG = BasePopupWindow.class.getSimpleName();

    public BasePopupWindow(Context context, boolean focusable) {
        this.mContext = context;
        mViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), getContentViewId(), null, false);
        mContentView = mViewDataBinding.getRoot();
        setContentView(mContentView);
        if (getWidthId() > 0) {
            setWidth(context.getResources().getDimensionPixelOffset(getWidthId()));
        } else if (getWidthId() == MATCH_PARENT) {
            setWidth(MATCH_PARENT);
        } else {
            setWidth(WRAP_CONTENT);
        }

        if (getHeightId() > 0) {
            setHeight(context.getResources().getDimensionPixelOffset(getHeightId()));
        } else if (getHeightId() == MATCH_PARENT) {
            setHeight(MATCH_PARENT);
        } else {
            setHeight(WRAP_CONTENT);
        }
        setFocusable(focusable);
        setBackgroundDrawable(new BitmapDrawable());
        mContentView.setFocusable(true);
    }


    public void setWindowSize(int width, int height) {
        setContentView(mContentView);
        setWidth(width);
        setHeight(height);
    }

    protected abstract boolean isBackgroundAlpha();

    public void show() {
        show(mContentView, Gravity.CENTER, 0, 0);
    }

    public void show(View view, int gravity, int offsetX, int offsetY) {
        if (isBackgroundAlpha()) {
            setBackgroundAlpha(1.0f, 0.5f, mDuration);
        }
        showAtLocation(view, gravity, offsetX, offsetY);
    }

    @Override
    public void dismiss() {
        if (isBackgroundAlpha()) {
            setBackgroundAlpha(mEndAlpha, 1.0f, mDuration);
        }
        super.dismiss();
    }

    private void updateBackgroundAlpha(float alpha) {
        if (mContext instanceof Activity) {
            WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
            lp.alpha = alpha;
            ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            ((Activity) mContext).getWindow().setAttributes(lp);
        }
    }

    private void setBackgroundAlpha(float startAlpha, float endAlpha, long duration) {
        mStartAlpha = startAlpha;
        mEndAlpha = endAlpha;
        mDuration = duration;
        ValueAnimator animator = ValueAnimator.ofFloat(startAlpha, endAlpha);
        animator.setDuration(duration);

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            updateBackgroundAlpha(animatedValue);
        });

        animator.start();
    }

    protected abstract int getHeightId();

    protected abstract int getWidthId();

    protected abstract int getContentViewId();
}
