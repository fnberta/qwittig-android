/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BasePurchaseAddEditReceiptFragment<T, S extends PurchaseAddEditViewModel> extends BaseFragment<T, S, BaseFragment.ActivityListener<T>> {

    private FragmentPurchaseShowReceiptBinding mBinding;

    public BasePurchaseAddEditReceiptFragment() {
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
        mBinding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt_fragment, menu);

        if (mViewModel.isReceiptAvailable()) {
            menu.findItem(R.id.action_purchase_edit_receipt_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_edit_receipt_delete).setVisible(true);
        } else {
            menu.findItem(R.id.action_purchase_edit_receipt_add).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_receipt_add:
                // fall through
            case R.id.action_purchase_edit_receipt_edit:
                mViewModel.onAddEditReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mViewModel.onDeleteReceiptMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivReceipt;
    }
}
