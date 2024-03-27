package smart.share.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.intelligent.share.base.ShareApp;

import java.util.Locale;
import java.util.UUID;


public class AndroidDeviceUtil
{
	private static Context appContext;
	static
	{
		appContext = ShareApp.getAppContext();
	}
	public static String getIMEI()
	{
		String imei = "None";
		TelephonyManager tm = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId() != null ? tm.getDeviceId() : imei;
	}

	public static String getWifiMac()
	{
		String macAddress = "None";
		WifiManager wifiMgr = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		if (null != info)
		{
			macAddress = info.getMacAddress() != null ? info.getMacAddress() : "None";;
		}
		return macAddress;
	}

	public static boolean isTablet()
	{
		return (appContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static String getDeviceUUID()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(appContext);
		String uuid = pref.getString("device_uuid", "None");
		if (!uuid.equals("None"))
		{
			return uuid;
		}

		String imei = UUID.randomUUID().toString();
		String mac = getWifiMac();
		if (imei.equals("None") && mac.equals("None"))
		{
			imei = UUID.randomUUID().toString();
			mac = UUID.randomUUID().toString();
		}
		uuid = String.format(Locale.US, "%s-%s", imei, mac);
		pref.edit().putString("device_uuid", uuid);
		return uuid;
	}
}
