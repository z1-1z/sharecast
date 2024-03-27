
package com.tosmart.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tosmart.dlna.application.BaseApplication;
import com.tosmart.dlna.util.Action;
import com.tosmart.dlna.util.CommonToast;
import com.tosmart.dlna.util.Constant;

public class GetTransportInfoCallback extends GetTransportInfo {
    private static final String TAG = "GetTransportInfoCallbac";

    private Handler handler;
    private boolean isOnlyGetState;
    private int type;
    private DMCControl dmcControl;

    public GetTransportInfoCallback(Service paramService, DMCControl dmcControl,Handler paramHandler,
                                    boolean paramBoolean, int paramInt) {
        super(paramService);
        this.handler = paramHandler;
        this.isOnlyGetState = paramBoolean;
        this.type = paramInt;
        this.dmcControl = dmcControl;
    }

    @Override
    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
                        String paramString) {
        Log.i(TAG, "[xxx] failure: paramString = " + paramString);
        Log.i(TAG, "[xxx] failure: paramUpnpResponse = " + paramUpnpResponse);
        if (dmcControl != null && !dmcControl.mIsExit) {
            if (!this.isOnlyGetState) {
                if (this.type == 1) {
                    this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
                } else if (this.type == 2) {
                    this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
                } else if (this.type == 3) {
                    this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
                }
            }
        }
    }

    @Override
    public void received(ActionInvocation paramActionInvocation, TransportInfo paramTransportInfo) {
        Log.e(TAG, "[xxx] received: " + paramTransportInfo.getCurrentTransportState());
        Log.e(TAG, "[xxx] received: " + paramTransportInfo.getCurrentTransportStatus());
        Log.e(TAG, "[xxx] received: isOnlyGetState=" + Boolean.toString(this.isOnlyGetState));
        if (dmcControl != null && !dmcControl.mIsExit) {
            Log.e(TAG, "[xxx] received: dmcControl.mIsExit = " + dmcControl.mIsExit);
            if (!this.isOnlyGetState) {
                this.handler.sendEmptyMessage(DMCControlMessage.SETURL);
            } else {
                Intent intent = new Intent(Action.PLAY_TRANSPORT_STATE);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_DLNA_PLAY_STATE, paramTransportInfo.getCurrentTransportState().name());
                intent.putExtras(bundle);
                BaseApplication.getContext().sendBroadcast(intent);
            }
        }
    }
}
