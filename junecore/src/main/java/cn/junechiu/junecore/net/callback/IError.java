package cn.junechiu.junecore.net.callback;

/**
 * Created by junzhao on 2017/12/2.
 * 网络请求错误回调
 */
public interface IError {

    void onError(int code, String msg);
}
