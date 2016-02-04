/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.BaseObservable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides an abstract base implementation of the {@link ViewModel} specification.
 * <p/>
 * Subclass of {@link BaseObservable} to make fields observable for data binding.
 */
public abstract class ViewModelBaseImpl<T extends ViewModel.ViewListener>
        extends BaseObservable
        implements ViewModel {

    User mCurrentUser;
    Group mCurrentGroup;
    T mView;
    CompositeSubscription mSubscriptions;
    UserRepository mUserRepo;

    public ViewModelBaseImpl(@Nullable Bundle savedState, @NonNull T view,
                             @NonNull UserRepository userRepository) {
        mView = view;
        mUserRepo = userRepository;
        updateCurrentUserAndGroup();
    }

    @CallSuper
    void updateCurrentUserAndGroup() {
        mCurrentUser = mUserRepo.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        // Empty default implementation
    }

    @Override
    @CallSuper
    public void onStart() {
        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
            mSubscriptions = new CompositeSubscription();
        }
    }

    @Override
    @CallSuper
    public void onNewGroupSet() {
        updateCurrentUserAndGroup();
    }

    @Override
    public void onStop() {
        if (mSubscriptions.hasSubscriptions()) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    @CallSuper
    public void onWorkerError(@NonNull String workerTag) {
        mView.removeWorker(workerTag);
        mView.showMessage(R.string.toast_unknown_error);
    }

    final boolean isUserInGroup() {
        return mCurrentUser != null && mCurrentGroup != null;
    }
}
