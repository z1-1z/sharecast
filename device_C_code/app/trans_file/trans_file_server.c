/*
 * trans_file_server.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/trans_file_client.h"
#include "trans_file/trans_file_server.h"

typedef void (*task_handle_func)(U32 param, int size);

typedef struct trans_file_server_tasks_service
{
    signed char progress_value;

    /* resource */
    TSF_HANDLE thread_handle;
    TSF_HANDLE msg_queue_handle;

} TSF_SERVER_TASK;

typedef struct simple_tasks_msg
{
    U32 msg_id;
    U32 param;
    U32 param1;
    U32 param2;
} TSF_TASK_MESSAGE;

static TSF_SERVER_TASK *trans_file_server_tasks_service = NULL;


static TSF_HANDLE created_json_mutex_id = TSF_INVALID_HANDLE;

TSF_HANDLE* get_created_json_mutex_id(void)
{
	return &created_json_mutex_id;
}

static void scramble_device_info_for_broadcast(char* send_buff)
{
    int stb_info_length = 0;
    int index = 0;
    unsigned char temp;

    stb_info_length = sizeof(TRANS_FILE_LOGIN_INFO);
    for (index = 0; index < stb_info_length/2; index ++)
    {
    	//reverse the order of stbinfo string.
    	temp = send_buff[(stb_info_length - 1) - index];
    	send_buff[(stb_info_length - 1) - index] = send_buff[index];
    	send_buff[index] = temp;
    	//xor the every byte.
    	send_buff[index] = send_buff[index] ^ TSF_BROADCAST_INFO_XOR_VALUE;
    	send_buff[(stb_info_length - 1) - index] = send_buff[(stb_info_length - 1) - index] ^ TSF_BROADCAST_INFO_XOR_VALUE;
    }
    if (stb_info_length%2 != 0)
    {
    	send_buff[stb_info_length/2] = send_buff[stb_info_length/2] ^ TSF_BROADCAST_INFO_XOR_VALUE;
    }
}

TRANS_FILE_RESPONSE_STATE construct_trans_file_broadcast_msg(unsigned char *broadcast_msg_buffer, int is_device_connected_full)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_NO_ERROR;
	TRANS_FILE_LOGIN_INFO trans_file_login_info;
	unsigned int uiIP = tsf_network_get_ip();
	if (broadcast_msg_buffer == NULL || uiIP == 0)
	{
		ret = TSF_RESPONSE_FAIL;
		TRANS_FILE_PRINTF("++++error fail++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		return ret;
	}

	memset(broadcast_msg_buffer, 0, TSF_BROADCAST_SEND_BUFF_LENGTH);
	memset(&trans_file_login_info, 0, sizeof(TRANS_FILE_LOGIN_INFO));
	uiIP = tsf_htonl(uiIP);

	memcpy((char * )trans_file_login_info.magic_code, TSF_BROADCAST_INFO_MAGIC_CODE, TSF_MIN(strlen(TSF_BROADCAST_INFO_MAGIC_CODE), sizeof(trans_file_login_info.magic_code)));

	trans_file_login_info.stb_ip_address = uiIP;

	trans_file_login_info.is_client_connented_full = is_device_connected_full;
	trans_file_login_info.send_data_type = 1;	 /*if send xml data to client,send_data_type == 0;else send_data_type == 1*/

	trans_file_login_info.stb_customer_id = tsf_get_customer_id();
	trans_file_login_info.stb_model_id = tsf_get_model_id();
	trans_file_login_info.platform_id = tsf_get_platform_type();
	trans_file_login_info.like_platform_id = tsf_get_platform_type();
	sprintf((char *) trans_file_login_info.model_name, "%s\n", tsf_get_model_name());
	tsf_get_device_chip_id(trans_file_login_info.stb_cpu_chip_id);
	tsf_get_device_sn(trans_file_login_info.stb_sn);

	trans_file_login_info.sw_version_byte1 = (tsf_get_software_version_number() >> 8) & 0xFF;
	trans_file_login_info.sw_version_byte2 = tsf_get_software_version_number() & 0xFF;
	trans_file_login_info.sw_sub_version = tsf_get_software_sub_version_number();

	memcpy(broadcast_msg_buffer, &trans_file_login_info, sizeof(TRANS_FILE_LOGIN_INFO));
	/*scramble the info*/
	scramble_device_info_for_broadcast((char *)broadcast_msg_buffer);
	
	return ret;
}

