#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>

#include <fcntl.h>
#include <unistd.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <sys/prctl.h>

#include "os/os_api.h"

#if 1
#define DEBUG(fmt, args...) printf("\033[32m[%s:%d]\033[36m "fmt"\033[0m", __FUNCTION__, __LINE__, ##args);
#else
#define DEBUG(fmt, args...) do{}while(0);
#endif

#if 1
#define DEBUG_WARN(fmt, args...) printf("\033[32m[%s:%d]\033[36m "fmt"\033[0m", __FUNCTION__, __LINE__, ##args);
#else
#define DEBUG_WARN(fmt, args...) do{}while(0);
#endif

#if 1
#define DEBUG_ERR(fmt, args...) printf("\033[32m[%s:%d]\033[36m "fmt"\033[0m", __FUNCTION__, __LINE__, ##args);
#else
#define DEBUG_ERR(fmt, args...) do{}while(0);
#endif

typedef struct
{
	void *(*func)(void *);
	void *arg;
	char name[16];
}thread_arg_t;

static void *thread_run(void *arg)
{
	void *func_ret = NULL;
	thread_arg_t *p_arg = arg;

	if(p_arg == NULL)
		return func_ret;

	do
	{
		if(p_arg->func == NULL)
			break;

		if(strlen(p_arg->name) > 0)
			prctl(PR_SET_NAME, p_arg->name, NULL, NULL, NULL);

		func_ret = p_arg->func(p_arg->arg);
	}while(0);

	free(p_arg);

	return func_ret;
}

int create_pthread(pthread_t *thread_id, void *func, void *arg, size_t stack_size, char *name)
{
	int ret = 0;
	int func_ret = -1;
	pthread_attr_t attr;
	pthread_t id = 0;
	thread_arg_t *p_arg = NULL;
	int min_stack_size = 64 * 1024;//PTHREAD_STACK_MIN;

	if (thread_id == NULL)
	{
		return func_ret;
	}

	ret = pthread_attr_init(&attr);
	if (ret != 0)
	{
		DEBUG_WARN("pthread_attr_init failed(%m)\n");
		return func_ret;
	}

	do
	{
		p_arg = calloc(1, sizeof(thread_arg_t));
		if(p_arg == NULL)
		{
			DEBUG_ERR("calloc failed!!!\n");
			break;
		}

		if (stack_size < min_stack_size)
			stack_size = min_stack_size;
		pthread_attr_setstacksize(&attr, stack_size);

		p_arg->func = func;
		p_arg->arg = arg;
		snprintf(p_arg->name, sizeof(p_arg->name), "%s", name == NULL ? "" : name);
		ret = pthread_create(&id, &attr, thread_run, p_arg);
		if (ret != 0)
		{
			DEBUG_WARN("pthread_create failed(%m)\n");
			func_ret = -2;
			break;
		}

		func_ret = 0;
		*thread_id = id;
	} while (0);

	if(func_ret != 0 && p_arg != NULL)
		free(p_arg);

	pthread_attr_destroy(&attr);

	return func_ret;
}

int delete_pthread(pthread_t *thread_id)
{
	if(thread_id == NULL)
		return -1;

	pthread_join(*thread_id, NULL);

	return 0;
}

unsigned int get_system_time(void)
{
    struct timespec time = {0, 0};

    clock_gettime(CLOCK_MONOTONIC, &time);

    return time.tv_sec * 1000 + time.tv_nsec / 1000000;
}

unsigned int time_from_mark(unsigned int TimeMark)
{
	unsigned int now = get_system_time();
	unsigned int diff = 0;

	if (now < TimeMark)
	{
		diff = 0xffffffff - TimeMark + now;
	}
	else
	{
		diff = now - TimeMark;
	}

	return diff;
}

static int connect_tcp_server(char *ip, int port)
{
	int sockfd = -1;
	struct sockaddr_in address;
	int len = 0;
	struct timeval optval = {3, 0};
	socklen_t optlen = sizeof(optval);

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd <= 0)
	{
		DEBUG_WARN("socket failed, sockfd = %d\n", sockfd);
		return -1;
	}

	address.sin_family = AF_INET;
	inet_aton(ip, &address.sin_addr);
	address.sin_port = htons(port);
	len = sizeof(address);

	optval.tv_sec = 2;
	optval.tv_usec = 0;
	optlen = sizeof(optval);
	setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, &optval, optlen);

	if (connect(sockfd, (struct sockaddr*)&address, len) != 0)
	{
		DEBUG_WARN("connect failed!!!\n");
		close(sockfd);
		return -1;
	}

	return sockfd;
}

static int tcp_send_data(int fd, void *p_data, int size)
{
	int ret = send(fd, p_data, size, MSG_NOSIGNAL);
	if(ret != size)
	{
		DEBUG_WARN("send ret %d, %d, %m\n", ret, size);
		return -1;
	}

	return 0;
}

