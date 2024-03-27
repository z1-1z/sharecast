package com.intelligent.share.setting;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.R;
import com.intelligent.share.base.ShareApp;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class SettingViewModel extends ViewModel {
    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<SettingItemViewModel> mBinFile = new MutableLiveData<>();
    private MutableLiveData<SettingItemViewModel> mAbout = new MutableLiveData<>();

    public SettingViewModel() {
        mBinFile.setValue(new SettingItemViewModel(ShareApp.getAppContext().getString(R.string.str_bin_file),
                ShareApp.getAppContext().getDrawable(R.drawable.setting_bin_icon)));
        mAbout.setValue(new SettingItemViewModel(ShareApp.getAppContext().getString(R.string.str_about_app),
                ShareApp.getAppContext().getDrawable(R.drawable.setting_about_icon)));
    }


    public MutableLiveData<SettingItemViewModel> getBinFile() {
        return mBinFile;
    }

    public MutableLiveData<SettingItemViewModel> getAbout() {
        return mAbout;
    }
}
