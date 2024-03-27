package com.intelligent.share.video;

import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentWebBinding;
import com.intelligent.share.web.BaseCastViewModel;
import com.intelligent.share.web.WebViewManager;

import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;

public class WebFragment extends BaseFragment<FragmentWebBinding> {
    private static final String TAG = "AppFragment";
    private BaseCastViewModel mCastViewModel;
    private WebViewManager mWebViewManager;
    private String mMediaUrl;
    private String mPictureUrl;
    private String mWebTitle;

    @Override
    protected void init() {
        mCastViewModel = ViewModelProviders.of(this).get(BaseCastViewModel.class);
        mViewDataBinding.setViewModel(mCastViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        mCastViewModel.init(getActivity());
        clearMiracastUrl();
        initWebViewData();
        initEvent();
    }

    private void initEvent() {
        mViewDataBinding.ivBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });
        mViewDataBinding.rlBottomBar.btPlay.setOnClickListener(view -> {
            mCastViewModel.videoSeekBarPlay();
        });

        addWebViewManagerEvent();
        mViewDataBinding.rlBottomBar.btMiracastMedia.setOnClickListener(view -> {
            Log.i(TAG, "mMediaUrl = : " + mMediaUrl);
            mCastViewModel.sendCastPlay(mMediaUrl/*"https://cesium.com/public/SandcastleSampleData/big-buck-bunny_trailer.mp4"*/,
                    DataConvertCastPlayInfoModel.VIDEO);

        });

        mViewDataBinding.rlBottomBar.btMiracastPic.setOnClickListener(view -> {
            Log.i(TAG, "mPictureUrl = : " + mPictureUrl);
            mCastViewModel.sendCastPlay(mPictureUrl, DataConvertCastPlayInfoModel.PICTURE);
        });

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
    }

    private void addWebViewManagerEvent() {
        mWebViewManager.setOnWebViewListener(new WebViewManager.OnWebViewListener() {
            @Override
            public void onKeyBack() {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.popBackStack();
            }

            @Override
            public void onProgressChanged(int progress) {
                if (progress >= BaseCastViewModel.MAX_PROGRESS) {
                    mViewDataBinding.progressBar.setVisibility(View.INVISIBLE);
                } else {
                    mViewDataBinding.progressBar.setVisibility(View.VISIBLE);
                }
                mViewDataBinding.progressBar.setProgress(progress);
            }

            @Override
            public void onUrlGetFromWebView(String url) {
                Log.i(TAG, "[xxx] onUrlGetFromWebView: url " + url);
                boolean isMediaMatch = mWebViewManager.isMediaMatchUrlStr(url);
                if (isMediaMatch) {
                    mMediaUrl = url;
                    mCastViewModel.getMiracastMediaEnable().setValue(true);
                }
            }

            @Override
            public void getTitle(View view, String title) {
                mWebTitle = title;
                String match = mWebViewManager.getMatch();
                String currentUrl = mViewDataBinding.webView.copyBackForwardList().getCurrentItem().getUrl();
            }

            @Override
            public void onPageStarted(String url) {
                clearMiracastUrl();
            }

            @Override
            public void onPageFinished(String url) {
            }

            @Override
            public void shouldStart(String url) {
                clearMiracastUrl();
            }

            @Override
            public void abortLoad(String url) {
            }

            @Override
            public void onHttpError(String url, int errorCode) {

            }

            @Override
            public void getImageUrl(String imageUrl) {
                mPictureUrl = imageUrl;
                mCastViewModel.getMiracastPicEnable().setValue(false);
            }
        });
    }

    private void clearMiracastUrl() {
        mMediaUrl = null;
        mPictureUrl = null;
        mCastViewModel.getMiracastMediaEnable().setValue(false);
        mCastViewModel.getMiracastPicEnable().setValue(false);
    }


    private void initWebViewData() {
        WebFragmentArgs webFragmentArgs = WebFragmentArgs.fromBundle(requireArguments());
        mWebViewManager = new WebViewManager(mViewDataBinding.webView, getContext(), webFragmentArgs.getWebUrl(), webFragmentArgs.getWebMatch());
        mWebViewManager.reloadHomeUrl();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_web;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCastViewModel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCastViewModel.onResume();
    }
}