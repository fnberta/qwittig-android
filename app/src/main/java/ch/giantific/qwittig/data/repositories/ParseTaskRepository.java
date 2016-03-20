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

import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
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
    public Single<Task> saveTaskLocal(@NonNull Task task, @NonNull String tag) {
        return pin(task, tag);
    }

    @Override
    public Observable<Task> getTasks(@NonNull Identity identity,
                                     @NonNull Date deadline) {
        ParseQuery<Task> deadlineQuery = ParseQuery.getQuery(Task.CLASS);
        deadlineQuery.whereLessThan(Task.DEADLINE, deadline);

        ParseQuery<Task> asNeededQuery = ParseQuery.getQuery(Task.CLASS);
        asNeededQuery.whereDoesNotExist(Task.DEADLINE);

        List<ParseQuery<Task>> queries = new ArrayList<>();
        queries.add(deadlineQuery);
        queries.add(asNeededQuery);

        ParseQuery<Task> query = ParseQuery.or(queries);
        query.whereEqualTo(Task.GROUP, identity.getGroup());
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Task.IDENTITIES);
        query.orderByAscending(Task.DEADLINE);

        return find(query)
                .concatMap(new Func1<List<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(List<Task> tasks) {
                        return Observable.from(tasks);
                    }
                });
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        final ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Task.IDENTITIES);

        return get(query, taskId);
    }

    @Override
    public Observable<TaskHistoryEvent> getTaskHistoryEvents(@NonNull Task task) {
        final ParseQuery<TaskHistoryEvent> query = ParseQuery.getQuery(TaskHistoryEvent.CLASS);
        query.whereEqualTo(TaskHistoryEvent.TASK, task);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(TaskHistoryEvent.IDENTITY);
        return find(query)
                .concatMap(new Func1<List<TaskHistoryEvent>, Observable<? extends TaskHistoryEvent>>() {
                    @Override
                    public Observable<? extends TaskHistoryEvent> call(List<TaskHistoryEvent> taskHistoryEvents) {
                        return Observable.from(taskHistoryEvents);
                    }
                });
    }

    @Override
    public Single<TaskHistoryEvent> saveTaskHistoryEvent(@NonNull TaskHistoryEvent taskHistoryEvent) {
        return pin(taskHistoryEvent, TaskHistoryEvent.PIN_LABEL)
                .doOnSuccess(new Action1<TaskHistoryEvent>() {
                    @Override
                    public void call(TaskHistoryEvent taskHistoryEvent) {
                        taskHistoryEvent.saveEventually();
                    }
                });
    }

    @Override
    public Single<Task> fetchTaskData(@NonNull final String taskId) {
        final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
        return fetchLocal(task);
    }

    @Override
    public boolean removeTaskLocal(@NonNull String taskId) {
        final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
        try {
            task.unpin(Task.PIN_LABEL);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean updateTasks(@NonNull List<Identity> identities) {
        final List<Group> groups = new ArrayList<>();
        for (Identity identity : identities) {
            groups.add(identity.getGroup());
        }

        try {
            final ParseQuery<Task> query = getTasksOnlineQuery(groups);
            final List<Task> tasks = query.find();
            ParseObject.unpinAll(Task.PIN_LABEL);
            ParseObject.pinAll(Task.PIN_LABEL, tasks);

            updateTaskHistoryEvents(tasks);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void updateTaskHistoryEvents(List<Task> tasks) throws ParseException {
        final ParseQuery<TaskHistoryEvent> query = ParseQuery.getQuery(TaskHistoryEvent.CLASS);
        query.whereContainedIn(TaskHistoryEvent.TASK, tasks);
        query.include(TaskHistoryEvent.IDENTITY);
        final List<TaskHistoryEvent> events = query.find();
        ParseObject.unpinAll(TaskHistoryEvent.PIN_LABEL);
        ParseObject.pinAll(TaskHistoryEvent.PIN_LABEL, events);
    }

    @NonNull
    private ParseQuery<Task> getTasksOnlineQuery(@NonNull List<Group> groups) {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
        query.whereContainedIn(Task.GROUP, groups);
        query.include(Task.IDENTITIES);
        return query;
    }

    @Override
    public boolean updateTask(@NonNull String taskId, boolean isNew) {
        try {
            final ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
            query.include(Task.IDENTITIES);
            final Task task = query.get(taskId);
            if (isNew) {
                task.pin(Task.PIN_LABEL);
            }
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean setTaskDone(@NonNull String taskId, @NonNull Identity identity) {
        try {
            final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
            task.fetchFromLocalDatastore();
            final TaskHistoryEvent newEvent = new TaskHistoryEvent(task, identity, new Date());
            newEvent.pin(TaskHistoryEvent.PIN_LABEL);
            newEvent.save();
            task.handleHistoryEvent();
            task.save();
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Single<String> pushTaskReminder(@NonNull String taskId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK_ID, taskId);
        return callFunctionInBackground(PUSH_TASK_REMIND, params);
    }
}
