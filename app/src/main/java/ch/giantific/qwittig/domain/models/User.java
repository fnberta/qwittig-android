/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseFacebookUtils;
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
    public static final String CURRENT_IDENTITY = "currentIdentity";
    public static final String GOOGLE_ID = "googleId";

    public User() {
        // A default constructor is required.
    }

    public User(@NonNull String email, @NonNull String password) {
        setUsername(email);
        setPassword(password);
    }

    @NonNull
    public List<Identity> getIdentities() {
        final List<Identity> identities = getList(IDENTITIES);
        if (identities == null) {
            return Collections.emptyList();
        }

        return identities;
    }

    public void setIdentities(@NonNull List<Identity> identities) {
        put(IDENTITIES, identities);
    }

    public void addIdentity(@NonNull Identity identity) {
        addUnique(IDENTITIES, identity);
    }

    public void removeIdentity(@NonNull Identity identity) {
        final List<Identity> identities = new ArrayList<>();
        identities.add(identity);
        removeAll(IDENTITIES, identities);
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

    public boolean hasIdentity(@NonNull List<String> identityIds) {
        final List<Identity> userIdentities = getIdentities();
        for (Identity identity : userIdentities) {
            if (identityIds.contains(identity.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    public boolean isInGroup(@NonNull String groupId) {
        final List<String> groupIds = new ArrayList<>();
        final List<Identity> identities = getIdentities();
        for (Identity identity : identities) {
            groupIds.add(identity.getGroup().getObjectId());
        }

        return groupIds.contains(groupId);
    }

    @Nullable
    public Identity getIdentityForGroup(@NonNull String groupId) {
        final List<Identity> identities = getIdentities();
        for (Identity identity : identities) {
            if (identity.getGroup().getObjectId().equals(groupId)) {
                return identity;
            }
        }

        return null;
    }
}

