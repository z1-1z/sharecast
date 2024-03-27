/*
 * trans_file_client.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_client.h"
#include "trans_file/trans_file_main.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/json_convert/json_write.h"
#include "trans_file/json_convert/json_parser.h"
#include "trans_file/trans_file_action.h"

#define DEFAULT_SOCKET_KEEP_ALIVE_TIMEOUT     (15 * 1000)

static TSF_CLIENT_CONFIG tsf_client_config_buff[TSF_MAX_SUPPORT_CLIENT_NUM];

static int judge_socket_keep_alive(int client_index)
{
	int connect_keep_alive = 1;
	int system_time = tsf_get_system_time();
	if ((system_time - tsf_client_config_buff[client_index].recv_message_from_mobile_time_mark) > tsf_client_config_buff[client_index].keep_alive_max_timeout)
		connect_keep_alive = 0;
	return connect_keep_alive;
}

static int convert_trans_file_msg_type_to_json_msg_type(TRANS_FILE_MSG_TYPE tsf_msg_type)
{
	json_struct_type json_msg_type = JSON_STRUCT_MAX;
	switch(tsf_msg_type)
	{
 		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
 			json_msg_type = JSON_STRUCT_STREAM_PLAY_INFO;
 			break;
		default :
			json_msg_type = JSON_STRUCT_MAX;
			break;
	}
	return json_msg_type;
}

static TRANS_FILE_RESPONSE_STATE compress_common_data(char* notCompressData,  char** compress_data_buffer,  unsigned int size_before_compress,  unsigned int *size_after_compress)
{
	TRANS_FILE_RESPONSE_STATE error_value = TSF_RESPONSE_FAIL;

	if (size_before_compress == 0 || notCompressData == NULL)
	{
		//no need to compress when size == 0.
		return TSF_RESPONSE_NO_ERROR;
	}
	if (notCompressData == NULL || compress_data_buffer == NULL || size_after_compress == NULL)
	{
		return error_value;
	}
	*size_after_compress = size_before_compress + size_before_compress/100 + COMPRESS_RESULT_BUFF_LENGTH_OFFSET;
	*compress_data_buffer = (char *)malloc(*size_after_compress);
	if (*compress_data_buffer == NULL) //Not enough memory
	{
		error_value = TSF_RESPONSE_NO_ENOUGH_MEMORY;
	}
	else
	{
		error_value = tsf_compr_zlib((U8 *)(*compress_data_buffer), (U32 *)size_after_compress, (U8 *)notCompressData, size_before_compress);
		if (error_value)/*error found*/
		{
			free(*compress_data_buffer);
			TRANS_FILE_PRINTF("+++ func: %s, line: %d +++ error_value %d\n", __FUNCTION__, __LINE__, error_value);
			error_value = TSF_RESPONSE_FAIL;
		}
		else
		{
			error_value = TSF_RESPONSE_NO_ERROR;
		}
	}
	return error_value;
}

static void construct_control_data_msg(TSF_MSG_CONTROL_DATA *data_message, unsigned int data_type, unsigned int data_length, unsigned int msg_response_state)
{
	memset(data_message, 0, sizeof(TSF_MSG_CONTROL_DATA));
	strncpy((char *)data_message->data_header, TRANS_FILE_CONTROL_DATA_HEADER_STR, TRANS_FILE_CONTROL_DATA_HEADER_LEN);
	data_message->data_len = data_length;
	data_message->data_type = data_type;
	data_message->msg_response_state = msg_response_state;
}

