#include <stdio.h>
#include <signal.h>

#include "os/os_api.h"
#include "event/event_list.h"
#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/trans_file_action.h"
#include "trans_file/trans_file_event.h"

static char pc_ipaddr[64] = { 0 };

/*net for test*/
int get_cur_network_type(void)
{
    return 0;
}

U32 network_get_ip(int type)
{
	if (type == -1)
		return 0;
	unsigned int uiA0 = 0, uiA1 = 0, uiA2 = 0, uiA3 = 0;
	sscanf(pc_ipaddr, "%u.%u.%u.%u", &uiA0, &uiA1, &uiA2, &uiA3);
	return ntohl((uiA0 << 24) | (uiA1 << 16) | (uiA2 << 8) | uiA3);
}
/*net for test*/

bool tsf_platform_app_handle(UIEvent event, U32 event_data)
{
	U32 *data = (U32 *)event_data;
	U32 param1 = 0, param2 = 0, param3 = 0;
	TRANS_FILE_STREAM_INFO stream_info;

	if (event.id < EVT_TSF_CAST_REQUEST_STREAM_PLAY_INFO || event.id > EVT_TSF_START_TRANS_OTHER_FILE_URL)
		return false;

	if ((event.id == EVT_TSF_START_PLAY_TRANS_FILE_URL) || (event.id == EVT_TSF_START_TRANS_OTHER_FILE_URL))
	{
		memset(&stream_info, 0, sizeof(stream_info));
		param1 = event_data;
		param2 = (U32)&stream_info;
		data = NULL;
	}
	else if (data != NULL)
	{
		param1 = data[0];
		param2 = data[1];
		param3 = data[2];
	}
	else
	{
		param1 = event_data;
	}

	if (event.id == EVT_TSF_CAST_DO_PLAY)
	{
		bool stb_is_busy = false;//todo
		if ((stb_is_busy == false) && ((param2 == TRANS_FILE_OTHER_FILE) || (tsf_is_playing_file() == false)))
		{
			//todo switch to playing page
		}
		param2 = stb_is_busy ? 0 : 1;
	}

	tsf_platform_action(event.id, param1, param2, param3);

	if (param2 == (U32)&stream_info)
	{
		printf("app get link: %s\n", stream_info.stream_url);
		printf("file type: %s\n", get_trans_file_type(stream_info.mediaType));
	}

	if (data)
	{
		free(data);
		data = NULL;
	}
	return true;
}

static char *get_eth_device(void)
{
	if (!access("/sys/class/net/enp2s0", 0))
		return "enp2s0";
	return "eth0";
}

int get_pc_ip(void)
{
	int sockfd = 0, res;
	struct sockaddr_in *sin;
	struct ifreq request;

	do
	{
		if ((sockfd = tsf_socket_create(SOCK_STREAM)) == TSF_INVALID_SOCKET)
			break;

		memset(&request, 0, sizeof(request));
		snprintf(request.ifr_name, sizeof(request.ifr_name), "%s", get_eth_device());

		res = ioctl(sockfd, SIOCGIFADDR, &request);
		if(res < 0)
			break;

		sin = (struct sockaddr_in *)&request.ifr_addr;
		snprintf(pc_ipaddr, sizeof(pc_ipaddr), "%s", (char*) inet_ntoa(sin->sin_addr));
	} while (0);

	if(sockfd != TSF_INVALID_SOCKET)
	{
		tsf_socket_close(sockfd);
		sockfd = TSF_INVALID_SOCKET;
	}
	return 0;
}

int main(int argc, char **argv)
{
	EventMsgNode msg;

	get_pc_ip();
	init_trans_file();

	while (1)
	{
		memset(&msg, 0, sizeof(msg));
		if (app_recv_trans_file_msg(&msg) == 0)
		{
			tsf_platform_app_handle(msg.event, msg.param2);
		}
	}
	return 0;
}
