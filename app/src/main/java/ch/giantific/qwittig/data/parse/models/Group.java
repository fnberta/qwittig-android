/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.parse.models;

import android.support.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a group, consisting of a name, a currency code and the emails of users invited
 * currently invited to the group.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Group")
public class Group extends ParseObject {

    public static final String CLASS = "Group";
    public static final String NAME = "name";
    public static final String CURRENCY = "currency";
    public static final String USERS_INVITED = "usersInvited";
    public static final String ROLE_PREFIX = "groupOf_";

    public Group() {
        // A default constructor is required.
    }

    public Group(@NonNull String name, @NonNull String currency) {
        setName(name);
        setCurrency(currency);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(@NonNull String name) {
        put(NAME, name);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public void setCurrency(@NonNull String currency) {
        put(CURRENCY, currency);
    }

    @NonNull
    public List<String> getUsersInvited() {
        List<String> usersInvited = getList(USERS_INVITED);
        if (usersInvited == null) {
            return Collections.emptyList();
        }

        return usersInvited;
    }

    public void setUsersInvited(@NonNull List<String> usersInvited) {
        put(USERS_INVITED, usersInvited);
    }

    public void addUsersInvited(@NonNull List<String> usersInvited) {
        addAllUnique(USERS_INVITED, usersInvited);
    }

    public void addUserInvited(@NonNull String userInvited) {
        add(USERS_INVITED, userInvited);
    }

    public void removeUserInvited(@NonNull String userInvited) {
        List<String> userList = new ArrayList<>();
        userList.add(userInvited);
        removeUsersInvited(userList);
    }

    public void removeUsersInvited(@NonNull List<String> usersInvited) {
        removeAll(USERS_INVITED, usersInvited);
    }
}
