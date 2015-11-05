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
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.query.MoreQueryHelper;
import ch.giantific.qwittig.data.helpers.query.PurchaseQueryHelper;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class HomePurchasesFragment extends BaseRecyclerViewOnlineFragment implements
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        PurchaseRepository.GetPurchasesLocalListener {

    public static final String INTENT_PURCHASE_ID = "INTENT_PURCHASE_ID";
    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private static final String STATE_ONLINE_QUERY = "STATE_ONLINE_QUERY";
    private static final String PURCHASE_QUERY_HELPER = "PURCHASE_QUERY_HELPER";
    private static final String LOG_TAG = HomePurchasesFragment.class.getSimpleName();
    private PurchaseRepository mPurchaseRepo;
    private PurchasesRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseObject> mPurchases = new ArrayList<>();
    private boolean mIsLoadingMore;
    private boolean mOnlineQueryInProgress;

    public HomePurchasesFragment() {
    }

    public void setOnlineQueryInProgress(boolean onlineQueryInProgress) {
        mOnlineQueryInProgress = onlineQueryInProgress;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPurchaseRepo = new ParsePurchaseRepository();

        if (savedInstanceState != null) {
            mIsLoadingMore = savedInstanceState.getBoolean(STATE_IS_LOADING_MORE, false);
            mOnlineQueryInProgress = savedInstanceState.getBoolean(STATE_ONLINE_QUERY, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING_MORE, mIsLoadingMore);
        outState.putBoolean(STATE_ONLINE_QUERY, mOnlineQueryInProgress);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_purchases, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new PurchasesRecyclerAdapter(getActivity(), mPurchases, mCurrentUser, this);
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
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onPurchaseUpdateFailed(int errorCode) {
        Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(context, errorCode));
        HelperUtils.removeHelper(getFragmentManager(), PURCHASE_QUERY_HELPER);

        setLoading(false);
    }

    /**
     * Re-queries the data and updates the adapter.
     */
    public void onPurchasesUpdated() {
        updateAdapter();
    }

    /**
     * Removes the retained helper fragment and loading indicators.
     */
    public void onAllPurchasesUpdated() {
        HelperUtils.removeHelper(getFragmentManager(), PURCHASE_QUERY_HELPER);
        setLoading(false);
    }

    @Override
    protected void updateAdapter() {
        mPurchaseRepo.getPurchasesLocalAsync(mCurrentUser, false, this);
    }

    @Override
    public void onPurchasesLocalLoaded(@NonNull List<ParseObject> purchases) {
        mPurchases.clear();

        if (!purchases.isEmpty()) {
            mPurchases.addAll(purchases);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrencyWithFallback(mCurrentGroup));
        mRecyclerAdapter.notifyDataSetChanged();
        showMainView();

        if (mIsLoadingMore) {
            mRecyclerAdapter.showLoadMoreIndicator();
            mRecyclerView.scrollToPosition(mRecyclerAdapter.getLastPosition());
        }
    }

    @Override
    void showMainView() {
        if (!mOnlineQueryInProgress) {
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
        getActivity().startActivityForResult(intent, BaseActivity.INTENT_REQUEST_PURCHASE_DETAILS,
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
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> purchases) {
        HelperUtils.removeHelper(getFragmentManager(), MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addPurchases(purchases);
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown during the process
     */
    public void onMoreObjectsLoadFailed(int errorCode) {
        Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(context, errorCode));
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
}
