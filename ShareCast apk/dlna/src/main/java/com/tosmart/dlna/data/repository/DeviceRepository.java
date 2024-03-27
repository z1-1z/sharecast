package com.tosmart.dlna.data.repository;

import android.annotation.SuppressLint;
import androidx.lifecycle.MediatorLiveData;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.tosmart.dlna.application.BaseApplication;
import com.tosmart.dlna.dmp.DeviceItem;
import com.tosmart.dlna.dmr.ZxtMediaRenderer;
import com.tosmart.dlna.dms.ContentTree;
import com.tosmart.dlna.dms.MediaServer;
import com.tosmart.dlna.util.ConfigureUtil;
import com.tosmart.dlna.util.TaskThreadPoolExecutor;
import com.tosmart.dlna.util.Utils;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by xxx on 2019/4/25.
 */
public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private static DeviceRepository sInstance;
    private MediatorLiveData<ArrayList<DeviceItem>> mDevListLiveData = new MediatorLiveData<>();
    private MediatorLiveData<ArrayList<DeviceItem>> mDmrListLiveData = new MediatorLiveData<>();
    private MediatorLiveData<DeviceItem> mDeviceItem = new MediatorLiveData<>();
    private MediatorLiveData<DeviceItem> mDmrDeviceItem = new MediatorLiveData<>();
    private ArrayList<DeviceItem> mDevList = new ArrayList<>();
    private ArrayList<DeviceItem> mDmrList = new ArrayList<>();
    private AndroidUpnpService mUpnpService;
    private DeviceListRegistryListener mDeviceListRegistryListener = new DeviceListRegistryListener();
    private MediaServer mMediaServer;
    private MediatorLiveData<Boolean> mIsLocalDmr = new MediatorLiveData<>();


    public synchronized static DeviceRepository obtain() {
        if (sInstance == null) {
            sInstance = new DeviceRepository();
        }
        return sInstance;
    }

    private DeviceRepository() {
        mIsLocalDmr.setValue(true);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDevList.clear();
            mDevListLiveData.postValue(mDevList);
            mDmrList.clear();
            mDmrListLiveData.postValue(mDmrList);
            mUpnpService = (AndroidUpnpService) service;
            Log.v(TAG, "Connected to UPnP Service");

            if ( ConfigureUtil.getDmsOn(BaseApplication.getContext())) {
                try {
                    if (mMediaServer == null) {
                        mMediaServer = new MediaServer(BaseApplication.getContext());
                    }
                    mUpnpService.getRegistry()
                            .addDevice(mMediaServer.getDevice());
                    DeviceItem localDevItem = new DeviceItem(
                            mMediaServer.getDevice());

                    mDeviceListRegistryListener.deviceAdded(localDevItem);
                    // init RootNode ahead of here，
                    // To prevent init library but RootNode not initialize after main thread request permission
                    ContentTree.init();
                    BaseApplication.setIsServiceInit(true);
                    LibraryRepository.getInstance().getIsInitComplete().setValue(false);
                    requestHostInfo();
                } catch (Exception ex) {
                    Log.i(TAG, "onServiceConnected: " + ex);
                    return;
                }
            }

            if (ConfigureUtil.getRenderOn(BaseApplication.getContext())) {
                ZxtMediaRenderer mediaRenderer = new ZxtMediaRenderer(1,
                        BaseApplication.getContext());
                mUpnpService.getRegistry().addDevice(mediaRenderer.getDevice());
                mDeviceListRegistryListener.dmrAdded(new DeviceItem(
                        mediaRenderer.getDevice()));
            }

            // xgf
            for (Device device : mUpnpService.getRegistry().getDevices()) {
                if (device.getType().getNamespace().equals("schemas-upnp-org")
                        && device.getType().getType().equals("MediaServer")) {
                    final DeviceItem display = new DeviceItem(device, device
                            .getDetails().getFriendlyName(),
                            device.getDisplayString(), "(REMOTE) "
                            + device.getType().getDisplayString());
                    mDeviceListRegistryListener.deviceAdded(display);
                }
            }

            // Getting ready for future device advertisements
            mUpnpService.getRegistry().addListener(mDeviceListRegistryListener);
            // Refresh device list
            mUpnpService.getControlPoint().search();

            // select first device by default
            if (null != mDevList && mDevList.size() > 0
                    && null == mDeviceItem.getValue()) {
                mDeviceItem.setValue(mDevList.get(0));
            }
            if (null != mDmrList && mDmrList.size() > 0
                    && null == mDmrDeviceItem.getValue()) {
                mDmrDeviceItem.setValue(mDmrList.get(0));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mUpnpService = null;
        }
    };

    @SuppressLint("DefaultLocale")
    public void requestHostInfo() {
        TaskThreadPoolExecutor.getInstance().execute(() -> {
            @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) BaseApplication.getContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();

            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff)));
                BaseApplication.setHostAddress(inetAddress.getHostAddress());
                BaseApplication.setHostName(inetAddress.getHostName());
                PlayListRepository.getInstance().queryPlayContentItems();
                Log.i(TAG, "[xxx] requestHostInfo:" + BaseApplication.HasPermission());
                // if has permission ,init Library
                if (BaseApplication.HasPermission()) {
                    LibraryRepository.getInstance().init();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
    }

    public void onDestroy() {
        if (mUpnpService != null) {
            mUpnpService.getRegistry()
                    .removeListener(mDeviceListRegistryListener);
        }
        BaseApplication.setIsServiceInit(false);
    }

    public class DeviceListRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
            Log.d(TAG, "remoteDeviceDiscoveryStarted() called with: registry = [" + registry + "], device = [" + device + "]");
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                final RemoteDevice device, final Exception ex) {
            Log.d(TAG, "remoteDeviceDiscoveryFailed() called with: registry = [" + registry + "], device = [" + device + "], ex = [" + ex + "]");
        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.d(TAG, "remoteDeviceAdded() called with: registry = [" + registry + "], device = [" + device + "]");
            remoteDeviceAdd(device);
        }

        public void remoteDeviceAdd(RemoteDevice device) {
            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaServer")) {
                final DeviceItem display = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                deviceAdded(display);
            }

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrAdded(dmrDisplay);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.d(TAG, "remoteDeviceRemoved() called with: registry = [" + registry + "], device = [" + device + "]");
            remoteDeviceRemoved(device);
        }

        public void remoteDeviceRemoved(RemoteDevice device) {
            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrRemoved(dmrDisplay);
            }
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.e(TAG,
                    "localDeviceAdded:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device, device
                    .getDetails().getFriendlyName(), device.getDisplayString(),
                    "(REMOTE) " + device.getType().getDisplayString());
            deviceAdded(display);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.e(TAG,
                    "localDeviceRemoved:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);
        }

        public void deviceAdded(final DeviceItem di) {
            if (mDevList.contains(di)) {
                mDevList.remove(di);
            }
            mDevList.add(di);
            mDevListLiveData.postValue(mDevList);
        }

        public void deviceRemoved(final DeviceItem di) {
            mDevList.remove(di);
            mDevListLiveData.postValue(mDevList);
        }

        public void dmrAdded(final DeviceItem di) {
            if (mDmrList.contains(di)) {
                mDevList.remove(di);
            }
            mDmrList.add(di);
            mDmrListLiveData.postValue(mDmrList);
        }

        public void dmrRemoved(final DeviceItem di) {
            mDmrList.remove(di);
            mDmrListLiveData.postValue(mDmrList);
        }
    }

    public MediatorLiveData<ArrayList<DeviceItem>> getDevListLiveData() {
        return mDevListLiveData;
    }

    public MediatorLiveData<ArrayList<DeviceItem>> getDmrListLiveData() {
        return mDmrListLiveData;
    }

    public MediatorLiveData<DeviceItem> getDeviceItem() {
        return mDeviceItem;
    }

    public void setDeviceItem(DeviceItem deviceItem) {
        mDeviceItem.postValue(deviceItem);
    }

    public MediatorLiveData<DeviceItem> getDmrDeviceItem() {
        return mDmrDeviceItem;
    }

    public void setDmrDeviceItem(DeviceItem dmrDeviceItem) {
        if (null != dmrDeviceItem.getDevice()
                && null != mDeviceItem.getValue()
                && null != dmrDeviceItem.getDevice()
                .getDetails().getModelDetails()
                && Utils.DMR_NAME.equals(dmrDeviceItem
                .getDevice().getDetails().getModelDetails()
                .getModelName())
                && Utils.getDevName(
                dmrDeviceItem.getDevice().getDetails()
                        .getFriendlyName()).equals(
                Utils.getDevName(mDeviceItem.getValue()
                        .getDevice().getDetails()
                        .getFriendlyName()))) {
            mIsLocalDmr.setValue(true);
        } else {
            mIsLocalDmr.setValue(false);
        }
        mDmrDeviceItem.postValue(dmrDeviceItem);
    }

    public MediaServer getMediaServer() {
        return mMediaServer;
    }

    public AndroidUpnpService getUpnpService() {
        return mUpnpService;
    }

    public boolean isIsLocalDmr() {
        return mIsLocalDmr.getValue();
    }

    public MediatorLiveData<Boolean> getIsLocalDmr() {
        return mIsLocalDmr;
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.mServiceConnection = serviceConnection;
    }
}
