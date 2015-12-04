/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides an abstract base class for worker fragments whose task it is to query data from the
 * online data store.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public abstract class BaseQueryWorker extends BaseWorker {

    private static final String LOG_TAG = BaseQueryWorker.class.getSimpleName();
    User mCurrentUser;
    Group mCurrentGroup;
    List<ParseObject> mCurrentUserGroups;

    /**
     * Returns if the current user's current group and groups are not null or empty.
     *
     * @return whether the current user's current group and groups are not null or empty
     */
    final boolean setCurrentGroups() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
            mCurrentUserGroups = mCurrentUser.getGroups();
        }

        return mCurrentUser != null && mCurrentGroup != null && !mCurrentUserGroups.isEmpty();

    }
}
