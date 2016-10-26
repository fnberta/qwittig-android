package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.purchases.addedit.dialogs.ExchangeRateDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.dialogs.NoteDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import ch.giantific.qwittig.utils.rxwrapper.android.transitions.TransitionEvent;
import rx.Observable;
import rx.Single;

/**
 * Created by fabio on 26.09.16.
 */

public interface PurchaseAddEditContract {

    interface Presenter extends
            BasePresenter<ViewListener>,
            NoteDialogFragment.DialogInteractionListener,
            DiscardChangesDialogFragment.DialogInteractionListener,
            ExchangeRateDialogFragment.DialogInteractionListener,
            RatesWorkerListener {

        void onDateClick(View view);

        void onDateSet(@NonNull Date date);

        void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onAddRowClick(@NonNull BasePurchaseAddEditItemViewModel itemViewModel);

        void onArticlePriceChanged(PurchaseAddEditArticleItemViewModel itemViewModel, CharSequence price);

        void onArticlePriceFocusChange(PurchaseAddEditArticleItemViewModel itemViewModel,
                                       boolean hasFocus);

        void onToggleIdentitiesClick(@NonNull PurchaseAddEditArticleItemViewModel itemViewModel);

        void onArticleRowIdentityClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel itemViewModel);

        boolean onArticleRowIdentityLongClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel itemViewModel);

        void onArticleDismiss(int position);

        void onAddEditNoteMenuClick();

        void onAddEditReceiptImageMenuClick();

        void onReceiptImageTaken(@NonNull String receiptImagePath);

        void onReceiptImageTakeFailed();

        void onDeleteReceiptMenuClick();

        void onExchangeRateClick(View view);

        void onSavePurchaseClick(View view);

        void onSaveAsDraftMenuClick();

        void onExitClick();
    }

    interface AddOcrPresenter extends Presenter {

        void setOcrDataId(@NonNull String ocrDataId);
    }

    interface EditDraftPresenter extends Presenter {

        void onDeleteDraftMenuClick();
    }

    interface ViewListener extends BaseView {

        Observable<TransitionEvent> getEnterTransition();

        Single<FloatingActionButton> showFab();

        void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency);

        void showDatePickerDialog();

        void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate);

        void showPurchaseDiscardDialog();

        void showDiscardEditChangesDialog();

        void showAddEditNoteDialog(@NonNull String note);

        void captureImage();

        void reloadOptionsMenu();

        void scrollToPosition(int position);

        void notifyItemAdded(int position);

        void notifyItemIdentityChanged(int position,
                                       @NonNull PurchaseAddEditArticleIdentityItemViewModel identityViewModel);

        void notifyItemRemoved(int position);

        void notifyItemRangeRemoved(int start, int end);
    }

    @IntDef({PurchaseResult.PURCHASE_SAVED, PurchaseResult.PURCHASE_SAVED_AUTO,
            PurchaseResult.PURCHASE_DRAFT, PurchaseResult.PURCHASE_DRAFT_CHANGES,
            PurchaseResult.PURCHASE_DISCARDED, PurchaseResult.PURCHASE_DRAFT_DELETED,
            Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseResult {
        int PURCHASE_SAVED = 2;
        int PURCHASE_SAVED_AUTO = 3;
        int PURCHASE_DRAFT = 4;
        int PURCHASE_DRAFT_CHANGES = 5;
        int PURCHASE_DISCARDED = 6;
        int PURCHASE_DRAFT_DELETED = 7;
    }
}
