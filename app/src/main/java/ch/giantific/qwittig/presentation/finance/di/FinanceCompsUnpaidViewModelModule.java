/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.finance.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.CompsUnpaidViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class FinanceCompsUnpaidViewModelModule extends BaseViewModelModule<CompsUnpaidViewModel.ViewListener> {

    public FinanceCompsUnpaidViewModelModule(@Nullable Bundle savedState,
                                             @NonNull CompsUnpaidViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    CompsUnpaidViewModel providesFinanceCompsUnpaidViewModel(@NonNull IdentityRepository identityRepository,
                                                             @NonNull UserRepository userRepository,
                                                             @NonNull CompensationRepository compsRepository) {
        return new CompsUnpaidViewModelImpl(mSavedState, mView, identityRepository,
                userRepository, compsRepository);
    }
}
