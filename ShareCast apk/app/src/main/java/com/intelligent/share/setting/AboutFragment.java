package com.intelligent.share.setting;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentAboutBinding;
import com.intelligent.share.databinding.FragmentSettingBinding;

public class AboutFragment extends BaseFragment<FragmentAboutBinding> {
    private static final String TAG = "SettingFragment";
    private AboutViewModel mAboutViewModel;


    @Override
    protected void init() {
        mAboutViewModel = ViewModelProviders.of(this).get(AboutViewModel.class);
        mViewDataBinding.setViewModel(mAboutViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        mViewDataBinding.ivBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_about;
    }
}