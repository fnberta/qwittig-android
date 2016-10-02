/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract.PurchaseResult;
import rx.Single;

/**
 * Provides an implementation of the {@link PurchaseAddEditContract.EditDraftPresenter}.
 * <p/>
 * Subclass of {@link PurchaseEditPresenter}.
 */
public class PurchaseEditDraftPresenter extends PurchaseEditPresenter
        implements PurchaseAddEditContract.EditDraftPresenter {

    public PurchaseEditDraftPresenter(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull UserRepository userRepo,
                                      @NonNull GroupRepository groupRepo,
                                      @NonNull PurchaseRepository purchaseRepo,
                                      @NonNull RemoteConfigHelper configHelper,
                                      @NonNull String editPurchaseId) {
        super(savedState, navigator, userRepo, groupRepo, purchaseRepo, configHelper, editPurchaseId);
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
        navigator.finish(PurchaseResult.PURCHASE_DRAFT_DELETED, editPurchaseId);
    }
}
