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
    public static final String ARCHIVED_IDENTITIES = "archivedIdentities";
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

    @NonNull
    public List<Identity> getArchivedIdentities() {
        final List<Identity> identities = getList(ARCHIVED_IDENTITIES);
        if (identities == null) {
            return Collections.emptyList();
        }

        return identities;
    }

    public void setArchivedIdentities(@NonNull List<Identity> identities) {
        put(ARCHIVED_IDENTITIES, identities);
    }

    public void addArchivedIdentity(@NonNull Identity identity) {
        addUnique(ARCHIVED_IDENTITIES, identity);
    }

    public Identity getCurrentIdentity() {
        return (Identity) getParseObject(CURRENT_IDENTITY);
    }

    public void setCurrentIdentity(@NonNull Identity currentIdentity) {
        put(CURRENT_IDENTITY, currentIdentity);
    }

    /**
     * Archives the current identity and sets the first in the list as the user's new current
     * identity.
     *
     * @return the new current identity.
     */
    public Identity archiveCurrentIdentity() {
        final Identity currentIdentity = getCurrentIdentity();

        // remove from active list
        final List<Identity> identities = new ArrayList<>();
        identities.add(currentIdentity);
        removeAll(IDENTITIES, identities);

        // add to archived list
        currentIdentity.setActive(false);
        addArchivedIdentity(currentIdentity);

        // set new current identity and return it
        final Identity newCurrent = getIdentities().get(0);
        setCurrentIdentity(newCurrent);
        return newCurrent;
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

    /**
     * Returns whether one of the identity ids provided belongs to the user.
     *
     * @param identityIds the ids of the identities to check
     * @return whether one of the identity ids provided belongs to the user
     */
    public boolean hasIdentity(@NonNull List<String> identityIds) {
        final List<Identity> userIdentities = getIdentities();
        for (Identity identity : userIdentities) {
            if (identityIds.contains(identity.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return whether the identity id provided belongs to the user
     *
     * @param identityId the id of the identity to check
     * @return whether the identity id provided belongs to the user
     */
    public boolean hasIdentity(@NonNull String identityId) {
        final List<Identity> userIdentities = getIdentities();
        for (Identity identity : userIdentities) {
            if (identity.getObjectId().equals(identityId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the user is part of the group
     *
     * @param groupId the id of the group to check
     * @return whether the user is part of the group
     */
    public boolean isInGroup(@NonNull String groupId) {
        final List<String> groupIds = new ArrayList<>();
        final List<Identity> identities = getIdentities();
        for (Identity identity : identities) {
            groupIds.add(identity.getGroup().getObjectId());
        }

        return groupIds.contains(groupId);
    }

    /**
     * Returns the identity for the group or null if the user is not part of the group.
     *
     * @param groupId the id of the group to return the identity for
     * @return the identity for the group or null if the user is not part of the group
     */
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

    public boolean isVanilla() {
        return getIdentities().isEmpty();
    }
}