static TRANS_FILE_RESPONSE_STATE send_common_data_to_mobile(int client_socket, TRANS_FILE_MSG_TYPE request, U8 *common_data, int data_length, unsigned int msg_response_state)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_FAIL;
	int data_left = -1;
	int send_data_count = 0;
	TSF_MSG_CONTROL_DATA control_data_msg;
	char *current_ponit_buff;
	char *send_buff_after_compress = NULL;
	int send_buff_length_after_compress = 0;

	do
	{
		if (data_length < 0)
			break;

		if (compress_common_data((char *) common_data, &send_buff_after_compress, data_length, (unsigned int *) &send_buff_length_after_compress) != TSF_RESPONSE_NO_ERROR)
		{
			TRANS_FILE_PRINTF("++++ compress data error ++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
			break;
		}

		construct_control_data_msg(&control_data_msg, request, send_buff_length_after_compress, msg_response_state);

#if DEBUG_SOCK
		TRANS_FILE_PRINTF("++ func %s, line %d++++ sock %d send (request:%d | msg_response_state: %d)\n", __FUNCTION__, __LINE__, client_socket, request, control_data_msg.msg_response_state);
#endif

		if (tsf_socket_send(client_socket, (U8*) &control_data_msg, sizeof(control_data_msg), TSF_SOCKET_RECEIVE_TIMEOUT) != sizeof(control_data_msg))
			break;

		if (data_length == 0 || send_buff_after_compress == NULL) //When no content, only send the control data message.
		{
			ret = TSF_RESPONSE_NO_ERROR;
			break;
		}

		data_left = send_buff_length_after_compress;
		current_ponit_buff = send_buff_after_compress;
		while (data_left > TSF_SOCKET_SEND_BUFF_LENGTH)
		{
			send_data_count = tsf_socket_send(client_socket, (unsigned char *)current_ponit_buff, TSF_SOCKET_SEND_BUFF_LENGTH, TSF_SOCKET_RECEIVE_TIMEOUT);
			if (send_data_count != TSF_SOCKET_SEND_BUFF_LENGTH)
			{
				TRANS_FILE_PRINTF("+++send data failed+++++FUNCTION: %s +++LINE: %d ++++++++ send_data_count==%d \n", __FUNCTION__, __LINE__, send_data_count);
				ret = TSF_RESPONSE_NO_ERROR; //Don't close the socket
				break;
			}
			data_left -= TSF_SOCKET_SEND_BUFF_LENGTH;
			current_ponit_buff += TSF_SOCKET_SEND_BUFF_LENGTH;
		}

		if (ret == TSF_RESPONSE_NO_ERROR)
			break;

		send_data_count = tsf_socket_send(client_socket, (unsigned char *)current_ponit_buff, data_left, TSF_SOCKET_RECEIVE_TIMEOUT);
#if DEBUG_SOCK
		printf("++ func %s, line %d++++ sock %d send\n", __FUNCTION__, __LINE__, client_socket);
#endif

		if (send_data_count != data_left)
		{
			TRANS_FILE_PRINTF("++++send data failed++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
			break;
		}

		ret = TSF_RESPONSE_NO_ERROR;
	} while (0);

	if (send_buff_after_compress != NULL)
	{
		free(send_buff_after_compress);
	}

	if (ret == TSF_RESPONSE_FAIL)
	{
		tsf_socket_close(client_socket);
#if DEBUG_SOCK
		printf("++ func %s, line %d++++ sock %d close\n", __FUNCTION__, __LINE__, client_socket);
#endif
	}
	return ret;
}

static TRANS_FILE_RESPONSE_STATE send_json_data_to_mobile(int client_socket, TRANS_FILE_MSG_TYPE request, void *json_root, json_struct_type json_type, unsigned int msg_response_state)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_NO_ERROR;
	unsigned char *json_send_buff_before_compress = NULL;
	int send_json_size = 0;
	int buffer_length = 0;
	bool result_of_create_json;
	if (client_socket < 0)
	{
		return TSF_RESPONSE_FAIL;
	}
	switch (request)
	{
		default:
			buffer_length = (TSF_SMALL_JSON_BUFF_LENGTH * sizeof(char));
			break;
	}
	json_send_buff_before_compress = malloc(buffer_length);
	if (json_send_buff_before_compress == NULL)
	{
		send_common_data_to_mobile(client_socket, request, NULL, 0, TSF_RESPONSE_NO_ENOUGH_MEMORY);
		return TSF_RESPONSE_NO_ENOUGH_MEMORY;
	}
	memset(json_send_buff_before_compress, 0, buffer_length);

	tsf_mutex_lock(*get_created_json_mutex_id(), TSF_INFINITE_WAIT);
	result_of_create_json = create_json_buff((char *)json_send_buff_before_compress, json_root, json_type);
	tsf_mutex_unlock(*get_created_json_mutex_id());
	if (result_of_create_json == false)
	{
		TRANS_FILE_PRINTF("create json buff failed ..\n");
		free(json_send_buff_before_compress);
		return TSF_RESPONSE_CREATE_JSON_FILE_FAILED;
	}
	else
	{
		switch(request)
		{
			default:
				send_json_size = strlen((char *)json_send_buff_before_compress);
				break;
		}
	}

	ret = send_common_data_to_mobile(client_socket, request, json_send_buff_before_compress, send_json_size, msg_response_state);
	free(json_send_buff_before_compress);
	return ret;
}

