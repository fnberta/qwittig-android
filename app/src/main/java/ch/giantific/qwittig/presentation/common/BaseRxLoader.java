/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

/**
 * Provides an abstract base class for a loader that returns a RxJava {@link Observable} as a
 * result.
 */
public abstract class BaseRxLoader<T> extends Loader<Observable<T>> {

    private Subscription subscription;
    private ReplaySubject<T> subject = ReplaySubject.create();

    public BaseRxLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (takeContentChanged() || !subject.hasValue()) {
            forceLoad();
        } else {
            deliverResult(subject.asObservable());
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        final Observable<T> observable = getObservable();
        subscription = observable.subscribe(subject);
        deliverResult(subject.asObservable());
    }

    @NonNull
    protected abstract Observable<T> getObservable();

    @Override
    public void deliverResult(Observable<T> observable) {
        if (isStarted()) {
            super.deliverResult(observable);
        }
    }

    @Override
    protected boolean onCancelLoad() {
        if (subscription != null) {
            subscription.unsubscribe();
            return true;
        }

        return false;
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        if (subject != null) {
            subject = ReplaySubject.create();
        }
    }
}
