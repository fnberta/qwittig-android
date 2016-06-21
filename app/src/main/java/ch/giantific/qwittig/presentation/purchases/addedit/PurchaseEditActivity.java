/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.DaggerPurchaseEditComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditViewModelModule;

/**
 * Hosts {@link PurchaseAddFragment} or {@link PurchaseEditDraftFragment} that handle the
 * editing of a purchase or draft..
 * <p/>
 * Asks the user if he wants to discard the possible changes when dismissing the activity.
 * <p/>
 * Subclass of {@link PurchaseAddActivity}.
 */
public class PurchaseEditActivity extends BasePurchaseAddEditActivity<PurchaseEditComponent> implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final String editPurchaseId = getIntent().getStringExtra(Navigator.INTENT_PURCHASE_ID);
        mComponent = DaggerPurchaseEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .purchaseEditViewModelModule(new PurchaseEditViewModelModule(savedInstanceState, editPurchaseId))
                .build();
        mComponent.inject(this);

        final boolean draft = getIntent().getBooleanExtra(Navigator.INTENT_PURCHASE_EDIT_DRAFT, false);
        mAddEditPurchaseViewModel = draft ? mComponent.getEditDraftViewModel() : mComponent.getEditViewModel();
        mAddEditPurchaseViewModel.attachView(this);
    }

    @NonNull
    @Override
    protected BasePurchaseAddEditFragment getPurchaseAddEditFragment() {
        final boolean draft = getIntent().getBooleanExtra(Navigator.INTENT_PURCHASE_EDIT_DRAFT, false);
        return draft ? new PurchaseEditDraftFragment() : new PurchaseEditFragment();
    }

    @Override
    protected BasePurchaseAddEditReceiptFragment getReceiptFragment() {
        return new PurchaseEditReceiptFragment();
    }

    @Override
    protected BasePurchaseAddEditNoteFragment getNoteFragment() {
        return new PurchaseEditNoteFragment();
    }

    @Override
    public void onDiscardChangesSelected() {
        mAddEditPurchaseViewModel.onDiscardChangesSelected();
    }
}
