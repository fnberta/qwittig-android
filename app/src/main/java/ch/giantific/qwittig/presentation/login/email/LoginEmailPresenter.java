/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.email;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.usecases.AfterLoginUseCase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.login.LoginWorker.LoginType;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginEmailContract}.
 */
public class LoginEmailPresenter extends BasePresenterImpl<LoginEmailContract.ViewListener>
        implements LoginEmailContract.Presenter {

    private static final String STATE_VIEW_MODEL = LoginEmailViewModel.class.getCanonicalName();
    private final LoginEmailViewModel viewModel;
    private final AfterLoginUseCase afterLoginUseCase;
    private String joinIdentityId;

    public LoginEmailPresenter(@Nullable Bundle savedState,
                               @NonNull Navigator navigator,
                               @NonNull UserRepository userRepo,
                               @NonNull AfterLoginUseCase afterLoginUseCase) {
        super(savedState, navigator, userRepo);

        this.afterLoginUseCase = afterLoginUseCase;

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new LoginEmailViewModel(false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public LoginEmailViewModel getViewModel() {
        return viewModel;
    }

    public void setJoinIdentityId(@NonNull String joinIdentityId) {
        this.joinIdentityId = joinIdentityId;
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> loginResult,
                                   @NonNull final String workerTag,
                                   @LoginType final int type) {
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
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        subscriptions.add(single.subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        view.removeWorker(workerTag);
                        viewModel.setLoading(false);

                        view.showMessage(R.string.toast_password_reset);
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
    public void onLoginClick(View view) {
        if (viewModel.isInputValid(false)) {
            viewModel.setLoading(true);
            this.view.loadEmailLoginWorker(viewModel.email.get(), viewModel.password.get());
        }
    }

    @Override
    public void onSignUpClick(View view) {
        if (!viewModel.isSignUp()) {
            viewModel.setSignUp(true);
            return;
        }

        if (viewModel.isInputValid(true)) {
            viewModel.setLoading(true);
            this.view.hideKeyboard();
            this.view.loadEmailSignUpWorker(viewModel.email.get(), viewModel.password.get());
        }
    }

    @Override
    public void onResetPasswordClick(View view) {
        this.view.showResetPasswordDialog(viewModel.email.get());
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        view.loadResetPasswordWorker(email);
    }
}
