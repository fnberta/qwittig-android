/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig;

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
}
