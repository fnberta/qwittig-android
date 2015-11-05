/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * Defines the needed methods for a {@link RecyclerView} adapter with state selection support.
 */
public interface SelectionAdapter {

    /**
     * Toggles the selection state of the item.
     *
     * @param position the position of the item
     */
    void toggleSelection(int position);

    /**
     * Clears the selected items and if needed deletes them also from the local data store.
     *
     * @param deleteSelectedItems whether to delete the selected items from the local data store
     */
    void clearSelection(boolean deleteSelectedItems);

    /**
     * Returns whether the item is currently selected or not.
     *
     * @param item the item or object id of the item in question
     * @return whether the item is currently selected or not
     */
    boolean isSelected(String item);
}
