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
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditContract.AddOcrPresenter} for the screen where the
 * purchase items get automatically filled in via an image of the receipt.
 * <p>
 * Subclass of {@link PurchaseAddPresenter}.
 */
public class PurchaseAddOcrPresenter extends PurchaseAddPresenter
        implements PurchaseAddEditContract.AddOcrPresenter {

    private static final String STATE_OCR_VALUES_SET = "STATE_OCR_VALUES_SET";

    private String ocrDataId;
    private boolean ocrValuesSet;
    private String purchaseId;

    public PurchaseAddOcrPresenter(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull UserRepository userRepo,
                                   @NonNull GroupRepository groupRepo,
                                   @NonNull PurchaseRepository purchaseRepo,
                                   @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, userRepo, groupRepo, purchaseRepo, configHelper);

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
    protected void loadPurchase(@NonNull final FirebaseUser currentUser) {
        subscriptions.add(getInitialChain(currentUser)
                .flatMap(identities1 -> purchaseRepo.getOcrData(ocrDataId, currentUser.getUid()))
                .subscribe(new SingleSubscriber<OcrData>() {
                    @Override
                    public void onSuccess(OcrData ocrData) {
                        if (!ocrValuesSet) {
                            setOcrData(ocrData);
                            ocrValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load ocr data with error:");
                        view.showMessage(R.string.toast_error_purchase_ocr_load);
                    }
                })
        );
    }

    @SuppressWarnings("unchecked")
    private void setOcrData(@NonNull OcrData ocrData) {
        purchaseId = ocrData.getPurchase();

        final Map<String, Object> data = ocrData.getData();
//        setDate(data.get("date"));
        final List<String> stores = (List<String>) data.get("store");
        if (stores != null && !stores.isEmpty()) {
            viewModel.setStore(stores.get(0));
        }
        viewModel.setReceipt(ocrData.getReceipt());

        final List<Map<String, Object>> articles = (List<Map<String, Object>>) data.get("items");
        if (articles != null && !articles.isEmpty()) {
            for (Map<String, Object> item : articles) {
                final String price = moneyFormatter.format(item.get("price"));
                final String name = (String) item.get("name");
                final PurchaseAddEditArticleItemViewModel articleItem =
                        new PurchaseAddEditArticleItemViewModel(name, price, getArticleIdentities());
                final int pos = getItemCount() - 2;
                this.items.add(pos, articleItem);
                listInteraction.notifyItemInserted(this.items.indexOf(articleItem));
            }
        }
    }

    @NonNull
    @Override
    protected Purchase createPurchase(@NonNull List<String> purchaseIdentities,
                                      @NonNull List<Article> purchaseArticles, int fractionDigits,
                                      boolean isDraft) {
        final double total = convertRoundTotal(fractionDigits);
        return new Purchase(currentIdentity.getGroup(), currentIdentity.getId(),
                viewModel.getDate(), viewModel.getStore(), total, viewModel.getCurrency(),
                viewModel.getExchangeRate(), viewModel.getReceipt(), viewModel.getNote(), isDraft,
                ocrDataId, purchaseIdentities, purchaseArticles);
    }

    protected void savePurchase(@NonNull Purchase purchase, boolean asDraft) {
        if (asDraft) {
            purchaseRepo.saveDraft(purchase, purchaseId);
        } else {
            purchaseRepo.savePurchase(purchase, purchaseId, currentIdentity.getUser(), false);
        }
    }

    @Override
    protected void onPurchaseSaved(boolean asDraft) {
        if (configHelper.isShowOcrRating()) {
            navigator.startOcrRating(ocrDataId);
        }

        super.onPurchaseSaved(asDraft);
    }

    @Override
    public void onDiscardChangesSelected() {
        purchaseRepo.discardOcrData(currentIdentity.getUser(), ocrDataId);

        super.onDiscardChangesSelected();
    }
}
