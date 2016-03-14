/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import ch.giantific.qwittig.presentation.stats.StatsBaseFragment;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.spending.di.DaggerStatsSpendingComponent;
import ch.giantific.qwittig.presentation.stats.spending.di.StatsSpendingComponent;
import ch.giantific.qwittig.presentation.stats.spending.di.StatsSpendingViewModelModule;
import ch.giantific.qwittig.presentation.stats.widgets.BarChart;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;

/**
 * Displays the spending statistics in a {@link BarChart}. Shows which user spent how much money in
 * a specified period of time.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class StatsSpendingFragment extends StatsBaseFragment<StatsSpendingViewModel, StatsBaseFragment.ActivityListener>
        implements StatsSpendingViewModel.ViewListener {

    private FragmentStatsSpendingBinding mBinding;
    private StatsSpendingComponent mComponent;

    public StatsSpendingFragment() {
        // Required empty public constructor
    }

    public static StatsSpendingFragment newInstance(@NonNull String year, @NonNull Month month) {
        final StatsSpendingFragment fragment = new StatsSpendingFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_YEAR, year);
        args.putParcelable(KEY_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedState, @NonNull String year, @NonNull Month month) {
        mComponent = DaggerStatsSpendingComponent.builder()
                .statsSpendingViewModelModule(new StatsSpendingViewModelModule(savedState, this, year, month))
                .build();
        mComponent.inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentStatsSpendingBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(StatsType.SPENDING, null, this);
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
        return new StatsLoader(getActivity(), mComponent.getUserRepo(),
                mComponent.getStatsRepo(), StatsType.SPENDING, mViewModel.getYear(),
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
