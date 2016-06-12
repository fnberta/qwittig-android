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
import android.text.TextUtils;
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
    private static final String STATE_IDENTITY_ID = "STATE_IDENTITY_ID";
    private boolean mLoading;
    private String mIdentityId;

    public LoginAccountsViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull LoginAccountsViewModel.ViewListener view,
                                      @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        if (savedState != null) {
            mLoading = savedState.getBoolean(STATE_LOADING);
            mIdentityId = savedState.getString(STATE_IDENTITY_ID, "");
        } else {
            mLoading = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
        if (!TextUtils.isEmpty(mIdentityId)) {
            outState.putString(STATE_IDENTITY_ID, mIdentityId);
        }
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
                    public void onSuccess(User user) {
                        mView.removeWorker(workerTag);
                        if (user.isNew()) {
                            mView.showProfileFragment(!TextUtils.isEmpty(mIdentityId));
                        } else {
                            mView.finishScreen(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

                        mView.showMessage(mUserRepo.getErrorMessage(error));
                    }
                })
        );
    }

    @Override
    public void setInvitationIdentityId(@NonNull String identityId) {
        mIdentityId = identityId;
    }

    @Override
    public void onGoogleSignedIn(@Nullable String tokenId, @Nullable String displayName,
                                 @Nullable Uri photoUrl) {
        mView.loadGoogleTokenVerifyWorker(tokenId, displayName, photoUrl, mIdentityId);
    }

    @Override
    public void onGoogleLoginFailed() {
        setLoading(false);
        mView.showMessage(R.string.toast_login_failed_google);
    }

    @Override
    public void onLoginFacebookClick(View view) {
        setLoading(true);
        mView.loadFacebookLoginWorker(mIdentityId);
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
        mView.showEmailFragment(mIdentityId);
    }
}
