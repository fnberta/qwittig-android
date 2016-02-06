/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

/**
 * Interface to notify a {@link android.support.v7.widget.RecyclerView.Adapter} of moving and
 * dismissal event from a {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}.
 *
 * @author Paul Burke (ipaulpro)
 */
public interface ItemTouchHelperAdapter {

    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and not at the end of a "drop" event.
     *
     * @param fromPosition the start position of the moved item
     * @param toPosition   the end position of the moved item
     */
    void onItemMove(int fromPosition, int toPosition);


    /**
     * Called when an item has been dismissed by a swipe.
     *
     * @param position the position of the item dismissed
     */
    void onItemDismiss(int position);
}
