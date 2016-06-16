/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the implementation to use for the help and feedback view model and how to instantiate it.
 */
@Module
public class HelpFeedbackViewModelModule extends BaseViewModelModule<HelpFeedbackViewModel.ViewListener> {

    public HelpFeedbackViewModelModule(@Nullable Bundle savedState,
                                       @NonNull HelpFeedbackViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    HelpFeedbackViewModel providesHelpFeedbackViewModel(@NonNull RxBus<Object> eventBus,
                                                        @NonNull UserRepository userRepository) {
        return new HelpFeedbackViewModelImpl(mSavedState, mView, eventBus, userRepository);
    }
}
