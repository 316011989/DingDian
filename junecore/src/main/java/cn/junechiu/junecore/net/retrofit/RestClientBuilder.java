package cn.junechiu.junecore.net.retrofit;

import android.content.Context;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import cn.junechiu.junecore.net.callback.IError;
import cn.junechiu.junecore.net.callback.IFailure;
import cn.junechiu.junecore.net.callback.IRequest;
import cn.junechiu.junecore.net.callback.ISuccess;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by junzhao on 2017/12/2.
 */

public class RestClientBuilder {

    private String mUrl = null;

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();

    private Map<String, Object> headerParams = null;

    private IRequest mIrequest = null;

    private ISuccess mIsuccess = null;

    private IError mIerror = null;

    private IFailure mIfailure = null;

    private RequestBody mBody = null;

    private String mJson = null;

    private File mFile = null;

    private Context mContext = null;

    private String mDownloadDir;  //保存的文件夹

    private String mExtension; //扩展名

    private String mName;  //下载的文件名

    RestClientBuilder() {

    }

    public final RestClientBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final RestClientBuilder params(WeakHashMap<String, Object> params) {
        PARAMS.putAll(params);
        return this;
    }

    public final RestClientBuilder headerParams(Map<String, Object> params) {
        this.headerParams = params;
        return this;
    }

    public final RestClientBuilder params(String key, Object value) {
        PARAMS.put(key, value);
        return this;
    }

    public final RestClientBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UFT-8"), raw);
        return this;
    }

    public final RestClientBuilder json(String json) {
        this.mJson = json;
        return this;
    }

    public final RestClientBuilder request(IRequest irequest) {
        this.mIrequest = irequest;
        return this;
    }

    public final RestClientBuilder success(ISuccess isuccess) {
        this.mIsuccess = isuccess;
        return this;
    }

    public final RestClientBuilder error(IError ierror) {
        this.mIerror = ierror;
        return this;
    }

    public final RestClientBuilder fail(IFailure ifailure) {
        this.mIfailure = ifailure;
        return this;
    }

    public final RestClientBuilder loader(Context context) {
        this.mContext = context;
        return this;
    }

    public final RestClientBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final RestClientBuilder file(String filePath) {
        this.mFile = new File(filePath);
        return this;
    }

    public final RestClientBuilder name(String name) {
        this.mName = name;
        return this;
    }

    public final RestClientBuilder dir(String dir) {
        this.mDownloadDir = dir;
        return this;
    }

    public final RestClientBuilder extension(String extension) {
        this.mExtension = extension;
        return this;
    }

//    public Map<String, Object> checkParams() {
//        if (mParams == null) {
//            return new WeakHashMap<>();
//        }
//        return mParams;
//    }

    public final RestClient build() {
        return new RestClient(mUrl,
                PARAMS,
                headerParams,
                mIrequest,
                mIsuccess,
                mIerror,
                mIfailure,
                mBody,
                mJson,
                mFile,
                mDownloadDir,
                mExtension,
                mName,
                mContext
        );
    }
}
