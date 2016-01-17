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
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link TaskRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseTaskRepository extends ParseBaseRepository<Task> implements TaskRepository {

    @Inject
    public ParseTaskRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Task.CLASS;
    }

    @Override
    public Observable<Task> getTasksLocalAsync(@NonNull Group group, @NonNull Date deadline) {
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
        query.whereEqualTo(Task.GROUP, group);
        query.include(Task.USERS_INVOLVED);
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
        query.include(Task.USERS_INVOLVED);

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
    public Observable<Task> updateTasksAsync(@NonNull List<ParseObject> groups) {
        ParseQuery<Task> query = getTasksOnlineQuery(groups);

        return find(query)
                .concatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return unpin(tasks, Task.PIN_LABEL);
                    }
                })
                .concatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return pin(tasks, Task.PIN_LABEL);
                    }
                })
                .concatMap(new Func1<List<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(List<Task> parseUsers) {
                        return Observable.from(parseUsers);
                    }
                });
    }

    @NonNull
    private ParseQuery<Task> getTasksOnlineQuery(@NonNull List<ParseObject> groups) {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
        query.whereContainedIn(Task.GROUP, groups);
        query.include(Task.USERS_INVOLVED);
        return query;
    }

    @Override
    public boolean updateTasks(@NonNull List<ParseObject> groups) {
        try {
            List<Task> tasks = getTasksOnline(groups);
            ParseObject.unpinAll(Task.PIN_LABEL);
            ParseObject.pinAll(Task.PIN_LABEL, tasks);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private List<Task> getTasksOnline(@NonNull List<ParseObject> groups) throws ParseException {
        ParseQuery<Task> query = getTasksOnlineQuery(groups);
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
