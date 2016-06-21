/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the unpaid compensations view model and how to
 * instantiate it.
 */
@Module
public class FinanceCompsUnpaidViewModelModule extends BaseViewModelModule {

    public FinanceCompsUnpaidViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    CompsUnpaidViewModel providesFinanceCompsUnpaidViewModel(@NonNull RxBus<Object> eventBus,
                                                             @NonNull UserRepository userRepository,
                                                             @NonNull CompensationRepository compsRepository) {
        return new CompsUnpaidViewModelImpl(mSavedState, eventBus, userRepository, compsRepository);
    }
}
