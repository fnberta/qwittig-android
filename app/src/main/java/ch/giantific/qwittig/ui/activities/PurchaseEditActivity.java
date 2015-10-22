package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseEditDraftFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseEditFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;


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

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            mPurchaseFragment = inDraftMode ?
                    PurchaseEditDraftFragment.newInstance(editPurchaseId) :
                    PurchaseEditFragment.newInstance(editPurchaseId);

            fragmentManager.beginTransaction()
                    .add(R.id.container, mPurchaseFragment)
                    .commit();
        } else {
            mPurchaseFragment = (PurchaseBaseFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_PURCHASE_FRAGMENT);
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
