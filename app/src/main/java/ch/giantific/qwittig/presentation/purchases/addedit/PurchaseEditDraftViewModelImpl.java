/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import rx.Single;
import rx.SingleSubscriber;

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
                                          @NonNull UserRepository userRepository,
                                          @NonNull PurchaseRepository purchaseRepo,
                                          @NonNull String editPurchaseId) {
        super(savedState, navigator, eventBus, userRepository, purchaseRepo, editPurchaseId);
    }

    @Override
    Single<Purchase> fetchEditPurchase() {
        return mPurchaseRepo.getPurchase(mEditPurchaseId);
    }

    @Override
    Single<Purchase> getSavePurchaseAction(@NonNull Purchase purchase) {
        return mPurchaseRepo.savePurchase(purchase);
    }

    @Override
    int getDraftFinishedResult() {
        return PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT_CHANGES;
    }

    @Override
    public void onDeleteDraftMenuClick() {
        getSubscriptions().add(mPurchaseRepo.deleteDraft(mEditPurchase)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase value) {
                        mNavigator.finish(PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT_DELETED);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_draft_delete);
                    }
                })
        );
    }
}
