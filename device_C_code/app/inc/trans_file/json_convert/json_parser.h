/*
 * json_parser.h
 */

#ifndef JSON_PARSER_H_
#define JSON_PARSER_H_

#include <json/cJSON.h>


#define JSON_HEADER_CONTROL_TAG "request"
#define FILE_SIZE   100
#define FIELD_NODE_TEXT  (1024 + 128)
#define COMMAND_TYPE_MAX_LENGTH   (8)

typedef struct json_attr_info
{
	char name[FILE_SIZE];
	char value[FILE_SIZE];
	struct json_attr_info *next;
} json_attr_info;

typedef struct json_node_info
{
	char tag_name[FILE_SIZE];
	char tag_text[FIELD_NODE_TEXT];

	//int tag_level;
	struct json_node_info* next;
} json_node_info;

typedef struct json_root_info
{
	char tag_name[FILE_SIZE];
	char json_encoding[FILE_SIZE];

	//json_attr_info* attr_list; //
	char command_type[COMMAND_TYPE_MAX_LENGTH];
	struct json_node_info* next;
} json_root_info;

static inline json_node_info *find_node_by_tag_name(const char *tag, json_node_info *pointer)
{
	while ((NULL != pointer) && strcmp(pointer->tag_name, tag) != 0) { pointer = pointer->next; }
	return pointer;
}

void json_command_parser(char *pstart, json_root_info* proot_node);
char* json_root_parser(char *pstart, json_root_info* proot_node);
json_root_info* json_string_parser(char *pstart);
json_root_info* command_json_parser(char *pjson_file);
void json_free_string_parser(json_root_info* json_root);
bool judge_trans_file_json_validity(char *pstart);

void parse_json_sub_type_others(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point, int type);
void parse_json_sub_type_array(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point);
void parse_json_sub_type_obj(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point);

bool tsf_parser_large_link(char *json_buf, const char *format, char *url, int url_size);
#endif /* 	JSON_PARSER_H_ */
