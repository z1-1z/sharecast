/*
 * mutex.h
 */

#ifndef _OS_MUTEX_H_
#define _OS_MUTEX_H_

#ifdef __cplusplus
extern "C"
{
#endif


#define OS_MUTEX_WAIT_FOREVER	0xFFFFFFFF

unsigned int os_mutex_create(void);
int os_mutex_delete(unsigned int handle);
int os_mutex_lock(unsigned int handle, unsigned int milli_seconds);
int os_mutex_unlock(unsigned int handle);


#ifdef __cplusplus
}
#endif

#endif /* _OS_MUTEX_H_ */
