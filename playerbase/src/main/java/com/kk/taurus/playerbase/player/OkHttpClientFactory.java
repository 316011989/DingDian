package com.kk.taurus.playerbase.player;

import okhttp3.OkHttpClient;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class OkHttpClientFactory {
    public static OkInterceptorListener listener;

    private static SSLContext sslContext = null;

    private OkHttpClientFactory() {
    }

    private static class SingletonHolder {
        private static OkHttpClientFactory INSTANCE = new OkHttpClientFactory();
    }

    public static OkHttpClientFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class OKHttpHolder {

        private static final int TIMEOUT = 60;

        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        private static final X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        private static final OkHttpClient.Builder addInterceptor() {
            BUILDER.addInterceptor(new LoggingInterceptor(listener));
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return BUILDER;
        }

        private static final OkHttpClient OKHTTP_CLIENT = addInterceptor()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return OKHttpHolder.OKHTTP_CLIENT;
    }


}