static void release_trans_file_client_resouce_by_index(int client_index, int from_thread)
{
	if (client_index >= TSF_MAX_SUPPORT_CLIENT_NUM)
	{
		TRANS_FILE_PRINTF("client is too many");
		return;
	}
	TSF_CLIENT_CONFIG* current_client_config = &(tsf_client_config_buff[client_index]);

	current_client_config->client_type = TSF_CLIENT_TYPE;

	if (current_client_config->msg_queue_id != TSF_INVALID_HANDLE)
	{
		tsf_msg_queue_delete(current_client_config->msg_queue_id);
		current_client_config->msg_queue_id = TSF_INVALID_HANDLE;
	}
	if (current_client_config->socket_id != -1)
	{
		tsf_socket_close(current_client_config->socket_id);
		current_client_config->socket_id = -1;
	}
	if (current_client_config->socket_recv_buff != NULL)
	{
		free(current_client_config->socket_recv_buff);
		current_client_config->socket_recv_buff = NULL;
	}

	current_client_config->first_msg_recv_from_moible = TSF_MSG_EVENT_MAX;

	if (current_client_config->task_id != 0)
	{
		int temp_task_id = current_client_config->task_id;

		current_client_config->task_id = 0;
		//delete task must be the last step.
		//This code must be the last code, otherwise, the codes after it won't be executed.
		tsf_thread_delete(temp_task_id);
	}
}

void release_all_trans_file_client_resouce(void)
{
	int client_index = 0;
	TSF_CLIENT_CONFIG* current_client_config = NULL;

	for (; client_index < TSF_MAX_SUPPORT_CLIENT_NUM; client_index++)
	{
		current_client_config = &(tsf_client_config_buff[client_index]);
		if (current_client_config->task_id == 0)
			continue;

		release_trans_file_client_resouce_by_index(client_index, 0);
	}
	return;
}

void init_all_trans_file_client_resouce(void)
{
	int client_index;
	TSF_CLIENT_CONFIG* current_client_config = NULL;

	for (client_index = 0; client_index < TSF_MAX_SUPPORT_CLIENT_NUM; client_index++)
	{
		current_client_config = &(tsf_client_config_buff[client_index]);
		current_client_config->client_index = client_index;
		current_client_config->client_type = TSF_CLIENT_TYPE;
		current_client_config->task_id = 0;
		current_client_config->msg_queue_id = TSF_INVALID_HANDLE;
		current_client_config->socket_id = -1;
		current_client_config->socket_recv_buff = NULL;
		current_client_config->first_msg_recv_from_moible = TSF_MSG_EVENT_MAX;
		current_client_config->keep_alive_max_timeout = DEFAULT_SOCKET_KEEP_ALIVE_TIMEOUT;
	}
}

int get_free_client_resouce_index(void)
{
	int client_index, tsf_number = 0;
	TSF_CLIENT_CONFIG* current_client_config = NULL;
	for (client_index = 0; client_index < TSF_MAX_SUPPORT_CLIENT_NUM; client_index++)
	{
		current_client_config = &(tsf_client_config_buff[client_index]);
		if (    (current_client_config->task_id == 0)
				&& (current_client_config->msg_queue_id == TSF_INVALID_HANDLE)
		        && (current_client_config->socket_id == -1)
		        && (current_client_config->socket_recv_buff == NULL)
		        )
			break;

		tsf_number++;
	}
	return tsf_number;
}

TRANS_FILE_RESPONSE_STATE send_message_to_trans_file_client_task(int client_index, unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_FAIL;
	TSF_MW_MSG msg;

	msg.message_id = msg_id;
	msg.param1 = param1;
	msg.param2 = param2;
	msg.param3 = param3;
	if (TSF_INVALID_HANDLE != tsf_client_config_buff[client_index].msg_queue_id)
	{
		ret = tsf_msg_queue_send(tsf_client_config_buff[client_index].msg_queue_id, (void *)&msg, sizeof(TSF_MW_MSG), QUEUE_MSG_SEND_TIMEOUT);
	}
	return ret;
}

