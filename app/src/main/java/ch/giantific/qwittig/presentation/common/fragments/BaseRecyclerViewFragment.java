/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
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
public abstract class BaseRecyclerViewFragment<T extends ViewModel, S extends BaseFragment.ActivityListener>
        extends BaseFragment<T, S>
        implements ListViewModel.ViewListener {

    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mRecyclerAdapter;

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
    public void notifyItemInserted(int position) {
        mRecyclerAdapter.notifyItemInserted(position);
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
    public void notifyItemMoved(int fromPosition, int toPosition) {
        mRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }
}
