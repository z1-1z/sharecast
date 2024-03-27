package com.intelligent.share.base;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.intelligent.share.R;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;


/**
 * Created by xxx on 2019/2/21.
 */
public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {
    private static final String TAG = "BaseFragment";
    private ZLoadingDialog mWaitDialog;

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
        Log.i(TAG, "[xxx] onCreateView:" + getClass().getSimpleName() + "--" + mViewDataBinding );
        if (mViewDataBinding == null) {
            mViewDataBinding = DataBindingUtil.inflate(inflater, getContentViewId(), null, false);
            init();
        }
        return mViewDataBinding.getRoot();
    }

    public ZLoadingDialog showWaitDialog() {
        return showWaitDialog(R.string.loading_tip);
    }

    @SuppressWarnings("WeakerAccess")
    public ZLoadingDialog showWaitDialog(int resId) {
        return showWaitDialog(getString(resId));
    }

    @SuppressWarnings("WeakerAccess")
    public ZLoadingDialog showWaitDialog(String message) {
        if (mWaitDialog == null) {
            mWaitDialog = new ZLoadingDialog(getContext());
            mWaitDialog.setLoadingBuilder(Z_TYPE.CIRCLE_CLOCK)
                    .setLoadingColor(Color.BLACK);
        }
        if (mWaitDialog != null) {
            mWaitDialog.setHintText(message);
            mWaitDialog.setCanceledOnTouchOutside(false);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }
        return mWaitDialog;
    }

    public void hideWaitDialog() {
        if (mWaitDialog != null) {
            try {
                mWaitDialog.dismiss();
                mWaitDialog = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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

    public boolean onBackPressed() {
        return false;
    }

}
