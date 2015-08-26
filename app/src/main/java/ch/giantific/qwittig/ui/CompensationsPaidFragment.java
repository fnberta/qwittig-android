package ch.giantific.qwittig.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.ui.adapters.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.ui.listeners.InfiniteScrollListener;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompensationsPaidFragment extends CompensationsBaseFragment implements
        LocalQuery.CompensationLocalQueryListener {

    private static final String STATE_IS_LOADING_MORE = "state_is_loading_more";
    private static final String LOG_TAG = CompensationsPaidFragment.class.getSimpleName();
    private InfiniteScrollListener mScrollListener;
    private CompensationsPaidRecyclerAdapter mRecyclerAdapter;
    private List<ParseObject> mCompensations = new ArrayList<>();
    private boolean mIsLoadingMore;

    public CompensationsPaidFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_compensations_paid, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_compensations_history);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_compensations_history);
        mEmptyView = rootView.findViewById(R.id.tv_empty_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsPaidRecyclerAdapter(getActivity(),
                R.layout.row_compensations_history, mCompensations);
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
        toggleEmptyViewVisibility();

        if (mIsLoadingMore) {
            int compensationsSize = mCompensations.size();
            addLoadMoreProgressBar(compensationsSize);
            mRecyclerView.scrollToPosition(compensationsSize);
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
        if (mSwipeRefreshLayout.isRefreshing()) {
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            showErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        if (!mIsLoadingMore) {
            mIsLoadingMore = true;

            int compensationsSize = mCompensations.size();
            addLoadMoreProgressBar(compensationsSize);
            loadMoreDataWithHelper(compensationsSize);
        }
    }

    private void addLoadMoreProgressBar(int progressBarPosition) {
        mCompensations.add(null);
        mRecyclerAdapter.notifyItemInserted(progressBarPosition);
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

        int progressBarPosition = mCompensations.size() - 1;
        mCompensations.remove(progressBarPosition);
        mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

        if (!objects.isEmpty()) {
            for (ParseObject compensation : objects) {
                mCompensations.add(compensation);
            }

            mRecyclerAdapter.notifyItemRangeInserted(progressBarPosition, objects.size());
        }
    }

    public void onMoreObjectsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeMoreQueryHelper();

        mIsLoadingMore = false;

        if (!mCompensations.isEmpty()) {
            int progressBarPosition = mCompensations.size() - 1;
            if (mCompensations.get(progressBarPosition) == null) {
                mCompensations.remove(progressBarPosition);
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

    private void showErrorSnackbar(String errorMessage) {
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
