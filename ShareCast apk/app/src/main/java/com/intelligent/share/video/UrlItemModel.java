package com.intelligent.share.video;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.bean.UrlLinkBean;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class UrlItemModel extends ViewModel {
    private static final String TAG = "DeviceItemModel";
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private MutableLiveData<String> mUri = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsSel = new MutableLiveData<>();
    private UrlLinkBean mUrlLinkBean;


    public UrlItemModel(UrlLinkBean urlLinkBean) {
        mUrlLinkBean = urlLinkBean;
        mName.setValue(urlLinkBean.getName());
        mIsSel.setValue(urlLinkBean.isSelect());
        mUri.setValue(urlLinkBean.getUrl());
    }

    public MutableLiveData<String> getName() {
        return mName;
    }

    public MutableLiveData<Boolean> getIsSel() {
        return mIsSel;
    }

    public MutableLiveData<String> getUri() {
        return mUri;
    }
}
