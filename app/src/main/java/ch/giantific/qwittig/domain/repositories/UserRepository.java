/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface UserRepository extends Repository {

    @Nullable
    User getCurrentUser();

    Single<String> requestPasswordReset(@NonNull String email);

    Single<User> loginEmail(@NonNull String username, @NonNull String password);

    Single<User> signUpEmail(@NonNull String username, @NonNull String password);

    Single<User> loginFacebook();

    Single<User> loginGoogle();

    Single<User> logOut(@NonNull User user);

    Observable<User> saveUserAsync(@NonNull User user);

    Single<String> getUserSessionToken();

    /**
     * Queries the local data store for users.
     *
     * @param group the group for which to get users for
     */
    Observable<User> getUsersLocalAsync(@NonNull Group group);

    /**
     * Updates all users in the local data store by deleting all users from the local data
     * store, querying and saving new ones.
     *
     * @param groups the groups for which to update the users
     */
    Observable<User> updateUsersAsync(@NonNull List<ParseObject> groups);

    /**
     * Deletes all users from the local data store and saves new ones.
     *
     * @param groups the groups for which to update the purchases
     * @return whether the update was successful or not
     */
    boolean updateUsers(@NonNull List<ParseObject> groups);
}
