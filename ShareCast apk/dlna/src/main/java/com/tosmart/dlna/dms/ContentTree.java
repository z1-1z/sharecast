package com.tosmart.dlna.dms;

import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;

import java.util.HashMap;


public class ContentTree {

    public final static String ROOT_ID = "0";
    public final static String VIDEO_ID = "1";
    public final static String AUDIO_ID = "2";
    public final static String IMAGE_ID = "3";
    public final static String TEMP_ID = "4";
    public final static String VIDEO_PREFIX = "video-item-";
    public final static String AUDIO_PREFIX = "audio-item-";
    public final static String IMAGE_PREFIX = "image-item-";

    private static HashMap<String, ContentNode> sContentMap;

    private volatile static ContentNode sRootNode;

    protected static ContentNode createRootNode() {
        // create root container
        Container root = new Container();
        root.setId(ROOT_ID);
        root.setParentID("-1");
        root.setTitle("GNaP MediaServer root directory");
        root.setCreator("GNaP Media Server");
        root.setRestricted(true);
        root.setSearchable(true);
        root.setWriteStatus(WriteStatus.NOT_WRITABLE);
        root.setChildCount(0);
        ContentNode rootNode = new ContentNode(ROOT_ID, root);
        sContentMap.put(ROOT_ID, rootNode);
        return rootNode;
    }

    public static ContentNode getRootNode() {
        return sRootNode;
    }

    public static ContentNode getNode(String id) {
        if (sContentMap.containsKey(id)) {
            return sContentMap.get(id);
        }
        return null;
    }

    public static boolean hasNode(String id) {
        return sContentMap != null && sContentMap.containsKey(id);
    }

    public static void addNode(String id, ContentNode node) {
        sContentMap.put(id, node);
    }

    public static void init() {
        if (sContentMap == null) {
            sContentMap = new HashMap<>();
        }
        sContentMap.clear();
        sRootNode = createRootNode();
    }
}
