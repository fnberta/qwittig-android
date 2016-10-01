/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.SharedPrefsHelper;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.camera.CameraContract;
import ch.giantific.qwittig.presentation.camera.CameraPresenter;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class CameraPresenterModule extends BasePresenterModule {

    public CameraPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    CameraContract.Presenter providesCameraPresenter(@NonNull Navigator navigator,
                                                     @NonNull SharedPrefsHelper prefsHelper,
                                                     @NonNull UserRepository userRepo) {
        return new CameraPresenter(savedState, navigator, prefsHelper, userRepo);
    }
}
