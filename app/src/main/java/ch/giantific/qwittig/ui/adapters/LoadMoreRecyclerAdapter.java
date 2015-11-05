/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Defines the needed methods for a {@link RecyclerView} adapter with infinite scrolling support.
 */
public interface LoadMoreRecyclerAdapter<T> {

    /**
     * Adds items to the adapter.
     *
     * @param items the items to be added
     */
    void addItems(@NonNull List<T> items);

    /**
     * Shows a progress bar in the last row as an indicator that more objects are being fetched.
     */
    void showLoadMoreIndicator();

    /**
     * Hides the progress bar in the last row.
     */
    void hideLoadMoreIndicator();
}
