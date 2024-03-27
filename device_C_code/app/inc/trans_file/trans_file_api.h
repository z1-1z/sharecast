/*
 * trans_file_api.h
 */

#ifndef APP_TRANS_FILE_API_H_
#define APP_TRANS_FILE_API_H_

#include "trans_file/trans_file_interface.h"

#define TSF_INVALID_SOCKET  	(-1)
#define TSF_SOL_SOCKET			(1)
#define TSF_SO_BROADCAST	    (6)

/** 255.255.255.255 */
#define TSF_IPADDR_BROADCAST    ((U32)0xffffffffUL)

typedef enum
{
	TSF_UDP_SOCKET,
	TSF_TCP_SOCKET,
} TSF_SOCKET_TYPE;

typedef enum
{
	TRANS_FILE_MAIN_THREAD,
	TRANS_FILE_SERVER_THREAD,
	TRANS_FILE_CLIENT1_THREAD,
	TRANS_FILE_CLIENT2_THREAD,
} TSFThreadID;

char *tsf_get_model_name();
unsigned char tsf_get_platform_type();
unsigned long tsf_get_software_version_number();
char *tsf_get_software_version();
unsigned char tsf_get_customer_id();
unsigned char tsf_get_model_id();
int tsf_get_device_chip_id(U8 *id);
int tsf_get_device_sn(U8 *sn);
unsigned long tsf_get_software_sub_version_number();

U32 tsf_get_system_time(void);

int tsf_compr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size);

void tsf_thread_sleep(U32 millisecond);

int  tsf_thread_create(TSF_HANDLE *handle, const char *name, void (*entry)(void*), void *arg, U32 stack_size, int priority);
int  tsf_thread_priority(TSFThreadID thread_id);
int  tsf_thread_delete(TSF_HANDLE handle);
void tsf_thread_exit(void);

int  tsf_msg_queue_create(TSF_HANDLE *handle, const char *name, int elem_num, int elem_size);
int  tsf_msg_queue_send(TSF_HANDLE handle, void *msg, int msg_size, U32 timeout);
int  tsf_msg_queue_receive(TSF_HANDLE handle, void *msg, int msg_size, U32 timeout);
int  tsf_msg_queue_delete(TSF_HANDLE handle);

int  tsf_mutex_create(TSF_HANDLE *handle, const char *name);
int  tsf_mutex_lock(TSF_HANDLE handle, U32 timeout);
int  tsf_mutex_unlock(TSF_HANDLE handle);
int  tsf_mutex_delete(TSF_HANDLE handle);

bool tsf_check_network_state(void);
U32  tsf_network_get_ip(void);
U32  tsf_htonl(U32 host);

int  tsf_socket_create(TSF_SOCKET_TYPE socket_type);
int  tsf_socket_bind(int sk, unsigned short port);
int  tsf_socket_listen(int sk, int backlog);
int  tsf_socket_accept(int sk, unsigned int *ip, unsigned short *port);
int  tsf_socket_set_sock_opt(int sk, int level, int optname, const char *optval, int optlen);
int  tsf_socket_set_buffer_size(int sk, int buffer_size, int read_buffer);
int  tsf_socket_send_to(int sk, unsigned char *buff, int len, int to, unsigned int addr, unsigned short port);
int  tsf_socket_send(int sk, unsigned char *buff, int len, int to);
int  tsf_socket_recv(int sk, unsigned char *buff, int len, int to);
int  tsf_socket_select_ex(int *sk, int sk_num, int flag, int to);
int  tsf_socket_select(int sk, int flag, int to);
void tsf_socket_close(int sk);

bool tsf_is_playing_file(void);
bool tsf_is_valid_link(char *url);
int  tsf_get_player_state(void);
int  tsf_get_player_cur_play_time(void);
int  tsf_get_player_play_total_time(void);

int  convert_tsf_msg_type_to_stb_msg_type(int tsf_msg_type);
bool tsf_need_check_result(int stb_msg_type);

const char *get_trans_file_type(int mediaType);

int init_trans_file(void);

#endif /* APP_TRANS_FILE_API_H_ */
