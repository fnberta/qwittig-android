/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove identities from the local and online data store.
 */
public interface IdentityRepository extends BaseRepository {

    /**
     * The base url for the invitation link.
     */
    String INVITATION_LINK = "https://qwittig.ch/invitation/";
    String FILE_NAME = "avatar.jpg";
    int JPEG_COMPRESSION_RATE = 60;
    int HEIGHT = 720;
    int WIDTH = 720;

    /**
     * Re-calculates the balances all users of the current user's groups.
     *
     * @return a {@link Single} emitting the result
     */
    Single<String> calcUserBalances();

    /**
     * Creates a new identity and generates an invitation link for it. Allows the user to interact
     * with the identity even if no one has yet accepted the invitation.
     *
     * @param nickname  the nickname to use in the new identity
     * @param groupId   the object id of the group
     * @param groupName the name of the group the new identity is created for, user for the link
     * @return a {@link Single} emitting the result
     */
    Single<String> addIdentity(@NonNull String nickname,
                               @NonNull String groupId,
                               @NonNull String groupName);

    /**
     * Returns the invitation url for the invited identity
     *
     * @param identity  the identity that is invited
     * @param groupName the name of the group
     * @return the invitation url
     */
    String getInvitationUrl(Identity identity, @NonNull String groupName);

    /**
     * Fetches the data of a {@link Identity} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param identity the identity to fetch the data for
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> fetchIdentityDataAsync(@NonNull Identity identity);

    /**
     * Fetches the data of multiple {@link Identity} objects from the local data store.If there is
     * no data available in the local data store it will try to fetch the data online.
     *
     * @param identities the identities to fetch data for
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> fetchIdentitiesDataAsync(@NonNull List<Identity> identities);

    /**
     * Queries the local data store for identities.
     *
     * @param group          the group for which to get identities for
     * @param includePending whether to include pending identities
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> getIdentitiesLocalAsync(@NonNull Group group, boolean includePending);

    /**
     * Updates all users in the local data store by deleting all identities from the local data
     * store, querying and saving new ones.
     *
     * @param identities the groups for which to update the identities
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> updateIdentitiesAsync(@NonNull List<Identity> identities);

    /**
     * Deletes all identities from the local data store and saves new ones.
     *
     * @param identities the groups for which to update the identities
     * @return whether the update was successful or not
     */
    boolean updateIdentities(@NonNull List<Identity> identities);

    /**
     * Saves the identities with a nickname and avatar.
     *
     * @param identities  the identities to save
     * @param nickname    the nickname
     * @param avatarBytes the avatar
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> saveIdentitiesWithAvatar(@NonNull List<Identity> identities,
                                                  @NonNull String nickname,
                                                  @NonNull byte[] avatarBytes);
}
