/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class HelpFeedbackViewModelModule extends BaseViewModelModule {

    public HelpFeedbackViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    HelpFeedbackViewModel providesHelpFeedbackViewModel(@NonNull Navigator navigator,
                                                        @NonNull RxBus<Object> eventBus,
                                                        @NonNull UserRepository userRepository) {
        return new HelpFeedbackViewModelImpl(mSavedState, navigator, eventBus, userRepository);
    }
}
