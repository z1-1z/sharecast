package com.intelligent.share.setting;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class SettingItemViewModel extends ViewModel {
    private static final String TAG = "SettingItemViewModel";
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private MutableLiveData<Drawable> mIcon = new MutableLiveData<>();

    public SettingItemViewModel(String name, Drawable icon) {
        mName.setValue(name);
        mIcon.setValue(icon);
    }


    public MutableLiveData<String> getName() {
        return mName;
    }

    public MutableLiveData<Drawable> getIcon() {
        return mIcon;
    }
}
