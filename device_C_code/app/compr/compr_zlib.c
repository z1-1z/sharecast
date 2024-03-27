/*
 * compr_zlib.c
 */

#include "trans_file/trans_file_common.h"
#include "zlib.h"
#include "compr/compr_zlib.h"

int compr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size)
{
    return compress((Bytef *)buf_out, (uLongf *)out_size, (Bytef *)buf_in, (uLong)in_size);
}

int uncompr_zlib(U8 *buf_out, U32 *out_size, U8 *buf_in, U32 in_size)
{
    return uncompress((Bytef *)buf_out, (uLongf *)out_size, (Bytef *)buf_in, in_size);
}
