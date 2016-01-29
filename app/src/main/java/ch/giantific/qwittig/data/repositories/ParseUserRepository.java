/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseUserRepository extends ParseBaseRepository<ParseUser> implements UserRepository {

    private static final String LOG_TAG = ParseUserRepository.class.getSimpleName();

    @Inject
    public ParseUserRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return User.CLASS;
    }

    @Nullable
    @Override
    public User getCurrentUser() {
        return (User) ParseUser.getCurrentUser();
    }

    @Override
    public Single<User> logOut(@NonNull final User user) {
        return Single.create(new Single.OnSubscribe<User>() {
            @Override
            public void call(final SingleSubscriber<? super User> singleSubscriber) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(user);
                        }
                    }
                });
            }
        });
    }

    @Override
    public Observable<User> saveUserAsync(@NonNull User user) {
        return save(user).toObservable().cast(User.class);
    }

    @Override
    public Single<String> getUserSessionToken() {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(final SingleSubscriber<? super String> singleSubscriber) {
                ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
                    @Override
                    public void done(@NonNull ParseSession parseSession, @Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(parseSession.getSessionToken());
                        }
                    }
                });
            }
        });
    }

    @Override
    public Observable<User> getUsersLocalAsync(@NonNull Group group) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo(User.GROUPS, group);
        query.whereEqualTo(User.IS_DELETED, false);
        query.ignoreACLs();

        return find(query)
                .flatMap(new Func1<List<ParseUser>, Observable<ParseUser>>() {
                    @Override
                    public Observable<ParseUser> call(List<ParseUser> parseUsers) {
                        return Observable.from(parseUsers);
                    }
                })
                .cast(User.class);
    }

    @Override
    public Observable<User> updateUsersAsync(@NonNull List<ParseObject> groups) {
        ParseQuery<ParseUser> query = getUsersOnlineQuery(groups);

        return find(query)
                .flatMap(new Func1<List<ParseUser>, Observable<List<ParseUser>>>() {
                    @Override
                    public Observable<List<ParseUser>> call(List<ParseUser> parseUsers) {
                        return unpinAll(parseUsers, User.PIN_LABEL);
                    }
                })
                .flatMap(new Func1<List<ParseUser>, Observable<List<ParseUser>>>() {
                    @Override
                    public Observable<List<ParseUser>> call(List<ParseUser> parseUsers) {
                        return pinAll(parseUsers, User.PIN_LABEL);
                    }
                })
                .flatMap(new Func1<List<ParseUser>, Observable<ParseUser>>() {
                    @Override
                    public Observable<ParseUser> call(List<ParseUser> parseUsers) {
                        return Observable.from(parseUsers);
                    }
                })
                .cast(User.class);
    }

    @NonNull
    private ParseQuery<ParseUser> getUsersOnlineQuery(@NonNull List<ParseObject> groups) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn(User.GROUPS, groups);
        query.whereEqualTo(User.IS_DELETED, false);
        return query;
    }

    @Override
    public boolean updateUsers(@NonNull List<ParseObject> groups) {
        try {
            List<ParseUser> onlineUsers = getUsersOnline(groups);
            ParseObject.unpinAll(User.PIN_LABEL);
            ParseObject.pinAll(User.PIN_LABEL, onlineUsers);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private List<ParseUser> getUsersOnline(@NonNull List<ParseObject> groups) throws ParseException {
        ParseQuery<ParseUser> query = getUsersOnlineQuery(groups);
        return query.find();
    }

}
