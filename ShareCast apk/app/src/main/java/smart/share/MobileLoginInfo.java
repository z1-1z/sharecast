package smart.share;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class MobileLoginInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String TAG = "GsMobileLoginInfo";
	private static final int BROADCAST_INFO_SN_LEN = 8;
	private static final int MAX_MODEL_STRING_LENGTH = 32;
	private static final int MAX_CHIP_LEN = 8;
	private static final int RESERVED1_LEN = 3;
	private static final int RESERVED3_LEN = 19;
	
	private String magic_code;
	private byte device_sn[];
	private String device_sn_disp;
	private String model_name;;
	private byte device_cpu_chip_id[];
	private byte device_flash_id[];
	private int device_ip_address;
	private String device_ip_address_disp;
	private int platform_id;
	private int sw_version;
	private int device_customer_id;
	private int device_model_id;
	private byte reserved_1[];
	private byte reserver_2[];
	private int sw_sub_version;
	private int client_type;
	private int is_current_device_connected_full;
	private int send_data_type;

	private int like_platform_id;
	private byte reserved_3[];

	
//	private int receiveTime;CN:改用记录上次发现时间来更新发现列表
	private int mIpLoginMark;
	private int mConnectStatus;
	private String upnpIp;
	private int upnpPort;
	
	/**
	 * CN:用于记录上一次发现时间*/
	
	private long mLastFoundTime = 0;

	public int getSend_data_type()
	{
		return send_data_type;
	}

	public void setSend_data_type(int send_data_type)
	{
		this.send_data_type = send_data_type;
	}

	public String getMagic_code()
	{
		return magic_code;
	}
	public void setMagic_code(String magic_code)
	{
		this.magic_code = magic_code;
	}
	public byte[] getDevice_sn()
	{
		return device_sn;
	}
	public void setDevice_sn(byte device_sn[])
	{
		this.device_sn = device_sn;
	}
	public String getModel_name()
	{
		return model_name;
	}
	public void setModel_name(String model_name)
	{
		this.model_name = model_name;
	}
	public byte[] getDevice_cpu_chip_id()
	{
		return device_cpu_chip_id;
	}
	public void setDevice_cpu_chip_id(byte device_cpu_chip_id[])
	{
		this.device_cpu_chip_id = device_cpu_chip_id;
	}
	public byte[] getDevice_flash_id()
	{
		return device_flash_id;
	}
	public void setDevice_flash_id(byte device_flash_id[])
	{
		this.device_flash_id = device_flash_id;
	}
	public int getDevice_ip_address()
	{
		return device_ip_address;
	}
	public void setDevice_ip_address(int device_ip_address)
	{
		this.device_ip_address = device_ip_address;
	}
	public String getDevice_sn_disp()
	{
		return device_sn_disp;
	}
	public void setDevice_sn_disp(String device_sn_disp)
	{
		this.device_sn_disp = device_sn_disp;
	}
	public String getDevice_ip_address_disp()
	{
		return device_ip_address_disp;
	}
	public void setDevice_ip_address_disp(String device_ip_address_disp)
	{
		this.device_ip_address_disp = device_ip_address_disp;
	}

	public int getRealPlatform_id() {
		return platform_id;
	}

	public int getPlatform_id() {
		return platform_id;
	}

	public void setPlatform_id(int platform_id)
	{
		this.platform_id = platform_id;
	}
	public int getSw_version()
	{
		return sw_version;
	}
	public void setSw_version(int sw_version)
	{
		this.sw_version = sw_version;
	}
	public int getDevice_customer_id()
	{
		return device_customer_id;
	}
	public void setDevice_customer_id(int device_customer_id)
	{
		this.device_customer_id = device_customer_id;
	}
	public int getDevice_model_id()
	{
		return device_model_id;
	}
	public void setDevice_model_id(int device_model_id)
	{
		this.device_model_id = device_model_id;
	}
	public byte[] getReserved_1()
	{
		return reserved_1;
	}
	public void setReserved_1(byte reserved_1[])
	{
		this.reserved_1 = reserved_1;
	}
	public int getIs_current_device_connected_full()
	{
		return is_current_device_connected_full;
	}
	public void setIs_current_device_connected_full(int is_current_device_connected_by_mobile)
	{
		this.is_current_device_connected_full = is_current_device_connected_by_mobile;
	}
	public byte[] getReserved_3()
	{
		return reserved_3;
	}
	public void setReserved_3(byte[] reserved_3)
	{
		this.reserved_3 = reserved_3;
	}
