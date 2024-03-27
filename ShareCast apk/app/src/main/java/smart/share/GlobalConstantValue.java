package smart.share;

public class GlobalConstantValue {
	
	public static final int S_MSG_REQUEST_SOCKET_KEEP_ALIVE = 26;

	public static final int S_MSG_CAST_REQUEST_STREAM_PLAY_INFO = 31;

	public static final int S_MSG_REQUEST_LOGIN_INFO = 998;

	public static final int S_MSG_CAST_DO_PLAY = 1063;

	
	public static final int S_MSG_NOTIFY_INPUT_PASSWORD_CANCEL = 2000;

	public static final int S_MSG_NOTIFY_CLIENT_TYPE_BECOME_MASTER = 2015;

	public static final int S_MSG_NOTIFY_MAX_VALUE = 2999;
	

	
	public static final int		CONNECT_TYEP_AUTO_LOGIN						= 0;
	public static final int		CONNECT_TYEP_IP_LOGIN						= 1;
	

	public static final int		GSCMD_NOTIFY_SOCKET_CLOSED					= 0x1010;
	public static final int		GSCMD_NOTIFY_BROADCAST_LOGIN_INFO_UPDATED	= 0x1011;

	public static final int		STRUCT_ONLY_ONE_DATA						= 15;
	public static final int		CAST_PLAY_INFO								= 35;

	public static final int		BUFF_LENGTH_RECEIVE_DATA_PER_TIME			= 2 * 1024;
	public static final int		BUFF_LENGTH_RECEIVE_DATA_TOTAL				= 1024 * 1024 * 8;
	public static final int		BACK_KEY_EXIT_TIMEOUT						= 2000;
	public static final int		SOCKET_TCP_TIMEOUT							= 3000;
	public static final int 	BROADCAST_PORT = 25860;
	public static final int 	BROADCAST_INFO_XOR_VALUE = 0x5b;
	public static final int 	LOGIN_DEFAULT_PORT_NUM = 19000;
	public static final String 	BROADCAST_INFO_MAGIC_CODE = "39WwijOog54a";
	public static final int 	BROADCAST_INFO_MAGIC_CODE_LEN = 12;
	public static final int		LOGIN_HISTORY_LIST_ITEM_MAX					= 10;
	public static final int 	DEVICE_LOGIN_INFO_DATA_LENGTH = 108;
	public static final int 	CONTROL_DATA_MSG_LENGTH = 16;
	public static final int 	CONTROL_DATA_HEADER_LEN = 4;
	public static final String 	CONTROL_DATA_HEADER_STR = "GCDH";

	public static final int 	CONNECT_DEVICE_ERROR_UNKONWN_ERROR = -1;
	public static final int 	CONNECT_DEVICE_ERROR_NOT_RESPONSE = -2;
	public static final int 	CONNECT_DEVICE_ERROR_NOT_REACHABLE = -3;
	public static final int 	CONNECT_DEVICE_ERROR_NOT_VALID = -4;
	public static final int 	CONNECT_DEVICE_ERROR_IP_ERROR = -5;
	public static final int 	CONNECT_DEVICE_ERROR_SW_VERSION_ERROR = -6;
	public static final int 	CONNECT_DEVICE_ERROR_DEVICE_IS_FULL = -7;
	public static final int 	CONNECT_DEVICE_ERROR_HAND_SHARK_ERROR = -8;
	public static final int 	CONNECT_DEVICE_ERROR_SERVER_IP_NON_EXIST = -9;
	public static final int 	CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL = -10;

}
