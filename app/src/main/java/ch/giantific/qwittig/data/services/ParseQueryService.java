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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.di.components.DaggerQueryServiceComponent;
import ch.giantific.qwittig.di.modules.LocalBroadcastModule;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

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
    private static final String ACTION_QUERY_IDENTITIES = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_IDENTITIES";
    private static final String ACTION_QUERY_COMPS = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_COMPS";
    private static final String ACTION_QUERY_ALL = BuildConfig.APPLICATION_ID + ".data.services.action.QUERY_ALL";
    private static final String ACTION_QUERY_TASK_DONE = BuildConfig.APPLICATION_ID + ".data.services.action.TASK_DONE";
    private static final String EXTRA_OBJECT_CLASS = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_CLASS";
    private static final String EXTRA_OBJECT_ID = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_ID";
    private static final String EXTRA_OBJECT_IS_NEW = BuildConfig.APPLICATION_ID + ".data.services.extra.OBJECT_IS_NEW";
    private static final String EXTRA_OBJECT_GROUP_ID = BuildConfig.APPLICATION_ID + ".data.services.extra.GROUP_ID";
    @Inject
    LocalBroadcast mLocalBroadcast;
    @Inject
    UserRepository mUserRepo;
    @Inject
    PurchaseRepository mPurchaseRepo;
    @Inject
    CompensationRepository mCompsRepo;
    @Inject
    TaskRepository mTasksRepo;
    @Inject
    GroupRepository mGroupRepo;
    @Inject
    IdentityRepository mIdentityRepo;
    private Identity mCurrentIdentity;
    private List<Identity> mIdentities;

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
    public static void startQueryIdentities(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_IDENTITIES);
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

        injectDependencies();

        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        mCurrentIdentity = currentUser.getCurrentIdentity();
        mIdentities = currentUser.getIdentities();

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
            case ACTION_QUERY_IDENTITIES: {
                queryIdentities();
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

    private void injectDependencies() {
        DaggerQueryServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .localBroadcastModule(new LocalBroadcastModule(this))
                .build()
                .inject(this);
    }

    private void unpinObject(@NonNull String className, @NonNull String objectId,
                             @NonNull String groupId) {
        switch (className) {
            case Purchase.CLASS: {
                if (mPurchaseRepo.removePurchaseLocal(objectId, groupId)) {
                    mLocalBroadcast.sendPurchasesUpdated();
                }
                break;
            }
            case Compensation.CLASS: {
                if (mCompsRepo.removeCompensationLocal(objectId)) {
                    mLocalBroadcast.sendCompensationsUpdated(false);
                }
                break;
            }
            case Task.CLASS: {
                if (mTasksRepo.removeTaskLocal(objectId)) {
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
        if (isNew && mPurchaseRepo.isAlreadySavedLocal(purchaseId)) {
            return;
        }

        if (mPurchaseRepo.updatePurchase(purchaseId, isNew)) {
            mLocalBroadcast.sendPurchasesUpdated();
        }
    }

    private void queryCompensation(@NonNull String compensationId, boolean isNew) {
        if (isNew && mCompsRepo.isAlreadySavedLocal(compensationId)) {
            return;
        }

        final Boolean isPaid = mCompsRepo.updateCompensation(compensationId, isNew);
        if (isPaid != null) {
            mLocalBroadcast.sendCompensationsUpdated(isPaid);
        }
    }

    private void queryTask(@NonNull String taskId, boolean isNew) {
        if (isNew && mTasksRepo.isAlreadySavedLocal(taskId)) {
            return;
        }

        if (mTasksRepo.updateTask(taskId, isNew)) {
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    private void setTaskDone(@NonNull String taskId) {
        final Task task = mTasksRepo.fetchTaskDataLocal(taskId);
        if (task != null) {
            task.addHistoryEvent(mCurrentIdentity);
            task.saveEventually();
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    private void queryGroup(@NonNull String groupId, boolean isNew) {
        if (isNew && mGroupRepo.isAlreadySavedLocal(groupId)) {
            return;
        }

        final Group group = mGroupRepo.getGroupOnline(groupId);
        if (group != null) {
            mLocalBroadcast.sendGroupUpdated();
        }
    }

    private void queryAll() {
        queryPurchases();
        queryIdentities();
        queryCompensations();
        queryTasks();
    }

    private void queryPurchases() {
        if (mPurchaseRepo.updatePurchases(mIdentities, mCurrentIdentity)) {
            mLocalBroadcast.sendPurchasesUpdated();
        }
    }

    private void queryIdentities() {
        if (mIdentityRepo.updateIdentities(mIdentities)) {
            mLocalBroadcast.sendUsersUpdated();
        }
    }

    private void queryCompensations() {
        if (mCompsRepo.updateCompensations(mIdentities)) {
            mLocalBroadcast.sendCompensationsUpdated(false);
            mLocalBroadcast.sendCompensationsUpdated(true);
        }
    }

    private void queryTasks() {
        if (mTasksRepo.updateTasks(mIdentities)) {
            mLocalBroadcast.sendTasksUpdated();
        }
    }

    @StringDef({User.CLASS, Group.CLASS, Purchase.CLASS, Compensation.CLASS, Group.CLASS, Task.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClassType {
    }
}
