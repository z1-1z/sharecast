/*
 * queue.h
 */

#ifndef _OS_QUEUE_H_
#define _OS_QUEUE_H_

#ifdef __cplusplus
extern "C"
{
#endif


typedef struct _os_queue_t_
{
	int buf_size;
	int element_size;
} OS_Queue_t;


int os_msgqueue_create(OS_Queue_t *queue_t);
int os_msgqueue_delete(int handle);
int os_msgqueue_send(int handle, void *msg, unsigned int msg_size, unsigned int timeout);
int os_msgqueue_receive(int handle, void *msg, unsigned int msg_size, unsigned int timeout);


#ifdef __cplusplus
}
#endif

#endif /* _OS_QUEUE_H_ */
