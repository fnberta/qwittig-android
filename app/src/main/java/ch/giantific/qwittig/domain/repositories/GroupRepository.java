/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove groups from the local and online data store.
 */
public interface GroupRepository extends Repository {

    Single<String> addNewGroup(@NonNull String groupName, @NonNull String groupCurrency);

    /**
     * Fetches the data of a {@link Group} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param group the group to fetch the data for
     */
    Observable<Group> fetchGroupDataAsync(@NonNull Group group);

    /**
     * Fetches the data of multiple {@link Group} objects from the local data store. If there is no
     * data  available in the local data store it will try to fetch the data online.
     *
     * @param identities the identities to fetch the group data for
     */
    Observable<Group> fetchGroupsDataAsync(@NonNull List<Identity> identities);

    /**
     * Queries a group from the online data store.
     *
     * @param groupId the object id of the group to get
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

    void unsubscribeGroup(@NonNull Group group);
}
