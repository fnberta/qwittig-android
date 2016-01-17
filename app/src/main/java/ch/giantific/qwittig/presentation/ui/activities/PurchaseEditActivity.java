/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseBaseFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.DiscardChangesDialogFragment;

/**
 * Hosts {@link PurchaseEditFragment} that handles the editing of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the possible changes when dismissing the activity.
 * <p/>
 * Subclass of {@link PurchaseBaseActivity}.
 */
public class PurchaseEditActivity extends PurchaseBaseActivity implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((PurchaseEditFragment) mPurchaseFragment).checkForChangesAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDiscardChangesSelected() {
        mPurchaseFragment.onDiscardPurchaseSelected();
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
