/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.BaseObservable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.bus.events.EventIdentitySelected;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides an abstract base implementation of the {@link ViewModel} specification.
 * <p/>
 * Subclass of {@link BaseObservable} to make fields observable for data binding.
 */
public abstract class ViewModelBaseImpl<T extends ViewModel.ViewListener>
        extends BaseObservable
        implements ViewModel {

    protected final T mView;
    protected final UserRepository mUserRepo;
    protected final RxBus<Object> mEventBus;
    protected User mCurrentUser;
    protected Identity mCurrentIdentity;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    public ViewModelBaseImpl(@Nullable Bundle savedState, @NonNull T view,
                             @NonNull RxBus<Object> eventBus,
                             @NonNull UserRepository userRepository) {
        mView = view;
        mEventBus = eventBus;
        mUserRepo = userRepository;
        mCurrentUser = mUserRepo.getCurrentUser();
        setCurrentIdentity();
    }

    protected void setCurrentIdentity() {
        if (mCurrentUser != null) {
            mCurrentIdentity = mCurrentUser.getCurrentIdentity();
        }
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        // Empty default implementation
    }

    protected CompositeSubscription getSubscriptions() {
        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
            mSubscriptions = new CompositeSubscription();
        }

        return mSubscriptions;
    }

    @Override
    @CallSuper
    public void onViewVisible() {
        setCurrentIdentity();

        getSubscriptions().add(mEventBus.observeEvents(EventIdentitySelected.class)
                .subscribe(new Action1<EventIdentitySelected>() {
                    @Override
                    public void call(EventIdentitySelected eventIdentitySelected) {
                        onIdentitySelected(eventIdentitySelected.getIdentity());
                    }
                })
        );
    }

    @CallSuper
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        mCurrentIdentity = identitySelected;
    }

    @Override
    public void onViewGone() {
        if (mSubscriptions.hasSubscriptions()) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    @CallSuper
    public void onWorkerError(@NonNull String workerTag) {
        mView.removeWorker(workerTag);
        mView.showMessage(R.string.toast_error_unknown);
    }
}
