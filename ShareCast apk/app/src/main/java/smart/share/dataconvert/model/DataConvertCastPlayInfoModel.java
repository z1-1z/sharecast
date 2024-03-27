package smart.share.dataconvert.model;

public class DataConvertCastPlayInfoModel {

    public static final int NONE = -1;
    public static final int VIDEO = 0;
    public static final int PICTURE = 1;
    public static final int AUDIO = 2;
    public static final int DEVICE_IOS = 3;
    public static final int DEVICE_ANDROID = 4;
    public static final int BIN = 5;

    public static final int STATE_CODE_PLAYING = 0;
    public static final int STATE_CODE_PAUSE = 1;
    public static final int STATE_CODE_STOP = 2;
    public static final int STATE_CODE_IDLE = 3;

    //  0 : video, 1:picture,  2:audio
    int mediaType;
    // 0:playing, 1: pause, 2: stop, 3:idle
    int stateCode;
    String title;
    String url;
    int currentTime;
    int totalTime;

    public DataConvertCastPlayInfoModel() {

    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}
