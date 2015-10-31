/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 28.10.15.
 */
public class ParseGenericRepository {

    public static final int QUERY_ITEMS_PER_PAGE = 15;
    int mNumberOfUpdatedQueries;
    int mQueryCount;

    public ParseGenericRepository() {
    }

    final boolean allUpdatesDone() {
        mQueryCount++;
        return mQueryCount == mNumberOfUpdatedQueries;

    }

    /**
     * Returns whether the object is already saved to the local data store.
     *
     * @param className the class name of the object in question
     * @param objectId  the object id of the object in question
     * @return whether the object is already saved to the local data store or not
     */
    public boolean isAlreadySavedLocal(@NonNull String className, @NonNull String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.ignoreACLs();
        query.fromLocalDatastore();
        try {
            query.get(objectId);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }
}
