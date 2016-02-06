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
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModelAddImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseAddViewModelModule extends BaseViewModelModule<PurchaseAddEditViewModel.ViewListener> {

    public PurchaseAddViewModelModule(@Nullable Bundle savedState,
                                      @NonNull PurchaseAddEditViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddViewModel(@NonNull GroupRepository groupRepository,
                                                          @NonNull UserRepository userRepository,
                                                          @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelAddImpl(mSavedState, mView, groupRepository,
                userRepository, purchaseRepository);
    }
}
