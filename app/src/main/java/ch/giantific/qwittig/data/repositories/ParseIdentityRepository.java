/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseIdentityRepository extends ParseBaseRepository implements IdentityRepository {

    private static final String CALCULATE_BALANCE = "calculateBalance";

    public ParseIdentityRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Identity.CLASS;
    }

    @Override
    public Single<String> calcUserBalances() {
        return callFunctionInBackground(CALCULATE_BALANCE, Collections.<String, Object>emptyMap());
    }

    @Override
    public Single<String> addIdentity(@NonNull String nickname, @NonNull String groupId,
                                      @NonNull final String groupName) {
        final Group group = (Group) ParseObject.createWithoutData(Group.CLASS, groupId);
        final Identity identity = new Identity(group, nickname);
        return save(identity)
                .map(new Func1<Identity, String>() {
                    @Override
                    public String call(Identity identity) {
                        return INVITATION_LINK + "?id=" + identity.getObjectId() + "&group=" + groupName;
                    }
                });
    }

    @Override
    public Observable<Identity> getUserIdentitiesLocalAsync(@NonNull User user) {
        final ParseQuery<Identity> query = getUserIdentitiesLocalQuery(user);
        return find(query)
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @NonNull
    private ParseQuery<Identity> getUserIdentitiesLocalQuery(@NonNull User user) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Identity.USER, user);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return query;
    }

    @Override
    public List<Identity> getUserIdentitiesLocal(@NonNull User user) {
        try {
            final ParseQuery<Identity> query = getUserIdentitiesLocalQuery(user);
            return query.find();
        } catch (ParseException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Observable<Identity> fetchIdentityDataAsync(@NonNull final Identity identity) {
        if (identity.isDataAvailable() && identity.getGroup().isDataAvailable()) {
            return Observable.just(identity);
        }

        return fetchLocal(identity)
                .toObservable()
                .onErrorResumeNext(fetchIfNeeded(identity).toObservable())
                .flatMap(new Func1<Identity, Observable<? extends Group>>() {
                    @Override
                    public Observable<? extends Group> call(Identity identity) {
                        final Group group = identity.getGroup();
                        return fetchLocal(group)
                                .toObservable()
                                .onErrorResumeNext(fetchIfNeeded(group).toObservable());
                    }
                })
                .map(new Func1<Group, Identity>() {
                    @Override
                    public Identity call(Group group) {
                        return identity;
                    }
                });
    }

    @Override
    public Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Identity.GROUP, group);
        query.whereEqualTo(Identity.ACTIVE, true);
        return find(query)
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @Override
    public Observable<Identity> updateIdentitiesAsync(@NonNull User user) {
        return Observable.just(user)
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return getUserIdentitiesAsync(user);
                    }
                })
                .map(new Func1<Identity, Group>() {
                    @Override
                    public Group call(Identity identity) {
                        return identity.getGroup();
                    }
                })
                .toList()
                .map(new Func1<List<Group>, ParseQuery<Identity>>() {
                    @Override
                    public ParseQuery<Identity> call(List<Group> groups) {
                        return getIdentitiesOnlineQuery(groups);
                    }
                })
                .flatMap(new Func1<ParseQuery<Identity>, Observable<List<Identity>>>() {
                    @Override
                    public Observable<List<Identity>> call(ParseQuery<Identity> identityParseQuery) {
                        return find(identityParseQuery);
                    }
                })
                .flatMap(new Func1<List<Identity>, Observable<List<Identity>>>() {
                    @Override
                    public Observable<List<Identity>> call(List<Identity> identities) {
                        return unpinAll(identities, Identity.PIN_LABEL);
                    }
                })
                .flatMap(new Func1<List<Identity>, Observable<List<Identity>>>() {
                    @Override
                    public Observable<List<Identity>> call(List<Identity> identities) {
                        return pinAll(identities, Identity.PIN_LABEL);
                    }
                })
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    private Observable<Identity> getUserIdentitiesAsync(@NonNull User user) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereEqualTo(Identity.USER, user);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return find(query)
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @NonNull
    private ParseQuery<Identity> getIdentitiesOnlineQuery(@NonNull List<Group> groups) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereContainedIn(Identity.GROUP, groups);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return query;
    }

    @Override
    public boolean updateIdentities(@NonNull User user) {
        try {
            final List<Identity> identities = getUserIdentities(user);
            final List<Group> groups = new ArrayList<>();
            for (Identity identity : identities) {
                groups.add(identity.getGroup());
            }

            final ParseQuery<Identity> query = getIdentitiesOnlineQuery(groups);
            final List<Identity> onlineUsers = query.find();

            ParseObject.unpinAll(Identity.PIN_LABEL);
            ParseObject.pinAll(Identity.PIN_LABEL, onlineUsers);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private List<Identity> getUserIdentities(@NonNull User user) throws ParseException {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereEqualTo(Identity.USER, user);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return query.find();
    }

    @Override
    public Single<Identity> saveIdentityLocalAsync(@NonNull Identity identity) {
        return pin(identity, Identity.PIN_LABEL);
    }
}
