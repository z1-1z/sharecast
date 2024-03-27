package com.tosmart.dlna.util;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * @date 2019/4/24
 */
public class ResourceUtils {
    public static int[] getIntResourceArray(Context context, int id) {
        if (context == null || id < 0) {
            return null;
        }
        TypedArray keyBoardArray = context.getResources().obtainTypedArray(id);
        int[] result = new int[keyBoardArray.length()];
        for (int i = 0; i < keyBoardArray.length(); i++) {
            result[i] = keyBoardArray.getResourceId(i, i);
        }
        keyBoardArray.recycle();
        return result;
    }
}
