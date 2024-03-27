package com.intelligent.share.bin;

import android.text.format.DateFormat;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.tool.Utils;

import java.io.File;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class BinItemModel extends ViewModel {
    private static final String TAG = "DeviceItemModel";
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private MutableLiveData<String> mTime = new MutableLiveData<>();
    private MutableLiveData<String> mSize = new MutableLiveData<>();
    private File mFile;

    public BinItemModel(File file) {
        mFile = file;
        mName.postValue(file.getName());
        long timestamp = file.lastModified();
        mTime.postValue(DateFormat.format("yyyy-MM-dd", timestamp).toString());
        mSize.postValue(Utils.getNetFileSizeDescription(file.length()));
    }


    public MutableLiveData<String> getName() {
        return mName;
    }

    public MutableLiveData<String> getTime() {
        return mTime;
    }

    public MutableLiveData<String> getSize() {
        return mSize;
    }

    public File getFile() {
        return mFile;
    }
}
