/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.workerfragments.query.MoreQueryWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateWorker;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.ui.activities.BaseActivity;
import ch.giantific.qwittig.presentation.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
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
    private static final String PURCHASE_QUERY_WORKER = "PURCHASE_QUERY_WORKER";
    private static final String LOG_TAG = HomePurchasesFragment.class.getSimpleName();
    private PurchaseRepository mPurchaseRepo;
    private PurchasesRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseObject> mPurchases = new ArrayList<>();
    private boolean mIsLoadingMore;

    public HomePurchasesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPurchaseRepo = new ParsePurchaseRepository(getActivity());

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
                return !Utils.isNetworkAvailable(getActivity()) || mIsLoadingMore ||
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
        if (!Utils.isNetworkAvailable(getActivity())) {
            setLoading(false);
            showErrorSnackbar(R.string.toast_no_connection, getOnlineQueryRetryAction());
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment PurchaseQueryWorker = WorkerUtils.findWorker(fragmentManager, PURCHASE_QUERY_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (PurchaseQueryWorker == null) {
            PurchaseQueryWorker = new PurchasesUpdateWorker();

            fragmentManager.beginTransaction()
                    .add(PurchaseQueryWorker, PURCHASE_QUERY_WORKER)
                    .commit();
        }
    }

    /**
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown in the process
     */
    public void onPurchaseUpdateFailed(@StringRes int errorMessage) {
        showErrorSnackbar(errorMessage, getOnlineQueryRetryAction());
        WorkerUtils.removeWorker(getFragmentManager(), PURCHASE_QUERY_WORKER);

        setLoading(false);
    }

    /**
     * Re-queries the data and updates the adapter.
     */
    public void onPurchasesUpdated() {
        updateAdapter();
    }

    /**
     * Removes the retained worker fragment and loading indicators.
     */
    public void onAllPurchasesUpdated() {
        WorkerUtils.removeWorker(getFragmentManager(), PURCHASE_QUERY_WORKER);
        setLoading(false);
    }

    @Override
    protected void updateAdapter() {
        mPurchaseRepo.getPurchasesLocalAsync(mCurrentUser, false);
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
        loadMoreDataWithWorker(skip);
    }

    private void loadMoreDataWithWorker(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment moreQueryWorker = WorkerUtils.findWorker(fragmentManager,
                MoreQueryWorker.MORE_QUERY_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryWorker == null) {
            moreQueryWorker = MoreQueryWorker.newInstance(Purchase.CLASS, skip);
            fragmentManager.beginTransaction()
                    .add(moreQueryWorker, MoreQueryWorker.MORE_QUERY_WORKER)
                    .commit();
        }
    }

    /**
     * Adds the newly pinned purchases to the list, removes the retained worker fragment and
     * loading indicators.
     *
     * @param purchases the newly pinned purchases
     */
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> purchases) {
        WorkerUtils.removeWorker(getFragmentManager(), MoreQueryWorker.MORE_QUERY_WORKER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addItems(purchases);
    }

    /**
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    public void onMoreObjectsLoadFailed(@StringRes int errorMessage) {
        showErrorSnackbar(errorMessage, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });
        WorkerUtils.removeWorker(getFragmentManager(), MoreQueryWorker.MORE_QUERY_WORKER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
    }
}
