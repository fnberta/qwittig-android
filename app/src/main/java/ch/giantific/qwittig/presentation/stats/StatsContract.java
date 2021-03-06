package ch.giantific.qwittig.presentation.stats;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.data.rest.stats.StatsResult;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import rx.Observable;
import rx.Single;

/**
 * Created by fabio on 14.08.16.
 */
public interface StatsContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void setType(@NonNull StatsTypeItem type);

        void setPeriod(@NonNull StatsPeriodItem statsPeriod);

        Date getStartDate();

        Date getEndDate();

        void onTypeSelected(AdapterView<?> parent, View view, int position, long id);

        void onPeriodSelected(AdapterView<?> parent, View view, int position, long id);
    }

    interface ViewListener extends BaseView {

        Observable<StatsResult> getStatsResult();

        void reloadData();

        int[] getStatsColors();

        String getSpendingLabel();
    }

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
}
