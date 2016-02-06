/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.di.components.DaggerPurchaseEditComponent;
import ch.giantific.qwittig.di.modules.PurchaseEditViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class PurchaseEditFragment extends PurchaseAddEditBaseFragment<PurchaseAddEditViewModel, PurchaseAddEditBaseFragment.ActivityListener>
        implements PurchaseAddEditViewModel.ViewListener {

    public PurchaseEditFragment() {
        // required empty constructor
    }

    /**
     * Returns a new edit instance of {@link PurchaseEditFragment}.
     *
     * @param editPurchaseId the object id of the purchase to edit
     * @return a new instance of {@link PurchaseEditFragment}
     */
    @NonNull
    public static PurchaseEditFragment newInstance(@NonNull String editPurchaseId) {
        PurchaseEditFragment fragment = new PurchaseEditFragment();
        Bundle args = new Bundle();
        args.putString(KEY_EDIT_PURCHASE_ID, editPurchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String editPurchaseId = getArguments().getString(KEY_EDIT_PURCHASE_ID, "");
        DaggerPurchaseEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .purchaseEditViewModelModule(new PurchaseEditViewModelModule(savedInstanceState, this, editPurchaseId))
                .build()
                .inject(this);
    }
}
