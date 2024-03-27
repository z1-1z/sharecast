package com.intelligent.share.local;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.data.repository.LibraryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author xxx
 * @date 2019/4/25
 */
public class LocalMediaViewModel extends ViewModel {
    private static final String TAG = LocalMediaViewModel.class.getSimpleName();
    private LibraryRepository mLibraryRepository = LibraryRepository.getInstance();
    private MediatorLiveData<List<DlnaItemEntity>> mContentItemList = new MediatorLiveData<>();
    private List<DlnaItemEntity> mSelectItems;
    private Stack<DlnaItemEntity> mBackStack = new Stack<>();
    private MutableLiveData<DlnaItemEntity> mParentDlnaItem = new MutableLiveData<>();
    private LiveData<List<LocalMediaItemModel>> mData = Transformations.switchMap(mContentItemList, input -> {
        MutableLiveData<List<LocalMediaItemModel>> data = new MutableLiveData<>();
        List<LocalMediaItemModel> list = new ArrayList<>();
        for (DlnaItemEntity dlnaItemEntity : input) {
            list.add(new LocalMediaItemModel(dlnaItemEntity));
        }
        data.setValue(list);
        return data;
    });
    private int mType;

    public LocalMediaViewModel() {

    }

    public void init(int type) {
        mType = type;
        MutableLiveData<List<DlnaItemEntity>> rootContentItemList = mLibraryRepository.getContentItemList((DlnaItemEntity) null);
        mContentItemList.addSource(rootContentItemList, contentItems -> {
            Log.i(TAG, "[xxx] init: " + contentItems.size());
            if (type < contentItems.size()) {
                updateContentItemList(contentItems.get(type));
            }
            mContentItemList.removeSource(rootContentItemList);
        });
    }

    public void updateContentItemList(int position) {
        // 处于 music video images Internal storage 界面
        if (mContentItemList != null && mContentItemList.getValue() != null && mContentItemList.getValue().size() > position) {
            mBackStack.add(mParentDlnaItem.getValue());
            updateContentItemList(mContentItemList.getValue().get(position));
            Log.i(TAG, this + "[xxx] back:" + mBackStack.size());
        }
    }

    public void updateContentItemList(DlnaItemEntity item) {
        mParentDlnaItem.setValue(item);
        addContentSource(mLibraryRepository.getContentItemList(item));
    }

    public void addContentSource(MutableLiveData<List<DlnaItemEntity>> contentItemList) {
        mContentItemList.addSource(contentItemList, contentItems -> {
            Log.i(TAG, "[xxx] addContentSource:" + contentItems.size());
            mContentItemList.postValue(contentItems);
            mContentItemList.removeSource(contentItemList);
        });
    }

    public MutableLiveData<List<DlnaItemEntity>> getContentItemList() {
        return mContentItemList;
    }

    public boolean back() {
        Log.i(TAG, this + "[xxx] back:" + mBackStack.size());
        if (!mBackStack.empty()) {
            mParentDlnaItem.setValue(mBackStack.pop());
            updateContentItemList(mParentDlnaItem.getValue());
            return true;
        }
        return false;
    }

    public MutableLiveData<DlnaItemEntity> getParentDlnaItem() {
        return mParentDlnaItem;
    }


    @Override
    protected void onCleared() {
        Log.d(TAG, "[sll_debug] onCleared: ");
        super.onCleared();
    }

    public void reloadContentItems() {
        updateContentItemList(mParentDlnaItem.getValue());
    }

    public boolean addToSelectItemList(int position) {
        if (mSelectItems == null) {
            mSelectItems = new ArrayList<>();
        }
        DlnaItemEntity item = mContentItemList.getValue().get(position);
        // 如果已经存在，则进行移除操作
        if (mSelectItems.contains(item)) {
            mSelectItems.remove(item);
            return false;
        } else {
            mSelectItems.add(item);
            return true;
        }
    }

    /**
     * 清除选中Flag，防止影响其他地方的选中状态
     */
    private void clearSelectedFlag() {
        List<DlnaItemEntity> value = mContentItemList.getValue();
        if (value == null) return;
        for (DlnaItemEntity item : value) {
            item.setSelected(false);
        }
    }

    private void getContentItemList(List<DlnaItemEntity> list, int position, boolean isCover) {
        MutableLiveData<List<DlnaItemEntity>> contentItemList = mLibraryRepository.getContentItemList(list);
    }

    public LiveData<List<LocalMediaItemModel>> getData() {
        return mData;
    }
}
