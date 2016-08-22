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
import ch.giantific.qwittig.presentation.login.LoginFirstGroupViewModel;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginFirstGroupViewModelModule extends BaseViewModelModule {

    public LoginFirstGroupViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginFirstGroupViewModel providesLoginFirstGroupViewModel(@NonNull Navigator navigator,
                                                              @NonNull RxBus<Object> eventBus,
                                                              @NonNull RemoteConfigHelper configHelper,
                                                              @NonNull UserRepository userRepository,
                                                              @NonNull GroupRepository groupRepository) {
        return new LoginFirstGroupViewModelImpl(savedState, navigator, eventBus, configHelper,
                userRepository, groupRepository);
    }
}
