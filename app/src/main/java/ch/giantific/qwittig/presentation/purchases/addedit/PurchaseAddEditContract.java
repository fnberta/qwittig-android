package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;

/**
 * Created by fabio on 26.09.16.
 */

public interface PurchaseAddEditContract {

    interface Presenter extends
            BasePresenter<ViewListener>,
            ListPresenter<BasePurchaseAddEditItemViewModel>,
            PurchaseAddEditArticleItemViewModel.PriceChangedListener,
            NoteDialogFragment.DialogInteractionListener,
            DiscardChangesDialogFragment.DialogInteractionListener,
            ExchangeRateDialogFragment.DialogInteractionListener,
            RatesWorkerListener {

        PurchaseAddEditViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void onDateClick(View view);

        void onDateSet(@NonNull Date date);

        void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onAddRowClick(@NonNull BasePurchaseAddEditItemViewModel itemViewModel);

        void onToggleIdentitiesClick(@NonNull PurchaseAddEditArticleItemViewModel itemViewModel);

        void onArticleRowIdentityClick();

        void onArticleRowIdentityLongClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel userClicked);

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

    interface ViewListener extends BaseViewListener {

        void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency);

        void showDatePickerDialog();

        void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate);

        void showPurchaseDiscardDialog();

        void showDiscardEditChangesDialog();

        void showAddEditNoteDialog(@NonNull String note);

        void captureImage();

        void reloadOptionsMenu();
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
