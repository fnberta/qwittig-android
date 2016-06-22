/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

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
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseDetailsReceiptFragment extends BaseFragment<PurchaseDetailsSubcomponent, PurchaseDetailsViewModel, PurchaseDetailsReceiptFragment.ActivityListener> {

    private FragmentPurchaseShowReceiptBinding mBinding;

    public PurchaseDetailsReceiptFragment() {
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
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.setReceiptShown(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.setReceiptShown(false);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivReceipt;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_purchase_details_receipt_fragment, menu);

        if (mActivity.isShowEditOptions()) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (mActivity.isShowExchangeRate()) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_edit:
                mViewModel.onEditPurchaseClick();
                return true;
            case R.id.action_purchase_delete:
                mViewModel.onDeletePurchaseClick();
                return true;
            case R.id.action_purchase_show_exchange_rate:
                mViewModel.onShowExchangeRateClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Defines the interaction with the hosting Activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<PurchaseDetailsSubcomponent> {

        boolean isShowEditOptions();

        boolean isShowExchangeRate();
    }
}
