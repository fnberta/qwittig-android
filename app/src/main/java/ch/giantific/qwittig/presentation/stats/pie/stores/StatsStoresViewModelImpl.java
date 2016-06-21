/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.stores;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModelBaseImpl;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides an implementation of the {@link StatsStoresViewModel} interface.
 */
public class StatsStoresViewModelImpl extends StatsPieViewModelBaseImpl<StatsStoresViewModel.ViewListener>
        implements StatsStoresViewModel {

    private static final String STATE_SHOW_AVERAGE = "STATE_SHOW_AVERAGE";
    private boolean mShowAverage;

    public StatsStoresViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        if (savedState != null) {
            mShowAverage = savedState.getBoolean(STATE_SHOW_AVERAGE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_SHOW_AVERAGE, mShowAverage);
    }

    @Override
    public boolean isShowAverage() {
        return mShowAverage;
    }

    @Override
    public void onToggleAverageMenuClick() {
        mShowAverage = !mShowAverage;
        setChartData();
    }

    @Override
    protected float getValue(@NonNull Stats.Unit unit) {
        if (mShowAverage) {
            return unit.getAverage();
        } else {
            return super.getValue(unit);
        }
    }

    @Override
    protected void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        super.setDataSetOptions(pieDataSet);

        if (mShowPercent) {
            pieDataSet.setValueFormatter(new PercentFormatter());
        } else {
            final String currency = mCurrentIdentity.getGroup().getCurrency();
            final NumberFormat numberFormatter = MoneyUtils.getMoneyFormatter(currency, true, false);
            pieDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return numberFormatter.format(value);
                }
            });
        }
    }

    @Override
    protected int getStatsType() {
        return StatsViewModel.StatsType.STORES;
    }
}
