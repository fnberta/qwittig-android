/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents a user in a group that shares their purchases and tasks and wishes to balance
 * everything.
 */
public class User extends ParseUser {

    public static final String CLASS = "_User";
    public static final String NAME = "username";
    public static final String PASSWORD = "password";
    public static final String IDENTITIES = "identities";
    public static final String CURRENT_IDENTITY = "currentIdentities";
    public static final String GOOGLE_ID = "googleId";

    public User() {
        // A default constructor is required.
    }

    public User(@NonNull String email, @NonNull String password) {
        setUsername(email);
        setPassword(password);
    }

    @NonNull
    public List<ParseObject> getIdentities() {
        List<ParseObject> identities = getList(IDENTITIES);
        if (identities != null) {
            return identities;
        } else {
            return Collections.emptyList();
        }
    }

    public void setIdentities(@NonNull List<ParseObject> identities) {
        put(IDENTITIES, identities);
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

    /**
     * Returns the object ids of the user's identities.
     *
     * @return the object ids of the user's identities
     */
    @NonNull
    public List<String> getIdentityIds() {
        List<String> identityIds = new ArrayList<>();
        List<ParseObject> identities = getIdentities();
        for (ParseObject parseObject : identities) {
            identityIds.add(parseObject.getObjectId());
        }

        return identityIds;
    }

    public void addIdentity(@NonNull Identity identity) {
        addUnique(IDENTITIES, identity);
    }

    public void removeIdentity(@NonNull Identity identity) {
        List<ParseObject> identities = new ArrayList<>();
        identities.add(identity);
        removeAll(IDENTITIES, identities);
    }

    public void removeIdentities() {
        remove(IDENTITIES);
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