static int parser_json_file_data_to_tsf_struct_ex(int command_type, unsigned char *org_json, json_node_info *node, void *msg_info_in_json)
{
	json_node_info *json_node_cusor = node;
	int ret = 0;
	if(node == NULL)
	{
		return ret;
	}


	switch (command_type)
	{
		/********stream command*************/
		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
			break;
		case TSF_MSG_CAST_DO_PLAY:
		{
			TRANS_FILE_STREAM_INFO *stream_file_info = (TRANS_FILE_STREAM_INFO *)msg_info_in_json;
			json_node_cusor = find_node_by_tag_name("actionCode", json_node_cusor);
			stream_file_info->stateCode = atoi(json_node_cusor->tag_text);
			json_node_cusor = find_node_by_tag_name("mediaType", json_node_cusor);
			stream_file_info->mediaType = atoi(json_node_cusor->tag_text);

			if ((org_json == NULL) || (tsf_parser_large_link((char *)org_json, "url", stream_file_info->stream_url, sizeof(stream_file_info->stream_url)) == false))
			{
				json_node_cusor = find_node_by_tag_name("url", json_node_cusor);
				memcpy(stream_file_info->stream_url, json_node_cusor->tag_text, TSF_MIN(sizeof(stream_file_info->stream_url), sizeof(json_node_cusor->tag_text)));
			}

			snprintf(stream_file_info->title, sizeof(stream_file_info->title), "MStream");

			json_node_cusor = find_node_by_tag_name("seekTime", node);
			if (json_node_cusor)
			{
				stream_file_info->seekTime = atoi(json_node_cusor->tag_text) / TSF_WAIT_FOR_1_SEC;
			}

			if (stream_file_info)
			{
				TRANS_FILE_PRINTF("file type: %s\n", get_trans_file_type(stream_file_info->mediaType));
				TRANS_FILE_PRINTF("file link: %s\n", stream_file_info->stream_url);
			}

			ret = sizeof(TRANS_FILE_STREAM_INFO);
			break;
		}
		/******** command*************/

		default:
			break;
	}
	return ret;
}

int parser_json_file_data_to_tsf_struct(int command_type, json_node_info *node, void *msg_info_in_json)
{
	return parser_json_file_data_to_tsf_struct_ex(command_type, NULL, node, msg_info_in_json);
}

static bool is_xml_login_info_request(unsigned char *json_file_buff)
{
	char request_login_type[REQUEST_TYPE_SIZE + 1];
	request_login_type[REQUEST_TYPE_SIZE] = '\0';

	TRANS_FILE_PRINTF("json file is invalid\n");

	strncpy(request_login_type, (char *)&(json_file_buff[REQUEST_TYPE_BEGIN_INDEX]), REQUEST_TYPE_SIZE);
	if (strcmp(request_login_type, REQUEST_LOGIN_TYPE) == 0)
	{
		//TSF_RESPONSE_NO_ERROR
		return true;
	}
	else
	{
		//TSF_RESPONSE_FAIL
		return false;
	}
}

