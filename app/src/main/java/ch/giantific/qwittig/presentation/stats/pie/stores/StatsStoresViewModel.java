/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.stores;

import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModel;

/**
 * Defines an observable interface for the stores stats screen.
 */
public interface StatsStoresViewModel extends StatsPieViewModel<StatsPieViewModel.ViewListener> {

    boolean isShowAverage();

    void onToggleAverageMenuClick();
}