//	public int getReceiveTime()
//	{
//		return receiveTime;
//	}
//	public void setReceiveTime(int receiveTime)
//	{
//		this.receiveTime = receiveTime;
//	}		
	
	public int getmIpLoginMark()
	{
		return mIpLoginMark;
	}
	public void setmIpLoginMark(int mIpLoginMark) 
	{
		this.mIpLoginMark = mIpLoginMark;
	}
	public MobileLoginInfo()
	{
		upnpPort = GlobalConstantValue.LOGIN_DEFAULT_PORT_NUM;
	}

	public int getLike_platform_id() {
		return like_platform_id;
	}

	public void setLike_platform_id(int like_platform_id) {
		this.like_platform_id = like_platform_id;
	}

	public MobileLoginInfo(byte[] transportMsg)
	{
		int bufferIndex = 0;
		int model_name_lenth = 0;
		device_sn = new byte[BROADCAST_INFO_SN_LEN];
		device_cpu_chip_id = new byte[MAX_CHIP_LEN];
		device_flash_id = new byte[MAX_CHIP_LEN];
		reserved_1 = new byte[RESERVED1_LEN];
		reserved_3 = new byte[RESERVED3_LEN];
		
		magic_code = new String(transportMsg, bufferIndex, GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN);
		bufferIndex = GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN;
		System.arraycopy(transportMsg, bufferIndex, device_sn, 0, BROADCAST_INFO_SN_LEN);
		device_sn_disp = SerialNumberToDisp(device_sn);
		bufferIndex = bufferIndex + BROADCAST_INFO_SN_LEN;
		
		for (model_name_lenth = 0; model_name_lenth < MAX_MODEL_STRING_LENGTH; model_name_lenth++)
		{
			if (transportMsg[bufferIndex + model_name_lenth] == 0)
			{
				break;
			}
		}
		
		model_name = new String(transportMsg, bufferIndex, model_name_lenth);
		
		bufferIndex = bufferIndex + MAX_MODEL_STRING_LENGTH;
		
		System.arraycopy(transportMsg, bufferIndex, device_cpu_chip_id, 0, MAX_CHIP_LEN);
		
		bufferIndex = bufferIndex + MAX_CHIP_LEN;
		
		
		System.arraycopy(transportMsg, bufferIndex, device_flash_id, 0, MAX_CHIP_LEN);
		bufferIndex = bufferIndex + MAX_CHIP_LEN;
		 
		device_ip_address_disp = (transportMsg[bufferIndex + 3] & 0x00FF)  + "." + (transportMsg[bufferIndex + 2] & 0x00FF) + "." + (transportMsg[bufferIndex + 1] & 0x00FF) + "." + (transportMsg[bufferIndex] & 0x00FF);
		 
		
		bufferIndex = bufferIndex + 4;		 
		platform_id = transportMsg[bufferIndex++] & 0x00FF;
		sw_version = (transportMsg[bufferIndex++] & 0x00FF) << 8 | (transportMsg[bufferIndex++] & 0x00FF);
		device_customer_id = transportMsg[bufferIndex++]& 0x00FF;
		device_model_id = transportMsg[bufferIndex++] & 0x00FF;
		System.arraycopy(transportMsg, bufferIndex, reserved_1, 0, RESERVED1_LEN);
		bufferIndex = bufferIndex + RESERVED1_LEN;
		sw_sub_version = ((transportMsg[bufferIndex + 3] & 0x00FF) << 24 & 0xFF000000) | ((transportMsg[bufferIndex + 2] & 0x00FF) << 16 & 0x00FF0000) | ((transportMsg[bufferIndex + 1] & 0x00FF) << 8 & 0x0000FF00) | (transportMsg[bufferIndex] & 0x000000FF);
		bufferIndex = bufferIndex + 4;

		is_current_device_connected_full = transportMsg[bufferIndex] & 0x0001;
		client_type 					= (transportMsg[bufferIndex] & 0x0002) >> 1;
		send_data_type					= (transportMsg[bufferIndex] & 0x0040) >> 6;
		bufferIndex = bufferIndex + 1;
		bufferIndex = bufferIndex + 3;

		like_platform_id = transportMsg[bufferIndex];
		bufferIndex = bufferIndex + 1;

		System.arraycopy(transportMsg, bufferIndex, reserved_3, 0, RESERVED3_LEN);
		bufferIndex = bufferIndex + RESERVED3_LEN;
	}
	
	private String SerialNumberToDisp(byte[] pcSNNumber)
	{
		int iDate;
		int iSerialNumber = 0;
		String SerialNumberDisp = "";

		if (null == pcSNNumber)
		{
			return SerialNumberDisp;
		}
		iDate = ((pcSNNumber[2] & 0x00FF) | (((pcSNNumber[1] & 0x00FF) << 8) & 0xff00) | (((pcSNNumber[0] & 0x00FF) << 16) & 0xff0000)) & 0xffffff;
		iSerialNumber = ((pcSNNumber[5] & 0x00FF) | (((pcSNNumber[4] & 0x00FF) << 8) & 0xff00) | (((pcSNNumber[3] & 0x00FF) << 16) & 0xff0000)) & 0xffffff;
		SerialNumberDisp = String.format("%06d%06d", iDate, iSerialNumber);
		
		return SerialNumberDisp;
	}
	public int getSw_sub_version()
	{
		return sw_sub_version;
	}
	public void setSw_sub_version(int sw_sub_version)
	{
		this.sw_sub_version = sw_sub_version;
	}
	public int getmConnectStatus()
	{
		return mConnectStatus;
	}
	public void setmConnectStatus(int mConnectStatus)
	{
		this.mConnectStatus = mConnectStatus;
	}
	public int getClient_type()
	{
		return client_type;
	}
	public void setClient_type(int client_type)
	{
		this.client_type = client_type;
	}
	
	public int getUpnpPort()
	{
		return this.upnpPort;
	}
	public void setUpnpPort(int upnpPort)
	{
		this.upnpPort = upnpPort;
	}
	
	public String getUpnpIp()
	{
		return this.upnpIp;
	}
	public void setUpnpIp(String upnpIp)
	{
		this.upnpIp = upnpIp;
	}
	/**
	 * @return　CN:返回上一次发现时间
	 */
	public long getLastFoundTime()
	{
		return mLastFoundTime;
	}
	/**
	 * @param mLastFoundTime　CN:设置上一次发现时间
	 */
	public void setLastFoundTime(long mLastFoundTime)
	{
		this.mLastFoundTime = mLastFoundTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MobileLoginInfo)) return false;
		MobileLoginInfo that = (MobileLoginInfo) o;
		return getDevice_ip_address() == that.getDevice_ip_address() && getPlatform_id() == that.getPlatform_id() && getSw_version() == that.getSw_version() && getDevice_customer_id() == that.getDevice_customer_id() && getDevice_model_id() == that.getDevice_model_id() && getSw_sub_version() == that.getSw_sub_version() && getClient_type() == that.getClient_type() && getIs_current_device_connected_full() == that.getIs_current_device_connected_full() && getSend_data_type() == that.getSend_data_type() && getLike_platform_id() == that.getLike_platform_id() && mIpLoginMark == that.mIpLoginMark && mConnectStatus == that.mConnectStatus && getUpnpPort() == that.getUpnpPort() && getLastFoundTime() == that.getLastFoundTime() && Objects.equals(getMagic_code(), that.getMagic_code()) && Arrays.equals(getDevice_sn(), that.getDevice_sn()) && Objects.equals(getDevice_sn_disp(), that.getDevice_sn_disp()) && Objects.equals(getModel_name(), that.getModel_name()) && Arrays.equals(getDevice_cpu_chip_id(), that.getDevice_cpu_chip_id()) && Arrays.equals(getDevice_flash_id(), that.getDevice_flash_id()) && Objects.equals(getDevice_ip_address_disp(), that.getDevice_ip_address_disp()) && Arrays.equals(getReserved_1(), that.getReserved_1()) && Arrays.equals(reserver_2, that.reserver_2) && Arrays.equals(getReserved_3(), that.getReserved_3()) && Objects.equals(getUpnpIp(), that.getUpnpIp());
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(getMagic_code(), getDevice_sn_disp(), getModel_name(), getDevice_ip_address(), getDevice_ip_address_disp(),
				getPlatform_id(), getSw_version(), getDevice_customer_id(), getDevice_model_id(), getSw_sub_version(), getClient_type(), getIs_current_device_connected_full(), getSend_data_type(),  getLike_platform_id(), mIpLoginMark, mConnectStatus, getUpnpIp(), getUpnpPort(),
				getLastFoundTime());
		result = 31 * result + Arrays.hashCode(getDevice_sn());
		result = 31 * result + Arrays.hashCode(getDevice_cpu_chip_id());
		result = 31 * result + Arrays.hashCode(getDevice_flash_id());
		result = 31 * result + Arrays.hashCode(getReserved_1());
		result = 31 * result + Arrays.hashCode(reserver_2);
		result = 31 * result + Arrays.hashCode(getReserved_3());
		return result;
	}
}
