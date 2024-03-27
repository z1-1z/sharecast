package com.intelligent.share.video;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentVideoBinding;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends BaseFragment<FragmentVideoBinding> {
    private static final String TAG = "VideoFragment";
    private static final int INDICATOR_TEXT_SIZE_SEL = 19;
    private static final int INDICATOR_TEXT_SIZE_NOR = 16;
    private static final int INDICATOR_TEXT_PADDING = 50;
    private static final int INDICATOR_LINE_WIDTH = 30;
    private static final int INDICATOR_LINE_HEIGHT = 2;
    private static final int INDICATOR_LINE_RADIUS = 3;
    private FragmentStatePagerAdapter mFragmentStatePagerAdapter;

    @Override
    protected void init() {
        initCategoryIndicator();
        initViewPage();
    }

    private void initViewPage() {
        mFragmentStatePagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return 0 == position ? AppFragment.newInstance() : UrlFragment.newInstance();
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public int getItemPosition(Object object) {
                return PagerAdapter.POSITION_NONE;
            }
        };
        mViewDataBinding.viewPager.setAdapter(mFragmentStatePagerAdapter);
    }

    private void initCategoryIndicator() {
        final List<String> categoryNames = new ArrayList<>();
        final List<Drawable> categorySelIcons = new ArrayList<>();
        final List<Drawable> categoryNorIcons = new ArrayList<>();
        categoryNames.add(getString(R.string.str_app));
        categoryNames.add(getString(R.string.str_url_link));
        categorySelIcons.add(mContext.getDrawable(R.drawable.video_app_icon_sel));
        categorySelIcons.add(mContext.getDrawable(R.drawable.video_url_icon_sel));

        categoryNorIcons.add(mContext.getDrawable(R.drawable.video_app_icon));
        categoryNorIcons.add(mContext.getDrawable(R.drawable.video_url_icon));

        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return categoryNames.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new SimplePagerTitleView(context);
                simplePagerTitleView.setText(categoryNames.get(index));
                simplePagerTitleView.getPaint().setFakeBoldText(true);
                simplePagerTitleView.setPadding(INDICATOR_TEXT_PADDING);
                simplePagerTitleView.setTextSize(INDICATOR_TEXT_SIZE_NOR);
                simplePagerTitleView.setGravity(Gravity.CENTER);
                simplePagerTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(categoryNorIcons.get(index), null, null, null);
                simplePagerTitleView.setNormalTextSize(INDICATOR_TEXT_SIZE_NOR);
                simplePagerTitleView.setSelectedTextSize(INDICATOR_TEXT_SIZE_SEL);
                simplePagerTitleView.setNormalTextIcon(categoryNorIcons.get(index));
                simplePagerTitleView.setSelectedIcon(categorySelIcons.get(index));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewDataBinding.viewPager.setCurrentItem(index);
                    }
                });

                simplePagerTitleView.setNormalColor(mContext.getResources().getColor(R.color.video_nav_title_txt_color_nor));
                simplePagerTitleView.setSelectedColor(mContext.getResources().getColor(R.color.video_nav_title_txt_color_sel));

                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, INDICATOR_LINE_HEIGHT));
                indicator.setLineWidth(UIUtil.dip2px(context, INDICATOR_LINE_WIDTH));
                indicator.setRoundRadius(UIUtil.dip2px(context, INDICATOR_LINE_RADIUS));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                //jem switch
                indicator.setColors(mContext.getResources().getColor(R.color.video_nav_title_txt_color_sel));
                return indicator;
            }
        });
        mViewDataBinding.indicator.setNavigator(commonNavigator);
        mViewDataBinding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mViewDataBinding.indicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                mViewDataBinding.indicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mViewDataBinding.indicator.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_video;
    }
}