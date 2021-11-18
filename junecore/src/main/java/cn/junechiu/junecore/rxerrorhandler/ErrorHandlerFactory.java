package cn.junechiu.junecore.rxerrorhandler;

import android.content.Context;

/**
 * Created by android on 2018/3/23.
 */

public class ErrorHandlerFactory {

    public final String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private ResponseErrorListener mResponseErrorListener;

    public ErrorHandlerFactory(Context mContext, ResponseErrorListener mResponseErrorListener) {
        this.mResponseErrorListener = mResponseErrorListener;
        this.mContext = mContext;
    }

    /**
     * 处理错误
     *
     * @param throwable
     */
    public void handleError(Throwable throwable) {
        mResponseErrorListener.handleResponseError(mContext, throwable);
    }
}

