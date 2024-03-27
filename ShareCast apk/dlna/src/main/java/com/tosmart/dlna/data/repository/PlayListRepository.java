package com.tosmart.dlna.data.repository;

import androidx.lifecycle.MutableLiveData;
import android.text.TextUtils;

import com.tosmart.dlna.R;
import com.tosmart.dlna.application.BaseApplication;
import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.greendao.DlnaItemEntityDao;
import com.tosmart.dlna.util.CommonToast;
import com.tosmart.dlna.util.ConfigureUtil;
import com.tosmart.dlna.util.Constant;
import com.tosmart.dlna.util.TaskThreadPoolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @date 2019/4/28
 */
public class PlayListRepository {
    private static final String TAG = "PlayListRepository";
    private static PlayListRepository sInstance = null;
    private boolean mIsFirstInit = false;
    private MutableLiveData<List<DlnaItemEntity>> mImageContentItems = new MutableLiveData<>();
    private MutableLiveData<List<DlnaItemEntity>> mAudioContentItems = new MutableLiveData<>();
    private MutableLiveData<List<DlnaItemEntity>> mVideoContentItems = new MutableLiveData<>();
    private MutableLiveData<List<DlnaItemEntity>> mAllContentItems = new MutableLiveData<>();
    private MutableLiveData<DlnaItemEntity> mCurrentPlayContentItem = new MutableLiveData<>();
    private int mCurrentPosition = 0;

    public static PlayListRepository getInstance() {
        if (sInstance == null) {
            synchronized (PlayListRepository.class) {
                if (sInstance == null) {
                    sInstance = new PlayListRepository();
                }
            }
        }
        return sInstance;
    }

    public void insertPlayContentItems(DlnaItemEntity item) {
        List<DlnaItemEntity> entities = new ArrayList<>();
        entities.add(item);
        insertPlayContentItems(entities, 0, false);
    }

    public void insertPlayContentItems(List<DlnaItemEntity> data, int position, boolean isCover) {
        TaskThreadPoolExecutor.getInstance().execute(() -> {
            DlnaItemEntityDao dlnaItemEntityDao = BaseApplication.getDaoSession().getDlnaItemEntityDao();
            if (isCover) {
                String type = data.get(0).getType();
                List<DlnaItemEntity> dlnaItemEntityDaos = dlnaItemEntityDao.queryBuilder().where(DlnaItemEntityDao.Properties.Type.eq(type)).list();
                dlnaItemEntityDao.deleteInTx(dlnaItemEntityDaos);
            }
            dlnaItemEntityDao.insertOrReplaceInTx(data);
            queryAll(() -> {
                if (isCover) {
                    setCurrentPlayContentItem(data.get(position));
                }
            });
        });
    }

    public void queryPlayContentItems() {
        TaskThreadPoolExecutor.getInstance().execute(() -> queryAll(() -> refreshCurrentPosition(mCurrentPlayContentItem.getValue())));
    }

    private interface QueryAllCallback {
        void queryFinish();
    }

    private synchronized void queryAll(QueryAllCallback queryAllCallback) {
        List<DlnaItemEntity> imagelist = new ArrayList<>();
        List<DlnaItemEntity> audiolist = new ArrayList<>();
        List<DlnaItemEntity> videolist = new ArrayList<>();
        List<DlnaItemEntity> alllist = BaseApplication.getDaoSession().getDlnaItemEntityDao().queryBuilder().build().list();
        if (alllist != null && alllist.size() > 0) {
            String path = alllist.get(0).getPath();
            int start = path.indexOf("//") + 2;
            int end = path.indexOf(":", path.indexOf(":") + 1);
            String ip = path.substring(start, end);
            if (!ip.equals(BaseApplication.getHostAddress())) {
                for (DlnaItemEntity entity : alllist) {
                    if (!TextUtils.isEmpty(entity.getPath()) && BaseApplication.getHostAddress() != null) {
                        entity.setPath(entity.getPath().replace(ip, BaseApplication.getHostAddress()));
                    }
                }
            }

            for (DlnaItemEntity dlnaItemEntity : alllist) {
                switch (dlnaItemEntity.getType()) {
                    case Constant.IMAGE_TYPE:
                        imagelist.add(dlnaItemEntity);
                        break;
                    case Constant.AUDIO_TYPE:
                        audiolist.add(dlnaItemEntity);
                        break;
                    case Constant.VIDEO_TYPE:
                        videolist.add(dlnaItemEntity);
                        break;
                    default:
                        break;
                }
            }
        }

        TaskThreadPoolExecutor.getInstance().executeInMainThread(() -> {
            mImageContentItems.setValue(imagelist);
            mVideoContentItems.setValue(videolist);
            mAudioContentItems.setValue(audiolist);
            mAllContentItems.setValue(alllist);
            if (!mIsFirstInit) {
                fetchLastPlayItem(imagelist);
                fetchLastPlayItem(videolist);
                fetchLastPlayItem(audiolist);
                mIsFirstInit = true;
            }
            if (queryAllCallback != null) {
                queryAllCallback.queryFinish();
            }
        });
    }

