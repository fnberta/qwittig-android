/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.ItemRow;
import ch.giantific.qwittig.domain.models.ocr.OcrItem;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import ch.giantific.qwittig.data.helpers.OcrHelper;
import ch.giantific.qwittig.ui.activities.PurchaseAddActivity;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * Displays a {@link ProgressBar} until the ocr process has finished on the server and the results
 * are populated in item rows. If the user is in trial mode, it will increase the trial counter
 * when he/she saves the purchase.
 * <p/>
 * Subclass of {@link PurchaseAddFragment}.
 */
public class PurchaseAddAutoFragment extends PurchaseAddFragment {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String LOG_TAG = PurchaseAddAutoFragment.class.getSimpleName();
    private static final String OCR_HELPER = "OCR_HELPER";
    private View mProgressView;
    private View mMainView;
    private boolean mOcrValuesAreSet;
    private boolean mInTrialMode;

    /**
     * Returns a new {@link PurchaseAddAutoFragment}.
     *
     * @param inTrialMode whether the user is in trial mode or not
     * @return a new {@link PurchaseAddAutoFragment}
     */
    @NonNull
    public static PurchaseAddAutoFragment newInstance(boolean inTrialMode) {
        PurchaseAddAutoFragment purchaseAddAutoFragment = new PurchaseAddAutoFragment();
        Bundle args = new Bundle();
        args.putBoolean(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, inTrialMode);
        purchaseAddAutoFragment.setArguments(args);
        return purchaseAddAutoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mInTrialMode = args.getBoolean(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, false);
        }

        if (savedInstanceState != null) {
            mOcrValuesAreSet = savedInstanceState.getBoolean(STATE_ITEMS_SET);
        } else {
            mOcrValuesAreSet = false;
        }

        captureImage();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOcrValuesAreSet);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_add_auto, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressView = view.findViewById(R.id.ll_progress);
        mMainView = view.findViewById(R.id.view_add);

        if (mOcrValuesAreSet) {
            showMainScreen();
        }
    }

    @Override
    void revealFab() {
        if (mOcrValuesAreSet) {
            mListener.showFab();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (mOcrValuesAreSet) {
            super.onCreateOptionsMenu(menu, inflater);
        } else {
            menu.clear();
        }
    }

    @Override
    void setupRows() {
        if (mOcrValuesAreSet) {
            super.setupRows();
        }
    }

    @Override
    void setFirstRowItemUsersChecked() {
        if (mOcrValuesAreSet) {
            super.setFirstRowItemUsersChecked();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        doReceiptOcrWithHelper();
                        break;
                    case Activity.RESULT_CANCELED:
                        setResultForSnackbar(RESULT_PURCHASE_DISCARDED);
                        getActivity().finish();
                        break;
                }
                break;
        }
    }

    @Override
    void showImageTakenSnackbar() {
        // don't show anything in auto mode
    }

    private void doReceiptOcrWithHelper() {
        if (!Utils.isConnected(getActivity())) {
            onOcrFailed(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        OcrHelper ocrHelper = (OcrHelper) fragmentManager.findFragmentByTag(OCR_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (ocrHelper == null) {
            ocrHelper = OcrHelper.newInstance(mReceiptImagePath);

            fragmentManager.beginTransaction()
                    .add(ocrHelper, OCR_HELPER)
                    .commit();
        }
    }

    /**
     * Populates the results from the server.
     *
     * @param ocrPurchase the ocr result from the server
     */
    public void onOcrFinished(@NonNull OcrPurchase ocrPurchase) {
        setValuesFromOcr(ocrPurchase);
    }

    /**
     * Displays an error message to user and shows the main screen.
     *
     * @param errorMessage the error message received from the server
     */
    public void onOcrFailed(@NonNull String errorMessage) {
        Snackbar.make(mButtonAddRow, errorMessage, Snackbar.LENGTH_LONG).show();
        showMainScreen();
    }

    private void setValuesFromOcr(@NonNull OcrPurchase ocrPurchase) {
        // set date
        //setDateSelected(ocrPurchase.getDate());
        // set store
        setStore(ocrPurchase.getStore(), false);

        // set item rows
        List<OcrItem> itemsRest = ocrPurchase.getItems();
        int itemsRestSize = itemsRest.size();
        List<ItemRow> itemRowsNew = new ArrayList<>(itemsRestSize);
        for (int i = 0; i < itemsRestSize; i++) {
            final OcrItem ocrItem = itemsRest.get(i);
            final ItemRow itemRowNew = addNewItemRow(i + 1);
            String price = MoneyUtils.formatPrice(ocrItem.getPrice(), mCurrencySelected);
            itemRowNew.fillValues(ocrItem.getName(), price);
            itemRowsNew.add(itemRowNew);

            // update ImeOptions
            setEditTextPriceImeOptions();
        }

        if (!itemRowsNew.isEmpty()) {
            ItemRow firstItemRow = itemRowsNew.get(0);
            firstItemRow.requestFocusForName();
        }

        mItemRowCount = itemsRestSize;
        mOcrValuesAreSet = true;
        showMainScreen();
    }

    private void showMainScreen() {
        mProgressView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPurchaseSavedAndPinned() {
        if (mInTrialMode) {
            mCurrentUser.incrementPremiumCount();
            mCurrentUser.saveEventually();
        }

        super.onPurchaseSavedAndPinned();
    }

    @Override
    @PurchaseResults
    int getPurchaseSavedAction() {
        return RESULT_PURCHASE_SAVED_AUTO;
    }
}
