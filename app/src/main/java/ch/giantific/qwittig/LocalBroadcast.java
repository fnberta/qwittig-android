/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 18.01.16.
 */
public interface LocalBroadcast {
    /**
     * Sends local broadcast that purchases have been updated;
     */
    void sendPurchasesUpdated();

    /**
     * Sends local broadcast that users have been updated;
     */
    void sendUsersUpdated();

    /**
     * Sends local broadcast that a group object has been updated;
     */
    void sendGroupUpdated();

    /**
     * Sends local broadcast that tasks have been updated;
     */
    void sendTasksUpdated();

    /**
     * Sends local broadcast that compensations have been updated;
     *
     * @param isPaid whether the compensations are paid or not
     */
    void sendCompensationsUpdated(boolean isPaid);

    @IntDef({DataType.ALL, DataType.PURCHASES_UPDATED, DataType.IDENTITIES_UPDATED,
            DataType.COMPENSATIONS_UPDATED, DataType.GROUP_UPDATED, DataType.TASKS_UPDATED})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
        int ALL = 1;
        int PURCHASES_UPDATED = 2;
        int IDENTITIES_UPDATED = 3;
        int COMPENSATIONS_UPDATED = 4;
        int GROUP_UPDATED = 5;
        int TASKS_UPDATED = 6;
    }
}
