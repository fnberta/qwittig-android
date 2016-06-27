/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.bus;

import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.BuildConfig;

/**
 * Defines the methods to send local messages for various events. using the Android
 * {@link LocalBroadcastManager}.
 */
public class LocalBroadcast {

    public static final String INTENT_FILTER_DATA_NEW = BuildConfig.APPLICATION_ID + ".intents.DATA_NEW";
    public static final String INTENT_DATA_TYPE = BuildConfig.APPLICATION_ID + ".intents.DATA_TYPE";
    public static final String INTENT_EXTRA_SUCCESSFUL = BuildConfig.APPLICATION_ID + ".intents.SUCCESSFUL";
    public static final String INTENT_EXTRA_COMPENSATION_PAID = BuildConfig.APPLICATION_ID + ".intents.COMPENSATION_PAID";
    public static final String INTENT_EXTRA_OCR_PURCHASE_ID = BuildConfig.APPLICATION_ID + ".intents.OCR_PURCHASE_ID";
    private final LocalBroadcastManager mBroadcastManager;

    @Inject
    public LocalBroadcast(@NonNull LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
    }

    /**
     * Sends local broadcast that purchases have been updated;
     *
     * @param successful whether the update was successful
     */
    public void sendPurchasesUpdated(boolean successful) {
        final Intent intent = getIntent(DataType.PURCHASES_UPDATED);
        intent.putExtra(INTENT_EXTRA_SUCCESSFUL, successful);
        send(intent);
    }

    /**
     * Sends local broadcast that users have been updated;
     *
     * @param successful whether the update was successful
     */
    public void sendIdentitiesUpdated(boolean successful) {
        final Intent intent = getIntent(DataType.IDENTITIES_UPDATED);
        intent.putExtra(INTENT_EXTRA_SUCCESSFUL, successful);
        send(intent);
    }

    /**
     * Sends local broadcast that a group object has been updated;
     */
    public void sendGroupUpdated() {
        final Intent intent = getIntent(DataType.GROUP_UPDATED);
        send(intent);
    }

    /**
     * Sends local broadcast that tasks have been updated;
     *
     * @param successful whether the update was successful
     */
    public void sendTasksUpdated(boolean successful) {
        final Intent intent = getIntent(DataType.TASKS_UPDATED);
        intent.putExtra(INTENT_EXTRA_SUCCESSFUL, successful);
        send(intent);
    }

    /**
     * Sends local broadcast that compensations have been updated;
     *
     * @param successful whether the update was successful
     * @param isPaid     whether the compensations are paid or not
     */
    public void sendCompensationsUpdated(boolean successful, boolean isPaid) {
        final Intent intent = getIntent(DataType.COMPENSATIONS_UPDATED);
        intent.putExtra(INTENT_EXTRA_COMPENSATION_PAID, isPaid);
        intent.putExtra(INTENT_EXTRA_SUCCESSFUL, successful);
        send(intent);
    }

    public void sendOcrDataUpdated(boolean successful, @Nullable String ocrPurchaseId) {
        final Intent intent = getIntent(DataType.OCR_PURCHASE_UPDATED);
        intent.putExtra(INTENT_EXTRA_SUCCESSFUL, successful);
        if (!TextUtils.isEmpty(ocrPurchaseId)) {
            intent.putExtra(INTENT_EXTRA_OCR_PURCHASE_ID, ocrPurchaseId);
        }
        send(intent);
    }

    private Intent getIntent(@DataType int dataType) {
        final Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, dataType);
        return intent;
    }

    private void send(@NonNull Intent intent) {
        mBroadcastManager.sendBroadcast(intent);
    }

    @IntDef({DataType.ALL, DataType.PURCHASES_UPDATED, DataType.IDENTITIES_UPDATED,
            DataType.COMPENSATIONS_UPDATED, DataType.GROUP_UPDATED, DataType.TASKS_UPDATED,
            DataType.OCR_PURCHASE_UPDATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {
        int ALL = 1;
        int PURCHASES_UPDATED = 2;
        int IDENTITIES_UPDATED = 3;
        int COMPENSATIONS_UPDATED = 4;
        int GROUP_UPDATED = 5;
        int TASKS_UPDATED = 6;
        int OCR_PURCHASE_UPDATED = 7;
    }
}
