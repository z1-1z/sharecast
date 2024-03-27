package com.intelligent.share.local;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.share.R;
import com.intelligent.share.tool.GlideUtil;
import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.util.Constant;

import java.util.List;

/**
 * @date 2020/3/16
 */
public class ImageAdapter extends PagerAdapter {
    private List<LocalMediaItemModel> mImageList;

    public ImageAdapter(List<LocalMediaItemModel> imageList) {
        mImageList = imageList;
    }

    public List<LocalMediaItemModel> getImageList() {
        return mImageList;
    }

    public void setImageList(List<LocalMediaItemModel> imageList) {
        mImageList = imageList;
    }

    @Override
    public int getCount() {
        return mImageList.size();// 返回数据的个数
    }

    @NonNull
    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {//子View显示
        View view = View.inflate(container.getContext(), R.layout.cast_item, null);
        ImageView imageView = view.findViewById(R.id.iv_icon);
        DlnaItemEntity contentItem = mImageList.get(position).getDlnaItemEntity();
        if (contentItem.getMimeType().contains(Constant.VIDEO_TYPE)) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            GlideUtil.load(view.getContext(), contentItem.getImage(), null, DiskCacheStrategy.ALL
                    , R.drawable.video_default_wrap, R.drawable.video_default_wrap, imageView);
            view.setBackgroundColor(container.getContext().getResources().getColor(R.color.black));
        } else if (contentItem.getMimeType().contains(Constant.AUDIO_TYPE)) {
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageResource(R.drawable.record);
            view.setBackgroundColor(container.getContext().getResources().getColor(R.color.common_bg_color));
        } else if (contentItem.getMimeType().contains(Constant.IMAGE_TYPE)) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            String path = contentItem.getImage().substring(contentItem.getImage().indexOf("/storage"));
            GlideUtil.load(view.getContext(), path, null, DiskCacheStrategy.ALL
                    , R.drawable.video_default_wrap, R.drawable.video_default_wrap, imageView);
            view.setBackgroundColor(container.getContext().getResources().getColor(R.color.black));
        }
        container.addView(view);//添加到父控件
        return view;
    }

    //解决ViewPager数据源改变时，刷新无效的解决办法
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;// 过滤和缓存的作用
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);//从viewpager中移除掉
    }
}
