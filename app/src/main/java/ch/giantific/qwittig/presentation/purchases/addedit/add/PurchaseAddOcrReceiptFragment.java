/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditReceiptFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseAddComponent;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseAddOcrReceiptFragment extends BasePurchaseAddEditReceiptFragment<PurchaseAddComponent,
        PurchaseAddEditContract.AddOcrPresenter> {

    public PurchaseAddOcrReceiptFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseAddComponent component) {
        component.inject(this);
    }
}
