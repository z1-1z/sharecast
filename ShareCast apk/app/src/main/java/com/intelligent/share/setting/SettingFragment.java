package com.intelligent.share.setting;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentSettingBinding;
import com.intelligent.share.tool.Utils;

public class SettingFragment extends BaseFragment<FragmentSettingBinding> {
    private static final String TAG = "SettingFragment";
    private SettingViewModel mSettingViewModel;


    @Override
    protected void init() {
        mSettingViewModel = ViewModelProviders.of(getActivity()).get(SettingViewModel.class);
        mViewDataBinding.setViewModel(mSettingViewModel);
        mViewDataBinding.setLifecycleOwner(this);

        mViewDataBinding.vBin.getRoot().setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_HomeFragment_to_binFragment, null, Utils.getNavOptions());
        });
        mViewDataBinding.vAbout.getRoot().setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_HomeFragment_to_AboutFragment, null, Utils.getNavOptions());
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_setting;
    }
}