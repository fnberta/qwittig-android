/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Provides a abstract base class for {@link RecyclerView} adapters with a list of items and
 * infinite scrolling.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 * <p/>
 * Implements {@link LoadMoreAdapter}.
 */
public abstract class BaseLoadMoreRecyclerAdapter<T> extends BaseRecyclerAdapter<T> implements
        LoadMoreAdapter<T> {

    private static final String LOG_TAG = BaseLoadMoreRecyclerAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link BaseLoadMoreRecyclerAdapter}.
     *
     * @param context the context to use in the adapter
     * @param items   the items to show in the adapter
     */
    public BaseLoadMoreRecyclerAdapter(@NonNull Context context, @NonNull List<T> items) {
        super(context, items);
    }

    @Override
    public void addItems(@NonNull List<T> items) {
        if (!items.isEmpty()) {
            mItems.addAll(items);
            notifyItemRangeInserted(getItemCount(), items.size());
        }
    }

    @Override
    public void showLoadMoreIndicator() {
        mItems.add(null);
        notifyItemInserted(getLastPosition());
    }

    @Override
    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mItems.remove(position);
        notifyItemRemoved(position);
    }
}
