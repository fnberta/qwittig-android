/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModel;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class LoginEmailViewModelModule extends BaseViewModelModule<LoginEmailViewModel.ViewListener> {

    public LoginEmailViewModelModule(@Nullable Bundle savedState,
                                     @NonNull LoginEmailViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    LoginEmailViewModel providesLoginEmailViewModel(@NonNull UserRepository userRepository) {
        return new LoginEmailViewModelImpl(mSavedState, mView, userRepository);
    }
}
