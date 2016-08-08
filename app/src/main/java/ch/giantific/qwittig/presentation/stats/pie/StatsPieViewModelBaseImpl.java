/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;

import android.databinding.Bindable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.stats.StatsViewModelBaseImpl;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an abstract base implementation of the {@link StatsPieViewModel} interface.
 */
public abstract class StatsPieViewModelBaseImpl<T extends StatsPieViewModel.ViewListener>
        extends StatsViewModelBaseImpl<T>
        implements StatsPieViewModel<T> {

    private static final String STATE_SORT_USERS = "STATE_SORT_USERS";
    private static final String STATE_SHOW_PERCENT = "STATE_SHOW_PERCENT";
    private final List<PieData> mUserPieData = new ArrayList<>();
    private final List<String> mUserNicknames = new ArrayList<>();
    protected boolean mSortUsers;
    protected boolean mShowPercent;
    protected String mGroupCurrency;
    private String mGroupName;
    private PieData mPieData;
    private String mCenterText;

    public StatsPieViewModelBaseImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        if (savedState != null) {
            mSortUsers = savedState.getBoolean(STATE_SORT_USERS, false);
            mShowPercent = savedState.getBoolean(STATE_SHOW_PERCENT, true);
        } else {
            mShowPercent = true;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_SORT_USERS, mSortUsers);
        outState.putBoolean(STATE_SHOW_PERCENT, mShowPercent);
    }

    @Override
    @Bindable
    public PieData getPieData() {
        return mPieData;
    }

    @Override
    public void setPieData(@NonNull PieData pieData) {
        mPieData = pieData;
        notifyPropertyChanged(BR.pieData);
    }

    @Override
    @Bindable
    public String getCenterText() {
        return mCenterText;
    }

    @Override
    public void setCenterText(@NonNull String centerText) {
        mCenterText = centerText;
        notifyPropertyChanged(BR.centerText);
    }

    @Override
    public boolean isShowPercentage() {
        return mShowPercent;
    }

    @Override
    @Bindable
    public boolean isSortUsers() {
        return mSortUsers;
    }

    @Override
    public void setSortUsers(boolean sortUsers) {
        mSortUsers = sortUsers;
        notifyPropertyChanged(BR.sortUsers);
    }

    @Override
    public void onDataLoaded(@Nullable final Observable<Stats> data) {
        final FirebaseUser currentUser = mUserRepo.getCurrentUser();
        if (data == null || currentUser == null) {
            setLoading(false);
            setDataEmpty(true);
            return;
        }

        getSubscriptions().add(mUserRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        mGroupName = identity.getGroupName();
                        mGroupCurrency = identity.getGroupCurrency();
                    }
                })
                .flatMap(new Func1<Identity, Single<Stats>>() {
                    @Override
                    public Single<Stats> call(Identity identity) {
                        return data.toSingle();
                    }
                })
                .doOnSuccess(new Action1<Stats>() {
                    @Override
                    public void call(Stats stats) {
                        mStatsData = stats;
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        setDataEmpty(true);
                        setLoading(false);
                    }
                })
                .subscribe(new SingleSubscriber<Stats>() {
                    @Override
                    public void onSuccess(Stats stats) {
                        setChartData();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessageWithAction(R.string.toast_error_stats_load, getRetryAction());
                    }
                })
        );
    }

    protected void setChartData() {
        final List<Stats.Member> userData = mStatsData.getMembers();
        final Stats.Group groupData = mStatsData.getGroup();

        if (mSortUsers) {
            setUserChartData(userData);
        } else {
            setGroupChartData(groupData);
        }
    }

    private void setUserChartData(@NonNull List<Stats.Member> userData) {
        mUserPieData.clear();
        mUserNicknames.clear();

        for (Stats.Member member : userData) {
            final List<Entry> yVals = new ArrayList<>();
            final List<String> xVals = new ArrayList<>();

            mUserNicknames.add(member.getNickname());

            final List<Stats.Unit> units = member.getUnits();
            for (int i = 0, unitsSize = units.size(); i < unitsSize; i++) {
                final Stats.Unit unit = units.get(i);
                final float value = getValue(unit);

                if (value > 0) {
                    xVals.add(unit.getIdentifier());
                    yVals.add(new Entry(value, i));

                    mDataEmpty = false;
                }
            }

            final PieDataSet pieDataSet = new PieDataSet(yVals, "");
            setDataSetOptions(pieDataSet);

            final PieData pieData = new PieData(xVals, pieDataSet);
            setDataOptions(pieData);
            mUserPieData.add(pieData);
        }

        if (!mDataEmpty) {
            mView.notifyDataSetChanged();
        }
        setLoading(false);
    }

    protected float getValue(@NonNull Stats.Unit unit) {
        return unit.getTotal();
    }

    private void setGroupChartData(@NonNull Stats.Group groupData) {
        final List<Entry> yVals = new ArrayList<>();
        final List<String> xVals = new ArrayList<>();

        final List<Stats.Unit> units = groupData.getUnits();
        for (int i = 0, unitsSize = units.size(); i < unitsSize; i++) {
            final Stats.Unit unit = units.get(i);
            final float value = getValue(unit);

            if (value > 0) {
                xVals.add(unit.getIdentifier());
                yVals.add(new Entry(value, i));

                mDataEmpty = false;
            }
        }

        final PieDataSet pieDataSet = new PieDataSet(yVals, "");
        setDataSetOptions(pieDataSet);
        final PieData pieData = new PieData(xVals, pieDataSet);
        setDataOptions(pieData);

        setCenterText(mGroupName);
        setPieData(pieData);
        setLoading(false);
    }

    @CallSuper
    protected void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        pieDataSet.setColors(getColors());
        pieDataSet.setSliceSpace(3f);
    }

    private void setDataOptions(@NonNull PieData pieData) {
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
    }

    @Override
    public PieData getDataAtPosition(int position) {
        return mUserPieData.get(position);
    }

    @Override
    public String getNicknameAtPosition(int position) {
        return mUserNicknames.get(position);
    }

    @Override
    public int getItemCount() {
        return mUserPieData.size();
    }

    @Override
    public void onTogglePercentMenuClick() {
        mShowPercent = !mShowPercent;
        setChartData();
    }

    @Override
    public void onToggleSortUsersMenuClick() {
        setSortUsers(!mSortUsers);
        setChartData();
    }
}
