package cn.junechiu.junecore.net.download;

import android.os.AsyncTask;
import java.util.WeakHashMap;

import cn.junechiu.junecore.net.callback.IError;
import cn.junechiu.junecore.net.callback.IFailure;
import cn.junechiu.junecore.net.callback.IRequest;
import cn.junechiu.junecore.net.callback.ISuccess;
import cn.junechiu.junecore.net.retrofit.RestCreator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by junzhao on 2017/12/2.
 */

public class DownloadHandler {

    private final String URL;   //请求url地址

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();

    private final IRequest IRQUEST;  //请求过程回调

    private final ISuccess ISUCCESS; //请求成功回调

    private final IError IERROR; //请求失败回调

    private final IFailure IFAILURE; //请求失败

    private final String DOWNLOAD_DTR;  //保存的文件夹

    private final String EXTENSION; //扩展名

    private final String NAME;  //下载的文件名

    public DownloadHandler(String url,
                           IRequest request,
                           ISuccess success,
                           IError error,
                           IFailure failure,
                           String downloadDir,
                           String extension,
                           String name) {
        this.URL = url;
        this.IRQUEST = request;
        this.ISUCCESS = success;
        this.IERROR = error;
        this.IFAILURE = failure;
        this.DOWNLOAD_DTR = downloadDir;
        this.EXTENSION = extension;
        this.NAME = name;
    }

    public final void handleDownload() {
        if (IRQUEST != null) {
            IRQUEST.onRequestStart();
        }
        RestCreator.getRestService().download(URL, PARAMS).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            final ResponseBody responseBody = response.body();
                            final SaveFileTask task = new SaveFileTask(IRQUEST, ISUCCESS);
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DOWNLOAD_DTR, EXTENSION, responseBody, NAME);
                            //这里要注意判断 否则文件下载不全
                            if (task.isCancelled()) {
                                if (IRQUEST != null) {
                                    IRQUEST.onRequestEnd();
                                }
                            }
                        } else {
                            if (IERROR != null) {
                                IERROR.onError(response.code(), response.message());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (IFAILURE != null) {
                            IFAILURE.onFailure();
                        }
                    }
                }
        );
    }
}
