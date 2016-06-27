/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.PieData;

/**
 * Provides an implementation of the {@link PieChartViewModel} interface for a recycler view row.
 */
public class PieChartRowViewModel extends BaseObservable implements PieChartViewModel {

    private final boolean mShowPercentage;
    private PieData mPieData;
    private String mCenterText;

    public PieChartRowViewModel(@NonNull PieData pieData, boolean showPercentage,
                                @NonNull String centerText) {
        mPieData = pieData;
        mShowPercentage = showPercentage;
        mCenterText = centerText;
    }

    @Override
    @Bindable
    public PieData getPieData() {
        return mPieData;
    }

    @Override
    public void setPieData(@NonNull PieData pieData) {
        mPieData = pieData;
    }

    @Override
    @Bindable
    public boolean isShowPercentage() {
        return mShowPercentage;
    }

    @Override
    @Bindable
    public String getCenterText() {
        return mCenterText;
    }

    @Override
    public void setCenterText(@NonNull String centerText) {
        mCenterText = centerText;
    }
}
