/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.ListInteraction;

/**
 * Defines an observable view model for a screen filled with a scrollable list.
 */
public interface ListViewModel<T, S extends ViewModel.ViewListener>
        extends ViewModel<S> {

    void setListInteraction(@NonNull ListInteraction listInteraction);

    /**
     * Returns whether the list is empty or not.
     *
     * @return whether the list is empty or not
     */
    @Bindable
    boolean isEmpty();

    /**
     * Returns the item at the position.
     *
     * @param position the position of the item
     * @return the item
     */
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
}
