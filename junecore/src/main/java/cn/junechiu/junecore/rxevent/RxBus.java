
package cn.junechiu.junecore.rxevent;

import androidx.annotation.NonNull;
import android.util.ArrayMap;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {

    private final ArrayMap<Object, RxBusEvent> rxBusEventArrayMap;

    private RxBus() {
        rxBusEventArrayMap = new ArrayMap<>();
    }

    public static RxBus getInstance() {
        return RxBusHolder.rxBus;
    }

    private static class RxBusHolder {
        private static final RxBus rxBus = new RxBus();
    }

    public boolean post(@NonNull Object obj) {
        return post(obj, obj);
    }

    public boolean post(@NonNull Object tag, @NonNull Object obj) {
        RxBusEvent rxBusEvent = rxBusEventArrayMap.get(tag);
        if (!RxUtils.isEmpty(rxBusEvent)) {
            rxBusEvent.subject.onNext(obj);
            return true;
        }
        return false;
    }

    /**
     * 接受消息
     *
     * @param tag 标志
     * @param callBack 回调
     */
    public <T> DisposableObserver register(@NonNull
    final Object tag, @NonNull
    final RxBusCallBack<T> callBack) {
        RxBusEvent rxBusEvent = rxBusEventArrayMap.get(tag);
        if (RxUtils.isEmpty(rxBusEvent)) {
            rxBusEvent = new RxBusEvent();
            rxBusEvent.subject = PublishSubject.create().toSerialized();
            rxBusEvent.disposable = rxBusEvent.subject.ofType(callBack.busOfType())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new RxBusObserver<T>() {
                        @Override
                        public void onError(@NonNull Throwable e) {
                            super.onError(e);
                            callBack.onBusError(e);
                        }

                        @Override
                        public void onNext(@NonNull T t) {
                            super.onNext(t);
                            callBack.onBusNext(t);
                        }
                    });
            rxBusEventArrayMap.put(tag, rxBusEvent);
        }
        return rxBusEvent.disposable;
    }

    /**
     * 取消订阅
     *
     * @param tag 标志
     * @return true 取消成功
     */
    public boolean unregister(@NonNull Object tag) {
        RxBusEvent rxBusEvent = rxBusEventArrayMap.get(tag);
        if (RxUtils.isEmpty(rxBusEvent)) {
            return true;
        }
        Subject<Object> subject = rxBusEvent.subject;
        Disposable disposable = rxBusEvent.disposable;
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        if (!subject.hasObservers()) {
            rxBusEventArrayMap.remove(tag);
            return true;
        }
        return false;
    }

    /**
     * 取消所有订阅
     *
     * @return 返回false 证明还有订阅没有被取消,注意内存泄漏...
     */
    public boolean unregisterAll() {
        for (Map.Entry<Object, RxBusEvent> objectRxBusEventEntry : rxBusEventArrayMap.entrySet()) {
            boolean unregister = unregister(objectRxBusEventEntry.getKey());
            if (!unregister) {
                return false;
            }
        }
        return true;
    }
}
