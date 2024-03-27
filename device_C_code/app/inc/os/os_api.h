#ifndef OS_API_H_
#define OS_API_H_

#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <os/queue.h>
#include <os/mutex.h>

#include <netdb.h>
#include <net/if.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <fcntl.h>
#include <sys/ioctl.h>

int create_pthread(pthread_t *thread_id, void *func, void *arg, size_t stack_size, char *name);
int delete_pthread(pthread_t *thread_id);

unsigned int get_system_time(void);
unsigned int time_from_mark(unsigned int TimeMark);

void *rw_ops_from_file(char *file_name, char *mode);
void *rw_ops_from_fifo(char *file_name);
void *rw_ops_from_http(char *url);

int rw_ops_free(void *context);

int rw_ops_size(void *context);
int rw_ops_seek(void *context, int offset, int whence);
int rw_ops_read(void *context, void *ptr, int size, int maxnum);
int rw_ops_write(void *context, void *ptr, int size, int maxnum);

#endif /* OS_API_H_ */
