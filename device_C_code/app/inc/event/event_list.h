/*
 * event_list.h
 */

#ifndef _EVENT_LIST_H_
#define _EVENT_LIST_H_

enum
{
    EVT_TSF_IDLE,
    EVT_TSF_CAST_REQUEST_STREAM_PLAY_INFO,      //盒端回传当前播放状态
	EVT_TSF_CAST_DO_PLAY,                       //接收播放链接并准备播放
	EVT_TSF_START_PLAY_TRANS_FILE_URL,          //开始播放文件
	EVT_TSF_START_TRANS_OTHER_FILE_URL,         //开始接收其他文件
};

#endif /* _EVENT_LIST_H_ */
