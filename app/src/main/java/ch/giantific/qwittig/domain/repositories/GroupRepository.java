/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove groups from the local and online data store.
 */
public interface GroupRepository extends Repository {
    /**
     * Fetches the data of a {@link Group} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param group the group to fetch the data for
     */
    Single<Group> fetchGroupDataAsync(@NonNull Group group);

    /**
     * Fetches the data of multiple {@link Group} objects from the local data store. If there is no
     * data  available in the local data store it will try to fetch the data online.
     *
     * @param groups the groups to fetch the data for
     */
    Observable<Group> fetchGroupsDataAsync(@NonNull List<ParseObject> groups);

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
}
