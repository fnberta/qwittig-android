/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.DaggerEditPurchaseComponent;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.EditPurchaseViewModelModule;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class EditPurchaseFragment extends AddEditPurchaseBaseFragment<AddEditPurchaseViewModel, AddEditPurchaseBaseFragment.ActivityListener>
        implements AddEditPurchaseViewModel.ViewListener {

    public EditPurchaseFragment() {
        // required empty constructor
    }

    /**
     * Returns a new edit instance of {@link EditPurchaseFragment}.
     *
     * @param editPurchaseId the object id of the purchase to edit
     * @return a new instance of {@link EditPurchaseFragment}
     */
    @NonNull
    public static EditPurchaseFragment newInstance(@NonNull String editPurchaseId) {
        final EditPurchaseFragment fragment = new EditPurchaseFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_EDIT_PURCHASE_ID, editPurchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String editPurchaseId = getArguments().getString(KEY_EDIT_PURCHASE_ID, "");
        DaggerEditPurchaseComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .editPurchaseViewModelModule(new EditPurchaseViewModelModule(savedInstanceState, this, editPurchaseId))
                .build()
                .inject(this);
    }
}
