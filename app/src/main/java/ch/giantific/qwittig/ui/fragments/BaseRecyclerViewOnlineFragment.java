/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.MessageUtils;

/**
 * Provides a an abstract base class for screens with a {@link RecyclerView} that is refreshable on
 * pull and shows a progress bar when loading and an empty view if no items are available.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 *
 * @see RecyclerView
 * @see SwipeRefreshLayout
 * @see ProgressBar
 */
public abstract class BaseRecyclerViewOnlineFragment extends BaseRecyclerViewFragment {

    private static final String STATE_IS_LOADING = "STATE_IS_LOADING";
    private static final String STATE_ONLINE_QUERY = "STATE_ONLINE_QUERY";
    SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mOnlineQueryInProgress;

    public BaseRecyclerViewOnlineFragment() {
    }

    public void setOnlineQueryInProgress(boolean onlineQueryInProgress) {
        mOnlineQueryInProgress = onlineQueryInProgress;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mOnlineQueryInProgress = savedInstanceState.getBoolean(STATE_ONLINE_QUERY, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING, mSwipeRefreshLayout.isRefreshing());
        outState.putBoolean(STATE_ONLINE_QUERY, mOnlineQueryInProgress);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    final void showErrorSnackbar(@NonNull String errorMessage,
                                 @Nullable View.OnClickListener retryAction) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        if (retryAction != null) {
            snackbar.setAction(R.string.action_retry, retryAction);
        }
        snackbar.show();
    }

    final View.OnClickListener getOnlineQueryRetryAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        };
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
    void showMainView() {
        if (!mOnlineQueryInProgress) {
            super.showMainView();
        }
    }
}
