package smart.share.util;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.intelligent.share.base.ShareApp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CommonHelper {
    private static final String TAG = "CommonHelper";
    private static final int ARRAY_SIZE = 1024;

    public static String getStrFromInputSteam(InputStream in) throws IOException {
        String string = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len = 0;
        byte[] data = new byte[ARRAY_SIZE];
        try {
            while ((len = in.read(data)) != -1) {

                outputStream.write(data, 0, len);
            }
            string = new String(outputStream.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }


    public static Uri getFileUri(String path) {

        File picPath = new File(path);
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }

        return uri;
    }


    public static long getDuration(String pt) {
        Log.i(TAG, "getDuration: pt = " + pt);
        Cursor mCursor = ShareApp.getAppContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null,
                null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (mCursor == null) {
            Log.e(TAG, "getDuration: mCursor == null");
            return 0;
        }
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
            Log.i(TAG, "getDuration: path = " + path);
            if (!TextUtils.isEmpty(path) && path.equals(pt)) {
                return mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            }
            mCursor.moveToNext();
        }
        mCursor.close();
        return 0;
    }

    public static ArrayList<String> readDirectory(String path) {
        ArrayList<String> files = new ArrayList<>();
        File directory = new File(path);
        if (directory.isDirectory() && directory.exists()) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    files.add(file.getName());
                }
            }
        }
        return files;
    }

    public static boolean isPathDirectory(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.isDirectory();
        }
        return false;
    }

}