static void trans_file_server_task(void *argv)
{
	int iRet = -1;

	/*********For broadcast stb info********/
	int socket_broadcast_listener = TSF_INVALID_SOCKET;
	int is_broadcast = 1;
	unsigned char send_buff[TSF_BROADCAST_SEND_BUFF_LENGTH]= {0};
	/*********For broadcast stb info********/

	/*********For stream client****************/
	int stream_file_tcp_socket = TSF_INVALID_SOCKET;
	int stream_file_client_socket = TSF_INVALID_SOCKET;
	int stream_file_tcp_port = TSF_SHAKE_HAND_PORT; //20006
	/*********For stream_file client****************/

	int accept_socket_list[3];
	int accept_socket_num = 0, i;
	int valid_socket;

	iRet = -1;
	accept_socket_num = 0;
	for (i = 0; i < TSF_ARRAY_SIZE(accept_socket_list); i++)
	{
		accept_socket_list[i] = TSF_INVALID_SOCKET;
	}

	/*********For broadcast stb info********/
	socket_broadcast_listener = tsf_socket_create(TSF_UDP_SOCKET);
	if (socket_broadcast_listener == TSF_INVALID_SOCKET)
	{
		TRANS_FILE_PRINTF("+++udp [err]socket()+++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		return;
	}
	tsf_socket_set_sock_opt(socket_broadcast_listener, TSF_SOL_SOCKET, TSF_SO_BROADCAST, (char * )&is_broadcast, sizeof(is_broadcast));
	if (tsf_socket_bind(socket_broadcast_listener, 0) != 0)
	{
		TRANS_FILE_PRINTF("+++Can't bind socket+++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		goto EXIT;
	}
	/*********For broadcast stb info********/

	/*********For accept stream_file client****************/
	stream_file_tcp_socket = tsf_socket_create(TSF_TCP_SOCKET);
	if (stream_file_tcp_socket == TSF_INVALID_SOCKET)
	{
		TRANS_FILE_PRINTF("+++stream_file_tcp_socket [err]socket()+++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		goto EXIT;
	}
	tsf_thread_sleep(TSF_BROADCAST_DELAY);
	iRet = tsf_socket_bind(stream_file_tcp_socket, stream_file_tcp_port);
	if (iRet < 0)
	{
		TRANS_FILE_PRINTF("+++stream_file_tcp_socket [err]Bind+++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		goto EXIT;
	}
	tsf_thread_sleep(TSF_BROADCAST_DELAY);

	iRet = tsf_socket_listen(stream_file_tcp_socket, TSF_MAX_SUPPORT_CLIENT_NUM + 1);
	if (iRet < 0)
	{
		TRANS_FILE_PRINTF("+++stream_file_tcp_socket [err]listen+++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
		goto EXIT;
	}
	accept_socket_list[accept_socket_num++] = stream_file_tcp_socket;
	/*********For accept stream_file client****************/

	init_all_trans_file_client_resouce();
	tsf_mutex_create(&created_json_mutex_id, "JSON");
	TRANS_FILE_PRINTF("standby already!\n");

	while (1)
	{
		if (tsf_check_network_state())
		{
			/*********For broadcast stb info********/
			if (construct_trans_file_broadcast_msg(send_buff, is_tsf_client_num_full()) == TSF_RESPONSE_NO_ERROR)
			{
				tsf_socket_send_to(socket_broadcast_listener, send_buff, sizeof(TRANS_FILE_LOGIN_INFO), TSF_SOCKET_RECEIVE_TIMEOUT, TSF_IPADDR_BROADCAST, TSF_BROADCAST_PORT);
			}
			/*********For broadcast stb info********/

			valid_socket = tsf_socket_select_ex(accept_socket_list, accept_socket_num, 1, TSF_SOCKET_RECEIVE_TIMEOUT);
			if (valid_socket == TSF_INVALID_SOCKET)
			{
				tsf_thread_sleep(TSF_BROADCAST_DELAY);
				continue;
			}

			if(stream_file_tcp_socket == valid_socket) /************for stream_file client***************/
			{
				stream_file_client_socket = tsf_socket_accept(stream_file_tcp_socket, NULL, NULL);
				if (stream_file_client_socket == TSF_INVALID_SOCKET)
				{
					TRANS_FILE_PRINTF("stream_file_client_socket port socket=%d tcplisten=%d\n", stream_file_client_socket, stream_file_tcp_socket);
					tsf_thread_sleep(TSF_BROADCAST_DELAY);
					continue;
				}
				/*set the length of the socket temporary buff*/
				tsf_socket_set_buffer_size(stream_file_client_socket, TSF_SOCKET_RECEIVE_TEMPORARY_BUFF_LENGTH, 1);
				trans_file_client_task_setup(stream_file_client_socket, TSF_CLIENT_TYPE);
			}
			else
			{
				tsf_thread_sleep(TSF_BROADCAST_DELAY);
			}
		}
		else
		{
			tsf_thread_sleep(TSF_BROADCAST_DELAY);
		}
	}

EXIT:
	release_all_trans_file_client_resouce();

	if(created_json_mutex_id != TSF_INVALID_HANDLE)
	{
		tsf_mutex_delete(created_json_mutex_id);
		created_json_mutex_id = TSF_INVALID_HANDLE;
	}

	if(stream_file_tcp_socket != TSF_INVALID_SOCKET)
	{
		tsf_socket_close(stream_file_tcp_socket);
		stream_file_tcp_socket = TSF_INVALID_SOCKET;
	}


	if(socket_broadcast_listener != TSF_INVALID_SOCKET)
	{
		tsf_socket_close(socket_broadcast_listener);
		socket_broadcast_listener = TSF_INVALID_SOCKET;
	}

	tsf_thread_exit();
}

static void cleanup_trans_file_server_tasks_service(void)
{
    if (trans_file_server_tasks_service == NULL)
        return;

    free(trans_file_server_tasks_service);
    trans_file_server_tasks_service = NULL;
}

TRANS_FILE_RESPONSE_STATE trans_file_server_task_setup(void)
{
    int err_code = -1;

	do
	{
		trans_file_server_tasks_service = (TSF_SERVER_TASK *) malloc(sizeof(TSF_SERVER_TASK));
		if (trans_file_server_tasks_service == NULL)
			break;

	    memset(trans_file_server_tasks_service, 0, sizeof(TSF_SERVER_TASK));
	    trans_file_server_tasks_service->thread_handle = TSF_INVALID_HANDLE;
	    trans_file_server_tasks_service->msg_queue_handle = TSF_INVALID_HANDLE;

	    if (tsf_thread_create(&(trans_file_server_tasks_service->thread_handle), "trans_file_server_tasks", trans_file_server_task, NULL, TSF_BROADCAST_TASK_STACK_SIZE, tsf_thread_priority(TRANS_FILE_SERVER_THREAD)) != 0)
	    {
	        err_code = -2;
	        break;
	    }

	    err_code = 0;
	} while (0);

	if (err_code != 0)
	{
		cleanup_trans_file_server_tasks_service();
	}
    return err_code;
}

