/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.parse.ParseInstallation;

import org.json.JSONObject;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface UserRepository extends BaseRepository {

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
     * @param username the email address for the login
     * @param password the pass word for the login
     * @return a {@link Single} emitting the result
     */
    Single<User> loginEmail(@NonNull String username, @NonNull String password);

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
     * @param username the user's email to use as username
     * @param password the user's password
     * @return a {@link Single} emitting the result
     */
    Single<User> signUpEmail(@NonNull String username, @NonNull String password);

    /**
     * Logs in a user using his facebook account
     *
     * @param fragment the fragment that is initiating the login
     * @return a {@link Single} emitting the result
     */
    Single<User> loginFacebook(@NonNull Fragment fragment);

    /**
     * Sets the information from facebook (emaio, nickname and avatar) for the user and its identity.
     *
     * @param user     the user to set the information for
     * @param identity the identity to set the information for
     * @param fragment the fragment to use as context  @return a {@link Single} emitting the result
     */
    Single<User> setFacebookData(@NonNull User user, @NonNull Identity identity,
                                 @NonNull final Fragment fragment);

    /**
     * Logs in the user using his google account.
     *
     * @param idToken the google id token
     * @return a {@link Single} emitting the result
     */
    Single<User> loginGoogle(@NonNull String idToken,
                             @NonNull final Fragment fragment);

    /**
     * Verifies the idToken obtained from Google and if successful logs in the user attached to the
     * email address in the token. If no such user exists yet, creates a new one.
     *
     * @param idToken the token obtained from the Google login
     * @return a {@link Single} emitting the result
     */
    Single<String> verifyGoogleLogin(@NonNull String idToken);

    /**
     * Sets the information from google (email, nickname and avatar) for the user and its identity.
     *
     * @param user        the user to set the information for
     * @param identity    the identity to set the information for
     * @param displayName the nickname from google
     * @param photoUrl    the url to the avatar
     * @param fragment    the fragment to use as context    @return a {@link Single} emitting the result
     */
    Single<User> setGoogleData(@NonNull User user, @NonNull Identity identity,
                               @NonNull String displayName, @NonNull Uri photoUrl,
                               @NonNull final Fragment fragment);

    /**
     * Handles an invitation link by calling a function in the cloud.
     *
     * @param identityId the object id of the identity the user is invited to
     * @return a {@link Single} emitting the result
     */
    Single<String> handleInvitation(@NonNull String identityId);

    /**
     * Logs out the user.
     *
     * @param user the user to log out
     * @return a {@link Single} emitting the result
     */
    Single<User> logOut(@NonNull User user);

    /**
     * Un-links the user's account from his facebook profile.
     *
     * @param user the user to unlink
     * @return a {@link Single} emitting the result
     */
    Single<User> unlinkFacebook(@NonNull User user);

    /**
     * Signs out the currently logged in user from his google profile.
     *
     * @param context the context to user for the operation
     * @return a {@link Single} emitting the result
     */
    Single<Void> signOutGoogle(@NonNull Context context);

    /**
     * Unlinks the user's account from his google profile.
     *
     * @param user the user to unlink
     * @return a {@link Single} emitting the result
     */
    Single<User> unlinkGoogle(@NonNull Context context, @NonNull User user);

    /**
     * Deletes a users account by deleting it. His identities will be kept because they are
     * referenced in other objects (e.g. purchases and items.
     *
     * @return a {@link Single} emitting the result
     */
    Single<User> deleteUser(@NonNull User user);

    /**
     * Sets up the installation object after login by subscribing to all group channels and setting
     * the user field.
     *
     * @param user the user that just logged in
     * @return an {@link Observable} emitting the results
     */
    Single<User> setupInstallation(@NonNull User user);

    /**
     * Clears the installation object by resetting channels and the user field
     *
     * @return a {@link Single} emitting the result
     */
    Single<ParseInstallation> clearInstallation();
}
