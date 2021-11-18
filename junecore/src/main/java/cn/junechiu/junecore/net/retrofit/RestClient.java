package cn.junechiu.junecore.net.retrofit;

import android.content.Context;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import cn.junechiu.junecore.net.callback.IError;
import cn.junechiu.junecore.net.callback.IFailure;
import cn.junechiu.junecore.net.callback.IRequest;
import cn.junechiu.junecore.net.callback.ISuccess;
import cn.junechiu.junecore.net.callback.RequestCallbacks;
import cn.junechiu.junecore.net.download.DownloadHandler;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * builder设计模式
 */
public class RestClient {

    private final String URL;   //请求url地址

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();

    private final Map<String, Object> HEADERPARAMS;

    private final IRequest IRQUEST;  //请求过程回调

    private final ISuccess ISUCCESS; //请求成功回调

    private final IError IERROR; //请求失败回调

    private final IFailure IFAILURE; //请求失败

    private final RequestBody BODY; //请求体

    private final String JSON; //请求json

    private final File FILE;  //上传的文件

    private final String DOWNLOAD_DTR;  //保存的文件夹

    private final String EXTENSION; //扩展名

    private final String NAME;  //下载的文件名

    private final Context CONTEXT;

    //封装请求的一些参数
    public RestClient(String url,
                      Map<String, Object> params,
                      Map<String, Object> headerParams,
                      IRequest request,
                      ISuccess success,
                      IError error,
                      IFailure failure,
                      RequestBody body,
                      String json,
                      File file,
                      String downloadDir,
                      String extension,
                      String name,
                      Context context) {
        this.URL = url;
        PARAMS.putAll(params);
        this.HEADERPARAMS = headerParams;
        this.IRQUEST = request;
        this.ISUCCESS = success;
        this.IERROR = error;
        this.IFAILURE = failure;
        this.BODY = body;
        this.JSON = json;
        this.FILE = file;
        this.DOWNLOAD_DTR = downloadDir;
        this.EXTENSION = extension;
        this.NAME = name;
        this.CONTEXT = context;
    }

    public static RestClientBuilder builder() {
        return new RestClientBuilder();
    }

    private void request(HttpMethod method) {
        final RestService service = RestCreator.getRestService();

        Call<String> call = null;

        if (IRQUEST != null) {
            IRQUEST.onRequestStart();
        }

        switch (method) {
            case GET:
                if (HEADERPARAMS != null && HEADERPARAMS.size() > 0) {
                    call = service.getWithHeaders(URL, HEADERPARAMS);
                } else {
                    call = service.get(URL, PARAMS);
                }
                break;
            case POST:
                call = service.post(URL, PARAMS);
                break;
            case PUT:
                call = service.put(URL, PARAMS);
                break;
            case DELETE:
                call = service.delete(URL, PARAMS);
                break;
            case PUT_RAW:
                call = service.putRaw(URL, BODY);
                break;
            case POST_JSON:
                call = service.postJson(URL, JSON);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), FILE);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                call = service.upload(URL, body);
                break;
            case POST_RAW:
                call = service.postRaw(URL, BODY);
                break;
            default:
                break;
        }

        if (call != null) {
            call.enqueue(getRequestCallback());
        }
    }

    //返回回调操作类
    private Callback<String> getRequestCallback() {
        return new RequestCallbacks(
                IRQUEST,
                ISUCCESS,
                IERROR,
                IFAILURE
        );
    }

    public final void get() {
        request(HttpMethod.GET);
    }

    public final void post() {
        if (BODY == null) {  //post请求
            request(HttpMethod.POST);
        } else {  //post raw请求
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null !");
            }
            request(HttpMethod.POST_RAW);
        }
    }

    public final void postJson() {
        request(HttpMethod.POST_JSON);
    }

    public final void put() {
        if (BODY == null) {
            request(HttpMethod.PUT);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null !");
            }
            request(HttpMethod.PUT_RAW);
        }
    }

    public final void delete() {
        request(HttpMethod.DELETE);
    }

    public final void upload() {
        request(HttpMethod.UPLOAD);
    }

    public final void download() {
        new DownloadHandler(
                URL,
                IRQUEST,
                ISUCCESS,
                IERROR,
                IFAILURE,
                DOWNLOAD_DTR,
                EXTENSION,
                NAME
        ).handleDownload();
    }
}
