#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/trans_file_interface.h"
#include "trans_file/trans_file_event.h"

#define EVENT_QUEUE_SIZE        100

static TSF_HANDLE g_app_msg_queue = TSF_INVALID_HANDLE;

void trans_file_init_msg(void)
{
    tsf_msg_queue_create(&g_app_msg_queue, "TSF Event", EVENT_QUEUE_SIZE, sizeof(EventMsgNode));
}

void trans_file_release_msg(void)
{
	if (g_app_msg_queue != TSF_INVALID_HANDLE)
	{
		tsf_msg_queue_delete(g_app_msg_queue);
		g_app_msg_queue = TSF_INVALID_HANDLE;
	}
}

int app_recv_trans_file_msg(EventMsgNode *p_msg)
{
	return tsf_msg_queue_receive(g_app_msg_queue, p_msg, sizeof(EventMsgNode), 100);
}

int tsf_send_msg_to_app(U32 tag, U32 evt_id, U32 event_data)
{
	EventMsgNode node;
	memset(&node, 0, sizeof(node));
	node.event.id = evt_id;
	node.param1 = tag;
	node.param2 = event_data;
	return tsf_msg_queue_send(g_app_msg_queue, &node, sizeof(EventMsgNode), 100);
}
