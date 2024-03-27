/*
 * trans_file_api.c
 */

#include "os/os_api.h"
#include "event/event_list.h"

#include "trans_file/trans_file_common.h"
#include "trans_file/trans_file_api.h"
#include "trans_file/trans_file_server.h"
#include "trans_file/trans_file_main.h"

#include "compr/compr_zlib.h"
#include "trans_file/trans_file_event.h"

/*stb info*/
char *tsf_get_model_name()
{
	return "Linux PC";
}

unsigned char tsf_get_platform_type()
{
	return 44;
}

unsigned long tsf_get_software_version_number()
{
	return 100;
}

char *tsf_get_software_version()
{
	static char soft_ver[20] = { 0 };
	int main_ver = tsf_get_software_version_number();
	sprintf(soft_ver, "%d.%02d", main_ver / 100, main_ver % 100);
	return soft_ver;
}

unsigned char tsf_get_customer_id()
{
	return 0;
}

unsigned char tsf_get_model_id()
{
	return 10;
}

int tsf_get_device_chip_id(U8 *id)//cpu id
{
	unsigned long long cpusn = 0x123456789abcdef0;
	memcpy(id, &cpusn, 8);
	return 0;
}

int tsf_get_device_sn(U8 *sn)
{
	return 0;
}

unsigned long tsf_get_software_sub_version_number()
{
	return 0;
}
/*stb info*/

/*system*/
U32 tsf_get_system_time(void)
{
    struct timespec time = {0, 0};
    clock_gettime(CLOCK_MONOTONIC, &time);
    return time.tv_sec * 1000 + time.tv_nsec / 1000000;
}

int tsf_compr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size)
{
	return compr_zlib(buf_out, out_size, buf_in, in_size);
}

void tsf_thread_sleep(U32 millisecond)
{
	usleep(millisecond * 1000);
}

int tsf_thread_create(TSF_HANDLE *handle, const char *name, void (*entry)(void*), void *arg, U32 stack_size, int priority)
{
	int ret = 0;
	pthread_t *thread_handle = malloc(sizeof(pthread_t));
	if(thread_handle == NULL)
		return -1;
	ret = create_pthread(thread_handle, entry, arg, stack_size, (char *)name);
	if(ret != 0)
	{
		return -1;
	}
	*handle = (TSF_HANDLE)thread_handle;
	return 0;
}

int tsf_thread_priority(TSFThreadID thread_id)
{
    return 0;
}

int tsf_thread_delete(TSF_HANDLE handle)
{
	pthread_t *thread_handle = (pthread_t *)handle;

    if (handle == TSF_INVALID_HANDLE || thread_handle == NULL)
        return -1;

    delete_pthread(thread_handle);
    free(thread_handle);

    return 0;
}

void tsf_thread_exit(void)
{
	pthread_detach(pthread_self());
	return;
}

int tsf_msg_queue_create(TSF_HANDLE *handle, const char *name, int elem_num, int elem_size)
{
	OS_Queue_t queue_param;
	int queue_id = 0;

	queue_param.buf_size = elem_num * elem_size;
	queue_param.element_size = elem_size;

	queue_id = os_msgqueue_create(&queue_param);
	if (queue_id == 0)
    {
        *handle = TSF_INVALID_HANDLE;
        return -1;
    }

	*handle = (TSF_HANDLE)queue_id;
	return 0;
}

int tsf_msg_queue_send(TSF_HANDLE handle, void *msg, int msg_size, U32 timeout)
{
	 return os_msgqueue_send((int)handle, msg, msg_size, timeout);
}

int tsf_msg_queue_receive(TSF_HANDLE handle, void *msg, int msg_size, U32 timeout)
{
	return os_msgqueue_receive((int)handle, msg, msg_size, timeout);
}

int tsf_msg_queue_delete(TSF_HANDLE handle)
{
	 return os_msgqueue_delete((int)handle);
}

int tsf_mutex_create(TSF_HANDLE *handle, const char *name)
{
    unsigned int mutex_id;
    mutex_id = os_mutex_create();
    if (mutex_id == 0)
    {
        *handle = TSF_INVALID_HANDLE;
        return -1;
    }
    *handle = mutex_id;
	return 0;
}

int tsf_mutex_lock(TSF_HANDLE handle, U32 timeout)
{
	return os_mutex_lock((unsigned int)(handle), timeout);
}

int tsf_mutex_unlock(TSF_HANDLE handle)
{
    return os_mutex_unlock((unsigned int)(handle));
}

int tsf_mutex_delete(TSF_HANDLE handle)
{
	 return os_mutex_delete((unsigned int)(handle));
}
/*system*/

/*network*/
extern int get_cur_network_type(void);//just for test
extern U32 network_get_ip(int type);//just for test

