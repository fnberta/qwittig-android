package ch.giantific.qwittig.presentation.stats;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.stats.formatters.ChartCurrencyFormatter;
import ch.giantific.qwittig.presentation.stats.formatters.DateAxisFormatter;

/**
 * Created by fabio on 29.09.16.
 */

public class StatsViewModel extends BaseObservable
        implements Parcelable, LoadingViewModel {

    public static final Creator<StatsViewModel> CREATOR = new Creator<StatsViewModel>() {
        @Override
        public StatsViewModel createFromParcel(Parcel in) {
            return new StatsViewModel(in);
        }

        @Override
        public StatsViewModel[] newArray(int size) {
            return new StatsViewModel[size];
        }
    };
    public static final String TAG = StatsViewModel.class.getCanonicalName();
    private boolean loading;
    private boolean empty;
    private PieData storesData;
    private PieData identitiesData;
    private BarData timeData;
    private String pieTotal;
    private String barAverage;
    private ChartCurrencyFormatter chartCurrencyFormatter;
    private IAxisValueFormatter barXAxisFormatter;

    public StatsViewModel() {
        this.empty = true;
        this.loading = true;
    }

    private StatsViewModel(Parcel in) {
        loading = in.readByte() != 0;
        empty = in.readByte() != 0;
        pieTotal = in.readString();
        barAverage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeString(pieTotal);
        dest.writeString(barAverage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
    }

    public void finishLoading(boolean empty) {
        this.empty = empty;
        setLoading(false);
    }

    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    @Bindable
    public String getPieTotal() {
        return pieTotal;
    }

    public void setPieTotal(@NonNull String pieTotal) {
        this.pieTotal = pieTotal;
        notifyPropertyChanged(BR.pieTotal);
    }

    @Bindable
    public PieData getStoresData() {
        return storesData;
    }

    public void setStoresData(@NonNull PieData pieData) {
        storesData = pieData;
        notifyPropertyChanged(BR.storesData);
    }

    @Bindable
    public PieData getIdentitiesData() {
        return identitiesData;
    }

    public void setIdentitiesData(@NonNull PieData pieData) {
        identitiesData = pieData;
        notifyPropertyChanged(BR.identitiesData);
    }

    @Bindable
    public String getBarAverage() {
        return barAverage;
    }

    public void setBarAverage(@NonNull String barAverage) {
        this.barAverage = barAverage;
        notifyPropertyChanged(BR.barAverage);
    }

    @Bindable
    public BarData getTimeData() {
        return timeData;
    }

    public void setTimeData(@NonNull BarData barData) {
        timeData = barData;
        notifyPropertyChanged(BR.timeData);
    }

    @Bindable
    public IAxisValueFormatter getBarXAxisFormatter() {
        return barXAxisFormatter;
    }

    public void setBarXAxisFormatter(@NonNull final String unit) {
        barXAxisFormatter = new DateAxisFormatter(unit);
        notifyPropertyChanged(BR.barXAxisFormatter);
    }

    @Bindable
    public ChartCurrencyFormatter getChartCurrencyFormatter() {
        return chartCurrencyFormatter;
    }

    public void setChartCurrencyFormatter(@NonNull ChartCurrencyFormatter chartCurrencyFormatter) {
        this.chartCurrencyFormatter = chartCurrencyFormatter;
        notifyPropertyChanged(BR.chartCurrencyFormatter);
    }

}
