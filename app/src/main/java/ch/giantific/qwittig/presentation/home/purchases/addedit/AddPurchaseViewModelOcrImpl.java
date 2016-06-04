/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.OcrPurchase;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AddEditPurchaseViewModel} for the screen where the
 * purchase items get automatically filled in via an image of the receipt.
 * <p/>
 * Subclass of {@link AddPurchaseViewModelImpl}.
 */
public class AddPurchaseViewModelOcrImpl extends AddPurchaseViewModelImpl {

    private static final String STATE_OCR_VALUES_SET = "STATE_OCR_VALUES_SET";
    private String mOcrPurchaseId;
    private OcrPurchase mOcrPurchase;
    private boolean mOcrValuesSet;

    public AddPurchaseViewModelOcrImpl(@Nullable Bundle savedState,
                                       @NonNull AddEditPurchaseViewModel.ViewListener view,
                                       @NonNull UserRepository userRepository,
                                       @NonNull PurchaseRepository purchaseRepo,
                                       @NonNull String ocrPurchaseId) {
        super(savedState, view, userRepository, purchaseRepo);

        mOcrPurchaseId = ocrPurchaseId;
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
                .flatMap(new Func1<List<Identity>, Single<OcrPurchase>>() {
                    @Override
                    public Single<OcrPurchase> call(List<Identity> identities) {
                        return mPurchaseRepo.fetchOcrPurchaseData(mOcrPurchaseId);
                    }
                })
                .subscribe(new SingleSubscriber<OcrPurchase>() {
                    @Override
                    public void onSuccess(OcrPurchase ocrPurchase) {
                        mOcrPurchase = ocrPurchase;

                        if (mOcrValuesSet) {
                            updateRows();
                        } else {
                            setOcrData(ocrPurchase);
                            mOcrValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "something went wrong with fetching");
                    }
                })
        );
    }

    @SuppressWarnings("unchecked")
    private void setOcrData(@NonNull OcrPurchase ocrPurchase) {
        final Map<String, Object> ocrData = ocrPurchase.getData();
//        setDate(ocrData.get("date"));
        final List<String> stores = (List<String>) ocrData.get("store");
        setStore(stores.get(0));
        final List<Map<String, Object>> items = (List<Map<String, Object>>) ocrData.get("items");
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

    @Override
    public void onShowReceiptImageMenuClick() {
        mView.showReceiptImage(mOcrPurchase.getReceipt().getUrl());
    }

    @NonNull
    @Override
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities, @NonNull List<Item> purchaseItems, int fractionDigits) {
        final Purchase purchase =  super.createPurchase(purchaseIdentities, purchaseItems, fractionDigits);
        purchase.setReceipt(mOcrPurchase.getReceipt());

        return purchase;
    }
}
