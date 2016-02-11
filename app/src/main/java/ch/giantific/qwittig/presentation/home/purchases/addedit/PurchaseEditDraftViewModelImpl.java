/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 29.01.16.
 */
public class PurchaseEditDraftViewModelImpl extends PurchaseAddEditViewModelEditImpl implements PurchaseEditDraftViewModel {

    public PurchaseEditDraftViewModelImpl(@Nullable Bundle savedState,
                                          @NonNull PurchaseAddEditViewModel.ViewListener view,
                                          @NonNull IdentityRepository identityRepository,
                                          @NonNull UserRepository userRepository,
                                          @NonNull PurchaseRepository purchaseRepo,
                                          @NonNull String editPurchaseId) {
        super(savedState, view, identityRepository, userRepository, purchaseRepo, editPurchaseId);
    }

    @Override
    Single<Purchase> fetchEditPurchase() {
        return mPurchaseRepo.getPurchaseLocalAsync(mEditPurchaseId, true);
    }

    @Override
    boolean hasOldReceiptFile() {
        return mEditPurchase.getReceiptData() != null;
    }

    @NonNull
    @Override
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                            @NonNull List<Item> purchaseItems) {
        final Purchase purchase = super.createPurchase(purchaseIdentities, purchaseItems);
        purchase.removeDraftId();
        return purchase;
    }

    @Override
    void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        mView.loadSavePurchaseWorker(purchase, receiptImage, purchase.getReceipt(),
                mDeleteOldReceipt, true);
    }

    @Override
    void onPurchaseSaveError(Throwable error) {
        mEditPurchase.setDraftId(mEditPurchaseId);
        super.onPurchaseSaveError(error);
    }

    @Override
    int getDraftFinishedResult() {
        return PurchaseResult.PURCHASE_DRAFT_CHANGES;
    }

    @Override
    public void onDeleteDraftClick() {
        mSubscriptions.add(mPurchaseRepo.removePurchaseLocalAsync(mEditPurchase, Purchase.PIN_LABEL_DRAFT)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase value) {
                        mView.finishScreen(PurchaseResult.PURCHASE_DRAFT_DELETED);
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                }));
    }
}
