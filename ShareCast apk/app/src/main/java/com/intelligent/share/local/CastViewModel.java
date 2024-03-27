package com.intelligent.share.local;


import static com.intelligent.share.tool.Utils.isDeviceConnect;
import static com.tosmart.dlna.util.Constant.AUDIO_TYPE;
import static com.tosmart.dlna.util.Constant.IMAGE_TYPE;
import static com.tosmart.dlna.util.Constant.VIDEO_TYPE;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.intelligent.share.R;
import com.intelligent.share.base.ShareApp;
import com.intelligent.share.web.BaseCastViewModel;
import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.data.repository.DeviceRepository;

import java.util.List;

import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;


public class CastViewModel extends BaseCastViewModel {
    private static final String TAG = "UrlLinkViewModel";
    private MutableLiveData<String> mTitle = new MutableLiveData<>();
    private MutableLiveData<List<LocalMediaItemModel>> mData = new MutableLiveData<>();
    private int mCurPosition = 0;


    public MutableLiveData<List<LocalMediaItemModel>> getData() {
        return mData;
    }

    public void setData(List<LocalMediaItemModel> data) {
        if (data.size() > 0) {
            mType.setValue(data.get(0).getDlnaItemEntity().getType());
            switch (data.get(0).getDlnaItemEntity().getType()) {
                case  IMAGE_TYPE:
                    mTitle.setValue(ShareApp.getAppContext().getString(R.string.str_image));
                    mIsShowSeekBar.setValue(false);
                    break;
                case  VIDEO_TYPE:
                    mTitle.setValue(ShareApp.getAppContext().getString(R.string.str_video));
                    mIsShowSeekBar.setValue(true);
                    break;
                case  AUDIO_TYPE:
                    mTitle.setValue(ShareApp.getAppContext().getString(R.string.str_music));
                    mIsShowSeekBar.setValue(true);
                    break;
            }
        }
        mData.setValue(data);
    }

    public void setCurPosition(int curPosition) {
        mCurPosition = curPosition;
    }

    public void sendCastPlay(DlnaItemEntity entity){
        if (!isDeviceConnect()) {
            Toast.makeText(ShareApp.getAppContext(), R.string.str_not_connect_device, Toast.LENGTH_SHORT).show();
            return;
        }
        int mediaType = DataConvertCastPlayInfoModel.PICTURE;
        if (entity.getType().equals(IMAGE_TYPE)) {
            mediaType = DataConvertCastPlayInfoModel.PICTURE;
        } else if (entity.getType().equals(VIDEO_TYPE)) {
            mediaType = DataConvertCastPlayInfoModel.VIDEO;
        } else if (entity.getType().equals(AUDIO_TYPE)) {
            mediaType = DataConvertCastPlayInfoModel.AUDIO;
        }
        String url = entity.getImage();
        Log.i(TAG, "[xxx] onClick: entity.getPath() " + entity.getPath());
        Log.i(TAG, "[xxx] onClick: entity.getImage() " + entity.getImage());
        if (TextUtils.isEmpty(url)) {
            url = entity.getPath();
        }
        if (!TextUtils.isEmpty(url) && !url.startsWith("http") && DeviceRepository.obtain().getMediaServer() != null) {
            url = "http://" + DeviceRepository.obtain().getMediaServer().getAddress() + "/" + url;
        }
//                url = "https://cesium.com/public/SandcastleSampleData/big-buck-bunny_trailer.mp4";
        Log.i(TAG, "[xxx] onClick: url " + url);

        Log.i(TAG, "[xxx] onClick: mediaType " + mediaType);
        sendCastPlay(url, mediaType);
    }



    public int getCurPosition() {
        return mCurPosition;
    }

    public MutableLiveData<String> getTitle() {
        return mTitle;
    }
}

