/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;
import ch.giantific.qwittig.presentation.common.views.SortedListView;

/**
 * Provides an abstract base class for fragments to house commonly used methods.
 * <p/>
 * Currently only incorporates a base interface for communication with an activity.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseSortedListFragment<U,
        T extends BasePresenter,
        S extends BaseFragment.ActivityListener<U>,
        V extends ChildItemViewModel>
        extends BaseFragment<U, T, S>
        implements BaseViewListener, SortedListView<V> {

    protected BaseSortedListRecyclerAdapter<V, T, ? extends RecyclerView.ViewHolder> recyclerAdapter;

    public BaseSortedListFragment() {
        // required empty constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerAdapter = getRecyclerAdapter();
        setupRecyclerView();
    }

    protected abstract BaseSortedListRecyclerAdapter<V, T, ? extends RecyclerView.ViewHolder> getRecyclerAdapter();

    protected abstract void setupRecyclerView();

    @Override
    public boolean isItemsEmpty() {
        return recyclerAdapter.isItemsEmpty();
    }

    @Override
    public void addItem(@NonNull V item) {
        recyclerAdapter.addItem(item);
    }

    @Override
    public void addItems(@NonNull List<V> items) {
        recyclerAdapter.addItems(items);
    }

    @Override
    public void removeItem(@NonNull V item) {
        recyclerAdapter.removeItem(item);
    }

    @Override
    public void removeItemAtPosition(int position) {
        recyclerAdapter.removeItemAtPosition(position);
    }

    @Override
    public void clearItems() {
        recyclerAdapter.clearItems();
    }

    @Override
    public void updateItemAt(int pos, @NonNull V item) {
        recyclerAdapter.updateItemAt(pos, item);
    }

    @Override
    public V getItemAtPosition(int position) {
        return recyclerAdapter.getItemAtPosition(position);
    }

    @Override
    public int getItemPositionForId(@NonNull String id) {
        return recyclerAdapter.getItemPositionForId(id);
    }

    @Override
    public int getItemPositionForItem(@NonNull V item) {
        return recyclerAdapter.getItemPositionForItem(item);
    }
}
