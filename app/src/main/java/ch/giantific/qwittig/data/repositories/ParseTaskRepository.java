/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link TaskRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseTaskRepository extends ParseBaseRepository implements TaskRepository {

    private static final String PUSH_TASK_REMIND = "pushTaskRemind";

    public ParseTaskRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Task.CLASS;
    }

    @Override
    public Single<Task> saveTaskLocalAsync(@NonNull Task task, @NonNull String tag) {
        return pin(task, tag);
    }

    @Override
    public Observable<Task> getTasksLocalAsync(@NonNull Identity identity,
                                               @NonNull Date deadline) {
        ParseQuery<Task> deadlineQuery = ParseQuery.getQuery(Task.CLASS);
        deadlineQuery.whereLessThan(Task.DEADLINE, deadline);

        ParseQuery<Task> asNeededQuery = ParseQuery.getQuery(Task.CLASS);
        asNeededQuery.whereDoesNotExist(Task.DEADLINE);

        List<ParseQuery<Task>> queries = new ArrayList<>();
        queries.add(deadlineQuery);
        queries.add(asNeededQuery);

        ParseQuery<Task> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Task.GROUP, identity.getGroup());
        query.include(Task.IDENTITIES);
        query.orderByAscending(Task.DEADLINE);

        return find(query).concatMap(new Func1<List<Task>, Observable<Task>>() {
            @Override
            public Observable<Task> call(List<Task> tasks) {
                return Observable.from(tasks);
            }
        });
    }

    @Override
    public Single<Task> getTaskLocalAsync(@NonNull String taskId) {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Task.IDENTITIES);

        return get(query, taskId);
    }

    @Override
    public Single<Task> fetchTaskDataLocalAsync(@NonNull final String taskId) {
        final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
        return fetchLocal(task);
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
    public Observable<Task> updateTasksAsync(@NonNull List<Identity> identities) {
        return Observable.from(identities)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return identity.isActive();
                    }
                })
                .map(new Func1<Identity, Group>() {
                    @Override
                    public Group call(Identity identity) {
                        return identity.getGroup();
                    }
                })
                .toList()
                .map(new Func1<List<Group>, ParseQuery<Task>>() {
                    @Override
                    public ParseQuery<Task> call(List<Group> groups) {
                        return getTasksOnlineQuery(groups);
                    }
                })
                .flatMap(new Func1<ParseQuery<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(ParseQuery<Task> taskParseQuery) {
                        return find(taskParseQuery);
                    }
                })
                .concatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return unpinAll(tasks, Task.PIN_LABEL);
                    }
                })
                .concatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return pinAll(tasks, Task.PIN_LABEL);
                    }
                })
                .concatMap(new Func1<List<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(List<Task> tasks) {
                        return Observable.from(tasks);
                    }
                });
    }

    @NonNull
    private ParseQuery<Task> getTasksOnlineQuery(@NonNull List<Group> groups) {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
        query.whereContainedIn(Task.GROUP, groups);
        query.include(Task.IDENTITIES);
        return query;
    }

    @Override
    public boolean updateTasks(@NonNull List<Identity> identities) {
        final List<Group> groups = new ArrayList<>();
        for (Identity identity : identities) {
            if (identity.isActive()) {
                groups.add(identity.getGroup());
            }
        }

        try {
            final ParseQuery<Task> query = getTasksOnlineQuery(groups);
            final List<Task> tasks = query.find();
            ParseObject.unpinAll(Task.PIN_LABEL);
            ParseObject.pinAll(Task.PIN_LABEL, tasks);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean updateTask(@NonNull String taskId, boolean isNew) {
        try {
            final Task task = getTaskOnline(taskId);
            if (isNew) {
                task.pin(Task.PIN_LABEL);
            }
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private Task getTaskOnline(@NonNull String taskId) throws ParseException {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.include(Task.IDENTITIES);
        return (Task) query.get(taskId);
    }

    @Override
    public Task fetchTaskDataLocal(@NonNull String taskId) {
        final Task task = (Task) ParseObject.createWithoutData(Task.CLASS, taskId);
        try {
            task.fetchFromLocalDatastore();
        } catch (ParseException e) {
            return null;
        }

        return task;
    }


    @Override
    public Single<String> pushTaskReminder(@NonNull String taskId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK_ID, taskId);
        return callFunctionInBackground(PUSH_TASK_REMIND, params);
    }
}
