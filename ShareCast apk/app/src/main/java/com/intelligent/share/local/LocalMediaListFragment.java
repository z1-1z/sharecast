package com.intelligent.share.local;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.excellence.basetoolslibrary.BR;
import com.excellence.basetoolslibrary.databinding.BaseRecyclerBindingAdapter;
import com.excellence.basetoolslibrary.databinding.MultiItemTypeBindingRecyclerAdapter;
import com.intelligent.share.R;
import com.intelligent.share.base.BaseFragment;
import com.intelligent.share.databinding.FragmentLocalMediaListBinding;
import com.intelligent.share.tool.GlideUtil;
import com.intelligent.share.tool.SpaceItemDecoration;
import com.intelligent.share.tool.Utils;
import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.data.repository.LibraryRepository;
import com.tosmart.dlna.util.Constant;

import java.util.ArrayList;
import java.util.List;

public class LocalMediaListFragment extends BaseFragment<FragmentLocalMediaListBinding> {
    private static final String TAG = "LocalMediaFragment";
    private LocalMediaViewModel mLocalMediaViewModel;
    public static final String KEY_TYPE = "type";
    public static final int TYPE_PIC = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;
    private int mType;

    public static LocalMediaListFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, type);
        LocalMediaListFragment fragment = new LocalMediaListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init() {
        mType = getArguments().getInt(KEY_TYPE);
        if (mType == TYPE_AUDIO) {
            mViewDataBinding.rvMediaList.setLayoutManager(new LinearLayoutManager(mContext));
            mViewDataBinding.rvMediaList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_little)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_tiny)));
        } else {
            mViewDataBinding.rvMediaList.setLayoutManager(new GridLayoutManager(mContext, 3));
            mViewDataBinding.rvMediaList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelOffset(R.dimen.margin_super_micro)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_super_micro)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_super_micro)
                    , getContext().getResources().getDimensionPixelOffset(R.dimen.margin_super_micro)));
        }
        BaseRecyclerBindingAdapter<LocalMediaItemModel> bindingAdapter = new BaseRecyclerBindingAdapter(new ArrayList()
                , mType == TYPE_AUDIO ? R.layout.local_music_item : R.layout.local_media_item, BR.viewModel) {

            @Override
            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                ImageView imageView = holder.itemView.findViewById(R.id.iv_icon);
                TextView title = holder.itemView.findViewById(R.id.tv_title);
                TextView title2 = holder.itemView.findViewById(R.id.tv_title2);
                TextView size = holder.itemView.findViewById(R.id.tv_size);
                DlnaItemEntity item = ((LocalMediaItemModel) getData().get(position)).getDlnaItemEntity();
                if (mType == TYPE_AUDIO) {
                    if (item.isContainer()) {
                        if (title2 != null) {
                            title2.setVisibility(View.GONE);
                        }
                        if (size != null) {
                            size.setVisibility(View.GONE);
                        }
                    } else {
                        if (title2 != null) {
                            title2.setVisibility(View.VISIBLE);
                            title2.setText(item.getDuration());
                        }
                        if (size != null) {
                            size.setVisibility(View.VISIBLE);
                            size.setText(Utils.getNetFileSizeDescription(item.getSize()));
                        }
                    }

                } else {
                    if (item.isContainer()) {
                        MutableLiveData<List<DlnaItemEntity>> rootContentItemList = LibraryRepository.getInstance().getContentItemList(item);
                        rootContentItemList.observeForever(new Observer<List<DlnaItemEntity>>() {
                            @Override
                            public void onChanged(List<DlnaItemEntity> dlnaItemEntities) {
                                Log.i(TAG, "[xxx] onChanged:" + dlnaItemEntities.get(0).getImage());
                                initView(imageView, dlnaItemEntities.get(0));
                                rootContentItemList.removeObserver(this);
                            }
                        });
                    } else {
                        initView(imageView, item);
                    }
                }
                if (item.isContainer()) {
                    title.setText(item.getName() + "(" + item.getChildCount() + ")");
                } else {
                    title.setText(item.getName());
                }
            }

            private void initView(ImageView imageView, DlnaItemEntity item) {
                switch (item.getType()) {
                    case Constant.IMAGE_TYPE:
                        RequestOptions options = new RequestOptions()
                                .placeholder(R.drawable.image_default_wrap)
                                .error(R.drawable.image_default_wrap)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .skipMemoryCache(true)
                                .format(DecodeFormat.PREFER_RGB_565);
                        String path = item.getImage().substring(item.getImage().indexOf("/storage"));
                        GlideUtil.load(imageView.getContext(), path, options, DiskCacheStrategy.ALL
                                , -1 , -1, imageView);
                        break;
                    case Constant.VIDEO_TYPE:
                        GlideUtil.load(imageView.getContext(), item.getImage(), null, DiskCacheStrategy.ALL
                                , R.drawable.video_default_wrap , R.drawable.video_default_wrap, imageView);
                        break;
                    case Constant.AUDIO_TYPE:
                        imageView.setImageResource(R.drawable.list_music_icon);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.list_music_icon);
                        break;
                }
            }
        };

        mViewDataBinding.rvMediaList.setAdapter(bindingAdapter);
        bindingAdapter.setOnItemClickListener(new MultiItemTypeBindingRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewDataBinding viewDataBinding, View view, int itemPosition) {
                if (mLocalMediaViewModel.getContentItemList() != null && mLocalMediaViewModel.getContentItemList().getValue() != null && mLocalMediaViewModel.getContentItemList().getValue().size() > itemPosition) {
                    DlnaItemEntity contentItem = mLocalMediaViewModel.getContentItemList().getValue().get(itemPosition);
                    if (contentItem.isContainer()) {
                        mLocalMediaViewModel.updateContentItemList(itemPosition);
                    } else {
                        //play
                        CastViewModel castViewModel = ViewModelProviders.of(getActivity()).get(CastViewModel.class);
                        castViewModel.setData(mLocalMediaViewModel.getData().getValue());
                        castViewModel.setCurPosition(itemPosition);
                        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigate(R.id.action_HomeFragment_to_castFragment,null, Utils.getNavOptions());
                    }
                }
            }
        });
        mLocalMediaViewModel = new LocalMediaViewModel();
        mViewDataBinding.setLifecycleOwner(this);
        mLocalMediaViewModel.getData().observe(this, new Observer<List<LocalMediaItemModel>>() {
            @Override
            public void onChanged(List<LocalMediaItemModel> localMediaItemModels) {
                bindingAdapter.notifyNewData(localMediaItemModels);
            }
        });
        mLocalMediaViewModel.init(mType);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_local_media_list;
    }


    public boolean back() {
        return mLocalMediaViewModel.back();
    }
}