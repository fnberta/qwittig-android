/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;

/**
 * Provides an implementation of {@link TaskRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseTaskRepository extends ParseGenericRepository implements TaskRepository {

    public ParseTaskRepository() {
        super();
    }

    @Override
    public void getTasksLocalAsync(@NonNull Group group, @NonNull Date deadline,
                                   @NonNull final GetTasksLocalListener listener) {
        ParseQuery<ParseObject> deadlineQuery = ParseQuery.getQuery(Task.CLASS);
        deadlineQuery.whereLessThan(Task.DEADLINE, deadline);

        ParseQuery<ParseObject> asNeededQuery = ParseQuery.getQuery(Task.CLASS);
        asNeededQuery.whereDoesNotExist(Task.DEADLINE);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(deadlineQuery);
        queries.add(asNeededQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Task.GROUP, group);
        query.include(Task.USERS_INVOLVED);
        query.orderByAscending(Task.DEADLINE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e == null) {
                    listener.onTasksLocalLoaded(parseObjects);
                }
            }
        });
    }

    @Override
    public void getTaskLocalAsync(@NonNull String taskId,
                                  @NonNull final GetTaskLocalListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Task.USERS_INVOLVED);
        query.getInBackground(taskId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, @Nullable ParseException e) {
                if (e == null) {
                    listener.onTaskLocalLoaded((Task) object);
                }
            }
        });
    }

    @Override
    public void fetchTaskDataLocalAsync(@NonNull String taskId,
                                        @NonNull final GetTaskLocalListener listener) {
        ParseObject parseObject = ParseObject.createWithoutData(Task.CLASS, taskId);
        parseObject.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e == null) {
                    listener.onTaskLocalLoaded((Task) parseObject);
                }
            }
        });
    }

    @Override
    public boolean removeTaskLocal(@NonNull String taskId) {
        ParseObject task = ParseObject.createWithoutData(Task.CLASS, taskId);
        try {
            task.unpin(Task.PIN_LABEL);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public void updateTasksAsync(@NonNull List<ParseObject> groups,
                                 @NonNull final UpdateTasksListener listener) {
        ParseQuery<ParseObject> query = getTasksOnlineQuery(groups);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    listener.onTaskUpdateFailed(e.getCode());
                    return;
                }

                ParseObject.unpinAllInBackground(Task.PIN_LABEL, new DeleteCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            listener.onTaskUpdateFailed(e.getCode());
                            return;
                        }

                        ParseObject.pinAllInBackground(Task.PIN_LABEL, parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e != null) {
                                    listener.onTaskUpdateFailed(e.getCode());
                                    return;
                                }

                                listener.onTasksUpdated();
                            }
                        });
                    }
                });
            }
        });
    }

    @NonNull
    private ParseQuery<ParseObject> getTasksOnlineQuery(@NonNull List<ParseObject> groups) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.whereContainedIn(Task.GROUP, groups);
        query.include(Task.USERS_INVOLVED);
        return query;
    }

    @Override
    public boolean updateTasks(@NonNull List<ParseObject> groups) {
        try {
            List<ParseObject> tasks = getTasksOnline(groups);
            ParseObject.unpinAll(Task.PIN_LABEL);
            ParseObject.pinAll(Task.PIN_LABEL, tasks);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private List<ParseObject> getTasksOnline(@NonNull List<ParseObject> groups) throws ParseException {
        ParseQuery<ParseObject> query = getTasksOnlineQuery(groups);
        return query.find();
    }

    @Override
    public boolean updateTask(@NonNull String taskId, boolean isNew) {
        try {
            Task task = getTaskOnline(taskId);
            if (isNew) {
                task.pin(Task.PIN_LABEL);
            }
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private Task getTaskOnline(@NonNull String taskId) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.include(Task.USERS_INVOLVED);
        return (Task) query.get(taskId);
    }

    @Override
    public Task fetchTaskDataLocal(@NonNull String taskId) {
        ParseObject parseObject = ParseObject.createWithoutData(Task.CLASS, taskId);
        try {
            parseObject.fetchFromLocalDatastore();
        } catch (ParseException e) {
            return null;
        }

        return (Task) parseObject;
    }

}
