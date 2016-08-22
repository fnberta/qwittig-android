package ch.giantific.qwittig.presentation.stats;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.data.rest.StatsResult;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.stats.models.ChartCurrencyFormatter;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import rx.Observable;

/**
 * Created by fabio on 14.08.16.
 */
public interface StatsViewModel extends ViewModel<StatsViewModel.ViewListener>,
        LoadingViewModel {

    void setType(@NonNull StatsTypeItem type);

    void setPeriod(@NonNull StatsPeriodItem statsPeriod);

    Date getStartDate();

    Date getEndDate();

    @Bindable
    boolean isEmpty();

    void setEmpty(boolean empty);

    @Bindable
    String getPieTotal();

    void setPieTotal(float pieTotal);

    @Bindable
    PieData getStoresData();

    void setStoresData(@NonNull PieData pieData);

    @Bindable
    PieData getIdentitiesData();

    void setIdentitiesData(@NonNull PieData pieData);

    @Bindable
    String getBarAverage();

    void setBarAverage(float barAverage);

    @Bindable
    BarData getTimeData();

    void setTimeData(@NonNull BarData barData);

    @Bindable
    AxisValueFormatter getBarXAxisFormatter();

    void setBarXAxisFormatter(@NonNull String unit);

    @Bindable
    ChartCurrencyFormatter getChartCurrencyFormatter();

    void setChartCurrencyFormatter(@NonNull ChartCurrencyFormatter formatter);

    void onDataLoaded(@Nullable Observable<StatsResult> data);

    void onTypeSelected(AdapterView<?> parent, View view, int position, long id);

    void onPeriodSelected(AdapterView<?> parent, View view, int position, long id);

    @IntDef({StatsType.GROUP, StatsType.USER})
    @Retention(RetentionPolicy.SOURCE)
    @interface StatsType {
        int GROUP = 1;
        int USER = 2;
    }

    @IntDef({StatsPeriod.THIS_MONTH, StatsPeriod.LAST_MONTH, StatsPeriod.THIS_YEAR,
            StatsPeriod.CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    @interface StatsPeriod {
        int THIS_MONTH = 1;
        int LAST_MONTH = 2;
        int THIS_YEAR = 3;
        int CUSTOM = 4;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void reloadData();

        int[] getStatsColors();
    }
}
