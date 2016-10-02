/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BasePurchaseAddEditFragment}.
 */
public class PurchaseEditFragment extends BasePurchaseAddEditFragment<PurchaseEditComponent, PurchaseAddEditContract.Presenter, BasePurchaseAddEditFragment.ActivityListener<PurchaseEditComponent>> {

    public PurchaseEditFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseEditComponent component) {
        component.inject(this);
    }
}
