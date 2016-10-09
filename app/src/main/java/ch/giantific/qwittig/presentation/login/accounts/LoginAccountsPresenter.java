/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.accounts;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.usecases.AfterLoginUseCase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginAccountsContract}.
 */
public class LoginAccountsPresenter extends BasePresenterImpl<LoginAccountsContract.ViewListener>
        implements LoginAccountsContract.Presenter {

    private final LoginAccountsViewModel viewModel;
    private final AfterLoginUseCase afterLoginUseCase;

    @Inject
    public LoginAccountsPresenter(@NonNull Navigator navigator,
                                  @NonNull LoginAccountsViewModel viewModel,
                                  @NonNull UserRepository userRepo,
                                  @NonNull AfterLoginUseCase afterLoginUseCase) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.afterLoginUseCase = afterLoginUseCase;
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> loginResult,
                                   @NonNull final String workerTag,
                                   @LoginWorker.LoginType int type) {
        final String joinIdentityId = viewModel.getJoinIdentityId();
        afterLoginUseCase.setLoginResult(loginResult);
        afterLoginUseCase.setJoinIdentityId(joinIdentityId);
        subscriptions.add(afterLoginUseCase.execute()
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isUserNew) {
                        view.removeWorker(workerTag);

                        if (isUserNew) {
                            view.showProfileAdjust(!TextUtils.isEmpty(joinIdentityId));
                        } else {
                            navigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        viewModel.setLoading(false);

                        view.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        view.loadGoogleLoginWorker(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        viewModel.setLoading(false);
        view.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void onFacebookSignedIn(@NonNull String idToken) {
        viewModel.setLoading(true);
        view.loadFacebookLoginWorker(idToken);
    }

    @Override
    public void onFacebookLoginFailed() {
        viewModel.setLoading(false);
        view.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public View.OnClickListener getLoginGoogleClickListener() {
        return v -> {
            viewModel.setLoading(true);
            view.loginWithGoogle();
        };
    }

    @Override
    public void onUseEmailClick(View view) {
        this.view.showEmailLogin(viewModel.getJoinIdentityId());
    }
}
