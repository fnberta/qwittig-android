/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.about.AboutViewModel;
import ch.giantific.qwittig.presentation.about.AboutViewModelImpl;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class AboutViewModelModule extends BaseViewModelModule {

    public AboutViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    AboutViewModel providesAboutViewModel(@NonNull Navigator navigator,
                                          @NonNull RxBus<Object> eventBus,
                                          @NonNull UserRepository userRepository) {
        return new AboutViewModelImpl(mSavedState, navigator, eventBus, userRepository);
    }
}
