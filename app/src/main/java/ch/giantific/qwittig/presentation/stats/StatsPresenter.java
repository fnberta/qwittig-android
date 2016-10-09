package ch.giantific.qwittig.presentation.stats;

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
import ch.giantific.qwittig.data.rest.StatsResult;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.stats.StatsContract.StatsPeriod;
import ch.giantific.qwittig.presentation.stats.formatters.ChartCurrencyFormatter;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import timber.log.Timber;

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
    public void onDataLoaded(@Nullable final Observable<StatsResult> data) {
        final FirebaseUser currentUser = userRepo.getCurrentUser();
        if (data == null || currentUser == null) {
            viewModel.setLoading(false);
            viewModel.setEmpty(true);
            return;
        }

        subscriptions.add(userRepo.getUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> {
                    currencyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, false);
                    viewModel.setChartCurrencyFormatter(new ChartCurrencyFormatter(currencyFormatter));
                })
                .flatMap(identity -> data.toSingle())
                .subscribe(new SingleSubscriber<StatsResult>() {
                    @Override
                    public void onSuccess(StatsResult statsResult) {
                        final StatsResult.PieStats pieStats = statsResult.getPieStats();
                        final StatsResult.BarStats barStats = statsResult.getBarStats();
                        final float total = pieStats.getTotal();
                        if (total > 0) {
                            viewModel.setEmpty(false);
                            viewModel.setPieTotal(currencyFormatter.format(total));
                            setStoresChartData(pieStats);
                            setIdentitiesChartData(pieStats);

                            viewModel.setBarAverage(currencyFormatter.format(barStats.getAverage()));
                            viewModel.setBarXAxisFormatter(barStats.getUnit());
                            setTimeChartData(barStats);
                        } else {
                            viewModel.setEmpty(true);
                        }

                        viewModel.setLoading(false);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load stats data with error:");
                        viewModel.setEmpty(true);
                        viewModel.setLoading(false);
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
        viewModel.setStoresData(pieData);
    }

    private void setIdentitiesChartData(@NonNull StatsResult.PieStats pieStats) {
        final List<PieEntry> entries = new ArrayList<>();

        final Map<String, StatsResult.PieStats.IdentityTotal> stores = pieStats.getIdentities();
        for (Map.Entry<String, StatsResult.PieStats.IdentityTotal> entry : stores.entrySet()) {
            final StatsResult.PieStats.IdentityTotal identityTotal = entry.getValue();
            entries.add(new PieEntry(identityTotal.getTotal(), identityTotal.getNickname()));
        }

        final PieData pieData = getPieData(entries);
        viewModel.setIdentitiesData(pieData);
    }

    @NonNull
    private PieData getPieData(@NonNull List<PieEntry> entries) {
        final PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setValueFormatter(viewModel.getChartCurrencyFormatter());
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
        barDataSet.setValueFormatter(viewModel.getChartCurrencyFormatter());

        final BarData barData = new BarData(barDataSet);
        barData.setHighlightEnabled(true);
        viewModel.setTimeData(barData);
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
        updateStartEndDate();
        // TODO: don't reload if custom period
        reloadData();
    }

    private void updateStartEndDate() {
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
            viewModel.setLoading(false);
            return;
        }

        viewModel.setEmpty(false);
        viewModel.setLoading(true);
        view.reloadData();
    }
}
