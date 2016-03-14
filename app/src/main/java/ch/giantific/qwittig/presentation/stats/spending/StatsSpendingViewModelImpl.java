/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.StatsViewModelBaseImpl;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link StatsSpendingViewModel} interface.
 */
public class StatsSpendingViewModelImpl extends StatsViewModelBaseImpl<StatsSpendingViewModel.ViewListener>
        implements StatsSpendingViewModel {

    private static final String STATE_SHOW_GROUP = "STATE_SHOW_GROUP";
    private static final String STATE_SHOW_AVERAGE = "STATE_SHOW_AVERAGE";
    private BarData mBarData;
    private boolean mShowGroup;
    private boolean mShowAverage;

    public StatsSpendingViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull StatsSpendingViewModel.ViewListener view,
                                      @NonNull UserRepository userRepository,
                                      @NonNull String defaultYear,
                                      @NonNull Month defaultMonth) {
        super(savedState, view, userRepository, defaultYear, defaultMonth);

        if (savedState != null) {
            mShowGroup = savedState.getBoolean(STATE_SHOW_GROUP, false);
            mShowAverage = savedState.getBoolean(STATE_SHOW_AVERAGE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_SHOW_GROUP, mShowGroup);
        outState.putBoolean(STATE_SHOW_AVERAGE, mShowAverage);
    }

    @Override
    @Bindable
    public BarData getBarData() {
        return mBarData;
    }

    @Override
    public void setBarData(@NonNull BarData barData) {
        mBarData = barData;
        notifyPropertyChanged(BR.barData);
    }

    @Override
    public boolean isShowGroup() {
        return mShowGroup;
    }

    @Override
    public boolean isShowAverage() {
        return mShowAverage;
    }

    @Override
    public void onDataLoaded(@Nullable final Observable<Stats> data) {
        if (data == null) {
            setLoading(false);
            setDataEmpty(true);
            return;
        }

        getSubscriptions().add(mUserRepo.fetchIdentityDataAsync(mCurrentIdentity)
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        mView.setYAxisFormatter(identity.getGroup().getCurrency());
                    }
                })
                .flatMap(new Func1<Identity, Single<Stats>>() {
                    @Override
                    public Single<Stats> call(Identity identity) {
                        return data.toSingle();
                    }
                })
                .subscribe(new SingleSubscriber<Stats>() {
                    @Override
                    public void onSuccess(Stats stats) {
                        mStatsData = stats;
                        setChartData();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessageWithAction(R.string.toast_error_stats_load, getRetryAction());
                        setDataEmpty(true);
                        setLoading(false);
                    }
                })
        );
    }

    private void setChartData() {
        final List<Stats.Member> userData = mStatsData.getMembers();
        final Stats.Group groupData = mStatsData.getGroup();
        final int unitSize = mStatsData.getNumberOfUnits();
        final List<String> xVals = getXvals(unitSize);

        final BarData barData = mShowGroup
                ? getGroupBarData(groupData, xVals)
                : getUserBarData(userData, xVals);

        setBarData(barData);
        setLoading(false);
    }

    @NonNull
    private List<String> getXvals(int unitSize) {
        final List<String> xVals = new ArrayList<>(unitSize);
        for (int i = 1; i <= unitSize; i++) {
            switch (mPeriodType) {
                case PERIOD_YEAR:
                    xVals.add(DateUtils.getMonthNameShort(i));
                    break;
                case PERIOD_MONTH:
                    xVals.add(String.valueOf(i));
                    break;
            }
        }
        return xVals;
    }

    @NonNull
    private BarData getUserBarData(@NonNull List<Stats.Member> userData, List<String> xVals) {
        final int userDataSize = userData.size();
        final List<IBarDataSet> barDataSets = new ArrayList<>(userDataSize);
        for (int i = 0; i < userDataSize; i++) {
            final Stats.Member user = userData.get(i);
            final List<Stats.Unit> units = user.getUnits();
            final List<BarEntry> barEntries = getBarEntries(units);

            final String userId = user.getMemberId();
            final Identity buyer = (Identity) Identity.createWithoutData(Identity.CLASS, userId);
            final BarDataSet barDataSet = new BarDataSet(barEntries, buyer.getNickname());
            barDataSet.setColor(getColor(i));

            barDataSets.add(barDataSet);
        }

        return new BarData(xVals, barDataSets);
    }

    @NonNull
    private List<BarEntry> getBarEntries(@NonNull List<Stats.Unit> units) {
        final List<BarEntry> barEntries = new ArrayList<>(units.size());

        for (Stats.Unit unit : units) {
            final float value = mShowAverage ? unit.getAverage() : unit.getTotal();
            final int identifier = Integer.parseInt(unit.getIdentifier());
            final BarEntry barEntry = new BarEntry(value, identifier);
            barEntries.add(barEntry);

            if (value > 0) {
                mDataEmpty = false;
            }
        }

        return barEntries;
    }

    @NonNull
    private BarData getGroupBarData(@NonNull Stats.Group groupData, List<String> xVals) {
        final List<Stats.Unit> units = groupData.getUnits();
        final List<BarEntry> barEntries = getBarEntries(units);
        final BarDataSet barDataSet = new BarDataSet(barEntries, mCurrentIdentity.getGroup().getName());
        barDataSet.setColors(getColors());

        return new BarData(xVals, barDataSet);
    }

    @Override
    public void onToggleGroupMenuClick() {
        mShowGroup = !mShowGroup;
        setChartData();
    }

    @Override
    public void onToggleAverageMenuClick() {
        mShowAverage = !mShowAverage;
        setChartData();
    }

    @Override
    protected int getStatsType() {
        return StatsViewModel.StatsType.SPENDING;
    }
}
