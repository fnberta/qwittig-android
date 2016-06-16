/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesViewModel;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the identities view model and how to instantiate it.
 */
@Module
public class FinanceIdentitiesViewModelModule extends BaseViewModelModule<IdentitiesViewModel.ViewListener> {

    public FinanceIdentitiesViewModelModule(@Nullable Bundle savedState,
                                            @NonNull IdentitiesViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    IdentitiesViewModel providesFinanceUsersViewModel(@NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository) {
        return new IdentitiesViewModelImpl(mSavedState, mView, eventBus, userRepository);
    }
}
