/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Defines an observable view model for a screen filled with a scrollable list that can be
 * refreshed with online content.
 */
public interface OnlineListViewModel<T, S extends ViewModel.ViewListener>
        extends ListViewModel<T, S>, DataRefreshViewModel {

    @Bindable
    boolean isRefreshing();

    void setRefreshing(boolean isRefreshing);

    SwipeRefreshLayout.OnRefreshListener getOnRefreshListener();
}