    private void fetchLastPlayItem(List<DlnaItemEntity> dlnaItemEntities) {
        if (dlnaItemEntities != null && dlnaItemEntities.size() > 0) {
            String path = ConfigureUtil.getLastPlayPath(BaseApplication.getContext());
            if (!TextUtils.isEmpty(path)) {
                for (DlnaItemEntity dlnaItemEntity : dlnaItemEntities) {
                    if (dlnaItemEntity.getPath().equalsIgnoreCase(path)) {
                        setCurrentPlayContentItem(dlnaItemEntity);
                        return;
                    }
                }
            }
        }
    }

    public void deletePlayContentItems(List<DlnaItemEntity> entities) {
        TaskThreadPoolExecutor.getInstance().execute(() -> {
            BaseApplication.getDaoSession().getDlnaItemEntityDao().deleteInTx(entities);
            queryAll(() -> refreshCurrentPosition(mCurrentPlayContentItem.getValue()));
        });
    }

    public void deletePlayContentItem(DlnaItemEntity entity) {
        List<DlnaItemEntity> entities = new ArrayList<>();
        entities.add(entity);
        deletePlayContentItems(entities);
    }

    public MutableLiveData<List<DlnaItemEntity>> getTargetCurrentPlayContentList() {
        return getTargetCurrentPlayContentList(mCurrentPlayContentItem.getValue());
    }

