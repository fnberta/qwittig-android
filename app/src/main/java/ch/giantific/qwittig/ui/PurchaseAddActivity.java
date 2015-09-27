package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.helpers.OcrHelper;
import ch.giantific.qwittig.ui.dialogs.PurchaseDiscardDialogFragment;


public class PurchaseAddActivity extends PurchaseBaseActivity implements
        PurchaseDiscardDialogFragment.DialogInteractionListener,
        OcrHelper.HelperInteractionListener {

    public static final String INTENT_PURCHASE_NEW_AUTO = "purchase_new_auto";
    public static final String INTENT_PURCHASE_NEW_TRIAL_MODE = "purchase_new_trial_mode";
    private static final String LOG_TAG = PurchaseAddActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean inAutoMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_AUTO, false);
        boolean inTrialMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_TRIAL_MODE, false);

        if (savedInstanceState == null) {
            PurchaseBaseFragment fragment = inAutoMode ?
                    PurchaseAddAutoFragment.newInstance(inTrialMode) :
                    new PurchaseAddFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, PURCHASE_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public void onOcrSuccessful(PurchaseRest purchaseRest) {
        ((PurchaseAddAutoFragment) mPurchaseFragment).onOcrSuccessful(purchaseRest);
        showFab();
    }

    @Override
    public void onOcrFailed(String errorMessage) {
        ((PurchaseAddAutoFragment) mPurchaseFragment).onOcrFailed(errorMessage);
        showFab();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        purchaseDiscardDialogFragment.show(getFragmentManager(), "discard_purchase");
    }

    @Override
    public void savePurchaseAsDraft() {
        mPurchaseFragment.savePurchase(true);
    }

    @Override
    public void discardPurchase() {
        mPurchaseFragment.discard();
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
