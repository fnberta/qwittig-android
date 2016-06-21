/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;

/**
 * Defines the basic observable view model for the stats screens.
 */
public interface StatsViewModel<T extends StatsViewModel.ViewListener> extends ViewModel<T>, LoadingViewModel {

    @Bindable
    boolean isDataEmpty();

    void setDataEmpty(boolean empty);

    String getYear();

    void setYear(@NonNull String year);

    Month getMonth();

    void setMonth(@NonNull Month month);

    void onDataLoaded(@Nullable Observable<Stats> data);

    void onTypeSelected(AdapterView<?> parent, View view, int position, long id);

    void onYearSelected(AdapterView<?> parent, View view, int position, long id);

    void onMonthSelected(AdapterView<?> parent, View view, int position, long id);

    @IntDef({StatsType.SPENDING, StatsType.STORES, StatsType.CURRENCIES})
    @Retention(RetentionPolicy.SOURCE)
    @interface StatsType {
        int SPENDING = 1;
        int STORES = 2;
        int CURRENCIES = 3;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void switchStatsScreen(@StatsType int type);

        void reloadData(@StatsType int type);

        int[] getStatsColors();
    }
}
