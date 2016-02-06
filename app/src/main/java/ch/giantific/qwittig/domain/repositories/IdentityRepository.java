/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface IdentityRepository extends Repository {

    /**
     * Fetches the data of a {@link Identity} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param identity the identity to fetch the data for
     */
    Observable<Identity> fetchIdentityDataAsync(@NonNull Identity identity);

    /**
     * Fetches the data of multiple {@link Identity} objects from the local data store. If there is no
     * data available in the local data store it will try to fetch the data online.
     *
     * @param identities the identities to fetch the data for
     */
    Observable<Identity> fetchIdentitiesDataAsync(@NonNull List<ParseObject> identities);

    /**
     * Queries the local data store for identities.
     *
     * @param group the group for which to get identities for
     */
    Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group);

    /**
     * Updates all users in the local data store by deleting all identities from the local data
     * store, querying and saving new ones.
     *
     * @param groups the groups for which to update the identities
     */
    Observable<Identity> updateIdentitiesAsync(@NonNull List<ParseObject> groups);

    /**
     * Deletes all identities from the local data store and saves new ones.
     *
     * @param groups the groups for which to update the identities
     * @return whether the update was successful or not
     */
    boolean updateIdentities(@NonNull List<ParseObject> groups);
}
