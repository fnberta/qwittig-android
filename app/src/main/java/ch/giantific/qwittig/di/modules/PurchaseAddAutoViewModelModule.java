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
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModelAddAutoImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseAddAutoViewModelModule extends BaseViewModelModule<PurchaseAddEditViewModel.ViewListener> {

    public PurchaseAddAutoViewModelModule(@Nullable Bundle savedState,
                                          @NonNull PurchaseAddEditViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddAutoViewModel(@NonNull GroupRepository groupRepository,
                                                              @NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelAddAutoImpl(mSavedState, mView, groupRepository,
                userRepository, purchaseRepository);
    }
}
