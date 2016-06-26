/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseShowNoteBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;

/**
 * Displays the note of a purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BasePurchaseAddEditNoteFragment<T, S extends PurchaseAddEditViewModel> extends BaseFragment<T, S, BaseFragment.ActivityListener<T>> {

    private FragmentPurchaseShowNoteBinding mBinding;

    public BasePurchaseAddEditNoteFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseShowNoteBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.setNoteShown(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.setNoteShown(false);
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
                mViewModel.onAddEditNoteMenuClick();
                return true;
            case R.id.action_purchase_edit_note_delete:
                mViewModel.onDeleteNoteMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.tvPurchaseNote;
    }
}
