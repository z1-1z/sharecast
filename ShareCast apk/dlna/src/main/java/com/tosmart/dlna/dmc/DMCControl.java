package com.tosmart.dlna.dmc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tosmart.dlna.R;
import com.tosmart.dlna.dmp.DeviceItem;
import com.tosmart.dlna.util.Action;
import com.tosmart.dlna.util.CommonToast;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

import java.lang.ref.WeakReference;

public class DMCControl {
    private static final String TAG = "DMCControl";
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_AUDIO = 2;
    private static final int TYPE_VIDEO = 3;
    private static final int CUT_VOC = 0;
    private static final int ADD_VOC = 1;
    public boolean mIsExit = false;
    private Activity mActivity;
    private int mControlType = 1;
    private DeviceItem mExecuteDeviceItem;
    public boolean mIsMute = false;
    private String mMetaData;
    private AndroidUpnpService mUpnpService;
    private String mUriString;
    private Handler mHandle;
    private int mGetProtocolInfoFailedTimes = 0;
    private String mCurrentContentFormatMimeType;

    public static class WeakHandler extends Handler {

        private WeakReference<DMCControl> mWeakReference;
        protected DMCControl mOwner;

        public WeakHandler(DMCControl owner) {
            mWeakReference = new WeakReference<>(owner);
        }

        public WeakHandler(DMCControl owner, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            mOwner = mWeakReference.get();
            if (mOwner == null) {
                return;
            }
            Log.i(TAG, "[xxx] handleMessage: " + mOwner + msg.what);
            switch (msg.what) {

                case DMCControlMessage.ADDVOLUME: {

                    break;
                }

                case DMCControlMessage.CONNECTIONFAILED: {
                    Log.i(TAG, "[xxx] handleMessage: mOwner.mGetProtocolInfoFailedTimes = " + mOwner.mGetProtocolInfoFailedTimes);
                    mOwner.mGetProtocolInfoFailedTimes++;
                    if (mOwner.mGetProtocolInfoFailedTimes >= 2) {
                        mOwner.setPlayErrorMessage();
                        mOwner.mGetProtocolInfoFailedTimes = 0;
                    } else {
                        mOwner.getProtocolInfo(mOwner.mCurrentContentFormatMimeType);
                    }
                    break;
                }

                case DMCControlMessage.CONNECTIONSUCESSED: {
                    mOwner.mGetProtocolInfoFailedTimes = 0;
                    mOwner.getTransportInfo(false);
                    break;
                }

                case DMCControlMessage.GETMUTE: {
                    mOwner.mIsMute = msg.getData().getBoolean("mute");
                    break;
                }

                case DMCControlMessage.GETMEDIA: {
                    mOwner.getPositionInfoDelay();
                    mOwner.getTransportInfoDelay();
                    break;
                }

                case DMCControlMessage.GETPOTITION: {
                    mOwner.getPositionInfo();
                    mOwner.getPositionInfoDelay();
                    break;
                }

                case DMCControlMessage.GETTRANSPORTINFO: {
                    mOwner.getTransportInfo(true);
                    mOwner.getTransportInfoDelay();
                    break;
                }

                case DMCControlMessage.GET_CURRENT_VOLUME: {

                    break;
                }

                case DMCControlMessage.PAUSE: {
                    Log.i(TAG, "PAUSE: ");
                    mOwner.stopGetPosition();
                    break;
                }

                case DMCControlMessage.PLAY: {
                    mOwner.play();
                    break;
                }

                case DMCControlMessage.PLAYAUDIOFAILED: {
                    mOwner.setPlayErrorMessage();
                    break;
                }

                case DMCControlMessage.PLAYIMAGEFAILED: {
                    mOwner.setPlayErrorMessage();
                    break;
                }

                case DMCControlMessage.PLAYVIDEOFAILED: {
                    mOwner.setPlayErrorMessage();
                    break;
                }

                case DMCControlMessage.PLAYMEDIAFAILED: {
                    mOwner.setPlayErrorMessage();
                    mOwner.stopGetPosition();
                    break;
                }
                case DMCControlMessage.REDUCEVOLUME: {

                    break;
                }

                case DMCControlMessage.REMOTE_NOMEDIA: {

                    break;
                }

                case DMCControlMessage.SETMUTE: {
                    mOwner.mIsMute = msg.getData().getBoolean("mute");
                    mOwner.setMute(!mOwner.mIsMute);
                    break;
                }

                case DMCControlMessage.SETMUTESUC: {
                    mOwner.mIsMute = msg.getData().getBoolean("mute");
                    break;
                }

                case DMCControlMessage.SETURL: {
                    mOwner.setAvURL(true);
                    break;
                }

                case DMCControlMessage.SETVOLUME: {
                    if (msg.getData().getInt("isSetVolume") == CUT_VOC) {
                        mOwner.setVolume(msg.getData().getLong("getVolume"), CUT_VOC);
                    } else {
                        mOwner.setVolume(msg.getData().getLong("getVolume"), ADD_VOC);
                    }
                    break;
                }

                case DMCControlMessage.STOP: {
                    mOwner.destory();
                    break;
                }

                case DMCControlMessage.UPDATE_PLAY_TRACK: {

                    break;
                }

            }
        }
    }

