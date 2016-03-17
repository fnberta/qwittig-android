/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

/**
 * Created by fabio on 16.03.16.
 */
public interface DataRefreshViewModel {

    /**
     * Called when the underlying data sources has been updated.
     *
     * @param successful whether the update was successful
     */
    void onDataUpdated(boolean successful);
}
