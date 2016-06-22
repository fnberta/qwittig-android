/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseRecyclerViewFragment<PurchaseDetailsSubcomponent, PurchaseDetailsViewModel, PurchaseDetailsFragment.ActivityListener> {

    private FragmentPurchaseDetailsBinding mBinding;

    public PurchaseDetailsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseDetailsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.setListInteraction(mRecyclerAdapter);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvPurchaseDetails;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new PurchaseDetailsRecyclerAdapter(mViewModel);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_purchase_details_fragment, menu);

        if (mActivity.isShowEditOptions()) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (mActivity.isShowExchangeRate()) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }

        if (mActivity.isShowReceipt()) {
            menu.findItem(R.id.action_purchase_show_receipt).setVisible(true);
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
            case R.id.action_purchase_show_receipt:
                mViewModel.onShowReceiptImageClick();
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

        boolean isShowReceipt();
    }
}
