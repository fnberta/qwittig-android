/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.RemoteConfigRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the screen where the
 * purchase items get automatically filled in via an image of the receipt.
 * <p/>
 * Subclass of {@link PurchaseAddViewModelImpl}.
 */
public class PurchaseAddOcrViewModelImpl extends PurchaseAddViewModelImpl implements PurchaseAddOcrViewModel {

    private static final String STATE_OCR_VALUES_SET = "STATE_OCR_VALUES_SET";
    private String mOcrDataId;
    private OcrData mOcrData;
    private boolean mOcrValuesSet;

    public PurchaseAddOcrViewModelImpl(@Nullable Bundle savedState,
                                       @NonNull Navigator navigator,
                                       @NonNull RxBus<Object> eventBus,
                                       @NonNull UserRepository userRepository,
                                       @NonNull RemoteConfigRepository configRepo,
                                       @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, eventBus, userRepository, configRepo, purchaseRepo);

        if (savedState != null) {
            mOcrValuesSet = savedState.getBoolean(STATE_OCR_VALUES_SET, false);
        } else {
            mOcrValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_OCR_VALUES_SET, mOcrValuesSet);
    }

    @Override
    public void setOcrDataId(@NonNull String ocrDataId) {
        mOcrDataId = ocrDataId;
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
                        return mPurchaseRepo.fetchOcrData(mOcrDataId);
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
        final Map<String, Object> data = ocrData.getData();
//        setDate(data.get("date"));
        final List<String> stores = (List<String>) data.get("store");
        if (!stores.isEmpty()) {
            setStore(stores.get(0));
        }
        setReceiptImage(ocrData.getReceipt().getUrl());

        final List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        if (!items.isEmpty()) {
            for (Map<String, Object> item : items) {
                final String price = mMoneyFormatter.format(item.get("price"));
                final String name = (String) item.get("name");
                final PurchaseAddEditItem purchaseAddEditItem =
                        new PurchaseAddEditItem(name, price, getItemUsers(mIdentities));
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                mItems.add(getLastPosition() - 1, purchaseAddEditItem);
                mListInteraction.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
            }
        }
    }

    @NonNull
    @Override
    protected Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems, int fractionDigits) {
        final Purchase purchase = super.createPurchase(purchaseIdentities, purchaseItems, fractionDigits);
        purchase.setReceipt(mOcrData.getReceipt());
        purchase.setOcrData(mOcrData);

        return purchase;
    }

    @Override
    protected void onPurchaseSaved(boolean asDraft) {
        if (mConfigRepo.isShowOcrRating()) {
            mNavigator.startOcrRating(mOcrDataId);
        }

        super.onPurchaseSaved(asDraft);
    }
}
