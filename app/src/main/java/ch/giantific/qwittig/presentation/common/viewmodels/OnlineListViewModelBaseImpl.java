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
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Created by fabio on 10.01.16.
 */
public abstract class OnlineListViewModelBaseImpl<T, S extends OnlineListViewModel.ViewListener>
        extends ListViewModelBaseImpl<T, S>
        implements OnlineListViewModel<T> {

    private static final String STATE_REFRESHING = "STATE_REFRESHING";
    private boolean mRefreshing;

    public OnlineListViewModelBaseImpl(@Nullable Bundle savedState,
                                       @NonNull S view,
                                       @NonNull IdentityRepository identityRepository,
                                       @NonNull UserRepository userRepository) {
        super(savedState, view, identityRepository, userRepository);

        if (savedState != null) {
            setRefreshing(savedState.getBoolean(STATE_REFRESHING, false));
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
