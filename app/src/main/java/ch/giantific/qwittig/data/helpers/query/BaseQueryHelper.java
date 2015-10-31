/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.query;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides an abstract base class for helper fragments whose task it is to query data from the
 * online data store.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public abstract class BaseQueryHelper extends BaseHelper {

    private static final String LOG_TAG = BaseQueryHelper.class.getSimpleName();
    Group mCurrentGroup;
    List<ParseObject> mCurrentUserGroups;

    /**
     * Returns if the current user's current group and groups are not null or empty.
     *
     * @return whether the current user's current group and groups are not null or empty
     */
    final boolean setCurrentGroups() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null) {
            mCurrentGroup = currentUser.getCurrentGroup();
            mCurrentUserGroups = currentUser.getGroups();
        }

        return !(mCurrentGroup == null || mCurrentUserGroups == null || mCurrentUserGroups.isEmpty());

    }
}
