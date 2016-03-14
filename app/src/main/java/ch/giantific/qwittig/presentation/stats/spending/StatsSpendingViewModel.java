/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.BarData;

import ch.giantific.qwittig.presentation.stats.StatsViewModel;

/**
 * Defines an observable view model for the spending stats screen.
 */
public interface StatsSpendingViewModel extends StatsViewModel {

    @Bindable
    BarData getBarData();

    void setBarData(@NonNull BarData barData);

    boolean isShowGroup();

    boolean isShowAverage();

    void onToggleGroupMenuClick();

    void onToggleAverageMenuClick();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends StatsViewModel.ViewListener {

        void setYAxisFormatter(@NonNull String currency);
    }
}
