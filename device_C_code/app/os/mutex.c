/*
 * mutex.cpp
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

unsigned int os_mutex_create(void)
{
	pthread_mutex_t *mutex = malloc(sizeof(pthread_mutex_t));

	if(mutex == NULL)
		return 0;

	pthread_mutex_init(mutex, NULL);

	return (unsigned int)mutex;
}

int os_mutex_delete(unsigned int handle)
{
	pthread_mutex_t *mutex = (pthread_mutex_t *)handle;

	if(mutex == NULL)
		return -1;

	pthread_mutex_destroy(mutex);
	free(mutex);

	return 0;
}

int os_mutex_lock(unsigned int handle, unsigned int milli_seconds)
{
	pthread_mutex_t *mutex = (pthread_mutex_t *)handle;

	if(mutex == NULL)
		return -1;

	pthread_mutex_lock(mutex);

	return 0;
}

int os_mutex_unlock(unsigned int handle)
{
	pthread_mutex_t *mutex = (pthread_mutex_t *)handle;

	if(mutex == NULL)
		return -1;

	pthread_mutex_unlock(mutex);

	return 0;
}
