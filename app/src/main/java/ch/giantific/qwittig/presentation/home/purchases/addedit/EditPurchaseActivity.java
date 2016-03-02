/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;

/**
 * Hosts {@link AddPurchaseFragment} or {@link EditPurchaseDraftFragment} that handle the
 * editing of a purchase or draft..
 * <p/>
 * Asks the user if he wants to discard the possible changes when dismissing the activity.
 * <p/>
 * Subclass of {@link AddPurchaseActivity}.
 */
public class EditPurchaseActivity extends AddPurchaseActivity implements
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
    AddEditPurchaseBaseFragment getPurchaseAddEditFragment() {
        final String editPurchaseId = getIntent().getStringExtra(PurchasesFragment.INTENT_PURCHASE_ID);
        final boolean draft = getIntent().getBooleanExtra(DraftsFragment.INTENT_PURCHASE_EDIT_DRAFT, false);
        return draft
                ? EditPurchaseDraftFragment.newInstance(editPurchaseId)
                : EditPurchaseFragment.newInstance(editPurchaseId);
    }

    @Override
    public void onDiscardChangesSelected() {
        mViewModel.onDiscardChangesSelected();
    }
}
