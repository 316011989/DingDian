package com.kk.taurus.playerbase.player;

import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {

    public OkInterceptorListener listener;

    public LoggingInterceptor(OkInterceptorListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Log.d("okhttp--", String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Log.d("okhttp--", String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        Log.d("okhttp---", "code: " + response.code());
        if (listener != null) {
            listener.responseCode(response.code());
        }
        return response;
    }
}
