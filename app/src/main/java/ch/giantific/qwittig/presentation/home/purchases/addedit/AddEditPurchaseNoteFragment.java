/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseShowNoteBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.AddEditPurchaseNoteViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.DaggerAddEditPurchaseNoteComponent;

/**
 * Displays the note of a purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class AddEditPurchaseNoteFragment extends BaseFragment<AddEditPurchaseNoteViewModel, AddEditPurchaseNoteFragment.ActivityListener>
        implements AddEditPurchaseNoteViewModel.ViewListener {

    private static final String KEY_NOTE = "NOTE";
    private FragmentPurchaseShowNoteBinding mBinding;

    public AddEditPurchaseNoteFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link AddEditPurchaseNoteFragment}.
     *
     * @param note the note to display
     * @return a new instance of {@link AddEditPurchaseNoteFragment}
     */
    public static AddEditPurchaseNoteFragment newInstance(@NonNull String note) {
        final AddEditPurchaseNoteFragment fragment = new AddEditPurchaseNoteFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final String note = getArguments().getString(KEY_NOTE, "");
        DaggerAddEditPurchaseNoteComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .addEditPurchaseNoteViewModelModule(new AddEditPurchaseNoteViewModelModule(savedInstanceState, this, note))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseShowNoteBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_note_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_edit_note_edit:
                mViewModel.onEditNoteMenuClick();
                return true;
            case R.id.action_purchase_edit_note_delete:
                mViewModel.onDeleteNoteMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setNoteViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.tvPurchaseNote;
    }

    @Override
    public void showEditNoteDialog(@NonNull String note) {
        NoteDialogFragment.display(getFragmentManager(), note);
    }

    @Override
    public void showPurchaseScreen() {
        mActivity.popBackStack();
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model
         */
        void setNoteViewModel(@NonNull AddEditPurchaseNoteViewModel viewModel);

        /**
         * Handles the call to delete the note from the purchase.
         */
        void popBackStack();
    }
}
