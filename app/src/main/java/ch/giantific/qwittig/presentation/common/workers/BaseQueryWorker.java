/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;

/**
 * Provides an abstract base class for worker fragments whose task it is to query data from the
 * online data store.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public abstract class BaseQueryWorker<T, S extends BaseWorkerListener>
        extends BaseWorker<T, S> {

    @Inject
    protected IdentityRepository mIdentityRepo;
    protected Identity mCurrentIdentity;
    protected List<Identity> mIdentities;

    /**
     * Returns if the current user's current identity and identities are not null or empty.
     *
     * @return whether the current user's current identities and identities are not null or empty
     */
    protected final boolean setUserInfo() {
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null) {
            mCurrentIdentity = currentUser.getCurrentIdentity();
            mIdentities = currentUser.getIdentities();
        }

        return currentUser != null && mCurrentIdentity != null && !mIdentities.isEmpty();
    }
}
