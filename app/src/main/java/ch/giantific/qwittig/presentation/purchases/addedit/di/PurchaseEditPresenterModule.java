/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditDraftPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit purchase screen and how to instantiate it.
 */
@Module
public class PurchaseEditPresenterModule extends BasePresenterModule {

    private final String editPurchaseId;

    public PurchaseEditPresenterModule(@Nullable Bundle savedState,
                                       @NonNull String editPurchaseId) {
        super(savedState);

        this.editPurchaseId = editPurchaseId;
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.Presenter providesPurchaseEditPresenter(@NonNull Navigator navigator,
                                                                    @NonNull UserRepository userRepo,
                                                                    @NonNull GroupRepository groupRepo,
                                                                    @NonNull PurchaseRepository purchaseRepo,
                                                                    @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseEditPresenter(savedState, navigator, userRepo, groupRepo, purchaseRepo,
                configHelper, editPurchaseId);
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.EditDraftPresenter providesPurchaseEditDraftPresenter(@NonNull Navigator navigator,
                                                                                  @NonNull UserRepository userRepo,
                                                                                  @NonNull GroupRepository groupRepo,
                                                                                  @NonNull PurchaseRepository purchaseRepo,
                                                                                  @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseEditDraftPresenter(savedState, navigator, userRepo, groupRepo,
                purchaseRepo, configHelper, editPurchaseId);
    }
}
