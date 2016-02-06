/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseIdentityRepository extends ParseBaseRepository<Identity> implements IdentityRepository {

    public ParseIdentityRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Identity.CLASS;
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
                        return fetchGroupLocal(identity.getGroup());
                    }
                })
                .map(new Func1<Group, Identity>() {
                    @Override
                    public Identity call(Group group) {
                        return identity;
                    }
                });
    }

    private Observable<Group> fetchGroupLocal(@NonNull final Group group) {
        return Observable
                .create(new Observable.OnSubscribe<Group>() {
                    @Override
                    public void call(final Subscriber<? super Group> subscriber) {
                        group.fetchFromLocalDatastoreInBackground(new GetCallback<Group>() {
                            @Override
                            public void done(Group groupFetched, @Nullable ParseException e) {
                                if (subscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (e != null) {
                                    subscriber.onError(e);
                                } else {
                                    subscriber.onNext(groupFetched);
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .onErrorResumeNext(Observable
                        .create(new Observable.OnSubscribe<Group>() {
                            @Override
                            public void call(final Subscriber<? super Group> subscriber) {
                                group.fetchIfNeededInBackground(new GetCallback<Group>() {
                                    @Override
                                    public void done(Group groupFetched, @Nullable ParseException e) {
                                        if (subscriber.isUnsubscribed()) {
                                            return;
                                        }

                                        if (e != null) {
                                            subscriber.onError(e);
                                        } else {
                                            subscriber.onNext(groupFetched);
                                            subscriber.onCompleted();
                                        }
                                    }
                                });
                            }
                        }));
    }

    @Override
    public Observable<Identity> fetchIdentitiesDataAsync(@NonNull List<ParseObject> identities) {
        return Observable.from(identities)
                .cast(Identity.class)
                .flatMap(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return fetchIdentityDataAsync(identity);
                    }
                });
    }

    @Override
    public Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.fromLocalDatastore();
        query.whereEqualTo(Identity.GROUP, group);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.ignoreACLs();

        return find(query)
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @Override
    public Observable<Identity> updateIdentitiesAsync(@NonNull List<ParseObject> groups) {
        final ParseQuery<Identity> query = getIdentitiesOnlineQuery(groups);

        return find(query)
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

    @NonNull
    private ParseQuery<Identity> getIdentitiesOnlineQuery(@NonNull List<ParseObject> groups) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereContainedIn(Identity.GROUP, groups);
        query.whereEqualTo(Identity.ACTIVE, true);
        return query;
    }

    @Override
    public boolean updateIdentities(@NonNull List<ParseObject> groups) {
        try {
            final List<Identity> onlineUsers = getIdentitiesOnline(groups);
            ParseObject.unpinAll(Identity.PIN_LABEL);
            ParseObject.pinAll(Identity.PIN_LABEL, onlineUsers);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private List<Identity> getIdentitiesOnline(@NonNull List<ParseObject> groups) throws ParseException {
        final ParseQuery<Identity> query = getIdentitiesOnlineQuery(groups);
        return query.find();
    }
}
