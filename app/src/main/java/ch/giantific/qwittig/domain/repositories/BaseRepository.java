/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by fabio on 10.01.16.
 */
public interface BaseRepository {

    /**
     * Returns the appropriate error message for the specified {@link Throwable}.
     *
     * @param e the exception to handle
     * @return the appropriate error message for the exception
     */
    @StringRes
    int getErrorMessage(@NonNull Throwable e);

    /**
     * Returns whether the object is already saved to the local data store.
     *
     * @param objectId the object id of the object in question
     * @return whether the object is already saved to the local data store or not
     */
    boolean isAlreadySavedLocal(@NonNull String objectId);
}