static TRANS_FILE_RESPONSE_STATE parser_json_file_to_command(unsigned char * json_file_buff, unsigned int client_index, int socket_server,TRANS_FILE_MSG_TYPE *tsf_command_type)
{
	TRANS_FILE_RESPONSE_STATE bRet = TSF_RESPONSE_NO_ERROR;
	json_root_info *json_root = NULL;
	json_node_info *json_node_cusor = NULL;
	int command_type = 0;
	int data_buffer_length = 0;

	if (json_file_buff == NULL)
	{
		bRet = TSF_RESPONSE_FAIL;
		return bRet;
	}

	if (!(judge_trans_file_json_validity((char *)json_file_buff)))
	{
		//to see whether it is xml_login_info_request
		if (is_xml_login_info_request(json_file_buff))
		{
			command_type = TSF_MSG_REQUEST_LOGIN_INFO;
		}
		else
		{
			bRet = TSF_RESPONSE_FAIL;
			return bRet;
		}
	}
	else
	{
		json_root = json_string_parser((char *)json_file_buff);
		if (NULL == json_root)
		{
			bRet = TSF_RESPONSE_FAIL;
			return bRet;
		}
		json_node_cusor = json_root->next;

		TRANS_FILE_PRINTF("request:%s\n", json_root->command_type);
		command_type = atoi(json_root->command_type);
	}

	*tsf_command_type = command_type;
//	printf("[%s][%d][%d][client index = %d]\n", __FUNCTION__, __LINE__, command_type, client_index);
//	printf("+++ func: %s, line: %d +++ json_file_buff %s\n", __FUNCTION__, __LINE__, json_file_buff);

	if ((tsf_client_config_buff[client_index].first_msg_recv_from_moible == TSF_MSG_EVENT_MAX))
	{
		tsf_client_config_buff[client_index].first_msg_recv_from_moible = command_type;
	}

	switch (command_type)
	{
		/*************stream command**************/
		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
			send_message_to_trans_file_main_task(command_type, client_index, 0, 0);
			break;
		/*************stream file command**************/
		case TSF_MSG_CAST_DO_PLAY:
		{
			TRANS_FILE_STREAM_INFO *stream_info = NULL;
			stream_info = (TRANS_FILE_STREAM_INFO *)malloc(sizeof(TRANS_FILE_STREAM_INFO));
			memset(stream_info, 0, sizeof(TRANS_FILE_STREAM_INFO));
			data_buffer_length = parser_json_file_data_to_tsf_struct_ex(command_type, json_file_buff, json_node_cusor, stream_info);
			if (strlen(stream_info->stream_url))
			{
				send_message_to_trans_file_main_task(command_type, client_index, (unsigned int)stream_info, data_buffer_length);
			}
			else
			{
				send_message_to_trans_file_main_task(command_type, client_index, 0, 0);
			}
			break;
		}
		/*************end of stream file command**************/

		case TSF_MSG_REQUEST_LOGIN_INFO:
		{
			unsigned char send_buff[TSF_BROADCAST_SEND_BUFF_LENGTH] = {0};
			if (construct_trans_file_broadcast_msg(send_buff, TSF_CONNECTED_NOT_FULL) == TSF_RESPONSE_NO_ERROR)
			{
#if DEBUG_SOCK
				printf("++ func %s, line %d++++ sock %d send\n", __FUNCTION__, __LINE__, tsf_client_config_buff[client_index].socket_id);
#endif
				if (tsf_socket_send(tsf_client_config_buff[client_index].socket_id, send_buff, sizeof(TRANS_FILE_LOGIN_INFO), TSF_SOCKET_RECEIVE_TIMEOUT) != sizeof(TRANS_FILE_LOGIN_INFO))
				{
					TRANS_FILE_PRINTF("connect failed ..\n");
					bRet = TSF_RESPONSE_NET_ERROR;
				}
				else
				{
					bRet = TSF_RESPONSE_NO_ERROR;
				}
			}
			break;
		}

		default:
			bRet = TSF_RESPONSE_UNKNOWN_MESSAGE;
			break;
	}

	json_free_string_parser(json_root);
	return bRet;
}

/* receive_length */
/*  0: nothing happen ; <0:  disconnect; other: receive some data*/
static int tsf_socket_modify_receive(int ClientSocket, unsigned char *trans_file_receive_buffer)
{
	int receive_length = 0;
	int receive_total_length = 0;
	int socket_start_flag_length = 0;
	int need_receive_data_len_rest = 0;
	int socket_data_total_len = 0;

	socket_start_flag_length = strlen(TSF_SOCKET_START_FLAG);

	receive_length = tsf_socket_select(ClientSocket, 1, TSF_SOCKET_RECEIVE_TIMEOUT);
	if (receive_length <= 0)
	{
		return receive_length;
	}

	receive_length = tsf_socket_recv(ClientSocket, trans_file_receive_buffer, TSF_SOCKET_RECEIVE_HEADER_LENGTH, TSF_SOCKET_RECEIVE_TIMEOUT);
#if DEBUG_SOCK
	printf("++ func %s, line %d++++ sock %d recv\n", __FUNCTION__, __LINE__, ClientSocket);
#endif
	if (receive_length <= 0)
	{
		return receive_length;
	}
	if (NULL == strstr((char *)trans_file_receive_buffer, TSF_SOCKET_START_FLAG) || receive_length != TSF_SOCKET_RECEIVE_HEADER_LENGTH)
	{
		return 0;
	}
	trans_file_receive_buffer += socket_start_flag_length;
	socket_data_total_len = atoi((char *)trans_file_receive_buffer);
	if (0 == socket_data_total_len)
	{
		return 0;
	}
	trans_file_receive_buffer = trans_file_receive_buffer + receive_length - socket_start_flag_length;
	need_receive_data_len_rest = socket_data_total_len;
	while (receive_total_length < socket_data_total_len)
	{
		receive_length = tsf_socket_recv(ClientSocket, trans_file_receive_buffer, need_receive_data_len_rest, TSF_SOCKET_RECEIVE_TIMEOUT);
#if DEBUG_SOCK
		printf("++ func %s, line %d++++ sock %d recv\n", __FUNCTION__, __LINE__, ClientSocket);
#endif
		if (receive_length <= 0)
		{
			return receive_length;
		}
		trans_file_receive_buffer += receive_length;
		need_receive_data_len_rest -= receive_length;
		receive_total_length += receive_length;
	}

	return receive_total_length;
}

