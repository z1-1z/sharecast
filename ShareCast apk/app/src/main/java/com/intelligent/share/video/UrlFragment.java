package com.intelligent.share.video;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.excellence.basetoolslibrary.BR;
import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentUrlBinding;
import com.intelligent.share.tool.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class UrlFragment extends BaseFragment<FragmentUrlBinding> {
    private static final String TAG = "UrlFragment";
    private UrlLinkViewModel mUrlLinkViewModel;

    public static UrlFragment newInstance() {
        Bundle args = new Bundle();
        UrlFragment fragment = new UrlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init() {
        mViewDataBinding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        BaseRecyclerBindingAdapter<UrlItemModel> bindingAdapter =
                new BaseRecyclerBindingAdapter<UrlItemModel>(new ArrayList<>(), R.layout.url_item, BR.viewModel);
        mViewDataBinding.rvList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)));
        mViewDataBinding.rvList.setAdapter(bindingAdapter);
        mUrlLinkViewModel = ViewModelProviders.of(this).get(UrlLinkViewModel.class);
        mViewDataBinding.setViewModel(mUrlLinkViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        bindingAdapter.setOnItemClickListener((viewDataBinding, view, position) -> mUrlLinkViewModel.onItemClick(position));
        initEvent();
        mUrlLinkViewModel.getUrlItems().observe(this, new Observer<List<UrlItemModel>>() {
            @Override
            public void onChanged(List<UrlItemModel> urlItemModels) {
                bindingAdapter.notifyNewData(urlItemModels);
            }
        });
    }

    private void initEvent() {
        mViewDataBinding.ivDel.setOnClickListener(view -> mViewDataBinding.searchText.setText(""));
        mViewDataBinding.ivSearch.setOnClickListener(view -> {
            addUrlLink();
        });
        mViewDataBinding.searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    addUrlLink();
                    return true;
                }
                return false;
            }
        });
        mViewDataBinding.rlBottomBar.btPlay.setOnClickListener(view -> {
            mUrlLinkViewModel.videoSeekBarPlay();
        });

        mViewDataBinding.rlBottomBar.progressBarPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUrlLinkViewModel.onPause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUrlLinkViewModel.setSeekTime(seekBar.getProgress());
            }
        });

    }

    private void addUrlLink() {
        String searchText = mViewDataBinding.searchText.getText().toString();
        mUrlLinkViewModel.addUrlLinkWithString(searchText);
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mViewDataBinding.searchText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_url;
    }

    @Override
    public void onPause() {
        super.onPause();
        mUrlLinkViewModel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUrlLinkViewModel.init(getActivity());
        mUrlLinkViewModel.onResume();
    }
}