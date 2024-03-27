package com.tosmart.dlna.dmc;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

public class GetProtocolInfoCallback extends GetProtocolInfo {

	private String TAG = "GetProtocolInfoCallback";

	private Handler handler;

	private boolean hasType = false;

	private String requestPlayMimeType = "";

	public GetProtocolInfoCallback(Service paramService,
			ControlPoint paramControlPoint, String paramString,
			Handler paramHandler) {
		super(paramService, paramControlPoint);
		this.requestPlayMimeType = paramString;
		this.handler = paramHandler;
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
		Log.i(TAG, "[xxx] failure: GetProtocolInfo  failure " + defaultMsg);
		if (handler != null) {
			this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONFAILED);
		}
	}

	@Override
	public void received(ActionInvocation paramActionInvocation, ProtocolInfos paramProtocolInfos1, ProtocolInfos paramProtocolInfos2) {
		if (handler != null) {
			this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONSUCESSED);
		}
	}
}
