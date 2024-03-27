package com.intelligent.share.device;

import static smart.share.GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_REACHABLE;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.R;
import com.intelligent.share.base.ShareApp;
import com.intelligent.share.socketthread.SocketReceiveThread;
import com.tosmart.dlna.util.ThreadUtils;

import java.util.ArrayList;

import smart.share.CreateSocket;
import smart.share.EditLoginHistoryFile;
import smart.share.GlobalConstantValue;
import smart.share.ConnectToDevice;
import smart.share.MobileLoginInfo;
import smart.share.TranGlobalInfo;
import smart.share.message.process.MessageProcessor;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class DeviceViewModel extends ViewModel {
    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<ArrayList<DeviceItemModel>> mDeviceList = new MutableLiveData<>();
    private MutableLiveData<DeviceItemModel> mConnectedDevice = new MutableLiveData<>();
    private SocketReceiveThread mSocketReceiveThread;
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private MutableLiveData<ArrayList<DeviceItemModel>> mHistoryConnectedDevice = new MutableLiveData<>();
    private ArrayList<MobileLoginInfo> mCurDeviceList;


    public DeviceViewModel() {
        refreshHistoryConnectedList();
    }

    private void refreshHistoryConnectedList() {
        ArrayList<MobileLoginInfo> historyList = EditLoginHistoryFile.obtain(ShareApp.getAppContext()).getListFromFile();
        ArrayList<DeviceItemModel> onlineList = mDeviceList.getValue();
        ArrayList<DeviceItemModel> list = new ArrayList<>();
        Log.i(TAG, "[xxx] refreshHistoryConnectedList: onlineList " + onlineList);
        for (MobileLoginInfo gsMobileLoginInfo : historyList) {
            boolean isOnline = false;
            if (onlineList != null) {
                for (DeviceItemModel deviceItemModel : onlineList) {
                    if (deviceItemModel.getMobileLoginInfo().getModel_name().trim().equals(gsMobileLoginInfo.getModel_name().trim())
                            && deviceItemModel.getMobileLoginInfo().getDevice_sn_disp().trim().equals(gsMobileLoginInfo.getDevice_sn_disp().trim())) {
                        isOnline = true;
                        break;
                    }
                }
            }
            list.add(new DeviceItemModel(gsMobileLoginInfo, isOnline));
        }
        mHistoryConnectedDevice.setValue(list);
    }

    public void setDeviceList(ArrayList<MobileLoginInfo> deviceList) {
        Log.i(TAG, "[xxx] setDeviceList:" + deviceList.size());

        ArrayList<DeviceItemModel> list = new ArrayList<>();

        for (MobileLoginInfo gsMobileLoginInfo : deviceList) {
            list.add(new DeviceItemModel(gsMobileLoginInfo));
        }
        if(TranGlobalInfo.getCurDeviceInfo() != null
                && TranGlobalInfo.getCurDeviceInfo().getDevice_sn_disp() != null
                && TranGlobalInfo.getCurDeviceInfo().getmConnectStatus() >= 0){
            boolean isExit = false;
            for (MobileLoginInfo gsMobileLoginInfo : deviceList) {
                if (TranGlobalInfo.getCurDeviceInfo().getDevice_sn_disp().equals(gsMobileLoginInfo.getDevice_sn_disp())) {
                    isExit = true;
                }
            }
            if (!isExit && mSocketReceiveThread != null) {
                CreateSocket.DestroySocket();
                mSocketReceiveThread.interrupt();
                TranGlobalInfo.getCurDeviceInfo().setmConnectStatus(CONNECT_DEVICE_ERROR_NOT_REACHABLE);
            }
        }

        mDeviceList.setValue(list);
        refreshHistoryConnectedList();
    }

    public void refresh() {
        ArrayList<DeviceItemModel> list = mDeviceList.getValue();
        for (DeviceItemModel deviceItemModel : list) {
            deviceItemModel.updateState();
        }
        mDeviceList.setValue(list);
    }

    public void setConnectedDevice(DeviceItemModel connectedDevice) {
        mConnectedDevice.setValue(connectedDevice);
    }

    public void connect(DeviceItemModel deviceItemModel , Activity activity) {
        if (deviceItemModel.getIsSel().getValue()) {
            Toast.makeText(ShareApp.getAppContext(), R.string.str_already_connected,Toast.LENGTH_SHORT).show();
            return;
        }
        if (!deviceItemModel.getIsOnline().getValue()) {
            Toast.makeText(ShareApp.getAppContext(), R.string.str_not_online,Toast.LENGTH_SHORT).show();
            return;
        }
        mIsLoading.setValue(true);
        if (mSocketReceiveThread != null) {
            mSocketReceiveThread.interrupt();
        }
        mSocketReceiveThread = new SocketReceiveThread();
        MobileLoginInfo mobileLoginInfo = deviceItemModel.getMobileLoginInfo();
        mobileLoginInfo.setmIpLoginMark(0);
        System.out.println("click ip : " + mobileLoginInfo.getDevice_ip_address_disp());
        String address = mobileLoginInfo.getDevice_ip_address_disp();
        ThreadUtils.execute(() -> {
            System.out.println("click ip Address : " + address);
            final MobileLoginInfo loginInfoTemp = ConnectToDevice.connecttoserver(address,
                    GlobalConstantValue.LOGIN_DEFAULT_PORT_NUM, GlobalConstantValue.CONNECT_TYEP_AUTO_LOGIN);
            TranGlobalInfo.setCurDeviceInfo(loginInfoTemp);

            if (loginInfoTemp.getmConnectStatus() < 0) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectToDevice.makeTextForConnectError(activity, loginInfoTemp.getmConnectStatus());
                    }
                });
            } else {
                EditLoginHistoryFile.obtain(ShareApp.getAppContext()).putListToFile(loginInfoTemp,
                        EditLoginHistoryFile.obtain(ShareApp.getAppContext()).getListFromFile());
                mSocketReceiveThread.start();
            }
            Log.i(TAG, "[xxx] connect: getDevice_sn_disp " +loginInfoTemp.getDevice_sn_disp() );
            Log.i(TAG, "[xxx] connect:  getmConnectStatus " +loginInfoTemp.getmConnectStatus() );
            activity.runOnUiThread(this::refresh);
            activity.runOnUiThread(this::refreshHistoryConnectedList);
            mIsLoading.postValue(false);
            Message isLogin = Message.obtain();
            isLogin.what = GlobalConstantValue.CONNECT_TYEP_AUTO_LOGIN;
            isLogin.arg1 = loginInfoTemp.getmConnectStatus();
            isLogin.obj = address;
            MessageProcessor.obtain().postMessage(isLogin);
        });
    }

    public MutableLiveData<ArrayList<DeviceItemModel>> getDeviceList() {
        return mDeviceList;
    }

    public MutableLiveData<ArrayList<DeviceItemModel>> getHistoryConnectedDevice() {
        return mHistoryConnectedDevice;
    }

    public MutableLiveData<DeviceItemModel> getConnectedDevice() {
        return mConnectedDevice;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }
}
