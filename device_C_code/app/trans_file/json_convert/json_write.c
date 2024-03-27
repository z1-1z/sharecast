/*
 * json_write.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_interface.h"
#include "trans_file/trans_file_server.h"
#include "trans_file/json_convert/json_write.h"
#include "trans_file/trans_file_api.h"
#include <json/cJSON.h>


static bool create_common_data_by_json(char *buffer)
{

	char *out = NULL;
	cJSON *root;
	cJSON *common_obj;

	if(buffer == NULL)
	{
		return false;
	}

	root = cJSON_CreateArray();
	common_obj = cJSON_CreateObject();
	cJSON_AddItemToArray(root, common_obj);
	//Command
	cJSON_AddStringToObject(common_obj, "Command", "");

	out = cJSON_Print(root);
	strcpy(buffer, out);
	free(out);
	cJSON_Delete(root);
	return true;
}

char general_buffer[MAX_SIZE_OF_GENERAL_BUFFER];

static bool create_stream_play_info(char *buffer)
{
	char *out = NULL;
	cJSON *root;
	cJSON *play_info_obj;

	if(buffer == NULL)
	{
		return false;
	}

	root = cJSON_CreateArray();
	play_info_obj = cJSON_CreateObject();

	cJSON_AddItemToArray(root, play_info_obj);

	if (tsf_is_playing_file())
	{
		int media_type = general_buffer[sizeof(general_buffer) - 1];
		//content
		cJSON_AddStringToObject(play_info_obj, "url", general_buffer);
		cJSON_AddNumberToObject(play_info_obj, "mediaType", media_type);
		cJSON_AddStringToObject(play_info_obj, "title", "MStream");
		if (media_type == TRANS_FILE_MEDIA_MOVIE || media_type == TRANS_FILE_MEDIA_AUDIO)
		{
			cJSON_AddNumberToObject(play_info_obj, "stateCode", tsf_get_player_state());
			cJSON_AddNumberToObject(play_info_obj, "currentTime", tsf_get_player_cur_play_time() * TSF_WAIT_FOR_1_SEC);
			cJSON_AddNumberToObject(play_info_obj, "totalTime", tsf_get_player_play_total_time() * TSF_WAIT_FOR_1_SEC);
		}
	}
	else
	{
		cJSON_AddNumberToObject(play_info_obj, "mediaType", TRANS_FILE_MEDIA_UNKNOW);
	}

	out = cJSON_Print(root);
	strcpy(buffer, out);
	free(out);
	cJSON_Delete(root);
	return true;
}


bool create_json_buff(char *json_buffer, void *json_root, json_struct_type type)
{
	bool ret = false;

	if (NULL == json_buffer)
	{
		return ret;
	}

	switch(type)
	{
		case JSON_STRUCT_STREAM_PLAY_INFO:
			ret = create_stream_play_info(json_buffer);
			break;
		default:
			ret = create_common_data_by_json(json_buffer);
			break;
	}

	return ret;
}


