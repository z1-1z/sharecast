package com.tosmart.dlna.base;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
//import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by xxx on 2019/2/21.
 */
public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {

    protected T mViewDataBinding = null;
    protected Context mContext = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mViewDataBinding == null) {
            mViewDataBinding = DataBindingUtil.inflate(inflater, getContentViewId(), null, false);
            init();
        }
        return mViewDataBinding.getRoot();
    }

    /**
     * init data and view
     */
    protected abstract void init();

    /**
     * get content view id
     *
     * @return
     */
    @LayoutRes
    protected abstract int getContentViewId();

    /**
     * set top icon
     */
    public abstract void setTopIconAndTopTitle();

    public boolean onBackPressed() {
        return false;
    }
}
