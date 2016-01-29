/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddEditBaseFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.DiscardChangesDialogFragment;

/**
 * Hosts {@link PurchaseAddFragment} or {@link PurchaseEditDraftFragment} that handle the
 * editing of a purchase or draft..
 * <p/>
 * Asks the user if he wants to discard the possible changes when dismissing the activity.
 * <p/>
 * Subclass of {@link PurchaseAddActivity}.
 */
public class PurchaseEditActivity extends PurchaseAddActivity implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    @NonNull
    @Override
    PurchaseAddEditBaseFragment getPurchaseAddEditFragment() {
        final String editPurchaseId = getIntent().getStringExtra(HomePurchasesFragment.INTENT_PURCHASE_ID);
        final boolean draft = getIntent().getBooleanExtra(HomeDraftsFragment.INTENT_PURCHASE_EDIT_DRAFT, false);
        return draft
                ? PurchaseEditDraftFragment.newInstance(editPurchaseId)
                : PurchaseEditFragment.newInstance(editPurchaseId);
    }

    @Override
    public void onDiscardChangesSelected() {
        mViewModel.onDiscardChangesSelected();
    }
}
