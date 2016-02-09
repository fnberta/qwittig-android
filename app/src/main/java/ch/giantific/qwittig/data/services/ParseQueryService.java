/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParseIdentityRepository;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;

/**
 * Handles the various background tasks to query, pin or unpin objects.
 * <p/>
 * Subclass of {@link IntentService}, used for handling asynchronous task requests in a service on
 * a separate handler thread.
 */
public class ParseQueryService extends IntentService {

    private static final String SERVICE_NAME = "ParseQueryService";
    private static final String ACTION_UNPIN_OBJECT = BuildConfig.APPLICATION_ID + ".data.services.action.UNPIN_OBJECT";
    private static final String ACTION_QUERY_OBJECT = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_OBJECT";
    private static final String ACTION_QUERY_USERS = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_USERS";
    private static final String ACTION_QUERY_COMPS = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_COMPS";
    private static final String ACTION_QUERY_ALL = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_ALL";
    private static final String ACTION_QUERY_TASK_DONE = BuildConfig.APPLICATION_ID + ".data.services.action.TASK_DONE";
    private static final String EXTRA_OBJECT_CLASS = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_CLASS";
    private static final String EXTRA_OBJECT_ID = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_ID";
    private static final String EXTRA_OBJECT_IS_NEW = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_IS_NEW";
    private static final String EXTRA_OBJECT_GROUP_ID = BuildConfig.APPLICATION_ID + ".data.services.extra.GROUP_ID";
    private User mCurrentUser;
    private Identity mCurrentIdentity;
    private List<ParseObject> mCurrentUserGroups = new ArrayList<>();
    private LocalBroadcast mLocalBroadcast;

    /**
     * Constructs a new {@link ParseQueryService}.
     */
    public ParseQueryService() {
        super(SERVICE_NAME);
    }

    /**
     * Starts this service to unpin a specific ParseObject.
     *
     * @param context   the context to use to start the service
     * @param className the class of the object to unpin
     * @param objectId  the id of the object to unpin
     * @see IntentService
     */
    public static void startUnpinObject(@NonNull Context context,
                                        @NonNull @ClassType String className,
                                        @NonNull String objectId) {
        startUnpinObject(context, className, objectId, "");
    }

