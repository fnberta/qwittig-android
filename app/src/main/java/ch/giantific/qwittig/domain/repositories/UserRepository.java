/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.parse.ParseInstallation;

import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface UserRepository extends BaseRepository {

    /**
     * The base url for the invitation link.
     */
    String FILE_NAME = "avatar.jpg";
    int JPEG_COMPRESSION_RATE = 60;
    int HEIGHT = 720;
    int WIDTH = 720;

    /**
     * Returns the currently logged in user, null if no one is logged in.
     *
     * @return the currently logged in user
     */
    @Nullable
    User getCurrentUser();

    /**
     * Updates the locally cached version of the user with fresh data from the online store.
     *
     * @param user the user to update
     * @return a {@link Single} emitting the result
     */
    Single<User> updateUser(@NonNull User user);

    /**
     * Returns the session token for current user session.
     *
     * @return a {@link Single} emitting the result
     */
    Single<String> getUserSessionToken();

    /**
     * Logs the user in using his email address and password.
     *
     * @param username   the email address for the login
     * @param password   the pass word for the login
     * @param identityId the identity id the user is invited to, empty if no invite pending
     * @return a {@link Single} emitting the result
     */
    Single<User> loginEmail(@NonNull String username, @NonNull String password, @NonNull String identityId);

    /**
     * Sends the user an email with a link to reset his password
     *
     * @param email the email address to send the link to
     * @return a {@link Single} emitting the result
     */
    Single<String> requestPasswordReset(@NonNull String email);

    /**
     * Signs up and logs in a new user using his email address as username and a password.
     *
     * @param username   the user's email to use as username
     * @param password   the user's password
     * @param identityId the identity id the user is invited to, empty if no invite pending
     * @return a {@link Single} emitting the result
     */
    Single<User> signUpEmail(@NonNull String username, @NonNull String password,
                             @NonNull String identityId);

    /**
     * Logs in a user using his facebook account
     *
     * @param activity   the fragment that is initiating the login
     * @param identityId the identity id the user is invited to, empty if no invite pending
     * @return a {@link Single} emitting the result
     */
    Single<User> loginFacebook(@NonNull FragmentActivity activity, @NonNull String identityId);

    /**
     * Logs in the user using his google account.
     *
     * @param idToken    the google id token
     * @param identityId the identity id the user is invited to, empty if no invite pending
     * @return a {@link Single} emitting the result
     */
    Single<User> loginGoogle(@NonNull String idToken,
                             @NonNull final String username,
                             @NonNull final Uri photoUrl,
                             @NonNull String identityId, @NonNull final Fragment fragment);

    /**
     * Handles an invitation link by calling a function in the cloud.
     *
     * @param user       the user checking the invitation
     * @param identityId the object id of the identity the user is invited to
     * @return a {@link Single} emitting the result
     */
    Single<Identity> handleInvitation(@NonNull User user, @NonNull String identityId);

    /**
     * Logs out the user.
     *
     * @param user       the user to log out
     * @param deleteUser whether to delete the user after logout
     * @return a {@link Single} emitting the result
     */
    Single<User> logoutUser(@NonNull User user, boolean deleteUser);

    /**
     * Un-links the user's account from his facebook profile.
     *
     * @param user       the user to unlink
     * @param deleteUser whether to delete the user after un-linking
     * @return a {@link Single} emitting the result
     */
    Single<User> unlinkFacebook(@NonNull User user, boolean deleteUser);

    /**
     * Signs out the currently logged in user from his google profile.
     *
     * @param context the context to user for the operation
     * @param user    the user to unlink
     * @return a {@link Single} emitting the result
     */
    Single<User> signOutGoogle(@NonNull Context context, @NonNull User user);

    /**
     * Unlinks the user's account from his google profile.
     *
     * @param user       the user to unlink
     * @param deleteUser whether to delete the user after un-linking
     * @return a {@link Single} emitting the result
     */
    Single<User> unlinkGoogle(@NonNull Context context, @NonNull User user, boolean deleteUser);

    /**
     * Sets up the installation object after login by subscribing to all group channels and setting
     * the user field.
     *
     * @param user the user that just logged in
     * @return an {@link Observable} emitting the results
     */
    Single<User> setupInstallation(@NonNull User user);

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

    /**
     * Clears the installation object by resetting channels and the user field
     *
     * @return a {@link Single} emitting the result
     */
    Single<ParseInstallation> clearInstallation();

    /**
     * Re-calculates the balances all users of the current user's groups.
     *
     * @return a {@link Single} emitting the result
     */
    Single<String> calcUserBalances();

    /**
     * Adds a new group and corresponding identity for the user.
     *
     * @param user          the user to add the group to
     * @param groupName     the name of the new group
     * @param groupCurrency the currency of the new group
     * @return a {@link Single} emitting the result
     */
    Single<Identity> addNewGroup(@NonNull User user, @NonNull String groupName,
                                 @NonNull String groupCurrency);

    /**
     * Creates a new identity and generates an invitation link for it. Allows the user to interact
     * with the identity even if no one has yet accepted the invitation.
     *
     * @param context
     * @param nickname        the nickname to use in the new identity
     * @param groupId         the object id of the group
     * @param groupName       the name of the group the new identity is created for, user for the link
     * @param inviterNickname
     * @return a {@link Single} emitting the result
     */
    Single<Identity> addIdentity(@NonNull Context context, @NonNull String nickname,
                                 @NonNull String groupId,
                                 @NonNull String groupName, @NonNull String inviterNickname);

    /**
     * Saves an identity to the local data store.
     *
     * @param identity the identity to save
     * @return a {@link Single} emitting the result
     */
    Single<Identity> saveIdentityLocal(@NonNull Identity identity);

    /**
     * Returns the invitation url for the invited identity
     *
     * @param context
     * @param identity        the identity that is invited
     * @param groupName       the name of the group
     * @param inviterNickname
     * @return the invitation url
     */
    Single<String> getInvitationUrl(@NonNull Context context, @NonNull Identity identity,
                                    @NonNull String groupName, @NonNull String inviterNickname);

    /**
     * Fetches the data of a {@link Identity} object from the local data store. If there is no data
     * available in the local data store it will try to fetch the data online.
     *
     * @param identity the identity to fetch the data for
     * @return a {@link Observable} emitting the results
     */
    Single<Identity> fetchIdentityData(@NonNull Identity identity);

    /**
     * Fetches the data of multiple {@link Identity} objects from the local data store.If there is
     * no data available in the local data store it will try to fetch the data online.
     *
     * @param identities the identities to fetch data for
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> fetchIdentitiesData(@NonNull List<Identity> identities);

    /**
     * Queries the local data store for identities.
     *
     * @param group          the group for which to get identities for
     * @param includePending whether to include pending identities
     * @return a {@link Observable} emitting the results
     */
    Observable<Identity> getIdentities(@NonNull Group group, boolean includePending);

    /**
     * Deletes all identities from the local data store and saves new ones.
     *
     * @param identities the groups for which to update the identities
     * @return whether the update was successful or not
     */
    boolean updateIdentities(@NonNull List<Identity> identities);

    Observable<Identity> saveCurrentUserIdentitiesWithAvatar(@Nullable String newNickname,
                                                             @NonNull String localAvatarPath);

    boolean uploadIdentities(@NonNull Context context, @NonNull List<Identity> identities);

    Single<Identity> saveIdentityWithAvatar(@NonNull Identity identity,
                                            @Nullable String newNickname,
                                            @NonNull String localAvatarPath);

    boolean uploadIdentityId(@NonNull Context context, @NonNull String identityId);

    Single<Identity> removePendingIdentity(@NonNull Identity identity);
}