    public MutableLiveData<List<DlnaItemEntity>> getTargetCurrentPlayContentList(DlnaItemEntity curContentItem) {
        if (curContentItem != null) {
            switch (curContentItem.getType()) {
                case Constant.IMAGE_TYPE:
                    return getImageContentItems();
                case Constant.AUDIO_TYPE:
                    return getAudioContentItems();
                case Constant.VIDEO_TYPE:
                default:
                    return getVideoContentItems();
            }
        }
        return null;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public MutableLiveData<DlnaItemEntity> getCurrentPlayContentItem() {
        return mCurrentPlayContentItem;
    }

    public void setCurrentPlayContentItem(DlnaItemEntity currentPlayContentItem) {
        refreshCurrentPosition(currentPlayContentItem);
        mCurrentPlayContentItem.postValue(currentPlayContentItem);
    }

    public void deleteAllPlayContentItems() {
        TaskThreadPoolExecutor.getInstance().execute(() -> BaseApplication.getDaoSession().getDlnaItemEntityDao().deleteAll());
    }

    private synchronized void refreshCurrentPosition(DlnaItemEntity curDlnaItemEntity) {
        if (curDlnaItemEntity != null) {
            if (getTargetCurrentPlayContentList(curDlnaItemEntity) != null) {
                List<DlnaItemEntity> dlnaItemEntityList = getTargetCurrentPlayContentList(curDlnaItemEntity).getValue();
                if (dlnaItemEntityList != null && dlnaItemEntityList.size() > 0 && curDlnaItemEntity != null) {
                    int position = 0;
                    for (DlnaItemEntity dlnaItemEntity : dlnaItemEntityList) {
                        if (dlnaItemEntity.getPath().equalsIgnoreCase(curDlnaItemEntity.getPath())) {
                            mCurrentPosition = position;
                            break;
                        }
                        position++;
                    }
                }
            }
        }
    }

    public boolean fetchNext() {
        boolean result = true;
        if (getTargetCurrentPlayContentList() != null) {
            List<DlnaItemEntity> dlnaItemEntities = getTargetCurrentPlayContentList().getValue();

            ConfigureUtil.RepeatMode repeatMode = ConfigureUtil.getRepeatMode(BaseApplication.getContext());
            refreshCurrentPosition(mCurrentPlayContentItem.getValue());
            switch (repeatMode) {
                case repeat_trace:
                    break;
                case repeat_play_list:
                case repeat_off:
                    if (dlnaItemEntities != null && dlnaItemEntities.size() > 0) {
                        ConfigureUtil.PlayMode playMode = ConfigureUtil.getPlayMode(BaseApplication.getContext());
                        if (playMode == ConfigureUtil.PlayMode.shuffle_off) {
                            if (mCurrentPosition >= dlnaItemEntities.size() - 1) {
                                if (repeatMode == ConfigureUtil.RepeatMode.repeat_play_list) {
                                    mCurrentPosition = 0;
                                } else {
                                    result = false;
                                }
                            } else {
                                mCurrentPosition = mCurrentPosition + 1;
                            }
                        } else {
                            int randomPosition = new Random().nextInt(dlnaItemEntities.size());
                            while (randomPosition == mCurrentPosition) {
                                randomPosition = new Random().nextInt(dlnaItemEntities.size());
                            }
                            mCurrentPosition = randomPosition;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (!result) {
//                CommonToast.obtain().show(R.string.info_last_content_item);
            } else {
                if (dlnaItemEntities != null && dlnaItemEntities.size() > 0) {
                    mCurrentPlayContentItem.setValue(dlnaItemEntities.get(mCurrentPosition));
                }
            }

        } else {
            result = false;
        }
        return result;
    }

    public boolean fetchPrevious() {
        boolean result = true;
        if (getTargetCurrentPlayContentList() != null) {
            List<DlnaItemEntity> dlnaItemEntities = getTargetCurrentPlayContentList().getValue();
            ConfigureUtil.RepeatMode repeatMode = ConfigureUtil.getRepeatMode(BaseApplication.getContext());
            refreshCurrentPosition(mCurrentPlayContentItem.getValue());
            switch (repeatMode) {
                case repeat_trace:
                    break;
                case repeat_play_list:
                case repeat_off:
                    if (dlnaItemEntities != null && dlnaItemEntities.size() > 0) {
                        ConfigureUtil.PlayMode playMode = ConfigureUtil.getPlayMode(BaseApplication.getContext());
                        if (playMode == ConfigureUtil.PlayMode.shuffle_off) {
                            if (mCurrentPosition <= 0) {
                                if (repeatMode == ConfigureUtil.RepeatMode.repeat_play_list) {
                                    mCurrentPosition = dlnaItemEntities.size() - 1;
                                } else {
                                    result = false;
                                }
                            } else {
                                mCurrentPosition = mCurrentPosition - 1;
                            }
                        } else {
                            int randomPosition = new Random().nextInt(dlnaItemEntities.size());
                            while (randomPosition == mCurrentPosition) {
                                randomPosition = new Random().nextInt(dlnaItemEntities.size());
                            }
                            mCurrentPosition = randomPosition;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (!result) {
//                CommonToast.obtain().show(R.string.info_first_content_item);
            } else {
                if (dlnaItemEntities != null && dlnaItemEntities.size() > 0) {
                    mCurrentPlayContentItem.setValue(dlnaItemEntities.get(mCurrentPosition));
                }
            }
        } else {
            result = false;
        }
        return result;
    }

    public MutableLiveData<List<DlnaItemEntity>> getImageContentItems() {
        return mImageContentItems;
    }

    public MutableLiveData<List<DlnaItemEntity>> getAudioContentItems() {
        return mAudioContentItems;
    }

    public MutableLiveData<List<DlnaItemEntity>> getVideoContentItems() {
        return mVideoContentItems;
    }

    public MutableLiveData<List<DlnaItemEntity>> getAllContentItems() {
        return mAllContentItems;
    }
}