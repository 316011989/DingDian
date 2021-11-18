package cn.junechiu.junecore.net.callback;

import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by junzhao on 2017/12/2.
 */

public class RequestCallbacks implements Callback<String> {

    private final IRequest IRQUEST;

    private final ISuccess ISUCCESS;

    private final IError IERROR;

    private final IFailure IFAILURE;

    private static final Handler HANDLER = new Handler(); //尽量声明成static，避免内存泄漏

    public RequestCallbacks(IRequest request, ISuccess success, IError error, IFailure failure) {
        this.IRQUEST = request;
        this.ISUCCESS = success;
        this.IERROR = error;
        this.IFAILURE = failure;
    }

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if (response.isSuccessful()) {
            if (call.isExecuted()) {
                if (ISUCCESS != null) {
                    ISUCCESS.onSuccess(response.body());
                }
            }
        } else {
            if (IERROR != null) {
                IERROR.onError(response.code(), response.message());
            }
        }

        stopLoading();
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        if (IFAILURE != null) {
            IFAILURE.onFailure();
        }
        if (IRQUEST != null) {
            IRQUEST.onRequestEnd();
        }

        stopLoading();
    }

    public void stopLoading() {
//        if (LOADER_STYLE != null) {
//            //加延时
//            HANDLER.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                }
//            }, 1000);
//        }
    }
}
