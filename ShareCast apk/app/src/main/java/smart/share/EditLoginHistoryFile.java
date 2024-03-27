package smart.share;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class EditLoginHistoryFile {
	private static final String TAG = "EditLoginHistoryFile";
	private Context mContext;
	private SharedPreferences mLoginHistoryFile;
	private SharedPreferences.Editor mEditor;
	private int mHistoryNum;
	private MobileLoginInfo deviceModel;
	
	private static final String MODEL_INFO = "ModelInfo";
	private static final String SN_INFO = "SnInfo";
	private static final String IP_INFO = "IPInfo";
	private static final String IS_IP_LOGIN = "IsIPLogin";
	private static final String PLATFORM_ID = "PlatformId";
	private static final String SW_VERSION = "SwVersion";
	private static final String CUSTOMER_ID = "CustomerId";
	private static final String MODEL_ID = "ModelId";
	private static final String SW_SUB_VERSION = "SwSubVersion";
	private static final String UPNP_PORT = "UpnpPort";
	private static EditLoginHistoryFile mInstance;

	private EditLoginHistoryFile(Context context)
	{
		this.mContext = context;
	}

	public static synchronized EditLoginHistoryFile obtain(Context context) {
		if (null == mInstance) {
			mInstance = new EditLoginHistoryFile(context);
		}
		return mInstance;
	}

	public EditLoginHistoryFile(){}
	public void readFileItem(int index){
		deviceModel = new MobileLoginInfo();
		
		deviceModel.setModel_name(mLoginHistoryFile.getString(MODEL_INFO+index,""));
		deviceModel.setDevice_sn_disp(mLoginHistoryFile.getString(SN_INFO+index, ""));
		deviceModel.setDevice_ip_address_disp(mLoginHistoryFile.getString(IP_INFO+index, ""));
		deviceModel.setmIpLoginMark(mLoginHistoryFile.getInt(IS_IP_LOGIN+index, 0));
		deviceModel.setPlatform_id(mLoginHistoryFile.getInt(PLATFORM_ID+index, 0));
		deviceModel.setSw_version(mLoginHistoryFile.getInt(SW_VERSION+index, 0));
		deviceModel.setDevice_customer_id(mLoginHistoryFile.getInt(CUSTOMER_ID +index, 0));
		deviceModel.setDevice_model_id(mLoginHistoryFile.getInt(MODEL_ID +index, 0));
		deviceModel.setSw_sub_version(mLoginHistoryFile.getInt(SW_SUB_VERSION+index, 0));
		deviceModel.setUpnpPort(mLoginHistoryFile.getInt(UPNP_PORT+index, 0));
	}
	public ArrayList<MobileLoginInfo> getListFromFile(){
		ArrayList<MobileLoginInfo> mHistoryDeviceInfoList = new ArrayList<>();
		mLoginHistoryFile = mContext.getSharedPreferences("history_list_file", 0);
		mHistoryNum = mLoginHistoryFile.getInt("pointer_num", 0);
		if(mHistoryNum > 0)
		{
			for(int i = 0;i<mHistoryNum;i++)
			{
				readFileItem(i);
				mHistoryDeviceInfoList.add(deviceModel);
			}
		}
		return mHistoryDeviceInfoList;
	}
	public void getIpLoginHistoryList(ArrayList<MobileLoginInfo> mIpLoginInfoList)
	{
		mLoginHistoryFile = mContext.getSharedPreferences("history_list_file", 0);
		mHistoryNum = mLoginHistoryFile.getInt("pointer_num", 0);
		if(mHistoryNum > 0)
		{
			for(int i = 0;i<mHistoryNum;i++)
			{
				readFileItem(i);
				if((deviceModel.getmIpLoginMark() == 1) && (!ipContains(mIpLoginInfoList, deviceModel)))// if upnp may have same ip
				{
					mIpLoginInfoList.add(deviceModel);
				}
			}
		}
	}
	
	private Boolean ipContains(ArrayList<MobileLoginInfo> mIpLoginInfoList, MobileLoginInfo addDevice)
	{
		int tempIndex = 0;
		for (tempIndex = 0; tempIndex < mIpLoginInfoList.size(); tempIndex++)
		{
			if (addDevice.getDevice_ip_address_disp().equals(mIpLoginInfoList.get(tempIndex).getDevice_ip_address_disp()))
			{
				return true;
			}
		}
		return false;
	}
	
	public void putListToFile(MobileLoginInfo mLoginTemp, ArrayList<MobileLoginInfo> mHistoryDeviceInfoList){
//		Log.d(TAG, "putListToFile() called with: mLoginTemp = [" + mLoginTemp + "], mHistoryDeviceInfoList = [" + mHistoryDeviceInfoList + "]");
		mLoginHistoryFile = mContext.getSharedPreferences("history_list_file", 0);
		mHistoryNum = mHistoryDeviceInfoList.size();
		mEditor = mLoginHistoryFile.edit();
		boolean isExist = false;
        if(mLoginTemp == null || mHistoryDeviceInfoList == null)
		{
			return ;
		}
		for(int i = 0;i < mHistoryNum;i++)
		{
			if(mLoginTemp.getDevice_sn_disp().equals(mHistoryDeviceInfoList.get(i).getDevice_sn_disp()))
			{
				if(mHistoryDeviceInfoList.get(i).getmIpLoginMark() == 1)
				{
					mLoginTemp.setmIpLoginMark(mHistoryDeviceInfoList.get(i).getmIpLoginMark());
				}
				mHistoryDeviceInfoList.remove(i);
				mHistoryDeviceInfoList.add(0, mLoginTemp);
				isExist = true;
				break;
			}
		}
		if(!isExist)
		{
			if(mHistoryNum >= GlobalConstantValue.LOGIN_HISTORY_LIST_ITEM_MAX)
			{
				mHistoryDeviceInfoList.remove(GlobalConstantValue.LOGIN_HISTORY_LIST_ITEM_MAX - 1);
			}
			mHistoryDeviceInfoList.add(0,mLoginTemp);
		}
		
		mEditor.putInt("pointer_num", mHistoryDeviceInfoList.size());
		for(int i = 0;i<mHistoryDeviceInfoList.size();i++)
		{
			mEditor.putString(MODEL_INFO+i, mHistoryDeviceInfoList.get(i).getModel_name());
			mEditor.putString(SN_INFO+i, mHistoryDeviceInfoList.get(i).getDevice_sn_disp());
			mEditor.putString(IP_INFO+i, mHistoryDeviceInfoList.get(i).getDevice_ip_address_disp());
			mEditor.putInt(IS_IP_LOGIN+i, mHistoryDeviceInfoList.get(i).getmIpLoginMark());
			mEditor.putInt(PLATFORM_ID+i, mHistoryDeviceInfoList.get(i).getPlatform_id());
			mEditor.putInt(SW_VERSION+i, mHistoryDeviceInfoList.get(i).getSw_version());
			mEditor.putInt(CUSTOMER_ID +i, mHistoryDeviceInfoList.get(i).getDevice_customer_id());
			mEditor.putInt(MODEL_ID +i, mHistoryDeviceInfoList.get(i).getDevice_model_id());
			mEditor.putInt(SW_SUB_VERSION+i, mHistoryDeviceInfoList.get(i).getSw_sub_version());
			mEditor.putInt(UPNP_PORT+i, mHistoryDeviceInfoList.get(i).getUpnpPort());
			mEditor.commit();
		}
	}
}
