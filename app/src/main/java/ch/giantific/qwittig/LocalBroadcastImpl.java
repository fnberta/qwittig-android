/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides easy to use methods for sending local broadcasts messages.
 *
 * @see LocalBroadcastImpl
 */
public class LocalBroadcastImpl implements LocalBroadcast {

    public static final int DATA_TYPE_ALL = 1;
    public static final int DATA_TYPE_PURCHASES_UPDATED = 2;
    public static final int DATA_TYPE_USERS_UPDATED = 3;
    public static final int DATA_TYPE_COMPENSATIONS_UPDATED = 4;
    public static final int DATA_TYPE_GROUP_UPDATED = 5;
    public static final int DATA_TYPE_TASKS_UPDATED = 6;
    public static final String INTENT_FILTER_DATA_NEW = "ch.giantific.qwittig.intents.DATA_NEW";
    public static final String INTENT_DATA_TYPE = "ch.giantific.qwittig.intents.DATA_TYPE";
    public static final String INTENT_EXTRA_COMPENSATION_PAID = "ch.giantific.qwittig.intents.COMPENSATION_PAID";
    private Context mContext;

    public LocalBroadcastImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void sendPurchasesUpdated() {
        Intent intent = getIntent(DATA_TYPE_PURCHASES_UPDATED);
        send(intent);
    }

    @Override
    public void sendUsersUpdated() {
        Intent intent = getIntent(DATA_TYPE_USERS_UPDATED);
        send(intent);
    }

    @Override
    public void sendGroupUpdated() {
        Intent intent = getIntent(DATA_TYPE_GROUP_UPDATED);
        send(intent);
    }

    @Override
    public void sendTasksUpdated() {
        Intent intent = getIntent(DATA_TYPE_TASKS_UPDATED);
        send(intent);
    }

    @Override
    public void sendCompensationsUpdated(boolean isPaid) {
        Intent intent = getIntent(DATA_TYPE_COMPENSATIONS_UPDATED);
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

    @IntDef({DATA_TYPE_ALL, DATA_TYPE_PURCHASES_UPDATED, DATA_TYPE_USERS_UPDATED, DATA_TYPE_COMPENSATIONS_UPDATED,
            DATA_TYPE_GROUP_UPDATED, DATA_TYPE_TASKS_UPDATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {
    }
}
