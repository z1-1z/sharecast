package com.intelligent.share.web;

import static com.intelligent.share.tool.Utils.isDeviceConnect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.R;
import com.intelligent.share.tool.EmptyUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import smart.share.CreateSocket;
import smart.share.GlobalConstantValue;
import smart.share.SendSocket;
import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;
import smart.share.dataconvert.model.DataConvertCastPlayModel;
import smart.share.dataconvert.parser.ParserFactory;
import smart.share.message.process.MessageProcessor;

public class BaseCastViewModel extends ViewModel  {


    public static final int GET_PLAY_INFO_MESSAGE = 1;
    public static final int MAX_PROGRESS = 100;
    public static final int GET_PLAY_INFO_TIME = 1000;
    private static final String TAG = "BaseCastViewModel";
    public static final String DEFAULT_TIME = "00:00";

    private MessageProcessor mMsgProc;
    private Timer mTimer = null;
    private TimerTask mTimerTask;
    private boolean mIsTimerRunning = false;
    private int mTotalTime;
    private int mCastMediaType = -1;
    private String mCastUrl;

    protected MutableLiveData<String> mType = new MutableLiveData<>();
    private MutableLiveData<Boolean> mPlayStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> mStartTimeLiveData = new MutableLiveData<>(DEFAULT_TIME);
    private MutableLiveData<String> mEndTimeLiveData = new MutableLiveData<>(DEFAULT_TIME);
    private MutableLiveData<Integer> mProgressLiveData = new MutableLiveData<>();
    protected MutableLiveData<Boolean> mIsShowSeekBar = new MutableLiveData<>(true);

    private MutableLiveData<Boolean> mMiracastPicEnable = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> mMiracastMediaEnable = new MutableLiveData<>(true);
    protected MutableLiveData<Boolean> mIsEnterMiracastModel = new MutableLiveData<>(false);
    private Activity mActivity;

    public BaseCastViewModel(){
    }

    public MutableLiveData<Boolean> getPlayStatusLiveData() {
        return mPlayStatusLiveData;
    }

    public MutableLiveData<String> getStartTimeLiveData() {
        return mStartTimeLiveData;
    }

    public MutableLiveData<String> getEndTimeLiveData() {
        return mEndTimeLiveData;
    }

    public MutableLiveData<Integer> getProgressLiveData() {
        return mProgressLiveData;
    }

    public MutableLiveData<Boolean> getMiracastPicEnable() {
        return mMiracastPicEnable;
    }

    public MutableLiveData<Boolean> getMiracastMediaEnable() {
        return mMiracastMediaEnable;
    }


    public MutableLiveData<Boolean> getIsShowSeekBar() {
        return mIsShowSeekBar;
    }

    public void init(Activity activity) {
        mActivity = activity;
        setMessageProcess();
    }

    public void onDestroy() {
        mMsgProc.recycle();
        mMsgProc.removeProcessCallback(null);
        stopTimer();
    }

    private void setMessageProcess() {
        mMsgProc = MessageProcessor.obtain();
        mMsgProc.recycle();
        mMsgProc.setOnMessageProcess(GlobalConstantValue.S_MSG_CAST_DO_PLAY, mActivity, (MessageProcessor.PerformOnBackground) msg -> {
            Log.d(TAG, "[xxx]" + "setMessageProcess() msg " + msg.arg1);
            if (msg.arg1 >= 0) {
                startTimer();
            }
        });

        mMsgProc.setOnMessageProcess(GlobalConstantValue.S_MSG_CAST_REQUEST_STREAM_PLAY_INFO, mActivity, (MessageProcessor.PerformOnBackground) msg -> {

            if (msg.arg1 > 0) {
                Bundle data = msg.getData();
                byte[] recvData = data.getByteArray("ReceivedData");

                try {
                    InputStream istream = new ByteArrayInputStream(recvData, 0, recvData.length);
                    DataConvertCastPlayInfoModel infoModel = ((List<DataConvertCastPlayInfoModel>) ParserFactory.getParser().parse(istream, GlobalConstantValue.CAST_PLAY_INFO)).get(0);
                    mCastMediaType = infoModel.getMediaType();
                    mCastUrl = infoModel.getUrl();
                    boolean isReset = false;
                    switch (infoModel.getStateCode()) {
                        case DataConvertCastPlayInfoModel.STATE_CODE_PLAYING:
                            setVideoSeekBarPlayIcon();
                            break;
                        case DataConvertCastPlayInfoModel.STATE_CODE_PAUSE:
                        case DataConvertCastPlayInfoModel.STATE_CODE_STOP:
                            setVideoSeekBarStopIcon();
                            break;

                    }

                    switch (infoModel.getMediaType()) {
                        case DataConvertCastPlayInfoModel.PICTURE:
                            isReset = true;
                            break;
                        case DataConvertCastPlayInfoModel.VIDEO:
                        case DataConvertCastPlayInfoModel.AUDIO: {
                            if (infoModel.getTotalTime() <= 0) {
                                return;
                            }
                            mTotalTime = infoModel.getTotalTime();
                            String endTime = getTimeStr(infoModel.getTotalTime());
                            String startTime = getTimeStr(infoModel.getCurrentTime());

                            mStartTimeLiveData.postValue(startTime);
                            mEndTimeLiveData.postValue(endTime);

                            int currentValue = 0;

                            if (infoModel.getTotalTime() > 0) {
                                currentValue = infoModel.getCurrentTime() * MAX_PROGRESS / infoModel.getTotalTime();
                            }
                            mProgressLiveData.postValue(currentValue);
                        }
                        break;
                        case DataConvertCastPlayInfoModel.NONE:
                            resetVideoSeekBarTime();
                            break;
                        default:
                            isReset = true;
                            break;
                    }

                    if (isReset && mIsTimerRunning) {
                        stopTimer();
                        resetVideoSeekBarTime();
                    }

                } catch (Exception e) {
                    Log.i(TAG, "setMessageProcess: Exception = " + e.getMessage());

                }
            }
        });
    }

