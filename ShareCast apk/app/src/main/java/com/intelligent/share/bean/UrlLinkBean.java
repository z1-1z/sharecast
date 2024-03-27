package com.intelligent.share.bean;

public class UrlLinkBean {
    private String name;
    private String url;
    private boolean select = false;

    public UrlLinkBean(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
