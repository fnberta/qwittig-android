/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of {@link GroupRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseGroupRepository extends ParseBaseRepository implements GroupRepository {

    private static final String ADD_NEW_GROUP = "addGroup";

    public ParseGroupRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Group.CLASS;
    }

    @Override
    public Single<String> addNewGroup(@NonNull String groupName, @NonNull String groupCurrency) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, groupCurrency);
        return callFunctionInBackground(ADD_NEW_GROUP, params);
    }

    @Override
    public Observable<Group> fetchGroupDataAsync(@NonNull final Group group) {
        if (group.isDataAvailable()) {
            return Observable.just(group);
        }

        return fetchLocal(group)
                .toObservable()
                .onErrorResumeNext(fetchIfNeeded(group).toObservable());
    }

    @Override
    public Single<Group> getGroupOnlineAsync(@NonNull final String groupId) {
        final ParseQuery<Group> query = ParseQuery.getQuery(Group.CLASS);
        return get(query, groupId);
    }

    @Override
    @Nullable
    public Group getGroupOnline(@NonNull String groupId) {
        final ParseQuery<Group> query = ParseQuery.getQuery(Group.CLASS);
        try {
            return query.get(groupId);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public Single<Group> subscribeGroup(@NonNull final Group group) {
        return Single.create(new Single.OnSubscribe<Group>() {
            @Override
            public void call(final SingleSubscriber<? super Group> singleSubscriber) {
                ParsePush.subscribeInBackground(group.getObjectId(), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(group);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void unSubscribeGroup(@NonNull Group group) {
        ParsePush.unsubscribeInBackground(group.getObjectId());
    }
}
