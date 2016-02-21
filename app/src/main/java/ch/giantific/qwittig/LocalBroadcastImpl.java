/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Provides easy to use methods for sending local broadcasts messages.
 *
 * @see LocalBroadcastImpl
 */
public class LocalBroadcastImpl implements LocalBroadcast {

    public static final String INTENT_FILTER_DATA_NEW = BuildConfig.APPLICATION_ID + ".intents.DATA_NEW";
    public static final String INTENT_DATA_TYPE = BuildConfig.APPLICATION_ID + ".intents.DATA_TYPE";
    public static final String INTENT_EXTRA_COMPENSATION_PAID = BuildConfig.APPLICATION_ID + ".intents.COMPENSATION_PAID";
    private Context mContext;

    public LocalBroadcastImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void sendPurchasesUpdated() {
        Intent intent = getIntent(DataType.PURCHASES_UPDATED);
        send(intent);
    }

    @Override
    public void sendUsersUpdated() {
        Intent intent = getIntent(DataType.IDENTITIES_UPDATED);
        send(intent);
    }

    @Override
    public void sendGroupUpdated() {
        Intent intent = getIntent(DataType.GROUP_UPDATED);
        send(intent);
    }

    @Override
    public void sendTasksUpdated() {
        Intent intent = getIntent(DataType.TASKS_UPDATED);
        send(intent);
    }

    @Override
    public void sendCompensationsUpdated(boolean isPaid) {
        Intent intent = getIntent(DataType.COMPENSATIONS_UPDATED);
        intent.putExtra(INTENT_EXTRA_COMPENSATION_PAID, isPaid);
        send(intent);
    }

    private Intent getIntent(@DataType int dataType) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, dataType);
        return intent;
    }

    private void send(@NonNull Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
