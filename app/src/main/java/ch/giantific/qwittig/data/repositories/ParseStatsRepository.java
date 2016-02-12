/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import rx.Single;
import rx.functions.Func1;

/**
 * Created by fabio on 09.02.16.
 */
public class ParseStatsRepository extends ParseBaseRepository implements StatsRepository {

    private static final String STATS_SPENDING = "statsSpending";
    private static final String STATS_STORES = "statsStores";
    private static final String STATS_CURRENCIES = "statsCurrencies";

    private static final String PARAM_YEAR = "year";
    private static final String PARAM_MONTH = "month";

    private Gson mGson;

    public ParseStatsRepository(@NonNull Gson gson) {
        mGson = gson;
    }

    @Override
    protected String getClassName() {
        throw new UnsupportedOperationException("Not supported in this repository!");
    }

    @Override
    public Single<Stats> calcStatsSpending(@NonNull String groupId, @NonNull String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        return this.<String>callFunctionInBackground(STATS_SPENDING, params)
                .map(new Func1<String, Stats>() {
                    @Override
                    public Stats call(String s) {
                        return mGson.fromJson(s, Stats.class);
                    }
                });
    }

    @Override
    public Single<Stats> calcStatsStores(@NonNull String groupId, @NonNull String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        return this.<String>callFunctionInBackground(STATS_STORES, params)
                .map(new Func1<String, Stats>() {
                    @Override
                    public Stats call(String s) {
                        return mGson.fromJson(s, Stats.class);
                    }
                });
    }

    @Override
    public Single<Stats> calcStatsCurrencies(@NonNull String groupId, @NonNull String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        return this.<String>callFunctionInBackground(STATS_CURRENCIES, params)
                .map(new Func1<String, Stats>() {
                    @Override
                    public Stats call(String s) {
                        return mGson.fromJson(s, Stats.class);
                    }
                });
    }

    @NonNull
    private Map<String, Object> getStatsPushParams(@NonNull String groupId, @NonNull String year,
                                                   int month) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);
        params.put(PARAM_YEAR, year);
        if (month != 0) {
            params.put(PARAM_MONTH, month - 1);
        }
        return params;
    }
}
