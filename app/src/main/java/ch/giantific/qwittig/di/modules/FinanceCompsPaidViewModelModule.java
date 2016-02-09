/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.finance.FinanceCompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.FinanceCompsPaidViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class FinanceCompsPaidViewModelModule extends BaseViewModelModule<FinanceCompsPaidViewModel.ViewListener> {

    public FinanceCompsPaidViewModelModule(@Nullable Bundle savedState,
                                           @NonNull FinanceCompsPaidViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    FinanceCompsPaidViewModel providesFinanceCompsPaidViewModel(@NonNull IdentityRepository identityRepository,
                                                                @NonNull UserRepository userRepository,
                                                                @NonNull CompensationRepository compsRepository) {
        return new FinanceCompsPaidViewModelImpl(mSavedState, mView, identityRepository,
                userRepository, compsRepository);
    }
}
