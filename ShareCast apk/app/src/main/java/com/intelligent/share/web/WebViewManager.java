package com.intelligent.share.web;

import static android.app.Activity.RESULT_OK;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.content.FileProvider;

import com.intelligent.share.tool.EmptyUtils;
import com.intelligent.share.widget.ObservableWebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class WebViewManager {
    private static final String kAndroidUserAgent =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2 like Mac OS X) AppleWebKit/601.1 (KHTML, like Gecko) CriOS/47.0.2526.107 Mobile/13C75 Safari/601.1.46";
    private static final String MATCH_WEB_URL = "((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)";

    public static final String MATCH_SEARCH_VIDEO = "videoplayback|m3u8|\\.mp4";

    private static final String WEB_SEARCH_URL = "https://www.google.com/search?q=";


    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageArray;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private Uri fileUri;
    private Uri videoUri;


    private long getFileSize(Uri fileUri) {
        Cursor returnCursor = mContext.getContentResolver().query(fileUri, null, null, null, null);
        returnCursor.moveToFirst();
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        return returnCursor.getLong(sizeIndex);
    }

    @TargetApi(7)
    class ResultHandler {
        public boolean handleResult(int requestCode, int resultCode, Intent intent) {
            boolean handled = false;
            if (Build.VERSION.SDK_INT >= 21) {
                if (requestCode == FILECHOOSER_RESULTCODE) {
                    Uri[] results = null;
                    if (resultCode == Activity.RESULT_OK) {
                        if (fileUri != null && getFileSize(fileUri) > 0) {
                            results = new Uri[]{fileUri};
                        } else if (videoUri != null && getFileSize(videoUri) > 0) {
                            results = new Uri[]{videoUri};
                        } else if (intent != null) {
                            results = getSelectedFiles(intent);
                        }
                    }
                    if (mUploadMessageArray != null) {
                        mUploadMessageArray.onReceiveValue(results);
                        mUploadMessageArray = null;
                    }
                    handled = true;
                }
            } else {
                if (requestCode == FILECHOOSER_RESULTCODE) {
                    Uri result = null;
                    if (resultCode == RESULT_OK && intent != null) {
                        result = intent.getData();
                    }
                    if (mUploadMessage != null) {
                        mUploadMessage.onReceiveValue(result);
                        mUploadMessage = null;
                    }
                    handled = true;
                }
            }
            return handled;
        }
    }

    private Uri[] getSelectedFiles(Intent data) {
        // we have one files selected
        if (data.getData() != null) {
            String dataString = data.getDataString();
            if (dataString != null) {
                return new Uri[]{Uri.parse(dataString)};
            }
        }
        // we have multiple files selected
        if (data.getClipData() != null) {
            final int numSelectedFiles = data.getClipData().getItemCount();
            Uri[] result = new Uri[numSelectedFiles];
            for (int i = 0; i < numSelectedFiles; i++) {
                result[i] = data.getClipData().getItemAt(i).getUri();
            }
            return result;
        }
        return null;
    }

    ObservableWebView mWebView;
    BrowserClient mWebViewClient;
    ResultHandler mResultHandler;
    Context mContext;
    private String mHomeUrl;
    private String mVideoMatchStr;
    private String mHomeVideoMatchStr;
    private boolean ignoreSSLErrors = false;
    private OnWebViewListener mOnWebViewListener;

    public void setOnWebViewListener(OnWebViewListener onWebViewListener) {
        mOnWebViewListener = onWebViewListener;
        mWebViewClient.setOnWebViewListener(onWebViewListener);
    }

    public WebViewManager(ObservableWebView webView, Context context, String homeUrl, String videoMatchStr) {
        mWebView = webView;
        mContext = context;
        mHomeUrl = homeUrl;
        mHomeVideoMatchStr = videoMatchStr;
        mVideoMatchStr = videoMatchStr;
        webViewSettingsInfo();

        mResultHandler = new ResultHandler();
        mWebViewClient = new BrowserClient(context) {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (ignoreSSLErrors) {
                    handler.proceed();
                } else {
                    super.onReceivedSslError(view, handler, error);
                }
            }
        };

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                            } else {
                                if (null != mOnWebViewListener) {
                                    mOnWebViewListener.onKeyBack();
                                }
                            }
                            return true;
                    }
                }

                return false;
            }
        });

        webView.setWebViewClient(mWebViewClient);
        addWebChromeClient();

        webView.setOnTouchScreenListener(new ObservableWebView.OnTouchScreenListener() {

            @Override
            public void onTouchUp() {
                WebView.HitTestResult result = webView.getHitTestResult();
                if (result != null) {
                    int type = result.getType();
                    if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        String imgurl = result.getExtra();
                        if(EmptyUtils.isNotEmpty(mOnWebViewListener)) {
                            mOnWebViewListener.getImageUrl(imgurl);
                        }
                    }
                }
            }
        });

    }

    private void addWebChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            //For Android 5.0+

            @Override
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (mUploadMessageArray != null) {
                    mUploadMessageArray.onReceiveValue(null);
                }
                mUploadMessageArray = filePathCallback;

                final String[] acceptTypes = getSafeAcceptedTypes(fileChooserParams);
                List<Intent> intentList = new ArrayList<Intent>();
                fileUri = null;
                videoUri = null;
                if (acceptsImages(acceptTypes)) {
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputFilename(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    intentList.add(takePhotoIntent);
                }
                if (acceptsVideo(acceptTypes)) {
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    videoUri = getOutputFilename(MediaStore.ACTION_VIDEO_CAPTURE);
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                    intentList.add(takeVideoIntent);
                }
                Intent contentSelectionIntent;
                if (Build.VERSION.SDK_INT >= 21) {
                    final boolean allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
                    contentSelectionIntent = fileChooserParams.createIntent();
                    contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
                } else {
                    contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("*/*");
                }
                Intent[] intentArray = intentList.toArray(new Intent[intentList.size()]);

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                mResultHandler.handleResult(FILECHOOSER_RESULTCODE, RESULT_OK, chooserIntent);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (null != mOnWebViewListener) {
                    mOnWebViewListener.onProgressChanged(progress);
                }

            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mOnWebViewListener.getTitle(view,title);
            }
        });
    }

    private Uri getOutputFilename(String intentType) {
        String prefix = "";
        String suffix = "";

        if (intentType == MediaStore.ACTION_IMAGE_CAPTURE) {
            prefix = "image-";
            suffix = ".jpg";
        } else if (intentType == MediaStore.ACTION_VIDEO_CAPTURE) {
            prefix = "video-";
            suffix = ".mp4";
        }

        String packageName = mContext.getPackageName();
        File capturedFile = null;
        try {
            capturedFile = createCapturedFile(prefix, suffix);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(mContext, packageName + ".fileprovider", capturedFile);
    }

    private File createCapturedFile(String prefix, String suffix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = prefix + "_" + timeStamp;
        File storageDir = mContext.getExternalFilesDir(null);
        return File.createTempFile(imageFileName, suffix, storageDir);
    }

    private Boolean acceptsImages(String[] types) {
        return isArrayEmpty(types) || arrayContainsString(types, "image");
    }

    private Boolean acceptsVideo(String[] types) {
        return isArrayEmpty(types) || arrayContainsString(types, "video");
    }

    private Boolean arrayContainsString(String[] array, String pattern) {
        for (String content : array) {
            if (content.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private Boolean isArrayEmpty(String[] arr) {
        // when our array returned from getAcceptTypes() has no values set from the
        // webview
        // i.e. <input type="file" />, without any "accept" attr
        // will be an array with one empty string element, afaik
        return arr.length == 0 || (arr.length == 1 && arr[0].length() == 0);
    }

    private String[] getSafeAcceptedTypes(WebChromeClient.FileChooserParams params) {

        // the getAcceptTypes() is available only in api 21+
        // for lower level, we ignore it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return params.getAcceptTypes();
        }

        final String[] EMPTY = {};
        return EMPTY;
    }

    private void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {

                }
            });
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
    }

    private void clearCache() {
        mWebView.clearCache(true);
        mWebView.clearFormData();
    }


    public void openUrl(String url, String videoMatchStr) {
        if (EmptyUtils.isNotEmpty(videoMatchStr)) {
            mVideoMatchStr = videoMatchStr;
        }
        mWebView.loadUrl(url);
    }

    private void webViewSettingsInfo() {

        WebSettings webSettings = mWebView.getSettings();
        //支持js脚本
        webSettings.setJavaScriptEnabled(true);
        //设置支持缩放
        webSettings.setBuiltInZoomControls(false);
        //支持缩放
        webSettings.setSupportZoom(false);
        webSettings.setDisplayZoomControls(true);
        // 开启 DOM storage 功能
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        //支持通过JS打开新窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        // 开启H5(APPCache)缓存功能
        webSettings.setAppCacheEnabled(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        /*允许跨域访问*/
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setUseWideViewPort(true); // 关键点
        // auto play video
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        webSettings.setUserAgentString(kAndroidUserAgent);

    }

    public void reloadUrl(String url) {
        mWebView.loadUrl(url);
    }

    public void reloadUrl(String url, Map<String, String> headers) {
        mWebView.loadUrl(url, headers);
    }


    public void close() {
        if (mWebView != null) {
            ViewGroup vg = (ViewGroup) (mWebView.getParent());
            vg.removeView(mWebView);
        }
        mWebView = null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void eval(String code, ValueCallback<String> result) {
        mWebView.evaluateJavascript(code, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                result.onReceiveValue(value);
            }
        });
    }


    public void reloadHomeUrl() {
        if (mWebView != null) {
            mWebView.loadUrl(mHomeUrl);
            mVideoMatchStr = mHomeVideoMatchStr;
        }
    }

    /**
     * Reloads the Webview.
     */
    public void reload() {
        if (mWebView != null) {
            mWebView.reload();
        }
    }

    /**
     * Navigates back on the Webview.
     */
    public void back() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    /**
     * Navigates forward on the Webview.
     */
    public void forward() {
        if (mWebView != null && mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    public void resize(FrameLayout.LayoutParams params) {
        mWebView.setLayoutParams(params);
    }

    /**
     * Checks if going back on the Webview is possible.
     */
    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    /**
     * Checks if going forward on the Webview is possible.
     */
    public boolean canGoForward() {
        return mWebView.canGoForward();
    }

    /**
     * Clears cache
     */
    public void cleanCache() {
        mWebView.clearCache(true);
    }

    public void hide() {
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }
    }

    public void show() {
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
    }

    public void stopLoading() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    public boolean isMediaMatchUrlStr(String url) {
        return isVideoMatchUrlStr(url);
    }

    public boolean isVideoMatchUrlStr(String url) {
        if(url.contains("/*~")) {
            return false;
        }
        if (mHomeUrl.equals("https://www.vidio.com/")) {
            if(url.contains("ads?")) {
                return false;
            }
        }
        return isMatchUrlStr(url, mVideoMatchStr);
    }


    public String getMatch() {
        return mVideoMatchStr;
    }

    private static boolean isMatchUrlStr(String url, String matchStr) {
        if (url.contains("http://") || url.contains("https://")) {
            Pattern pattern = Pattern.compile(matchStr);
            Matcher matcher = pattern.matcher(url);
            return matcher.find();
        }

        return false;
    }

    public static String getSearchUrl(String searchText) {
        String url = "";
        boolean isWebUrl = isMatchUrlStr(searchText, MATCH_WEB_URL);
        if (isWebUrl) {
            if (!(searchText.contains("http://") || searchText.contains("https://"))) {
                url = "https://" + searchText;
            } else {
                url = searchText;
            }
        } else {
            url = WEB_SEARCH_URL + searchText;
        }

        return url;
    }


    public interface OnWebViewListener {
        void onKeyBack();

        void onProgressChanged(int progress);

        void onUrlGetFromWebView(String url);

        void onPageStarted(String url);

        void onPageFinished(String url);

        void shouldStart(String url);

        void abortLoad(String url);

        void getTitle(View view,String title);

        void onHttpError(String url, int errorCode);

        void getImageUrl(String imageUrl);

    }
}
