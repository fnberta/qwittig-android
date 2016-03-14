/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.PieData;

/**
 * Defines an observable view model for a pie chart.
 */
public interface PieChartViewModel extends Observable {

    @Bindable
    PieData getPieData();

    void setPieData(@NonNull PieData pieData);

    @Bindable
    boolean isShowPercentage();

    @Bindable
    String getCenterText();

    void setCenterText(@NonNull String centerText);
}
