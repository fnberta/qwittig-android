/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseFile;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface IdentityRepository extends Repository {

    String INVITATION_LINK = "https://qwittig.ch/invitation/";

    /**
     * Re-calculates the balances all users of the current user's groups.
     */
    Single<String> calcUserBalances();

    /**
     * Creates a new identity and generates an invitation link for it. Allows the user to interact
     * with the identity even if no one has yet accepted the invitation.
     *
     * @param nickname  the nickname to use in the new identity
     * @param groupId   the object id of the group
     * @param groupName the name of the group the new identity is created for, user for the link
     */
    Single<String> addIdentity(@NonNull String nickname,
                               @NonNull String groupId,
                               @NonNull String groupName);

    /**
     * Fetches all identities of a user form the local datastore.
     *
     * @param user the user to fetch the identities for
     * @return all identities of a user
     */
    Observable<Identity> getUserIdentitiesLocalAsync(@NonNull User user);

    List<Identity> getUserIdentitiesLocal(@NonNull User user);

    /**
     * Fetches the data of a {@link Identity} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param identity the identity to fetch the data for
     */
    Observable<Identity> fetchIdentityDataAsync(@NonNull Identity identity);

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
     * @param user the groups for which to update the identities
     */
    Observable<Identity> updateIdentitiesAsync(@NonNull User user);

    /**
     * Deletes all identities from the local data store and saves new ones.
     *
     * @param user the groups for which to update the identities
     * @return whether the update was successful or not
     */
    boolean updateIdentities(@NonNull User user);

    Single<Identity> saveIdentityLocalAsync(@NonNull Identity identity);
}
