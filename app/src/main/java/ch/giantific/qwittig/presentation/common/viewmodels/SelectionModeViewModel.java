/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

/**
 * Defines the needed methods for a view model with a list that supports state selection.
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
     */
    void clearSelection();
}
