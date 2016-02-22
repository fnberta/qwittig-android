/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class DraftsListViewModelModule extends BaseViewModelModule<DraftsViewModel.ViewListener> {

    public DraftsListViewModelModule(@Nullable Bundle savedState,
                                     @NonNull DraftsViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    DraftsViewModel providesHomeDraftsViewModel(@NonNull IdentityRepository identityRepository,
                                                @NonNull UserRepository userRepository,
                                                @NonNull PurchaseRepository purchaseRepository) {
        return new DraftsViewModelImpl(mSavedState, mView, identityRepository, userRepository,
                purchaseRepository);
    }
}
