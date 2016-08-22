package ch.giantific.qwittig.presentation.common;

import android.support.annotation.CallSuper;

import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by fabio on 08.08.16.
 */
public abstract class IndefiniteSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {
        // never called, do nothing
    }

    @Override
    @CallSuper
    public void onError(Throwable e) {
        Timber.e(e, "subscription failed with error:");
    }
}
