package ch.giantific.qwittig.data.repositories;

import javax.inject.Inject;

/**
 * Created by fabio on 19.07.16.
 */
public class StatsRepository {

    private static final String STATS_SPENDING = "statsSpending";
    private static final String STATS_STORES = "statsStores";
    private static final String STATS_CURRENCIES = "statsCurrencies";

    private static final String PARAM_YEAR = "year";
    private static final String PARAM_MONTH = "month";

    @Inject
    public StatsRepository() {
    }

    //    @Override
//    public Single<Stats> calcStatsSpending(@NonNull String groupId, @NonNull String year, int month) {
//        Map<String, Object> params = getStatsPushParams(groupId, year, month);
//        return this.<String>callFunctionInBackground(STATS_SPENDING, params)
//                .map(new Func1<String, Stats>() {
//                    @Override
//                    public Stats call(String s) {
//                        return mGson.fromJson(s, Stats.class);
//                    }
//                });
//    }
//
//    @Override
//    public Single<Stats> calcStatsStores(@NonNull String groupId, @NonNull String year, int month) {
//        Map<String, Object> params = getStatsPushParams(groupId, year, month);
//        return this.<String>callFunctionInBackground(STATS_STORES, params)
//                .map(new Func1<String, Stats>() {
//                    @Override
//                    public Stats call(String s) {
//                        return mGson.fromJson(s, Stats.class);
//                    }
//                });
//    }
//
//    @Override
//    public Single<Stats> calcStatsCurrencies(@NonNull String groupId, @NonNull String year, int month) {
//        Map<String, Object> params = getStatsPushParams(groupId, year, month);
//        return this.<String>callFunctionInBackground(STATS_CURRENCIES, params)
//                .map(new Func1<String, Stats>() {
//                    @Override
//                    public Stats call(String s) {
//                        return mGson.fromJson(s, Stats.class);
//                    }
//                });
//    }
//
//    @NonNull
//    private Map<String, Object> getStatsPushParams(@NonNull String groupId, @NonNull String year,
//                                                   int month) {
//        Map<String, Object> params = new HashMap<>();
//        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);
//        params.put(PARAM_YEAR, year);
//        if (month != 0) {
//            params.put(PARAM_MONTH, month - 1);
//        }
//        return params;
//    }
}
