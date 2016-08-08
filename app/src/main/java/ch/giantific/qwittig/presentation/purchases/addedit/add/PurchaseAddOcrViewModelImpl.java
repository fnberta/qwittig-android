/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import rx.Single;
import rx.SingleSubscriber;
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
    private boolean mOcrValuesSet;

    public PurchaseAddOcrViewModelImpl(@Nullable Bundle savedState,
                                       @NonNull Navigator navigator,
                                       @NonNull RxBus<Object> eventBus,
                                       @NonNull UserRepository userRepository,
                                       @NonNull PurchaseRepository purchaseRepo,
                                       @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, eventBus, userRepository, purchaseRepo, configHelper);

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
    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        getSubscriptions().add(getInitialChain(currentUser)
                .flatMap(new Func1<List<Identity>, Single<OcrData>>() {
                    @Override
                    public Single<OcrData> call(List<Identity> identities) {
                        return mPurchaseRepo.getOcrData(mOcrDataId);
                    }
                })
                .subscribe(new SingleSubscriber<OcrData>() {
                    @Override
                    public void onSuccess(OcrData ocrData) {
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
        setReceipt(ocrData.getReceipt());

        final List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        if (!items.isEmpty()) {
            for (Map<String, Object> item : items) {
                final String price = mMoneyFormatter.format(item.get("price"));
                final String name = (String) item.get("name");
                final PurchaseAddEditItem purchaseAddEditItem =
                        new PurchaseAddEditItem(name, price, getItemUsers());
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                final int pos = getItemCount() - 2;
                mItems.add(pos, purchaseAddEditItem);
                mListInteraction.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
            }
        }
    }

    @NonNull
    @Override
    protected Purchase createPurchase(@NonNull List<String> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems, int fractionDigits,
                                      boolean isDraft) {
        final double total = convertRoundTotal(fractionDigits);
        return new Purchase(mCurrentIdentity.getGroup(), mCurrentIdentity.getId(), mDate, mStore,
                total, mCurrency, mExchangeRate, mReceipt, mNote, isDraft, mOcrDataId,
                purchaseIdentities, purchaseItems);
    }

    @Override
    protected void onPurchaseSaved(boolean asDraft) {
        if (mConfigHelper.isShowOcrRating()) {
            mNavigator.startOcrRating(mOcrDataId);
        }

        super.onPurchaseSaved(asDraft);
    }
}