bool tsf_check_network_state(void)
{
	return (network_get_ip(get_cur_network_type()) != 0);
}

U32 tsf_network_get_ip(void)
{
	return network_get_ip(get_cur_network_type());
}

U32 tsf_htonl(U32 host)
{
	return htonl(host);
}

int tsf_socket_create(TSF_SOCKET_TYPE socket_type)
{
	int socket_handle = TSF_INVALID_SOCKET;

    if (socket_type == TSF_TCP_SOCKET)
    {
        socket_handle = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    }
    else if (socket_type == TSF_UDP_SOCKET)
    {
        socket_handle = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    }
    return socket_handle;
}

int tsf_socket_bind(int sk, unsigned short port)
{
    struct sockaddr_in bind_sa;

    if (sk == TSF_INVALID_SOCKET)
        return -1;

    memset(&bind_sa, 0, sizeof(struct sockaddr_in));
    bind_sa.sin_family = AF_INET;
    bind_sa.sin_addr.s_addr = htonl(INADDR_ANY); //INADDR_ANY
    bind_sa.sin_port = htons(port);

    return bind(sk, (struct sockaddr * )&bind_sa, sizeof(struct sockaddr_in));
}

int tsf_socket_listen(int sk, int backlog)
{
    int iRet = -1;
    if (sk != TSF_INVALID_SOCKET)
    {
        iRet = listen(sk, backlog);
    }
    return iRet;
}

int tsf_socket_accept(int sk, unsigned int *ip, unsigned short *port)
{
    struct sockaddr_in sin;
    int sin_len = sizeof(struct sockaddr);
    int ret_sk = TSF_INVALID_SOCKET;

    if (sk == TSF_INVALID_SOCKET)
        return TSF_INVALID_SOCKET;

    memset(&sin, 0, sizeof(struct sockaddr));
    ret_sk = accept(sk, (struct sockaddr * )&sin, (socklen_t *)&sin_len);
    if (ip)
    {
        *ip = sin.sin_addr.s_addr;
    }
    if (port)
    {
        *port = ntohs(sin.sin_port);
    }
    return ret_sk;
}

int tsf_socket_set_sock_opt(int sk, int level, int optname, const char *optval, int optlen)
{
	return setsockopt(sk, level, optname, optval, optlen);
}

int tsf_socket_set_buffer_size(int sk, int buffer_size, int read_buffer)
{
    int opt_name = SO_SNDBUF;
    socklen_t socket_len = sizeof(int);
    if (read_buffer == 1)
    {    opt_name = SO_RCVBUF;

    }
	return tsf_socket_set_sock_opt(sk, SOL_SOCKET, opt_name, (const const char*)&buffer_size, socket_len);
}

int tsf_socket_send_to(int sk, unsigned char *buff, int len, int to, unsigned int addr, unsigned short port)
{
    int iRet = 0;
    struct sockaddr_in socket_sa;
    socket_sa.sin_family = AF_INET;
    socket_sa.sin_port = htons(port);
    if (addr <= 0)
    {
        return iRet;
    }
    else
    {
        socket_sa.sin_addr.s_addr = addr;
    }
    if (sk != TSF_INVALID_SOCKET && buff && len > 0)
    {
        iRet = tsf_socket_select(sk, 2, to);

        if (iRet > 0)
        {
            iRet = sendto(sk, (char* )buff, len, 0, (struct sockaddr * )&socket_sa, sizeof(socket_sa));
        }
    }
    return iRet;
}

int tsf_socket_send(int sk, unsigned char *buff, int len, int to)
{
    int iRet = 0;

    if (sk != TSF_INVALID_SOCKET && buff && len > 0)
    {
        iRet = tsf_socket_select(sk, 2, to);

        if (iRet > 0)
        {
            iRet = send(sk, (char* )buff, len, 0);
        }
    }
    return iRet;
}

int tsf_socket_recv(int sk, unsigned char *buff, int len, int to)
{
    int iRet = 0;
    if (sk != TSF_INVALID_SOCKET && buff && len > 0)
    {
        iRet = tsf_socket_select(sk, 1, to);
        if (iRet > 0)
        {
            iRet = recv(sk, (char* )buff, len, 0);
            if (iRet == 0)
            {
                iRet = -1;
            }
        }
    }
    return iRet;
}

