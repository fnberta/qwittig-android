/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.DaggerWorkerComponent;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;

/**
 * Provides an abstract class for a so-called headless {@link Fragment}, which does not contain
 * any UI elements and is retained across configuration changes. It is useful for encapsulating
 * background tasks.
 */
public abstract class BaseWorker<T, S extends BaseWorkerListener> extends Fragment {

    private final ReplaySubject<T> mSubject = ReplaySubject.create();
    protected S mActivity;
    @Inject
    protected UserRepository mUserRepo;
    private Subscription mSubscription;

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

        // inject dependencies
        final WorkerComponent component = DaggerWorkerComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .build();
        injectWorkerDependencies(component);

        final Observable<T> observable = getObservable(getArguments());
        if (observable != null) {
            mSubscription = observable.subscribe(mSubject);
        } else {
            onError();
        }
    }

    protected abstract void injectWorkerDependencies(@NonNull WorkerComponent component);

    @Override
    public void onStart() {
        super.onStart();

        setStream(mSubject.asObservable());
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

    @Nullable
    protected abstract Observable<T> getObservable(@NonNull Bundle args);

    protected abstract void onError();

    protected abstract void setStream(@NonNull Observable<T> observable);
}