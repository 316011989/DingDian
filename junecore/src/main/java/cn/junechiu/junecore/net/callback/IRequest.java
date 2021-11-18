package cn.junechiu.junecore.net.callback;

/**
 * Created by junzhao on 2017/12/2.
 * 请求开始、结束回调
 */

public interface IRequest {

    void onRequestStart();

    void onRequestEnd();
}