static void send_json_data_to_mobile_by_response_state(int client_socket, TRANS_FILE_MSG_TYPE request, void *json_root, unsigned int msg_response_state)
{
	json_struct_type json_type;
	json_type = convert_trans_file_msg_type_to_json_msg_type(request);
	switch(msg_response_state)
	{
		case TSF_RESPONSE_NO_ERROR:
		{
			send_json_data_to_mobile(client_socket, request, json_root, json_type, msg_response_state);
			break;
		}
		default:
		{
			send_common_data_to_mobile(client_socket, request, NULL, 0, msg_response_state);
			break;
		}
	}
}

static void trans_file_client_handle_message(TSF_CLIENT_CONFIG* current_client_config, TSF_MW_MSG msg)
{
	switch (msg.message_id)
	{
		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
		case TSF_MSG_CAST_DO_PLAY:
		{
			TRANS_FILE_RESPONSE_STATE msg_response_state = msg.param3;
			send_json_data_to_mobile_by_response_state(current_client_config->socket_id, msg.message_id, (void *)msg.param1, msg_response_state);
			break;
		}
		default:
			break;
	}
}

static void trans_file_client_task(void *argv)
{
	int delay_for_recv_message = TSF_WAIT_FOR_1_SEC / 4;
	TSF_MW_MSG msg;
	int client_index = (int)argv;
	TSF_CLIENT_CONFIG* current_client_config = NULL;
	TRANS_FILE_MSG_TYPE tsf_command_type = TSF_MSG_EVENT_MAX;

	if (client_index >= TSF_MAX_SUPPORT_CLIENT_NUM)
	{
		TRANS_FILE_PRINTF("too many client");
		release_trans_file_client_resouce_by_index(client_index, 1);
		return;
	}

	current_client_config = &(tsf_client_config_buff[client_index]);
	tsf_client_config_buff[client_index].recv_message_from_mobile_time_mark = tsf_get_system_time();
	while (1)
	{
		if (judge_socket_keep_alive(client_index) == 0)
		{
			TRANS_FILE_PRINTF("++++socket closed++++FUNCTION: %s +++LINE: %d ++++++++ \n", __FUNCTION__, __LINE__);
			break;
		}
		tsf_command_type = TSF_MSG_EVENT_MAX;
		/*********For receive socket ********/
		{
			int data_total_length = 0;

			memset(current_client_config->socket_recv_buff, 0, TSF_SOCKET_RECEIVE_BUFF_LENGTH);
			data_total_length = tsf_socket_modify_receive(current_client_config->socket_id, (unsigned char *)current_client_config->socket_recv_buff);

			if (data_total_length < 0)
			{
				TRANS_FILE_PRINTF("data_total_length error %d\n", data_total_length);
				break;
			}
			if (data_total_length == strlen(current_client_config->socket_recv_buff) - TSF_SOCKET_RECEIVE_HEADER_LENGTH)
			{
				TRANS_FILE_RESPONSE_STATE resp_state;

				resp_state = parser_json_file_to_command((unsigned char *)&((current_client_config->socket_recv_buff)[TSF_SOCKET_RECEIVE_HEADER_LENGTH]), client_index, current_client_config->socket_id, &tsf_command_type);
				tsf_client_config_buff[client_index].recv_message_from_mobile_time_mark = tsf_get_system_time();

				if (resp_state == TSF_RESPONSE_UNKNOWN_MESSAGE)
				{
					send_common_data_to_mobile(current_client_config->socket_id, tsf_command_type, NULL, 0, TSF_RESPONSE_UNKNOWN_MESSAGE);
				}
			}

			switch (tsf_command_type)
			{
				/***********************************************************************************************/
				/**Below message no need STB do some action, So send response data to mobile here. **/
				/***********************************************************************************************/
				case TSF_MSG_REQUEST_SOCKET_KEEP_ALIVE:
				{
					send_common_data_to_mobile(current_client_config->socket_id, tsf_command_type, NULL, 0, TSF_RESPONSE_NO_ERROR);
					switch (tsf_command_type)
					{
						default:
							break;
					}
					break;
				}
				default:
					break;
			}
		}
		/*********For receive socket ********/

		/*********For receive msg ********/
		if (0 == tsf_msg_queue_receive(current_client_config->msg_queue_id, &msg, sizeof(TSF_MW_MSG), delay_for_recv_message))
		{
			trans_file_client_handle_message(current_client_config, msg);
		}
		/*********For receive msg ********/

	}

	release_trans_file_client_resouce_by_index(client_index, 1);
	return;
}

