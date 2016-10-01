/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;

/**
 * Defines an observable view model for the purchase details screen.
 */
public interface PurchaseDetailsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<PurchaseDetailsArticleItemViewModel> {

        PurchaseDetailsViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void setIdentitiesListInteraction(@NonNull ListInteraction listInteraction);

        PurchaseDetailsIdentityItemViewModel getIdentityAtPosition(int position);

        int getIdentityCount();

        void onEditPurchaseClick();

        void onDeletePurchaseClick();

        void onShowExchangeRateClick();
    }

    interface ViewListener extends BaseViewListener {

        void startEnterTransition();

        void toggleMenuOptions(boolean showEditOptions, boolean showExchangeRateOption);
    }

    @IntDef({PurchaseDetailsResult.PURCHASE_DELETED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseDetailsResult {
        int PURCHASE_DELETED = 2;
    }
}
