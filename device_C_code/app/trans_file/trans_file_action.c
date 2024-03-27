/*
 * trans_file_platform_action.c
 */
#include "event/event_list.h"
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_action.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/trans_file_interface.h"
#include "trans_file/trans_file_event.h"

bool tsf_platform_action(U32 msg_id, U32 param1, U32 param2, U32 param3)
{
	int receive_evt =  msg_id;
	int ret_status = false;

	switch (receive_evt)
	{

		case EVT_TSF_IDLE:
			if (param1 != EXIT_TO_MAIN_MENU)
			{
//				printf("+++ func: %s, line: %d +++ @@@@ EXIT ALL\n", __FUNCTION__, __LINE__);
				//standby to play, switch page to video play or update menu todo
			}
			else
			{
//				printf("+++ func: %s, line: %d +++ @@@@ EXIT_TO_MAIN_MENU\n", __FUNCTION__, __LINE__);
				ret_status = true;
				send_responses_msg(receive_evt, ret_status, 0, 0);
			}
			break;
		case EVT_TSF_CAST_REQUEST_STREAM_PLAY_INFO:
			ret_status = true;
			send_responses_msg(receive_evt, ret_status, 0, 0);
			break;
		case EVT_TSF_CAST_DO_PLAY:
		{
			if (!param1)
			{
				ret_status = param2 ? true : false;
				send_responses_msg(receive_evt, ret_status, 0, 0);
				break;
			}
			TRANS_FILE_STREAM_INFO *steam_file_info = (TRANS_FILE_STREAM_INFO *)param1;
			if (!steam_file_info)
			{
				break;
			}

			if (tsf_is_valid_link(steam_file_info->stream_url))
			{
				U32 send_msg_id = EVT_TSF_START_PLAY_TRANS_FILE_URL;

				if (steam_file_info->mediaType == TRANS_FILE_OTHER_FILE)
				{
					send_msg_id = EVT_TSF_START_TRANS_OTHER_FILE_URL;
				}

				if (tsf_send_msg_to_app(TSF_SEND_AUTO, send_msg_id, (U32)steam_file_info) != 0)
				{
					free(steam_file_info);
				}
				ret_status = true;
			}
			send_responses_msg(receive_evt, ret_status, 0, 0);
			break;
		}

		case EVT_TSF_START_PLAY_TRANS_FILE_URL:
		case EVT_TSF_START_TRANS_OTHER_FILE_URL:
		{
			TRANS_FILE_STREAM_INFO *play_file_info = (TRANS_FILE_STREAM_INFO *)param1;
			TRANS_FILE_STREAM_INFO *output_file_info = (TRANS_FILE_STREAM_INFO *)param2;
			if (play_file_info != NULL)
			{
				if (output_file_info)
				{
					memcpy(output_file_info, play_file_info, sizeof(TRANS_FILE_STREAM_INFO));
				}
				free(play_file_info);
			}
			break;
		}

		default:
			break;
	}


	return ret_status;
}


