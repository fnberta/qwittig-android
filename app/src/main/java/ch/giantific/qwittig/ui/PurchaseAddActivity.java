package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.helper.OcrHelper;
import ch.giantific.qwittig.ui.dialogs.PurchaseDiscardDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import retrofit.RetrofitError;


public class PurchaseAddActivity extends PurchaseBaseActivity implements
        PurchaseDiscardDialogFragment.DialogInteractionListener,
        OcrHelper.HelperInteractionListener {

    public static final String INTENT_PURCHASE_NEW_AUTO = "purchase_new_auto";
    public static final String INTENT_PURCHASE_NEW_TRIAL_MODE = "purchase_new_trial_mode";
    private static final String PURCHASE_ADD_FRAGMENT = "purchase_add_fragment";
    private static final String OCR_HELPER = "ocr_helper";
    private static final String LOG_TAG = PurchaseAddActivity.class.getSimpleName();
    private boolean mInAutoMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if in autoMode, start Camera Activity via intent
        mInAutoMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_AUTO, false);
        boolean inTrialMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_TRIAL_MODE, false);

        if (savedInstanceState == null) {
            PurchaseBaseFragment fragment;
            if (mInAutoMode) {
                captureImage();
                fragment = PurchaseAddAutoFragment.newInstance(inTrialMode);
            } else {
                fragment = new PurchaseAddFragment();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, PURCHASE_ADD_FRAGMENT)
                    .commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setActivityTransition() {
        Transition transitionEnter = new Slide(Gravity.BOTTOM);
        transitionEnter.excludeTarget(android.R.id.statusBarBackground, true);
        transitionEnter.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(transitionEnter);
    }

    @Override
    public void findPurchaseFragment() {
        mPurchaseFragment = (PurchaseAddFragment) getFragmentManager()
                .findFragmentByTag(PURCHASE_ADD_FRAGMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (mInAutoMode) {
                    doReceiptOcrWithHelper();
                } else {
                    MessageUtils.showBasicSnackbar(mFabPurchaseSave, getString(R.string.toast_receipt_added));
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (mInAutoMode) {
                    finish();
                }
            }
        }
    }

    private void doReceiptOcrWithHelper() {
        if (!Utils.isConnected(this)) {
            onOcrFailed(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        OcrHelper ocrHelper = (OcrHelper) fragmentManager.findFragmentByTag(OCR_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (ocrHelper == null) {
            ocrHelper = OcrHelper.newInstance(mCurrentPhotoPath);

            fragmentManager.beginTransaction()
                    .add(ocrHelper, OCR_HELPER)
                    .commit();
        }
    }

    @Override
    public void onOcrSuccessful(PurchaseRest purchaseRest) {
        ((PurchaseAddAutoFragment) mPurchaseFragment).setValuesFromOcr(purchaseRest);
        showFab(false);
    }

    @Override
    public void onOcrFailed(String errorMessage) {
        MessageUtils.showBasicSnackbar(mToolbar, errorMessage);
        ((PurchaseAddAutoFragment) mPurchaseFragment).showMainScreen();
        showFab(true);
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
        setResultForSnackbar(PURCHASE_DISCARDED);
        finishPurchase();
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