    /**
     * Starts this service to unpin a specific ParseObject.
     *
     * @param context   the context to use to start the service
     * @param className the class of the object to unpin
     * @param objectId  the id of the object to unpin
     * @param groupId   the group id used to construct the correct pin label
     * @see IntentService
     */
    public static void startUnpinObject(@NonNull Context context,
                                        @NonNull @ClassType String className,
                                        @NonNull String objectId,
                                        @NonNull String groupId) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UNPIN_OBJECT);
        intent.putExtra(EXTRA_OBJECT_CLASS, className);
        intent.putExtra(EXTRA_OBJECT_ID, objectId);
        intent.putExtra(EXTRA_OBJECT_GROUP_ID, groupId);
        context.startService(intent);
    }

    /**
     * Starts this service to query a specific ParseObject.
     *
     * @param context   the context to use to start the service
     * @param className the class of the object to query
     * @param objectId  the id of the object to query
     * @param isNew     whether the object was already queried once
     * @see IntentService
     */
    public static void startQueryObject(@NonNull Context context,
                                        @NonNull @ClassType String className,
                                        @NonNull String objectId,
                                        boolean isNew) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_OBJECT);
        intent.putExtra(EXTRA_OBJECT_CLASS, className);
        intent.putExtra(EXTRA_OBJECT_ID, objectId);
        intent.putExtra(EXTRA_OBJECT_IS_NEW, isNew);
        context.startService(intent);
    }

    /**
     * Starts this service to query all users.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startQueryUsers(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_USERS);
        context.startService(intent);
    }

    /**
     * Starts this service to query all compensations.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startQueryCompensations(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_COMPS);
        context.startService(intent);
    }

    /**
     * Starts this service to query all users, purchases, compensations and tasks.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startQueryAll(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_ALL);
        context.startService(intent);
    }

    /**
     * Starts this service to query a task and rotate the users involved.
     *
     * @param context the context to use to start the service
     * @param taskId  the object id of the task to query
     * @see IntentService
     */
    public static void startTaskDone(@NonNull Context context, @NonNull String taskId) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_TASK_DONE);
        intent.putExtra(EXTRA_OBJECT_ID, taskId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        mCurrentUser = (User) ParseUser.getCurrentUser();
        mCurrentIdentity = mCurrentUser.getCurrentIdentity();
        final List<ParseObject> identities = mCurrentUser.getIdentities();
        for (ParseObject parseObject : identities) {
            final Identity identity = (Identity) parseObject;
            mCurrentUserGroups.add(identity.getGroup());
        }
        mLocalBroadcast = new LocalBroadcastImpl(this);

        final String action = intent.getAction();
        switch (action) {
            case ACTION_UNPIN_OBJECT: {
                final String className = intent.getStringExtra(EXTRA_OBJECT_CLASS);
                final String objectId = intent.getStringExtra(EXTRA_OBJECT_ID);
                final String groupId = intent.getStringExtra(EXTRA_OBJECT_GROUP_ID);
                unpinObject(className, objectId, groupId);
                break;
            }
            case ACTION_QUERY_OBJECT: {
                final String className = intent.getStringExtra(EXTRA_OBJECT_CLASS);
                final String objectId = intent.getStringExtra(EXTRA_OBJECT_ID);
                final boolean isNew = intent.getBooleanExtra(EXTRA_OBJECT_IS_NEW, false);
                queryObject(className, objectId, isNew);
                break;
            }
            case ACTION_QUERY_USERS: {
                queryUsers();
                break;
            }
            case ACTION_QUERY_COMPS: {
                queryCompensations();
                break;
            }
            case ACTION_QUERY_ALL: {
                queryAll();
                break;
            }
            case ACTION_QUERY_TASK_DONE: {
                final String taskId = intent.getStringExtra(EXTRA_OBJECT_ID);
                setTaskDone(taskId);
                break;
            }
        }
    }

    private void unpinObject(@NonNull String className, @NonNull String objectId,
                             @NonNull String groupId) {
        switch (className) {
            case Purchase.CLASS: {
                PurchaseRepository repo = new ParsePurchaseRepository();
                if (repo.removePurchaseLocal(objectId, groupId)) {
                    mLocalBroadcast.sendPurchasesUpdated();
                }
                break;
            }
            case Compensation.CLASS: {
                CompensationRepository repo = new ParseCompensationRepository();
                if (repo.removeCompensationLocal(objectId)) {
                    mLocalBroadcast.sendCompensationsUpdated(false);
                }
                break;
            }
            case Task.CLASS: {
                TaskRepository repo = new ParseTaskRepository();
                if (repo.removeTaskLocal(objectId)) {
                    mLocalBroadcast.sendTasksUpdated();
                }
                break;
            }
        }
    }

    private void queryObject(@NonNull String className, @NonNull String objectId, boolean isNew) {
        switch (className) {
            case Purchase.CLASS:
                queryPurchase(objectId, isNew);
                break;
            case Compensation.CLASS:
                queryCompensation(objectId, isNew);
                break;
            case Task.CLASS:
                queryTask(objectId, isNew);
                break;
            case Group.CLASS:
                queryGroup(objectId, isNew);
                break;
        }
    }

    private void queryPurchase(@NonNull String purchaseId, boolean isNew) {
        PurchaseRepository repo = new ParsePurchaseRepository();
        if (isNew && repo.isAlreadySavedLocal(purchaseId)) {
            return;
        }

        if (repo.updatePurchase(purchaseId, isNew)) {
            mLocalBroadcast.sendPurchasesUpdated();
        }
    }

    private void queryCompensation(@NonNull String compensationId, boolean isNew) {
        CompensationRepository repo = new ParseCompensationRepository();
        if (isNew && repo.isAlreadySavedLocal(compensationId)) {
            return;
        }

        Boolean isPaid = repo.updateCompensation(compensationId, isNew);
        if (isPaid != null) {
            mLocalBroadcast.sendCompensationsUpdated(isPaid);
        }
    }

    private void queryTask(@NonNull String taskId, boolean isNew) {
        TaskRepository repo = new ParseTaskRepository();
        if (isNew && repo.isAlreadySavedLocal(taskId)) {
            return;
        }

        if (repo.updateTask(taskId, isNew)) {
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    private void setTaskDone(@NonNull String taskId) {
        TaskRepository repo = new ParseTaskRepository();
        Task task = repo.fetchTaskDataLocal(taskId);
        if (task != null) {
            task.addHistoryEvent(mCurrentIdentity);
            task.saveEventually();
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    private void queryGroup(@NonNull String groupId, boolean isNew) {
        GroupRepository repo = new ParseGroupRepository();
        if (isNew && repo.isAlreadySavedLocal(groupId)) {
            return;
        }

        Group group = repo.getGroupOnline(groupId);
        if (group != null) {
            mLocalBroadcast.sendGroupUpdated();
        }
    }

    private void queryAll() {
        queryPurchases();
        queryUsers();
        queryCompensations();
        queryTasks();
    }

    private void queryPurchases() {
        PurchaseRepository repo = new ParsePurchaseRepository();
        if (repo.updatePurchases(mCurrentUser)) {
            mLocalBroadcast.sendPurchasesUpdated();
        }
    }

    private void queryUsers() {
        IdentityRepository repo = new ParseIdentityRepository();
        if (repo.updateIdentities(mCurrentUserGroups)) {
            mLocalBroadcast.sendUsersUpdated();
        }
    }

    private void queryCompensations() {
        CompensationRepository repo = new ParseCompensationRepository();
        if (repo.updateCompensations(mCurrentUser)) {
            mLocalBroadcast.sendCompensationsUpdated(false);
            mLocalBroadcast.sendCompensationsUpdated(true);
        }
    }

    private void queryTasks() {
        TaskRepository repo = new ParseTaskRepository();
        if (repo.updateTasks(mCurrentUser)) {
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    @StringDef({User.CLASS, Group.CLASS, Purchase.CLASS, Compensation.CLASS, Group.CLASS, Task.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClassType {
    }
}
