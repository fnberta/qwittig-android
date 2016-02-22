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
import ch.giantific.qwittig.presentation.login.LoginAccountsViewModel;
import ch.giantific.qwittig.presentation.login.LoginAccountsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class LoginAccountsViewModelModule extends BaseViewModelModule<LoginAccountsViewModel.ViewListener> {

    public LoginAccountsViewModelModule(@Nullable Bundle savedState,
                                        @NonNull LoginAccountsViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    LoginAccountsViewModel providesLoginAccountsViewModel(@NonNull UserRepository userRepository) {
        return new LoginAccountsViewModelImpl(mSavedState, mView, userRepository);
    }
}
