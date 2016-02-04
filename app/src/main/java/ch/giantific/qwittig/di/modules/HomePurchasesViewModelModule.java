/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class HomePurchasesViewModelModule extends BaseViewModelModule<HomePurchasesViewModel.ViewListener> {

    public HomePurchasesViewModelModule(@Nullable Bundle savedState,
                                        @NonNull HomePurchasesViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    HomePurchasesViewModel providesHomePurchasesViewModel(@NonNull GroupRepository groupRepository,
                                                          @NonNull UserRepository userRepository,
                                                          @NonNull PurchaseRepository purchaseRepository) {
        return new HomePurchasesViewModelImpl(mSavedState, mView, groupRepository, userRepository,
                purchaseRepository);
    }
}
