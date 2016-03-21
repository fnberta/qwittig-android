/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

/**
 * Defines a view model for a screen that allows the underlying data to be refreshed.
 */
public interface DataRefreshViewModel {

    /**
     * Called when the underlying data sources has been updated.
     *
     * @param successful whether the update was successful
     */
    void onDataUpdated(boolean successful);
}
