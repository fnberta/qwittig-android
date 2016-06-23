/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;

/**
 * Displays the interface where the user can edit a purchase draft. The user can either save the
 * changes again in the draft or save the purchase to the online database.
 * <p/>
 * Subclass of {@link PurchaseAddFragment}.
 */
public class PurchaseEditDraftFragment extends BasePurchaseAddEditFragment<PurchaseEditComponent, PurchaseEditDraftViewModel, BasePurchaseAddEditFragment.ActivityListener<PurchaseEditComponent>> {

    public PurchaseEditDraftFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseEditComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_edit_draft_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_save_changes_draft:
                mViewModel.onSaveAsDraftMenuClick();
                return true;
            case R.id.action_purchase_edit_draft_delete:
                mViewModel.onDeleteDraftMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
