/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

/**
 * Created by fabio on 04.11.15.
 */
public interface SelectionRecyclerAdapter {

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
