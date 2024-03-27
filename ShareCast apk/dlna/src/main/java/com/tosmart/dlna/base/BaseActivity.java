package com.tosmart.dlna.base;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tosmart.dlna.util.StatusBarUtil;

/**
 * Created by xxx on 2019/2/21.
 */
public abstract class BaseActivity<T extends ViewDataBinding> extends AppCompatActivity {

    protected Context mContext = null;
    protected T mViewDataBinding = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.fullScreen(this);
        StatusBarUtil.setStatusTextColor(true,this);
        mViewDataBinding = DataBindingUtil.setContentView(this, getContentViewId());
        mContext = this;
        init();
    }

    /**
     * get content view id
     *
     * @return
     */
    @LayoutRes
    protected abstract int getContentViewId();

    /**
     * init data and view
     */
    protected abstract void init();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
