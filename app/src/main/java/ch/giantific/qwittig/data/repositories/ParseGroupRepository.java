/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseQuery;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;

/**
 * Provides an implementation of {@link GroupRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseGroupRepository extends ParseBaseRepository implements GroupRepository {


    public ParseGroupRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Group.CLASS;
    }

    @Override
    @Nullable
    public Group queryGroup(@NonNull String groupId) {
        final ParseQuery<Group> query = ParseQuery.getQuery(Group.CLASS);
        try {
            return query.get(groupId);
        } catch (ParseException e) {
            return null;
        }
    }
}
