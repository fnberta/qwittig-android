package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsRequest {

    private final String idToken;
    private final Date startDate;
    private final Date endDate;

    public StatsRequest(@NonNull String idToken,
                        @NonNull Date startDate,
                        @NonNull Date endDate) {
        this.idToken = idToken;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getIdToken() {
        return idToken;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
