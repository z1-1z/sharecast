package com.intelligent.share.web;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BrowserClient extends WebViewClient {
    private Pattern invalidUrlPattern = null;
    private WebViewManager.OnWebViewListener mOnWebViewListener;
    private Handler platformThreadHandler;

    public void setOnWebViewListener(WebViewManager.OnWebViewListener onWebViewListener) {
        mOnWebViewListener = onWebViewListener;
    }

    public BrowserClient(Context context) {
        this.invalidUrlPattern = null;
        this.platformThreadHandler = new Handler(context.getMainLooper());
    }


    public void updateInvalidUrlRegex(String invalidUrlRegex) {
        if (invalidUrlRegex != null) {
            invalidUrlPattern = Pattern.compile(invalidUrlRegex);
        } else {
            invalidUrlPattern = null;
        }
    }

    public void PostUrl(final String url) {
        Runnable postUrlRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if(null != mOnWebViewListener) {
                            mOnWebViewListener.onUrlGetFromWebView(url);
                        }
                    }
                };
        if (platformThreadHandler.getLooper() == Looper.myLooper()) {
            postUrlRunnable.run();
        } else {
            platformThreadHandler.post(postUrlRunnable);
        }
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        PostUrl(url);
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        String NewUrl = "";
        StringBuilder strbuild = new StringBuilder();
        BufferedReader bufferread = null;
        WebResourceResponse respon;

        if (url.contains("youtube") && (url.contains("base.js"))) {
            NewUrl = url;
            try {
                Log.i("test", "change url is :" + url);
                URL uri = new URL(NewUrl);
                HttpURLConnection httpcon = (HttpURLConnection) uri.openConnection();
                httpcon.setReadTimeout(40 * 1000);
                bufferread = new BufferedReader(
                        new InputStreamReader(httpcon.getInputStream())
                );
                String line = "";
                while ((line = bufferread.readLine()) != null) {
                    strbuild.append(line);
                    strbuild.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bufferread != null) {
                    try {
                        bufferread.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            String finialStr = "";
            finialStr = strbuild.toString();
            finialStr = finialStr.replace("window.MediaSource.isTypeSupported(a)", "!1");
            WebResourceResponse webRespon = null;
            webRespon = new WebResourceResponse("text/html", "utf-8",
                    new ByteArrayInputStream(finialStr.getBytes()));
            Log.i("test", "respon :" + webRespon);
            return webRespon;
        } else {
            respon = super.shouldInterceptRequest(view, request);
        }

        return respon;
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if(null != mOnWebViewListener) {
            mOnWebViewListener.onPageStarted(url);
        }

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (null != mOnWebViewListener) {
            mOnWebViewListener.onPageFinished(url);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        // returning true causes the current WebView to abort loading the URL,
        // while returning false causes the WebView to continue loading the URL as usual.
        String url = request.getUrl().toString();
        boolean isInvalid = checkInvalidUrl(url);
        if(null != mOnWebViewListener) {
            if(isInvalid) {
                mOnWebViewListener.abortLoad(url);
            }else {
                mOnWebViewListener.shouldStart(url);
            }
        }

        return isInvalid;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // returning true causes the current WebView to abort loading the URL,
        // while returning false causes the WebView to continue loading the URL as usual.
        boolean isInvalid = checkInvalidUrl(url);
        if(null != mOnWebViewListener) {
            if(isInvalid) {
                mOnWebViewListener.abortLoad(url);
            }else {
                mOnWebViewListener.shouldStart(url);
            }
        }
        return isInvalid;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if(null != mOnWebViewListener) {
            String url = request.getUrl().toString();
            mOnWebViewListener.onHttpError(url, errorResponse.getStatusCode());
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        if(null != mOnWebViewListener) {
            mOnWebViewListener.onHttpError(failingUrl, errorCode);
        }
    }

    private boolean checkInvalidUrl(String url) {
        if (invalidUrlPattern == null) {
            return false;
        } else {
            Matcher matcher = invalidUrlPattern.matcher(url);
            return matcher.lookingAt();
        }
    }
}