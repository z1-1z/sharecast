/*
 * trans_file_main.h
 */

#ifndef TRANS_FILE_MAIN_H_
#define TRANS_FILE_MAIN_H_

#include "trans_file/trans_file_interface.h"


#define TSF_MAIN_TASK_STACK_SIZE			(6 * 1024)
#define TSF_MAIN_TASK_PRIORITY			(OSAL_PRI_NORMAL)
#define TSF_MAIN_TASK_DELAY				(TSF_WAIT_FOR_1_SEC / 4)

TRANS_FILE_RESPONSE_STATE trans_file_main_task_setup(void);
TRANS_FILE_RESPONSE_STATE send_message_to_trans_file_main_task(unsigned int msg_id, unsigned int param1, unsigned int param2, unsigned int param3);

#endif /* TRANS_FILE_MAIN_H_ */
