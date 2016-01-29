/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;

/**
 * Created by fabio on 10.01.16.
 */
public interface ListViewModel<T, S extends ListViewModel.ViewListener>
        extends ViewModel<S> {

    @Bindable
    boolean isLoading();

    void setLoading(boolean isLoading);

    @Bindable
    boolean isEmpty();

    void updateList();

    T getItemAtPosition(int position);

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    int getItemViewType(int position);

    /**
     * Returns the total number of items in the data set hold by the view model.
     *
     * @return the total number of items in this view model.
     */
    int getItemCount();

    int getLastPosition();

    interface ViewListener extends ViewModel.ViewListener {

        void notifyDataSetChanged();

        void notifyItemChanged(int position);

        void notifyItemRangeChanged(int positionStart, int itemCount);

        void notifyItemRemoved(int position);

        void notifyItemInserted(int lastPosition);

        void notifyItemRangeInserted(int positionStart, int itemCount);

        void scrollToPosition(int position);
    }
}
