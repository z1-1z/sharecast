
package com.tosmart.dlna.dmc;

import android.os.Handler;
import android.util.Log;

import com.tosmart.dlna.util.CommonToast;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

public class SetAVTransportURIActionCallback extends SetAVTransportURI {
    private static final String TAG = "SetAVTransportURIAction";

    private Handler handler;
    private int type;
    private boolean isGoPlay = false;

    public SetAVTransportURIActionCallback(Service paramService, String paramString1,
            String paramString2, Handler paramHandler, int paramInt) {
        this(paramService, paramString1, paramString2, paramHandler, paramInt, true);
    }

    public SetAVTransportURIActionCallback(Service paramService, String paramString1,
                                           String paramString2, Handler paramHandler, int paramInt,boolean isGoPlay) {
        super(paramService, paramString1, paramString2);
        this.handler = paramHandler;
        this.isGoPlay = isGoPlay;
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        Log.i(TAG, "failure: defaultMsg = " + defaultMsg);
        Log.i(TAG, "failure: operation = " + operation);
        if (operation != null) {
            CommonToast.obtain().show(operation.getStatusMessage());
        }
        if (this.type == 1)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
        if (this.type == 2)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
        if (this.type == 3)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
    }

    @Override
    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        if (isGoPlay) {
            try {
                Thread.sleep(2000L);
                this.handler.sendEmptyMessage(DMCControlMessage.PLAY);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }
    }

}
