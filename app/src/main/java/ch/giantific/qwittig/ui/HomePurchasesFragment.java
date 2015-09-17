package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.helpers.PurchaseQueryHelper;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.ui.listeners.InfiniteScrollListener;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class HomePurchasesFragment extends BaseRecyclerViewFragment implements
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        LocalQuery.PurchaseLocalQueryListener {

    public static final String INTENT_PURCHASE_ID = "purchase_id";
    public static final String INTENT_THEME = "intent_theme";
    private static final String STATE_IS_LOADING_MORE = "state_is_loading_more";
    private static final String PURCHASE_QUERY_HELPER = "purchase_query_helper";
    private static final String LOG_TAG =  HomePurchasesFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private PurchasesRecyclerAdapter mRecyclerAdapter;
    private InfiniteScrollListener mScrollListener;
    private List<ParseObject> mPurchases = new ArrayList<>();
    private boolean mIsLoadingMore;

    public HomePurchasesFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsLoadingMore = savedInstanceState.getBoolean(STATE_IS_LOADING_MORE, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING_MORE, mIsLoadingMore);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_purchases, container, false);
        findBaseViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new PurchasesRecyclerAdapter(getActivity(),
                R.layout.row_purchases, mPurchases, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mScrollListener = new InfiniteScrollListener(mLayoutManager, mRecyclerView) {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        PurchaseQueryHelper PurchaseQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (PurchaseQueryHelper == null) {
            PurchaseQueryHelper = new PurchaseQueryHelper();

            fragmentManager.beginTransaction()
                    .add(PurchaseQueryHelper, PURCHASE_QUERY_HELPER)
                    .commit();
        }
    }

    private PurchaseQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (PurchaseQueryHelper) fragmentManager.findFragmentByTag(PURCHASE_QUERY_HELPER);
    }

    /**
     * Called from activity when helper fails to pin new purchases
     * @param e
     */
    public void onPurchasesPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeQueryHelper();

        setLoading(false);
    }

    public void onPurchasesPinned() {
        updateAdapter();
    }

    /**
     * Called from activity when all purchases queries are finished
     */
    public void onAllPurchasesQueriesFinished() {
        removeQueryHelper();
        setLoading(false);
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseQueryHelper purchaseQueryHelper = findQueryHelper(fragmentManager);

        if (purchaseQueryHelper != null) {
            fragmentManager.beginTransaction().remove(purchaseQueryHelper).commitAllowingStateLoss();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryPurchases(this);
    }

    @Override
    public void onPurchasesLocalQueried(List<ParseObject> purchases) {
        mPurchases.clear();

        if (!purchases.isEmpty()) {
            for (ParseObject purchase : purchases) {
                mPurchases.add(purchase);
            }
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility();

        if (mIsLoadingMore) {
            int purchasesSize = mPurchases.size();
            addLoadMoreProgressBar(purchasesSize);
            mRecyclerView.scrollToPosition(purchasesSize);
        }
    }

    @Override
    void toggleMainVisibility() {
        if (!mListener.isNewQueryNeeded()) {
            super.toggleMainVisibility();
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
        if (mSwipeRefreshLayout.isRefreshing()) {
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            showLoadMoreErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        if (!mIsLoadingMore) {
            mIsLoadingMore = true;

            int purchasesSize = mPurchases.size();
            addLoadMoreProgressBar(purchasesSize);
            loadMoreDataWithHelper(purchasesSize);
        }
    }

    private void addLoadMoreProgressBar(int progressBarPosition) {
        mPurchases.add(null);
        mRecyclerAdapter.notifyItemInserted(progressBarPosition);
    }
    
    private void loadMoreDataWithHelper(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        MoreQueryHelper moreQueryHelper = findMoreQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryHelper == null) {
            moreQueryHelper = MoreQueryHelper.newInstance(Purchase.CLASS, skip);

            fragmentManager.beginTransaction()
                    .add(moreQueryHelper, MoreQueryHelper.MORE_QUERY_HELPER)
                    .commit();
        }
    }

    private MoreQueryHelper findMoreQueryHelper(FragmentManager fragmentManager) {
        return (MoreQueryHelper) fragmentManager.findFragmentByTag(MoreQueryHelper.MORE_QUERY_HELPER);
    }

    private void removeMoreQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        MoreQueryHelper moreQueryHelper = findMoreQueryHelper(fragmentManager);

        if (moreQueryHelper != null) {
            fragmentManager.beginTransaction().remove(moreQueryHelper).commitAllowingStateLoss();
        }
    }

    public void onMoreObjectsPinned(List<ParseObject> objects) {
        removeMoreQueryHelper();
        mIsLoadingMore = false;

        int progressBarPosition = mPurchases.size() - 1;
        mPurchases.remove(progressBarPosition);
        mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

        if (!objects.isEmpty()) {
            for (ParseObject purchase : objects) {
                mPurchases.add(purchase);
            }

            mRecyclerAdapter.notifyItemRangeInserted(progressBarPosition, objects.size());
        }
    }

    public void onMoreObjectsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeMoreQueryHelper();

        mIsLoadingMore = false;

        if (!mPurchases.isEmpty()) {
            int progressBarPosition = mPurchases.size() - 1;
            if (mPurchases.get(progressBarPosition) == null) {
                mPurchases.remove(progressBarPosition);
                mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

                // scroll to top, otherwise another "load more cycle" will be triggered immediately
                mRecyclerView.smoothScrollToPosition(0);
                // Reset previousTotal to zero (after a short delay to let the smooth scroll finish)
                // Otherwise the user won't be able to start a new "load more cycle"
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScrollListener.resetPreviousTotal();
                    }
                }, 200);
            }
        }
    }

    private void showLoadMoreErrorSnackbar(String errorMessage) {
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

    public interface FragmentInteractionListener {
        boolean isNewQueryNeeded();
    }
}
