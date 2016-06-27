/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.NumberFormat;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentStatsSpendingBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.stats.BaseStatsFragment;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.widgets.BarChart;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;

/**
 * Displays the spending statistics in a {@link BarChart}. Shows which user spent how much money in
 * a specified period of time.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class StatsSpendingFragment extends BaseStatsFragment<StatsSpendingViewModel, BaseStatsFragment.ActivityListener>
        implements StatsSpendingViewModel.ViewListener {

    private FragmentStatsSpendingBinding mBinding;
    private StatsSubcomponent mComponent;

    public StatsSpendingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentStatsSpendingBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
        getLoaderManager().initLoader(StatsType.SPENDING, null, this);
    }

    @Override
    protected void injectDependencies(@NonNull StatsSubcomponent component) {
        mComponent = component;
        mComponent.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_spending, menu);

        final MenuItem sortByUsers = menu.findItem(R.id.action_show_average);
        sortByUsers.setChecked(mViewModel.isShowAverage());

        final MenuItem showGroupValues = menu.findItem(R.id.action_show_group);
        showGroupValues.setChecked(mViewModel.isShowGroup());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_show_group:
                item.setChecked(!item.isChecked());
                mViewModel.onToggleGroupMenuClick();
                return true;
            case R.id.action_show_average:
                item.setChecked(!item.isChecked());
                mViewModel.onToggleAverageMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.bcSpending;
    }

    @Override
    public Loader<Observable<Stats>> onCreateLoader(int id, Bundle args) {
        return new StatsLoader(getActivity(), mComponent.getUserRepository(),
                mComponent.getStatsRepository(), StatsType.SPENDING, mViewModel.getYear(),
                mViewModel.getMonth().getNumber());
    }

    @Override
    public void setYAxisFormatter(@NonNull String currency) {
        final YAxis yAxis = mBinding.bcSpending.getAxisLeft();
        final NumberFormat moneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, false);
        yAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return moneyFormatter.format(value);
            }
        });
    }
}
