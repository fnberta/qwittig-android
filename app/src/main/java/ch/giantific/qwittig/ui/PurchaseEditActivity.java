package ch.giantific.qwittig.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;


public class PurchaseEditActivity extends PurchaseBaseActivity implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    private static final String PURCHASE_EDIT_FRAGMENT = "purchase_edit_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        String editPurchaseId = getIntent().getStringExtra(HomePurchasesFragment.INTENT_PURCHASE_ID);
        boolean inDraftMode = getIntent().getBooleanExtra(
                HomeDraftsFragment.INTENT_PURCHASE_EDIT_DRAFT, false);

        if (savedInstanceState == null) {
            PurchaseEditFragment fragment;
            if (inDraftMode) {
                fragment = PurchaseEditDraftFragment.newInstance(editPurchaseId);
            } else {
                fragment = PurchaseEditFragment.newInstance(editPurchaseId);
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment,
                            PURCHASE_EDIT_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void findPurchaseFragment() {
        mPurchaseFragment = (PurchaseEditFragment) getFragmentManager()
                .findFragmentByTag(PURCHASE_EDIT_FRAGMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                String message;
                if (mReceiptParseFile != null) {
                    message = getString(R.string.toast_receipt_changed);
                } else {
                    message = getString(R.string.toast_receipt_added);
                }
                MessageUtils.showBasicSnackbar(mToolbar, message);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkForChangesAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkForChangesAndExit() {
        if (((PurchaseEditFragment) mPurchaseFragment).changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            setResultForSnackbar(PURCHASE_NO_CHANGES);
            finishPurchase();
        }
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), "discard_changes");
    }

    @Override
    public void discardChanges() {
        setResultForSnackbar(PURCHASE_DISCARDED);
        finishPurchase();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            checkForChangesAndExit();
        }
    }
}
