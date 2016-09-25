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
    protected final UserRepository userRepo;
    protected final Navigator navigator;
    protected final RxBus<Object> eventBus;
    protected T view;
    protected boolean loading;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public ViewModelBaseImpl(@Nullable Bundle savedState,
                             @NonNull Navigator navigator,
                             @NonNull RxBus<Object> eventBus,
                             @NonNull UserRepository userRepo) {
        this.navigator = navigator;
        this.eventBus = eventBus;
        this.userRepo = userRepo;

        //noinspection SimplifiableIfStatement
        if (savedState != null) {
            loading = savedState.getBoolean(STATE_LOADING, false);
        } else {
            loading = false;
        }
    }

    @Override
    public void attachView(@NonNull T view) {
        this.view = view;
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_LOADING, loading);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
    }

    protected final CompositeSubscription getSubscriptions() {
        if (subscriptions == null || subscriptions.isUnsubscribed()) {
            subscriptions = new CompositeSubscription();
        }

        return subscriptions;
    }

    @Override
    public final void onViewVisible() {
        getSubscriptions().add(userRepo.observeAuthStatus()
                .subscribe(new IndefiniteSubscriber<FirebaseUser>() {
                    @Override
                    public void onNext(FirebaseUser currentUser) {
                        if (currentUser != null) {
                            onUserLoggedIn(currentUser);
                        } else {
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
        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }
    }

    @Override
    public final void onWorkerError(@NonNull String workerTag) {
        view.removeWorker(workerTag);
        view.showMessage(R.string.toast_error_unknown);
    }
}
