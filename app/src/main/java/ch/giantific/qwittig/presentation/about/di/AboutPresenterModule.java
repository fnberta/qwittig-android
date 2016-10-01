/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.about.AboutContract;
import ch.giantific.qwittig.presentation.about.AboutPresenter;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class AboutPresenterModule extends BasePresenterModule {

    public AboutPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    AboutContract.Presenter providesAboutPresenter(@NonNull Navigator navigator,
                                                   @NonNull UserRepository userRepo) {
        return new AboutPresenter(savedState, navigator, userRepo);
    }
}
