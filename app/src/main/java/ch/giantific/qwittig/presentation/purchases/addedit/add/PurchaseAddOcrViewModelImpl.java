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
import ch.giantific.qwittig.data.repositories.GroupRepository;
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
 * <p>
 * Subclass of {@link PurchaseAddViewModelImpl}.
 */
public class PurchaseAddOcrViewModelImpl extends PurchaseAddViewModelImpl implements PurchaseAddOcrViewModel {

    private static final String STATE_OCR_VALUES_SET = "STATE_OCR_VALUES_SET";

    private String ocrDataId;
    private boolean ocrValuesSet;

    public PurchaseAddOcrViewModelImpl(@Nullable Bundle savedState,
                                       @NonNull Navigator navigator,
                                       @NonNull RxBus<Object> eventBus,
                                       @NonNull UserRepository userRepository,
                                       @NonNull GroupRepository groupRepository,
                                       @NonNull PurchaseRepository purchaseRepo,
                                       @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, eventBus, userRepository, groupRepository, purchaseRepo,
                configHelper);

        if (savedState != null) {
            ocrValuesSet = savedState.getBoolean(STATE_OCR_VALUES_SET, false);
        } else {
            ocrValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_OCR_VALUES_SET, ocrValuesSet);
    }

    @Override
    public void setOcrDataId(@NonNull String ocrDataId) {
        this.ocrDataId = ocrDataId;
    }

    @Override
    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        getSubscriptions().add(getInitialChain(currentUser)
                .flatMap(new Func1<List<Identity>, Single<OcrData>>() {
                    @Override
                    public Single<OcrData> call(List<Identity> identities) {
                        return purchaseRepo.getOcrData(ocrDataId);
                    }
                })
                .subscribe(new SingleSubscriber<OcrData>() {
                    @Override
                    public void onSuccess(OcrData ocrData) {
                        if (ocrValuesSet) {
                            updateRows();
                        } else {
                            setOcrData(ocrData);
                            ocrValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.showMessage(R.string.toast_error_purchase_ocr_load);
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
                final String price = moneyFormatter.format(item.get("price"));
                final String name = (String) item.get("name");
                final PurchaseAddEditItem purchaseAddEditItem =
                        new PurchaseAddEditItem(name, price, getItemUsers());
                purchaseAddEditItem.setMoneyFormatter(moneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                final int pos = getItemCount() - 2;
                this.items.add(pos, purchaseAddEditItem);
                listInteraction.notifyItemInserted(this.items.indexOf(purchaseAddEditItem));
            }
        }
    }

    @NonNull
    @Override
    protected Purchase createPurchase(@NonNull List<String> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems, int fractionDigits,
                                      boolean isDraft) {
        final double total = convertRoundTotal(fractionDigits);
        return new Purchase(currentIdentity.getGroup(), currentIdentity.getId(), date, store,
                total, currency, exchangeRate, receipt, note, isDraft, ocrDataId,
                purchaseIdentities, purchaseItems);
    }

    @Override
    protected void onPurchaseSaved(boolean asDraft) {
        if (configHelper.isShowOcrRating()) {
            navigator.startOcrRating(ocrDataId);
        }

        super.onPurchaseSaved(asDraft);
    }
}
