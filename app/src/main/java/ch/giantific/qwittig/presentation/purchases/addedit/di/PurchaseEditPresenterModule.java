/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditDraftPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit purchase screen and how to instantiate it.
 */
@Module
public class PurchaseEditPresenterModule {

    private final String editPurchaseId;

    public PurchaseEditPresenterModule(@NonNull String editPurchaseId) {
        this.editPurchaseId = editPurchaseId;
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.Presenter providesPurchaseEditPresenter(@NonNull Navigator navigator,
                                                                    @NonNull PurchaseAddEditViewModel viewModel,
                                                                    @NonNull UserRepository userRepo,
                                                                    @NonNull GroupRepository groupRepo,
                                                                    @NonNull PurchaseRepository purchaseRepo,
                                                                    @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseEditPresenter(navigator, viewModel, userRepo, groupRepo, purchaseRepo,
                configHelper, editPurchaseId);
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.EditDraftPresenter providesPurchaseEditDraftPresenter(@NonNull Navigator navigator,
                                                                                  @NonNull PurchaseAddEditViewModel viewModel,
                                                                                  @NonNull UserRepository userRepo,
                                                                                  @NonNull GroupRepository groupRepo,
                                                                                  @NonNull PurchaseRepository purchaseRepo,
                                                                                  @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseEditDraftPresenter(navigator, viewModel, userRepo, groupRepo,
                purchaseRepo, configHelper, editPurchaseId);
    }
}
