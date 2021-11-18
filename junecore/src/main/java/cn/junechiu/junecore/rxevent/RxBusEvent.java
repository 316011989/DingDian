
package cn.junechiu.junecore.rxevent;

import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.Subject;

/**
 * Created by android on 2017/8/17.
 */

public class RxBusEvent {
    Subject<Object> subject;

    DisposableObserver disposable;

    RxBusEvent() {
    }
}
