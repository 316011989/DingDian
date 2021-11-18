package cn.junechiu.junecore.net.rx;

import android.content.Context;

import java.io.File;
import java.util.WeakHashMap;

import cn.junechiu.junecore.net.retrofit.RestCreator;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by junzhao on 2017/12/2.
 */
public class RxRestClientBuilder {

    private String mUrl = null;

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();

    private RequestBody mBody = null;

    private File mFile = null;

    private Context mContext = null;

    RxRestClientBuilder() {
    }

    public final RxRestClientBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final RxRestClientBuilder params(WeakHashMap<String, Object> params) {
        PARAMS.putAll(params);
        return this;
    }

    public final RxRestClientBuilder params(String key, Object value) {
        PARAMS.put(key, value);
        return this;
    }

    public final RxRestClientBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UFT-8"), raw);
        return this;
    }

    public final RxRestClientBuilder loader(Context context) {
        this.mContext = context;
        return this;
    }

    public final RxRestClientBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final RxRestClientBuilder file(String filePath) {
        this.mFile = new File(filePath);
        return this;
    }

//    public Map<String, Object> checkParams() {
//        if (mParams == null) {
//            return new WeakHashMap<>();
//        }
//        return mParams;
//    }

    public final RxRestClient build() {
        return new RxRestClient(mUrl,
                PARAMS,
                mBody,
                mFile,
                mContext
        );
    }
}
