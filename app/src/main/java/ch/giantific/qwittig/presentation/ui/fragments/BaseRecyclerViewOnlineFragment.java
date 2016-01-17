/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.viewmodels.ViewModel;

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
public abstract class BaseRecyclerViewOnlineFragment<T extends OnlineListViewModel, S extends BaseFragment.ActivityListener<T>> extends BaseRecyclerViewFragment<T, S> {

    public BaseRecyclerViewOnlineFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mSwipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                mSwipeRefreshLayout.setRefreshing(isLoading);
//            }
//        });
    }
}
