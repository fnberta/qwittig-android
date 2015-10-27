/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.helpers.PurchaseQueryHelper;
import ch.giantific.qwittig.ui.activities.HomeActivity;
import ch.giantific.qwittig.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class HomePurchasesFragment extends BaseRecyclerViewFragment implements
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        LocalQuery.PurchaseLocalQueryListener {

    public static final String INTENT_PURCHASE_ID = "INTENT_PURCHASE_ID";
    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private static final String PURCHASE_QUERY_HELPER = "PURCHASE_QUERY_HELPER";
    private static final String LOG_TAG = HomePurchasesFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private PurchasesRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseObject> mPurchases = new ArrayList<>();
    private boolean mIsLoadingMore;

    public HomePurchasesFragment() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsLoadingMore = savedInstanceState.getBoolean(STATE_IS_LOADING_MORE, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING_MORE, mIsLoadingMore);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_purchases, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new PurchasesRecyclerAdapter(getActivity(), mPurchases, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        Mugen.with(mRecyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }

            @Override
            public boolean isLoading() {
                return !Utils.isConnected(getActivity()) || mIsLoadingMore ||
                        mSwipeRefreshLayout.isRefreshing();
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment PurchaseQueryHelper = HelperUtils.findHelper(fragmentManager, PURCHASE_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (PurchaseQueryHelper == null) {
            PurchaseQueryHelper = new PurchaseQueryHelper();

            fragmentManager.beginTransaction()
                    .add(PurchaseQueryHelper, PURCHASE_QUERY_HELPER)
                    .commit();
        }
    }

    /**
     * Passes the {@link ParseException} to the generic error handler, shows the user an error
     * message and removes the retained helper fragment and loading indicators.
     *
     * @param e the {@link ParseException} thrown in the process
     */
    public void onPurchasesPinFailed(@NonNull ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), PURCHASE_QUERY_HELPER);

        setLoading(false);
    }

    /**
     * Re-queries the data and updates the adapter.
     */
    public void onPurchasesPinned() {
        updateAdapter();
    }

    /**
     * Removes the retained helper fragment and loading indicators.
     */
    public void onAllPurchasesQueried() {
        HelperUtils.removeHelper(getFragmentManager(), PURCHASE_QUERY_HELPER);
        setLoading(false);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryPurchases(this);
    }

    @Override
    public void onPurchasesLocalQueried(@NonNull List<ParseObject> purchases) {
        mPurchases.clear();

        if (!purchases.isEmpty()) {
            mPurchases.addAll(purchases);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        showMainView();

        if (mIsLoadingMore) {
            mRecyclerAdapter.showLoadMoreIndicator();
            mRecyclerView.scrollToPosition(mRecyclerAdapter.getLastPosition());
        }
    }

    @Override
    void showMainView() {
        if (!mListener.isNewQueryNeeded()) {
            super.showMainView();
        }
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mPurchases.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPurchaseRowItemClick(int position) {
        Purchase purchase = (Purchase) mPurchases.get(position);
        Intent intent = new Intent(getActivity(), PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchase.getObjectId());

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity());
        getActivity().startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    private void loadMoreData() {
        mIsLoadingMore = true;
        final int skip = mPurchases.size();
        mRecyclerAdapter.showLoadMoreIndicator();
        loadMoreDataWithHelper(skip);
    }

    private void loadMoreDataWithHelper(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment moreQueryHelper = HelperUtils.findHelper(fragmentManager,
                MoreQueryHelper.MORE_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryHelper == null) {
            moreQueryHelper = MoreQueryHelper.newInstance(Purchase.CLASS, skip);
            fragmentManager.beginTransaction()
                    .add(moreQueryHelper, MoreQueryHelper.MORE_QUERY_HELPER)
                    .commit();
        }
    }

    /**
     * Adds the newly pinned purchases to the list, removes the retained helper fragment and
     * loading indicators.
     *
     * @param purchases the newly pinned purchases
     */
    public void onMoreObjectsPinned(@NonNull List<ParseObject> purchases) {
        HelperUtils.removeHelper(getFragmentManager(), MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addPurchases(purchases);
    }

    /**
     * Passes the {@link ParseException} to the generic error handler, showing the user an error
     * message and removing the retained helper fragment and loading indicators.
     *
     * @param e the {@link ParseException} thrown during the process
     */
    public void onMoreObjectsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
    }

    private void showLoadMoreErrorSnackbar(@NonNull String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });
        snackbar.show();
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
         * Gets the information whether a new online query is needed or not.
         *
         * @return whether a new online query is needed or not
         */
        boolean isNewQueryNeeded();
    }
}
