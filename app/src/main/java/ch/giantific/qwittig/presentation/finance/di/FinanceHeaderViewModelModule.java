/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModel;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the balance header view model and how to instantiate it.
 */
@Module
public class FinanceHeaderViewModelModule extends BaseViewModelModule {

    public FinanceHeaderViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    BalanceHeaderViewModel providesBalanceHeaderViewModel(@NonNull Navigator navigator,
                                                          @NonNull RxBus<Object> eventBus,
                                                          @NonNull UserRepository userRepository) {
        return new BalanceHeaderViewModelImpl(savedState, navigator, eventBus, userRepository);
    }
}
