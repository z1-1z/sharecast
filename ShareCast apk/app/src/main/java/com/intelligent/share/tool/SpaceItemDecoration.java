package com.intelligent.share.tool;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

/**
 * @author Doris on 2018/5/12.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int mLeftSpace = 0;
    private int mTopSpace = 0;
    private int mRightSpace = 0;
    private int mBottomSpace = 0;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mLeftSpace;
        outRect.right = mRightSpace;
        outRect.bottom = mBottomSpace;
        outRect.top = mTopSpace;
    }

    public SpaceItemDecoration(int space) {
        mLeftSpace = space;
        mTopSpace = space;
        mRightSpace = space;
        mBottomSpace = space;
    }

    public SpaceItemDecoration(int left, int top, int right, int bottom) {
        mLeftSpace = left;
        mTopSpace = top;
        mRightSpace = right;
        mBottomSpace = bottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpaceItemDecoration)) return false;
        SpaceItemDecoration that = (SpaceItemDecoration) o;
        return mLeftSpace == that.mLeftSpace && mTopSpace == that.mTopSpace && mRightSpace == that.mRightSpace && mBottomSpace == that.mBottomSpace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLeftSpace, mTopSpace, mRightSpace, mBottomSpace);
    }
}
