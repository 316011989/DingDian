
package cn.junechiu.junecore.rxevent;

import androidx.annotation.NonNull;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by android on 2017/8/17.
 */

class RxBusObserver<T> extends DisposableObserver<T> {
    @Override
    public void onNext(@NonNull T t) {

    }

    @Override
    public void onError(@NonNull Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
