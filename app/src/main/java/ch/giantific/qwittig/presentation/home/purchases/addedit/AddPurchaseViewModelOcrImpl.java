/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link AddEditPurchaseViewModel} for the screen where the
 * purchase items get automatically filled in via an image of the receipt.
 * <p/>
 * Subclass of {@link AddPurchaseViewModelImpl}.
 */
public class AddPurchaseViewModelOcrImpl extends AddPurchaseViewModelImpl {

    private static final String STATE_OCR_VALUES_SET = "STATE_OCR_VALUES_SET";
    private String mOcrPurchaseId;
    private OcrData mOcrData;
    private boolean mOcrValuesSet;

    public AddPurchaseViewModelOcrImpl(@Nullable Bundle savedState,
                                       @NonNull AddEditPurchaseViewModel.ViewListener view,
                                       @NonNull UserRepository userRepository,
                                       @NonNull PurchaseRepository purchaseRepo,
                                       @NonNull String ocrPurchaseId) {
        super(savedState, view, userRepository, purchaseRepo);

        mOcrPurchaseId = ocrPurchaseId;
        mOcrValuesSet = savedState != null && savedState.getBoolean(STATE_OCR_VALUES_SET, false);
        if (!mOcrValuesSet) {
            mLoading = true;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_OCR_VALUES_SET, mOcrValuesSet);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.getIdentities(mCurrentGroup, true)
                .toSortedList()
                .toSingle()
                .doOnSuccess(new Action1<List<Identity>>() {
                    @Override
                    public void call(List<Identity> identities) {
                        identities.remove(mCurrentIdentity);
                        identities.add(0, mCurrentIdentity);
                        mIdentities = identities;
                    }
                })
                .flatMap(new Func1<List<Identity>, Single<OcrData>>() {
                    @Override
                    public Single<OcrData> call(List<Identity> identities) {
                        return mPurchaseRepo.fetchOcrPurchaseData(mOcrPurchaseId);
                    }
                })
                .subscribe(new SingleSubscriber<OcrData>() {
                    @Override
                    public void onSuccess(OcrData ocrData) {
                        mOcrData = ocrData;

                        if (mOcrValuesSet) {
                            updateRows();
                        } else {
                            setOcrData(ocrData);
                            mOcrValuesSet = true;
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_ocr_load);
                    }
                })
        );
    }

    @SuppressWarnings("unchecked")
    private void setOcrData(@NonNull OcrData ocrData) {
        mView.toggleReceiptMenuOption(true);

        final Map<String, Object> data = ocrData.getData();
//        setDate(data.get("date"));
        final List<String> stores = (List<String>) data.get("store");
        if (!stores.isEmpty()) {
            setStore(stores.get(0));
        }
        final List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        if (!items.isEmpty()) {
            for (Map<String, Object> item : items) {
                final String price = mMoneyFormatter.format(item.get("price"));
                final String name = (String) item.get("name");
                final AddEditPurchaseItem purchaseAddEditItem =
                        new AddEditPurchaseItem(name, price, getItemUsers(mIdentities));
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                mItems.add(getLastPosition() - 1, purchaseAddEditItem);
                mView.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
            }
        }
    }

    @Override
    public void onShowReceiptImageMenuClick() {
        mView.showReceiptImage(mOcrData.getReceipt().getUrl());
    }

    @NonNull
    @Override
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                            @NonNull List<Item> purchaseItems, int fractionDigits) {
        final Purchase purchase =  super.createPurchase(purchaseIdentities, purchaseItems, fractionDigits);
        purchase.setReceipt(mOcrData.getReceipt());
        purchase.setOcrData(mOcrData);

        return purchase;
    }
}
