package net.lucode.hackware.magicindicator.buildins.commonnavigator.titles;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IMeasurablePagerTitleView;


/**
 * 带文本的指示器标题
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class SimplePagerTitleView extends TextView implements IMeasurablePagerTitleView {
    protected int mSelectedColor;
    protected int mNormalColor;
    protected int mSelectedTextSize = -1;
    protected int mNormalTextSize = -1;
    protected Drawable mSelectedIcon;
    protected Drawable mNormalTextIcon;
    private Context mContext;
    private boolean mIsSelected;
    private OnSelectChange mOnSelectChange;

    public SimplePagerTitleView(Context context) {
        super(context, null);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        setGravity(Gravity.CENTER);
        int padding = UIUtil.dip2px(context, 10);
        setPadding(padding, 0, padding, 0);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        setTextColor(mSelectedColor);
        if (mSelectedTextSize != -1) {
            setTextSize(mSelectedTextSize);
        }
        if (mSelectedIcon != null) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mSelectedIcon, null, null, null);
        }
        mIsSelected = true;
        if (mOnSelectChange != null) {
            mOnSelectChange.onSelected();
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        setTextColor(mNormalColor);
        if (mNormalTextSize != -1) {
            setTextSize(mNormalTextSize);
        }
        if (mNormalTextIcon != null) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mNormalTextIcon, null, null, null);
        }
        mIsSelected = false;
        if (mOnSelectChange != null) {
            mOnSelectChange.onDeselected();
        }
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
    }

    @Override
    public int getContentLeft() {
        Rect bound = new Rect();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), bound);
        int contentWidth = bound.width();
        return getLeft() + getWidth() / 2 - contentWidth / 2;
    }

    @Override
    public int getContentTop() {
        Paint.FontMetrics metrics = getPaint().getFontMetrics();
        float contentHeight = metrics.bottom - metrics.top;
        return (int) (getHeight() / 2 - contentHeight / 2);
    }

    @Override
    public int getContentRight() {
        Rect bound = new Rect();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), bound);
        int contentWidth = bound.width();
        return getLeft() + getWidth() / 2 + contentWidth / 2;
    }

    @Override
    public int getContentBottom() {
        Paint.FontMetrics metrics = getPaint().getFontMetrics();
        float contentHeight = metrics.bottom - metrics.top;
        return (int) (getHeight() / 2 + contentHeight / 2);
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int normalColor) {
        mNormalColor = normalColor;
    }

    public void setSelectedTextSize(int selectedTextSize) {
        mSelectedTextSize = selectedTextSize;
    }

    public void setNormalTextSize(int normalTextSize) {
        mNormalTextSize = normalTextSize;
    }

    public void setSelectedIcon(Drawable selectedIcon) {
        mSelectedIcon = selectedIcon;
    }

    public void setNormalTextIcon(Drawable normalTextIcon) {
        mNormalTextIcon = normalTextIcon;
    }

    public void setPadding(int paddingDp) {
        int padding = UIUtil.dip2px(mContext, paddingDp);
        setPadding(padding, 0, padding, 0);
    }

    public void setPaddingPx(int padding) {
        setPadding(padding, 0, padding, 0);
    }

    public void setThemeTextColor(int normalColor, int selectedColor) {
        mSelectedColor = selectedColor;
        mNormalColor = normalColor;
        setTextColor(mIsSelected ? selectedColor : normalColor);
    }

    public void setOnSelectChange(OnSelectChange onSelectChange) {
        mOnSelectChange = onSelectChange;
    }

    public interface OnSelectChange {
        void onSelected();
        void onDeselected();
    }

}
