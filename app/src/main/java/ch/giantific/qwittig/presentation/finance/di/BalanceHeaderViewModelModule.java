/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModel;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the balance header view model and how to instantiate it.
 */
@Module
public class BalanceHeaderViewModelModule extends BaseViewModelModule<BalanceHeaderViewModel.ViewListener> {

    public BalanceHeaderViewModelModule(@Nullable Bundle savedState,
                                        @NonNull BalanceHeaderViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    BalanceHeaderViewModel providesBalanceHeaderViewModel(@NonNull UserRepository userRepository) {
        return new BalanceHeaderViewModelImpl(mSavedState, mView, userRepository);
    }
}
