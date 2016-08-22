/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModel;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login/sign-up with email screen and how to
 * instantiate it.
 */
@Module
public class LoginEmailViewModelModule extends BaseViewModelModule {

    public LoginEmailViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginEmailViewModel providesLoginEmailViewModel(@NonNull Navigator navigator,
                                                    @NonNull RxBus<Object> eventBus,
                                                    @NonNull RemoteConfigHelper configHelper,
                                                    @NonNull UserRepository userRepository,
                                                    @NonNull GroupRepository groupRepository) {
        return new LoginEmailViewModelImpl(savedState, navigator, eventBus, configHelper,
                userRepository, groupRepository);
    }
}
