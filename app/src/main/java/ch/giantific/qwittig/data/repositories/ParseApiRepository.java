/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import rx.Single;
import rx.SingleSubscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * Provides access to server functions implemented in the Parse.com Cloud Code framework.
 */
public class ParseApiRepository implements ApiRepository {

    private static final String CALCULATE_BALANCE = "calculateBalance";
    private static final String INVITE_USER = "inviteUsers";
    private static final String PUSH_TASK_REMIND = "pushTaskRemind";
    private static final String PUSH_COMPENSATION_REMIND = "pushCompensationRemind";
    private static final String GROUP_ROLE_ADD_USER = "groupRoleAddUser";
    private static final String GROUP_ROLE_REMOVE_USER = "groupRoleRemoveUser";
    private static final String DELETE_PARSE_FILE = "deleteParseFile";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String STATS_SPENDING = "statsSpending";
    private static final String STATS_STORES = "statsStores";
    private static final String STATS_CURRENCIES = "statsCurrencies";

    private static final String PARAM_EMAIL = "emails";
    private static final String PARAM_FILE_NAME = "fileName";
    private static final String PARAM_YEAR = "year";
    private static final String PARAM_MONTH = "month";
    private static final String PARAM_ID_TOKEN = "idToken";
    private static final String LOGIN_WITH_GOOGLE = "loginWithGoogle";

    private Gson mGson;

    public ParseApiRepository(@NonNull Gson gson) {
        mGson = gson;
    }

    private <T> Single<T> callFunctionInBackground(@NonNull final String function,
                                                   @NonNull final Map<String, ?> params) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                ParseCloud.callFunctionInBackground(function, params, new FunctionCallback<T>() {
                    @Override
                    public void done(T object, ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @Override
    public Single<String> calcUserBalances() {
        return callFunctionInBackground(CALCULATE_BALANCE, Collections.<String, Object>emptyMap());
    }

    @Override
    public Single<String> addIdentity(@NonNull String nickname,
                                      @NonNull String groupName) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PARAM_EMAIL, nickname);
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);

        return callFunctionInBackground(INVITE_USER, params);
    }

    @Override
    public Single<String> pushTaskReminder(@NonNull String taskId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK_ID, taskId);

        return callFunctionInBackground(PUSH_TASK_REMIND, params);
    }

    @Override
    public Single<String> pushCompensationReminder(@NonNull final String compensationId,
                                                   @NonNull final String currencyCode) {
        final Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        return callFunctionInBackground(PUSH_COMPENSATION_REMIND, params);
    }

    @NonNull
    private Map<String, Object> getCompensationPushParams(@NonNull String compensationId,
                                                          @NonNull String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION_ID, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }

    @Override
    public Single<String> addUserToGroupRole(@NonNull final String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);

        return callFunctionInBackground(GROUP_ROLE_ADD_USER, params);
    }

    @Override
    public Single<String> removeUserFromGroupRole(@NonNull final String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);

        return callFunctionInBackground(GROUP_ROLE_REMOVE_USER, params);

        // had no callback before
    }

    @Override
    public Single<String> deleteParseFile(@NonNull String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, fileName);

        return callFunctionInBackground(DELETE_PARSE_FILE, params);
    }

    @Override
    public Single<String> deleteAccount() {
        return callFunctionInBackground(DELETE_ACCOUNT, Collections.<String, Object>emptyMap());
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

    @Override
    public Single<JSONObject> loginWithGoogle(@NonNull String idToken) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_ID_TOKEN, idToken);

        return this.<String>callFunctionInBackground(LOGIN_WITH_GOOGLE, params)
                .map(new Func1<String, JSONObject>() {
                    @Override
                    public JSONObject call(String s) {
                        try {
                            return new JSONObject(s);
                        } catch (JSONException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                });
    }
}
