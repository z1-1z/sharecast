/*
 * trans_file_server.h
 */

#ifndef TRANS_FILE_SERVER_H_
#define TRANS_FILE_SERVER_H_

#include "trans_file/trans_file_interface.h"

#define TSF_SERVER_TASK_DELAY					    (500)

#define TSF_SMALL_JSON_BUFF_LENGTH			        (1024 * 256)

#define TSF_SOCKET_SEND_BUFF_LENGTH				    (16 * 1024)
#define TSF_SOCKET_RECEIVE_BUFF_LENGTH				(1024 * 1024)
#define TSF_SOCKET_RECEIVE_HEADER_LENGTH			(15)
#define TSF_SOCKET_RECEIVE_TEMPORARY_BUFF_LENGTH  	(640 * 360 * 2)
#define TSF_SOCKET_RECEIVE_TIMEOUT		            (400)

#define TSF_SHAKE_HAND_PORT							(19000)
#define TSF_BROADCAST_PORT		 					(25860)
#define TSF_BROADCAST_SEND_BUFF_LENGTH				(512)
#define TSF_BROADCAST_INFO_MAGIC_CODE				("39WwijOog54a")//广播信息标记
#define TSF_BROADCAST_INFO_XOR_VALUE				(0x5b)          //广播信息加密key

#define TSF_BROADCAST_TASK_STACK_SIZE				(4 * 1024)
#define TSF_BROADCAST_DELAY							(TSF_WAIT_FOR_1_SEC)

#define TSF_SOCKET_START_FLAG						("Start")
#define TSF_SOCKET_END_FLAG							("End")
#define TSF_SOCKET_DATA_LENGTH_BIT_NUMBER			(8)

typedef enum _tsf_client_type_
{
	TSF_CLIENT_TYPE 	= 0,
	TSF_CLIENT_TYPE_MAX = TSF_MAX_SUPPORT_CLIENT_NUM,
} CLIENT_TYPE;

enum
{
	TSF_CONNECTED_NOT_FULL		= 0,
	TSF_CONNECTED_IS_FULL		= 1,
} ;

typedef struct _tsf_client_config_
{
	char client_index;
	CLIENT_TYPE client_type;	//mobile client type,
	TSF_HANDLE task_id;
	int socket_id;
	TSF_HANDLE msg_queue_id;
	char* socket_recv_buff;
	unsigned int recv_message_from_mobile_time_mark;
	int first_msg_recv_from_moible;
	unsigned int keep_alive_max_timeout;
} TSF_CLIENT_CONFIG;

TRANS_FILE_RESPONSE_STATE trans_file_server_task_setup(void);
TRANS_FILE_RESPONSE_STATE construct_trans_file_broadcast_msg(unsigned char *broadcast_msg_buffer, int is_stb_connected_full);
TSF_HANDLE* get_created_json_mutex_id(void);

#endif /* TRANS_FILE_SERVER_H_ */
