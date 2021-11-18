package cn.junechiu.junecore.rxerrorhandler;

import android.content.Context;

/**
 * Created by android on 2018/3/23.
 */

public interface ResponseErrorListener {

    void handleResponseError(Context context, Throwable t);
}
