package smart.share.dataconvert.model;

public class DataConvertCastPlayModel {
    public static final String CAST_MEDIA_TYPE = "mediaType";
    public static final String CAST_ACTION_CODE = "actionCode";
    public static final String CAST_URL = "url";
    public static final String CAST_SEEK_TIME = "seekTime";

    public static final int ACTION_PLAY = 0;
    public static final int ACTION_RESUME = 1;
    public static final int ACTION_PAUSE = 2;
    public static final int ACTION_STOP = 3;
    public static final int ACTION_SEEK_TIME = 4;



    // 0 : video,  1:picture,  2:audio
   int mediaType;
   // 0:play, 1: resume 2: pause, 3: stop, 4:seek
   int actionCode;
   String url;
   // other media_type time is 0
   int seekTime;

    public DataConvertCastPlayModel(int mediaType, int actionCode, String url, int seekTime) {
        this.mediaType = mediaType;
        this.actionCode = actionCode;
        this.url = url;
        this.seekTime = seekTime;
    }

    public int getMediaType() {
        return mediaType;
    }

    public int getActionCode() {
        return actionCode;
    }

    public String getUrl() {
        return url;
    }

    public int getSeekTime() {
        return seekTime;
    }
}
