package com.tosmart.dlna.data.local;

import android.text.TextUtils;
import android.util.Log;

import com.tosmart.dlna.dmc.GenerateXml;
import com.tosmart.dlna.util.Constant;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * @date 2019/4/29
 */
@Entity
public class DlnaItemEntity {
    @Id
    private Long id;
    private String type = null;
    private String mimeType = null;
    private String image = null;
    private String name = null;
    private String path = null;
    private boolean isContainer = false;
    private boolean isSelected = false;
    private int childCount = -1;
    private String containerId = null;
    private String metaData = null;
    private long size;
    private String duration;


    @Generated(hash = 1036664247)
    public DlnaItemEntity(Long id, String type, String mimeType, String image, String name, String path,
            boolean isContainer, boolean isSelected, int childCount, String containerId, String metaData,
            long size, String duration) {
        this.id = id;
        this.type = type;
        this.mimeType = mimeType;
        this.image = image;
        this.name = name;
        this.path = path;
        this.isContainer = isContainer;
        this.isSelected = isSelected;
        this.childCount = childCount;
        this.containerId = containerId;
        this.metaData = metaData;
        this.size = size;
        this.duration = duration;
    }

    @Generated(hash = 1934381736)
    public DlnaItemEntity() {
    }


    public static DlnaItemEntity convertFromContainer(Container childContainer) {
        DlnaItemEntity entity = new DlnaItemEntity();
        entity.isContainer = true;
        entity.childCount = childContainer.getChildCount();
        entity.containerId = childContainer.getId();
        entity.name = childContainer.getTitle();
        return entity;
    }

    public static DlnaItemEntity convertFromItem(Item childItem) {
        DlnaItemEntity entity = new DlnaItemEntity();
        entity.isContainer = false;
        entity.name = childItem.getTitle();
        Log.i("TAG", "[xxx] convertFromItem:getSize " + childItem.getFirstResource().getSize());
        Log.i("TAG", "[xxx] convertFromItem:getDuration"+ childItem.getFirstResource().getDuration());
        entity.size = childItem.getFirstResource().getSize();
        entity.duration = childItem.getFirstResource().getDuration();
        entity.mimeType = childItem.getResources().get(0).getProtocolInfo().getContentFormatMimeType().toString();
        entity.type = childItem.getResources().get(0).getProtocolInfo().getContentFormatMimeType().getType();
        entity.path = childItem.getFirstResource().getValue();
        entity.id = Long.parseLong(entity.path.hashCode() + "");
        try {
            entity.metaData = new GenerateXml().generate(childItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getThumbnail(childItem, entity);
        return entity;
    }

    private static void getThumbnail(Item item, DlnaItemEntity entity) {
        if (entity.type.equals(Constant.IMAGE_TYPE)) {
            entity.image = item.getFirstResource().getValue();
        } else if (entity.type.equals(Constant.VIDEO_TYPE)) {
            entity.image = item.getFirstResource().getRealPath();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public void setContainer(boolean container) {
        isContainer = container;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public boolean getIsContainer() {
        return this.isContainer;
    }

    public void setIsContainer(boolean isContainer) {
        this.isContainer = isContainer;
    }

    public boolean getIsSelected() {
        return this.isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
