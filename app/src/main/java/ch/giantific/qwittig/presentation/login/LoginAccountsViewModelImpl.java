/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.usecases.AfterLoginUseCase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginAccountsViewModel}.
 */
public class LoginAccountsViewModelImpl extends ViewModelBaseImpl<LoginAccountsViewModel.ViewListener>
        implements LoginAccountsViewModel {

    private static final String STATE_IDENTITY_ID = "STATE_IDENTITY_ID";

    private final AfterLoginUseCase afterLoginUseCase;
    private String joinIdentityId;

    public LoginAccountsViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepo,
                                      @NonNull AfterLoginUseCase afterLoginUseCase) {
        super(savedState, navigator, eventBus, userRepo);

        this.afterLoginUseCase = afterLoginUseCase;

        if (savedState != null) {
            joinIdentityId = savedState.getString(STATE_IDENTITY_ID, "");
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(joinIdentityId)) {
            outState.putString(STATE_IDENTITY_ID, joinIdentityId);
        }
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> loginResult,
                                   @NonNull final String workerTag,
                                   @LoginWorker.LoginType int type) {
        afterLoginUseCase.setLoginResult(loginResult);
        afterLoginUseCase.setJoinIdentityId(joinIdentityId);
        getSubscriptions().add(afterLoginUseCase.execute()
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isUserNew) {
                        view.removeWorker(workerTag);

                        if (isUserNew) {
                            view.showProfileFragment(!TextUtils.isEmpty(joinIdentityId));
                        } else {
                            navigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        setLoading(false);

                        view.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    @Override
    public void setInvitationIdentityId(@NonNull String identityId) {
        this.joinIdentityId = identityId;
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        view.loadGoogleLoginWorker(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        setLoading(false);
        view.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void onFacebookSignedIn(@NonNull String idToken) {
        setLoading(true);
        view.loadFacebookLoginWorker(idToken);
    }

    @Override
    public void onFacebookLoginFailed() {
        setLoading(false);
        view.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public View.OnClickListener getLoginGoogleClickListener() {
        return v -> {
            setLoading(true);
            view.loginWithGoogle();
        };
    }

    @Override
    public void onUseEmailClick(View view) {
        this.view.showEmailFragment(joinIdentityId);
    }
}
