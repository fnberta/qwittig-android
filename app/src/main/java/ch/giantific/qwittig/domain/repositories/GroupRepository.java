/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import ch.giantific.qwittig.domain.models.parse.Group;

/**
 * Provides the methods to get, update and remove groups from the local and online data store.
 */
public interface GroupRepository {
    /**
     * Fetches the data of a {@link Group} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *  @param group    the group to fetch the data for
     * @param listener the callback called when the query finishes
     */
    void fetchGroupDataAsync(@NonNull ParseObject group,
                             @NonNull GetGroupLocalListener listener);

    /**
     * Queries a group from the online data store.
     *
     * @param groupId  the object id of the group to get
     * @param listener the callback when the group is queried
     */
    void getGroupOnlineAsync(@NonNull String groupId,
                             @NonNull GetGroupOnlineListener listener);

    /**
     * Returns a {@link Group} object queried from the online data store or null if the query does
     * not return any objects.
     *
     * @param groupId the object id of the group to query
     * @return a {@link Group} object queried from the online data store
     */
    @Nullable
    Group getGroupOnline(@NonNull String groupId);

    /**
     * Defines the callback when a group is loaded from the local data store.
     */
    interface GetGroupLocalListener {
        /**
         * Called when a local group was successfully loaded.
         *
         * @param group the loaded group
         */
        void onGroupLocalLoaded(@NonNull Group group);
    }

    /**
     * Defines the callback when a group is loaded from the online data store.
     */
    interface GetGroupOnlineListener {
        /**
         * Called when online group was successfully loaded.
         *
         * @param group the loaded group
         */
        void onGroupOnlineLoaded(@NonNull Group group);

        /**
         * Called when the group load failed.
         *
         * @param errorCode the error code of the exception thrown in the process
         */
        void onGroupOnlineLoadFailed(int errorCode);
    }
}
