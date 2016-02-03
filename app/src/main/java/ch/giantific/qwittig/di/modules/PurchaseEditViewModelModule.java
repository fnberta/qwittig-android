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
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModelEditImpl;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseEditDraftViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseEditDraftViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseEditViewModelModule extends BaseViewModelModule {

    private String mEditPurchaseId;

    public PurchaseEditViewModelModule(@Nullable Bundle savedState, @NonNull String editPurchaseId) {
        super(savedState);

        mEditPurchaseId = editPurchaseId;
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseEditViewModel(@NonNull GroupRepository groupRepository,
                                                           @NonNull UserRepository userRepository,
                                                           @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelEditImpl(mSavedState, groupRepository, userRepository,
                purchaseRepository, mEditPurchaseId);
    }

    @PerFragment
    @Provides
    PurchaseEditDraftViewModel providesPurchaseEditDraftViewModel(@NonNull GroupRepository groupRepository,
                                                                  @NonNull UserRepository userRepository,
                                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseEditDraftViewModelImpl(mSavedState, groupRepository, userRepository,
                purchaseRepository, mEditPurchaseId);
    }
}