static int http_connect(char *url)
{
	char *p0 = NULL, *p1 = NULL, *p3= NULL;
	int port = 80;
	char host[256] = {0};
	char req_path[256] = {0};

	if (strncmp(url, "http://", 7) != 0)
	{
		DEBUG_WARN("not http connect\n");
		return -1;
	}

	p0 = url + strlen("http://");

	p1 = strchr(p0, '/');
	if (p1 != NULL)
	{
		memcpy(host, p0, p1 - p0);
		p3 = strchr(host, ':');
		if (p3 != NULL)
		{
			port = atoi(p3 + 1);
			host[p3 - host] = '\0';
		}

		if (*(p1 + 1) != '\0')
		{
			memcpy(req_path, p1 + 1, strlen(p1) - 1);
			req_path[strlen(p1) - 1] = 0;
		}
	}
	else
	{
		memcpy(host, p0, strlen(p0));
		p3 = strchr(host, ':');
		if (p3 != NULL)
		{
			port = atoi(p3 + 1);
			host[p3 - host] = '\0';
		}
	}

	DEBUG("http req %s %d %s\n", host, port, req_path);
	int ret = 0;
	int req_size = 0;
	char req_buf[256];
	int fd = connect_tcp_server(host, port);
	if(fd <= 0)
	{
		DEBUG_WARN("http connect failed\n");
		return -1;
	}

	req_size = snprintf(req_buf, sizeof(req_buf), "GET /%s HTTP/1.1\r\n\r\n", req_path);
	ret = tcp_send_data(fd, req_buf, req_size);
	if(ret != 0)
	{
		DEBUG_WARN("http send failed,%m, %d-%d\n", ret, req_size);
		close(fd);
		return -1;
	}
	DEBUG("http req ok\n");

	return fd;
}

typedef struct RW_OPS_T
{
	int (*size)(struct RW_OPS_T *ops);
	int (*seek)(struct RW_OPS_T *ops, int offset, int whence);
	int (*read)(struct RW_OPS_T *ops, void *ptr, int size, int maxnum);
	int (*write)(struct RW_OPS_T *ops, void *ptr, int size, int maxnum);
	int (*close)(struct RW_OPS_T *ops);
	int type;
	union
	{
		struct
		{
			FILE *fp;
		} stdio;
		struct
		{
			int fd;
		}dcp;
	}hidden;
}RW_OPS_T;

static int rw_ops_file_read(RW_OPS_T *ops, void *ptr, int size, int maxnum)
{
	if(ops == NULL || ops->hidden.stdio.fp == NULL)
		return -1;

	return fread(ptr, size, maxnum, ops->hidden.stdio.fp);
}

static int rw_ops_file_close(RW_OPS_T *ops)
{
	if(ops == NULL)
		return -1;

	if(ops->hidden.stdio.fp != NULL)
	{
		fclose(ops->hidden.stdio.fp);
		ops->hidden.stdio.fp = NULL;
	}

	return 0;
}

void *rw_ops_from_file(char *file_name, char *mode)
{
	RW_OPS_T *ops = NULL;

	ops  = calloc(1, sizeof(RW_OPS_T));
	if(ops == NULL)
		return NULL;

	ops->type = 1;
	ops->hidden.stdio.fp = fopen(file_name, mode);
	if(ops->hidden.stdio.fp == NULL)
	{
		free(ops);
		return NULL;
	}

	ops->read = rw_ops_file_read;
	ops->close = rw_ops_file_close;

	return ops;
}

static int rw_ops_dcp_read(RW_OPS_T *ops, void *ptr, int size, int maxnum)
{
	if(ops == NULL || ops->hidden.dcp.fd <= 0 || size <= 0)
		return -1;

	return read(ops->hidden.dcp.fd, ptr, size * maxnum) / size;
}

static int rw_ops_dcp_close(RW_OPS_T *ops)
{
	if(ops == NULL)
		return -1;

	if(ops->hidden.dcp.fd > 0)
	{
		close(ops->hidden.dcp.fd);
		ops->hidden.dcp.fd = -1;
	}

	return 0;
}

void *rw_ops_from_fifo(char *file_name)
{
	RW_OPS_T *ops = NULL;

	ops  = calloc(1, sizeof(RW_OPS_T));
	if(ops == NULL)
		return NULL;

	ops->type = 2;
	ops->hidden.dcp.fd = open(file_name, O_RDONLY | O_NONBLOCK);
	if(ops->hidden.dcp.fd < 0)
	{
		free(ops);
		return NULL;
	}

	ops->read = rw_ops_dcp_read;
	ops->close = rw_ops_dcp_close;

	return ops;
}

void *rw_ops_from_http(char *url)
{
	RW_OPS_T *ops = NULL;

	ops  = calloc(1, sizeof(RW_OPS_T));
	if(ops == NULL)
		return NULL;

	ops->type = 3;
	ops->hidden.dcp.fd = http_connect(url);
	if(ops->hidden.dcp.fd < 0)
	{
		free(ops);
		return NULL;
	}

	ops->read = rw_ops_dcp_read;
	ops->close = rw_ops_dcp_close;

	return ops;
}

int rw_ops_size(void *context)
{
	RW_OPS_T *ops = context;
	if(ops == NULL)
		return 0;

	if(ops->size)
		return ops->size(ops);

	return 0;
}

int rw_ops_seek(void *context, int offset, int whence)
{
	RW_OPS_T *ops = context;
	if(ops == NULL)
		return -1;

	if(ops->seek)
		return ops->seek(ops, offset, whence);

	return 0;
}

int rw_ops_read(void *context, void *ptr, int size, int maxnum)
{
	RW_OPS_T *ops = context;
	if(ops == NULL)
		return -1;

	if(ops->read)
		return ops->read(ops, ptr, size, maxnum);

	return 0;
}

int rw_ops_write(void *context, void *ptr, int size, int maxnum)
{
	RW_OPS_T *ops = context;
	if(ops == NULL)
		return -1;

	if(ops->write)
		return ops->write(ops, ptr, size, maxnum);

	return 0;
}

int rw_ops_free(void *context)
{
	RW_OPS_T *ops = context;
	if(ops == NULL)
		return -1;

	if(ops->close)
	{
		ops->close(ops);
	}

	free(ops);
	ops = NULL;

	return 0;
}

