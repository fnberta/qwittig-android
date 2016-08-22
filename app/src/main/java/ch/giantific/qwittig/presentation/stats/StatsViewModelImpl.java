package ch.giantific.qwittig.presentation.stats;

import android.databinding.Bindable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rest.StatsResult;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.stats.models.ChartCurrencyFormatter;
import ch.giantific.qwittig.presentation.stats.models.DateAxisFormatter;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsViewModelImpl extends ViewModelBaseImpl<StatsViewModel.ViewListener>
        implements StatsViewModel {

    private StatsTypeItem type;
    private StatsPeriodItem period;
    private Date startDate;
    private Date endDate;
    private PieData storesData;
    private PieData identitiesData;
    private BarData timeData;
    private String pieTotal;
    private String barAverage;
    private NumberFormat currencyFormatter;
    private ChartCurrencyFormatter chartCurrencyFormatter;
    private AxisValueFormatter barXAxisFormatter;
    private boolean empty;

    public StatsViewModelImpl(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull RxBus<Object> eventBus,
                              @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        if (savedState == null) {
            loading = true;
        }
    }

    @Override
    public void setType(@NonNull StatsTypeItem type) {
        this.type = type;
    }

    @Override
    public void setPeriod(@NonNull StatsPeriodItem period) {
        this.period = period;
        updateStartAndEndDate();
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    @Override
    @Bindable
    public String getPieTotal() {
        return pieTotal;
    }

    @Override
    public void setPieTotal(float pieTotal) {
        this.pieTotal = currencyFormatter.format(pieTotal);
        notifyPropertyChanged(BR.pieTotal);
    }

    @Override
    @Bindable
    public PieData getStoresData() {
        return storesData;
    }

    @Override
    public void setStoresData(@NonNull PieData pieData) {
        storesData = pieData;
        notifyPropertyChanged(BR.storesData);
    }

    @Override
    @Bindable
    public PieData getIdentitiesData() {
        return identitiesData;
    }

    @Override
    public void setIdentitiesData(@NonNull PieData pieData) {
        identitiesData = pieData;
        notifyPropertyChanged(BR.identitiesData);
    }

    @Override
    public String getBarAverage() {
        return barAverage;
    }

    @Override
    public void setBarAverage(float barAverage) {
        this.barAverage = currencyFormatter.format(barAverage);
        notifyPropertyChanged(BR.barAverage);
    }

    @Override
    public BarData getTimeData() {
        return timeData;
    }

    @Override
    public void setTimeData(@NonNull BarData barData) {
        timeData = barData;
        notifyPropertyChanged(BR.timeData);
    }

    @Override
    @Bindable
    public AxisValueFormatter getBarXAxisFormatter() {
        return barXAxisFormatter;
    }

    @Override
    public void setBarXAxisFormatter(@NonNull final String unit) {
        barXAxisFormatter = new DateAxisFormatter(unit);
        notifyPropertyChanged(BR.barXAxisFormatter);
    }

    @Override
    @Bindable
    public ChartCurrencyFormatter getChartCurrencyFormatter() {
        return chartCurrencyFormatter;
    }

    @Override
    public void setChartCurrencyFormatter(@NonNull ChartCurrencyFormatter chartCurrencyFormatter) {
        this.chartCurrencyFormatter = chartCurrencyFormatter;
        notifyPropertyChanged(BR.chartCurrencyFormatter);
    }

    @Override
    public void onDataLoaded(@Nullable final Observable<StatsResult> data) {
        final FirebaseUser currentUser = userRepo.getCurrentUser();
        if (data == null || currentUser == null) {
            setLoading(false);
            setEmpty(true);
            return;
        }

        getSubscriptions().add(userRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        currencyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, false);
                        setChartCurrencyFormatter(new ChartCurrencyFormatter(currencyFormatter));
                    }
                })
                .flatMap(new Func1<Identity, Single<StatsResult>>() {
                    @Override
                    public Single<StatsResult> call(Identity identity) {
                        return data.toSingle();
                    }
                })
                .subscribe(new SingleSubscriber<StatsResult>() {
                    @Override
                    public void onSuccess(StatsResult statsResult) {
                        final StatsResult.PieStats pieStats = statsResult.getPieStats();
                        final StatsResult.BarStats barStats = statsResult.getBarStats();
                        final float total = pieStats.getTotal();
                        if (total > 0) {
                            empty = false;
                            setPieTotal(total);
                            setStoresChartData(pieStats);
                            setIdentitiesChartData(pieStats);

                            setBarAverage(barStats.getAverage());
                            setBarXAxisFormatter(barStats.getUnit());
                            setTimeChartData(barStats);
                        } else {
                            empty = true;
                        }

                        setLoading(false);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load stats data with error:");
                        setEmpty(true);
                        setLoading(false);
                    }
                })
        );
    }

    private void setStoresChartData(@NonNull StatsResult.PieStats pieStats) {
        final List<PieEntry> entries = new ArrayList<>();

        final Map<String, Float> stores = pieStats.getStores();
        for (Map.Entry<String, Float> entry : stores.entrySet()) {
            final Float value = entry.getValue();
            entries.add(new PieEntry(value, entry.getKey()));
        }

        final PieData pieData = getPieData(entries);
        setStoresData(pieData);
    }

    private void setIdentitiesChartData(@NonNull StatsResult.PieStats pieStats) {
        final List<PieEntry> entries = new ArrayList<>();

        final Map<String, StatsResult.PieStats.IdentityTotal> stores = pieStats.getIdentities();
        for (Map.Entry<String, StatsResult.PieStats.IdentityTotal> entry : stores.entrySet()) {
            final StatsResult.PieStats.IdentityTotal identityTotal = entry.getValue();
            entries.add(new PieEntry(identityTotal.getTotal(), identityTotal.getNickname()));
        }

        final PieData pieData = getPieData(entries);
        setIdentitiesData(pieData);
    }

    @NonNull
    private PieData getPieData(@NonNull List<PieEntry> entries) {
        final PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setValueFormatter(chartCurrencyFormatter);
        pieDataSet.setColors(getColors());
        pieDataSet.setSliceSpace(4);

        final PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
        return pieData;
    }

    private void setTimeChartData(@NonNull StatsResult.BarStats barStats) {
        final List<BarEntry> entries = new ArrayList<>();

        final Map<Long, Float> data = barStats.getData();
        for (Map.Entry<Long, Float> entry : data.entrySet()) {
            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        final BarDataSet barDataSet = new BarDataSet(entries, "");
        barDataSet.setColors(getColors());
        barDataSet.setValueFormatter(chartCurrencyFormatter);

        final BarData barData = new BarData(barDataSet);
        barData.setHighlightEnabled(true);
        setTimeData(barData);
    }

    @NonNull
    private List<Integer> getColors() {
        final List<Integer> colors = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            colors.add(getColor(i));
        }

        return colors;
    }

    @ColorInt
    private int getColor(int position) {
        final int[] colors = view.getStatsColors();
        if (position >= 0 && position < colors.length) {
            return colors[position];
        } else if (position >= colors.length) {
            return getColor(position - colors.length);
        }

        return -1;
    }

    @Override
    public void onTypeSelected(AdapterView<?> parent, View view, int position, long id) {
        final StatsTypeItem type = (StatsTypeItem) parent.getItemAtPosition(position);
        if (!Objects.equals(type, this.type)) {
            this.type = type;
            reloadData();
        }
    }

    @Override
    public void onPeriodSelected(AdapterView<?> parent, View view, int position, long id) {
        final StatsPeriodItem period = (StatsPeriodItem) parent.getItemAtPosition(position);
        if (Objects.equals(period, this.period)) {
            return;
        }

        this.period = period;
        updateStartAndEndDate();
        // TODO: don't if custom
        reloadData();
    }

    private void updateStartAndEndDate() {
        final int type = period.getType();
        switch (type) {
            case StatsPeriod.THIS_MONTH: {
                endDate = new Date();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                DateUtils.resetToMidnight(calendar);
                startDate = calendar.getTime();
                break;
            }
            case StatsPeriod.LAST_MONTH: {
                final Calendar calendar = Calendar.getInstance();
                DateUtils.resetToMidnight(calendar);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MILLISECOND, -1);
                endDate = calendar.getTime();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                DateUtils.resetToMidnight(calendar);
                startDate = calendar.getTime();
                break;
            }
            case StatsPeriod.THIS_YEAR: {
                endDate = new Date();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                DateUtils.resetToMidnight(calendar);
                startDate = calendar.getTime();
                break;
            }
            case StatsPeriod.CUSTOM:
                // TODO: show date picker
                break;
        }
    }

    private void reloadData() {
        if (!view.isNetworkAvailable()) {
            view.showMessageWithAction(R.string.toast_error_stats_load,
                    new MessageAction(R.string.action_retry) {
                        @Override
                        public void onClick(View v) {
                            reloadData();
                        }
                    });
            setLoading(false);
            return;
        }

        empty = false;
        setLoading(true);
        view.reloadData();
    }
}
