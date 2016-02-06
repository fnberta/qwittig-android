/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Defines the needed methods for a {@link RecyclerView} adapter with state selection support.
 */
public interface SelectionModeViewModel<T> {

    /**
     * Toggles the selection state of the item.
     *
     * @param position the position of the item
     */
    void toggleSelection(int position);

    /**
     * Clears the selected items and if needed deletes them also from the local data store.
     *
     */
    void clearSelection();

    /**
     * Returns whether the item is currently selected or not.
     *
     * @param item the item in question
     * @return whether the item is currently selected or not
     */
    boolean isSelected(@NonNull T item);
}
