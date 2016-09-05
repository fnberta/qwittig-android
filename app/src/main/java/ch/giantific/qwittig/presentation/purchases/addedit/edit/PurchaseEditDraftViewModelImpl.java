/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import rx.Single;

/**
 * Provides an implementation of the {@link PurchaseEditDraftViewModel}.
 * <p/>
 * Subclass of {@link PurchaseEditViewModelImpl}.
 */
public class PurchaseEditDraftViewModelImpl extends PurchaseEditViewModelImpl
        implements PurchaseEditDraftViewModel {

    public PurchaseEditDraftViewModelImpl(@Nullable Bundle savedState,
                                          @NonNull Navigator navigator,
                                          @NonNull RxBus<Object> eventBus,
                                          @NonNull RemoteConfigHelper configHelper,
                                          @NonNull UserRepository userRepo,
                                          @NonNull GroupRepository groupRepo,
                                          @NonNull PurchaseRepository purchaseRepo,
                                          @NonNull String editPurchaseId) {
        super(savedState, navigator, eventBus, configHelper, userRepo,
                groupRepo, purchaseRepo, editPurchaseId);
    }

    @Override
    protected Single<Purchase> getPurchase() {
        return purchaseRepo.getDraft(editPurchaseId, currentIdentity.getId());
    }

    @Override
    boolean isDraft() {
        return true;
    }

    @Override
    protected void onPurchaseSaved(boolean asDraft) {
        navigator.finish(PurchaseResult.PURCHASE_DRAFT_CHANGES);
    }

    @Override
    public void onDeleteDraftMenuClick() {
        purchaseRepo.deleteDraft(editPurchaseId, editPurchase.getBuyer());
        navigator.finish(PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT_DELETED, editPurchaseId);
    }
}
