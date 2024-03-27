package com.intelligent.share.bin;

import android.view.View;
import android.widget.Toast;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.excellence.basetoolslibrary.BR;
import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.excellence.basetoolslibrary.databinding.MultiItemTypeBindingRecyclerAdapter;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentBinFileBinding;
import com.intelligent.share.widget.CommonTipWindow;
import com.tosmart.dlna.util.ThreadUtils;

import java.util.ArrayList;

import smart.share.GlobalConstantValue;
import smart.share.message.process.MessageProcessor;

public class BinFragment extends BaseFragment<FragmentBinFileBinding> {
    private static final String TAG = "BinFragment";
    private BinViewModel mBinViewModel;
    private MessageProcessor mMsgProc;


    @Override
    protected void init() {
        ThreadUtils.prepare();
        mBinViewModel = ViewModelProviders.of(getActivity()).get(BinViewModel.class);
        mViewDataBinding.setViewModel(mBinViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        BaseRecyclerBindingAdapter<BinItemModel> bindingAdapter =
                new BaseRecyclerBindingAdapter<BinItemModel>(new ArrayList<>(), R.layout.bin_file_item, BR.viewModel);
        mViewDataBinding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mViewDataBinding.rvList.setAdapter(bindingAdapter);
        mBinViewModel.getData().observe(this, new Observer<ArrayList<BinItemModel>>() {
            @Override
            public void onChanged(ArrayList<BinItemModel> binItemModels) {
                bindingAdapter.notifyNewData(binItemModels);
            }
        });
        mBinViewModel.scan();
        bindingAdapter.setOnItemClickListener(new MultiItemTypeBindingRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewDataBinding viewDataBinding, View view, int i) {
                CommonTipWindow commonTipWindow = new CommonTipWindow(mContext, true);
                commonTipWindow.getLeftBtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        commonTipWindow.dismiss();
                    }
                });
                commonTipWindow.getRightBtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBinViewModel.sendCastPlay(bindingAdapter.getData().get(i));
                        commonTipWindow.dismiss();
                    }
                });
                commonTipWindow.show();
            }
        });
        mMsgProc = MessageProcessor.obtain();
        mMsgProc.recycle();
        mMsgProc.setOnMessageProcess(GlobalConstantValue.S_MSG_CAST_DO_PLAY, getActivity(), (MessageProcessor.PerformOnBackground) msg -> {
            if (msg.arg1 == 0) {
                Toast.makeText(mContext, R.string.str_push_success, Toast.LENGTH_SHORT).show();
            }
        });
        mViewDataBinding.ivBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });
        mViewDataBinding.tvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinViewModel.clear();
                mBinViewModel.scan();
            }
        });
    }



    @Override
    protected int getContentViewId() {
        return R.layout.fragment_bin_file;
    }
}