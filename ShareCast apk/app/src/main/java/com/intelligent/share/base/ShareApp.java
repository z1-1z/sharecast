package com.intelligent.share.base;

import android.content.Context;

import com.tosmart.dlna.application.BaseApplication;

public class ShareApp extends BaseApplication {

    public static Context getAppContext()
    {
        return mContext;
    }

}
