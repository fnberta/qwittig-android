/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface IdentityRepository extends BaseRepository {

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

    String getInvitationUrl(Identity identity, @NonNull String groupName);

    /**
     * Fetches the data of a {@link Identity} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param identity the identity to fetch the data for
     */
    Observable<Identity> fetchIdentityDataAsync(@NonNull Identity identity);

    Observable<Identity> fetchIdentitiesDataAsync(@NonNull List<Identity> identities);

    /**
     * Queries the local data store for identities.
     *
     * @param group the group for which to get identities for
     * @param includePending
     */
    Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group, boolean includePending);

    /**
     * Updates all users in the local data store by deleting all identities from the local data
     * store, querying and saving new ones.
     *
     * @param identities the groups for which to update the identities
     */
    Observable<Identity> updateIdentitiesAsync(@NonNull List<Identity> identities);

    /**
     * Deletes all identities from the local data store and saves new ones.
     *
     * @param identities the groups for which to update the identities
     * @return whether the update was successful or not
     */
    boolean updateIdentities(@NonNull List<Identity> identities);

    Observable<Identity> saveIdentitiesWithAvatar(@NonNull List<Identity> identities,
                                                  @NonNull String nickname,
                                                  @NonNull byte[] avatarBytes);
}
