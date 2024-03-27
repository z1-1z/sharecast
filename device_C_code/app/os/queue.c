/*
 * queue.cpp
 */

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <time.h>
#include <unistd.h>
#include <os/queue.h>
#include <stdint.h>
#include <pthread.h>

typedef uintptr_t msg_type;
typedef struct
{
	int max_count;
	int msg_size;
	pthread_mutex_t mutex;
	pthread_cond_t cond;
	void *buf;
	int buf_size;
	int read_ptr;
	int write_ptr;
	int used_size;
}msg_handle_t;

msg_type create_msg(int elem_num, int elem_size)
{
	msg_handle_t *p_handle = NULL;
	pthread_condattr_t condattr;

	p_handle = calloc(1, sizeof(msg_handle_t));
	if(p_handle == NULL)
	{
		return 0;
	}

	p_handle->max_count = elem_num;
	p_handle->msg_size = elem_size;
	p_handle->buf_size = elem_num * elem_size;
	p_handle->buf = calloc(elem_num, elem_size);
	if(NULL == p_handle->buf)
	{
		free(p_handle);
		return 0;
	}

	pthread_mutex_init(&p_handle->mutex, NULL);

	pthread_condattr_init(&condattr);
	pthread_condattr_setclock(&condattr, CLOCK_MONOTONIC);
	pthread_cond_init(&p_handle->cond, &condattr);
	pthread_condattr_destroy(&condattr);

	return (msg_type)p_handle;
}

void delete_msg(msg_type handle)
{
	msg_handle_t *p_handle = (msg_handle_t *)handle;

	if(p_handle == NULL)
	{
		return;
	}


	pthread_cond_destroy(&p_handle->cond);
	pthread_mutex_destroy(&p_handle->mutex);
	free(p_handle->buf);
	free(p_handle);
}

static void init_abs_time(struct timespec *p_abs_time, int time_ms)
{
	struct timespec now;
	struct timespec abstime;

	clock_gettime(CLOCK_MONOTONIC, &now);

	abstime.tv_sec = now.tv_sec + time_ms / 1000 + ((time_ms % 1000) * 1000000 + now.tv_nsec) / 1000000000;
	abstime.tv_nsec = ((time_ms % 1000) * 1000000 + now.tv_nsec) % 1000000000;

	*p_abs_time = abstime;
}

int send_msg(msg_type handle, void *msg, int time_ms)
{
	msg_handle_t *p_handle = (msg_handle_t *)handle;
	int func_ret = -1;
	struct timespec abstime;

	if(p_handle == NULL)
	{
		return func_ret;
	}

	pthread_mutex_lock(&p_handle->mutex);
	if(p_handle->used_size >= p_handle->buf_size)
	{
		init_abs_time(&abstime, time_ms);
		pthread_cond_timedwait(&p_handle->cond, &p_handle->mutex, &abstime);
	}

	if(p_handle->used_size < p_handle->buf_size)
	{
		memcpy(p_handle->buf + p_handle->write_ptr, msg, p_handle->msg_size);
		p_handle->write_ptr += p_handle->msg_size;
		if(p_handle->write_ptr >= p_handle->buf_size)
		{
			p_handle->write_ptr = 0;
		}
		p_handle->used_size += p_handle->msg_size;
		func_ret = 0;
		pthread_cond_signal(&p_handle->cond);
	}
	else
	{
		printf("\033[32m[%s:%d]\033[31m msg queue full!!!!!!!!!!!!\033[0m\n", __FUNCTION__, __LINE__);
	}
	pthread_mutex_unlock(&p_handle->mutex);

	return func_ret;
}

int recv_msg(msg_type handle, void *msg, int time_ms)
{
	msg_handle_t *p_handle = (msg_handle_t *)handle;
	int func_ret = -1;
	struct timespec abstime;

	if(p_handle == NULL)
	{
		return func_ret;
	}

	pthread_mutex_lock(&p_handle->mutex);
	if(p_handle->used_size <= 0)
	{
		init_abs_time(&abstime, time_ms);
		pthread_cond_timedwait(&p_handle->cond, &p_handle->mutex, &abstime);
	}

	if(p_handle->used_size > 0)
	{
		memcpy(msg, p_handle->buf + p_handle->read_ptr, p_handle->msg_size);
		p_handle->read_ptr += p_handle->msg_size;
		if(p_handle->read_ptr >= p_handle->buf_size)
		{
			p_handle->read_ptr = 0;
		}
		p_handle->used_size -= p_handle->msg_size;
		func_ret = 0;
		pthread_cond_signal(&p_handle->cond);
	}
	pthread_mutex_unlock(&p_handle->mutex);

	return func_ret;
}

int os_msgqueue_create(OS_Queue_t *queue_t)
{
	if(queue_t == NULL || queue_t->element_size <= 0 || queue_t->buf_size <= 0)
		return 0;

	return create_msg(queue_t->buf_size / queue_t->element_size, queue_t->element_size);
}

int os_msgqueue_delete(int handle)
{
	delete_msg(handle);
	return 0;
}

int os_msgqueue_send(int handle, void *msg, unsigned int msg_size, unsigned int timeout)
{
	return send_msg(handle, msg, timeout);
}

int os_msgqueue_receive(int handle, void *msg, unsigned int msg_size, unsigned int timeout)
{
	return recv_msg(handle, msg, timeout);
}
