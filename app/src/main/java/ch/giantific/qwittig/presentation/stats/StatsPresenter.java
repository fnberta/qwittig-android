package ch.giantific.qwittig.presentation.stats;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rest.stats.StatsResult;
import ch.giantific.qwittig.data.rest.stats.StatsResult.UnitType;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.stats.StatsContract.StatsPeriod;
import ch.giantific.qwittig.presentation.stats.formatters.ChartCurrencyFormatter;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsPresenter extends BasePresenterImpl<StatsContract.ViewListener>
        implements StatsContract.Presenter {

    private StatsViewModel viewModel;
    private StatsTypeItem type;
    private StatsPeriodItem period;
    private Date startDate;
    private Date endDate;
    private NumberFormat currencyFormatter;

    @Inject
    public StatsPresenter(@NonNull Navigator navigator,
                          @NonNull StatsViewModel viewModel,
                          @NonNull UserRepository userRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
    }

    @Override
    public void setType(@NonNull StatsTypeItem type) {
        this.type = type;
    }

    @Override
    public void setPeriod(@NonNull StatsPeriodItem period) {
        this.period = period;
        updateStartEndDate();
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.getUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> {
                    currencyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, false);
                    viewModel.setChartCurrencyFormatter(new ChartCurrencyFormatter(currencyFormatter));
                })
                .flatMapObservable(identity -> view.getStatsResult())
                .subscribe(new IndefiniteSubscriber<StatsResult>() {
                    @Override
                    public void onNext(StatsResult statsResult) {
                        final StatsResult.GroupStats groupStats = statsResult.getGroupStats();
                        final StatsResult.Pie pie = groupStats.getPie();
                        final StatsResult.Bar bar = groupStats.getBar();
                        final float total = pie.getTotal();
                        final boolean hasData = total > 0;
                        if (hasData) {
                            viewModel.setPieTotal(currencyFormatter.format(total));
                            setStoresChartData(pie);
                            setIdentitiesChartData(pie);

                            viewModel.setBarAverage(currencyFormatter.format(bar.getAverage()));
                            viewModel.setBarXAxisFormatter(bar.getUnit());
                            setTimeChartData(bar);
                        }

                        viewModel.finishLoading(!hasData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        viewModel.finishLoading(true);
                    }
                })
        );
    }

    private void setStoresChartData(@NonNull StatsResult.Pie pie) {
        final List<PieEntry> entries = new ArrayList<>();

        final Map<String, Float> stores = pie.getStores();
        for (Map.Entry<String, Float> entry : stores.entrySet()) {
            final Float value = entry.getValue();
            entries.add(new PieEntry(value, entry.getKey()));
        }

        final PieData pieData = getPieData(entries);
        viewModel.setStoresData(pieData);
    }

    private void setIdentitiesChartData(@NonNull StatsResult.Pie pie) {
        final List<PieEntry> entries = new ArrayList<>();

        final Map<String, StatsResult.Pie.IdentityTotal> stores = pie.getIdentities();
        for (Map.Entry<String, StatsResult.Pie.IdentityTotal> entry : stores.entrySet()) {
            final StatsResult.Pie.IdentityTotal identityTotal = entry.getValue();
            entries.add(new PieEntry(identityTotal.getTotal(), identityTotal.getNickname()));
        }

        final PieData pieData = getPieData(entries);
        viewModel.setIdentitiesData(pieData);
    }

    @NonNull
    private PieData getPieData(@NonNull List<PieEntry> entries) {
        final PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setValueFormatter(viewModel.getChartCurrencyFormatter());
        pieDataSet.setColors(view.getStatsColors());
        pieDataSet.setSliceSpace(4);

        final PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
        return pieData;
    }

    private void setTimeChartData(@NonNull StatsResult.Bar bar) {
        final List<BarEntry> entries = new ArrayList<>();

        final Map<Long, Float> data = bar.getData();
        final Date date = new Date();
        final Calendar calendar = Calendar.getInstance();
        final String unit = bar.getUnit();
        for (Map.Entry<Long, Float> entry : data.entrySet()) {
            date.setTime(entry.getKey());
            calendar.setTime(date);
            float dateValue = 0;
            switch (unit) {
                case UnitType.DAYS:
                    dateValue = calendar.get(Calendar.DAY_OF_MONTH);
                    break;
                case UnitType.MONTHS:
                    dateValue = calendar.get(Calendar.MONTH);
                    break;
                case UnitType.YEARS:
                    dateValue = calendar.get(Calendar.YEAR);
                    break;
            }

            entries.add(new BarEntry(dateValue, entry.getValue()));
        }

        final BarDataSet barDataSet = new BarDataSet(entries, view.getSpendingLabel());
        final int[] statsColors = view.getStatsColors();
        barDataSet.setColors(statsColors[0]);
        barDataSet.setValueFormatter(viewModel.getChartCurrencyFormatter());

        final BarData barData = new BarData(barDataSet);
        barData.setHighlightEnabled(true);

        viewModel.setTimeData(barData);
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
        if (!Objects.equals(period, this.period)) {
            this.period = period;
            final boolean reload = updateStartEndDate();
            if (reload) {
                reloadData();
            }
        }
    }

    private boolean updateStartEndDate() {
        final int type = period.getType();
        switch (type) {
            case StatsPeriod.THIS_MONTH: {
                endDate = new Date();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                DateUtils.resetToMidnight(calendar);
                startDate = calendar.getTime();
                return true;
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
                return true;
            }
            case StatsPeriod.THIS_YEAR: {
                endDate = new Date();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                DateUtils.resetToMidnight(calendar);
                startDate = calendar.getTime();
                return true;
            }
            case StatsPeriod.CUSTOM:
                // TODO: show date picker
                return false;
        }

        return false;
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
            return;
        }

        viewModel.setLoading(true);
        viewModel.setEmpty(false);
        view.reloadData();
    }
}
