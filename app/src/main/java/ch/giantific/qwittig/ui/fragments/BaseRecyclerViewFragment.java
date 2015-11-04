/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.utils.MessageUtils;

/**
 * Provides a an abstract base class for screens with a {@link RecyclerView} that is refreshable on
 * pull and shows a progress bar when loading and an empty view if no items are available.
 * <p/>
 * Subclass of {@link BaseFragment}.
 *
 * @see RecyclerView
 * @see SwipeRefreshLayout
 * @see ProgressBar
 */
public abstract class BaseRecyclerViewFragment extends BaseFragment {

    private static final String STATE_IS_LOADING = "STATE_IS_LOADING";
    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View mEmptyView;
    private ProgressBar mProgressBarLoading;
    private GroupRepository mGroupRepository;

    public BaseRecyclerViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();
        mGroupRepository = new ParseGroupRepository();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBarLoading = (ProgressBar) view.findViewById(R.id.pb_base);
        mEmptyView = view.findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_base);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_base);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
                R.color.accent,
                R.color.green,
                R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onlineQuery();
            }
        });

        if (savedInstanceState != null) {
            final boolean isLoading = savedInstanceState.getBoolean(STATE_IS_LOADING, false);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(isLoading);
                }
            });
        }
    }

    protected abstract void onlineQuery();

    final void showOnlineQueryErrorSnackbar(@NonNull String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        });
        snackbar.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING, mSwipeRefreshLayout.isRefreshing());
    }

    /**
     * Sets the loading state of the {@link SwipeRefreshLayout}.
     *
     * @param isLoading whether the {@link SwipeRefreshLayout} should be loading or not
     */
    public void setLoading(boolean isLoading) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isLoading);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateAdapter();
    }

    protected abstract void updateAdapter();

    final void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                updateView();
            } else {
                mGroupRepository.fetchGroupDataAsync(mCurrentGroup, new GroupRepository.GetGroupLocalListener() {
                    @Override
                    public void onGroupLocalLoaded(@NonNull Group group) {
                        updateView();
                    }
                });
            }
        } else {
            updateView();
        }
    }

    protected abstract void updateView();

    /**
     * Hides the loading {@link ProgressBar} and displays the {@link RecyclerView}.
     */
    @CallSuper
    void showMainView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBarLoading.setVisibility(View.GONE);
        toggleEmptyViewVisibility();
    }

    protected abstract void toggleEmptyViewVisibility();

    /**
     * Updates the current user and current group fields and tells the {@link RecyclerView} adapter
     * to reload its data.
     */
    public void updateFragment() {
        updateCurrentUserAndGroup();
        updateAdapter();
    }
}
