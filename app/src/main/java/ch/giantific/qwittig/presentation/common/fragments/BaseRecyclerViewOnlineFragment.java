/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

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
public abstract class BaseRecyclerViewOnlineFragment<T extends OnlineListViewModel, S extends BaseFragment.ActivityListener>
        extends BaseRecyclerViewFragment<T, S> {

    public BaseRecyclerViewOnlineFragment() {
        // required empty constructor
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
