/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.presentation.workerfragments.query.MoreQueryWorker;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link FinanceCompensationsBaseFragment}.
 */
public class FinanceCompensationsPaidFragment extends FinanceCompensationsBaseFragment implements
        CompensationRepository.GetCompensationsLocalListener {

    private static final String COMPENSATION_PAID_QUERY_WORKER = "COMPENSATION_PAID_QUERY_WORKER";
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

        mRecyclerAdapter = new CompensationsPaidRecyclerAdapter(getActivity(), mCompensations,
                mCurrentUser);
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

    @NonNull
    @Override
    protected String getQueryWorkerTag() {
        return COMPENSATION_PAID_QUERY_WORKER;
    }

    @Override
    protected void onlineQuery() {
        onlineQuery(true);
    }

    @Override
    protected void updateAdapter() {
        mCompsRepo.getCompensationsLocalPaidAsync(mCurrentUser, mCurrentGroup);
    }

    @Override
    public void onCompensationsLocalLoaded(@NonNull List<ParseObject> compensations) {
        mCompensations.clear();
        mCompensations.addAll(compensations);

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
        if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Removes the retained worker fragment and and loading indicators.
     */
    public void onAllCompensationsUpdated() {
        WorkerUtils.removeWorker(getFragmentManager(), COMPENSATION_PAID_QUERY_WORKER);
        setLoading(false);
    }

    private void loadMoreData() {
        mIsLoadingMore = true;
        final int skip = mCompensations.size();
        mRecyclerAdapter.showLoadMoreIndicator();
        loadMoreDataWithWorker(skip);
    }

    private void loadMoreDataWithWorker(int skip) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment moreQueryWorker = WorkerUtils.findWorker(fragmentManager, MoreQueryWorker.MORE_QUERY_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (moreQueryWorker == null) {
            moreQueryWorker = MoreQueryWorker.newInstance(Compensation.CLASS, skip);

            fragmentManager.beginTransaction()
                    .add(moreQueryWorker, MoreQueryWorker.MORE_QUERY_WORKER)
                    .commit();
        }
    }

    /**
     * Adds the newly pinned compensations to the list, removes the retained worker fragment and
     * loading indicators.
     *
     * @param compensations the newly pinned compensations
     */
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> compensations) {
        WorkerUtils.removeWorker(getFragmentManager(), MoreQueryWorker.MORE_QUERY_WORKER);

        mIsLoadingMore = false;
        mRecyclerAdapter.hideLoadMoreIndicator();
        mRecyclerAdapter.addItems(compensations);
    }

    /**
     * Shows the user an error message and removes the retained worker fragment and loading
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
