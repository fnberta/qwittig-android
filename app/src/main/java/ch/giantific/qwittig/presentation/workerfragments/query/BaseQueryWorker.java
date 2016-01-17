/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import com.parse.ParseObject;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;

/**
 * Provides an abstract base class for worker fragments whose task it is to query data from the
 * online data store.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public abstract class BaseQueryWorker<T, S extends BaseWorkerListener> extends BaseWorker<T, S> {

    private static final String LOG_TAG = BaseQueryWorker.class.getSimpleName();
    @Inject
    User mCurrentUser;
    Group mCurrentGroup;
    List<ParseObject> mCurrentUserGroups;

    /**
     * Returns if the current user's current group and groups are not null or empty.
     *
     * @return whether the current user's current group and groups are not null or empty
     */
    final boolean setCurrentGroups() {
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
            mCurrentUserGroups = mCurrentUser.getGroups();
        }

        return mCurrentUser != null && mCurrentGroup != null && !mCurrentUserGroups.isEmpty();
    }
}
