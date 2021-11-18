package cn.junechiu.junecore.net.interceptors;

import android.util.Log;
import okhttp3.*;

import java.io.IOException;

public class DNSInterceptor implements Interceptor {
    private String authority;
    private String ip;

    public DNSInterceptor(String authority, String ip) {
        this.ip = ip;
        this.authority = authority;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        //请求域名的host
        if (chain.request().url().host().equals(authority)) {
            Request request = chain.request();
            String url = chain.request().url().toString();
            Log.d("DNSInterceptor", "before change ip--" + url);
            url = url.replace(authority, ip);
            Log.d("DNSInterceptor", "after change ip--" + url);
            request = request.newBuilder()
                    .url(url)
                    .addHeader("host", authority)
                    .build();
            return chain.proceed(request);
        } else {
            return chain.proceed(chain.request());
        }
    }

}
