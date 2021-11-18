
package cn.junechiu.junecore.rxevent;

public interface RxBusCallBack<T> {
    void onBusNext(T t);

    void onBusError(Throwable throwable);

    Class<T> busOfType();
}
