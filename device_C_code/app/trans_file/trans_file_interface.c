/*
 * trans_file_interface.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_interface.h"
#include "trans_file/json_convert/json_write.h"
#include "trans_file/trans_file_action.h"
#include "trans_file/trans_file_client.h"
#include "trans_file/trans_file_event.h"
#include "trans_file/trans_file_api.h"

static TSF_HANDLE s_responses_msg_queue_id = TSF_INVALID_HANDLE;

static int get_responses_wait_loop_number_by_msg(int msg_type)
{
	int loop_number = 0;

	switch (msg_type)
	{
		case TSF_MSG_EXIT_MENU:
			loop_number = 10;
			break;
		default:
			loop_number = 4;
			break;
	}

	return loop_number;
}

TRANS_FILE_RESPONSE_STATE trans_file_callback(unsigned int msg_id, unsigned int param1, unsigned int param2, void* param3)
{
	TRANS_FILE_RESPONSE_STATE callback_ret = TSF_RESPONSE_NO_ERROR;
	int timeout_loop = 0;
	TSF_MW_MSG msg;

	switch (msg_id)
	{
		case TSF_MSG_CMD_INIT:
		{
			tsf_msg_queue_create(&s_responses_msg_queue_id, "TSF Response"/*"TSF Response"*/, 32, sizeof(TSF_MW_MSG));
			if (s_responses_msg_queue_id == TSF_INVALID_HANDLE)
			{
				callback_ret = TSF_RESPONSE_CREATE_MESSAGE_FAILED;
			}
			break;
		}
		default:
		{
			TRANS_FILE_MSG_TYPE tsf_msg_type = msg_id;
			int stb_msg_type = convert_tsf_msg_type_to_stb_msg_type(tsf_msg_type);
			int is_send_msg_success = -1;
			U32 *data = malloc(3 * sizeof(U32));
			data[0] = param1;
			data[1] = param2;
			data[2] = (int)param3;
			is_send_msg_success = tsf_send_msg_to_app(TSF_SEND_DESK, stb_msg_type, (U32) data);
			if (is_send_msg_success == 0)
			{
				int recv_msg_state = 0;
				while ((timeout_loop++) < get_responses_wait_loop_number_by_msg(msg_id))
				{
					recv_msg_state = tsf_msg_queue_receive(s_responses_msg_queue_id, &msg, sizeof(TSF_MW_MSG), TSF_WAIT_FOR_1_SEC / 2);
					if (0 == recv_msg_state && stb_msg_type == msg.message_id)
					{
						if (tsf_need_check_result(msg.message_id))
						{
							if (msg.param1 != true)
							{
								TRANS_FILE_PRINTF("++++++++++++++++ msg type: %u\n", msg.message_id);
								callback_ret = TSF_RESPONSE_FAIL;
							}
						}
						break; //When receive the msg success, exit the wait.
					}
					else
					{
						tsf_thread_sleep(TSF_WAIT_FOR_1_SEC/2);
					}
				}
				if (timeout_loop >= get_responses_wait_loop_number_by_msg(msg_id))
				{
					printf("multi: msg_id-%d, timeout_loop-%d\n", msg_id, timeout_loop);
					callback_ret = TSF_RESPONSE_TIMEOUT;
				}
			}
			else
			{
				if (data) free(data);
				callback_ret = TSF_RESPONSE_FAIL;
			}
			break;
		}
	}
	return callback_ret;
}

TRANS_FILE_RESPONSE_STATE send_responses_msg(unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_FAIL;
	TSF_MW_MSG msg;

	msg.message_id = msg_id;
	msg.param1 = param1;
	msg.param2 = param2;
	msg.param3 = param3;
	if (TSF_INVALID_HANDLE != s_responses_msg_queue_id)
	{
		ret = tsf_msg_queue_send(s_responses_msg_queue_id, (void *)&msg, sizeof(TSF_MW_MSG), QUEUE_MSG_SEND_TIMEOUT);
	}
	return ret;
}

