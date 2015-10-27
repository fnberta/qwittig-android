/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.helpers.OcrHelper;
import ch.giantific.qwittig.ui.fragments.PurchaseAddAutoFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseDiscardDialogFragment;

/**
 * Hosts {@link PurchaseAddFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 * <p/>
 * Subclass of {@link PurchaseBaseActivity}.
 */
public class PurchaseAddActivity extends PurchaseBaseActivity implements
        PurchaseDiscardDialogFragment.DialogInteractionListener,
        OcrHelper.HelperInteractionListener {

    public static final String INTENT_PURCHASE_NEW_AUTO = "INTENT_PURCHASE_NEW_AUTO";
    public static final String INTENT_PURCHASE_NEW_TRIAL_MODE = "INTENT_PURCHASE_NEW_TRIAL_MODE";
    private static final String LOG_TAG = PurchaseAddActivity.class.getSimpleName();
    private static final String DISCARD_PURCHASE_DIALOG = "DISCARD_PURCHASE_DIALOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean inAutoMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_AUTO, false);
        boolean inTrialMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_TRIAL_MODE, false);

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            mPurchaseFragment = inAutoMode ?
                    PurchaseAddAutoFragment.newInstance(inTrialMode) :
                    new PurchaseAddFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.container, mPurchaseFragment)
                    .commit();
        } else {
            mPurchaseFragment = (PurchaseBaseFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_PURCHASE_FRAGMENT);
        }
    }

    @Override
    public void onOcrFinished(@NonNull PurchaseRest purchaseRest) {
        ((PurchaseAddAutoFragment) mPurchaseFragment).onOcrFinished(purchaseRest);
        showFab();
    }

    @Override
    public void onOcrFailed(@NonNull String errorMessage) {
        ((PurchaseAddAutoFragment) mPurchaseFragment).onOcrFailed(errorMessage);
        showFab();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                showPurchaseDiscardDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPurchaseDiscardDialog() {
        PurchaseDiscardDialogFragment purchaseDiscardDialogFragment =
                new PurchaseDiscardDialogFragment();
        purchaseDiscardDialogFragment.show(getFragmentManager(), DISCARD_PURCHASE_DIALOG);
    }

    @Override
    public void onSavePurchaseAsDraftSelected() {
        mPurchaseFragment.savePurchase(true);
    }

    @Override
    public void onDiscardPurchaseSelected() {
        mPurchaseFragment.onDiscardPurchaseSelected();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            showPurchaseDiscardDialog();
        }
    }
}
