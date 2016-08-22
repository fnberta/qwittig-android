/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the list of drafts screen and how to
 * instantiate it.
 */
@Module
public class DraftsListViewModelModule extends BaseViewModelModule {

    public DraftsListViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    DraftsViewModel providesHomeDraftsViewModel(@NonNull Navigator navigator,
                                                @NonNull RxBus<Object> eventBus,
                                                @NonNull UserRepository userRepository,
                                                @NonNull PurchaseRepository purchaseRepository) {
        return new DraftsViewModelImpl(savedState, navigator, eventBus, userRepository,
                purchaseRepository);
    }
}
