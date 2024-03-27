/*
 * json_write.h
 */

#ifndef JSON_WRITE_H_
#define JSON_WRITE_H_

typedef enum _json_struct_type_
{
	JSON_STRUCT_STREAM_PLAY_INFO = 35,

	JSON_STRUCT_MAX,

}json_struct_type;

bool create_json_buff(char *buffer, void *json_root, json_struct_type type);

#define MAX_LEN_FOR_EVENT_DESC_JSON (4 * 1024)
#define MAX_CREATE_CHANNEL_LIST_PROGRAM_NO_PER_COMPRESS (500)

#endif /* JSON_WRITE_H_ */
