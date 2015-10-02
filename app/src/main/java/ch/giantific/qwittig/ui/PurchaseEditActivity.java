package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.dialogs.DiscardChangesDialogFragment;


public class PurchaseEditActivity extends PurchaseBaseActivity implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        String editPurchaseId = getIntent().getStringExtra(HomePurchasesFragment.INTENT_PURCHASE_ID);
        boolean inDraftMode = getIntent().getBooleanExtra(
                HomeDraftsFragment.INTENT_PURCHASE_EDIT_DRAFT, false);

        if (savedInstanceState == null) {
            PurchaseEditFragment fragment = inDraftMode ?
                    PurchaseEditDraftFragment.newInstance(editPurchaseId) :
                    PurchaseEditFragment.newInstance(editPurchaseId);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, PURCHASE_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((PurchaseEditFragment) mPurchaseFragment).checkForChangesAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void discardChanges() {
        mPurchaseFragment.discard();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            ((PurchaseEditFragment) mPurchaseFragment).checkForChangesAndExit();
        }
    }
}
