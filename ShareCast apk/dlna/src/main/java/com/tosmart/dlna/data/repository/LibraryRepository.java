package com.tosmart.dlna.data.repository;

import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.tosmart.dlna.application.BaseApplication;
import com.tosmart.dlna.data.local.DlnaItemEntity;
import com.tosmart.dlna.dms.ContentNode;
import com.tosmart.dlna.dms.ContentTree;
import com.tosmart.dlna.dms.MediaServer;
import com.tosmart.dlna.util.FileUtil;
import com.tosmart.dlna.util.ImageUtil;
import com.tosmart.dlna.util.TaskThreadPoolExecutor;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date 2019/4/25
 */
public class LibraryRepository {
    private static LibraryRepository sInstance = null;
    private static Context sContext = BaseApplication.getContext();
    private MediaServer mMediaServer;
    private DeviceRepository mDeviceRepository;
    private int mImageContainerId = Integer.valueOf(ContentTree.IMAGE_ID) + 1;
    private MutableLiveData<Boolean> mIsInitComplete = new MutableLiveData<>(false);

    private AtomicInteger mResultInteger;
    private AtomicInteger mCountInteger;
    private final Object mLockObject = new Object();

    private LibraryRepository(MediaServer server) {
        mMediaServer = server;
        mDeviceRepository = DeviceRepository.obtain();
    }

    public void init() {
        TaskThreadPoolExecutor.getInstance().execute(() -> {
            Log.d(TAG, "[sll_debug] run: Thread name = " + Thread.currentThread().getName());
            // 防止两个线程一起初始化
            if (!mIsInitComplete.getValue()) {
                Log.d(TAG, "[sll_debug] run: mIsInitComplete == null Thread name = " + Thread.currentThread().getName());
                synchronized (LibraryRepository.class) {
                    if (!mIsInitComplete.getValue()) {
                        Log.d(TAG, "[sll_debug] run: synchronized mIsInitComplete == null Thread name = " + Thread.currentThread().getName());
                        loadImageItemList();
                        loadVideoItemList();
                        loadMusicItemList();
                        mIsInitComplete.postValue(true);
                    }
                }
            }
        });
    }

    public static LibraryRepository getInstance() {
        if (sInstance == null) {
            synchronized (LibraryRepository.class) {
                if (sInstance == null) {
                    sInstance = new LibraryRepository(DeviceRepository.obtain().getMediaServer());
                }
            }
        }
        return sInstance;
    }

    public MutableLiveData<List<DlnaItemEntity>> getContentItemList(List<DlnaItemEntity> entities) {
        MutableLiveData<List<DlnaItemEntity>> result = new MutableLiveData<>();
        Stack<DlnaItemEntity> stack = new Stack<>();
        TaskThreadPoolExecutor.getInstance().execute(() -> {
            stack.addAll(entities);
            Service service = mDeviceRepository.getDeviceItem().getValue().getDevice().findService(new UDAServiceType("ContentDirectory"));
            String containerId;
            mResultInteger = new AtomicInteger();
            mCountInteger = new AtomicInteger();
            mCountInteger.set(entities.size());
            List<DlnaItemEntity> list = Collections.synchronizedList(new ArrayList<>());
            DlnaItemEntity tempEntity;
            while (!stack.isEmpty()) {
                tempEntity = stack.pop();
                if (tempEntity != null && tempEntity.isContainer() && mDeviceRepository.getUpnpService() != null) {
                    containerId = tempEntity.getContainerId();
                    mDeviceRepository.getUpnpService().getControlPoint().execute(new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN, "*", 0, null, new SortCriterion(true, "dc:title")) {

                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

                        }

                        @Override
                        public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                            for (Item childItem : didl.getItems()) {
                                list.add(DlnaItemEntity.convertFromItem(childItem));
                            }
                            for (Container childContainer : didl.getContainers()) {
                                stack.push(DlnaItemEntity.convertFromContainer(childContainer));
                                mCountInteger.incrementAndGet();
                            }
                            if (mResultInteger.incrementAndGet() == mCountInteger.get()) {
                                result.postValue(list);
                            }
                            synchronized (mLockObject) {
                                mLockObject.notifyAll();
                            }
                        }

                        @Override
                        public void updateStatus(Status status) {

                        }
                    });
                }
                try {
                    synchronized (mLockObject) {
                        mLockObject.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    public MutableLiveData<List<DlnaItemEntity>> getContentItemList(DlnaItemEntity entity) {
        MutableLiveData<List<DlnaItemEntity>> result = new MutableLiveData<>();
        if (mDeviceRepository.getDeviceItem().getValue() != null && mDeviceRepository.getUpnpService() != null) {
            Service service = mDeviceRepository.getDeviceItem().getValue().getDevice().findService(new UDAServiceType("ContentDirectory"));
            String containerId;
            if (entity == null) {
                containerId = createRootContainer(service).getId();
            } else {
                containerId = entity.getContainerId();
            }
            mDeviceRepository.getUpnpService().getControlPoint().execute(new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN, "*", 0, null, new SortCriterion(true, "dc:title")) {

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    Log.d(TAG, "[xxx]" + "failure() called with: invocation = [" + invocation + "], operation = [" + operation + "], defaultMsg = " +
                            "[" + defaultMsg + "]");
                }

                @Override
                public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                    Log.d(TAG, "[sll_debug] received: ");
                    List<DlnaItemEntity> list = new ArrayList<>();
                    for (Container childContainer : didl.getContainers()) {
                        list.add(DlnaItemEntity.convertFromContainer(childContainer));
                    }
                    for (Item childItem : didl.getItems()) {
                        list.add(DlnaItemEntity.convertFromItem(childItem));
                    }
                    result.postValue(list);
                }

                @Override
                public void updateStatus(Status status) {
                    Log.i(TAG, "[xxx] updateStatus:" + status);

                }
            });
        }
        return result;
    }

