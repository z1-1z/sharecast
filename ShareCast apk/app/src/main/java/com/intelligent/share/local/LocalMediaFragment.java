package com.intelligent.share.local;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentLocalMediaBinding;
import com.tosmart.dlna.data.repository.LibraryRepository;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.List;

public class LocalMediaFragment extends BaseFragment<FragmentLocalMediaBinding> {
    private static final String TAG = "LocalMediaFragment";
    private static final int INDICATOR_TEXT_SIZE_SEL = 19;
    private static final int INDICATOR_TEXT_SIZE_NOR = 16;
    private static final int INDICATOR_TEXT_PADDING = 20;
    private static final int INDICATOR_LINE_WIDTH = 30;
    private static final int INDICATOR_LINE_HEIGHT = 2;
    private static final int INDICATOR_LINE_RADIUS = 3;
    private FragmentStatePagerAdapter mFragmentStatePagerAdapter;
    private List<Fragment> mList;


    @Override
    protected void init() {
        LibraryRepository.getInstance().getIsInitComplete().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.i(TAG, "[xxx] onChanged:aBoolean " + aBoolean + ",mFragmentStatePagerAdapter" + mFragmentStatePagerAdapter);
                if (aBoolean) {
                    initCategoryIndicator();
                    initViewPage();
                }
                mViewDataBinding.pbLoading.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void initViewPage() {
        mList = new ArrayList<>();
        mList.add(LocalMediaListFragment.newInstance(LocalMediaListFragment.TYPE_PIC));
        mList.add(LocalMediaListFragment.newInstance(LocalMediaListFragment.TYPE_VIDEO));
        mList.add(LocalMediaListFragment.newInstance(LocalMediaListFragment.TYPE_AUDIO));
        mFragmentStatePagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mList.get(position);
            }

            @Override
            public int getCount() {
                return mList.size();
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
        categoryNames.add(getString(R.string.str_image));
        categoryNames.add(getString(R.string.str_video));
        categoryNames.add(getString(R.string.str_music));
        categorySelIcons.add(mContext.getDrawable(R.drawable.image_icon_sel));
        categorySelIcons.add(mContext.getDrawable(R.drawable.media_video_icon_sel));
        categorySelIcons.add(mContext.getDrawable(R.drawable.music_icon_sel));

        categoryNorIcons.add(mContext.getDrawable(R.drawable.image_icon));
        categoryNorIcons.add(mContext.getDrawable(R.drawable.media_video_icon));
        categoryNorIcons.add(mContext.getDrawable(R.drawable.music_icon));
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
                simplePagerTitleView.setNormalTextSize(INDICATOR_TEXT_SIZE_NOR);
                simplePagerTitleView.setSelectedTextSize(INDICATOR_TEXT_SIZE_SEL);
                simplePagerTitleView.setNormalTextIcon(categoryNorIcons.get(index));
                simplePagerTitleView.setSelectedIcon(categorySelIcons.get(index));
                simplePagerTitleView.setGravity(Gravity.CENTER);
                simplePagerTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(categoryNorIcons.get(index), null, null, null);
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
        mViewDataBinding.viewPager.setOffscreenPageLimit(3);
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
        return R.layout.fragment_local_media;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this/*LifecycleOwner*/, mCallback);
    }

    private OnBackPressedCallback mCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (mList == null || !((LocalMediaListFragment)mList.get(mViewDataBinding.viewPager.getCurrentItem())).back()) {
                getActivity().finish();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallback.remove();
    }
}