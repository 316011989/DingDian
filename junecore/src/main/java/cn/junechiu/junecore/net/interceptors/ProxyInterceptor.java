package cn.junechiu.junecore.net.interceptors;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ProxyInterceptor implements Interceptor {
    private Context context;
    private boolean isToast = false;//是否已经Toast提示过

    public ProxyInterceptor(Context context) {
        this.context = context;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        if (isWifiProxy()) {
            //使用代理情况下
            if (!isToast) {
                isToast = true;
            }
            //返回空数据
            return new Response.Builder()
                    .code(200)
                    .addHeader("Content-Type", "application/json")
                    .body(ResponseBody.create(MediaType.parse("application/json"), "{}"))
                    .message("请关闭代理重试")
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .build();
        } else {
            return chain.proceed(chain.request());
        }
    }

    public boolean isWifiProxy() {
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }
}
