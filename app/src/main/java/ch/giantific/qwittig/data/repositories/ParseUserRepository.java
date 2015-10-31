/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseUserRepository extends ParseGenericRepository implements UserRepository {

    private static final String LOG_TAG = ParseUserRepository.class.getSimpleName();

    public ParseUserRepository() {
        super();
    }

    @Override
    public void getUsersLocalAsync(@NonNull Group group,
                                   @NonNull final GetUsersLocalListener listener) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo(User.GROUPS, group);
        query.whereEqualTo(User.IS_DELETED, false);
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, @Nullable ParseException e) {
                if (e == null) {
                    listener.onUsersLocalLoaded(parseUsers);
                }
            }
        });
    }

    @Override
    public void updateUsersAsync(@NonNull List<ParseObject> groups,
                                 @NonNull final UpdateUsersListener listener) {
        ParseQuery<ParseUser> query = getUsersOnlineQuery(groups);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(@NonNull final List<ParseUser> userList, @Nullable ParseException e) {
                if (e != null) {
                    listener.onUserUpdateFailed(e.getCode());
                    return;
                }

                ParseObject.unpinAllInBackground(User.PIN_LABEL, new DeleteCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            listener.onUserUpdateFailed(e.getCode());
                            return;
                        }

                        ParseObject.pinAllInBackground(User.PIN_LABEL, userList, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e != null) {
                                    listener.onUserUpdateFailed(e.getCode());
                                    return;
                                }

                                listener.onUsersUpdated();
                            }
                        });
                    }
                });
            }
        });
    }

    @NonNull
    private ParseQuery<ParseUser> getUsersOnlineQuery(@NonNull List<ParseObject> groups) {
        ParseQuery<ParseUser> query = User.getQuery();
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
