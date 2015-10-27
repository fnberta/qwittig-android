/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.ui.activities.PurchaseEditActivity;
import ch.giantific.qwittig.ui.adapters.PurchaseDetailsRecyclerAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 * <p/>
 * Subclass of {@link PurchaseDetailsFragment}.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    private static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    private static final String LOG_TAG = PurchaseDetailsFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private String mPurchaseId;
    private Purchase mPurchase;
    private PurchaseDetailsRecyclerAdapter mRecyclerAdapter;
    private User mCurrentUser;
    private Group mCurrentGroup;
    private boolean mHasReceiptFile;

    public PurchaseDetailsFragment() {
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
        args.putString(HomePurchasesFragment.INTENT_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mPurchaseId = args.getString(HomePurchasesFragment.INTENT_PURCHASE_ID, "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_purchase_details);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_purchase_details);
        mRecyclerAdapter = new PurchaseDetailsRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
            if (mCurrentGroup != null) {
                queryData();
            } else {
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_error_purchase_details_group_not));
            }
        }
    }

    /**
     * Queries the data of the purchase from the local data store in order to display it to the
     * user.
     * <p/>
     * Uses a query instead of a fetchFromLocalDatastore because a fetch would not include
     * the data for the pointers.
     */
    public void queryData() {
        LocalQuery.queryPurchase(mPurchaseId, this);
    }

    @Override
    public void onObjectFetched(@NonNull ParseObject object) {
        mPurchase = (Purchase) object;

        updateToolbarTitle();
        updateActionBarMenu();

        mRecyclerAdapter.setPurchase(object);
        mRecyclerAdapter.notifyDataSetChanged();

        toggleMainViewVisibility();
        ActivityCompat.startPostponedEnterTransition(getActivity());

        updateReadBy();
    }

    private void updateToolbarTitle() {
        mListener.setToolbarStoreAndDate(mPurchase.getStore(), mPurchase.getDate());
    }

    /**
     * Checks if the current user is the buyer of the purchase, if yes shows the delete/edit options
     * in the ActionBar of the hosting activity. Checks also if the purchase has a receipt file,
     * if yes shows option to display it in the ActionBar of the hosting activity.
     */
    private void updateActionBarMenu() {
        List<ParseUser> usersInvolved = mPurchase.getUsersInvolved();
        boolean allUsersAreValid = true;

        for (ParseUser parseUser : usersInvolved) {
            User user = (User) parseUser;
            if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                allUsersAreValid = false;
                break;
            }
        }

        mHasReceiptFile = mPurchase.getReceiptParseFile() != null;
        // invalidateOptionsMenu will be called in toggleActionBarOptions()

        boolean userIsBuyer = false;
        if (allUsersAreValid) {
            String buyerId = mPurchase.getBuyer().getObjectId();
            userIsBuyer = buyerId.equals(mCurrentUser.getObjectId());
        }
        boolean hasForeignCurrency = !mCurrentGroup.getCurrency().equals(mPurchase.getCurrency());
        mListener.toggleActionBarOptions(userIsBuyer, hasForeignCurrency);
    }

    private void toggleMainViewVisibility() {
        boolean purchaseIsNull = mPurchase == null;
        mRecyclerView.setVisibility(purchaseIsNull ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(purchaseIsNull ? View.VISIBLE : View.GONE);
    }

    private void updateReadBy() {
        if (!mPurchase.currentUserHasReadPurchase() &&
                !ParseUtils.isTestUser(ParseUser.getCurrentUser())) {
            mPurchase.addCurrentUserToReadBy();
            mPurchase.saveEventually();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_details_frag, menu);

        if (mHasReceiptFile) {
            menu.findItem(R.id.action_purchase_show_receipt).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_show_receipt:
                replaceWithReceiptFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceWithReceiptFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseReceiptDetailFragment purchaseReceiptDetailFragment =
                PurchaseReceiptDetailFragment.newInstance(mPurchaseId);
        fragmentManager.beginTransaction()
                .replace(R.id.container, purchaseReceiptDetailFragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Starts {@link PurchaseEditActivity} to edit the purchase.
     */
    public void editPurchase() {
        Intent intent = new Intent(getActivity(), PurchaseEditActivity.class);
        intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, mPurchaseId);
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    /**
     * Deletes the purchase and all of its items and finishes if the user is not a test user.
     */
    public void deletePurchase() {
        if (!ParseUtils.isTestUser(ParseUser.getCurrentUser())) {
            mPurchase.deleteEventually();

            finish(PurchaseDetailsActivity.RESULT_PURCHASE_DELETED);
        } else {
            mListener.showAccountCreateDialog();
        }
    }

    private void finish(int result) {
        Activity activity = getActivity();
        activity.setResult(result);
        activity.finish();
    }

    /**
     * Shows the user a {@link Snackbar} with the currency exchange rate used in the purchase.
     */
    public void showExchangeRate() {
        float exchangeRate = mPurchase.getExchangeRate();
        String message = getString(R.string.toast_exchange_rate_value,
                MoneyUtils.formatMoneyNoSymbol(exchangeRate,
                        MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS));
        MessageUtils.showBasicSnackbar(mRecyclerView, message);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Sets the store name and the date of the purchase.
         *
         * @param store the store name to set
         * @param date  the date to set
         */
        void setToolbarStoreAndDate(String store, Date date);

        /**
         * Toggles whether the edit/delete and show currency exchange rate should be shown or not.
         *
         * @param showEditOptions    whether to show the edit/delete options or not
         * @param hasForeignCurrency whether the purchase was saved with a foreign currency
         */
        void toggleActionBarOptions(boolean showEditOptions, boolean hasForeignCurrency);
    }
}
