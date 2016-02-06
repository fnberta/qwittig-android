/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import ch.giantific.qwittig.domain.models.parse.User;
import rx.Single;

/**
 * Provides the methods to get, update and remove users from the local and online data store.
 */
public interface UserRepository extends Repository {

    /**
     * Returns the currently logged in user, null if no one is logged in.
     *
     * @return the currently logged in user
     */
    @Nullable
    User getCurrentUser();

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
    Single<User> loginGoogle(@NonNull final Fragment fragment,
                             @NonNull String idToken,
                             @NonNull final String displayName,
                             @NonNull final Uri photoUrl);

    /**
     * Logs out the user.
     *
     * @param user the user to log out
     * @return the logged out user
     */
    Single<User> logOut(@NonNull User user);
}
