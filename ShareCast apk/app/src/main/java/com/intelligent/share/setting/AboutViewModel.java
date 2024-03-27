package com.intelligent.share.setting;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.BuildConfig;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class AboutViewModel extends ViewModel {
    private static final String TAG = "AboutViewModel";
    private MutableLiveData<String> mVersion = new MutableLiveData<>();

    public AboutViewModel() {
        mVersion.setValue(BuildConfig.VERSION_NAME);
    }

    public MutableLiveData<String> getVersion() {
        return mVersion;
    }
}
