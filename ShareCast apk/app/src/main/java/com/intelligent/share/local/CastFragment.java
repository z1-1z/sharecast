package com.intelligent.share.local;

import android.view.View;
import android.widget.SeekBar;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentCastBinding;
import com.tosmart.dlna.data.local.DlnaItemEntity;

import java.util.ArrayList;
import java.util.List;

public class CastFragment extends BaseFragment<FragmentCastBinding> {
    private static final String TAG = "CastFragment";
    private ImageAdapter mImageAdapter;
    private CastViewModel mCastViewModel;


    @Override
    protected void init() {
        initViewPager();
        mCastViewModel = ViewModelProviders.of(getActivity()).get(CastViewModel.class);
        mViewDataBinding.setViewModel(mCastViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        mCastViewModel.getData().observe(this, new Observer<List<LocalMediaItemModel>>() {
            @Override
            public void onChanged(List<LocalMediaItemModel> localMediaItemModels) {
                mImageAdapter.setImageList(localMediaItemModels);
                mImageAdapter.notifyDataSetChanged();
                mViewDataBinding.viewPager.setCurrentItem(mCastViewModel.getCurPosition());
            }
        });
        mViewDataBinding.rlBottomBar.btMiracastMedia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DlnaItemEntity entity = mImageAdapter.getImageList().get(mViewDataBinding.viewPager.getCurrentItem()).getDlnaItemEntity();
                mCastViewModel.sendCastPlay(entity);
            }
        });
        mViewDataBinding.rlBottomBar.btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCastViewModel.videoSeekBarPlay();
            }
        });
        mViewDataBinding.rlBottomBar.btMiracastMedia2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DlnaItemEntity entity = mImageAdapter.getImageList().get(mViewDataBinding.viewPager.getCurrentItem()).getDlnaItemEntity();
                mCastViewModel.sendCastPlay(entity);
            }
        });
        mViewDataBinding.rlBottomBar.btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewDataBinding.viewPager.setCurrentItem(mViewDataBinding.viewPager.getCurrentItem() + 1);
            }
        });

        mViewDataBinding.rlBottomBar.btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewDataBinding.viewPager.setCurrentItem(mViewDataBinding.viewPager.getCurrentItem() - 1);
            }
        });

        mCastViewModel.init(getActivity());

        mViewDataBinding.rlBottomBar.progressBarPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mCastViewModel.onPause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCastViewModel.setSeekTime(seekBar.getProgress());
            }
        });
        mViewDataBinding.ivBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });
        mViewDataBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCastViewModel.getIsEnterMiracastModel().getValue()) {
                    DlnaItemEntity entity = mImageAdapter.getImageList().get(position).getDlnaItemEntity();
                    mCastViewModel.sendCastPlay(entity);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initViewPager() {
        if (mImageAdapter == null) {
            mImageAdapter = new ImageAdapter(new ArrayList<>());
            mViewDataBinding.viewPager.setAdapter(mImageAdapter);
            mImageAdapter.notifyDataSetChanged();
        }
        mViewDataBinding.viewPager.setPageMargin(20);
        mViewDataBinding.viewPager.setOffscreenPageLimit(3);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_cast;
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        //自由控制缩放比例
        private static final float MAX_SCALE = 1f;
        private static final float MIN_SCALE = 0.8f;//0.85f

        @Override
        public void transformPage(View page, float position) {

            if (position <= 1) {

                float scaleFactor = MIN_SCALE + (1 - Math.abs(position)) * (MAX_SCALE - MIN_SCALE);

                page.setScaleX(scaleFactor);

                if (position > 0) {
                    page.setTranslationX(-scaleFactor * 2);
                } else if (position < 0) {
                    page.setTranslationX(scaleFactor * 2);
                }
                page.setScaleY(scaleFactor);
            } else {

                page.setScaleX(MIN_SCALE);
                page.setScaleY(MIN_SCALE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCastViewModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCastViewModel.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastViewModel.onDestroy();
    }
}