package cn.junechiu.junecore.net.rx;

import android.content.Context;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import cn.junechiu.junecore.net.retrofit.HttpMethod;
import cn.junechiu.junecore.net.retrofit.RestCreator;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * builder设计模式
 */
public class RxRestClient {

    private final String URL;   //请求url地址

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();

    private final RequestBody BODY; //请求体

    private final File FILE;  //上传的文件

    private final Context CONTEXT;

    //封装请求的一些参数
    public RxRestClient(String url,
                        Map<String, Object> params,
                        RequestBody body,
                        File file,
                        Context context) {
        this.URL = url;
        PARAMS.putAll(params);
        this.BODY = body;
        this.FILE = file;
        this.CONTEXT = context;
    }

    public static RxRestClientBuilder builder() {
        return new RxRestClientBuilder();
    }

    private Observable<String> request(HttpMethod method) {
        final RxRestService service = RestCreator.getRxRestService();

        Observable<String> observable = null;

        switch (method) {
            case GET:
                observable = service.get(URL, PARAMS);
                break;
            case POST:
                observable = service.post(URL, PARAMS);
                break;
            case PUT:
                observable = service.put(URL, PARAMS);
                break;
            case DELETE:
                observable = service.delete(URL, PARAMS);
                break;
            case PUT_RAW:
                observable = service.putRaw(URL, BODY);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), FILE);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                observable = service.upload(URL, body);
                break;
            case POST_RAW:
                observable = service.postRaw(URL, BODY);
                break;
            default:
                break;
        }
        return observable;
    }

    public final Observable<String> get() {
        return request(HttpMethod.GET);
    }

    public final Observable<String> post() {
        if (BODY == null) {  //post请求
            return request(HttpMethod.POST);
        } else {  //post raw请求
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null !");
            }
            return request(HttpMethod.POST_RAW);
        }
    }

    public final Observable<String> put() {
        if (BODY == null) {
            return request(HttpMethod.PUT);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null !");
            }
            return request(HttpMethod.PUT_RAW);
        }
    }

    public final Observable<String> delete() {
        return request(HttpMethod.DELETE);
    }

    public final Observable<String> upload() {
        return request(HttpMethod.UPLOAD);
    }

    public final Observable<ResponseBody> download() {
        return RestCreator.getRxRestService().download(URL, PARAMS);
    }
}
