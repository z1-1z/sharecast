/*
 * trans_file_event.h
 */

#ifndef _TRANS_FILE_TRANS_FILE_EVENT_H_
#define _TRANS_FILE_TRANS_FILE_EVENT_H_

typedef struct event
{
	U32 id						:20;
	U32 reserved1				:12;
} UIEvent;

typedef struct
{
    UIEvent event;
    U32 param1;
    U32 param2;
    U32 param3;
} EventMsgNode;

void trans_file_init_msg(void);
void release_app_msg(void);

int app_recv_trans_file_msg(EventMsgNode *p_msg);
int tsf_send_msg_to_app(U32 tag, U32 evt_id, U32 event_data);

#endif /* _TRANS_FILE_TRANS_FILE_EVENT_H_ */
