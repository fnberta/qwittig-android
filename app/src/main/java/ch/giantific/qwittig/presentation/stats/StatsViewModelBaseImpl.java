/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.models.StatsPage;
import ch.giantific.qwittig.utils.MessageAction;

/**
 * Provides an abstract base implementation of the {@link StatsViewModel} interface.
 */
public abstract class StatsViewModelBaseImpl<T extends StatsViewModel.ViewListener> extends ViewModelBaseImpl<T>
        implements StatsViewModel {

    protected static final int PERIOD_YEAR = 0;
    protected static final int PERIOD_MONTH = 1;
    private static final String STATE_PERIOD_TYPE = "STATE_PERIOD_TYPE";
    private static final String STATE_LOADING = "STATE_LOADING";
    private static final String STATE_EMPTY = "STATE_EMPTY";
    protected boolean mLoading;
    protected boolean mDataEmpty;
    protected String mYear;
    protected Month mMonth;
    protected Stats mStatsData;
    protected int mPeriodType;

    public StatsViewModelBaseImpl(@Nullable Bundle savedState, @NonNull T view,
                                  @NonNull UserRepository userRepository,
                                  @NonNull String defaultYear,
                                  @NonNull Month defaultMonth) {
        super(savedState, view, userRepository);

        mYear = defaultYear;
        mMonth = defaultMonth;

        if (savedState != null) {
            mPeriodType = savedState.getInt(STATE_PERIOD_TYPE);
            mLoading = savedState.getBoolean(STATE_LOADING);
            mDataEmpty = savedState.getBoolean(STATE_EMPTY);
        } else {
            mLoading = true;
            mDataEmpty = true;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putInt(STATE_PERIOD_TYPE, mPeriodType);
        outState.putBoolean(STATE_LOADING, mLoading);
        outState.putBoolean(STATE_EMPTY, mDataEmpty);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    @Bindable
    public boolean isDataEmpty() {
        return mDataEmpty;
    }

    @Override
    public void setDataEmpty(boolean empty) {
        mDataEmpty = empty;
        notifyPropertyChanged(BR.dataEmpty);
    }

    @Override
    public String getYear() {
        return mYear;
    }

    @Override
    public Month getMonth() {
        return mMonth;
    }

    @Override
    public void onTypeSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final StatsPage statsPage = (StatsPage) parent.getItemAtPosition(position);
        final int type = statsPage.getType();
        if (type != getStatsType()) {
            mView.switchStatsScreen(type);
        }
    }

    @Override
    public void onYearSelected(AdapterView<?> parent, View view, int position, long id) {
        final String year = (String) parent.getItemAtPosition(position);
        if (!year.equals(mYear)) {
            mYear = year;
            reloadData();
        }
    }

    @Override
    public void onMonthSelected(AdapterView<?> parent, View view, int position, long id) {
        final Month month = (Month) parent.getItemAtPosition(position);
        if (!month.equals(mMonth)) {
            mMonth = month;
            reloadData();
        }
    }

    private void reloadData() {
        if (!mView.isNetworkAvailable()) {
            mView.showMessageWithAction(R.string.toast_error_stats_load, getRetryAction());
            setLoading(false);
            return;
        }

        setDataEmpty(true);
        setLoading(true);
        mPeriodType = mMonth.getNumber() == 0 ? PERIOD_YEAR : PERIOD_MONTH;
        mView.reloadData(getStatsType());
    }

    @NonNull
    final protected MessageAction getRetryAction() {
        return new MessageAction(R.string.action_retry) {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        };
    }

    @NonNull
    final protected List<Integer> getColors() {
        final List<Integer> colors = new ArrayList<>();
        for (int i = 0; i <= 11; i++) {
            colors.add(getColor(i));
        }

        return colors;
    }

    @ColorInt
    final protected int getColor(int position) {
        final int[] colors = mView.getStatsColors();
        final int colorsSize = colors.length;
        if (position >= 0 && position < colorsSize) {
            return colors[position];
        } else if (position >= colorsSize) {
            return getColor(position - colorsSize);
        }

        return -1;
    }

    @StatsType
    protected abstract int getStatsType();
}
