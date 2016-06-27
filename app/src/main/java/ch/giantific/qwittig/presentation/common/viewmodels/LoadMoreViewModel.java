/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

/**
 * Defines an observable view model for a screen with an infinite scrolling list.
 */
public interface LoadMoreViewModel {

    /**
     * Callback for when the next set of items should be loaded
     */
    void onLoadMore();

    /**
     * Callback for whether a load more operation is currently ongoing
     *
     * @return <code>true</code> if a load operation is happening,
     * <code>false</code> otherwise. If <code>true</code>, load more
     * event won't be triggered
     */
    boolean isLoadingMore();
}
