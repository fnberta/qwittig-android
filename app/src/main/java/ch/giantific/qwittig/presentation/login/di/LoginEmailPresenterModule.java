/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.usecases.AfterLoginUseCase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.login.email.LoginEmailContract;
import ch.giantific.qwittig.presentation.login.email.LoginEmailPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login/sign-up with email screen and how to
 * instantiate it.
 */
@Module
public class LoginEmailPresenterModule extends BasePresenterModule {

    public LoginEmailPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginEmailContract.Presenter providesLoginEmailPresenter(@NonNull Navigator navigator,
                                                             @NonNull UserRepository userRepo,
                                                             @NonNull AfterLoginUseCase afterLoginUseCase) {
        return new LoginEmailPresenter(savedState, navigator, userRepo, afterLoginUseCase);
    }
}
