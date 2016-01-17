/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Provides an abstract class for a so-called headless {@link Fragment}, which does not contain
 * any UI elements and is retained across configuration changes. It is useful for encapsulating
 * background tasks.
 */
public abstract class BaseWorker<T, S extends BaseWorkerListener> extends Fragment {

    protected S mActivity;
    private Subscription mSubscription;
    private PublishSubject<T> mSubject = PublishSubject.create();

    public BaseWorker() {
        // empty default constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (S) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BaseWorkerListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        final Bundle args = getArguments();
        if (args == null) {
            mActivity.onWorkerError(getWorkerTag());
            return;
        }

        final Observable<T> observable = getObservable(args);
        if (observable != null) {
            mSubscription = observable.subscribe(mSubject);
        } else {
            mActivity.onWorkerError(getWorkerTag());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setStream(mSubject.asObservable(), getWorkerTag());
    }


    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    protected abstract String getWorkerTag();

    @Nullable
    protected abstract Observable<T> getObservable(@NonNull Bundle args);

    protected abstract void setStream(@NonNull Observable<T> observable, @NonNull String workerTag);
}
