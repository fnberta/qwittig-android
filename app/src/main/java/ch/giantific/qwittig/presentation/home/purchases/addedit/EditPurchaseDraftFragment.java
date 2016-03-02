/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.DaggerEditPurchaseComponent;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.EditPurchaseViewModelModule;

/**
 * Displays the interface where the user can edit a purchase draft. The user can either save the
 * changes again in the draft or save the purchase to the online database.
 * <p/>
 * Subclass of {@link AddPurchaseFragment}.
 */
public class EditPurchaseDraftFragment extends AddEditPurchaseBaseFragment<EditPurchaseDraftViewModel, AddEditPurchaseBaseFragment.ActivityListener> {

    public EditPurchaseDraftFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link EditPurchaseDraftFragment}.
     *
     * @param draftId the id of the draft to edit
     * @return a new instance of {@link EditPurchaseDraftFragment}
     */
    @NonNull
    public static EditPurchaseDraftFragment newInstance(@NonNull String draftId) {
        EditPurchaseDraftFragment fragment = new EditPurchaseDraftFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_EDIT_PURCHASE_ID, draftId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final String editPurchaseId = getArguments().getString(KEY_EDIT_PURCHASE_ID, "");
        DaggerEditPurchaseComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .editPurchaseViewModelModule(new EditPurchaseViewModelModule(savedInstanceState, this, editPurchaseId))
                .build()
                .inject(this);
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
                mViewModel.onDeleteDraftClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
