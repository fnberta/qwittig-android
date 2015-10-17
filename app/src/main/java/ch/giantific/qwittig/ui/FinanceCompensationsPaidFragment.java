package ch.giantific.qwittig.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.ui.adapters.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class FinanceCompensationsPaidFragment extends FinanceCompensationsBaseFragment implements
        LocalQuery.CompensationLocalQueryListener {

    private static final String COMPENSATION_QUERY_HELPER = "compensation_paid_query_helper";
    private static final String STATE_IS_LOADING_MORE = "state_is_loading_more";
    private static final String LOG_TAG = FinanceCompensationsPaidFragment.class.getSimpleName();
    private CompensationsPaidRecyclerAdapter mRecyclerAdapter;
    private List<ParseObject> mCompensations = new ArrayList<>();
    private boolean mIsLoadingMore;

    public FinanceCompensationsPaidFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finance_compensations_paid, container, false);
        findBaseViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsPaidRecyclerAdapter(getActivity(),
                R.layout.row_compensations_paid, mCompensations);
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
    protected String getQueryHelperTag() {
        return COMPENSATION_QUERY_HELPER;
    }

    @Override
    protected void onlineQuery() {
        onlineQuery(true);
    }

    /**
     * Called from activity when helper finished pinning new compensations
     */
    public void onCompensationsPinned() {
        setLoading(false);
        updateAdapter();
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryCompensationsPaid(this);
    }

    @Override
    public void onCompensationsLocalQueried(List<ParseObject> compensations) {
        mCompensations.clear();
        for (ParseObject compensation : compensations) {
            mCompensations.add(compensation);
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
    protected void toggleEmptyViewVisibility() {
        if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void loadMoreData() {
        mIsLoadingMore = true;
        final int skip = mCompensations.size();
        mRecyclerAdapter.showLoadMoreIndicator();
        loadMoreDataWithHelper(skip);
    }

    private void loadMoreDataWithHelper(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        MoreQueryHelper moreQueryHelper = findMoreQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryHelper == null) {
            moreQueryHelper = MoreQueryHelper.newInstance(Compensation.CLASS, skip);

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
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addCompensations(objects);
    }

    public void onMoreObjectsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeMoreQueryHelper();

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
}
