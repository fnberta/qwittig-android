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

import ch.giantific.qwittig.domain.models.User;
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

    Single<User> updateUser(@NonNull User user);

    /**
     * Returns the session token for current user session.
     *
     * @return the session token
     */
    Single<String> getUserSessionToken();

    /**
     * Logs the user in using his email address and password.
     *
     * @param username the email address for the login
     * @param password the pass word for the login
     * @return the logged in user
     */
    Single<User> loginEmail(@NonNull String username, @NonNull String password);

    /**
     * Sends the user an email with a link to reset his password
     *
     * @param email the email address to send the link to
     * @return the email address the reset link was sent to
     */
    Single<String> requestPasswordReset(@NonNull String email);

    /**
     * Signs up and logs in a new user using his email address as username and a password.
     *
     * @param username the user's email to use as username
     * @param password the user's password
     * @return the now signed up and logged in user
     */
    Single<User> signUpEmail(@NonNull String username, @NonNull String password);

    /**
     * Logs in a user using his facebook account
     *
     * @param fragment the fragment that is initiating the login
     * @return the logged in user
     */
    Single<User> loginFacebook(@NonNull Fragment fragment);

    /**
     * Logs in the user using his google account.
     *
     * @param idToken     the google id token
     * @param displayName the google profile display name
     * @param photoUrl    the url to the google profile image
     * @return the logged in user
     */
    Single<User> loginGoogle(@NonNull final Fragment fragment, @NonNull String idToken,
                             @NonNull final String displayName, @NonNull final Uri photoUrl);

    /**
     * Verifies the idToken obtained from Google and if successful logs in the user attached to the
     * email address in the token. If no such user exists yet, creates a new one.
     *
     * @param idToken the token obtained from the Google login
     */
    Single<JSONObject> verifyGoogleLogin(@NonNull String idToken);

    /**
     * Logs out the user.
     *
     * @param user the user to log out
     * @return the logged out user
     */
    Single<User> logOut(@NonNull User user);

    Single<User> unlinkFacebook(@NonNull User user);

    Single<Void> signOutGoogle(@NonNull Context context);

    Single<User> unlinkGoogle(@NonNull Context context, @NonNull User user);

    /**
     * Deletes a users account by deleting it. His identities will be kept because they are
     * referenced in other objects (e.g. purchases and items.
     *
     * @param user the user to delete
     */
    Single<User> deleteUser(@NonNull User user);

    Single<ParseInstallation> clearInstallation();
}