int is_tsf_client_num_full(void)
{
	int tsf_client_num = 0;
	int tsf_client_full_flag = TSF_CONNECTED_NOT_FULL;

	tsf_client_num = get_free_client_resouce_index();

	if(tsf_client_num == TSF_MAX_SUPPORT_CLIENT_NUM)
	{
		tsf_client_full_flag = TSF_CONNECTED_IS_FULL;
	}
	return tsf_client_full_flag;
}


TRANS_FILE_RESPONSE_STATE trans_file_client_task_setup(int client_socket, int client_type)
{
	int client_index;
	TSF_CLIENT_CONFIG* current_client_config = NULL;
	int tsf_client_full_flag = 0;

	client_index = get_free_client_resouce_index();
	tsf_client_full_flag = is_tsf_client_num_full();

	if (tsf_client_full_flag)
	{
		unsigned char send_buff[TSF_BROADCAST_SEND_BUFF_LENGTH]= {0};

		//send msg to mobile to notice the device server is connected full.
		if (construct_trans_file_broadcast_msg(send_buff, tsf_client_full_flag) == TSF_RESPONSE_NO_ERROR)
		{
			TRANS_FILE_PRINTF("stb server is full [%s][%d] client_socket [%d]\n",__FUNCTION__,__LINE__, client_socket);
			tsf_socket_send(client_socket, send_buff, sizeof(TRANS_FILE_LOGIN_INFO), TSF_SOCKET_RECEIVE_TIMEOUT);
		}
		release_trans_file_client_resouce_by_index(client_index, 0);
		return TSF_RESPONSE_BEYOND_SUPPORT_CLIENT_NUM;
	}
	current_client_config = &(tsf_client_config_buff[client_index]);

	current_client_config->socket_id = client_socket;
	current_client_config->client_type = client_type;
	current_client_config->keep_alive_max_timeout = DEFAULT_SOCKET_KEEP_ALIVE_TIMEOUT;

	tsf_msg_queue_create(&(current_client_config->msg_queue_id), "MSG Client", 32, sizeof(TSF_MW_MSG));
	if (current_client_config->msg_queue_id == TSF_INVALID_HANDLE)
	{
		TRANS_FILE_PRINTF("create tsf msg queue failed\n");
		release_trans_file_client_resouce_by_index(client_index, 0);
		return TSF_RESPONSE_CREATE_MESSAGE_FAILED;
	}


	current_client_config->socket_recv_buff = (char *)malloc(TSF_SOCKET_RECEIVE_BUFF_LENGTH);
	if (current_client_config->socket_recv_buff == NULL)
	{
		release_trans_file_client_resouce_by_index(client_index, 0);
		return TSF_RESPONSE_FAIL;
	}

	if (tsf_thread_create(&(current_client_config->task_id), "trans_file_client_tasks", trans_file_client_task, (void*)client_index,
	        TSF_CLIENT_TASK_STACK_SIZE, tsf_thread_priority(TRANS_FILE_CLIENT1_THREAD+client_index)) != 0)
	{
		TRANS_FILE_PRINTF("create tsf client task failed\n");
		release_trans_file_client_resouce_by_index(client_index, 0);
		return TSF_RESPONSE_CREATE_TASK_FAILED;
	}

	return TSF_RESPONSE_NO_ERROR;
}

