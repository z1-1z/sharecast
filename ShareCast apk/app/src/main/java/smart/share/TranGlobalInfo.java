package smart.share;

import smart.share.dataconvert.parser.ParserFactory;

public class TranGlobalInfo {
    public static final int CLIENT_TYPE_MASTER = 0;                                // master client can do everything mInputStream mobile
	// application.
    public static final int CLIENT_TYPE_SLAVE = 1;                                // slave client only can view content, can't change content.


    public static final int SOCKET_TIME_OUT_EXCEPTION = -1;

    public static boolean check_is_apk_match_platform(int platform_id) {
        return true;
    }

    private static MobileLoginInfo curDeviceInfo = null;

    public static MobileLoginInfo getCurDeviceInfo() {
        if (curDeviceInfo == null) {
            curDeviceInfo = new MobileLoginInfo();
        }
        return curDeviceInfo;
    }

    public static void setCurDeviceInfo(MobileLoginInfo curDeviceInfo) {
        TranGlobalInfo.curDeviceInfo = curDeviceInfo;
        ParserFactory.setDataType(curDeviceInfo.getSend_data_type());
    }

    private static boolean isLowPlatform() {

        return false;
    }


}
