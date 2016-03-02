/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginAccountsViewModel}.
 */
public class LoginAccountsViewModelImpl extends ViewModelBaseImpl<LoginAccountsViewModel.ViewListener>
        implements LoginAccountsViewModel {

    private static final String STATE_LOADING = "STATE_LOADING";
    private boolean mLoading;

    public LoginAccountsViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull LoginAccountsViewModel.ViewListener view,
                                      @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        mLoading = savedState != null && savedState.getBoolean(STATE_LOADING);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean isLoading) {
        mLoading = isLoading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    public void setUserLoginStream(@NonNull Single<User> single, @NonNull final String workerTag,
                                   @LoginWorker.Type int type) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User value) {
                mView.removeWorker(workerTag);
                mView.finishScreen(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                setLoading(false);

                mView.showMessage(mUserRepo.getErrorMessage(error));
            }
        }));
    }

    @Override
    public void onGoogleSignedIn(@Nullable String tokenId, @Nullable String displayName,
                                 @Nullable Uri photoUrl) {
        mView.loadGoogleTokenVerifyWorker(tokenId, displayName, photoUrl);
    }

    @Override
    public void onGoogleLoginFailed() {
        mView.showMessage(R.string.toast_login_failed_google);
    }

    @Override
    public void onLoginFacebookClick(View view) {
        setLoading(true);
        mView.loadFacebookLoginWorker();
    }

    @Override
    public View.OnClickListener getLoginGoogleClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                mView.loginWithGoogle();
            }
        };
    }

    @Override
    public void onUseEmailClick(View view) {
        mView.showEmailFragment();
    }
}
