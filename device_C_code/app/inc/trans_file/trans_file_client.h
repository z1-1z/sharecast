/*
 * trans_file_client.h
 */
#ifndef TRANS_FILE_CLIENT_H_
#define TRANS_FILE_CLIENT_H_
#define REQUEST_LOGIN_TYPE "998"
#define REQUEST_TYPE_SIZE 3
#define REQUEST_TYPE_BEGIN_INDEX  74
#define REQUEST_TYPE_END_INDEX	  77

#include "trans_file/trans_file_interface.h"
#include "trans_file/trans_file_server.h"

#define TSF_CLIENT_TASK_STACK_SIZE			(4 * 1024)
#define TSF_CLIENT_TASK_PRIORITY			(OSAL_PRI_NORMAL)
#define TSF_CLIENT_TASK_DELAY				(TSF_WAIT_FOR_1_SEC / 4)

TRANS_FILE_RESPONSE_STATE send_message_to_trans_file_client_task(int client_index, unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3);
void init_all_trans_file_client_resouce(void);
void release_all_trans_file_client_resouce(void);
int get_free_client_resouce_index(void);
int is_tsf_client_num_full(void);
TRANS_FILE_RESPONSE_STATE trans_file_client_task_setup(int client_socket, int client_type);

#endif /* TRANS_FILE_CLIENT_H_ */
