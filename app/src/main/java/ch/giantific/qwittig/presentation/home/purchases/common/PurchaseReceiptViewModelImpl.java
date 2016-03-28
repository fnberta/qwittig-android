/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link PurchaseReceiptViewModel}.
 */
public class PurchaseReceiptViewModelImpl extends ViewModelBaseImpl<PurchaseReceiptViewModel.ViewListener>
        implements PurchaseReceiptViewModel {

    private static final String STATE_LOADING = "STATE_LOADING";
    private final PurchaseRepository mPurchaseRepo;
    private final String mPurchaseId;
    private boolean mLoading;
    private String mReceiptImagePath;

    private PurchaseReceiptViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull PurchaseReceiptViewModel.ViewListener view,
                                         @NonNull UserRepository userRepository,
                                         @NonNull PurchaseRepository purchaseRepo,
                                         @Nullable String purchaseId,
                                         @Nullable String imagePath) {
        super(savedState, view, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        mReceiptImagePath = imagePath;

        if (savedState != null) {
            mLoading = savedState.getBoolean(STATE_LOADING, false);
        } else {
            mLoading = true;
        }
    }

    public static PurchaseReceiptViewModelImpl createImagePathInstance(@Nullable Bundle savedState,
                                                                       @NonNull PurchaseReceiptViewModel.ViewListener view,
                                                                       @NonNull UserRepository userRepository,
                                                                       @NonNull PurchaseRepository purchaseRepo,
                                                                       @NonNull String imagePath) {
        return new PurchaseReceiptViewModelImpl(savedState, view, userRepository, purchaseRepo, null, imagePath);
    }

    public static PurchaseReceiptViewModelImpl createPurchaseIdInstance(@Nullable Bundle savedState,
                                                                        @NonNull PurchaseReceiptViewModel.ViewListener view,
                                                                        @NonNull UserRepository userRepository,
                                                                        @NonNull PurchaseRepository purchaseRepo,
                                                                        @NonNull String purchaseId) {
        return new PurchaseReceiptViewModelImpl(savedState, view, userRepository, purchaseRepo, purchaseId, null);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    public void onViewVisible() {
        super.onViewVisible();

        loadReceipt();
    }

    private void loadReceipt() {
        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            mView.setReceiptImage(mReceiptImagePath);
            setLoading(false);
        } else if (ParseUtils.isObjectId(mPurchaseId)) {
            getSubscriptions().add(mPurchaseRepo.fetchPurchaseData(mPurchaseId)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase purchase) {
                            setLoading(false);
                            mView.setReceiptImage(purchase.getReceiptUrl());
                        }

                        @Override
                        public void onError(Throwable error) {
                            onReceiptLoadFailed();
                        }
                    })
            );
        } else {
            getSubscriptions().add(mPurchaseRepo.getPurchase(mPurchaseId)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase purchase) {
                            setLoading(false);
                            mView.setReceiptImage(purchase.getReceiptData());
                        }

                        @Override
                        public void onError(Throwable error) {
                            onReceiptLoadFailed();
                        }
                    }));
        }
    }

    private void onReceiptLoadFailed() {
        setLoading(false);
        mView.showMessage(R.string.toast_error_receipt_load);
    }

    @Override
    public void onReceiptImagePathSet(@NonNull String receiptImagePath) {
        mReceiptImagePath = receiptImagePath;
    }

    @Override
    public void onReceiptImageCaptured() {
        mView.setReceiptImage(mReceiptImagePath);
        mView.showMessage(R.string.toast_receipt_changed);
    }
}