    public DMCControl(Activity paramActivity, int paramInt,
                      DeviceItem paramDeviceItem,
                      AndroidUpnpService paramAndroidUpnpService, String paramString1,
                      String paramString2) {
        this.mActivity = paramActivity;
        this.mControlType = paramInt;
        this.mExecuteDeviceItem = paramDeviceItem;
        this.mUpnpService = paramAndroidUpnpService;
        this.mUriString = paramString1;
        this.mMetaData = paramString2;
        this.mHandle = new WeakHandler(this);
    }

    private void getPositionInfoDelay() {
        if (!mIsExit && mControlType != TYPE_IMAGE && mHandle != null) {
            mHandle.removeMessages(DMCControlMessage.GETPOTITION);
            mHandle.sendEmptyMessageDelayed(
                    DMCControlMessage.GETPOTITION, 1000);
        }
    }

    private void getTransportInfoDelay() {
        if (!mIsExit && mControlType != TYPE_IMAGE && mHandle != null) {
            mHandle.removeMessages(DMCControlMessage.GETTRANSPORTINFO);
            mHandle.sendEmptyMessageDelayed(
                    DMCControlMessage.GETTRANSPORTINFO, 1000);
        }
    }

    private void setPlayErrorMessage() {
        Intent localIntent = new Intent();
        if (this.mControlType == TYPE_VIDEO) {
            localIntent.setAction(Action.PLAY_ERR_VIDEO);
        } else if (this.mControlType == TYPE_AUDIO) {
            localIntent.setAction(Action.PLAY_ERR_AUDIO);
        } else {
            localIntent.setAction(Action.PLAY_ERR_IMAGE);
        }
        mActivity.sendBroadcast(localIntent);
    }

    public void stopGetPosition() {
        if (mHandle != null) {
            mHandle.removeMessages(DMCControlMessage.GETPOTITION);
        }
    }

