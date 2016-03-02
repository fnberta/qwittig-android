/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.models.Group;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove groups from the local and online data store.
 */
public interface GroupRepository extends BaseRepository {

    /**
     * Adds a new group and corresponding identity for the user.
     *
     * @param groupName     the name of the new group
     * @param groupCurrency the currency of the new group
     * @return a {@link Single} emitting the result
     */
    Single<String> addNewGroup(@NonNull String groupName, @NonNull String groupCurrency);

    /**
     * Fetches the data of a {@link Group} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param group the group to fetch the data for
     * @return a {@link Observable} emitting the results
     */
    Observable<Group> fetchGroupDataAsync(@NonNull Group group);

    /**
     * Queries a group from the online data store.
     *
     * @param groupId the object id of the group to get
     * @return a {@link Single} emitting the result
     */
    Single<Group> getGroupOnlineAsync(@NonNull String groupId);

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
     * Subscribes the user to push messages for the group
     *
     * @param group the group to subscribe
     * @return a {@link Single} emitting the result
     */
    Single<Group> subscribeGroup(@NonNull Group group);

    /**
     * Un-subscribes the user for push message from the group
     *
     * @param group the group to un-subscribe from
     */
    void unSubscribeGroup(@NonNull Group group);
}
