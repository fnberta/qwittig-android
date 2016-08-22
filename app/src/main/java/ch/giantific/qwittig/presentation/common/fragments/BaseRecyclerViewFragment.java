/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Provides a an abstract base class for screens with a {@link RecyclerView} and shows a progress
 * bar when loading and an empty view if no items are available.
 * <p/>
 * Subclass of {@link BaseFragment}.
 *
 * @see RecyclerView
 * @see ProgressBar
 */
public abstract class BaseRecyclerViewFragment<U, T extends ViewModel, S extends BaseFragment.ActivityListener<U>>
        extends BaseFragment<U, T, S> {

    protected RecyclerView recyclerView;
    protected BaseRecyclerAdapter recyclerAdapter;

    public BaseRecyclerViewFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = getRecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
    }

    protected abstract RecyclerView getRecyclerView();

    protected abstract BaseRecyclerAdapter getRecyclerAdapter();

    @Override
    protected View getSnackbarView() {
        return recyclerView;
    }
}
