/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.di.components.DaggerPurchaseAddEditComponent;
import ch.giantific.qwittig.di.components.PurchaseAddEditComponent;
import ch.giantific.qwittig.di.modules.PurchaseEditViewModelModule;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseEditDraftViewModel;

/**
 * Displays the interface where the user can edit a purchase draft. The user can either save the
 * changes again in the draft or save the purchase to the online database.
 * <p/>
 * Subclass of {@link PurchaseAddFragment}.
 */
public class PurchaseEditDraftFragment extends PurchaseAddEditBaseFragment<PurchaseEditDraftViewModel, PurchaseAddEditBaseFragment.ActivityListener> {

    public PurchaseEditDraftFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link PurchaseEditDraftFragment}.
     *
     * @param draftId the id of the draft to edit
     * @return a new instance of {@link PurchaseEditDraftFragment}
     */
    @NonNull
    public static PurchaseEditDraftFragment newInstance(@NonNull String draftId) {
        PurchaseEditDraftFragment fragment = new PurchaseEditDraftFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_EDIT_PURCHASE_ID, draftId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final String editPurchaseId = getArguments().getString(KEY_EDIT_PURCHASE_ID, "");
        DaggerPurchaseAddEditComponent.builder()
                .purchaseEditViewModelModule(new PurchaseEditViewModelModule(savedInstanceState, editPurchaseId))
                .build()
                .inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_edit_draft_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_save_changes_draft:
                mViewModel.onSavePurchaseAsDraftClick();
                return true;
            case R.id.action_purchase_edit_draft_delete:
                mViewModel.onDeleteDraftClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
