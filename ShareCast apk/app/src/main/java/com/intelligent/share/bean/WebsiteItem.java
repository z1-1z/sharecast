package com.intelligent.share.bean;

import java.util.List;

/**
 * @author Administrator
 */
public class WebsiteItem {
    private List<WebInfoItem> webInfoList;
    private String title;

    public WebsiteItem(List<WebInfoItem> webList, String title) {
        this.webInfoList = webList;
        this.title = title;
    }

    public List<WebInfoItem> getWebInfoList() {
        return webInfoList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class WebInfoItem {
        private String url;
        private String match;
        private int imageId;
        private String name;

        public WebInfoItem(String url, String match, int imageId, String name) {
            this.url = url;
            this.match = match;
            this.imageId = imageId;
            this.name = name;
        }

        public WebInfoItem(int imageId, String url, String match) {
            this.imageId = imageId;
            this.url = url;
            this.match = match;
        }

        public String getUrl() {
            return url;
        }

        public String getMatch() {
            return match;
        }

        public int getImageId() {
            return imageId;
        }

        public String getName() {
            return name;
        }
    }
}
