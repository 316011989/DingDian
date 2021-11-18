package cn.junechiu.junecore.rxerrorhandler;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by android on 2018/3/23.
 */

public class RetryWithDelay implements
        Function<Observable<Throwable>, ObservableSource<?>> {

    public final String TAG = this.getClass().getSimpleName();
    private final int maxRetries;
    private final int retryDelaySecond;
    private int retryCount;

    public RetryWithDelay(int maxRetries, int retryDelaySecond) {
        this.maxRetries = maxRetries;
        this.retryDelaySecond = retryDelaySecond;
    }

    @Override
    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {
        return throwableObservable
                .flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    if (++retryCount <= maxRetries) {
                        // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                        Log.d(TAG, "Observable get error, it will try after " + retryDelaySecond
                                + " second, retry count " + retryCount);
                        return Observable.timer(retryDelaySecond,
                                TimeUnit.SECONDS);
                    }
                    // Max retries hit. Just pass the error along.
                    return Observable.error(throwable);
                });
    }
}

