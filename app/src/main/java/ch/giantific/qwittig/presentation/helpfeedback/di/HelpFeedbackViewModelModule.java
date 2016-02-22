/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class HelpFeedbackViewModelModule extends BaseViewModelModule<HelpFeedbackViewModel.ViewListener> {

    public HelpFeedbackViewModelModule(@Nullable Bundle savedState,
                                       @NonNull HelpFeedbackViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    HelpFeedbackViewModel providesHelpFeedbackViewModel(@NonNull UserRepository userRepository) {
        return new HelpFeedbackViewModelImpl(mSavedState, mView, userRepository);
    }
}
