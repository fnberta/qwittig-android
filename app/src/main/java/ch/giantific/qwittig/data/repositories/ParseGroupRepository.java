/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link GroupRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseGroupRepository extends ParseBaseRepository<Group> implements GroupRepository {

    public ParseGroupRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Group.CLASS;
    }

    @Override
    public Single<Group> fetchGroupDataAsync(@NonNull final Group group) {
        if (group.isDataAvailable()) {
            return Single.just(group);
        }

        return fetchLocal(group)
                .toObservable()
                .onErrorResumeNext(fetchIfNeeded(group).toObservable())
                .toSingle();
    }

    @Override
    public Observable<Group> fetchGroupsDataAsync(@NonNull List<ParseObject> identities) {
        return Observable.from(identities)
                .cast(Identity.class)
                .map(new Func1<Identity, Group>() {
                    @Override
                    public Group call(Identity identity) {
                        return identity.getGroup();
                    }
                })
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
