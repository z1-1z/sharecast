package com.intelligent.share.widget;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.View;

import com.intelligent.share.R;
import com.intelligent.share.databinding.CommonTipWindowBinding;
import com.tosmart.dlna.base.BasePopupWindow;

/**
 * @author xxx
 * @date 2024/1/25
 */
public class CommonTipWindow extends BasePopupWindow<CommonTipWindowBinding> {


    public CommonTipWindow(Context context, boolean focusable) {
        super(context, focusable);
    }

    @Override
    protected boolean isBackgroundAlpha() {
        return true;
    }

    @Override
    protected int getHeightId() {
        return WRAP_CONTENT;
    }

    @Override
    protected int getWidthId() {
        return R.dimen.common_tip_window_width;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.common_tip_window;
    }

    public View getRightBtn() {
        return mViewDataBinding.tvConfirm;
    }

    public View getLeftBtn() {
        return mViewDataBinding.tvCancel;
    }

}
