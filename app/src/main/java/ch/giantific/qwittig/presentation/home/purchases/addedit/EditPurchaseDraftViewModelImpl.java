/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link EditPurchaseDraftViewModel}.
 * <p/>
 * Subclass of {@link EditPurchaseViewModelImpl}.
 */
public class EditPurchaseDraftViewModelImpl extends EditPurchaseViewModelImpl
        implements EditPurchaseDraftViewModel {

    public EditPurchaseDraftViewModelImpl(@Nullable Bundle savedState,
                                          @NonNull AddEditPurchaseViewModel.ViewListener view,
                                          @NonNull RxBus<Object> eventBus,
                                          @NonNull UserRepository userRepository,
                                          @NonNull PurchaseRepository purchaseRepo,
                                          @NonNull String editPurchaseId) {
        super(savedState, view, eventBus, userRepository, purchaseRepo, editPurchaseId);
    }

    @Override
    Single<Purchase> fetchEditPurchase() {
        return mPurchaseRepo.getPurchase(mEditPurchaseId);
    }

    @Override
    int getDraftFinishedResult() {
        return AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DRAFT_CHANGES;
    }

    @Override
    public void onDeleteDraftMenuClick() {
        getSubscriptions().add(mPurchaseRepo.deleteDraft(mEditPurchase)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase value) {
                        mView.finishScreen(AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DRAFT_DELETED);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_draft_delete);
                    }
                })
        );
    }
}
