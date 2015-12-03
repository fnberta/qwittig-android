/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides easy to use methods for sending local broadcasts messages.
 *
 * @see LocalBroadcast
 */
public class LocalBroadcast {

    @IntDef({DATA_TYPE_ALL, DATA_TYPE_PURCHASES_UPDATED, DATA_TYPE_USERS_UPDATED, DATA_TYPE_COMPENSATIONS_UPDATED,
            DATA_TYPE_GROUP_UPDATED, DATA_TYPE_TASKS_UPDATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {}
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
    public LocalBroadcast(Context context) {
        mContext = context;
    }

    /**
     * Sends local broadcast that purchases have been updated;
     */
    public void sendLocalBroadcastPurchasesUpdated() {
        Intent intent = getIntent(DATA_TYPE_PURCHASES_UPDATED);
        sendLocalBroadcast(intent);
    }

    /**
     * Sends local broadcast that users have been updated;
     */
    public void sendLocalBroadcastUsersUpdated() {
        Intent intent = getIntent(DATA_TYPE_USERS_UPDATED);
        sendLocalBroadcast(intent);
    }

    /**
     * Sends local broadcast that a group object has been updated;
     */
    public void sendLocalBroadcastGroupUpdated() {
        Intent intent = getIntent(DATA_TYPE_GROUP_UPDATED);
        sendLocalBroadcast(intent);
    }

    /**
     * Sends local broadcast that tasks have been updated;
     */
    public void sendLocalBroadcastTasksUpdated() {
        Intent intent = getIntent(DATA_TYPE_TASKS_UPDATED);
        sendLocalBroadcast(intent);
    }

    /**
     * Sends local broadcast that compensations have been updated;
     *
     * @param isPaid whether the compensations are paid or not
     */
    public void sendLocalBroadcastCompensationsUpdated(boolean isPaid) {
        Intent intent = getIntent(DATA_TYPE_COMPENSATIONS_UPDATED);
        intent.putExtra(INTENT_EXTRA_COMPENSATION_PAID, isPaid);
        sendLocalBroadcast(intent);
    }

    private Intent getIntent(@DataType int dataType) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, dataType);
        return intent;
    }

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
