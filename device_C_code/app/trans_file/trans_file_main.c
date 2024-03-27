/*
 * trans_file_main.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_main.h"
#include "trans_file/trans_file_interface.h"
#include "trans_file/trans_file_client.h"
#include "trans_file/trans_file_server.h"
#include "trans_file/trans_file_api.h"

typedef void (*task_handle_func)(U32 param, int size);

typedef struct trans_file_main_tasks_service
{
    signed char progress_value;

    /* resource */
    TSF_HANDLE thread_handle;
    TSF_HANDLE msg_queue_handle;

} TSF_MAIN_TASK_SERVICE;

typedef struct trans_file_main_tasks_msg
{
    U32 msg_id;
    U32 param;
    U32 param1;
    U32 param2;
} TSF_MAIN_TASK_MESSAGE;

static TSF_MAIN_TASK_SERVICE *trans_file_main_tasks_service = NULL;

TRANS_FILE_RESPONSE_STATE send_message_to_trans_file_main_task(unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3)
{
	TRANS_FILE_RESPONSE_STATE ret = TSF_RESPONSE_FAIL;
	TSF_MW_MSG msg;

	msg.message_id = msg_id;
	msg.param1 = param1;
	msg.param2 = param2;
	msg.param3 = param3;
	if (TSF_INVALID_HANDLE != trans_file_main_tasks_service->msg_queue_handle)
	{
		ret = tsf_msg_queue_send(trans_file_main_tasks_service->msg_queue_handle, &msg, sizeof(TSF_MAIN_TASK_MESSAGE), 10);

	}

	return ret;
}

static void trans_file_main_handle_message(TSF_MW_MSG msg)
{
	TRANS_FILE_RESPONSE_STATE callback_ret = TSF_RESPONSE_NO_ERROR;

	int client_index = msg.param1;
	unsigned int tsf_msg_type = msg.message_id;
	switch (tsf_msg_type)
	{
		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
			callback_ret = trans_file_callback(tsf_msg_type, 0, 0, NULL);
			send_message_to_trans_file_client_task(client_index, tsf_msg_type, 0, 0, callback_ret);
			break;
		case TSF_MSG_CAST_DO_PLAY:
		{
			TRANS_FILE_STREAM_INFO *tsf_file_info = (TRANS_FILE_STREAM_INFO*)msg.param2;
			callback_ret = trans_file_callback(tsf_msg_type, 0, tsf_file_info->mediaType, NULL);
			if(callback_ret == TSF_RESPONSE_NO_ERROR)
			{
				callback_ret = trans_file_callback(tsf_msg_type, (unsigned int)tsf_file_info, tsf_file_info->mediaType, NULL);
			}

			if ((callback_ret == TSF_RESPONSE_FAIL) && tsf_file_info)
			{
				free(tsf_file_info);
				tsf_file_info = NULL;
			}
			send_message_to_trans_file_client_task(client_index, tsf_msg_type, 0, 0, callback_ret);
			break;
		}
		default:
			break;
	}
}

static void trans_file_main_task(void *argv)
{
	int delay_for_recv_message = TSF_WAIT_FOR_1_SEC / 4;
	TSF_MW_MSG msg;


	while (1)
	{
		if (0 == tsf_msg_queue_receive(trans_file_main_tasks_service->msg_queue_handle, &msg, sizeof(TSF_MAIN_TASK_MESSAGE), delay_for_recv_message))
		{
			trans_file_main_handle_message(msg);
		}
		tsf_thread_sleep(TSF_MAIN_TASK_DELAY);
	}
}

static void cleanup_trans_file_main_tasks_service(void)
{
    if (trans_file_main_tasks_service == NULL)
        return;

    free(trans_file_main_tasks_service);
    trans_file_main_tasks_service = NULL;
}

TRANS_FILE_RESPONSE_STATE trans_file_main_task_setup(void)
{
    int err_code = -1;

	do
	{
	    trans_file_main_tasks_service = (TSF_MAIN_TASK_SERVICE *)malloc(sizeof(TSF_MAIN_TASK_SERVICE));
	    if (trans_file_main_tasks_service == NULL)
	        break;

	    memset(trans_file_main_tasks_service, 0, sizeof(TSF_MAIN_TASK_SERVICE));
	    trans_file_main_tasks_service->thread_handle = TSF_INVALID_HANDLE;
	    trans_file_main_tasks_service->msg_queue_handle = TSF_INVALID_HANDLE;

	    if (tsf_msg_queue_create(&(trans_file_main_tasks_service->msg_queue_handle), "trans_file_main_tasks", 32, sizeof(TSF_MAIN_TASK_MESSAGE)) != 0)
	    {
	        err_code = -2;
	        break;
	    }

	    if (tsf_thread_create(&(trans_file_main_tasks_service->thread_handle), "trans_file_main_tasks", trans_file_main_task, NULL, TSF_MAIN_TASK_STACK_SIZE, tsf_thread_priority(TRANS_FILE_MAIN_THREAD)) != 0)
	    {
	        err_code = -3;
	        break;
	    }

	    err_code = 0;
	} while (0);

	if (err_code != 0)
	{
		cleanup_trans_file_main_tasks_service();
	}
    return err_code;
}


