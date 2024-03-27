package com.intelligent.share.device;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import smart.share.TranGlobalInfo;
import smart.share.MobileLoginInfo;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class DeviceItemModel extends ViewModel {
    private static final String TAG = "DeviceItemModel";
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsSel = new MutableLiveData<>();
    private MutableLiveData<String> mSn = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsOnline = new MutableLiveData<>();
    private MobileLoginInfo mMobileLoginInfo;


    public DeviceItemModel(MobileLoginInfo gsMobileLoginInfo) {
        this(gsMobileLoginInfo, true);
    }

    public DeviceItemModel(MobileLoginInfo gsMobileLoginInfo, boolean isOnline) {
        mMobileLoginInfo = gsMobileLoginInfo;
        mName.setValue(mMobileLoginInfo.getModel_name().trim());
        mIsOnline.setValue(isOnline);
        mSn.setValue(mMobileLoginInfo.getDevice_sn_disp());
        updateState();
    }

    public void updateState() {
        mIsSel.setValue(TranGlobalInfo.getCurDeviceInfo() != null
                && TranGlobalInfo.getCurDeviceInfo().getDevice_sn_disp() != null
                && TranGlobalInfo.getCurDeviceInfo().getDevice_sn_disp().equals(mMobileLoginInfo.getDevice_sn_disp())
                && TranGlobalInfo.getCurDeviceInfo().getmConnectStatus() >= 0);
    }

    public MutableLiveData<String> getName() {
        return mName;
    }

    public MutableLiveData<Boolean> getIsSel() {
        return mIsSel;
    }

    public MutableLiveData<String> getSn() {
        return mSn;
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return mIsOnline;
    }

    public MobileLoginInfo getMobileLoginInfo() {
        return mMobileLoginInfo;
    }
}
