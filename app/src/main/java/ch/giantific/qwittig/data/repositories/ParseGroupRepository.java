/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;

/**
 * Provides an implementation of {@link GroupRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseGroupRepository extends ParseGenericRepository implements GroupRepository {

    public ParseGroupRepository() {
        super();
    }

    @Override
    public void fetchGroupDataAsync(@NonNull final ParseObject group,
                                    @NonNull final GetGroupLocalListener listener) {
        group.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e == null) {
                    listener.onGroupLocalLoaded((Group) parseObject);
                } else {
                    group.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, @Nullable ParseException e) {
                            if (e == null) {
                                listener.onGroupLocalLoaded((Group) parseObject);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void getGroupOnlineAsync(@NonNull String groupId,
                                    @NonNull final GetGroupOnlineListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Group.CLASS);
        query.getInBackground(groupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e != null) {
                    listener.onGroupOnlineLoadFailed(e.getCode());
                    return;
                }

                listener.onGroupOnlineLoaded((Group) parseObject);
            }
        });
    }

    @Override
    @Nullable
    public Group getGroupOnline(@NonNull String groupId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Group.CLASS);
        try {
            return (Group) query.get(groupId);
        } catch (ParseException e) {
            return null;
        }
    }

}
