package com.intelligent.share.local;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tosmart.dlna.data.local.DlnaItemEntity;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class LocalMediaItemModel extends ViewModel {
    private static final String TAG = "DeviceItemModel";
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private MutableLiveData<String> mLength = new MutableLiveData<>();
    private MutableLiveData<String> mSize = new MutableLiveData<>();
    private MediatorLiveData<String> mUrl = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> mIsRound = new MediatorLiveData<>();
    private DlnaItemEntity mDlnaItemEntity;

    public LocalMediaItemModel(DlnaItemEntity dlnaItemEntity) {
        mDlnaItemEntity = dlnaItemEntity;
    }

    public MutableLiveData<String> getName() {
        return mName;
    }

    public MutableLiveData<String> getUrl() {
        return mUrl;
    }

    public MediatorLiveData<Boolean> getIsRound() {
        return mIsRound;
    }

    public DlnaItemEntity getDlnaItemEntity() {
        return mDlnaItemEntity;
    }
}
