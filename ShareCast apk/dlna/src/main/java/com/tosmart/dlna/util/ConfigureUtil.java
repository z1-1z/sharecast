package com.tosmart.dlna.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tosmart.dlna.R;

/**
 * Created by xxx on 2019/5/5.
 */
public class ConfigureUtil {
    private final static String PLAYER_NAME = "player_name";
    private final static String DMS_STATUS = "dms_status";
    private final static String DMS_NAME = "dms_name";
    private final static String IMAGE_SLIDE_TIME = "image_slide_time";
    private final static String DMR_STATUS = "dmr_status";
    private final static String PLAY_MODE = "play_mode";
    private final static String REPEAT_MODE = "repeat_mode";
    private final static String LAST_PLAY_PATH = "last_play_path";

    public enum PlayMode {
        shuffle_on, shuffle_off
    }

    public enum RepeatMode {
        repeat_play_list, repeat_trace, repeat_off
    }

    public static String getRenderName(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(PLAYER_NAME,
                context.getString(R.string.player_name_local));
    }

    public static boolean getDmsOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean(DMS_STATUS, true);
    }

    public static String getDeviceName(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(DMS_NAME,
                context.getString(R.string.device_local));
    }

    public static int getSlideTime(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getInt(IMAGE_SLIDE_TIME, 5000);
    }

    public static void setSlideTime(Context context, int time) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().putInt(IMAGE_SLIDE_TIME, time).apply();
    }

    public static boolean getRenderOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean(DMR_STATUS, true);
    }

    public static void setPlayMode(Context context, PlayMode mode) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().putString(PLAY_MODE, mode.name()).apply();
    }

    public static PlayMode getPlayMode(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String playMode = prefs.getString(PLAY_MODE, PlayMode.shuffle_off.name());
        return PlayMode.valueOf(playMode);
    }

    public static PlayMode switchPlayMode(Context context) {
        PlayMode curMode = getPlayMode(context);
        switch (curMode) {
            case shuffle_on:
                setPlayMode(context, PlayMode.shuffle_off);
                break;
            case shuffle_off:
                setPlayMode(context, PlayMode.shuffle_on);
                break;
        }
        return getPlayMode(context);
    }

    public static void setRepeatMode(Context context, RepeatMode mode) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().putString(REPEAT_MODE, mode.name()).apply();
    }

    public static RepeatMode getRepeatMode(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String playMode = prefs.getString(REPEAT_MODE, RepeatMode.repeat_off.name());
        return RepeatMode.valueOf(playMode);
    }

    public static RepeatMode switchRepeatMode(Context context) {
        RepeatMode curMode = getRepeatMode(context);
        switch (curMode) {
            case repeat_off:
                setRepeatMode(context, RepeatMode.repeat_play_list);
                break;
            case repeat_play_list:
                setRepeatMode(context, RepeatMode.repeat_trace);
                break;
            case repeat_trace:
                setRepeatMode(context, RepeatMode.repeat_off);
                break;
        }
        return getRepeatMode(context);
    }

    public static String getLastPlayPath(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(LAST_PLAY_PATH,
                null);
    }

    public static void setLastPlayPath(Context context, String path) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().putString(LAST_PLAY_PATH, path).apply();
    }
}
