/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModel;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModelBaseImpl;

/**
 * Provides an implementation of the {@link StatsPieViewModel} interface for currencies stats screen.
 */
public class StatsCurrenciesViewModelImpl extends StatsPieViewModelBaseImpl<StatsCurrenciesViewModel.ViewListener>
        implements StatsCurrenciesViewModel {

    public StatsCurrenciesViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);
    }

    @Override
    protected void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        super.setDataSetOptions(pieDataSet);
        pieDataSet.setValueFormatter(new PercentFormatter());
    }

    @Override
    protected int getStatsType() {
        return StatsViewModel.StatsType.CURRENCIES;
    }
}
