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
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.services.di.DaggerServiceComponent;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.OcrPurchase;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
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

    private static final String SERVICE_NAME = ParseQueryService.class.getSimpleName();
    private static final String ACTION_UNPIN_OBJECT = BuildConfig.APPLICATION_ID + ".data.services.action.UNPIN_OBJECT";
    private static final String ACTION_UPDATE_OBJECT = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_OBJECT";
    private static final String ACTION_UPDATE_IDENTITIES = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_IDENTITIES";
    private static final String ACTION_UPDATE_PURCHASES = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_PURCHASES";
    private static final String ACTION_UPDATE_COMPENSATIONS = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_COMPENSATIONS";
    private static final String ACTION_UPDATE_COMPENSATIONS_PAID = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_COMPENSATIONS_PAID";
    private static final String ACTION_UPDATE_COMPENSATIONS_UNPAID = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_COMPENSATIONS_UNPAID";
    private static final String ACTION_UPDATE_TASKS = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_TASKS";
    private static final String ACTION_UPDATE_ALL = BuildConfig.APPLICATION_ID + ".data.services.action.UPDATE_ALL";
    private static final String ACTION_SET_TASK_DONE = BuildConfig.APPLICATION_ID + ".data.services.action.SET_TASK_DONE";
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
    public static void startUpdateObject(@NonNull Context context,
                                         @NonNull @ClassType String className,
                                         @NonNull String objectId,
                                         boolean isNew) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_OBJECT);
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
    public static void startUpdateIdentities(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_IDENTITIES);
        context.startService(intent);
    }

    /**
     * Starts this service to query all purchases.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdatePurchases(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_PURCHASES);
        context.startService(intent);
    }

    /**
     * Starts this service to query all compensations.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdateCompensations(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_COMPENSATIONS);
        context.startService(intent);
    }

    /**
     * Starts this service to query all paid compensations.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdateCompensationsPaid(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_COMPENSATIONS_PAID);
        context.startService(intent);
    }

    /**
     * Starts this service to query all unpaidcompensations.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdateCompensationsUnpaid(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_COMPENSATIONS_UNPAID);
        context.startService(intent);
    }

    /**
     * Starts this service to query all tasks.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdateTasks(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_TASKS);
        context.startService(intent);
    }

    /**
     * Starts this service to query all users, purchases, compensations and tasks.
     *
     * @param context the context to use to start the service
     * @see IntentService
     */
    public static void startUpdateAll(@NonNull Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UPDATE_ALL);
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
        intent.setAction(ACTION_SET_TASK_DONE);
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
            case ACTION_UPDATE_OBJECT: {
                final String className = intent.getStringExtra(EXTRA_OBJECT_CLASS);
                final String objectId = intent.getStringExtra(EXTRA_OBJECT_ID);
                final boolean isNew = intent.getBooleanExtra(EXTRA_OBJECT_IS_NEW, false);
                updateObject(className, objectId, isNew);
                break;
            }
            case ACTION_UPDATE_IDENTITIES: {
                updateIdentities();
                break;
            }
            case ACTION_UPDATE_PURCHASES: {
                updatePurchases();
                break;
            }
            case ACTION_UPDATE_COMPENSATIONS: {
                updateCompensationsPaid();
                updateCompensationsUnpaid();
                break;
            }
            case ACTION_UPDATE_COMPENSATIONS_PAID: {
                updateCompensationsPaid();
                break;
            }
            case ACTION_UPDATE_COMPENSATIONS_UNPAID: {
                updateCompensationsUnpaid();
                break;
            }
            case ACTION_UPDATE_TASKS: {
                updateTasks();
                break;
            }
            case ACTION_UPDATE_ALL: {
                updatePurchases();
                updateIdentities();
                updateCompensationsPaid();
                updateCompensationsUnpaid();
                updateTasks();
                break;
            }
            case ACTION_SET_TASK_DONE: {
                final String taskId = intent.getStringExtra(EXTRA_OBJECT_ID);
                setTaskDone(taskId);
                break;
            }
        }
    }

    private void injectDependencies() {
        DaggerServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }

    private void unpinObject(@NonNull String className, @NonNull String objectId,
                             @NonNull String groupId) {
        switch (className) {
            case Purchase.CLASS: {
                final boolean successful = mPurchaseRepo.deletePurchaseLocal(objectId, groupId);
                mLocalBroadcast.sendPurchasesUpdated(successful);
                break;
            }
            case Compensation.CLASS: {
                final boolean successful = mCompsRepo.removeCompensationLocal(objectId);
                mLocalBroadcast.sendCompensationsUpdated(successful, false);
                break;
            }
            case Task.CLASS: {
                final boolean successful = mTasksRepo.removeTaskLocal(objectId);
                mLocalBroadcast.sendTasksUpdated(successful);
                break;
            }
        }
    }

    private void updateObject(@NonNull String className, @NonNull String objectId, boolean isNew) {
        switch (className) {
            case Purchase.CLASS:
                updatePurchase(objectId, isNew);
                break;
            case Compensation.CLASS:
                updateCompensation(objectId, isNew);
                break;
            case Task.CLASS:
                updateTask(objectId, isNew);
                break;
            case TaskHistoryEvent.CLASS:
                updateTaskHistoryEvent(objectId, isNew);
                break;
            case Group.CLASS:
                updateGroup(objectId, isNew);
                break;
            case OcrPurchase.CLASS: {
                updateOcrPurchase(objectId);
                break;
            }
        }
    }

    private void updatePurchase(@NonNull String purchaseId, boolean isNew) {
        if (isNew && mPurchaseRepo.isAlreadySavedLocal(purchaseId)) {
            return;
        }

        final boolean successful = mPurchaseRepo.updatePurchase(purchaseId, isNew);
        mLocalBroadcast.sendPurchasesUpdated(successful);
    }

    private void updateCompensation(@NonNull String compensationId, boolean isNew) {
        if (isNew && mCompsRepo.isAlreadySavedLocal(compensationId)) {
            return;
        }

        final Boolean isPaid = mCompsRepo.updateCompensation(compensationId, isNew);
        if (isPaid != null) {
            mLocalBroadcast.sendCompensationsUpdated(true, isPaid);
        }

        // TODO: no broadcast is sent if update failed, probably won't matter
    }

    private void updateTask(@NonNull String taskId, boolean isNew) {
        if (isNew && mTasksRepo.isAlreadySavedLocal(taskId)) {
            return;
        }

        final boolean successful = mTasksRepo.updateTask(taskId, isNew);
        mLocalBroadcast.sendTasksUpdated(successful);
    }

    private void updateTaskHistoryEvent(@NonNull String eventId, boolean isNew) {
        // TODO: check if new and already queried, if yes return immediately

        final boolean successful = mTasksRepo.updateTaskHistoryEvent(eventId, isNew);
        mLocalBroadcast.sendTasksUpdated(successful);
    }

    private void updateGroup(@NonNull String groupId, boolean isNew) {
        if (isNew && mGroupRepo.isAlreadySavedLocal(groupId)) {
            return;
        }

        final Group group = mGroupRepo.queryGroup(groupId);
        if (group != null) {
            mLocalBroadcast.sendGroupUpdated();
        }
    }

    private void updateOcrPurchase(@NonNull String ocrPurchaseId) {
        final boolean successful = mPurchaseRepo.updateOcrPurchase(ocrPurchaseId);
        mLocalBroadcast.sendOcrPurchaseUpdated(successful, ocrPurchaseId);
    }

    private void updatePurchases() {
        final boolean successful = mPurchaseRepo.updatePurchases(mIdentities, mCurrentIdentity);
        mLocalBroadcast.sendPurchasesUpdated(successful);
    }

    private void updateIdentities() {
        final boolean successful = mUserRepo.updateIdentities(mIdentities);
        mLocalBroadcast.sendIdentitiesUpdated(successful);
    }

    private void updateCompensationsPaid() {
        final boolean successful = mCompsRepo.updateCompensationsPaid(mIdentities);
        mLocalBroadcast.sendCompensationsUpdated(successful, true);
    }

    private void updateCompensationsUnpaid() {
        final boolean successful = mCompsRepo.updateCompensationsUnpaid(mIdentities);
        mLocalBroadcast.sendCompensationsUpdated(successful, false);
    }

    private void updateTasks() {
        final boolean successful = mTasksRepo.updateTasks(mIdentities);
        mLocalBroadcast.sendTasksUpdated(successful);
    }

    private void setTaskDone(@NonNull String taskId) {
        final boolean successful = mTasksRepo.setTaskDone(taskId, mCurrentIdentity);
        mLocalBroadcast.sendTasksUpdated(successful);
    }

    @StringDef({User.CLASS, Group.CLASS, Purchase.CLASS, Compensation.CLASS, Group.CLASS,
            Task.CLASS, TaskHistoryEvent.CLASS, OcrPurchase.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClassType {
    }
}
