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
import ch.giantific.qwittig.presentation.common.BaseFragment;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BasePurchaseAddEditReceiptFragment<T, S extends PurchaseAddEditContract.Presenter> extends BaseFragment<T, S, BaseFragment.ActivityListener<T>> {

    private FragmentPurchaseShowReceiptBinding binding;

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
        binding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt_fragment, menu);

        if (presenter.getViewModel().isReceiptAvailable()) {
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
                presenter.onAddEditReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                presenter.onDeleteReceiptMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarView() {
        return binding.ivReceipt;
    }
}