    protected Container createRootContainer(Service service) {
        Container rootContainer = new Container();
        rootContainer.setId("0");
        rootContainer.setTitle("Content Directory on "
                + service.getDevice().getDisplayString());
        return rootContainer;
    }

    private static final String TAG = LibraryRepository.class.getSimpleName();

    public void loadMusicItemList() {
        mMediaServer = mDeviceRepository.getMediaServer();
        ContentNode rootNode = ContentTree.getRootNode();
        Container audioContainer = new Container(ContentTree.AUDIO_ID,
                ContentTree.ROOT_ID, "Audios", "GNaP MediaServer",
                new DIDLObject.Class("object.container"), 0);
        audioContainer.setRestricted(true);
        audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
        if (rootNode != null) {
            rootNode.getContainer().addContainer(audioContainer);
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
                    ContentTree.AUDIO_ID, audioContainer));
            String[] projection = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_MODIFIED};
            String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = sContext.getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null) {
                return;
            }
            Container typeContainer = null;
            Res res = null;
            HashMap<String, Integer> nodeIdMap = new HashMap<>();
            String fileName = "";

            while (cursor.moveToNext()) {
                String id = ContentTree.AUDIO_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                try {
                    res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')), mimeType.substring(mimeType.indexOf('/') + 1)),
                            size,
                            "http://" + mMediaServer.getAddress() + "/" + id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (res == null || TextUtils.isEmpty(filePath)) {
                    break;
                }
                res.setDuration(duration / (1000 * 60 * 60) + ":"
                        + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                        + (duration % (1000 * 60)) / 1000);
                Container tempTypeContainer = null;
                fileName = FileUtil.getFoldName(filePath);
                int nodeId = -1;
                if (nodeIdMap.containsKey(fileName)) {
                    nodeId = nodeIdMap.get(fileName);
                }
                if (nodeId != -1 && ContentTree.getNode(String.valueOf(nodeId)) != null) {
                    if (res.getSize() > 0) {
                        tempTypeContainer = ContentTree.getNode(String.valueOf(nodeId)).getContainer();
                        MusicTrack musicTrack = new MusicTrack(id, ContentTree.AUDIO_ID, title, creator, album, new PersonWithRole(creator, "Performer"), res);
                        tempTypeContainer.addItem(musicTrack);
                        tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                        ContentTree.addNode(id, new ContentNode(id, musicTrack, filePath));
                    }
                } else {
                    nodeId = ++mImageContainerId;
                    nodeIdMap.put(fileName, nodeId);
                    typeContainer = new Container(String.valueOf(nodeId), ContentTree.AUDIO_ID, fileName, "GNaP MediaServer", new DIDLObject.Class("object.container"), 0);
                    typeContainer.setRestricted(true);
                    typeContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
                    tempTypeContainer = typeContainer;
                    audioContainer.addContainer(tempTypeContainer);
                    audioContainer.setChildCount(audioContainer.getChildCount() + 1);
                    ContentTree.addNode(String.valueOf(nodeId), new ContentNode(String.valueOf(nodeId), tempTypeContainer));

                    MusicTrack musicTrack = new MusicTrack(id, ContentTree.AUDIO_ID, title, creator, album, new PersonWithRole(creator, "Performer"), res);
                    tempTypeContainer.addItem(musicTrack);
                    tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                    ContentTree.addNode(id, new ContentNode(id, musicTrack, filePath));
                }
            }
            cursor.close();
        }
        Log.d(TAG, "[sll_debug] run: audio scan over");
    }

    public void loadVideoItemList() {
        mMediaServer = mDeviceRepository.getMediaServer();
        ContentNode rootNode = ContentTree.getRootNode();
        // Video Container
        Container videoContainer = new Container();
        videoContainer.setClazz(new DIDLObject.Class("object.container"));
        videoContainer.setId(ContentTree.VIDEO_ID);
        videoContainer.setParentID(ContentTree.ROOT_ID);
        videoContainer.setTitle("Videos");
        videoContainer.setRestricted(true);
        videoContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
        videoContainer.setChildCount(0);
        if (rootNode != null) {
            rootNode.getContainer().addContainer(videoContainer);
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.VIDEO_ID, new ContentNode(ContentTree.VIDEO_ID, videoContainer));

            String[] projection = {MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.RESOLUTION,
                    MediaStore.Video.Media.DESCRIPTION,
                    MediaStore.Video.Media.DATE_MODIFIED};
            String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = sContext.getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null) {
                return;
            }
            Container typeContainer = null;
            Res res = null;
            HashMap<String, Integer> nodeIdMap = new HashMap<>();
            String fileName = "";

            while (cursor.moveToNext()) {
                String id = ContentTree.VIDEO_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                String resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DESCRIPTION));
                try {
                    res = new Res(new MimeType(mimeType.substring(0,
                            mimeType.indexOf('/')), mimeType.substring(mimeType
                            .indexOf('/') + 1)), size, "http://"
                            + mMediaServer.getAddress() + "/" + id, filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (res == null || TextUtils.isEmpty(filePath)) {
                    break;
                }
                res.setDuration(duration / (1000 * 60 * 60) + ":"
                        + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                        + (duration % (1000 * 60)) / 1000);
                res.setResolution(resolution);

                Container tempTypeContainer = null;
                fileName = FileUtil.getFoldName(filePath);
                int nodeId = -1;
                if (nodeIdMap.containsKey(fileName)) {
                    nodeId = nodeIdMap.get(fileName);
                }
                if (nodeId != -1 && ContentTree.getNode(String.valueOf(nodeId)) != null) {
                    if (res.getSize() > 0 ) {
                        tempTypeContainer = ContentTree.getNode(String.valueOf(nodeId)).getContainer();
                        VideoItem videoItem = new VideoItem(id, ContentTree.VIDEO_ID, title, creator, res);

                        // add video thumb Property
                        String videoSavePath = ImageUtil.getSaveVideoFilePath(filePath, id);
                        DIDLObject.Property albumArtURI = new DIDLObject.Property.UPNP.ALBUM_ART_URI(URI.create("http://" + mMediaServer.getAddress() + videoSavePath));
                        DIDLObject.Property[] properties = {albumArtURI};

                        videoItem.addProperties(properties);
                        videoItem.setDescription(description);
                        tempTypeContainer.addItem(videoItem);
                        tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                        ContentTree.addNode(id, new ContentNode(id, videoItem, filePath));
                    }
                } else {
                    nodeId = ++mImageContainerId;
                    nodeIdMap.put(fileName, nodeId);
                    typeContainer = new Container(String.valueOf(nodeId), ContentTree.VIDEO_ID, fileName, "GNaP MediaServer", new DIDLObject.Class("object.container"), 0);
                    typeContainer.setRestricted(true);
                    typeContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
                    tempTypeContainer = typeContainer;
                    videoContainer.addContainer(tempTypeContainer);
                    videoContainer.setChildCount(videoContainer.getChildCount() + 1);
                    ContentTree.addNode(String.valueOf(nodeId), new ContentNode(String.valueOf(nodeId), tempTypeContainer));

                    VideoItem videoItem = new VideoItem(id, ContentTree.VIDEO_ID, title, creator, res);

                    // add video thumb Property
                    String videoSavePath = ImageUtil.getSaveVideoFilePath(filePath, id);
                    DIDLObject.Property albumArtURI = new DIDLObject.Property.UPNP.ALBUM_ART_URI(URI.create("http://" + mMediaServer.getAddress() + videoSavePath));
                    DIDLObject.Property[] properties = {albumArtURI};

                    videoItem.addProperties(properties);
                    videoItem.setDescription(description);
                    tempTypeContainer.addItem(videoItem);
                    tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                    ContentTree.addNode(id, new ContentNode(id, videoItem, filePath));
                }

            }
            cursor.close();
        }
        Log.d(TAG, "[sll_debug] run: video scan over");
    }

    public void loadImageItemList() {
        mMediaServer = mDeviceRepository.getMediaServer();
        ContentNode rootNode = ContentTree.getRootNode();
        Container imageContainer = new Container(ContentTree.IMAGE_ID,
                ContentTree.ROOT_ID, "Images", "GNaP MediaServer",
                new DIDLObject.Class("object.container"), 0);
        imageContainer.setRestricted(true);
        imageContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
        if (rootNode != null) {
            rootNode.getContainer().addContainer(imageContainer);
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.IMAGE_ID, new ContentNode(ContentTree.IMAGE_ID, imageContainer));
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DESCRIPTION,
                    MediaStore.Images.Media.DATE_MODIFIED};
            String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = sContext.getContentResolver().query(uri, projection, null, null, sortOrder);

            if (cursor == null) {
                return;
            }
            Container typeContainer = null;
            Res res = null;
            HashMap<String, Integer> nodeIdMap = new HashMap<>();
            String fileName = "";
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String id = ContentTree.IMAGE_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String creator = "unkown";
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION));
                String url = "http://" + mMediaServer.getAddress() + "/" + filePath;
                try {
                    res = new Res(new MimeType(mimeType.substring(0,
                            mimeType.indexOf('/')), mimeType.substring(mimeType
                            .indexOf('/') + 1)), size, url);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (res == null || TextUtils.isEmpty(filePath)) {
                    break;
                }
                Container tempTypeContainer = null;
                fileName = FileUtil.getFoldName(filePath);
                int nodeId = -1;
                if (nodeIdMap.containsKey(fileName)) {
                    nodeId = nodeIdMap.get(fileName);
                }
                if (nodeId != -1 && ContentTree.getNode(String.valueOf(nodeId)) != null) {
                    if (res.getSize() > 0) {
                        Log.i("11111", "title = " + title);
                        Log.i("11111", "filePath = " + filePath);
                        tempTypeContainer = ContentTree.getNode(String.valueOf(nodeId)).getContainer();
                        ImageItem imageItem = new ImageItem(id, String.valueOf(nodeId), title, creator, res);
                        imageItem.setDescription(description);
                        tempTypeContainer.addItem(imageItem);
                        tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                        ContentTree.addNode(id, new ContentNode(id, imageItem, filePath));
                    }
                } else {
                    nodeId = ++mImageContainerId;
                    nodeIdMap.put(fileName, nodeId);
                    typeContainer = new Container(String.valueOf(nodeId), ContentTree.IMAGE_ID, fileName, "GNaP MediaServer", new DIDLObject.Class("object.container"), 0);
                    typeContainer.setRestricted(true);
                    typeContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
                    tempTypeContainer = typeContainer;
                    imageContainer.addContainer(tempTypeContainer);
                    imageContainer.setChildCount(imageContainer.getChildCount() + 1);
                    ContentTree.addNode(String.valueOf(nodeId), new ContentNode(String.valueOf(nodeId), tempTypeContainer));
                    ImageItem imageItem = new ImageItem(id, String.valueOf(nodeId), title, creator, res);
                    imageItem.setDescription(description);
                    tempTypeContainer.addItem(imageItem);
                    tempTypeContainer.setChildCount(tempTypeContainer.getChildCount() + 1);
                    ContentTree.addNode(id, new ContentNode(id, imageItem, filePath));
                }
            }
            cursor.close();
        }
        Log.d(TAG, "[sll_debug] run: image scan over");
    }

    public MutableLiveData<Boolean> getIsInitComplete() {
        return mIsInitComplete;
    }

    public void onDestroy() {
        mIsInitComplete.setValue(false);

    }
}
