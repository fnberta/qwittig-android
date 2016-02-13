/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.AvatarUtils;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
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
        final Identity identity = new Identity(group, nickname, true);
        return save(identity)
                .flatMap(new Func1<Identity, Single<? extends Identity>>() {
                    @Override
                    public Single<? extends Identity> call(Identity identity) {
                        return pin(identity, Identity.PIN_LABEL);
                    }
                })
                .map(new Func1<Identity, String>() {
                    @Override
                    public String call(Identity identity) {
                        return getInvitationUrl(identity, groupName);
                    }
                });
    }

    @NonNull
    @Override
    public String getInvitationUrl(Identity identity, @NonNull String groupName) {
        return INVITATION_LINK + "?id=" + identity.getObjectId() + "&group=" + groupName;
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
    public Observable<Identity> fetchUserIdentitiesDataAsync(@NonNull List<Identity> identities) {
        return Observable.from(identities)
                .flatMap(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return fetchIdentityDataAsync(identity);
                    }
                });
    }

    @Override
    public Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group, boolean includePending) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Identity.GROUP, group);
        query.whereEqualTo(Identity.ACTIVE, true);
        if (!includePending) {
            query.whereEqualTo(Identity.PENDING, true);
        }
        return find(query)
                .flatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @Override
    public Observable<Identity> updateIdentitiesAsync(@NonNull List<Identity> identities) {
        return Observable.from(identities)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return identity.isActive();
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

    @NonNull
    private ParseQuery<Identity> getIdentitiesOnlineQuery(@NonNull List<Group> groups) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereContainedIn(Identity.GROUP, groups);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return query;
    }

    @Override
    public boolean updateIdentities(@NonNull List<Identity> identities) {
        try {
            final List<Group> groups = new ArrayList<>();
            for (Identity identity : identities) {
                if (identity.isActive()) {
                    groups.add(identity.getGroup());
                }
            }

            final ParseQuery<Identity> query = getIdentitiesOnlineQuery(groups);
            final List<Identity> onlineIdentities = query.find();

            ParseObject.unpinAll(Identity.PIN_LABEL);
            ParseObject.pinAll(Identity.PIN_LABEL, onlineIdentities);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Observable<Identity> saveIdentitiesWithAvatar(@NonNull final List<Identity> identities,
                                                         @NonNull final String nickname,
                                                         @NonNull byte[] avatarBytes) {
        final ParseFile avatar = new ParseFile(AvatarUtils.FILE_NAME, avatarBytes);
        return saveFile(avatar)
                .flatMapObservable(new Func1<ParseFile, Observable<? extends Identity>>() {
                    @Override
                    public Observable<? extends Identity> call(ParseFile parseFile) {
                        return Observable.from(identities)
                                .filter(new Func1<Identity, Boolean>() {
                                    @Override
                                    public Boolean call(Identity identity) {
                                        return identity.isActive();
                                    }
                                })
                                .doOnNext(new Action1<Identity>() {
                                    @Override
                                    public void call(Identity identity) {
                                        identity.setNickname(nickname);
                                        identity.setAvatar(avatar);
                                        identity.saveEventually();
                                    }
                                });
                    }
                });
    }
}
