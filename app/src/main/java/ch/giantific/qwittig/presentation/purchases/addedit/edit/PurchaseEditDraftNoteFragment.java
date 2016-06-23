/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditNoteFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;

/**
 * Displays the note of a purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseEditDraftNoteFragment extends BasePurchaseAddEditNoteFragment<PurchaseEditComponent, PurchaseEditDraftViewModel> {

    public PurchaseEditDraftNoteFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseEditComponent component) {
        component.inject(this);
    }
}
