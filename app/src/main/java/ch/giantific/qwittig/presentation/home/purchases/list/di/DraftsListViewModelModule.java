/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the list of drafts screen and how to
 * instantiate it.
 */
@Module
public class DraftsListViewModelModule extends BaseViewModelModule<DraftsViewModel.ViewListener> {

    public DraftsListViewModelModule(@Nullable Bundle savedState,
                                     @NonNull DraftsViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    DraftsViewModel providesHomeDraftsViewModel(@NonNull RxBus<Object> eventBus,
                                                @NonNull UserRepository userRepository,
                                                @NonNull PurchaseRepository purchaseRepository) {
        return new DraftsViewModelImpl(mSavedState, mView, eventBus, userRepository, purchaseRepository);
    }
}
