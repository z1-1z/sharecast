package com.tosmart.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.model.PositionInfo;

import com.tosmart.dlna.util.Action;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class GetPositionInfoCallback extends GetPositionInfo {
    private String TAG = "GetPositionInfoCallback";

    private Activity activity;

    private Handler handler;

    private DMCControl mDMCControl;

    public GetPositionInfoCallback(DMCControl dmcControl, Service paramService, Handler paramHandler,
                                   Activity paramActivity) {
        super(paramService);
        this.mDMCControl = dmcControl;
        this.handler = paramHandler;
        this.activity = paramActivity;
    }

    public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
        Log.e(this.TAG, "failed");
    }

    public void received(ActionInvocation paramActionInvocation,
                         PositionInfo paramPositionInfo) {
        if (mDMCControl != null && !mDMCControl.mIsExit) {
            Bundle localBundle = new Bundle();
            localBundle.putString("TrackDuration",
                    paramPositionInfo.getTrackDuration());
            localBundle.putString("RelTime", paramPositionInfo.getRelTime());
            Intent localIntent = new Intent(Action.PLAY_UPDATE);
            localIntent.putExtras(localBundle);
            activity.sendBroadcast(localIntent);
        }
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
    }

}
