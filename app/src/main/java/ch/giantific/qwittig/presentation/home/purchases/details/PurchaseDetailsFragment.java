/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.home.purchases.details.di.DaggerPurchaseDetailsComponent;
import ch.giantific.qwittig.presentation.home.purchases.details.di.PurchaseDetailsViewModelModule;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditActivity;

import static ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment.INTENT_PURCHASE_ID;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 * <p/>
 * Subclass of {@link PurchaseDetailsFragment}.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseRecyclerViewFragment<PurchaseDetailsViewModel, PurchaseDetailsFragment.ActivityListener>
        implements PurchaseDetailsViewModel.ViewListener {

    private static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    private boolean mShowEditOptions;
    private boolean mHasForeignCurrency;
    private boolean mHasReceiptFile;
    private FragmentPurchaseDetailsBinding mBinding;

    public PurchaseDetailsFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link PurchaseDetailsFragment}.
     *
     * @param purchaseId the object id of the purchase
     * @return a new instance of {@link PurchaseDetailsFragment}
     */
    @NonNull
    public static PurchaseDetailsFragment newInstance(@NonNull String purchaseId) {
        PurchaseDetailsFragment fragment = new PurchaseDetailsFragment();

        Bundle args = new Bundle();
        args.putString(INTENT_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final String purchaseId = getArguments().getString(INTENT_PURCHASE_ID, "");
        DaggerPurchaseDetailsComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .purchaseDetailsViewModelModule(new PurchaseDetailsViewModelModule(savedInstanceState, this, purchaseId))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseDetailsBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_details_fragment, menu);

        if (mShowEditOptions) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (mHasForeignCurrency) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }

        if (mHasReceiptFile) {
            menu.findItem(R.id.action_purchase_show_receipt).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

    @Override
    protected void setViewModelToActivity() {
        mActivity.setDetailsViewModel(mViewModel);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvPurchaseDetails;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new PurchaseDetailsRecyclerAdapter(mViewModel);
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseEditScreen(@NonNull String purchaseId) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, PurchaseEditActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchaseId);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, BaseActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    @Override
    public void showReceiptImage(@NonNull String purchaseId) {
        final FragmentManager fragmentManager = getFragmentManager();
        final PurchaseReceiptDetailFragment fragment = PurchaseReceiptDetailFragment.newInstance(purchaseId);

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void toggleMenuOptions(boolean showEditOptions, boolean hasReceiptImage,
                                  boolean hasForeignCurrency) {
        mShowEditOptions = showEditOptions;
        mHasForeignCurrency = hasForeignCurrency;
        mHasReceiptFile = hasReceiptImage;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void finishScreen(int result) {
        final Activity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setDetailsViewModel(@NonNull PurchaseDetailsViewModel viewModel);
    }
}
