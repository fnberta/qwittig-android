/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.SharedPrefsHelper;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.camera.CameraViewModel;
import ch.giantific.qwittig.presentation.camera.CameraViewModelImpl;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class CameraViewModelModule extends BaseViewModelModule {

    public CameraViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    CameraViewModel providesCameraViewModel(@NonNull Navigator navigator,
                                            @NonNull RxBus<Object> eventBus,
                                            @NonNull SharedPrefsHelper prefsHelper,
                                            @NonNull UserRepository userRepository) {
        return new CameraViewModelImpl(savedState, navigator, eventBus, prefsHelper, userRepository);
    }
}
