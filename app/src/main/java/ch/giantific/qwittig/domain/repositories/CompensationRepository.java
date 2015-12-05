/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides the methods to get, update and remove compensations from the local and online data
 * store.
 */
public interface CompensationRepository {
    /**
     * Queries the local data store for unpaid compensations.
     *
     * @param group    the group for which to get compensations for
     * @param listener the callback when the query finishes
     */
    void getCompensationsLocalUnpaidAsync(@NonNull Group group,
                                          @NonNull GetCompensationsLocalListener listener);

    /**
     * Queries the local data store for paid compensations where the current user is either the
     * buyer or the beneficiary.
     *
     * @param currentUser the current user
     * @param group       the group for which to get compensations for
     * @param listener    the callback when the query finishes
     */
    void getCompensationsLocalPaidAsync(@NonNull User currentUser, @NonNull Group group,
                                        @NonNull GetCompensationsLocalListener listener);

    /**
     * Removes a compensation from the local data store.
     *
     * @param compensationId the object id of the compensation to remove
     * @return whether the removal was successful or not
     */
    boolean removeCompensationLocal(@NonNull String compensationId);

    /**
     * Updates all unpaid compensations in the local data store by deleting all compensations from the
     * local data store, querying and saving new ones.
     *
     * @param groups   the group for which to update the compensations
     * @param listener the callback when a query finishes, fails or all queries are finished
     */
    void updateCompensationsUnpaidAsync(@NonNull List<ParseObject> groups,
                                        @NonNull UpdateCompensationsListener listener);

    /**
     * Updates all paid compensations in the local data store by deleting all compensations from the
     * local data store, querying and saving new ones.
     *
     * @param groups         the group for which to update the compensations
     * @param currentGroupId the object id of the user's current group
     * @param listener       the callback when a query finishes, fails or all queries are finished
     */
    void updateCompensationsPaidAsync(@NonNull List<ParseObject> groups,
                                      @NonNull String currentGroupId,
                                      @NonNull UpdateCompensationsListener listener);

    /**
     * Queries paid compensations from the online data store and saves them in the local data store.
     *
     * @param group    the group for which to get compensations for
     * @param skip     the number of compensations to skip for the query
     * @param listener the callback when the new compensations are saved in the local data store
     */
    void getCompensationsPaidOnlineAsync(@NonNull Group group, int skip,
                                         @NonNull GetCompensationsOnlineListener listener);

    /**
     * Deletes all compensations from the local data store and saves new ones.
     *
     * @param groups the groups for which to update the compensations
     * @return whether the update was successful or not
     */
    boolean updateCompensations(@NonNull List<ParseObject> groups);

    /**
     * Updates a compensation if is already available in the local data store (by simply querying
     * it) or saves it for the first time to the local data store if not.
     *
     * @param compensationId the object id of the compensations to query
     * @param isNew          whether the compensations is already available in the local data store
     *                       or not
     * @return whether the update was successful or not
     */
    @Nullable
    Boolean updateCompensation(@NonNull String compensationId, boolean isNew);

    /**
     * Defines the callback when compensations are loaded from the local data store.
     */
    interface GetCompensationsLocalListener {
        /**
         * Called when local compensations were successfully loaded.
         *
         * @param compensations the loaded compensations
         */
        void onCompensationsLocalLoaded(@NonNull List<ParseObject> compensations);
    }

    /**
     * Defines the callback when compensations are loaded from the online data store.
     */
    interface GetCompensationsOnlineListener {
        /**
         * Called when online paid compensations were successfully loaded.
         *
         * @param compensations the loaded compensations
         */
        void onCompensationsPaidOnlineLoaded(@NonNull List<ParseObject> compensations);

        /**
         * Called when the compensations load failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onCompensationsPaidOnlineLoadFailed(@StringRes int errorMessage);
    }

    /**
     * Defines the callback when compensations in the local data store are updated from the online
     * data store.
     */
    interface UpdateCompensationsListener {
        /**
         * Called when local paid compensations were successfully updated.
         *
         * @param isCurrentGroup whether the updated compensations belong to the current group
         */
        void onCompensationsPaidUpdated(boolean isCurrentGroup);

        /**
         * Called when all paid compensations from all group were successfully updated.
         */
        void onAllCompensationsPaidUpdated();

        /**
         * Called when local unpaid compensations were successfully updated.
         */
        void onCompensationsUnpaidUpdated();

        /**
         * Called when compensations update failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onCompensationUpdateFailed(@StringRes int errorMessage);
    }
}
