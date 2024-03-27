package com.intelligent.share.video;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.base.ShareApp;
import com.intelligent.share.bean.WebsiteItem;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class AppItemViewModel extends ViewModel {
    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<Drawable> mIcon = new MutableLiveData<>();
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private WebsiteItem.WebInfoItem mWebsiteItem;

    public AppItemViewModel(WebsiteItem.WebInfoItem websiteItem) {
        mWebsiteItem = websiteItem;
        mIcon.setValue(ShareApp.getAppContext().getDrawable(websiteItem.getImageId()));
        mName.setValue("test");
    }

    public MutableLiveData<Drawable> getIcon() {
        return mIcon;
    }

    public MutableLiveData<String> getName() {
        return mName;
    }

    public WebsiteItem.WebInfoItem getWebsiteItem() {
        return mWebsiteItem;
    }
}
