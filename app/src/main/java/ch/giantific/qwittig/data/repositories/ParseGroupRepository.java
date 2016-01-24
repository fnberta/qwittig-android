/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link GroupRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseGroupRepository extends ParseBaseRepository<Group> implements GroupRepository {

    @Inject
    public ParseGroupRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Group.CLASS;
    }

    @Override
    public Single<Group> fetchGroupDataAsync(@NonNull final Group group) {
        return Single.create(new Single.OnSubscribe<Group>() {
            @Override
            public void call(final SingleSubscriber<? super Group> singleSubscriber) {
                if (group.isDataAvailable()) {
                    if (!singleSubscriber.isUnsubscribed()) {
                        singleSubscriber.onSuccess(group);
                    }

                    return;
                }

                group.fetchFromLocalDatastoreInBackground(new GetCallback<Group>() {
                    @Override
                    public void done(Group group, @Nullable ParseException e) {
                        if (e == null) {
                            if (!singleSubscriber.isUnsubscribed()) {
                                singleSubscriber.onSuccess(group);
                            }
                        } else {
                            group.fetchIfNeededInBackground(new GetCallback<Group>() {
                                @Override
                                public void done(Group group, @Nullable ParseException e) {
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
                    }
                });
            }
        });
    }

    @Override
    public Observable<Group> fetchGroupsDataAsync(@NonNull List<ParseObject> groups) {
        return Observable.from(groups)
                .cast(Group.class)
                .flatMap(new Func1<Group, Observable<Group>>() {
                    @Override
                    public Observable<Group> call(Group parseObject) {
                        return fetchGroupDataAsync(parseObject).toObservable();
                    }
                });
    }

    @Override
    public Single<Group> getGroupOnlineAsync(@NonNull final String groupId) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.CLASS);
        return get(query, groupId);
    }

    @Override
    @Nullable
    public Group getGroupOnline(@NonNull String groupId) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.CLASS);
        try {
            return query.get(groupId);
        } catch (ParseException e) {
            return null;
        }
    }

}