    private String getTimeStr(int time) {
        int minutes = ((time / 1000 - time / 1000 % 60) / 60);
        int seconds = (time / 1000 % 60);
        String timestr = String.format("%02d : %02d", minutes, seconds);
        return timestr;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PLAY_INFO_MESSAGE:
                    SendSocket.sendOnlyCommandSocketToDevice(CreateSocket.GetSocket(), GlobalConstantValue.S_MSG_CAST_REQUEST_STREAM_PLAY_INFO);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    private void startTimer() {

        if (mIsTimerRunning) {
            return;
        }

        mIsTimerRunning = true;

        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = GET_PLAY_INFO_MESSAGE;
                    handler.sendMessage(message);
                }
            };
        }

        if (mTimer != null && mTimerTask != null) {
            mTimer.schedule(mTimerTask, GET_PLAY_INFO_TIME, GET_PLAY_INFO_TIME);
        }


    }

    private void stopTimer() {
        if (!mIsTimerRunning) {
            return;
        }
        mIsTimerRunning = false;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    protected void resetVideoSeekBarTime() {
        mTotalTime = 0;
        mStartTimeLiveData.postValue("00:00");
        mEndTimeLiveData.postValue("00:00");
        mProgressLiveData.postValue(0);

        setVideoSeekBarStopIcon();

    }

    private void setVideoSeekBarPlayIcon() {
        mPlayStatusLiveData.postValue(true);
    }

    private void setVideoSeekBarStopIcon() {
        mPlayStatusLiveData.postValue(false);
    }


    protected void sendSocketToDeviceCastPlay(DataConvertCastPlayModel model) {
        try {
            List<DataConvertCastPlayModel> models = new ArrayList<>();
            models.add(model);
            byte[] data_buff = ParserFactory.getParser().serialize(models, GlobalConstantValue.S_MSG_CAST_DO_PLAY).getBytes("UTF-8");
            CreateSocket.GetSocket().setSoTimeout(GlobalConstantValue.SOCKET_TCP_TIMEOUT);
            SendSocket.sendSocketToDevice(data_buff, CreateSocket.GetSocket(), 0, data_buff.length, GlobalConstantValue.S_MSG_CAST_DO_PLAY);

        } catch (Exception e) {
            Log.i(TAG, "[xxx] sendSocketToDeviceCastPlay: e " + e);
            e.printStackTrace();
        }
    }

    public void videoSeekBarPlay() {
        if (mCastMediaType == DataConvertCastPlayInfoModel.VIDEO
        || mCastMediaType == DataConvertCastPlayInfoModel.AUDIO) {
            int actionCode = DataConvertCastPlayModel.ACTION_RESUME;
            if (mPlayStatusLiveData.getValue()) {
                actionCode = DataConvertCastPlayModel.ACTION_PAUSE;
            }
            DataConvertCastPlayModel model = new DataConvertCastPlayModel(mCastMediaType, actionCode, mCastUrl, 0);
            sendSocketToDeviceCastPlay(model);
        }

    }

    public void setSeekTime(int progress) {

        if (mTotalTime > 0 && (mCastMediaType == DataConvertCastPlayInfoModel.VIDEO
                || mCastMediaType == DataConvertCastPlayInfoModel.AUDIO)) {
            int seekTime = progress * mTotalTime / MAX_PROGRESS;
            DataConvertCastPlayModel model = new DataConvertCastPlayModel(mCastMediaType, DataConvertCastPlayModel.ACTION_SEEK_TIME, mCastUrl, seekTime);
            sendSocketToDeviceCastPlay(model);
        }
    }

    public void sendCastPlay(String url, int mediaType) {
        if (!isDeviceConnect()) {
            Toast.makeText(mActivity, R.string.str_not_connect_device, Toast.LENGTH_SHORT).show();
            return;
        }
        if (EmptyUtils.isEmpty(url)) {
            return;
        }
        mIsEnterMiracastModel.setValue(true);
        DataConvertCastPlayModel model = new DataConvertCastPlayModel(mediaType, DataConvertCastPlayModel.ACTION_PLAY, url, 0);
        sendSocketToDeviceCastPlay(model);
        resetVideoSeekBarTime();
    }

    public void onResume() {
        startTimer();
    }

    public void onPause() {
        stopTimer();
    }

    public MutableLiveData<String> getType() {
        return mType;
    }

    public MutableLiveData<Boolean> getIsEnterMiracastModel() {
        return mIsEnterMiracastModel;
    }
}
