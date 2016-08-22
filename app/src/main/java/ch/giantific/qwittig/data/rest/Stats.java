/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Single;

/**
 * Defines the call to the server to calculate spending stats.
 */
public interface Stats {
    /**
     * Makes a POST call to the server to calculate spending stats.
     *
     * @param statsRequest the parameters for the request
     * @return the result as a {@link Single}
     */
    @POST("stats")
    Single<StatsResult> calculateStats(@Body StatsRequest statsRequest);
}
