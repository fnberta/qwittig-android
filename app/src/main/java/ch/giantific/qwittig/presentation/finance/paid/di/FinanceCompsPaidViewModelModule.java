/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the paid compensations view model and how to instantiate
 * it.
 */
@Module
public class FinanceCompsPaidViewModelModule extends BaseViewModelModule<CompsPaidViewModel.ViewListener> {

    public FinanceCompsPaidViewModelModule(@Nullable Bundle savedState,
                                           @NonNull CompsPaidViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    CompsPaidViewModel providesFinanceCompsPaidViewModel(@NonNull IdentityRepository identityRepository,
                                                         @NonNull UserRepository userRepository,
                                                         @NonNull CompensationRepository compsRepository) {
        return new CompsPaidViewModelImpl(mSavedState, mView, identityRepository,
                userRepository, compsRepository);
    }
}
