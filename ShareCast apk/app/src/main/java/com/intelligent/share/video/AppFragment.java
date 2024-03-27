package com.intelligent.share.video;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.excellence.basetoolslibrary.databinding.MultiItemTypeBindingRecyclerAdapter;
import com.intelligent.share.BR;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentAppBinding;
import com.intelligent.share.tool.SpaceItemDecoration;
import com.intelligent.share.tool.Utils;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends BaseFragment<FragmentAppBinding> {
    private static final String TAG = "AppFragment";
    private AppViewModel mAppViewModel;

    public static AppFragment newInstance() {
        Bundle args = new Bundle();
        AppFragment fragment = new AppFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected void init() {
        mAppViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        mViewDataBinding.rvApps.setLayoutManager(new GridLayoutManager(mContext, 3));
        BaseRecyclerBindingAdapter<AppItemViewModel> bindingAdapter =
                new BaseRecyclerBindingAdapter<AppItemViewModel>(new ArrayList<>(), R.layout.web_item, BR.viewModel);
        mViewDataBinding.rvApps.setAdapter(bindingAdapter);
        bindingAdapter.setOnItemClickListener(new MultiItemTypeBindingRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewDataBinding viewDataBinding, View view, int i) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                        WebFragmentArgs args = WebFragmentArgs.fromBundle(requireArguments());
                navController.navigate(R.id.action_HomeFragment_to_webFragment,  new WebFragmentArgs.Builder(args)
                                .setWebMatch(bindingAdapter.getData().get(i).getWebsiteItem().getMatch())
                                .setWebUrl(bindingAdapter.getData().get(i).getWebsiteItem().getUrl()).build().toBundle(), Utils.getNavOptions());
            }
        });
        mViewDataBinding.rvApps.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)));
        mAppViewModel.getAppItemList().observe(this, new Observer<List<AppItemViewModel>>() {
            @Override
            public void onChanged(List<AppItemViewModel> appItemViewModels) {
                bindingAdapter.notifyNewData(appItemViewModels);
            }
        });

    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_app;
    }
}