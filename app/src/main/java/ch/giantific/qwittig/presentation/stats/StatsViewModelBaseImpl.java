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
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
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
        implements StatsViewModel<T> {

    protected static final int PERIOD_YEAR = 0;
    protected static final int PERIOD_MONTH = 1;
    private static final String STATE_PERIOD_TYPE = "STATE_PERIOD_TYPE";
    private static final String STATE_LOADING = "STATE_LOADING";
    private static final String STATE_EMPTY = "STATE_EMPTY";
    private static final String STATE_YEAR = "STATE_YEAR";
    private static final String STATE_MONTH = "STATE_MONTH";
    protected boolean mLoading;
    protected boolean mDataEmpty;
    protected String mYear;
    protected Month mMonth;
    protected Stats mStatsData;
    protected int mPeriodType;

    public StatsViewModelBaseImpl(@Nullable Bundle savedState,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        if (savedState != null) {
            mYear = savedState.getString(STATE_YEAR);
            mMonth = savedState.getParcelable(STATE_MONTH);
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

        outState.putString(STATE_YEAR, mYear);
        outState.putParcelable(STATE_MONTH, mMonth);
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
    public void setYear(@NonNull String year) {
        mYear = year;
    }

    @Override
    public Month getMonth() {
        return mMonth;
    }

    @Override
    public void setMonth(@NonNull Month month) {
        mMonth = month;
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
        if (!Objects.equals(year, mYear)) {
            mYear = year;
            reloadData();
        }
    }

    @Override
    public void onMonthSelected(AdapterView<?> parent, View view, int position, long id) {
        final Month month = (Month) parent.getItemAtPosition(position);
        if (!Objects.equals(month, mMonth)) {
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
