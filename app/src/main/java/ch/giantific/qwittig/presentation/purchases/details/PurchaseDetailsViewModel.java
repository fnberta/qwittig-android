/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsArticleItemModel;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsIdentityItemModel;

/**
 * Defines an observable view model for the purchase details screen.
 */
public interface PurchaseDetailsViewModel extends ListViewModel<PurchaseDetailsArticleItemModel, PurchaseDetailsViewModel.ViewListener>, PurchaseReceiptViewModel {

    void setIdentitiesListInteraction(@NonNull ListInteraction listInteraction);

    @Bindable
    String getStore();

    void setStore(@NonNull String store);

    @Bindable
    String getDate();

    void setDate(@NonNull Date date);

    @Bindable
    String getTotal();

    void setTotal(double total);

    @Bindable
    String getTotalForeign();

    void setTotalForeign(double totalForeign);

    @Bindable
    String getMyShare();

    void setMyShare(double myShare);

    @Bindable
    String getMyShareForeign();

    void setMyShareForeign(double myShareForeign);

    @Bindable
    String getNote();

    void setNote(@NonNull String note);

    @Bindable
    boolean isNoteAvailable();

    PurchaseDetailsIdentityItemModel getIdentityAtPosition(int position);

    int getIdentitiesCount();

    void onEditPurchaseClick();

    void onDeletePurchaseClick();

    void onShowExchangeRateClick();

    @IntDef({PurchaseDetailsResult.PURCHASE_DELETED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseDetailsResult {
        int PURCHASE_DELETED = 2;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

        void startEnterTransition();

        void toggleMenuOptions(boolean showEditOptions, boolean showExchangeRateOption);
    }
}
