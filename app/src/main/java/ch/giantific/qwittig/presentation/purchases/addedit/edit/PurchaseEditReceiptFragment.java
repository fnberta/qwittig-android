/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditReceiptFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseEditReceiptFragment extends BasePurchaseAddEditReceiptFragment<PurchaseEditComponent, PurchaseAddEditContract.Presenter> {

    public PurchaseEditReceiptFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseEditComponent component) {
        component.inject(this);
    }
}
