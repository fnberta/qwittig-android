/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides an abstract base implementation of the {@link ViewModel} specification.
 * <p/>
 * Subclass of {@link BaseObservable} to make fields observable for data binding.
 */
public abstract class ViewModelBaseImpl<T extends ViewModel.ViewListener>
        extends BaseObservable implements ViewModel<T> {

    private static final String STATE_LOADING = "STATE_LOADING";
    protected final UserRepository mUserRepo;
    protected final Navigator mNavigator;
    protected final RxBus<Object> mEventBus;
    protected T mView;
    protected boolean mLoading;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private int mAuthStateCounter = 0;

    public ViewModelBaseImpl(@Nullable Bundle savedState,
                             @NonNull Navigator navigator,
                             @NonNull RxBus<Object> eventBus,
                             @NonNull UserRepository userRepository) {
        mNavigator = navigator;
        mEventBus = eventBus;
        mUserRepo = userRepository;

        if (savedState != null) {
            mLoading = savedState.getBoolean(STATE_LOADING, false);
        } else {
            mLoading = false;
        }
    }

    @Override
    public void attachView(@NonNull T view) {
        mView = view;
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    protected final CompositeSubscription getSubscriptions() {
        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
            mSubscriptions = new CompositeSubscription();
        }

        return mSubscriptions;
    }

    @Override
    public final void onViewVisible() {
        mAuthStateCounter = 0;
        getSubscriptions().add(mUserRepo.observeAuthStatus()
                .subscribe(new IndefiniteSubscriber<FirebaseUser>() {
                    @Override
                    public void onNext(FirebaseUser currentUser) {
                        if (currentUser != null) {
                            if (mAuthStateCounter == 0) {
                                onUserLoggedIn(currentUser);
                                mAuthStateCounter++;
                            }
                        } else {
                            mAuthStateCounter = 0;
                            onUserNotLoggedIn();
                        }
                    }
                })
        );
    }

    @CallSuper
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        // empty default implementation
    }

    @CallSuper
    protected void onUserNotLoggedIn() {
        // empty default implementation
    }

    @Override
    public final void onViewGone() {
        if (mSubscriptions.hasSubscriptions()) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public final void onWorkerError(@NonNull String workerTag) {
        mView.removeWorker(workerTag);
        mView.showMessage(R.string.toast_error_unknown);
    }
}
