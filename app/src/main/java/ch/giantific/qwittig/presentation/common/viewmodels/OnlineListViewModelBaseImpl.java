/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Provides an abstract base implementation of the {@link OnlineListViewModel}.
 */
public abstract class OnlineListViewModelBaseImpl<T, S extends OnlineListViewModel.ViewListener>
        extends ListViewModelBaseImpl<T, S>
        implements OnlineListViewModel<T> {

    private static final String STATE_REFRESHING = "STATE_REFRESHING";
    private boolean mRefreshing;

    public OnlineListViewModelBaseImpl(@Nullable Bundle savedState,
                                       @NonNull S view,
                                       @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        if (savedState != null) {
            mRefreshing = savedState.getBoolean(STATE_REFRESHING, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_REFRESHING, mRefreshing);
    }

    @Override
    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        };
    }

    protected abstract void refreshItems();

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        super.onWorkerError(workerTag);

        setRefreshing(false);
    }
}
