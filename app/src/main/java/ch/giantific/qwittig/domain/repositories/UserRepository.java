/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface UserRepository {
    /**
     * Queries the local data store for users.
     *
     * @param group     the group for which to get users for
     * @param listener  the callback when the query finishes
     */
    void getUsersLocalAsync(@NonNull Group group,
                            @NonNull GetUsersLocalListener listener);

    /**
     * Updates all users in the local data store by deleting all users from the local data
     * store, querying and saving new ones.
     *
     * @param groups   the groups for which to update the users
     * @param listener the callback when a query finishes, fails or all queries are finished
     */
    void updateUsersAsync(@NonNull List<ParseObject> groups,
                          @NonNull UpdateUsersListener listener);

    /**
     * Deletes all users from the local data store and saves new ones.
     *
     * @param groups the groups for which to update the purchases
     * @return whether the update was successful or not
     */
    boolean updateUsers(@NonNull List<ParseObject> groups);

    /**
     * Defines the callback when users are loaded from the local data store.
     */
    interface GetUsersLocalListener {
        /**
         * Called when local users were successfully loaded.
         *
         * @param users the loaded users
         */
        void onUsersLocalLoaded(@NonNull List<ParseUser> users);
    }

    /**
     * Defines the callback when users in the local data store are updated from the online data
     * store including the calculation of their balances.
     */
    interface UpdateUsersListener {
        /**
         * Called when local users were successfully updated.
         */
        void onUsersUpdated();

        /**
         * Called when users update failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onUserUpdateFailed(@StringRes int errorMessage);
    }
}
