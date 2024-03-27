
package com.tosmart.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Pause;

import android.os.Handler;
import android.util.Log;

public class PauseCallback extends Pause {
    private Handler handler;

    public PauseCallback(Service paramService, Handler paramHandler) {
        super(paramService);
        this.handler = paramHandler;
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
            String paramString) {
        handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
        Log.e("pause failed", "pause failed");
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        Log.e("pause success", "pause success");
        handler.sendEmptyMessage(DMCControlMessage.PAUSE);
    }

}
