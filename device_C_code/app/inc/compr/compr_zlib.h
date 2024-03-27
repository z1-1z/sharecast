/*
 * compr_zlib.h
 */

#ifndef APP_COMPR_ZLIB_H_
#define APP_COMPR_ZLIB_H_


int compr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size);
int uncompr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size);


#endif /* APP_COMPR_ZLIB_H_ */
