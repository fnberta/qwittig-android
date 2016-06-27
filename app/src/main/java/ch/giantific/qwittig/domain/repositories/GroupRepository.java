/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.models.Group;

/**
 * Provides the methods to get, update and remove groups from the local and online data store.
 */
public interface GroupRepository extends BaseRepository {

    /**
     * Returns a {@link Group} object queried from the online data store or null if the query does
     * not return any objects.
     *
     * @param groupId the object id of the group to query
     * @return a {@link Group} object queried from the online data store
     */
    @Nullable
    Group queryGroup(@NonNull String groupId);
}
