package com.intelligent.share.video;


import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.intelligent.share.base.ShareApp;
import com.intelligent.share.bean.UrlLinkBean;
import com.intelligent.share.tool.EmptyUtils;
import com.intelligent.share.tool.HistoryWordMgr;
import com.intelligent.share.web.BaseCastViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;


public class UrlLinkViewModel extends BaseCastViewModel {
    private static final String TAG = "UrlLinkViewModel";

    private static List<String> VideoSuffixList = Arrays.asList(
            "mkv",
            "mp4",
            "avi",
            "mpg",
            "mpeg",
            "flv",
            "m3u8",
            "mov",
            "3gp",
            "rmvb",
            "vob",
            "ts"
    );

    private static List<String> AudioSuffixList = Arrays.asList(
            "mp2",
            "mp3",
            "aac",
            "ac3"
    );

    private static List<String> PictureSuffixList = Arrays.asList(
            "png",
            "jpg",
            "jpeg",
            "bmp",
            "gif"
    );


    private UrlLinkBean mTempBean;
    private MutableLiveData<List<UrlLinkBean>> mData = new MutableLiveData<>();
    private LiveData<List<UrlItemModel>> mUrlItems = Transformations.switchMap(mData,
            new Function<List<UrlLinkBean>, LiveData<List<UrlItemModel>>>() {
                @Override
                public LiveData<List<UrlItemModel>> apply(List<UrlLinkBean> input) {
                    MutableLiveData<List<UrlItemModel>> listMutableLiveData = new MutableLiveData<>();
                    List<UrlItemModel> list = new ArrayList<>();
                    for (UrlLinkBean urlLinkBean : input) {
                        list.add(new UrlItemModel(urlLinkBean));
                    }
                    listMutableLiveData.setValue(list);
                    return listMutableLiveData;
                }
            });

    private MutableLiveData<String> mEditTxt = new MutableLiveData<>();

    public UrlLinkViewModel() {
        super();
        mData.setValue(new ArrayList<>());
    }

    public void onItemClick(int position) {
        List<UrlLinkBean> list = mData.getValue();
        UrlLinkBean bean = list.get(position);
        mTempBean = bean;
        int index = 0;
        for (UrlLinkBean urlLinkBean : list) {
            urlLinkBean.setSelect(index == position);
            index++;
        }
        mData.setValue(list);
        sendCastPlay();
    }

    public void addUrlLinkWithString(String contentUrl) {
        List<UrlLinkBean> list = mData.getValue();
        if (EmptyUtils.isEmpty(contentUrl)) {
            return;
        }

        if (!contentUrl.contains(".")) {
            return;
        }
        String url = contentUrl;
        if (!contentUrl.startsWith("http")) {
            url = "http://" + contentUrl;
        }

        String[] items;
        String item;
        if (!url.endsWith(".json") && !url.endsWith(".xml")) {
            items = url.split("\\.");
            item = items[items.length - 1];

            String itemName =
                    url.substring(url.lastIndexOf('/') + 1, url.length() - item.length() - 1);
            UrlLinkBean bean = new UrlLinkBean(itemName, url);
            if (!list.contains(url)) {
                list.add(bean);
                HistoryWordMgr.getInstance(ShareApp.getAppContext()).addHistoryWord(HistoryWordMgr.TYPE_ULL, url);
            }
        }
        mData.setValue(list);
    }

    public void sendCastPlay() {
        Log.i(TAG, "[xxx] sendCastPlay:");
        int mediaType = DataConvertCastPlayInfoModel.VIDEO;
        String[] items = mTempBean.getUrl().split("\\.");
        String suffix = items[items.length - 1];
        if (VideoSuffixList.contains(suffix)) {
            mediaType = DataConvertCastPlayInfoModel.VIDEO;
        } else if (AudioSuffixList.contains(suffix)) {
            mediaType = DataConvertCastPlayInfoModel.AUDIO;
        } else if (PictureSuffixList.contains(suffix)) {
            mediaType = DataConvertCastPlayInfoModel.PICTURE;
        }

        Log.i(TAG, "[xxx] sendCastPlay:" + mTempBean.getUrl());
        sendCastPlay(mTempBean.getUrl()/*"https://cesium.com/public/SandcastleSampleData/big-buck-bunny_trailer.mp4"*/, mediaType);
    }


    public LiveData<List<UrlItemModel>> getUrlItems() {
        return mUrlItems;
    }

    public MutableLiveData<String> getEditTxt() {
        return mEditTxt;
    }
}

