/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 29.01.16.
 */
public class PurchaseEditDraftViewModelImpl extends PurchaseAddEditViewModelEditImpl implements PurchaseEditDraftViewModel {

    public PurchaseEditDraftViewModelImpl(@Nullable Bundle savedState,
                                          @NonNull GroupRepository groupRepository,
                                          @NonNull UserRepository userRepository,
                                          @NonNull SharedPreferences sharedPreferences,
                                          @NonNull PurchaseRepository purchaseRepo,
                                          @NonNull String editPurchaseId) {
        super(savedState, groupRepository, userRepository, sharedPreferences, purchaseRepo, editPurchaseId);
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
    Purchase createPurchase(@NonNull List<User> purchaseUsersInvolved,
                            @NonNull List<Item> purchaseItems) {
        final Purchase purchase = super.createPurchase(purchaseUsersInvolved, purchaseItems);
        purchase.removeDraftId();
        return purchase;
    }

    @Override
    void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        mView.loadSavePurchaseWorker(purchase, receiptImage, purchase.getReceiptParseFile(),
                mDeleteOldReceipt, true);
    }

    @Override
    void onPurchaseSaveError(Throwable error) {
        mEditPurchase.setDraftId(mEditPurchaseId);
        super.onPurchaseSaveError(error);
    }

    @Override
    public void onDeleteDraftClick() {
        mSubscriptions.add(mPurchaseRepo.removePurchaseLocalAsync(mEditPurchase, null)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase value) {
                        mView.finishScreen(RESULT_PURCHASE_DRAFT_DELETED);
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                }));
    }
}
