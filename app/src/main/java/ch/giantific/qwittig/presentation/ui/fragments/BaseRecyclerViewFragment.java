/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.presentation.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.viewmodels.ViewModel;

/**
 * Provides a an abstract base class for screens with a {@link RecyclerView} and shows a progress
 * bar when loading and an empty view if no items are available.
 * <p/>
 * Subclass of {@link BaseFragment}.
 *
 * @see RecyclerView
 * @see ProgressBar
 */
public abstract class BaseRecyclerViewFragment<T extends ListViewModel, S extends BaseFragment.ActivityListener>
        extends BaseFragment<T, S>
        implements ListViewModel.ViewListener {

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mRecyclerAdapter;

    public BaseRecyclerViewFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerAdapter = getRecyclerAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    protected abstract RecyclerView getRecyclerView();

    protected abstract RecyclerView.Adapter getRecyclerAdapter();

    @Override
    protected View getSnackbarView() {
        return mRecyclerView;
    }

    @Override
    public void notifyDataSetChanged() {
        mRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        mRecyclerAdapter.notifyItemChanged(position);
    }

    @Override
    public void notifyItemRemoved(int position) {
        mRecyclerAdapter.notifyItemRemoved(position);
    }

    @Override
    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mRecyclerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void notifyItemInserted(int lastPosition) {
        mRecyclerAdapter.notifyItemInserted(lastPosition);
    }

    @Override
    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        mRecyclerAdapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int itemCount) {
        mRecyclerAdapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }
}
