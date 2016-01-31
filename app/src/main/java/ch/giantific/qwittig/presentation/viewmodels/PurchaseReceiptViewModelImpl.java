/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 29.01.16.
 */
public class PurchaseReceiptViewModelImpl extends ViewModelBaseImpl<PurchaseReceiptViewModel.ViewListener>
        implements PurchaseReceiptViewModel {

    private PurchaseRepository mPurchaseRepo;
    private boolean mLoading;
    private String mReceiptImagePath;
    private String mPurchaseId;
    private boolean mDraft;

    public PurchaseReceiptViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String receiptImagePath) {
        super(savedState, userRepository);

        mPurchaseRepo = purchaseRepo;
        mReceiptImagePath = receiptImagePath;
    }

    public PurchaseReceiptViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId, boolean draft) {
        super(savedState, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        mDraft = draft;
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
    public void attachView(@NonNull PurchaseReceiptViewModel.ViewListener view) {
        super.attachView(view);

        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            mView.setReceiptImage(mReceiptImagePath);
        } else if (mDraft) {
            mSubscriptions.add(mPurchaseRepo.getPurchaseLocalAsync(mPurchaseId, true)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase purchase) {
                            onReceiptImageLoaded(purchase.getReceiptData());
                        }

                        @Override
                        public void onError(Throwable error) {
                            onReceiptLoadFailed();
                        }
                    }));
        } else {
            mSubscriptions.add(mPurchaseRepo.fetchPurchaseDataLocalAsync(mPurchaseId)
                    .flatMap(new Func1<Purchase, Single<byte[]>>() {
                        @Override
                        public Single<byte[]> call(Purchase purchase) {
                            return mPurchaseRepo.getPurchaseReceiptImageAsync(purchase);
                        }
                    })
                    .subscribe(new SingleSubscriber<byte[]>() {
                        @Override
                        public void onSuccess(byte[] receiptImage) {
                            onReceiptImageLoaded(receiptImage);
                        }

                        @Override
                        public void onError(Throwable error) {
                            onReceiptLoadFailed();
                        }
                    })
            );
        }
    }

    private void onReceiptImageLoaded(byte[] receiptImage) {
        setLoading(false);
        mView.setReceiptImage(receiptImage);
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
