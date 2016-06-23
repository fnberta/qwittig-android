/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseAddComponent;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class PurchaseAddOcrFragment extends BasePurchaseAddEditFragment<PurchaseAddComponent, PurchaseAddOcrViewModel, BasePurchaseAddEditFragment.ActivityListener<PurchaseAddComponent>> {

    public PurchaseAddOcrFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseAddComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_add_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_save_draft:
                mViewModel.onSaveAsDraftMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