    public void getCurrentConnectionInfo(int paramInt) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("ConnectionManager"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new CurrentConnectionInfoCallback(localService,
                                this.mUpnpService.getControlPoint(), paramInt));
            } else {
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void getDeviceCapability() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new GetDeviceCapabilitiesCallback(localService));
            } else {
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void getMediaInfo() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new GetMediaInfoCallback(localService));
            } else {
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void getMute() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("RenderingControl"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new GetMuteCallback(localService, mHandle));
            } else {
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void getPositionInfo() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new GetPositionInfoCallback(DMCControl.this, localService, mHandle,
                                this.mActivity));
            } else {
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void getProtocolInfo(String paramString) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("ConnectionManager"));
            if (localService != null) {
                mCurrentContentFormatMimeType = paramString;
                this.mUpnpService.getControlPoint().execute(
                        new GetProtocolInfoCallback(localService,
                                this.mUpnpService.getControlPoint(),
                                mCurrentContentFormatMimeType, mHandle));
            } else {
                Log.i(TAG, "[xxx] getProtocolInfo: localService == null");
            }
        } catch (Exception localException) {
            Log.i(TAG, "[xxx] getProtocolInfo: " + localException);
            localException.printStackTrace();
        }
    }

    public void getTransportInfo(boolean paramBoolean) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new GetTransportInfoCallback(localService, DMCControl.this, mHandle,
                                paramBoolean, this.mControlType));
            } else {
                Log.i(TAG, "[xxx] getTransportInfo: localService == null");
            }
        } catch (Exception localException) {
            Log.i(TAG, "[xxx] getTransportInfo: " + localException);
            localException.printStackTrace();
        }
    }

    public void getVolume(int paramInt) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("RenderingControl"));
            if (localService != null) {
                Log.e("get volume", "get volume");
                this.mUpnpService.getControlPoint().execute(
                        new GetVolumeCallback(this.mActivity, mHandle, paramInt,
                                localService, this.mControlType));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void pause() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                Log.e("pause", "pause");
                this.mUpnpService.getControlPoint().execute(
                        new PauseCallback(localService, mHandle));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void play() {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                Log.e("start play", "start play");
                this.mUpnpService.getControlPoint().execute(
                        new PlayerCallback(localService, mHandle));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    @SuppressLint("LongLogTag")
    public void seekBarPosition(String paramString) {
        try {
            Device localDevice = this.mExecuteDeviceItem.getDevice();
            Log.e("control action", "seekBarPosition");
            Service localService = localDevice.findService(new UDAServiceType(
                    "AVTransport"));
            if (localService != null) {
                Log.e("get seekBarPosition info", "get seekBarPosition info");
                this.mUpnpService.getControlPoint().execute(
                        new SeekCallback(mActivity, localService, paramString,
                                mHandle));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void setAvURL(boolean isGoPlay) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                Log.e("set url", "set url" + this.mUriString);
                this.mUpnpService.getControlPoint().execute(
                        new SetAVTransportURIActionCallback(localService,
                                this.mUriString, this.mMetaData, mHandle,
                                this.mControlType, isGoPlay));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void setCurrentPlayPath(String paramString) {
        mUriString = paramString;
    }

    public void setCurrentPlayPath(String paramString1, String paramString2) {
        mUriString = paramString1;
        mMetaData = paramString2;
    }

    public void setMute(boolean paramBoolean) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("RenderingControl"));
            if (localService != null) {
                ControlPoint localControlPoint = this.mUpnpService
                        .getControlPoint();
                localControlPoint.execute(new SetMuteCalllback(localService,
                        paramBoolean, mHandle));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void setVolume(long paramLong, int paramInt) {
        if (paramInt == 0) {
        }
        Service localService = null;
        try {
            localService = this.mExecuteDeviceItem.getDevice().findService(
                    new UDAServiceType("RenderingControl"));
            if (localService != null) {
                if (paramInt == CUT_VOC) {
                    if (paramLong >= 0L) {
                        paramLong -= 1L;
                    } else {
                        CommonToast.obtain().show(R.string.min_voc);
                    }
                } else {
                    paramLong += 1L;
                }
                this.mUpnpService.getControlPoint().execute(
                        new SetVolumeCallback(localService, paramLong));
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void stop(Boolean paramBoolean) {
        try {
            Service localService = this.mExecuteDeviceItem.getDevice()
                    .findService(new UDAServiceType("AVTransport"));
            if (localService != null) {
                this.mUpnpService.getControlPoint().execute(
                        new StopCallback(localService, mHandle, paramBoolean,
                                this.mControlType));
            } else {
                Log.e("null", "null");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void destory() {
        mIsExit = true;
        if (mHandle != null) {
            mHandle.removeCallbacksAndMessages(null);
            mHandle = null;
        }
    }

}
