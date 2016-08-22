/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
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
public class FinanceCompsPaidViewModelModule extends BaseViewModelModule {

    private final String compGroupId;

    public FinanceCompsPaidViewModelModule(@Nullable Bundle savedState,
                                           @Nullable String compGroupId) {
        super(savedState);

        this.compGroupId = compGroupId;
    }

    @PerActivity
    @Provides
    CompsPaidViewModel providesFinanceCompsPaidViewModel(@NonNull Navigator navigator,
                                                         @NonNull RxBus<Object> eventBus,
                                                         @NonNull UserRepository userRepository,
                                                         @NonNull CompensationRepository compsRepository) {
        return new CompsPaidViewModelImpl(savedState, navigator, eventBus, userRepository,
                compsRepository, compGroupId);
    }
}
