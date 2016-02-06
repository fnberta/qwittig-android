/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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
}
