package com.intelligent.share.binding;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.intelligent.share.tool.GlideUtil;

/**
 * Created by wujunzhang on 2018/5/7.
 */

public final class ViewBindingAdapter {
    private static final String TAG = "ViewBindingAdapter";
    private static final long INVALID_TIME = 30 * 60 * 1000L;

    @BindingAdapter({"width"})
    public static void setWidth(View view, int width) {
        view.getLayoutParams().width = width;
    }

    @BindingAdapter({"height"})
    public static void setHeight(View view, float height) {
        view.getLayoutParams().height = (int) height;
    }

    @BindingAdapter({"background"})
    public static void setBackground(ImageView view, int resourceId) {
        view.setBackgroundResource(resourceId);
    }

    @BindingAdapter({"icon"})
    public static void setIcon(ImageView view, int resourceId) {
        view.setImageResource(resourceId);
    }

    @BindingAdapter({"bitmap"})
    public static void setImageBitmap(ImageView view, Bitmap bitmap) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        }
    }

    @BindingAdapter({"setSelected"})
    public static void setSelected(TextView textView, boolean isSelected) {
        textView.setSelected(isSelected);
    }

    @BindingAdapter({"alpha"})
    public static void setAlpha(View view, int alpha) {
        view.getBackground().setAlpha(alpha);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLeftMargin(View view, int leftMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setRightMargin(View view, int rightMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setTopMargin(View view, int top) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setBottomMargin(View view, int bottomMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_margin")
    public static void setMargin(View view, int margin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_width")
    public static void setViewWidth(View view, int width) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (width == -1) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.width = width;
        }
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_height")
    public static void setViewHeight(View view, int height) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (height == -1) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.height = height;
        }
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setStartMargin(View view, int start) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart(start);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("relativelayout_below")
    public static void setLayoutBelow(View view, int id) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.addRule(RelativeLayout.BELOW, id);
        view.setLayoutParams(layoutParams);
    }


    @BindingAdapter("android:layout_marginEnd")
    public static void setEndMargin(View view, int end) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMarginEnd(end);
        view.setLayoutParams(layoutParams);
    }


    @BindingAdapter("android:drawablePadding")
    public static void setDrawablePadding(TextView view, int padding) {
        view.setCompoundDrawablePadding(padding);
    }


    @BindingAdapter("android:visibility")
    public static void setDrawablePadding(FrameLayout view, int padding) {
        view.setVisibility(padding);
    }

    @BindingAdapter("drawableStart")
    public static void setDrawableStart(EditText view, Drawable drawable) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
    }

    @BindingAdapter("drawableTop")
    public static void setDrawableTop(TextView view, Drawable drawable) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
    }

    @BindingAdapter(value = {"uri","placeholder", "error"}, requireAll = false)
    public static void setUri(final ImageView view, String uri, int placeholder, int error) {
        Log.d(TAG,
                "[xxx]" + "setUri() called with: view = [" + view + "], uri = [" + uri + "], placeholder = [" + placeholder + "], error = [" + error + "]");
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .error(error)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .format(DecodeFormat.PREFER_RGB_565);
        if (!TextUtils.isEmpty(uri)) {
            view.setTag(null);
            view.setTag(view.getId(), uri);
            if (view.getAdjustViewBounds() && view.getMaxHeight() != 0 && view.getMaxWidth() != 0) {
                options = options.override(view.getMaxWidth(), view.getMaxHeight());
            }
            GlideUtil.load(view.getContext(), uri, options, DiskCacheStrategy.ALL, -1, -1, view);
        }
    }

}