int tsf_socket_select_ex(int *sk, int sk_num, int flag, int to)
{
	fd_set fds;
	struct timeval tv;
	struct timeval *ptv = (struct timeval*) 0;
	int i, max_sk_id = -1;
	int ret_socket_id = TSF_INVALID_SOCKET;

	if (sk == NULL || sk_num <= 0)
		return ret_socket_id;

	FD_ZERO(&fds);
	for (i = 0; i < sk_num; i++)
	{
		if (sk[i] == TSF_INVALID_SOCKET)
			continue;
		FD_SET(sk[i], &fds);
		max_sk_id = TSF_MAX(max_sk_id, sk[i]);
	}

	if (max_sk_id != TSF_INVALID_SOCKET)
	{
		if (-1 != to)
		{
			tv.tv_sec = to / 1000;
			tv.tv_usec = (to % 1000) * 1000;
			ptv = &tv;
		}

		if (select(max_sk_id + 1, ((flag & 1) ? &fds : 0), ((flag & 2) ? &fds : 0), ((flag & 4) ? &fds : 0), ptv) > 0)
		{
			for (i = 0; i < sk_num; i++)
			{
				if (sk[i] == TSF_INVALID_SOCKET)
					continue;

				if (FD_ISSET(sk[i], &fds))
				{
					ret_socket_id = sk[i];
					break;
				}
			}
		}
	}

	for (i = 0; i < sk_num; i++)
	{
		if (sk[i] == TSF_INVALID_SOCKET)
			continue;
		FD_CLR(sk[i], &fds);
	}
	return ret_socket_id;
}

int tsf_socket_select(int sk, int flag, int to)
{
    fd_set fds;
    struct timeval tv;
    struct timeval *ptv = (struct timeval*)0;
    int iRet = -1;

    if (sk == TSF_INVALID_SOCKET)
        return -1;

    FD_ZERO(&fds);
    FD_SET(sk, &fds);

    if (-1 != to)
    {
        tv.tv_sec = to / 1000;
        tv.tv_usec = (to % 1000) * 1000;
        ptv = &tv;
    }

    iRet = select(sk + 1, ((flag & 1) ? &fds : 0), ((flag & 2) ? &fds : 0), ((flag & 4) ? &fds : 0), ptv);
    if (iRet > 0)
    {
        iRet = FD_ISSET(sk,&fds) ? 1 : 0;
    }
    FD_CLR(sk, &fds);

    return iRet;
}

void tsf_socket_close(int sk)
{
    if (sk != TSF_INVALID_SOCKET)
        close(sk);
}
/*network*/

/*play file*/
bool tsf_is_playing_file(void)
{
	return false;
}

bool tsf_is_valid_link(char *url)
{
	static char *net_media_suport_url[] =
	{
		"http://",
		"https://",
		"hls://",
		"V://",
		"A://",
	};
	int ret = false;
	int i = 0;

	if (NULL == url || strlen(url) == 0)
	{
		return ret;
	}

	for (i = 0; i < TSF_ARRAY_SIZE(net_media_suport_url); i++)
	{
		if (memcmp(url, net_media_suport_url[i], strlen(net_media_suport_url[i])) == 0)
		{
			ret = true;
			break;
		}
	}
	return ret;

}

int tsf_get_player_state(void)
{
	return 3;//0:play 2:pause 3: stop
}

int tsf_get_player_cur_play_time(void)
{
	return 0;
}

int tsf_get_player_play_total_time(void)
{
	return 0;
}
/*play file*/

/*msg*/
int convert_tsf_msg_type_to_stb_msg_type(int tsf_msg_type)
{
	int stb_msg_type = EVT_TSF_IDLE;

	switch(tsf_msg_type)
	{
		case TSF_MSG_CAST_REQUEST_STREAM_PLAY_INFO:
			stb_msg_type = EVT_TSF_CAST_REQUEST_STREAM_PLAY_INFO;
			break;
		case TSF_MSG_CAST_DO_PLAY:
			stb_msg_type = EVT_TSF_CAST_DO_PLAY;
			break;
		case TSF_MSG_EXIT_MENU:
			stb_msg_type = EVT_TSF_IDLE;
			break;
		default:
			break;
	}
	return stb_msg_type;
}

bool tsf_need_check_result(int stb_msg_type)
{
	switch (stb_msg_type)
	{
		case EVT_TSF_CAST_REQUEST_STREAM_PLAY_INFO:
		case EVT_TSF_CAST_DO_PLAY:
		case EVT_TSF_IDLE:
			return true;
		default:
			break;
	}
	return false;
}
/*msg*/

const char *get_trans_file_type(int mediaType)
{
	switch(mediaType)
	{
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_UNKNOW);
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_MOVIE);
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_IMAGE);
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_AUDIO);
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_LIVE);
		TSF_ENUM_CASE(TRANS_FILE_MEDIA_LIVE_ANDROID);
		TSF_ENUM_CASE(TRANS_FILE_OTHER_FILE);
		default:
			break;
	}
	return "Unknow";
}

int init_trans_file(void)
{
	trans_file_init_msg();
	trans_file_server_task_setup();
	trans_file_main_task_setup();
	trans_file_callback(TSF_MSG_CMD_INIT, 0, 0, 0);
	return 0;
}

