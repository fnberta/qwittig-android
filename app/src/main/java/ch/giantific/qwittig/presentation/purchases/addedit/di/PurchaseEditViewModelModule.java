/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditDraftViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditDraftViewModelImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit purchase screen and how to instantiate it.
 */
@Module
public class PurchaseEditViewModelModule extends BaseViewModelModule {

    private final String editPurchaseId;

    public PurchaseEditViewModelModule(@Nullable Bundle savedState,
                                       @NonNull String editPurchaseId) {
        super(savedState);

        this.editPurchaseId = editPurchaseId;
    }

    @PerActivity
    @Provides
    PurchaseAddEditViewModel providesPurchaseEditViewModel(@NonNull Navigator navigator,
                                                           @NonNull RxBus<Object> eventBus,
                                                           @NonNull RemoteConfigHelper configHelper,
                                                           @NonNull UserRepository userRepository,
                                                           @NonNull GroupRepository groupRepository,
                                                           @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseEditViewModelImpl(savedState, navigator, eventBus, configHelper,
                userRepository, groupRepository, purchaseRepository, editPurchaseId);
    }

    @PerActivity
    @Provides
    PurchaseEditDraftViewModel providesPurchaseEditDraftViewModel(@NonNull Navigator navigator,
                                                                  @NonNull RxBus<Object> eventBus,
                                                                  @NonNull RemoteConfigHelper configHelper,
                                                                  @NonNull UserRepository userRepository,
                                                                  @NonNull GroupRepository groupRepository,
                                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseEditDraftViewModelImpl(savedState, navigator, eventBus, configHelper,
                userRepository, groupRepository, purchaseRepository, editPurchaseId);
    }
}
