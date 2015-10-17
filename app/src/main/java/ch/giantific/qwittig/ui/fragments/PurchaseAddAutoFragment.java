package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemRow;
import ch.giantific.qwittig.data.ocr.models.ItemRest;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.helpers.OcrHelper;
import ch.giantific.qwittig.ui.activities.PurchaseAddActivity;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseAddAutoFragment extends PurchaseAddFragment {

    private static final String STATE_ITEMS_SET = "items_set";
    private static final String LOG_TAG = PurchaseAddAutoFragment.class.getSimpleName();
    static final String OCR_HELPER = "ocr_helper";
    private View mProgressView;
    private View mMainView;
    private boolean mOcrValuesAreSet;
    private boolean mInTrialMode;

    public static PurchaseAddAutoFragment newInstance(boolean inTrialMode) {
        PurchaseAddAutoFragment purchaseAddAutoFragment = new PurchaseAddAutoFragment();
        Bundle args = new Bundle();
        args.putBoolean(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, inTrialMode);
        purchaseAddAutoFragment.setArguments(args);
        return purchaseAddAutoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOcrValuesAreSet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_purchase_add_auto, container, false);

        findViews(rootView);

        return rootView;
    }

    @Override
    void findViews(View rootView) {
        super.findViews(rootView);

        mProgressView = rootView.findViewById(R.id.ll_progress);
        mMainView = rootView.findViewById(R.id.view_add);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mOcrValuesAreSet) {
            showMainScreen();
        }
    }

    @Override
    void revealFab() {
        if (mOcrValuesAreSet) {
            mListener.showFab(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        doReceiptOcrWithHelper();
                        break;
                    case Activity.RESULT_CANCELED:
                        setResultForSnackbar(PURCHASE_DISCARDED);
                        getActivity().finish();
                        break;
                }
                break;
        }
    }

    @Override
    void showReceiptAddedSnackbar() {
        // don't show anything in auto mode
    }

    public void doReceiptOcrWithHelper() {
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

    public void onOcrSuccessful(PurchaseRest purchaseRest) {
        setValuesFromOcr(purchaseRest);
    }

    public void onOcrFailed(String errorMessage) {
        MessageUtils.showBasicSnackbar(mButtonAddRow, errorMessage);
        showMainScreen();
    }

    public void setValuesFromOcr(PurchaseRest purchaseRest) {
        // set date
        //setDateSelected(purchaseRest.getDate());
        // set store
        setStore(purchaseRest.getStore(), false);

        // set item rows
        List<ItemRest> itemsRest = purchaseRest.getItems();
        int itemsRestSize = itemsRest.size();
        List<ItemRow> itemRowsNew = new ArrayList<>(itemsRestSize);
        for (int i = 0; i < itemsRestSize; i++) {
            final ItemRest itemRest = itemsRest.get(i);
            final ItemRow itemRowNew = addNewItemRow(i + 1);
            itemRowNew.setEditTextName(itemRest.getName());
            String price = MoneyUtils.formatPrice(itemRest.getPrice(), mCurrencySelected);
            itemRowNew.setEditTextPrice(price);
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

    public void showMainScreen() {
        mProgressView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPurchaseSaveAndPinSucceeded() {
        if (mInTrialMode) {
            mCurrentUser.incrementPremiumCount();
            mCurrentUser.saveEventually();
        }

        super.onPurchaseSaveAndPinSucceeded();
    }

    @Override
    @PurchaseAction
    int getPurchaseSavedAction() {
        return PURCHASE_SAVED_AUTO;
    }
}
