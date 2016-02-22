/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.bus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import ch.giantific.qwittig.BuildConfig;

/**
 * Provides an implementation of {@link LocalBroadcast} using the Android
 * {@link LocalBroadcastManager}.
 */
public class LocalBroadcastImpl implements LocalBroadcast {

    public static final String INTENT_FILTER_DATA_NEW = BuildConfig.APPLICATION_ID + ".intents.DATA_NEW";
    public static final String INTENT_DATA_TYPE = BuildConfig.APPLICATION_ID + ".intents.DATA_TYPE";
    public static final String INTENT_EXTRA_COMPENSATION_PAID = BuildConfig.APPLICATION_ID + ".intents.COMPENSATION_PAID";
    private final LocalBroadcastManager mBroadcastManager;

    public LocalBroadcastImpl(@NonNull LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
    }

    @Override
    public void sendPurchasesUpdated() {
        final Intent intent = getIntent(DataType.PURCHASES_UPDATED);
        send(intent);
    }

    @Override
    public void sendUsersUpdated() {
        final Intent intent = getIntent(DataType.IDENTITIES_UPDATED);
        send(intent);
    }

    @Override
    public void sendGroupUpdated() {
        final Intent intent = getIntent(DataType.GROUP_UPDATED);
        send(intent);
    }

    @Override
    public void sendTasksUpdated() {
        final Intent intent = getIntent(DataType.TASKS_UPDATED);
        send(intent);
    }

    @Override
    public void sendCompensationsUpdated(boolean isPaid) {
        final Intent intent = getIntent(DataType.COMPENSATIONS_UPDATED);
        intent.putExtra(INTENT_EXTRA_COMPENSATION_PAID, isPaid);
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
}
