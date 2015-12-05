/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.Context;
import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Provides a base class for repository implementations that use the Parse.com framework.
 */
public class ParseGenericRepository {

    public static final int QUERY_ITEMS_PER_PAGE = 15;
    int mNumberOfUpdatedQueries;
    int mQueryCount;
    Context mContext;

    public ParseGenericRepository(Context context) {
        mContext = context.getApplicationContext();
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
