/*
 * trans_file_common.h
 */

#ifndef _TRANS_FILE_TRANS_FILE_COMMON_H_
#define _TRANS_FILE_TRANS_FILE_COMMON_H_

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

#ifndef U8
typedef unsigned char       U8;
#endif

#ifndef U32
typedef unsigned long       U32;
#endif

#define MAX_SIZE_OF_GENERAL_BUFFER  (4096)

#ifndef TSF_MIN
#define TSF_MIN(x, y)   				((x) < (y) ? (x) : (y))
#endif

#ifndef TSF_MAX
#define TSF_MAX(x, y)   				((x) > (y) ? (x) : (y))
#endif

#ifndef TSF_ARRAY_SIZE
#define TSF_ARRAY_SIZE(x)   			(sizeof(x)/sizeof(x[0]))
#endif

#endif /* _TRANS_FILE_TRANS_FILE_COMMON_H_ */
