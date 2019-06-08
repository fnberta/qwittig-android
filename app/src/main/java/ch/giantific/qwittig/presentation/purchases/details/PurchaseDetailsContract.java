/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;

/**
 * Defines an observable view model for the purchase details screen.
 */
public interface PurchaseDetailsContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void onEditPurchaseClick();

        void onDeletePurchaseClick();

        void onShowExchangeRateClick();
    }

    interface ViewListener extends BaseView {

        void startEnterTransition();

        void toggleMenuOptions(boolean showEditOptions, boolean showExchangeRateOption);

        void addArticle(@NonNull PurchaseDetailsArticleItemViewModel item);

        void clearArticles();

        boolean isArticlesEmpty();

        void notifyArticlesChanged();

        void addIdentities(@NonNull List<PurchaseDetailsIdentityItemViewModel> items);

        void clearIdentities();

        void notifyIdentitiesChanged();
    }

    @IntDef({PurchaseDetailsResult.PURCHASE_DELETED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseDetailsResult {
        int PURCHASE_DELETED = 2;
    }
}
