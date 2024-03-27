package com.intelligent.share.device;

import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.excellence.basetoolslibrary.BR;
import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentHistoryDeviceBinding;
import com.intelligent.share.socketthread.UdpSocketReceiveBroadcastThread;
import com.intelligent.share.tool.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class HistoryDeviceFragment extends BaseFragment<FragmentHistoryDeviceBinding> {
    private static final String TAG = "DeviceFragment";

    private UdpSocketReceiveBroadcastThread mBroadcastRecvThread;
    private BaseRecyclerBindingAdapter<DeviceItemModel> mDeviceAdapter;
    private DeviceViewModel mDeviceViewModel;

    @Override
    protected void init() {
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
        mViewDataBinding.rvDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewDataBinding.rvDevices.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                ,getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)));
        mDeviceAdapter = new BaseRecyclerBindingAdapter<>(new ArrayList<>(), R.layout.device_item, BR.viewModel);
        mViewDataBinding.rvDevices.setAdapter(mDeviceAdapter);
        mViewDataBinding.setLifecycleOwner(getViewLifecycleOwner());
        mViewDataBinding.setViewModel(mDeviceViewModel);
        mDeviceAdapter.setOnItemClickListener((viewDataBinding, view, i) -> {
            mDeviceViewModel.connect(mDeviceAdapter.getData().get(i), getActivity());
        });

        mDeviceViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showWaitDialog();
                } else {
                    hideWaitDialog();
                }
            }
        });

        mDeviceViewModel.getHistoryConnectedDevice().observe(this, new Observer<List<DeviceItemModel>>() {
            @Override
            public void onChanged(List<DeviceItemModel> deviceItemModels) {
                Log.i(TAG, "[xxx] onChanged:" + deviceItemModels.size());
                mDeviceAdapter.notifyNewData(deviceItemModels);
            }
        });
        mViewDataBinding.ivBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_history_device;
    }}