package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
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
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class HomePurchasesFragment extends BaseRecyclerViewFragment implements
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        LocalQuery.PurchaseLocalQueryListener {

    public static final String INTENT_PURCHASE_ID = "purchase_id";
    private static final String STATE_IS_LOADING_MORE = "state_is_loading_more";
    private static final String PURCHASE_QUERY_HELPER = "purchase_query_helper";
    private static final String LOG_TAG = HomePurchasesFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private PurchasesRecyclerAdapter mRecyclerAdapter;
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
        Fragment PurchaseQueryHelper = findHelper(fragmentManager, PURCHASE_QUERY_HELPER);

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
     * Called from activity when helper fails to pin new purchases
     *
     * @param e
     */
    public void onPurchasesPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeHelper(PURCHASE_QUERY_HELPER);

        setLoading(false);
    }

    public void onPurchasesPinned() {
        updateAdapter();
    }

    /**
     * Called from activity when all purchases queries are finished
     */
    public void onAllPurchasesQueriesFinished() {
        removeHelper(PURCHASE_QUERY_HELPER);
        setLoading(false);
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
            mPurchases.addAll(purchases);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility();

        if (mIsLoadingMore) {
            mRecyclerAdapter.showLoadMoreIndicator();
            mRecyclerView.scrollToPosition(mRecyclerAdapter.getLastPosition());
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
        mIsLoadingMore = true;
        final int skip = mPurchases.size();
        mRecyclerAdapter.showLoadMoreIndicator();
        loadMoreDataWithHelper(skip);
    }

    private void loadMoreDataWithHelper(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment moreQueryHelper = findHelper(fragmentManager, MoreQueryHelper.MORE_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryHelper == null) {
            moreQueryHelper = MoreQueryHelper.newInstance(Purchase.CLASS, skip);

            fragmentManager.beginTransaction()
                    .add(moreQueryHelper, MoreQueryHelper.MORE_QUERY_HELPER)
                    .commit();
        }
    }

    public void onMoreObjectsPinned(List<ParseObject> objects) {
        removeHelper(MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addPurchases(objects);
    }

    public void onMoreObjectsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeHelper(MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
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
