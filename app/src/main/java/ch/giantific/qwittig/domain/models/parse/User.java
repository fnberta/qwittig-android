/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;


/**
 * Represents a user in a group that shares their purchases and tasks and wishes to balance
 * everything.
 */
public class User extends ParseUser {

    public static final String CLASS = "_User";
    public static final String NAME = "username";
    public static final String PASSWORD = "password";
    public static final String CURRENT_IDENTITY = "currentIdentity";
    public static final String GOOGLE_ID = "googleId";

    public User() {
        // A default constructor is required.
    }

    public User(@NonNull String email, @NonNull String password) {
        setUsername(email);
        setPassword(password);
    }

    public Identity getCurrentIdentity() {
        return (Identity) getParseObject(CURRENT_IDENTITY);
    }

    public void setCurrentIdentity(@NonNull Identity currentIdentity) {
        put(CURRENT_IDENTITY, currentIdentity);
    }

    public void removeCurrentIdentity() {
        remove(CURRENT_IDENTITY);
    }

    public String getGoogleId() {
        return getString(GOOGLE_ID);
    }

    public void removeGoogleId() {
        remove(GOOGLE_ID);
    }

    public boolean isGoogleUser() {
        return !TextUtils.isEmpty(getGoogleId());
    }

    public boolean isFacebookUser() {
        return ParseFacebookUtils.isLinked(this);
    }
}

