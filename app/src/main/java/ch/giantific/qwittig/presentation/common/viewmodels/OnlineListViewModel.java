/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by fabio on 10.01.16.
 */
public interface OnlineListViewModel<T>
        extends ListViewModel<T> {

    @Bindable
    boolean isRefreshing();

    void setRefreshing(boolean isRefreshing);

    SwipeRefreshLayout.OnRefreshListener getOnRefreshListener();

    interface ViewListener extends ListViewModel.ViewListener {

    }
}
