package com.intelligent.share.video;

import android.content.res.TypedArray;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.R;
import com.intelligent.share.base.ShareApp;
import com.intelligent.share.bean.WebsiteItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class AppViewModel extends ViewModel {
    private static final String TAG = "DeviceViewModel";
    private MutableLiveData<List<WebsiteItem.WebInfoItem>> mWebList = new MutableLiveData<>();
    private LiveData<List<AppItemViewModel>> mAppItemList = Transformations.switchMap(mWebList, new Function<List<WebsiteItem.WebInfoItem>,
            LiveData<List<AppItemViewModel>>>() {
        @Override
        public LiveData<List<AppItemViewModel>> apply(List<WebsiteItem.WebInfoItem> input) {
            MutableLiveData<List<AppItemViewModel>> appItemList = new MutableLiveData<>();
            List<AppItemViewModel> list = new ArrayList<>();
            for (WebsiteItem.WebInfoItem webInfoItem : input) {
                list.add(new AppItemViewModel(webInfoItem));
            }
            appItemList.setValue(list);
            return appItemList;
        }
    });

    public AppViewModel() {
        mWebList.setValue(getInternetInfo(R.array.movie_list));
    }

    private List<WebsiteItem.WebInfoItem> getInternetInfo(int arrayId) {
        int[] itemContentInfo = getResArray(arrayId);
        List<WebsiteItem.WebInfoItem> infoList = new ArrayList<>();
        for (int strContent : itemContentInfo) {
            int[] idArray = getResArray(strContent);
            String[] infoArray = ShareApp.getAppContext().getResources().getStringArray(strContent);
            WebsiteItem.WebInfoItem infoItem = new WebsiteItem.WebInfoItem(idArray[0], infoArray[1], infoArray[2]);
            infoList.add(infoItem);
        }
        return infoList ;
    }

    private int[] getResArray(int typedArrayId) {
        TypedArray typedArray = ShareApp.getAppContext().getResources().obtainTypedArray(typedArrayId);
        int[] resourceArray = new int[typedArray.length()];
        for (int i = 0; i < typedArray.length(); i++) {
            resourceArray[i] = typedArray.getResourceId(i, 0);
        }
        typedArray.recycle();
        return resourceArray;
    }

    public MutableLiveData<List<WebsiteItem.WebInfoItem>> getWebList() {
        return mWebList;
    }

    public LiveData<List<AppItemViewModel>> getAppItemList() {
        return mAppItemList;
    }
}
