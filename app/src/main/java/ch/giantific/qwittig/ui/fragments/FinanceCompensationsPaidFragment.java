/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.data.helpers.query.MoreQueryHelper;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.ui.adapters.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link FinanceCompensationsBaseFragment}.
 */
public class FinanceCompensationsPaidFragment extends FinanceCompensationsBaseFragment implements
        CompensationRepository.GetCompensationsLocalListener {

    private static final String COMPENSATION_PAID_QUERY_HELPER = "COMPENSATION_PAID_QUERY_HELPER";
    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private static final String LOG_TAG = FinanceCompensationsPaidFragment.class.getSimpleName();
    private CompensationsPaidRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseObject> mCompensations = new ArrayList<>();
    private boolean mIsLoadingMore;

    public FinanceCompensationsPaidFragment() {
        // Required empty public constructor
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finance_compensations_paid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsPaidRecyclerAdapter(getActivity(), mCompensations);
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

    @NonNull
    @Override
    protected String getQueryHelperTag() {
        return COMPENSATION_PAID_QUERY_HELPER;
    }

    @Override
    protected void onlineQuery() {
        onlineQuery(true);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        mCompsRepo.getCompensationsLocalPaidAsync(mCurrentUser, mCurrentGroup, this);
    }

    @Override
    public void onCompensationsLocalLoaded(@NonNull List<ParseObject> compensations) {
        mCompensations.clear();
        mCompensations.addAll(compensations);

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
    protected void toggleEmptyViewVisibility() {
        if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Removes the retained helper fragment and and loading indicators.
     */
    public void onAllCompensationsUpdated() {
        HelperUtils.removeHelper(getFragmentManager(), COMPENSATION_PAID_QUERY_HELPER);
        setLoading(false);
    }

    private void loadMoreData() {
        mIsLoadingMore = true;
        final int skip = mCompensations.size();
        mRecyclerAdapter.showLoadMoreIndicator();
        loadMoreDataWithHelper(skip);
    }

    private void loadMoreDataWithHelper(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment moreQueryHelper = HelperUtils.findHelper(fragmentManager, MoreQueryHelper.MORE_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryHelper == null) {
            moreQueryHelper = MoreQueryHelper.newInstance(Compensation.CLASS, skip);

            fragmentManager.beginTransaction()
                    .add(moreQueryHelper, MoreQueryHelper.MORE_QUERY_HELPER)
                    .commit();
        }
    }

    /**
     * Adds the newly pinned compensations to the list, removes the retained helper fragment and
     * loading indicators.
     *
     * @param compensations the newly pinned compensations
     */
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> compensations) {
        HelperUtils.removeHelper(getFragmentManager(), MoreQueryHelper.MORE_QUERY_HELPER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addCompensations(compensations);
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown during the process
     */
    public void onMoreObjectsLoadFailed(int errorCode) {
        ParseErrorHandler.handleParseError(getActivity(), errorCode);
        showLoadMoreErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), errorCode));
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
