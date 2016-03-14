/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;

import android.databinding.Bindable;

import com.github.mikephil.charting.data.PieData;

import ch.giantific.qwittig.presentation.stats.StatsViewModel;

/**
 * Defines a base observable view model for stats screens with a pie chart.
 */
public interface StatsPieViewModel extends StatsViewModel, PieChartViewModel {

    @Bindable
    boolean isSortUsers();

    void setSortUsers(boolean sortUsers);

    void onToggleSortUsersMenuClick();

    void onTogglePercentMenuClick();

    /**
     * Returns the item at the position.
     *
     * @param position the position of the item
     * @return the item
     */
    PieData getDataAtPosition(int position);

    String getNicknameAtPosition(int position);

    /**
     * Returns the total number of items in the data set hold by the view model.
     *
     * @return the total number of items in this view model.
     */
    int getItemCount();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends StatsViewModel.ViewListener {

        void notifyDataSetChanged();
    }
}
