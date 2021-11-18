package cn.video.star.utils;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.webkit.*;

/**
 * Created by android on 2018/10/19.
 */
public class MyWebViewClient extends WebViewClient {

    public OnParseWebUrlListener onParseWebUrlListener;

    public MyWebViewClient(OnParseWebUrlListener onParseWebUrlListener) {
        this.onParseWebUrlListener = onParseWebUrlListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request.getUrl().toString().startsWith("intent") || request.getUrl().toString().startsWith("youku")) {
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("intent") || url.startsWith("youku")) {
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    /*解决ssl证书问题*/
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (onParseWebUrlListener != null) {
            onParseWebUrlListener.onFindUrl(url);
        }
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String url = request.getUrl().toString();
            if (onParseWebUrlListener != null) {
                onParseWebUrlListener.onFindUrl(url);
            }
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    public interface OnParseWebUrlListener {
        void onFindUrl(String url);

        void onError(String errorMsg);
    }
}
