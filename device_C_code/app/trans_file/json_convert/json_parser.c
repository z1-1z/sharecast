/*
 * json_parser.c
 */
#include "trans_file/trans_file_common.h"
#include "trans_file/json_convert/json_parser.h"

#ifndef ITOA
#define ITOA(buf, value) snprintf(buf, sizeof(buf), "%d", value)
#endif

json_root_info* json_string_parser(char *pstart)
{
	json_root_info *json_root = NULL;

	if (NULL == pstart)
	{
		return NULL;
	}
	json_root = (json_root_info *)malloc(sizeof(json_root_info));
	if (NULL == json_root)
	{
		return NULL;
	}
	memset(json_root, 0, sizeof(json_root_info));
	json_command_parser(pstart, json_root);
	return json_root;
}

void json_free_string_parser(json_root_info *json_root)
{

	json_node_info *node_info_temp_a = NULL;
	json_node_info *node_info_temp_b = NULL;

	if (json_root == NULL)
	{
		return;
	}


	//free node list
	for (node_info_temp_a = json_root->next; node_info_temp_a != NULL;)
	{
		node_info_temp_b = node_info_temp_a;
		node_info_temp_a = node_info_temp_a->next;
		free(node_info_temp_b);
	}

	free(json_root);
}

void parse_json_sub_type_others(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point, int type)
{

	int text_int = 0;
	char *text_string = NULL;

	char *tag = (*psub)->string;

	if (cJSON_Number == type)
	{
		text_int = (*psub)->valueint;
	}
	else
	{
		text_string = (*psub)->valuestring;
	}

	if (strcmp(tag, JSON_HEADER_CONTROL_TAG) != 0)
	{
		(*tag_node) = (json_node_info *)malloc(sizeof(json_node_info));
		memset((*tag_node)->tag_name, 0, sizeof((*tag_node)->tag_name));
		memset((*tag_node)->tag_text, 0, sizeof((*tag_node)->tag_text));

		strcpy((*tag_node)->tag_name, tag);
		if (cJSON_Number == type)
		{
			ITOA((*tag_node)->tag_text, text_int);
		}
		else
		{
			snprintf((*tag_node)->tag_text, sizeof((*tag_node)->tag_text), "%s", text_string);
		}
		(*tag_node)->next = NULL;

		if ((*proot_node)->next == NULL)
		{
			(*proot_node)->next = (*tag_node);
			(*tag_point) = (*tag_node);
		}
		else
		{
			(*tag_point)->next = (*tag_node);
			(*tag_point) = (*tag_node);
		}
	}
	else
	{
		strcpy((*proot_node)->command_type, text_string);
	}

}

void parse_json_sub_type_array(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point)
{

	int j = 0;
	for (j = 0; j < cJSON_GetArraySize((*psub)); j++)
	{
		cJSON *psub_sub = cJSON_GetArrayItem((*psub), j);

		if (psub_sub->type == cJSON_Object)
		{
			parse_json_sub_type_obj(&psub_sub, tag_node, proot_node, tag_point);
		}
	}

}

void parse_json_sub_type_obj(cJSON** psub, json_node_info** tag_node, json_root_info** proot_node, json_node_info** tag_point)
{

	int j = 0;
	for (j = 0; j < cJSON_GetArraySize((*psub)); j++)
	{
		cJSON *psub_sub = cJSON_GetArrayItem((*psub), j);
		parse_json_sub_type_others(&psub_sub, tag_node, proot_node, tag_point, psub_sub->type);
	}

}

int parse_cjson_to_json_root(cJSON *pjson, json_root_info *proot_node)
{
	int i = 0;
	int size = 0;

	json_node_info* tag_node;
	json_node_info* tag_point = NULL;

	if (NULL == proot_node)
	{
		return -1;
	}

	if (NULL == pjson)
	{
		return -1;
	}

	(proot_node)->next = NULL;
	size = cJSON_GetArraySize(pjson);
	for (i = 0; i < size; i++)
	{
		cJSON *psub = cJSON_GetArrayItem(pjson, i);
		switch (psub->type)
		{
			case cJSON_Array:
				parse_json_sub_type_array(&psub, &tag_node, &proot_node, &tag_point);
				break;
			case cJSON_Object:
				parse_json_sub_type_obj(&psub, &tag_node, &proot_node, &tag_point);
				break;
			case cJSON_String:
			case cJSON_Number:
				parse_json_sub_type_others(&psub, &tag_node, &proot_node, &tag_point, psub->type);
				break;
			default:
				return -1;
		}

	}
	return 0;
}

bool judge_trans_file_json_validity(char *json_string)
{
	int start_tag = 0;
	int end_tag = 0;
	int colons_count = 0;
	int json_index_n = 0;
	int json_string_len = 0;

	json_string_len = strlen(json_string);
	if ('[' == json_string[json_index_n]) ///[{:},{:},{:},...]
	{
		json_index_n++;
		while (']' != json_string[json_index_n])
		{
			if ('{' == json_string[json_index_n])
			{
				start_tag++;
			}
			else if ('}' == json_string[json_index_n])
			{
				end_tag++;
			}
			else if (':' == json_string[json_index_n])
			{
				colons_count++;
			}
			json_index_n++;
		}
		if (start_tag == end_tag && colons_count > 0)
		{
			return true;
		}
	}
	else if ('{' == json_string[json_index_n])
	{
		while ('[' != json_string[json_index_n] && json_index_n < json_string_len)
		{
			if (':' == json_string[json_index_n])
			{
				colons_count++;
			}
			json_index_n++;
		}
		if (json_index_n < json_string_len) ///{:[{:},{:},{:},..],:}
		{
			while (']' != json_string[json_index_n])
			{
				if ('{' == json_string[json_index_n])
				{
					start_tag++;
				}
				else if ('}' == json_string[json_index_n])
				{
					end_tag++;
				}
				else if (':' == json_string[json_index_n])
				{
					colons_count++;
				}
				json_index_n++;
			}
			while (json_index_n < json_string_len)
			{
				if (':' == json_string[json_index_n])
				{
					colons_count++;
				}
				json_index_n++;
			}
			if (start_tag == end_tag && '}' == json_string[json_string_len - 1] && colons_count > 0)
			{
				return true;
			}
		}
		else ///{:,:,:,...}
		{
			if ('}' == json_string[json_string_len - 1] && colons_count > 0)
			{
				return true;
			}
		}
	}
	return false;
}

void json_command_parser(char *pstart, json_root_info *proot_node)
{
	cJSON *pjson = cJSON_Parse(pstart);
	parse_cjson_to_json_root(pjson, proot_node);
	cJSON_Delete(pjson);
}

bool tsf_parser_large_link(char *json_buf, const char *format, char *url, int url_size)
{
	cJSON *root = NULL;
	bool func_ret = false;

	do
	{
		if (json_buf == NULL || format == NULL || url == NULL)
			break;

		root = cJSON_Parse(json_buf);
		if (root == NULL)
			break;

		if (cjson_get_string(root, format, url, url_size) == NULL)
			break;
		func_ret = true;
	} while (0);

	if (root)
	{
		cJSON_Delete(root);
		root = NULL;
	}
	return func_ret;
}
