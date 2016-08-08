package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;

/**
 * Created by fabio on 12.07.16.
 */
public class TaskRepository {

    private final DatabaseReference mDatabaseRef;

    @Inject
    public TaskRepository(@NonNull FirebaseDatabase firebaseDatabase) {
        mDatabaseRef = firebaseDatabase.getReference();
    }

    public void deleteTask(@NonNull String taskId) {
        mDatabaseRef.child(Task.PATH).child(taskId).removeValue();
    }

    public void addHistoryEvent(@NonNull TaskHistoryEvent historyEvent) {

    }

    public void saveTask(@NonNull Task task) {

    }

    //    @Override
//    protected String getClassName() {
//        return Task.CLASS;
//    }
//
//    @Override
//    public Single<Task> saveTask(@NonNull Task task) {
//        return pin(task, Task.PIN_LABEL)
//                .doOnSuccess(new Action1<Task>() {
//                    @Override
//                    public void call(Task task) {
//                        task.saveEventually();
//                    }
//                });
//    }
//
//    @Override
//    public Observable<Task> getTasks(@NonNull Identity identity,
//                                     @NonNull Date deadline) {
//        ParseQuery<Task> deadlineQuery = ParseQuery.getQuery(Task.CLASS);
//        deadlineQuery.whereLessThan(Task.DEADLINE, deadline);
//
//        ParseQuery<Task> asNeededQuery = ParseQuery.getQuery(Task.CLASS);
//        asNeededQuery.whereDoesNotExist(Task.DEADLINE);
//
//        List<ParseQuery<Task>> queries = new ArrayList<>();
//        queries.add(deadlineQuery);
//        queries.add(asNeededQuery);
//
//        ParseQuery<Task> query = ParseQuery.or(queries);
//        query.whereEqualTo(Task.GROUP, identity.getGroup());
//        query.fromLocalDatastore();
//        query.ignoreACLs();
//        query.include(Task.IDENTITIES);
//        query.orderByAscending(Task.DEADLINE);
//
//        return find(query)
//                .concatMap(new Func1<List<Task>, Observable<Task>>() {
//                    @Override
//                    public Observable<Task> call(List<Task> tasks) {
//                        return Observable.from(tasks);
//                    }
//                });
//    }
//
//    @Override
//    public Single<Task> getTask(@NonNull String taskId) {
//        final ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
//        query.fromLocalDatastore();
//        query.ignoreACLs();
//        query.include(Task.IDENTITIES);
//
//        return get(query, taskId);
//    }
//
//    @Override
//    public Observable<TaskHistoryEvent> getTaskHistoryEvents(@NonNull Task task) {
//        final ParseQuery<TaskHistoryEvent> query = ParseQuery.getQuery(TaskHistoryEvent.CLASS);
//        query.whereEqualTo(TaskHistoryEvent.TASK, task);
//        query.fromLocalDatastore();
//        query.ignoreACLs();
//        query.include(TaskHistoryEvent.IDENTITY);
//        return find(query)
//                .concatMap(new Func1<List<TaskHistoryEvent>, Observable<? extends TaskHistoryEvent>>() {
//                    @Override
//                    public Observable<? extends TaskHistoryEvent> call(List<TaskHistoryEvent> taskHistoryEvents) {
//                        return Observable.from(taskHistoryEvents);
//                    }
//                });
//    }
//
//    @Override
//    public Single<TaskHistoryEvent> saveTaskHistoryEvent(@NonNull TaskHistoryEvent taskHistoryEvent) {
//        return pin(taskHistoryEvent, TaskHistoryEvent.PIN_LABEL)
//                .doOnSuccess(new Action1<TaskHistoryEvent>() {
//                    @Override
//                    public void call(TaskHistoryEvent taskHistoryEvent) {
//                        taskHistoryEvent.saveEventually();
//                    }
//                });
//    }
//
//    @Override
//    public Single<Task> fetchTaskData(@NonNull final String taskId) {
//        final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
//        return fetchLocal(task);
//    }
//
//    @Override
//    public boolean removeTaskLocal(@NonNull String taskId) {
//        final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
//        try {
//            task.unpin(Task.PIN_LABEL);
//        } catch (ParseException e) {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean updateTasks(@NonNull List<Identity> identities) {
//        final List<Group> groups = new ArrayList<>();
//        for (Identity identity : identities) {
//            groups.add(identity.getGroup());
//        }
//
//        try {
//            final ParseQuery<Task> query = getTasksOnlineQuery(groups);
//            final List<Task> tasks = query.find();
//            ParseObject.unpinAll(Task.PIN_LABEL);
//            ParseObject.pinAll(Task.PIN_LABEL, tasks);
//
//            updateTaskHistoryEvents(tasks);
//        } catch (ParseException e) {
//            return false;
//        }
//
//        return true;
//    }
//
//    private void updateTaskHistoryEvents(List<Task> tasks) throws ParseException {
//        final ParseQuery<TaskHistoryEvent> query = ParseQuery.getQuery(TaskHistoryEvent.CLASS);
//        query.whereContainedIn(TaskHistoryEvent.TASK, tasks);
//        query.include(TaskHistoryEvent.IDENTITY);
//        final List<TaskHistoryEvent> events = query.find();
//        ParseObject.unpinAll(TaskHistoryEvent.PIN_LABEL);
//        ParseObject.pinAll(TaskHistoryEvent.PIN_LABEL, events);
//    }
//
//    @NonNull
//    private ParseQuery<Task> getTasksOnlineQuery(@NonNull List<Group> groups) {
//        ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
//        query.whereContainedIn(Task.GROUP, groups);
//        query.include(Task.IDENTITIES);
//        return query;
//    }
//
//    @Override
//    public boolean updateTask(@NonNull String taskId, boolean isNew) {
//        try {
//            final ParseQuery<Task> query = ParseQuery.getQuery(Task.CLASS);
//            query.include(Task.IDENTITIES);
//            final Task task = query.get(taskId);
//            if (isNew) {
//                task.pin(Task.PIN_LABEL);
//            }
//        } catch (ParseException e) {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean setTaskDone(@NonNull String taskId, @NonNull Identity identity) {
//        try {
//            final Task task = (Task) Task.createWithoutData(Task.CLASS, taskId);
//            task.fetchFromLocalDatastore();
//            final TaskHistoryEvent newEvent = new TaskHistoryEvent(task, identity, new Date());
//            newEvent.pin(TaskHistoryEvent.PIN_LABEL);
//            newEvent.save();
//            task.handleHistoryEvent();
//            task.save();
//        } catch (ParseException e) {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean updateTaskHistoryEvent(@NonNull String eventId, boolean isNew) {
//        try {
//            final ParseQuery<TaskHistoryEvent> query = ParseQuery.getQuery(TaskHistoryEvent.CLASS);
//            query.include(TaskHistoryEvent.IDENTITY);
//            final TaskHistoryEvent event = query.get(eventId);
//            if (isNew) {
//                event.pin(TaskHistoryEvent.PIN_LABEL);
//            }
//        } catch (ParseException e) {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public Single<String> pushTaskReminder(@NonNull String taskId) {
//        final Map<String, Object> params = new HashMap<>();
//        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK_ID, taskId);
//        return callFunctionInBackground(PUSH_TASK_REMIND, params);
//    }
}
