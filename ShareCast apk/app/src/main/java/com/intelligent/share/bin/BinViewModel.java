package com.intelligent.share.bin;

import static com.intelligent.share.tool.Utils.isDeviceConnect;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.intelligent.share.R;
import com.intelligent.share.base.ShareApp;
import com.tosmart.dlna.data.repository.DeviceRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import smart.share.CreateSocket;
import smart.share.GlobalConstantValue;
import smart.share.SendSocket;
import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;
import smart.share.dataconvert.model.DataConvertCastPlayModel;
import smart.share.dataconvert.parser.DataParser;
import smart.share.dataconvert.parser.ParserFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author xxx
 * @date 2024/1/18
 */
public class BinViewModel extends ViewModel {
    private static final String TAG = "BinViewModel";
    private MutableLiveData<ArrayList<BinItemModel>> mData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private Subscription mSubscription;
    private DataParser mParser;

    public void clear() {
        mData.setValue(new ArrayList<>());
    }

    public void scan() {
        if (mData.getValue() != null && mData.getValue().size() > 0) {
            return;
        }
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable.create(new Observable.OnSubscribe<List<File>>() {
            @Override
            public void call(Subscriber<? super List<File>> subscriber) {
                mIsLoading.postValue(true);
                List<File> binFiles = scanBinFiles(Environment.getExternalStorageDirectory().getPath() + "/Download");
                subscriber.onNext(binFiles);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<File>>() {
            @Override
            public void call(List<File> files) {
                mIsLoading.postValue(false);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mIsLoading.postValue(false);
                Log.i(TAG, "[xxx] call:" + throwable);
            }
        });
    }

    public List<File> scanBinFiles(String directoryPath) {
        List<File> binFiles = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            scanDirectoryForBinFiles(directory, binFiles);
        }

        return binFiles;
    }

    private void scanDirectoryForBinFiles(File directory, List<File> binFiles) {
        File[] files = directory.listFiles();
        if (files != null) {

            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectoryForBinFiles(file, binFiles);
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
                    binFiles.add(file);
                    updateBinFiles(binFiles);
                }
            }
        }
    }

    private void updateBinFiles(List<File> binFiles) {
        Collections.sort(binFiles, new Comparator<File>() {
            public int compare(File file1, File file2) {
                long lastModified1 = file1.lastModified();
                long lastModified2 = file2.lastModified();
                return Long.compare(lastModified2, lastModified1);
            }
        });
        ArrayList<BinItemModel> list = new ArrayList<>();
        for (File file1 : binFiles) {
            list.add(new BinItemModel(file1));
        }
        mData.postValue(list);
    }

    public MutableLiveData<ArrayList<BinItemModel>> getData() {
        return mData;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public void sendCastPlay(BinItemModel binItemModel) {
        if (!isDeviceConnect()) {
            Toast.makeText(ShareApp.getAppContext(), R.string.str_not_connect_device, Toast.LENGTH_SHORT).show();
            return;
        }
        String url = binItemModel.getFile().getAbsolutePath();
        if (!TextUtils.isEmpty(url) && !url.startsWith("http") && DeviceRepository.obtain().getMediaServer() != null) {
            url = "http://" + DeviceRepository.obtain().getMediaServer().getAddress() + "/" + url;
        }
        DataConvertCastPlayModel model = new DataConvertCastPlayModel(DataConvertCastPlayInfoModel.BIN, DataConvertCastPlayModel.ACTION_PLAY, url, 0);
        sendSocketToDeviceCastPlay(model);
    }

    protected void sendSocketToDeviceCastPlay(DataConvertCastPlayModel model) {
        try {
            List<DataConvertCastPlayModel> models = new ArrayList<>();
            models.add(model);
            if (mParser == null) {
                mParser = ParserFactory.getParser();
            }
            byte[] data_buff = mParser.serialize(models, GlobalConstantValue.S_MSG_CAST_DO_PLAY).getBytes("UTF-8");
            CreateSocket.GetSocket().setSoTimeout(GlobalConstantValue.SOCKET_TCP_TIMEOUT);
            SendSocket.sendSocketToDevice(data_buff, CreateSocket.GetSocket(), 0, data_buff.length, GlobalConstantValue.S_MSG_CAST_DO_PLAY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
