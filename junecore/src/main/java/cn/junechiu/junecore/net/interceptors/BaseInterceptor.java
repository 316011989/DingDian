package cn.junechiu.junecore.net.interceptors;

import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;

public abstract class BaseInterceptor implements Interceptor {

    //get请求
    protected LinkedHashMap<String, String> getUrlParameters(Chain chain) {
        final HttpUrl url = chain.request().url();
        int size = url.querySize();  //请求参数的个数
        final LinkedHashMap<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) { //key value 请求参数
            params.put(url.queryParameterName(i), url.queryParameterValue(i));
        }
        return params;
    }

    protected String getUrlParameters(Chain chain, String key) {
        final Request request = chain.request();
        return request.url().queryParameter(key);
    }

    //post请求
    protected LinkedHashMap<String, String> getBodyparameters(Chain chain) {
        final FormBody formBody = (FormBody) chain.request().body();
        final LinkedHashMap<String, String> params = new LinkedHashMap<>();//有序的
        int size = formBody.size();
        for (int i = 0; i < size; i++) {
            params.put(formBody.name(i), formBody.value(i));
        }
        return params;
    }

    protected String getBodyparameters(Chain chain, String key) {
        return getBodyparameters(chain).get(key);
    }
}
