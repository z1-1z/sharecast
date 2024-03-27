package com.intelligent.share.device;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.excellence.basetoolslibrary.BR;
import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentDeviceBinding;
import com.intelligent.share.socketthread.UdpSocketReceiveBroadcastThread;
import com.intelligent.share.tool.SpaceItemDecoration;
import com.intelligent.share.tool.Utils;
import com.tosmart.dlna.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import smart.share.GlobalConstantValue;
import smart.share.MobileLoginInfo;
import smart.share.message.process.MessageProcessor;

public class DeviceFragment extends BaseFragment<FragmentDeviceBinding> {
    private static final String TAG = "DeviceFragment";

    private UdpSocketReceiveBroadcastThread mBroadcastRecvThread;
    private MessageProcessor mMsgProc;
    private BaseRecyclerBindingAdapter<DeviceItemModel> mDeviceAdapter;
    private DeviceViewModel mDeviceViewModel;
    private AnimatorSet mAnimatorSet;

    @Override
    protected void init() {
        ThreadUtils.prepare();
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
        mViewDataBinding.rvDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewDataBinding.rvDevices.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)));
        mDeviceAdapter = new BaseRecyclerBindingAdapter<>(new ArrayList<>(), R.layout.device_item, BR.viewModel);
        mViewDataBinding.rvDevices.setAdapter(mDeviceAdapter);
        mViewDataBinding.setLifecycleOwner(getViewLifecycleOwner());
        mViewDataBinding.setViewModel(mDeviceViewModel);
        mDeviceAdapter.setOnItemClickListener((viewDataBinding, view, i) -> {
            mDeviceViewModel.connect(mDeviceAdapter.getData().get(i), getActivity());
        });
        mMsgProc = MessageProcessor.obtain();
        mMsgProc.recycle();
        mMsgProc.setOnMessageProcess(GlobalConstantValue.GSCMD_NOTIFY_BROADCAST_LOGIN_INFO_UPDATED, getActivity(),
                (MessageProcessor.PerformOnForeground) msg -> {
                    try {
                        Log.d(TAG, "[xxx]" + "init() called");
                        mDeviceViewModel.setDeviceList((ArrayList<MobileLoginInfo>) msg.obj);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        mMsgProc.setOnMessageProcess(GlobalConstantValue.CONNECT_TYEP_AUTO_LOGIN, getActivity(), (MessageProcessor.PerformOnForeground) msg -> {
            if (msg.arg1 > 0) {
                hideWaitDialog();
            }
        });
        mDeviceViewModel.getDeviceList().observe(this, new Observer<List<DeviceItemModel>>() {
            @Override
            public void onChanged(List<DeviceItemModel> deviceItemModels) {
                Log.i(TAG, "[xxx] onChanged:" + deviceItemModels.size());
                if (deviceItemModels.size() == 0) {
                    mDeviceAdapter.getData().clear();
                    mDeviceAdapter.notifyDataSetChanged();
                } else {
                    mDeviceAdapter.notifyNewData(deviceItemModels);
                }
                mViewDataBinding.llSearchTip.setVisibility(deviceItemModels.size() == 0 ? View.VISIBLE : View.GONE);

            }
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
        mViewDataBinding.tvTitle2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_HomeFragment_to_HistoryDeviceFragment, null, Utils.getNavOptions());
            }
        });
        startAnimationSequence();
    }

    public void onResume() {
        super.onResume();
        mBroadcastRecvThread = new UdpSocketReceiveBroadcastThread();
        mBroadcastRecvThread.start();
    }

    private void startAnimationSequence() {
        Animator animator1 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        animator1.setTarget(mViewDataBinding.ivSearch2);

        Animator animator2 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        animator2.setTarget(mViewDataBinding.ivSearch3);

        Animator animator3 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        animator3.setTarget(mViewDataBinding.ivSearch4);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator1, animator2, animator3);
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.i(TAG, "[xxx] onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                resetViews();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.i(TAG, "[xxx] onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mAnimatorSet.start();
    }

    private void pause() {
        if (mAnimatorSet != null) {
            mAnimatorSet.pause();
        }
    }

    private void resetViews() {
        mViewDataBinding.ivSearch4.setAlpha(0f);
        mViewDataBinding.ivSearch3.setAlpha(0f);
        mViewDataBinding.ivSearch2.setAlpha(0f);
        startAnimationSequence();
    }

    @Override
    public void onPause() {
        super.onPause();
        ThreadUtils.execute(() -> mBroadcastRecvThread.interrupt());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMsgProc.recycle();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_device;
    }
}