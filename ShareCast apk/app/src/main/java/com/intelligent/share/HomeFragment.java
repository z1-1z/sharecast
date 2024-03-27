package com.intelligent.share;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.intelligent.share.bin.BinViewModel;
import com.intelligent.share.databinding.FragmentFirstBinding;
import com.intelligent.share.device.DeviceFragment;
import com.intelligent.share.local.LocalMediaFragment;
import com.intelligent.share.setting.SettingFragment;
import com.intelligent.share.video.VideoFragment;
import com.tosmart.dlna.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends com.intelligent.share.base.BaseFragment<FragmentFirstBinding> {
    private static final String TAG = "HomeFragment";

    private PagerAdapter mPagerAdapter;
    private int mCurrentPosition;


    @Override
    protected void init() {
        List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Integer> icons = new ArrayList<>();

        fragments.add(new DeviceFragment());
        fragments.add(new VideoFragment());
        fragments.add(new LocalMediaFragment());
        fragments.add(new SettingFragment());
        titles.add(getString(R.string.str_device));
        titles.add(getString(R.string.str_video));
        titles.add(getString(R.string.str_local_video));
        titles.add(getString(R.string.str_setting));
        icons.add(R.drawable.nav_device_selector);
        icons.add(R.drawable.nav_video_selector);
        icons.add(R.drawable.nav_local_video_selector);
        icons.add(R.drawable.nav_setting_selector);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager(), fragments, titles, icons);

        mViewDataBinding.vpContent.setAdapter(mPagerAdapter);
        mViewDataBinding.vpContent.setOffscreenPageLimit(3);
        mViewDataBinding.vpContent.setScrollable(false);
        mViewDataBinding.tabLayout.setupWithViewPager(mViewDataBinding.vpContent);
        TabLayout.Tab tempTab;
        for (int i = 0; i < mViewDataBinding.tabLayout.getTabCount(); i++) {
            tempTab = mViewDataBinding.tabLayout.getTabAt(i);
            if (tempTab != null) {
                tempTab.setCustomView(mPagerAdapter.getTabView(getContext(), i));
            }
        }
        mViewDataBinding.vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && fragments.get(mCurrentPosition).isVisible()) {
                }
            }

        });
        BinViewModel binViewModel = ViewModelProviders.of(getActivity()).get(BinViewModel.class);
        binViewModel.scan();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_first;
    }

    public static class PagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments;
        private final List<String> mTitles;
        private final List<Integer> mIcons;
        private Fragment mCurrentFragment;

        public PagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles, List<Integer> icons) {
            super(fm);
            mFragments = fragments;
            mTitles = titles;
            mIcons = icons;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "[xxx]" + "getItem() called with: position = [" + position + "]");
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object instanceof BaseFragment) {
                mCurrentFragment = (BaseFragment) object;
            }
        }

        View getTabView(Context context, int position) {
            View v = LayoutInflater.from(context).inflate(R.layout.bottom_nav_item, null, false);
            ImageView iv = v.findViewById(R.id.iv_tab_icon);
            TextView tv = v.findViewById(R.id.tv_tab_text);
            tv.setText(getPageTitle(position));
            iv.setImageResource(mIcons.get(position));
            return v;
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }
    }

}