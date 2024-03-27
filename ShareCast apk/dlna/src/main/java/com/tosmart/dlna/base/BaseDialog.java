package com.tosmart.dlna.base;

import android.app.Dialog;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.tosmart.dlna.R;


/**
 * Created by xxx on 2019/2/21.
 */
public abstract class BaseDialog<T extends ViewDataBinding> extends Dialog {

    protected Context mContext = null;
    protected T mViewDataBinding = null;

    public BaseDialog(Context context) {
        this(context, R.style.dialog);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
        mViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), getContentViewId(), null, false);
        setContentView(mViewDataBinding.getRoot());
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            if (getWidthId() != 0) {
                params.width = mContext.getResources().getDimensionPixelOffset(getWidthId());
            }
            if (getHeightId() != 0) {
                params.height = mContext.getResources().getDimensionPixelOffset(getHeightId());
            }
            getWindow().setAttributes(params);
        }
    }

    protected abstract int getHeightId();

    protected abstract int getWidthId();

    protected abstract void init();

    protected abstract int getContentViewId();

}
