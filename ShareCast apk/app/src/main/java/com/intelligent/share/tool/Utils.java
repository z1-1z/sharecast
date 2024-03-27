package com.intelligent.share.tool;

import androidx.navigation.NavOptions;

import com.intelligent.share.R;

import java.text.DecimalFormat;

import smart.share.TranGlobalInfo;

/**
 * @author xxx
 * @date 2024/1/25
 */
public class Utils {

    public static boolean isDeviceConnect() {
        return TranGlobalInfo.getCurDeviceInfo() != null
                && TranGlobalInfo.getCurDeviceInfo().getDevice_sn_disp() != null
                && TranGlobalInfo.getCurDeviceInfo().getmConnectStatus() >= 0;
    }

    public static String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static boolean isHarmonyOs() {
        try {
            Class<?> buildExClass = Class.forName("com.huawei.system.BuildEx");
            Object osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass);
            return "Harmony".equalsIgnoreCase(osBrand.toString());
        } catch (Throwable x) {
            return false;
        }
    }

    public static NavOptions getNavOptions(){
        return isHarmonyOs() ? null : new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();
    }


}
