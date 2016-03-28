/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.services;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.services.di.DaggerServiceComponent;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Uploads a locally saved purchase to the online data store. Tries until it succeeds, even across
 * device reboots.
 *
 * @see {@link GcmNetworkManager}
 */
public class SavePurchaseTaskService extends GcmTaskService {

    private static final String SERVICE_TASK = SavePurchaseTaskService.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_PURCHASE_ID = "PURCHASE_ID";
    private static final String KEY_DRAFT = "DRAFT";
    private static final String KEY_DELETE_OLD_RECEIPT = "DELETE_OLD_RECEIPT";
    @Inject
    PurchaseRepository mPurchaseRepo;

    public static void scheduleSaveNew(@NonNull GcmNetworkManager networkManager,
                                       @NonNull String tempId) {
        final Bundle extras = new Bundle();
        extras.putInt(KEY_TYPE, Type.NEW);
        extras.putString(KEY_PURCHASE_ID, tempId);
        startService(networkManager, extras);
    }

    public static void scheduleSaveEdit(@NonNull GcmNetworkManager networkManager,
                                        @NonNull String purchaseId, boolean wasDraft,
                                        boolean deleteOldReceipt) {
        final Bundle extras = new Bundle();
        extras.putInt(KEY_TYPE, Type.EDIT);
        extras.putString(KEY_PURCHASE_ID, purchaseId);
        extras.putBoolean(KEY_DRAFT, wasDraft);
        extras.putBoolean(KEY_DELETE_OLD_RECEIPT, deleteOldReceipt);
        startService(networkManager, extras);
    }

    private static void startService(@NonNull GcmNetworkManager networkManager,
                                     @NonNull Bundle extras) {
        final OneoffTask task = new OneoffTask.Builder()
                .setService(SavePurchaseTaskService.class)
                .setTag(SERVICE_TASK)
                .setExecutionWindow(0L, 5L)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setExtras(extras)
                .build();

        networkManager.schedule(task);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        injectDependencies();

        final Bundle extras = taskParams.getExtras();
        final String purchaseId = extras.getString(KEY_PURCHASE_ID);
        if (!TextUtils.isEmpty(purchaseId)) {
            final int type = extras.getInt(KEY_TYPE, 0);
            switch (type) {
                case Type.NEW: {
                    return mPurchaseRepo.uploadPurchase(purchaseId)
                            ? GcmNetworkManager.RESULT_SUCCESS
                            : GcmNetworkManager.RESULT_RESCHEDULE;
                }
                case Type.EDIT: {
                    final boolean wasDraft = extras.getBoolean(KEY_DRAFT);
                    final boolean deleteOldReceipt = extras.getBoolean(KEY_DELETE_OLD_RECEIPT);

                    return mPurchaseRepo.uploadPurchaseEdit(purchaseId, wasDraft, deleteOldReceipt)
                            ? GcmNetworkManager.RESULT_SUCCESS
                            : GcmNetworkManager.RESULT_RESCHEDULE;
                }
            }
        }

        return GcmNetworkManager.RESULT_FAILURE;
    }

    private void injectDependencies() {
        DaggerServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }

    @IntDef({Type.NEW, Type.EDIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int NEW = 1;
        int EDIT = 2;
    }
}
