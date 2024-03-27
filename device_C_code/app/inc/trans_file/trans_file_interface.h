/*
 * trans_file_interface.h
 */
#ifndef _TRANS_FILE_INTERFACE_H_
#define _TRANS_FILE_INTERFACE_H_

typedef unsigned int        		TSF_HANDLE;
#define TSF_INVALID_HANDLE   		(0xFFFFFFFF)

#define TSF_WAIT_FOR_1_SEC			(1000)
#define TSF_INFINITE_WAIT	        (0xFFFFFFFF)

#ifndef MEM_PRINT_SIZE
#define MEM_PRINT_SIZE				(0x4000)


#define MAX_SN_LEN                  8
#define MAX_CHIP_LEN                8
#define MAX_SN_NUMBER_LENGTH		(20)

#define QUEUE_MSG_SEND_TIMEOUT      (10)

#define TSF_MAX_SERVICE_NAME_LENGTH 		MAX_SERVICE_NAME_LENGTH
#define TSF_MAX_MODEL_STRING_LENGTH      	(32)
#define TSF_MAX_SW_VERSION_LENGTH			(20)
#define TSF_MAX_SN_NUMBER_LENGTH			MAX_SN_NUMBER_LENGTH
#define TSF_MAX_SUPPORT_CLIENT_NUM			(1)
#define TSF_BROADCAST_INFO_MAGIC_CODE_LEN 	(12)
#define TSF_RESERVED1_LEN					(3)
#define TSF_RESERVED3_LEN					(19)
#define COMPRESS_RESULT_BUFF_LENGTH_OFFSET  (16)
#define	TRANS_FILE_CONTROL_DATA_HEADER_LEN	(4)
#define	TRANS_FILE_CONTROL_DATA_HEADER_STR	("GCDH")//change to your code
#define TSF_URL_LENGTH						(1024 + 128)
#define TSF_MEDIA_TITLE_LENGTH				(255)
#define STB_MAX_SUPPORT_CLIENT_NUM			(TSF_MAX_SUPPORT_CLIENT_NUM)

//#define TRANS_FILE_DEBUG

#ifdef TRANS_FILE_DEBUG
#define TRANS_FILE_PRINTF(fmt, args...) printf("\033[32m[%s:%d]\033[36m "fmt"\033[0m", __FUNCTION__, __LINE__, ##args);
#else
#define TRANS_FILE_PRINTF(fmt, arg...) ((void)0)
#endif
#define DEBUG_SOCK 				0
#define TSF_ENUM_CASE(x)    	case x: return(#x)

enum
{
	EXIT_TO_MAIN_MENU = 4,
};


enum
{
	TSF_SEND_AUTO,
	TSF_SEND_DESK,
};

typedef enum _trans_file_msg_type_
{

	TSF_MSG_REQUEST_SOCKET_KEEP_ALIVE			= 26,
	TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO		= 31,
	TSF_MSG_REQUEST_LOGIN_INFO					= 998,
	TSF_MSG_CAST_DO_PLAY						= 1063,


	/*************** start command message ***************/
	TSF_MSG_CMD_INIT							= 3000,
	TSF_MSG_CMD_DEINIT							= 3001,
	TSF_MSG_CMD_MAX_VALUE						= 3999,
	/*************** end command message ***************/

	TSF_FILE_MSG_MAX							= 9999,

} TRANS_FILE_MSG_TYPE;


/****************************************************************
            	 enum definition
****************************************************************/

/*********************************************************
message list between STB and mobile application
*********************************************************/
typedef enum _tfs_msg_type_
{
	TSF_MSG_EXIT_MENU						= 11000,

	TSF_MSG_EVENT_MAX						= 19999,
}TSF_MSG_TYPE;

/****************************************************************
            	  struct definition
****************************************************************/
typedef enum
{
	TRANS_FILE_MEDIA_UNKNOW = -1,
	TRANS_FILE_MEDIA_MOVIE = 0,
	TRANS_FILE_MEDIA_IMAGE = 1,
	TRANS_FILE_MEDIA_AUDIO = 2,
	TRANS_FILE_MEDIA_LIVE = 3,
	TRANS_FILE_MEDIA_LIVE_ANDROID = 4,
	TRANS_FILE_OTHER_FILE = 5,
}TRANS_FILE_MEDIA_TYPE;

typedef struct
{
	char title[TSF_MEDIA_TITLE_LENGTH+1];
	char stream_url[TSF_URL_LENGTH];
	TRANS_FILE_MEDIA_TYPE mediaType;//-1:none  0: video 1:picture 2:audio 3: live screen iOS 4: live screen android 5: bin file
	int stateCode;
	int cur_time;
	int total_time;
	int seekTime;
}TRANS_FILE_STREAM_INFO;


typedef enum
{
	TSF_RESPONSE_NO_ERROR = 0,
	TSF_RESPONSE_FAIL,
	TSF_RESPONSE_NO_ENOUGH_MEMORY,
	TSF_RESPONSE_TIMEOUT,
	TSF_RESPONSE_BEYOND_SUPPORT_CLIENT_NUM,
	TSF_RESPONSE_CREATE_MESSAGE_FAILED,
	TSF_RESPONSE_CREATE_TASK_FAILED,
	TSF_RESPONSE_CREATE_JSON_FILE_FAILED,
	TSF_RESPONSE_NET_ERROR,
	TSF_RESPONSE_COMPRESS_FAILED,
	TSF_RESPONSE_UNKNOWN_MESSAGE,
} TRANS_FILE_RESPONSE_STATE;


typedef struct
{
	unsigned char data_header[TRANS_FILE_CONTROL_DATA_HEADER_LEN];
	unsigned int data_len;
	unsigned int data_type;
	unsigned int msg_response_state;
}TSF_MSG_CONTROL_DATA;//keep size to 16 bytes


typedef struct tsf_mw_msg_struct
{
	unsigned int message_id;
	unsigned int param1;
	unsigned int param2;
	unsigned int param3;

}TSF_MW_MSG;

typedef struct _TRANS_FILE_LONGIN_INFO_
{
	unsigned char magic_code[TSF_BROADCAST_INFO_MAGIC_CODE_LEN];
	unsigned char stb_sn[MAX_SN_LEN];
	unsigned char model_name[32];
	unsigned char stb_cpu_chip_id[MAX_CHIP_LEN];
	unsigned char stb_flash_id[MAX_CHIP_LEN];
	unsigned int  stb_ip_address;
	unsigned char platform_id;
	unsigned char sw_version_byte1;
	unsigned char sw_version_byte2;
	unsigned char stb_customer_id;
	unsigned char stb_model_id;
	unsigned char reserved_1[TSF_RESERVED1_LEN];
	unsigned int sw_sub_version;
	unsigned int is_current_stb_connected_full			:1;
	unsigned int client_type							:1;
	unsigned int reserved_2								:3;
	unsigned int is_client_connented_full				:1;
	unsigned int send_data_type							:1;				  /*if send xml data to client,send_data_type == 0;else send_data_type == 1*/
	unsigned int reserved_3								:25;
	unsigned char like_platform_id;	/*App platform logic is similar to which platform*/
	unsigned char reserved_4[TSF_RESERVED3_LEN];
}TRANS_FILE_LOGIN_INFO; /*sizeof(TRANS_FILE_LOGIN_INFO) = 108; Please keep the size*/

TRANS_FILE_RESPONSE_STATE trans_file_callback(unsigned int msg_id, unsigned int param1, unsigned int param2, void* param3);


TRANS_FILE_RESPONSE_STATE send_responses_msg(unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3);
#endif
#endif /* _TRANS_FILE_INTERFACE_H_ */
