/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditNoteFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseAddComponent;

/**
 * Displays the note of a purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseAddNoteFragment extends BasePurchaseAddEditNoteFragment<PurchaseAddComponent, PurchaseAddEditViewModel> {

    public PurchaseAddNoteFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseAddComponent component) {
        component.inject(this);
    }
}
